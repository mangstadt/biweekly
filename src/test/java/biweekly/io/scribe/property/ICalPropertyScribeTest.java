package biweekly.io.scribe.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.scribe.property.ICalPropertyScribeTest.TestProperty;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.util.ICalDate;
import biweekly.util.ListMultimap;

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
public class ICalPropertyScribeTest extends ScribeTest<TestProperty> {
	private final Date datetime = date("2013-06-11 14:43:02");

	public ICalPropertyScribeTest() {
		super(new ICalPropertyMarshallerImpl());
	}

	@Test
	public void DateParser_timezone() {
		String value = "20130611T134302Z";

		Date actual = ICalPropertyScribe.date(value).parse();

		assertEquals(datetime, actual);
	}

	@Test
	public void DateParser_local() {
		String value = "20130611T144302";

		Date actual = ICalPropertyScribe.date(value).parse();

		assertEquals(datetime, actual);
	}

	@Test
	public void DateWriter_datetime() {
		String expected = "20130611T134302Z"; //write as UTC by default
		String actual = ICalPropertyScribe.date(datetime).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_extended() {
		String expected = "2013-06-11T13:43:02Z";
		String actual = ICalPropertyScribe.date(datetime).extended(true).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_utc() {
		String expected = "20130611T134302Z";
		String actual = ICalPropertyScribe.date(datetime).utc(true).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_utc_extended() {
		String expected = "2013-06-11T13:43:02Z";
		String actual = ICalPropertyScribe.date(datetime).utc(true).extended(true).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_date() {
		String expected = "20130611";
		String actual = ICalPropertyScribe.date(new ICalDate(datetime, false)).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_date_extended() {
		String expected = "2013-06-11";
		String actual = ICalPropertyScribe.date(new ICalDate(datetime, false)).extended(true).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_timezone() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		String expected = "20130611T154302";
		String actual = ICalPropertyScribe.date(datetime).tz(false, timezone).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_timezone_extended() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		String expected = "2013-06-11T15:43:02";
		String actual = ICalPropertyScribe.date(datetime).tz(false, timezone).extended(true).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_local_time() {
		String expected = "20130611T144302";
		String actual = ICalPropertyScribe.date(datetime).tz(true, null).write();
		assertEquals(expected, actual);
	}

	@Test
	public void prepareParameters() {
		TestProperty property = new TestProperty("value");
		ICalParameters copy = scribe.prepareParameters(property, null);

		assertFalse(property.getParameters() == copy);
		assertEquals("value", copy.first("PARAM"));
	}

	@Test
	public void writeText() {
		TestProperty property = new TestProperty("value");
		sensei.assertWriteText(property).run("value");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("value").warnings((Integer) null).run(new Check<TestProperty>() {
			public void check(TestProperty property, ParseContext context) {
				has(ICalDataType.TEXT, "value").check(property, context);
			}
		});
	}

	@Test
	public void writeXml() {
		TestProperty prop = new TestProperty("value");
		sensei.assertWriteXml(prop).run("<text>value</text>");
	}

	@Test
	public void parseXml() {
		//@formatter:off
		sensei.assertParseXml(
		"<ignore xmlns=\"http://example.com\">ignore-me</ignore>" +
		"<integer>value</integer>" +
		"<text>ignore-me</text>"
		).warnings((Integer) null).run(has(ICalDataType.INTEGER, "value"));
		
		//no xCal element
		sensei.assertParseXml(
		"<one xmlns=\"http://example.com\">1</one>" +
		"<two xmlns=\"http://example.com\">2</two>"
		).warnings((Integer) null).run(has(null, "12"));
		
		//no child elements
		sensei.assertParseXml("value").warnings((Integer)null).run(has(null, "value"));
		
		//unknown data type
		sensei.assertParseXml("<unknown>value</unknown>"
		).warnings((Integer) null).run(has(null, "value"));
		sensei.assertParseXml("<unknown />"
		).warnings((Integer) null).run(has(null, ""));
		//@formatter:on
	}

	@Test
	public void writeJson() {
		TestProperty prop = new TestProperty("value");
		sensei.assertWriteJson(prop).run("value");
	}

	@Test
	public void parseJson() {
		//@formatter:off
		sensei.assertParseJson("value").warnings((Integer)null).run(has(ICalDataType.TEXT, "value"));
		
		//multivalued
		sensei.assertParseJson(JCalValue.multi("value1", "val,;ue2")).warnings((Integer)null).run(has(ICalDataType.TEXT, "value1,val\\,\\;ue2"));
		
		//structured
		sensei.assertParseJson(JCalValue.structured("value1", "val,;ue2")).warnings((Integer)null).run(has(ICalDataType.TEXT, "value1;val\\,\\;ue2"));
		
		//object
		ListMultimap<String, Object> map = new ListMultimap<String, Object>();
		map.put("a", "one");
		map.put("b", "two");
		map.put("b", "three,four;five\\six=seven");
		sensei.assertParseJson(JCalValue.object(map)).warnings((Integer)null).run(has(ICalDataType.TEXT, "A=one;B=two,three\\,four\\;five\\\\six=seven"));
		//@formatter:on
	}

	@Test
	public void handleTzidParameter() {
		ICalProperty property = new TestProperty("");
		final TimezoneAssignment nyTimeZone = new TimezoneAssignment(TimeZone.getTimeZone("America/New_York"), new VTimezone("America/New_York"));
		final TimezoneAssignment nySolidus = new TimezoneAssignment(TimeZone.getTimeZone("America/New_York"), "America/New_York");

		//1.0 doesn't use TZID parameter
		ICalVersion version = V1_0;
		{
			boolean hasTime = true;
			TimezoneInfo tzinfo = new TimezoneInfo();
			tzinfo.setDefaultTimezone(nyTimeZone);
			ICalParameters parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertNull(parameters.getTimezoneId());
		}

		version = V2_0;
		{
			//property has no time component
			boolean hasTime = false;
			TimezoneInfo tzinfo = new TimezoneInfo();
			ICalParameters parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertNull(parameters.getTimezoneId());

			//no timezone assigned to property
			hasTime = true;
			tzinfo = new TimezoneInfo();
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertNull(parameters.getTimezoneId());

			//floating
			hasTime = true;
			tzinfo = new TimezoneInfo();
			tzinfo.setFloating(property, true);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertNull(parameters.getTimezoneId());

			//default timezone
			hasTime = true;
			tzinfo = new TimezoneInfo();
			tzinfo.setDefaultTimezone(nyTimeZone);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertEquals(nyTimeZone.getTimeZone().getID(), parameters.getTimezoneId());

			//property timezone
			hasTime = true;
			tzinfo = new TimezoneInfo();
			tzinfo.setTimezone(property, nyTimeZone);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertEquals(nyTimeZone.getTimeZone().getID(), parameters.getTimezoneId());

			//solidus timezone
			hasTime = true;
			tzinfo = new TimezoneInfo();
			tzinfo.setTimezone(property, nySolidus);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertEquals("/" + nySolidus.getGlobalId(), parameters.getTimezoneId());

			//global timezone
			hasTime = true;
			tzinfo = new TimezoneInfo();
			tzinfo.setDefaultTimezone(nyTimeZone);
			TimezoneAssignment globalTz = new TimezoneAssignment(new SimpleTimeZone(0, "TEST"), new VTimezone("Foo/Bar"));
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, globalTz));
			assertEquals("Foo/Bar", parameters.getTimezoneId());

			//global timezone with solidus
			hasTime = true;
			tzinfo = new TimezoneInfo();
			tzinfo.setTimezone(property, nySolidus);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, globalTz));
			assertEquals("Foo/Bar", parameters.getTimezoneId());
		}
	}

	public static class ICalPropertyMarshallerImpl extends ICalPropertyScribe<TestProperty> {
		private ICalPropertyMarshallerImpl() {
			super(TestProperty.class, "TEST", ICalDataType.TEXT);
		}

		@Override
		protected ICalParameters _prepareParameters(TestProperty property, WriteContext context) {
			ICalParameters copy = new ICalParameters(property.getParameters());
			copy.put("PARAM", "value");
			return copy;
		}

		@Override
		protected String _writeText(TestProperty property, WriteContext context) {
			return property.value;
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			context.addWarning("parseText");
			return new TestProperty(value, dataType);
		}
	}

	public static class TestProperty extends ICalProperty {
		public String value;
		public ICalDataType parsedDataType;

		public TestProperty(String value) {
			this.value = value;
		}

		public TestProperty(String value, ICalDataType parsedDataType) {
			this.value = value;
			this.parsedDataType = parsedDataType;
		}
	}

	private Check<TestProperty> has(final ICalDataType dataType, final String value) {
		return new Check<TestProperty>() {
			public void check(TestProperty property, ParseContext context) {
				assertEquals(dataType, property.parsedDataType);
				assertEquals(value, property.value);
			}
		};
	}

}
