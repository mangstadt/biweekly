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

import java.util.Arrays;

import biweekly.util.com.google.ical.values.DateValue;

/**
 * A recurrence iterator that iterates over an array of dates.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class RDateIteratorImpl implements RecurrenceIterator {
  private final DateValue[] datesUtc;
  private int i;

  /**
   * Creates a new recurrence iterator.
   * @param datesUtc the dates to iterate over (assumes they are all in UTC)
   */
  RDateIteratorImpl(DateValue[] datesUtc) {
    datesUtc = datesUtc.clone();
    Arrays.sort(datesUtc);
    this.datesUtc = removeDuplicates(datesUtc);
  }

  public boolean hasNext() { return i < datesUtc.length; }

  public DateValue next() { return datesUtc[i++]; }

  public void remove() { throw new UnsupportedOperationException(); }

  public void advanceTo(DateValue newStartUtc) {
    long startCmp = DateValueComparison.comparable(newStartUtc);
    while (i < datesUtc.length
           && startCmp > DateValueComparison.comparable(datesUtc[i])) {
      ++i;
    }
  }
  
  /**
   * Removes duplicates from a list of date values.
   * @param dates the date values (must be sorted in ascending order)
   * @return a new array if any elements were removed or the original array
   * if no elements were removed
   */
  private static DateValue[] removeDuplicates(DateValue[] dates) {
    int k = 0;
    for (int i = 1; i < dates.length; ++i) {
      if (!dates[i].equals(dates[k])) {
        dates[++k] = dates[i];
      }
    }

    if (++k < dates.length) {
      DateValue[] uniqueDates = new DateValue[k];
      System.arraycopy(dates, 0, uniqueDates, 0, k);
      return uniqueDates;
    }
    return dates;
  }
}
