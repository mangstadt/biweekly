package biweekly.issues;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.ParseWarning;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.assertParseWarnings;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/106"
 * @see "https://github.com/mangstadt/biweekly/pull/107"
 */
public class Issue106 {
	@Test
	public void test() throws Exception {
		String input =
		"BEGIN:VCALENDAR\n" +
		"VERSION:2.0\n" +
		"PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\n" +
		"BEGIN:VEVENT\n" +
		"CREATED:2020216T152600Z\n" +
		"LAST-MODIFIED:20200216T152600Z\n" +
		"DTSTAMP:20070216T152600Z\n" +
		"UID:6bcc834e-791f-4054-bfc1-13d698567c94\n" +
		"SUMMARY:Cancun 9AM Mon\n" +
		"RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO\n" +
		"DTSTART;TZID=/mozilla.org/20050126_1/America/Cancun:20201221T090000\n" +
		"DTEND;TZID=/mozilla.org/20050126_1/America/Cancun:20201221T100000\n" +
		"END:VEVENT\n" +
		"END:VCALENDAR\n";

		TimeZone americaCancun = TimeZone.getTimeZone("America/Cancun");
		TimeZone defaultTz = TimeZone.getTimeZone("Europe/Paris");
		List<List<ParseWarning>> warnings = new ArrayList<List<ParseWarning>>();
		ICalendar ical = Biweekly.parse(input).defaultTimezone(defaultTz).warnings(warnings).first();

		assertParseWarnings(warnings.get(0), 17);

		VEvent event = ical.getEvents().get(0);

		{
			DateStart property = event.getDateStart();
			Date expected = date("2020-12-21 09:00:00", americaCancun);
			Date actual = property.getValue();
			assertEquals(expected, actual);
			assertNull(property.getParameters().getTimezoneId());
		}

		{
			DateEnd property = event.getDateEnd();
			Date expected = date("2020-12-21 10:00:00", americaCancun);
			Date actual = property.getValue();
			assertEquals(expected, actual);
			assertNull(property.getParameters().getTimezoneId());
		}
	}

	@Test
	public void regex_workaround() throws Exception {
		String input =
		"BEGIN:VCALENDAR\n" +
		"VERSION:2.0\n" +
		"PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\n" +
		"BEGIN:VEVENT\n" +
		"CREATED:2020216T152600Z\n" +
		"LAST-MODIFIED:20200216T152600Z\n" +
		"DTSTAMP:20070216T152600Z\n" +
		"UID:6bcc834e-791f-4054-bfc1-13d698567c94\n" +
		"SUMMARY:Cancun 9AM Mon\n" +
		"RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO\n" +
		"DTSTART;TZID=/mozilla.org/20050126_1/America/Cancun:20201221T090000\n" +
		"DTEND;TZID=/mozilla.org/20050126_1/America/Cancun;X-FOO=bar:20201221T100000\n" +
		"END:VEVENT\n" +
		"END:VCALENDAR\n";

		String actual = input.replaceAll("TZID=/mozilla.org/.*?(/.*[:;])", "TZID=$1");
		String expected =
		"BEGIN:VCALENDAR\n" +
		"VERSION:2.0\n" +
		"PRODID:-//Mozilla.org/NONSGML Mozilla Calendar V1.1//EN\n" +
		"BEGIN:VEVENT\n" +
		"CREATED:2020216T152600Z\n" +
		"LAST-MODIFIED:20200216T152600Z\n" +
		"DTSTAMP:20070216T152600Z\n" +
		"UID:6bcc834e-791f-4054-bfc1-13d698567c94\n" +
		"SUMMARY:Cancun 9AM Mon\n" +
		"RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=MO\n" +
		"DTSTART;TZID=/America/Cancun:20201221T090000\n" +
		"DTEND;TZID=/America/Cancun;X-FOO=bar:20201221T100000\n" +
		"END:VEVENT\n" +
		"END:VCALENDAR\n";
		assertEquals(expected, actual);

		List<List<ParseWarning>> warnings = new ArrayList<List<ParseWarning>>();
		Biweekly.parse(actual).warnings(warnings).first();
		assertParseWarnings(warnings.get(0), 17); //there should only be 1 warning (for CREATED property)
	}
}
