package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.ProcedureAlarm;
import biweekly.util.Duration;

/*
 Copyright (c) 2013, Michael Angstadt
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
public class ProcedureAlarmScribeTest {
	private final ProcedureAlarmScribe scribe = new ProcedureAlarmScribe();
	private final Sensei<ProcedureAlarm> sensei = new Sensei<ProcedureAlarm>(scribe);

	private final Date start = date("2014-01-01 01:00:00 +0000");

	private final ProcedureAlarm empty = new ProcedureAlarm(null);

	private final ProcedureAlarm noValue = new ProcedureAlarm(null);
	{
		noValue.setRepeat(5);
		noValue.setSnooze(new Duration.Builder().minutes(10).build());
		noValue.setStart(start);
	}

	private final String path = "file:///bin/ls";
	private final ProcedureAlarm withValue = new ProcedureAlarm(path);
	{
		withValue.setRepeat(5);
		withValue.setSnooze(new Duration.Builder().minutes(10).build());
		withValue.setStart(start);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(empty).run(";;");
		sensei.assertWriteText(noValue).run("20140101T010000Z;PT10M;5");
		sensei.assertWriteText(withValue).run("20140101T010000Z;PT10M;5;" + path);
	}

	@Test
	public void parseText() {
		sensei.assertParseText("").run(is(empty));
		sensei.assertParseText("20140101T010000Z;PT10M;5").run(is(noValue));
		sensei.assertParseText("20140101T010000Z;PT10M;5;" + path).run(is(withValue));
	}

	private Check<ProcedureAlarm> is(final ProcedureAlarm expected) {
		return new Check<ProcedureAlarm>() {
			public void check(ProcedureAlarm actual) {
				assertEquals(expected.getStart(), actual.getStart());
				assertEquals(expected.getSnooze(), actual.getSnooze());
				assertEquals(expected.getRepeat(), actual.getRepeat());
				assertEquals(expected.getPath(), actual.getPath());
			}
		};
	}
}
