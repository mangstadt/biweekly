package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import biweekly.io.CannotParseException;
import biweekly.parameter.ICalParameters;
import biweekly.property.IntegerProperty;
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
public class IntegerPropertyMarshallerTest {
	private final IntegerPropertyMarshallerImpl marshaller = new IntegerPropertyMarshallerImpl();

	@Test
	public void writeText() {
		IntegerProperty prop = new IntegerProperty(5);

		String actual = marshaller.writeText(prop);

		String expected = "5";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		IntegerProperty prop = new IntegerProperty(null);

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "5";
		ICalParameters params = new ICalParameters();

		Result<IntegerProperty> result = marshaller.parseText(value, params);

		assertIntEquals(5, result.getValue().getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_invalid() {
		String value = "invalid";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_empty() {
		String value = "";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	private class IntegerPropertyMarshallerImpl extends IntegerPropertyMarshaller<IntegerProperty> {
		public IntegerPropertyMarshallerImpl() {
			super(IntegerProperty.class, "INT");
		}

		@Override
		protected IntegerProperty newInstance(Integer value) {
			return new IntegerProperty(value);
		}
	}
}
