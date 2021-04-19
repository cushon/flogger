/*
 * Copyright (C) 2021 The Flogger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.flogger.util;

/** Interface for finding call site information. */
abstract class StackGetter {
  /**
   * Returns the first caller of a method on the {@code target} class that is *not* a member of
   * {@code target} class, by walking back on the stack, or null if the {@code target} class cannot
   * be found or is the last element of the stack.
   *
   * @param target the class to find the caller of
   * @param skipFrames skip this many frames before looking for the caller. This can be used for
   *     optimization.
   */
  public abstract StackTraceElement callerOf(Class<?> target, int skipFrames);

  /**
   * Returns up to {@code maxDepth} frames of the stack starting at the stack frame that is a caller
   * of a method on {@code target} class but is *not* itself a method on {@code target} class.
   *
   * @param target the class to get the stack from
   * @param maxDepth the maximum depth of the stack to return. A value of -1 means to return the
   *     whole stack
   * @param skipFrames skip this many stack frames before looking for the target class. Used for
   *     optimization.
   * @throws IllegalArgumentException if {@code maxDepth} is 0 or < -1 or {@code skipFrames} is < 0.
   */
  public abstract StackTraceElement[] getStackForCaller(
      Class<?> target, int maxDepth, int skipFrames);

  private static final String[] STACK_GETTER_IMPLEMENTATIONS = {
    "com.google.common.flogger.util.StackWalkerStackGetter",
    "com.google.common.flogger.util.JavaLangAccessStackGetter",
  };

  /**
   * Returns the first available class implementing the {@link StackGetter} methods. The
   * implementation returned is dependent on the current java version.
   */
  static StackGetter getBestStackGetter() {
    for (String stackGetterName : STACK_GETTER_IMPLEMENTATIONS) {
      StackGetter stackGetter = maybeCreateStackGetter(stackGetterName);
      if (stackGetter != null) {
        return stackGetter;
      }
    }
    return new ThrowableStackGetter();
  }

  private static StackGetter maybeCreateStackGetter(String className) {
    try {
      return Class.forName(className)
          .asSubclass(StackGetter.class)
          .getDeclaredConstructor()
          .newInstance();
    } catch (Throwable t) {
      // We may not be able to create one or both of the implementations in some cases, like for
      // exmaple on Android. This is not a problem because we have ThrowableStackGetter as a
      // fallback.
      // Note, this Throwable is replacing ClassNotFoundException, NoSuchMethodException,
      // InstantiationException, IllegalAccessException, InvocationTargetException, LinkageError,
      // because multi-catch is not available with -source 6 and we would need a separate empty
      // catch clause for all of those. Also, we generally don't want this method to throw any kind
      // of Exception.
    }
    return null;
  }
}
