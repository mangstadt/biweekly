package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.DateOrDateTimeProperty;
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
public class DateOrDateTimePropertyMarshallerTest {
	private final DateOrDateTimePropertyMarshallerImpl marshaller = new DateOrDateTimePropertyMarshallerImpl();
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
	public void prepareParameters_datetime() {
		DateOrDateTimePropertyImpl prop = new DateOrDateTimePropertyImpl(datetime, true);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertNull(params.getValue());
	}

	@Test
	public void prepareParameters_date() {
		DateOrDateTimePropertyImpl prop = new DateOrDateTimePropertyImpl(datetime, false);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertEquals(Value.DATE, params.getValue());
	}

	@Test
	public void prepareParameters_null_value() {
		DateOrDateTimePropertyImpl prop = new DateOrDateTimePropertyImpl(null, false);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertNull(params.getValue());
	}

	@Test
	public void writeText_datetime() {
		DateOrDateTimePropertyImpl prop = new DateOrDateTimePropertyImpl(datetime, true);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_date() {
		DateOrDateTimePropertyImpl prop = new DateOrDateTimePropertyImpl(datetime, false);

		String actual = marshaller.writeText(prop);

		String expected = "20130611";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		DateOrDateTimePropertyImpl prop = new DateOrDateTimePropertyImpl(null, true);

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText_datetime() {
		String value = "20130611T134302Z";
		ICalParameters params = new ICalParameters();

		Result<DateOrDateTimePropertyImpl> result = marshaller.parseText(value, params);

		assertEquals(datetime, result.getValue().getValue());
		assertTrue(result.getValue().hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_date() {
		String value = "20130611";
		ICalParameters params = new ICalParameters();

		Result<DateOrDateTimePropertyImpl> result = marshaller.parseText(value, params);

		assertEquals(date, result.getValue().getValue());
		assertFalse(result.getValue().hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_invalid() {
		String value = "invalid";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test
	public void writeXml_datetime() {
		DateOrDateTimePropertyImpl prop = new DateOrDateTimePropertyImpl(datetime, true);
		assertWriteXml("<date-time>2013-06-11T13:43:02Z</date-time>", prop, marshaller);
	}

	@Test
	public void writeXml_date() {
		DateOrDateTimePropertyImpl prop = new DateOrDateTimePropertyImpl(datetime, false);
		assertWriteXml("<date>2013-06-11</date>", prop, marshaller);
	}

	@Test
	public void writeXml_null() {
		DateOrDateTimePropertyImpl prop = new DateOrDateTimePropertyImpl(null, true);
		assertWriteXml("", prop, marshaller);
	}

	@Test
	public void parseXml_datetime() {
		Result<DateOrDateTimePropertyImpl> result = parseXCalProperty("<date-time>2013-06-11T13:43:02Z</date-time>", marshaller);

		DateOrDateTimePropertyImpl prop = result.getValue();
		assertEquals(datetime, prop.getValue());
		assertTrue(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_date() {
		Result<DateOrDateTimePropertyImpl> result = parseXCalProperty("<date>2013-06-11</date>", marshaller);

		DateOrDateTimePropertyImpl prop = result.getValue();
		assertEquals(date, prop.getValue());
		assertFalse(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_invalid() {
		parseXCalProperty("<date-time>invalid</date-time>", marshaller);
	}

	@Test
	public void parseJson_datetime() {
		Result<DateOrDateTimePropertyImpl> result = marshaller.parseJson(JCalValue.single(Value.DATE_TIME, "2013-06-11T13:43:02Z"), new ICalParameters());

		DateOrDateTimePropertyImpl prop = result.getValue();
		assertEquals(datetime, prop.getValue());
		assertTrue(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseJson_date() {
		Result<DateOrDateTimePropertyImpl> result = marshaller.parseJson(JCalValue.single(Value.DATE, "2013-06-11"), new ICalParameters());

		DateOrDateTimePropertyImpl prop = result.getValue();
		assertEquals(date, prop.getValue());
		assertFalse(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseJson_invalid() {
		marshaller.parseJson(JCalValue.single(Value.DATE, "invalid"), new ICalParameters());
	}

	private class DateOrDateTimePropertyMarshallerImpl extends DateOrDateTimePropertyMarshaller<DateOrDateTimePropertyImpl> {
		public DateOrDateTimePropertyMarshallerImpl() {
			super(DateOrDateTimePropertyImpl.class, "DATE-OR-DATETIME");
		}

		@Override
		protected DateOrDateTimePropertyImpl newInstance(Date date, boolean hasTime) {
			return new DateOrDateTimePropertyImpl(date, hasTime);
		}
	}

	private class DateOrDateTimePropertyImpl extends DateOrDateTimeProperty {
		public DateOrDateTimePropertyImpl(Date value, boolean hasTime) {
			super(value, hasTime);
		}
	}
}
