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

package biweekly.util.com.google.ical.values;

/**
 * <p>
 * A half-open range of {@link DateValue}s.
 * </p>
 * <p>
 * The start is inclusive, and the end is exclusive. The end must be on or after
 * the start. When the start and end are the same, the period is zero width
 * (i.e. contains zero seconds).
 * </p>
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public interface PeriodValue {
  /**
   * Gets the start of the period.
   * @return the start
   */
  DateValue start();

  /**
   * <p>
   * Gets the end of the period.
   * </p>
   * <p>
   * The end date must be:
   * </p>
   * <ul>
   * <li>on or after the start date</li>
   * <li>the same data type as the start date (i.e.,
   * <code>(start() instanceof {@link TimeValue}) ==
   *     (end() instanceof TimeValue)</code>)</li>
   * </ul>
   * @return the end
   */
  DateValue end();

  /**
   * Determines if this period overlaps the given period.
   * @param period the period to compare against
   * @return true if this period overlaps the given period, false if not
   */
  boolean intersects(PeriodValue period);

  /**
   * Determines if this period completely contains the given period.
   * @param period the period to compare against
   * @return true if this period completely contains the given period, false if
   * not
   */
  boolean contains(PeriodValue period);
}
