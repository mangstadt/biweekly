package biweekly.io.scribe.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.scribe.property.ICalPropertyScribe.SemiStructuredIterator;
import biweekly.io.scribe.property.ICalPropertyScribe.StructuredIterator;
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
	public void unescape() {
		String expected, actual;

		actual = ICalPropertyScribe.unescape("\\\\ \\, \\; \\n \\\\\\,");
		expected = "\\ , ; " + NEWLINE + " \\,";
		assertEquals(expected, actual);
	}

	@Test
	public void escape() {
		String actual, expected;

		actual = ICalPropertyScribe.escape("One; Two, Three\\ Four\n Five\r\n Six\r");
		expected = "One\\; Two\\, Three\\\\ Four\n Five\r\n Six\r";
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_limit() {
		String str = "one,two,three,four";
		List<String> actual, expected;

		actual = ICalPropertyScribe.splitter(',').split(str);
		expected = Arrays.asList("one", "two", "three", "four");
		assertEquals(expected, actual);

		actual = ICalPropertyScribe.splitter(',').limit(2).split(str);
		expected = Arrays.asList("one", "two,three,four");
		assertEquals(expected, actual);

		actual = ICalPropertyScribe.splitter(',').limit(4).split(str);
		expected = Arrays.asList("one", "two", "three", "four");
		assertEquals(expected, actual);

		actual = ICalPropertyScribe.splitter(',').limit(10).split(str);
		expected = Arrays.asList("one", "two", "three", "four");
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_unescape() {
		String str = "one,two\\,\\;three";
		List<String> actual, expected;

		actual = ICalPropertyScribe.splitter(',').split(str);
		expected = Arrays.asList("one", "two\\,\\;three");
		assertEquals(expected, actual);

		actual = ICalPropertyScribe.splitter(',').unescape(true).split(str);
		expected = Arrays.asList("one", "two,;three");
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_nullEmpties() {
		String str = ",one,,two,";
		List<String> actual, expected;

		actual = ICalPropertyScribe.splitter(',').split(str);
		expected = Arrays.asList("", "one", "", "two", "");
		assertEquals(expected, actual);

		actual = ICalPropertyScribe.splitter(',').nullEmpties(true).split(str);
		expected = Arrays.asList(null, "one", null, "two", null);
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_trim() {
		List<String> actual = ICalPropertyScribe.splitter(',').split("one , two");
		List<String> expected = Arrays.asList("one", "two");
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_empty() {
		List<String> actual = ICalPropertyScribe.splitter(',').split("");
		List<String> expected = Arrays.asList("");
		assertEquals(expected, actual);
	}

	@Test
	public void splitter_all_settings() {
		String str = "one ,two\\,three,,four,five";
		List<String> actual, expected;

		actual = ICalPropertyScribe.splitter(',').split(str);
		expected = Arrays.asList("one", "two\\,three", "", "four", "five");
		assertEquals(expected, actual);

		actual = ICalPropertyScribe.splitter(',').unescape(true).limit(4).nullEmpties(true).split(str);
		expected = Arrays.asList("one", "two,three", null, "four,five");
		assertEquals(expected, actual);
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
	public void list_parse() {
		List<String> actual = ICalPropertyScribe.list("one ,, two,three\\,four");
		List<String> expected = Arrays.asList("one", "", "two", "three,four");
		assertEquals(expected, actual);
	}

	@Test
	public void list_parse_empty() {
		List<String> actual = ICalPropertyScribe.list("");
		List<String> expected = Arrays.asList();
		assertEquals(expected, actual);
	}

	@Test
	public void list_write() {
		String actual = ICalPropertyScribe.list("one", null, "two", "three,four");
		String expected = "one,,two,three\\,four";
		assertEquals(expected, actual);
	}

	@Test
	public void semistructured_parse() {
		String input = "one;two,three\\,four;;;five\\;six";

		SemiStructuredIterator it = ICalPropertyScribe.semistructured(input);
		assertEquals("one", it.next());
		assertEquals("two,three,four", it.next());
		assertEquals("", it.next());
		assertEquals("", it.next());
		assertEquals("five;six", it.next());
		assertEquals(null, it.next());
	}

	@Test
	public void semistructured_nullEmpties() {
		String input = "one;two,three\\,four;;;five\\;six";

		SemiStructuredIterator it = ICalPropertyScribe.semistructured(input, true);
		assertEquals("one", it.next());
		assertEquals("two,three,four", it.next());
		assertEquals(null, it.next());
		assertEquals(null, it.next());
		assertEquals("five;six", it.next());
		assertEquals(null, it.next());
	}

	@Test
	public void semistructured_parse_empty() {
		String input = "";

		SemiStructuredIterator it = ICalPropertyScribe.semistructured(input);
		assertEquals("", it.next());
		assertEquals(null, it.next());
		assertFalse(it.hasNext());
	}

	@Test
	public void structured_parse() {
		String input = "one;two,three\\,four;;;five\\;six";

		//using "nextComponent()"
		StructuredIterator it = ICalPropertyScribe.structured(input);
		assertEquals(Arrays.asList("one"), it.nextComponent());
		assertEquals(Arrays.asList("two", "three,four"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList("five;six"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());

		//using "nextString()"
		it = ICalPropertyScribe.structured(input);
		assertEquals("one", it.nextString());
		assertEquals("two", it.nextString());
		assertEquals(null, it.nextString());
		assertEquals(null, it.nextString());
		assertEquals("five;six", it.nextString());
		assertEquals(null, it.nextString());
	}

	@Test
	public void structured_parse_jcal_value() {
		JCalValue input = JCalValue.structured("one", Arrays.asList("two", "three,four"), null, "", "five;six");

		//using "nextComponent()"
		StructuredIterator it = ICalPropertyScribe.structured(input);
		assertEquals(Arrays.asList("one"), it.nextComponent());
		assertEquals(Arrays.asList("two", "three,four"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList("five;six"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());

		//using "nextString()"
		it = ICalPropertyScribe.structured(input);
		assertEquals("one", it.nextString());
		assertEquals("two", it.nextString());
		assertEquals(null, it.nextString());
		assertEquals(null, it.nextString());
		assertEquals("five;six", it.nextString());
		assertEquals(null, it.nextString());
	}

	@Test
	public void structured_parse_empty() {
		String input = "";

		//using "nextComponent()"
		StructuredIterator it = ICalPropertyScribe.structured(input);
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertFalse(it.hasNext());

		//using "nextString()"
		it = ICalPropertyScribe.structured(input);
		assertEquals(null, it.nextString());
		assertEquals(null, it.nextString());
		assertFalse(it.hasNext());
	}

	@Test
	public void structured_write() {
		String actual = ICalPropertyScribe.structured("one", 2, null, "four;five,six\\seven", Arrays.asList("eight"), Arrays.asList("nine", null, "ten;eleven,twelve\\thirteen"));
		assertEquals("one;2;;four\\;five\\,six\\\\seven;eight;nine,,ten\\;eleven\\,twelve\\\\thirteen", actual);
	}

	@Test
	public void object_parse() {
		String input = "a=one;b=two,three\\,four\\;five;c;d=six=seven";

		ListMultimap<String, String> expected = new ListMultimap<String, String>();
		expected.put("A", "one");
		expected.put("B", "two");
		expected.put("B", "three,four;five");
		expected.put("C", "");
		expected.put("D", "six=seven");

		ListMultimap<String, String> actual = ICalPropertyScribe.object(input);
		assertEquals(expected, actual);
	}

	@Test
	public void object_parse_empty() {
		String input = "";

		ListMultimap<String, String> expected = new ListMultimap<String, String>();
		ListMultimap<String, String> actual = ICalPropertyScribe.object(input);
		assertEquals(expected, actual);
	}

	@Test
	public void object_write() {
		ListMultimap<String, String> input = new ListMultimap<String, String>();
		input.put("A", "one");
		input.put("B", "two");
		input.put("B", "three,four;five");
		input.put("C", "");
		input.put("d", "six=seven");

		String expected = "A=one;B=two,three\\,four\\;five;C=;D=six=seven";
		String actual = ICalPropertyScribe.object(input.asMap());
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
		sensei.assertParseText("value").warnings(1).run(new Check<TestProperty>() {
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
		).warnings(1).run(has(ICalDataType.INTEGER, "value"));
		
		//no xCal element
		sensei.assertParseXml(
		"<one xmlns=\"http://example.com\">1</one>" +
		"<two xmlns=\"http://example.com\">2</two>"
		).warnings(1).run(has(null, "12"));
		
		//no child elements
		sensei.assertParseXml("value").warnings(1).run(has(null, "value"));
		
		//unknown data type
		sensei.assertParseXml("<unknown>value</unknown>"
		).warnings(1).run(has(null, "value"));
		sensei.assertParseXml("<unknown />"
		).warnings(1).run(has(null, ""));
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
		sensei.assertParseJson("value").warnings(1).run(has(ICalDataType.TEXT, "value"));
		
		//multivalued
		sensei.assertParseJson(JCalValue.multi("value1", "val,;ue2")).warnings(1).run(has(ICalDataType.TEXT, "value1,val\\,\\;ue2"));
		
		//structured
		sensei.assertParseJson(JCalValue.structured("value1", "val,;ue2")).warnings(1).run(has(ICalDataType.TEXT, "value1;val\\,\\;ue2"));
		
		//object
		ListMultimap<String, Object> map = new ListMultimap<String, Object>();
		map.put("a", "one");
		map.put("b", "two");
		map.put("b", "three,four;five\\six=seven");
		sensei.assertParseJson(JCalValue.object(map)).warnings(1).run(has(ICalDataType.TEXT, "A=one;B=two,three\\,four\\;five\\\\six=seven"));
		//@formatter:on
	}

	@Test
	public void handleTzidParameter() {
		ICalProperty property = new TestProperty("");

		//1.0 doesn't use TZID parameter
		ICalVersion version = V1_0;
		{
			boolean hasTime = true;
			TimezoneInfo tzinfo = new TimezoneInfo();
			VTimezone component = new VTimezone("America/New_York");
			TimeZone timezone = TimeZone.getTimeZone("America/New_York");
			tzinfo.assign(component, timezone);
			tzinfo.setDefaultTimeZone(timezone);
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
			VTimezone component = new VTimezone("America/New_York");
			TimeZone timezone = TimeZone.getTimeZone("America/New_York");
			tzinfo.assign(component, timezone);
			tzinfo.setDefaultTimeZone(timezone);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertEquals(timezone.getID(), parameters.getTimezoneId());

			//property timezone
			hasTime = true;
			tzinfo = new TimezoneInfo();
			component = new VTimezone("America/New_York");
			timezone = TimeZone.getTimeZone("America/New_York");
			tzinfo.assign(component, timezone);
			tzinfo.setTimeZone(property, timezone);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertEquals(timezone.getID(), parameters.getTimezoneId());

			//solidus timezone
			hasTime = true;
			tzinfo = new TimezoneInfo();
			timezone = TimeZone.getTimeZone("America/New_York");
			tzinfo.setTimeZone(property, timezone, false);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, null));
			assertEquals("/" + timezone.getID(), parameters.getTimezoneId());

			//global timezone
			hasTime = true;
			tzinfo = new TimezoneInfo();
			component = new VTimezone("America/New_York");
			timezone = TimeZone.getTimeZone("America/New_York");
			tzinfo.assign(component, timezone);
			tzinfo.setDefaultTimeZone(timezone);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, new SimpleTimeZone(0, "TEST")));
			assertEquals("TEST", parameters.getTimezoneId());

			//global timezone with solidus
			hasTime = true;
			tzinfo = new TimezoneInfo();
			component = new VTimezone("America/New_York");
			timezone = TimeZone.getTimeZone("America/New_York");
			tzinfo.setTimeZone(property, timezone, false);
			parameters = ICalPropertyScribe.handleTzidParameter(property, hasTime, new WriteContext(version, tzinfo, new SimpleTimeZone(0, "TEST")));
			assertEquals("/TEST", parameters.getTimezoneId());
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
