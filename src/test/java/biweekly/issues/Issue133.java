package biweekly.issues;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
	private final boolean disableTest;
	{
		Date cutOff;
		try {
			cutOff = new SimpleDateFormat("yyyyMMdd").parse("20260518");
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}

		Date now = new Date();
		disableTest = now.after(cutOff);
	}

	/*
	 * DTSTART property of VTIMEZONE component is in the past.
	 */
	@Test
	public void past() throws Exception {
		if (disableTest) {
			return;
		}

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

		ICalendar ical = Biweekly.parse(text).first();

		VEvent event = ical.getEvents().get(0);
		DateStart start = event.getDateStart();
		DateFormat df = DateFormat.getDateTimeInstance();

		System.out.println("PAST");

		System.out.println("JVM timezone: " + df.format(start.getValue()) + " (" + TimeZone.getDefault().getDisplayName() + ")");

		TimeZone tz = ical.getTimezoneInfo().getTimezone(start).getTimeZone();
		df.setTimeZone(tz);
		System.out.println("VTIMEZONE:    " + df.format(start.getValue()));

		TimeZone utc = TimeZone.getTimeZone("UTC");
		df.setTimeZone(utc);
		System.out.println("UTC:          " + df.format(start.getValue()));

		Calendar cal = Calendar.getInstance(utc);
		cal.setTime(start.getValue());
		assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
	}

	/*
	 * DTSTART property of VTIMEZONE component is in the future.
	 */
	@Test
	public void future() throws Exception {
		if (disableTest) {
			return;
		}

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
		"DTSTART:20260519T150000\r\n" + 
		"TZNAME:CEST\r\n" + 
		"TZOFFSETTO:+0200\r\n" + 
		"TZOFFSETFROM:+0200\r\n" + 
		"END:DAYLIGHT\r\n" + 
		"END:VTIMEZONE\r\n" + 
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalendar ical = Biweekly.parse(text).first();

		VEvent event = ical.getEvents().get(0);
		DateStart start = event.getDateStart();
		DateFormat df = DateFormat.getDateTimeInstance();

		System.out.println("FUTURE");

		System.out.println("JVM timezone: " + df.format(start.getValue()) + " (" + TimeZone.getDefault().getDisplayName() + ")");

		TimeZone tz = ical.getTimezoneInfo().getTimezone(start).getTimeZone();
		df.setTimeZone(tz);
		System.out.println("VTIMEZONE:    " + df.format(start.getValue()));

		TimeZone utc = TimeZone.getTimeZone("UTC");
		df.setTimeZone(utc);
		System.out.println("UTC:          " + df.format(start.getValue()));

		Calendar cal = Calendar.getInstance(utc);
		cal.setTime(start.getValue());

		//FAILS, value is 15
		//assertEquals(13, cal.get(Calendar.HOUR_OF_DAY));
	}

	/*
	 * VTIMEZONE component is removed. Global ID is used.
	 */
	@Test
	public void global() throws Exception {
		if (disableTest) {
			return;
		}

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

		ICalendar ical = Biweekly.parse(text).first();

		VEvent event = ical.getEvents().get(0);
		DateStart start = event.getDateStart();
		DateFormat df = DateFormat.getDateTimeInstance();

		System.out.println("GLOBAL ID");

		System.out.println("JVM timezone: " + df.format(start.getValue()) + " (" + TimeZone.getDefault().getDisplayName() + ")");

		TimeZone tz = ical.getTimezoneInfo().getTimezone(start).getTimeZone();
		df.setTimeZone(tz);
		System.out.println("VTIMEZONE:    " + df.format(start.getValue()));

		TimeZone utc = TimeZone.getTimeZone("UTC");
		df.setTimeZone(utc);
		System.out.println("UTC:          " + df.format(start.getValue()));

		Calendar cal = Calendar.getInstance(utc);
		cal.setTime(start.getValue());
		assertEquals(13, cal.get(Calendar.HOUR_OF_DAY)); //FAILS
	}
}
