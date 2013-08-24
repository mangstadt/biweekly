package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.w3c.dom.Document;

import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalNamespaceContext;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.Xml;
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
public class XmlMarshallerTest {
	private final XmlMarshaller marshaller = new XmlMarshaller();

	@Test
	public void writeText() throws Throwable {
		Xml prop = new Xml("<element xmlns=\"http://example.com\"/>");

		String actual = marshaller.writeText(prop);

		String expected = "<element xmlns=\"http://example.com\"/>";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() throws Throwable {
		String value = "<element xmlns=\"http://example.com\"/>";
		ICalParameters params = new ICalParameters();

		Result<Xml> result = marshaller.parseText(value, Value.TEXT, params);

		Document expected = XmlUtils.toDocument(value);

		Xml prop = result.getValue();
		assertXMLEqual(expected, prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_invalid() throws Exception {
		String value = "invalid";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, Value.TEXT, params);
	}

	@Test
	public void writeXml() {
		//not called by the marshaller
	}

	@Test
	public void parseXml() throws Throwable {
		ICalParameters parameters = new ICalParameters();
		parameters.put("x-foo", "value");
		//@formatter:off
		Document doc = XmlUtils.toDocument(
		"<element xmlns=\"http://example.com\">" +
			"<xcal:parameters xmlns:xcal=\"" + XCalNamespaceContext.XCAL_NS + "\">" +
				"<xcal:x-foo>" +
					"<xcal:text>value</xcal:text>" +
				"</xcal:x-foo>" +
			"</xcal:parameters>" +
		"</element>");
		//@formatter:on
		Result<Xml> result = marshaller.parseXml(XmlUtils.getRootElement(doc), parameters);

		Document expected = XmlUtils.toDocument("<element xmlns=\"http://example.com\"/>");
		Xml prop = result.getValue();
		assertXMLEqual(expected, prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeJson() throws Throwable {
		Xml prop = new Xml("<element xmlns=\"http://example.com\"/>");

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals("<element xmlns=\"http://example.com\"/>", actual.getSingleValued());
	}

	@Test
	public void writeJson_null() throws Throwable {
		Xml prop = new Xml((Document) null);

		JCalValue actual = marshaller.writeJson(prop);
		assertTrue(actual.getValues().get(0).isNull());
	}

	@Test
	public void parseJson() throws Throwable {
		String xml = "<element xmlns=\"http://example.com\"/>";
		Result<Xml> result = marshaller.parseJson(JCalValue.single(xml), Value.TEXT, new ICalParameters());

		Document expected = XmlUtils.toDocument(xml);
		Xml prop = result.getValue();
		assertXMLEqual(expected, prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseJson_invalid() throws Throwable {
		marshaller.parseJson(JCalValue.single("invalid"), Value.TEXT, new ICalParameters());
	}
}
