package biweekly.io.json;

import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.TimeZone;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.Warning;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.StandardTime;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.io.SkipMeException;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.CalendarScale;
import biweekly.property.DateStart;
import biweekly.property.ICalProperty;
import biweekly.property.RecurrenceDates;
import biweekly.property.Summary;
import biweekly.property.Version;
import biweekly.util.DateTimeComponents;
import biweekly.util.Duration;
import biweekly.util.IOUtils;
import biweekly.util.Period;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;

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
@SuppressWarnings("resource")
public class JCalWriterTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private final DateFormat utcFormatter;
	{
		utcFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	private final DateFormat usEasternFormatter;
	{
		usEasternFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		usEasternFormatter.setTimeZone(TimeZone.getTimeZone("US/Eastern"));
	}
	private final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	public void basic() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");

		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setSummary("summary");
		ical.addEvent(event);

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"prodid\",{},\"text\",\"prodid\"]" +
			"]," +
			"[" +
				"[\"vevent\"," +
					"[" +
						"[\"summary\",{},\"text\",\"summary\"]" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void write_multiple() throws Throwable {
		ICalendar ical1 = new ICalendar();
		ical1.setProductId("prodid1");

		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setSummary("summary1");
		ical1.addEvent(event);

		ICalendar ical2 = new ICalendar();
		ical2.setProductId("prodid2");

		event = new VEvent();
		event.getProperties().clear();
		event.setSummary("summary2");
		ical2.addEvent(event);

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw, true);
		writer.write(ical1);
		writer.write(ical2);
		writer.close();

		//@formatter:off
		String expected =
		"[" +
			"[\"vcalendar\"," +
				"[" +
					"[\"version\",{},\"text\",\"2.0\"]," +
					"[\"prodid\",{},\"text\",\"prodid1\"]" +
				"]," +
				"[" +
					"[\"vevent\"," +
						"[" +
							"[\"summary\",{},\"text\",\"summary1\"]" +
						"]," +
						"[" +
						"]" +
					"]" +
				"]" +
			"]," +
			"[\"vcalendar\"," +
				"[" +
					"[\"version\",{},\"text\",\"2.0\"]," +
					"[\"prodid\",{},\"text\",\"prodid2\"]" +
				"]," +
				"[" +
					"[\"vevent\"," +
						"[" +
							"[\"summary\",{},\"text\",\"summary2\"]" +
						"]," +
						"[" +
						"]" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void write_multiple_components() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");

		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setSummary("summary1");
		ical.addEvent(event);

		event = new VEvent();
		event.getProperties().clear();
		event.setSummary("summary2");
		ical.addEvent(event);

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"prodid\",{},\"text\",\"prodid\"]" +
			"]," +
			"[" +
				"[\"vevent\"," +
					"[" +
						"[\"summary\",{},\"text\",\"summary1\"]" +
					"]," +
					"[" +
					"]" +
				"]," +
				"[\"vevent\"," +
					"[" +
						"[\"summary\",{},\"text\",\"summary2\"]" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void write_empty() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void no_property_marshaller() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");
		ical.addProperty(new TestProperty("value"));

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.write(ical);
	}

	@Test(expected = IllegalArgumentException.class)
	public void no_component_marshaller() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");
		ical.addComponent(new Party());

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.write(ical);
	}

	@Test
	public void skipMeException() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");
		ical.addProperty(new TestProperty("value"));

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.registerMarshaller(new SkipMeMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"prodid\",{},\"text\",\"prodid\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void override_marshaller() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.registerMarshaller(new MyVersionMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0 (beta)\"]," +
				"[\"prodid\",{},\"text\",\"prodid\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void experimental_property() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");
		ical.addExperimentalProperty("X-NUMBER", "1");
		ical.addExperimentalProperty("X-NUMBER", ICalDataType.INTEGER, "2");

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"prodid\",{},\"text\",\"prodid\"]," +
				"[\"x-number\",{},\"unknown\",\"1\"]," +
				"[\"x-number\",{},\"integer\",\"2\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void experimental_property_marshaller() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");
		ical.addProperty(new TestProperty("one"));
		ical.addProperty(new TestProperty("two"));

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.registerMarshaller(new TestPropertyMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"prodid\",{},\"text\",\"prodid\"]," +
				"[\"x-test\",{},\"text\",\"one\"]," +
				"[\"x-test\",{},\"text\",\"two\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void experimental_component() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");
		ical.addExperimentalComponent("X-VPARTY");

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"prodid\",{},\"text\",\"prodid\"]" +
			"]," +
			"[" +
				"[\"x-vparty\"," +
					"[" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void experimental_component_marshaller() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");
		ical.addComponent(new Party());

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.registerMarshaller(new PartyMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"prodid\",{},\"text\",\"prodid\"]" +
			"]," +
			"[" +
				"[\"x-vparty\"," +
					"[" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void indent() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");

		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setSummary("summary");
		ical.addEvent(event);

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.setIndent(true);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[" + NEWLINE +
		"\"vcalendar\",[[" + NEWLINE +
		"  \"version\",{},\"text\",\"2.0\"],[" + NEWLINE +
		"  \"prodid\",{},\"text\",\"prodid\"]],[[" + NEWLINE +
		"  \"vevent\",[[" + NEWLINE +
		"    \"summary\",{},\"text\",\"summary\"]],[]]]]";
		//@formatter:on
		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void utf8() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new Summary("\u1e66ummary"));

		File file = tempFolder.newFile();
		JCalWriter writer = new JCalWriter(file);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"summary\",{},\"text\",\"\u1e66ummary\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		String actual = IOUtils.getFileContents(file, "UTF-8");
		assertEquals(expected, actual);
	}

	@Test
	public void jcal_draft_example1() throws Throwable {
		//see: http://tools.ietf.org/html/draft-ietf-jcardcal-jcal-05#page-25
		//Note: all whitespace is removed from the expected JSON string it easier to compare it with the actual result
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setCalendarScale(CalendarScale.gregorian());
		ical.setProductId("-//ExampleInc.//ExampleCalendar//EN");
		ical.setVersion(Version.v2_0());
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.setDateTimeStamp(utcFormatter.parse("2008-02-05T19:12:24"));
			event.setDateStart(new DateStart(dateFormatter.parse("2008-10-06"), false));
			event.setSummary("Planningmeeting");
			event.setUid("4088E990AD89CB3DBB484909");
			ical.addEvent(event);
		}

		assertValidate(ical.validate());
		assertExample(ical, "jcal-draft-example1.json");
	}

	@Test
	public void jcal_draft_example2() throws Throwable {
		//see: http://tools.ietf.org/html/draft-ietf-jcardcal-jcal-05#page-27
		//Note: all whitespace is removed from the expected JSON string it easier to compare it with the actual result
		VTimezone usEasternTz;
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//ExampleCorp.//ExampleClient//EN");
		ical.setVersion(Version.v2_0());
		{
			usEasternTz = new VTimezone(null);
			usEasternTz.setLastModified(utcFormatter.parse("2004-01-10T03:28:45"));
			usEasternTz.setTimezoneId("US/Eastern");
			{
				DaylightSavingsTime daylight = new DaylightSavingsTime();
				daylight.setDateStart(new DateTimeComponents(2000, 4, 4, 2, 0, 0, false));

				Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY).byDay(1, DayOfWeek.SUNDAY).byMonth(4).build();
				daylight.setRecurrenceRule(rrule);

				daylight.addTimezoneName("EDT");
				daylight.setTimezoneOffsetFrom(-5, 0);
				daylight.setTimezoneOffsetTo(-4, 0);

				usEasternTz.addDaylightSavingsTime(daylight);
			}
			{
				StandardTime standard = new StandardTime();
				standard.setDateStart(new DateTimeComponents(2000, 10, 26, 2, 0, 0, false));

				Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY).byDay(1, DayOfWeek.SUNDAY).byMonth(10).build();
				standard.setRecurrenceRule(rrule);

				standard.addTimezoneName("EST");
				standard.setTimezoneOffsetFrom(-4, 0);
				standard.setTimezoneOffsetTo(-5, 0);

				usEasternTz.addStandardTime(standard);
			}
			ical.addTimezone(usEasternTz);
		}
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utcFormatter.parse("2006-02-06T00:11:21"));
			event.setDateStart(usEasternFormatter.parse("2006-01-02T12:00:00")).setTimezone(usEasternTz);
			event.setDuration(Duration.builder().hours(1).build());

			Recurrence rrule = new Recurrence.Builder(Frequency.DAILY).count(5).build();
			event.setRecurrenceRule(rrule);

			RecurrenceDates rdate = new RecurrenceDates(Arrays.asList(new Period(usEasternFormatter.parse("2006-01-02T15:00:00"), Duration.builder().hours(2).build())));
			rdate.setTimezone(usEasternTz);
			event.addRecurrenceDates(rdate);

			event.setSummary("Event#2");
			event.setDescription("Wearehavingameetingallthisweekat12pmforonehour,withanadditionalmeetingonthefirstday2hourslong.\nPleasebringyourownlunchforthe12pmmeetings.");
			event.setUid("00959BC664CA650E933C892C@example.com");
			ical.addEvent(event);
		}
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utcFormatter.parse("2006-02-06T00:11:21"));
			event.setDateStart(usEasternFormatter.parse("2006-01-02T14:00:00")).setTimezone(usEasternTz);
			event.setDuration(Duration.builder().hours(1).build());

			event.setRecurrenceId(usEasternFormatter.parse("2006-01-04T12:00:00")).setTimezone(usEasternTz);

			event.setSummary("Event#2");
			event.setUid("00959BC664CA650E933C892C@example.com");
			ical.addEvent(event);
		}

		assertValidate(ical.validate());
		assertExample(ical, "jcal-draft-example2.json");
	}

	private void assertExample(ICalendar ical, String exampleFileName) throws IOException {
		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.write(ical);
		writer.close();

		String expected = new String(IOUtils.toByteArray(getClass().getResourceAsStream(exampleFileName)));
		expected = expected.replaceAll("\\s", "");

		String actual = sw.toString();

		assertEquals(expected, actual);
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

	private class TestPropertyMarshaller extends ICalPropertyScribe<TestProperty> {
		public TestPropertyMarshaller() {
			super(TestProperty.class, "X-TEST", ICalDataType.TEXT);
		}

		@Override
		protected String _writeText(TestProperty property) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
			return new TestProperty(value);
		}

		@Override
		protected JCalValue _writeJson(TestProperty property) {
			return JCalValue.single(_writeText(property));
		}
	}

	private class SkipMeMarshaller extends ICalPropertyScribe<TestProperty> {
		public SkipMeMarshaller() {
			super(TestProperty.class, "NAME", null);
		}

		@Override
		protected String _writeText(TestProperty property) {
			throw new SkipMeException("Skipped");
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
			return new TestProperty(value);
		}
	}

	private class MyVersionMarshaller extends ICalPropertyScribe<Version> {
		public MyVersionMarshaller() {
			super(Version.class, "VERSION", ICalDataType.TEXT);
		}

		@Override
		protected String _writeText(Version property) {
			return property.getMaxVersion() + " (beta)";
		}

		@Override
		protected Version _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
			return new Version(value);
		}

		@Override
		protected JCalValue _writeJson(Version property) {
			return JCalValue.single(_writeText(property));
		}
	}

	private class PartyMarshaller extends ICalComponentScribe<Party> {
		public PartyMarshaller() {
			super(Party.class, "X-VPARTY");
		}

		@Override
		protected Party _newInstance() {
			return new Party();
		}
	}

	private class Party extends ICalComponent {
		//empty
	}
}
