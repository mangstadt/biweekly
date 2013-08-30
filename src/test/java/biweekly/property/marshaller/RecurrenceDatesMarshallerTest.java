package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertDateEquals;
import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
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

	private final Duration duration = Duration.builder().hours(2).build();

	@Test
	public void getDataType_periods() {
		List<Period> periods = Arrays.asList(new Period(start, end), new Period(start, duration));
		RecurrenceDates prop = new RecurrenceDates(periods);

		assertEquals(ICalDataType.PERIOD, marshaller.getDataType(prop));
	}

	@Test
	public void getDataType_dates_with_time() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, true);

		assertEquals(ICalDataType.DATE_TIME, marshaller.getDataType(prop));
	}

	@Test
	public void getDataType_dates_without_time() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, false);

		assertEquals(ICalDataType.DATE, marshaller.getDataType(prop));
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

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.PERIOD, params);

		RecurrenceDates prop = result.getValue();
		Iterator<Period> it = prop.getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		period = it.next();
		assertEquals(start, period.getStartDate());
		assertNull(period.getEndDate());
		assertEquals(duration, period.getDuration());

		assertFalse(it.hasNext());

		assertNull(prop.getDates());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_invalid_start_date() {
		String value = "20130611T134302Z/20130611T154302Z,invalid/PT2H";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.PERIOD, params);

		RecurrenceDates prop = result.getValue();
		Iterator<Period> it = prop.getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertNull(prop.getDates());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_invalid_end_date() {
		String value = "20130611T134302Z/20130611T154302Z,20130611T134302Z/invalid";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.PERIOD, params);

		RecurrenceDates prop = result.getValue();
		Iterator<Period> it = prop.getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertNull(prop.getDates());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_invalid_no_end_date() {
		String value = "20130611T134302Z/20130611T154302Z,20130611T134302Z";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.PERIOD, params);

		RecurrenceDates prop = result.getValue();
		Iterator<Period> it = prop.getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertNull(prop.getDates());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_datetimes() {
		String value = "20130611T134302Z,20130611T154302Z";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.DATE_TIME, params);

		RecurrenceDates prop = result.getValue();
		Iterator<Date> it = prop.getDates().iterator();

		Date date = it.next();
		assertEquals(start, date);

		date = it.next();
		assertEquals(end, date);

		assertFalse(it.hasNext());

		assertNull(prop.getPeriods());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_invalid_datetime() {
		String value = "20130611T134302Z,invalid,20130611T154302Z";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.DATE_TIME, params);

		RecurrenceDates prop = result.getValue();
		Iterator<Date> it = prop.getDates().iterator();

		Date date = it.next();
		assertEquals(start, date);

		date = it.next();
		assertEquals(end, date);

		assertFalse(it.hasNext());

		assertNull(prop.getPeriods());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_dates() {
		String value = "20130611,20130612";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.DATE, params);

		RecurrenceDates prop = result.getValue();
		Iterator<Date> it = prop.getDates().iterator();

		Date date = it.next();
		assertDateEquals("20130611", date);

		date = it.next();
		assertDateEquals("20130612", date);

		assertFalse(it.hasNext());

		assertNull(prop.getPeriods());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_invalid_dates() {
		String value = "20130611,invalid,20130612";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.DATE, params);

		RecurrenceDates prop = result.getValue();
		Iterator<Date> it = prop.getDates().iterator();

		Date date = it.next();
		assertDateEquals("20130611", date);

		date = it.next();
		assertDateEquals("20130612", date);

		assertFalse(it.hasNext());

		assertNull(prop.getPeriods());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_empty_periods() {
		String value = "";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.PERIOD, params);
		RecurrenceDates prop = result.getValue();

		assertEquals(0, prop.getPeriods().size());
		assertNull(prop.getDates());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_empty_dates() {
		String value = "";
		ICalParameters params = new ICalParameters();

		Result<RecurrenceDates> result = marshaller.parseText(value, ICalDataType.DATE, params);
		RecurrenceDates prop = result.getValue();

		assertEquals(0, prop.getDates().size());
		assertNull(prop.getPeriods());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeXml_periods() {
		List<Period> periods = Arrays.asList(new Period(start, end), new Period(start, duration));
		RecurrenceDates prop = new RecurrenceDates(periods);
		//@formatter:off
		assertWriteXml(
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<end>2013-06-11T15:43:02Z</end>" +
		"</period>" +
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<duration>PT2H</duration>" +
		"</period>", prop, marshaller);
		//@formatter:on
	}

	@Test
	public void writeXml_datetimes() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, true);
		//@formatter:off
		assertWriteXml(
		"<date-time>2013-06-11T13:43:02Z</date-time>" +
		"<date-time>2013-06-11T15:43:02Z</date-time>", prop, marshaller);
		//@formatter:on
	}

	@Test
	public void writeXml_dates() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, false);
		//@formatter:off
		assertWriteXml(
		"<date>2013-06-11</date>" +
		"<date>2013-06-11</date>", prop, marshaller);
		//@formatter:on
	}

	@Test
	public void writeXml_empty() {
		List<Date> dates = Arrays.asList();
		RecurrenceDates prop = new RecurrenceDates(dates, false);
		assertWriteXml("", prop, marshaller);
	}

	@Test
	public void parseXml_periods() {
		//@formatter:off
		Result<RecurrenceDates> result = parseXCalProperty(
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<end>2013-06-11T15:43:02Z</end>" +
		"</period>" +
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<duration>PT2H</duration>" +
		"</period>", marshaller);
		//@formatter:on

		RecurrenceDates prop = result.getValue();
		assertNull(prop.getDates());
		Iterator<Period> it = prop.getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		period = it.next();
		assertEquals(start, period.getStartDate());
		assertNull(period.getEndDate());
		assertEquals(duration, period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_dates() {
		//marshaller is lenient in accepting a combination of both
		//@formatter:off
		Result<RecurrenceDates> result = parseXCalProperty(
		"<date-time>2013-06-11T13:43:02Z</date-time>" +
		"<date>2013-06-12</date>", marshaller);
		//@formatter:on

		Date date;
		{
			Calendar c = Calendar.getInstance();
			c.clear();
			c.set(Calendar.YEAR, 2013);
			c.set(Calendar.MONTH, Calendar.JUNE);
			c.set(Calendar.DATE, 12);
			date = c.getTime();
		}

		RecurrenceDates prop = result.getValue();
		assertNull(prop.getPeriods());
		assertTrue(prop.hasTime());
		assertEquals(2, prop.getDates().size());
		assertTrue(prop.getDates().contains(start));
		assertTrue(prop.getDates().contains(date));

		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_dates_invalid() {
		//marshaller is lenient in accepting a combination of both
		//@formatter:off
		Result<RecurrenceDates> result = parseXCalProperty(
		"<date-time>2013-06-11T13:43:02Z</date-time>" +
		"<date>invalid</date>", marshaller);
		//@formatter:on

		RecurrenceDates prop = result.getValue();
		assertNull(prop.getPeriods());
		assertTrue(prop.hasTime());
		assertEquals(1, prop.getDates().size());
		assertTrue(prop.getDates().contains(start));

		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseXml_periods_invalid() {
		//@formatter:off
		Result<RecurrenceDates> result = parseXCalProperty(
		"<period>" +
			"<start>invalid</start>" +
			"<end>2013-06-11T15:43:02Z</end>" +
		"</period>" +
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<end>invalid</end>" +
		"</period>" +
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<duration>invalid</duration>" +
		"</period>", marshaller);
		//@formatter:on

		RecurrenceDates prop = result.getValue();
		assertNull(prop.getDates());
		Iterator<Period> it = prop.getPeriods().iterator();
		assertFalse(it.hasNext());

		assertWarnings(3, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_empty() {
		parseXCalProperty("", marshaller);
	}

	@Test
	public void writeJson_periods() {
		List<Period> periods = Arrays.asList(new Period(start, end), new Period(start, duration));
		RecurrenceDates prop = new RecurrenceDates(periods);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Arrays.asList("2013-06-11T13:43:02Z/2013-06-11T15:43:02Z", "2013-06-11T13:43:02Z/PT2H"), actual.getMultivalued());
	}

	@Test
	public void writeJson_datetimes() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, true);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Arrays.asList("2013-06-11T13:43:02Z", "2013-06-11T15:43:02Z"), actual.getMultivalued());
	}

	@Test
	public void writeJson_dates() {
		List<Date> dates = Arrays.asList(start, end);
		RecurrenceDates prop = new RecurrenceDates(dates, false);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Arrays.asList("2013-06-11", "2013-06-11"), actual.getMultivalued());
	}

	@Test
	public void writeJson_empty() {
		List<Date> dates = Arrays.asList();
		RecurrenceDates prop = new RecurrenceDates(dates, false);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Arrays.asList(), actual.getMultivalued());
	}

	@Test
	public void parseJson_periods() {
		Result<RecurrenceDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z/2013-06-11T15:43:02Z", "2013-06-11T13:43:02Z/PT2H"), ICalDataType.PERIOD, new ICalParameters());

		RecurrenceDates prop = result.getValue();
		assertNull(prop.getDates());
		Iterator<Period> it = prop.getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		period = it.next();
		assertEquals(start, period.getStartDate());
		assertNull(period.getEndDate());
		assertEquals(duration, period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseJson_invalid_start_date() {
		Result<RecurrenceDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z/2013-06-11T15:43:02Z", "invalid/PT2H"), ICalDataType.PERIOD, new ICalParameters());

		RecurrenceDates prop = result.getValue();
		Iterator<Period> it = prop.getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertNull(prop.getDates());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_invalid_end_date() {
		Result<RecurrenceDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z/2013-06-11T15:43:02Z", "2013-06-11T13:43:02Z/invalid"), ICalDataType.PERIOD, new ICalParameters());

		RecurrenceDates prop = result.getValue();
		Iterator<Period> it = prop.getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertNull(prop.getDates());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_invalid_no_end_date() {
		Result<RecurrenceDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z/2013-06-11T15:43:02Z", "2013-06-11T13:43:02Z"), ICalDataType.PERIOD, new ICalParameters());

		RecurrenceDates prop = result.getValue();
		Iterator<Period> it = prop.getPeriods().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertNull(prop.getDates());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_datetimes() {
		Result<RecurrenceDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z", "2013-06-11T15:43:02Z"), ICalDataType.DATE_TIME, new ICalParameters());

		RecurrenceDates prop = result.getValue();
		Iterator<Date> it = prop.getDates().iterator();

		Date date = it.next();
		assertEquals(start, date);

		date = it.next();
		assertEquals(end, date);

		assertFalse(it.hasNext());

		assertNull(prop.getPeriods());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseJson_invalid_datetime() {
		Result<RecurrenceDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z", "invalid", "2013-06-11T15:43:02Z"), ICalDataType.DATE_TIME, new ICalParameters());

		RecurrenceDates prop = result.getValue();
		Iterator<Date> it = prop.getDates().iterator();

		Date date = it.next();
		assertEquals(start, date);

		date = it.next();
		assertEquals(end, date);

		assertFalse(it.hasNext());

		assertNull(prop.getPeriods());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_dates() {
		Result<RecurrenceDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11", "2013-06-12"), ICalDataType.DATE, new ICalParameters());

		RecurrenceDates prop = result.getValue();
		Iterator<Date> it = prop.getDates().iterator();

		Date date = it.next();
		assertDateEquals("20130611", date);

		date = it.next();
		assertDateEquals("20130612", date);

		assertFalse(it.hasNext());

		assertNull(prop.getPeriods());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseJson_invalid_dates() {
		Result<RecurrenceDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11", "invalid", "2013-06-12"), ICalDataType.DATE, new ICalParameters());

		RecurrenceDates prop = result.getValue();
		Iterator<Date> it = prop.getDates().iterator();

		Date date = it.next();
		assertDateEquals("20130611", date);

		date = it.next();
		assertDateEquals("20130612", date);

		assertFalse(it.hasNext());

		assertNull(prop.getPeriods());
		assertWarnings(1, result.getWarnings());
	}
}
