package biweekly;

import static biweekly.util.StringUtils.NEWLINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import biweekly.ValidationWarnings.WarningsGroup;
import biweekly.component.ICalComponent;
import biweekly.property.ICalProperty;

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
class ValidationWarningsTest {
	@Test
	void isEmpty() {
		List<WarningsGroup> groups = new ArrayList<>();
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertTrue(warnings.isEmpty());

		groups.add(new WarningsGroup(new TestProperty2(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new ValidationWarning("four"))));
		assertFalse(warnings.isEmpty());
	}

	@Test
	void getByProperty() {
		List<WarningsGroup> groups = new ArrayList<>();
		WarningsGroup group1 = new WarningsGroup(new TestProperty1(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList(new ValidationWarning("one"), new ValidationWarning("two")));
		groups.add(group1);
		WarningsGroup group2 = new WarningsGroup(new TestProperty1(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new ValidationWarning("three")));
		groups.add(group2);
		groups.add(new WarningsGroup(new TestProperty2(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new ValidationWarning("four"))));
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertEquals(Arrays.asList(group1, group2), warnings.getByProperty(TestProperty1.class));
	}

	@Test
	void getByProperty_empty() {
		List<WarningsGroup> groups = new ArrayList<>();
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList(new ValidationWarning("one"), new ValidationWarning("two"))));
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new ValidationWarning("three"))));
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertEquals(Arrays.asList(), warnings.getByProperty(TestProperty2.class));
	}

	@Test
	void getByComponent() {
		List<WarningsGroup> groups = new ArrayList<>();
		WarningsGroup group1 = new WarningsGroup(new TestComponent1(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList(new ValidationWarning("one"), new ValidationWarning("two")));
		groups.add(group1);
		WarningsGroup group2 = new WarningsGroup(new TestComponent1(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new ValidationWarning("three")));
		groups.add(group2);
		groups.add(new WarningsGroup(new TestComponent2(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new ValidationWarning("four"))));
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertEquals(Arrays.asList(group1, group2), warnings.getByComponent(TestComponent1.class));
	}

	@Test
	void getByComponent_empty() {
		List<WarningsGroup> groups = new ArrayList<>();
		groups.add(new WarningsGroup(new TestComponent1(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList(new ValidationWarning("one"), new ValidationWarning("two"))));
		groups.add(new WarningsGroup(new TestComponent1(), Arrays.<ICalComponent> asList(new Parent()), Arrays.asList(new ValidationWarning("three"))));
		ValidationWarnings warnings = new ValidationWarnings(groups);
		assertEquals(Arrays.asList(), warnings.getByComponent(TestComponent2.class));
	}

	@Test
	void toString_() {
		List<WarningsGroup> groups = new ArrayList<>();
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList(new ValidationWarning("one"), new ValidationWarning("two"))));
		groups.add(new WarningsGroup(new TestProperty1(), Arrays.<ICalComponent> asList(), Arrays.asList(new ValidationWarning("three"))));
		groups.add(new WarningsGroup(new TestComponent1(), Arrays.asList(new Grandparent(), new Parent()), Arrays.asList(new ValidationWarning("four"), new ValidationWarning("five"))));
		groups.add(new WarningsGroup(new TestComponent1(), Arrays.<ICalComponent> asList(), Arrays.asList(new ValidationWarning("six"))));
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
