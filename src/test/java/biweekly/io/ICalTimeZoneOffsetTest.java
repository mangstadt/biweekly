package biweekly.io;

import static biweekly.util.TestUtils.each;
import static biweekly.util.TestUtils.utc;
import static biweekly.util.TestUtils.vtimezoneNewYork;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.property.DateStart;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDateFormat;
import biweekly.util.UtcOffset;

/*
Copyright (c) 2013-2024, Michael Angstadt
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
 * @see "https://github.com/mangstadt/biweekly/pull/138"
 */
class ICalTimeZoneOffsetTest {
	@Test
	void constant_daylight_offset_before_and_after_onset() {
		for (String onset : each("20000519T150000", "21000519T150000")) {
			for (String event : each("20010519T150000", "21010519T150000")) {
				assertParsed(onset, event, "+0200", "+0200", event.substring(0, 8) + "T130000Z");
			}
		}
	}

	@Test
	void half_hour_daylight_saving() {
		assertParsed("20000519T020000", "20010519T150000", "+1030", "+1100", "20010519T040000Z");
	}

	@Test
	void negative_constant_offset() {
		assertParsed("21000519T150000", "20010519T150000", "-0330", "-0330", "20010519T183000Z");
	}

	@Test
	void utc_transition_boundaries() {
		ICalTimeZone tz = new ICalTimeZone(vtimezoneNewYork());
		assertEquals(-5 * 3600000, tz.getOffset(utc(2014, 3, 9, 6, 59, 59).getTime()));
		assertEquals(-4 * 3600000, tz.getOffset(utc(2014, 3, 9, 7, 0, 0).getTime()));
		assertEquals(-4 * 3600000, tz.getOffset(utc(2014, 11, 2, 5, 59, 59).getTime()));
		assertEquals(-5 * 3600000, tz.getOffset(utc(2014, 11, 2, 6, 0, 0).getTime()));
	}

	@Test
	void parse_gap_and_overlap() {
		ICalTimeZone tz = new ICalTimeZone(vtimezoneNewYork());
		assertEquals(utc(2014, 3, 9, 7, 30, 0), ICalDateFormat.parse("20140309T023000", tz));
		assertEquals(utc(2014, 11, 2, 5, 30, 0), ICalDateFormat.parse("20141102T013000", tz));
	}

	@Test
	void explicit_offset_overrides_timezone() {
		ICalTimeZone tz = new ICalTimeZone(vtimezoneNewYork());
		assertEquals(utc(2014, 3, 9, 2, 30, 0), ICalDateFormat.parse("20140309T023000Z", tz));
		assertEquals(utc(2014, 3, 9, 0, 30, 0), ICalDateFormat.parse("20140309T023000+0200", tz));
	}

	@Test
	void date_only_and_fractional_seconds() {
		ICalTimeZone tz = new ICalTimeZone(vtimezoneNewYork());
		assertEquals(utc(2014, 7, 1, 4, 0, 0), ICalDateFormat.parse("20140701", tz));
		assertEquals(utc(2014, 7, 1, 16, 0, 0).getTime() + 123, ICalDateFormat.parse("20140701T120000.123", tz).getTime());
	}

	@Test
	void undated_vcalendar_standard_offset() {
		VTimezone component = new VTimezone("TZ");
		StandardTime standard = new StandardTime();
		standard.setTimezoneOffsetFrom(new UtcOffset(false, 5, 0));
		standard.setTimezoneOffsetTo(new UtcOffset(false, 5, 0));
		component.addStandardTime(standard);
		ICalTimeZone timezone = new ICalTimeZone(component);
		assertEquals(utc(2014, 3, 8, 17, 0, 0), new DateTimeComponents(2014, 3, 8, 12, 0, 0, false).toDate(timezone));
	}

	private static void assertParsed(String onset, String event, String from, String to, String expected) {
		//@formatter:off
		String ics =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2.0\r\n" +
		"BEGIN:VEVENT\r\n" +
			"DTSTART;TZID=custom:" + event + "\r\n" +
		"END:VEVENT\r\n" +
		"BEGIN:VTIMEZONE\r\n" +
			"TZID:custom\r\n" +
			"BEGIN:DAYLIGHT\r\n" +
				"DTSTART:" + onset + "\r\n" + 
				"TZOFFSETFROM:" + from + "\r\n" +
				"TZOFFSETTO:" + to + "\r\n" +
			"END:DAYLIGHT\r\n" + 
		"END:VTIMEZONE\r\n" + 
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalendar calendar = Biweekly.parse(ics).first();
		DateStart start = calendar.getEvents().get(0).getDateStart();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertEquals(expected, format.format(start.getValue()), () -> onset + " / " + event);
		format.applyPattern("yyyyMMdd'T'HHmmss");
		format.setTimeZone(calendar.getTimezoneInfo().getTimezone(start).getTimeZone());
		assertEquals(event, format.format(start.getValue()));
	}
}
