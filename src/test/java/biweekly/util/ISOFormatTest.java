package biweekly.util;

import static biweekly.util.TestUtils.buildTimezone;
import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.ClassRule;
import org.junit.Test;

/*
 Copyright (c) 2013, Michael Angstadt
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
public class ISOFormatTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	@Test
	public void format() {
		Date datetime;
		{
			Calendar cal = Calendar.getInstance();
			cal.clear();
			cal.set(Calendar.YEAR, 2006);
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			cal.set(Calendar.DAY_OF_MONTH, 2);
			cal.set(Calendar.HOUR_OF_DAY, 10);
			cal.set(Calendar.MINUTE, 20);
			cal.set(Calendar.SECOND, 30);
			datetime = cal.getTime();
		}

		assertEquals("20060102", ISOFormat.DATE_BASIC.format(datetime));
		assertEquals("2006-01-02", ISOFormat.DATE_EXTENDED.format(datetime));
		assertEquals("20060102T102030+0100", ISOFormat.DATE_TIME_BASIC.format(datetime));
		assertEquals("2006-01-02T10:20:30+01:00", ISOFormat.DATE_TIME_EXTENDED.format(datetime));
		assertEquals("20060102T102030", ISOFormat.DATE_TIME_BASIC_WITHOUT_TZ.format(datetime));
		assertEquals("2006-01-02T10:20:30", ISOFormat.DATE_TIME_EXTENDED_WITHOUT_TZ.format(datetime));
		assertEquals("20060102T092030Z", ISOFormat.UTC_TIME_BASIC.format(datetime));
		assertEquals("2006-01-02T09:20:30Z", ISOFormat.UTC_TIME_EXTENDED.format(datetime));
	}

	@Test
	public void format_timezone() {
		TimeZone timezone = buildTimezone(-2, 0);

		Date datetime;
		{
			Calendar cal = Calendar.getInstance();
			cal.clear();
			cal.set(Calendar.YEAR, 2006);
			cal.set(Calendar.MONTH, Calendar.JANUARY);
			cal.set(Calendar.DAY_OF_MONTH, 2);
			cal.set(Calendar.HOUR_OF_DAY, 10);
			cal.set(Calendar.MINUTE, 20);
			cal.set(Calendar.SECOND, 30);
			datetime = cal.getTime();
		}

		assertEquals("20060102T072030-0200", ISOFormat.DATE_TIME_BASIC.format(datetime, timezone));
	}

	@Test
	public void parse() {
		Date date;
		{
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.JULY);
			c.set(Calendar.DAY_OF_MONTH, 1);
			date = c.getTime();
		}

		Date datetime;
		{
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.JULY);
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.HOUR_OF_DAY, 8);
			c.set(Calendar.MINUTE, 1);
			c.set(Calendar.SECOND, 30);
			datetime = c.getTime();
		}

		//basic, date
		assertEquals(date, ISOFormat.parse("20120701"));

		//extended, date
		assertEquals(date, ISOFormat.parse("2012-07-01"));

		//basic, datetime, GMT
		assertEquals(datetime, ISOFormat.parse("20120701T070130Z"));

		//extended, datetime, GMT
		assertEquals(datetime, ISOFormat.parse("2012-07-01T07:01:30Z"));

		//basic, datetime, timezone
		assertEquals(datetime, ISOFormat.parse("20120701T100130+0300"));

		//extended, datetime, timezone
		assertEquals(datetime, ISOFormat.parse("2012-07-01T10:01:30+03:00"));

		//basic, datetime (should use local timezone)
		assertEquals(datetime, ISOFormat.parse("20120701T080130"));

		//extended, datetime (should use local timezone)
		assertEquals(datetime, ISOFormat.parse("2012-07-01T08:01:30"));
	}

	@Test
	public void parse_timezone() {
		TimeZone timezone = buildTimezone(-2, 0);

		Date expected;
		{
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
			c.clear();
			c.set(Calendar.YEAR, 2012);
			c.set(Calendar.MONTH, Calendar.JULY);
			c.set(Calendar.DAY_OF_MONTH, 1);
			c.set(Calendar.HOUR_OF_DAY, 8);
			c.set(Calendar.MINUTE, 1);
			c.set(Calendar.SECOND, 30);
			expected = c.getTime();
		}

		Date actual = ISOFormat.parse("20120701T060130", timezone);
		assertEquals(actual, expected);

		//timezone in date string takes presidence
		actual = ISOFormat.parse("20120701T080130Z", timezone);
		assertEquals(actual, expected);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		ISOFormat.parse("invalid");
	}
}
