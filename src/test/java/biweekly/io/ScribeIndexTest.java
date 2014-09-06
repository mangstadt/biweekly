package biweekly.io;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VEvent;
import biweekly.io.scribe.ScribeIndex;
import biweekly.property.ICalProperty;

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
public class ScribeIndexTest {
	private final ScribeIndex index = new ScribeIndex();

	@Test
	public void hasScribesFor_no_missing_scribes() {
		ICalendar ical = new ICalendar();

		assertHasScribesFor(ical);
	}

	@Test
	public void hasScribesFor_property_root() {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty());

		assertHasScribesFor(ical, TestProperty.class);
	}

	@Test
	public void hasScribesFor_property_in_component() {
		ICalendar ical = new ICalendar();
		VEvent event = new VEvent();
		event.addProperty(new TestProperty());
		ical.addComponent(event);

		assertHasScribesFor(ical, TestProperty.class);
	}

	@Test
	public void hasScribesFor_component_root() {
		ICalendar ical = new ICalendar();
		ical.addComponent(new TestComponent());

		assertHasScribesFor(ical, TestComponent.class);
	}

	@Test
	public void hasScribesFor_component_in_component() {
		ICalendar ical = new ICalendar();
		VEvent event = new VEvent();
		event.addComponent(new TestComponent());
		ical.addComponent(event);

		assertHasScribesFor(ical, TestComponent.class);
	}

	private void assertHasScribesFor(ICalendar ical, Object... propertiesAndComponents) {
		List<Object> actual = new ArrayList<Object>(index.hasScribesFor(ical));
		List<Object> expected = Arrays.<Object> asList(propertiesAndComponents);
		assertEquals(expected, actual);
	}

	private class TestProperty extends ICalProperty {
		//empty
	}

	private class TestComponent extends ICalComponent {
		//empty
	}
}
