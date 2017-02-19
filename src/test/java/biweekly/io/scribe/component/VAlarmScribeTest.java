package biweekly.io.scribe.component;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.io.DataModelConversionException;
import biweekly.parameter.Related;
import biweekly.property.Action;
import biweekly.property.Attachment;
import biweekly.property.Attendee;
import biweekly.property.AudioAlarm;
import biweekly.property.DisplayAlarm;
import biweekly.property.EmailAlarm;
import biweekly.property.ProcedureAlarm;
import biweekly.property.Trigger;
import biweekly.util.Duration;

/*
 Copyright (c) 2013-2017, Michael Angstadt
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
public class VAlarmScribeTest {
	private final VAlarmScribe scribe = new VAlarmScribe();
	private final Date date = new Date();

	@Test
	public void checkForDataModelConversions_version() {
		VAlarm alarm = new VAlarm(Action.audio(), new Trigger(date));

		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
			fail();
		} catch (DataModelConversionException e) {
			assertNull(e.getOriginalProperty());
			assertEquals(1, e.getProperties().size());
			assertEquals(0, e.getComponents().size());
		}
		scribe.checkForDataModelConversions(alarm, null, V2_0_DEPRECATED);
		scribe.checkForDataModelConversions(alarm, null, V2_0);
	}

	@Test
	public void checkForDataModelConversions_no_action() {
		VAlarm alarm = new VAlarm(null, new Trigger(date));
		scribe.checkForDataModelConversions(alarm, null, V1_0);
	}

	@Test
	public void checkForDataModelConversions_unknown_action() {
		VAlarm alarm = new VAlarm(new Action("foo"), new Trigger(date));
		scribe.checkForDataModelConversions(alarm, null, V1_0);
	}

	@Test
	public void checkForDataModelConversions_start_date() {
		Action action = Action.audio();
		Duration duration = new Duration.Builder().hours(1).build();
		VAlarm alarm;
		VEvent parent;

		//empty trigger
		Trigger trigger = new Trigger(null, null);
		{
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, null, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}
		}

		//trigger date
		trigger = new Trigger(date);
		{
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, null, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				expected.setStart(date);
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}
		}

		//related trigger, no parent
		trigger = new Trigger(duration, Related.START);
		{
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, null, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}
		}

		trigger = new Trigger(duration, Related.START);
		{
			//no parent start date
			parent = new VEvent();
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, parent, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}

			//parent start date
			parent = new VEvent();
			parent.setDateStart(date);
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, parent, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				expected.setStart(duration.add(date));
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}
		}

		trigger = new Trigger(duration, Related.END);
		{
			//no parent end date
			parent = new VEvent();
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, parent, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}

			//parent end date
			parent = new VEvent();
			parent.setDateEnd(date);
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, parent, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				expected.setStart(duration.add(date));
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}

			//parent with start date
			parent = new VEvent();
			parent.setDateStart(date);
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, parent, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}

			//parent with duration
			parent = new VEvent();
			parent.setDuration(duration);
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, parent, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}

			//parent with start date and duration
			parent = new VEvent();
			parent.setDateStart(date);
			parent.setDuration(duration);
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, parent, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				expected.setStart(duration.add(duration.add(date)));
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}
		}

		//unknown RELATED
		trigger = new Trigger(duration, Related.get("FOO"));
		{
			parent = new VEvent();
			alarm = new VAlarm(action, trigger);
			try {
				scribe.checkForDataModelConversions(alarm, parent, V1_0);
			} catch (DataModelConversionException e) {
				AudioAlarm expected = new AudioAlarm();
				assertNull(e.getOriginalProperty());
				assertEquals(Arrays.asList(expected), e.getProperties());
				assertEquals(Arrays.asList(), e.getComponents());
			}
		}
	}

	@Test
	public void checkForDataModelConversions_snooze() {
		Action action = Action.audio();
		Duration snooze = new Duration.Builder().minutes(10).build();

		VAlarm alarm = new VAlarm(action, null);
		alarm.setDuration(snooze);
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			AudioAlarm expected = new AudioAlarm();
			expected.setSnooze(snooze);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
	}

	@Test
	public void checkForDataModelConversions_repeat() {
		Action action = Action.audio();
		int repeat = 2;

		VAlarm alarm = new VAlarm(action, null);
		alarm.setRepeat(repeat);
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			AudioAlarm expected = new AudioAlarm();
			expected.setRepeat(repeat);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
	}

	@Test
	public void checkForDataModelConversions_AudioAlarm() {
		Action action = Action.audio();

		VAlarm alarm = new VAlarm(action, new Trigger(date));
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			AudioAlarm expected = new AudioAlarm();
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//empty attachment
		alarm = new VAlarm(action, new Trigger(date));
		alarm.addAttachment(new Attachment("image/png", (String) null));
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			AudioAlarm expected = new AudioAlarm();
			expected.setType("image/png");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//data attachment
		alarm = new VAlarm(action, new Trigger(date));
		alarm.addAttachment(new Attachment("image/png", new byte[0]));
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			AudioAlarm expected = new AudioAlarm();
			expected.setType("image/png");
			expected.setData(new byte[0]);
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//uri attachment
		alarm = new VAlarm(action, new Trigger(date));
		alarm.addAttachment(new Attachment("image/png", "uri"));
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			AudioAlarm expected = new AudioAlarm();
			expected.setType("image/png");
			expected.setUri("uri");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//CID attachment
		alarm = new VAlarm(action, new Trigger(date));
		alarm.addAttachment(new Attachment("image/png", "cid:id"));
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			AudioAlarm expected = new AudioAlarm();
			expected.setType("image/png");
			expected.setContentId("id");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
	}

	@Test
	public void checkForDataModelConversions_DisplayAlarm() {
		Action action = Action.display();

		VAlarm alarm = new VAlarm(action, new Trigger(date));
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			DisplayAlarm expected = new DisplayAlarm((String) null);
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		alarm = new VAlarm(action, new Trigger(date));
		alarm.setDescription("description");
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			DisplayAlarm expected = new DisplayAlarm("description");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
	}

	@Test
	public void checkForDataModelConversions_EmailAlarm() {
		Action action = Action.email();

		VAlarm alarm = new VAlarm(action, new Trigger(date));
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			EmailAlarm expected = new EmailAlarm((String) null);
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		alarm = new VAlarm(action, new Trigger(date));
		alarm.addAttendee(new Attendee("Name", "email1@example.com"));
		alarm.addAttendee(new Attendee("Name", "email2@example.com"));
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			EmailAlarm expected = new EmailAlarm("email1@example.com");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		alarm = new VAlarm(action, new Trigger(date));
		alarm.setDescription("note");
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			EmailAlarm expected = new EmailAlarm((String) null);
			expected.setNote("note");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
	}

	@Test
	public void checkForDataModelConversions_ProcedureAlarm() {
		Action action = Action.procedure();

		VAlarm alarm = new VAlarm(action, new Trigger(date));
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			ProcedureAlarm expected = new ProcedureAlarm((String) null);
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		alarm = new VAlarm(action, new Trigger(date));
		alarm.setDescription("/usr/bin/alarm");
		try {
			scribe.checkForDataModelConversions(alarm, null, V1_0);
		} catch (DataModelConversionException e) {
			ProcedureAlarm expected = new ProcedureAlarm("/usr/bin/alarm");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
	}
}
