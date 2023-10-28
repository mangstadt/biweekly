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

import static biweekly.util.TestUtils.date;

import org.junit.Before;
import org.junit.Test;

import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.util.TimeUtils;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class StressTest {
	@Before
	public void before() {
		//prime the VM
		for (int runs = 10; --runs >= 0;) {
			runOne();
		}
	}

	@Test
	public void speed() {
		long t0 = System.nanoTime();
		//cycle through 10 recurrence rules, advancing and pulling a few dates off each
		for (int runs = 5000; --runs >= 0;) {
			runOne();
		}
		long dt = System.nanoTime() - t0;
		System.out.println(getClass().getSimpleName() + " took " + (dt / 1e6) + " ms");
	}

	//@formatter:off
	private static final Recurrence[] RECURRENCE_RULES = {
		new Recurrence.Builder(Frequency.DAILY).build(),
		new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
			.build(),
		new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
			.count(10)
			.build(),
		new Recurrence.Builder(Frequency.WEEKLY)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
			.until(new ICalDate(date(2006, 8, 1), false))
			.build(),
		new Recurrence.Builder(Frequency.MONTHLY)
			.interval(2)
			.byDay(DayOfWeek.TUESDAY, DayOfWeek.THURSDAY)
			.until(new ICalDate(date(2006, 8, 1), false))
			.build(),
		new Recurrence.Builder(Frequency.MONTHLY)
			.byMonthDay(13)
			.byDay(DayOfWeek.FRIDAY)
			.until(new ICalDate(date(2006, 8, 1), false))
			.build(),
		new Recurrence.Builder(Frequency.YEARLY)
			.byMonth(6)
			.byMonthDay(15)
			.until(new ICalDate(date(2020, 6, 15), false))
			.build()
	};
	//@formatter:on

	private static final DateValue DT_START = new DateValueImpl(2006, 4, 3);
	private static final DateValue T0 = new DateValueImpl(2006, 8, 3);

	private static void runOne() {
		for (Recurrence rrule : RECURRENCE_RULES) {
			RecurrenceIterator iter = RecurrenceIteratorFactory.createRecurrenceIterator(rrule, DT_START, TimeUtils.utcTimezone());
			iter.advanceTo(T0);
			for (int k = 20; iter.hasNext() && --k >= 0;) {
				iter.next();
			}
		}
	}
}
