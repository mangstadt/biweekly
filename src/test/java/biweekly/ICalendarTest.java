package biweekly;

import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;

import biweekly.component.ICalComponent;
import biweekly.property.Geo;
import biweekly.property.ICalProperty;
import biweekly.property.Version;

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
public class ICalendarTest {
	@Test
	public void constructor() {
		ICalendar ical = new ICalendar();
		assertEquals(Version.ICAL, ical.getVersion().getMaxVersion());
		assertNotNull(ical.getProductId());
	}

	@Test
	public void validate() {
		ICalendar ical = new ICalendar();
		assertValidate(ical).run(4);

		ical.addExperimentalComponent("X-TEST");
		assertValidate(ical).run();

		ical.addProperty(new Geo(1.0, 2.0));
		assertValidate(ical).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run(44);
		assertValidate(ical).versions(ICalVersion.V1_0).run();
	}

	@Test
	public void validate_recursive() {
		ICalendar ical = new ICalendar();

		TestComponent outter1 = new TestComponent();
		TestProperty prop1 = new TestProperty();
		outter1.addProperty(prop1);
		TestComponent inner = new TestComponent();
		TestProperty prop2 = new TestProperty();
		inner.addProperty(prop2);
		outter1.addComponent(inner);
		ical.addComponent(outter1);

		TestComponent outter2 = new TestComponent();
		TestProperty prop3 = new TestProperty();
		outter2.addProperty(prop3);
		ical.addComponent(outter2);

		assertValidate(ical).warn(outter1, 1).warn(prop1, 2).warn(inner, 1).warn(prop2, 2).warn(outter2, 1).warn(prop3, 2).run();
	}

	private class TestComponent extends ICalComponent {
		@Override
		protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
			warnings.add(Warning.parse(1));
		}
	}

	private class TestProperty extends ICalProperty {
		@Override
		protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
			warnings.add(Warning.parse(2));
		}
	}
}
