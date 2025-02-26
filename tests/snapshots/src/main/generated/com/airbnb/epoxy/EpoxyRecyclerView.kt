package com.airbnb.epoxy
//      ^^^ reference com/
//          ^^^^^^ reference com/airbnb/
//                 ^^^^^ reference com/airbnb/epoxy/

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import androidx.annotation.CallSuper
//     ^^^^^^^^ reference androidx/
//              ^^^^^^^^^^ reference androidx/annotation/
//                         ^^^^^^^^^ reference androidx/annotation/CallSuper#
import androidx.annotation.DimenRes
//     ^^^^^^^^ reference androidx/
//              ^^^^^^^^^^ reference androidx/annotation/
//                         ^^^^^^^^ reference androidx/annotation/DimenRes#
import androidx.annotation.Dimension
//     ^^^^^^^^ reference androidx/
//              ^^^^^^^^^^ reference androidx/annotation/
//                         ^^^^^^^^^ reference androidx/annotation/Dimension#
import androidx.annotation.Px
//     ^^^^^^^^ reference androidx/
//              ^^^^^^^^^^ reference androidx/annotation/
//                         ^^ reference androidx/annotation/Px#
import androidx.recyclerview.widget.GridLayoutManager
//     ^^^^^^^^ reference androidx/
import androidx.recyclerview.widget.LinearLayoutManager
//     ^^^^^^^^ reference androidx/
import androidx.recyclerview.widget.RecyclerView
//     ^^^^^^^^ reference androidx/
import com.airbnb.epoxy.preload.EpoxyModelPreloader
//     ^^^ reference com/
//         ^^^^^^ reference com/airbnb/
//                ^^^^^ reference com/airbnb/epoxy/
//                      ^^^^^^^ reference com/airbnb/epoxy/preload/
//                              ^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/EpoxyModelPreloader#
import com.airbnb.epoxy.preload.EpoxyPreloader
//     ^^^ reference com/
//         ^^^^^^ reference com/airbnb/
//                ^^^^^ reference com/airbnb/epoxy/
//                      ^^^^^^^ reference com/airbnb/epoxy/preload/
//                              ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/EpoxyPreloader#
import com.airbnb.epoxy.preload.PreloadErrorHandler
//     ^^^ reference com/
//         ^^^^^^ reference com/airbnb/
//                ^^^^^ reference com/airbnb/epoxy/
//                      ^^^^^^^ reference com/airbnb/epoxy/preload/
//                              ^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/PreloadErrorHandler#
import com.airbnb.epoxy.preload.PreloadRequestHolder
//     ^^^ reference com/
//         ^^^^^^ reference com/airbnb/
//                ^^^^^ reference com/airbnb/epoxy/
//                      ^^^^^^^ reference com/airbnb/epoxy/preload/
//                              ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/PreloadRequestHolder#
import com.airbnb.epoxy.preload.ViewMetadata
//     ^^^ reference com/
//         ^^^^^^ reference com/airbnb/
//                ^^^^^ reference com/airbnb/epoxy/
//                      ^^^^^^^ reference com/airbnb/epoxy/preload/
//                              ^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/ViewMetadata#
import com.airbnb.viewmodeladapter.R
//     ^^^ reference com/
//         ^^^^^^ reference com/airbnb/

/**
 * A RecyclerView implementation that makes for easier integration with Epoxy. The goal of this
 * class is to reduce boilerplate in setting up a RecyclerView by applying common defaults.
 * Additionally, several performance optimizations are made.
 *
 * Improvements in this class are:
 *
 * 1. A single view pool is automatically shared between all [EpoxyRecyclerView] instances in
 * the same activity. This should increase view recycling potential and increase performance when
 * nested RecyclerViews are used. See [.initViewPool].
 *
 * 2. A layout manager is automatically added with assumed defaults. See [createLayoutManager]
 *
 * 3. Fixed size is enabled if this view's size is MATCH_PARENT
 *
 * 4. If a [GridLayoutManager] is used this will automatically sync the span count with the
 * [EpoxyController]. See [syncSpanCount]
 *
 * 5. Helper methods like [withModels], [setModels], [buildModelsWith]
 * make it simpler to set up simple RecyclerViews.
 *
 * 6. Set an EpoxyController and build models in one step -
 * [setControllerAndBuildModels] or [withModels]
 *
 * 7. Support for automatic item spacing. See [.setItemSpacingPx]
 *
 * 8. Defaults for usage as a nested recyclerview are provided in [Carousel].
 *
 * 9. [setClipToPadding] is set to false by default since that behavior is commonly
 * desired in a scrolling list
 */
