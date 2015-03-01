package biweekly;

import static biweekly.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import biweekly.ValidationWarnings.WarningsGroup;
import biweekly.component.ICalComponent;
import biweekly.property.ICalProperty;

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
public class ValidationWarningsTest {
	@Test
	public void isEmpty() {
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertTrue(warnings.isEmpty());

		groups.add(new WarningsGroup(new TestProperty2(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new Warning("four"))));
		assertFalse(warnings.isEmpty());
	}

	@Test
	public void getByProperty() {
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		WarningsGroup group1 = new WarningsGroup(new TestProperty1(), Arrays.<ICalComponent> asList(new Grandparent(), new Parent()), Arrays.asList(new Warning("one"), new Warning("two")));
		groups.add(group1);
		WarningsGroup group2 = new WarningsGroup(new TestProperty1(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new Warning("three")));
		groups.add(group2);
		groups.add(new WarningsGroup(new TestProperty2(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new Warning("four"))));
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertEquals(Arrays.asList(group1, group2), warnings.getByProperty(TestProperty1.class));
	}

	@Test
	public void getByProperty_empty() {
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.<ICalComponent> asList(new Grandparent(), new Parent()), Arrays.asList(new Warning("one"), new Warning("two"))));
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new Warning("three"))));
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertEquals(Arrays.asList(), warnings.getByProperty(TestProperty2.class));
	}

	@Test
	public void getByComponent() {
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		WarningsGroup group1 = new WarningsGroup(new TestComponent1(), Arrays.<ICalComponent> asList(new Grandparent(), new Parent()), Arrays.asList(new Warning("one"), new Warning("two")));
		groups.add(group1);
		WarningsGroup group2 = new WarningsGroup(new TestComponent1(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new Warning("three")));
		groups.add(group2);
		groups.add(new WarningsGroup(new TestComponent2(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new Warning("four"))));
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertEquals(Arrays.asList(group1, group2), warnings.getByComponent(TestComponent1.class));
	}

	@Test
	public void getByComponent_empty() {
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		groups.add(new WarningsGroup(new TestComponent1(), Arrays.<ICalComponent> asList(new Grandparent(), new Parent()), Arrays.asList(new Warning("one"), new Warning("two"))));
		groups.add(new WarningsGroup(new TestComponent1(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new Warning("three"))));
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertEquals(Arrays.asList(), warnings.getByComponent(TestComponent2.class));
	}

	@Test
	public void toString_() {
		List<WarningsGroup> groups = new ArrayList<WarningsGroup>();
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.<ICalComponent> asList(new Grandparent(), new Parent()), Arrays.asList(new Warning("one"), new Warning("two"))));
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.<ICalComponent> asList(), Arrays.asList(new Warning("three"))));
		groups.add(new WarningsGroup(new TestComponent1(), Arrays.<ICalComponent> asList(new Grandparent(), new Parent()), Arrays.asList(new Warning("four"), new Warning("five"))));
		groups.add(new WarningsGroup(new TestComponent1(), Arrays.<ICalComponent> asList(), Arrays.asList(new Warning("six"))));
		ValidationWarnings warnings = new ValidationWarnings(groups);

		//@formatter:off
		String expected =
		"[Grandparent > Parent > TestProperty1]: one" + NEWLINE +
		"[Grandparent > Parent > TestProperty1]: two" + NEWLINE +
		"[TestProperty1]: three" + NEWLINE +
		"[Grandparent > Parent > TestComponent1]: four" + NEWLINE +
		"[Grandparent > Parent > TestComponent1]: five" + NEWLINE +
		"[TestComponent1]: six";
		//@formatter:on
		String actual = warnings.toString();
		assertEquals(expected, actual);
	}

	private class TestProperty1 extends ICalProperty {
		//empty
	}

	private class TestProperty2 extends ICalProperty {
		//empty
	}

	private class TestComponent1 extends ICalComponent {
		//empty
	}

	private class TestComponent2 extends ICalComponent {
		//empty
	}

	private class Parent extends ICalComponent {
		//empty
	}

	private class Grandparent extends ICalComponent {
		//empty
	}
}
