package biweekly.issues;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/85"
 */
public class Issue85 {
	@Test
	public void test() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		String files[] = { "issue85.ics", "issue85-1980.ics" };
		for (String file : files) {
			ICalendar ical = Biweekly.parse(Issue85.class.getResourceAsStream(file)).first();
			VEvent event = ical.getEvents().get(0);

			assertEquals("2018-07-22T10:00:00", df.format(event.getDateStart().getValue()));
			assertEquals("2018-07-22T11:00:00", df.format(event.getDateEnd().getValue()));
		}
	}
}
