package com.sourcegraph.scip_java.commands

import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import java.util.stream.Stream

import scala.collection.immutable.SortedSet
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters._

import com.google.common.graph.EndpointPair
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.MutableNetwork
import com.google.common.graph.NetworkBuilder
import com.google.protobuf.InvalidProtocolBufferException
import com.google.protobuf.util.JsonFormat
import com.sourcegraph.io.AbsolutePath
import com.sourcegraph.io.DeleteVisitor
import com.sourcegraph.lsif_protocol.LsifHover
import com.sourcegraph.lsif_protocol.LsifObject
import com.sourcegraph.lsif_protocol.LsifPosition
import com.sourcegraph.scip_semanticdb.ScipOutputFormat
import com.sourcegraph.semanticdb_javac.Semanticdb
import com.sourcegraph.semanticdb_javac.Semanticdb.Language
import com.sourcegraph.semanticdb_javac.Semanticdb.SymbolInformation
import com.sourcegraph.semanticdb_javac.Semanticdb.SymbolOccurrence
import com.sourcegraph.semanticdb_javac.Semanticdb.SymbolOccurrence.Role
import com.sourcegraph.semanticdb_javac.Semanticdb.TextDocument
import moped.annotations.CommandName
import moped.annotations.Description
import moped.annotations.ExampleValue
import moped.annotations.Inline
import moped.annotations.PositionalArguments
import moped.cli.Application
import moped.cli.Command
import moped.cli.CommandParser
import moped.reporters.Input
import moped.reporters.Position
import moped.reporters.Reporter
import org.scalameta.ascii.layout.prefs.LayoutPrefsImpl

@CommandName("snapshot-lsif")
case class SnapshotLsifCommand(
    @Inline() app: Application = Application.default,
    output: Path = Paths.get("generated"),
    @Description(
      "Space-separated list of '$FILE_EXTENSION,$COMMENT_SYNTAX' that determines what syntax " +
        "to use for comments depending on the file extension of the source file."
    )
    @ExampleValue("py,# scala,// ") commentSyntax: CommentSyntax =
      CommentSyntax.default,
    @PositionalArguments() input: List[Path] = List(Paths.get("dump.lsif"))
) extends Command {

  def sourceroot: Path = AbsolutePath.of(app.env.workingDirectory).normalize()
  private val finalOutput = AbsolutePath.of(output, sourceroot).normalize()

  def run(): Int = {
    Files.walkFileTree(finalOutput, new DeleteVisitor())
    for {
      inputPath <- input
      in = AbsolutePath.of(inputPath, sourceroot)
      if Files.isRegularFile(in) || {
        app.error(s"no such file: $in")
        false
      }
      doc <- SnapshotLsifCommand.parseTextDocument(in, sourceroot, app.reporter)
    } {
      val docPath = AbsolutePath
        .of(Paths.get(doc.getUri), sourceroot)
        .normalize()
      if (docPath.toAbsolutePath.startsWith(sourceroot)) {
        SnapshotCommand.writeSnapshot(doc, finalOutput, commentSyntax)
      } else {
        app.warning(
          s"skipping path '$docPath' because it is not part of the sourceroot '$sourceroot'"
        )
      }
    }
    app.info(finalOutput.toString())
    0
  }

}

object SnapshotLsifCommand {
  private val jsonParser = JsonFormat.parser().ignoringUnknownFields()
  def parseTextDocument(
      input: Path,
      sourceroot: Path,
      reporter: Reporter
  ): List[TextDocument] = {
    parseSemanticdb(input, parseInput(input), sourceroot, reporter)
  }

