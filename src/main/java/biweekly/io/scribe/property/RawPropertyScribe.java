package biweekly.io.scribe.property;

import java.util.List;

import org.w3c.dom.Element;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.xml.XCalElement;
import biweekly.io.xml.XCalNamespaceContext;
import biweekly.parameter.ICalParameters;
import biweekly.property.RawProperty;
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
 * Marshals properties that do not have a scribe associated with them.
 * @author Michael Angstadt
 */
public class RawPropertyScribe extends ICalPropertyScribe<RawProperty> {
	public RawPropertyScribe(String propertyName) {
		super(RawProperty.class, propertyName, null);
	}

	@Override
	protected ICalDataType _dataType(RawProperty property, ICalVersion version) {
		return property.getDataType();
	}

	@Override
	protected String _writeText(RawProperty property, WriteContext context) {
		String value = property.getValue();
		if (value != null) {
			return value;
		}

		return "";
	}

	@Override
	protected RawProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return new RawProperty(propertyName, dataType, value);
	}

	@Override
	protected RawProperty _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		Element rawElement = element.getElement();
		String name = rawElement.getLocalName();

		//get the text content of the first child element with the xCard namespace
		List<Element> children = XmlUtils.toElementList(rawElement.getChildNodes());
		for (Element child : children) {
			if (!XCalNamespaceContext.XCAL_NS.equals(child.getNamespaceURI())) {
				continue;
			}

			String dataTypeStr = child.getLocalName();
			ICalDataType dataType = "unknown".equals(dataTypeStr) ? null : ICalDataType.get(dataTypeStr);
			String value = child.getTextContent();
			return new RawProperty(name, dataType, value);
		}

		//get the text content of the property element
		String value = rawElement.getTextContent();
		return new RawProperty(name, null, value);
	}
}
