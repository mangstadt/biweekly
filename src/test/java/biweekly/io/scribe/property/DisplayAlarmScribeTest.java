package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import biweekly.io.ParseContext;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.DisplayAlarm;
import biweekly.util.Duration;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
public class DisplayAlarmScribeTest {
	private final DisplayAlarmScribe scribe = new DisplayAlarmScribe();
	private final Sensei<DisplayAlarm> sensei = new Sensei<DisplayAlarm>(scribe);

	private final Date start = date("2014-01-01 01:00:00 +0000");

	private final DisplayAlarm empty = new DisplayAlarm(null);

	private final DisplayAlarm noValue = new DisplayAlarm(null);
	{
		noValue.setRepeat(5);
		noValue.setSnooze(new Duration.Builder().minutes(10).build());
		noValue.setStart(start);
	}

	private final String text = "display-text";
	private final DisplayAlarm withValue = new DisplayAlarm(text);
	{
		withValue.setRepeat(5);
		withValue.setSnooze(new Duration.Builder().minutes(10).build());
		withValue.setStart(start);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(empty).run(";;");
		sensei.assertWriteText(noValue).run("20140101T010000Z;PT10M;5");
		sensei.assertWriteText(withValue).run("20140101T010000Z;PT10M;5;" + text);
	}

	@Test
	public void parseText() {
		sensei.assertParseText("").run(is(empty));
		sensei.assertParseText("20140101T010000Z;PT10M;5").run(is(noValue));
		sensei.assertParseText("20140101T010000Z;PT10M;5;" + text).run(is(withValue));
	}

	private Check<DisplayAlarm> is(final DisplayAlarm expected) {
		return new Check<DisplayAlarm>() {
			public void check(DisplayAlarm actual, ParseContext context) {
				assertEquals(expected.getStart(), actual.getStart());
				assertEquals(expected.getSnooze(), actual.getSnooze());
				assertEquals(expected.getRepeat(), actual.getRepeat());
				assertEquals(expected.getText(), actual.getText());
			}
		};
	}
}
