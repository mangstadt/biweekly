package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.DateOrDateTimeProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.DefaultTimezoneRule;

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
public class DateOrDateTimePropertyScribeTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	private final DateOrDateTimePropertyMarshallerImpl marshaller = new DateOrDateTimePropertyMarshallerImpl();
	private final Sensei<DateOrDateTimePropertyImpl> sensei = new Sensei<DateOrDateTimePropertyImpl>(marshaller);

	private final Date date = date("2013-06-11");
	private final String dateStr = "20130611";
	private final String dateStrExt = "2013-06-11";

	private final Date datetime = date("2013-06-11 13:43:02");
	private final String datetimeStr = "20130611T124302Z";
	private final String datetimeStrExt = "2013-06-11T12:43:02Z";

	private final DateTimeComponents components = new DateTimeComponents(2013, 6, 11, 12, 43, 2, false);
	private final String componentsStr = "20130611T124302";
	private final String componentsStrExt = "2013-06-11T12:43:02";

	private final DateOrDateTimePropertyImpl withDateTime = new DateOrDateTimePropertyImpl(datetime, true);
	private final DateOrDateTimePropertyImpl withDate = new DateOrDateTimePropertyImpl(datetime, false);
	private final DateOrDateTimePropertyImpl withComponents = new DateOrDateTimePropertyImpl(components);
	private final DateOrDateTimePropertyImpl empty = new DateOrDateTimePropertyImpl(null);

	@Test
	public void dataType() {
		sensei.assertDataType(withDate).run(ICalDataType.DATE);
		sensei.assertDataType(withDateTime).run(ICalDataType.DATE_TIME);
		sensei.assertDataType(withComponents).run(ICalDataType.DATE_TIME);
		sensei.assertDataType(empty).run(ICalDataType.DATE_TIME);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withDate).run(dateStr);
		sensei.assertWriteText(withDateTime).run(datetimeStr);
		sensei.assertWriteText(withComponents).run(componentsStr);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(dateStr).dataType(ICalDataType.DATE).run(hasDate);
		sensei.assertParseText(datetimeStr).dataType(ICalDataType.DATE_TIME).run(hasDateTime);
		sensei.assertParseText("invalid").dataType(ICalDataType.DATE_TIME).cannotParse();
		sensei.assertParseText("").dataType(ICalDataType.DATE_TIME).cannotParse();
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withDate).run("<date>" + dateStrExt + "</date>");
		sensei.assertWriteXml(withDateTime).run("<date-time>" + datetimeStrExt + "</date-time>");
		sensei.assertWriteXml(withComponents).run("<date-time>" + componentsStrExt + "</date-time>");
		sensei.assertWriteXml(empty).run("<date-time/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<date>" + dateStrExt + "</date>").run(hasDate);
		sensei.assertParseXml("<date-time>" + datetimeStrExt + "</date-time>").run(hasDateTime);
		sensei.assertParseXml("<date-time>invalid</date-time>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withDate).run(dateStrExt);
		sensei.assertWriteJson(withDateTime).run(datetimeStrExt);
		sensei.assertWriteJson(withComponents).run(componentsStrExt);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(dateStrExt).dataType(ICalDataType.DATE_TIME).run(hasDate);
		sensei.assertParseJson(datetimeStrExt).dataType(ICalDataType.DATE_TIME).run(hasDateTime);
		sensei.assertParseJson("invalid").dataType(ICalDataType.DATE_TIME).cannotParse();
		sensei.assertParseJson("").dataType(ICalDataType.DATE_TIME).cannotParse();
	}

	private class DateOrDateTimePropertyMarshallerImpl extends DateOrDateTimePropertyScribe<DateOrDateTimePropertyImpl> {
		public DateOrDateTimePropertyMarshallerImpl() {
			super(DateOrDateTimePropertyImpl.class, "DATE-OR-DATETIME");
		}

		@Override
		protected DateOrDateTimePropertyImpl newInstance(Date date, boolean hasTime) {
			return new DateOrDateTimePropertyImpl(date, hasTime);
		}
	}

	private class DateOrDateTimePropertyImpl extends DateOrDateTimeProperty {
		public DateOrDateTimePropertyImpl(DateTimeComponents component) {
			super(component);
		}

		public DateOrDateTimePropertyImpl(Date value, boolean hasTime) {
			super(value, hasTime);
		}
	}

	private final Check<DateOrDateTimePropertyImpl> hasDate = new Check<DateOrDateTimePropertyImpl>() {
		public void check(DateOrDateTimePropertyImpl property) {
			assertEquals(date, property.getValue());
			assertEquals(new DateTimeComponents(2013, 6, 11, 0, 0, 0, false), property.getRawComponents());
			assertFalse(property.hasTime());
		}
	};

	private final Check<DateOrDateTimePropertyImpl> hasDateTime = new Check<DateOrDateTimePropertyImpl>() {
		public void check(DateOrDateTimePropertyImpl property) {
			assertEquals(datetime, property.getValue());
			assertEquals(new DateTimeComponents(2013, 6, 11, 12, 43, 2, true), property.getRawComponents());
			assertTrue(property.hasTime());
		}
	};

}
