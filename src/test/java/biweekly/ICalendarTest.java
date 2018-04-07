package biweekly;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertSize;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import biweekly.component.ICalComponent;
import biweekly.component.VEvent;
import biweekly.property.Color;
import biweekly.property.Description;
import biweekly.property.Geo;
import biweekly.property.ICalProperty;
import biweekly.property.LastModified;
import biweekly.property.Name;
import biweekly.property.ProductId;
import biweekly.property.RefreshInterval;
import biweekly.property.Source;
import biweekly.property.Uid;
import biweekly.property.Url;
import biweekly.util.Duration;

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
public class ICalendarTest {
	@Test
	public void constructor() {
		ICalendar ical = new ICalendar();
		assertNotNull(ical.getProductId());
	}

	@Test
	public void validate_no_component() {
		ICalendar ical = new ICalendar();
		assertValidate(ical).versions(V1_0).run();
		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run(4);

		ical.addExperimentalComponent("X-TEST");
		assertValidate(ical).run();

	}

	@Test
	public void validate_cardinality_required() {
		ICalendar ical = new ICalendar();
		ical.addExperimentalComponent("X-SUPPRESS-NO-COMPONENT-WARNING");
		assertValidate(ical).run();

		ProductId prodid = new ProductId("value");
		ical.addProperty(prodid);
		assertValidate(ical).versions(V1_0).run();
		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run(3);
	}

	@Test
	public void validate_cardinality_optional() {
		ICalendar ical = new ICalendar();
		ical.addExperimentalComponent("X-SUPPRESS-NO-COMPONENT-WARNING");
		assertValidate(ical).run();

		ical.addProperty(new Uid("value"));
		ical.addProperty(new LastModified(new Date()));
		ical.addProperty(new Url(""));
		ical.addProperty(new RefreshInterval(new Duration.Builder().hours(1).build()));
		ical.addProperty(new Color("value"));
		ical.addProperty(new Source("value"));
		assertValidate(ical).run();

		ical.addProperty(new Uid("value"));
		ical.addProperty(new LastModified(new Date()));
		ical.addProperty(new Url(""));
		ical.addProperty(new RefreshInterval(new Duration.Builder().hours(1).build()));
		ical.addProperty(new Color("value"));
		ical.addProperty(new Source("value"));
		assertValidate(ical).run(3, 3, 3, 3, 3, 3);
	}

	@Test
	public void validate_geo() {
		ICalendar ical = new ICalendar();
		ical.addExperimentalComponent("X-SUPPRESS-NO-COMPONENT-WARNING");
		ical.addProperty(new Geo(1.0, 2.0));

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run(44);
		assertValidate(ical).versions(V1_0).run();
	}

	@Test
	public void validate_name() {
		ICalendar ical = new ICalendar();
		ical.addExperimentalComponent("X-SUPPRESS-NO-COMPONENT-WARNING");
		assertValidate(ical).run();

		ical.addName("name1");
		assertValidate(ical).run();

		Name name2 = ical.addName("name2");
		assertValidate(ical).run(55);

		name2.setLanguage("fr");
		assertValidate(ical).run();
	}

	@Test
	public void validate_description() {
		ICalendar ical = new ICalendar();
		ical.addExperimentalComponent("X-SUPPRESS-NO-COMPONENT-WARNING");
		assertValidate(ical).run();

		ical.addDescription("description2");
		assertValidate(ical).run();

		Description description2 = ical.addDescription("description2");
		assertValidate(ical).run(55);

		description2.setLanguage("fr");
		assertValidate(ical).run();
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

	@Test
	public void copy() {
		ICalendar ical = new ICalendar();
		ical.setVersion(ICalVersion.V1_0);

		VEvent event = new VEvent();
		event.setSummary("value");
		ical.addEvent(event);

		ICalendar copy = new ICalendar(ical);

		assertEquals(ical.getVersion(), copy.getVersion());
		assertNotSame(ical, copy);
		assertSize(copy, 1, 1);
		assertNotSame(ical.getProductId(), copy.getProductId());
		assertEquals(ical.getProductId().getValue(), copy.getProductId().getValue());

		VEvent eventCopy = copy.getEvents().get(0);
		assertNotSame(event, eventCopy);
		assertSize(eventCopy, 0, 3);
		assertNotSame(event.getUid(), eventCopy.getUid());
		assertEquals(event.getUid().getValue(), eventCopy.getUid().getValue());
		assertNotSame(event.getDateTimeStamp(), eventCopy.getDateTimeStamp());
		assertEquals(event.getDateTimeStamp().getValue(), eventCopy.getDateTimeStamp().getValue());
		assertNotSame(event.getSummary(), eventCopy.getSummary());
		assertEquals(event.getSummary().getValue(), eventCopy.getSummary().getValue());

	}

	private class TestComponent extends ICalComponent {
		@Override
		protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
			warnings.add(new ValidationWarning(1));
		}
	}

	private class TestProperty extends ICalProperty {
		@Override
		protected void validate(List<ICalComponent> components, ICalVersion version, List<ValidationWarning> warnings) {
			warnings.add(new ValidationWarning(2));
		}
	}
}
