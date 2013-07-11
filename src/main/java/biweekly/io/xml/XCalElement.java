package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
 * Wraps xCal functionality around an XML {@link Element} object.
 * @author Michael Angstadt
 */
public class XCalElement {
	private final Element element;
	private final Document document;

	/**
	 * Creates a new xCal element.
	 * @param element the XML element to wrap
	 */
	public XCalElement(Element element) {
		this.element = element;
		document = element.getOwnerDocument();
	}

	/**
	 * Gets the first value of the given data type.
	 * @param dataType the data type to look for or null for the "unknown" data
	 * type
	 * @return the value or null if not found
	 */
	public String first(Value dataType) {
		String dataTypeStr = (dataType == null) ? "unknown" : dataType.getValue().toLowerCase();
		return first(dataTypeStr);
	}

	/**
	 * Gets the first value of the "unknown" data type.
	 * @return the value or null if not found
	 */
	public String firstUnknown() {
		return first((Value) null);
	}

	/**
	 * Gets the value of the first child element with the given name.
	 * @param localName the name of the element
	 * @return the element's text or null if not found
	 */
	public String first(String localName) {
		for (Element child : children()) {
			if (localName.equals(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
				return child.getTextContent();
			}
		}
		return null;
	}

	/**
	 * Gets all the values of a given data type.
	 * @param dataType the data type to look for
	 * @return the values
	 */
	public List<String> all(Value dataType) {
		return all(dataType.getValue().toLowerCase());
	}

	/**
	 * Gets the values of all child elements that have the given name.
	 * @param localName the element name
	 * @return the values of the child elements
	 */
	public List<String> all(String localName) {
		List<String> childrenText = new ArrayList<String>();
		for (Element child : children()) {
			if (localName.equals(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
				String text = child.getTextContent();
				childrenText.add(text);
			}
		}
		return childrenText;
	}

	/**
	 * Adds a value with the "unknown" data type.
	 * @param value the value
	 * @return the created element
	 */
	public Element appendUnknown(String value) {
		return append((Value) null, value);
	}

	/**
	 * Adds a value.
	 * @param dataType the data type or null for the "unknown" data type
	 * @param value the value
	 * @return the created element
	 */
	public Element append(Value dataType, String value) {
		String dataTypeStr = (dataType == null) ? "unknown" : dataType.getValue().toLowerCase();
		return append(dataTypeStr, value);
	}

	/**
	 * Adds a child element.
	 * @param name the name of the child element
	 * @param value the value of the child element.
	 * @return the created element
	 */
	public Element append(String name, String value) {
		Element child = document.createElementNS(XCAL_NS, name);
		child.setTextContent(value);
		element.appendChild(child);
		return child;
	}

	/**
	 * Adds a child element.
	 * @param name the name of the child element
	 * @return the created element
	 */
	public XCalElement append(String name) {
		return new XCalElement(append(name, (String) null));
	}

	/**
	 * Adds an empty value.
	 * @param dataType the data type
	 * @return the created element
	 */
	public XCalElement append(Value dataType) {
		return append(dataType.getValue().toLowerCase());
	}

	/**
	 * Adds multiple child elements, each with the same name.
	 * @param name the name for all the child elements
	 * @param values the values of each child element
	 * @return the created elements
	 */
	public List<Element> append(String name, Collection<String> values) {
		List<Element> elements = new ArrayList<Element>(values.size());
		for (String value : values) {
			elements.add(append(name, value));
		}
		return elements;
	}

	/**
	 * Gets the owner document.
	 * @return the owner document
	 */
	public Document document() {
		return document;
	}

	/**
	 * Gets the wrapped XML element.
	 * @return the wrapped XML element
	 */
	public Element getElement() {
		return element;
	}

	/**
	 * Gets the child elements of the wrapped XML element.
	 * @return the child elements
	 */
	private List<Element> children() {
		return XmlUtils.toElementList(element.getChildNodes());
	}

	/**
	 * Gets all child elements with the given data type.
	 * @param dataType the data type
	 * @return the child elements
	 */
	public List<XCalElement> children(Value dataType) {
		String localName = dataType.getValue().toLowerCase();
		List<XCalElement> children = new ArrayList<XCalElement>();
		for (Element child : children()) {
			if (localName.equals(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
				children.add(new XCalElement(child));
			}
		}
		return children;
	}

	/**
	 * Gets the first child element with the given data type.
	 * @param dataType the data type
	 * @return the child element or null if not found
	 */
	public XCalElement child(Value dataType) {
		String localName = dataType.getValue().toLowerCase();
		for (Element child : children()) {
			if (localName.equals(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
				return new XCalElement(child);
			}
		}
		return null;
	}
}
