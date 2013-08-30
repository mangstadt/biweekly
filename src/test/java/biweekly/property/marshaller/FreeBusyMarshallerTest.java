package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.FreeBusy;
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
public class FreeBusyMarshallerTest {
	private final FreeBusyMarshaller marshaller = new FreeBusyMarshaller();

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
	public void writeText() {
		FreeBusy prop = new FreeBusy();
		prop.addValue(start, end);
		prop.addValue(start, duration);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z/20130611T154302Z,20130611T134302Z/PT2H";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_single() {
		FreeBusy prop = new FreeBusy();
		prop.addValue(start, end);

		String actual = marshaller.writeText(prop);

		String expected = "20130611T134302Z/20130611T154302Z";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_empty() {
		FreeBusy prop = new FreeBusy();

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "20130611T134302Z/20130611T154302Z,20130611T134302Z/PT2H";
		ICalParameters params = new ICalParameters();

		Result<FreeBusy> result = marshaller.parseText(value, ICalDataType.PERIOD, params);

		Iterator<Period> it = result.getValue().getValues().iterator();

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
	public void parseText_invalid_start_date() {
		String value = "20130611T134302Z/20130611T154302Z,invalid/PT2H";
		ICalParameters params = new ICalParameters();

		Result<FreeBusy> result = marshaller.parseText(value, ICalDataType.PERIOD, params);

		Iterator<Period> it = result.getValue().getValues().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_invalid_end_date() {
		String value = "20130611T134302Z/20130611T154302Z,20130611T134302Z/invalid";
		ICalParameters params = new ICalParameters();

		Result<FreeBusy> result = marshaller.parseText(value, ICalDataType.PERIOD, params);

		Iterator<Period> it = result.getValue().getValues().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_invalid_no_end_date() {
		String value = "20130611T134302Z/20130611T154302Z,20130611T134302Z";
		ICalParameters params = new ICalParameters();

		Result<FreeBusy> result = marshaller.parseText(value, ICalDataType.PERIOD, params);

		Iterator<Period> it = result.getValue().getValues().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseText_empty() {
		String value = "";
		ICalParameters params = new ICalParameters();

		Result<FreeBusy> result = marshaller.parseText(value, ICalDataType.PERIOD, params);

		Iterator<Period> it = result.getValue().getValues().iterator();

		assertFalse(it.hasNext());

		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeXml() {
		FreeBusy prop = new FreeBusy();
		prop.addValue(start, end);
		prop.addValue(start, duration);
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
	public void writeXml_empty() {
		FreeBusy prop = new FreeBusy();
		assertWriteXml("", prop, marshaller);
	}

	@Test
	public void parseXml() {
		//@formatter:off
		Result<FreeBusy> result = parseXCalProperty(
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<end>2013-06-11T15:43:02Z</end>" +
		"</period>" +
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<duration>PT2H</duration>" +
		"</period>", marshaller);
		//@formatter:on

		Iterator<Period> it = result.getValue().getValues().iterator();

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
	public void parseXml_invalid_start_date() {
		//@formatter:off
		Result<FreeBusy> result = parseXCalProperty(
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<end>2013-06-11T15:43:02Z</end>" +
		"</period>" +
		"<period>" +
			"<start>invalid</start>" +
			"<duration>PT2H</duration>" +
		"</period>", marshaller);
		//@formatter:on

		Iterator<Period> it = result.getValue().getValues().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseXml_invalid_end_date() {
		//@formatter:off
		Result<FreeBusy> result = parseXCalProperty(
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<end>invalid</end>" +
		"</period>" +
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<duration>PT2H</duration>" +
		"</period>", marshaller);
		//@formatter:on

		Iterator<Period> it = result.getValue().getValues().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertNull(period.getEndDate());
		assertEquals(duration, period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseXml_invalid_duration() {
		//@formatter:off
		Result<FreeBusy> result = parseXCalProperty(
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<end>2013-06-11T15:43:02Z</end>" +
		"</period>" +
		"<period>" +
			"<start>2013-06-11T13:43:02Z</start>" +
			"<duration>invalid</duration>" +
		"</period>", marshaller);
		//@formatter:on

		Iterator<Period> it = result.getValue().getValues().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(1, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_empty() {
		parseXCalProperty("", marshaller);
	}

	@Test
	public void writeJson() {
		FreeBusy prop = new FreeBusy();
		prop.addValue(start, end);
		prop.addValue(start, duration);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Arrays.asList("2013-06-11T13:43:02Z/2013-06-11T15:43:02Z", "2013-06-11T13:43:02Z/PT2H"), actual.getMultivalued());
	}

	@Test
	public void writeJson_empty() {
		FreeBusy prop = new FreeBusy();

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Arrays.asList(), actual.getMultivalued());
	}

	@Test
	public void parseJson() {
		Result<FreeBusy> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z/2013-06-11T15:43:02Z", "2013-06-11T13:43:02Z/PT2H"), ICalDataType.PERIOD, new ICalParameters());

		Iterator<Period> it = result.getValue().getValues().iterator();

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
		Result<FreeBusy> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z/2013-06-11T15:43:02Z", "invalid/PT2H"), ICalDataType.PERIOD, new ICalParameters());

		Iterator<Period> it = result.getValue().getValues().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_invalid_end_date() {
		Result<FreeBusy> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z/invalid", "2013-06-11T13:43:02Z/PT2H"), ICalDataType.PERIOD, new ICalParameters());

		Iterator<Period> it = result.getValue().getValues().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertNull(period.getEndDate());
		assertEquals(duration, period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_invalid_duration() {
		Result<FreeBusy> result = marshaller.parseJson(JCalValue.multi("2013-06-11T13:43:02Z/2013-06-11T15:43:02Z", "2013-06-11T13:43:02Z/invalid"), ICalDataType.PERIOD, new ICalParameters());

		Iterator<Period> it = result.getValue().getValues().iterator();

		Period period = it.next();
		assertEquals(start, period.getStartDate());
		assertEquals(end, period.getEndDate());
		assertNull(period.getDuration());

		assertFalse(it.hasNext());

		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_empty() {
		Result<FreeBusy> result = marshaller.parseJson(JCalValue.multi(), ICalDataType.PERIOD, new ICalParameters());

		Iterator<Period> it = result.getValue().getValues().iterator();

		assertFalse(it.hasNext());

		assertWarnings(0, result.getWarnings());
	}
}
