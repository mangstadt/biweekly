package biweekly.component;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertValidate;

import java.util.Date;

import org.junit.Test;

import biweekly.parameter.Related;
import biweekly.property.Action;
import biweekly.property.Attachment;
import biweekly.property.Attendee;
import biweekly.property.Trigger;
import biweekly.util.Duration;

/*
 Copyright (c) 2013-2023, Michael Angstadt
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
public class VAlarmTest {
	@Test
	public void validate_required() {
		VAlarm component = new VAlarm(null, null);
		assertValidate(component).run(2, 2);

		component.setAction(Action.audio());
		component.setTrigger(new Trigger(new Date()));
		assertValidate(component).run();
	}

	@Test
	public void validate_audio_attachment() {
		//2 attachments
		VAlarm component = new VAlarm(Action.audio(), new Trigger(new Date()));
		component.addAttachment(new Attachment("", new byte[0]));
		component.addAttachment(new Attachment("", new byte[0]));
		assertValidate(component).run(7);

		//1 attachment
		component = new VAlarm(Action.audio(), new Trigger(new Date()));
		component.addAttachment(new Attachment("", new byte[0]));
		assertValidate(component).run();

		//no attachments
		component = new VAlarm(Action.audio(), new Trigger(new Date()));
		assertValidate(component).run();
	}

	@Test
	public void validate_display() {
		VAlarm component = new VAlarm(Action.display(), new Trigger(new Date()));
		assertValidate(component).run(2);

		component = new VAlarm(Action.display(), new Trigger(new Date()));
		component.setDescription("");
		assertValidate(component).run();
	}

	@Test
	public void validate_email() {
		VAlarm component = new VAlarm(Action.email(), new Trigger(new Date()));
		assertValidate(component).run(2, 2, 8);

		component = new VAlarm(Action.email(), new Trigger(new Date()));
		component.setSummary("");
		component.setDescription("");
		assertValidate(component).run(8);

		component = new VAlarm(Action.email(), new Trigger(new Date()));
		component.setSummary("");
		component.setDescription("");
		component.addAttendee(new Attendee(null, null, ""));
		assertValidate(component).run();

		//only EMAIL alarms can have attendees
		component = new VAlarm(Action.audio(), new Trigger(new Date()));
		component.addAttendee(new Attendee(null, null, ""));
		assertValidate(component).run(9);
		component = new VAlarm(Action.display(), new Trigger(new Date()));
		component.addAttendee(new Attendee(null, null, ""));
		assertValidate(component).run(9, 2);
	}

	@Test
	public void validate_procedure() {
		Action action = Action.procedure();
		VAlarm component = new VAlarm(action, new Trigger(new Date()));
		assertValidate(component).versions(V1_0).run(2);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).warn(action, 46).run(2);

		component.setDescription("");
		assertValidate(component).versions(V1_0).run();
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).warn(action, 46).run();
	}

	@Test
	public void validate_related_start() {
		VEvent event = new VEvent();
		VAlarm component = new VAlarm(Action.audio(), new Trigger(new Duration.Builder().build(), Related.START));
		assertValidate(component).parents(event).run(11);

		event = new VEvent();
		event.setDateStart(new Date());
		component = new VAlarm(Action.audio(), new Trigger(new Duration.Builder().build(), Related.START));
		assertValidate(component).parents(event).run();
	}

	@Test
	public void validate_related_end() {
		VAlarm component = new VAlarm(Action.audio(), new Trigger(new Duration.Builder().build(), Related.END));

		VEvent event = new VEvent();
		assertValidate(component).parents(event).run(12);

		event = new VEvent();
		event.setDateStart(new Date());
		event.setDateEnd(new Date());
		assertValidate(component).parents(event).run();

		event = new VEvent();
		event.setDateStart(new Date());
		assertValidate(component).parents(event).run(12);

		event = new VEvent();
		event.setDuration(new Duration.Builder().build());
		assertValidate(component).parents(event).run(12);

		event = new VEvent();
		event.setDateStart(new Date());
		event.setDuration(new Duration.Builder().build());
		assertValidate(component).parents(event).run();

		VTodo todo = new VTodo();
		assertValidate(component).parents(todo).run(12);

		todo = new VTodo();
		todo.setDateStart(new Date());
		todo.setDateDue(new Date());
		assertValidate(component).parents(todo).run();

		todo = new VTodo();
		todo.setDateStart(new Date());
		assertValidate(component).parents(todo).run(12);

		todo = new VTodo();
		todo.setDuration(new Duration.Builder().build());
		assertValidate(component).parents(todo).run(12);

		todo = new VTodo();
		todo.setDateStart(new Date());
		todo.setDuration(new Duration.Builder().build());
		assertValidate(component).parents(todo).run();

		VJournal journal = new VJournal();
		assertValidate(component).parents(journal).run();
	}
}
