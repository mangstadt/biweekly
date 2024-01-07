package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import biweekly.util.XmlUtils;

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
public class XmlTest {
	@Test
	public void constructors() throws Exception {
		Xml property = new Xml((Document) null);
		assertNull(property.getValue());

		property = new Xml((Element) null);
		assertNull(property.getValue());

		Document value = XmlUtils.toDocument("<root><child/></root>");
		property = new Xml((Element) value.getDocumentElement().getFirstChild());
		assertXMLEqual(XmlUtils.toDocument("<child/>"), property.getValue());

		property = new Xml("<root/>");
		assertXMLEqual(XmlUtils.toDocument("<root/>"), property.getValue());
	}

	@Test(expected = SAXException.class)
	public void constructors_invalid_xml() throws Throwable {
		new Xml("not valid XML");
	}

	@Test
	public void set_value() throws Exception {
		Document value = XmlUtils.toDocument("<root/>");
		Xml property = new Xml(value);

		Document value2 = XmlUtils.toDocument("<root2/>");
		property.setValue(value2);
		assertSame(value2, property.getValue());

		property.setValue(null);
		assertNull(property.getValue());
	}

	@Test
	public void validate() throws Throwable {
		Xml empty = new Xml((Document) null);
		assertValidate(empty).run(26);

		Xml withValue = new Xml("<root/>");
		assertValidate(withValue).run();
	}

	@Test
	public void toStringValues() throws Exception {
		Xml property = new Xml(XmlUtils.toDocument("<root/>"));
		assertFalse(property.toStringValues().isEmpty());

		property = new Xml((Document) null);
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() throws Exception {
		Xml original = new Xml((Document) null);
		assertCopy(original);

		original = new Xml("<root/>");
		assertCopy(original).notSame("getValue");

		original = new Xml(XmlUtils.createDocument());
		assertCopy(original);
	}

	@Test
	public void equals() throws Exception {
		//@formatter:off
		assertNothingIsEqual(
			new Xml((Document)null),
			new Xml("<root/>"),
			new Xml("<root2/>")
		);
		
		assertEqualsMethod(Xml.class, "<root/>")
		.constructor(new Class<?>[]{String.class}, (String)null).test()
		.constructor("<root/>").test();
		//@formatter:on
	}
}
