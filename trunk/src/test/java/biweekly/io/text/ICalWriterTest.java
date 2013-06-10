package biweekly.io.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringWriter;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.Test;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VEvent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.io.SkipMeException;
import biweekly.io.text.ICalWriter;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.Version;
import biweekly.property.marshaller.ICalPropertyMarshaller;


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
public class ICalWriterTest {
	public static void main(String args[]) {
		String regex = "BEGIN:VCALENDAR\r\nVERSION:2\\.0\r\nBEGIN:VEVENT\r\nSUMMARY:summary\r\nEND:VEVENT\r\nEND:VCALENDAR";
		String str = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nBEGIN:VEVENT\r\nSUMMARY:summary\r\nEND:VEVENT\r\nEND:VCALENDAR";

		Pattern p = Pattern.compile(regex, Pattern.DOTALL);

		System.out.println(p.matcher(str).matches());
	}

	@Test
	public void basic() throws Exception {
		ICalendar ical = new ICalendar();

		VEvent event = new VEvent();
		event.setSummary("summary");
		ical.addEvent(event);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
			"BEGIN:VEVENT\r\n" +
				"UID:.*?\r\n" +
				"DTSTAMP:.*?\r\n" +
				"SUMMARY:summary\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(0, writer.getWarnings());
	}

	@Test
	public void escape_newlines() throws Exception {
		ICalendar ical = new ICalendar();

		VEvent event = new VEvent();
		event.setSummary("summary\nof event");
		ical.addEvent(event);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
			"BEGIN:VEVENT\r\n" +
				"UID:.*?\r\n" +
				"DTSTAMP:.*?\r\n" +
				"SUMMARY:summary\\\\nof event\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(0, writer.getWarnings());
	}

	@Test
	public void bad_parameter_value_chars() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProductId().addParameter("X-TEST", "\"test\"");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID;X-TEST='test':.*?\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(1, writer.getWarnings());
	}

	@Test
	public void caret_encoding() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProductId().addParameter("X-TEST", "\"test\"");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.setCaretEncodingEnabled(true);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID;X-TEST=\\^'test\\^':.*?\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(0, writer.getWarnings());
	}

	@Test
	public void multiple() throws Exception {
		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.write(new ICalendar());
		writer.write(new ICalendar());
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
	}

	@Test
	public void no_property_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty("value"));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(1, writer.getWarnings());
	}

	@Test
	public void no_component_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addComponent(new Party());

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(1, writer.getWarnings());
	}

	@Test
	public void bad_property_name() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty("value"));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.registerMarshaller(new BadNameMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(1, writer.getWarnings());
	}

	@Test
	public void skipMeException() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty("value"));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.registerMarshaller(new SkipMeMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(1, writer.getWarnings());
	}

	@Test
	public void override_marshaller() throws Exception {
		ICalendar ical = new ICalendar();

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.registerMarshaller(new MyVersionMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0 \\(beta\\)\r\n" +
			"PRODID:.*?\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(0, writer.getWarnings());
	}

	@Test
	public void experimental_property() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addExperimentalProperty("X-NUMBER", "1");
		ical.addExperimentalProperty("X-NUMBER", "2");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.registerMarshaller(new TestPropertyMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
			"X-NUMBER:1\r\n" +
			"X-NUMBER:2\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(0, writer.getWarnings());
	}

	@Test
	public void experimental_property_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty("one"));
		ical.addProperty(new TestProperty("two"));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.registerMarshaller(new TestPropertyMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
			"X-TEST:one\r\n" +
			"X-TEST:two\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(0, writer.getWarnings());
	}

	@Test
	public void experimental_component() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addExperimentalComponents("X-VPARTY");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
			"BEGIN:X-VPARTY\r\n" +
			"END:X-VPARTY\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(0, writer.getWarnings());
	}

	@Test
	public void experimental_component_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addComponent(new Party());

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw);
		writer.registerMarshaller(new PartyMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
			"BEGIN:X-VPARTY\r\n" +
			"END:X-VPARTY\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
		assertWarnings(0, writer.getWarnings());
	}

	private class TestProperty extends ICalProperty {
		private String value;

		public TestProperty(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
	}

	private class TestPropertyMarshaller extends ICalPropertyMarshaller<TestProperty> {
		public TestPropertyMarshaller() {
			super(TestProperty.class, "X-TEST");
		}

		@Override
		protected String _writeText(TestProperty property, List<String> warnings) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalParameters parameters, List<String> warnings) {
			return new TestProperty(value);
		}
	}

	private class BadNameMarshaller extends ICalPropertyMarshaller<TestProperty> {
		public BadNameMarshaller() {
			super(TestProperty.class, "BAD*NAME");
		}

		@Override
		protected String _writeText(TestProperty property, List<String> warnings) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalParameters parameters, List<String> warnings) {
			return new TestProperty(value);
		}
	}

	private class SkipMeMarshaller extends ICalPropertyMarshaller<TestProperty> {
		public SkipMeMarshaller() {
			super(TestProperty.class, "NAME");
		}

		@Override
		protected String _writeText(TestProperty property, List<String> warnings) {
			throw new SkipMeException("Skipped");
		}

		@Override
		protected TestProperty _parseText(String value, ICalParameters parameters, List<String> warnings) {
			return new TestProperty(value);
		}
	}

	private class MyVersionMarshaller extends ICalPropertyMarshaller<Version> {
		public MyVersionMarshaller() {
			super(Version.class, "VERSION");
		}

		@Override
		protected String _writeText(Version property, List<String> warnings) {
			return property.getMaxVersion() + " (beta)";
		}

		@Override
		protected Version _parseText(String value, ICalParameters parameters, List<String> warnings) {
			return new Version(value);
		}
	}

	private class PartyMarshaller extends ICalComponentMarshaller<Party> {
		public PartyMarshaller() {
			super(Party.class, "X-VPARTY");
		}

		@Override
		public Party newInstance() {
			return new Party();
		}
	}

	private class Party extends ICalComponent {
		//empty
	}

	private static void assertRegex(String regex, String string) {
		Pattern p = Pattern.compile(regex);
		assertTrue(string, p.matcher(string).matches());
	}

	private static void assertWarnings(int expectedSize, List<String> warnings) {
		assertEquals(warnings.toString(), expectedSize, warnings.size());
	}
}
