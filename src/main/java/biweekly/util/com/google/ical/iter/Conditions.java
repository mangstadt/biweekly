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

import biweekly.util.com.google.ical.util.Predicate;
import biweekly.util.com.google.ical.values.DateValue;

/**
 * Factory for predicates used to test whether a recurrence is over.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class Conditions {
  /**
   * Constructs a condition that fails after counting a certain number of dates.
   * @param count the number of dates to count before the condition fails
   * @return the condition
   */
  static Predicate<DateValue> countCondition(final int count) {
    return new Predicate<DateValue>() {
	  private static final long serialVersionUID = -3770774958208833665L;
	  int count_ = count;
      public boolean apply(DateValue value) {
        return --count_ >= 0;
      }
      @Override
      public String toString() {
        return "CountCondition:" + count_;
      }
    };
  }

  /**
   * Constructs a condition that passes all dates that are less than or equal to the given date.
   * @param until the date
   * @return the condition
   */
  static Predicate<DateValue> untilCondition(final DateValue until) {
    return new Predicate<DateValue>() {
      private static final long serialVersionUID = -130394842437801858L;
      public boolean apply(DateValue date) {
        return date.compareTo(until) <= 0;
      }
      @Override
      public String toString() {
        return "UntilCondition:" + until;
      }
    };
  }

  private Conditions() {
    //uninstantiable
  }
}
