package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import biweekly.io.ParseContext;
import biweekly.io.scribe.property.DateTimePropertyScribeTest.DateTimePropertyImpl;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.DateTimeProperty;

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
public class DateTimePropertyScribeTest extends ScribeTest<DateTimePropertyImpl> {
	private final Date datetime = date("2013-06-11 13:43:02");
	private final String datetimeStr = "20130611T124302Z";
	private final String datetimeStrExt = "2013-06-11T12:43:02Z";

	private final DateTimePropertyImpl withDateTime = new DateTimePropertyImpl(datetime);
	private final DateTimePropertyImpl empty = new DateTimePropertyImpl(null);

	public DateTimePropertyScribeTest() {
		super(new DateTimePropertyMarshallerImpl());
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withDateTime).run(datetimeStr);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(datetimeStr).run(hasDateTime);
		sensei.assertParseText("invalid").cannotParse();
		sensei.assertParseText("").cannotParse();
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withDateTime).run("<date-time>" + datetimeStrExt + "</date-time>");
		sensei.assertWriteXml(empty).run("<date-time/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<date-time>" + datetimeStrExt + "</date-time>").run(hasDateTime);
		sensei.assertParseXml("<date-time>invalid</date-time>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withDateTime).run(datetimeStrExt);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(datetimeStrExt).run(hasDateTime);
		sensei.assertParseJson("invalid").cannotParse();
		sensei.assertParseJson("").cannotParse();
	}

	public static class DateTimePropertyMarshallerImpl extends DateTimePropertyScribe<DateTimePropertyImpl> {
		public DateTimePropertyMarshallerImpl() {
			super(DateTimePropertyImpl.class, "DATETIME");
		}

		@Override
		protected DateTimePropertyImpl newInstance(Date date) {
			return new DateTimePropertyImpl(date);
		}
	}

	public static class DateTimePropertyImpl extends DateTimeProperty {
		public DateTimePropertyImpl(Date value) {
			super(value);
		}
	}

	private final Check<DateTimePropertyImpl> hasDateTime = new Check<DateTimePropertyImpl>() {
		public void check(DateTimePropertyImpl property, ParseContext context) {
			assertEquals(datetime, property.getValue());
		}
	};
}
