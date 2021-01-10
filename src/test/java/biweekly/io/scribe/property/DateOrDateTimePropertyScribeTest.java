package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.buildTimezone;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.scribe.property.DateOrDateTimePropertyScribeTest.DateOrDateTimePropertyImpl;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.io.scribe.property.Sensei.WriteTest;
import biweekly.property.DateOrDateTimeProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDate;

/*
 Copyright (c) 2013-2021, Michael Angstadt
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
public class DateOrDateTimePropertyScribeTest extends ScribeTest<DateOrDateTimePropertyImpl> {
	private final Date date = date("2013-06-11");
	private final String dateStr = "20130611";
	private final String dateStrExt = "2013-06-11";

	private final Date datetime = date("2013-06-11 13:43:02");
	private final String datetimeStr = "20130611T124302Z";
	private final String datetimeStrExt = "2013-06-11T12:43:02Z";

	private final DateOrDateTimePropertyImpl withDateTime = new DateOrDateTimePropertyImpl(datetime, true);
	private final DateOrDateTimePropertyImpl withDate = new DateOrDateTimePropertyImpl(datetime, false);
	private final DateOrDateTimePropertyImpl empty = new DateOrDateTimePropertyImpl(null, false);

	private final TimezoneInfo floatingGlobal = new TimezoneInfo();
	{
		floatingGlobal.setGlobalFloatingTime(true);
	}

	private final TimezoneInfo timezoneDefault = new TimezoneInfo();
	{
		TimeZone tz = buildTimezone(-2, 0);
		timezoneDefault.setDefaultTimezone(new TimezoneAssignment(tz, new VTimezone("id")));
	}

	public DateOrDateTimePropertyScribeTest() {
		super(new DateOrDateTimePropertyMarshallerImpl());
	}

	@Test
	public void dataType() {
		sensei.assertDataType(withDate).run(ICalDataType.DATE);
		sensei.assertDataType(withDateTime).run(ICalDataType.DATE_TIME);
		sensei.assertDataType(empty).run(ICalDataType.DATE_TIME);
	}

	@Test
	public void writeText() {
		assertDate(sensei.assertWriteText(withDate), dateStr);

		//@formatter:off
		assertDateTime(sensei.assertWriteText(withDateTime),
			"20130611T124302Z",
			"20130611T134302",
			"20130611T114302",
			"20130611T104302"
		);
		//@formatter:on

		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(dateStr).dataType(ICalDataType.DATE).run(hasDate);
		sensei.assertParseText(datetimeStr).dataType(ICalDataType.DATE_TIME).run(hasDateTime);
		sensei.assertParseText("invalid").dataType(ICalDataType.DATE_TIME).cannotParse(17);
		sensei.assertParseText("").dataType(ICalDataType.DATE_TIME).cannotParse(17);
	}

	@Test
	public void writeXml() {
		assertDate(sensei.assertWriteXml(withDate), "<date>" + dateStrExt + "</date>");

		//@formatter:off
		assertDateTime(sensei.assertWriteXml(withDateTime),
			"<date-time>2013-06-11T12:43:02Z</date-time>",
			"<date-time>2013-06-11T13:43:02</date-time>",
			"<date-time>2013-06-11T11:43:02</date-time>",
			"<date-time>2013-06-11T10:43:02</date-time>"
		);
		//@formatter:on

		sensei.assertWriteXml(empty).run("<date-time/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<date>" + dateStrExt + "</date>").run(hasDate);
		sensei.assertParseXml("<date-time>" + datetimeStrExt + "</date-time>").run(hasDateTime);
		sensei.assertParseXml("<date-time>invalid</date-time>").cannotParse(17);
		sensei.assertParseXml("").cannotParse(23);
	}

	@Test
	public void writeJson() {
		assertDate(sensei.assertWriteJson(withDate), dateStrExt);

		//@formatter:off
		assertDateTime(sensei.assertWriteJson(withDateTime),
			"2013-06-11T12:43:02Z",
			"2013-06-11T13:43:02",
			"2013-06-11T11:43:02",
			"2013-06-11T10:43:02"
		);
		//@formatter:on

		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(dateStrExt).dataType(ICalDataType.DATE_TIME).run(hasDate);
		sensei.assertParseJson(datetimeStrExt).dataType(ICalDataType.DATE_TIME).run(hasDateTime);
		sensei.assertParseJson("invalid").dataType(ICalDataType.DATE_TIME).cannotParse(17);
		sensei.assertParseJson("").dataType(ICalDataType.DATE_TIME).cannotParse(17);
	}

	@SuppressWarnings("rawtypes")
	private void assertDate(Sensei<DateOrDateTimePropertyImpl>.WriteTest<? extends WriteTest> test, String expected) {
		//date values are uneffected by timezone options
		test.run(expected);
		test.tz(floatingGlobal).run(expected);
		test.tz(timezoneDefault).run(expected);
	}

	@SuppressWarnings("rawtypes")
	private void assertDateTime(Sensei<DateOrDateTimePropertyImpl>.WriteTest<? extends Sensei<DateOrDateTimePropertyImpl>.WriteTest<? extends WriteTest>> test, String utc, String floating, String minusOne, String minusTwo) {
		TimezoneAssignment tz1 = new TimezoneAssignment(buildTimezone(-1, 0), new VTimezone("id"));
		TimezoneAssignment tz2 = new TimezoneAssignment(buildTimezone(-2, 0), new VTimezone("id"));
		TimezoneInfo tzinfo;

		//UTC time
		tzinfo = new TimezoneInfo();
		test.tz(tzinfo).run(utc);

		//global floating time
		test.tz(floatingGlobal).run(floating);

		//property floating time
		tzinfo = new TimezoneInfo();
		tzinfo.setFloating(test.property, true);
		test.tz(tzinfo).run(floating);

		//default timezone
		test.tz(timezoneDefault).run(minusTwo);

		//property-assigned timezone
		tzinfo = new TimezoneInfo();
		tzinfo.setTimezone(test.property, tz1);
		test.tz(tzinfo).run(minusOne);

		//property-assigned floating should override global timezone
		tzinfo = new TimezoneInfo();
		tzinfo.setDefaultTimezone(tz1);
		tzinfo.setFloating(test.property, true);
		test.tz(tzinfo).run(floating);

		//property-assigned timezone should override global timezone
		tzinfo = new TimezoneInfo();
		tzinfo.setDefaultTimezone(tz1);
		tzinfo.setTimezone(test.property, tz2);
		test.tz(tzinfo).run(minusTwo);

		//property-assigned timezone should override global floating
		tzinfo = new TimezoneInfo();
		tzinfo.setGlobalFloatingTime(true);
		tzinfo.setTimezone(test.property, tz1);
		test.tz(tzinfo).run(minusOne);

		//global timezone should override everything
		test.tz(tzinfo).globalTz(tz2).run(minusTwo);
	}

	public static class DateOrDateTimePropertyMarshallerImpl extends DateOrDateTimePropertyScribe<DateOrDateTimePropertyImpl> {
		public DateOrDateTimePropertyMarshallerImpl() {
			super(DateOrDateTimePropertyImpl.class, "DATE-OR-DATETIME");
		}

		@Override
		protected DateOrDateTimePropertyImpl newInstance(ICalDate date) {
			return new DateOrDateTimePropertyImpl(date);
		}
	}

	public static class DateOrDateTimePropertyImpl extends DateOrDateTimeProperty {
		public DateOrDateTimePropertyImpl(Date value, boolean hasTime) {
			super(value, hasTime);
		}

		public DateOrDateTimePropertyImpl(ICalDate value) {
			super(value);
		}
	}

	private final Check<DateOrDateTimePropertyImpl> hasDate = new Check<DateOrDateTimePropertyImpl>() {
		public void check(DateOrDateTimePropertyImpl property, ParseContext context) {
			ICalDate value = property.getValue();
			assertEquals(date, value);
			assertEquals(new DateTimeComponents(2013, 6, 11), value.getRawComponents());
			assertFalse(value.hasTime());
		}
	};

	private final Check<DateOrDateTimePropertyImpl> hasDateTime = new Check<DateOrDateTimePropertyImpl>() {
		public void check(DateOrDateTimePropertyImpl property, ParseContext context) {
			ICalDate value = property.getValue();
			assertEquals(datetime, value);
			assertEquals(new DateTimeComponents(2013, 6, 11, 12, 43, 2, true), value.getRawComponents());
			assertTrue(value.hasTime());
		}
	};

}
