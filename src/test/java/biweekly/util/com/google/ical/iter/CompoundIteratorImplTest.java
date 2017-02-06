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
 Copyright (c) 2013-2016, Michael Angstadt
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

package biweekly.util.com.google.ical.iter;

import static biweekly.util.TestUtils.assertIterator;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
//@formatter:off
public class CompoundIteratorImplTest {
	private static final TimeZone PST = TimeZone.getTimeZone("America/Los_Angeles");
	private static final TimeZone UTC = TimeUtils.utcTimezone();

	@Test
	public void multipleCallsToHasNext() {
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.THURSDAY)
			.count(3)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 9);
		RecurrenceIterator ri = RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST);
		
		List<DateValue> expected = Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 13),
			new DateValueImpl(2006, 4, 20),
			new DateValueImpl(2006, 4, 27)
		);

		List<DateValue> actual = new ArrayList<DateValue>();
		while (ri.hasNext() && ri.hasNext()) {
			actual.add(ri.next());
		}
		assertEquals(expected, actual);
	}

	@Test
	public void interleavingOfDateIterators() {
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2007, 1, 1)
			)),
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 22),
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 12)
			))
		);
		Collection<RecurrenceIterator> exclusions = new ArrayList<RecurrenceIterator>();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 12),
			new DateValueImpl(2006, 4, 17),
			new DateValueImpl(2006, 4, 18),
			new DateValueImpl(2006, 4, 22),
			new DateValueImpl(2007, 1, 1)
		), it);
	}
	
	@Test
	public void interleavingOfDateIterators_advanceTo_on_date() {
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2007, 1, 1)
			)),
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 22),
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 12)
			))
		);
		Collection<RecurrenceIterator> exclusions = new ArrayList<RecurrenceIterator>();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2006, 4, 17));
		assertIterator(Arrays.<DateValue>asList(
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2006, 4, 22),
				new DateValueImpl(2007, 1, 1)
		), it);
	}
	
	@Test
	public void interleavingOfDateIterators_advanceTo_after_date() {
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2007, 1, 1)
			)),
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 22),
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 12)
			))
		);
		Collection<RecurrenceIterator> exclusions = new ArrayList<RecurrenceIterator>();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2006, 4, 20));
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 22),
			new DateValueImpl(2007, 1, 1)
		), it);
	}
	
	@Test
	public void interleavingOfDateIterators_advanceTo_after_end() {
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2007, 1, 1)
			)),
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 22),
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 12)
			))
		);
		Collection<RecurrenceIterator> exclusions = new ArrayList<RecurrenceIterator>();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2007, 1, 2));
		assertIterator(Arrays.<DateValue>asList(
		), it);
	}

	@Test
	public void interleavingOfDateIteratorsWithExclusions() {
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2007, 1, 1),
				new DateValueImpl(2006, 4, 17)
			)),
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 22),
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 12)
			))
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2007, 4, 15)
			))
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 12),
			new DateValueImpl(2006, 4, 18),
			new DateValueImpl(2006, 4, 22),
			new DateValueImpl(2007, 1, 1)
		), it);
	}
	
	@Test
	public void interleavingOfDateIteratorsWithExclusions_advanceTo_on_date() {
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2007, 1, 1),
				new DateValueImpl(2006, 4, 17)
			)),
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 22),
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 12)
			))
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2007, 4, 15)
			))
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2006, 4, 18));
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 18),
			new DateValueImpl(2006, 4, 22),
			new DateValueImpl(2007, 1, 1)
		), it);
	}
	
	@Test
	public void interleavingOfDateIteratorsWithExclusions_advanceTo_after_date() {
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2007, 1, 1),
				new DateValueImpl(2006, 4, 17)
			)),
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 22),
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 12)
			))
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2007, 4, 15)
			))
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2006, 4, 19));
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 22),
			new DateValueImpl(2007, 1, 1)
		), it);
	}

	@Test
	public void infiniteRecurrences() {
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
		.build();
		Recurrence exrule = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.byDay(DayOfWeek.THURSDAY)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 11);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
				RecurrenceIteratorFactory.createRecurrenceIterator(exrule, start, PST)
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 11),
			new DateValueImpl(2006, 4, 12),
			new DateValueImpl(2006, 4, 14),
			
			new DateValueImpl(2006, 4, 17),
			new DateValueImpl(2006, 4, 18),
			new DateValueImpl(2006, 4, 19),
			new DateValueImpl(2006, 4, 20),
			new DateValueImpl(2006, 4, 21),
			
			new DateValueImpl(2006, 4, 24),
			new DateValueImpl(2006, 4, 25),
			new DateValueImpl(2006, 4, 26),
			new DateValueImpl(2006, 4, 28),
			
			new DateValueImpl(2006, 5, 1),
			new DateValueImpl(2006, 5, 2),
			new DateValueImpl(2006, 5, 3),
			new DateValueImpl(2006, 5, 4),
			new DateValueImpl(2006, 5, 5)
		), it, false);
	}
	
	@Test
	public void infiniteRecurrences_advanceTo_on_date() {
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
		.build();
		Recurrence exrule = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.byDay(DayOfWeek.THURSDAY)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 11);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(exrule, start, PST)
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2006, 4, 20));
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 20),
			new DateValueImpl(2006, 4, 21),
			
			new DateValueImpl(2006, 4, 24),
			new DateValueImpl(2006, 4, 25),
			new DateValueImpl(2006, 4, 26),
			new DateValueImpl(2006, 4, 28),
			
			new DateValueImpl(2006, 5, 1),
			new DateValueImpl(2006, 5, 2),
			new DateValueImpl(2006, 5, 3),
			new DateValueImpl(2006, 5, 4),
			new DateValueImpl(2006, 5, 5)
		), it, false);
	}
	
	@Test
	public void infiniteRecurrences_advanceTo_after_date() {
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
		.build();
		Recurrence exrule = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.byDay(DayOfWeek.THURSDAY)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 11);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(exrule, start, PST)
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2006, 4, 23));
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 24),
			new DateValueImpl(2006, 4, 25),
			new DateValueImpl(2006, 4, 26),
			new DateValueImpl(2006, 4, 28),
			
			new DateValueImpl(2006, 5, 1),
			new DateValueImpl(2006, 5, 2),
			new DateValueImpl(2006, 5, 3),
			new DateValueImpl(2006, 5, 4),
			new DateValueImpl(2006, 5, 5)
		), it, false);
	}

	@Test
	public void infiniteExclusionsAndFiniteInclusions() {
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
			.until(new ICalDate(date("2006-05-03"), false))
		.build();
		Recurrence exrule = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.byDay(DayOfWeek.THURSDAY)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 11);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(exrule, start, PST)
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 11),
			new DateValueImpl(2006, 4, 12),
			new DateValueImpl(2006, 4, 14),
			
			new DateValueImpl(2006, 4, 17),
			new DateValueImpl(2006, 4, 18),
			new DateValueImpl(2006, 4, 19),
			new DateValueImpl(2006, 4, 20),
			new DateValueImpl(2006, 4, 21),
			
			new DateValueImpl(2006, 4, 24),
			new DateValueImpl(2006, 4, 25),
			new DateValueImpl(2006, 4, 26),
			new DateValueImpl(2006, 4, 28),
			
			new DateValueImpl(2006, 5, 1),
			new DateValueImpl(2006, 5, 2)
		), it);
	}
	
	@Test
	public void infiniteExclusionsAndFiniteInclusions_advanceTo_on_date() {
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
			.until(new ICalDate(date("2006-05-03"), false))
		.build();
		Recurrence exrule = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.byDay(DayOfWeek.THURSDAY)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 11);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(exrule, start, PST)
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2006, 4, 18));
		assertIterator(Arrays.<DateValue>asList(
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2006, 4, 19),
				new DateValueImpl(2006, 4, 20),
				new DateValueImpl(2006, 4, 21),
				
				new DateValueImpl(2006, 4, 24),
				new DateValueImpl(2006, 4, 25),
				new DateValueImpl(2006, 4, 26),
				new DateValueImpl(2006, 4, 28),
				
				new DateValueImpl(2006, 5, 1),
				new DateValueImpl(2006, 5, 2)
		), it);
	}
	
	@Test
	public void infiniteExclusionsAndFiniteInclusions_advanceTo_after_date() {
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
			.until(new ICalDate(date("2006-05-03"), false))
		.build();
		Recurrence exrule = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.byDay(DayOfWeek.THURSDAY)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 11);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(exrule, start, PST)
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2006, 4, 22));
		assertIterator(Arrays.<DateValue>asList(
				new DateValueImpl(2006, 4, 24),
				new DateValueImpl(2006, 4, 25),
				new DateValueImpl(2006, 4, 26),
				new DateValueImpl(2006, 4, 28),
				
				new DateValueImpl(2006, 5, 1),
				new DateValueImpl(2006, 5, 2)
		), it);
	}
	
	@Test
	public void identicalInclusionsAndExclusions() {
		Recurrence rrule = new Recurrence.Builder(Frequency.DAILY).count(3).build();
		DateValue start = new DateValueImpl(2006, 4, 11);
		
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2007, 1, 1),
				new DateValueImpl(2006, 4, 17)
			)),
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(Arrays.asList(
				new DateValueImpl(2006, 4, 17),
				new DateValueImpl(2006, 4, 18),
				new DateValueImpl(2007, 1, 1),
				new DateValueImpl(2006, 4, 17)
			)),
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(), it);
	}

	@Test
	public void monkey1() {
		Recurrence rrule = new Recurrence.Builder(Frequency.MONTHLY)
			.interval(1)
			.byMonth(9, 5, 3)
		.build();
		DateValue start = new DateValueImpl(2006, 5, 3);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 5, 3),
			new DateValueImpl(2006, 9, 3),
			new DateValueImpl(2007, 3, 3),
			new DateValueImpl(2007, 5, 3),
			new DateValueImpl(2007, 9, 3),
			new DateValueImpl(2008, 3, 3)
		), it, false);
	}

	@Test
	public void monkey2() {
		Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY)
			.count(19)
			.interval(1)
			.byMonth(1, 11, 6)
			.bySetPos(7, -4, 6)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 22);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
		), it);
	}

	@Test
	public void monkey3() {
		Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY)
			.workweekStarts(DayOfWeek.SUNDAY)
			.interval(1)
			.bySecond(14)
		.build();
		DateValue start = new DateTimeValueImpl(2006, 5, 9, 3, 45, 40);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateTimeValueImpl(2007, 5, 9, 10, 45, 14),
			new DateTimeValueImpl(2008, 5, 9, 10, 45, 14),
			new DateTimeValueImpl(2009, 5, 9, 10, 45, 14)
		), it, false);
	}

	@Test
	public void monkey4() {
		Recurrence rrule = new Recurrence.Builder(Frequency.MONTHLY)
			.workweekStarts(DayOfWeek.SUNDAY)
			.interval(1)
			.byMonthDay(31, -16, 23, -4, -19, -23, 6, -4, -10, 1, 10)
		.build();
		DateValue start = new DateValueImpl(2006, 5, 1);

		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 5, 1),
			new DateValueImpl(2006, 5, 6),
			new DateValueImpl(2006, 5, 9),
			new DateValueImpl(2006, 5, 10),
			new DateValueImpl(2006, 5, 13),
			new DateValueImpl(2006, 5, 16),
			new DateValueImpl(2006, 5, 22),
			new DateValueImpl(2006, 5, 23),
			new DateValueImpl(2006, 5, 28),
			new DateValueImpl(2006, 5, 31),
			new DateValueImpl(2006, 6, 1),
			new DateValueImpl(2006, 6, 6)
		), it, false);
	}

	@Test
	public void monkey5() {
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.count(14)
			.interval(1)
			.byMonth(11, 6, 7, 7, 12)
		.build();
		DateValue start = new DateTimeValueImpl(2006, 5, 2, 22, 46, 53);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateTimeValueImpl(2006, 6, 7, 5, 46, 53),
			new DateTimeValueImpl(2006, 6, 14, 5, 46, 53),
			new DateTimeValueImpl(2006, 6, 21, 5, 46, 53),
			new DateTimeValueImpl(2006, 6, 28, 5, 46, 53),
			
			new DateTimeValueImpl(2006, 7, 5, 5, 46, 53),
			new DateTimeValueImpl(2006, 7, 12, 5, 46, 53),
			new DateTimeValueImpl(2006, 7, 19, 5, 46, 53),
			new DateTimeValueImpl(2006, 7, 26, 5, 46, 53),
			
			/*
			 * The first of November is NOT part of this recurrence because the day is
			 * calculated relative to the 2nd as per dtstart and then the timezone
			 * calculation pushes it first a day when translating into UTC.
			 */
			new DateTimeValueImpl(2006, 11, 8, 6, 46, 53),
			new DateTimeValueImpl(2006, 11, 15, 6, 46, 53),
			new DateTimeValueImpl(2006, 11, 22, 6, 46, 53),
			new DateTimeValueImpl(2006, 11, 29, 6, 46, 53),
			
			new DateTimeValueImpl(2006, 12, 6, 6, 46, 53),
			new DateTimeValueImpl(2006, 12, 13, 6, 46, 53)
		), it);
	}

	@Test
	public void monkey6() {
		Recurrence rrule = new Recurrence.Builder(Frequency.MONTHLY)
			.until(date("2006-05-10 15:10:44", UTC))
			.interval(1)
			.bySecond(48)
		.build();
		DateValue start = new DateTimeValueImpl(2006, 4, 5, 4, 42, 26);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateTimeValueImpl(2006, 4, 5, 11, 42, 48),
			new DateTimeValueImpl(2006, 5, 5, 11, 42, 48)
		), it);
	}

	@Test
	public void monkey7() {
		//a bug in the by week generator was causing us to skip Feb 2007
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.workweekStarts(DayOfWeek.SUNDAY)
			.interval(1)
		.build();
		DateValue start = new DateTimeValueImpl(2006, 5, 8, 9, 47, 41);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateTimeValueImpl(2006, 5, 8, 16, 47, 41),
			new DateTimeValueImpl(2006, 5, 15, 16, 47, 41),
			new DateTimeValueImpl(2006, 5, 22, 16, 47, 41),
			new DateTimeValueImpl(2006, 5, 29, 16, 47, 41),
			new DateTimeValueImpl(2006, 6, 5, 16, 47, 41),
			new DateTimeValueImpl(2006, 6, 12, 16, 47, 41),
			new DateTimeValueImpl(2006, 6, 19, 16, 47, 41),
			new DateTimeValueImpl(2006, 6, 26, 16, 47, 41)
		), it, false);
	}

	@Test
	public void monkey8() {
		//I don't know which side this failing on?
		Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY)
			.count(18)
			.byMonth(7, 11)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 27);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			//all the Thursdays in July and November
			new DateValueImpl(2006, 7, 6),
			new DateValueImpl(2006, 7, 13),
			new DateValueImpl(2006, 7, 20),
			new DateValueImpl(2006, 7, 27),
			
			new DateValueImpl(2006, 11, 2),
			new DateValueImpl(2006, 11, 9),
			new DateValueImpl(2006, 11, 16),
			new DateValueImpl(2006, 11, 23),
			new DateValueImpl(2006, 11, 30),
			
			new DateValueImpl(2007, 7, 5),
			new DateValueImpl(2007, 7, 12),
			new DateValueImpl(2007, 7, 19),
			new DateValueImpl(2007, 7, 26),
			
			new DateValueImpl(2007, 11, 1),
			new DateValueImpl(2007, 11, 8),
			new DateValueImpl(2007, 11, 15),
			new DateValueImpl(2007, 11, 22),
			new DateValueImpl(2007, 11, 29)
		), it);
	}

	@Test
	public void monkey9() {
		// another libical crasher
		Recurrence rrule = new Recurrence.Builder(Frequency.MONTHLY)
			.workweekStarts(DayOfWeek.SUNDAY)
			.interval(1)
			.byWeekNo(5,-5)
			.bySecond(0)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 27);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			//all the 27ths of the month.	BYWEEKNO ignores since not yearly
			new DateValueImpl(2006, 4, 27),
			new DateValueImpl(2006, 5, 27),
			new DateValueImpl(2006, 6, 27),
			new DateValueImpl(2006, 7, 27),
			new DateValueImpl(2006, 8, 27)
		), it, false);
	}

	@Test
	public void monkey10() {
		// another libical hanger
		Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY)
			.byYearDay(1)
			.byMonthDay(1)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 27);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2007, 1, 1),
			new DateValueImpl(2008, 1, 1),
			new DateValueImpl(2009, 1, 1),
			new DateValueImpl(2010, 1, 1)
		), it, false);
	}
	
	@Test
	public void monkey10_2() {
		// another libical hanger
		Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY)
			.byYearDay(1)
			.byMonthDay(2)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 27);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			//empty
		), it);
	}

	@Test
	public void monkey11() {
		//days of the month in December
		//8, 10, 9, 6, 4, 14, 22, 23, 18, 24, 4, 24, 18
		//unique
		//4, *6, 8, 9, 10, 14, *18, *22, 23, *24
		Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY)
			.interval(1)
			.byMonthDay(8, -22, 9, -26, -28, 14, -10, -9, -14, -8, 4, 24, -14)
			.bySetPos(-1, -3, -4, -9)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 9);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 12, 6),
			new DateValueImpl(2006, 12, 18),
			new DateValueImpl(2006, 12, 22),
			new DateValueImpl(2006, 12, 24)
		), it, false);
	}

	@Test
	public void monkey11WithAdvanceTo() {
		Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY)
			.interval(1)
			.byMonthDay(8, -22, 9, -26, -28, 14, -10, -9, -14, -8, 4, 24, -14)
			.bySetPos(-1, -3, -4, -9)
		.build();
		DateValue start = new DateValueImpl(2006, 4, 9);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		it.advanceTo(new DateValueImpl(2006, 12, 20));
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 12, 22),
			new DateValueImpl(2006, 12, 24),
			new DateValueImpl(2007, 12, 6),
			new DateValueImpl(2007, 12, 18),
			new DateValueImpl(2007, 12, 22)
		), it, false);
	}

	@Test
	public void monkey12() {
		Recurrence rrule = new Recurrence.Builder(Frequency.DAILY)
			.interval(1)
			.byMonthDay(5, 29, 31, -19, -28)
		.build();
		DateValue start = new DateTimeValueImpl(2006, 5, 2, 18, 47, 45);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateTimeValueImpl(2006, 5, 5, 1, 47, 45),
			new DateTimeValueImpl(2006, 5, 6, 1, 47, 45),
			new DateTimeValueImpl(2006, 5, 14, 1, 47, 45),
			new DateTimeValueImpl(2006, 5, 30, 1, 47, 45),
			new DateTimeValueImpl(2006, 6, 1, 1, 47, 45)
		), it, false);
	}

	@Test
	public void monkey13() {
		Recurrence rrule = new Recurrence.Builder(Frequency.DAILY)
			.workweekStarts(DayOfWeek.SUNDAY)
			.count(4)
			.interval(1)
			.byMonthDay(20, -20)
		.build();
		DateValue start = new DateValueImpl(2006, 5, 19);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 5, 20),
			new DateValueImpl(2006, 6, 11),
			new DateValueImpl(2006, 6, 20),
			new DateValueImpl(2006, 7, 12)
		), it);
	}

	@Test
	public void monkey14() {
		Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY)
			.byDay(DayOfWeek.THURSDAY)
		.build();
		DateValue start = new DateValueImpl(2006, 5, 6);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateValueImpl(2006, 5, 11),
			new DateValueImpl(2006, 5, 18),
			new DateValueImpl(2006, 5, 25),
			new DateValueImpl(2006, 6, 1)
		), it, false);
	}

	@Test
	public void excludedStart() {
		Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY)
			.until(date("2007-04-14", PST))
			.interval(1)
			.byDay(3, DayOfWeek.SUNDAY)
			.byMonth(4)
		.build();
		List<DateValue> exdates = Arrays.<DateValue>asList(
			new DateValueImpl(2006, 4, 16)
		);
		DateValue start = new DateValueImpl(2006, 4, 16);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(exdates)
		);
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
		), it);
	}

	@Test
	public void monkeySeptember1() {
		// From the Monkey Tester
		// RANDOM SEED 1156837020593
		// RRULE:FREQ=DAILY;WKST=SU;INTERVAL=1;BYMINUTE=60 / 2006-09-20 23:15:51
		// last=2006-09-21 07:00:51, current=2006-09-21 07:00:51
		
		Recurrence rrule = new Recurrence.Builder(Frequency.DAILY)
			.workweekStarts(DayOfWeek.SUNDAY)
			.interval(1)
			.byMinute(59)
		.build();
		DateValue start = new DateTimeValueImpl(2006, 9, 20, 23, 15, 51);
	
		Collection<RecurrenceIterator> inclusions = Arrays.asList(
			RecurrenceIteratorFactory.createRecurrenceIterator(rrule, start, PST)
		);
		Collection<RecurrenceIterator> exclusions = Arrays.asList();
		
		CompoundIteratorImpl it = new CompoundIteratorImpl(inclusions, exclusions);
		assertIterator(Arrays.<DateValue>asList(
			new DateTimeValueImpl(2006, 9, 21, 6, 59, 51),
			new DateTimeValueImpl(2006, 9, 22, 6, 59, 51),
			new DateTimeValueImpl(2006, 9, 23, 6, 59, 51),
			new DateTimeValueImpl(2006, 9, 24, 6, 59, 51)
		), it, false);

		/*
		 * We can't actually create an RRULE with BYMINUTE=60 since that's out of
		 * range.
		 * 
		 * TODO(msamuel): write me
		 */
	}
}
//@formatter:on