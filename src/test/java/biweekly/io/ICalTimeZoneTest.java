package biweekly.io;

import static org.junit.Assert.assertEquals;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.util.DateTimeComponents;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.UtcOffset;

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
		assertOffset(-4, 0, tz.getOffset(0, 2014, 2, 9, 0, ms(2, 0, 0)));
		assertOffset(-4, 0, tz.getOffset(0, 2014, 2, 9, 0, ms(2, 0, 1)));
	}

	@Test
	public void getOffset_no_dtstart() {
		//TODO
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
