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
	public void splitBy() {
		String[] actual, expected;

		actual = ICalPropertyMarshaller.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', false, false);
		expected = new String[] { "Doe", "John", "Joh\\,\\;nny", "", "Sr.,III" };
		assertArrayEquals(expected, actual);

		actual = ICalPropertyMarshaller.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', true, false);
		expected = new String[] { "Doe", "John", "Joh\\,\\;nny", "Sr.,III" };
		assertArrayEquals(expected, actual);

		actual = ICalPropertyMarshaller.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', false, true);
		expected = new String[] { "Doe", "John", "Joh,;nny", "", "Sr.,III" };
		assertArrayEquals(expected, actual);

		actual = ICalPropertyMarshaller.splitBy("Doe;John;Joh\\,\\;nny;;Sr.,III", ';', true, true);
		expected = new String[] { "Doe", "John", "Joh,;nny", "Sr.,III" };
		assertArrayEquals(expected, actual);
	}

	@Test
	public void parseDate_timezone() {
		String value = "20130611T134302Z";
		List<String> warnings = new ArrayList<String>();

		Date actual = ICalPropertyMarshaller.parseDate(value, null, warnings);

		assertEquals(datetime, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void parseDate_local() {
		String value = "20130611T144302";
		List<String> warnings = new ArrayList<String>();

		Date actual = ICalPropertyMarshaller.parseDate(value, null, warnings);

		assertEquals(datetime, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void parseDate_tzid() {
		String value = "20130611T144302";
		List<String> warnings = new ArrayList<String>();

		Date actual = ICalPropertyMarshaller.parseDate(value, "some ID", warnings);

		assertEquals(datetime, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void parseDate_global_tzid() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		int hour = 13 + (timezone.getOffset(System.currentTimeMillis()) / (1000 * 60 * 60)); //it might be daylight savings today
		String value = "20130611T" + hour + "4302";
		List<String> warnings = new ArrayList<String>();

		Date actual = ICalPropertyMarshaller.parseDate(value, timezone.getID(), warnings);

		assertEquals(datetime, actual);
		assertWarnings(0, warnings);
	}

	@Test
	public void parseDate_invalid_tzid() {
		String value = "20130611T144302";
		List<String> warnings = new ArrayList<String>();

		Date actual = ICalPropertyMarshaller.parseDate(value, "invalid/timezone", warnings);

		//parse as local time and add warning
		assertEquals(datetime, actual);
		assertWarnings(1, warnings);
	}

	@Test
	public void writeDate_datetime() {
		String expected = "20130611T134302Z";
		String actual = ICalPropertyMarshaller.writeDate(datetime, true, null);
		assertEquals(expected, actual);
	}

	@Test
	public void writeDate_date() {
		String expected = "20130611";
		String actual = ICalPropertyMarshaller.writeDate(datetime, false, null);
		assertEquals(expected, actual);
	}

	@Test
	public void writeDate_datetime_global_tzid() {
		TimeZone timezone = TimeZone.getTimeZone("Africa/Johannesburg"); //+02:00
		int hour = 13 + (timezone.getOffset(System.currentTimeMillis()) / (1000 * 60 * 60)); //it might be daylight savings today
		String expected = "20130611T" + hour + "4302";
		String actual = ICalPropertyMarshaller.writeDate(datetime, true, timezone.getID());
		assertEquals(expected, actual);
	}

	@Test
	public void writeDate_datetime_invalid_global_tzid() {
		String expected = "20130611T134302Z";
		String actual = ICalPropertyMarshaller.writeDate(datetime, true, "invalid/timezone");
		assertEquals(expected, actual);
	}

	@Test
	public void writeDate_datetime_tzid() {
		String expected = "20130611T144302";
		String actual = ICalPropertyMarshaller.writeDate(datetime, true, "some ID");
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
