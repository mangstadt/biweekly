package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.component.VAlarm;
import biweekly.io.Version1ConversionException;
import biweekly.property.Action;
import biweekly.property.Attachment;
import biweekly.property.AudioAlarm;
import biweekly.property.Trigger;
import biweekly.util.Duration;
import biweekly.util.org.apache.commons.codec.binary.Base64;

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
public class AudioAlarmScribeTest extends ScribeTest<AudioAlarm> {
	private final Date start = date("2014-01-01 01:00:00 +0000");

	private final AudioAlarm empty = new AudioAlarm();

	private final AudioAlarm noValue = new AudioAlarm();
	{
		noValue.setRepeat(5);
		noValue.setSnooze(new Duration.Builder().minutes(10).build());
		noValue.setStart(start);
	}

	private final String contentId = "content-id";
	private final AudioAlarm withContentId = new AudioAlarm();
	{
		withContentId.setRepeat(5);
		withContentId.setSnooze(new Duration.Builder().minutes(10).build());
		withContentId.setStart(start);
		withContentId.setContentId(contentId);
	}

	private final String uri = "http://example.com/file";
	private final AudioAlarm withUri = new AudioAlarm();
	{
		withUri.setRepeat(5);
		withUri.setSnooze(new Duration.Builder().minutes(10).build());
		withUri.setStart(start);
		withUri.setUri(uri);
	}

	private final byte[] data = "data".getBytes();
	private final String dataBase64 = Base64.encodeBase64String(data);
	private final AudioAlarm withData = new AudioAlarm();
	{
		withData.setRepeat(5);
		withData.setSnooze(new Duration.Builder().minutes(10).build());
		withData.setStart(start);
		withData.setData(data);
	}

	public AudioAlarmScribeTest() {
		super(new AudioAlarmScribe());
	}

	@Test
	public void dataType() {
		sensei.assertDataType(empty).run(null);
		sensei.assertDataType(noValue).run(null);
		sensei.assertDataType(withContentId).run(ICalDataType.CONTENT_ID);
		sensei.assertDataType(withUri).run(ICalDataType.URL);
		sensei.assertDataType(withData).run(ICalDataType.BINARY);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(empty).run(";;");
		sensei.assertWriteText(noValue).run("20140101T010000Z;PT10M;5");
		sensei.assertWriteText(withContentId).run("20140101T010000Z;PT10M;5;" + contentId);
		sensei.assertWriteText(withUri).run("20140101T010000Z;PT10M;5;" + uri);
		sensei.assertWriteText(withData).run("20140101T010000Z;PT10M;5;" + dataBase64);
	}

	@Test
	public void parseText() {
		try {
			sensei.assertParseText("").run();
			fail();
		} catch (Version1ConversionException e) {
			assertEquals(empty, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.audio(), new Trigger((Date) null));
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}

		try {
			sensei.assertParseText("20140101T010000Z;PT10M;5").run();
			fail();
		} catch (Version1ConversionException e) {
			assertEquals(noValue, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.audio(), new Trigger(noValue.getStart()));
			expected.setDuration(noValue.getSnooze());
			expected.setRepeat(noValue.getRepeat());
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}

		try {
			sensei.assertParseText("20140101T010000Z;PT10M;5;" + contentId).dataType(ICalDataType.CONTENT_ID).run();
			fail();
		} catch (Version1ConversionException e) {
			assertEquals(withContentId, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.audio(), new Trigger(withContentId.getStart()));
			Attachment attach = new Attachment(null, (String) null);
			attach.setContentId("content-id");
			expected.addAttachment(attach);
			expected.setDuration(withContentId.getSnooze());
			expected.setRepeat(withContentId.getRepeat());
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}

		try {
			sensei.assertParseText("20140101T010000Z;PT10M;5;" + uri).dataType(ICalDataType.URL).run();
			fail();
		} catch (Version1ConversionException e) {
			assertEquals(withUri, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.audio(), new Trigger(withUri.getStart()));
			Attachment attach = new Attachment(null, uri);
			expected.addAttachment(attach);
			expected.setDuration(withUri.getSnooze());
			expected.setRepeat(withUri.getRepeat());
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}

		try {
			sensei.assertParseText("20140101T010000Z;PT10M;5;" + dataBase64).dataType(ICalDataType.BINARY).run();
			fail();
		} catch (Version1ConversionException e) {
			assertEquals(withData, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.audio(), new Trigger(withData.getStart()));
			Attachment attach = new Attachment(null, data);
			expected.addAttachment(attach);
			expected.setDuration(withData.getSnooze());
			expected.setRepeat(withData.getRepeat());
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}

		try {
			sensei.assertParseText("20140101T010000Z;PT10M;5;" + uri).run();
			fail();
		} catch (Version1ConversionException e) {
			assertEquals(withUri, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.audio(), new Trigger(withUri.getStart()));
			Attachment attach = new Attachment(null, uri);
			expected.addAttachment(attach);
			expected.setDuration(withUri.getSnooze());
			expected.setRepeat(withUri.getRepeat());
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}
	}
}
