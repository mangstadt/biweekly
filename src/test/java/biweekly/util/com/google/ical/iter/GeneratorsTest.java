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
 Copyright (c) 2013-2017, Michael Angstadt
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import biweekly.util.ByDay;
import biweekly.util.DayOfWeek;
import biweekly.util.com.google.ical.iter.Generator.IteratorShortCircuitingException;
import biweekly.util.com.google.ical.util.DTBuilder;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class GeneratorsTest {
	//@formatter:off
	@Test
	public void byYearDayGenerator() throws Exception {
		{
			int[] yearDays = { 1, 5, -1, 100 };
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.byYearDayGenerator(yearDays, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue expected[] = {
				new DateValueImpl(2006, 1, 1),
				new DateValueImpl(2006, 1, 5),
			};
			
			run(generator, builder, expected);
		}
		
		{
			int[] yearDays = { 1, 5, -1, 100 };
			DateValue start = new DateValueImpl(2006, 1, 2);
			Generator generator = Generators.byYearDayGenerator(yearDays, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue expected[] = {
				new DateValueImpl(2006, 1, 1),
				new DateValueImpl(2006, 1, 5),
			};
			
			run(generator, builder, expected);
		}
		
		{
			int[] yearDays = { 100 };
			DateValue start = new DateValueImpl(2006, 1, 6);
			Generator generator = Generators.byYearDayGenerator(yearDays, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue expected[] = {
			};
			
			run(generator, builder, expected);
		}
		
		{
			int[] yearDays = {};
			DateValue start = new DateValueImpl(2006, 1, 6);
			Generator generator = Generators.byYearDayGenerator(yearDays, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue expected[] = {
			};
			
			run(generator, builder, expected);
		}
		
		{
			int[] yearDays = { 1, 5, -1, 100 };
			DateValue start = new DateValueImpl(2006, 2, 1);
			Generator generator = Generators.byYearDayGenerator(yearDays, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue expected[] = {
			};
			
			run(generator, builder, expected);
		}
		
		{
			int[] yearDays = { 1, 5, -1, 100 };
			DateValue start = new DateValueImpl(2006, 12, 1);
			Generator generator = Generators.byYearDayGenerator(yearDays, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue expected[] = {
				new DateValueImpl(2006, 12, 31)
			};
			
			run(generator, builder, expected);
		}
		
		{
			int[] yearDays = { 1, 5, -1, 100 };
			DateValue start = new DateValueImpl(2006, 4, 1);
			Generator generator = Generators.byYearDayGenerator(yearDays, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue expected[] = {
				new DateValueImpl(2006, 4, 10)
			};
			
			run(generator, builder, expected);
		}
	}
	
	@Test
	public void byWeekNoGenerator() throws Exception {
		{
			int[] weekNumbers = { 22 };
			DayOfWeek weekStart = DayOfWeek.SUNDAY;
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.byWeekNoGenerator(weekNumbers, weekStart, start);
			
			{
				DTBuilder builder = new DTBuilder(2006, 1, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 2, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 3, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 4, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 5, 1);
				DateValue[] expected = new DateValue[]{
					new DateValueImpl(2006, 5, 28),
					new DateValueImpl(2006, 5, 29),
					new DateValueImpl(2006, 5, 30),
					new DateValueImpl(2006, 5, 31)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 6, 1);
				DateValue[] expected = new DateValue[]{
					new DateValueImpl(2006, 6, 1),
					new DateValueImpl(2006, 6, 2),
					new DateValueImpl(2006, 6, 3)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 7, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
		}

		//weekstart of monday shifts each week forward by one
		{
			int[] weekNumbers = { 22 };
			DayOfWeek weekStart = DayOfWeek.MONDAY;
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.byWeekNoGenerator(weekNumbers, weekStart, start);
			
			{
				DTBuilder builder = new DTBuilder(2006, 1, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 2, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 3, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 4, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 5, 1);
				DateValue[] expected = new DateValue[]{
					new DateValueImpl(2006, 5, 29),
					new DateValueImpl(2006, 5, 30),
					new DateValueImpl(2006, 5, 31)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 6, 1);
				DateValue[] expected = new DateValue[]{
					new DateValueImpl(2006, 6, 1),
					new DateValueImpl(2006, 6, 2),
					new DateValueImpl(2006, 6, 3),
					new DateValueImpl(2006, 6, 4)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 7, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
		}

		//2004 with a week start of monday has no orphaned days.
		//2004-01-01 falls on Thursday
		{
			int[] weekNumbers = { 14 };
			DayOfWeek weekStart = DayOfWeek.MONDAY;
			DateValue start = new DateValueImpl(2004, 1, 1);
			Generator generator = Generators.byWeekNoGenerator(weekNumbers, weekStart, start);
			
			{
				DTBuilder builder = new DTBuilder(2004, 1, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2004, 2, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2004, 3, 1);
				DateValue[] expected = new DateValue[]{
					new DateValueImpl(2004, 3, 29),
					new DateValueImpl(2004, 3, 30),
					new DateValueImpl(2004, 3, 31)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2004, 4, 1);
				DateValue[] expected = new DateValue[]{
					new DateValueImpl(2004, 4, 1),
					new DateValueImpl(2004, 4, 2),
					new DateValueImpl(2004, 4, 3),
					new DateValueImpl(2004, 4, 4)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2004, 5, 1);
				DateValue[] expected = new DateValue[]{
				};
				run(generator, builder, expected);
				}
		}
	}

	@Test
	public void byDayGenerator() throws Exception {
		ByDay[] days = {
			new ByDay(DayOfWeek.SUNDAY), //every sunday
			new ByDay(1, DayOfWeek.MONDAY), //first monday
			new ByDay(5, DayOfWeek.MONDAY), //fifth monday
			new ByDay(-2, DayOfWeek.TUESDAY) //second to last tuesday
		};
		boolean weeksInYear = false;
		DateValue start = new DateValueImpl(2006, 1, 1);
		Generator generator = Generators.byDayGenerator(days, weeksInYear, start);
		
		{
			DTBuilder builder = new DTBuilder(2006, 1, 1);
			DateValue[] expected = new DateValue[] {
				new DateValueImpl(2006, 1, 1),
				new DateValueImpl(2006, 1, 2),
				new DateValueImpl(2006, 1, 8),
				new DateValueImpl(2006, 1, 15),
				new DateValueImpl(2006, 1, 22),
				new DateValueImpl(2006, 1, 24),
				new DateValueImpl(2006, 1, 29),
				new DateValueImpl(2006, 1, 30)
			};
			run(generator, builder, expected);
		}
		
		{
			DTBuilder builder = new DTBuilder(2006, 2, 1);
			DateValue[] expected = new DateValue[] {
				new DateValueImpl(2006, 2, 5),
				new DateValueImpl(2006, 2, 6),
				new DateValueImpl(2006, 2, 12),
				new DateValueImpl(2006, 2, 19),
				new DateValueImpl(2006, 2, 21),
				new DateValueImpl(2006, 2, 26)
			};
			run(generator, builder, expected);
		}
	}

	@Test
	public void byMonthDayGenerator() throws Exception {
		{
			int[] monthDays = { 1, 15, 29 };

			{
				DateValue start = new DateValueImpl(2006, 1, 1);
				Generator generator = Generators.byMonthDayGenerator(monthDays, start);
				DTBuilder builder = new DTBuilder(start);
				DateValue[] expected = new DateValue[] {
					new DateValueImpl(2006, 1, 1),
					new DateValueImpl(2006, 1, 15),
					new DateValueImpl(2006, 1, 29)
				};
				run(generator, builder, expected);
			}
			
			{
				DateValue start = new DateValueImpl(2006, 1, 15);
				Generator generator = Generators.byMonthDayGenerator(monthDays, start);
				DTBuilder builder = new DTBuilder(start);
				DateValue[] expected = new DateValue[] {
					new DateValueImpl(2006, 1, 1),
					new DateValueImpl(2006, 1, 15),
					new DateValueImpl(2006, 1, 29)
				};
				run(generator, builder, expected);
			}
			
			{
				DateValue start = new DateValueImpl(2006, 2, 1);
				Generator generator = Generators.byMonthDayGenerator(monthDays, start);
				DTBuilder builder = new DTBuilder(start);
				DateValue[] expected = new DateValue[] {
					new DateValueImpl(2006, 2, 1),
					new DateValueImpl(2006, 2, 15)
				};
				run(generator, builder, expected);
			}
			
			{
				DateValue start = new DateValueImpl(2006, 2, 16);
				Generator generator = Generators.byMonthDayGenerator(monthDays, start);
				DTBuilder builder = new DTBuilder(start);
				DateValue[] expected = new DateValue[] {
					new DateValueImpl(2006, 2, 1),
					new DateValueImpl(2006, 2, 15)
				};
				run(generator, builder, expected);
			}
		}

		
		{
			int[] monthDays = { 1, -30, 30 };
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.byMonthDayGenerator(monthDays, start);

			{
				DTBuilder builder = new DTBuilder(2006, 1, 1);
				DateValue[] expected = new DateValue[] {
					new DateValueImpl(2006, 1, 1),
					new DateValueImpl(2006, 1, 2),
					new DateValueImpl(2006, 1, 30)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 2, 1);
				DateValue[] expected = new DateValue[] {
					new DateValueImpl(2006, 2, 1)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 3, 1);
				DateValue[] expected = new DateValue[] {
					new DateValueImpl(2006, 3, 1),
					new DateValueImpl(2006, 3, 2),
					new DateValueImpl(2006, 3, 30)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 4, 1);
				DateValue[] expected = new DateValue[] {
					new DateValueImpl(2006, 4, 1),
					new DateValueImpl(2006, 4, 30)
				};
				run(generator, builder, expected);
			}
		}
	}

	@Test
	public void byMonthGenerator() throws Exception {
		{
			int[] months = { 2, 8, 6, 10 };
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.byMonthGenerator(months, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue[] expected = {
				new DateValueImpl(2006, 2, 1),
				new DateValueImpl(2006, 6, 1),
				new DateValueImpl(2006, 8, 1),
				new DateValueImpl(2006, 10, 1),
			};
			run(generator, builder, expected);
		}
		
		{
			int[] months = { 2, 8, 6, 10 };
			DateValue start = new DateValueImpl(2006, 4, 1);
			Generator generator = Generators.byMonthGenerator(months, start);
			
			{
				DTBuilder builder = new DTBuilder(2006, 4, 1);
				DateValue[] expected = new DateValue[]{
					new DateValueImpl(2006, 2, 1),
					new DateValueImpl(2006, 6, 1),
					new DateValueImpl(2006, 8, 1),
					new DateValueImpl(2006, 10, 1),
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2007, 11, 1);
				DateValue[] expected = new DateValue[]{
					new DateValueImpl(2007, 2, 1),
					new DateValueImpl(2007, 6, 1),
					new DateValueImpl(2007, 8, 1),
					new DateValueImpl(2007, 10, 1),
				};
				run(generator, builder, expected);
			}
		}
	}

	@Test
	public void byYearGenerator() throws Exception {
		{
			int years[] = { 1066, 1492, 1876, 1975, 2006 };
			
			{
				DateValue start = new DateValueImpl(2006, 1, 1);
				Generator generator = Generators.byYearGenerator(years, start);
				DTBuilder builder = new DTBuilder(start);
				DateValue[] expected = {
						new DateValueImpl(2006, 1, 1)
				};
				run(generator, builder, expected);
			}
			
			{
				DateValue start = new DateValueImpl(2007, 1, 1);
				Generator generator = Generators.byYearGenerator(years, start);
				DTBuilder builder = new DTBuilder(start);
				DateValue[] expected = {
				};
				run(generator, builder, expected);
			}
		}
		
		{
			int years[] = { 1066, 1492, 1876, 1975, 2006 };
			
			{
				DateValue start = new DateValueImpl(1066, 7, 1);
				Generator generator = Generators.byYearGenerator(years, start);
				DTBuilder builder = new DTBuilder(start);
				DateValue[] expected = {
						new DateValueImpl(1066, 7, 1),
						new DateValueImpl(1492, 7, 1),
						new DateValueImpl(1876, 7, 1),
						new DateValueImpl(1975, 7, 1),
						new DateValueImpl(2006, 7, 1),
				};
				run(generator, builder, expected);
			}
			
			{
				DateValue start = new DateValueImpl(1900, 7, 1);
				Generator generator = Generators.byYearGenerator(years, start);
				DTBuilder builder = new DTBuilder(start);
				DateValue[] expected = {
						new DateValueImpl(1975, 7, 1),
						new DateValueImpl(2006, 7, 1),
				};
				run(generator, builder, expected);
			}
		}
	}

	@Test
	public void serialDayGenerator() throws Exception {
		{
			int interval = 1;
			DateValue start = new DateValueImpl(2006, 1, 15);
			Generator generator = Generators.serialDayGenerator(interval, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue[] expected = allDaysInMonth(new DateValueImpl(2006, 1, 15));
			run(generator, builder, expected);
		}
		
		{
			int interval = 1;
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.serialDayGenerator(interval, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue[] expected = allDaysInMonth(new DateValueImpl(2006, 1, 1));
			run(generator, builder, expected);
		}
		
		{
			int interval = 2;
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.serialDayGenerator(interval, start);
			
			{
				DTBuilder builder = new DTBuilder(start);
				DateValue[] expected = everyOtherDayInMonth(new DateValueImpl(2006, 1, 1));
				run(generator, builder, expected);
			}
			
			//now generator should start on the second of February
			{
				DTBuilder builder = new DTBuilder(2006, 2, 1);
				DateValue[] expected = everyOtherDayInMonth(new DateValueImpl(2006, 2, 2));
				run(generator, builder, expected);
			}
			

			//and if we skip way ahead to June, it should start on the 1st
			{
				DTBuilder builder = new DTBuilder(2006, 4, 1);
				DateValue[] expected = everyOtherDayInMonth(new DateValueImpl(2006, 4, 1));
				run(generator, builder, expected);
			}
		}
		
		//test with intervals longer than 30 days
		{
			int interval = 45;
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.serialDayGenerator(interval, start);
			
			{
				DTBuilder builder = new DTBuilder(2006, 1, 1);
				DateValue[] expected = {
						new DateValueImpl(2006, 1, 1)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 2, 1);
				DateValue[] expected = {
						new DateValueImpl(2006, 2, 15)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 3, 1);
				DateValue[] expected = {
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 4, 1);
				DateValue[] expected = {
						new DateValueImpl(2006, 4, 1)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2006, 5, 1);
				DateValue[] expected = {
						new DateValueImpl(2006, 5, 16)
				};
				run(generator, builder, expected);
			}
		}
	}

	@Test
	public void serialMonthGenerator() throws Exception {
		{
			int interval = 1;
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.serialMonthGenerator(interval, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue[] expected = allMonthsInYear(start);
			run(generator, builder, expected);
		}
		
		{
			int interval = 1;
			DateValue start = new DateValueImpl(2006, 4, 1);
			Generator generator = Generators.serialMonthGenerator(interval, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue[] expected = allMonthsInYear(start);
			run(generator, builder, expected);
		}
		
		{
			int interval = 2;
			DateValue start = new DateValueImpl(2006, 4, 1);
			Generator generator = Generators.serialMonthGenerator(interval, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue[] expected = everyOtherMonthInYear(start);
			run(generator, builder, expected);
		}
		
		{
			int interval = 7;
			DateValue start = new DateValueImpl(2006, 4, 1);
			Generator generator = Generators.serialMonthGenerator(interval, start);
			
			{
				DTBuilder builder = new DTBuilder(2006, 4, 1);
				DateValue[] expected = {
						new DateValueImpl(2006, 4, 1),
						new DateValueImpl(2006, 11, 1),
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2007, 11, 1);
				DateValue[] expected = {
						new DateValueImpl(2007, 6, 1)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2008, 4, 1);
				DateValue[] expected = {
						new DateValueImpl(2008, 1, 1),
						new DateValueImpl(2008, 8, 1)
				};
				run(generator, builder, expected);
			}
		}
		
		{
			int interval = 18;
			DateValue start = new DateValueImpl(2006, 4, 1);
			Generator generator = Generators.serialMonthGenerator(interval, start);
			
			{
				DTBuilder builder = new DTBuilder(2006, 4, 1);
				DateValue[] expected = {
						new DateValueImpl(2006, 4, 1)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2007, 11, 1);
				DateValue[] expected = {
						new DateValueImpl(2007, 10, 1)
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2008, 6, 1);
				DateValue[] expected = {
				};
				run(generator, builder, expected);
			}
			
			{
				DTBuilder builder = new DTBuilder(2009, 6, 1);
				DateValue[] expected = {
					new DateValueImpl(2009, 4, 1)
				};
				run(generator, builder, expected);
			}
		}
	}

	@Test
	public void serialYearGenerator() throws Exception {
		{
			int interval = 1;
			DateValue start = new DateValueImpl(2006, 4, 1);
			Generator generator = Generators.serialYearGenerator(interval, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue[] expected = {
				new DateValueImpl(2006, 4, 1),
				new DateValueImpl(2007, 4, 1),
				new DateValueImpl(2008, 4, 1),
				new DateValueImpl(2009, 4, 1),
				new DateValueImpl(2010, 4, 1),
			};
			run(generator, builder, expected, false);
		}
		
		{
			int interval = 2;
			DateValue start = new DateValueImpl(2006, 1, 1);
			Generator generator = Generators.serialYearGenerator(interval, start);
			DTBuilder builder = new DTBuilder(start);
			DateValue[] expected = {
				new DateValueImpl(2006, 1, 1),
				new DateValueImpl(2008, 1, 1),
				new DateValueImpl(2010, 1, 1),
				new DateValueImpl(2012, 1, 1),
				new DateValueImpl(2014, 1, 1)
			};
			run(generator, builder, expected, false);
		}
	}
	//@formatter:on

	@Test
	public void serialHourGeneratorGivenDate() throws Exception {
		int interval = 7;
		DateValue start = new DateValueImpl(2011, 8, 8);
		Generator generator = Generators.serialHourGenerator(interval, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 0, 0, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 7, 0, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 14, 0, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 21, 0, 0), builder.toDateTime());

		assertFalse(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 21, 0, 0), builder.toDateTime());

		++builder.day;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 4, 0, 0), builder.toDateTime());

		builder.day += 2;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 11, 5, 0, 0), builder.toDateTime());
	}

	@Test
	public void serialHourGeneratorGivenTime() throws Exception {
		int interval = 7;
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 1, 25, 30);
		Generator generator = Generators.serialHourGenerator(interval, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 1, 25, 30), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 8, 25, 30), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 15, 25, 30), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 22, 25, 30), builder.toDateTime());

		assertFalse(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 22, 25, 30), builder.toDateTime());

		++builder.day;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 5, 25, 30), builder.toDateTime());

		builder.day += 2;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 11, 6, 25, 30), builder.toDateTime());
	}

	@Test
	public void serialHourGeneratorRolledBack() throws Exception {
		int interval = 7;
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 1, 25, 30);
		Generator generator = Generators.serialHourGenerator(interval, start);
		DTBuilder builder = new DTBuilder(new DateTimeValueImpl(2011, 8, 1, 0, 29, 50));

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 1, 1, 29, 50), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 1, 8, 29, 50), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 1, 15, 29, 50), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 1, 22, 29, 50), builder.toDateTime());

		assertFalse(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 1, 22, 29, 50), builder.toDateTime());

		++builder.day;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 2, 5, 29, 50), builder.toDateTime());

		builder.day += 2;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 4, 6, 29, 50), builder.toDateTime());
	}

	@Test
	public void byHourGeneratorGivenDate() throws Exception {
		int[] hours = { 3, 9, 11 };
		DateValue start = new DateValueImpl(2011, 8, 8);
		Generator generator = Generators.byHourGenerator(hours, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 3, 0, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 9, 0, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 11, 0, 0), builder.toDateTime());

		assertFalse(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 11, 0, 0), builder.toDateTime());

		++builder.day;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 3, 0, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 9, 0, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 11, 0, 0), builder.toDateTime());

		++builder.month;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 9, 9, 3, 0, 0), builder.toDateTime());
	}

	@Test
	public void byHourGeneratorGivenDateTime() throws Exception {
		int[] hours = { 3, 9, 11 };
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 3, 11, 12);
		Generator generator = Generators.byHourGenerator(hours, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 3, 11, 12), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 9, 11, 12), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 11, 11, 12), builder.toDateTime());

		assertFalse(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 11, 11, 12), builder.toDateTime());

		++builder.day;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 3, 11, 12), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 9, 11, 12), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 11, 11, 12), builder.toDateTime());

		++builder.month;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 9, 9, 3, 11, 12), builder.toDateTime());
	}

	@Test
	public void singleByHourGeneratorGivenDateTime() throws Exception {
		int[] hours = { 7 };
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 3, 11, 12);
		Generator generator = Generators.byHourGenerator(hours, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 7, 11, 12), builder.toDateTime());

		++builder.day;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 7, 11, 12), builder.toDateTime());

		++builder.month;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 9, 9, 7, 11, 12), builder.toDateTime());
	}

	@Test
	public void serialMinuteGeneratorBigInterval() throws Exception {
		int interval = 100;
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 15, 30, 0);
		Generator generator = Generators.serialMinuteGenerator(interval, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 15, 30, 0), builder.toDateTime());

		assertFalse(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 15, 30, 0), builder.toDateTime());

		++builder.hour;
		assertFalse(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 16, 30, 0), builder.toDateTime());

		++builder.hour;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 17, 10, 0), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.hour;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 18, 50, 0), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.hour;
		assertFalse(generator.generate(builder));
		++builder.hour;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 20, 30, 0), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.hour;
		assertFalse(generator.generate(builder));
		++builder.hour;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 22, 10, 0), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.hour;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 23, 50, 0), builder.toDateTime());

		assertFalse(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 23, 50, 0), builder.toDateTime());

		++builder.day;
		builder.hour = 0;
		assertFalse(generator.generate(builder));
		++builder.hour;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 1, 30, 0), builder.toDateTime());

		builder.day += 2;
		builder.hour = 6;
		//Skipping over 3:10, 4:50, 6:30, 8:10, 9:50, 11:30, 13:10, 14:50, 16:30, 18:10, 19:50, 21:30, 23:10,
		//on the 9th, and 0:50, 2:30, 4:10, 5:50, 7:30, 9:10, 10:50, 12:30, 14:10, 15:50, 17:30, 19:10, 20:50, 22:30,
		//on the 10th, and 00:10, 1:50, 3:30, 5:10 on the 11th, to 6:50.
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 11, 6, 50, 0), builder.toDateTime());
	}

	@Test
	public void serialMinuteGeneratorSmallInterval() throws Exception {
		int interval = 15;
		DateValue start = new DateValueImpl(2011, 8, 8);
		Generator generator = Generators.serialMinuteGenerator(interval, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 0, 0, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 0, 15, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 0, 30, 0), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 0, 45, 0), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.hour;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 1, 0, 0), builder.toDateTime());
	}

	@Test
	public void byMinuteGenerator() throws Exception {
		int[] minutes = { 3, 57, 20, 3 };
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 5, 0, 17);
		Generator generator = Generators.byMinuteGenerator(minutes, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 5, 3, 17), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 5, 20, 17), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 5, 57, 17), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.hour;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 6, 3, 17), builder.toDateTime());
	}

	@Test
	public void singleByMinuteGenerator() throws Exception {
		int minutes[] = {};
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 5, 30, 17);
		Generator generator = Generators.byMinuteGenerator(minutes, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 5, 30, 17), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.hour;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 6, 30, 17), builder.toDateTime());

		assertFalse(generator.generate(builder));
		builder.day += 1;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 9, 6, 30, 17), builder.toDateTime());
		assertFalse(generator.generate(builder));
	}

	@Test
	public void serialSecondGenerator() throws Exception {
		int interval = 25;
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 19, 1, 23);
		Generator generator = Generators.serialSecondGenerator(interval, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 1, 23), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 1, 48), builder.toDateTime());

		assertFalse(generator.generate(builder));
		builder.minute += 1;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 2, 13), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 2, 38), builder.toDateTime());

		assertFalse(generator.generate(builder));
		builder.minute += 1;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 3, 3), builder.toDateTime());

		builder.minute += 1;
		//skipped 2:28, 2:53
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 4, 18), builder.toDateTime());
	}

	@Test
	public void bySecondGenerator() throws Exception {
		int seconds[] = { 25, 48, 2 };
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 19, 1, 23);
		Generator generator = Generators.bySecondGenerator(seconds, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 1, 25), builder.toDateTime());

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 1, 48), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.minute;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 2, 2), builder.toDateTime());
	}

	@Test
	public void singleBySecondGenerator() throws Exception {
		int seconds[] = {};
		DateValue start = new DateTimeValueImpl(2011, 8, 8, 19, 1, 23);
		Generator generator = Generators.bySecondGenerator(seconds, start);
		DTBuilder builder = new DTBuilder(start);

		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 1, 23), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.minute;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 2, 23), builder.toDateTime());

		assertFalse(generator.generate(builder));
		++builder.minute;
		assertTrue(generator.generate(builder));
		assertEquals(new DateTimeValueImpl(2011, 8, 8, 19, 3, 23), builder.toDateTime());
	}

	/**
	 * Asserts the date values that a generator will produce. This method
	 * assumes that the given generator will eventually terminate.
	 * @param generator the generator
	 * @param builder the date builder to pass into the generator
	 * @param expected the expected date values
	 * @throws IteratorShortCircuitingException
	 */
	private static void run(Generator generator, DTBuilder builder, DateValue[] expected) throws IteratorShortCircuitingException {
		run(generator, builder, expected, true);
	}

	/**
	 * Asserts the date values that a generator will produce.
	 * @param generator the generator
	 * @param builder the date builder to pass into the generator
	 * @param expected the expected date values
	 * @param terminating true if the generator will eventually terminate, false
	 * to limit the number of generated dates to however long the expected date
	 * list is
	 * @throws IteratorShortCircuitingException
	 */
	private static void run(Generator generator, DTBuilder builder, DateValue[] expected, boolean terminating) throws IteratorShortCircuitingException {
		List<DateValue> actual = new ArrayList<DateValue>();
		while (generator.generate(builder) && (terminating || actual.size() < expected.length)) {
			actual.add(builder.toDate());
		}
		assertEquals(Arrays.asList(expected), actual);
	}

	/**
	 * Generates date values for all the days in a month.
	 * @param start the day to start on
	 * @return the days
	 */
	private static DateValue[] allDaysInMonth(DateValue start) {
		return daysInMonth(start, 1);
	}

	/**
	 * Generates date values for all the days in a month.
	 * @param start the day to start on
	 * @return the days
	 */
	private static DateValue[] everyOtherDayInMonth(DateValue start) {
		return daysInMonth(start, 2);
	}

	/**
	 * Generates date values for all the days in a month.
	 * @param start the day to start on
	 * @param interval the interval (e.g. "2" for every other day)
	 * @return the date values
	 */
	private static DateValue[] daysInMonth(DateValue start, int interval) {
		List<DateValue> dates = new ArrayList<DateValue>();
		DTBuilder builder = new DTBuilder(start);
		DateValue d = builder.toDate();
		while (d.month() == start.month()) {
			dates.add(d);
			builder.day += interval;
			d = builder.toDate();
		}

		return dates.toArray(new DateValue[0]);
	}

	/**
	 * Generates date values for all the months in a year.
	 * @param start the day to start on
	 * @return the days
	 */
	private static DateValue[] allMonthsInYear(DateValue start) {
		return monthsInYear(start, 1);
	}

	/**
	 * Generates date values for all the months in a year.
	 * @param start the day to start on
	 * @return the days
	 */
	private static DateValue[] everyOtherMonthInYear(DateValue start) {
		return monthsInYear(start, 2);
	}

	/**
	 * Generates date values for all the months in a year.
	 * @param start the day to start on
	 * @param interval the interval (e.g. "2" for every other month)
	 * @return the date values
	 */
	private static DateValue[] monthsInYear(DateValue start, int interval) {
		List<DateValue> dates = new ArrayList<DateValue>();
		DTBuilder builder = new DTBuilder(start);
		DateValue d = builder.toDate();
		while (d.year() == start.year()) {
			dates.add(d);
			builder.month += interval;
			d = builder.toDate();
		}

		return dates.toArray(new DateValue[0]);
	}
}
