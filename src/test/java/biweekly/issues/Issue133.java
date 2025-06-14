package biweekly.issues;

import java.text.DateFormat;
import java.util.TimeZone;

import org.junit.Ignore;
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
	@Test
	@Ignore
	public void test() throws Exception {
		ICalendar ical = Biweekly.parse(getClass().getResourceAsStream("issue133.ics")).first();

		VEvent event = ical.getEvents().get(0);
		DateStart start = event.getDateStart();

		TimeZone tz = ical.getTimezoneInfo().getTimezone(start).getTimeZone();
		DateFormat df = DateFormat.getDateTimeInstance();
		df.setTimeZone(tz);
		System.out.println(df.format(start.getValue()));
	}
}
