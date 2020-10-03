package biweekly.issues;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static org.junit.Assert.assertEquals;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/101"
 */
public class Issue101 {
	@Rule
	public final DefaultTimezoneRule defaultTimezoneRule = new DefaultTimezoneRule("CET");

	@Test
	public void test() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		ICalendar calendar = Biweekly.parse(getClass().getResourceAsStream("issue101.ics")).first();

		List<Date> actual = new ArrayList<Date>();
		for (VEvent evt: calendar.getEvents()) {
			DateIterator it = evt.getDateIterator(TimeZone.getDefault());
			while (it.hasNext()) {
				Date current = it.next();
				actual.add(current);
			}
		}

		List<Date> expected = Arrays.asList( //@formatter:off
			df.parse("2020-10-04T17:00:00"),
			df.parse("2020-10-05T17:00:00"),
			df.parse("2020-10-06T17:00:00"),
			df.parse("2020-10-07T17:00:00"),
			df.parse("2020-10-08T17:00:00"),
			df.parse("2020-10-09T17:00:00")
		); //@formatter:on

		assertEquals(expected, actual);
	}
}
