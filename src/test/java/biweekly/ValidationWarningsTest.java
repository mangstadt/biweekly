package biweekly;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import biweekly.component.ICalComponent;
import biweekly.property.ICalProperty;
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
public class ValidationWarningsTest {
	private final String NEWLINE = System.getProperty("line.separator");

	@Test
	public void toString_() {
		//@formatter:off
		Tests tests = new Tests();
		tests.add(
			"[Grandparent > Parent > TestComponent]: one",
			new ValidationWarnings(new TestComponent(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList("one"))
		);
		tests.add(
			"[Grandparent > Parent > TestComponent]: one" + NEWLINE + 
			"[Grandparent > Parent > TestComponent]: two",
			new ValidationWarnings(new TestComponent(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList("one", "two"))
		);
		tests.add(
			"[TestComponent]: one",
			new ValidationWarnings(new TestComponent(), Arrays.asList(new ICalComponent[0]), Arrays.asList("one"))
		);
		tests.add(
			"[Grandparent > Parent > TestProperty]: one",
			new ValidationWarnings(new TestProperty(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList("one"))
		);
		tests.add(
			"[Grandparent > Parent > TestProperty]: one" + NEWLINE + 
			"[Grandparent > Parent > TestProperty]: two",
			new ValidationWarnings(new TestProperty(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList("one", "two"))
		);
		tests.add(
			"[TestProperty]: one",
			new ValidationWarnings(new TestProperty(), Arrays.asList(new ICalComponent[0]), Arrays.asList("one"))
		);
		//@formatter:on

		for (Object[] test : tests) {
			String expected = (String) test[0];
			ValidationWarnings warnings = (ValidationWarnings) test[1];
			String actual = warnings.toString();

			assertEquals(expected, actual);
		}
	}

	private class TestProperty extends ICalProperty {
		//empty
	}

	private class TestComponent extends ICalComponent {
		//empty
	}

	private class Parent extends ICalComponent {
		//empty
	}

	private class Grandparent extends ICalComponent {
		//empty
	}
}
