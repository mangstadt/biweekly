package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import biweekly.component.VAlarm;
import biweekly.io.Version1ConversionException;
import biweekly.property.Action;
import biweekly.property.Attendee;
import biweekly.property.EmailAlarm;
import biweekly.property.Trigger;
import biweekly.util.Duration;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
public class EmailAlarmScribeTest extends ScribeTest<EmailAlarm> {
	private final Date start = date("2014-01-01 01:00:00 +0000");

	private final EmailAlarm empty = new EmailAlarm((String) null);

	private final EmailAlarm noValue = new EmailAlarm((String) null);
	{
		noValue.setRepeat(5);
		noValue.setSnooze(new Duration.Builder().minutes(10).build());
		noValue.setStart(start);
	}

	private final String email = "jdoe@example.com";
	private final String note = "note";
	private final EmailAlarm withValue = new EmailAlarm(email);
	{
		withValue.setNote(note);
		withValue.setRepeat(5);
		withValue.setSnooze(new Duration.Builder().minutes(10).build());
		withValue.setStart(start);
	}

	public EmailAlarmScribeTest() {
		super(new EmailAlarmScribe());
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(empty).run(";;");
		sensei.assertWriteText(noValue).run("20140101T010000Z;PT10M;5");
		sensei.assertWriteText(withValue).run("20140101T010000Z;PT10M;5;" + email + ";" + note);
	}

	@Test
	public void parseText() {
		try {
			sensei.assertParseText("").run();
			fail();
		} catch (Version1ConversionException e) {
			assertEquals(empty, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.email(), new Trigger((Date) null));
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}

		try {
			sensei.assertParseText("20140101T010000Z;PT10M;5").run();
			fail();
		} catch (Version1ConversionException e) {
			assertEquals(noValue, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.email(), new Trigger(noValue.getStart()));
			expected.setDuration(noValue.getSnooze());
			expected.setRepeat(noValue.getRepeat());
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}

		try {
			sensei.assertParseText("20140101T010000Z;PT10M;5;" + email + ";" + note).run();
			fail();
		} catch (Version1ConversionException e) {
			assertEquals(withValue, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.email(), new Trigger(withValue.getStart()));
			expected.setDuration(withValue.getSnooze());
			expected.setRepeat(withValue.getRepeat());
			expected.addAttendee(new Attendee(null, email));
			expected.setDescription(withValue.getNote());
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}
	}
}
