package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.ExceptionDates;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;

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
public class ExceptionDatesMarshallerTest {
	private final ExceptionDatesMarshaller marshaller = new ExceptionDatesMarshaller();

	private final Date datetime;
	{
		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		c.clear();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DATE, 11);
		c.set(Calendar.HOUR_OF_DAY, 13);
		c.set(Calendar.MINUTE, 43);
		c.set(Calendar.SECOND, 2);
		datetime = c.getTime();
	}

	private final Date date;
	{
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DATE, 11);
		date = c.getTime();
	}

	@Test
	public void getDataType_datetime() {
		ExceptionDates prop = new ExceptionDates(true);
		assertEquals(ICalDataType.DATE_TIME, marshaller.getDataType(prop));
	}

	@Test
	public void getDataType_date() {
		ExceptionDates prop = new ExceptionDates(false);
		assertEquals(ICalDataType.DATE, marshaller.getDataType(prop));
	}

	@Test
	public void writeText_datetime() {
		ExceptionDates prop = new ExceptionDates(true);
		prop.addValue(datetime);
		prop.addValue(datetime);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z,20130611T134302Z";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_date() {
		ExceptionDates prop = new ExceptionDates(false);
		prop.addValue(datetime);
		prop.addValue(datetime);

		String actual = marshaller.writeText(prop);

		String expected = "20130611,20130611";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText_datetime() {
		String value = "20130611T134302Z,20130611T134302Z";
		ICalParameters params = new ICalParameters();

		Result<ExceptionDates> result = marshaller.parseText(value, ICalDataType.DATE_TIME, params);

		ExceptionDates prop = result.getValue();
		assertEquals(Arrays.asList(datetime, datetime), prop.getValues());
		assertTrue(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_date() {
		String value = "20130611,20130611";
		ICalParameters params = new ICalParameters();

		Result<ExceptionDates> result = marshaller.parseText(value, ICalDataType.DATE, params);

		ExceptionDates prop = result.getValue();
		assertEquals(Arrays.asList(date, date), prop.getValues());
		assertFalse(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_invalid() {
		String value = "20130611T134302Z,invalid";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, ICalDataType.DATE_TIME, params);
	}

	@Test
	public void writeXml_datetime() {
		ExceptionDates prop = new ExceptionDates(true);
		prop.addValue(datetime);
		prop.addValue(datetime);
		assertWriteXml("<date-time>2013-06-11T13:43:02Z</date-time><date-time>2013-06-11T13:43:02Z</date-time>", prop, marshaller);
	}

	@Test
	public void writeXml_date() {
		ExceptionDates prop = new ExceptionDates(false);
		prop.addValue(datetime);
		prop.addValue(datetime);
		assertWriteXml("<date>2013-06-11</date><date>2013-06-11</date>", prop, marshaller);
	}

	@Test
	public void parseXml_datetime() {
		Result<ExceptionDates> result = parseXCalProperty("<date-time>2013-06-11T13:43:02Z</date-time><date-time>2013-06-11T13:43:02Z</date-time>", marshaller);

		ExceptionDates prop = result.getValue();
		assertEquals(Arrays.asList(datetime, datetime), prop.getValues());
		assertTrue(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_date() {
		Result<ExceptionDates> result = parseXCalProperty("<date>2013-06-11</date><date>2013-06-11</date>", marshaller);

		ExceptionDates prop = result.getValue();
		assertEquals(Arrays.asList(date, date), prop.getValues());
		assertFalse(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_combination() {
		Result<ExceptionDates> result = parseXCalProperty("<date-time>2013-06-11T13:43:02Z</date-time><date>2013-06-11</date>", marshaller);

		ExceptionDates prop = result.getValue();
		assertEquals(Arrays.asList(datetime, date), prop.getValues());
		assertTrue(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_invalid() {
		parseXCalProperty("<date>2013-06-11</date><date>invalid</date>", marshaller);
	}

	@Test
	public void writeJson_datetime() {
		ExceptionDates prop = new ExceptionDates(true);
		prop.addValue(datetime);
		prop.addValue(datetime);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Arrays.asList("2013-06-11T13:43:02Z", "2013-06-11T13:43:02Z"), actual.getMultivalued());
	}

	@Test
	public void writeJson_date() {
		ExceptionDates prop = new ExceptionDates(false);
		prop.addValue(datetime);
		prop.addValue(datetime);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Arrays.asList("2013-06-11", "2013-06-11"), actual.getMultivalued());
	}

	@Test
	public void parseJson_datetime() {
		Result<ExceptionDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z", "2013-06-11T13:43:02Z"), ICalDataType.DATE_TIME, new ICalParameters());

		ExceptionDates prop = result.getValue();
		assertEquals(Arrays.asList(datetime, datetime), prop.getValues());
		assertTrue(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseJson_date() {
		Result<ExceptionDates> result = marshaller.parseJson(JCalValue.multi("2013-06-11", "2013-06-11"), ICalDataType.DATE, new ICalParameters());

		ExceptionDates prop = result.getValue();
		assertEquals(Arrays.asList(date, date), prop.getValues());
		assertFalse(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseJson_invalid() {
		marshaller.parseJson(JCalValue.multi("2013-06-11", "invalid"), ICalDataType.DATE, new ICalParameters());
	}
}
