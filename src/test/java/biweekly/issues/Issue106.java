package biweekly.issues;

import java.io.IOException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import biweekly.Biweekly;
import biweekly.component.VEvent;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDate;

public class Issue106 {

	@Test
	public void givenBadTzid_whenParse_thenFallbackToConfiguredDefault() throws IOException, ParseException {
		DateTimeComponents.assignGlobally(TimeZone.getTimeZone("America/Los_Angeles"));
		VEvent vEvent = Biweekly.parse(getClass().getResourceAsStream("issue106.ics")).first().getEvents().get(0);
		DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		Date parsed = df.parse("20201221T180000"); //should be 6pm at Los Angeles Time
		ICalDate value = vEvent.getDateStart().getValue();
		DateTimeComponents.clearGlobalTimeZone();
		Assert.assertEquals(parsed.getTime(), value.getTime());
	}
}
