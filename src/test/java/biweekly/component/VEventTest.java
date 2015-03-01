package biweekly.component;

import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;

import java.util.Date;

import org.junit.Test;

import biweekly.ICalVersion;
import biweekly.property.Classification;
import biweekly.property.Created;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.Description;
import biweekly.property.Geo;
import biweekly.property.LastModified;
import biweekly.property.Location;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.property.Priority;
import biweekly.property.RecurrenceId;
import biweekly.property.RecurrenceRule;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Transparency;
import biweekly.property.Url;
import biweekly.util.Duration;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.Frequency;

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
public class VEventTest {
	@Test
	public void validate_required() {
		TestComponent parent = new TestComponent();
		VEvent component = new VEvent();
		component.getProperties().clear();
		assertValidate(component).parents(parent).versions(ICalVersion.V1_0).run();
		assertValidate(component).parents(parent).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run(2, 2, 14);
	}

	@Test
	public void validate_optional() {
		TestComponent parent = new TestComponent();
		VEvent component = new VEvent();
		component.addProperty(Classification.confidential());
		component.addProperty(Classification.confidential());
		component.addProperty(new Created(new Date()));
		component.addProperty(new Created(new Date()));
		component.addProperty(new Description(""));
		component.addProperty(new Description(""));
		component.addProperty(new Geo(1.1, 1.1));
		component.addProperty(new Geo(1.1, 1.1));
		component.addProperty(new LastModified(new Date()));
		component.addProperty(new LastModified(new Date()));
		component.addProperty(new Location(""));
		component.addProperty(new Location(""));
		component.addProperty(new Organizer(null, null));
		component.addProperty(new Organizer(null, null));
		component.addProperty(new Priority(1));
		component.addProperty(new Priority(1));
		Status status1 = Status.cancelled();
		component.addProperty(status1);
		Status status2 = Status.cancelled();
		component.addProperty(status2);
		component.addProperty(new Summary(""));
		component.addProperty(new Summary(""));
		component.addProperty(Transparency.opaque());
		component.addProperty(Transparency.opaque());
		component.addProperty(new Url(""));
		component.addProperty(new Url(""));
		component.addProperty(new RecurrenceId(new Date()));
		component.addProperty(new RecurrenceId(new Date()));

		assertValidate(component).parents(parent).versions(ICalVersion.V1_0).warn(status1, 46).warn(status2, 46).run();
		assertValidate(component).parents(parent).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 14);
	}

	@Test
	public void validate_status() {
		TestComponent parent = new TestComponent();
		VEvent component = new VEvent();
		Status status = Status.draft();
		component.setStatus(status);

		assertValidate(component).parents(parent).versions(ICalVersion.V1_0).warn(status, 46).run(13);
		assertValidate(component).parents(parent).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run(13, 14);
	}

	@Test
	public void validate_dateStart_and_method_relationship() {
		TestComponent parent = new TestComponent();
		VEvent component = new VEvent();
		component.setDateStart(new Date());
		assertValidate(component).parents(parent).run();

		parent = new TestComponent();
		parent.addProperty(new Method(""));
		component = new VEvent();
		assertValidate(component).parents(parent).run();
	}

	@Test
	public void validate_dateEnd_without_dateStart() {
		TestComponent parent = new TestComponent();
		parent.addProperty(new Method(""));
		VEvent component = new VEvent();
		component.setDateEnd(new Date());
		assertValidate(component).parents(parent).run(15);
	}

	@Test
	public void validate_dateStart_before_dateEnd() {
		TestComponent parent = new TestComponent();
		VEvent component = new VEvent();
		component.setDateStart(date("2000-01-10"));
		component.setDateEnd(date("2000-01-01"));
		assertValidate(component).parents(parent).run(16);
	}

	@Test
	public void validate_different_date_datatypes() {
		TestComponent parent = new TestComponent();
		VEvent component = new VEvent();
		component.setDateStart(new DateStart(date("2000-01-01"), false));
		component.setDateEnd(new DateEnd(date("2000-01-10"), true));
		assertValidate(component).parents(parent).run(17);

		parent = new TestComponent();
		component = new VEvent();
		component.setDateStart(new DateStart(date("2000-01-01"), false));
		component.setDateEnd(new DateEnd(date("2000-01-10"), false));
		component.setRecurrenceId(new RecurrenceId(date("2000-01-01"), true));
		assertValidate(component).parents(parent).run(19);
	}

	@Test
	public void validate_dateEnd_with_duration() {
		TestComponent parent = new TestComponent();
		VEvent component = new VEvent();
		component.setDateStart(date("2000-01-01"));
		component.setDateEnd(date("2000-01-10"));
		component.setDuration(new Duration.Builder().build());
		assertValidate(component).parents(parent).run(18);
	}

	@Test
	public void validate_time_in_rrule() {
		//@formatter:off
		Recurrence[] recurrences = {
			new Recurrence.Builder(Frequency.DAILY).byHour(1).build(),
			new Recurrence.Builder(Frequency.DAILY).byMinute(1).build(),
			new Recurrence.Builder(Frequency.DAILY).bySecond(1).build()
		};
		//@formatter:on
		for (Recurrence recurrence : recurrences) {
			TestComponent parent = new TestComponent();
			VEvent component = new VEvent();
			component.setDateStart(new DateStart(date("2000-01-01"), false));
			component.setDateEnd(new DateEnd(date("2000-01-10"), false));
			component.setRecurrenceRule(recurrence);
			assertValidate(component).parents(parent).run(5);
		}
	}

	@Test
	public void validate_multiple_rrules() {
		TestComponent parent = new TestComponent();
		VEvent component = new VEvent();
		component.setDateStart(new DateStart(date("2000-01-01"), false));
		component.setDateEnd(new DateEnd(date("2000-01-10"), false));
		component.addProperty(new RecurrenceRule(new Recurrence.Builder(Frequency.DAILY).build()));
		component.addProperty(new RecurrenceRule(new Recurrence.Builder(Frequency.DAILY).build()));
		assertValidate(component).parents(parent).run(6);
	}

	private static class TestComponent extends ICalComponent {
		//empty
	}
}
