package biweekly.issues;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.property.DateStart;
import biweekly.property.RecurrenceRule;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/pull/63"
 */
public class PR63 {
	@Test
	public void pr63() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("America/New_York");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date until = df.parse("2016-12-23T09:00:00");
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).until(until).build();

		Date start = df.parse("2016-12-19T09:00:00");
		DateIterator it = recur.getDateIterator(start, TimeZone.getDefault());

		List<Date> actual = new ArrayList<Date>();
		while (it.hasNext()) {
			Date startDate = it.next();
			actual.add(startDate);
		}

		List<Date> expected = Arrays.asList( //@formatter:off
			df.parse("2016-12-19T09:00:00"),
			df.parse("2016-12-20T09:00:00"),
			df.parse("2016-12-21T09:00:00"),
			df.parse("2016-12-22T09:00:00"),
			df.parse("2016-12-23T09:00:00")
		); //@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void pr63_original() throws Exception {
		ICalendar ical = Biweekly.parse(PR63.class.getResourceAsStream("pr63.ical")).first();
		TimezoneInfo tzInfo = ical.getTimezoneInfo();
		VEvent vEvent = ical.getEvents().get(0);

		RecurrenceRule recurrenceRule = vEvent.getRecurrenceRule();
		if (recurrenceRule != null) {
			DateStart dateStart = vEvent.getDateStart();

			TimeZone timezone;
			if (tzInfo.isFloating(dateStart)) {
				timezone = TimeZone.getDefault();
			} else {
				TimezoneAssignment dtstartTimezone = tzInfo.getTimezone(dateStart);
				timezone = (dtstartTimezone == null) ? TimeZone.getTimeZone("UTC") : dtstartTimezone.getTimeZone();
			}
			DateIterator iterator = vEvent.getDateIterator(timezone);

			List<Date> actual = new ArrayList<Date>();
			while (iterator.hasNext()) {
				Date startDate = iterator.next();
				actual.add(startDate);
			}
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
			df.setTimeZone(timezone);
			
			List<Date> expected = Arrays.asList( //@formatter:off
				df.parse("2016-12-19T09:00:00"),
				df.parse("2016-12-20T09:00:00"),
				df.parse("2016-12-21T09:00:00"),
				df.parse("2016-12-22T09:00:00"),
				df.parse("2016-12-23T09:00:00")
			); //@formatter:on

			assertEquals(expected, actual);
		} else {
			fail();
		}
	}
}
