package biweekly.util;

import static biweekly.util.TestUtils.assertIterator;
import static biweekly.util.TestUtils.date;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.util.com.google.ical.compat.javautil.DateIterator;

/*
 Copyright (c) 2013-2018, Michael Angstadt
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
public class RecurrenceTest {
	@Test
	public void getDateIterator() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).count(5).build();
		Date start = date("2014-11-22 10:00:00");

		//@formatter:off
		List<Date> expected = Arrays.asList(
			date("2014-11-22 10:00:00"),
			date("2014-11-23 10:00:00"),
			date("2014-11-24 10:00:00"),
			date("2014-11-25 10:00:00"),
			date("2014-11-26 10:00:00")
		);
		//@formatter:on

		DateIterator it = recur.getDateIterator(start, TimeZone.getDefault());
		assertIterator(expected, it);
	}

	@Test
	public void getDateIterator_gap_hour() {
		TimeZone pacificTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).interval(1).count(3).build();
		Date start = date("2016-03-06 02:30:00", pacificTimeZone);

		//@formatter:off
		List<Date> expected = Arrays.asList(
			date("2016-03-06 02:30:00", pacificTimeZone),
			date("2016-03-13 03:30:00", pacificTimeZone),
			date("2016-03-20 02:30:00", pacificTimeZone)
		);
		//@formatter:on

		DateIterator it = recur.getDateIterator(start, pacificTimeZone);
		assertIterator(expected, it);
	}

	@Test
	public void getDateIterator_overlap_hour() {
		TimeZone pacificTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).interval(1).count(3).build();
		Date start = date("2016-10-30 01:30:00", pacificTimeZone);

		//@formatter:off
		//first date will be in PDT while the second and third are PST
		List<Date> expected = Arrays.asList(
			date("2016-10-30 01:30:00", pacificTimeZone),
			date("2016-11-06 01:30:00", pacificTimeZone),
			date("2016-11-13 01:30:00", pacificTimeZone)
		);
		//@formatter:on

		DateIterator it = recur.getDateIterator(start, pacificTimeZone);
		assertIterator(expected, it);
	}

	@Test
	public void advanceDateIterator() {
		Date start = date("2014-11-22 10:00:00");
		Date advanceTo = date("2014-11-24 10:00:00");

		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).count(5).build();
		DateIterator it = recur.getDateIterator(start, TimeZone.getDefault());
		it.advanceTo(advanceTo);

		//@formatter:off
		List<Date> expected = Arrays.asList(
			date("2014-11-24 10:00:00"),
			date("2014-11-25 10:00:00"),
			date("2014-11-26 10:00:00")
		);
		//@formatter:on

		assertIterator(expected, it);
	}

	@Test
	public void advanceDateIterator_negativeTimeZoneOffset() {
		TimeZone pacificTimeZone = TimeZone.getTimeZone("America/Los_Angeles");
		Date start = date("2016-07-01 00:01:00", pacificTimeZone);
		Date advanceTo = date("2016-07-01 06:59:00", pacificTimeZone); //advance iterator to time 1-minute less than the time zone offset

		//Note: date-time used for advancement must first be converted to UTC
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).count(4).build();
		DateIterator it = recur.getDateIterator(start, pacificTimeZone);
		it.advanceTo(advanceTo);

		//@formatter:off
		//first occurrence is skipped; the last three should be returned
		List<Date> expected = Arrays.asList(
			date("2016-07-02 00:01:00", pacificTimeZone),
			date("2016-07-03 00:01:00", pacificTimeZone),
			date("2016-07-04 00:01:00", pacificTimeZone)
		);
		//@formatter:on

		assertIterator(expected, it);
	}

	@Test
	public void advanceDateIterator_positiveTimeZoneOffset() {
		TimeZone singapore = TimeZone.getTimeZone("Asia/Singapore");
		Date start = date("2016-07-01 00:01:00", singapore);
		Date advanceTo = date("2016-07-01 00:05:00", singapore); //advance iterator 5-minutes from start time

		//Note: date-time used for advancement must first be converted to UTC
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).count(4).build();
		DateIterator it = recur.getDateIterator(start, singapore);
		it.advanceTo(advanceTo);

		//@formatter:off
		//first occurrence is skipped; the last three should be returned
		List<Date> expected = Arrays.asList(
			date("2016-07-02 00:01:00", singapore),
			date("2016-07-03 00:01:00", singapore),
			date("2016-07-04 00:01:00", singapore)
		);
		//@formatter:on

		assertIterator(expected, it);
	}

	@Test
	public void copy_xrule_is_mutable() {
		Recurrence recur = new Recurrence.Builder(Frequency.WEEKLY).build();
		new Recurrence.Builder(recur).xrule("NAME", "value").build();
	}
}
