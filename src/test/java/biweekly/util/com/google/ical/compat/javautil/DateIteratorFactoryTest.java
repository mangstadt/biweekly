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

import static biweekly.util.TestUtils.date;

import java.util.TimeZone;

import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.iter.RecurrenceIterable;
import biweekly.util.com.google.ical.iter.RecurrenceIteratorFactory;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;
import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class DateIteratorFactoryTest extends TestCase {
  private static final TimeZone UTC = TimeUtils.utcTimezone();
  private static final TimeZone PST = TimeZone.getTimeZone("America/Los_Angeles");

  public void testCreateDateIterableUntimed() {
    Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
      .interval(2)
      .count(3)
    .build();
    DateValue start = new DateValueImpl(2006, 1, 1);
    RecurrenceIterable recurIt = RecurrenceIteratorFactory.createRecurrenceIterable(recur, start, PST);
    DateIterable iterable = DateIteratorFactory.createDateIterable(recurIt);

    DateIterator it = iterable.iterator();
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-01", UTC), it.next());
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-03", UTC), it.next());
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-05", UTC), it.next());
    assertFalse(it.hasNext());

    it = iterable.iterator();
    it.advanceTo(date("2006-01-03", UTC));
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-03", UTC), it.next());
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-05", UTC), it.next());
    assertFalse(it.hasNext());
  }

  public void testCreateDateIterableMidnight() {
    Recurrence recur = new Recurrence.Builder(Frequency.HOURLY)
      .interval(2)
      .count(3)
    .build();
    DateValue start = new DateTimeValueImpl(2006, 1, 1, 22, 0, 0);
    RecurrenceIterable recurIt = RecurrenceIteratorFactory.createRecurrenceIterable(recur, start, UTC);
    DateIterable iterable = DateIteratorFactory.createDateIterable(recurIt);

    DateIterator it = iterable.iterator();
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-01 22:00:00", UTC), it.next());
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-02 00:00:00", UTC), it.next());
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-02 02:00:00", UTC), it.next());
    assertFalse(it.hasNext());

    it = iterable.iterator();
    it.advanceTo(date("2006-01-02", UTC));
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-02 00:00:00", UTC), it.next());
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-02 02:00:00", UTC), it.next());
    assertFalse(it.hasNext());
  }

  public void testCreateDateIterableTimed() {
    Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
      .interval(2)
      .count(3)
    .build();
    DateValue start = new DateTimeValueImpl(2006, 1, 1, 12, 30, 1);
    RecurrenceIterable recurIt = RecurrenceIteratorFactory.createRecurrenceIterable(recur, start, PST);
    DateIterable iterable = DateIteratorFactory.createDateIterable(recurIt);

    DateIterator it = iterable.iterator();
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-01 12:30:01", PST), it.next());
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-03 12:30:01", PST), it.next());
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-05 12:30:01", PST), it.next());
    assertFalse(it.hasNext());

    it = iterable.iterator();
    it.advanceTo(date("2006-01-03", PST));
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-03 12:30:01", PST), it.next());
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-05 12:30:01", PST), it.next());
    assertFalse(it.hasNext());

    it = iterable.iterator();
    it.advanceTo(date("2006-01-03 14:30:01", PST)); //advance past
    assertTrue(it.hasNext());
    assertEquals(date("2006-01-05 12:30:01", PST), it.next());
    assertFalse(it.hasNext());
  }
}
