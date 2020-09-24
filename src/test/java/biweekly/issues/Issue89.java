package biweekly.issues;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.ICalVersion;
import biweekly.io.ParseContext;
import biweekly.io.scribe.property.RecurrenceRuleScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.RecurrenceRule;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/89"
 */
public class Issue89 {
	@Test
	public void givenRRULE_whenAdvanceTo_ensureNextIsNextOccurrence() throws Exception {
		TimeZone utc = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(utc);

		// given
		final String rrule = "FREQ=MONTHLY;BYSETPOS=3;BYDAY=MO"; // every 3rd Monday of the month
		final Date startDate = df.parse("2019-01-01T00:00:00"); // starting in 2019

		// when
		RecurrenceRuleScribe scribe = new RecurrenceRuleScribe();
		ParseContext context = new ParseContext();
		context.setVersion(ICalVersion.V2_0);
		RecurrenceRule recurrenceRule = scribe.parseText(rrule, null, new ICalParameters(), context);
		DateIterator iter = recurrenceRule.getDateIterator(startDate, utc);

		Date evalDate = df.parse("2019-08-01T00:00:00");
		iter.advanceTo(evalDate);
		Date nextOccurrence = iter.next();

		// ensure
		assertEquals(df.parse("2019-08-19T00:00:00Z"), nextOccurrence);
	}
}