open class EpoxyRecyclerView @JvmOverloads constructor(
//         ^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#  public open class EpoxyRecyclerView
//                            ^^^^^^^^^^^^ reference kotlin/jvm/JvmOverloads#`<init>`().
//                                         ^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().  public constructor EpoxyRecyclerView(context: [ERROR : Context], attrs: [ERROR : AttributeSet]? = ..., defStyleAttr: kotlin.Int = ...)
    context: Context,
//  ^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(context)  value-parameter context: [ERROR : Context]
    attrs: AttributeSet? = null,
//  ^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(attrs)  value-parameter attrs: [ERROR : AttributeSet]? = ...
    defStyleAttr: Int = 0
//  ^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(defStyleAttr)  value-parameter defStyleAttr: kotlin.Int = ...
//                ^^^ reference kotlin/Int#
) : RecyclerView(context, attrs, defStyleAttr) {
//               ^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(context)
//                        ^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(attrs)
//                               ^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(defStyleAttr)

    protected val spacingDecorator = EpoxyItemSpacingDecorator()
//                ^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#spacingDecorator.  protected final val spacingDecorator: [ERROR : Type for EpoxyItemSpacingDecorator()]
//                ^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#getSpacingDecorator().  protected final val spacingDecorator: [ERROR : Type for EpoxyItemSpacingDecorator()]

    private var epoxyController: EpoxyController? = null
//              ^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.  private final var epoxyController: [ERROR : EpoxyController]?
//              ^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().  private final var epoxyController: [ERROR : EpoxyController]?
//              ^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().  private final var epoxyController: [ERROR : EpoxyController]?

    /**
     * The adapter that was removed because the RecyclerView was detached from the window. We save it
     * so we can reattach it if the RecyclerView is reattached to window. This allows us to
     * automatically restore the adapter, without risking leaking the RecyclerView if this view is
     * never used again.
     *
     * Since the adapter is removed this recyclerview won't get adapter changes, but that's fine since
     * the view isn't attached to window and isn't being drawn.
     *
     * This reference is cleared if another adapter is manually set, so we don't override the user's
     * adapter choice.
     *
     * @see .setRemoveAdapterWhenDetachedFromWindow
     */
    private var removedAdapter: RecyclerView.Adapter<*>? = null
//              ^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#removedAdapter.  private final var removedAdapter: [ERROR : RecyclerView.Adapter<*>]<out [ERROR : *]>?
//              ^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#getRemovedAdapter().  private final var removedAdapter: [ERROR : RecyclerView.Adapter<*>]<out [ERROR : *]>?
//              ^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setRemovedAdapter().  private final var removedAdapter: [ERROR : RecyclerView.Adapter<*>]<out [ERROR : *]>?

    private var removeAdapterWhenDetachedFromWindow = true
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#removeAdapterWhenDetachedFromWindow.  private final var removeAdapterWhenDetachedFromWindow: kotlin.Boolean
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#getRemoveAdapterWhenDetachedFromWindow().  private final var removeAdapterWhenDetachedFromWindow: kotlin.Boolean
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setRemoveAdapterWhenDetachedFromWindow().  public final fun setRemoveAdapterWhenDetachedFromWindow(removeAdapterWhenDetachedFromWindow: kotlin.Boolean)

    private var delayMsWhenRemovingAdapterOnDetach: Int = DEFAULT_ADAPTER_REMOVAL_DELAY_MS
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#delayMsWhenRemovingAdapterOnDetach.  private final var delayMsWhenRemovingAdapterOnDetach: kotlin.Int
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#getDelayMsWhenRemovingAdapterOnDetach().  private final var delayMsWhenRemovingAdapterOnDetach: kotlin.Int
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setDelayMsWhenRemovingAdapterOnDetach().  public final fun setDelayMsWhenRemovingAdapterOnDetach(delayMsWhenRemovingAdapterOnDetach: kotlin.Int)
//                                                  ^^^ reference kotlin/Int#
//                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#Companion#DEFAULT_ADAPTER_REMOVAL_DELAY_MS.
//                                                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#Companion#getDEFAULT_ADAPTER_REMOVAL_DELAY_MS().

    /**
     * Tracks whether [.removeAdapterRunnable] has been posted to run
     * later. This lets us know if we should cancel the runnable at certain times. This removes the
     * overhead of needlessly attempting to remove the runnable when it isn't posted.
     */
    private var isRemoveAdapterRunnablePosted: Boolean = false
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#isRemoveAdapterRunnablePosted.  private final var isRemoveAdapterRunnablePosted: kotlin.Boolean
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#getIsRemoveAdapterRunnablePosted().  private final var isRemoveAdapterRunnablePosted: kotlin.Boolean
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setIsRemoveAdapterRunnablePosted().  private final var isRemoveAdapterRunnablePosted: kotlin.Boolean
//                                             ^^^^^^^ reference kotlin/Boolean#
    private val removeAdapterRunnable = Runnable {
//              ^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#removeAdapterRunnable.  private final val removeAdapterRunnable: java.lang.Runnable
//              ^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#getRemoveAdapterRunnable().  private final val removeAdapterRunnable: java.lang.Runnable
//                                      ^^^^^^^^ reference java/lang/Runnable#
        if (isRemoveAdapterRunnablePosted) {
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#isRemoveAdapterRunnablePosted.
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getIsRemoveAdapterRunnablePosted().
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setIsRemoveAdapterRunnablePosted().
            // Canceling a runnable doesn't work accurately when a view switches between
            // attached/detached, so we manually check that this should still be run
            isRemoveAdapterRunnablePosted = false
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#isRemoveAdapterRunnablePosted.
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getIsRemoveAdapterRunnablePosted().
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setIsRemoveAdapterRunnablePosted().
            removeAdapter()
//          ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removeAdapter().
        }
    }

    private val preloadScrollListeners: MutableList<EpoxyPreloader<*>> = mutableListOf()
//              ^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#preloadScrollListeners.  private final val preloadScrollListeners: kotlin.collections.MutableList<com.airbnb.epoxy.preload.EpoxyPreloader<*>>
//              ^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#getPreloadScrollListeners().  private final val preloadScrollListeners: kotlin.collections.MutableList<com.airbnb.epoxy.preload.EpoxyPreloader<*>>
//                                      ^^^^^^^^^^^ reference kotlin/collections/MutableList#
//                                                  ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/EpoxyPreloader#
//                                                                       ^^^^^^^^^^^^^ reference kotlin/collections/CollectionsKt#mutableListOf().

    private val preloadConfigs: MutableList<PreloadConfig<*, *, *>> = mutableListOf()
//              ^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#preloadConfigs.  private final val preloadConfigs: kotlin.collections.MutableList<com.airbnb.epoxy.EpoxyRecyclerView.PreloadConfig<*, *, *>>
//              ^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#getPreloadConfigs().  private final val preloadConfigs: kotlin.collections.MutableList<com.airbnb.epoxy.EpoxyRecyclerView.PreloadConfig<*, *, *>>
//                              ^^^^^^^^^^^ reference kotlin/collections/MutableList#
//                                          ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#
//                                                                    ^^^^^^^^^^^^^ reference kotlin/collections/CollectionsKt#mutableListOf().

    private class PreloadConfig<T : EpoxyModel<*>, U : ViewMetadata?, P : PreloadRequestHolder>(
//                ^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#  private final class PreloadConfig<T, U : com.airbnb.epoxy.preload.ViewMetadata?, P : com.airbnb.epoxy.preload.PreloadRequestHolder>
//                ^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#`<init>`().  public constructor PreloadConfig<T, U : com.airbnb.epoxy.preload.ViewMetadata?, P : com.airbnb.epoxy.preload.PreloadRequestHolder>(maxPreload: kotlin.Int, errorHandler: com.airbnb.epoxy.preload.PreloadErrorHandler /* = ([ERROR : Context], kotlin.RuntimeException /* = java.lang.RuntimeException */) -> kotlin.Unit */, preloader: com.airbnb.epoxy.preload.EpoxyModelPreloader<T, U, P>, requestHolderFactory: () -> P)
//                              ^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#[T]  <T>
//                                                 ^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#[U]  <U : com.airbnb.epoxy.preload.ViewMetadata?>
//                                                     ^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/ViewMetadata#
//                                                                    ^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#[P]  <P : com.airbnb.epoxy.preload.PreloadRequestHolder>
//                                                                        ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/PreloadRequestHolder#
        val maxPreload: Int,
//          ^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#maxPreload.  public final val maxPreload: kotlin.Int
//          ^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getMaxPreload().  public final val maxPreload: kotlin.Int
//          ^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#`<init>`().(maxPreload)  value-parameter maxPreload: kotlin.Int
//                      ^^^ reference kotlin/Int#
        val errorHandler: PreloadErrorHandler,
//          ^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#errorHandler.  public final val errorHandler: com.airbnb.epoxy.preload.PreloadErrorHandler /* = ([ERROR : Context], kotlin.RuntimeException /* = java.lang.RuntimeException */) -> kotlin.Unit */
//          ^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getErrorHandler().  public final val errorHandler: com.airbnb.epoxy.preload.PreloadErrorHandler /* = ([ERROR : Context], kotlin.RuntimeException /* = java.lang.RuntimeException */) -> kotlin.Unit */
//          ^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#`<init>`().(errorHandler)  value-parameter errorHandler: com.airbnb.epoxy.preload.PreloadErrorHandler /* = ([ERROR : Context], kotlin.RuntimeException /* = java.lang.RuntimeException */) -> kotlin.Unit */
//                        ^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/PreloadErrorHandler#
        val preloader: EpoxyModelPreloader<T, U, P>,
//          ^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#preloader.  public final val preloader: com.airbnb.epoxy.preload.EpoxyModelPreloader<T, U, P>
//          ^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getPreloader().  public final val preloader: com.airbnb.epoxy.preload.EpoxyModelPreloader<T, U, P>
//          ^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#`<init>`().(preloader)  value-parameter preloader: com.airbnb.epoxy.preload.EpoxyModelPreloader<T, U, P>
//                     ^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/EpoxyModelPreloader#
//                                         ^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#[T]
//                                            ^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#[U]
//                                               ^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#[P]
        val requestHolderFactory: () -> P
//          ^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#requestHolderFactory.  public final val requestHolderFactory: () -> P
//          ^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getRequestHolderFactory().  public final val requestHolderFactory: () -> P
//          ^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#`<init>`().(requestHolderFactory)  value-parameter requestHolderFactory: () -> P
//                                      ^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#[P]
    )

    /**
     * Setup a preloader to fetch content for a model's view before it is bound.
     * This can be called multiple times if you would like to add separate preloaders
     * for different models or content types.
     *
     * Preloaders are automatically attached and run, and are updated if the adapter changes.
     *
     * @param maxPreloadDistance How many items to prefetch ahead of the last bound item
     * @param errorHandler Called when the preloader encounters an exception. We recommend throwing an
     * exception in debug builds, and logging an error in production.
     * @param preloader Describes how view content for the EpoxyModel should be preloaded
     * @param requestHolderFactory Should create and return a new [PreloadRequestHolder] each time it is invoked
     */
    fun <T : EpoxyModel<*>, U : ViewMetadata?, P : PreloadRequestHolder> addPreloader(
//       ^ definition com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().[T]  <T>
//                          ^ definition com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().[U]  <U : com.airbnb.epoxy.preload.ViewMetadata?>
//                              ^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/ViewMetadata#
//                                             ^ definition com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().[P]  <P : com.airbnb.epoxy.preload.PreloadRequestHolder>
//                                                 ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/PreloadRequestHolder#
//                                                                       ^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().  public final fun <T, U : com.airbnb.epoxy.preload.ViewMetadata?, P : com.airbnb.epoxy.preload.PreloadRequestHolder> addPreloader(maxPreloadDistance: kotlin.Int = ..., errorHandler: com.airbnb.epoxy.preload.PreloadErrorHandler /* = ([ERROR : Context], kotlin.RuntimeException /* = java.lang.RuntimeException */) -> kotlin.Unit */, preloader: com.airbnb.epoxy.preload.EpoxyModelPreloader<T, U, P>, requestHolderFactory: () -> P)
        maxPreloadDistance: Int = 3,
//      ^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().(maxPreloadDistance)  value-parameter maxPreloadDistance: kotlin.Int = ...
//                          ^^^ reference kotlin/Int#
        errorHandler: PreloadErrorHandler,
//      ^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().(errorHandler)  value-parameter errorHandler: com.airbnb.epoxy.preload.PreloadErrorHandler /* = ([ERROR : Context], kotlin.RuntimeException /* = java.lang.RuntimeException */) -> kotlin.Unit */
//                    ^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/PreloadErrorHandler#
        preloader: EpoxyModelPreloader<T, U, P>,
//      ^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().(preloader)  value-parameter preloader: com.airbnb.epoxy.preload.EpoxyModelPreloader<T, U, P>
//                 ^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/EpoxyModelPreloader#
//                                     ^ reference com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().[T]
//                                        ^ reference com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().[U]
//                                           ^ reference com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().[P]
        requestHolderFactory: () -> P
//      ^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().(requestHolderFactory)  value-parameter requestHolderFactory: () -> P
//                                  ^ reference com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().[P]
    ) {
        preloadConfigs.add(
//      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#preloadConfigs.
//      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getPreloadConfigs().
//                     ^^^ reference kotlin/collections/MutableList#add().
            PreloadConfig(
//          ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#`<init>`().
                maxPreloadDistance,
//              ^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().(maxPreloadDistance)
                errorHandler,
//              ^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().(errorHandler)
                preloader,
//              ^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().(preloader)
                requestHolderFactory
//              ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#addPreloader().(requestHolderFactory)
            )
        )

        updatePreloaders()
//      ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#updatePreloaders().
    }

    /**
     * Clears all preloaders added with [addPreloader]
     */
    fun clearPreloaders() {
//      ^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#clearPreloaders().  public final fun clearPreloaders()
        preloadConfigs.clear()
//      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#preloadConfigs.
//      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getPreloadConfigs().
//                     ^^^^^ reference kotlin/collections/MutableList#clear().
        updatePreloaders()
//      ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#updatePreloaders().
    }

    private fun updatePreloaders() {
//              ^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#updatePreloaders().  private final fun updatePreloaders()
        preloadScrollListeners.forEach { removeOnScrollListener(it) }
//      ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#preloadScrollListeners.
//      ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getPreloadScrollListeners().
//                             ^^^^^^^ reference kotlin/collections/CollectionsKt#forEach(+10).
//                                                              ^^ reference local0
        preloadScrollListeners.clear()
//      ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#preloadScrollListeners.
//      ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getPreloadScrollListeners().
//                             ^^^^^ reference kotlin/collections/MutableList#clear().
        val currAdapter = adapter ?: return
//          ^^^^^^^^^^^ definition local1  val currAdapter: [ERROR : Type for adapter ?: return]

        preloadConfigs.forEach { preloadConfig ->
//      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#preloadConfigs.
//      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getPreloadConfigs().
//                     ^^^^^^^ reference kotlin/collections/CollectionsKt#forEach(+10).
//                               ^^^^^^^^^^^^^ definition local2  value-parameter preloadConfig: com.airbnb.epoxy.EpoxyRecyclerView.PreloadConfig<*, *, *>

            if (currAdapter is EpoxyAdapter) {
//              ^^^^^^^^^^^ reference local1
                EpoxyPreloader.with(
//              ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/EpoxyPreloader#
                    currAdapter,
//                  ^^^^^^^^^^^ reference local1
                    preloadConfig.requestHolderFactory,
//                  ^^^^^^^^^^^^^ reference local2
//                                ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#requestHolderFactory.
//                                ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getRequestHolderFactory().
                    preloadConfig.errorHandler,
//                  ^^^^^^^^^^^^^ reference local2
//                                ^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#errorHandler.
//                                ^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getErrorHandler().
                    preloadConfig.maxPreload,
//                  ^^^^^^^^^^^^^ reference local2
//                                ^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#maxPreload.
//                                ^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getMaxPreload().
                    listOf(preloadConfig.preloader)
//                  ^^^^^^ reference kotlin/collections/CollectionsKt#listOf().
//                         ^^^^^^^^^^^^^ reference local2
//                                       ^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#preloader.
//                                       ^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getPreloader().
                )
            } else {
                epoxyController?.let {
//              ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//              ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//              ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
                    EpoxyPreloader.with(
//                  ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/EpoxyPreloader#
                        it,
                        preloadConfig.requestHolderFactory,
//                      ^^^^^^^^^^^^^ reference local2
//                                    ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#requestHolderFactory.
//                                    ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getRequestHolderFactory().
                        preloadConfig.errorHandler,
//                      ^^^^^^^^^^^^^ reference local2
//                                    ^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#errorHandler.
//                                    ^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getErrorHandler().
                        preloadConfig.maxPreload,
//                      ^^^^^^^^^^^^^ reference local2
//                                    ^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#maxPreload.
//                                    ^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getMaxPreload().
                        listOf(preloadConfig.preloader)
//                      ^^^^^^ reference kotlin/collections/CollectionsKt#listOf().
//                             ^^^^^^^^^^^^^ reference local2
//                                           ^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#preloader.
//                                           ^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#PreloadConfig#getPreloader().
                    )
                }
            }?.let {
                preloadScrollListeners.add(it)
//              ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#preloadScrollListeners.
//              ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getPreloadScrollListeners().
//                                     ^^^ reference kotlin/collections/MutableList#add().
                addOnScrollListener(it)
            }
        }
    }

    /**
     * If set to true, any adapter set on this recyclerview will be removed when this view is detached
     * from the window. This is useful to prevent leaking a reference to this RecyclerView. This is
     * useful in cases where the same adapter can be used across multiple views (views which can be
     * destroyed and recreated), such as with fragments. In that case the adapter is not necessarily
     * cleared from previous RecyclerViews, so the adapter will continue to hold a reference to those
     * views and leak them. More details at https://github.com/airbnb/epoxy/wiki/Avoiding-Memory-Leaks#parent-view
     *
     * The default is true, but you can disable this if you don't want your adapter detached
     * automatically.
     *
     * If the adapter is removed via this setting, it will be re-set on the RecyclerView if the
     * RecyclerView is re-attached to the window at a later point.
     */
    fun setRemoveAdapterWhenDetachedFromWindow(removeAdapterWhenDetachedFromWindow: Boolean) {
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setRemoveAdapterWhenDetachedFromWindow().  public final fun setRemoveAdapterWhenDetachedFromWindow(removeAdapterWhenDetachedFromWindow: kotlin.Boolean)
//                                             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setRemoveAdapterWhenDetachedFromWindow().(removeAdapterWhenDetachedFromWindow)  value-parameter removeAdapterWhenDetachedFromWindow: kotlin.Boolean
//                                                                                  ^^^^^^^ reference kotlin/Boolean#
        this.removeAdapterWhenDetachedFromWindow = removeAdapterWhenDetachedFromWindow
//      ^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#
//           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removeAdapterWhenDetachedFromWindow.
//           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getRemoveAdapterWhenDetachedFromWindow().
//           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setRemoveAdapterWhenDetachedFromWindow().
//                                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setRemoveAdapterWhenDetachedFromWindow().(removeAdapterWhenDetachedFromWindow)
    }

    /**
     * If [.setRemoveAdapterWhenDetachedFromWindow] is set to true, this is the delay
     * in milliseconds between when [.onDetachedFromWindow] is called and when the adapter is
     * actually removed.
     *
     * By default a delay of {@value #DEFAULT_ADAPTER_REMOVAL_DELAY_MS} ms is used so that view
     * transitions can complete before the adapter is removed. Otherwise if the adapter is removed
     * before transitions finish it can clear the screen and break the transition. A notable case is
     * fragment transitions, in which the fragment view is detached from window before the transition
     * ends.
     */
    fun setDelayMsWhenRemovingAdapterOnDetach(delayMsWhenRemovingAdapterOnDetach: Int) {
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setDelayMsWhenRemovingAdapterOnDetach().  public final fun setDelayMsWhenRemovingAdapterOnDetach(delayMsWhenRemovingAdapterOnDetach: kotlin.Int)
//                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setDelayMsWhenRemovingAdapterOnDetach().(delayMsWhenRemovingAdapterOnDetach)  value-parameter delayMsWhenRemovingAdapterOnDetach: kotlin.Int
//                                                                                ^^^ reference kotlin/Int#
        this.delayMsWhenRemovingAdapterOnDetach = delayMsWhenRemovingAdapterOnDetach
//      ^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#
//           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#delayMsWhenRemovingAdapterOnDetach.
//           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getDelayMsWhenRemovingAdapterOnDetach().
//           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setDelayMsWhenRemovingAdapterOnDetach().
//                                                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setDelayMsWhenRemovingAdapterOnDetach().(delayMsWhenRemovingAdapterOnDetach)
    }

    init {

        if (attrs != null) {
//          ^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(attrs)
            val a = context.obtainStyledAttributes(
//              ^ definition local3  val a: [ERROR : <ERROR FUNCTION RETURN TYPE>]
//                  ^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(context)
                attrs, R.styleable.EpoxyRecyclerView,
//              ^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(attrs)
                defStyleAttr, 0
//              ^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#`<init>`().(defStyleAttr)
            )
            setItemSpacingPx(
//          ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingPx().
                a.getDimensionPixelSize(
//              ^ reference local3
                    R.styleable.EpoxyRecyclerView_itemSpacing,
                    0
                )
            )
            a.recycle()
//          ^ reference local3
        }

        init()
//      ^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#init().
    }

    @CallSuper
//   ^^^^^^^^^ reference androidx/annotation/CallSuper#`<init>`().
    protected open fun init() {
//                     ^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#init().  protected open fun init()
        clipToPadding = false
        initViewPool()
//      ^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#initViewPool().
    }

    /**
     * Get or create a view pool to use for this RecyclerView. By default the same pool is shared for
     * all [EpoxyRecyclerView] usages in the same Activity.
     *
     * @see .createViewPool
     * @see .shouldShareViewPoolAcrossContext
     */
    private fun initViewPool() {
//              ^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#initViewPool().  private final fun initViewPool()
        if (!shouldShareViewPoolAcrossContext()) {
//          ^ reference kotlin/Boolean#not().
//           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#shouldShareViewPoolAcrossContext().
            setRecycledViewPool(createViewPool())
//                              ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#createViewPool().
            return
        }

        setRecycledViewPool(
            ACTIVITY_RECYCLER_POOL.getPool(
//          ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#Companion#ACTIVITY_RECYCLER_POOL.
//          ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#Companion#getACTIVITY_RECYCLER_POOL().
//                                 ^^^^^^^ reference com/airbnb/epoxy/ActivityRecyclerPool#getPool().
                context
            ) { createViewPool() }.viewPool
//              ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#createViewPool().
//                                 ^^^^^^^^ reference com/airbnb/epoxy/PoolReference#viewPool.
//                                 ^^^^^^^^ reference com/airbnb/epoxy/PoolReference#getViewPool().
        )
    }

    /**
     * Create a new instance of a view pool to use with this recyclerview. By default a [ ] is used.
     */
    protected open fun createViewPool(): RecyclerView.RecycledViewPool {
//                     ^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#createViewPool().  protected open fun createViewPool(): [ERROR : RecyclerView.RecycledViewPool]
        return UnboundedViewPool()
//             ^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/UnboundedViewPool#`<init>`().
    }

    /**
     * To maximize view recycling by default we share the same view pool across all instances in the same Activity. This behavior can be disabled by returning
     * false here.
     */
    open fun shouldShareViewPoolAcrossContext(): Boolean {
//           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#shouldShareViewPoolAcrossContext().  public open fun shouldShareViewPoolAcrossContext(): kotlin.Boolean
//                                               ^^^^^^^ reference kotlin/Boolean#
        return true
    }

    override fun setLayoutParams(params: ViewGroup.LayoutParams) {
//               ^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setLayoutParams().  public open fun setLayoutParams(params: [ERROR : ViewGroup.LayoutParams])
//                               ^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setLayoutParams().(params)  value-parameter params: [ERROR : ViewGroup.LayoutParams]
        val isFirstParams = layoutParams == null
//          ^^^^^^^^^^^^^ definition local4  val isFirstParams: kotlin.Boolean
        super.setLayoutParams(params)
//      ^^^^^ reference kotlin/Any#
//                            ^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setLayoutParams().(params)

        if (isFirstParams) {
//          ^^^^^^^^^^^^^ reference local4
            // Set a default layout manager if one was not set via xml
            // We need layout params for this to guess at the right size and type
            if (layoutManager == null) {
                layoutManager = createLayoutManager()
//                              ^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#createLayoutManager().
            }
        }
    }

    /**
     * Create a new [androidx.recyclerview.widget.RecyclerView.LayoutManager]
     * instance to use for this RecyclerView.
     *
     * By default a LinearLayoutManager is used, and a reasonable default is chosen for scrolling
     * direction based on layout params.
     *
     * If the RecyclerView is set to match parent size then the scrolling orientation is set to
     * vertical and [.setHasFixedSize] is set to true.
     *
     * If the height is set to wrap_content then the scrolling orientation is set to horizontal, and
     * [.setClipToPadding] is set to false.
     */
    protected open fun createLayoutManager(): RecyclerView.LayoutManager {
//                     ^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#createLayoutManager().  protected open fun createLayoutManager(): [ERROR : RecyclerView.LayoutManager]
        val layoutParams = layoutParams
//          ^^^^^^^^^^^^ definition local5  val layoutParams: [ERROR : Type for layoutParams]

        // 0 represents matching constraints in a LinearLayout or ConstraintLayout
        if (layoutParams.height == RecyclerView.LayoutParams.MATCH_PARENT || layoutParams.height == 0) {
//          ^^^^^^^^^^^^ reference local5
//                                                                           ^^^^^^^^^^^^ reference local5

            if (layoutParams.width == RecyclerView.LayoutParams.MATCH_PARENT || layoutParams.width == 0) {
//              ^^^^^^^^^^^^ reference local5
//                                                                              ^^^^^^^^^^^^ reference local5
                // If we are filling as much space as possible then we usually are fixed size
                setHasFixedSize(true)
            }

            // A sane default is a vertically scrolling linear layout
            return LinearLayoutManager(context)
        } else {
            // This is usually the case for horizontally scrolling carousels and should be a sane
            // default
            return LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    override fun setLayoutManager(layout: RecyclerView.LayoutManager?) {
//               ^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setLayoutManager().  public open fun setLayoutManager(layout: [ERROR : RecyclerView.LayoutManager]?)
//                                ^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setLayoutManager().(layout)  value-parameter layout: [ERROR : RecyclerView.LayoutManager]?
        super.setLayoutManager(layout)
//      ^^^^^ reference kotlin/Any#
//                             ^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setLayoutManager().(layout)
        syncSpanCount()
//      ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#syncSpanCount().
    }

    /**
     * If a grid layout manager is set we sync the span count between the layout and the epoxy
     * adapter automatically.
     */
    private fun syncSpanCount() {
//              ^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#syncSpanCount().  private final fun syncSpanCount()
        val layout = layoutManager
//          ^^^^^^ definition local6  val layout: [ERROR : Type for layoutManager]
        val controller = epoxyController
//          ^^^^^^^^^^ definition local7  val controller: [ERROR : EpoxyController]?
//                       ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//                       ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//                       ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
        if (layout is GridLayoutManager && controller != null) {
//          ^^^^^^ reference local6
//                                         ^^^^^^^^^^ reference local7

            if (controller.spanCount != layout.spanCount || layout.spanSizeLookup !== controller.spanSizeLookup) {
//              ^^^^^^^^^^ reference local7
//                                      ^^^^^^ reference local6
//                                                          ^^^^^^ reference local6
//                                                                                    ^^^^^^^^^^ reference local7
                controller.spanCount = layout.spanCount
//              ^^^^^^^^^^ reference local7
//                                     ^^^^^^ reference local6
                layout.spanSizeLookup = controller.spanSizeLookup
//              ^^^^^^ reference local6
//                                      ^^^^^^^^^^ reference local7
            }
        }
    }

    override fun requestLayout() {
//               ^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#requestLayout().  public open fun requestLayout()
        // Grid layout manager calls this when the span count is changed. Its the easiest way to
        // detect a span count change and update our controller accordingly.
        syncSpanCount()
//      ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#syncSpanCount().
        super.requestLayout()
//      ^^^^^ reference kotlin/Any#
    }

    fun setItemSpacingRes(@DimenRes itemSpacingRes: Int) {
//      ^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingRes().  public final fun setItemSpacingRes(itemSpacingRes: kotlin.Int)
//                         ^^^^^^^^ reference androidx/annotation/DimenRes#`<init>`().
//                                  ^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingRes().(itemSpacingRes)  value-parameter itemSpacingRes: kotlin.Int
//                                                  ^^^ reference kotlin/Int#
        setItemSpacingPx(resToPx(itemSpacingRes))
//      ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingPx().
//                       ^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#resToPx().
//                               ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingRes().(itemSpacingRes)
    }

    fun setItemSpacingDp(@Dimension(unit = Dimension.DP) dp: Int) {
//      ^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingDp().  public final fun setItemSpacingDp(dp: kotlin.Int)
//                        ^^^^^^^^^ reference androidx/annotation/Dimension#`<init>`().
//                                  ^^^^ reference androidx/annotation/Dimension#`<init>`().(unit)
//                                         ^^^^^^^^^ reference androidx/annotation/Dimension#
//                                                   ^^ reference androidx/annotation/Dimension#DP.
//                                                       ^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingDp().(dp)  value-parameter dp: kotlin.Int
//                                                           ^^^ reference kotlin/Int#
        setItemSpacingPx(dpToPx(dp))
//      ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingPx().
//                       ^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#dpToPx().
//                              ^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingDp().(dp)
    }

    /**
     * Set a pixel value to use as spacing between items. If this is a positive number an item
     * decoration will be added to space all items this far apart from each other. If the value is 0
     * or negative no extra spacing will be used, and any previous spacing will be removed.
     *
     * This only works if a [LinearLayoutManager] or [GridLayoutManager] is used with this
     * RecyclerView.
     *
     * This can also be set via the `app:itemSpacing` styleable attribute.
     *
     * @see .setItemSpacingDp
     * @see .setItemSpacingRes
     */
    open fun setItemSpacingPx(@Px spacingPx: Int) {
//           ^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingPx().  public open fun setItemSpacingPx(spacingPx: kotlin.Int)
//                             ^^ reference androidx/annotation/Px#`<init>`().
//                                ^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingPx().(spacingPx)  value-parameter spacingPx: kotlin.Int
//                                           ^^^ reference kotlin/Int#
        removeItemDecoration(spacingDecorator)
//                           ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#spacingDecorator.
//                           ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getSpacingDecorator().
        spacingDecorator.pxBetweenItems = spacingPx
//      ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#spacingDecorator.
//      ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getSpacingDecorator().
//                                        ^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingPx().(spacingPx)

        if (spacingPx > 0) {
//          ^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setItemSpacingPx().(spacingPx)
//                    ^ reference kotlin/Int#compareTo(+3).
            addItemDecoration(spacingDecorator)
//                            ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#spacingDecorator.
//                            ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getSpacingDecorator().
        }
    }

    /**
     * Set a list of [EpoxyModel]'s to show in this RecyclerView.
     *
     * Alternatively you can set an [EpoxyController] to handle building models dynamically.
     *
     * @see withModels
     * @see setController
     * @see setControllerAndBuildModels
     * @see buildModelsWith
     */

    open fun setModels(models: List<EpoxyModel<*>>) {
//           ^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setModels().  public open fun setModels(models: kotlin.collections.List<[ERROR : EpoxyModel<*>]<out [ERROR : *]>>)
//                     ^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setModels().(models)  value-parameter models: kotlin.collections.List<[ERROR : EpoxyModel<*>]<out [ERROR : *]>>
//                             ^^^^ reference kotlin/collections/List#
        val controller = (epoxyController as? SimpleEpoxyController)
//          ^^^^^^^^^^ definition local8  val controller: ???
//                        ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//                        ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//                        ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
            ?: SimpleEpoxyController().also {
                setController(it)
//              ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setController().
            }

        controller.setModels(models)
//      ^^^^^^^^^^ reference local8
//                           ^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setModels().(models)
    }

    /**
     * Set an EpoxyController to populate this RecyclerView. This does not make the controller build
     * its models, that must be done separately via [requestModelBuild].
     *
     * Use this if you don't want [requestModelBuild] called automatically. Common cases
     * are if you are using [TypedEpoxyController] (in which case you must call setData on the
     * controller), or if you have not otherwise populated your controller's data yet.
     *
     * Otherwise if you want models built automatically for you use [setControllerAndBuildModels]
     *
     * The controller can be cleared with [clear]
     *
     * @see .setControllerAndBuildModels
     * @see .buildModelsWith
     * @see .setModels
     */

    fun setController(controller: EpoxyController) {
//      ^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setController().  public final fun setController(controller: [ERROR : EpoxyController])
//                    ^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setController().(controller)  value-parameter controller: [ERROR : EpoxyController]
        epoxyController = controller
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
//                        ^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setController().(controller)
        adapter = controller.adapter
//                ^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setController().(controller)
        syncSpanCount()
//      ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#syncSpanCount().
    }

    /**
     * Set an EpoxyController to populate this RecyclerView, and tell the controller to build
     * models.
     *
     * The controller can be cleared with [clear]
     *
     * @see setController
     * @see buildModelsWith
     * @see setModels
     */
    fun setControllerAndBuildModels(controller: EpoxyController) {
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setControllerAndBuildModels().  public final fun setControllerAndBuildModels(controller: [ERROR : EpoxyController])
//                                  ^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setControllerAndBuildModels().(controller)  value-parameter controller: [ERROR : EpoxyController]
        controller.requestModelBuild()
//      ^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setControllerAndBuildModels().(controller)
        setController(controller)
//      ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setController().
//                    ^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setControllerAndBuildModels().(controller)
    }

    /**
     * The simplest way to add models to the RecyclerView without needing to create an EpoxyController.
     * This is intended for Kotlin usage, and has the EpoxyController as the lambda receiver so
     * models can be added easily.
     *
     * Multiple calls to this will reuse the same underlying EpoxyController so views in the
     * RecyclerView will be reused.
     *
     * The Java equivalent is [buildModelsWith].
     */
    fun withModels(buildModels: EpoxyController.() -> Unit) {
//      ^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#withModels().  public final fun withModels(buildModels: [ERROR : EpoxyController].() -> kotlin.Unit)
//                 ^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#withModels().(buildModels)  value-parameter buildModels: [ERROR : EpoxyController].() -> kotlin.Unit
//                                                    ^^^^ reference kotlin/Unit#
        val controller = (epoxyController as? WithModelsController)
//          ^^^^^^^^^^ definition local9  val controller: com.airbnb.epoxy.EpoxyRecyclerView.WithModelsController
//                        ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//                        ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//                        ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
//                                            ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#
            ?: WithModelsController().also { setController(it) }
//             ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#`<init>`().
//                                    ^^^^ reference kotlin/StandardKt#also().
//                                           ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setController().
//                                                         ^^ reference local10

        controller.callback = buildModels
//      ^^^^^^^^^^ reference local9
//                 ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#callback.
//                 ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#getCallback().
//                 ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#setCallback().
//                            ^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#withModels().(buildModels)
        controller.requestModelBuild()
//      ^^^^^^^^^^ reference local9
    }

    private class WithModelsController : EpoxyController() {
//                ^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#  private final class WithModelsController
//                ^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#`<init>`().  public constructor WithModelsController()
        var callback: EpoxyController.() -> Unit = {}
//          ^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#callback.  public final var callback: [ERROR : EpoxyController].() -> kotlin.Unit
//          ^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#getCallback().  public final var callback: [ERROR : EpoxyController].() -> kotlin.Unit
//          ^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#setCallback().  public final var callback: [ERROR : EpoxyController].() -> kotlin.Unit
//                                          ^^^^ reference kotlin/Unit#

        override fun buildModels() {
//                   ^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#buildModels().  public open fun buildModels()
            callback(this)
//          ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#callback.
//          ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#getCallback().
//          ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#setCallback().
//                   ^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#WithModelsController#
        }
    }

    /**
     * Allows you to build models via a callback instead of needing to create a new EpoxyController
     * class. This is useful if your models are simple and you would like to simply declare them in
     * your activity/fragment.
     *
     * Multiple calls to this will reuse the same underlying EpoxyController so views in the
     * RecyclerView will be reused.
     *
     * Another useful pattern is having your Activity or Fragment implement [ModelBuilderCallback].
     *
     * If you're using Kotlin, prefer [withModels].
     *
     * @see setController
     * @see setControllerAndBuildModels
     * @see setModels
     */
    fun buildModelsWith(callback: ModelBuilderCallback) {
//      ^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#buildModelsWith().  public final fun buildModelsWith(callback: com.airbnb.epoxy.EpoxyRecyclerView.ModelBuilderCallback)
//                      ^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#buildModelsWith().(callback)  value-parameter callback: com.airbnb.epoxy.EpoxyRecyclerView.ModelBuilderCallback
//                                ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallback#
        val controller = (epoxyController as? ModelBuilderCallbackController)
//          ^^^^^^^^^^ definition local11  val controller: com.airbnb.epoxy.EpoxyRecyclerView.ModelBuilderCallbackController
//                        ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//                        ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//                        ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
//                                            ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#
            ?: ModelBuilderCallbackController().also { setController(it) }
//             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#`<init>`().
//                                              ^^^^ reference kotlin/StandardKt#also().
//                                                     ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setController().
//                                                                   ^^ reference local12

        controller.callback = callback
//      ^^^^^^^^^^ reference local11
//                 ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#callback.
//                 ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#getCallback().
//                 ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#setCallback().
//                            ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#buildModelsWith().(callback)
        controller.requestModelBuild()
//      ^^^^^^^^^^ reference local11
    }

    private class ModelBuilderCallbackController : EpoxyController() {
//                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#  private final class ModelBuilderCallbackController
//                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#`<init>`().  public constructor ModelBuilderCallbackController()
        var callback: ModelBuilderCallback = object : ModelBuilderCallback {
//          ^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#callback.  public final var callback: com.airbnb.epoxy.EpoxyRecyclerView.ModelBuilderCallback
//          ^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#getCallback().  public final var callback: com.airbnb.epoxy.EpoxyRecyclerView.ModelBuilderCallback
//          ^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#setCallback().  public final var callback: com.airbnb.epoxy.EpoxyRecyclerView.ModelBuilderCallback
//                    ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallback#
//                                                    ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallback#
            override fun buildModels(controller: EpoxyController) {
//                       ^^^^^^^^^^^ definition local13  public open fun buildModels(controller: [ERROR : EpoxyController])
//                                   ^^^^^^^^^^ definition local14  value-parameter controller: [ERROR : EpoxyController]
            }
        }

        override fun buildModels() {
//                   ^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#buildModels().  public open fun buildModels()
            callback.buildModels(this)
//          ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#callback.
//          ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#getCallback().
//          ^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#setCallback().
//                   ^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallback#buildModels().
//                               ^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallbackController#
        }
    }

    /**
     * A callback for creating models without needing a custom EpoxyController class. Used with [buildModelsWith]
     */
    interface ModelBuilderCallback {
//            ^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallback#  public interface ModelBuilderCallback
        /**
         * Analagous to [EpoxyController.buildModels]. You should create new model instances and
         * add them to the given controller. [AutoModel] cannot be used with models added this
         * way.
         */
        fun buildModels(controller: EpoxyController)
//          ^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallback#buildModels().  public abstract fun buildModels(controller: [ERROR : EpoxyController])
//                      ^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#ModelBuilderCallback#buildModels().(controller)  value-parameter controller: [ERROR : EpoxyController]
    }

    /**
     * Request that the currently set EpoxyController has its models rebuilt. You can use this to
     * avoid saving your controller as a field.
     *
     * You cannot use this if your controller is a [TypedEpoxyController] or if you set
     * models via [setModels]. In that case you must set data directly on the
     * controller or set models again.
     */
    fun requestModelBuild() {
//      ^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#requestModelBuild().  public final fun requestModelBuild()
        if (epoxyController == null) {
//          ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//          ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//          ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
            throw IllegalStateException("A controller must be set before requesting a model build.")
//                ^^^^^^^^^^^^^^^^^^^^^ reference kotlin/IllegalStateException#`<init>`(+1).
        }

        if (epoxyController is SimpleEpoxyController) {
//          ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//          ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//          ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
            throw IllegalStateException("Models were set with #setModels, they can not be rebuilt.")
//                ^^^^^^^^^^^^^^^^^^^^^ reference kotlin/IllegalStateException#`<init>`(+1).
        }

        epoxyController!!.requestModelBuild()
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
    }

    /**
     * Clear the currently set EpoxyController or Adapter as well as any models that are displayed.
     *
     * Any pending requests to the EpoxyController to build models are canceled.
     *
     * Any existing child views are recycled to the view pool.
     */
    open fun clear() {
//           ^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#clear().  public open fun clear()
        // The controller is cleared so the next time models are set we can create a fresh one.
        epoxyController?.cancelPendingModelBuild()
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().
        epoxyController = null
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#epoxyController.
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getEpoxyController().
//      ^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setEpoxyController().

        // We use swapAdapter instead of setAdapter so that the view pool is not cleared.
        // 'removeAndRecycleExistingViews=true' is used in case this is a nested recyclerview
        // and we want to recycle the views back to a shared view pool
        swapAdapter(null, true)
//      ^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#swapAdapter().
    }

    @Px
//   ^^ reference androidx/annotation/Px#`<init>`().
    protected fun dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Int {
//                ^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#dpToPx().  protected final fun dpToPx(dp: kotlin.Int): kotlin.Int
//                        ^^^^^^^^^ reference androidx/annotation/Dimension#`<init>`().
//                                  ^^^^ reference androidx/annotation/Dimension#`<init>`().(unit)
//                                         ^^^^^^^^^ reference androidx/annotation/Dimension#
//                                                   ^^ reference androidx/annotation/Dimension#DP.
//                                                       ^^ definition com/airbnb/epoxy/EpoxyRecyclerView#dpToPx().(dp)  value-parameter dp: kotlin.Int
//                                                           ^^^ reference kotlin/Int#
//                                                                 ^^^ reference kotlin/Int#
        return TypedValue
            .applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
//                                           ^^ reference com/airbnb/epoxy/EpoxyRecyclerView#dpToPx().(dp)
//                                              ^^^^^^^ reference kotlin/Int#toFloat().
                resources.displayMetrics
            ).toInt()
    }

    @Px
//   ^^ reference androidx/annotation/Px#`<init>`().
    protected fun resToPx(@DimenRes itemSpacingRes: Int): Int {
//                ^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#resToPx().  protected final fun resToPx(itemSpacingRes: kotlin.Int): kotlin.Int
//                         ^^^^^^^^ reference androidx/annotation/DimenRes#`<init>`().
//                                  ^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#resToPx().(itemSpacingRes)  value-parameter itemSpacingRes: kotlin.Int
//                                                  ^^^ reference kotlin/Int#
//                                                        ^^^ reference kotlin/Int#
        return resources.getDimensionPixelOffset(itemSpacingRes)
//                                               ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#resToPx().(itemSpacingRes)
    }

    override fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
//               ^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setAdapter().  public open fun setAdapter(adapter: [ERROR : RecyclerView.Adapter<*>]<out [ERROR : *]>?)
//                          ^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#setAdapter().(adapter)  value-parameter adapter: [ERROR : RecyclerView.Adapter<*>]<out [ERROR : *]>?
        super.setAdapter(adapter)
//      ^^^^^ reference kotlin/Any#
//                       ^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setAdapter().(adapter)

        clearRemovedAdapterAndCancelRunnable()
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#clearRemovedAdapterAndCancelRunnable().
        updatePreloaders()
//      ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#updatePreloaders().
    }

    override fun swapAdapter(
//               ^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#swapAdapter().  public open fun swapAdapter(adapter: [ERROR : RecyclerView.Adapter<*>]<out [ERROR : *]>?, removeAndRecycleExistingViews: kotlin.Boolean)
        adapter: RecyclerView.Adapter<*>?,
//      ^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#swapAdapter().(adapter)  value-parameter adapter: [ERROR : RecyclerView.Adapter<*>]<out [ERROR : *]>?
        removeAndRecycleExistingViews: Boolean
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#swapAdapter().(removeAndRecycleExistingViews)  value-parameter removeAndRecycleExistingViews: kotlin.Boolean
//                                     ^^^^^^^ reference kotlin/Boolean#
    ) {
        super.swapAdapter(adapter, removeAndRecycleExistingViews)
//      ^^^^^ reference kotlin/Any#
//                        ^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#swapAdapter().(adapter)
//                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#swapAdapter().(removeAndRecycleExistingViews)

        clearRemovedAdapterAndCancelRunnable()
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#clearRemovedAdapterAndCancelRunnable().
        updatePreloaders()
//      ^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#updatePreloaders().
    }

    public override fun onAttachedToWindow() {
//                      ^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#onAttachedToWindow().  public open fun onAttachedToWindow()
        super.onAttachedToWindow()
//      ^^^^^ reference kotlin/Any#

        if (removedAdapter != null) {
//          ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removedAdapter.
//          ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getRemovedAdapter().
//          ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setRemovedAdapter().
            // Restore the adapter that was removed when the view was detached from window
            swapAdapter(removedAdapter, false)
//          ^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#swapAdapter().
//                      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removedAdapter.
//                      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getRemovedAdapter().
//                      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setRemovedAdapter().
        }
        clearRemovedAdapterAndCancelRunnable()
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#clearRemovedAdapterAndCancelRunnable().
    }

    public override fun onDetachedFromWindow() {
//                      ^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#onDetachedFromWindow().  public open fun onDetachedFromWindow()
        super.onDetachedFromWindow()
//      ^^^^^ reference kotlin/Any#
        preloadScrollListeners.forEach { it.cancelPreloadRequests() }
//      ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#preloadScrollListeners.
//      ^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getPreloadScrollListeners().
//                             ^^^^^^^ reference kotlin/collections/CollectionsKt#forEach(+10).
//                                       ^^ reference local15
//                                          ^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/preload/EpoxyPreloader#cancelPreloadRequests().

        if (removeAdapterWhenDetachedFromWindow) {
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removeAdapterWhenDetachedFromWindow.
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getRemoveAdapterWhenDetachedFromWindow().
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setRemoveAdapterWhenDetachedFromWindow().
            if (delayMsWhenRemovingAdapterOnDetach > 0) {
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#delayMsWhenRemovingAdapterOnDetach.
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getDelayMsWhenRemovingAdapterOnDetach().
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setDelayMsWhenRemovingAdapterOnDetach().
//                                                 ^ reference kotlin/Int#compareTo(+3).

                isRemoveAdapterRunnablePosted = true
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#isRemoveAdapterRunnablePosted.
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getIsRemoveAdapterRunnablePosted().
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setIsRemoveAdapterRunnablePosted().
                postDelayed(removeAdapterRunnable, delayMsWhenRemovingAdapterOnDetach.toLong())
//                          ^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removeAdapterRunnable.
//                          ^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getRemoveAdapterRunnable().
//                                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#delayMsWhenRemovingAdapterOnDetach.
//                                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getDelayMsWhenRemovingAdapterOnDetach().
//                                                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setDelayMsWhenRemovingAdapterOnDetach().
//                                                                                    ^^^^^^ reference kotlin/Int#toLong().
            } else {
                removeAdapter()
//              ^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removeAdapter().
            }
        }
        clearPoolIfActivityIsDestroyed()
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#clearPoolIfActivityIsDestroyed().
    }

    private fun removeAdapter() {
//              ^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#removeAdapter().  private final fun removeAdapter()
        val currentAdapter = adapter
//          ^^^^^^^^^^^^^^ definition local16  val currentAdapter: [ERROR : Type for adapter]
        if (currentAdapter != null) {
//          ^^^^^^^^^^^^^^ reference local16
            // Clear the adapter so the adapter releases its reference to this RecyclerView.
            // Views are recycled so they can return to a view pool (default behavior is to not recycle
            // them).
            swapAdapter(null, true)
//          ^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#swapAdapter().
            // Keep a reference to the removed adapter so we can add it back if the recyclerview is
            // attached again.
            removedAdapter = currentAdapter
//          ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removedAdapter.
//          ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getRemovedAdapter().
//          ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setRemovedAdapter().
//                           ^^^^^^^^^^^^^^ reference local16
        }

        // Do this after clearing the adapter, since that sends views back to the pool
        clearPoolIfActivityIsDestroyed()
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#clearPoolIfActivityIsDestroyed().
    }

    private fun clearRemovedAdapterAndCancelRunnable() {
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#clearRemovedAdapterAndCancelRunnable().  private final fun clearRemovedAdapterAndCancelRunnable()
        removedAdapter = null
//      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removedAdapter.
//      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getRemovedAdapter().
//      ^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setRemovedAdapter().
        if (isRemoveAdapterRunnablePosted) {
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#isRemoveAdapterRunnablePosted.
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getIsRemoveAdapterRunnablePosted().
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setIsRemoveAdapterRunnablePosted().
            removeCallbacks(removeAdapterRunnable)
//                          ^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#removeAdapterRunnable.
//                          ^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getRemoveAdapterRunnable().
            isRemoveAdapterRunnablePosted = false
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#isRemoveAdapterRunnablePosted.
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#getIsRemoveAdapterRunnablePosted().
//          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/EpoxyRecyclerView#setIsRemoveAdapterRunnablePosted().
        }
    }

    private fun clearPoolIfActivityIsDestroyed() {
//              ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#clearPoolIfActivityIsDestroyed().  private final fun clearPoolIfActivityIsDestroyed()
        // Views in the pool hold context references which can keep the activity from being GC'd,
        // plus they can hold significant memory resources. We should clear it asap after the pool
        // is no longer needed - the main signal we use for this is that the activity is destroyed.
        if (context.isActivityDestroyed()) {
            recycledViewPool.clear()
        }
    }

    companion object {
//            ^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#Companion#  public companion object
        private const val DEFAULT_ADAPTER_REMOVAL_DELAY_MS = 2000
//                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#Companion#DEFAULT_ADAPTER_REMOVAL_DELAY_MS.  private const final val DEFAULT_ADAPTER_REMOVAL_DELAY_MS: kotlin.Int
//                        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#Companion#getDEFAULT_ADAPTER_REMOVAL_DELAY_MS().  private const final val DEFAULT_ADAPTER_REMOVAL_DELAY_MS: kotlin.Int

        /**
         * Store one unique pool per activity. They are cleared out when activities are destroyed, so this
         * only needs to hold pools for active activities.
         */
        private val ACTIVITY_RECYCLER_POOL = ActivityRecyclerPool()
//                  ^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#Companion#ACTIVITY_RECYCLER_POOL.  private final val ACTIVITY_RECYCLER_POOL: com.airbnb.epoxy.ActivityRecyclerPool
//                  ^^^^^^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/EpoxyRecyclerView#Companion#getACTIVITY_RECYCLER_POOL().  private final val ACTIVITY_RECYCLER_POOL: com.airbnb.epoxy.ActivityRecyclerPool
//                                           ^^^^^^^^^^^^^^^^^^^^ reference com/airbnb/epoxy/ActivityRecyclerPool#`<init>`().
    }
}