  def parseSemanticdb(
      input: Path,
      objects: mutable.Buffer[LsifObject],
      sourceroot: Path,
      reporter: Reporter
  ): List[TextDocument] = {
    val scip = new IndexedScip(input, objects, sourceroot, reporter)
    scip
      .ranges
      .iterator
      .filter(o => scip.contains.contains(o.getId))
      .foreach { o =>
        val docId = scip.contains(o.getId)
        val doc = scip.textDocument(docId)
        val isDefinition = scip
          .G
          .predecessors(o)
          .asScala
          .exists(_.getLabel == "definitionResult")
        val isSyntheticDefinition = isDefinition && !scip.next.contains(o.getId)
        val role =
          if (isSyntheticDefinition)
            Role.SYNTHETIC_DEFINITION
          else if (isDefinition)
            Role.DEFINITION
          else
            Role.REFERENCE

        val symbol = scip
          .symbolFromRange(o)
          .orElse(scip.monikerViaDefinition(o))
          .getOrElse {
            val id = scip
              .next
              .getOrElse(o.getId, scip.nextMissingMonikerId(doc.getUri))
            s"localMissingMoniker$id"
          }
        val occ = SymbolOccurrence
          .newBuilder()
          .setRange(
            Semanticdb
              .Range
              .newBuilder()
              .setStartLine(o.getStart.getLine)
              .setStartCharacter(o.getStart.getCharacter)
              .setEndLine(o.getEnd.getLine)
              .setEndCharacter(o.getEnd.getCharacter)
          )
          .setRole(role)
          .setSymbol(symbol)
          .build()
        doc.addOccurrences(occ)

        if (isDefinition) {
          val hover = scip.hoverViaDefinition(o)
          val symInfo = SymbolInformation
            .newBuilder()
            // we cheese it a bit here, as this is less work than trying to reconstruct
            // a Signature from the pretty-printed Signature, with accompanying logic
            // to fallback to display_name in SemanticdbPrinters.scala
            .setDisplayName(hover.replace('\n', ' ')).setSymbol(symbol).build()
          doc.addSymbols(symInfo)
        }
      }
    scip.documents.values.map(_.build()).toList
  }

  def signatureLines(documentation: String): Iterator[String] = {
    documentation
      .linesIterator
      .dropWhile(!_.startsWith("```"))
      .drop(1)
      .takeWhile(_ != "```")
  }

