package biweekly.util;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.component.VEvent;
import biweekly.property.ExceptionDates;
import biweekly.property.ExceptionRule;
import biweekly.property.RecurrenceDates;

/*
 Copyright (c) 2013-2023, Michael Angstadt
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
public class Google2445UtilsTest {
	@Test
	public void getDateIterator_empty() {
		VEvent event = new VEvent();

		List<Date> expectedList = Arrays.asList();
		assertIteratorEquals(expectedList, Google2445Utils.getDateIterator(event, TimeZone.getTimeZone("UTC")));
	}

	@Test
	public void getDateIterator_start_date_only() {
		VEvent event = new VEvent();
		event.setDateStart(date(2016, 3, 25, 14, 0, 0));

		//@formatter:off
		List<Date> expectedList = Arrays.asList(
			date(2016, 3, 25, 14, 0, 0)
		);
		//@formatter:on

		assertIteratorEquals(expectedList, Google2445Utils.getDateIterator(event, TimeZone.getTimeZone("UTC")));
	}

	@Test
	public void getDateIterator_start_date_has_no_time() {
		VEvent event = new VEvent();

		event.setDateStart(date(2016, 3, 25), false);
		event.setRecurrenceRule(new Recurrence.Builder(Frequency.DAILY).count(3).build());

		//@formatter:off
		List<Date> expectedList = Arrays.asList(
			date(2016, 3, 25, 0, 0, 0),
			date(2016, 3, 26, 0, 0, 0),
			date(2016, 3, 27, 0, 0, 0)
		);
		//@formatter:on

		assertIteratorEquals(expectedList, Google2445Utils.getDateIterator(event, null));
	}

	@Test
	public void getDateIterator() {
		TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");

		VEvent event = new VEvent();

		event.setDateStart(date(2016, 3, 25, 14, 0, 0, tz));

		event.setRecurrenceRule(new Recurrence.Builder(Frequency.DAILY).count(10).build());

		RecurrenceDates rdate = new RecurrenceDates();
		rdate.getDates().add(new ICalDate(date(2016, 3, 26, 20, 0, 0, tz)));
		rdate.getDates().add(new ICalDate(date(2016, 3, 27, 20, 0, 0, tz)));
		event.addRecurrenceDates(rdate);

		ExceptionDates exdate = new ExceptionDates();
		exdate.getValues().add(new ICalDate(date(2016, 3, 27, 14, 0, 0, tz)));
		event.addExceptionDates(exdate);

		ExceptionRule exrule = new ExceptionRule(new Recurrence.Builder(Frequency.WEEKLY).count(2).build());
		event.addProperty(exrule);

		//@formatter:off
		List<Date> expectedList = Arrays.asList(
			date(2016, 3, 26, 14, 0, 0, tz),
			date(2016, 3, 26, 20, 0, 0, tz),
			date(2016, 3, 27, 20, 0, 0, tz),
			date(2016, 3, 28, 14, 0, 0, tz),
			date(2016, 3, 29, 14, 0, 0, tz),
			date(2016, 3, 30, 14, 0, 0, tz),
			date(2016, 3, 31, 14, 0, 0, tz),
			date(2016, 4, 2, 14, 0, 0, tz),
			date(2016, 4, 3, 14, 0, 0, tz)
		);
		//@formatter:on

		assertIteratorEquals(expectedList, Google2445Utils.getDateIterator(event, TimeZone.getTimeZone("UTC")));
	}

	private static <T> void assertIteratorEquals(List<T> expectedList, Iterator<T> actualIt) {
		Iterator<T> expectedIt = expectedList.iterator();
		while (expectedIt.hasNext()) {
			T actual = actualIt.next();
			T expected = expectedIt.next();
			assertEquals(expected, actual);
		}
		assertFalse(actualIt.hasNext());
	}
}
