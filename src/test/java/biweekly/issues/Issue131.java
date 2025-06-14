package biweekly.issues;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import biweekly.component.VEvent;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/131"
 */
public class Issue131 {
	@Rule
	public final DefaultTimezoneRule rule = new DefaultTimezoneRule("America/Chicago");

	@Test
	@Ignore
	public void test() throws Exception {
		Date start = date(2024, 1, 1, 0, 0, 0);
		Recurrence rrule = new Recurrence.Builder(Frequency.HOURLY).build();

		VEvent event = new VEvent();
		event.setDateStart(start);
		event.setRecurrenceRule(rrule);

		DateIterator it = event.getDateIterator(TimeZone.getDefault());
		it.advanceTo(date(2024, 1, 31, 18, 0, 0));

		Date actual = it.next(); //actual: "2024-02-01 00:00"
		Date expected = date(2024, 1, 31, 19, 0, 0);
		assertEquals(expected, actual);
	}
}
