package biweekly.issues;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/**
 * @author Michael Angstadt
 * @see "https://github.com/mangstadt/biweekly/issues/69"
 */
public class Issue69 {
	//original test case
	@Test
	public void lesser_year__greater_month__greater_date() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2016-11-27T00:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-04T02:00:00", df.format(recurrence));
	}
	
	@Test
	public void lesser_year__lesser_month__greater_date() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2016-02-27T00:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-04T02:00:00", df.format(recurrence));
	}
	
	@Test
	public void lesser_year__greater_month__lesser_date() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2016-11-02T00:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-04T02:00:00", df.format(recurrence));
	}
	
	@Test
	public void lesser_year__lesser_month__lesser_date() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2016-02-02T00:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-04T02:00:00", df.format(recurrence));
	}
	
	@Test
	public void equal_year__lesser_month__greater_date() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2017-02-27T00:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-04T02:00:00", df.format(recurrence));
	}
	
	@Test
	public void equal_year__lesser_month__lesser_date() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2017-02-02T00:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-04T02:00:00", df.format(recurrence));
	}
	
	@Test
	public void equal_year__equal_month__lesser_date() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2017-04-02T00:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-04T02:00:00", df.format(recurrence));
	}
	
	@Test
	public void equal_year__equal_month__equal_date() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2017-04-04T00:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-04T02:00:00", df.format(recurrence));
	}
	
	@Test
	public void equal_year__equal_month__equal_date_equal_hour() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2017-04-04T1:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-04T02:00:00", df.format(recurrence));
	}
	
	@Test
	public void advanceto_lessthan_start() throws Exception {
		TimeZone timezone = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		df.setTimeZone(timezone);

		Date awardedOn = df.parse("2017-04-04T01:19:24");
		Date periodStart = df.parse("2017-04-05T2:00:00");

		Recurrence rec = new Recurrence.Builder(Frequency.HOURLY).interval(1).build();
		DateIterator iter = rec.getDateIterator(periodStart, timezone);
		iter.advanceTo(awardedOn);

		Date recurrence = null;
		if (iter.hasNext()) {
			recurrence = iter.next();
		}

		assertEquals("2017-04-05T02:00:00", df.format(recurrence));
	}
}
