package biweekly.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Iterator;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.Observance;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.util.DateTimeComponents;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.UtcOffset;

import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;

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
public class ICalTimeZoneTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(3, 0);

	@Test
	public void getOffset_no_rdates_or_rrules() {
		VTimezone component = new VTimezone("America/New_York");
		{
			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(1998, 10, 25, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(-4, 0);
			standard.setTimezoneOffsetTo(-5, 0);
			component.addStandardTime(standard);

			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setDateStart(new DateTimeComponents(1999, 4, 4, 2, 0, 0, false));
			daylight.setTimezoneOffsetFrom(-5, 0);
			daylight.setTimezoneOffsetTo(-4, 0);
			component.addDaylightSavingsTime(daylight);
		}

		ICalTimeZone tz = new ICalTimeZone(component);

		assertEquals(component.getTimezoneId().getValue(), tz.getID());

		assertOffset(-4, 0, tz.getOffset(0, 1997, 9, 24, 0, 0));
		assertOffset(-4, 0, tz.getOffset(0, 1997, 9, 25, 0, ms(1, 59, 59)));
		assertOffset(-5, 0, tz.getOffset(0, 1998, 9, 25, 0, ms(2, 0, 1)));
		assertOffset(-5, 0, tz.getOffset(0, 1998, 9, 26, 0, 0));

		assertOffset(-4, 0, tz.getOffset(0, 1999, 3, 5, 0, 0));
		assertOffset(-4, 0, tz.getOffset(0, 2010, 9, 24, 0, 0));
	}

	@Test
	public void getOffset() throws Exception {
		VTimezone component;
		{
			ICalendar ical = Biweekly.parse(getClass().getResourceAsStream("New_York.ics")).first();
			component = ical.getTimezones().get(0);
		}

		ICalTimeZone tz = new ICalTimeZone(component);

		assertEquals(component.getTimezoneId().getValue(), tz.getID());

		assertOffset(-4, 56, tz.getOffset(0, 1883, 10, 17, 0, 0));
		assertOffset(-5, 0, tz.getOffset(0, 1883, 10, 19, 0, 0));

		assertOffset(-5, 0, tz.getOffset(0, 1918, 2, 30, 0, 0));
		assertOffset(-4, 0, tz.getOffset(0, 1918, 3, 1, 0, 0));
		assertOffset(-5, 0, tz.getOffset(0, 1918, 9, 28, 0, 0));

		assertOffset(-5, 0, tz.getOffset(0, 1977, 0, 1, 0, 0));
		assertOffset(-4, 0, tz.getOffset(0, 1977, 3, 25, 0, 0));

		assertOffset(-5, 0, tz.getOffset(0, 2006, 9, 30, 0, 0));

		assertOffset(-4, 0, tz.getOffset(0, 2007, 2, 12, 0, 0));
		assertOffset(-5, 0, tz.getOffset(0, 2007, 10, 5, 0, 0));

		assertOffset(-4, 0, tz.getOffset(0, 2014, 2, 10, 0, 0));
		assertOffset(-5, 0, tz.getOffset(0, 2014, 10, 3, 0, 0));

		/////////////////////////////////////

		//18831118T120358
		assertOffset(-4, 56, tz.getOffset(0, 1883, 10, 18, 0, ms(12, 3, 57)));
		assertOffset(-5, 0, tz.getOffset(0, 1883, 10, 18, 0, ms(12, 3, 58)));
		assertOffset(-5, 0, tz.getOffset(0, 1883, 10, 19, 0, ms(12, 3, 59)));

		//19240427T020000
		assertOffset(-5, 0, tz.getOffset(0, 1924, 3, 27, 0, ms(1, 59, 59)));
		assertOffset(-4, 0, tz.getOffset(0, 1924, 3, 27, 0, ms(2, 0, 0)));
		assertOffset(-4, 0, tz.getOffset(0, 1924, 3, 27, 0, ms(2, 0, 1)));

		//19420101T000000
		assertOffset(-5, 0, tz.getOffset(0, 1941, 11, 31, 0, ms(23, 59, 59)));
		assertOffset(-5, 0, tz.getOffset(0, 1942, 0, 1, 0, ms(0, 0, 0)));
		assertOffset(-5, 0, tz.getOffset(0, 1942, 0, 1, 0, ms(0, 0, 1)));

		assertOffset(-5, 0, tz.getOffset(0, 2014, 2, 9, 0, ms(1, 59, 59)));
		assertOffset(-4, 0, tz.getOffset(0, 2014, 2, 9, 0, ms(3, 0, 0)));
		assertOffset(-4, 0, tz.getOffset(0, 2014, 2, 9, 0, ms(3, 0, 1)));
	}

	@Test
	public void getOffset_no_dtstart() {
		//TODO
	}

	@Test
	public void createIterator() throws Exception {
		VTimezone component;
		{
			ICalendar ical = Biweekly.parse(getClass().getResourceAsStream("New_York.ics")).first();
			component = ical.getTimezones().get(0);
		}

		ICalTimeZone tz = new ICalTimeZone(component);
		Iterator<Observance> observances = tz.getSortedObservances().iterator();

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

	private void assertIterator(ICalTimeZone tz, Observance observance, DateValue... values) {
		RecurrenceIterator it = tz.createIterator(observance);
		for (DateValue value : values) {
			if (value == null) {
				return;
			}

			assertEquals(value, it.next());
		}
		assertFalse(it.hasNext());
	}

	private void assertOffset(int expectedHours, int expectedMinutes, int actualMillis) {
		UtcOffset expected = new UtcOffset(expectedHours, expectedMinutes);
		UtcOffset actual = new UtcOffset(actualMillis);
		assertEquals(expected, actual);
	}

	private int ms(int hours, int minutes, int seconds) {
		int ms = 0;

		ms += hours * 60 * 60 * 1000;
		ms += minutes * 60 * 1000;
		ms += seconds * 1000;

		return ms;
	}
}
