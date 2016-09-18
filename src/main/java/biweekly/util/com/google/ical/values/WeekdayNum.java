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
 * Represents a day of the week in a month or year such as the third Monday of
 * the month, or the last Monday of the month.
 * </p>
 * <blockquote> Each BYDAY value can also be preceded by a positive (+n) or
 * negative (-n) integer. If present, this indicates the nth occurrence of the
 * specific day within the MONTHLY or YEARLY RRULE. For example, within a
 * MONTHLY rule, +1MO (or simply 1MO) represents the first Monday within the
 * month, whereas -1MO represents the last Monday of the month. If an integer
 * modifier is not present, it means all days of this type within the specified
 * frequency. For example, within a MONTHLY rule, MO represents all Mondays
 * within the month. </blockquote>
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class WeekdayNum {
  public final int num;
  public final Weekday wday;

  /**
   * Creates a new weekday number.
   * @param num the number (must be in range [-53,53])
   * @param wday the day of the week
   * @throws IllegalArgumentException if the number is not the proper range, or
   * the day of the week is null
   */
  public WeekdayNum(int num, Weekday wday) {
    if (!(num > -53 && num < 53 && wday != null)) {
      throw new IllegalArgumentException();
    }
    this.num = num;
    this.wday = wday;
  }

  public String toIcal() {
    return (this.num != 0)
        ? String.valueOf(this.num) + this.wday
        : this.wday.toString();
  }

  @Override
  public String toString() {
    return toIcal();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof WeekdayNum)) { return false; }
    WeekdayNum that = (WeekdayNum) o;
    return this.num == that.num && this.wday == that.wday;
  }

  @Override
  public int hashCode() {
    return num ^ (53 * wday.hashCode());
  }
}
