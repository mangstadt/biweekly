package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.TextProperty;
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
public class TextPropertyMarshallerTest {
	private final TextPropertyMarshallerImpl marshaller = new TextPropertyMarshallerImpl();

	@Test
	public void writeText() {
		TextPropertyImpl prop = new TextPropertyImpl("the;text");

		String actual = marshaller.writeText(prop);

		String expected = "the\\;text";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		TextPropertyImpl prop = new TextPropertyImpl(null);

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "the\\;text";
		ICalParameters params = new ICalParameters();

		Result<TextPropertyImpl> result = marshaller.parseText(value, ICalDataType.TEXT, params);

		TextPropertyImpl prop = result.getProperty();
		assertEquals("the;text", prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeXml() {
		TextPropertyImpl prop = new TextPropertyImpl("text");
		assertWriteXml("<text>text</text>", prop, marshaller);
	}

	@Test
	public void writeXml_null() {
		TextPropertyImpl prop = new TextPropertyImpl(null);
		assertWriteXml("<text></text>", prop, marshaller);
	}

	@Test
	public void writeXml_data_type() {
		TextPropertyMarshallerImpl marshaller = new TextPropertyMarshallerImpl(ICalDataType.CAL_ADDRESS);
		TextPropertyImpl prop = new TextPropertyImpl("mailto:johndoe@example.com");
		assertWriteXml("<cal-address>mailto:johndoe@example.com</cal-address>", prop, marshaller);
	}

	@Test
	public void parseXml() {
		Result<TextPropertyImpl> result = parseXCalProperty("<text>text</text>", marshaller);

		TextPropertyImpl prop = result.getProperty();
		assertEquals("text", prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_data_type() {
		TextPropertyMarshallerImpl marshaller = new TextPropertyMarshallerImpl(ICalDataType.CAL_ADDRESS);
		Result<TextPropertyImpl> result = parseXCalProperty("<cal-address>mailto:johndoe@example.com</cal-address>", marshaller);

		TextPropertyImpl prop = result.getProperty();
		assertEquals("mailto:johndoe@example.com", prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_empty() {
		parseXCalProperty("", marshaller);
	}

	@Test
	public void writeJson() {
		TextPropertyImpl prop = new TextPropertyImpl("text");

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals("text", actual.getSingleValued());
	}

	@Test
	public void writeJson_null() {
		TextPropertyImpl prop = new TextPropertyImpl(null);

		JCalValue actual = marshaller.writeJson(prop);
		assertTrue(actual.getValues().get(0).isNull());
	}

	@Test
	public void parseJson() {
		Result<TextPropertyImpl> result = marshaller.parseJson(JCalValue.single("text"), ICalDataType.TEXT, new ICalParameters());

		TextPropertyImpl prop = result.getProperty();
		assertEquals("text", prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	private class TextPropertyMarshallerImpl extends TextPropertyMarshaller<TextPropertyImpl> {
		public TextPropertyMarshallerImpl() {
			super(TextPropertyImpl.class, "TEXT");
		}

		public TextPropertyMarshallerImpl(ICalDataType dataType) {
			super(TextPropertyImpl.class, "TEXT", dataType);
		}

		@Override
		protected TextPropertyImpl newInstance(String value) {
			return new TextPropertyImpl(value);
		}
	}

	private class TextPropertyImpl extends TextProperty {
		public TextPropertyImpl(String value) {
			super(value);
		}
	}
}
