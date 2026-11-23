package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.utc;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;

import biweekly.component.VAlarm;
import biweekly.io.DataModelConversionException;
import biweekly.property.Action;
import biweekly.property.DisplayAlarm;
import biweekly.property.Trigger;
import java.time.Duration;

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
class DisplayAlarmScribeTest extends ScribeTest<DisplayAlarm> {
	private final Date start = utc(2014, 1, 1, 1, 0, 0);

	private final DisplayAlarm empty = new DisplayAlarm((String) null);

	private final DisplayAlarm noValue = new DisplayAlarm((String) null);
	{
		noValue.setRepeat(5);
		noValue.setSnooze(Duration.ofMinutes(10));
		noValue.setStart(start);
	}

	private final String text = "display-text";
	private final DisplayAlarm withValue = new DisplayAlarm(text);
	{
		withValue.setRepeat(5);
		withValue.setSnooze(Duration.ofMinutes(10));
		withValue.setStart(start);
	}

	public DisplayAlarmScribeTest() {
		super(new DisplayAlarmScribe());
	}

	@Test
	void writeText() {
		sensei.assertWriteText(empty).run(";;");
		sensei.assertWriteText(noValue).run("20140101T010000Z;PT10M;5");
		sensei.assertWriteText(withValue).run("20140101T010000Z;PT10M;5;" + text);
	}

	@Test
	void parseText() {
		{
			Sensei<DisplayAlarm>.ParseTextTest test = sensei.assertParseText("");
			DataModelConversionException e = assertThrows(DataModelConversionException.class, test::run);

			assertEquals(empty, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.display(), new Trigger((Date) null));
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}

		{
			Sensei<DisplayAlarm>.ParseTextTest test = sensei.assertParseText("20140101T010000Z;PT10M;5");
			DataModelConversionException e = assertThrows(DataModelConversionException.class, test::run);

			assertEquals(noValue, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.display(), new Trigger(noValue.getStart()));
			expected.setDuration(noValue.getSnooze());
			expected.setRepeat(noValue.getRepeat());
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}

		{
			Sensei<DisplayAlarm>.ParseTextTest test = sensei.assertParseText("20140101T010000Z;PT10M;5;" + text);
			DataModelConversionException e = assertThrows(DataModelConversionException.class, test::run);

			assertEquals(withValue, e.getOriginalProperty());
			VAlarm expected = new VAlarm(Action.display(), new Trigger(withValue.getStart()));
			expected.setDuration(withValue.getSnooze());
			expected.setRepeat(withValue.getRepeat());
			expected.setDescription(withValue.getText());
			assertEquals(Arrays.asList(expected), e.getComponents());
			assertEquals(Arrays.asList(), e.getProperties());
		}
	}
}
