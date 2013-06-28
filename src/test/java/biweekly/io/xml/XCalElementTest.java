package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import biweekly.parameter.Value;
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
public class XCalElementTest {
	@Test
	public void first() {
		XCalElement XCalElement = build("<prop><one>1</one><two>2</two></prop>");
		assertEquals("2", XCalElement.first("two"));
	}

	@Test
	public void first_value_object() {
		XCalElement XCalElement = build("<prop><text>1</text></prop>");
		assertEquals("1", XCalElement.first(Value.TEXT));
	}

	@Test
	public void first_unknown() {
		XCalElement XCalElement = build("<prop><unknown>1</unknown></prop>");
		assertEquals("1", XCalElement.firstUnknown());
	}

	@Test
	public void first_with_prefix() {
		XCalElement XCalElement = build("<v:prop><v:one>1</v:one><v:two>2</v:two></v:prop>", "v");
		assertEquals("2", XCalElement.first("two"));
	}

	@Test
	public void first_empty() {
		XCalElement XCalElement = build("<prop><one>1</one><two></two></prop>");
		assertEquals("", XCalElement.first("two"));
	}

	@Test
	public void first_none() {
		XCalElement XCalElement = build("<prop><one>1</one><two>2</two></prop>");
		assertNull(XCalElement.first("three"));
	}

	@Test
	public void first_multiple_names() {
		XCalElement XCalElement = build("<prop><one>1</one><two>2</two><three>3</three></prop>");
		assertEquals("2", XCalElement.first("two", "three"));
		assertEquals("2", XCalElement.first("three", "two"));
	}

	@Test
	public void first_ignore_other_namespaces() {
		XCalElement XCalElement = build("<prop><n:four xmlns:n=\"http://example.com\"></n:four></prop>");
		assertNull(XCalElement.first("four"));
	}

	@Test
	public void all() {
		XCalElement XCalElement = build("<prop><one>1</one><two>2</two><two /><three>3</three><two>2-2</two></prop>");
		assertEquals(Arrays.asList("2", "", "2-2"), XCalElement.all("two"));
	}

	@Test
	public void all_value_object() {
		XCalElement XCalElement = build("<prop><one>1</one><text>2</text><text /><three>3</three><text>2-2</text></prop>");
		assertEquals(Arrays.asList("2", "", "2-2"), XCalElement.all(Value.TEXT));
	}

	@Test
	public void all_none() {
		XCalElement XCalElement = build("<prop><one>1</one><two>2</two></prop>");
		assertTrue(XCalElement.all("three").isEmpty());
	}

	@Test
	public void append() {
		XCalElement xCalElement = build("<prop><one>1</one></prop>");
		Element appendedElement = xCalElement.append("two", "2");
		assertEquals("two", appendedElement.getLocalName());
		assertEquals(XCAL_NS, appendedElement.getNamespaceURI());
		assertEquals("2", appendedElement.getTextContent());

		Iterator<Element> it = XmlUtils.toElementList(xCalElement.getElement().getChildNodes()).iterator();

		Element element = it.next();
		assertEquals("one", element.getLocalName());
		assertEquals(XCAL_NS, element.getNamespaceURI());
		assertEquals("1", element.getTextContent());

		element = it.next();
		assertEquals(appendedElement, element);

		assertFalse(it.hasNext());
	}

	@Test
	public void append_value_object() {
		XCalElement xCalElement = build("<prop><one>1</one></prop>");
		Element appendedElement = xCalElement.append(Value.TEXT, "2");
		assertEquals("text", appendedElement.getLocalName());
		assertEquals(XCAL_NS, appendedElement.getNamespaceURI());
		assertEquals("2", appendedElement.getTextContent());

		Iterator<Element> it = XmlUtils.toElementList(xCalElement.getElement().getChildNodes()).iterator();

		Element element = it.next();
		assertEquals("one", element.getLocalName());
		assertEquals(XCAL_NS, element.getNamespaceURI());
		assertEquals("1", element.getTextContent());

		element = it.next();
		assertEquals(appendedElement, element);

		assertFalse(it.hasNext());
	}

	@Test
	public void appendUnknown() {
		XCalElement xCalElement = build("<prop><one>1</one></prop>");
		Element appendedElement = xCalElement.appendUnknown("2");
		assertEquals("unknown", appendedElement.getLocalName());
		assertEquals(XCAL_NS, appendedElement.getNamespaceURI());
		assertEquals("2", appendedElement.getTextContent());

		Iterator<Element> it = XmlUtils.toElementList(xCalElement.getElement().getChildNodes()).iterator();

		Element element = it.next();
		assertEquals("one", element.getLocalName());
		assertEquals(XCAL_NS, element.getNamespaceURI());
		assertEquals("1", element.getTextContent());

		element = it.next();
		assertEquals(appendedElement, element);

		assertFalse(it.hasNext());
	}

	@Test
	public void append_multiple() {
		XCalElement XCalElement = build("<prop />");
		List<Element> elements = XCalElement.append("number", Arrays.asList("1", "2", "3"));
		Iterator<Element> it = elements.iterator();

		Element element = it.next();
		assertEquals("number", element.getLocalName());
		assertEquals(XCAL_NS, element.getNamespaceURI());
		assertEquals("1", element.getTextContent());

		element = it.next();
		assertEquals("number", element.getLocalName());
		assertEquals(XCAL_NS, element.getNamespaceURI());
		assertEquals("2", element.getTextContent());

		element = it.next();
		assertEquals("number", element.getLocalName());
		assertEquals(XCAL_NS, element.getNamespaceURI());
		assertEquals("3", element.getTextContent());

		assertFalse(it.hasNext());

		assertEquals(XmlUtils.toElementList(XCalElement.getElement().getChildNodes()), elements);
	}

	private XCalElement build(String innerXml) {
		return build(innerXml, null);
	}

	private XCalElement build(String innerXml, String prefix) {
		//@formatter:off
		String xml =
		"<%sroot xmlns%s=\"" + XCalNamespaceContext.XCAL_NS + "\">" +
			innerXml +
		"</%sroot>";
		//@formatter:on
		if (prefix == null) {
			xml = String.format(xml, "", "", "", "", "");
		} else {
			xml = String.format(xml, prefix + ":", ":" + prefix, prefix + ":");
		}

		Document document;
		try {
			document = XmlUtils.toDocument(xml);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
		Element element = XmlUtils.getFirstChildElement(XmlUtils.getRootElement(document));
		return new XCalElement(element);
	}
}
