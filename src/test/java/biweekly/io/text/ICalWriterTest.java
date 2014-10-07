package biweekly.io.text;

import static biweekly.util.TestUtils.assertRegex;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.each;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
import biweekly.io.ParseContext;
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
import biweekly.util.Duration;
import biweekly.util.IOUtils;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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

	@Test
	public void bad_parameter_value_chars() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProductId().addParameter("X-TEST", "\"test\"");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
	}

	@Test
	public void caret_encoding() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProductId().addParameter("X-TEST", "\"test\"");

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
		writer.write(ical);
	}

	@Test(expected = IllegalArgumentException.class)
	public void no_component_marshaller() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addComponent(new Party());

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
		writer.write(ical);
	}

	@Test(expected = IllegalArgumentException.class)
	public void bad_property_name() throws Exception {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty("value"));

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
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
		ICalWriter writer = new ICalWriter(file, ICalVersion.V2_0);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"SUMMARY:\u1e66ummary\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = IOUtils.getFileContents(file, "UTF-8");
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_VTimezone_to_Daylight() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		VTimezone timezone = new VTimezone(null);

		DaylightSavingsTime daylightSavings = new DaylightSavingsTime();
		daylightSavings.setDateStart(new DateStart(date("2014-01-01 01:00:00")));
		daylightSavings.setTimezoneOffsetFrom(-5, 0);
		daylightSavings.setTimezoneOffsetTo(-4, 0);
		daylightSavings.addTimezoneName("EDT");
		timezone.addDaylightSavingsTime(daylightSavings);

		StandardTime standard = new StandardTime();
		standard.setDateStart(new DateStart(date("2014-02-01 01:00:00")));
		standard.setTimezoneOffsetFrom(-4, 0);
		standard.setTimezoneOffsetTo(-5, 0);
		standard.addTimezoneName("EST");
		timezone.addStandardTime(standard);

		ical.addComponent(timezone);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V1_0);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DAYLIGHT:TRUE;-0400;20140101T010000;20140201T010000;EST;EDT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_VTimezone_to_Daylight_multiple_observances() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new DateStart(date("2014-10-07 09:34:00")));

		VTimezone timezone = new VTimezone("id");

		DaylightSavingsTime daylightSavings = new DaylightSavingsTime();
		daylightSavings.setDateStart(new DateStart(date("2014-01-01 01:00:00")));
		daylightSavings.setTimezoneOffsetFrom(-5, 0);
		daylightSavings.setTimezoneOffsetTo(-4, 0);
		daylightSavings.addTimezoneName("EDT");
		timezone.addDaylightSavingsTime(daylightSavings);

		StandardTime standard = new StandardTime();
		standard.setDateStart(new DateStart(date("2014-02-01 01:00:00")));
		standard.setTimezoneOffsetFrom(-4, 0);
		standard.setTimezoneOffsetTo(-5, 0);
		standard.addTimezoneName("EST");
		timezone.addStandardTime(standard);

		daylightSavings = new DaylightSavingsTime();
		daylightSavings.setDateStart(new DateStart(date("2014-03-01 01:00:00")));
		daylightSavings.setTimezoneOffsetFrom(-5, 0);
		daylightSavings.setTimezoneOffsetTo(-4, 0);
		daylightSavings.addTimezoneName("EDT2");
		timezone.addDaylightSavingsTime(daylightSavings);

		standard = new StandardTime();
		standard.setDateStart(new DateStart(date("2014-04-01 01:00:00")));
		standard.setTimezoneOffsetFrom(-4, 0);
		standard.setTimezoneOffsetTo(-5, 0);
		standard.addTimezoneName("EST2");
		timezone.addStandardTime(standard);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V1_0);
		writer.getTimezoneInfo().assign(timezone, TimeZone.getDefault());
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DTSTART:20141007T093400\r\n" +
			"DAYLIGHT:TRUE;-0400;20140101T010000;20140201T010000;EST;EDT\r\n" +
			"DAYLIGHT:TRUE;-0400;20140301T010000;20140401T010000;EST2;EDT2\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_VTimezone_to_Daylight_no_daylight_component() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		VTimezone timezone = new VTimezone(null);

		StandardTime standard = new StandardTime();
		standard.setDateStart(new DateStart(date("2014-02-01 01:00:00")));
		standard.setTimezoneOffsetFrom(-4, 0);
		standard.setTimezoneOffsetTo(-5, 0);
		standard.addTimezoneName("EST");
		timezone.addStandardTime(standard);

		ical.addComponent(timezone);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V1_0);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DAYLIGHT:FALSE\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_VTimezone_to_Daylight_no_standard_component() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		VTimezone timezone = new VTimezone(null);

		DaylightSavingsTime daylightSavings = new DaylightSavingsTime();
		daylightSavings.setDateStart(new DateStart(date("2014-01-01 01:00:00")));
		daylightSavings.setTimezoneOffsetFrom(-5, 0);
		daylightSavings.setTimezoneOffsetTo(-4, 0);
		daylightSavings.addTimezoneName("EDT");
		timezone.addDaylightSavingsTime(daylightSavings);

		ical.addComponent(timezone);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V1_0);
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
	public void vcal_Organizer_to_Attendee() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		Organizer organizer = new Organizer("John Doe", "jdoe@example.com");
		organizer.setLanguage("en");
		ical.addProperty(organizer);

		{
			ICalVersion version = ICalVersion.V1_0;
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

		for (ICalVersion version : each(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0)) {
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

		Created created = new Created(date("2014-01-01 01:00:00 +0000"));
		ical.addProperty(created);

		{
			ICalVersion version = ICalVersion.V1_0;
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

		for (ICalVersion version : each(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0)) {
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
	public void vcal_do_not_write_DTSTAMP() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.removeProperties(ProductId.class);
		VEvent event = new VEvent();
		ical.addEvent(event);

		{
			ICalVersion version = ICalVersion.V1_0;
			StringWriter sw = new StringWriter();
			ICalWriter writer = new ICalWriter(sw, version);
			writer.write(ical);
			writer.close();

			//@formatter:off
			String expected = 
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1\\.0\r\n" +
				"BEGIN:VEVENT\r\n" +
					"UID:(.*?)\r\n" +
				"END:VEVENT\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertTrue(actual, actual.matches(expected));
		}

		{
			ICalVersion version = ICalVersion.V2_0;
			StringWriter sw = new StringWriter();
			ICalWriter writer = new ICalWriter(sw, version);
			writer.write(ical);
			writer.close();

			//@formatter:off
			String expected = 
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"BEGIN:VEVENT\r\n" +
					"UID:(.*?)\r\n" +
					"DTSTAMP:(.*?)\r\n" +
				"END:VEVENT\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertTrue(actual, actual.matches(expected));
		}
	}

	@Test
	public void example1() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN");
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.setDateTimeStamp(utc("1996-07-04 12:00:00"));
			event.setUid("uid1@example.com");
			event.setOrganizer("jsmith@example.com");
			event.setDateStart(utc("1996-09-18 14:30:00"));
			event.setDateEnd(utc("1996-09-20 22:00:00"));
			event.setStatus(Status.confirmed());
			event.addCategories("CONFERENCE");
			event.setSummary("Networld+Interop Conference");
			event.setDescription("Networld+Interop Conferenceand Exhibit\nAtlanta World Congress Center\nAtlanta, Georgia");
			ical.addEvent(event);
		}

		assertValidate(ical).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run();
		assertExample(ical, "rfc5545-example1.ics", new TimezoneInfo());
	}

	@Test
	public void example2() throws Throwable {
		TimeZone eastern = TimeZone.getTimeZone("America/New_York");
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//RDU Software//NONSGML HandCal//EN");
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utc("1998-03-09 23:10:00"));
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
			event.setCreated(utc("1998-03-09 13:00:00"));
			event.setSummary("XYZ Project Review");
			event.setDateStart(date("1998-03-12 08:30:00", eastern));
			event.setDateEnd(date("1998-03-12 09:30:00", eastern));
			event.setLocation("1CP Conference Room 4350");
			ical.addEvent(event);
		}

		assertValidate(ical).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run();

		TimezoneInfo tzinfo = new TimezoneInfo();
		VTimezone usEasternTz;
		{
			usEasternTz = new VTimezone("America/New_York");
			{
				StandardTime standard = new StandardTime();
				standard.setDateStart(new DateTimeComponents(1998, 10, 25, 2, 0, 0, false));
				standard.setTimezoneOffsetFrom(-4, 0);
				standard.setTimezoneOffsetTo(-5, 0);
				standard.addTimezoneName("EST");
				usEasternTz.addStandardTime(standard);
			}
			{
				DaylightSavingsTime daylight = new DaylightSavingsTime();
				daylight.setDateStart(new DateTimeComponents(1999, 4, 4, 2, 0, 0, false));
				daylight.setTimezoneOffsetFrom(-5, 0);
				daylight.setTimezoneOffsetTo(-4, 0);
				daylight.addTimezoneName("EDT");
				usEasternTz.addDaylightSavingsTime(daylight);
			}
		}

		tzinfo.assign(usEasternTz, eastern);
		tzinfo.setDefaultTimeZone(eastern);
		assertExample(ical, "rfc5545-example2.ics", tzinfo);
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
			event.setDateTimeStamp(utc("1997-03-24 12:00:00"));
			event.setSequence(0);
			event.setUid("uid3@example.com");
			event.setOrganizer("jdoe@example.com");

			Attendee attendee = new Attendee(null, "jsmith@example.com");
			attendee.setRsvp(true);
			event.addAttendee(attendee);

			event.setDateStart(utc("1997-03-24 12:30:00"));
			event.setDateEnd(utc("1997-03-24 21:00:00"));
			event.addCategories("MEETING", "PROJECT");
			event.setClassification(Classification.public_());
			event.setSummary("Calendaring Interoperability Planning Meeting");
			event.setDescription("Discuss how we can test c&s interoperability\nusing iCalendar and other IETF standards.");
			event.setLocation("LDB Lobby");

			Attachment attach = new Attachment("application/postscript", "ftp://example.com/pub/conf/bkgrnd.ps");
			event.addAttachment(attach);

			ical.addEvent(event);
		}

		assertValidate(ical).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run();
		assertExample(ical, "rfc5545-example3.ics", new TimezoneInfo());
	}

	@Test
	public void example4() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//ABC Corporation//NONSGML My Product//EN");
		{
			VTodo todo = new VTodo();
			todo.getProperties().clear();
			todo.setDateTimeStamp(utc("1998-01-30 13:45:00"));
			todo.setSequence(2);
			todo.setUid("uid4@example.com");
			todo.setOrganizer("unclesam@example.com");

			Attendee attendee = new Attendee(null, "jqpublic@example.com");
			attendee.setParticipationStatus(ParticipationStatus.ACCEPTED);
			todo.addAttendee(attendee);

			todo.setDateDue(date("1998-04-15"));
			todo.setStatus(Status.needsAction());
			todo.setSummary("Submit Income Taxes");
			{
				Trigger trigger = new Trigger(utc("1998-04-03 12:00:00"));
				Attachment attach = new Attachment("audio/basic", "http://example.com/pub/audio-files/ssbanner.aud");
				VAlarm alarm = VAlarm.audio(trigger, attach);
				alarm.setRepeat(4);
				alarm.setDuration(Duration.builder().hours(1).build());
				todo.addAlarm(alarm);
			}

			ical.addTodo(todo);
		}

		assertValidate(ical).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run();
		TimezoneInfo options = new TimezoneInfo();
		options.setFloating(ical.getTodos().get(0).getDateDue(), true);
		assertExample(ical, "rfc5545-example4.ics", options);
	}

	@Test
	public void example5() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//ABC Corporation//NONSGML My Product//EN");
		{
			VJournal journal = new VJournal();
			journal.getProperties().clear();
			journal.setDateTimeStamp(utc("1997-03-24 12:00:00"));
			journal.setUid("uid5@example.com");
			journal.setOrganizer("jsmith@example.com");
			journal.setStatus(Status.draft());
			journal.setClassification(Classification.public_());
			journal.addCategories("Project Report", "XYZ", "Weekly Meeting");
			journal.addDescription("Project xyz Review Meeting Minutes\nAgenda\n1. Review of project version 1.0 requirements.\n2.Definitionof project processes.\n3. Review of project schedule.\nParticipants: John Smith, Jane Doe, Jim Dandy\n-It wasdecided that the requirements need to be signed off byproduct marketing.\n-Project processes were accepted.\n-Project schedule needs to account for scheduled holidaysand employee vacation time. Check with HR for specificdates.\n-New schedule will be distributed by Friday.\n-Next weeks meeting is cancelled. No meeting until 3/23.");
			ical.addJournal(journal);
		}

		assertValidate(ical).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run();
		assertExample(ical, "rfc5545-example5.ics", new TimezoneInfo());
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
			freebusy.setDateStart(utc("1998-03-13 14:17:11"));
			freebusy.setDateEnd(utc("1998-04-10 14:17:11"));

			FreeBusy fb = new FreeBusy();
			fb.addValue(utc("1998-03-14 23:30:00"), utc("1998-03-15 00:30:00"));
			freebusy.addFreeBusy(fb);

			fb = new FreeBusy();
			fb.addValue(utc("1998-03-16 15:30:00"), utc("1998-03-16 16:30:00"));
			freebusy.addFreeBusy(fb);

			fb = new FreeBusy();
			fb.addValue(utc("1998-03-18 03:00:00"), utc("1998-03-18 04:00:00"));
			freebusy.addFreeBusy(fb);

			freebusy.setUrl("http://www.example.com/calendar/busytime/jsmith.ifb");
			ical.addFreeBusy(freebusy);
		}

		assertValidate(ical).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).warn(freebusy, 2, 2).run(); //UID and DTSTAMP missing
		assertExample(ical, "rfc5545-example6.ics", new TimezoneInfo());
	}

	private void assertExample(ICalendar ical, String exampleFileName, TimezoneInfo timezoneOptions) throws IOException {
		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
		writer.getRawWriter().getFoldedLineWriter().setLineLength(null);
		writer.setTimezoneInfo(timezoneOptions);
		writer.write(ical);
		writer.close();

		String expected = new String(IOUtils.toByteArray(getClass().getResourceAsStream(exampleFileName)));
		expected = expected.replaceAll("([^\r])\n", "$1\r\n"); //replace \n with \r\n
		expected = expected.replaceAll("\r\n ", ""); //unfold folded lines

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
			super(TestProperty.class, "BAD*NAME", null);
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
