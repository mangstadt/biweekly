package biweekly.util;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Date;

import org.junit.Test;

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

/**
 * @author Michael Angstadt
 */
public class ICalDateTest {
	@Test
	public void equals_() {
		ICalDate date1 = new ICalDate(date("2014-10-01 12:00:00"), true);
		ICalDate date2 = new ICalDate(date("2014-10-01 12:00:00"), true);
		assertEquals(date1, date2);

		date1 = new ICalDate(date("2014-10-01 12:00:00"), false);
		date2 = new ICalDate(date("2014-10-01 12:00:00"), false);
		assertEquals(date1, date2);
	}

	@Test
	public void equals_hasTime() {
		ICalDate date1 = new ICalDate(date("2014-10-01 12:00:00"), true);
		ICalDate date2 = new ICalDate(date("2014-10-01 12:00:00"), false);
		assertNotEquals(date1, date2);
	}

	@Test
	public void equals_ignore_components() {
		ICalDate date1 = new ICalDate(date("2014-10-01 12:00:00"), new DateTimeComponents(2014, 10, 1, 12, 0, 0, false), true);
		ICalDate date2 = new ICalDate(date("2014-10-01 12:00:00"), new DateTimeComponents(1990, 10, 1, 12, 0, 0, false), true);
		assertEquals(date1, date2);
	}

	@Test
	public void equals_java_date() {
		ICalDate date1 = new ICalDate(date("2014-10-01 12:00:00"), true);
		Date date2 = date("2014-10-01 12:00:00");
		assertEquals(date1, date2);
		assertEquals(date2, date1);

		date1 = new ICalDate(date("2014-10-01 12:00:00"), false);
		date2 = date("2014-10-01 00:00:00");
		assertEquals(date1, date2);
		assertEquals(date2, date1);
	}

	@Test
	public void equals_other_object() {
		ICalDate date1 = new ICalDate(date("2014-10-01 12:00:00"), true);
		String date2 = "string";
		assertNotEquals(date1, date2);
	}

	@Test
	public void truncate_time_for_dates() {
		ICalDate date = new ICalDate(date("2014-10-01 12:00:00"), false);
		assertEquals(date("2014-10-01 00:00:00"), date);
	}
}
