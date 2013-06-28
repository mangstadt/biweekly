package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.xcalProperty;
import static biweekly.util.TestUtils.xcalPropertyElement;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.TextProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
import biweekly.util.XmlUtils;

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

		Result<TextPropertyImpl> result = marshaller.parseText(value, params);

		TextPropertyImpl prop = result.getValue();
		assertEquals("the;text", prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeXml() {
		TextPropertyImpl prop = new TextPropertyImpl("text");

		Document actual = xcalProperty(marshaller);
		marshaller.writeXml(prop, XmlUtils.getRootElement(actual));

		Document expected = xcalProperty(marshaller, "<text>text</text>");
		assertXMLEqual(expected, actual);
	}

	@Test
	public void writeXml_null() {
		TextPropertyImpl prop = new TextPropertyImpl(null);

		Document actual = xcalProperty(marshaller);
		marshaller.writeXml(prop, XmlUtils.getRootElement(actual));

		Document expected = xcalProperty(marshaller, "<text></text>");
		assertXMLEqual(expected, actual);
	}

	@Test
	public void writeXml_data_type() {
		TextPropertyMarshallerImpl marshaller = new TextPropertyMarshallerImpl(Value.CAL_ADDRESS);
		TextPropertyImpl prop = new TextPropertyImpl("mailto:johndoe@example.com");

		Document actual = xcalProperty(marshaller);
		marshaller.writeXml(prop, XmlUtils.getRootElement(actual));

		Document expected = xcalProperty(marshaller, "<cal-address>mailto:johndoe@example.com</cal-address>");
		assertXMLEqual(expected, actual);
	}

	@Test
	public void parseXml() {
		ICalParameters params = new ICalParameters();

		Element element = xcalPropertyElement(marshaller, "<text>text</text>");
		Result<TextPropertyImpl> result = marshaller.parseXml(element, params);

		TextPropertyImpl prop = result.getValue();
		assertEquals("text", prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_data_type() {
		TextPropertyMarshallerImpl marshaller = new TextPropertyMarshallerImpl(Value.CAL_ADDRESS);
		ICalParameters params = new ICalParameters();

		Element element = xcalPropertyElement(marshaller, "<cal-address>mailto:johndoe@example.com</cal-address>");
		Result<TextPropertyImpl> result = marshaller.parseXml(element, params);

		TextPropertyImpl prop = result.getValue();
		assertEquals("mailto:johndoe@example.com", prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	private class TextPropertyMarshallerImpl extends TextPropertyMarshaller<TextPropertyImpl> {
		public TextPropertyMarshallerImpl() {
			super(TextPropertyImpl.class, "TEXT");
		}

		public TextPropertyMarshallerImpl(Value dataType) {
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
