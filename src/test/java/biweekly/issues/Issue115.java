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
 * @see "https://github.com/mangstadt/biweekly/issues/115"
 */
public class Issue115 {
	@Test
	public void test1() throws Exception {
		ICalendar ical = Biweekly.parse(getClass().getResourceAsStream("issue115-1.ics")).first();

		/*
		 * The event comes before the last Sunday in October, so it should
		 * be in daylight time (offset of +02:00).
		 */
		VEvent event = ical.getEvents().get(0);

		Date expected = date(2022, 10, 29, 8, 0, 0, "+0200");
		Date actual = event.getDateStart().getValue();
		assertEquals(expected, actual);

		expected = date(2022, 10, 29, 8, 30, 0, "+0200");
		actual = event.getDateEnd().getValue();
		assertEquals(expected, actual);
	}

	@Test
	public void test2() throws Exception {
		ICalendar ical = Biweekly.parse(getClass().getResourceAsStream("issue115-2.ics")).first();

		/*
		 * The event comes after the last Sunday in October, so it should
		 * be in standard time (offset of +01:00).
		 */
		VEvent event = ical.getEvents().get(0);

		Date expected = date(2022, 10, 31, 8, 0, 0, "+0100");
		System.out.println(expected);
		Date actual = event.getDateStart().getValue();
		assertEquals(expected, actual);

		expected = date(2022, 10, 31, 8, 30, 0, "+0100");
		actual = event.getDateEnd().getValue();
		assertEquals(expected, actual);
	}
}
