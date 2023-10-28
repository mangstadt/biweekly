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
 Copyright (c) 2013-2023, Michael Angstadt
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.util.DTBuilder;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
//@formatter:off
public class RRuleIteratorImplTest {
	private static final TimeZone PST = TimeZone.getTimeZone("America/Los_Angeles");
	private static final TimeZone UTC = TimeUtils.utcTimezone();

	@Test
	public void frequencyLimits() {
		Recurrence.Builder rb = new Recurrence.Builder(Frequency.SECONDLY);
		for (int i = 0; i < 60; i++){
			rb.bySecond(i);
		}
		Recurrence recur = rb.build();
		DateValue start = new DateValueImpl(2000, 1, 1);
		
		RecurrenceIteratorFactory.createRecurrenceIterator(recur, start, UTC);
	}

	@Test
	public void simpleDaily() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).build();
		DateValue start = new DateValueImpl(2006, 1, 20);
		DateValue[] expected = {
			new DateValueImpl(2006, 1, 20),
			new DateValueImpl(2006, 1, 21),
			new DateValueImpl(2006, 1, 22),
			new DateValueImpl(2006, 1, 23),
			new DateValueImpl(2006, 1, 24)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void simpleWeekly() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).build();
		DateValue start = new DateValueImpl(2006, 1, 20);
		DateValue[] expected = {
			new DateValueImpl(2006, 1, 20),
			new DateValueImpl(2006, 1, 27),
			new DateValueImpl(2006, 2, 3),
			new DateValueImpl(2006, 2, 10),
			new DateValueImpl(2006, 2, 17)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void simpleMonthly() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY).build();
		DateValue start = new DateValueImpl(2006, 1, 20);
		DateValue[] expected = {
			new DateValueImpl(2006, 1, 20),
			new DateValueImpl(2006, 2, 20),
			new DateValueImpl(2006, 3, 20),
			new DateValueImpl(2006, 4, 20),
			new DateValueImpl(2006, 5, 20)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void simpleYearly() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY).build();
		DateValue start = new DateValueImpl(2006, 1, 20);
		DateValue[] expected = {
			new DateValueImpl(2006, 1, 20),
			new DateValueImpl(2007, 1, 20),
			new DateValueImpl(2008, 1, 20),
			new DateValueImpl(2009, 1, 20),
			new DateValueImpl(2010, 1, 20)
		};
		
		run(recur, start, expected);
	}

	// from section 4.3.10
	@Test
	public void multipleByParts() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.interval(2)
			.byMonth(1)
			.byDay(DayOfWeek.SUNDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 1, 5);
		DateValue[] expected = {
			new DateValueImpl(1997, 1, 5),
			new DateValueImpl(1997, 1, 12),
			new DateValueImpl(1997, 1, 19),
			new DateValueImpl(1997, 1, 26),
			new DateValueImpl(1999, 1, 3),
			new DateValueImpl(1999, 1, 10),
			new DateValueImpl(1999, 1, 17),
			new DateValueImpl(1999, 1, 24)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void countWithInterval() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
			.count(10)
			.interval(2)
		.build();
		DateValue start = new DateValueImpl(1997, 1, 5);
		DateValue[] expected = {
			new DateValueImpl(1997, 1, 5),
			new DateValueImpl(1997, 1, 7),
			new DateValueImpl(1997, 1, 9),
			new DateValueImpl(1997, 1, 11),
			new DateValueImpl(1997, 1, 13),
			new DateValueImpl(1997, 1, 15),
			new DateValueImpl(1997, 1, 17),
			new DateValueImpl(1997, 1, 19),
			new DateValueImpl(1997, 1, 21),
			new DateValueImpl(1997, 1, 23)
		};
		
		run(recur, start, expected);
	}

	// from section 4.6.5
	@Test
	public void negativeOffsets() {
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.byDay(-1, DayOfWeek.SUNDAY)
				.byMonth(10)
			.build();
			DateValue start = new DateValueImpl(1997, 1, 5);
			DateValue[] expected = {
				new DateValueImpl(1997, 10, 26),
				new DateValueImpl(1998, 10, 25),
				new DateValueImpl(1999, 10, 31),
				new DateValueImpl(2000, 10, 29),
				new DateValueImpl(2001, 10, 28)
			};
			
			run(recur, start, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.byDay(1, DayOfWeek.SUNDAY)
				.byMonth(4)
			.build();
			DateValue start = new DateValueImpl(1997, 1, 5);
			DateValue[] expected = {
				new DateValueImpl(1997, 4, 6),
				new DateValueImpl(1998, 4, 5),
				new DateValueImpl(1999, 4, 4),
				new DateValueImpl(2000, 4, 2),
				new DateValueImpl(2001, 4, 1)
			};
			
			run(recur, start, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.byDay(1, DayOfWeek.SUNDAY)
				.byMonth(4)
				.until(date(1998, 4, 4, 15, 0, 0, UTC))
			.build();
			DateValue start = new DateValueImpl(1997, 1, 5);
			DateValue[] expected = {
				new DateValueImpl(1997, 4, 6)
			};
			
			run(recur, start, expected);
		}
	}

	// from section 4.8.5.4
	@Test
	public void dailyFor10Occ() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
			.count(10)
		.build();
		DateValue start = new DateTimeValueImpl(1997, 9, 2, 9, 0, 0);
		DateValue[] expected = {
			new DateTimeValueImpl(1997, 9, 2, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 3, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 4, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 5, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 6, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 7, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 8, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 9, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 10, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 11, 9, 0, 0)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void dailyUntilDec4() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
			.until(new ICalDate(date(1997, 12, 4), false))
		.build();
		DateValue start = new DateValueImpl(1997, 11, 28);
		DateValue[] expected = {
			new DateValueImpl(1997, 11, 28),
			new DateValueImpl(1997, 11, 29),
			new DateValueImpl(1997, 11, 30),
			new DateValueImpl(1997, 12, 1),
			new DateValueImpl(1997, 12, 2),
			new DateValueImpl(1997, 12, 3),
			new DateValueImpl(1997, 12, 4)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void everyOtherDayForever() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
			.interval(2)
		.build();
		DateValue start = new DateValueImpl(1997, 11, 28);
		DateValue[] expected = {
			new DateValueImpl(1997, 11, 28),
			new DateValueImpl(1997, 11, 30),
			new DateValueImpl(1997, 12, 2),
			new DateValueImpl(1997, 12, 4),
			new DateValueImpl(1997, 12, 6)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void every10Days5Occ() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
			.interval(10)
			.count(5)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 2);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 2),
			new DateValueImpl(1997, 9, 12),
			new DateValueImpl(1997, 9, 22),
			new DateValueImpl(1997, 10, 2),
			new DateValueImpl(1997, 10, 12)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void everyDayInJanuaryFor3Years() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.until(date(2000, 1, 31, 9, 0, 0, UTC))
			.byMonth(1)
			.byDay(DayOfWeek.values())
		.build();
		DateValue start = new DateValueImpl(1998, 1, 1);
		
		List<DateValue> expected = new ArrayList<DateValue>();
		expected.addAll(dateRange(new DateValueImpl(1998, 1, 1), new DateValueImpl(1998, 1, 31)));
		expected.addAll(dateRange(new DateValueImpl(1999, 1, 1), new DateValueImpl(1999, 1, 31)));
		expected.addAll(dateRange(new DateValueImpl(2000, 1, 1), new DateValueImpl(2000, 1, 31)));
		
		run(recur, start, expected.toArray(new DateValue[0]));
	}

	@Test
	public void weeklyFor10Occ() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.count(10)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 2);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 2),
			new DateValueImpl(1997, 9, 9),
			new DateValueImpl(1997, 9, 16),
			new DateValueImpl(1997, 9, 23),
			new DateValueImpl(1997, 9, 30),
			new DateValueImpl(1997, 10, 7),
			new DateValueImpl(1997, 10, 14),
			new DateValueImpl(1997, 10, 21),
			new DateValueImpl(1997, 10, 28),
			new DateValueImpl(1997, 11, 4)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void weeklyUntilDec24() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.until(new ICalDate(date(1997, 12, 24), false))
		.build();
		DateValue start = new DateValueImpl(1997, 9, 2);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 2),
			new DateValueImpl(1997, 9, 9),
			new DateValueImpl(1997, 9, 16),
			new DateValueImpl(1997, 9, 23),
			new DateValueImpl(1997, 9, 30),
			new DateValueImpl(1997, 10, 7),
			new DateValueImpl(1997, 10, 14),
			new DateValueImpl(1997, 10, 21),
			new DateValueImpl(1997, 10, 28),
			new DateValueImpl(1997, 11, 4),
			new DateValueImpl(1997, 11, 11),
			new DateValueImpl(1997, 11, 18),
			new DateValueImpl(1997, 11, 25),
			new DateValueImpl(1997, 12, 2),
			new DateValueImpl(1997, 12, 9),
			new DateValueImpl(1997, 12, 16),
			new DateValueImpl(1997, 12, 23)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void everyOtherWeekForever() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.workweekStarts(DayOfWeek.SUNDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 2);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 2),
			new DateValueImpl(1997, 9, 16),
			new DateValueImpl(1997, 9, 30),
			new DateValueImpl(1997, 10, 14),
			new DateValueImpl(1997, 10, 28),
			new DateValueImpl(1997, 11, 11),
			new DateValueImpl(1997, 11, 25),
			new DateValueImpl(1997, 12, 9),
			new DateValueImpl(1997, 12, 23)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void weeklyOnTuesdayAndThursdayFor5Weeks() {
		/*
		 * If UNTIL date does not match start date, then until date treated as
		 * occurring on midnight.
		 */
		{
			Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
				.until(new ICalDate(date(1997, 10, 7), false))
				.workweekStarts(DayOfWeek.SUNDAY)
				.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
			.build();
			DateValue start = new DateTimeValueImpl(1997, 9, 2, 9, 0, 0);
			DateValue[] expected = {
				new DateTimeValueImpl(1997, 9, 2, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 4, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 9, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 11, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 16, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 18, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 23, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 25, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 30, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 2, 9, 0, 0)
			};
			
			run(recur, start, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
				.until(date(1997, 10, 7, 0, 0, 0, UTC))
				.workweekStarts(DayOfWeek.SUNDAY)
				.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
			.build();
			DateValue start = new DateTimeValueImpl(1997, 9, 2, 9, 0, 0);
			DateValue[] expected = {
				new DateTimeValueImpl(1997, 9, 2, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 4, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 9, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 11, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 16, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 18, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 23, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 25, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 30, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 2, 9, 0, 0)
			};
			
			run(recur, start, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
				.count(10)
				.workweekStarts(DayOfWeek.SUNDAY)
				.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
			.build();
			DateValue start = new DateValueImpl(1997, 9, 2);
			DateValue[] expected = {
				new DateValueImpl(1997, 9, 2),
				new DateValueImpl(1997, 9, 4),
				new DateValueImpl(1997, 9, 9),
				new DateValueImpl(1997, 9, 11),
				new DateValueImpl(1997, 9, 16),
				new DateValueImpl(1997, 9, 18),
				new DateValueImpl(1997, 9, 23),
				new DateValueImpl(1997, 9, 25),
				new DateValueImpl(1997, 9, 30),
				new DateValueImpl(1997, 10, 2)
			};
			
			run(recur, start, expected);
		}
	}

	@Test
	public void everyOtherWeekOnMWFUntilDec24() {
		{
			Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
				.interval(2)
				.until(date(1997, 12, 24, 0, 0, 0, UTC))
				.workweekStarts(DayOfWeek.SUNDAY)
				.byDay(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
			.build();
			DateValue start = new DateTimeValueImpl(1997, 9, 3, 9, 0, 0);
			DateValue[] expected = {
				new DateTimeValueImpl(1997, 9, 3, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 5, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 15, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 17, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 19, 9, 0, 0),
				new DateTimeValueImpl(1997, 9, 29, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 1, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 3, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 13, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 15, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 17, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 27, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 29, 9, 0, 0),
				new DateTimeValueImpl(1997, 10, 31, 9, 0, 0),
				new DateTimeValueImpl(1997, 11, 10, 9, 0, 0),
				new DateTimeValueImpl(1997, 11, 12, 9, 0, 0),
				new DateTimeValueImpl(1997, 11, 14, 9, 0, 0),
				new DateTimeValueImpl(1997, 11, 24, 9, 0, 0),
				new DateTimeValueImpl(1997, 11, 26, 9, 0, 0),
				new DateTimeValueImpl(1997, 11, 28, 9, 0, 0),
				new DateTimeValueImpl(1997, 12, 8, 9, 0, 0),
				new DateTimeValueImpl(1997, 12, 10, 9, 0, 0),
				new DateTimeValueImpl(1997, 12, 12, 9, 0, 0),
				new DateTimeValueImpl(1997, 12, 22, 9, 0, 0)
			};
			
			run(recur, start, expected);
		}
		
		/*
		 * If the UNTIL date is timed, when the start is not, the time should be
		 * ignored, so we get one more instance.
		 */
		{
			Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
				.interval(2)
				.until(date(1997, 12, 24, 0, 0, 0, UTC))
				.workweekStarts(DayOfWeek.SUNDAY)
				.byDay(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
			.build();
			DateValue start = new DateValueImpl(1997, 9, 3);
			DateValue[] expected = {
				new DateValueImpl(1997, 9, 3),
				new DateValueImpl(1997, 9, 5),
				new DateValueImpl(1997, 9, 15),
				new DateValueImpl(1997, 9, 17),
				new DateValueImpl(1997, 9, 19),
				new DateValueImpl(1997, 9, 29),
				new DateValueImpl(1997, 10, 1),
				new DateValueImpl(1997, 10, 3),
				new DateValueImpl(1997, 10, 13),
				new DateValueImpl(1997, 10, 15),
				new DateValueImpl(1997, 10, 17),
				new DateValueImpl(1997, 10, 27),
				new DateValueImpl(1997, 10, 29),
				new DateValueImpl(1997, 10, 31),
				new DateValueImpl(1997, 11, 10),
				new DateValueImpl(1997, 11, 12),
				new DateValueImpl(1997, 11, 14),
				new DateValueImpl(1997, 11, 24),
				new DateValueImpl(1997, 11, 26),
				new DateValueImpl(1997, 11, 28),
				new DateValueImpl(1997, 12, 8),
				new DateValueImpl(1997, 12, 10),
				new DateValueImpl(1997, 12, 12),
				new DateValueImpl(1997, 12, 22),
				new DateValueImpl(1997, 12, 24)
			};
			
			run(recur, start, expected);
		}
		
		/*
		 * Test with an alternate timezone.
		 */
		{
			Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
				.interval(2)
				.until(date(1997, 12, 24, 9, 0, 0, UTC))
				.workweekStarts(DayOfWeek.SUNDAY)
				.byDay(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
			.build();
			DateValue start = new DateTimeValueImpl(1997, 9, 3, 9, 0, 0);
			DateValue[] expected = {
				new DateTimeValueImpl(1997, 9, 3, 16, 0, 0),
				new DateTimeValueImpl(1997, 9, 5, 16, 0, 0),
				new DateTimeValueImpl(1997, 9, 15, 16, 0, 0),
				new DateTimeValueImpl(1997, 9, 17, 16, 0, 0),
				new DateTimeValueImpl(1997, 9, 19, 16, 0, 0),
				new DateTimeValueImpl(1997, 9, 29, 16, 0, 0),
				new DateTimeValueImpl(1997, 10, 1, 16, 0, 0),
				new DateTimeValueImpl(1997, 10, 3, 16, 0, 0),
				new DateTimeValueImpl(1997, 10, 13, 16, 0, 0),
				new DateTimeValueImpl(1997, 10, 15, 16, 0, 0),
				new DateTimeValueImpl(1997, 10, 17, 16, 0, 0),
				new DateTimeValueImpl(1997, 10, 27, 17, 0, 0),
				new DateTimeValueImpl(1997, 10, 29, 17, 0, 0),
				new DateTimeValueImpl(1997, 10, 31, 17, 0, 0),
				new DateTimeValueImpl(1997, 11, 10, 17, 0, 0),
				new DateTimeValueImpl(1997, 11, 12, 17, 0, 0),
				new DateTimeValueImpl(1997, 11, 14, 17, 0, 0),
				new DateTimeValueImpl(1997, 11, 24, 17, 0, 0),
				new DateTimeValueImpl(1997, 11, 26, 17, 0, 0),
				new DateTimeValueImpl(1997, 11, 28, 17, 0, 0),
				new DateTimeValueImpl(1997, 12, 8, 17, 0, 0),
				new DateTimeValueImpl(1997, 12, 10, 17, 0, 0),
				new DateTimeValueImpl(1997, 12, 12, 17, 0, 0),
				new DateTimeValueImpl(1997, 12, 22, 17, 0, 0)
			};
			
			run(recur, start, PST, expected);
		}
	}

	@Test
	public void everyOtherWeekOnTuThFor8Occ() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.count(8)
			.workweekStarts(DayOfWeek.SUNDAY)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 2);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 2),
			new DateValueImpl(1997, 9, 4),
			new DateValueImpl(1997, 9, 16),
			new DateValueImpl(1997, 9, 18),
			new DateValueImpl(1997, 9, 30),
			new DateValueImpl(1997, 10, 2),
			new DateValueImpl(1997, 10, 14),
			new DateValueImpl(1997, 10, 16)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void monthlyOnThe1stFridayFor10Occ() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.count(10)
			.byDay(1, DayOfWeek.FRIDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 5);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 5),
			new DateValueImpl(1997, 10, 3),
			new DateValueImpl(1997, 11, 7),
			new DateValueImpl(1997, 12, 5),
			new DateValueImpl(1998, 1, 2),
			new DateValueImpl(1998, 2, 6),
			new DateValueImpl(1998, 3, 6),
			new DateValueImpl(1998, 4, 3),
			new DateValueImpl(1998, 5, 1),
			new DateValueImpl(1998, 6, 5)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void monthlyOnThe1stFridayUntilDec24() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.until(date(1997, 12, 24, 0, 0, 0, UTC))
			.byDay(1, DayOfWeek.FRIDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 5);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 5),
			new DateValueImpl(1997, 10, 3),
			new DateValueImpl(1997, 11, 7),
			new DateValueImpl(1997, 12, 5)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void everyOtherMonthOnThe1stAndLastSundayFor10Occ() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.interval(2)
			.count(10)
			.byDay(1, DayOfWeek.SUNDAY)
			.byDay(-1, DayOfWeek.SUNDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 7);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 7),
			new DateValueImpl(1997, 9, 28),
			new DateValueImpl(1997, 11, 2),
			new DateValueImpl(1997, 11, 30),
			new DateValueImpl(1998, 1, 4),
			new DateValueImpl(1998, 1, 25),
			new DateValueImpl(1998, 3, 1),
			new DateValueImpl(1998, 3, 29),
			new DateValueImpl(1998, 5, 3),
			new DateValueImpl(1998, 5, 31)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void monthlyOnTheSecondToLastMondayOfTheMonthFor6Months() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.count(6)
			.byDay(-2, DayOfWeek.MONDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 22);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 22),
			new DateValueImpl(1997, 10, 20),
			new DateValueImpl(1997, 11, 17),
			new DateValueImpl(1997, 12, 22),
			new DateValueImpl(1998, 1, 19),
			new DateValueImpl(1998, 2, 16)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void monthlyOnTheThirdToLastDay() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.byMonthDay(-3)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 28);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 28),
			new DateValueImpl(1997, 10, 29),
			new DateValueImpl(1997, 11, 28),
			new DateValueImpl(1997, 12, 29),
			new DateValueImpl(1998, 1, 29),
			new DateValueImpl(1998, 2, 26)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void monthlyOnThe2ndAnd15thFor10Occ() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.count(10)
			.byMonthDay(2, 15)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 2);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 2),
			new DateValueImpl(1997, 9, 15),
			new DateValueImpl(1997, 10, 2),
			new DateValueImpl(1997, 10, 15),
			new DateValueImpl(1997, 11, 2),
			new DateValueImpl(1997, 11, 15),
			new DateValueImpl(1997, 12, 2),
			new DateValueImpl(1997, 12, 15),
			new DateValueImpl(1998, 1, 2),
			new DateValueImpl(1998, 1, 15)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void monthlyOnTheFirstAndLastFor10Occ() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.count(10)
			.byMonthDay(-1, 1)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 30);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 30),
			new DateValueImpl(1997, 10, 1),
			new DateValueImpl(1997, 10, 31),
			new DateValueImpl(1997, 11, 1),
			new DateValueImpl(1997, 11, 30),
			new DateValueImpl(1997, 12, 1),
			new DateValueImpl(1997, 12, 31),
			new DateValueImpl(1998, 1, 1),
			new DateValueImpl(1998, 1, 31),
			new DateValueImpl(1998, 2, 1)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void every18MonthsOnThe10thThru15thFor10Occ() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.interval(18)
			.count(10)
			.byMonthDay(10, 11, 12, 13, 14, 15)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 10);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 10),
			new DateValueImpl(1997, 9, 11),
			new DateValueImpl(1997, 9, 12),
			new DateValueImpl(1997, 9, 13),
			new DateValueImpl(1997, 9, 14),
			new DateValueImpl(1997, 9, 15),
			new DateValueImpl(1999, 3, 10),
			new DateValueImpl(1999, 3, 11),
			new DateValueImpl(1999, 3, 12),
			new DateValueImpl(1999, 3, 13)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void everyTuesdayEveryOtherMonth() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.interval(2)
			.byDay(DayOfWeek.TUESDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 2);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 2),
			new DateValueImpl(1997, 9, 9),
			new DateValueImpl(1997, 9, 16),
			new DateValueImpl(1997, 9, 23),
			new DateValueImpl(1997, 9, 30),
			new DateValueImpl(1997, 11, 4),
			new DateValueImpl(1997, 11, 11),
			new DateValueImpl(1997, 11, 18),
			new DateValueImpl(1997, 11, 25),
			new DateValueImpl(1998, 1, 6),
			new DateValueImpl(1998, 1, 13),
			new DateValueImpl(1998, 1, 20),
			new DateValueImpl(1998, 1, 27),
			new DateValueImpl(1998, 3, 3),
			new DateValueImpl(1998, 3, 10),
			new DateValueImpl(1998, 3, 17),
			new DateValueImpl(1998, 3, 24),
			new DateValueImpl(1998, 3, 31)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void yearlyInJuneAndJulyFor10Occurrences() {
		/*
		 * Note: Since none of the BYDAY, BYMONTHDAY or BYYEARDAY components are
		 * specified, the day is gotten from DTSTART
		 */
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.count(10)
			.byMonth(6, 7)
		.build();
		DateValue start = new DateValueImpl(1997, 6, 10);
		DateValue[] expected = {
			new DateValueImpl(1997, 6, 10),
			new DateValueImpl(1997, 7, 10),
			new DateValueImpl(1998, 6, 10),
			new DateValueImpl(1998, 7, 10),
			new DateValueImpl(1999, 6, 10),
			new DateValueImpl(1999, 7, 10),
			new DateValueImpl(2000, 6, 10),
			new DateValueImpl(2000, 7, 10),
			new DateValueImpl(2001, 6, 10),
			new DateValueImpl(2001, 7, 10),
		};
		
		run(recur, start, expected);
	}

	@Test
	public void everyOtherYearOnJanuaryFebruaryAndMarchFor10Occurrences() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.interval(2)
			.count(10)
			.byMonth(1, 2, 3)
		.build();
		DateValue start = new DateValueImpl(1997, 3, 10);
		DateValue[] expected = {
			new DateValueImpl(1997, 3, 10),
			new DateValueImpl(1999, 1, 10),
			new DateValueImpl(1999, 2, 10),
			new DateValueImpl(1999, 3, 10),
			new DateValueImpl(2001, 1, 10),
			new DateValueImpl(2001, 2, 10),
			new DateValueImpl(2001, 3, 10),
			new DateValueImpl(2003, 1, 10),
			new DateValueImpl(2003, 2, 10),
			new DateValueImpl(2003, 3, 10)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void every3rdYearOnThe1st100thAnd200thDayFor10Occurrences() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.interval(3)
			.count(10)
			.byYearDay(1, 100, 200)
		.build();
		DateValue start = new DateValueImpl(1997, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1997, 1, 1),
			new DateValueImpl(1997, 4, 10),
			new DateValueImpl(1997, 7, 19),
			new DateValueImpl(2000, 1, 1),
			new DateValueImpl(2000, 4, 9),
			new DateValueImpl(2000, 7, 18),
			new DateValueImpl(2003, 1, 1),
			new DateValueImpl(2003, 4, 10),
			new DateValueImpl(2003, 7, 19),
			new DateValueImpl(2006, 1, 1)
		};
		
		run(recur, start, expected);
	}

	@Test
	public void every20thMondayOfTheYearForever() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byDay(20, DayOfWeek.MONDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 5, 19);
		DateValue[] expected = {
			new DateValueImpl(1997, 5, 19),
			new DateValueImpl(1998, 5, 18),
			new DateValueImpl(1999, 5, 17)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void mondayOfWeekNumber20WhereTheDefaultStartOfTheWeekIsMonday() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byWeekNo(20)
			.byDay(DayOfWeek.MONDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 5, 12);
		DateValue[] expected = {
			new DateValueImpl(1997, 5, 12),
			new DateValueImpl(1998, 5, 11),
			new DateValueImpl(1999, 5, 17)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void everyThursdayInMarchForever() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byMonth(3)
			.byDay(DayOfWeek.THURSDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 3, 13);
		DateValue[] expected = {
			new DateValueImpl(1997, 3, 13),
			new DateValueImpl(1997, 3, 20),
			new DateValueImpl(1997, 3, 27),
			new DateValueImpl(1998, 3, 5),
			new DateValueImpl(1998, 3, 12),
			new DateValueImpl(1998, 3, 19),
			new DateValueImpl(1998, 3, 26),
			new DateValueImpl(1999, 3, 4),
			new DateValueImpl(1999, 3, 11),
			new DateValueImpl(1999, 3, 18),
			new DateValueImpl(1999, 3, 25)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void everyThursdayButOnlyDuringJuneJulyAndAugustForever() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byDay(DayOfWeek.THURSDAY)
			.byMonth(6, 7, 8)
		.build();
		DateValue start = new DateValueImpl(1997, 6, 5);
		DateValue[] expected = {
			new DateValueImpl(1997, 6, 5),
			new DateValueImpl(1997, 6, 12),
			new DateValueImpl(1997, 6, 19),
			new DateValueImpl(1997, 6, 26),
			new DateValueImpl(1997, 7, 3),
			new DateValueImpl(1997, 7, 10),
			new DateValueImpl(1997, 7, 17),
			new DateValueImpl(1997, 7, 24),
			new DateValueImpl(1997, 7, 31),
			new DateValueImpl(1997, 8, 7),
			new DateValueImpl(1997, 8, 14),
			new DateValueImpl(1997, 8, 21),
			new DateValueImpl(1997, 8, 28),
			new DateValueImpl(1998, 6, 4),
			new DateValueImpl(1998, 6, 11),
			new DateValueImpl(1998, 6, 18),
			new DateValueImpl(1998, 6, 25),
			new DateValueImpl(1998, 7, 2),
			new DateValueImpl(1998, 7, 9),
			new DateValueImpl(1998, 7, 16),
			new DateValueImpl(1998, 7, 23),
			new DateValueImpl(1998, 7, 30),
			new DateValueImpl(1998, 8, 6),
			new DateValueImpl(1998, 8, 13),
			new DateValueImpl(1998, 8, 20),
			new DateValueImpl(1998, 8, 27),
			new DateValueImpl(1999, 6, 3),
			new DateValueImpl(1999, 6, 10),
			new DateValueImpl(1999, 6, 17),
			new DateValueImpl(1999, 6, 24),
			new DateValueImpl(1999, 7, 1),
			new DateValueImpl(1999, 7, 8),
			new DateValueImpl(1999, 7, 15),
			new DateValueImpl(1999, 7, 22),
			new DateValueImpl(1999, 7, 29),
			new DateValueImpl(1999, 8, 5),
			new DateValueImpl(1999, 8, 12),
			new DateValueImpl(1999, 8, 19),
			new DateValueImpl(1999, 8, 26)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void everyFridayThe13thForever() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.byDay(DayOfWeek.FRIDAY)
			.byMonthDay(13)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 2);
		DateValue[] expected = {
			new DateValueImpl(1998, 2, 13),
			new DateValueImpl(1998, 3, 13),
			new DateValueImpl(1998, 11, 13),
			new DateValueImpl(1999, 8, 13),
			new DateValueImpl(2000, 10, 13)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void theFirstSaturdayThatFollowsTheFirstSundayOfTheMonthForever() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.byDay(DayOfWeek.SATURDAY)
			.byMonthDay(7, 8, 9, 10, 11, 12, 13)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 13);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 13),
			new DateValueImpl(1997, 10, 11),
			new DateValueImpl(1997, 11, 8),
			new DateValueImpl(1997, 12, 13),
			new DateValueImpl(1998, 1, 10),
			new DateValueImpl(1998, 2, 7),
			new DateValueImpl(1998, 3, 7),
			new DateValueImpl(1998, 4, 11),
			new DateValueImpl(1998, 5, 9),
			new DateValueImpl(1998, 6, 13),
		};
	
		run(recur, start, expected);
	}

	@Test
	public void every4YearsThe1stTuesAfterAMonInNovForever() {
		// US Presidential Election Day
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.interval(4)
			.byMonth(11)
			.byDay(DayOfWeek.TUESDAY)
			.byMonthDay(2, 3, 4, 5, 6, 7, 8)
		.build();
		DateValue start = new DateValueImpl(1996, 11, 5);
		DateValue[] expected = {
			new DateValueImpl(1996, 11, 5),
			new DateValueImpl(2000, 11, 7),
			new DateValueImpl(2004, 11, 2)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void the3rdInstanceIntoTheMonthOfOneOfTuesWedThursForNext3Months() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.count(3)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY)
			.bySetPos(3)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 4);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 4),
			new DateValueImpl(1997, 10, 7),
			new DateValueImpl(1997, 11, 6)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void the2ndToLastWeekdayOfTheMonth() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
			.bySetPos(-2)
		.build();
		DateValue start = new DateValueImpl(1997, 9, 29);
		DateValue[] expected = {
			new DateValueImpl(1997, 9, 29),
			new DateValueImpl(1997, 10, 30),
			new DateValueImpl(1997, 11, 27),
			new DateValueImpl(1997, 12, 30),
			new DateValueImpl(1998, 1, 29),
			new DateValueImpl(1998, 2, 26),
			new DateValueImpl(1998, 3, 30)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void every3HoursFrom900AmTo500PmOnASpecificDay() {
		Recurrence recur = new Recurrence.Builder(Frequency.HOURLY)
			.interval(3)
			.until(date(1997, 9, 3, 9, 0, 0, UTC))
		.build();
		DateValue start = new DateTimeValueImpl(1997, 9, 2, 9, 0, 0);
		DateValue[] expected = {
			new DateTimeValueImpl(1997, 9, 2, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 2, 12, 0, 0),
			new DateTimeValueImpl(1997, 9, 2, 15, 0, 0),
			new DateTimeValueImpl(1997, 9, 2, 18, 0, 0),
			new DateTimeValueImpl(1997, 9, 2, 21, 0, 0),
			new DateTimeValueImpl(1997, 9, 3, 0, 0, 0),
			new DateTimeValueImpl(1997, 9, 3, 3, 0, 0),
			new DateTimeValueImpl(1997, 9, 3, 6, 0, 0),
			new DateTimeValueImpl(1997, 9, 3, 9, 0, 0)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void every15MinutesFor6Occurrences() {
		Recurrence recur = new Recurrence.Builder(Frequency.MINUTELY)
			.interval(15)
			.count(6)
		.build();
		DateValue start = new DateTimeValueImpl(1997, 9, 2, 9, 0, 0);
		DateValue[] expected = {
			new DateTimeValueImpl(1997, 9, 2, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 2, 9, 15, 0),
			new DateTimeValueImpl(1997, 9, 2, 9, 30, 0),
			new DateTimeValueImpl(1997, 9, 2, 9, 45, 0),
			new DateTimeValueImpl(1997, 9, 2, 10, 0, 0),
			new DateTimeValueImpl(1997, 9, 2, 10, 15, 0)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void everyHourAndAHalfFor4Occurrences() {
		Recurrence recur = new Recurrence.Builder(Frequency.MINUTELY)
			.interval(90)
			.count(4)
		.build();
		DateValue start = new DateTimeValueImpl(1997, 9, 2, 9, 0, 0);
		DateValue[] expected = {
			new DateTimeValueImpl(1997, 9, 2, 9, 0, 0),
			new DateTimeValueImpl(1997, 9, 2, 10, 30, 0),
			new DateTimeValueImpl(1997, 9, 2, 12, 0, 0),
			new DateTimeValueImpl(1997, 9, 2, 13, 30, 0)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void evert20MinutesFrom900AMto440PMEveryDay() {
		{
			Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
				.byHour(9, 10, 11, 12, 13, 14, 15, 16)
				.byMinute(0, 20, 40)
			.build();
			DateValue start = new DateTimeValueImpl(1997, 9, 2, 9, 0, 0);
			DateValue[] expected = {
				new DateTimeValueImpl(1997, 9, 2, 9, 0, 0), new DateTimeValueImpl(1997, 9, 2, 9, 20, 0), new DateTimeValueImpl(1997, 9, 2, 9, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 10, 0, 0), new DateTimeValueImpl(1997, 9, 2, 10, 20, 0), new DateTimeValueImpl(1997, 9, 2, 10, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 11, 0, 0), new DateTimeValueImpl(1997, 9, 2, 11, 20, 0), new DateTimeValueImpl(1997, 9, 2, 11, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 12, 0, 0), new DateTimeValueImpl(1997, 9, 2, 12, 20, 0), new DateTimeValueImpl(1997, 9, 2, 12, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 13, 0, 0), new DateTimeValueImpl(1997, 9, 2, 13, 20, 0), new DateTimeValueImpl(1997, 9, 2, 13, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 14, 0, 0), new DateTimeValueImpl(1997, 9, 2, 14, 20, 0), new DateTimeValueImpl(1997, 9, 2, 14, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 15, 0, 0), new DateTimeValueImpl(1997, 9, 2, 15, 20, 0), new DateTimeValueImpl(1997, 9, 2, 15, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 16, 0, 0), new DateTimeValueImpl(1997, 9, 2, 16, 20, 0), new DateTimeValueImpl(1997, 9, 2, 16, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 9, 0, 0), new DateTimeValueImpl(1997, 9, 3, 9, 20, 0), new DateTimeValueImpl(1997, 9, 3, 9, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 10, 0, 0), new DateTimeValueImpl(1997, 9, 3, 10, 20, 0), new DateTimeValueImpl(1997, 9, 3, 10, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 11, 0, 0), new DateTimeValueImpl(1997, 9, 3, 11, 20, 0), new DateTimeValueImpl(1997, 9, 3, 11, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 12, 0, 0), new DateTimeValueImpl(1997, 9, 3, 12, 20, 0), new DateTimeValueImpl(1997, 9, 3, 12, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 13, 0, 0), new DateTimeValueImpl(1997, 9, 3, 13, 20, 0), new DateTimeValueImpl(1997, 9, 3, 13, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 14, 0, 0), new DateTimeValueImpl(1997, 9, 3, 14, 20, 0), new DateTimeValueImpl(1997, 9, 3, 14, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 15, 0, 0), new DateTimeValueImpl(1997, 9, 3, 15, 20, 0), new DateTimeValueImpl(1997, 9, 3, 15, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 16, 0, 0), new DateTimeValueImpl(1997, 9, 3, 16, 20, 0), new DateTimeValueImpl(1997, 9, 3, 16, 40, 0)
			};
		
			run(recur, start, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.MINUTELY)
				.interval(20)
				.byHour(9, 10, 11, 12, 13, 14, 15, 16)
			.build();
			DateValue start = new DateTimeValueImpl(1997, 9, 2, 9, 0, 0);
			DateValue[] expected = {
				new DateTimeValueImpl(1997, 9, 2, 9, 0, 0), new DateTimeValueImpl(1997, 9, 2, 9, 20, 0), new DateTimeValueImpl(1997, 9, 2, 9, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 10, 0, 0), new DateTimeValueImpl(1997, 9, 2, 10, 20, 0), new DateTimeValueImpl(1997, 9, 2, 10, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 11, 0, 0), new DateTimeValueImpl(1997, 9, 2, 11, 20, 0), new DateTimeValueImpl(1997, 9, 2, 11, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 12, 0, 0), new DateTimeValueImpl(1997, 9, 2, 12, 20, 0), new DateTimeValueImpl(1997, 9, 2, 12, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 13, 0, 0), new DateTimeValueImpl(1997, 9, 2, 13, 20, 0), new DateTimeValueImpl(1997, 9, 2, 13, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 14, 0, 0), new DateTimeValueImpl(1997, 9, 2, 14, 20, 0), new DateTimeValueImpl(1997, 9, 2, 14, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 15, 0, 0), new DateTimeValueImpl(1997, 9, 2, 15, 20, 0), new DateTimeValueImpl(1997, 9, 2, 15, 40, 0),
				new DateTimeValueImpl(1997, 9, 2, 16, 0, 0), new DateTimeValueImpl(1997, 9, 2, 16, 20, 0), new DateTimeValueImpl(1997, 9, 2, 16, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 9, 0, 0), new DateTimeValueImpl(1997, 9, 3, 9, 20, 0), new DateTimeValueImpl(1997, 9, 3, 9, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 10, 0, 0), new DateTimeValueImpl(1997, 9, 3, 10, 20, 0), new DateTimeValueImpl(1997, 9, 3, 10, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 11, 0, 0), new DateTimeValueImpl(1997, 9, 3, 11, 20, 0), new DateTimeValueImpl(1997, 9, 3, 11, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 12, 0, 0), new DateTimeValueImpl(1997, 9, 3, 12, 20, 0), new DateTimeValueImpl(1997, 9, 3, 12, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 13, 0, 0), new DateTimeValueImpl(1997, 9, 3, 13, 20, 0), new DateTimeValueImpl(1997, 9, 3, 13, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 14, 0, 0), new DateTimeValueImpl(1997, 9, 3, 14, 20, 0), new DateTimeValueImpl(1997, 9, 3, 14, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 15, 0, 0), new DateTimeValueImpl(1997, 9, 3, 15, 20, 0), new DateTimeValueImpl(1997, 9, 3, 15, 40, 0),
				new DateTimeValueImpl(1997, 9, 3, 16, 0, 0), new DateTimeValueImpl(1997, 9, 3, 16, 20, 0), new DateTimeValueImpl(1997, 9, 3, 16, 40, 0)
			};
		
			run(recur, start, expected);
		}
	}

	@Test
	public void anExampleWhereTheDaysGeneratedMakesADifferenceBecauseOfWkst() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.count(4)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY)
			.workweekStarts(DayOfWeek.MONDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 8, 5);
		DateValue[] expected = {
			new DateValueImpl(1997, 8, 5),
			new DateValueImpl(1997, 8, 10),
			new DateValueImpl(1997, 8, 19),
			new DateValueImpl(1997, 8, 24)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void anExampleWhereTheDaysGeneratedMakesADifferenceBecauseOfWkst2() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.count(4)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY)
			.workweekStarts(DayOfWeek.SUNDAY)
		.build();
		DateValue start = new DateValueImpl(1997, 8, 5);
		DateValue[] expected = {
			new DateValueImpl(1997, 8, 5),
			new DateValueImpl(1997, 8, 17),
			new DateValueImpl(1997, 8, 19),
			new DateValueImpl(1997, 8, 31)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void withByDayAndByMonthDayFilter() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.count(4)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY)
			.byMonthDay(13, 14, 15, 16, 17, 18, 19, 20)
		.build();
		DateValue start = new DateValueImpl(1997, 8, 5);
		DateValue[] expected = {
			new DateValueImpl(1997, 8, 17),
			new DateValueImpl(1997, 8, 19),
			new DateValueImpl(1997, 9, 14),
			new DateValueImpl(1997, 9, 16)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void annuallyInAugustOnTuesAndSunBetween13thAnd20th() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.count(4)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY)
			.byMonthDay(13, 14, 15, 16, 17, 18, 19, 20)
			.byMonth(8)
		.build();
		DateValue start = new DateValueImpl(1997, 6, 5);
		DateValue[] expected = {
			new DateValueImpl(1997, 8, 17),
			new DateValueImpl(1997, 8, 19),
			new DateValueImpl(1998, 8, 16),
			new DateValueImpl(1998, 8, 18)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void lastDayOfTheYearIsASundayOrTuesday() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.count(4)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY)
			.byYearDay(-1)
		.build();
		DateValue start = new DateValueImpl(1994, 6, 5);
		DateValue[] expected = {
			new DateValueImpl(1995, 12, 31),
			new DateValueImpl(1996, 12, 31),
			new DateValueImpl(2000, 12, 31),
			new DateValueImpl(2002, 12, 31)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void lastWeekdayOfMonth() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.bySetPos(-1)
			.byDay(-1, DayOfWeek.MONDAY)
			.byDay(-1, DayOfWeek.TUESDAY)
			.byDay(-1, DayOfWeek.WEDNESDAY)
			.byDay(-1, DayOfWeek.THURSDAY)
			.byDay(-1, DayOfWeek.FRIDAY)
		.build();
		DateValue start = new DateValueImpl(1994, 6, 5);
		DateValue[] expected = {
			new DateValueImpl(1994, 6, 30),
			new DateValueImpl(1994, 7, 29),
			new DateValueImpl(1994, 8, 31),
			new DateValueImpl(1994, 9, 30),
			new DateValueImpl(1994, 10, 31),
			new DateValueImpl(1994, 11, 30),
			new DateValueImpl(1994, 12, 30),
			new DateValueImpl(1995, 1, 31)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void monthsThatStartOrEndOnFriday() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.byMonthDay(1, -1)
			.byDay(DayOfWeek.FRIDAY)
			.count(6)
		.build();
		DateValue start = new DateValueImpl(1994, 6, 5);
		DateValue[] expected = {
			new DateValueImpl(1994, 7, 1),
			new DateValueImpl(1994, 9, 30),
			new DateValueImpl(1995, 3, 31),
			new DateValueImpl(1995, 6, 30),
			new DateValueImpl(1995, 9, 1),
			new DateValueImpl(1995, 12, 1)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void monthsThatStartOrEndOnFridayOnEvenWeeks() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.byMonthDay(1, -1)
			.byDay(DayOfWeek.FRIDAY)
			.count(3)
		.build();
		DateValue start = new DateValueImpl(1994, 6, 3);
		
		/*
		 * Figure out which of the answers from the above test fall on even weeks.
		 */
		List<DateValue> expected = new ArrayList<DateValue>();
		for (DateValue candidate : new DateValue[] {
			new DateValueImpl(1994, 7, 1),
			new DateValueImpl(1994, 9, 30),
			new DateValueImpl(1995, 3, 31),
			new DateValueImpl(1995, 6, 30),
			new DateValueImpl(1995, 9, 1),
			new DateValueImpl(1995, 12, 1),
		}) {
			if (TimeUtils.daysBetween(candidate, start) % 14 == 0) {
				expected.add(candidate);
			}
		}
	
		run(recur, start, expected.toArray(new DateValue[0]));
	}

	@Test
	public void centuriesThatAreNotLeapYears() {
		/*
		 * I can't think of a good reason anyone would want to specify both a month
		 * day and a year day, so here's a really contrived example.
		 */
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.interval(100)
			.byYearDay(60)
			.byMonthDay(1)
		.build();
		DateValue start = new DateValueImpl(1900, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1900, 3, 1),
			new DateValueImpl(2100, 3, 1),
			new DateValueImpl(2200, 3, 1),
			new DateValueImpl(2300, 3, 1)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void nextCalledWithoutHasNext() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).build();
		DateValue start = new DateValueImpl(2000, 1, 1);
		
		RecurrenceIterator it = RecurrenceIteratorFactory.createRecurrenceIterator(recur, start, UTC);
		assertEquals(new DateValueImpl(2000, 1, 1), it.next());
		assertEquals(new DateValueImpl(2000, 1, 2), it.next());
		assertEquals(new DateValueImpl(2000, 1, 3), it.next());
	}

	@Test
	public void noInstancesGenerated() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
			.until(new ICalDate(date(1999, 1, 1), false))
		.build();
		DateValue start = new DateValueImpl(2000, 1, 1);
		
		RecurrenceIterator it = RecurrenceIteratorFactory.createRecurrenceIterator(recur, start, UTC);
		assertFalse(it.hasNext());
		assertNull(it.next());
		assertNull(it.next());
		assertNull(it.next());
	}

	@Test
	public void noInstancesGenerated2() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byMonth(2)
			.byMonthDay(30)
		.build();
		DateValue start = new DateValueImpl(2000, 1, 1);
		
		RecurrenceIterator it = RecurrenceIteratorFactory.createRecurrenceIterator(recur, start, UTC);
		assertFalse(it.hasNext());
	}

	@Test
	public void noInstancesGenerated3() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.interval(4)
			.byYearDay(366)
		.build();
		DateValue start = new DateValueImpl(2001, 1, 1);
		
		RecurrenceIterator it = RecurrenceIteratorFactory.createRecurrenceIterator(recur, start, UTC);
		assertFalse(it.hasNext());
	}

	@Test
	public void lastWeekdayOfMarch() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.byMonth(3)
			.byDay(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
			.bySetPos(-1)
		.build();
		DateValue start = new DateValueImpl(2000, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(2000, 3, 26),
			new DateValueImpl(2001, 3, 31),
			new DateValueImpl(2002, 3, 31),
			new DateValueImpl(2003, 3, 30)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void firstWeekdayOfMarch() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.byMonth(3)
			.byDay(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
			.bySetPos(1)
		.build();
		DateValue start = new DateValueImpl(2000, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(2000, 3, 4),
			new DateValueImpl(2001, 3, 3),
			new DateValueImpl(2002, 3, 2),
			new DateValueImpl(2003, 3, 1)
		};
	
		run(recur, start, expected);
	}


	//		 January 1999
	// Mo Tu We Th Fr Sa Su
	//							1	2	3		// < 4 days, so not a week
	//	4	5	6	7	8	9 10

	//		 January 2000
	// Mo Tu We Th Fr Sa Su
	//								 1	2		// < 4 days, so not a week
	//	3	4	5	6	7	8	9

	//		 January 2001
	// Mo Tu We Th Fr Sa Su
	//	1	2	3	4	5	6	7
	//	8	9 10 11 12 13 14

	//		 January 2002
	// Mo Tu We Th Fr Sa Su
	//		 1	2	3	4	5	6
	//	7	8	9 10 11 12 13

	/**
	 * Find the first weekday of the first week of the year.
	 * The first week of the year may be partial, and the first week is considered
	 * to be the first one with at least four days.
	 */
	@Test
	public void firstWeekdayOfFirstWeekOfYear() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byWeekNo(1)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
			.bySetPos(1)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1999, 1, 4),
			new DateValueImpl(2000, 1, 3),
			new DateValueImpl(2001, 1, 1),
			new DateValueImpl(2002, 1, 1)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void firstSundayOfTheYear1() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byWeekNo(1)
			.byDay(DayOfWeek.SUNDAY)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1999, 1, 10),
			new DateValueImpl(2000, 1, 9),
			new DateValueImpl(2001, 1, 7),
			new DateValueImpl(2002, 1, 6)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void firstSundayOfTheYear2() {
		// TODO(msamuel): is this right?
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byDay(1, DayOfWeek.SUNDAY)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1999, 1, 3),
			new DateValueImpl(2000, 1, 2),
			new DateValueImpl(2001, 1, 7),
			new DateValueImpl(2002, 1, 6)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void firstSundayOfTheYear3() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byDay(DayOfWeek.SUNDAY)
			.byYearDay(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13)
			.bySetPos(1)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1999, 1, 3),
			new DateValueImpl(2000, 1, 2),
			new DateValueImpl(2001, 1, 7),
			new DateValueImpl(2002, 1, 6)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void firstWeekdayOfYear() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
			.bySetPos(1)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1999, 1, 1),
			new DateValueImpl(2000, 1, 3),
			new DateValueImpl(2001, 1, 1),
			new DateValueImpl(2002, 1, 1)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void lastWeekdayOfFirstWeekOfYear() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byWeekNo(1)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
			.bySetPos(-1)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1999, 1, 8),
			new DateValueImpl(2000, 1, 7),
			new DateValueImpl(2001, 1, 5),
			new DateValueImpl(2002, 1, 4)
		};

		run(recur, start, expected);
	}

	//		 January 1999
	// Mo Tu We Th Fr Sa Su
	//							1	2	3
	//	4	5	6	7	8	9 10
	// 11 12 13 14 15 16 17
	// 18 19 20 21 22 23 24
	// 25 26 27 28 29 30 31

	@Test
	public void secondWeekday1() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
			.bySetPos(2)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1999, 1, 5),
			new DateValueImpl(1999, 1, 12),
			new DateValueImpl(1999, 1, 19),
			new DateValueImpl(1999, 1, 26)
		};
	
		run(recur, start, expected);
	}

	//		 January 1997
	// Mo Tu We Th Fr Sa Su
	//				1	2	3	4	5
	//	6	7	8	9 10 11 12
	// 13 14 15 16 17 18 19
	// 20 21 22 23 24 25 26
	// 27 28 29 30 31

	@Test
	public void secondWeekday2() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
			.bySetPos(2)
		.build();
		DateValue start = new DateValueImpl(1997, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1997, 1, 2),
			new DateValueImpl(1997, 1, 7),
			new DateValueImpl(1997, 1, 14),
			new DateValueImpl(1997, 1, 21)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void byYearDayAndByDayFilterInteraction() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byYearDay(15)
			.byDay(3, DayOfWeek.MONDAY)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(2001, 1, 15),
			new DateValueImpl(2007, 1, 15),
			new DateValueImpl(2018, 1, 15),
			new DateValueImpl(2024, 1, 15)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void byDayWithNegWeekNoAsFilter() {
		Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
			.byMonthDay(26)
			.byDay(-1, DayOfWeek.FRIDAY)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1999, 2, 26),
			new DateValueImpl(1999, 3, 26),
			new DateValueImpl(1999, 11, 26),
			new DateValueImpl(2000, 5, 26)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void lastWeekOfTheYear() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.byWeekNo(-1)
		.build();
		DateValue start = new DateValueImpl(1999, 1, 1);
		DateValue[] expected = {
			new DateValueImpl(1999, 12, 27),
			new DateValueImpl(1999, 12, 28),
			new DateValueImpl(1999, 12, 29),
			new DateValueImpl(1999, 12, 30),
			new DateValueImpl(1999, 12, 31),
			new DateValueImpl(2000, 12, 25)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void userSubmittedTest1() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.interval(2)
			.workweekStarts(DayOfWeek.WEDNESDAY)
			.byDay(DayOfWeek.SUNDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.SATURDAY)
			.until(date(2000, 2, 15, 11, 30, 0, UTC))
		.build();
		DateValue start = new DateTimeValueImpl(2000, 1, 27, 3, 30, 0);
		DateValue[] expected = {
			new DateTimeValueImpl(2000, 1, 27, 3, 30, 0),
			new DateTimeValueImpl(2000, 1, 29, 3, 30, 0),
			new DateTimeValueImpl(2000, 1, 30, 3, 30, 0),
			new DateTimeValueImpl(2000, 2, 1, 3, 30, 0),
			new DateTimeValueImpl(2000, 2, 10, 3, 30, 0),
			new DateTimeValueImpl(2000, 2, 12, 3, 30, 0),
			new DateTimeValueImpl(2000, 2, 13, 3, 30, 0),
			new DateTimeValueImpl(2000, 2, 15, 3, 30, 0)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void advanceTo() {
		//a bunch of tests grabbed from above with an advance-to date tacked on
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.byMonth(3)
				.byDay(DayOfWeek.THURSDAY)
			.build();
			DateValue start = new DateValueImpl(1997, 3, 13);
			DateValue advanceTo = new DateValueImpl(1997, 6, 1);
			DateValue[] expected = {
				//new DateValueImpl(1997, 3, 13),
				//new DateValueImpl(1997, 3, 20),
				//new DateValueImpl(1997, 3, 27),
				new DateValueImpl(1998, 3, 5),
				new DateValueImpl(1998, 3, 12),
				new DateValueImpl(1998, 3, 19),
				new DateValueImpl(1998, 3, 26),
				new DateValueImpl(1999, 3, 4),
				new DateValueImpl(1999, 3, 11),
				new DateValueImpl(1999, 3, 18),
				new DateValueImpl(1999, 3, 25),
				new DateValueImpl(2000, 3, 2),
				new DateValueImpl(2000, 3, 9),
				new DateValueImpl(2000, 3, 16)
			};
		
			run(recur, start, advanceTo, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.byDay(20, DayOfWeek.MONDAY)
			.build();
			DateValue start = new DateValueImpl(1997, 5, 19);
			DateValue advanceTo = new DateValueImpl(1998, 5, 15);
			DateValue[] expected = {
				//new DateValueImpl(1997, 5, 19),
				new DateValueImpl(1998, 5, 18),
				new DateValueImpl(1999, 5, 17),
				new DateValueImpl(2000, 5, 15)
			};
		
			run(recur, start, advanceTo, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.interval(3)
				.until(new ICalDate(date(2009, 1, 1), false))
				.byYearDay(1, 100, 200)
			.build();
			DateValue start = new DateValueImpl(1997, 1, 1);
			DateValue advanceTo = new DateValueImpl(2000, 2, 28);
			DateValue[] expected = {
				//new DateValueImpl(1997, 1, 1),
				//new DateValueImpl(1997, 4, 10),
				//new DateValueImpl(1997, 7, 19),
				//new DateValueImpl(2000, 1, 1),
				new DateValueImpl(2000, 4, 9),
				new DateValueImpl(2000, 7, 18),
				new DateValueImpl(2003, 1, 1),
				new DateValueImpl(2003, 4, 10),
				new DateValueImpl(2003, 7, 19),
				new DateValueImpl(2006, 1, 1),
				new DateValueImpl(2006, 4, 10),
				new DateValueImpl(2006, 7, 19),
				new DateValueImpl(2009, 1, 1)
			};
		
			run(recur, start, advanceTo, expected);
		}

		//make sure that count preserved
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.interval(3)
				.count(10)
				.byYearDay(1, 100, 200)
			.build();
			DateValue start = new DateValueImpl(1997, 1, 1);
			DateValue advanceTo = new DateValueImpl(2000, 2, 28);
			DateValue[] expected = {
				//new DateValueImpl(1997, 1, 1),
				//new DateValueImpl(1997, 4, 10),
				//new DateValueImpl(1997, 7, 19),
				//new DateValueImpl(2000, 1, 1),
				new DateValueImpl(2000, 4, 9),
				new DateValueImpl(2000, 7, 18),
				new DateValueImpl(2003, 1, 1),
				new DateValueImpl(2003, 4, 10),
				new DateValueImpl(2003, 7, 19),
				new DateValueImpl(2006, 1, 1)
			};
		
			run(recur, start, advanceTo, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.interval(2)
				.count(10)
				.byMonth(1, 2, 3)
			.build();
			DateValue start = new DateValueImpl(1997, 3, 10);
			DateValue advanceTo = new DateValueImpl(1998, 4, 1);
			DateValue[] expected = {
				//new DateValueImpl(1997, 3, 10),
				new DateValueImpl(1999, 1, 10),
				new DateValueImpl(1999, 2, 10),
				new DateValueImpl(1999, 3, 10),
				new DateValueImpl(2001, 1, 10),
				new DateValueImpl(2001, 2, 10),
				new DateValueImpl(2001, 3, 10),
				new DateValueImpl(2003, 1, 10),
				new DateValueImpl(2003, 2, 10),
				new DateValueImpl(2003, 3, 10)
			};
			
			run(recur, start, advanceTo, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
				.until(new ICalDate(date(1997, 12, 24), false))
			.build();
			DateValue start = new DateValueImpl(1997, 9, 2);
			DateValue advanceTo = new DateValueImpl(1997, 9, 30);
			DateValue[] expected = {
				//new DateValueImpl(1997, 9, 2),
				//new DateValueImpl(1997, 9, 9),
				//new DateValueImpl(1997, 9, 16),
				//new DateValueImpl(1997, 9, 23),
				new DateValueImpl(1997, 9, 30),
				new DateValueImpl(1997, 10, 7),
				new DateValueImpl(1997, 10, 14),
				new DateValueImpl(1997, 10, 21),
				new DateValueImpl(1997, 10, 28),
				new DateValueImpl(1997, 11, 4),
				new DateValueImpl(1997, 11, 11),
				new DateValueImpl(1997, 11, 18),
				new DateValueImpl(1997, 11, 25),
				new DateValueImpl(1997, 12, 2),
				new DateValueImpl(1997, 12, 9),
				new DateValueImpl(1997, 12, 16),
				new DateValueImpl(1997, 12, 23)
			};
			
			run(recur, start, advanceTo, expected);
		}
		
		{
			Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
				.interval(18)
				.byMonthDay(10, 11, 12, 13, 14, 15)
			.build();
			DateValue start = new DateValueImpl(1997, 9, 10);
			DateValue advanceTo = new DateValueImpl(1999, 1, 1);
			DateValue[] expected = {
				//new DateValueImpl(1997, 9, 10),
				//new DateValueImpl(1997, 9, 11),
				//new DateValueImpl(1997, 9, 12),
				//new DateValueImpl(1997, 9, 13),
				//new DateValueImpl(1997, 9, 14),
				//new DateValueImpl(1997, 9, 15),
				new DateValueImpl(1999, 3, 10),
				new DateValueImpl(1999, 3, 11),
				new DateValueImpl(1999, 3, 12),
				new DateValueImpl(1999, 3, 13),
				new DateValueImpl(1999, 3, 14)
			};
			
			run(recur, start, advanceTo, expected);
		}

		//advancing into the past
		{
			Recurrence recur = new Recurrence.Builder(Frequency.MONTHLY)
				.interval(18)
				.byMonthDay(10, 11, 12, 13, 14, 15)
			.build();
			DateValue start = new DateValueImpl(1997, 9, 10);
			DateValue advanceTo = new DateValueImpl(1997, 9, 1);
			DateValue[] expected = {
				new DateValueImpl(1997, 9, 10),
				new DateValueImpl(1997, 9, 11),
				new DateValueImpl(1997, 9, 12),
				new DateValueImpl(1997, 9, 13),
				new DateValueImpl(1997, 9, 14),
				new DateValueImpl(1997, 9, 15),
				new DateValueImpl(1999, 3, 10),
				new DateValueImpl(1999, 3, 11),
				new DateValueImpl(1999, 3, 12),
				new DateValueImpl(1999, 3, 13),
				new DateValueImpl(1999, 3, 14)
			};
			
			run(recur, start, advanceTo, expected);
		}

		//skips first instance
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.interval(100)
				.byMonth(2)
				.byMonthDay(29)
			.build();
			DateValue start = new DateValueImpl(1900, 1, 1);
			DateValue advanceTo = new DateValueImpl(2004, 1, 1);
			DateValue[] expected = {
				new DateValueImpl(2400, 2, 29),
				new DateValueImpl(2800, 2, 29),
				new DateValueImpl(3200, 2, 29),
				new DateValueImpl(3600, 2, 29),
				new DateValueImpl(4000, 2, 29)
			};
			
			run(recur, start, advanceTo, expected);
		}

		//filter hits until date before first instance
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.interval(100)
				.byMonth(2)
				.byMonthDay(29)
				.until(new ICalDate(date(2100, 1, 1), false))
			.build();
			DateValue start = new DateValueImpl(1900, 1, 1);
			DateValue advanceTo = new DateValueImpl(2004, 1, 1);
			DateValue[] expected = {
			};
			
			run(recur, start, advanceTo, expected);
		}

		//advancing something that returns no instances
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.byMonth(2)
				.byMonthDay(30)
			.build();
			DateValue start = new DateValueImpl(2000, 1, 1);
			DateValue advanceTo = new DateValueImpl(1997, 9, 1);
			DateValue[] expected = {
			};
			
			run(recur, start, advanceTo, expected);
		}
		
		//advancing something that returns no instances and has a BYSETPOS rule
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.byMonth(2)
				.byMonthDay(30, 31)
				.bySetPos(1)
			.build();
			DateValue start = new DateValueImpl(2000, 1, 1);
			DateValue advanceTo = new DateValueImpl(1997, 9, 1);
			DateValue[] expected = {
			};
			
			run(recur, start, advanceTo, expected);
		}

		//advancing way past year generator timeout
		{
			Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
				.byMonth(2)
				.byMonthDay(28)
			.build();
			DateValue start = new DateValueImpl(2000, 1, 1);
			DateValue advanceTo = new DateValueImpl(2500, 9, 1);
			DateValue[] expected = {
			};
			
			run(recur, start, advanceTo, expected);
		}

		// TODO(msamuel): check advancement of more examples
	}

	/**
	 * A testcase that yielded dupes due to BYSETPOS evilness
	 */
	@Test
	public void caseThatYieldedDupes() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.workweekStarts(DayOfWeek.SUNDAY)
			.interval(1)
			.byMonth(9, 1, 12, 8)
			.byMonthDay(-9, -29, 24)
			.bySetPos(-1, -4, 10, -6, -1, -10, -10, -9, -8)
		.build();
		DateValue start = new DateValueImpl(2006, 5, 28);
		DateValue[] expected = {
			new DateValueImpl(2006, 9, 24), new DateValueImpl(2006, 12, 3), new DateValueImpl(2006, 12, 24), new DateValueImpl(2007, 9, 2), new DateValueImpl(2007, 12, 23), new DateValueImpl(2008, 8, 3), new DateValueImpl(2008, 8, 24),
			new DateValueImpl(2009, 8, 23), new DateValueImpl(2010, 1, 3), new DateValueImpl(2010, 1, 24), new DateValueImpl(2011, 1, 23), new DateValueImpl(2012, 9, 2), new DateValueImpl(2012, 12, 23), new DateValueImpl(2013, 9, 22),
			new DateValueImpl(2014, 8, 3), new DateValueImpl(2014, 8, 24), new DateValueImpl(2015, 8, 23), new DateValueImpl(2016, 1, 3), new DateValueImpl(2016, 1, 24), new DateValueImpl(2017, 9, 24), new DateValueImpl(2017, 12, 3),
			new DateValueImpl(2017, 12, 24), new DateValueImpl(2018, 9, 2), new DateValueImpl(2018, 12, 23), new DateValueImpl(2019, 9, 22), new DateValueImpl(2020, 8, 23), new DateValueImpl(2021, 1, 3), new DateValueImpl(2021, 1, 24),
			new DateValueImpl(2022, 1, 23), new DateValueImpl(2023, 9, 24), new DateValueImpl(2023, 12, 3), new DateValueImpl(2023, 12, 24), new DateValueImpl(2024, 9, 22), new DateValueImpl(2025, 8, 3), new DateValueImpl(2025, 8, 24),
			new DateValueImpl(2026, 8, 23), new DateValueImpl(2027, 1, 3), new DateValueImpl(2027, 1, 24), new DateValueImpl(2028, 1, 23), new DateValueImpl(2028, 9, 24), new DateValueImpl(2028, 12, 3), new DateValueImpl(2028, 12, 24),
			new DateValueImpl(2029, 9, 2), new DateValueImpl(2029, 12, 23), new DateValueImpl(2030, 9, 22), new DateValueImpl(2031, 8, 3), new DateValueImpl(2031, 8, 24), new DateValueImpl(2033, 1, 23), new DateValueImpl(2034, 9, 24),
			new DateValueImpl(2034, 12, 3), new DateValueImpl(2034, 12, 24), new DateValueImpl(2035, 9, 2), new DateValueImpl(2035, 12, 23), new DateValueImpl(2036, 8, 3), new DateValueImpl(2036, 8, 24), new DateValueImpl(2037, 8, 23),
			new DateValueImpl(2038, 1, 3), new DateValueImpl(2038, 1, 24), new DateValueImpl(2039, 1, 23), new DateValueImpl(2040, 9, 2), new DateValueImpl(2040, 12, 23), new DateValueImpl(2041, 9, 22), new DateValueImpl(2042, 8, 3),
			new DateValueImpl(2042, 8, 24), new DateValueImpl(2043, 8, 23), new DateValueImpl(2044, 1, 3), new DateValueImpl(2044, 1, 24), new DateValueImpl(2045, 9, 24), new DateValueImpl(2045, 12, 3), new DateValueImpl(2045, 12, 24),
			new DateValueImpl(2046, 9, 2), new DateValueImpl(2046, 12, 23), new DateValueImpl(2047, 9, 22), new DateValueImpl(2048, 8, 23), new DateValueImpl(2049, 1, 3), new DateValueImpl(2049, 1, 24), new DateValueImpl(2050, 1, 23),
			new DateValueImpl(2051, 9, 24), new DateValueImpl(2051, 12, 3), new DateValueImpl(2051, 12, 24), new DateValueImpl(2052, 9, 22), new DateValueImpl(2053, 8, 3), new DateValueImpl(2053, 8, 24), new DateValueImpl(2054, 8, 23),
			new DateValueImpl(2055, 1, 3), new DateValueImpl(2055, 1, 24), new DateValueImpl(2056, 1, 23), new DateValueImpl(2056, 9, 24), new DateValueImpl(2056, 12, 3), new DateValueImpl(2056, 12, 24), new DateValueImpl(2057, 9, 2),
			new DateValueImpl(2057, 12, 23), new DateValueImpl(2058, 9, 22), new DateValueImpl(2059, 8, 3), new DateValueImpl(2059, 8, 24), new DateValueImpl(2061, 1, 23), new DateValueImpl(2062, 9, 24), new DateValueImpl(2062, 12, 3),
			new DateValueImpl(2062, 12, 24), new DateValueImpl(2063, 9, 2), new DateValueImpl(2063, 12, 23), new DateValueImpl(2064, 8, 3), new DateValueImpl(2064, 8, 24), new DateValueImpl(2065, 8, 23), new DateValueImpl(2066, 1, 3),
			new DateValueImpl(2066, 1, 24), new DateValueImpl(2067, 1, 23), new DateValueImpl(2068, 9, 2), new DateValueImpl(2068, 12, 23), new DateValueImpl(2069, 9, 22), new DateValueImpl(2070, 8, 3), new DateValueImpl(2070, 8, 24),
			new DateValueImpl(2071, 8, 23), new DateValueImpl(2072, 1, 3), new DateValueImpl(2072, 1, 24), new DateValueImpl(2073, 9, 24), new DateValueImpl(2073, 12, 3), new DateValueImpl(2073, 12, 24), new DateValueImpl(2074, 9, 2),
			new DateValueImpl(2074, 12, 23), new DateValueImpl(2075, 9, 22), new DateValueImpl(2076, 8, 23), new DateValueImpl(2077, 1, 3), new DateValueImpl(2077, 1, 24), new DateValueImpl(2078, 1, 23), new DateValueImpl(2079, 9, 24),
			new DateValueImpl(2079, 12, 3), new DateValueImpl(2079, 12, 24), new DateValueImpl(2080, 9, 22), new DateValueImpl(2081, 8, 3), new DateValueImpl(2081, 8, 24), new DateValueImpl(2082, 8, 23), new DateValueImpl(2083, 1, 3),
			new DateValueImpl(2083, 1, 24), new DateValueImpl(2084, 1, 23), new DateValueImpl(2084, 9, 24), new DateValueImpl(2084, 12, 3), new DateValueImpl(2084, 12, 24), new DateValueImpl(2085, 9, 2), new DateValueImpl(2085, 12, 23),
			new DateValueImpl(2086, 9, 22), new DateValueImpl(2087, 8, 3), new DateValueImpl(2087, 8, 24), new DateValueImpl(2089, 1, 23), new DateValueImpl(2090, 9, 24), new DateValueImpl(2090, 12, 3), new DateValueImpl(2090, 12, 24),
			new DateValueImpl(2091, 9, 2), new DateValueImpl(2091, 12, 23), new DateValueImpl(2092, 8, 3), new DateValueImpl(2092, 8, 24), new DateValueImpl(2093, 8, 23), new DateValueImpl(2094, 1, 3), new DateValueImpl(2094, 1, 24),
			new DateValueImpl(2095, 1, 23), new DateValueImpl(2096, 9, 2), new DateValueImpl(2096, 12, 23), new DateValueImpl(2097, 9, 22), new DateValueImpl(2098, 8, 3), new DateValueImpl(2098, 8, 24), new DateValueImpl(2099, 8, 23),
			new DateValueImpl(2100, 1, 3), new DateValueImpl(2100, 1, 24), new DateValueImpl(2101, 1, 23), new DateValueImpl(2102, 9, 24), new DateValueImpl(2102, 12, 3), new DateValueImpl(2102, 12, 24), new DateValueImpl(2103, 9, 2),
			new DateValueImpl(2103, 12, 23), new DateValueImpl(2104, 8, 3), new DateValueImpl(2104, 8, 24), new DateValueImpl(2105, 8, 23), new DateValueImpl(2106, 1, 3), new DateValueImpl(2106, 1, 24), new DateValueImpl(2107, 1, 23),
			new DateValueImpl(2108, 9, 2), new DateValueImpl(2108, 12, 23), new DateValueImpl(2109, 9, 22), new DateValueImpl(2110, 8, 3), new DateValueImpl(2110, 8, 24), new DateValueImpl(2111, 8, 23), new DateValueImpl(2112, 1, 3),
			new DateValueImpl(2112, 1, 24), new DateValueImpl(2113, 9, 24), new DateValueImpl(2113, 12, 3), new DateValueImpl(2113, 12, 24), new DateValueImpl(2114, 9, 2), new DateValueImpl(2114, 12, 23), new DateValueImpl(2115, 9, 22),
			new DateValueImpl(2116, 8, 23), new DateValueImpl(2117, 1, 3), new DateValueImpl(2117, 1, 24), new DateValueImpl(2118, 1, 23), new DateValueImpl(2119, 9, 24), new DateValueImpl(2119, 12, 3), new DateValueImpl(2119, 12, 24),
			new DateValueImpl(2120, 9, 22), new DateValueImpl(2121, 8, 3), new DateValueImpl(2121, 8, 24), new DateValueImpl(2122, 8, 23)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void hourlyWithByday() {
		Recurrence recur = new Recurrence.Builder(Frequency.HOURLY)
			.interval(6)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
			.count(5)
		.build();
		DateValue start = new DateTimeValueImpl(2011, 8, 9, 12, 30, 0);
		DateValue[] expected = {
			new DateTimeValueImpl(2011, 8, 9, 12, 30, 0),
			new DateTimeValueImpl(2011, 8, 9, 18, 30, 0),
			new DateTimeValueImpl(2011, 8, 11, 0, 30, 0),
			new DateTimeValueImpl(2011, 8, 11, 6, 30, 0),
			new DateTimeValueImpl(2011, 8, 11, 12, 30, 0)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void hourlyWithBydayAcrossMonthBoundary() {
		Recurrence recur = new Recurrence.Builder(Frequency.HOURLY)
			.interval(6)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
			.count(5)
		.build();
		DateValue start = new DateTimeValueImpl(2011, 8, 31, 12, 30, 0);
		DateValue[] expected = {
			new DateTimeValueImpl(2011, 9, 1, 0, 30, 0),
			new DateTimeValueImpl(2011, 9, 1, 6, 30, 0),
			new DateTimeValueImpl(2011, 9, 1, 12, 30, 0),
			new DateTimeValueImpl(2011, 9, 1, 18, 30, 0),
			new DateTimeValueImpl(2011, 9, 6, 0, 30, 0)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void hourlyWithByMonthday() {
		Recurrence recur = new Recurrence.Builder(Frequency.HOURLY)
			.interval(6)
			.byMonthDay(9)
			.count(5)
		.build();
		DateValue start = new DateTimeValueImpl(2011, 8, 9, 12, 30, 0);
		DateValue[] expected = {
			new DateTimeValueImpl(2011, 8, 9, 12, 30, 0),
			new DateTimeValueImpl(2011, 8, 9, 18, 30, 0),
			new DateTimeValueImpl(2011, 9, 9, 0, 30, 0),
			new DateTimeValueImpl(2011, 9, 9, 6, 30, 0),
			new DateTimeValueImpl(2011, 9, 9, 12, 30, 0)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void weirdByMonth() {
		Recurrence recur = new Recurrence.Builder(Frequency.YEARLY)
			.interval(1)
			.byMonth(2, 7, 4, 9, 9, 6, 11, 1)
		.build();
		DateValue start = new DateValueImpl(1949, 3, 20);
		DateValue[] expected = {
				new DateValueImpl(1949, 4, 20),
				new DateValueImpl(1949, 6, 20),
				new DateValueImpl(1949, 7, 20),
				new DateValueImpl(1949, 9, 20),
				new DateValueImpl(1949, 11, 20),
				new DateValueImpl(1950, 1, 20),
				new DateValueImpl(1950, 2, 20),
				new DateValueImpl(1950, 4, 20),
				new DateValueImpl(1950, 6, 20),
				new DateValueImpl(1950, 7, 20),
				new DateValueImpl(1950, 9, 20),
				new DateValueImpl(1950, 11, 20)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void monkeyByMinute1() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY)
			.interval(1)
			.byMinute(19, 27, 38, 1, 5)
		.build();
		DateValue start = new DateTimeValueImpl(1936, 5, 8, 0, 9, 41);
		DateValue[] expected = {
			new DateTimeValueImpl(1936, 5, 8, 0, 19, 41),
			new DateTimeValueImpl(1936, 5, 8, 0, 27, 41),
			new DateTimeValueImpl(1936, 5, 8, 0, 38, 41)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void monkeyByMinute2() {
		Recurrence recur = new Recurrence.Builder(Frequency.MINUTELY)
			.workweekStarts(DayOfWeek.MONDAY)
			.interval(1)
			.byMonth(10, 12)
			.byMonthDay(9, 28, -5, -19)
			.byHour(13, 0, 13, 8)
			.bySecond(51, 26, 31)
		.build();
		DateValue start = new DateTimeValueImpl(1939, 1, 8, 10, 58, 27);
		
		//since it starts at month 10 instead of January, the minute 58 is irrelevant
		DateValue[] expected = {
			new DateTimeValueImpl(1939, 10, 9, 0, 0, 26),
			new DateTimeValueImpl(1939, 10, 9, 0, 0, 31),
			new DateTimeValueImpl(1939, 10, 9, 0, 0, 51),
			new DateTimeValueImpl(1939, 10, 9, 0, 1, 26),
			new DateTimeValueImpl(1939, 10, 9, 0, 1, 31),
			new DateTimeValueImpl(1939, 10, 9, 0, 1, 51),
			new DateTimeValueImpl(1939, 10, 9, 0, 2, 26),
			new DateTimeValueImpl(1939, 10, 9, 0, 2, 31),
			new DateTimeValueImpl(1939, 10, 9, 0, 2, 51)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void monkeyBySecondSetPos() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY)
			.count(9)
			.interval(1)
			.byDay(DayOfWeek.MONDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, DayOfWeek.FRIDAY)
			.bySecond(6, 48, 20)
			.bySetPos(8, 2, 5, 7, -8, 4)
		.build();
		DateValue start = new DateTimeValueImpl(1909, 4, 24, 7, 57, 54);
		DateValue[] expected = {
			new DateTimeValueImpl(1909, 4, 25, 7, 57, 6),
			new DateTimeValueImpl(1909, 4, 25, 7, 57, 48),
			new DateTimeValueImpl(1909, 4, 30, 7, 57, 6),
			new DateTimeValueImpl(1909, 4, 30, 7, 57, 20),
			new DateTimeValueImpl(1909, 4, 30, 7, 57, 48),
			new DateTimeValueImpl(1909, 5, 1, 7, 57, 6),
			new DateTimeValueImpl(1909, 5, 1, 7, 57, 48),
			new DateTimeValueImpl(1909, 5, 2, 7, 57, 6),
			new DateTimeValueImpl(1909, 5, 7, 7, 57, 6)
		};
	
		run(recur, start, expected);
	}

	@Test
	public void monkeyHourly() {
		Recurrence recur = new Recurrence.Builder(Frequency.HOURLY)
			.interval(1)
			.byMonthDay(12, 10, -4)
		.build();
		DateValue start = new DateTimeValueImpl(2051, 1, 20, 3, 10, 47);
		DateValue[] expected = {
			new DateTimeValueImpl(2051, 1, 28, 0, 10, 47), new DateTimeValueImpl(2051, 1, 28, 1, 10, 47), new DateTimeValueImpl(2051, 1, 28, 2, 10, 47), new DateTimeValueImpl(2051, 1, 28, 3, 10, 47),
			new DateTimeValueImpl(2051, 1, 28, 4, 10, 47), new DateTimeValueImpl(2051, 1, 28, 5, 10, 47), new DateTimeValueImpl(2051, 1, 28, 6, 10, 47), new DateTimeValueImpl(2051, 1, 28, 7, 10, 47),
			new DateTimeValueImpl(2051, 1, 28, 8, 10, 47), new DateTimeValueImpl(2051, 1, 28, 9, 10, 47), new DateTimeValueImpl(2051, 1, 28, 10, 10, 47), new DateTimeValueImpl(2051, 1, 28, 11, 10, 47),
			new DateTimeValueImpl(2051, 1, 28, 12, 10, 47), new DateTimeValueImpl(2051, 1, 28, 13, 10, 47), new DateTimeValueImpl(2051, 1, 28, 14, 10, 47), new DateTimeValueImpl(2051, 1, 28, 15, 10, 47),
			new DateTimeValueImpl(2051, 1, 28, 16, 10, 47), new DateTimeValueImpl(2051, 1, 28, 17, 10, 47), new DateTimeValueImpl(2051, 1, 28, 18, 10, 47), new DateTimeValueImpl(2051, 1, 28, 19, 10, 47),
			new DateTimeValueImpl(2051, 1, 28, 20, 10, 47), new DateTimeValueImpl(2051, 1, 28, 21, 10, 47), new DateTimeValueImpl(2051, 1, 28, 22, 10, 47), new DateTimeValueImpl(2051, 1, 28, 23, 10, 47),
			new DateTimeValueImpl(2051, 2, 10, 0, 10, 47), new DateTimeValueImpl(2051, 2, 10, 1, 10, 47), new DateTimeValueImpl(2051, 2, 10, 2, 10, 47), new DateTimeValueImpl(2051, 2, 10, 3, 10, 47),
			new DateTimeValueImpl(2051, 2, 10, 4, 10, 47), new DateTimeValueImpl(2051, 2, 10, 5, 10, 47), new DateTimeValueImpl(2051, 2, 10, 6, 10, 47), new DateTimeValueImpl(2051, 2, 10, 7, 10, 47),
			new DateTimeValueImpl(2051, 2, 10, 8, 10, 47), new DateTimeValueImpl(2051, 2, 10, 9, 10, 47), new DateTimeValueImpl(2051, 2, 10, 10, 10, 47), new DateTimeValueImpl(2051, 2, 10, 11, 10, 47),
			new DateTimeValueImpl(2051, 2, 10, 12, 10, 47), new DateTimeValueImpl(2051, 2, 10, 13, 10, 47), new DateTimeValueImpl(2051, 2, 10, 14, 10, 47), new DateTimeValueImpl(2051, 2, 10, 15, 10, 47),
			new DateTimeValueImpl(2051, 2, 10, 16, 10, 47), new DateTimeValueImpl(2051, 2, 10, 17, 10, 47), new DateTimeValueImpl(2051, 2, 10, 18, 10, 47), new DateTimeValueImpl(2051, 2, 10, 19, 10, 47),
			new DateTimeValueImpl(2051, 2, 10, 20, 10, 47), new DateTimeValueImpl(2051, 2, 10, 21, 10, 47), new DateTimeValueImpl(2051, 2, 10, 22, 10, 47), new DateTimeValueImpl(2051, 2, 10, 23, 10, 47),
			new DateTimeValueImpl(2051, 2, 12, 0, 10, 47), new DateTimeValueImpl(2051, 2, 12, 1, 10, 47), new DateTimeValueImpl(2051, 2, 12, 2, 10, 47), new DateTimeValueImpl(2051, 2, 12, 3, 10, 47),
			new DateTimeValueImpl(2051, 2, 12, 4, 10, 47), new DateTimeValueImpl(2051, 2, 12, 5, 10, 47), new DateTimeValueImpl(2051, 2, 12, 6, 10, 47), new DateTimeValueImpl(2051, 2, 12, 7, 10, 47),
			new DateTimeValueImpl(2051, 2, 12, 8, 10, 47), new DateTimeValueImpl(2051, 2, 12, 9, 10, 47), new DateTimeValueImpl(2051, 2, 12, 10, 10, 47), new DateTimeValueImpl(2051, 2, 12, 11, 10, 47),
			new DateTimeValueImpl(2051, 2, 12, 12, 10, 47), new DateTimeValueImpl(2051, 2, 12, 13, 10, 47), new DateTimeValueImpl(2051, 2, 12, 14, 10, 47), new DateTimeValueImpl(2051, 2, 12, 15, 10, 47),
			new DateTimeValueImpl(2051, 2, 12, 16, 10, 47), new DateTimeValueImpl(2051, 2, 12, 17, 10, 47), new DateTimeValueImpl(2051, 2, 12, 18, 10, 47), new DateTimeValueImpl(2051, 2, 12, 19, 10, 47),
			new DateTimeValueImpl(2051, 2, 12, 20, 10, 47), new DateTimeValueImpl(2051, 2, 12, 21, 10, 47), new DateTimeValueImpl(2051, 2, 12, 22, 10, 47), new DateTimeValueImpl(2051, 2, 12, 23, 10, 47),
			new DateTimeValueImpl(2051, 2, 25, 0, 10, 47), new DateTimeValueImpl(2051, 2, 25, 1, 10, 47), new DateTimeValueImpl(2051, 2, 25, 2, 10, 47), new DateTimeValueImpl(2051, 2, 25, 3, 10, 47),
			new DateTimeValueImpl(2051, 2, 25, 4, 10, 47), new DateTimeValueImpl(2051, 2, 25, 5, 10, 47), new DateTimeValueImpl(2051, 2, 25, 6, 10, 47), new DateTimeValueImpl(2051, 2, 25, 7, 10, 47),
			new DateTimeValueImpl(2051, 2, 25, 8, 10, 47), new DateTimeValueImpl(2051, 2, 25, 9, 10, 47), new DateTimeValueImpl(2051, 2, 25, 10, 10, 47), new DateTimeValueImpl(2051, 2, 25, 11, 10, 47),
			new DateTimeValueImpl(2051, 2, 25, 12, 10, 47), new DateTimeValueImpl(2051, 2, 25, 13, 10, 47), new DateTimeValueImpl(2051, 2, 25, 14, 10, 47), new DateTimeValueImpl(2051, 2, 25, 15, 10, 47),
			new DateTimeValueImpl(2051, 2, 25, 16, 10, 47), new DateTimeValueImpl(2051, 2, 25, 17, 10, 47), new DateTimeValueImpl(2051, 2, 25, 18, 10, 47), new DateTimeValueImpl(2051, 2, 25, 19, 10, 47),
			new DateTimeValueImpl(2051, 2, 25, 20, 10, 47), new DateTimeValueImpl(2051, 2, 25, 21, 10, 47), new DateTimeValueImpl(2051, 2, 25, 22, 10, 47), new DateTimeValueImpl(2051, 2, 25, 23, 10, 47),
			new DateTimeValueImpl(2051, 3, 10, 0, 10, 47), new DateTimeValueImpl(2051, 3, 10, 1, 10, 47), new DateTimeValueImpl(2051, 3, 10, 2, 10, 47), new DateTimeValueImpl(2051, 3, 10, 3, 10, 47),
			new DateTimeValueImpl(2051, 3, 10, 4, 10, 47), new DateTimeValueImpl(2051, 3, 10, 5, 10, 47), new DateTimeValueImpl(2051, 3, 10, 6, 10, 47), new DateTimeValueImpl(2051, 3, 10, 7, 10, 47),
			new DateTimeValueImpl(2051, 3, 10, 8, 10, 47), new DateTimeValueImpl(2051, 3, 10, 9, 10, 47), new DateTimeValueImpl(2051, 3, 10, 10, 10, 47), new DateTimeValueImpl(2051, 3, 10, 11, 10, 47),
			new DateTimeValueImpl(2051, 3, 10, 12, 10, 47), new DateTimeValueImpl(2051, 3, 10, 13, 10, 47), new DateTimeValueImpl(2051, 3, 10, 14, 10, 47), new DateTimeValueImpl(2051, 3, 10, 15, 10, 47),
			new DateTimeValueImpl(2051, 3, 10, 16, 10, 47), new DateTimeValueImpl(2051, 3, 10, 17, 10, 47), new DateTimeValueImpl(2051, 3, 10, 18, 10, 47), new DateTimeValueImpl(2051, 3, 10, 19, 10, 47),
			new DateTimeValueImpl(2051, 3, 10, 20, 10, 47), new DateTimeValueImpl(2051, 3, 10, 21, 10, 47), new DateTimeValueImpl(2051, 3, 10, 22, 10, 47), new DateTimeValueImpl(2051, 3, 10, 23, 10, 47),
			new DateTimeValueImpl(2051, 3, 12, 0, 10, 47), new DateTimeValueImpl(2051, 3, 12, 1, 10, 47), new DateTimeValueImpl(2051, 3, 12, 2, 10, 47), new DateTimeValueImpl(2051, 3, 12, 3, 10, 47),
			new DateTimeValueImpl(2051, 3, 12, 4, 10, 47), new DateTimeValueImpl(2051, 3, 12, 5, 10, 47), new DateTimeValueImpl(2051, 3, 12, 6, 10, 47), new DateTimeValueImpl(2051, 3, 12, 7, 10, 47),
			new DateTimeValueImpl(2051, 3, 12, 8, 10, 47), new DateTimeValueImpl(2051, 3, 12, 9, 10, 47), new DateTimeValueImpl(2051, 3, 12, 10, 10, 47), new DateTimeValueImpl(2051, 3, 12, 11, 10, 47),
			new DateTimeValueImpl(2051, 3, 12, 12, 10, 47), new DateTimeValueImpl(2051, 3, 12, 13, 10, 47), new DateTimeValueImpl(2051, 3, 12, 14, 10, 47), new DateTimeValueImpl(2051, 3, 12, 15, 10, 47),
			new DateTimeValueImpl(2051, 3, 12, 16, 10, 47), new DateTimeValueImpl(2051, 3, 12, 17, 10, 47), new DateTimeValueImpl(2051, 3, 12, 18, 10, 47), new DateTimeValueImpl(2051, 3, 12, 19, 10, 47),
			new DateTimeValueImpl(2051, 3, 12, 20, 10, 47), new DateTimeValueImpl(2051, 3, 12, 21, 10, 47), new DateTimeValueImpl(2051, 3, 12, 22, 10, 47), new DateTimeValueImpl(2051, 3, 12, 23, 10, 47)
		};
	
		run(recur, start, expected);
	}
	
	/**
	 * <p>
	 * Asserts the dates in a given recurrence.
	 * </p>
	 * <p>
	 * Also asserts that the same dates are generated when the iterator is
	 * advanced to the given start date.
	 * </p>
	 * <p>
	 * This method automatically determines if the the given recurrence is
	 * non-terminating. If it is non-terminating, then this method will stop
	 * iterating once it generates the same number of items that there are in the
	 * list of expected values.
	 * </p>
	 * @param recur the recurrence
	 * @param start the start date (in UTC)
	 * @param expected the expected dates in the recurrence
	 */
	private static void run(Recurrence recur, DateValue start, DateValue[] expected) {
		run(recur, start, UTC, null, expected);
	}
	
	/**
	 * <p>
	 * Asserts the dates in a given recurrence.
	 * </p>
	 * <p>
	 * Also asserts that the same dates are generated when the iterator is
	 * advanced to the given start date.
	 * </p>
	 * <p>
	 * This method automatically determines if the the given recurrence is
	 * non-terminating. If it is non-terminating, then this method will stop
	 * iterating once it generates the same number of items that there are in the
	 * list of expected values.
	 * </p>
	 * @param recur the recurrence
	 * @param start the start date
	 * @param tz the timezone of the start date
	 * @param expected the expected dates in the recurrence
	 */
	private static void run(Recurrence recur, DateValue start, TimeZone tz, DateValue[] expected) {
		run(recur, start, tz, null, expected);
	}
	
	/**
	 * <p>
	 * Asserts the dates in a given recurrence.
	 * </p>
	 * <p>
	 * This method automatically determines if the the given recurrence is
	 * non-terminating. If it is non-terminating, then this method will stop
	 * iterating once it generates the same number of items that there are in the
	 * list of expected values.
	 * </p>
	 * @param recur the recurrence
	 * @param start the start date
	 * @param advanceTo the date for the iterator to advance to
	 * @param expected the expected dates in the recurrence
	 */
	private static void run(Recurrence recur, DateValue start, DateValue advanceTo, DateValue[] expected) {
		run(recur, start, UTC, advanceTo, expected);
	}
	
	/**
	 * <p>
	 * Asserts the dates in a given recurrence.
	 * </p>
	 * <p>
	 * This method automatically determines if the the given recurrence is
	 * non-terminating. If it is non-terminating, then this method will stop
	 * iterating once it generates the same number of items that there are in the
	 * list of expected values.
	 * </p>
	 * @param recur the recurrence
	 * @param start the start date
	 * @param tz the timezone of the start date
	 * @param advanceTo the date for the iterator to advance to (in UTC)
	 * @param expected the expected dates in the recurrence
	 */
	private static void run(Recurrence recur, DateValue start, TimeZone tz, DateValue advanceTo, DateValue[] expected) {		
		RecurrenceIterator it = RecurrenceIteratorFactory.createRecurrenceIterator(recur, start, tz);
		if (advanceTo != null){
			it.advanceTo(advanceTo);
		}
		
		boolean iteratorIsTerminating = (recur.getUntil() != null || recur.getCount() != null);
		
		assertIterator(Arrays.asList(expected), it, iteratorIsTerminating);
		
		/*
		 * Advancing to the start date should yield the same result.
		 */
		if (advanceTo == null){
			it = RecurrenceIteratorFactory.createRecurrenceIterator(recur, start, tz);
			it.advanceTo(start);
			assertIterator(Arrays.asList(expected), it, iteratorIsTerminating);
		}
	}

	/**
	 * Generates a list of dates in between two given dates. Each date is one day
	 * apart.
	 * @param start the start date
	 * @param end the end date
	 * @return the list of dates (including the start and end dates)
	 */
	private static List<DateValue> dateRange(DateValue start, DateValue end) {
		DTBuilder b = new DTBuilder(start);
		List<DateValue> list = new ArrayList<DateValue>();
		while (true) {
			DateValue d = b.toDate();
			if (d.compareTo(end) > 0) { break; }
			list.add(d);
			b.day += 1;
		}
		return list;
	}

	// TODO(msamuel): test BYSETPOS with FREQ in (WEEKLY,MONTHLY,YEARLY) x
	// (setPos absolute, setPos relative, setPos mixed)

	// TODO(msamuel): test that until date properly compared to UTC dates.

	// TODO(msamuel): test that monotonically increasing over timezone boundaries

	// TODO(msamuel): test that advanceTo handles timezones properly
}
//@formatter:on
