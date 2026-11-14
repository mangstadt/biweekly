package biweekly.issues;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.property.DateStart;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/133"
 */
public class Issue133 {
	/*
	 * DTSTART property of VTIMEZONE component is in the past.
	 */
	@Test
	public void past() {
		//@formatter:off
		String text = 
		"BEGIN:VCALENDAR\r\n" + 
		"VERSION:2.0\r\n" + 
		"BEGIN:VEVENT\r\n" + 
		"DTSTART;TZID=Europe/Berlin:20250519T150000\r\n" + 
		"DTEND;TZID=Europe/Berlin:20250519T160000\r\n" + 
		"END:VEVENT\r\n" + 
		"BEGIN:VTIMEZONE\r\n" + 
		"TZID:Europe/Berlin\r\n" + 
		"BEGIN:DAYLIGHT\r\n" + 
		"DTSTART:20250519T150000\r\n" + 
		"TZNAME:CEST\r\n" + 
		"TZOFFSETTO:+0200\r\n" + 
		"TZOFFSETFROM:+0200\r\n" + 
		"END:DAYLIGHT\r\n" + 
		"END:VTIMEZONE\r\n" + 
		"END:VCALENDAR\r\n";
		//@formatter:on

		assertEventStartTime(text);
	}

	/*
	 * DTSTART property of VTIMEZONE component is in the future.
	 */
	@Test
	public void future() {
		//@formatter:off
		String text = 
		"BEGIN:VCALENDAR\r\n" + 
		"VERSION:2.0\r\n" + 
		"BEGIN:VEVENT\r\n" + 
		"DTSTART;TZID=Europe/Berlin:20250519T150000\r\n" + 
		"DTEND;TZID=Europe/Berlin:20250519T160000\r\n" + 
		"END:VEVENT\r\n" + 
		"BEGIN:VTIMEZONE\r\n" + 
		"TZID:Europe/Berlin\r\n" + 
		"BEGIN:DAYLIGHT\r\n" + 
		"DTSTART:" + (currentYear() + 1) +"0519T150000\r\n" + 
		"TZNAME:CEST\r\n" + 
		"TZOFFSETTO:+0200\r\n" + 
		"TZOFFSETFROM:+0200\r\n" + 
		"END:DAYLIGHT\r\n" + 
		"END:VTIMEZONE\r\n" + 
		"END:VCALENDAR\r\n";
		//@formatter:on

		assertEventStartTime(text);
	}

	/*
	 * VTIMEZONE component is removed. Global ID is used.
	 */
	@Test
	public void global() {
		//@formatter:off
		String text = 
		"BEGIN:VCALENDAR\r\n" + 
		"VERSION:2.0\r\n" + 
		"BEGIN:VEVENT\r\n" + 
		"DTSTART;TZID=/Europe/Berlin:20250519T150000\r\n" + 
		"DTEND;TZID=/Europe/Berlin:20250519T160000\r\n" + 
		"END:VEVENT\r\n" + 
		"END:VCALENDAR\r\n";
		//@formatter:on

		assertEventStartTime(text);
	}

	private static void assertEventStartTime(String text) {
		ICalendar ical = Biweekly.parse(text).first();

		VEvent event = ical.getEvents().get(0);
		DateStart start = event.getDateStart();

		//assertHour(9, start, TimeZone.getDefault());

		TimeZone tz = ical.getTimezoneInfo().getTimezone(start).getTimeZone();
		assertHour(15, start, tz);

		TimeZone utc = TimeZone.getTimeZone("UTC");
		assertHour(13, start, utc);

		//DateFormat df = DateFormat.getDateTimeInstance();
		//System.out.println("JVM timezone: " + df.format(start.getValue()) + " (" + TimeZone.getDefault().getDisplayName() + ")");
		//df.setTimeZone(tz);
		//System.out.println("VTIMEZONE:    " + df.format(start.getValue()));
		//df.setTimeZone(utc);
		//System.out.println("UTC:          " + df.format(start.getValue()));
	}

	private static void assertHour(int expected, DateStart start, TimeZone tz) {
		Calendar cal = Calendar.getInstance(tz);
		cal.setTime(start.getValue());
		int actual = cal.get(Calendar.HOUR_OF_DAY);
		assertEquals(expected, actual);
	}

	private static int currentYear() {
		Calendar cal = Calendar.getInstance();
		return cal.get(Calendar.YEAR);
	}
}
