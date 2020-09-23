package biweekly.issues;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/67"
 */
public class Issue67 {
	/*
	 * These tests failed for user Dupplicate under the default timezone "CET"
	 * RRuleIteratorImplTest#dailyUntilDec4
	 * RRuleIteratorImplTest#advanceTo
	 */

	@Test
	public void carolinelane10() throws Exception {
		ICalendar ical = Biweekly.parseJson(Issue67.class.getResourceAsStream("issue67.json")).first();
		VEvent vEvent = ical.getEvents().get(0);

		TimeZone eventTimezone = TimeZone.getTimeZone("America/New_York");
		DateIterator iterator = vEvent.getDateIterator(eventTimezone);

		List<Date> actual = new ArrayList<Date>();
		while (iterator.hasNext()) {
			Date startDate = iterator.next();
			actual.add(startDate);
		}

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(eventTimezone);

		List<Date> expected = Arrays.asList( //@formatter:off
			df.parse("2017-03-22T12:00:00"),
			df.parse("2017-03-29T12:00:00")
		); //@formatter:on

		assertEquals(expected, actual);
	}
}
