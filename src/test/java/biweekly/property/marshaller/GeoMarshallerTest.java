package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.xcalProperty;
import static biweekly.util.TestUtils.xcalPropertyElement;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import biweekly.io.CannotParseException;
import biweekly.parameter.ICalParameters;
import biweekly.property.Geo;
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
public class GeoMarshallerTest {
	private final GeoMarshaller marshaller = new GeoMarshaller();

	@Test
	public void writeText() {
		Geo prop = new Geo(12.34, 56.78);

		String actual = marshaller.writeText(prop);

		String expected = "12.34;56.78";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_missing_latitude() {
		Geo prop = new Geo(null, 56.78);
		String actual = marshaller.writeText(prop);
		String expected = ";56.78";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_missing_longitude() {
		Geo prop = new Geo(12.34, null);
		String actual = marshaller.writeText(prop);
		String expected = "12.34;";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_missing_both() {
		Geo prop = new Geo(null, null);
		String actual = marshaller.writeText(prop);
		String expected = ";";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "12.34;56.78";
		ICalParameters params = new ICalParameters();

		Result<Geo> result = marshaller.parseText(value, params);

		Geo prop = result.getValue();
		assertEquals(12.34, prop.getLatitude(), 0.001);
		assertEquals(56.78, prop.getLongitude(), 0.001);
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_no_longitude() {
		String value = "12.34";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_empty() {
		String value = "";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_bad_latitude() {
		String value = "bad;56.78";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_bad_longitude() {
		String value = "12.34;bad";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test
	public void writeXml() {
		Geo prop = new Geo(12.34, 56.78);

		Document actual = xcalProperty(marshaller);
		marshaller.writeXml(prop, XmlUtils.getRootElement(actual));

		Document expected = xcalProperty(marshaller, "<latitude>12.34</latitude><longitude>56.78</longitude>");
		assertXMLEqual(expected, actual);
	}

	@Test
	public void writeXml_missing_latitude() {
		Geo prop = new Geo(null, 56.78);

		Document actual = xcalProperty(marshaller);
		marshaller.writeXml(prop, XmlUtils.getRootElement(actual));

		Document expected = xcalProperty(marshaller, "<longitude>56.78</longitude>");
		assertXMLEqual(expected, actual);
	}

	@Test
	public void writeXml_missing_longitude() {
		Geo prop = new Geo(12.34, null);

		Document actual = xcalProperty(marshaller);
		marshaller.writeXml(prop, XmlUtils.getRootElement(actual));

		Document expected = xcalProperty(marshaller, "<latitude>12.34</latitude>");
		assertXMLEqual(expected, actual);
	}

	@Test
	public void writeXml_missing_both() {
		Geo prop = new Geo(null, null);

		Document actual = xcalProperty(marshaller);
		marshaller.writeXml(prop, XmlUtils.getRootElement(actual));

		Document expected = xcalProperty(marshaller, "");
		assertXMLEqual(expected, actual);
	}

	@Test
	public void parseXml() {
		ICalParameters params = new ICalParameters();

		Element element = xcalPropertyElement(marshaller, "<latitude>12.34</latitude><longitude>56.78</longitude>");
		Result<Geo> result = marshaller.parseXml(element, params);

		Geo prop = result.getValue();
		assertEquals(12.34, prop.getLatitude(), 0.001);
		assertEquals(56.78, prop.getLongitude(), 0.001);
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_missing_latitude() {
		ICalParameters params = new ICalParameters();

		Element element = xcalPropertyElement(marshaller, "<longitude>56.78</longitude>");
		Result<Geo> result = marshaller.parseXml(element, params);

		Geo prop = result.getValue();
		assertNull(prop.getLatitude());
		assertEquals(56.78, prop.getLongitude(), 0.001);
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_missing_longitude() {
		ICalParameters params = new ICalParameters();

		Element element = xcalPropertyElement(marshaller, "<latitude>12.34</latitude>");
		Result<Geo> result = marshaller.parseXml(element, params);

		Geo prop = result.getValue();
		assertEquals(12.34, prop.getLatitude(), 0.001);
		assertNull(prop.getLongitude());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_missing_both() {
		ICalParameters params = new ICalParameters();

		Element element = xcalPropertyElement(marshaller, "");
		Result<Geo> result = marshaller.parseXml(element, params);

		Geo prop = result.getValue();
		assertNull(prop.getLatitude());
		assertNull(prop.getLongitude());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_bad_latitude() {
		ICalParameters params = new ICalParameters();

		Element element = xcalPropertyElement(marshaller, "<latitude>bad</latitude><longitude>56.78</longitude>");
		marshaller.parseXml(element, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_bad_longitude() {
		ICalParameters params = new ICalParameters();

		Element element = xcalPropertyElement(marshaller, "<latitude>12.34</latitude><longitude>bad</longitude>");
		marshaller.parseXml(element, params);
	}
}
