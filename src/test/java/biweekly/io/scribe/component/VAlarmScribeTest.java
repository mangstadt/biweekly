package biweekly.io.scribe.component;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

import java.time.Duration;
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
public class VAlarmScribeTest {
	private final VAlarmScribe scribe = new VAlarmScribe();
	private final Date date = new Date();

	@Test
	public void checkForDataModelConversions_version() {
		VAlarm alarm = new VAlarm(Action.audio(), new Trigger(date));

		DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));
		assertNull(e.getOriginalProperty());
		assertEquals(1, e.getProperties().size());
		assertEquals(0, e.getComponents().size());

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
		Duration duration = Duration.ofHours(1);

		//empty trigger
		Trigger trigger = new Trigger(null, null);
		{
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			AudioAlarm expected = new AudioAlarm();
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//trigger date
		trigger = new Trigger(date);
		{
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			AudioAlarm expected = new AudioAlarm();
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//related trigger, no parent
		trigger = new Trigger(duration, Related.START);
		{
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			AudioAlarm expected = new AudioAlarm();
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		trigger = new Trigger(duration, Related.START);
		{
			//no parent start date
			VEvent parent = new VEvent();
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, parent, V1_0));

			AudioAlarm expected = new AudioAlarm();
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
		{
			//parent start date
			VEvent parent = new VEvent();
			parent.setDateStart(date);
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, parent, V1_0));

			AudioAlarm expected = new AudioAlarm();
			expected.setStart(new Date(date.getTime() + duration.toMillis()));
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		trigger = new Trigger(duration, Related.END);
		{
			//no parent end date
			VEvent parent = new VEvent();
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, parent, V1_0));

			AudioAlarm expected = new AudioAlarm();
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
		{
			//parent end date
			VEvent parent = new VEvent();
			parent.setDateEnd(date);
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, parent, V1_0));

			AudioAlarm expected = new AudioAlarm();
			expected.setStart(new Date(date.getTime() + duration.toMillis()));
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
		{
			//parent with start date
			VEvent parent = new VEvent();
			parent.setDateStart(date);
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, parent, V1_0));

			AudioAlarm expected = new AudioAlarm();
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
		{
			//parent with duration
			VEvent parent = new VEvent();
			parent.setDuration(duration);
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, parent, V1_0));

			AudioAlarm expected = new AudioAlarm();
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
		{
			//parent with start date and duration
			VEvent parent = new VEvent();
			parent.setDateStart(date);
			parent.setDuration(duration);
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, parent, V1_0));

			AudioAlarm expected = new AudioAlarm();
			expected.setStart(new Date(date.getTime() + duration.toMillis() + duration.toMillis()));
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//unknown RELATED
		trigger = new Trigger(duration, Related.get("FOO"));
		{
			VEvent parent = new VEvent();
			VAlarm alarm = new VAlarm(action, trigger);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, parent, V1_0));

			AudioAlarm expected = new AudioAlarm();
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
	}

	@Test
	public void checkForDataModelConversions_snooze() {
		Action action = Action.audio();
		Duration snooze = Duration.ofMinutes(10);

		VAlarm alarm = new VAlarm(action, null);
		alarm.setDuration(snooze);
		DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

		AudioAlarm expected = new AudioAlarm();
		expected.setSnooze(snooze);
		assertNull(e.getOriginalProperty());
		assertEquals(Arrays.asList(expected), e.getProperties());
		assertEquals(Arrays.asList(), e.getComponents());
	}

	@Test
	public void checkForDataModelConversions_repeat() {
		Action action = Action.audio();
		int repeat = 2;

		VAlarm alarm = new VAlarm(action, null);
		alarm.setRepeat(repeat);
		DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

		AudioAlarm expected = new AudioAlarm();
		expected.setRepeat(repeat);
		assertNull(e.getOriginalProperty());
		assertEquals(Arrays.asList(expected), e.getProperties());
		assertEquals(Arrays.asList(), e.getComponents());
	}

	@Test
	public void checkForDataModelConversions_AudioAlarm() {
		Action action = Action.audio();

		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			AudioAlarm expected = new AudioAlarm();
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//empty attachment
		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			alarm.addAttachment(new Attachment("image/png", (String) null));
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			AudioAlarm expected = new AudioAlarm();
			expected.setType("image/png");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//data attachment
		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			alarm.addAttachment(new Attachment("image/png", new byte[0]));
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			AudioAlarm expected = new AudioAlarm();
			expected.setType("image/png");
			expected.setData(new byte[0]);
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//uri attachment
		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			alarm.addAttachment(new Attachment("image/png", "uri"));
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			AudioAlarm expected = new AudioAlarm();
			expected.setType("image/png");
			expected.setUri("uri");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		//CID attachment
		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			alarm.addAttachment(new Attachment("image/png", "cid:id"));
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

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

		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			DisplayAlarm expected = new DisplayAlarm((String) null);
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			alarm.setDescription("description");
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

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

		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			EmailAlarm expected = new EmailAlarm((String) null);
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		{

			VAlarm alarm = new VAlarm(action, new Trigger(date));
			alarm.addAttendee(new Attendee("Name", "email1@example.com"));
			alarm.addAttendee(new Attendee("Name", "email2@example.com"));
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			EmailAlarm expected = new EmailAlarm("email1@example.com");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			alarm.setDescription("note");
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

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

		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			ProcedureAlarm expected = new ProcedureAlarm((String) null);
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}

		{
			VAlarm alarm = new VAlarm(action, new Trigger(date));
			alarm.setDescription("/usr/bin/alarm");
			DataModelConversionException e = assertThrows(DataModelConversionException.class, () -> scribe.checkForDataModelConversions(alarm, null, V1_0));

			ProcedureAlarm expected = new ProcedureAlarm("/usr/bin/alarm");
			expected.setStart(date);
			assertNull(e.getOriginalProperty());
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
	}
}
