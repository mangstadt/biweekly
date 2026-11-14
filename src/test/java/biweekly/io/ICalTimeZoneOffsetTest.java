package biweekly.io;

import static biweekly.util.TestUtils.utc;
import static biweekly.util.TestUtils.vtimezoneNewYork;
import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.property.DateStart;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.util.UtcOffset;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDateFormat;

public class ICalTimeZoneOffsetTest {
	@Test
	public void constant_daylight_offset_before_and_after_onset() throws Exception {
		for (String onset : new String[] { "20000519T150000", "21000519T150000" }) {
			for (String event : new String[] { "20010519T150000", "21010519T150000" }) {
				assertParsed(onset, event, "+0200", "+0200", event.substring(0, 8) + "T130000Z");
			}
		}
	}

	@Test
	public void half_hour_daylight_saving() throws Exception {
		assertParsed("20000519T020000", "20010519T150000", "+1030", "+1100", "20010519T040000Z");
	}

	@Test
	public void negative_constant_offset() throws Exception {
		assertParsed("21000519T150000", "20010519T150000", "-0330", "-0330", "20010519T183000Z");
	}

	@Test
	public void utc_transition_boundaries() {
		ICalTimeZone tz = new ICalTimeZone(vtimezoneNewYork());
		assertEquals(-5 * 3600000, tz.getOffset(utc(2014, 3, 9, 6, 59, 59).getTime()));
		assertEquals(-4 * 3600000, tz.getOffset(utc(2014, 3, 9, 7, 0, 0).getTime()));
		assertEquals(-4 * 3600000, tz.getOffset(utc(2014, 11, 2, 5, 59, 59).getTime()));
		assertEquals(-5 * 3600000, tz.getOffset(utc(2014, 11, 2, 6, 0, 0).getTime()));
	}

	@Test
	public void parse_gap_and_overlap() {
		ICalTimeZone tz = new ICalTimeZone(vtimezoneNewYork());
		assertEquals(utc(2014, 3, 9, 7, 30, 0), ICalDateFormat.parse("20140309T023000", tz));
		assertEquals(utc(2014, 11, 2, 5, 30, 0), ICalDateFormat.parse("20141102T013000", tz));
	}

	@Test
	public void explicit_offset_overrides_timezone() {
		ICalTimeZone tz = new ICalTimeZone(vtimezoneNewYork());
		assertEquals(utc(2014, 3, 9, 2, 30, 0), ICalDateFormat.parse("20140309T023000Z", tz));
		assertEquals(utc(2014, 3, 9, 0, 30, 0), ICalDateFormat.parse("20140309T023000+0200", tz));
	}

	@Test
	public void date_only_and_fractional_seconds() {
		ICalTimeZone tz = new ICalTimeZone(vtimezoneNewYork());
		assertEquals(utc(2014, 7, 1, 4, 0, 0), ICalDateFormat.parse("20140701", tz));
		assertEquals(utc(2014, 7, 1, 16, 0, 0).getTime() + 123,
			ICalDateFormat.parse("20140701T120000.123", tz).getTime());
	}

	@Test
	public void undated_vcalendar_standard_offset() {
		VTimezone component = new VTimezone("TZ");
		StandardTime standard = new StandardTime();
		standard.setTimezoneOffsetFrom(new UtcOffset(false, 5, 0));
		standard.setTimezoneOffsetTo(new UtcOffset(false, 5, 0));
		component.addStandardTime(standard);
		ICalTimeZone timezone = new ICalTimeZone(component);
		assertEquals(utc(2014, 3, 8, 17, 0, 0), new DateTimeComponents(2014, 3, 8, 12, 0, 0, false).toDate(timezone));
	}

	private void assertParsed(String onset, String event, String from, String to, String expected) throws Exception {
		String ics = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//Test//EN\r\n"
			+ "BEGIN:VEVENT\r\nUID:test\r\nDTSTART;TZID=custom:" + event + "\r\nEND:VEVENT\r\n"
			+ "BEGIN:VTIMEZONE\r\nTZID:custom\r\nBEGIN:DAYLIGHT\r\nDTSTART:" + onset
			+ "\r\nTZOFFSETFROM:" + from + "\r\nTZOFFSETTO:" + to
			+ "\r\nEND:DAYLIGHT\r\nEND:VTIMEZONE\r\nEND:VCALENDAR\r\n";
		ICalendar calendar = Biweekly.parse(ics).first();
		DateStart start = calendar.getEvents().get(0).getDateStart();
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		assertEquals(onset + " / " + event, expected, format.format(start.getValue()));
		format.applyPattern("yyyyMMdd'T'HHmmss");
		format.setTimeZone(calendar.getTimezoneInfo().getTimezone(start).getTimeZone());
		assertEquals(event, format.format(start.getValue()));
	}
}
