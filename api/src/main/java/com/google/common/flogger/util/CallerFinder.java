/*
 * Copyright (C) 2012 The Flogger Authors.
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

import static com.google.common.flogger.util.Checks.checkNotNull;

import com.google.errorprone.annotations.CheckReturnValue;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

/** A helper class for determining callers of a specified class currently on the stack. */
@CheckReturnValue
public final class CallerFinder {
  private static final StackGetter STACK_GETTER = StackGetter.getBestStackGetter();

  /**
   * Returns the stack trace element of the immediate caller of the specified class.
   *
   * @param target the target class whose callers we are looking for.
   * @param skip the minimum number of calls known to have occurred between the first call to the
   *     target class and the point at which the specified throwable was created. If in doubt,
   *     specify zero here to avoid accidentally skipping past the caller. This is particularly
   *     important for code which might be used in Android, since you cannot know whether a tool
   *     such as Proguard has merged methods or classes and reduced the number of intermediate stack
   *     frames.
   * @return the stack trace element representing the immediate caller of the specified class, or
   *     null if no caller was found (due to incorrect target, wrong skip count or use of JNI).
   */
  @NullableDecl
  public static StackTraceElement findCallerOf(Class<?> target, int skip) {
    checkNotNull(target, "target");
    if (skip < 0) {
      throw new IllegalArgumentException("skip count cannot be negative: " + skip);
    }
    return STACK_GETTER.callerOf(target, skip + 1);
  }

  /**
   * Returns a synthetic stack trace starting at the immediate caller of the specified target.
   *
   * @param target the class who caller the returned stack trace will start at.
   * @param maxDepth the maximum size of the returned stack (pass -1 for the complete stack).
   * @param skip the minimum number of stack frames to skip before looking for callers.
   * @return a synthetic stack trace starting at the immediate caller of the specified target, or
   *     the empty array if no caller was found (due to incorrect target, wrong skip count or use of
   *     JNI).
   */
  public static StackTraceElement[] getStackForCallerOf(Class<?> target, int maxDepth, int skip) {
    checkNotNull(target, "target");
    if (maxDepth <= 0 && maxDepth != -1) {
      throw new IllegalArgumentException("invalid maximum depth: " + maxDepth);
    }
    if (skip < 0) {
      throw new IllegalArgumentException("skip count cannot be negative: " + skip);
    }
    return STACK_GETTER.getStackForCaller(target, maxDepth, skip + 1);
  }
}
