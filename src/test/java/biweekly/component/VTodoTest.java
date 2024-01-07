package biweekly.component;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;

import java.util.Date;

import org.junit.Test;

import biweekly.property.Classification;
import biweekly.property.Color;
import biweekly.property.Completed;
import biweekly.property.Created;
import biweekly.property.DateDue;
import biweekly.property.DateStart;
import biweekly.property.Description;
import biweekly.property.Geo;
import biweekly.property.LastModified;
import biweekly.property.Location;
import biweekly.property.Organizer;
import biweekly.property.PercentComplete;
import biweekly.property.Priority;
import biweekly.property.RecurrenceId;
import biweekly.property.RecurrenceRule;
import biweekly.property.Sequence;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Url;
import biweekly.util.Duration;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;

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
public class VTodoTest {
	@Test
	public void validate_cardinality_required() {
		VTodo component = new VTodo();
		component.getProperties().clear();
		assertValidate(component).versions(V1_0).run();
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(2, 2);

		component.setUid("");
		component.setDateTimeStamp(new Date());
		assertValidate(component).run();
	}

	@Test
	public void validate_cardinality_optional() {
		VTodo component = new VTodo();
		component.addProperty(Classification.confidential());
		component.addProperty(new Completed(new Date()));
		component.addProperty(new Created(new Date()));
		component.addProperty(new Description(""));
		component.addProperty(new DateStart(new Date()));
		component.addProperty(new Geo(1.1, 1.1));
		component.addProperty(new LastModified(new Date()));
		component.addProperty(new Location(""));
		component.addProperty(new Organizer("", ""));
		component.addProperty(new PercentComplete(0));
		component.addProperty(new Priority(0));
		component.addProperty(new Sequence(0));
		component.addProperty(Status.needsAction());
		component.addProperty(new Summary(""));
		component.addProperty(new Url(""));
		component.addProperty(new RecurrenceId(new Date()));
		component.addProperty(new Color(""));
		assertValidate(component).versions(V1_0).run();
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run();

		component.addProperty(Classification.confidential());
		component.addProperty(new Completed(new Date()));
		component.addProperty(new Created(new Date()));
		component.addProperty(new Description(""));
		component.addProperty(new DateStart(new Date()));
		component.addProperty(new Geo(1.1, 1.1));
		component.addProperty(new LastModified(new Date()));
		component.addProperty(new Location(""));
		component.addProperty(new Organizer("", ""));
		component.addProperty(new PercentComplete(0));
		component.addProperty(new Priority(0));
		component.addProperty(new Sequence(0));
		component.addProperty(Status.needsAction());
		component.addProperty(new Summary(""));
		component.addProperty(new Url(""));
		component.addProperty(new RecurrenceId(new Date()));
		component.addProperty(new Color(""));
		assertValidate(component).versions(V1_0).run(3);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3);
	}

	@Test
	public void validate_status() {
		VTodo component = new VTodo();

		Status status = Status.accepted();
		component.setStatus(status);
		assertValidate(component).versions(V1_0).run();
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).warn(status, 46).run(13);

		status = Status.cancelled();
		component.setStatus(status);
		assertValidate(component).versions(V1_0).warn(status, 46).run(13);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run();

		status = Status.completed();
		component.setStatus(status);
		assertValidate(component).run();

		status = Status.confirmed();
		component.setStatus(status);
		assertValidate(component).run(13);

		status = Status.declined();
		component.setStatus(status);
		assertValidate(component).versions(V1_0).run();
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).warn(status, 46).run(13);

		status = Status.delegated();
		component.setStatus(status);
		assertValidate(component).versions(V1_0).run();
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).warn(status, 46).run(13);

		status = Status.draft();
		component.setStatus(status);
		assertValidate(component).versions(V1_0).warn(status, 46).run(13);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(13);

		status = Status.final_();
		component.setStatus(status);
		assertValidate(component).versions(V1_0).warn(status, 46).run(13);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(13);

		status = Status.inProgress();
		component.setStatus(status);
		assertValidate(component).versions(V1_0).warn(status, 46).run(13);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run();

		status = Status.needsAction();
		component.setStatus(status);
		assertValidate(component).run();

		status = Status.sent();
		component.setStatus(status);
		assertValidate(component).versions(V1_0).run();
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).warn(status, 46).run(13);

		status = Status.tentative();
		component.setStatus(status);
		assertValidate(component).run(13);
	}

	@Test
	public void validate_dateStart_before_dateDue() {
		VTodo component = new VTodo();
		component.setDateStart(date(2000, 1, 10));
		component.setDateDue(date(2000, 1, 1));
		assertValidate(component).run(22);
	}

	@Test
	public void validate_different_date_datatypes() {
		VTodo component = new VTodo();
		component.setDateStart(new DateStart(date(2000, 1, 1), false));
		component.setDateDue(new DateDue(date(2000, 1, 10), true));
		assertValidate(component).run(23);

		component = new VTodo();
		component.setDateStart(new DateStart(date(2000, 1, 1), false));
		component.setDateDue(new DateDue(date(2000, 1, 10), false));
		component.setRecurrenceId(new RecurrenceId(date(2000, 1, 1), true));
		assertValidate(component).run(19);
	}

	@Test
	public void validate_dateDue_with_duration() {
		VTodo component = new VTodo();
		component.setDateStart(date(2000, 1, 1));
		component.setDateDue(date(2000, 1, 10));
		component.setDuration(new Duration.Builder().build());
		assertValidate(component).run(24);
	}

	@Test
	public void validate_no_dateStart() {
		VTodo component = new VTodo();
		component.setDuration(new Duration.Builder().build());
		assertValidate(component).run(25);
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
			VTodo component = new VTodo();
			component.setDateStart(new DateStart(date(2000, 1, 1), false));
			component.setDateDue(new DateDue(date(2000, 1, 10), false));
			component.setRecurrenceRule(recurrence);
			assertValidate(component).run(5);
		}
	}

	@Test
	public void validate_multiple_rrules() {
		VTodo component = new VTodo();
		component.setDateStart(new DateStart(date(2000, 1, 1), false));
		component.setDateDue(new DateDue(date(2000, 1, 10), false));
		component.addProperty(new RecurrenceRule(new Recurrence.Builder(Frequency.DAILY).build()));
		component.addProperty(new RecurrenceRule(new Recurrence.Builder(Frequency.DAILY).build()));
		assertValidate(component).run(6);
	}
}
