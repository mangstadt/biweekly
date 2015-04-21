package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.ParseContext;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.parameter.Related;
import biweekly.property.Trigger;
import biweekly.util.Duration;

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
public class TriggerScribeTest extends ScribeTest<Trigger> {
	private final Date datetime = date("2013-06-11 13:43:02");
	private final String datetimeStr = "20130611T124302Z";
	private final String datetimeStrExt = "2013-06-11T12:43:02Z";

	private final Duration duration = Duration.builder().hours(2).build();
	private final String durationStr = duration.toString();

	private final Trigger withDateTime = new Trigger(datetime);
	private final Trigger withDuration = new Trigger(duration, Related.START);
	private final Trigger empty = new Trigger(null);

	public TriggerScribeTest() {
		super(new TriggerScribe());
	}

	@Test
	public void dataType() {
		sensei.assertDataType(withDateTime).run(ICalDataType.DATE_TIME);
		sensei.assertDataType(withDuration).run(ICalDataType.DURATION);
		sensei.assertDataType(empty).run(ICalDataType.DURATION);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withDateTime).run(datetimeStr);
		sensei.assertWriteText(withDuration).run(durationStr);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(datetimeStr).run(is(withDateTime));
		sensei.assertParseText(durationStr).run(is(withDuration));
		sensei.assertParseText("invalid").cannotParse();
		sensei.assertParseText("").cannotParse();
	}

	@Test
	public void writeXml_date() {
		sensei.assertWriteXml(withDateTime).run("<date-time>" + datetimeStrExt + "</date-time>");
		sensei.assertWriteXml(withDuration).run("<duration>" + durationStr + "</duration>");
		sensei.assertWriteXml(empty).run("<duration/>");
	}

	@Test
	public void parseXml_date() {
		sensei.assertParseXml("<date-time>" + datetimeStrExt + "</date-time>").run(is(withDateTime));
		sensei.assertParseXml("<duration>" + durationStr + "</duration>").run(is(withDuration));

		//prefers <duration> element if both elements exist
		sensei.assertParseXml("<duration>" + durationStr + "</duration><date-time>" + datetimeStrExt + "</date-time>").run(is(withDuration));

		sensei.assertParseXml("<date-time>invalid</date-time>").cannotParse();
		sensei.assertParseXml("<duration>invalid</duration>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson_date() {
		sensei.assertWriteJson(withDateTime).run(datetimeStrExt);
		sensei.assertWriteJson(withDuration).run(durationStr);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson_date() {
		sensei.assertParseJson(datetimeStrExt).run(is(withDateTime));
		sensei.assertParseJson(durationStr).run(is(withDuration));
		sensei.assertParseJson("invalid").cannotParse();
		sensei.assertParseJson("").cannotParse();
	}

	private Check<Trigger> is(final Trigger expected) {
		return new Check<Trigger>() {
			public void check(Trigger actual, ParseContext context) {
				assertEquals(expected.getDate(), actual.getDate());
				assertEquals(expected.getDuration(), actual.getDuration());
			}
		};
	}
}
