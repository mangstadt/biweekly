/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * All Rights Reserved.
 */

package biweekly.util.com.google.ical.values;

/**
 * A calendar date.
 *
 * @author Neal Gafter
 */
public class DateValueImpl implements DateValue {
  private final int year, month, day;

  /**
   * Creates a new date value.
   * @param year the year
   * @param month the month (1-12)
   * @param day the day (1-31)
   */
  public DateValueImpl(int year, int month, int day) {
    this.year = year;
    this.month = month;
    this.day = day;
  }

  public int year() {
    return year;
  }

  public int month() {
    return month;
  }

  public int day() {
    return day;
  }

  @Override
  public String toString() {
    return String.format("%04d%02d%02d", year, month, day);
  }

  public final int compareTo(DateValue other) {
    int n0 = day() +               //5 bits
             (month() << 5) +      //4 bits
             (year() << 9);
    int n1 = other.day() +
             (other.month() << 5) +
             (other.year() << 9);
    if (n0 != n1) return n0 - n1;
    if (!(this instanceof TimeValue))
      return (other instanceof TimeValue) ? -1 : 0;

    TimeValue self = (TimeValue) this;
    if (!(other instanceof TimeValue)) return 1;
    TimeValue othr = (TimeValue) other;
    int m0 = self.second() +            //6 bits
             (self.minute() << 6) +     //6 bits
             (self.hour() << 12);
    int m1 = othr.second() +
             (othr.minute() << 6) +
             (othr.hour() << 12);
    return m0 - m1;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DateValue)) { return false; }
    return compareTo((DateValue) o) == 0;
  }

  @Override
  public int hashCode() {
    return (year() << 9) + (month() << 5) + day();
  }
}
