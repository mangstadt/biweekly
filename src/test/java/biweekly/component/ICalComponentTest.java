package biweekly.component;

import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertEqualsAndHash;
import static biweekly.util.TestUtils.assertEqualsMethodEssentials;
import static biweekly.util.TestUtils.assertSize;
import static biweekly.util.TestUtils.assertWarnings;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.Warning;
import biweekly.property.Description;
import biweekly.property.Location;
import biweekly.property.RawProperty;
import biweekly.property.Status;
import biweekly.property.Summary;

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
public class ICalComponentTest {
	@Test
	public void constructors() {
		ICalComponentImpl component = new ICalComponentImpl();
		assertSize(component, 0, 0);
	}

	@Test
	public void getProperty_addProperty() {
		ICalComponentImpl component = new ICalComponentImpl();
		Summary property = new Summary("value");

		assertNull(component.getProperty(Summary.class));

		component.addProperty(property);
		assertSame(property, component.getProperty(Summary.class));

		component.addProperty(new Summary("value2"));
		assertSame(property, component.getProperty(Summary.class));
	}

	@Test
	public void getProperties() {
		ICalComponentImpl component = new ICalComponentImpl();
		List<Summary> list = component.getProperties(Summary.class);

		assertEquals(asList(), list);
		assertEquals(asList(), component.getProperties(Summary.class));

		Summary property1 = new Summary("value1");
		list.add(property1);
		assertEquals(asList(property1), list);
		assertEquals(asList(property1), component.getProperties(Summary.class));

		Summary property2 = new Summary("value2");
		component.addProperty(property2);
		assertEquals(asList(property1, property2), list);
		assertEquals(asList(property1, property2), component.getProperties(Summary.class));

		list.remove(0);
		assertEquals(asList(property2), list);
		assertEquals(asList(property2), component.getProperties(Summary.class));

		Summary property3 = new Summary("value3");
		list.set(0, property3);
		assertEquals(asList(property3), list);
		assertEquals(asList(property3), component.getProperties(Summary.class));

		component.removeProperty(property3);
		assertEquals(asList(), list);
		assertEquals(asList(), component.getProperties(Summary.class));
	}

	@Test
	public void setProperty() {
		ICalComponentImpl component = new ICalComponentImpl();
		Summary property = new Summary("value");
		Summary property2 = new Summary("value2");

		assertEquals(asList(), component.setProperty(property));
		assertEquals(asList(property), component.getProperties(Summary.class));

		component.addProperty(property2);
		assertEquals(asList(property, property2), component.getProperties(Summary.class));
		assertEquals(asList(property, property2), component.setProperty(property));
		assertEquals(asList(property), component.getProperties(Summary.class));

		assertEquals(asList(property), component.setProperty(Summary.class, null));
		assertEquals(asList(), component.getProperties(Summary.class));

		assertEquals(asList(), component.setProperty(Summary.class, property));
		assertEquals(asList(property), component.getProperties(Summary.class));

		assertEquals(asList(property), component.setProperty(Summary.class, property2));
		assertEquals(asList(property2), component.getProperties(Summary.class));
	}

	@Test
	public void removeProperty() {
		ICalComponentImpl component = new ICalComponentImpl();
		Summary property = new Summary("value");
		Summary property2 = new Summary("value2");

		component.addProperty(property);
		assertEquals(asList(property), component.getProperties(Summary.class));

		assertFalse(component.removeProperty(property2));
		assertEquals(asList(property), component.getProperties(Summary.class));

		assertTrue(component.removeProperty(property));
		assertEquals(asList(), component.getProperties(Summary.class));
	}

	@Test
	public void removeProperties() {
		ICalComponentImpl component = new ICalComponentImpl();
		Summary property = new Summary("value");
		Summary property2 = new Summary("value2");

		assertEquals(asList(), component.getProperties(Summary.class));
		assertEquals(asList(), component.removeProperties(Summary.class));

		component.addProperty(property);
		component.addProperty(property2);
		assertEquals(asList(property, property2), component.getProperties(Summary.class));
		assertEquals(asList(property, property2), component.removeProperties(Summary.class));
		assertEquals(asList(), component.getProperties(Summary.class));
	}

