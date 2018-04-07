package biweekly.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertCollectionContains;
import static biweekly.util.TestUtils.assertEqualsAndHash;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import biweekly.ICalVersion;

/*
 Copyright (c) 2013-2018, Michael Angstadt
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
public class EnumPropertyTest {
	@Test
	public void constructors() throws Exception {
		EnumPropertyImpl property = new EnumPropertyImpl();
		assertNull(property.getValue());

		property = new EnumPropertyImpl("value");
		assertEquals("value", property.getValue());
	}

	@Test
	public void set_value() {
		EnumPropertyImpl property = new EnumPropertyImpl();

		property.setValue("value");
		assertEquals("value", property.getValue());
		assertTrue(property.is("value"));
		assertTrue(property.is("VALUE"));
		assertFalse(property.is("notvalue"));
		assertCollectionContains(property.getValueSupportedVersions());

		property.setValue("one");
		assertCollectionContains(property.getValueSupportedVersions(), V1_0);

		property.setValue("two");
		assertCollectionContains(property.getValueSupportedVersions(), V2_0_DEPRECATED, V2_0);
	}

	@Test
	public void getValueSupportedVersions() {
		EnumPropertyDefaultImpl property = new EnumPropertyDefaultImpl();
		assertEquals(Arrays.asList(), property.getValueSupportedVersions());

		property = new EnumPropertyDefaultImpl();
		property.setValue("value");
		assertCollectionContains(property.getValueSupportedVersions(), ICalVersion.values());
	}

	@Test
	public void validate() {
		//null value
		EnumPropertyImpl prop = new EnumPropertyImpl();
		assertValidate(prop).run(26);

		//invalid value
		prop = new EnumPropertyImpl("three");
		assertValidate(prop).run(28);

		prop = new EnumPropertyImpl("");
		assertValidate(prop).run(28);

		prop = new EnumPropertyImpl("ONE");
		assertValidate(prop).versions(V1_0).run();
		assertValidate(prop).versions(V2_0_DEPRECATED, V2_0).run(46);

		prop = new EnumPropertyImpl("TWO");
		assertValidate(prop).versions(V1_0).run(46);
		assertValidate(prop).versions(V2_0_DEPRECATED, V2_0).run();
	}

	@Test
	public void toStringValues() {
		EnumPropertyImpl property = new EnumPropertyImpl();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		EnumPropertyImpl original = new EnumPropertyImpl("value");
		assertCopy(original);

		original = new EnumPropertyImpl();
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new EnumPropertyImpl(),
			new EnumPropertyImpl("one"),
			new EnumPropertyImpl("two")
		);

		assertEqualsMethod(EnumPropertyImpl.class)
		.constructor().test()
		.constructor("value").test();
		//@formatter:on

		EnumPropertyImpl one = new EnumPropertyImpl("one");
		EnumPropertyImpl two = new EnumPropertyImpl("ONE");
		assertEqualsAndHash(one, two);
	}

	public static class EnumPropertyImpl extends EnumProperty {
		public EnumPropertyImpl() {
			super((String) null);
		}

		public EnumPropertyImpl(String value) {
			super(value);
		}

		public EnumPropertyImpl(EnumPropertyImpl original) {
			super(original);
		}

		@Override
		protected Collection<String> getStandardValues(ICalVersion version) {
			switch (version) {
			case V1_0:
				return Arrays.asList("one");
			default:
				return Arrays.asList("TWO");
			}
		}

		@Override
		protected Collection<ICalVersion> getValueSupportedVersions() {
			if (value == null) {
				return Collections.emptyList();
			}

			if ("one".equalsIgnoreCase(value)) {
				return Arrays.asList(V1_0);
			}
			if ("two".equalsIgnoreCase(value)) {
				return Arrays.asList(V2_0, V2_0_DEPRECATED);
			}

			return Collections.emptyList();
		}
	}

	public static class EnumPropertyDefaultImpl extends EnumProperty {
		public EnumPropertyDefaultImpl() {
			super((String) null);
		}

		@Override
		protected Collection<String> getStandardValues(ICalVersion version) {
			return Collections.emptyList();
		}
	}
}
