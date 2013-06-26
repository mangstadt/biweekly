package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.io.CannotParseException;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
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
	public void prepareParameters_datetime() {
		ExceptionDates prop = new ExceptionDates(true);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertNull(params.getValue());
	}

	@Test
	public void prepareParameters_date() {
		ExceptionDates prop = new ExceptionDates(false);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertEquals(Value.DATE, params.getValue());
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
	public void writeText_null() {
		ExceptionDates prop = new ExceptionDates(true);
		prop.addValue(datetime);
		prop.addValue(null);
		prop.addValue(datetime);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z,,20130611T134302Z";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText_datetime() {
		String value = "20130611T134302Z,20130611T134302Z";
		ICalParameters params = new ICalParameters();

		Result<ExceptionDates> result = marshaller.parseText(value, params);

		ExceptionDates prop = result.getValue();
		assertEquals(Arrays.asList(datetime, datetime), prop.getValues());
		assertTrue(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_date() {
		String value = "20130611,20130611";
		ICalParameters params = new ICalParameters();
		params.setValue(Value.DATE);

		Result<ExceptionDates> result = marshaller.parseText(value, params);

		ExceptionDates prop = result.getValue();
		assertEquals(Arrays.asList(date, date), prop.getValues());
		assertFalse(prop.hasTime());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_invalid() {
		String value = "20130611T134302Z,invalid";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}
}