	@Test
	public void addExperimentalProperty() {
		ICalComponentImpl component = new ICalComponentImpl();

		RawProperty property = component.addExperimentalProperty("NAME", "value");
		assertEquals("NAME", property.getName());
		assertEquals("value", property.getValue());
		assertNull(property.getDataType());
		assertSame(property, component.getExperimentalProperty("NAME"));

		RawProperty property2 = component.addExperimentalProperty("NAME2", ICalDataType.TEXT, "value2");
		assertEquals("NAME2", property2.getName());
		assertEquals("value2", property2.getValue());
		assertEquals(ICalDataType.TEXT, property2.getDataType());
		assertSame(property2, component.getExperimentalProperty("NAME2"));
	}

	@Test
	public void getExperimentalProperty() {
		ICalComponentImpl component = new ICalComponentImpl();

		assertNull(component.getExperimentalProperty("NAME"));

		component.addExperimentalProperty("NAME2", "value");
		RawProperty property = component.addExperimentalProperty("NAME", "value");
		component.addExperimentalProperty("NAME3", "value");
		assertSame(property, component.getExperimentalProperty("NAME"));
		assertSame(property, component.getExperimentalProperty("name"));

		component.addExperimentalProperty("NAME", "value2");
		assertSame(property, component.getExperimentalProperty("NAME"));
		assertSame(property, component.getExperimentalProperty("name"));
	}

	@Test
	public void getExperimentalProperties_byName() {
		ICalComponentImpl component = new ICalComponentImpl();

		assertEquals(asList(), component.getExperimentalProperties("NAME"));

		component.addExperimentalProperty("NAME2", "value");
		RawProperty property = component.addExperimentalProperty("NAME", "value");
		assertEquals(asList(property), component.getExperimentalProperties("NAME"));
		assertEquals(asList(property), component.getExperimentalProperties("name"));

		RawProperty property2 = component.addExperimentalProperty("NAME", "value2");
		assertEquals(asList(property, property2), component.getExperimentalProperties("NAME"));
		assertEquals(asList(property, property2), component.getExperimentalProperties("name"));
	}

	@Test
	public void getExperimentalProperties() {
		ICalComponentImpl component = new ICalComponentImpl();

		assertEquals(asList(), component.getExperimentalProperties());

		RawProperty property = component.addExperimentalProperty("NAME", "value");
		assertEquals(asList(property), component.getExperimentalProperties());

		RawProperty property2 = component.addExperimentalProperty("NAME2", "value2");
		assertEquals(asList(property, property2), component.getExperimentalProperties());
	}

	@Test
	public void setExperimentalProperty() {
		ICalComponentImpl component = new ICalComponentImpl();

		RawProperty property = component.setExperimentalProperty("NAME", "value");
		assertEquals("NAME", property.getName());
		assertEquals("value", property.getValue());
		assertNull(property.getDataType());
		assertEquals(asList(property), component.getExperimentalProperties("NAME"));

		RawProperty property2 = component.setExperimentalProperty("NAME", ICalDataType.TEXT, "value2");
		assertEquals("NAME", property2.getName());
		assertEquals("value2", property2.getValue());
		assertEquals(ICalDataType.TEXT, property2.getDataType());
		assertEquals(asList(property2), component.getExperimentalProperties("NAME"));
	}

	@Test
	public void removeExperimentalProperties() {
		ICalComponentImpl component = new ICalComponentImpl();

		assertEquals(asList(), component.removeExperimentalProperties("NAME"));

		RawProperty property = component.addExperimentalProperty("NAME", "value");
		RawProperty property2 = component.addExperimentalProperty("NAME", "value2");
		assertEquals(asList(property, property2), component.removeExperimentalProperties("name"));
		assertEquals(asList(), component.getExperimentalProperties("NAME"));
	}

	@Test
	public void getComponent_addComponent() {
		ICalComponentImpl component = new ICalComponentImpl();
		VEvent subComponent = new VEvent();

		assertNull(component.getComponent(VEvent.class));

		component.addComponent(subComponent);
		assertSame(subComponent, component.getComponent(VEvent.class));
	}

