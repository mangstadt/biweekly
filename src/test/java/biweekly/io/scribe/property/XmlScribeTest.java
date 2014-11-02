package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import biweekly.io.ParseContext;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.io.xml.XCalNamespaceContext;
import biweekly.parameter.ICalParameters;
import biweekly.property.Xml;
import biweekly.util.XmlUtils;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
public class XmlScribeTest extends ScribeTest<Xml> {
	private final String xml = "<element xmlns=\"http://example.com\">text</element>";
	private final Document value;
	{
		try {
			value = XmlUtils.toDocument(xml);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	private final Xml withValue = new Xml(value);
	private final Xml empty = new Xml((Document) null);

	public XmlScribeTest() {
		super(new XmlScribe());
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).run(xml);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(xml).run(has(value));
		sensei.assertParseText("invalid").cannotParse();
		sensei.assertParseText("").cannotParse();
	}

	@Test
	public void writeXml() {
		//not called by the marshaller
	}

	@Test
	public void parseXml() {
		ParseContext context = new ParseContext();
		Xml prop = scribe.parseXml(XmlUtils.getRootElement(value), new ICalParameters(), context);

		assertXMLEqual(value, prop.getValue());
		assertWarnings(0, context.getWarnings());
	}

	@Test
	public void parseXml_remove_parameters() throws Throwable {
		//<parameters> element should be removed
		//@formatter:off
		Document doc = XmlUtils.toDocument(
		"<element xmlns=\"http://example.com\">" +
			"<xcal:parameters xmlns:xcal=\"" + XCalNamespaceContext.XCAL_NS + "\">" +
				"<xcal:x-foo>" +
					"<xcal:text>value</xcal:text>" +
				"</xcal:x-foo>" +
			"</xcal:parameters>" +
			"text" +
		"</element>");
		//@formatter:on

		ICalParameters parameters = new ICalParameters();
		parameters.put("x-foo", "value");
		ParseContext context = new ParseContext();
		Xml prop = scribe.parseXml(XmlUtils.getRootElement(doc), parameters, context);

		assertXMLEqual(value, prop.getValue());
		assertWarnings(0, context.getWarnings());
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withValue).run(xml);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(xml).run(has(value));
		sensei.assertParseJson("invalid").cannotParse();
		sensei.assertParseJson("").cannotParse();
	}

	private Check<Xml> has(final Document expected) {
		return new Check<Xml>() {
			public void check(Xml actual, ParseContext context) {
				assertXMLEqual(expected, actual.getValue());
			}
		};
	}
}
