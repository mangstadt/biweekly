package biweekly.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.util.Frequency;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/*
 Copyright (c) 2013-2016, Michael Angstadt
 All rights reserved.

 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 

 1. Redistributions of source code must retain the above copyright notice, this
 list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
 this list of conditions and the following disclaimer in the documentation
 and/or other materials provided with the distribution. 

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @author Michael Angstadt
 */
public class RecurrencePropertyTest {
	@Test
	public void getDateIterator_empty() {
		RecurrenceProperty property = new RecurrenceProperty((Recurrence) null);
		Date start = date("2014-11-22 10:00:00");
		DateIterator it = property.getDateIterator(start, TimeZone.getTimeZone("UTC"));
		assertFalse(it.hasNext());
	}

	@Test
	public void getDateIterator() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).count(5).build();
		Date start = date("2014-11-22 10:00:00");
		RecurrenceProperty property = new RecurrenceProperty(recur);

		//@formatter:off
		List<Date> expected = Arrays.asList(
			date("2014-11-22 10:00:00"),
			date("2014-11-23 10:00:00"),
			date("2014-11-24 10:00:00"),
			date("2014-11-25 10:00:00"),
			date("2014-11-26 10:00:00")
		);
		//@formatter:on

		List<Date> actual = new ArrayList<Date>();
		DateIterator it = property.getDateIterator(start, TimeZone.getTimeZone("UTC"));
		while (it.hasNext()) {
			actual.add(it.next());
		}

		assertEquals(expected, actual);
	}

	@Test
	public void getDateIterator_gap_hour() {
		TimeZone pacificTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).interval(1).count(3).build();
		Date start = date("2016-03-06 02:30:00", pacificTimeZone);
		RecurrenceProperty property = new RecurrenceProperty(recur);

		//@formatter:off
		List<Date> expected = Arrays.asList(
			date("2016-03-06 02:30:00", pacificTimeZone),
			date("2016-03-13 03:30:00", pacificTimeZone),
			date("2016-03-20 02:30:00", pacificTimeZone)
		);
		//@formatter:on

		List<Date> actual = new ArrayList<Date>();
		DateIterator it = property.getDateIterator(start, pacificTimeZone);
		while (it.hasNext()) {
			actual.add(it.next());
		}

		assertEquals(expected, actual);
	}

	@Test
	public void getDateIterator_overlap_hour() {
		TimeZone pacificTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).interval(1).count(3).build();
		Date start = date("2016-10-30 01:30:00", pacificTimeZone);
		RecurrenceProperty property = new RecurrenceProperty(recur);

		//@formatter:off
		// First date will be in PDT while the second and third are PST.
		List<Date> expected = Arrays.asList(
				date("2016-10-30 01:30:00", pacificTimeZone),
				date("2016-11-06 01:30:00", pacificTimeZone),
				date("2016-11-13 01:30:00", pacificTimeZone)
		);
		//@formatter:on

		List<Date> actual = new ArrayList<Date>();
		DateIterator it = property.getDateIterator(start, pacificTimeZone);
		while (it.hasNext()) {
			actual.add(it.next());
		}

		assertEquals(expected, actual);
	}

	@Test
	public void validate() {
		RecurrenceProperty property = new RecurrenceProperty((Recurrence) null);
		assertValidate(property).run(26);

		property = new RecurrenceProperty(new Recurrence.Builder((Frequency) null).build());
		assertValidate(property).run(30);

		property = new RecurrenceProperty(new Recurrence.Builder((Frequency) null).until(new ICalDate()).count(1).build());
		assertValidate(property).run(30, 31);

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).until(new ICalDate()).count(1).build());
		assertValidate(property).run(31);

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).build());
		assertValidate(property).run();
	}

	@Test
	public void validate_xrules() {
		RecurrenceProperty property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).xrule("foo", "bar").build());
		assertValidate(property).versions(V1_0).run(new Integer[] { null });

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).xrule("foo", "bar").build());
		assertValidate(property).versions(V2_0_DEPRECATED).run();

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).xrule("foo", "bar").build());
		assertValidate(property).versions(V2_0).run(32);
	}

	@Test
	public void validate_bysetpos() {
		RecurrenceProperty property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).bySetPos(-1).build());
		assertValidate(property).versions(V1_0).run(new Integer[] { null });

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).bySetPos(-1).build());
		assertValidate(property).versions(V2_0_DEPRECATED, V2_0).run();
	}

	@Test
	public void validate_secondly_frequency() {
		RecurrenceProperty property = new RecurrenceProperty(new Recurrence.Builder(Frequency.SECONDLY).build());
		assertValidate(property).versions(V1_0).run(new Integer[] { null });

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.SECONDLY).build());
		assertValidate(property).versions(V2_0_DEPRECATED, V2_0).run();
	}

	@Test
	public void advanceDateIterator_UTC() {
		Date start = date("2014-11-22 10:00:00");
		Date advanceTo = date("2014-11-24 10:00:00");

		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).count(5).build();
		RecurrenceProperty property = new RecurrenceProperty(recur);
		DateIterator it = property.getDateIterator(start, TimeZone.getTimeZone("UTC"));
		it.advanceTo(advanceTo);

		//@formatter:off
		List<Date> expected = Arrays.asList(
			date("2014-11-24 10:00:00"),
			date("2014-11-25 10:00:00"),
			date("2014-11-26 10:00:00")
		);
		//@formatter:on

		List<Date> actual = new ArrayList<Date>();
		while (it.hasNext()) {
			actual.add(it.next());
		}

		assertEquals(expected, actual);
	}

	@Test
	public void advanceDateIterator_negativeTimeZoneOffset() {
		TimeZone pacificTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
		Date start = date("2016-07-01 00:01:00", pacificTimeZone);
		Date advanceTo = date("2016-07-01 06:59:00", pacificTimeZone); //advance iterator to time 1-minute less than the time zone offset

		//Note: date-time used for advancement must first be converted to UTC
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).count(4).build();
		RecurrenceProperty recurrenceProperty = new RecurrenceProperty(recur);
		DateIterator it = recurrenceProperty.getDateIterator(start, pacificTimeZone);
		it.advanceTo(advanceTo);

		//@formatter:off
		//First occurrence is skipped; the last three should be returned
		List<Date> expected = Arrays.asList(
			date("2016-07-02 00:01:00", pacificTimeZone),
			date("2016-07-03 00:01:00", pacificTimeZone),
			date("2016-07-04 00:01:00", pacificTimeZone)
		);
		//@formatter:on

		List<Date> actual = new ArrayList<Date>();
		while (it.hasNext()) {
			actual.add(it.next());
		}

		assertEquals(expected, actual);
	}

	@Test
	public void advanceDateIterator_positiveTimeZoneOffset() {
		TimeZone singapore = TimeZone.getTimeZone("Asia/Singapore");
		Date start = date("2016-07-01 00:01:00", singapore);
		Date advanceTo = date("2016-07-01 00:05:00", singapore); //advance iterator 5-minutes from start time

		//Note: date-time used for advancement must first be converted to UTC
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).count(4).build();
		RecurrenceProperty recurrenceProperty = new RecurrenceProperty(recur);
		DateIterator it = recurrenceProperty.getDateIterator(start, singapore);
		it.advanceTo(advanceTo);

		//@formatter:off
		//First occurrence is skipped; the last three should be returned
		List<Date> expected = Arrays.asList(
			date("2016-07-02 00:01:00", singapore),
			date("2016-07-03 00:01:00", singapore),
			date("2016-07-04 00:01:00", singapore)
		);
		//@formatter:on

		List<Date> actual = new ArrayList<Date>();
		while (it.hasNext()) {
			actual.add(it.next());
		}

		assertEquals(expected, actual);
	}
}
