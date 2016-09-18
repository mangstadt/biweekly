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

import biweekly.util.com.google.ical.util.TimeUtils;

/**
 * A half-open range of {@link DateValue}s.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class PeriodValueImpl implements PeriodValue {
  private DateValue start, end;

  /**
   * Creates a period with the given start and end dates.
   * @param start the start date
   * @param end the end date (must be on or after the start date and must be the
   * same data type as the start date).
   * @return the period
   * @throws IllegalArgumentException if the start date comes after the end
   * date, or the start and end dates are not the same type (e.g. they are not
   * both date values or both date-time values)
   */
  public static PeriodValue create(DateValue start, DateValue end) {
    return new PeriodValueImpl(start, end);
  }

  /**
   * Creates a period with the given start date and duration.
   * @param start the start date
   * @param duration the duration (must be positive)
   * @return the period
   * @throws IllegalArgumentException if the duration is negative
   */
  public static PeriodValue createFromDuration(DateValue start, DateValue duration) {
    DateValue end = TimeUtils.add(start, duration);
    if (end instanceof TimeValue && !(start instanceof TimeValue)) {
      start = TimeUtils.dayStart(start);
    }
    return new PeriodValueImpl(start, end);
  }

  /**
   * Creates a new period.
   * @param start the start date
   * @param end the end date
   * @throws IllegalArgumentException if the start date comes after the end
   * date, or the start and end dates are not the same type (e.g. they are not
   * both either both date values or date-time values)
   */
  protected PeriodValueImpl(DateValue start, DateValue end) {
    if (start.compareTo(end) > 0) {
      throw new IllegalArgumentException
          ("Start (" + start + ") must precede end (" + end + ")");
    }
    if ((start instanceof TimeValue) ^ (end instanceof TimeValue)) {
      throw new IllegalArgumentException
        ("Start (" + start + ") and end (" + end +
         ") must both have times or neither have times.");
    }

    this.start = start;
    this.end = end;
  }

  public DateValue start() { return start; }

  public DateValue end() { return end; }

  public boolean intersects(PeriodValue period) {
    DateValue sa = start(),
              ea = end(),
              sb = period.start(),
              eb = period.end();

    return sa.compareTo(eb) < 0 && sb.compareTo(ea) < 0;
  }

  public boolean contains(PeriodValue period) {
    DateValue sa = start(),
              ea = end(),
              sb = period.start(),
              eb = period.end();

    return !(sb.compareTo(sa) < 0 || ea.compareTo(eb) < 0);
  }

  @Override public boolean equals(Object o) {
    if (!(o instanceof PeriodValue)) { return false; }
    PeriodValue that = (PeriodValue) o;
    return this.start().equals(that.start())
        && this.end().equals(that.end());
  }

  @Override public int hashCode() {
    return start().hashCode() ^ (31 * end().hashCode());
  }

  @Override public String toString() {
    return start() + "/" + end();
  }
}