	@Test
	public void getComponents() {
		ICalComponentImpl component = new ICalComponentImpl();
		List<VEvent> list = component.getComponents(VEvent.class);

		assertEquals(asList(), list);
		assertEquals(asList(), component.getComponents(VEvent.class));

		VEvent subComponent1 = new VEvent();
		list.add(subComponent1);
		assertEquals(asList(subComponent1), list);
		assertEquals(asList(subComponent1), component.getComponents(VEvent.class));

		VEvent subComponent2 = new VEvent();
		component.addComponent(subComponent2);
		assertEquals(asList(subComponent1, subComponent2), list);
		assertEquals(asList(subComponent1, subComponent2), component.getComponents(VEvent.class));

		list.remove(0);
		assertEquals(asList(subComponent2), list);
		assertEquals(asList(subComponent2), component.getComponents(VEvent.class));

		VEvent subComponent3 = new VEvent();
		list.set(0, subComponent3);
		assertEquals(asList(subComponent3), list);
		assertEquals(asList(subComponent3), component.getComponents(VEvent.class));

		component.removeComponent(subComponent3);
		assertEquals(asList(), list);
		assertEquals(asList(), component.getComponents(VEvent.class));
	}

	@Test
	public void setComponent() {
		ICalComponentImpl component = new ICalComponentImpl();
		VEvent subComponent = new VEvent();
		VEvent subComponent2 = new VEvent();

		assertEquals(asList(), component.setComponent(subComponent));
		assertEquals(asList(subComponent), component.getComponents(VEvent.class));

		component.addComponent(subComponent2);
		assertEquals(asList(subComponent, subComponent2), component.getComponents(VEvent.class));
		assertEquals(asList(subComponent, subComponent2), component.setComponent(subComponent));
		assertEquals(asList(subComponent), component.getComponents(VEvent.class));

		assertEquals(asList(subComponent), component.setComponent(VEvent.class, null));
		assertEquals(asList(), component.getComponents(VEvent.class));

		assertEquals(asList(), component.setComponent(VEvent.class, subComponent));
		assertEquals(asList(subComponent), component.getComponents(VEvent.class));

		assertEquals(asList(subComponent), component.setComponent(VEvent.class, subComponent2));
		assertEquals(asList(subComponent2), component.getComponents(VEvent.class));
	}

	@Test
	public void removeComponent() {
		ICalComponentImpl component = new ICalComponentImpl();
		VEvent subComponent = new VEvent();
		VEvent subComponent2 = new VEvent();

		component.addComponent(subComponent);
		assertEquals(asList(subComponent), component.getComponents(VEvent.class));

		assertFalse(component.removeComponent(subComponent2));
		assertEquals(asList(subComponent), component.getComponents(VEvent.class));

		assertTrue(component.removeComponent(subComponent));
		assertEquals(asList(), component.getComponents(VEvent.class));
	}

	@Test
	public void removeComponents() {
		ICalComponentImpl component = new ICalComponentImpl();
		VEvent subComponent = new VEvent();
		VEvent subComponent2 = new VEvent();

		assertEquals(asList(), component.getComponents(VEvent.class));
		assertEquals(asList(), component.removeComponents(VEvent.class));

		component.addComponent(subComponent);
		component.addComponent(subComponent2);
		assertEquals(asList(subComponent, subComponent2), component.getComponents(VEvent.class));
		assertEquals(asList(subComponent, subComponent2), component.removeComponents(VEvent.class));
		assertEquals(asList(), component.getComponents(VEvent.class));
	}

	@Test
	public void addExperimentalComponent() {
		ICalComponentImpl component = new ICalComponentImpl();

		RawComponent subComponent = component.addExperimentalComponent("NAME");
		assertEquals("NAME", subComponent.getName());
		assertSize(subComponent, 0, 0);
		assertSame(subComponent, component.getExperimentalComponent("NAME"));
	}

	@Test
	public void getExperimentalComponent() {
		ICalComponentImpl component = new ICalComponentImpl();

		assertNull(component.getExperimentalComponent("NAME"));

		component.addExperimentalComponent("NAME2");
		RawComponent subComponent = component.addExperimentalComponent("NAME");
		component.addExperimentalComponent("NAME3");
		assertSame(subComponent, component.getExperimentalComponent("NAME"));
		assertSame(subComponent, component.getExperimentalComponent("name"));

		component.addExperimentalComponent("NAME");
		assertSame(subComponent, component.getExperimentalComponent("NAME"));
		assertSame(subComponent, component.getExperimentalComponent("name"));
	}

