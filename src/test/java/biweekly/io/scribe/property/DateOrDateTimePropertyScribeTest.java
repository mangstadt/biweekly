package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.buildTimezone;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.TimeZone;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.component.VTimezone;
import biweekly.io.TimezoneInfo;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.io.scribe.property.Sensei.WriteTest;
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

	private final DateOrDateTimePropertyMarshallerImpl scribe = new DateOrDateTimePropertyMarshallerImpl();
	private final Sensei<DateOrDateTimePropertyImpl> sensei = new Sensei<DateOrDateTimePropertyImpl>(scribe);

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
	private final DateOrDateTimePropertyImpl withComponents = new DateOrDateTimePropertyImpl(components, true);
	private final DateOrDateTimePropertyImpl withDateAndComponents = new DateOrDateTimePropertyImpl(datetime, false);
	{
		withDateAndComponents.setRawComponents(components, false);
	}
	private final DateOrDateTimePropertyImpl withDateTimeAndComponents = new DateOrDateTimePropertyImpl(datetime, true);
	{
		withDateTimeAndComponents.setRawComponents(components, true);
	}
	private final DateOrDateTimePropertyImpl empty = new DateOrDateTimePropertyImpl((Date) null, false);

	private final TimezoneInfo floatingGlobal = new TimezoneInfo();
	{
		floatingGlobal.setGlobalFloatingTime(true);
	}

	private final TimezoneInfo timezoneGlobal = new TimezoneInfo();
	{
		TimeZone tz = buildTimezone(-2, 0);
		timezoneGlobal.assign(new VTimezone("id"), tz);
		timezoneGlobal.setDefaultTimeZone(tz);
	}

	@Test
	public void dataType() {
		sensei.assertDataType(withDate).run(ICalDataType.DATE);
		sensei.assertDataType(withDateTime).run(ICalDataType.DATE_TIME);
		sensei.assertDataType(withComponents).run(ICalDataType.DATE_TIME);
		sensei.assertDataType(withDateAndComponents).run(ICalDataType.DATE);
		sensei.assertDataType(withDateTimeAndComponents).run(ICalDataType.DATE_TIME);
		sensei.assertDataType(empty).run(ICalDataType.DATE);
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

		assertComponents(sensei.assertWriteText(withComponents), componentsStr);

		assertDate(sensei.assertWriteText(withDateAndComponents), dateStr);

		//@formatter:off
		assertDateTime(sensei.assertWriteText(withDateTimeAndComponents),
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
		sensei.assertParseText("invalid").dataType(ICalDataType.DATE_TIME).cannotParse();
		sensei.assertParseText("").dataType(ICalDataType.DATE_TIME).cannotParse();
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

		assertComponents(sensei.assertWriteXml(withComponents), "<date-time>" + componentsStrExt + "</date-time>");

		assertDate(sensei.assertWriteXml(withDateAndComponents), "<date>" + dateStrExt + "</date>");

		//@formatter:off
		assertDateTime(sensei.assertWriteXml(withDateTimeAndComponents),
			"<date-time>2013-06-11T12:43:02Z</date-time>",
			"<date-time>2013-06-11T13:43:02</date-time>",
			"<date-time>2013-06-11T11:43:02</date-time>",
			"<date-time>2013-06-11T10:43:02</date-time>"
		);
		//@formatter:on

		sensei.assertWriteXml(empty).run("<date/>");
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
		assertDate(sensei.assertWriteJson(withDate), dateStrExt);

		//@formatter:off
		assertDateTime(sensei.assertWriteJson(withDateTime),
			"2013-06-11T12:43:02Z",
			"2013-06-11T13:43:02",
			"2013-06-11T11:43:02",
			"2013-06-11T10:43:02"
		);
		//@formatter:on

		assertComponents(sensei.assertWriteJson(withComponents), componentsStrExt);

		assertDate(sensei.assertWriteJson(withDateAndComponents), dateStrExt);

		//@formatter:off
		assertDateTime(sensei.assertWriteJson(withDateTimeAndComponents),
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
		sensei.assertParseJson("invalid").dataType(ICalDataType.DATE_TIME).cannotParse();
		sensei.assertParseJson("").dataType(ICalDataType.DATE_TIME).cannotParse();
	}

	@SuppressWarnings("rawtypes")
	private void assertDate(Sensei<DateOrDateTimePropertyImpl>.WriteTest<? extends WriteTest> test, String expected) {
		//date values are uneffected by timezone options
		test.run(expected);
		test.tz(floatingGlobal).run(expected);
		test.tz(timezoneGlobal).run(expected);
	}

	@SuppressWarnings("rawtypes")
	private void assertDateTime(Sensei<DateOrDateTimePropertyImpl>.WriteTest<? extends Sensei<DateOrDateTimePropertyImpl>.WriteTest<? extends WriteTest>> test, String utc, String floating, String minusOne, String minusTwo) {
		VTimezone vtimezone = new VTimezone("id");
		TimeZone tz1 = buildTimezone(-1, 0);
		TimeZone tz2 = buildTimezone(-2, 0);
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

		//global timezone
		test.tz(timezoneGlobal).run(minusTwo);

		//property-assigned timezone
		tzinfo = new TimezoneInfo();
		tzinfo.assign(vtimezone, tz1);
		tzinfo.setTimeZone(test.property, tz1);
		test.tz(tzinfo).run(minusOne);

		//property-assigned floating should override global timezone
		tzinfo = new TimezoneInfo();
		tzinfo.assign(vtimezone, tz1);
		tzinfo.setDefaultTimeZone(tz1);
		tzinfo.setFloating(test.property, true);
		test.tz(tzinfo).run(floating);

		//property-assigned timezone should override global timezone
		tzinfo = new TimezoneInfo();
		tzinfo.assign(vtimezone, tz1);
		tzinfo.assign(vtimezone, tz2);
		tzinfo.setDefaultTimeZone(tz1);
		tzinfo.setTimeZone(test.property, tz2);
		test.tz(tzinfo).run(minusTwo);

		//property-assigned timezone should override global floating
		tzinfo = new TimezoneInfo();
		tzinfo.assign(vtimezone, tz1);
		tzinfo.setGlobalFloatingTime(true);
		tzinfo.setTimeZone(test.property, tz1);
		test.tz(tzinfo).run(minusOne);
	}

	@SuppressWarnings("rawtypes")
	private void assertComponents(Sensei<DateOrDateTimePropertyImpl>.WriteTest<? extends WriteTest> test, String expected) {
		//date components are uneffected by timezone options
		test.run(expected);
		test.tz(floatingGlobal).run(expected);
		test.tz(timezoneGlobal).run(expected);
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
		public DateOrDateTimePropertyImpl(DateTimeComponents component, boolean hasTime) {
			super(component, hasTime);
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
