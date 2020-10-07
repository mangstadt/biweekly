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

/*
 Copyright (c) 2013-2020, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package biweekly.util.com.google.ical.compat.javautil;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import org.junit.Test;

import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.iter.RecurrenceIterable;
import biweekly.util.com.google.ical.iter.RecurrenceIteratorFactory;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class DateIteratorFactoryTest {
	private static final TimeZone UTC = TimeUtils.utcTimezone();
	private static final TimeZone PST = TimeZone.getTimeZone("America/Los_Angeles");

	@Test
	public void createDateIterableUntimed() {
		//@formatter:off
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
			.interval(2)
			.count(3)
			.build();
		//@formatter:on
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

	@Test
	public void createDateIterableMidnight() {
		//@formatter:off
		Recurrence recur = new Recurrence.Builder(Frequency.HOURLY)
			.interval(2)
			.count(3)
		.build();
		//@formatter:on
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

	@Test
	public void createDateIterableTimed() {
		//@formatter:off
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
			.interval(2)
			.count(3)
		.build();
		//@formatter:on
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
