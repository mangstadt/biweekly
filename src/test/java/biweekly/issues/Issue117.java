package biweekly.issues;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/117"
 */
public class Issue117 {
	@Test
	public void test() {
		ICalendar ical1 = ical("RRULE:FREQ=WEEKLY;INTERVAL=1;WKST=SU;BYDAY=TH,FR");
		ICalendar ical2 = ical("RRULE:FREQ=WEEKLY;INTERVAL=1;BYDAY=TH,FR;WKST=SU");
		
		TimeZone tz = TimeZone.getDefault();
		DateIterator it1 = ical1.getEvents().get(0).getDateIterator(tz);
		DateIterator it2 = ical2.getEvents().get(0).getDateIterator(tz);
		
		for (int i = 0; i < 20; i++) {
			Date d1 = it1.next();
			Date d2 = it2.next();
			assertEquals(d1, d2);
			System.out.println(d1);
		}
	}
	
	private ICalendar ical(String rruleString) {
		//@formatter:off
		String str =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2.0\r\n" +
		"PRODID:-//Microsoft Corporation//Outlook 14.0 MIMEDIR//EN\r\n" +
		"BEGIN:VEVENT\r\n" +
		"DTSTART:20220606T110000Z\r\n" +
		rruleString + "\r\n" +
		"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on
		
		return Biweekly.parse(str).first();
	}
}
