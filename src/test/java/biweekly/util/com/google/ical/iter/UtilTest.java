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
 Copyright (c) 2013-2018, Michael Angstadt
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import biweekly.util.ByDay;
import biweekly.util.DayOfWeek;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class UtilTest {
	@Test
	public void dayNumToDateInMonth() {
		//        March 2006
		// Su Mo Tu We Th Fr Sa
		//           1  2  3  4
		//  5  6  7  8  9 10 11
		// 12 13 14 15 16 17 18
		// 19 20 21 22 23 24 25
		// 26 27 28 29 30 31
		DayOfWeek dow0 = DayOfWeek.WEDNESDAY;
		int nDays = 31;
		int d0 = 0;

		DayOfWeek dow = DayOfWeek.WEDNESDAY;
		assertEquals(1, Util.dayNumToDate(dow0, nDays, 1, dow, d0, nDays));
		assertEquals(8, Util.dayNumToDate(dow0, nDays, 2, dow, d0, nDays));
		assertEquals(29, Util.dayNumToDate(dow0, nDays, -1, dow, d0, nDays));
		assertEquals(22, Util.dayNumToDate(dow0, nDays, -2, dow, d0, nDays));

		dow = DayOfWeek.FRIDAY;
		assertEquals(3, Util.dayNumToDate(dow0, nDays, 1, dow, d0, nDays));
		assertEquals(10, Util.dayNumToDate(dow0, nDays, 2, dow, d0, nDays));
		assertEquals(31, Util.dayNumToDate(dow0, nDays, -1, dow, d0, nDays));
		assertEquals(24, Util.dayNumToDate(dow0, nDays, -2, dow, d0, nDays));

		dow = DayOfWeek.TUESDAY;
		assertEquals(7, Util.dayNumToDate(dow0, nDays, 1, dow, d0, nDays));
		assertEquals(14, Util.dayNumToDate(dow0, nDays, 2, dow, d0, nDays));
		assertEquals(28, Util.dayNumToDate(dow0, nDays, 4, dow, d0, nDays));
		assertEquals(0, Util.dayNumToDate(dow0, nDays, 5, dow, d0, nDays));
		assertEquals(28, Util.dayNumToDate(dow0, nDays, -1, dow, d0, nDays));
		assertEquals(21, Util.dayNumToDate(dow0, nDays, -2, dow, d0, nDays));
		assertEquals(7, Util.dayNumToDate(dow0, nDays, -4, dow, d0, nDays));
		assertEquals(0, Util.dayNumToDate(dow0, nDays, -5, dow, d0, nDays));
	}

	@Test
	public void dayNumToDateInYear() {
		//        January 2006
		//  # Su Mo Tu We Th Fr Sa
		//  1  1  2  3  4  5  6  7
		//  2  8  9 10 11 12 13 14
		//  3 15 16 17 18 19 20 21
		//  4 22 23 24 25 26 27 28
		//  5 29 30 31

		//      February 2006
		//  # Su Mo Tu We Th Fr Sa
		//  5           1  2  3  4
		//  6  5  6  7  8  9 10 11
		//  7 12 13 14 15 16 17 18
		//  8 19 20 21 22 23 24 25
		//  9 26 27 28

		//           March 2006
		//  # Su Mo Tu We Th Fr Sa
		//  9           1  2  3  4
		// 10  5  6  7  8  9 10 11
		// 11 12 13 14 15 16 17 18
		// 12 19 20 21 22 23 24 25
		// 13 26 27 28 29 30 31

		DayOfWeek dow0 = DayOfWeek.SUNDAY;
		int nInMonth = 31;
		int nDays = 365;
		int d0 = 59;

		// TODO(msamuel): check that these answers are right
		DayOfWeek dow = DayOfWeek.WEDNESDAY;
		assertEquals(1, Util.dayNumToDate(dow0, nDays, 9, dow, d0, nInMonth));
		assertEquals(8, Util.dayNumToDate(dow0, nDays, 10, dow, d0, nInMonth));
		assertEquals(29, Util.dayNumToDate(dow0, nDays, -40, dow, d0, nInMonth));
		assertEquals(22, Util.dayNumToDate(dow0, nDays, -41, dow, d0, nInMonth));

		dow = DayOfWeek.FRIDAY;
		assertEquals(3, Util.dayNumToDate(dow0, nDays, 9, dow, d0, nInMonth));
		assertEquals(10, Util.dayNumToDate(dow0, nDays, 10, dow, d0, nInMonth));
		assertEquals(31, Util.dayNumToDate(dow0, nDays, -40, dow, d0, nInMonth));
		assertEquals(24, Util.dayNumToDate(dow0, nDays, -41, dow, d0, nInMonth));

		dow = DayOfWeek.TUESDAY;
		assertEquals(7, Util.dayNumToDate(dow0, nDays, 10, dow, d0, nInMonth));
		assertEquals(14, Util.dayNumToDate(dow0, nDays, 11, dow, d0, nInMonth));
		assertEquals(28, Util.dayNumToDate(dow0, nDays, 13, dow, d0, nInMonth));
		assertEquals(0, Util.dayNumToDate(dow0, nDays, 14, dow, d0, nInMonth));
		assertEquals(28, Util.dayNumToDate(dow0, nDays, -40, dow, d0, nInMonth));
		assertEquals(21, Util.dayNumToDate(dow0, nDays, -41, dow, d0, nInMonth));
		assertEquals(7, Util.dayNumToDate(dow0, nDays, -43, dow, d0, nInMonth));
		assertEquals(0, Util.dayNumToDate(dow0, nDays, -44, dow, d0, nInMonth));
	}

	@Test
	public void uniquify() {
		int[] ints = new int[] { 1, 4, 4, 2, 7, 3, 8, 0, 0, 3 };
		int[] actual = Util.uniquify(ints);
		int[] expected = { 0, 1, 2, 3, 4, 7, 8 };
		assertTrue(Arrays.equals(expected, actual));
	}

	@Test
	public void nextWeekStart() {
		assertEquals(new DateValueImpl(2006, 1, 24), Util.nextWeekStart(new DateValueImpl(2006, 1, 23), DayOfWeek.TUESDAY));
		assertEquals(new DateValueImpl(2006, 1, 24), Util.nextWeekStart(new DateValueImpl(2006, 1, 24), DayOfWeek.TUESDAY));
		assertEquals(new DateValueImpl(2006, 1, 31), Util.nextWeekStart(new DateValueImpl(2006, 1, 25), DayOfWeek.TUESDAY));
		assertEquals(new DateValueImpl(2006, 1, 23), Util.nextWeekStart(new DateValueImpl(2006, 1, 23), DayOfWeek.MONDAY));
		assertEquals(new DateValueImpl(2006, 1, 30), Util.nextWeekStart(new DateValueImpl(2006, 1, 24), DayOfWeek.MONDAY));
		assertEquals(new DateValueImpl(2006, 1, 30), Util.nextWeekStart(new DateValueImpl(2006, 1, 25), DayOfWeek.MONDAY));
		assertEquals(new DateValueImpl(2006, 2, 6), Util.nextWeekStart(new DateValueImpl(2006, 1, 31), DayOfWeek.MONDAY));
	}

	@Test
	public void countInPeriod() {
		//        January 2006
		//  Su Mo Tu We Th Fr Sa
		//   1  2  3  4  5  6  7
		//   8  9 10 11 12 13 14
		//  15 16 17 18 19 20 21
		//  22 23 24 25 26 27 28
		//  29 30 31
		assertEquals(5, Util.countInPeriod(DayOfWeek.SUNDAY, DayOfWeek.SUNDAY, 31));
		assertEquals(5, Util.countInPeriod(DayOfWeek.MONDAY, DayOfWeek.SUNDAY, 31));
		assertEquals(5, Util.countInPeriod(DayOfWeek.TUESDAY, DayOfWeek.SUNDAY, 31));
		assertEquals(4, Util.countInPeriod(DayOfWeek.WEDNESDAY, DayOfWeek.SUNDAY, 31));
		assertEquals(4, Util.countInPeriod(DayOfWeek.THURSDAY, DayOfWeek.SUNDAY, 31));
		assertEquals(4, Util.countInPeriod(DayOfWeek.FRIDAY, DayOfWeek.SUNDAY, 31));
		assertEquals(4, Util.countInPeriod(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY, 31));

		//      February 2006
		//  Su Mo Tu We Th Fr Sa
		//            1  2  3  4
		//   5  6  7  8  9 10 11
		//  12 13 14 15 16 17 18
		//  19 20 21 22 23 24 25
		//  26 27 28
		assertEquals(4, Util.countInPeriod(DayOfWeek.SUNDAY, DayOfWeek.WEDNESDAY, 28));
		assertEquals(4, Util.countInPeriod(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, 28));
		assertEquals(4, Util.countInPeriod(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, 28));
		assertEquals(4, Util.countInPeriod(DayOfWeek.WEDNESDAY, DayOfWeek.WEDNESDAY, 28));
		assertEquals(4, Util.countInPeriod(DayOfWeek.THURSDAY, DayOfWeek.WEDNESDAY, 28));
		assertEquals(4, Util.countInPeriod(DayOfWeek.FRIDAY, DayOfWeek.WEDNESDAY, 28));
		assertEquals(4, Util.countInPeriod(DayOfWeek.SATURDAY, DayOfWeek.WEDNESDAY, 28));
	}

	@Test
	public void invertWeekdayNum() {
		//        January 2006
		//  # Su Mo Tu We Th Fr Sa
		//  1  1  2  3  4  5  6  7
		//  2  8  9 10 11 12 13 14
		//  3 15 16 17 18 19 20 21
		//  4 22 23 24 25 26 27 28
		//  5 29 30 31

		// the 1st falls on a sunday, so dow0 == SU
		assertEquals(5, Util.invertWeekdayNum(new ByDay(-1, DayOfWeek.SUNDAY), DayOfWeek.SUNDAY, 31));
		assertEquals(5, Util.invertWeekdayNum(new ByDay(-1, DayOfWeek.MONDAY), DayOfWeek.SUNDAY, 31));
		assertEquals(5, Util.invertWeekdayNum(new ByDay(-1, DayOfWeek.TUESDAY), DayOfWeek.SUNDAY, 31));
		assertEquals(4, Util.invertWeekdayNum(new ByDay(-1, DayOfWeek.WEDNESDAY), DayOfWeek.SUNDAY, 31));
		assertEquals(3, Util.invertWeekdayNum(new ByDay(-2, DayOfWeek.WEDNESDAY), DayOfWeek.SUNDAY, 31));

		//      February 2006
		//  # Su Mo Tu We Th Fr Sa
		//  1           1  2  3  4
		//  2  5  6  7  8  9 10 11
		//  3 12 13 14 15 16 17 18
		//  4 19 20 21 22 23 24 25
		//  5 26 27 28

		assertEquals(4, Util.invertWeekdayNum(new ByDay(-1, DayOfWeek.SUNDAY), DayOfWeek.WEDNESDAY, 28));
		assertEquals(4, Util.invertWeekdayNum(new ByDay(-1, DayOfWeek.MONDAY), DayOfWeek.WEDNESDAY, 28));
		assertEquals(4, Util.invertWeekdayNum(new ByDay(-1, DayOfWeek.TUESDAY), DayOfWeek.WEDNESDAY, 28));
		assertEquals(4, Util.invertWeekdayNum(new ByDay(-1, DayOfWeek.WEDNESDAY), DayOfWeek.WEDNESDAY, 28));
		assertEquals(3, Util.invertWeekdayNum(new ByDay(-2, DayOfWeek.WEDNESDAY), DayOfWeek.WEDNESDAY, 28));
	}
}
