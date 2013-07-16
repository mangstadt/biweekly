package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Related;
import biweekly.parameter.Value;
import biweekly.property.Trigger;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
import biweekly.util.Duration;

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
public class TriggerMarshallerTest {
	private final TriggerMarshaller marshaller = new TriggerMarshaller();

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

	private final Duration duration;
	{
		duration = Duration.builder().hours(2).build();
	}

	@Test
	public void prepareParameters_date() {
		Trigger prop = new Trigger(datetime);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertEquals(Value.DATE_TIME, params.getValue());
		assertNull(params.getRelated());
	}

	@Test
	public void prepareParameters_duration() {
		Trigger prop = new Trigger(duration, Related.START);

		ICalParameters params = marshaller.prepareParameters(prop);

		assertNull(params.getValue());
		assertEquals(Related.START, params.getRelated());
	}

	@Test
	public void writeText_date() {
		Trigger prop = new Trigger(datetime);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_duration() {
		Trigger prop = new Trigger(duration, Related.START);

		String actual = marshaller.writeText(prop);

		String expected = "PT2H";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		Trigger prop = new Trigger(null);

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText_date() {
		String value = "20130611T134302Z";
		ICalParameters params = new ICalParameters();

		Result<Trigger> result = marshaller.parseText(value, params);

		Trigger prop = result.getValue();
		assertEquals(datetime, prop.getDate());
		assertNull(prop.getDuration());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_duration() {
		String value = "PT2H";
		ICalParameters params = new ICalParameters();

		Result<Trigger> result = marshaller.parseText(value, params);

		Trigger prop = result.getValue();
		assertNull(prop.getDate());
		assertEquals(duration, prop.getDuration());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_invalid() {
		String value = "invalid";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test
	public void writeXml_date() {
		Trigger prop = new Trigger(datetime);
		assertWriteXml("<date-time>2013-06-11T13:43:02Z</date-time>", prop, marshaller);
	}

	@Test
	public void writeXml_duration() {
		Trigger prop = new Trigger(duration, Related.START);
		assertWriteXml("<duration>PT2H</duration>", prop, marshaller);
	}

	@Test
	public void writeXml_null() {
		Trigger prop = new Trigger(null);
		assertWriteXml("", prop, marshaller);
	}

	@Test
	public void parseXml_date() {
		Result<Trigger> result = parseXCalProperty("<date-time>2013-06-11T13:43:02Z</date-time>", marshaller);

		Trigger prop = result.getValue();
		assertEquals(datetime, prop.getDate());
		assertNull(prop.getDuration());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_duration() {
		Result<Trigger> result = parseXCalProperty("<duration>PT2H</duration>", marshaller);

		Trigger prop = result.getValue();
		assertNull(prop.getDate());
		assertEquals(duration, prop.getDuration());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_date_invalid() {
		parseXCalProperty("<date-time>invalid</date-time>", marshaller);
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_duration_invalid() {
		parseXCalProperty("<duration>invalid</duration>", marshaller);
	}

	@Test
	public void writeJson_date() {
		Trigger prop = new Trigger(datetime);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Value.DATE_TIME, actual.getDataType());
		assertEquals("2013-06-11T13:43:02Z", actual.getSingleValued());
	}

	@Test
	public void writeJson_duration() {
		Trigger prop = new Trigger(duration, Related.START);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Value.DURATION, actual.getDataType());
		assertEquals("PT2H", actual.getSingleValued());
	}

	@Test
	public void writeJson_null() {
		Trigger prop = new Trigger(null);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Value.DURATION, actual.getDataType());
		assertTrue(actual.getValues().get(0).isNull());
	}

	@Test
	public void parseJson_date() {
		Result<Trigger> result = marshaller.parseJson(JCalValue.single(Value.DATE_TIME, "2013-06-11T13:43:02Z"), new ICalParameters());

		Trigger prop = result.getValue();
		assertEquals(datetime, prop.getDate());
		assertNull(prop.getDuration());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseJson_duration() {
		Result<Trigger> result = marshaller.parseJson(JCalValue.single(Value.DURATION, "PT2H"), new ICalParameters());

		Trigger prop = result.getValue();
		assertNull(prop.getDate());
		assertEquals(duration, prop.getDuration());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseJson_date_invalid() {
		marshaller.parseJson(JCalValue.single(Value.DATE_TIME, "invalid"), new ICalParameters());
	}

	@Test(expected = CannotParseException.class)
	public void parseJson_duration_invalid() {
		marshaller.parseJson(JCalValue.single(Value.DURATION, "invalid"), new ICalParameters());
	}
}
