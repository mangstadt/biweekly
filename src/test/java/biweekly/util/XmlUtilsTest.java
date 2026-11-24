package biweekly.util;

import static biweekly.util.TestUtils.assertXMLEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
class XmlUtilsTest {
	//@formatter:off
	private final String xml =
		"<root>" +
			"<child1 />" +
			"some text" +
			"<n:child2 xmlns:n=\"http://example.com\" />" +
		"</root>";
	//@formatter:on

	@Test
	void createDocument() {
		Document document = XmlUtils.createDocument();
		assertNotNull(document);
	}

	@Test
	void toDocument() throws Exception {
		Document document = XmlUtils.toDocument(xml);

		Element root = (Element) document.getFirstChild();
		assertEquals("root", root.getLocalName());
		assertNull(root.getNamespaceURI());

		NodeList nodes = root.getChildNodes();
		assertEquals(3, nodes.getLength());

		Element element = (Element) nodes.item(0);
		assertEquals("child1", element.getLocalName());
		assertNull(element.getNamespaceURI());

		Text text = (Text) nodes.item(1);
		assertEquals("some text", text.getTextContent());

		element = (Element) nodes.item(2);
		assertEquals("child2", element.getLocalName());
		assertEquals("http://example.com", element.getNamespaceURI());
	}

	@Test
	void toDocument_utf8(@TempDir Path tempDir) throws Exception {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
		"<root><child>\u1e08hild</child></root>";
		//@formatter:on

		Path file = tempDir.resolve("test.xml");
		Writer writer = Utf8Streams.writer(file);
		writer.write(xml);
		writer.close();

		Document document = XmlUtils.toDocument(Files.newInputStream(file));
		Element root = (Element) document.getFirstChild();
		NodeList nodes = root.getChildNodes();
		Element child = (Element) nodes.item(0);
		assertEquals("\u1e08hild", child.getTextContent());
	}

	@Test
	void toDocument_invalid_xml() {
		String xml = "not-xml";
		assertThrows(SAXException.class, () -> XmlUtils.toDocument(xml));
	}

	@Test
	void toString_() throws Exception {
		Document expected = XmlUtils.toDocument(xml);
		String string = XmlUtils.toString(expected);
		Document actual = XmlUtils.toDocument(string);
		assertXMLEqual(expected, actual);
	}

	@Test
	void toString_output_properties() throws Exception {
		Document expected = XmlUtils.toDocument(xml);
		Map<String, String> outputProperties = new HashMap<>();
		outputProperties.put(OutputKeys.STANDALONE, "no");
		String string = XmlUtils.toString(expected, outputProperties);
		Document actual = XmlUtils.toDocument(string);
		assertXMLEqual(expected, actual);
	}

	@Test
	void toElementList() throws Exception {
		Document document = XmlUtils.toDocument(xml);

		List<Element> elements = XmlUtils.toElementList(document.getFirstChild().getChildNodes());
		assertEquals(2, elements.size());
		assertEquals("child1", elements.get(0).getLocalName());
		assertEquals("child2", elements.get(1).getLocalName());
	}

	@Test
	void toElementList_no_children() throws Exception {
		String xml = "<root/>";
		Document document = XmlUtils.toDocument(xml);

		List<Element> elements = XmlUtils.toElementList(document.getFirstChild().getChildNodes());
		assertTrue(elements.isEmpty());
	}

	@Test
	void getFirstChildElement() throws Exception {
		Document document = XmlUtils.toDocument(xml);
		Element root = (Element) document.getFirstChild();
		Element element = XmlUtils.firstChild(root);
		assertEquals("child1", element.getLocalName());
	}
}
