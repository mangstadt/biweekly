package biweekly.issues;

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Rule;
import org.junit.Test;

import biweekly.Biweekly;
import biweekly.component.VEvent;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/122"
 */
public class Issue122 {
	@Rule
	public final DefaultTimezoneRule defaultTimezoneRule = new DefaultTimezoneRule("America/New_York");

	/**
	 * @see "https://icalendar.org/iCalendar-RFC-5545/3-8-5-3-recurrence-rule.html"
	 */
	@Test
	public void example_cited_in_issue() throws Exception {
		TimeZone tz = TimeZone.getDefault();

		//@formatter:off
		String iCalString =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VEVENT\r\n" +
				"DTSTART;TZID=America/New_York:19970904T090000\r\n" +
				"RRULE:FREQ=MONTHLY;COUNT=3;BYDAY=TU,WE,TH;BYSETPOS=3\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		//@formatter:off
		List<Date> expected = Arrays.asList(
			date("1997-09-04 09:00"),
			date("1997-10-07 09:00"),
			date("1997-11-06 09:00")
		);
		//@formatter:on

		VEvent vEvent = Biweekly.parse(iCalString).first().getEvents().get(0);
		DateIterator iterator = vEvent.getRecurrenceRule().getDateIterator(vEvent.getDateStart().getValue(), tz);
		List<Date> actual = new ArrayList<Date>();
		while (iterator.hasNext()) {
			actual.add(iterator.next());
		}

		assertEquals(expected, actual);
	}

	/**
	 * @see "https://icalendar.org/iCalendar-RFC-5545/3-8-5-3-recurrence-rule.html"
	 */
	@Test
	public void other_bysetpos_example() throws Exception {
		TimeZone tz = TimeZone.getDefault();

		//@formatter:off
		String iCalString =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VEVENT\r\n" +
				"DTSTART;TZID=America/New_York:19970929T090000\r\n" +
				"RRULE:FREQ=MONTHLY;BYDAY=MO,TU,WE,TH,FR;BYSETPOS=-2\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		//@formatter:off
		List<Date> expected = Arrays.asList(
			date("1997-09-29 09:00"),
			date("1997-10-30 09:00"),
			date("1997-11-27 09:00"),
			date("1997-12-30 09:00"),
			date("1998-01-29 09:00"),
			date("1998-02-26 09:00"),
			date("1998-03-30 09:00")
		);
		//@formatter:on

		VEvent vEvent = Biweekly.parse(iCalString).first().getEvents().get(0);
		DateIterator iterator = vEvent.getRecurrenceRule().getDateIterator(vEvent.getDateStart().getValue(), tz);
		List<Date> actual = new ArrayList<Date>();
		for (int i = 0; i < expected.size(); i++) {
			actual.add(iterator.next());
		}

		assertEquals(expected, actual);
	}

	private static Date date(String s) throws ParseException {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(s);
	}
}
