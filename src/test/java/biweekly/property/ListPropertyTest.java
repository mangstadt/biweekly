package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertEqualsAndHash;
import static biweekly.util.TestUtils.assertEqualsMethodEssentials;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

/*
 Copyright (c) 2013-2020, Michael Angstadt
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
public class ListPropertyTest {
	@Test
	public void constructors() throws Exception {
		ListProperty<String> property = new ListProperty<String>();
		assertEquals(Arrays.asList(), property.getValues());

		List<String> list = Arrays.asList("value");
		property = new ListProperty<String>(list);
		assertSame(list, property.getValues());

		property = new ListProperty<String>("value1", "value2");
		assertEquals(Arrays.asList("value1", "value2"), property.getValues());
	}

	@Test(expected = NullPointerException.class)
	public void constructor_null_list() {
		new ListProperty<String>((List<String>) null);
	}

	@Test
	public void validate() {
		ListProperty<String> property = new ListProperty<String>();
		assertValidate(property).run(26);

		property = new ListProperty<String>();
		property.getValues().add("value");
		assertValidate(property).run();
	}

	@Test
	public void toStringValues() {
		ListProperty<String> property = new ListProperty<String>();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		ListProperty<String> original = new ListProperty<String>();
		assertCopy(original).notSame("getValues");

		original = new ListProperty<String>("one", "two");
		assertCopy(original).notSame("getValues");
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new ListProperty<String>(),
			new ListProperty<String>((String)null),
			new ListProperty<String>("one"),
			new ListProperty<String>("two"),
			new ListProperty<String>("one", "two"),
			new ListProperty<Integer>(1)
		);
		//@formatter:on

		ListProperty<String> one = new ListProperty<String>();
		assertEqualsMethodEssentials(one);

		ListProperty<String> two = new ListProperty<String>();
		assertEqualsAndHash(one, two);

		one.getValues().add("value");
		two.getValues().add("value");
		assertEqualsAndHash(one, two);
	}
}
