package biweekly.util;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;
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
 * Utility classes for unit tests.
 * @author Michael Angstadt
 */
public class TestUtils {
	/**
	 * Asserts that a warnings list is a certain size.
	 * @param expectedSize the expected size of the warnings list
	 * @param warnings the warnings list
	 */
	public static void assertWarnings(int expectedSize, List<String> warnings) {
		assertEquals(warnings.toString(), expectedSize, warnings.size());
	}

	/**
	 * Asserts that a string matches a regular expression.
	 * @param regex the regular expression
	 * @param string the string
	 */
	public static void assertRegex(String regex, String string) {
		Pattern p = Pattern.compile(regex);
		assertTrue(string, p.matcher(string).matches());
	}

	/**
	 * Asserts the value of a {@link Date} object.
	 * @param expected the expected value of the date, in string form (e.g.
	 * "20130610T102301")
	 * @param actual the actual date object
	 */
	public static void assertDateEquals(String expected, Date actual) {
		if (expected.contains("Z")) {
			expected = expected.replace("Z", "+0000");
		}

		SimpleDateFormat df;
		if (expected.contains("T")) {
			if (expected.contains("-") || expected.contains("+")) {
				df = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
			} else {
				df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
			}
		} else {
			df = new SimpleDateFormat("yyyyMMdd");
		}

		try {
			assertEquals(df.parse(expected), actual);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Asserts the value of an {@link Integer} object.
	 * @param expected the expected value
	 * @param actual the actual value
	 */
	public static void assertIntEquals(int expected, Integer actual) {
		assertEquals(Integer.valueOf(expected), actual);
	}

	/**
	 * Builds a timezone object with the given offset.
	 * @param hours the hour offset
	 * @param minutes the minute offset
	 * @return the timezone object
	 */
	public static TimeZone buildTimezone(int hours, int minutes) {
		int hourMillis = 1000 * 60 * 60 * hours;

		int minuteMillis = 1000 * 60 * minutes;
		if (hours < 0) {
			minuteMillis *= -1;
		}

		return new SimpleTimeZone(hourMillis + minuteMillis, "");
	}

	/**
	 * Builds an XML document that contains an empty xCal property element.
	 * @param marshaller the property marshaller
	 * @return the document
	 */
	public static Document xcalProperty(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
		QName qname = marshaller.getQName();
		Document document = XmlUtils.createDocument();
		Element element = document.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
		document.appendChild(element);
		return document;
	}

	/**
	 * Builds an XML document that contains a xCal property element.
	 * @param marshaller the property marshaller
	 * @param body the XML of the element body
	 * @return the document
	 */
	public static Document xcalProperty(ICalPropertyMarshaller<? extends ICalProperty> marshaller, String body) {
		QName qname = marshaller.getQName();
		try {
			return XmlUtils.toDocument("<" + qname.getLocalPart() + " xmlns=\"" + qname.getNamespaceURI() + "\">" + body + "</" + qname.getLocalPart() + ">");
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Builds a xCal property element.
	 * @param marshaller the property marshaller
	 * @param body the XML of the element body
	 * @return the property element
	 */
	public static Element xcalPropertyElement(ICalPropertyMarshaller<? extends ICalProperty> marshaller, String body) {
		return XmlUtils.getRootElement(xcalProperty(marshaller, body));
	}

	/**
	 * Tests to see if an xCal property was marshalled correctly.
	 * @param expectedInnerXml the expected inner XML of the property element
	 * @param propertyToWrite the property to marshal
	 * @param marshaller the marshaller object
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void assertWriteXml(String expectedInnerXml, ICalProperty propertyToWrite, ICalPropertyMarshaller marshaller) {
		Document actual = xcalProperty(marshaller);
		marshaller.writeXml(propertyToWrite, XmlUtils.getRootElement(actual));

		Document expected = xcalProperty(marshaller, expectedInnerXml);
		assertXMLEqual(expected, actual);
	}

	/**
	 * Unmarshals an xCal property element.
	 * @param <T> the property class
	 * @param innerXml the inner XML of the property element
	 * @param marshaller the marshaller object
	 * @return the unmarshal result
	 */
	public static <T extends ICalProperty> Result<T> parseXCalProperty(String innerXml, ICalPropertyMarshaller<T> marshaller) {
		ICalParameters params = new ICalParameters();
		Element element = xcalPropertyElement(marshaller, innerXml);
		return marshaller.parseXml(element, params);
	}
}
