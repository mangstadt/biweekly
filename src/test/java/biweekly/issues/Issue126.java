package biweekly.issues;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/126"
 */
public class Issue126 {
	@Test
	public void test() throws Exception {
		ICalendar ical = Biweekly.parse(getClass().getResourceAsStream("issue126.ics")).first();

		/*
		 * The first event comes after the last Sunday in October, so it should
		 * be in standard time (offset of +01:00).
		 */
		{
			VEvent event = ical.getEvents().get(0);

			Date expected = date(2023, 11, 22, 9, 30, 0, "+0100");
			Date actual = event.getDateStart().getValue();
			assertEquals(expected, actual);

			expected = date(2023, 11, 22, 12, 0, 0, "+0100");
			actual = event.getDateEnd().getValue();
			assertEquals(expected, actual);
		}
		
		/*
		 * The second event comes after the last Sunday in March, so it should
		 * be in daylight time (offset of +02:00).
		 */
		{
			VEvent event = ical.getEvents().get(1);

			Date expected = date(2023, 4, 22, 9, 30, 0, "+0200");
			Date actual = event.getDateStart().getValue();
			assertEquals(expected, actual);

			expected = date(2023, 4, 22, 12, 0, 0, "+0200");
			actual = event.getDateEnd().getValue();
			assertEquals(expected, actual);
		}
	}
}
