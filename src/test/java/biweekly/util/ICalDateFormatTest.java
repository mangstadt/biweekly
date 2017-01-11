package biweekly.util;

import static biweekly.util.TestUtils.buildTimezone;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.ClassRule;
import org.junit.Test;

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

/**
 * @author Michael Angstadt
 */
public class ICalDateFormatTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	@Test
	public void format() {
		Date datetime = date("2006-01-02 10:20:30");

		assertEquals("20060102", ICalDateFormat.DATE_BASIC.format(datetime));
		assertEquals("2006-01-02", ICalDateFormat.DATE_EXTENDED.format(datetime));
		assertEquals("20060102T102030+0100", ICalDateFormat.DATE_TIME_BASIC.format(datetime));
		assertEquals("2006-01-02T10:20:30+01:00", ICalDateFormat.DATE_TIME_EXTENDED.format(datetime));
		assertEquals("20060102T102030", ICalDateFormat.DATE_TIME_BASIC_WITHOUT_TZ.format(datetime));
		assertEquals("2006-01-02T10:20:30", ICalDateFormat.DATE_TIME_EXTENDED_WITHOUT_TZ.format(datetime));
		assertEquals("20060102T092030Z", ICalDateFormat.UTC_TIME_BASIC.format(datetime));
		assertEquals("2006-01-02T09:20:30Z", ICalDateFormat.UTC_TIME_EXTENDED.format(datetime));
	}

	@Test
	public void format_timezone() {
		TimeZone timezone = buildTimezone(-2, 0);

		Date datetime = date("2006-01-02 10:20:30");

		assertEquals("20060102T072030-0200", ICalDateFormat.DATE_TIME_BASIC.format(datetime, timezone));
	}

	@Test
	public void parse() {
		Date date = date("2012-07-01");
		Date datetime = date("2012-07-01 08:01:30");

		//basic, date
		assertEquals(date, ICalDateFormat.parse("20120701"));

		//extended, date
		assertEquals(date, ICalDateFormat.parse("2012-07-01"));

		//basic, datetime, GMT
		assertEquals(datetime, ICalDateFormat.parse("20120701T070130Z"));

		//extended, datetime, GMT
		assertEquals(datetime, ICalDateFormat.parse("2012-07-01T07:01:30Z"));

		//basic, datetime, timezone
		assertEquals(datetime, ICalDateFormat.parse("20120701T100130+0300"));
		assertEquals(datetime, ICalDateFormat.parse("20120701T100130+03"));
		assertEquals(datetime, ICalDateFormat.parse("20120701T040130-0300"));

		//extended, datetime, timezone
		assertEquals(datetime, ICalDateFormat.parse("2012-07-01T10:01:30+03:00"));

		//basic, datetime (should use local timezone)
		assertEquals(datetime, ICalDateFormat.parse("20120701T080130"));

		//extended, datetime (should use local timezone)
		assertEquals(datetime, ICalDateFormat.parse("2012-07-01T08:01:30"));

		//with milliseconds
		Calendar c = Calendar.getInstance();
		c.setTime(datetime);
		c.set(Calendar.MILLISECOND, 100);
		assertEquals(c.getTime(), ICalDateFormat.parse("20120701T070130.1Z"));
		c.set(Calendar.MILLISECOND, 124);
		assertEquals(c.getTime(), ICalDateFormat.parse("20120701T070130.1239Z")); //round

		//ignore timezone if there is an offset in the string
		assertEquals(datetime, ICalDateFormat.parse("20120701T100130+0300", TimeZone.getTimeZone("Europe/Paris")));
	}

	@Test
	public void parse_timezone() {
		TimeZone timezone = buildTimezone(-2, 0);

		Date expected = utc("2012-07-01 08:01:30");

		Date actual = ICalDateFormat.parse("20120701T060130", timezone);
		assertEquals(actual, expected);

		//timezone in date string takes presidence
		actual = ICalDateFormat.parse("20120701T080130Z", timezone);
		assertEquals(actual, expected);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		ICalDateFormat.parse("invalid");
	}

	@Test
	public void dateHasTime() {
		assertFalse(ICalDateFormat.dateHasTime("20130601"));
		assertTrue(ICalDateFormat.dateHasTime("20130601T120000"));
	}

	@Test
	public void dateHasTimezone() {
		assertFalse(ICalDateFormat.dateHasTimezone("20130601T120000"));
		assertTrue(ICalDateFormat.dateHasTimezone("20130601T120000Z"));
		assertTrue(ICalDateFormat.dateHasTimezone("20130601T120000+0100"));
		assertTrue(ICalDateFormat.dateHasTimezone("20130601T120000-0100"));
		assertTrue(ICalDateFormat.dateHasTimezone("2013-06-01T12:00:00+01:00"));
		assertTrue(ICalDateFormat.dateHasTimezone("2013-06-01T12:00:00-01:00"));
	}

	@Test
	public void parseTimezoneId() {
		TimeZone tz = ICalDateFormat.parseTimeZoneId("America/New_York");
		assertEquals(tz.getID(), "America/New_York");

		tz = ICalDateFormat.parseTimeZoneId("GMT");
		assertEquals(tz.getID(), "GMT");

		tz = ICalDateFormat.parseTimeZoneId("Bogus/Timezone");
		assertNull(tz);
	}
}
