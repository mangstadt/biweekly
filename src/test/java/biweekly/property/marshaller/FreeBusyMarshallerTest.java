package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

import org.junit.Test;

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

	private final Duration duration = new Duration.Builder().hours(2).build();

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

		Result<FreeBusy> result = marshaller.parseText(value, params);

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

		Result<FreeBusy> result = marshaller.parseText(value, params);

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

		Result<FreeBusy> result = marshaller.parseText(value, params);

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

		Result<FreeBusy> result = marshaller.parseText(value, params);

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

		Result<FreeBusy> result = marshaller.parseText(value, params);

		Iterator<Period> it = result.getValue().getValues().iterator();

		assertFalse(it.hasNext());

		assertWarnings(0, result.getWarnings());
	}
}
