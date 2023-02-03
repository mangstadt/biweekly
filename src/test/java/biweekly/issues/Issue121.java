package biweekly.issues;

import static org.junit.Assert.assertEquals;

import java.util.TimeZone;

import org.junit.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/121"
 */
public class Issue121 {
	@Test
	public void test() throws Exception {
		//@formatter:off
		String input =
		"BEGIN:VCALENDAR\n" +
		"VERSION:2.0\n" +
		"BEGIN:VEVENT\n" +
		"RRULE:FREQ=WEEKLY;INTERVAL=1;BYMONTH=96\n" +
		"DTSTART:20209621T090000Z\n" + //invalid month value: 96
		"DTEND:20209621T100000Z\n" +  //invalid month value: 96
		"END:VEVENT\n" +
		"END:VCALENDAR\n";
		//@formatter:on

		ICalendar ical = Biweekly.parse(input).first();
		try {
			ical.getEvents().get(0).getDateIterator(TimeZone.getDefault());
		} catch (AssertionError e) {
			assertEquals("96", e.getMessage());
		}
	}
}
