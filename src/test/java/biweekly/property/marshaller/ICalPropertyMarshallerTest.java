package biweekly.property.marshaller;

import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.buildTimezone;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
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
	private static TimeZone defaultTz;

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

	@BeforeClass
	public static void beforeClass() {
		defaultTz = TimeZone.getDefault();
		TimeZone.setDefault(buildTimezone(1, 0));
	}

	@AfterClass
	public static void afterClass() {
		TimeZone.setDefault(defaultTz);
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
		List<String> warnings = new ArrayList<String>();

		Date actual = ICalPropertyMarshaller.date(value).tzid("some ID", warnings).parse();

		//parse as local time
		assertEquals(datetime, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void DateParser_tzid_null() {
		String value = "20130611T144302";
		List<String> warnings = new ArrayList<String>();

		Date actual = ICalPropertyMarshaller.date(value).tzid(null, warnings).parse();

		//parse as local time
		assertEquals(datetime, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void DateParser_global_tzid() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		String value = "20130611T154302";
		List<String> warnings = new ArrayList<String>();

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
		List<String> warnings = new ArrayList<String>();

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
	public void parseList() {
		List<String> actual = ICalPropertyMarshaller.parseList("one , two,three\\,four");
		List<String> expected = Arrays.asList("one", "two", "three,four");
		assertEquals(expected, actual);
	}

	@Test
	public void parseComponents() {
		List<List<String>> actual = ICalPropertyMarshaller.parseComponent("one ; two,three\\,four;; ;five\\;six");
		//@formatter:off
		List<List<?>> expected = Arrays.<List<?>>asList(
			Arrays.asList("one"),
			Arrays.asList("two", "three,four"),
			Arrays.asList(),
			Arrays.asList(),
			Arrays.asList("five;six")
		);
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void prepareParameters() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		TestProperty property = new TestProperty("value");
		ICalParameters copy = m.prepareParameters(property);

		assertFalse(property.getParameters() == copy);
		assertEquals("value", copy.first("PARAM"));
	}

	@Test
	public void writeText() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		TestProperty property = new TestProperty("value");
		String value = m.writeText(property);

		assertEquals("value", value);
	}

	@Test
	public void parseText() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		ICalParameters params = new ICalParameters();
		params.put("PARAM", "value");
		ICalPropertyMarshaller.Result<TestProperty> result = m.parseText("value", ICalDataType.TEXT, params);

		assertEquals(Arrays.asList("parseText"), result.getWarnings());
		assertTrue(params == result.getValue().getParameters());
	}

	@Test
	public void writeXml() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		TestProperty prop = new TestProperty("value");
		assertWriteXml("<text>value</text>", prop, m);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void parseXml() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		parseXCalProperty("<text>text</text>", m);
	}

	@Test
	public void writeJson() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		TestProperty prop = new TestProperty("value");

		JCalValue actual = m.writeJson(prop);
		assertEquals("value", actual.getSingleValued());
	}

	@Test
	public void parseJson_single() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		Result<TestProperty> result = m.parseJson(JCalValue.single("value"), ICalDataType.TEXT, new ICalParameters());

		TestProperty prop = result.getValue();
		assertEquals("value", prop.getValue());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_list() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		Result<TestProperty> result = m.parseJson(JCalValue.multi("value1", "val,;ue2"), ICalDataType.TEXT, new ICalParameters());

		TestProperty prop = result.getValue();
		assertEquals("value1,val\\,\\;ue2", prop.getValue());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_structured() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		Result<TestProperty> result = m.parseJson(JCalValue.structured("value1", "val,;ue2"), ICalDataType.TEXT, new ICalParameters());

		TestProperty prop = result.getValue();
		assertEquals("value1;val\\,\\;ue2", prop.getValue());
		assertWarnings(1, result.getWarnings());
	}

	@Test
	public void parseJson_object() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		ListMultimap<String, Object> map = new ListMultimap<String, Object>();
		map.put("a", "one");
		map.put("b", "two");
		map.put("b", "three");
		Result<TestProperty> result = m.parseJson(JCalValue.object(map), ICalDataType.TEXT, new ICalParameters());

		TestProperty prop = result.getValue();
		assertEquals("a=one;b=two,three", prop.getValue());
		assertWarnings(1, result.getWarnings());
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
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<String> warnings) {
			warnings.add("parseText");
			return new TestProperty(value);
		}
	}

	private class TestProperty extends ICalProperty {
		private String value;

		public TestProperty(String value) {
			this.value = value;
		}

		private String getValue() {
			return value;
		}
	}
}
