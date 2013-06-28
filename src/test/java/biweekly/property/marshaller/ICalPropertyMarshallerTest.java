package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.buildTimezone;
import static org.junit.Assert.assertArrayEquals;
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

import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;

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
	private final String NEWLINE = System.getProperty("line.separator");

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
		String[] actual, expected;

		actual = ICalPropertyMarshaller.split("Doe;John;Joh\\,\\;nny;;Sr.,III", ";").split();
		expected = new String[] { "Doe", "John", "Joh\\,\\;nny", "", "Sr.,III" };
		assertArrayEquals(expected, actual);

		actual = ICalPropertyMarshaller.split("Doe;John;Joh\\,\\;nny;;Sr.,III", ";").removeEmpties(true).split();
		expected = new String[] { "Doe", "John", "Joh\\,\\;nny", "Sr.,III" };
		assertArrayEquals(expected, actual);

		actual = ICalPropertyMarshaller.split("Doe;John;Joh\\,\\;nny;;Sr.,III", ";").unescape(true).split();
		expected = new String[] { "Doe", "John", "Joh,;nny", "", "Sr.,III" };
		assertArrayEquals(expected, actual);

		actual = ICalPropertyMarshaller.split("Doe;John;Joh\\,\\;nny;;Sr.,III", ";").removeEmpties(true).unescape(true).split();
		expected = new String[] { "Doe", "John", "Joh,;nny", "Sr.,III" };
		assertArrayEquals(expected, actual);
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
	public void DateParser_invalid_tzid() {
		String value = "20130611T144302";
		List<String> warnings = new ArrayList<String>();

		Date actual = ICalPropertyMarshaller.date(value).tzid("invalid/timezone", warnings).parse();

		//parse as local time and add warning
		assertEquals(datetime, actual);
		assertWarnings(1, warnings);
	}

	@Test
	public void DateWriter_datetime() {
		String expected = "20130611T134302Z";
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
	public void DateWriter_datetime_global_tzid_exnteded() {
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
	public void parseList() {
		String[] actual = ICalPropertyMarshaller.parseList("one , two,three\\,four");
		String[] expected = new String[] { "one", "two", "three,four" };
		assertArrayEquals(expected, actual);
	}

	@Test
	public void parseComponents() {
		String[][] actual = ICalPropertyMarshaller.parseComponent("one ; two,three\\,four;; ;five\\;six");
		//@formatter:off
		String[][] expected = new String[][] {
			new String[]{"one"},
			new String[]{"two", "three,four"},
			new String[]{},
			new String[]{},
			new String[]{"five;six"}
		};
		//@formatter:on
		assertArrayEquals(expected, actual);
	}

	@Test
	public void prepareParameters() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		TestProperty property = new TestProperty();
		ICalParameters copy = m.prepareParameters(property);

		assertFalse(property.getParameters() == copy);
		assertEquals("value", copy.first("PARAM"));
	}

	@Test
	public void writeText() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		TestProperty property = new TestProperty();
		String value = m.writeText(property);

		assertEquals("value", value);
	}

	@Test
	public void parseText() {
		ICalPropertyMarshallerImpl m = new ICalPropertyMarshallerImpl();
		ICalParameters params = new ICalParameters();
		params.put("PARAM", "value");
		ICalPropertyMarshaller.Result<TestProperty> result = m.parseText("value", params);

		assertEquals(Arrays.asList("parseText"), result.getWarnings());
		assertTrue(params == result.getValue().getParameters());
	}

	private class ICalPropertyMarshallerImpl extends ICalPropertyMarshaller<TestProperty> {
		ICalPropertyMarshallerImpl() {
			super(TestProperty.class, "TEST");
		}

		@Override
		protected void _prepareParameters(TestProperty property, ICalParameters copy) {
			copy.put("PARAM", "value");
		}

		@Override
		protected String _writeText(TestProperty property) {
			return "value";
		}

		@Override
		protected TestProperty _parseText(String value, ICalParameters parameters, List<String> warnings) {
			warnings.add("parseText");
			return new TestProperty();
		}
	}

	private class TestProperty extends ICalProperty {
		//empty
	}
}
