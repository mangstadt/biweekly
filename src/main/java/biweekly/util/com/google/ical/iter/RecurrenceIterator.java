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

import java.util.Iterator;

import biweekly.util.com.google.ical.values.DateValue;

/**
 * Iterates over a series of dates in a recurrence rule in ascending order.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public interface RecurrenceIterator extends Iterator<DateValue> {
  /**
   * Determines if there are more dates in the series.
   * @return true if there are more dates, false if not
   */
  boolean hasNext();

  /**
   * Returns the next date in the series. If {@link #hasNext()} returns
   * {@code false}, then this method's behavior is undefined.
   * @return the next date (in UTC; will be strictly later than any date
   * previously returned by this iterator)
   */
  DateValue next();

  /**
   * Skips all dates in the series that come before the given date.
   * @param newStartUtc the date to advance to (in UTC)
   */
  void advanceTo(DateValue newStartUtc);

  /**
   * Implementors of this interface are not expected to implement this method.
   * @throws UnsupportedOperationException always
   */
  void remove();
}
