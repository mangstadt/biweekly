package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.DateTimeProperty;
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
public class DateTimePropertyMarshallerTest {
	private final DateTimePropertyMarshallerImpl marshaller = new DateTimePropertyMarshallerImpl();
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

	@Test
	public void writeText() {
		DateTimePropertyImpl prop = new DateTimePropertyImpl(datetime);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		DateTimePropertyImpl prop = new DateTimePropertyImpl(null);

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "20130611T134302Z";
		ICalParameters params = new ICalParameters();

		Result<DateTimePropertyImpl> result = marshaller.parseText(value, Value.DATE_TIME, params);

		DateTimePropertyImpl prop = result.getValue();
		assertEquals(datetime, prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_invalid() {
		String value = "invalid";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, Value.DATE_TIME, params);
	}

	@Test
	public void writeXml() {
		DateTimePropertyImpl prop = new DateTimePropertyImpl(datetime);
		assertWriteXml("<date-time>2013-06-11T13:43:02Z</date-time>", prop, marshaller);
	}

	@Test
	public void writeXml_null() {
		DateTimePropertyImpl prop = new DateTimePropertyImpl(null);
		assertWriteXml("", prop, marshaller);
	}

	@Test
	public void parseXml() {
		Result<DateTimePropertyImpl> result = parseXCalProperty("<date-time>2013-06-11T13:43:02Z</date-time>", marshaller);

		DateTimePropertyImpl prop = result.getValue();
		assertEquals(datetime, prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_invalid() {
		parseXCalProperty("<date-time>invalid</date-time>", marshaller);
	}

	@Test
	public void writeJson() {
		DateTimePropertyImpl prop = new DateTimePropertyImpl(datetime);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals("2013-06-11T13:43:02Z", actual.getSingleValued());
	}

	@Test
	public void writeJson_null() {
		DateTimePropertyImpl prop = new DateTimePropertyImpl(null);

		JCalValue actual = marshaller.writeJson(prop);
		assertTrue(actual.getValues().get(0).isNull());
	}

	@Test
	public void parseJson() {
		Result<DateTimePropertyImpl> result = marshaller.parseJson(JCalValue.single("2013-06-11T13:43:02Z"), Value.DATE_TIME, new ICalParameters());

		DateTimePropertyImpl prop = result.getValue();
		assertEquals(datetime, prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseJson_invalid() {
		marshaller.parseJson(JCalValue.single("invalid"), Value.DATE_TIME, new ICalParameters());
	}

	private class DateTimePropertyMarshallerImpl extends DateTimePropertyMarshaller<DateTimePropertyImpl> {
		public DateTimePropertyMarshallerImpl() {
			super(DateTimePropertyImpl.class, "DATETIME");
		}

		@Override
		protected DateTimePropertyImpl newInstance(Date date) {
			return new DateTimePropertyImpl(date);
		}
	}

	private class DateTimePropertyImpl extends DateTimeProperty {
		public DateTimePropertyImpl(Date value) {
			super(value);
		}
	}
}
