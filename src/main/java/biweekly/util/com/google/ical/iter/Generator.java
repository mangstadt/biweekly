// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package biweekly.util.com.google.ical.iter;

import biweekly.util.com.google.ical.util.DTBuilder;

/**
 * <p>
 * A stateful operation that can be successively invoked to generate the next
 * part of a date in a series.
 * </p>
 * <p>
 * Each field generator takes as input the larger fields, modifies its field,
 * and leaves the other fields unchanged. For example, a year generator will
 * update {@link DTBuilder#year}, leaving the smaller fields unchanged. And a
 * month generator will update {@link DTBuilder#month}, taking its cue from
 * {@link DTBuilder#year}, also leaving the smaller fields unchanged.
 * </p>
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
abstract class Generator {
  /**
   * <p>
   * Generates the next part of a date in a series.
   * </p>
   * <p>
   * If a generator is exhausted, generating a new value of a larger field may
   * allow it to continue. For example, a month generator that runs out of
   * months at 12, may start over at 1 if called with a {@link DTBuilder} with a
   * different year.
   * </p>
   * @param bldr used for both input and output, modified in place
   * @return true iff there are more instances of the generator's field to
   * generate
   * @throws IteratorShortCircuitingException when an iterator reaches a
   * threshold past which it cannot generate any more dates. This indicates that
   * the entire iteration process should end.
   */
  abstract boolean generate(DTBuilder bldr)
      throws IteratorShortCircuitingException;

  /**
   * <p>
   * Thrown when an iteration process should be ended completely due to an
   * artificial system limit. This allows us to make a distinction between
   * normal exhaustion of iteration, and an artificial limit that may fall in a
   * set, and so affect subsequent evaluation of BYSETPOS rules.
   * </p>
   * <p>
   * Since this class is meant to be thrown as a flow control construct to
   * indicate an artificial limit has been reached (as opposed to an exceptional
   * condition), and since its clients have no need of the stacktrace, we use a
   * singleton to avoid forcing the JVM to unoptimize and decompile the
   * {@link RecurrenceIterator}'s inner loop.
   * </p>
   */
  @SuppressWarnings("serial")
  static class IteratorShortCircuitingException extends Exception {
    private IteratorShortCircuitingException() {
      super();
      setStackTrace(new StackTraceElement[0]);
    }

    private static final IteratorShortCircuitingException INSTANCE =
        new IteratorShortCircuitingException();

    static IteratorShortCircuitingException instance() { return INSTANCE; }
  }

  static {
    /*
     * Suffer the stack trace generation on class load of Generator, which will
     * happen before any of the recurrence stuff could possibly have been JIT
     * compiled.
     */
    IteratorShortCircuitingException.instance();
  }
}
