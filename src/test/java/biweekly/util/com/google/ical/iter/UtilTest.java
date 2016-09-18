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

import biweekly.util.DayOfWeek;
import biweekly.util.com.google.ical.values.DateValueImpl;
import biweekly.util.com.google.ical.values.WeekdayNum;
import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class UtilTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testDayNumToDateInMonth() throws Exception {
    //        March 2006
    // Su Mo Tu We Th Fr Sa
    //           1  2  3  4
    //  5  6  7  8  9 10 11
    // 12 13 14 15 16 17 18
    // 19 20 21 22 23 24 25
    // 26 27 28 29 30 31
    DayOfWeek dow0 = DayOfWeek.WEDNESDAY;
    int nDays = 31;
    int d0 = 0;

    assertEquals(1, Util.dayNumToDate(dow0, nDays, 1, DayOfWeek.WEDNESDAY, d0, nDays));
    assertEquals(8, Util.dayNumToDate(dow0, nDays, 2, DayOfWeek.WEDNESDAY, d0, nDays));
    assertEquals(29, Util.dayNumToDate(dow0, nDays, -1, DayOfWeek.WEDNESDAY, d0, nDays));
    assertEquals(22, Util.dayNumToDate(dow0, nDays, -2, DayOfWeek.WEDNESDAY, d0, nDays));

    assertEquals(3, Util.dayNumToDate(dow0, nDays, 1, DayOfWeek.FRIDAY, d0, nDays));
    assertEquals(10, Util.dayNumToDate(dow0, nDays, 2, DayOfWeek.FRIDAY, d0, nDays));
    assertEquals(31, Util.dayNumToDate(dow0, nDays, -1, DayOfWeek.FRIDAY, d0, nDays));
    assertEquals(24, Util.dayNumToDate(dow0, nDays, -2, DayOfWeek.FRIDAY, d0, nDays));

    assertEquals(7, Util.dayNumToDate(dow0, nDays, 1, DayOfWeek.TUESDAY, d0, nDays));
    assertEquals(14, Util.dayNumToDate(dow0, nDays, 2, DayOfWeek.TUESDAY, d0, nDays));
    assertEquals(28, Util.dayNumToDate(dow0, nDays, 4, DayOfWeek.TUESDAY, d0, nDays));
    assertEquals(0, Util.dayNumToDate(dow0, nDays, 5, DayOfWeek.TUESDAY, d0, nDays));
    assertEquals(28, Util.dayNumToDate(dow0, nDays, -1, DayOfWeek.TUESDAY, d0, nDays));
    assertEquals(21, Util.dayNumToDate(dow0, nDays, -2, DayOfWeek.TUESDAY, d0, nDays));
    assertEquals(7, Util.dayNumToDate(dow0, nDays, -4, DayOfWeek.TUESDAY, d0, nDays));
    assertEquals(0, Util.dayNumToDate(dow0, nDays, -5, DayOfWeek.TUESDAY, d0, nDays));
  }

  public void testDayNumToDateInYear() throws Exception {
    //        January 2006
    //  # Su Mo Tu We Th Fr Sa
    //  1  1  2  3  4  5  6  7
    //  2  8  9 10 11 12 13 14
    //  3 15 16 17 18 19 20 21
    //  4 22 23 24 25 26 27 28
    //  5 29 30 31

    //      February 2006
    //  # Su Mo Tu We Th Fr Sa
    //  5           1  2  3  4
    //  6  5  6  7  8  9 10 11
    //  7 12 13 14 15 16 17 18
    //  8 19 20 21 22 23 24 25
    //  9 26 27 28

    //           March 2006
    //  # Su Mo Tu We Th Fr Sa
    //  9           1  2  3  4
    // 10  5  6  7  8  9 10 11
    // 11 12 13 14 15 16 17 18
    // 12 19 20 21 22 23 24 25
    // 13 26 27 28 29 30 31

    DayOfWeek dow0 = DayOfWeek.SUNDAY;
    int nInMonth = 31;
    int nDays = 365;
    int d0 = 59;

    // TODO(msamuel): check that these answers are right
    assertEquals(
        1, Util.dayNumToDate(dow0, nDays, 9, DayOfWeek.WEDNESDAY, d0, nInMonth));
    assertEquals(
        8, Util.dayNumToDate(dow0, nDays, 10, DayOfWeek.WEDNESDAY, d0, nInMonth));
    assertEquals(
        29, Util.dayNumToDate(dow0, nDays, -40, DayOfWeek.WEDNESDAY, d0, nInMonth));
    assertEquals(
        22, Util.dayNumToDate(dow0, nDays, -41, DayOfWeek.WEDNESDAY, d0, nInMonth));

    assertEquals(
        3, Util.dayNumToDate(dow0, nDays, 9, DayOfWeek.FRIDAY, d0, nInMonth));
    assertEquals(
        10, Util.dayNumToDate(dow0, nDays, 10, DayOfWeek.FRIDAY, d0, nInMonth));
    assertEquals(
        31, Util.dayNumToDate(dow0, nDays, -40, DayOfWeek.FRIDAY, d0, nInMonth));
    assertEquals(
        24, Util.dayNumToDate(dow0, nDays, -41, DayOfWeek.FRIDAY, d0, nInMonth));

    assertEquals(
        7, Util.dayNumToDate(dow0, nDays, 10, DayOfWeek.TUESDAY, d0, nInMonth));
    assertEquals(
        14, Util.dayNumToDate(dow0, nDays, 11, DayOfWeek.TUESDAY, d0, nInMonth));
    assertEquals(
        28, Util.dayNumToDate(dow0, nDays, 13, DayOfWeek.TUESDAY, d0, nInMonth));
    assertEquals(
        0, Util.dayNumToDate(dow0, nDays, 14, DayOfWeek.TUESDAY, d0, nInMonth));
    assertEquals(
        28, Util.dayNumToDate(dow0, nDays, -40, DayOfWeek.TUESDAY, d0, nInMonth));
    assertEquals(
        21, Util.dayNumToDate(dow0, nDays, -41, DayOfWeek.TUESDAY, d0, nInMonth));
    assertEquals(
        7, Util.dayNumToDate(dow0, nDays, -43, DayOfWeek.TUESDAY, d0, nInMonth));
    assertEquals(
        0, Util.dayNumToDate(dow0, nDays, -44, DayOfWeek.TUESDAY, d0, nInMonth));
  }

  public void testUniquify() throws Exception {
    int[] ints = new int[] { 1, 4, 4, 2, 7, 3, 8, 0, 0, 3 };
    ints = Util.uniquify(ints);
    assertEquals("0,1,2,3,4,7,8", arrToString(ints));
  }

  public void testNextWeekStart() throws Exception {
    assertEquals(new DateValueImpl(2006, 1, 24),
                 Util.nextWeekStart(new DateValueImpl(2006, 1, 23),
                                    DayOfWeek.TUESDAY));

    assertEquals(new DateValueImpl(2006, 1, 24),
                 Util.nextWeekStart(new DateValueImpl(2006, 1, 24),
                                    DayOfWeek.TUESDAY));

    assertEquals(new DateValueImpl(2006, 1, 31),
                 Util.nextWeekStart(new DateValueImpl(2006, 1, 25),
                                    DayOfWeek.TUESDAY));

    assertEquals(new DateValueImpl(2006, 1, 23),
                 Util.nextWeekStart(new DateValueImpl(2006, 1, 23),
                                    DayOfWeek.MONDAY));

    assertEquals(new DateValueImpl(2006, 1, 30),
                 Util.nextWeekStart(new DateValueImpl(2006, 1, 24),
                                    DayOfWeek.MONDAY));

    assertEquals(new DateValueImpl(2006, 1, 30),
                 Util.nextWeekStart(new DateValueImpl(2006, 1, 25),
                                    DayOfWeek.MONDAY));

    assertEquals(new DateValueImpl(2006, 2, 6),
                 Util.nextWeekStart(new DateValueImpl(2006, 1, 31),
                                    DayOfWeek.MONDAY));
  }

  public void testCountInPeriod() throws Exception {
    //        January 2006
    //  Su Mo Tu We Th Fr Sa
    //   1  2  3  4  5  6  7
    //   8  9 10 11 12 13 14
    //  15 16 17 18 19 20 21
    //  22 23 24 25 26 27 28
    //  29 30 31
    assertEquals(5, Util.countInPeriod(DayOfWeek.SUNDAY, DayOfWeek.SUNDAY, 31));
    assertEquals(5, Util.countInPeriod(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, 31));
    assertEquals(5, Util.countInPeriod(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY, 31));
    assertEquals(4, Util.countInPeriod(DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY, 31));
    assertEquals(4, Util.countInPeriod(DayOfWeek.THURSDAY, DayOfWeek.SUNDAY, 31));
    assertEquals(4, Util.countInPeriod(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY, 31));
    assertEquals(4, Util.countInPeriod(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, 31));

    //      February 2006
    //  Su Mo Tu We Th Fr Sa
    //            1  2  3  4
    //   5  6  7  8  9 10 11
    //  12 13 14 15 16 17 18
    //  19 20 21 22 23 24 25
    //  26 27 28
    assertEquals(4, Util.countInPeriod(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY, 28));
    assertEquals(4, Util.countInPeriod(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, 28));
    assertEquals(4, Util.countInPeriod(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 28));
    assertEquals(4, Util.countInPeriod(DayOfWeek.WEDNESDAY, DayOfWeek.WEDNESDAY, 28));
    assertEquals(4, Util.countInPeriod(DayOfWeek.THURSDAY, DayOfWeek.WEDNESDAY, 28));
    assertEquals(4, Util.countInPeriod(DayOfWeek.FRIDAY, DayOfWeek.WEDNESDAY, 28));
    assertEquals(4, Util.countInPeriod(DayOfWeek.SATURDAY, DayOfWeek.WEDNESDAY, 28));
  }

  public void testInvertWeekdayNum() throws Exception {

    //        January 2006
    //  # Su Mo Tu We Th Fr Sa
    //  1  1  2  3  4  5  6  7
    //  2  8  9 10 11 12 13 14
    //  3 15 16 17 18 19 20 21
    //  4 22 23 24 25 26 27 28
    //  5 29 30 31

    // the 1st falls on a sunday, so dow0 == SU
    assertEquals(
        5,
        Util.invertWeekdayNum(new WeekdayNum(-1, DayOfWeek.SUNDAY), DayOfWeek.SUNDAY, 31));
    assertEquals(
        5,
        Util.invertWeekdayNum(new WeekdayNum(-1, DayOfWeek.MONDAY), DayOfWeek.SUNDAY, 31));
    assertEquals(
        5,
        Util.invertWeekdayNum(new WeekdayNum(-1, DayOfWeek.TUESDAY), DayOfWeek.SUNDAY, 31));
    assertEquals(
        4,
        Util.invertWeekdayNum(new WeekdayNum(-1, DayOfWeek.WEDNESDAY), DayOfWeek.SUNDAY, 31));
    assertEquals(
        3,
        Util.invertWeekdayNum(new WeekdayNum(-2, DayOfWeek.WEDNESDAY), DayOfWeek.SUNDAY, 31));


    //      February 2006
    //  # Su Mo Tu We Th Fr Sa
    //  1           1  2  3  4
    //  2  5  6  7  8  9 10 11
    //  3 12 13 14 15 16 17 18
    //  4 19 20 21 22 23 24 25
    //  5 26 27 28

    assertEquals(
        4,
        Util.invertWeekdayNum(new WeekdayNum(-1, DayOfWeek.SUNDAY), DayOfWeek.WEDNESDAY, 28));
    assertEquals(
        4,
        Util.invertWeekdayNum(new WeekdayNum(-1, DayOfWeek.MONDAY), DayOfWeek.WEDNESDAY, 28));
    assertEquals(
        4,
        Util.invertWeekdayNum(new WeekdayNum(-1, DayOfWeek.TUESDAY), DayOfWeek.WEDNESDAY, 28));
    assertEquals(
        4,
        Util.invertWeekdayNum(new WeekdayNum(-1, DayOfWeek.WEDNESDAY), DayOfWeek.WEDNESDAY, 28));
    assertEquals(
        3,
        Util.invertWeekdayNum(new WeekdayNum(-2, DayOfWeek.WEDNESDAY), DayOfWeek.WEDNESDAY, 28));
  }

  private static String arrToString(int[] arr) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < arr.length; ++i) {
      if (0 != i) { sb.append(','); }
      sb.append(arr[i]);
    }
    return sb.toString();
  }
}

            // For dow0 == MO
    //        January 2006
    //  # Mo Tu We Th Fr Sa Su
    //  1                    1
    //  2  2  3  4  5  6  7  8
    //  3  9 10 11 12 13 14 15
    //  4 16 17 18 19 20 21 22
    //  5 23 24 25 26 27 28 29
    //  6 30 31
