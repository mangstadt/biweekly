package biweekly.component;

import static biweekly.util.TestUtils.assertEqualsAndHash;
import static biweekly.util.TestUtils.assertEqualsMethodEssentials;
import static biweekly.util.TestUtils.assertSize;
import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import biweekly.Warning;
import biweekly.property.Description;
import biweekly.property.Location;
import biweekly.property.Status;
import biweekly.property.Summary;

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
public class ICalComponentTest {
	@SuppressWarnings("unchecked")
	@Test
	public void checkRequiredCardinality() {
		ICalComponentImpl component = new ICalComponentImpl();
		component.addProperty(new Summary(""));
		component.addProperty(new Description(""));
		component.addProperty(new Description(""));

		List<Warning> warnings = new ArrayList<Warning>();
		component.checkRequiredCardinality(warnings, Summary.class, Description.class, Location.class);

		//too many instances of Description and no instances of Location
		assertWarnings(2, warnings);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void checkOptionalCardinality() {
		ICalComponentImpl component = new ICalComponentImpl();
		component.addProperty(new Summary(""));
		component.addProperty(new Description(""));
		component.addProperty(new Description(""));

		List<Warning> warnings = new ArrayList<Warning>();
		component.checkOptionalCardinality(warnings, Summary.class, Description.class, Location.class);

		//too many instances of Description
		assertWarnings(1, warnings);
	}

	@Test
	public void checkStatus_valid() {
		ICalComponentImpl component = new ICalComponentImpl();
		component.addProperty(Status.cancelled());

		List<Warning> warnings = new ArrayList<Warning>();
		component.checkStatus(warnings, Status.cancelled());

		assertWarnings(0, warnings);
	}

	@Test
	public void checkStatus_invalid() {
		ICalComponentImpl component = new ICalComponentImpl();
		component.addProperty(Status.cancelled());

		List<Warning> warnings = new ArrayList<Warning>();
		component.checkStatus(warnings, Status.completed());

		assertWarnings(1, warnings);
	}

	@Test
	public void checkStatus_null() {
		ICalComponentImpl component = new ICalComponentImpl();

		List<Warning> warnings = new ArrayList<Warning>();
		component.checkStatus(warnings, Status.cancelled());

		assertWarnings(0, warnings);
	}

	@Test
	public void copy() {
		TestComponentWithCopyConstructor component = new TestComponentWithCopyConstructor();
		component.addExperimentalProperty("PROP", "one");
		RawComponent subComponent = new RawComponent("RAW");
		subComponent.addExperimentalProperty("PROP", "two");
		component.addComponent(subComponent);
		TestComponentWithCopyConstructor copy = (TestComponentWithCopyConstructor) component.copy();

		assertNotSame(component, copy);
		assertSize(copy, 1, 1);
		assertNotSame(component.getExperimentalProperty("PROP"), copy.getExperimentalProperty("PROP"));
		assertEquals("one", copy.getExperimentalProperty("PROP").getValue());
		assertNotSame(component.getExperimentalComponent("RAW"), copy.getExperimentalComponent("RAW"));
		assertSize(copy.getExperimentalComponent("RAW"), 0, 1);
		assertNotSame(component.getExperimentalComponent("RAW").getExperimentalProperty("PROP"), copy.getExperimentalComponent("RAW").getExperimentalProperty("PROP"));
		assertEquals("two", copy.getExperimentalComponent("RAW").getExperimentalProperty("PROP").getValue());
	}

	@Test
	public void copy_constructor_throws_exception() {
		RuntimeException exception = new RuntimeException();
		TestComponentCopyConstructorThrowsException component = new TestComponentCopyConstructorThrowsException(exception);
		try {
			component.copy();
			fail("Expected an exception to be thrown.");
		} catch (UnsupportedOperationException e) {
			assertTrue(e.getCause() instanceof InvocationTargetException);
			assertSame(e.getCause().getCause(), exception);
		}
	}

	@Test
	public void copy_no_copy_constructor_or_method() {
		ICalComponentImpl component = new ICalComponentImpl();
		try {
			component.copy();
			fail("Expected an exception to be thrown.");
		} catch (UnsupportedOperationException e) {
			assertTrue(e.getCause() instanceof NoSuchMethodException);
		}
	}

	@Test
	public void equals_essentials() {
		ICalComponentImpl one = new ICalComponentImpl();
		assertEqualsMethodEssentials(one);
	}

	@Test
	public void equals_different_number_of_properties() {
		ICalComponentImpl one = new ICalComponentImpl();
		one.addExperimentalProperty("PROP", "value");

		ICalComponentImpl two = new ICalComponentImpl();
		two.addExperimentalProperty("PROP", "value");
		two.addExperimentalProperty("PROP", "value");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_different_number_of_components() {
		ICalComponentImpl one = new ICalComponentImpl();
		one.addExperimentalComponent("COMP");

		ICalComponentImpl two = new ICalComponentImpl();
		two.addExperimentalComponent("COMP");
		two.addExperimentalComponent("COMP");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_properties_not_equal() {
		ICalComponentImpl one = new ICalComponentImpl();
		one.addExperimentalProperty("PROP", "one");

		ICalComponentImpl two = new ICalComponentImpl();
		one.addExperimentalProperty("PROP", "two");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_ignore_order() {
		ICalComponentImpl one = new ICalComponentImpl();
		one.addExperimentalProperty("PROP", "one");
		one.addExperimentalProperty("PROP", "two");
		one.addExperimentalProperty("PROP", "three");
		one.addExperimentalComponent("COMP");

		ICalComponentImpl two = new ICalComponentImpl();
		two.addExperimentalComponent("COMP");
		two.addExperimentalProperty("PROP", "two");
		two.addExperimentalProperty("PROP", "one");
		two.addExperimentalProperty("PROP", "three");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void equals_multiple_identical_properties() {
		ICalComponentImpl one = new ICalComponentImpl();
		one.addExperimentalProperty("PROP", "one");
		one.addExperimentalProperty("PROP", "one");
		one.addExperimentalProperty("PROP", "two");

		ICalComponentImpl two = new ICalComponentImpl();
		two.addExperimentalProperty("PROP", "one");
		two.addExperimentalProperty("PROP", "two");
		two.addExperimentalProperty("PROP", "one");

		assertEqualsAndHash(one, two);
	}

	/**
	 * This tests to make sure that, if some hashing mechanism is used to
	 * determine equality, identical properties in the same vCard are not
	 * treated as a single property when they are put in a HashSet.
	 */
	@Test
	public void equals_multiple_identical_properties_not_equal() {
		ICalComponentImpl one = new ICalComponentImpl();
		one.addExperimentalProperty("PROP", "one");
		one.addExperimentalProperty("PROP", "one");
		one.addExperimentalProperty("PROP", "two");

		ICalComponentImpl two = new ICalComponentImpl();
		two.addExperimentalProperty("PROP", "one");
		two.addExperimentalProperty("PROP", "two");
		two.addExperimentalProperty("PROP", "two");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	private static class ICalComponentImpl extends ICalComponent {
		//empty
	}

	private static class TestComponentWithCopyConstructor extends ICalComponent {
		public TestComponentWithCopyConstructor() {
			//empty
		}

		@SuppressWarnings("unused")
		public TestComponentWithCopyConstructor(TestComponentWithCopyConstructor original) {
			super(original);
		}
	}

	private static class TestComponentCopyConstructorThrowsException extends ICalComponent {
		private final RuntimeException exception;

		public TestComponentCopyConstructorThrowsException(RuntimeException exception) {
			this.exception = exception;
		}

		@SuppressWarnings("unused")
		public TestComponentCopyConstructorThrowsException(TestComponentCopyConstructorThrowsException original) {
			throw original.exception;
		}
	}
}
