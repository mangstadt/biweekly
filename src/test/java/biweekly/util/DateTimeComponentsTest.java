package biweekly.util;

import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

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

/**
 * @author Michael Angstadt
 */
public class DateTimeComponentsTest {
	@Test
	public void parse() {
		assertParse(new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), "20130722T150630");
		assertParse(new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), "2013-07-22T15:06:30");
		assertParse(new DateTimeComponents(2013, 7, 22, 15, 6, 30, true), "20130722T150630Z");
		assertParse(new DateTimeComponents(2013, 7, 22, 15, 6, 30, true), "2013-07-22T15:06:30Z");
		assertParse(new DateTimeComponents(2013, 7, 22), "20130722");
		assertParse(new DateTimeComponents(2013, 7, 22), "2013-07-22");
	}

	private void assertParse(DateTimeComponents expected, String input) {
		DateTimeComponents actual = DateTimeComponents.parse(input);
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		DateTimeComponents.parse("invalid");
	}

	@Test
	public void toString_() {
		assertToString("20130722T150630", "2013-07-22T15:06:30", new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), true);
		assertToString("20130722T150630Z", "2013-07-22T15:06:30Z", new DateTimeComponents(2013, 7, 22, 15, 6, 30, true), true);
		assertToString("20130722T000000", "2013-07-22T00:00:00", new DateTimeComponents(2013, 7, 22, 0, 0, 0, false), true);

		assertToString("20130722", "2013-07-22", new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), false);
		assertToString("20130722", "2013-07-22", new DateTimeComponents(2013, 7, 22, 15, 6, 30, true), false);
		assertToString("20130722", "2013-07-22", new DateTimeComponents(2013, 7, 22, 0, 0, 0, false), false);
	}

	private void assertToString(String expectedBasic, String expectedExtended, DateTimeComponents components, boolean includeTime) {
		assertEquals(expectedBasic, components.toString(includeTime, false));
		assertEquals(expectedExtended, components.toString(includeTime, true));
	}

	@Test
	public void toDate() throws Throwable {
		assertToDate(date("2013-07-22 15:06:30"), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false));
		assertToDate(utc("2013-07-22 15:06:30"), new DateTimeComponents(2013, 7, 22, 15, 6, 30, true));
	}

	private void assertToDate(Date expected, DateTimeComponents components) {
		Date actual = components.toDate();
		assertEquals(expected, actual);
	}

	@Test
	public void copy() {
		assertCopy(new DateTimeComponents(2014, 7, 22, 15, 6, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), 2014, null, null, null, null, null, null);
		assertCopy(new DateTimeComponents(2013, 8, 22, 15, 6, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, 8, null, null, null, null, null);
		assertCopy(new DateTimeComponents(2013, 7, 23, 15, 6, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, 23, null, null, null, null);
		assertCopy(new DateTimeComponents(2013, 7, 22, 16, 6, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, null, 16, null, null, null);
		assertCopy(new DateTimeComponents(2013, 7, 22, 15, 7, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, null, null, 7, null, null);
		assertCopy(new DateTimeComponents(2013, 7, 22, 15, 6, 31, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, null, null, null, 31, null);
		assertCopy(new DateTimeComponents(2013, 7, 22, 15, 6, 30, true), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, null, null, null, null, true);
	}

	private void assertCopy(DateTimeComponents expected, DateTimeComponents original, Integer year, Integer month, Integer date, Integer hour, Integer minute, Integer second, Boolean utc) {
		assertEquals(expected, new DateTimeComponents(original, year, month, date, hour, minute, second, utc));
	}
}
