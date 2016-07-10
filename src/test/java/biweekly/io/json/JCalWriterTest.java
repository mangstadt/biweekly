package biweekly.io.json;

import static biweekly.ICalVersion.V2_0;
import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.TimeZone;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.StandardTime;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.SkipMeScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.CalendarScale;
import biweekly.property.DateStart;
import biweekly.property.ICalProperty;
import biweekly.property.RecurrenceDates;
import biweekly.property.SkipMeProperty;
import biweekly.property.Summary;
import biweekly.property.Version;
import biweekly.util.DateTimeComponents;
import biweekly.util.Duration;
import biweekly.util.IOUtils;
import biweekly.util.Period;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;
import biweekly.util.UtcOffset;

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
@SuppressWarnings("resource")
public class JCalWriterTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

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
				"[\"version\",{},\"text\",\"2.0\"]" +
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
		ical.getProperties().clear();
		ical.addProperty(new SkipMeProperty());
		ical.addExperimentalProperty("X-FOO", "bar");

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.registerScribe(new SkipMeScribe());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"x-foo\",{},\"unknown\",\"bar\"]" +
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
		ical.addProperty(new Version("2.0"));
		ical.setProductId("prodid");

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.registerScribe(new MyVersionMarshaller());
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
		writer.registerScribe(new TestPropertyMarshaller());
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
		writer.registerScribe(new PartyMarshaller());
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
	public void prettyPrint() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");

		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setSummary("summary");
		ical.addEvent(event);

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw);
		writer.setPrettyPrint(true);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[" + NEWLINE +
		"  \"vcalendar\"," + NEWLINE +
		"  [" + NEWLINE +
		"    [ \"version\", { }, \"text\", \"2.0\" ]," + NEWLINE +
		"    [ \"prodid\", { }, \"text\", \"prodid\" ]" + NEWLINE +
		"  ]," + NEWLINE +
		"  [" + NEWLINE +
		"    [" + NEWLINE +
		"      \"vevent\"," + NEWLINE +
		"      [" + NEWLINE +
		"        [ \"summary\", { }, \"text\", \"summary\" ]" + NEWLINE +
		"      ]," + NEWLINE +
		"      [ ]" + NEWLINE +
		"    ]" + NEWLINE +
		"  ]" + NEWLINE +
		"]";
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
				"[\"version\",{},\"text\",\"2.0\"]," +
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
	public void setGlobalTimezone() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setDateStart(utc("1996-07-04 12:00:00"));
		ical.addEvent(event);

		TimeZone nyTimezone = TimeZone.getTimeZone("America/New_York");
		VTimezone nyComponent = new VTimezone(nyTimezone.getID());
		ical.getTimezoneInfo().setDefaultTimezone(new TimezoneAssignment(nyTimezone, nyComponent));

		TimeZone laTimezone = TimeZone.getTimeZone("America/Los_Angeles");
		VTimezone laComponent = new VTimezone(laTimezone.getID());

		StringWriter sw = new StringWriter();
		JCalWriter writer = new JCalWriter(sw, true);
		writer.write(ical);
		writer.setGlobalTimezone(new TimezoneAssignment(laTimezone, laComponent));
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"[[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]" +
			"]," +
			"[" +
				"[\"vtimezone\"," +
					"[" +
						"[\"tzid\",{},\"text\",\"America/New_York\"]" +
					"]," +
					"[]" +
				"]," +
				"[\"vevent\"," +
					"[" +
						"[\"dtstart\",{\"tzid\":\"America/New_York\"},\"date-time\",\"1996-07-04T08:00:00\"]" +
					"]," +
					"[]" +
				"]" +
			"]" +
		"]," +
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]" +
			"]," +
			"[" +
				"[\"vtimezone\"," +
					"[" +
						"[\"tzid\",{},\"text\",\"America/Los_Angeles\"]" +
					"]," +
					"[]" +
				"]," +
				"[\"vevent\"," +
					"[" +
						"[\"dtstart\",{\"tzid\":\"America/Los_Angeles\"},\"date-time\",\"1996-07-04T05:00:00\"]" +
					"]," +
					"[]" +
				"]" +
			"]" +
		"]]";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void jcal_draft_example1() throws Throwable {
		//Note: all whitespace is removed from the expected JSON string it easier to compare it with the actual result
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setCalendarScale(CalendarScale.gregorian());
		ical.setProductId("-//ExampleInc.//ExampleCalendar//EN");
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.setDateTimeStamp(utc("2008-02-05 19:12:24"));
			event.setDateStart(new DateStart(date("2008-10-06"), false));
			event.setSummary("Planningmeeting");
			event.setUid("4088E990AD89CB3DBB484909");
			ical.addEvent(event);
		}

		assertValidate(ical).versions(V2_0).run();
		assertExample(ical, "rfc7265-example1.json");
	}

	@Test
	public void jcal_draft_example2() throws Throwable {
		TimeZone eastern = TimeZone.getTimeZone("US/Eastern");

		//Note: all whitespace is removed from the expected JSON string it easier to compare it with the actual result
		VTimezone usEasternTz;
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//ExampleCorp.//ExampleClient//EN");
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utc("2006-02-06 00:11:21"));
			event.setDateStart(date("2006-01-02 12:00:00", eastern));
			event.setDuration(Duration.builder().hours(1).build());

			Recurrence rrule = new Recurrence.Builder(Frequency.DAILY).count(5).build();
			event.setRecurrenceRule(rrule);

			RecurrenceDates rdate = new RecurrenceDates();
			rdate.getPeriods().add(new Period(date("2006-01-02 15:00:00", eastern), Duration.builder().hours(2).build()));
			event.addRecurrenceDates(rdate);

			event.setSummary("Event#2");
			event.setDescription("Wearehavingameetingallthisweekat12pmforonehour,withanadditionalmeetingonthefirstday2hourslong.\nPleasebringyourownlunchforthe12pmmeetings.");
			event.setUid("00959BC664CA650E933C892C@example.com");
			ical.addEvent(event);
		}
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utc("2006-02-06 00:11:21"));
			event.setDateStart(date("2006-01-02 14:00:00", eastern));
			event.setDuration(Duration.builder().hours(1).build());

			event.setRecurrenceId(date("2006-01-04 12:00:00", eastern));

			event.setSummary("Event#2");
			event.setUid("00959BC664CA650E933C892C@example.com");
			ical.addEvent(event);
		}

		assertValidate(ical).versions(V2_0).run();

		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		{
			usEasternTz = new VTimezone("US/Eastern");
			usEasternTz.setLastModified(utc("2004-01-10 03:28:45"));
			{
				DaylightSavingsTime daylight = new DaylightSavingsTime();
				daylight.setDateStart(new DateTimeComponents(2000, 4, 4, 2, 0, 0, false));

				Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY).byDay(1, DayOfWeek.SUNDAY).byMonth(4).build();
				daylight.setRecurrenceRule(rrule);

				daylight.addTimezoneName("EDT");
				daylight.setTimezoneOffsetFrom(new UtcOffset(false, 5, 0));
				daylight.setTimezoneOffsetTo(new UtcOffset(false, 4, 0));

				usEasternTz.addDaylightSavingsTime(daylight);
			}
			{
				StandardTime standard = new StandardTime();
				standard.setDateStart(new DateTimeComponents(2000, 10, 26, 2, 0, 0, false));

				Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY).byDay(1, DayOfWeek.SUNDAY).byMonth(10).build();
				standard.setRecurrenceRule(rrule);

				standard.addTimezoneName("EST");
				standard.setTimezoneOffsetFrom(new UtcOffset(false, 4, 0));
				standard.setTimezoneOffsetTo(new UtcOffset(false, 5, 0));

				usEasternTz.addStandardTime(standard);
			}
		}
		tzinfo.setDefaultTimezone(new TimezoneAssignment(eastern, usEasternTz));
		assertExample(ical, "rfc7265-example2.json");
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
		protected String _writeText(TestProperty property, WriteContext context) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return new TestProperty(value);
		}

		@Override
		protected JCalValue _writeJson(TestProperty property, WriteContext context) {
			return JCalValue.single(_writeText(property, null));
		}
	}

	private class MyVersionMarshaller extends ICalPropertyScribe<Version> {
		public MyVersionMarshaller() {
			super(Version.class, "VERSION", ICalDataType.TEXT);
		}

		@Override
		protected String _writeText(Version property, WriteContext context) {
			return property.getMaxVersion() + " (beta)";
		}

		@Override
		protected Version _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return new Version(value);
		}

		@Override
		protected JCalValue _writeJson(Version property, WriteContext context) {
			return JCalValue.single(_writeText(property, null));
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