  class IndexedScip(
      val path: Path,
      val objects: mutable.Buffer[LsifObject],
      val sourceroot: Path,
      val reporter: Reporter
  ) {
    val documents = mutable.Map.empty[Int, TextDocument.Builder]
    val next = mutable.Map.empty[Int, Int]
    private val missingMonikerCounter = mutable.Map.empty[String, AtomicInteger]
    def nextMissingMonikerId(uri: String) =
      missingMonikerCounter
        .getOrElseUpdate(uri, new AtomicInteger())
        .incrementAndGet()
    val monikerIdentifier = mutable.Map.empty[Int, String]
    val moniker = mutable.Map.empty[Int, Int]
    val monikerInverse = mutable.Map.empty[Int, Int]
    val item = mutable.Map.empty[Int, mutable.Buffer[Integer]]
    val contains = mutable.Map.empty[Int, Int]
    val hoverVertexes = mutable.Map.empty[Int, LsifHover]
    val hoverEdges = mutable.Map.empty[Int, Int]
    val ranges: ArrayBuffer[LsifObject] = mutable.ArrayBuffer.empty[LsifObject]
    val isDefinitionResult = mutable.Set.empty[Integer]

    def textDocument(id: Int): TextDocument.Builder = {
      documents.getOrElseUpdate(id, TextDocument.newBuilder())
    }
    def symbolFromRange(o: LsifObject): Option[String] = {
      val moniker =
        for {
          monikerId <- this.moniker.get(o.getId())
          moniker <- this.monikerIdentifier.get(monikerId)
        } yield moniker
      moniker.orElse {
        for {
          resultSetId <- this.next.get(o.getId())
          resultSet <- this.byId.get(resultSetId)
          fromResultSet <- symbolFromRange(resultSet)
        } yield fromResultSet
      }
    }

    val monikers: Map[String, LsifObject] =
      objects
        .iterator
        .filter(_.getType == "vertex")
        .filter(_.getLabel == "moniker")
        .map(m => m.getIdentifier -> m)
        .toMap
    val hovers: Map[String, String] = monikers.map { case (sym, obj) =>
      val hover =
        for {
          resultSet <- monikerInverse.get(obj.getId).toList
          hoverEdge <- hoverEdges.get(resultSet).toList
          hoverResult <- hoverVertexes.get(hoverEdge).toList
          contents = hoverResult.getContents.getValue.trim
          codeFence <- contents
            .linesIterator
            .dropWhile(_ != "```java")
            .drop(1)
            .takeWhile(_ != "```")
        } yield codeFence
      sym -> hover.mkString("\n\n")
    }

    def visualizeGraph(): String = {
      import scala.sys.process._
      s"scip-visualize $path".!!
    }
    def asciiGraph(symbol: String): String = {
      import org.scalameta.ascii.layout._
      import org.scalameta.ascii.graph.Graph
      val monikers =
        byLabel("moniker")
          .filter(o => o.getType == "vertex" && o.getIdentifier == symbol)
          .toList
      val moniker =
        monikers match {
          case Nil =>
            throw new NoSuchElementException(symbol)
          case head :: Nil =>
            head
          case many =>
            throw new MatchError(many)
        }
      val S = subgraph(moniker)
      val vertices = SortedSet.newBuilder[String]
      val edges = ListBuffer.empty[(String, String)]
      val inputs = mutable.Map.empty[String, Input]
      def input(range: LsifObject): Input = {
        val uri =
          G.predecessors(range)
            .asScala
            .iterator
            .filter(_.getLabel == "document")
            .next()
            .getUri
        inputs.getOrElseUpdate(uri, Input.path(Paths.get(URI.create(uri))))
      }
      def renderPos(pos: LsifPosition): String = {
        s"${pos.getLine}:${pos.getCharacter}"
      }
      val addedVertexes = mutable.Set.empty[String]
      def render(node: LsifObject): String = {
        (node.getType, node.getLabel) match {
          case ("vertex", "document") =>
            val filename = sourceroot
              .relativize(Paths.get(URI.create(node.getUri)))
              .iterator()
              .asScala
              .mkString("/")
            s"document ${filename}"
          case ("vertex", "hoverResult") =>
            val contents = node
              .getResult
              .getContents
              .getValue
              .replace("\n", "\\n")
              .trim()
            s"hoverResult(${node.getId}) ${contents}"
          case ("vertex", "moniker") =>
            s"${node.getKind} ${node.getIdentifier}"
          case ("vertex", "range") =>
            val i = input(node)
            val p = Position.range(
              i,
              node.getStart.getLine,
              node.getStart.getCharacter,
              node.getEnd.getLine,
              node.getEnd.getCharacter
            )
            s"range(${node.getId}) ${renderPos(node.getStart)} '${p.text}'"
          case ("vertex", "packageInformation") =>
            s"${node.getName} ${node.getVersion}(${node.getId})"
          case ("vertex", label) =>
            s"${label}(${node.getId})"
          case _ =>
            s"${node.getType}/${node.getLabel} (${node.getId})"
        }
      }
      def isAdded(vertex: LsifObject) = addedVertexes.contains(render(vertex))
      val addedEdges = mutable.Set.empty[(String, String)]
      S.nodes()
        .forEach { node =>
          val r = render(node)
          addedVertexes += r
          vertices += r
        }
      S.edges()
        .forEach { pair =>
          val edge = render(pair.target()) -> render(pair.source())
          if (
            addedEdges.add(edge) && isAdded(pair.source()) &&
            isAdded(pair.source())
          ) {
            edges += edge
          }
        }
      val output = GraphLayout.renderGraph(
        Graph[String](vertices.result(), edges.toList.sorted),
        layoutPrefs = LayoutPrefsImpl
          .DEFAULT
          .copy(rounded = true, explicitAsciiBends = true)
      )
      output
        .linesIterator
        .map { line =>
          line.replaceAll(" +$", "")
        }
        .mkString("\n")
    }

    /**
     * Runs `scip-visualize` against this SCIP dump and opens the generated SVG
     * file in your browser.
     */
    def visualizeOpenBrowser(): Unit = {
      import scala.sys.process._
      val svg = Files.createTempFile("scip-java", "dump.svg")
      (s"scip-visualize $path" #| s"dot -Tsvg -o $svg").!
      s"open $svg".!
    }

    val isSuccessorRelevantLabel = Set("resultSet", "moniker")
    def subgraph(node: LsifObject): Graph[LsifObject] = {
      val S = GraphBuilder.directed().immutable[LsifObject]()
      val visited = mutable.Set.empty[Int]
      def loop(l: LsifObject): Unit = {
        if (visited.add(l.getId)) {
          S.addNode(l)
          G.predecessors(l)
            .forEach { n =>
              S.putEdge(EndpointPair.ordered(l, n))
              loop(n)
            }
          if (isSuccessorRelevantLabel(l.getLabel)) {
            G.successors(l)
              .forEach { n =>
                S.putEdge(EndpointPair.ordered(n, l))
                loop(n)
              }
          }
        }
      }
      loop(node)
      S.build()
    }

    def definitioResultSet(node: LsifObject): Option[LsifObject] = {
      for {
        definitionResult <- G.predecessors(node).asScala
        if definitionResult.getLabel == "definitionResult"
        resultSet <- G.predecessors(definitionResult).asScala
        if resultSet.getLabel == "resultSet"
      } yield resultSet
    }.headOption

    def monikerViaDefinition(node: LsifObject): Option[String] = {
      for {
        resultSet <- definitioResultSet(node).toList
        moniker <- G.successors(resultSet).asScala
        if moniker.getLabel == "moniker"
      } yield moniker.getIdentifier
    }.headOption

    def hoverViaDefinition(node: LsifObject): String = {
      for {
        resultSet <- definitioResultSet(node).toList
        hover <-
          G.successors(resultSet)
            .asScala
            .find(_.getLabel == "hoverResult")
            .toList
        line <- signatureLines(hover.getResult.getContents.getValue)
      } yield line
    }.mkString("\n")

    val byId = objects.iterator.map(o => o.getId -> o).toMap
    val byType = objects.groupBy(_.getType)
    val byLabel = objects.groupBy(_.getLabel)

    val G: MutableNetwork[LsifObject, LsifObject] = {
      val B: MutableNetwork[LsifObject, LsifObject] = NetworkBuilder
        .directed()
        .allowsParallelEdges(true)
        .allowsSelfLoops(true)
        .expectedEdgeCount(byType("edge").size)
        .expectedNodeCount(byType("vertex").size)
        .build()
      objects.foreach { o =>
        o.getType match {
          case "vertex" =>
            B.addNode(o)
          case "edge" =>
            byId
              .get(o.getOutV)
              .foreach { outV =>
                for {
                  inId <-
                    Iterator(o.getInV: Integer) ++
                      o.getInVsList.asScala.iterator
                  inV <- byId.get(inId)
                } {
                  B.addEdge(
                    EndpointPair.ordered(outV, inV),
                    o.toBuilder.clearInVs().setInV(inId).build()
                  )
                }
              }
          case _ =>
        }
      }
      B
    }

    objects.foreach { o =>
      o.getType match {
        case "edge" =>
          o.getLabel match {
            case "item" =>
              o.getInVsList
                .forEach { inV =>
                  item.getOrElseUpdate(inV, ListBuffer.empty) += o.getOutV()
                }
            case "contains" =>
              o.getInVsList.forEach(inV => contains(inV) = o.getOutV)
            case "next" =>
              next(o.getOutV) = o.getInV
            case "moniker" =>
              moniker(o.getOutV) = o.getInV
              monikerInverse(o.getInV) = o.getOutV
            case "textDocument/hover" =>
              hoverEdges(o.getOutV) = o.getInV
            case _ =>
          }
        case "vertex" =>
          o.getLabel match {
            case "moniker" =>
              monikerIdentifier(o.getId) = o.getIdentifier
            case "document" =>
              val relativeFile = Paths.get(URI.create(o.getUri))
              val absoluteFile = sourceroot.resolve(relativeFile)
              if (!Files.isRegularFile(absoluteFile)) {
                reporter.warning(s"no such file: $absoluteFile")
              } else {
                val text =
                  new String(
                    Files.readAllBytes(absoluteFile),
                    StandardCharsets.UTF_8
                  )
                val relativeUri = sourceroot
                  .relativize(absoluteFile)
                  .iterator()
                  .asScala
                  .mkString("/")
                val language = Language
                  .values()
                  .find(_.name().compareToIgnoreCase(o.getLanguage) == 0)
                  .getOrElse(Language.UNKNOWN_LANGUAGE)
                textDocument(o.getId)
                  .setUri(relativeUri)
                  .setLanguage(language)
                  .setText(text)
              }
            case "definitionResult" =>
              isDefinitionResult += o.getId()
            case "hoverResult" =>
              hoverVertexes(o.getId) = o.getResult
            case "range" =>
              ranges += o
            case _ =>
          }
        case _ =>
      }
    }
  }

  def parseInput(input: Path): mutable.Buffer[LsifObject] = {
    val format = ScipOutputFormat.fromFilename(input.getFileName().toString())
    if (format == ScipOutputFormat.GRAPH_NDJSON) {
      val lines = Files.lines(input)
      try {
        lines.parallel().flatMap(parseLine).collect(Collectors.toList()).asScala
      } finally {
        lines.close()
      }
    } else {
      throw new UnsupportedOperationException(input.toString)
    }
  }
  def parseLine(line: String): Stream[LsifObject] = {
    val builder = LsifObject.newBuilder()
    try {
      jsonParser.merge(line, builder)
      Stream.of(builder.build())
    } catch {
      case e: InvalidProtocolBufferException
          if e.getMessage.startsWith("Expect message object but got") =>
        // Ignore: usually it's a failure to decode the hover message
        // and we don't use hovers anyways.
        Stream.empty[LsifObject]()
    }
  }
  val default = SnapshotLsifCommand()
  implicit val parser = CommandParser.derive(default)
}