	@Test
	public void getExperimentalComponents_byName() {
		ICalComponentImpl component = new ICalComponentImpl();

		assertEquals(asList(), component.getExperimentalComponents("NAME"));

		component.addExperimentalComponent("NAME2");
		RawComponent subComponent = component.addExperimentalComponent("NAME");
		component.addExperimentalComponent("NAME3");
		assertEquals(asList(subComponent), component.getExperimentalComponents("NAME"));
		assertEquals(asList(subComponent), component.getExperimentalComponents("name"));

		RawComponent subComponent2 = component.addExperimentalComponent("NAME");
		assertEquals(asList(subComponent, subComponent2), component.getExperimentalComponents("NAME"));
		assertEquals(asList(subComponent, subComponent2), component.getExperimentalComponents("name"));
	}

	@Test
	public void getExperimentalComponents() {
		ICalComponentImpl component = new ICalComponentImpl();

		assertEquals(asList(), component.getExperimentalComponents());

		RawComponent subComponent = component.addExperimentalComponent("NAME");
		assertEquals(asList(subComponent), component.getExperimentalComponents());

		RawComponent subComponent2 = component.addExperimentalComponent("NAME2");
		assertEquals(asList(subComponent, subComponent2), component.getExperimentalComponents());
	}

	@Test
	public void setExperimentalComponent() {
		ICalComponentImpl component = new ICalComponentImpl();

		RawComponent subComponent = component.setExperimentalComponent("NAME");
		assertEquals("NAME", subComponent.getName());
		assertEquals(asList(subComponent), component.getExperimentalComponents("NAME"));

		RawComponent subComponent2 = component.setExperimentalComponent("NAME");
		assertEquals("NAME", subComponent2.getName());
		assertEquals(asList(subComponent2), component.getExperimentalComponents("NAME"));
	}

	@Test
	public void removeExperimentalComponents() {
		ICalComponentImpl component = new ICalComponentImpl();

		assertEquals(asList(), component.removeExperimentalComponents("NAME"));

		RawComponent subComponent = component.addExperimentalComponent("NAME");
		RawComponent subComponent2 = component.addExperimentalComponent("NAME");
		assertEquals(asList(subComponent, subComponent2), component.removeExperimentalComponents("name"));
		assertEquals(asList(), component.getExperimentalComponents("NAME"));
	}

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
		two.addExperimentalProperty("PROP", "two");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_components_not_equal() {
		ICalComponentImpl one = new ICalComponentImpl();
		one.addExperimentalComponent("COMP");

		ICalComponentImpl two = new ICalComponentImpl();
		two.addExperimentalComponent("COMP2");

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
	 * determine equality, identical properties in the same component are not
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

	@Test
	public void toString_() {
		//@formatter:off
		ICalComponentImpl component = new ICalComponentImpl();
		assertEquals(
			component.getClass().getName() + NEWLINE,
		component.toString());

		component.addExperimentalProperty("NAME", "value");
		assertEquals(
			component.getClass().getName() + NEWLINE +
			"  biweekly.property.RawProperty [ parameters={} | name=NAME | value=value | dataType=null ]" + NEWLINE,
		component.toString());

		RawComponent subComponent = component.addExperimentalComponent("COMP");
		assertEquals(
			component.getClass().getName() + NEWLINE +
			"  biweekly.property.RawProperty [ parameters={} | name=NAME | value=value | dataType=null ]" + NEWLINE +
			"  biweekly.component.RawComponent" + NEWLINE,
		component.toString());

		subComponent.addExperimentalProperty("NAME2", "value");
		assertEquals(
			component.getClass().getName() + NEWLINE +
			"  biweekly.property.RawProperty [ parameters={} | name=NAME | value=value | dataType=null ]" + NEWLINE +
			"  biweekly.component.RawComponent" + NEWLINE +
			"    biweekly.property.RawProperty [ parameters={} | name=NAME2 | value=value | dataType=null ]" + NEWLINE,
		component.toString());
		//@formatter:on
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
