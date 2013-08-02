package biweekly.property;

import static biweekly.util.TestUtils.assertWarnings;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import biweekly.property.EnumProperty;
import biweekly.util.TestUtils.Tests;

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
 * @author Michael Angstadt
 */
public class EnumPropertyTest {
	@Test
	public void validate() {
		Tests tests = new Tests();

		//null value
		tests.add(null, 1);

		//invalid value
		tests.add("three", 1);

		//case-insensitive compare
		tests.add("two", 0);
		tests.add("ONE", 0);

		for (Object[] test : tests) {
			String value = (String) test[0];
			Integer expectedWarnings = (Integer) test[1];

			TestProperty prop = new TestProperty(value);
			assertWarnings(expectedWarnings, prop.validate(null));
		}
	}

	private class TestProperty extends EnumProperty {
		public TestProperty(String value) {
			super(value);
		}

		@Override
		protected Collection<String> getStandardValues() {
			return Arrays.asList("one", "TWO");
		}
	}
}
