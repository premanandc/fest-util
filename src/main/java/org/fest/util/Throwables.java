/*
 * Created on Dec 13, 2008
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * Copyright @2008-2013 the original author or authors.
 */
package org.fest.util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static org.fest.util.Lists.newArrayList;
import static org.fest.util.Preconditions.checkNotNull;

/**
 * Utility methods related to {@link Throwable}s.
 *
 * @author Alex Ruiz
 */
// TODO(Alex): Clean up this code.
public final class Throwables {
  private Throwables() {
  }

  /**
   * Appends the stack trace of the current thread to the one in the given {@link Throwable}.
   *
   * @param t              the given {@code Throwable}.
   * @param startingMethod the name of the method used as the starting point of the current thread's stack trace.
   */
  // TODO(Alex): Rename to 'appendStackTrace'
  public static void appendStackTraceInCurentThreadToThrowable(@Nonnull Throwable t, @Nonnull String startingMethod) {
    List<StackTraceElement> stackTrace = newArrayList(checkNotNull(t.getStackTrace()));
    stackTrace.addAll(filteredStackTrace(startingMethod));
    t.setStackTrace(stackTrace.toArray(new StackTraceElement[stackTrace.size()]));
  }

  private static @Nonnull List<StackTraceElement> filteredStackTrace(@Nonnull String startingMethod) {
    List<StackTraceElement> filtered = stackTraceInCurrentThread();
    List<StackTraceElement> toRemove = new ArrayList<StackTraceElement>();
    for (StackTraceElement e : filtered) {
      if (startingMethod.equals(e.getMethodName())) {
        break;
      }
      toRemove.add(e);
    }
    filtered.removeAll(toRemove);
    return filtered;
  }

  private static List<StackTraceElement> stackTraceInCurrentThread() {
    StackTraceElement[] stackTrace = checkNotNull(Thread.currentThread().getStackTrace());
    return newArrayList(stackTrace);
  }

  /**
   * Removes the FEST-related elements from the {@link Throwable} stack trace that have little value for end user.
   * Therefore, instead of seeing this:
   * <p/>
   * <pre>
   * org.junit.ComparisonFailure: expected:<'[Ronaldo]'> but was:<'[Messi]'>
   *   at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
   *   at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
   *   at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
   *   at java.lang.reflect.Constructor.newInstance(Constructor.java:501)
   *   at org.fest.assertions.error.ConstructorInvoker.newInstance(ConstructorInvoker.java:34)
   *   at org.fest.assertions.error.ShouldBeEqual.newComparisonFailure(ShouldBeEqual.java:111)
   *   at org.fest.assertions.error.ShouldBeEqual.comparisonFailure(ShouldBeEqual.java:103)
   *   at org.fest.assertions.error.ShouldBeEqual.newAssertionError(ShouldBeEqual.java:81)
   *   at org.fest.assertions.internal.Failures.failure(Failures.java:76)
   *   at org.fest.assertions.internal.Objects.assertEqual(Objects.java:116)
   *   at org.fest.assertions.api.AbstractAssert.isEqualTo(AbstractAssert.java:74)
   *   at examples.StackTraceFilterExample.main(StackTraceFilterExample.java:13)
   * </pre>
   * <p/>
   * We get this:
   * <p/>
   * <pre>
   * org.junit.ComparisonFailure: expected:<'[Ronaldo]'> but was:<'[Messi]'>
   *   at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
   *   at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
   *   at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
   *   at examples.StackTraceFilterExample.main(StackTraceFilterExample.java:20)
   * </pre>
   *
   * @param throwable the {@code Throwable} to filter stack trace.
   */
  // TODO(Alex): Rename to 'removeFestFromStackTrace'
  public static void removeFestRelatedElementsFromStackTrace(@Nonnull Throwable throwable) {
    StackTraceElement[] stackTrace = checkNotNull(throwable.getStackTrace());
    List<StackTraceElement> filtered = newArrayList(stackTrace);
    StackTraceElement previous = null;
    for (StackTraceElement element : throwable.getStackTrace()) {
      if (element.getClassName().contains("org.fest")) {
        filtered.remove(element);
        // Handle the case when FEST builds a ComparisonFailure by reflection (see ShouldBeEqual.newAssertionError
        // method), the stack trace looks like:
        //
        // java.lang.reflect.Constructor.newInstance(Constructor.java:501),
        // org.fest.assertions.error.ConstructorInvoker.newInstance(ConstructorInvoker.java:34),
        //
        // We want to remove java.lang.reflect.Constructor.newInstance element because it is related to FEST.
        if (previous != null && previous.getClassName().equals("java.lang.reflect.Constructor")
            && element.getClassName().contains("org.fest.assertions.error.ConstructorInvoker")) {
          filtered.remove(previous);
        }
      }
      previous = element;
    }
    StackTraceElement[] newStackTrace = filtered.toArray(new StackTraceElement[filtered.size()]);
    throwable.setStackTrace(newStackTrace);
  }
}
