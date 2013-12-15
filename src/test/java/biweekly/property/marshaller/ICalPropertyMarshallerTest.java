package biweekly.property.marshaller;

import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.Warning;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller.SemiStructuredIterator;
import biweekly.property.marshaller.ICalPropertyMarshaller.StructuredIterator;
import biweekly.property.marshaller.Sensei.Check;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.ListMultimap;

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
public class ICalPropertyMarshallerTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	private final ICalPropertyMarshallerImpl marshaller = new ICalPropertyMarshallerImpl();
	private final Sensei<TestProperty> sensei = new Sensei<TestProperty>(marshaller);

	private final Date datetime;
	{
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Calendar.YEAR, 2013);
		c.set(Calendar.MONTH, Calendar.JUNE);
		c.set(Calendar.DATE, 11);
		c.set(Calendar.HOUR_OF_DAY, 14);
		c.set(Calendar.MINUTE, 43);
		c.set(Calendar.SECOND, 2);
		datetime = c.getTime();
	}

	@Test
	public void unescape() {
		String expected, actual;

		actual = ICalPropertyMarshaller.unescape("\\\\ \\, \\; \\n \\\\\\,");
		expected = "\\ , ; " + NEWLINE + " \\,";
		assertEquals(expected, actual);
	}

	@Test
	public void escape() {
		String actual, expected;

		actual = ICalPropertyMarshaller.escape("One; Two, Three\\ Four\n Five\r\n Six\r");
		expected = "One\\; Two\\, Three\\\\ Four\n Five\r\n Six\r";
		assertEquals(expected, actual);
	}

	@Test
	public void split() {
		List<String> actual, expected;

		actual = ICalPropertyMarshaller.split("Doe;John;Joh\\,\\;nny;;Sr.,III", ";").split();
		expected = Arrays.asList("Doe", "John", "Joh\\,\\;nny", "", "Sr.,III");
		assertEquals(expected, actual);

		actual = ICalPropertyMarshaller.split("Doe;John;Joh\\,\\;nny;;Sr.,III", ";").removeEmpties(true).split();
		expected = Arrays.asList("Doe", "John", "Joh\\,\\;nny", "Sr.,III");
		assertEquals(expected, actual);

		actual = ICalPropertyMarshaller.split("Doe;John;Joh\\,\\;nny;;Sr.,III", ";").unescape(true).split();
		expected = Arrays.asList("Doe", "John", "Joh,;nny", "", "Sr.,III");
		assertEquals(expected, actual);

		actual = ICalPropertyMarshaller.split("Doe;John;Joh\\,\\;nny;;Sr.,III", ";").removeEmpties(true).unescape(true).split();
		expected = Arrays.asList("Doe", "John", "Joh,;nny", "Sr.,III");
		assertEquals(expected, actual);
	}

	@Test
	public void DateParser_timezone() {
		String value = "20130611T134302Z";

		Date actual = ICalPropertyMarshaller.date(value).parse();

		assertEquals(datetime, actual);
	}

	@Test
	public void DateParser_local() {
		String value = "20130611T144302";

		Date actual = ICalPropertyMarshaller.date(value).parse();

		assertEquals(datetime, actual);
	}

	@Test
	public void DateParser_tzid() {
		String value = "20130611T144302";
		List<Warning> warnings = new ArrayList<Warning>();

		Date actual = ICalPropertyMarshaller.date(value).tzid("some ID", warnings).parse();

		//parse as local time
		assertEquals(datetime, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void DateParser_tzid_null() {
		String value = "20130611T144302";
		List<Warning> warnings = new ArrayList<Warning>();

		Date actual = ICalPropertyMarshaller.date(value).tzid(null, warnings).parse();

		//parse as local time
		assertEquals(datetime, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void DateParser_global_tzid() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		String value = "20130611T154302";
		List<Warning> warnings = new ArrayList<Warning>();

		Date actual = ICalPropertyMarshaller.date(value).tzid(timezone.getID(), warnings).parse();

		assertEquals(datetime, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void DateParser_timezone_object() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		String value = "20130611T154302";

		Date actual = ICalPropertyMarshaller.date(value).tz(timezone).parse();

		assertEquals(datetime, actual);
	}

	@Test
	public void DateParser_invalid_global_tzid() {
		String value = "20130611T144302";
		List<Warning> warnings = new ArrayList<Warning>();

		Date actual = ICalPropertyMarshaller.date(value).tzid("invalid/timezone", warnings).parse();

		//parse as local time and add warning
		assertEquals(datetime, actual);
		assertWarnings(1, warnings);
	}

	@Test
	public void DateWriter_datetime() {
		String expected = "20130611T134302Z"; //write as UTC by default
		String actual = ICalPropertyMarshaller.date(datetime).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_extended() {
		String expected = "2013-06-11T13:43:02Z";
		String actual = ICalPropertyMarshaller.date(datetime).extended(true).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_date() {
		String expected = "20130611";
		String actual = ICalPropertyMarshaller.date(datetime).time(false).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_date_extended() {
		String expected = "2013-06-11";
		String actual = ICalPropertyMarshaller.date(datetime).time(false).extended(true).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_global_tzid() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		String expected = "20130611T154302";
		String actual = ICalPropertyMarshaller.date(datetime).tzid(timezone.getID()).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_global_tzid_extended() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		String expected = "2013-06-11T15:43:02";
		String actual = ICalPropertyMarshaller.date(datetime).tzid(timezone.getID()).extended(true).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_timezone() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		String expected = "20130611T154302";
		String actual = ICalPropertyMarshaller.date(datetime).tz(timezone).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_invalid_global_tzid() {
		String expected = "20130611T134302Z";
		String actual = ICalPropertyMarshaller.date(datetime).tzid("invalid/timezone").write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_tzid() {
		String expected = "20130611T144302";
		String actual = ICalPropertyMarshaller.date(datetime).tzid("some ID").write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_tzid_null() {
		String expected = "20130611T134302Z";
		String actual = ICalPropertyMarshaller.date(datetime).tzid(null).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_local_time() {
		String expected = "20130611T144302";
		String actual = ICalPropertyMarshaller.date(datetime).localTz(true).write();
		assertEquals(expected, actual);
	}

	@Test
	public void DateWriter_datetime_local_time_false() {
		String expected = "20130611T134302Z";
		String actual = ICalPropertyMarshaller.date(datetime).localTz(false).write(); //should ignore the method call
		assertEquals(expected, actual);
	}

	@Test
	public void list_parse() {
		List<String> actual = ICalPropertyMarshaller.list("one ,, two,three\\,four");
		List<String> expected = Arrays.asList("one", "", "two", "three,four");
		assertEquals(expected, actual);
	}

	@Test
	public void list_parse_empty() {
		List<String> actual = ICalPropertyMarshaller.list("");
		List<String> expected = Arrays.asList();
		assertEquals(expected, actual);
	}

	@Test
	public void list_write() {
		String actual = ICalPropertyMarshaller.list("one", null, "two", "three,four");
		String expected = "one,,two,three\\,four";
		assertEquals(expected, actual);
	}

	@Test
	public void semistructured_parse() {
		String input = "one;two,three\\,four;;;five\\;six";

		SemiStructuredIterator it = ICalPropertyMarshaller.semistructured(input);
		assertEquals("one", it.next());
		assertEquals("two,three,four", it.next());
		assertEquals(null, it.next());
		assertEquals(null, it.next());
		assertEquals("five;six", it.next());
		assertEquals(null, it.next());
	}

	@Test
	public void semistructured_parse_limit() {
		String input = "one;two,three\\,four;;;five\\;six";

		SemiStructuredIterator it = ICalPropertyMarshaller.semistructured(input, 2);
		assertEquals("one", it.next());
		assertEquals("two,three,four;;;five;six", it.next());
		assertEquals(null, it.next());
		assertEquals(null, it.next());
		assertEquals(null, it.next());
		assertEquals(null, it.next());
	}

	@Test
	public void structured_parse_string() {
		String input = "one;two,three\\,four;;;five\\;six";

		//using "nextComponent()"
		StructuredIterator it = ICalPropertyMarshaller.structured(input);
		assertEquals(Arrays.asList("one"), it.nextComponent());
		assertEquals(Arrays.asList("two", "three,four"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList("five;six"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());

		//using "nextString()"
		it = ICalPropertyMarshaller.structured(input);
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
		StructuredIterator it = ICalPropertyMarshaller.structured(input);
		assertEquals(Arrays.asList("one"), it.nextComponent());
		assertEquals(Arrays.asList("two", "three,four"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());
		assertEquals(Arrays.asList("five;six"), it.nextComponent());
		assertEquals(Arrays.asList(), it.nextComponent());

		//using "nextString()"
		it = ICalPropertyMarshaller.structured(input);
		assertEquals("one", it.nextString());
		assertEquals("two", it.nextString());
		assertEquals(null, it.nextString());
		assertEquals(null, it.nextString());
		assertEquals("five;six", it.nextString());
		assertEquals(null, it.nextString());
	}

	@Test
	public void structured_write() {
		String actual = ICalPropertyMarshaller.structured("one", 2, null, "four;five,six\\seven", Arrays.asList("eight"), Arrays.asList("nine", null, "ten;eleven,twelve\\thirteen"));
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

		ListMultimap<String, String> actual = ICalPropertyMarshaller.object(input);
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
		String actual = ICalPropertyMarshaller.object(input.getMap());
		assertEquals(expected, actual);
	}

	@Test
	public void prepareParameters() {
		TestProperty property = new TestProperty("value");
		ICalParameters copy = marshaller.prepareParameters(property);

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
		final ICalParameters params = new ICalParameters();
		sensei.assertParseText("value").params(params).warnings(1).run(new Check<TestProperty>() {
			public void check(TestProperty property) {
				assertTrue(params == property.getParameters());
				has(ICalDataType.TEXT, "value").check(property);
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
	public void missingXmlElements() {
		CannotParseException e = ICalPropertyMarshaller.missingXmlElements(new String[0]);
		assertEquals("Property value empty.", e.getMessage());

		e = ICalPropertyMarshaller.missingXmlElements("one");
		assertEquals("Property value empty (no <one> element found).", e.getMessage());

		e = ICalPropertyMarshaller.missingXmlElements("one", "two");
		assertEquals("Property value empty (no <one> or <two> elements found).", e.getMessage());

		e = ICalPropertyMarshaller.missingXmlElements("one", "two", "THREE");
		assertEquals("Property value empty (no <one>, <two>, or <THREE> elements found).", e.getMessage());

		e = ICalPropertyMarshaller.missingXmlElements(ICalDataType.TEXT, null, ICalDataType.DATE);
		assertEquals("Property value empty (no <text>, <unknown>, or <date> elements found).", e.getMessage());
	}

	private class ICalPropertyMarshallerImpl extends ICalPropertyMarshaller<TestProperty> {
		private ICalPropertyMarshallerImpl() {
			super(TestProperty.class, "TEST", ICalDataType.TEXT);
		}

		@Override
		protected void _prepareParameters(TestProperty property, ICalParameters copy) {
			copy.put("PARAM", "value");
		}

		@Override
		protected String _writeText(TestProperty property) {
			return property.value;
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
			warnings.add(new Warning("parseText"));
			return new TestProperty(value, dataType);
		}
	}

	private class TestProperty extends ICalProperty {
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
			public void check(TestProperty property) {
				assertEquals(dataType, property.parsedDataType);
				assertEquals(value, property.value);
			}
		};
	}

}
