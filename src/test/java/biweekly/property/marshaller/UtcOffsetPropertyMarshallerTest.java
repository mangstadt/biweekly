package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import biweekly.io.CannotParseException;
import biweekly.parameter.ICalParameters;
import biweekly.property.UtcOffsetProperty;
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
public class UtcOffsetPropertyMarshallerTest {
	private final UtcOffsetPropertyMarshallerImpl marshaller = new UtcOffsetPropertyMarshallerImpl();

	@Test
	public void writeText() {
		UtcOffsetPropertyImpl prop = new UtcOffsetPropertyImpl(1, 30);

		String actual = marshaller.writeText(prop);

		String expected = "+0130";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		UtcOffsetPropertyImpl prop = new UtcOffsetPropertyImpl(null, 30);
		String actual = marshaller.writeText(prop);
		String expected = "+0030";
		assertEquals(expected, actual);

		prop = new UtcOffsetPropertyImpl(1, null);
		actual = marshaller.writeText(prop);
		expected = "+0100";
		assertEquals(expected, actual);

		prop = new UtcOffsetPropertyImpl(null, null);
		actual = marshaller.writeText(prop);
		expected = "+0000";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "+0130";
		ICalParameters params = new ICalParameters();

		Result<UtcOffsetPropertyImpl> result = marshaller.parseText(value, params);

		UtcOffsetProperty prop = result.getValue();
		assertIntEquals(1, prop.getHourOffset());
		assertIntEquals(30, prop.getMinuteOffset());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_invalid() {
		String value = "invalid";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test
	public void writeXml() {
		UtcOffsetPropertyImpl prop = new UtcOffsetPropertyImpl(1, 30);
		assertWriteXml("<utc-offset>+01:30</utc-offset>", prop, marshaller);
	}

	@Test
	public void writeXml_null() {
		UtcOffsetPropertyImpl prop = new UtcOffsetPropertyImpl(null, null);
		assertWriteXml("<utc-offset>+00:00</utc-offset>", prop, marshaller);
	}

	@Test
	public void parseXml() {
		Result<UtcOffsetPropertyImpl> result = parseXCalProperty("<utc-offset>+01:30</utc-offset>", marshaller);

		UtcOffsetPropertyImpl prop = result.getValue();
		assertIntEquals(1, prop.getHourOffset());
		assertIntEquals(30, prop.getMinuteOffset());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_invalid() {
		parseXCalProperty("<utc-offset>invalid</utc-offset>", marshaller);
	}

	@Test
	public void parseXml_empty() {
		Result<UtcOffsetPropertyImpl> result = parseXCalProperty("", marshaller);

		UtcOffsetPropertyImpl prop = result.getValue();
		assertNull(prop.getHourOffset());
		assertNull(prop.getMinuteOffset());
		assertWarnings(0, result.getWarnings());
	}

	private class UtcOffsetPropertyMarshallerImpl extends UtcOffsetPropertyMarshaller<UtcOffsetPropertyImpl> {
		public UtcOffsetPropertyMarshallerImpl() {
			super(UtcOffsetPropertyImpl.class, "UTC");
		}

		@Override
		protected UtcOffsetPropertyImpl newInstance(Integer hourOffset, Integer minuteOffset) {
			return new UtcOffsetPropertyImpl(hourOffset, minuteOffset);
		}
	}

	private class UtcOffsetPropertyImpl extends UtcOffsetProperty {
		public UtcOffsetPropertyImpl(Integer hourOffset, Integer minuteOffset) {
			super(hourOffset, minuteOffset);
		}
	}
}
