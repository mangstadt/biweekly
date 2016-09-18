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

import java.util.Calendar;

import biweekly.util.com.google.ical.util.TimeUtils;

/**
 * The days of the week.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public enum Weekday {
  SU(Calendar.SUNDAY),
  MO(Calendar.MONDAY),
  TU(Calendar.TUESDAY),
  WE(Calendar.WEDNESDAY),
  TH(Calendar.THURSDAY),
  FR(Calendar.FRIDAY),
  SA(Calendar.SATURDAY),
  ;
  
  private static final Weekday[] VALUES;
  static {
    Weekday[] values = values();
    VALUES = new Weekday[values.length];
    System.arraycopy(values, 0, VALUES, 0, values.length);
  }
  
  /**
   * The number that represents this day of the week according to Java's
   * {@link Calendar} class.
   */
  public final int javaDayNum;

  Weekday(int javaDayNum) {
    this.javaDayNum = javaDayNum;
  }

  /**
   * Gets the day of the week of the given date.
   * @param date the date
   * @return the day of the week
   */
  public static Weekday valueOf(DateValue date) {
    int dayIndex =
      TimeUtils.fixedFromGregorian(date.year(), date.month(), date.day()) % 7;
    if (dayIndex < 0) { dayIndex += 7; }
    return VALUES[dayIndex];
  }

  /**
   * Gets the day of the week of the first day in the given month.
   * @param year the year
   * @param month the month (1-12)
   * @return the day of the week
   */
  public static Weekday firstDayOfWeekInMonth(int year, int month) {
    int result = TimeUtils.fixedFromGregorian(year, month, 1) % 7;
    if (result < 0) { result += 7; }
    return VALUES[result];
  }

  /**
   * Gets the day of the week that comes after this day of the week.
   * @return the next day
   */
  public Weekday successor() {
    return VALUES[(ordinal() + 1) % 7];
  }

  /**
   * Gets the day of the week that comes before this day of the week.
   * @return the previous day
   */
  public Weekday predecessor() {
    return VALUES[(ordinal() - 1 + 7) % 7];
  }
}
