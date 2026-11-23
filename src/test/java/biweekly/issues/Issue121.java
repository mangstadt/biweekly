package biweekly.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.TimeZone;

import org.junit.jupiter.api.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/121"
 */
class Issue121 {
	@Test
	void test() throws Exception {
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
		VEvent event = ical.getEvents().get(0);
		TimeZone tz = TimeZone.getDefault();

		AssertionError e = assertThrows(AssertionError.class, () -> event.getDateIterator(tz));
		assertEquals("96", e.getMessage());
	}
}
