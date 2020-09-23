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

import biweekly.component.VEvent;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/99"
 */
public class Issue99 {
	@Test
	public void issue99() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("America/New_York");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		VEvent event = new VEvent();

		Date start = df.parse("2020-03-01T12:00:00");
		event.setDateStart(start);

		Date until = df.parse("2020-03-05T12:01:00");
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).until(until).build();
		event.setRecurrenceRule(recur);

		List<Date> actual = new ArrayList<Date>();
		DateIterator iterator = event.getDateIterator(timezone);
		while (iterator.hasNext()) {
			Date date = iterator.next();
			actual.add(date);
		}

		List<Date> expected = Arrays.asList( //@formatter:off
			df.parse("2020-03-01T12:00:00"),
			df.parse("2020-03-02T12:00:00"),
			df.parse("2020-03-03T12:00:00"),
			df.parse("2020-03-04T12:00:00"),
			df.parse("2020-03-05T12:00:00")
		); //@formatter:on

		assertEquals(expected, actual);
	}
}
