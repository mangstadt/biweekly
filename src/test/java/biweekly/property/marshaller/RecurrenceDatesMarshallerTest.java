package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.RecurrenceDates;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
import biweekly.util.Duration;
import biweekly.util.Period;

/*
 Copyright (c) 2013, Michael Angstadt
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
public class RecurrenceDatesMarshallerTest {
	private final RecurrenceDatesMarshaller marshaller = new RecurrenceDatesMarshaller();

	private final Date start;
	{
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.clear();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DATE, 11);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 43);
		c.set(Calendar.SECOND, 2);
		start = c.getTime();
	}

	private final Date end;
	{
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.setTime(start);
		c.add(Calendar.HOUR, 2);
		end = c.getTime();
	}

	private final Duration duration = new Duration.Builder().hours(2).build();

	@Test
	public void prepareParameters_periods() {
		List<Period> periods = Arrays.asList(new Period(start, end), new Period(start, duration));
		RecurrenceDates prop = new RecurrenceDates(periods);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertEquals(Value.PERIOD, params.getValue());
	}

	@Test
	public void prepareParameters_dates_with_time() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, true);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertNull(params.getValue());
	}

	@Test
	public void prepareParameters_dates_without_time() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, false);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertEquals(Value.DATE, params.getValue());
	}

	@Test
	public void writeText_multiple_periods() {
		List<Period> periods = Arrays.asList(new Period(start, end), new Period(start, duration));
		RecurrenceDates prop = new RecurrenceDates(periods);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z/20130611T154302Z,20130611T134302Z/PT2H";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_one_period() {
		List<Period> periods = Arrays.asList(new Period(start, end));
		RecurrenceDates prop = new RecurrenceDates(periods);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z/20130611T154302Z";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_multiple_datetimes() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, true);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z,20130611T154302Z";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_one_datetime() {
		List<Date> dates = Arrays.asList(start);
		RecurrenceDates prop = new RecurrenceDates(dates, true);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_multiple_dates() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, false);

		String actual = marshaller.writeText(prop);

		String expected = "20130611,20130611";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_one_date() {
		List<Date> dates = Arrays.asList(start);
		RecurrenceDates prop = new RecurrenceDates(dates, false);

		String actual = marshaller.writeText(prop);

		String expected = "20130611";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_empty() {
		List<Date> dates = Arrays.asList();
		RecurrenceDates prop = new RecurrenceDates(dates, false);

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText_periods() {
		String value = "20130611T134302Z/20130611T154302Z,20130611T134302Z/PT2H";
		ICalParameters params = new ICalParameters();
		params.setValue(Value.PERIOD);

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		Iterator<Period> it = result.getValue().getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		period = it.next();
		assertEquals(start, period.getStartDate());
		assertNull(period.getEndDate());
		assertEquals(duration, period.getDuration());

		assertFalse(it.hasNext());

		assertNull(result.getValue().getDates());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_invalid_start_date() {
		String value = "20130611T134302Z/20130611T154302Z,invalid/PT2H";
		ICalParameters params = new ICalParameters();
		params.setValue(Value.PERIOD);

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		Iterator<Period> it = result.getValue().getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertNull(result.getValue().getDates());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_invalid_end_date() {
		String value = "20130611T134302Z/20130611T154302Z,20130611T134302Z/invalid";
		ICalParameters params = new ICalParameters();
		params.setValue(Value.PERIOD);

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		Iterator<Period> it = result.getValue().getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertNull(result.getValue().getDates());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_invalid_no_end_date() {
		String value = "20130611T134302Z/20130611T154302Z,20130611T134302Z";
		ICalParameters params = new ICalParameters();
		params.setValue(Value.PERIOD);

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		Iterator<Period> it = result.getValue().getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertNull(result.getValue().getDates());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_datetimes() {
		String value = "20130611T134302Z,20130611T154302Z";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		Iterator<Date> it = result.getValue().getDates().iterator();

		Date date = it.next();
		assertEquals(start, date);

		date = it.next();
		assertEquals(end, date);

		assertFalse(it.hasNext());

		assertNull(result.getValue().getPeriods());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_invalid_datetime() {
		String value = "20130611T134302Z,invalid,20130611T154302Z";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		Iterator<Date> it = result.getValue().getDates().iterator();

		Date date = it.next();
		assertEquals(start, date);

		date = it.next();
		assertEquals(end, date);

		assertFalse(it.hasNext());

		assertNull(result.getValue().getPeriods());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_dates() throws Exception {
		String value = "20130611,20130612";
		ICalParameters params = new ICalParameters();
		params.setValue(Value.DATE);

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		Iterator<Date> it = result.getValue().getDates().iterator();

		Date date = it.next();
		assertEquals(new SimpleDateFormat("yyyyMMdd").parse("20130611"), date);

		date = it.next();
		assertEquals(new SimpleDateFormat("yyyyMMdd").parse("20130612"), date);

		assertFalse(it.hasNext());

		assertNull(result.getValue().getPeriods());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_invalid_dates() throws Exception {
		String value = "20130611,invalid,20130612";
		ICalParameters params = new ICalParameters();
		params.setValue(Value.DATE);

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		Iterator<Date> it = result.getValue().getDates().iterator();

		Date date = it.next();
		assertEquals(new SimpleDateFormat("yyyyMMdd").parse("20130611"), date);

		date = it.next();
		assertEquals(new SimpleDateFormat("yyyyMMdd").parse("20130612"), date);

		assertFalse(it.hasNext());

		assertNull(result.getValue().getPeriods());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_empty_periods() {
		String value = "";
		ICalParameters params = new ICalParameters();
		params.setValue(Value.PERIOD);

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		assertEquals(0, result.getValue().getPeriods().size());
		assertNull(result.getValue().getDates());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_empty_dates() {
		String value = "";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, params);

		assertEquals(0, result.getValue().getDates().size());
		assertNull(result.getValue().getPeriods());
		assertWarnings(0, result.getWarnings());
	}
}
