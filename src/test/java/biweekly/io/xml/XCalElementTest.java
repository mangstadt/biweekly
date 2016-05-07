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

import biweekly.ICalDataType;
import biweekly.io.xml.XCalElement.XCalValue;
import biweekly.util.XmlUtils;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<one>1</one>" +
			"<two>2</two>" +
		"</prop>"
		);
		//@formatter:on
		assertEquals("2", xcalElement.first("two"));
	}

	@Test
	public void first_value_object() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<text>1</text>" +
		"</prop>"
		);
		//@formatter:on
		assertEquals("1", xcalElement.first(ICalDataType.TEXT));
	}

	@Test
	public void first_unknown() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<unknown>1</unknown>" +
		"</prop>"
		);
		//@formatter:on
		assertEquals("1", xcalElement.first((ICalDataType) null));
	}

	@Test
	public void first_with_prefix() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<v:prop>" +
			"<v:one>1</v:one>" +
			"<v:two>2</v:two>" +
		"</v:prop>", "v"
		);
		//@formatter:on
		assertEquals("2", xcalElement.first("two"));
	}

	@Test
	public void first_empty() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<one>1</one>" +
			"<two></two>" +
		"</prop>"
		);
		//@formatter:on
		assertEquals("", xcalElement.first("two"));
	}

	@Test
	public void first_none() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<one>1</one>" +
			"<two>2</two>" +
		"</prop>"
		);
		//@formatter:on
		assertNull(xcalElement.first("three"));
	}

	@Test
	public void first_ignore_other_namespaces() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<n:four xmlns:n=\"http://example.com\"></n:four>" +
		"</prop>"
		);
		//@formatter:on
		assertNull(xcalElement.first("four"));
	}

	@Test
	public void all() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<one>1</one>" +
			"<two>2</two>" +
			"<two />" +
			"<three>3</three>" +
			"<two>2-2</two>" +
		"</prop>"
		);
		//@formatter:on
		assertEquals(Arrays.asList("2", "", "2-2"), xcalElement.all("two"));
	}

	@Test
	public void all_value_object() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<one>1</one>" +
			"<text>2</text>" +
			"<text />" +
			"<three>3</three>" +
			"<text>2-2</text>" +
		"</prop>"
		);
		//@formatter:on
		assertEquals(Arrays.asList("2", "", "2-2"), xcalElement.all(ICalDataType.TEXT));
	}

	@Test
	public void all_unknown() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<one>1</one>" +
			"<unknown>2</unknown>" +
			"<unknown />" +
			"<three>3</three>" +
			"<unknown>2-2</unknown>" +
		"</prop>"
		);
		//@formatter:on
		assertEquals(Arrays.asList("2", "", "2-2"), xcalElement.all((ICalDataType) null));
	}

	@Test
	public void all_none() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<one>1</one>" +
			"<two>2</two>" +
		"</prop>"
		);
		//@formatter:on
		assertTrue(xcalElement.all("three").isEmpty());
	}

	@Test
	public void all_ignore_other_namespaces() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<n:one xmlns:n=\"http://example.com\">1</n:one>" +
		"</prop>"
		);
		//@formatter:on
		assertTrue(xcalElement.all("one").isEmpty());
	}

	@Test
	public void children() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<text>" +
				"<integer>1</integer>" +
			"</text>" +
			"<text>" +
				"<integer>2</integer>" +
			"</text>" +
		"</prop>"
		);
		//@formatter:on
		Iterator<XCalElement> it = xcalElement.children(ICalDataType.TEXT).iterator();

		XCalElement child = it.next();
		assertEquals("1", child.first(ICalDataType.INTEGER));

		child = it.next();
		assertEquals("2", child.first(ICalDataType.INTEGER));

		assertFalse(it.hasNext());
	}

	@Test
	public void child() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<text>" +
				"<integer>1</integer>" +
			"</text>" +
			"<text>" +
				"<integer>2</integer>" +
			"</text>" +
		"</prop>"
		);
		//@formatter:on
		XCalElement child = xcalElement.child(ICalDataType.TEXT);
		assertEquals("1", child.first(ICalDataType.INTEGER));
	}

	@Test
	public void children_ignore_other_namespaces() {
		//@formatter:off
		XCalElement xcalElement = build(
		"<prop>" +
			"<n:text xmlns:n=\"http://example.com\">" +
				"<integer>3</integer>" +
			"</n:text>" +
		"</prop>"
		);
		//@formatter:on
		Iterator<XCalElement> it = xcalElement.children(ICalDataType.TEXT).iterator();

		assertFalse(it.hasNext());
	}

	@Test
	public void firstValue() {
		XCalElement XCalElement = build("<prop><text>one</text></prop>");
		XCalValue child = XCalElement.firstValue();
		assertEquals(ICalDataType.TEXT, child.getDataType());
		assertEquals("one", child.getValue());
	}

	@Test
	public void firstValue_unknown() {
		XCalElement XCalElement = build("<prop><unknown>one</unknown></prop>");
		XCalValue child = XCalElement.firstValue();
		assertNull(child.getDataType());
		assertEquals("one", child.getValue());
	}

	@Test
	public void firstValue_namespace() {
		XCalElement XCalElement = build("<prop><n:foo xmlns:n=\"http://example.com\">one</n:foo><text>two</text></prop>");
		XCalValue child = XCalElement.firstValue();
		assertEquals(ICalDataType.TEXT, child.getDataType());
		assertEquals("two", child.getValue());
	}

	@Test
	public void firstValue_no_xcard_children() {
		XCalElement XCalElement = build("<prop><n:foo xmlns:n=\"http://example.com\">one</n:foo><n:bar xmlns:n=\"http://example.com\">two</n:bar></prop>");
		XCalValue child = XCalElement.firstValue();
		assertNull(child.getDataType());
		assertEquals("onetwo", child.getValue());
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
		Element appendedElement = xCalElement.append(ICalDataType.TEXT, "2");
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
	public void append_unknown() {
		//@formatter:off
		XCalElement xCalElement = build(
		"<prop>" +
			"<one>1</one>" +
		"</prop>"
		);
		//@formatter:on
		Element appendedElement = xCalElement.append((ICalDataType) null, "2");
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
		XCalElement xcalElement = build("<prop />");
		List<Element> elements = xcalElement.append("number", Arrays.asList("1", "2", "3"));
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

		assertEquals(XmlUtils.toElementList(xcalElement.getElement().getChildNodes()), elements);
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
		Element element = XmlUtils.getFirstChildElement(document.getDocumentElement());
		return new XCalElement(element);
	}
}
