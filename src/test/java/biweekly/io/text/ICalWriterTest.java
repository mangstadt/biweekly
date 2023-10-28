package biweekly.io.text;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertRegex;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.each;
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
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.StandardTime;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.component.VFreeBusy;
import biweekly.component.VJournal;
import biweekly.component.VTimezone;
import biweekly.component.VTodo;
import biweekly.io.ICalTimeZone;
import biweekly.io.ParseContext;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.SkipMeScribe;
import biweekly.parameter.CalendarUserType;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.ParticipationLevel;
import biweekly.parameter.ParticipationStatus;
import biweekly.property.Attachment;
import biweekly.property.Attendee;
import biweekly.property.Classification;
import biweekly.property.Created;
import biweekly.property.DateStart;
import biweekly.property.FreeBusy;
import biweekly.property.ICalProperty;
import biweekly.property.Organizer;
import biweekly.property.ProductId;
import biweekly.property.SkipMeProperty;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Trigger;
import biweekly.util.DateTimeComponents;
import biweekly.util.DayOfWeek;
import biweekly.util.Duration;
import biweekly.util.Frequency;
import biweekly.util.Gobble;
import biweekly.util.Period;
import biweekly.util.Recurrence;
import biweekly.util.UtcOffset;

/*
 Copyright (c) 2013-2023, Michael Angstadt
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
public class ICalWriterTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void basic() throws Exception {
		ICalendar ical = new ICalendar();

		VEvent event = new VEvent();
		event.setSummary("summary");
		ical.addEvent(event);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
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
	}

	@Test
	public void escape_newlines() throws Exception {
		ICalendar ical = new ICalendar();

		VEvent event = new VEvent();
		event.setSummary("summary\nof event");
		ical.addEvent(event);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
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
	}

	@Test(expected = IllegalArgumentException.class)
	public void bad_parameter_value_chars() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProductId().addParameter("X-TEST", "\"test\"");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.write(ical);
	}

	@Test
	public void caret_encoding() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProductId().addParameter("X-TEST", "\"test\"");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
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
	}

	@Test
	public void multiple() throws Exception {
		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
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

	@Test(expected = IllegalArgumentException.class)
	public void no_property_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty("value"));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.write(ical);
	}

	@Test(expected = IllegalArgumentException.class)
	public void no_component_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addComponent(new Party());

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.write(ical);
	}

	@Test(expected = IllegalArgumentException.class)
	public void bad_property_name() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty("value"));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.registerScribe(new BadNameMarshaller());
		writer.write(ical);
	}

	@Test
	public void skipMeException() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new SkipMeProperty());
		ical.addExperimentalProperty("X-FOO", "bar");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.registerScribe(new SkipMeScribe());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"X-FOO:bar\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void override_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.setProductId("prod id");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.registerScribe(new MyProdIdScribe());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"PRODID:PROD ID\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void experimental_property() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addExperimentalProperty("X-NUMBER", "1");
		ical.addExperimentalProperty("X-NUMBER", "2");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
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
	}

	@Test
	public void experimental_property_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty("one"));
		ical.addProperty(new TestProperty("two"));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.registerScribe(new TestPropertyMarshaller());
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
	}

	@Test
	public void experimental_component() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addExperimentalComponent("X-VPARTY");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
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
	}

	@Test
	public void experimental_component_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addComponent(new Party());

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.registerScribe(new PartyMarshaller());
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
	}

	@Test
	public void data_types() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty("one"));
		ical.addProperty(new TestProperty("2"));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.registerScribe(new DataTypePropertyMarshaller());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2\\.0\r\n" +
			"PRODID:.*?\r\n" +
			"X-TEST:one\r\n" +
			"X-TEST;VALUE=INTEGER:2\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertRegex(expected, actual);
	}

	@Test
	public void utf8() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new Summary("\u1e66ummary"));

		File file = tempFolder.newFile();
		ICalWriter writer = new ICalWriter(file, V2_0);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"SUMMARY:\u1e66ummary\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = new Gobble(file).asString("UTF-8");
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_timezone_no_dates() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V1_0);
		ical.setTimezoneInfo(americaNewYork());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_timezone_one_daylight_one_date() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new DateStart(utc(2014, 10, 7, 9, 34, 0)));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V1_0);
		ical.setTimezoneInfo(americaNewYork());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DTSTART:20141007T053400\r\n" +
			"TZ:-0500\r\n" +
			"DAYLIGHT:TRUE;-0400;20140309T020000;20141102T020000;EST;EDT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_timezone_one_daylight_two_dates() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new DateStart(utc(2014, 10, 7, 9, 34, 0)));
		ical.addProperty(new DateStart(utc(2014, 10, 8, 9, 34, 0)));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V1_0);
		ical.setTimezoneInfo(americaNewYork());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DTSTART:20141007T053400\r\n" +
			"DTSTART:20141008T053400\r\n" +
			"TZ:-0500\r\n" +
			"DAYLIGHT:TRUE;-0400;20140309T020000;20141102T020000;EST;EDT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_timezone_standard_one_date() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new DateStart(utc(2014, 1, 7, 9, 34, 0)));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V1_0);
		ical.setTimezoneInfo(americaNewYork());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DTSTART:20140107T043400\r\n" +
			"TZ:-0500\r\n" +
			"DAYLIGHT:FALSE\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_timezone_standard_two_dates() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new DateStart(utc(2014, 1, 7, 9, 34, 0)));
		ical.addProperty(new DateStart(utc(2014, 2, 7, 9, 34, 0)));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V1_0);
		ical.setTimezoneInfo(americaNewYork());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DTSTART:20140107T043400\r\n" +
			"DTSTART:20140207T043400\r\n" +
			"TZ:-0500\r\n" +
			"DAYLIGHT:FALSE\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_timezone_standard_and_daylight() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new DateStart(utc(2014, 1, 7, 9, 34, 0)));
		ical.addProperty(new DateStart(utc(2014, 10, 7, 9, 34, 0)));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V1_0);
		ical.setTimezoneInfo(americaNewYork());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DTSTART:20140107T043400\r\n" +
			"DTSTART:20141007T053400\r\n" +
			"TZ:-0500\r\n" +
			"DAYLIGHT:TRUE;-0400;20140309T020000;20141102T020000;EST;EDT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_timezone_standard_and_two_daylights() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new DateStart(utc(2014, 1, 7, 9, 34, 0)));
		ical.addProperty(new DateStart(utc(2014, 10, 7, 9, 34, 0)));
		ical.addProperty(new DateStart(utc(2015, 10, 7, 9, 34, 0)));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V1_0);
		ical.setTimezoneInfo(americaNewYork());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DTSTART:20140107T043400\r\n" +
			"DTSTART:20141007T053400\r\n" +
			"DTSTART:20151007T053400\r\n" +
			"TZ:-0500\r\n" +
			"DAYLIGHT:TRUE;-0400;20140309T020000;20141102T020000;EST;EDT\r\n" +
			"DAYLIGHT:TRUE;-0400;20150308T020000;20151101T020000;EST;EDT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_timezone_no_daylight_component() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		ical.addProperty(new DateStart(utc(2014, 1, 7, 9, 34, 0)));
		ical.addProperty(new DateStart(utc(2014, 10, 7, 9, 34, 0)));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V1_0);
		ical.setTimezoneInfo(americaNewYorkWithoutDaylight());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DTSTART:20140107T043400\r\n" +
			"DTSTART:20141007T043400\r\n" +
			"TZ:-0500\r\n" +
			"DAYLIGHT:FALSE\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_Organizer_to_Attendee() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		Organizer organizer = new Organizer("John Doe", "jdoe@example.com");
		organizer.setLanguage("en");
		ical.addProperty(organizer);

		{
			ICalVersion version = V1_0;
			StringWriter sw = new StringWriter();
			ICalWriter writer = new ICalWriter(sw, version);
			writer.write(ical);
			writer.close();

			//@formatter:off
			String expected = 
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"ATTENDEE;LANGUAGE=en;ROLE=ORGANIZER:John Doe <jdoe@example.com>\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}

		for (ICalVersion version : each(V2_0_DEPRECATED, V2_0)) {
			StringWriter sw = new StringWriter();
			ICalWriter writer = new ICalWriter(sw, version);
			writer.write(ical);
			writer.close();

			//@formatter:off
			String expected = 
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"ORGANIZER;LANGUAGE=en;CN=John Doe:mailto:jdoe@example.com\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}

	@Test
	public void vcal_DCREATED_property() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		Created created = new Created(utc(2014, 1, 1, 1, 0, 0));
		ical.addProperty(created);

		{
			ICalVersion version = V1_0;
			StringWriter sw = new StringWriter();
			ICalWriter writer = new ICalWriter(sw, version);
			writer.write(ical);
			writer.close();

			//@formatter:off
			String expected = 
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"DCREATED:20140101T010000Z\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}

		for (ICalVersion version : each(V2_0_DEPRECATED, V2_0)) {
			StringWriter sw = new StringWriter();
			ICalWriter writer = new ICalWriter(sw, version);
			writer.write(ical);
			writer.close();

			//@formatter:off
			String expected = 
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"CREATED:20140101T010000Z\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}

	@Test
	public void setGlobalTimezone() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setDateStart(utc(1996, 7, 4, 12, 0, 0));
		ical.addEvent(event);

		TimeZone nyTimezone = TimeZone.getTimeZone("America/New_York");
		VTimezone nyComponent = new VTimezone(nyTimezone.getID());
		ical.getTimezoneInfo().setDefaultTimezone(new TimezoneAssignment(nyTimezone, nyComponent));

		TimeZone laTimezone = TimeZone.getTimeZone("America/Los_Angeles");
		VTimezone laComponent = new VTimezone(laTimezone.getID());

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, V2_0);
		writer.write(ical);
		writer.setGlobalTimezone(new TimezoneAssignment(laTimezone, laComponent));
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VTIMEZONE\r\n" +
				"TZID:America/New_York\r\n" +
			"END:VTIMEZONE\r\n" +
			"BEGIN:VEVENT\r\n" +
				"DTSTART;TZID=America/New_York:19960704T080000\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VTIMEZONE\r\n" +
				"TZID:America/Los_Angeles\r\n" +
			"END:VTIMEZONE\r\n" +
			"BEGIN:VEVENT\r\n" +
				"DTSTART;TZID=America/Los_Angeles:19960704T050000\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void example1() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN");
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.setDateTimeStamp(utc(1996, 7, 4, 12, 0, 0));
			event.setUid("uid1@example.com");
			event.setOrganizer("jsmith@example.com");
			event.setDateStart(utc(1996, 9, 18, 14, 30, 0));
			event.setDateEnd(utc(1996, 9, 20, 22, 0, 0));
			event.setStatus(Status.confirmed());
			event.addCategories("CONFERENCE");
			event.setSummary("Networld+Interop Conference");
			event.setDescription("Networld+Interop Conferenceand Exhibit\nAtlanta World Congress Center\nAtlanta, Georgia");
			ical.addEvent(event);
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();
		assertExample(ical, "rfc5545-example1.ics", V2_0);
	}

	@Test
	public void example2() throws Throwable {
		TimeZone eastern = TimeZone.getTimeZone("America/New_York");
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//RDU Software//NONSGML HandCal//EN");
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utc(1998, 3, 9, 23, 10, 0));
			event.setUid("guid-1.example.com");
			event.setOrganizer("mrbig@example.com");

			Attendee attendee = new Attendee(null, "employee-A@example.com");
			attendee.setRsvp(true);
			attendee.setParticipationLevel(ParticipationLevel.REQUIRED);
			attendee.setCalendarUserType(CalendarUserType.GROUP);
			event.addAttendee(attendee);

			event.setDescription("Project XYZ Review Meeting");
			event.addCategories("MEETING");
			event.setClassification(Classification.public_());
			event.setCreated(utc(1998, 3, 9, 13, 0, 0));
			event.setSummary("XYZ Project Review");
			event.setDateStart(date(1998, 3, 12, 8, 30, 0, eastern));
			event.setDateEnd(date(1998, 3, 12, 9, 30, 0, eastern));
			event.setLocation("1CP Conference Room 4350");
			ical.addEvent(event);
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();

		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		VTimezone usEasternTz;
		{
			usEasternTz = new VTimezone("America/New_York");
			{
				StandardTime standard = new StandardTime();
				standard.setDateStart(new DateTimeComponents(1998, 10, 25, 2, 0, 0, false));
				standard.setTimezoneOffsetFrom(new UtcOffset(false, 4, 0));
				standard.setTimezoneOffsetTo(new UtcOffset(false, 5, 0));
				standard.addTimezoneName("EST");
				usEasternTz.addStandardTime(standard);
			}
			{
				DaylightSavingsTime daylight = new DaylightSavingsTime();
				daylight.setDateStart(new DateTimeComponents(1999, 4, 4, 2, 0, 0, false));
				daylight.setTimezoneOffsetFrom(new UtcOffset(false, 5, 0));
				daylight.setTimezoneOffsetTo(new UtcOffset(false, 4, 0));
				daylight.addTimezoneName("EDT");
				usEasternTz.addDaylightSavingsTime(daylight);
			}
		}

		tzinfo.setDefaultTimezone(new TimezoneAssignment(eastern, usEasternTz));
		assertExample(ical, "rfc5545-example2.ics", V2_0);
	}

	@Test
	public void example3() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setMethod("xyz");
		ical.setProductId("-//ABC Corporation//NONSGML My Product//EN");
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.setDateTimeStamp(utc(1997, 3, 24, 12, 0, 0));
			event.setSequence(0);
			event.setUid("uid3@example.com");
			event.setOrganizer("jdoe@example.com");

			Attendee attendee = new Attendee(null, "jsmith@example.com");
			attendee.setRsvp(true);
			event.addAttendee(attendee);

			event.setDateStart(utc(1997, 3, 24, 12, 30, 0));
			event.setDateEnd(utc(1997, 3, 24, 21, 0, 0));
			event.addCategories("MEETING", "PROJECT");
			event.setClassification(Classification.public_());
			event.setSummary("Calendaring Interoperability Planning Meeting");
			event.setDescription("Discuss how we can test c&s interoperability\nusing iCalendar and other IETF standards.");
			event.setLocation("LDB Lobby");

			Attachment attach = new Attachment("application/postscript", "ftp://example.com/pub/conf/bkgrnd.ps");
			event.addAttachment(attach);

			ical.addEvent(event);
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();
		assertExample(ical, "rfc5545-example3.ics", V2_0);
	}

	@Test
	public void example4() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//ABC Corporation//NONSGML My Product//EN");
		{
			VTodo todo = new VTodo();
			todo.getProperties().clear();
			todo.setDateTimeStamp(utc(1998, 1, 30, 13, 45, 0));
			todo.setSequence(2);
			todo.setUid("uid4@example.com");
			todo.setOrganizer("unclesam@example.com");

			Attendee attendee = new Attendee(null, "jqpublic@example.com");
			attendee.setParticipationStatus(ParticipationStatus.ACCEPTED);
			todo.addAttendee(attendee);

			todo.setDateDue(date(1998, 4, 15));
			todo.setStatus(Status.needsAction());
			todo.setSummary("Submit Income Taxes");
			{
				Trigger trigger = new Trigger(utc(1998, 4, 3, 12, 0, 0));
				Attachment attach = new Attachment("audio/basic", "http://example.com/pub/audio-files/ssbanner.aud");
				VAlarm alarm = VAlarm.audio(trigger, attach);
				alarm.setRepeat(4);
				alarm.setDuration(Duration.builder().hours(1).build());
				todo.addAlarm(alarm);
			}

			ical.addTodo(todo);
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		tzinfo.setFloating(ical.getTodos().get(0).getDateDue(), true);
		assertExample(ical, "rfc5545-example4.ics", V2_0);
	}

	@Test
	public void example5() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//ABC Corporation//NONSGML My Product//EN");
		{
			VJournal journal = new VJournal();
			journal.getProperties().clear();
			journal.setDateTimeStamp(utc(1997, 3, 24, 12, 0, 0));
			journal.setUid("uid5@example.com");
			journal.setOrganizer("jsmith@example.com");
			journal.setStatus(Status.draft());
			journal.setClassification(Classification.public_());
			journal.addCategories("Project Report", "XYZ", "Weekly Meeting");
			journal.addDescription("Project xyz Review Meeting Minutes\nAgenda\n1. Review of project version 1.0 requirements.\n2.Definitionof project processes.\n3. Review of project schedule.\nParticipants: John Smith, Jane Doe, Jim Dandy\n-It wasdecided that the requirements need to be signed off byproduct marketing.\n-Project processes were accepted.\n-Project schedule needs to account for scheduled holidaysand employee vacation time. Check with HR for specificdates.\n-New schedule will be distributed by Friday.\n-Next weeks meeting is cancelled. No meeting until 3/23.");
			ical.addJournal(journal);
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();
		assertExample(ical, "rfc5545-example5.ics", V2_0);
	}

	@Test
	public void example6() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//RDU Software//NONSGML HandCal//EN");
		VFreeBusy freebusy = new VFreeBusy();
		{
			freebusy.getProperties().clear();
			freebusy.setOrganizer("jsmith@example.com");
			freebusy.setDateStart(utc(1998, 3, 13, 14, 17, 11));
			freebusy.setDateEnd(utc(1998, 4, 10, 14, 17, 11));

			FreeBusy fb = new FreeBusy();
			fb.getValues().add(new Period(utc(1998, 3, 14, 23, 30, 0), utc(1998, 3, 15, 0, 30, 0)));
			freebusy.addFreeBusy(fb);

			fb = new FreeBusy();
			fb.getValues().add(new Period(utc(1998, 3, 16, 15, 30, 0), utc(1998, 3, 16, 16, 30, 0)));
			freebusy.addFreeBusy(fb);

			fb = new FreeBusy();
			fb.getValues().add(new Period(utc(1998, 3, 18, 3, 0, 0), utc(1998, 3, 18, 4, 0, 0)));
			freebusy.addFreeBusy(fb);

			freebusy.setUrl("http://www.example.com/calendar/busytime/jsmith.ifb");
			ical.addFreeBusy(freebusy);
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).warn(freebusy, 2, 2).run(); //UID and DTSTAMP missing
		assertExample(ical, "rfc5545-example6.ics", V2_0);
	}

	@Test
	public void vcal_example1() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.addCategories("MEETING");
			event.setStatus(Status.tentative());
			event.setDateStart(utc(1996, 4, 1, 3, 30, 0));
			event.setDateEnd(utc(1996, 4, 1, 4, 30, 0));
			event.setSummary("Your Proposal Review");
			event.setDescription("Steve and John to review newest proposal material");
			event.setClassification(Classification.private_());

			ical.addEvent(event);
		}

		assertValidate(ical).versions(V1_0).run();
		assertExample(ical, "vcal-example1.vcs", V1_0);
	}

	@Test
	public void vcal_example2() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		{
			VTodo todo = new VTodo();
			todo.getProperties().clear();
			todo.setSummary("John to pay for lunch");
			todo.setDateDue(utc(1996, 4, 1, 8, 30, 0));
			todo.setStatus(Status.needsAction());

			ical.addTodo(todo);
		}

		assertValidate(ical).versions(V1_0).run();
		assertExample(ical, "vcal-example2.vcs", V1_0);
	}

	@Test
	public void vcal_example3() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		VEvent event;
		{
			event = new VEvent();
			event.getProperties().clear();
			event.addCategories("MEETING");
			event.setStatus(Status.needsAction());
			event.setDateStart(utc(1996, 4, 1, 7, 30, 0));
			event.setDateEnd(utc(1996, 4, 1, 8, 30, 0));
			event.setSummary("Steve's Proposal Review");
			event.setDescription("Steve and John to review newest proposal material");
			event.setClassification(Classification.private_());

			ical.addEvent(event);
		}

		{
			VTodo todo = new VTodo();
			todo.getProperties().clear();
			todo.setSummary("John to pay for lunch");
			todo.setDateDue(utc(1996, 4, 1, 8, 30, 0));
			todo.setStatus(Status.needsAction());

			ical.addTodo(todo);
		}

		assertValidate(ical).versions(V1_0).run();
		assertExample(ical, "vcal-example3.vcs", V1_0);
	}

	private void assertExample(ICalendar ical, String exampleFileName, ICalVersion version) throws IOException {
		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, version);
		writer.getVObjectWriter().getFoldedLineWriter().setLineLength(null);
		writer.write(ical);
		writer.close();

		String expected = new Gobble(getClass().getResourceAsStream(exampleFileName)).asString();
		expected = expected.replaceAll("([^\r])\n", "$1\r\n"); //replace \n with \r\n
		expected = expected.replaceAll("\r\n ", ""); //unfold folded lines

		String actual = sw.toString();

		assertEquals(expected, actual);
	}

	private TimezoneInfo americaNewYorkWithoutDaylight() {
		VTimezone timezone = new VTimezone("id");
		{

			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(2007, 11, 4, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(new UtcOffset(false, 4, 0));
			standard.setTimezoneOffsetTo(new UtcOffset(false, 5, 0));
			standard.addTimezoneName("EST");
			//@formatter:off
			standard.setRecurrenceRule(new Recurrence.Builder
			(Frequency.YEARLY)
			.byMonth(11)
			.byDay(1, DayOfWeek.SUNDAY)
			.build());
			//@formatter:on
			timezone.addStandardTime(standard);
		}

		ICalTimeZone icalTz = new ICalTimeZone(timezone);

		TimezoneInfo tzinfo = new TimezoneInfo();
		tzinfo.setDefaultTimezone(new TimezoneAssignment(icalTz, timezone));
		return tzinfo;
	}

	private TimezoneInfo americaNewYork() {
		VTimezone timezone = new VTimezone("id");
		{
			DaylightSavingsTime daylightSavings = new DaylightSavingsTime();
			daylightSavings.setDateStart(new DateTimeComponents(2007, 3, 11, 2, 0, 0, false));
			daylightSavings.setTimezoneOffsetFrom(new UtcOffset(false, 5, 0));
			daylightSavings.setTimezoneOffsetTo(new UtcOffset(false, 4, 0));
			daylightSavings.addTimezoneName("EDT");
			//@formatter:off
			daylightSavings.setRecurrenceRule(new Recurrence.Builder
			(Frequency.YEARLY)
			.byMonth(3)
			.byDay(2, DayOfWeek.SUNDAY)
			.build());
			//@formatter:on
			timezone.addDaylightSavingsTime(daylightSavings);

			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(2007, 11, 4, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(new UtcOffset(false, 4, 0));
			standard.setTimezoneOffsetTo(new UtcOffset(false, 5, 0));
			standard.addTimezoneName("EST");
			//@formatter:off
			standard.setRecurrenceRule(new Recurrence.Builder
			(Frequency.YEARLY)
			.byMonth(11)
			.byDay(1, DayOfWeek.SUNDAY)
			.build());
			//@formatter:on
			timezone.addStandardTime(standard);
		}

		ICalTimeZone icalTz = new ICalTimeZone(timezone);

		TimezoneInfo tzinfo = new TimezoneInfo();
		tzinfo.setDefaultTimezone(new TimezoneAssignment(icalTz, timezone));
		return tzinfo;
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
			super(TestProperty.class, "X-TEST", null);
		}

		@Override
		protected String _writeText(TestProperty property, WriteContext context) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return new TestProperty(value);
		}
	}

	private class BadNameMarshaller extends ICalPropertyScribe<TestProperty> {
		public BadNameMarshaller() {
			super(TestProperty.class, "BAD:NAME", null);
		}

		@Override
		protected String _writeText(TestProperty property, WriteContext context) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return new TestProperty(value);
		}
	}

	private class MyProdIdScribe extends ICalPropertyScribe<ProductId> {
		public MyProdIdScribe() {
			super(ProductId.class, "PRODID", ICalDataType.TEXT);
		}

		@Override
		protected String _writeText(ProductId property, WriteContext context) {
			return property.getValue().toUpperCase();
		}

		@Override
		protected ProductId _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return new ProductId(value);
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

	private class DataTypePropertyMarshaller extends ICalPropertyScribe<TestProperty> {
		public DataTypePropertyMarshaller() {
			super(TestProperty.class, "X-TEST", ICalDataType.TEXT);
		}

		@Override
		protected ICalDataType _dataType(TestProperty property, ICalVersion version) {
			if (property.getValue().matches("\\d+")) {
				return ICalDataType.INTEGER;
			}
			return ICalDataType.TEXT;
		}

		@Override
		protected String _writeText(TestProperty property, WriteContext context) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return new TestProperty(value);
		}
	}
}
