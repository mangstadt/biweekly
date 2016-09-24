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

import static biweekly.util.TestUtils.date;
import junit.framework.TestCase;
import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class StressTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();

    // prime the VM
    for (int runs = 10; --runs >= 0;) {
      runOne();
    }
  }

  public void testSpeed() {
    long t0 = System.nanoTime();
    //cycle through 10 recurrence rules, advancing and pulling a few dates off each
    for (int runs = 5000; --runs >= 0;) {
      runOne();
    }
    long dt = System.nanoTime() - t0;
    System.out.println(getName() + " took " + (dt / 1e6) + " ms");
  }

  static Recurrence[] RECURRENCE_RULES = {
    new Recurrence.Builder(Frequency.DAILY).build(),
    new Recurrence.Builder(Frequency.WEEKLY)
      .byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
      .build(),
    new Recurrence.Builder(Frequency.WEEKLY)
      .byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
      .count(10)
      .build(),
    new Recurrence.Builder(Frequency.WEEKLY)
      .byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
      .until(new ICalDate(date("2006-08-01"), false))
      .build(),
    new Recurrence.Builder(Frequency.MONTHLY)
      .interval(2)
      .byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
      .until(new ICalDate(date("2006-08-01"), false))
      .build(),
    new Recurrence.Builder(Frequency.MONTHLY)
      .byMonthDay(13)
      .byDay(DayOfWeek.FRIDAY)
      .until(new ICalDate(date("2006-08-01"), false))
      .build(),
    new Recurrence.Builder(Frequency.YEARLY)
      .byMonth(6)
      .byMonthDay(15)
      .until(new ICalDate(date("2020-06-15"), false))
      .build()
  };

  private static final DateValue DT_START = new DateValueImpl(2006, 4, 3);
  private static final DateValue T0 = new DateValueImpl(2006, 8, 3);

  private void runOne() {
    for (Recurrence rrule : RECURRENCE_RULES) {
      RecurrenceIterator iter =
        RecurrenceIteratorFactory.createRecurrenceIterator(
            rrule, DT_START, TimeUtils.utcTimezone());
      iter.advanceTo(T0);
      for (int k = 20; iter.hasNext() && --k >= 0;) {
        iter.next();
      }
    }
  }
}
