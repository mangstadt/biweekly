package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
public class ValuedPropertyTest {
	@Test
	public void constructors() throws Exception {
		ValuedProperty<String> property = new ValuedProperty<String>((String) null);
		assertNull(property.getValue());

		property = new ValuedProperty<String>("value");
		assertEquals("value", property.getValue());
	}

	@Test
	public void set_value() {
		ValuedProperty<String> property = new ValuedProperty<String>((String) null);

		property.setValue("value");
		assertEquals("value", property.getValue());
	}

	@Test
	public void getValue() {
		ValuedProperty<String> property = new ValuedProperty<String>("value");
		assertEquals("value", ValuedProperty.getValue(property));

		property = new ValuedProperty<String>((String) null);
		assertNull(ValuedProperty.getValue(property));

		assertNull(ValuedProperty.getValue(null));
	}

	@Test
	public void validate() {
		ValuedProperty<String> property = new ValuedProperty<String>((String) null);
		assertValidate(property).run(26);

		property = new ValuedProperty<String>("value");
		assertValidate(property).run();
	}

	@Test
	public void toStringValues() {
		ValuedProperty<String> property = new ValuedProperty<String>("value");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		ValuedProperty<String> original = new ValuedProperty<String>((String) null);
		assertCopy(original);

		original = new ValuedProperty<String>("value");
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new ValuedProperty<String>((String) null),
			new ValuedProperty<String>("value"),
			new ValuedProperty<String>("value2"),
			new ValuedProperty<Integer>(1)
		);

		Class<?> parameterTypes[] = new Class<?>[]{Object.class};
		assertEqualsMethod(ValuedProperty.class, parameterTypes, "value")
		.constructor(parameterTypes, (Object)null).test()
		.constructor(parameterTypes, "value").test();
		//@formatter:on
	}
}
