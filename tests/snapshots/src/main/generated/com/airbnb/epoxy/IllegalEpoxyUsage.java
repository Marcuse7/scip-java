package com.airbnb.epoxy;

public class IllegalEpoxyUsage extends RuntimeException {
//           ^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/IllegalEpoxyUsage# public class IllegalEpoxyUsage
//                                     ^^^^^^^^^^^^^^^^ reference java/lang/RuntimeException#
  public IllegalEpoxyUsage(String message) {
//       ^^^^^^^^^^^^^^^^^ definition com/airbnb/epoxy/IllegalEpoxyUsage#`<init>`(). public IllegalEpoxyUsage(String message)
//                         ^^^^^^ reference java/lang/String#
//                                ^^^^^^^ definition local0 String message
    super(message);
//  ^^^^^ reference java/lang/RuntimeException#`<init>`(+1).
//        ^^^^^^^ reference local0
  }
}
