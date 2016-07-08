package biweekly.io;

import static biweekly.util.TestUtils.vtimezoneNewYork;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.component.DaylightSavingsTime;
import biweekly.component.Observance;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.property.TimezoneOffsetFrom;
import biweekly.property.TimezoneOffsetTo;
import biweekly.util.DateTimeComponents;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.ICalDate;
import biweekly.util.UtcOffset;
import biweekly.util.com.google.ical.iter.RecurrenceIterator;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;

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
public class ICalTimeZoneTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(3, 0);

	private final UtcOffset minus4 = new UtcOffset(false, 4, 0);
	private final UtcOffset minus5 = new UtcOffset(false, 5, 0);

	@Test
	public void getOffset_simple_example() {
		VTimezone component = new VTimezone("America/New_York");
		{
			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(1998, 10, 25, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(minus4);
			standard.setTimezoneOffsetTo(minus5);
			component.addStandardTime(standard);

			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setDateStart(new DateTimeComponents(1999, 4, 4, 2, 0, 0, false));
			daylight.setTimezoneOffsetFrom(minus5);
			daylight.setTimezoneOffsetTo(minus4);
			component.addDaylightSavingsTime(daylight);
		}

		ICalTimeZone tz = new ICalTimeZone(component);
		assertEquals(component.getTimezoneId().getValue(), tz.getID());
		assertTrue(tz.useDaylightTime());

		assertOffset(-4, 0, false, tz, 1998, 10, 24);
		assertOffset(-4, 0, false, tz, 1998, 10, 25, 1, 59, 59);
		assertOffset(-5, 0, false, tz, 1998, 10, 25, 2, 0, 0);
		assertOffset(-5, 0, false, tz, 1998, 10, 25, 2, 0, 1);
		assertOffset(-5, 0, false, tz, 1998, 10, 26);

		assertOffset(-5, 0, false, tz, 1999, 4, 3);
		assertOffset(-5, 0, false, tz, 1999, 4, 4, 1, 59, 59);
		assertOffset(-4, 0, true, tz, 1999, 4, 4, 2, 0, 0);
		assertOffset(-4, 0, true, tz, 1999, 4, 4, 2, 0, 1);
		assertOffset(-4, 0, true, tz, 1999, 4, 5);
	}

	@Test
	public void getOffset_no_dtstart() {
		VTimezone component = new VTimezone("America/New_York");
		{
			StandardTime standard = new StandardTime();
			standard.setTimezoneOffsetFrom(minus4);
			standard.setTimezoneOffsetTo(minus5);
			component.addStandardTime(standard);

			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setTimezoneOffsetFrom(minus5);
			daylight.setTimezoneOffsetTo(minus4);
			component.addDaylightSavingsTime(daylight);
		}

		ICalTimeZone tz = new ICalTimeZone(component);
		assertEquals(component.getTimezoneId().getValue(), tz.getID());
		assertTrue(tz.useDaylightTime());

		assertOffset(0, 0, false, tz, 1998, 10, 24);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 1, 59, 59);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 2, 0, 0);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 2, 0, 1);
		assertOffset(0, 0, false, tz, 1998, 10, 26);

		assertOffset(0, 0, false, tz, 1999, 4, 3);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 1, 59, 59);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 2, 0, 0);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 2, 0, 1);
		assertOffset(0, 0, false, tz, 1999, 4, 5);
	}

	@Test
	public void getOffset_no_standard() {
		VTimezone component = new VTimezone("America/New_York");
		{
			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setDateStart(new DateTimeComponents(1999, 4, 4, 2, 0, 0, false));
			daylight.setTimezoneOffsetFrom(minus5);
			daylight.setTimezoneOffsetTo(minus4);
			component.addDaylightSavingsTime(daylight);
		}

		ICalTimeZone tz = new ICalTimeZone(component);
		assertEquals(component.getTimezoneId().getValue(), tz.getID());
		assertTrue(tz.useDaylightTime());

		assertOffset(-5, 0, false, tz, 1998, 10, 24);
		assertOffset(-5, 0, false, tz, 1998, 10, 25, 1, 59, 59);
		assertOffset(-5, 0, false, tz, 1998, 10, 25, 2, 0, 0);
		assertOffset(-5, 0, false, tz, 1998, 10, 25, 2, 0, 1);
		assertOffset(-5, 0, false, tz, 1998, 10, 26);

		assertOffset(-5, 0, false, tz, 1999, 4, 3);
		assertOffset(-5, 0, false, tz, 1999, 4, 4, 1, 59, 59);
		assertOffset(-4, 0, true, tz, 1999, 4, 4, 2, 0, 0);
		assertOffset(-4, 0, true, tz, 1999, 4, 4, 2, 0, 1);
		assertOffset(-4, 0, true, tz, 1999, 4, 5);
	}

	@Test
	public void getOffset_no_daylight() {
		VTimezone component = new VTimezone("America/New_York");
		{
			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(1998, 10, 25, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(minus4);
			standard.setTimezoneOffsetTo(minus5);
			component.addStandardTime(standard);
		}

		ICalTimeZone tz = new ICalTimeZone(component);
		assertEquals(component.getTimezoneId().getValue(), tz.getID());
		assertFalse(tz.useDaylightTime());

		assertOffset(-4, 0, false, tz, 1998, 10, 24);
		assertOffset(-4, 0, false, tz, 1998, 10, 25, 1, 59, 59);
		assertOffset(-5, 0, false, tz, 1998, 10, 25, 2, 0, 0);
		assertOffset(-5, 0, false, tz, 1998, 10, 25, 2, 0, 1);
		assertOffset(-5, 0, false, tz, 1998, 10, 26);

		assertOffset(-5, 0, false, tz, 1999, 4, 3);
		assertOffset(-5, 0, false, tz, 1999, 4, 4, 1, 59, 59);
		assertOffset(-5, 0, false, tz, 1999, 4, 4, 2, 0, 0);
		assertOffset(-5, 0, false, tz, 1999, 4, 4, 2, 0, 1);
		assertOffset(-5, 0, false, tz, 1999, 4, 5);
	}

	@Test
	public void getOffset_empty_dtstart() {
		VTimezone component = new VTimezone("America/New_York");
		{
			StandardTime standard = new StandardTime();
			standard.setDateStart((ICalDate) null);
			standard.setTimezoneOffsetFrom(minus4);
			standard.setTimezoneOffsetTo(minus5);
			component.addStandardTime(standard);

			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setDateStart((ICalDate) null);
			daylight.setTimezoneOffsetFrom(minus5);
			daylight.setTimezoneOffsetTo(minus4);
			component.addDaylightSavingsTime(daylight);
		}

		ICalTimeZone tz = new ICalTimeZone(component);
		assertEquals(component.getTimezoneId().getValue(), tz.getID());
		assertTrue(tz.useDaylightTime());

		assertOffset(0, 0, false, tz, 1998, 10, 24);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 1, 59, 59);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 2, 0, 0);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 2, 0, 1);
		assertOffset(0, 0, false, tz, 1998, 10, 26);

		assertOffset(0, 0, false, tz, 1999, 4, 3);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 1, 59, 59);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 2, 0, 0);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 2, 0, 1);
		assertOffset(0, 0, false, tz, 1999, 4, 5);
	}

	@Test
	public void getOffset_empty_offsets() {
		VTimezone component = new VTimezone("America/New_York");
		{
			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(1998, 10, 25, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(new TimezoneOffsetFrom((UtcOffset) null));
			standard.setTimezoneOffsetTo(new TimezoneOffsetTo((UtcOffset) null));
			component.addStandardTime(standard);

			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setDateStart(new DateTimeComponents(1999, 4, 4, 2, 0, 0, false));
			daylight.setTimezoneOffsetFrom(new TimezoneOffsetFrom((UtcOffset) null));
			daylight.setTimezoneOffsetTo(new TimezoneOffsetTo((UtcOffset) null));
			component.addDaylightSavingsTime(daylight);
		}

		ICalTimeZone tz = new ICalTimeZone(component);
		assertEquals(component.getTimezoneId().getValue(), tz.getID());
		assertTrue(tz.useDaylightTime());

		assertOffset(0, 0, false, tz, 1998, 10, 24);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 1, 59, 59);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 2, 0, 0);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 2, 0, 1);
		assertOffset(0, 0, false, tz, 1998, 10, 26);

		assertOffset(0, 0, false, tz, 1999, 4, 3);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 1, 59, 59);
		assertOffset(0, 0, true, tz, 1999, 4, 4, 2, 0, 0);
		assertOffset(0, 0, true, tz, 1999, 4, 4, 2, 0, 1);
		assertOffset(0, 0, true, tz, 1999, 4, 5);
	}

	@Test
	public void getOffset_no_observances() {
		VTimezone component = new VTimezone("America/New_York");
		ICalTimeZone tz = new ICalTimeZone(component);
		assertEquals(component.getTimezoneId().getValue(), tz.getID());
		assertFalse(tz.useDaylightTime());

		assertOffset(0, 0, false, tz, 1998, 10, 24);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 1, 59, 59);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 2, 0, 0);
		assertOffset(0, 0, false, tz, 1998, 10, 25, 2, 0, 1);
		assertOffset(0, 0, false, tz, 1998, 10, 26);

		assertOffset(0, 0, false, tz, 1999, 4, 3);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 1, 59, 59);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 2, 0, 0);
		assertOffset(0, 0, false, tz, 1999, 4, 4, 2, 0, 1);
		assertOffset(0, 0, false, tz, 1999, 4, 5);
	}

	@Test
	public void getOffset() {
		VTimezone component = vtimezoneNewYork();
		ICalTimeZone tz = new ICalTimeZone(component);
		assertEquals(component.getTimezoneId().getValue(), tz.getID());
		assertTrue(tz.useDaylightTime());

		assertOffset(-4, 0, true, tz, 1918, 4, 1);
		assertOffset(-5, 0, false, tz, 1918, 10, 28);

		assertOffset(-5, 0, false, tz, 1977, 1, 1);
		assertOffset(-4, 0, true, tz, 1977, 4, 25);

		assertOffset(-5, 0, false, tz, 2006, 10, 30);

		assertOffset(-4, 0, true, tz, 2007, 3, 12);
		assertOffset(-5, 0, false, tz, 2007, 11, 5);

		assertOffset(-4, 0, true, tz, 2014, 3, 10);
		assertOffset(-5, 0, false, tz, 2014, 11, 3);

		/////////////////////////////////////

		//18831118T120358
		assertOffset(-4, 56, false, tz, 1883, 11, 17);
		assertOffset(-4, 56, false, tz, 1883, 11, 18, 12, 3, 57);
		assertOffset(-5, 0, false, tz, 1883, 11, 18, 12, 3, 58);
		assertOffset(-5, 0, false, tz, 1883, 11, 19);

		//19240427T020000
		assertOffset(-5, 0, false, tz, 1924, 4, 26);
		assertOffset(-5, 0, false, tz, 1924, 4, 27, 1, 59, 59);
		assertOffset(-4, 0, true, tz, 1924, 4, 27, 2, 0, 0);
		assertOffset(-4, 0, true, tz, 1924, 4, 27, 2, 0, 1);
		assertOffset(-4, 0, true, tz, 1924, 4, 28);

		//19240928T020000
		assertOffset(-4, 0, true, tz, 1924, 9, 27);
		assertOffset(-4, 0, true, tz, 1924, 9, 28, 1, 59, 59);
		assertOffset(-5, 0, false, tz, 1924, 9, 28, 2, 0, 0);
		assertOffset(-5, 0, false, tz, 1924, 9, 28, 2, 0, 1);
		assertOffset(-5, 0, false, tz, 1924, 9, 29);

		//19420101T000000
		assertOffset(-5, 0, false, tz, 1941, 12, 31);
		assertOffset(-5, 0, false, tz, 1941, 12, 31, 23, 59, 59);
		assertOffset(-5, 0, false, tz, 1942, 1, 1);
		assertOffset(-5, 0, false, tz, 1942, 1, 1, 0, 0, 1);
		assertOffset(-5, 0, false, tz, 1942, 1, 2);

		//20140309T020000
		assertOffset(-5, 0, false, tz, 2014, 3, 8);
		assertOffset(-5, 0, false, tz, 2014, 3, 9, 1, 59, 59);
		assertOffset(-4, 0, true, tz, 2014, 3, 9, 2, 0, 0);
		assertOffset(-4, 0, true, tz, 2014, 3, 9, 2, 0, 1);
		assertOffset(-4, 0, true, tz, 2014, 3, 10);
	}

	@Test
	public void createIterator() {
		VTimezone component = vtimezoneNewYork();
		ICalTimeZone tz = new ICalTimeZone(component);
		Iterator<Observance> observances = tz.sortedObservances.iterator();

		//@formatter:off
		assertIterator(tz, observances.next(),
			new DateTimeValueImpl(1883, 11, 18, 12, 3, 58)
		);

		assertIterator(tz, observances.next(),
			new DateTimeValueImpl(1918, 3, 31, 2, 0, 0),
			new DateTimeValueImpl(1919, 3, 30, 2, 0, 0),
			new DateTimeValueImpl(1920, 3, 28, 2, 0, 0),
			new DateTimeValueImpl(1921, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1922, 4, 30, 2, 0, 0),
			new DateTimeValueImpl(1923, 4, 29, 2, 0, 0),
			new DateTimeValueImpl(1924, 4, 27, 2, 0, 0),
			new DateTimeValueImpl(1925, 4, 26, 2, 0, 0),
			new DateTimeValueImpl(1926, 4, 25, 2, 0, 0),
			new DateTimeValueImpl(1927, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1928, 4, 29, 2, 0, 0),
			new DateTimeValueImpl(1929, 4, 28, 2, 0, 0),
			new DateTimeValueImpl(1930, 4, 27, 2, 0, 0),
			new DateTimeValueImpl(1931, 4, 26, 2, 0, 0),
			new DateTimeValueImpl(1932, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1933, 4, 30, 2, 0, 0),
			new DateTimeValueImpl(1934, 4, 29, 2, 0, 0),
			new DateTimeValueImpl(1935, 4, 28, 2, 0, 0),
			new DateTimeValueImpl(1936, 4, 26, 2, 0, 0),
			new DateTimeValueImpl(1937, 4, 25, 2, 0, 0),
			new DateTimeValueImpl(1938, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1939, 4, 30, 2, 0, 0),
			new DateTimeValueImpl(1940, 4, 28, 2, 0, 0),
			new DateTimeValueImpl(1941, 4, 27, 2, 0, 0),
			new DateTimeValueImpl(1946, 4, 28, 2, 0, 0),
			new DateTimeValueImpl(1947, 4, 27, 2, 0, 0),
			new DateTimeValueImpl(1948, 4, 25, 2, 0, 0),
			new DateTimeValueImpl(1949, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1950, 4, 30, 2, 0, 0),
			new DateTimeValueImpl(1951, 4, 29, 2, 0, 0),
			new DateTimeValueImpl(1952, 4, 27, 2, 0, 0),
			new DateTimeValueImpl(1953, 4, 26, 2, 0, 0),
			new DateTimeValueImpl(1954, 4, 25, 2, 0, 0),
			new DateTimeValueImpl(1955, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1956, 4, 29, 2, 0, 0),
			new DateTimeValueImpl(1957, 4, 28, 2, 0, 0),
			new DateTimeValueImpl(1958, 4, 27, 2, 0, 0),
			new DateTimeValueImpl(1959, 4, 26, 2, 0, 0),
			new DateTimeValueImpl(1960, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1961, 4, 30, 2, 0, 0),
			new DateTimeValueImpl(1962, 4, 29, 2, 0, 0),
			new DateTimeValueImpl(1963, 4, 28, 2, 0, 0),
			new DateTimeValueImpl(1964, 4, 26, 2, 0, 0),
			new DateTimeValueImpl(1965, 4, 25, 2, 0, 0),
			new DateTimeValueImpl(1966, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1967, 4, 30, 2, 0, 0),
			new DateTimeValueImpl(1968, 4, 28, 2, 0, 0),
			new DateTimeValueImpl(1969, 4, 27, 2, 0, 0),
			new DateTimeValueImpl(1970, 4, 26, 2, 0, 0),
			new DateTimeValueImpl(1971, 4, 25, 2, 0, 0),
			new DateTimeValueImpl(1972, 4, 30, 2, 0, 0),
			new DateTimeValueImpl(1973, 4, 29, 2, 0, 0),
			new DateTimeValueImpl(1974, 1, 6, 2, 0, 0),
			new DateTimeValueImpl(1975, 2, 23, 2, 0, 0),
			new DateTimeValueImpl(1976, 4, 25, 2, 0, 0),
			new DateTimeValueImpl(1977, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1978, 4, 30, 2, 0, 0),
			new DateTimeValueImpl(1979, 4, 29, 2, 0, 0),
			new DateTimeValueImpl(1980, 4, 27, 2, 0, 0),
			new DateTimeValueImpl(1981, 4, 26, 2, 0, 0),
			new DateTimeValueImpl(1982, 4, 25, 2, 0, 0),
			new DateTimeValueImpl(1983, 4, 24, 2, 0, 0),
			new DateTimeValueImpl(1984, 4, 29, 2, 0, 0),
			new DateTimeValueImpl(1985, 4, 28, 2, 0, 0),
			new DateTimeValueImpl(1986, 4, 27, 2, 0, 0),
			new DateTimeValueImpl(1987, 4, 5, 2, 0, 0),
			new DateTimeValueImpl(1988, 4, 3, 2, 0, 0),
			new DateTimeValueImpl(1989, 4, 2, 2, 0, 0),
			new DateTimeValueImpl(1990, 4, 1, 2, 0, 0),
			new DateTimeValueImpl(1991, 4, 7, 2, 0, 0),
			new DateTimeValueImpl(1992, 4, 5, 2, 0, 0),
			new DateTimeValueImpl(1993, 4, 4, 2, 0, 0),
			new DateTimeValueImpl(1994, 4, 3, 2, 0, 0),
			new DateTimeValueImpl(1995, 4, 2, 2, 0, 0),
			new DateTimeValueImpl(1996, 4, 7, 2, 0, 0),
			new DateTimeValueImpl(1997, 4, 6, 2, 0, 0),
			new DateTimeValueImpl(1998, 4, 5, 2, 0, 0),
			new DateTimeValueImpl(1999, 4, 4, 2, 0, 0),
			new DateTimeValueImpl(2000, 4, 2, 2, 0, 0),
			new DateTimeValueImpl(2001, 4, 1, 2, 0, 0),
			new DateTimeValueImpl(2002, 4, 7, 2, 0, 0),
			new DateTimeValueImpl(2003, 4, 6, 2, 0, 0),
			new DateTimeValueImpl(2004, 4, 4, 2, 0, 0),
			new DateTimeValueImpl(2005, 4, 3, 2, 0, 0),
			new DateTimeValueImpl(2006, 4, 2, 2, 0, 0)
		);
		
		assertIterator(tz, observances.next(),
			new DateTimeValueImpl(1918, 10, 27, 2, 0, 0),
			new DateTimeValueImpl(1919, 10, 26, 2, 0, 0),
			new DateTimeValueImpl(1920, 10, 31, 2, 0, 0),
			new DateTimeValueImpl(1921, 9, 25, 2, 0, 0),
			new DateTimeValueImpl(1922, 9, 24, 2, 0, 0),
			new DateTimeValueImpl(1923, 9, 30, 2, 0, 0),
			new DateTimeValueImpl(1924, 9, 28, 2, 0, 0),
			new DateTimeValueImpl(1925, 9, 27, 2, 0, 0),
			new DateTimeValueImpl(1926, 9, 26, 2, 0, 0),
			new DateTimeValueImpl(1927, 9, 25, 2, 0, 0),
			new DateTimeValueImpl(1928, 9, 30, 2, 0, 0),
			new DateTimeValueImpl(1929, 9, 29, 2, 0, 0),
			new DateTimeValueImpl(1930, 9, 28, 2, 0, 0),
			new DateTimeValueImpl(1931, 9, 27, 2, 0, 0),
			new DateTimeValueImpl(1932, 9, 25, 2, 0, 0),
			new DateTimeValueImpl(1933, 9, 24, 2, 0, 0),
			new DateTimeValueImpl(1934, 9, 30, 2, 0, 0),
			new DateTimeValueImpl(1935, 9, 29, 2, 0, 0),
			new DateTimeValueImpl(1936, 9, 27, 2, 0, 0),
			new DateTimeValueImpl(1937, 9, 26, 2, 0, 0),
			new DateTimeValueImpl(1938, 9, 25, 2, 0, 0),
			new DateTimeValueImpl(1939, 9, 24, 2, 0, 0),
			new DateTimeValueImpl(1940, 9, 29, 2, 0, 0),
			new DateTimeValueImpl(1941, 9, 28, 2, 0, 0),
			new DateTimeValueImpl(1945, 9, 30, 2, 0, 0),
			new DateTimeValueImpl(1946, 9, 29, 2, 0, 0),
			new DateTimeValueImpl(1947, 9, 28, 2, 0, 0),
			new DateTimeValueImpl(1948, 9, 26, 2, 0, 0),
			new DateTimeValueImpl(1949, 9, 25, 2, 0, 0),
			new DateTimeValueImpl(1950, 9, 24, 2, 0, 0),
			new DateTimeValueImpl(1951, 9, 30, 2, 0, 0),
			new DateTimeValueImpl(1952, 9, 28, 2, 0, 0),
			new DateTimeValueImpl(1953, 9, 27, 2, 0, 0),
			new DateTimeValueImpl(1954, 9, 26, 2, 0, 0),
			new DateTimeValueImpl(1955, 10, 30, 2, 0, 0),
			new DateTimeValueImpl(1956, 10, 28, 2, 0, 0),
			new DateTimeValueImpl(1957, 10, 27, 2, 0, 0),
			new DateTimeValueImpl(1958, 10, 26, 2, 0, 0),
			new DateTimeValueImpl(1959, 10, 25, 2, 0, 0),
			new DateTimeValueImpl(1960, 10, 30, 2, 0, 0),
			new DateTimeValueImpl(1961, 10, 29, 2, 0, 0),
			new DateTimeValueImpl(1962, 10, 28, 2, 0, 0),
			new DateTimeValueImpl(1963, 10, 27, 2, 0, 0),
			new DateTimeValueImpl(1964, 10, 25, 2, 0, 0),
			new DateTimeValueImpl(1965, 10, 31, 2, 0, 0),
			new DateTimeValueImpl(1966, 10, 30, 2, 0, 0),
			new DateTimeValueImpl(1967, 10, 29, 2, 0, 0),
			new DateTimeValueImpl(1968, 10, 27, 2, 0, 0),
			new DateTimeValueImpl(1969, 10, 26, 2, 0, 0),
			new DateTimeValueImpl(1970, 10, 25, 2, 0, 0),
			new DateTimeValueImpl(1971, 10, 31, 2, 0, 0),
			new DateTimeValueImpl(1972, 10, 29, 2, 0, 0),
			new DateTimeValueImpl(1973, 10, 28, 2, 0, 0),
			new DateTimeValueImpl(1974, 10, 27, 2, 0, 0),
			new DateTimeValueImpl(1975, 10, 26, 2, 0, 0),
			new DateTimeValueImpl(1976, 10, 31, 2, 0, 0),
			new DateTimeValueImpl(1977, 10, 30, 2, 0, 0),
			new DateTimeValueImpl(1978, 10, 29, 2, 0, 0),
			new DateTimeValueImpl(1979, 10, 28, 2, 0, 0),
			new DateTimeValueImpl(1980, 10, 26, 2, 0, 0),
			new DateTimeValueImpl(1981, 10, 25, 2, 0, 0),
			new DateTimeValueImpl(1982, 10, 31, 2, 0, 0),
			new DateTimeValueImpl(1983, 10, 30, 2, 0, 0),
			new DateTimeValueImpl(1984, 10, 28, 2, 0, 0),
			new DateTimeValueImpl(1985, 10, 27, 2, 0, 0),
			new DateTimeValueImpl(1986, 10, 26, 2, 0, 0),
			new DateTimeValueImpl(1987, 10, 25, 2, 0, 0),
			new DateTimeValueImpl(1988, 10, 30, 2, 0, 0),
			new DateTimeValueImpl(1989, 10, 29, 2, 0, 0),
			new DateTimeValueImpl(1990, 10, 28, 2, 0, 0),
			new DateTimeValueImpl(1991, 10, 27, 2, 0, 0),
			new DateTimeValueImpl(1992, 10, 25, 2, 0, 0),
			new DateTimeValueImpl(1993, 10, 31, 2, 0, 0),
			new DateTimeValueImpl(1994, 10, 30, 2, 0, 0),
			new DateTimeValueImpl(1995, 10, 29, 2, 0, 0),
			new DateTimeValueImpl(1996, 10, 27, 2, 0, 0),
			new DateTimeValueImpl(1997, 10, 26, 2, 0, 0),
			new DateTimeValueImpl(1998, 10, 25, 2, 0, 0),
			new DateTimeValueImpl(1999, 10, 31, 2, 0, 0),
			new DateTimeValueImpl(2000, 10, 29, 2, 0, 0),
			new DateTimeValueImpl(2001, 10, 28, 2, 0, 0),
			new DateTimeValueImpl(2002, 10, 27, 2, 0, 0),
			new DateTimeValueImpl(2003, 10, 26, 2, 0, 0),
			new DateTimeValueImpl(2004, 10, 31, 2, 0, 0),
			new DateTimeValueImpl(2005, 10, 30, 2, 0, 0),
			new DateTimeValueImpl(2006, 10, 29, 2, 0, 0)
		);
		
		assertIterator(tz, observances.next(), 
			new DateTimeValueImpl(1920, 1, 1, 0, 0, 0),
			new DateTimeValueImpl(1942, 1, 1, 0, 0, 0),
			new DateTimeValueImpl(1946, 1, 1, 0, 0, 0),
			new DateTimeValueImpl(1967, 1, 1, 0, 0, 0)
		);
		
		assertIterator(tz, observances.next(), 
			new DateTimeValueImpl(1942, 2, 9, 2, 0, 0)
		);
		
		assertIterator(tz, observances.next(), 
			new DateTimeValueImpl(1945, 8, 14, 19, 0, 0)
		);
		
		assertIterator(tz, observances.next(), 
			new DateTimeValueImpl(2007, 3, 11, 2, 0, 0),
			new DateTimeValueImpl(2008, 3, 9, 2, 0, 0),
			new DateTimeValueImpl(2009, 3, 8, 2, 0, 0),
			new DateTimeValueImpl(2010, 3, 14, 2, 0, 0),
			new DateTimeValueImpl(2011, 3, 13, 2, 0, 0),
			new DateTimeValueImpl(2012, 3, 11, 2, 0, 0),
			new DateTimeValueImpl(2013, 3, 10, 2, 0, 0),
			new DateTimeValueImpl(2014, 3, 9, 2, 0, 0),
			null
		);
		
		assertIterator(tz, observances.next(), 
			new DateTimeValueImpl(2007, 11, 4, 2, 0, 0),
			new DateTimeValueImpl(2008, 11, 2, 2, 0, 0),
			new DateTimeValueImpl(2009, 11, 1, 2, 0, 0),
			new DateTimeValueImpl(2010, 11, 7, 2, 0, 0),
			new DateTimeValueImpl(2011, 11, 6, 2, 0, 0),
			new DateTimeValueImpl(2012, 11, 4, 2, 0, 0),
			new DateTimeValueImpl(2013, 11, 3, 2, 0, 0),
			new DateTimeValueImpl(2014, 11, 2, 2, 0, 0),
			null
		);
		//@formatter:on

		assertFalse(observances.hasNext());
	}

	private static void assertIterator(ICalTimeZone tz, Observance observance, DateValue... values) {
		RecurrenceIterator it = tz.createIterator(observance);
		for (DateValue value : values) {
			if (value == null) {
				return;
			}

			assertEquals(value, it.next());
		}
		assertFalse(it.hasNext());
	}

	private static void assertOffset(int expectedHours, int expectedMinutes, boolean expectedInDaylight, ICalTimeZone tz, int year, int month, int date) {
		assertOffset(expectedHours, expectedMinutes, expectedInDaylight, tz, year, month, date, 0, 0, 0);
	}

	private static void assertOffset(int expectedHours, int expectedMinutes, boolean expectedInDaylight, ICalTimeZone tz, int year, int month, int date, int hour, int minute, int second) {
		month -= 1;

		UtcOffset expected = new UtcOffset(expectedHours >= 0, expectedHours, expectedMinutes);
		int actualMillis = tz.getOffset(0, year, month, date, 0, ms(hour, minute, second));
		UtcOffset actual = new UtcOffset(actualMillis);
		assertEquals(expected, actual);

		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.clear();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DATE, date);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		assertEquals(expectedInDaylight, tz.inDaylightTime(c.getTime()));
	}

	private static int ms(int hours, int minutes, int seconds) {
		//@formatter:off
		return
		hours * 60 * 60 * 1000 +
		minutes * 60 * 1000 +
		seconds * 1000;
		//@formatter:on
	}
}
