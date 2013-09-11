package biweekly.util;

import static biweekly.util.TestUtils.buildTimezone;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
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
public class ICalDateFormatterTest {
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

		//@formatter:off
		Object[][] tests = new Object[][]{
			new Object[]{"20060102", ISOFormat.DATE_BASIC},
			new Object[]{"2006-01-02", ISOFormat.DATE_EXTENDED},
			new Object[]{"20060102T102030+0100", ISOFormat.TIME_BASIC},
			new Object[]{"2006-01-02T10:20:30+01:00", ISOFormat.TIME_EXTENDED},
			new Object[]{"20060102T102030", ISOFormat.TIME_BASIC_WITHOUT_TZ},
			new Object[]{"2006-01-02T10:20:30", ISOFormat.TIME_EXTENDED_WITHOUT_TZ},
			new Object[]{"2006-01-02T10:20:30+0100", ISOFormat.HCARD_TIME_TAG},
			new Object[]{"20060102T092030Z", ISOFormat.UTC_TIME_BASIC},
			new Object[]{"2006-01-02T09:20:30Z", ISOFormat.UTC_TIME_EXTENDED},
		};
		//@formatter:off
		
		for (Object[] test : tests){
			String expected = (String)test[0];
			ISOFormat format = (ISOFormat)test[1];
			
			String actual = ICalDateFormatter.format(datetime, format);
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void format_timezone(){
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
		
		String actual = ICalDateFormatter.format(datetime, ISOFormat.TIME_BASIC, timezone);
		assertEquals("20060102T072030-0200", actual);
	}

	@Test
	public void formatTimeZone() {
		//@formatter:off
		Object[][] tests = new Object[][]{
			//positive
			new Object[]{"+0200", "+02:00", buildTimezone(2, 0)},
			
			//negative
			new Object[]{"-0500", "-05:00", buildTimezone(-5, 0)},
			
			//with minutes
			new Object[]{"-0530", "-05:30", buildTimezone(-5, 30)},
			
			//hour >= 10
			new Object[]{"+1000", "+10:00", buildTimezone(10, 0)},
			
			//zero hour
			new Object[]{"+0000", "+00:00", buildTimezone(0, 0)},
		};
		//@formatter:off
		
		for (Object[] test : tests){
			String expectedBasic = (String)test[0];
			String expectedExtended = (String)test[1];
			TimeZone timezone = (TimeZone)test[2];
			
			String actualBasic = ICalDateFormatter.formatTimeZone(timezone, false);
			assertEquals(expectedBasic, actualBasic);
			
			String actualExtended = ICalDateFormatter.formatTimeZone(timezone, true);
			assertEquals(expectedExtended, actualExtended);
		}
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

		//@formatter:off
		Object[][] tests = new Object[][]{
			//basic, date
			new Object[]{"20120701", date},
			
			//extended, date
			new Object[]{"2012-07-01", date},
			
			//basic, datetime, GMT
			new Object[]{"20120701T070130Z", datetime},
			
			//extended, datetime, GMT
			new Object[]{"2012-07-01T07:01:30Z", datetime},
			
			//basic, datetime, timezone
			new Object[]{"2012-07-01T10:01:30+0300", datetime},
			
			//extended, datetime, timezone
			new Object[]{"2012-07-01T10:01:30+03:00", datetime},
			
			//basic, datetime (should use local timezone)
			new Object[]{"20120701T080130", datetime},
			
			//extended, datetime (should use local timezone)
			new Object[]{"2012-07-01T08:01:30", datetime},
		};
		//@formatter:on

		for (Object[] test : tests) {
			String input = (String) test[0];
			Date expected = (Date) test[1];

			Date actual = ICalDateFormatter.parse(input);

			assertEquals(expected, actual);
		}
	}

	@Test
	public void dateHasTime() {
		assertFalse(ICalDateFormatter.dateHasTime("20130601"));
		assertTrue(ICalDateFormatter.dateHasTime("20130601T120000"));
	}

	@Test
	public void dateHasTimezone() {
		assertFalse(ICalDateFormatter.dateHasTimezone("20130601T120000"));
		assertTrue(ICalDateFormatter.dateHasTimezone("20130601T120000Z"));
		assertTrue(ICalDateFormatter.dateHasTimezone("20130601T120000+0100"));
		assertTrue(ICalDateFormatter.dateHasTimezone("20130601T120000-0100"));
		assertTrue(ICalDateFormatter.dateHasTimezone("2013-06-01T12:00:00+01:00"));
		assertTrue(ICalDateFormatter.dateHasTimezone("2013-06-01T12:00:00-01:00"));
	}

	@Test
	public void parse_timezone() {
		TimeZone timezone = new SimpleTimeZone(-1000 * 60 * 60 * 2, "");

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

		Date actual = ICalDateFormatter.parse("20120701T060130", timezone);
		assertEquals(actual, expected);

		//timezone in date string takes presidence
		actual = ICalDateFormatter.parse("20120701T080130Z", timezone);
		assertEquals(actual, expected);
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		ICalDateFormatter.parse("invalid");
	}

	@Test
	public void parseTimeZone() {
		//@formatter:off
		Object[][] tests = new Object[][]{	
			new Object[]{"5", 5, 0},
			new Object[]{"10", 10, 0},
			new Object[]{"+5", 5, 0},
			new Object[]{"+10", 10, 0},
			new Object[]{"-5", -5, 0},
			new Object[]{"-10", -10, 0},
			new Object[]{"05", 5, 0},
			new Object[]{"+05", 5, 0},
			new Object[]{"-05", -5, 0},
			new Object[]{"500", 5, 0},
			new Object[]{"+500", 5, 0},
			new Object[]{"-500", -5, 0},
			new Object[]{"530", 5, 30},
			new Object[]{"+530", 5, 30},
			new Object[]{"-530", -5, 30},
			new Object[]{"5:00", 5, 0},
			new Object[]{"10:00", 10, 0},
			new Object[]{"+5:00", 5, 0},
			new Object[]{"+10:00", 10, 0},
			new Object[]{"-5:00", -5, 0},
			new Object[]{"-10:00", -10, 0},
			new Object[]{"5:30", 5, 30},
			new Object[]{"10:30", 10, 30},
			new Object[]{"+5:30", 5, 30},
			new Object[]{"+10:30", 10, 30},
			new Object[]{"-5:30", -5, 30},
			new Object[]{"-10:30", -10, 30},
			new Object[]{"0500", 5, 0},
			new Object[]{"1000", 10, 0},
			new Object[]{"+0500", 5, 0},
			new Object[]{"+1000", 10, 0},
			new Object[]{"-0500", -5, 0},
			new Object[]{"-1000", -10, 0},
			new Object[]{"0530", 5, 30},
			new Object[]{"1030", 10, 30},
			new Object[]{"+0530", 5, 30},
			new Object[]{"+1030", 10, 30},
			new Object[]{"-0530", -5, 30},
			new Object[]{"-1030", -10, 30},
			new Object[]{"05:00", 5, 0},
			new Object[]{"10:00", 10, 0},
			new Object[]{"+05:00", 5, 0},
			new Object[]{"+10:00", 10, 0},
			new Object[]{"-05:00", -5, 0},
			new Object[]{"-10:00", -10, 0},
			new Object[]{"05:30", 5, 30},
			new Object[]{"10:30", 10, 30},
			new Object[]{"+05:30", 5, 30},
			new Object[]{"+10:30", 10, 30},
			new Object[]{"-05:30", -5, 30},
			new Object[]{"-10:30", -10, 30},
		};
		//@formatter:on

		for (Object test[] : tests) {
			String input = (String) test[0];
			int[] expected = new int[] { (Integer) test[1], (Integer) test[2] };
			int[] actual = ICalDateFormatter.parseTimeZone(input);
			assertArrayEquals(expected, actual);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parseTimeZone_invalid() {
		ICalDateFormatter.parseTimeZone("invalid");
	}

	@Test
	public void parseTimezoneId() {
		TimeZone tz = ICalDateFormatter.parseTimeZoneId("America/New_York");
		assertEquals(tz.getID(), "America/New_York");

		tz = ICalDateFormatter.parseTimeZoneId("Bogus/Timezone");
		assertNull(tz);
	}
}
