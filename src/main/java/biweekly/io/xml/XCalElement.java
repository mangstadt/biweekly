package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
	private List<Element> children;

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
	 * @param dataType the data type to look for
	 * @return the value or null if not found
	 */
	public String getValue(Value dataType) {
		return getValue(dataType.getValue().toLowerCase());
	}

	/**
	 * Gets the first value of the "unknown" data type.
	 * @return the value or null if not found
	 */
	public String getValueUnknown() {
		return getValue("unknown");
	}

	/**
	 * Gets the value of the first child element with one of the given names.
	 * @param names the possible names of the element
	 * @return the element's text or null if not found
	 */
	public String getValue(String... names) {
		List<String> localNamesList = Arrays.asList(names);
		for (Element child : children()) {
			if (localNamesList.contains(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
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
	public List<String> getValues(Value dataType) {
		return getValues(dataType.getValue().toLowerCase());
	}

	/**
	 * Gets the value of all non-empty child elements that have the given name.
	 * @param localName the element name
	 * @return the values of the child elements
	 */
	public List<String> getValues(String localName) {
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
	public Element appendValueUnknown(String value) {
		return appendValue("unknown", value);
	}

	/**
	 * Adds a value.
	 * @param dataType the data type
	 * @param value the value
	 * @return the created element
	 */
	public Element appendValue(Value dataType, String value) {
		return appendValue(dataType.getValue().toLowerCase(), value);
	}

	/**
	 * Adds a child element.
	 * @param name the name of the child element
	 * @param value the value of the child element.
	 * @return the created element
	 */
	public Element appendValue(String name, String value) {
		Element child = document.createElementNS(XCAL_NS, name);
		child.setTextContent(value);
		element.appendChild(child);

		if (children != null) {
			children.add(child);
		}

		return child;
	}

	/**
	 * Adds multiple child elements, each with the same name.
	 * @param name the name for all the child elements
	 * @param values the values of each child element
	 * @return the created elements
	 */
	public List<Element> appendValues(String name, Collection<String> values) {
		List<Element> elements = new ArrayList<Element>(values.size());
		for (String value : values) {
			elements.add(appendValue(name, value));
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
	 * Gets the child elements of the XML element.
	 * @return the child elements
	 */
	private List<Element> children() {
		if (children == null) {
			children = Collections.unmodifiableList(XmlUtils.toElementList(element.getChildNodes()));
		}
		return children;
	}
}
