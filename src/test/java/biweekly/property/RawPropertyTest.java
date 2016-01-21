package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Assert;
import org.junit.Test;

import biweekly.ICalDataType;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
public class RawPropertyTest {
	@Test
	public void constructors() throws Exception {
		RawProperty property = new RawProperty("name", "value");
		assertEquals("name", property.getName());
		assertEquals("value", property.getValue());
		assertNull(property.getDataType());

		property = new RawProperty("name", ICalDataType.TEXT, "value");
		assertEquals("name", property.getName());
		assertEquals("value", property.getValue());
		assertEquals(ICalDataType.TEXT, property.getDataType());
	}

	@Test
	public void set_value() {
		RawProperty property = new RawProperty("name", "value");

		property.setName("name2");
		assertEquals("name2", property.getName());
		assertEquals("value", property.getValue());
		assertNull(property.getDataType());

		property.setValue("value2");
		assertEquals("name2", property.getName());
		assertEquals("value2", property.getValue());
		assertNull(property.getDataType());

		property.setDataType(ICalDataType.TEXT);
		assertEquals("name2", property.getName());
		assertEquals("value2", property.getValue());
		assertEquals(ICalDataType.TEXT, property.getDataType());
	}

	@Test
	public void validate() {
		RawProperty property = new RawProperty("foo:bar", "value");
		assertValidate(property).run(52);

		property = new RawProperty("foobar", "value");
		assertValidate(property).run();
	}

	@Test
	public void toStringValues() {
		RawProperty property = new RawProperty("name", "value");
		Assert.assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		RawProperty original = new RawProperty("name", ICalDataType.TEXT, "value");
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new RawProperty(null, null),
			new RawProperty("name", null),
			new RawProperty(null, "value"),
			new RawProperty(null, ICalDataType.TEXT, null),
			new RawProperty("name", ICalDataType.TEXT, null),
			new RawProperty(null, ICalDataType.TEXT, "value"),
			new RawProperty("name", "value"),
			new RawProperty("name2", "value"),
			new RawProperty("name", "value2"),
			new RawProperty("name2", "value2"),
			new RawProperty("name", ICalDataType.TEXT, "value"),
			new RawProperty("name", ICalDataType.URI, "value")
		);
		
		assertEqualsMethod(RawProperty.class, "name", "value")
		.constructor(new Class<?>[]{String.class, String.class}, null, null).test()
		.constructor("name", "value").test()
		.constructor("NAME", "value").test()
		.constructor("name", ICalDataType.TEXT, "value").test();
		//@formatter:on
	}
}
