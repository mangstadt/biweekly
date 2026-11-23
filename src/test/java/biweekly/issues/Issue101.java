package biweekly.issues;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/101"
 */
class Issue101 {
	@RegisterExtension
	public static final DefaultTimezoneRule defaultTimezoneRule = new DefaultTimezoneRule("CET");

	@Test
	void test() throws Exception {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		ICalendar calendar = Biweekly.parse(getClass().getResourceAsStream("issue101.ics")).first();

		List<Date> actual = new ArrayList<>();
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
