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

package biweekly.util.com.google.ical.compat.javautil;

import biweekly.util.com.google.ical.iter.RecurrenceIterable;
import biweekly.util.com.google.ical.iter.RecurrenceIterator;
import biweekly.util.com.google.ical.iter.RecurrenceIteratorFactory;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;
import biweekly.util.com.google.ical.values.TimeValue;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * A factory for converting RRULEs and RDATEs into
 * <code>Iterator&lt;Date&gt;</code> and <code>Iterable&lt;Date&gt;</code>.
 * @see RecurrenceIteratorFactory
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class DateIteratorFactory {
  /**
   * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
   * them into a single date iterator.
   * @param rdata RRULE, EXRULE, RDATE, and EXDATE lines.
   * @param start the first occurrence of the series.
   * @param tzid the local timezone -- used to interpret start and any dates in
   *   RDATE and EXDATE lines that don't have TZID params.
   * @param strict true if any failure to parse should result in a
   *   ParseException.  false causes bad content lines to be logged and ignored.
   */
  public static DateIterator createDateIterator(
      String rdata, Date start, TimeZone tzid, boolean strict)
      throws ParseException {
    return new RecurrenceIteratorWrapper(
        RecurrenceIteratorFactory.createRecurrenceIterator(
            rdata, dateToDateValue(start, true),
            tzid, strict));
  }

  /**
   * given a block of RRULE, EXRULE, RDATE, and EXDATE content lines, parse
   * them into a single date iterable.
   * @param rdata RRULE, EXRULE, RDATE, and EXDATE lines.
   * @param start the first occurrence of the series.
   * @param tzid the local timezone -- used to interpret start and any dates in
   *   RDATE and EXDATE lines that don't have TZID params.
   * @param strict true if any failure to parse should result in a
   *   ParseException.  false causes bad content lines to be logged and ignored.
   */
  public static DateIterable createDateIterable(
      String rdata, Date start, TimeZone tzid, boolean strict)
      throws ParseException {
    return new RecurrenceIterableWrapper(
        RecurrenceIteratorFactory.createRecurrenceIterable(
            rdata, dateToDateValue(start, true),
            tzid, strict));
  }

  /**
   * Creates a date iterator from a recurrence iterator.
   * @param rit the recurrence iterator
   * @return the date iterator
   */
  public static DateIterator createDateIterator(RecurrenceIterator rit) {
    return new RecurrenceIteratorWrapper(rit);
  }

  private static final class RecurrenceIterableWrapper
      implements DateIterable {
    private final RecurrenceIterable it;

    public RecurrenceIterableWrapper(RecurrenceIterable it) { this.it = it; }

    public DateIterator iterator() {
      return new RecurrenceIteratorWrapper(it.iterator());
    }
  }

  private static final class RecurrenceIteratorWrapper
      implements DateIterator {
    private final RecurrenceIterator it;
    RecurrenceIteratorWrapper(RecurrenceIterator it) { this.it = it; }
    public boolean hasNext() { return it.hasNext(); }
    public Date next() { return dateValueToDate(it.next()); }
    public void remove() { throw new UnsupportedOperationException(); }
    public void advanceTo(Date d) {
      /*
       * We need to treat midnight as a date value so that passing in
       * dateValueToDate(<some-date-value>) will not advance past any
       * occurrences of some-date-value in the iterator.
       */
      it.advanceTo(dateToDateValue(d, true));
    }
  }

  /**
   * Converts a {@link DateValue} object into a Java {@link Date} object.
   * @param dateUtc the date value object (assumed to be in UTC time)
   * @return the Java date object
   */
  static Date dateValueToDate(DateValue dateUtc) {
    TimeValue tvUtc = TimeUtils.timeOf(dateUtc);
    GregorianCalendar c = new GregorianCalendar(TimeUtils.utcTimezone());
    c.clear();
    c.set(
      dateUtc.year(),
      dateUtc.month() - 1, //java.util's dates are zero-indexed
      dateUtc.day(),
      tvUtc.hour(),
      tvUtc.minute(),
      tvUtc.second()
    );
    return c.getTime();
  }

  /**
   * Converts a Java {@link Date} object into a {@link DateValue} object. The
   * {@link DateValue} object will be in UTC time.
   * @param date the Java date object
   * @param midnightAsDate true to return a date value without a time component
   * if the Java date's time is set to midnight, or false to still return a
   * date-time value
   * @return the date value object (in UTC time)
   */
  static DateValue dateToDateValue(Date date, boolean midnightAsDate) {
    GregorianCalendar c = new GregorianCalendar(TimeUtils.utcTimezone());
    c.setTime(date);
    
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH) + 1; //java.util's dates are zero-indexed
    int day = c.get(Calendar.DAY_OF_MONTH);
    int hour = c.get(Calendar.HOUR_OF_DAY);
    int minute = c.get(Calendar.MINUTE);
    int second = c.get(Calendar.SECOND);

    if (midnightAsDate && 0 == (hour | minute | second)) {
      return new DateValueImpl(year, month, day);
    }
    return new DateTimeValueImpl(year, month, day, hour, minute, second);
  }

  private DateIteratorFactory() {
    // uninstantiable
  }
}
