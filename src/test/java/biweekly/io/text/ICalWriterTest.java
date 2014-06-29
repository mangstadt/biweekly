package biweekly.io.text;

import static biweekly.util.TestUtils.assertRegex;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.Warning;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.StandardTime;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.component.VFreeBusy;
import biweekly.component.VJournal;
import biweekly.component.VTimezone;
import biweekly.component.VTodo;
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
import biweekly.property.DateStart;
import biweekly.property.Daylight;
import biweekly.property.FreeBusy;
import biweekly.property.ICalProperty;
import biweekly.property.ProductId;
import biweekly.property.SkipMeProperty;
import biweekly.property.Status;
import biweekly.property.Summary;
import biweekly.property.Trigger;
import biweekly.property.Version;
import biweekly.util.DateTimeComponents;
import biweekly.util.Duration;
import biweekly.util.IOUtils;
import biweekly.util.UtcOffset;

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
public class ICalWriterTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private final DateFormat utcFormatter;
	{
		utcFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		utcFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	private final DateFormat usEasternFormatter;
	{
		usEasternFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
		usEasternFormatter.setTimeZone(TimeZone.getTimeZone("America/New_York"));
	}
	private final DateFormat localFormatter = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

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

		ical.addComponent(timezone);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V1_0);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
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
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void vcal_Daylight_to_VTimezone() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();

		Daylight daylight = new Daylight(true, new UtcOffset(-4, 0), date("2014-01-01 01:00:00"), date("2014-02-01 01:00:00"), "EST", "EDT");
		ical.addProperty(daylight);

		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0);
		writer.write(ical);
		writer.close();

		//@formatter:off
		String expected = 
		"BEGIN:VCALENDAR\r\n" +
			"BEGIN:VTIMEZONE\r\n" +
				"TZID:TZ1\r\n" +
				"BEGIN:DAYLIGHT\r\n" +
					"DTSTART:20140101T010000\r\n" +
					"TZOFFSETFROM:-0500\r\n" +
					"TZOFFSETTO:-0400\r\n" +
					"TZNAME:EDT\r\n" +
				"END:DAYLIGHT\r\n" +
				"BEGIN:STANDARD\r\n" +
					"DTSTART:20140201T010000\r\n" +
					"TZOFFSETFROM:-0400\r\n" +
					"TZOFFSETTO:-0500\r\n" +
					"TZNAME:EST\r\n" +
				"END:STANDARD\r\n" +
			"END:VTIMEZONE\r\n" +
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
		ical.setVersion(Version.v2_0());
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.setDateTimeStamp(utcFormatter.parse("19960704T120000"));
			event.setUid("uid1@example.com");
			event.setOrganizer("jsmith@example.com");
			event.setDateStart(utcFormatter.parse("19960918T143000"));
			event.setDateEnd(utcFormatter.parse("19960920T220000"));
			event.setStatus(Status.confirmed());
			event.addCategories("CONFERENCE");
			event.setSummary("Networld+Interop Conference");
			event.setDescription("Networld+Interop Conferenceand Exhibit\nAtlanta World Congress Center\nAtlanta, Georgia");
			ical.addEvent(event);
		}

		assertValidate(ical).run();
		assertExample(ical, "rfc5545-example1.ics");
	}

	@Test
	public void example2() throws Throwable {
		VTimezone usEasternTz;
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//RDU Software//NONSGML HandCal//EN");
		ical.setVersion(Version.v2_0());
		{
			usEasternTz = new VTimezone(null);
			usEasternTz.setTimezoneId("America/New_York");
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
			ical.addTimezone(usEasternTz);
		}
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utcFormatter.parse("19980309T231000"));
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
			event.setCreated(utcFormatter.parse("19980309T130000"));
			event.setSummary("XYZ Project Review");
			event.setDateStart(usEasternFormatter.parse("19980312T083000")).setTimezone(usEasternTz);
			event.setDateEnd(usEasternFormatter.parse("19980312T093000")).setTimezone(usEasternTz);
			event.setLocation("1CP Conference Room 4350");
			ical.addEvent(event);
		}

		assertValidate(ical).run();
		assertExample(ical, "rfc5545-example2.ics");
	}

	@Test
	public void example3() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setMethod("xyz");
		ical.setVersion(Version.v2_0());
		ical.setProductId("-//ABC Corporation//NONSGML My Product//EN");
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.setDateTimeStamp(utcFormatter.parse("19970324T120000"));
			event.setSequence(0);
			event.setUid("uid3@example.com");
			event.setOrganizer("jdoe@example.com");

			Attendee attendee = new Attendee(null, "jsmith@example.com");
			attendee.setRsvp(true);
			event.addAttendee(attendee);

			event.setDateStart(utcFormatter.parse("19970324T123000"));
			event.setDateEnd(utcFormatter.parse("19970324T210000"));
			event.addCategories("MEETING", "PROJECT");
			event.setClassification(Classification.public_());
			event.setSummary("Calendaring Interoperability Planning Meeting");
			event.setDescription("Discuss how we can test c&s interoperability\nusing iCalendar and other IETF standards.");
			event.setLocation("LDB Lobby");

			Attachment attach = new Attachment("application/postscript", "ftp://example.com/pub/conf/bkgrnd.ps");
			event.addAttachment(attach);

			ical.addEvent(event);
		}

		assertValidate(ical).run();
		assertExample(ical, "rfc5545-example3.ics");
	}

	@Test
	public void example4() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setVersion(Version.v2_0());
		ical.setProductId("-//ABC Corporation//NONSGML My Product//EN");
		{
			VTodo todo = new VTodo();
			todo.getProperties().clear();
			todo.setDateTimeStamp(utcFormatter.parse("19980130T134500"));
			todo.setSequence(2);
			todo.setUid("uid4@example.com");
			todo.setOrganizer("unclesam@example.com");

			Attendee attendee = new Attendee(null, "jqpublic@example.com");
			attendee.setParticipationStatus(ParticipationStatus.ACCEPTED);
			todo.addAttendee(attendee);

			todo.setDateDue(localFormatter.parse("19980415T000000")).setLocalTime(true);
			todo.setStatus(Status.needsAction());
			todo.setSummary("Submit Income Taxes");
			{
				Trigger trigger = new Trigger(utcFormatter.parse("19980403T120000"));
				Attachment attach = new Attachment("audio/basic", "http://example.com/pub/audio-files/ssbanner.aud");
				VAlarm alarm = VAlarm.audio(trigger, attach);
				alarm.setRepeat(4);
				alarm.setDuration(Duration.builder().hours(1).build());
				todo.addAlarm(alarm);
			}

			ical.addTodo(todo);
		}

		assertValidate(ical).run();
		assertExample(ical, "rfc5545-example4.ics");
	}

	@Test
	public void example5() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setVersion(Version.v2_0());
		ical.setProductId("-//ABC Corporation//NONSGML My Product//EN");
		{
			VJournal journal = new VJournal();
			journal.getProperties().clear();
			journal.setDateTimeStamp(utcFormatter.parse("19970324T120000"));
			journal.setUid("uid5@example.com");
			journal.setOrganizer("jsmith@example.com");
			journal.setStatus(Status.draft());
			journal.setClassification(Classification.public_());
			journal.addCategories("Project Report", "XYZ", "Weekly Meeting");
			journal.addDescription("Project xyz Review Meeting Minutes\nAgenda\n1. Review of project version 1.0 requirements.\n2.Definitionof project processes.\n3. Review of project schedule.\nParticipants: John Smith, Jane Doe, Jim Dandy\n-It wasdecided that the requirements need to be signed off byproduct marketing.\n-Project processes were accepted.\n-Project schedule needs to account for scheduled holidaysand employee vacation time. Check with HR for specificdates.\n-New schedule will be distributed by Friday.\n-Next weeks meeting is cancelled. No meeting until 3/23.");
			ical.addJournal(journal);
		}

		assertValidate(ical).run();
		assertExample(ical, "rfc5545-example5.ics");
	}

	@Test
	public void example6() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setVersion(Version.v2_0());
		ical.setProductId("-//RDU Software//NONSGML HandCal//EN");
		VFreeBusy freebusy = new VFreeBusy();
		{
			freebusy.getProperties().clear();
			freebusy.setOrganizer("jsmith@example.com");
			freebusy.setDateStart(utcFormatter.parse("19980313T141711"));
			freebusy.setDateEnd(utcFormatter.parse("19980410T141711"));

			FreeBusy fb = new FreeBusy();
			fb.addValue(utcFormatter.parse("19980314T233000"), utcFormatter.parse("19980315T003000"));
			freebusy.addFreeBusy(fb);

			fb = new FreeBusy();
			fb.addValue(utcFormatter.parse("19980316T153000"), utcFormatter.parse("19980316T163000"));
			freebusy.addFreeBusy(fb);

			fb = new FreeBusy();
			fb.addValue(utcFormatter.parse("19980318T030000"), utcFormatter.parse("19980318T040000"));
			freebusy.addFreeBusy(fb);

			freebusy.setUrl("http://www.example.com/calendar/busytime/jsmith.ifb");
			ical.addFreeBusy(freebusy);
		}

		assertValidate(ical).warn(freebusy, 2, 2).run(); //UID and DTSTAMP missing
		assertExample(ical, "rfc5545-example6.ics");
	}

	private void assertExample(ICalendar ical, String exampleFileName) throws IOException {
		StringWriter sw = new StringWriter();
		ICalWriter writer = new ICalWriter(sw, ICalVersion.V2_0, null);
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
		protected String _writeText(TestProperty property, ICalVersion version) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ICalVersion version, List<Warning> warnings) {
			return new TestProperty(value);
		}
	}

	private class BadNameMarshaller extends ICalPropertyScribe<TestProperty> {
		public BadNameMarshaller() {
			super(TestProperty.class, "BAD*NAME", null);
		}

		@Override
		protected String _writeText(TestProperty property, ICalVersion version) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ICalVersion version, List<Warning> warnings) {
			return new TestProperty(value);
		}
	}

	private class MyProdIdScribe extends ICalPropertyScribe<ProductId> {
		public MyProdIdScribe() {
			super(ProductId.class, "PRODID", ICalDataType.TEXT);
		}

		@Override
		protected String _writeText(ProductId property, ICalVersion version) {
			return property.getValue().toUpperCase();
		}

		@Override
		protected ProductId _parseText(String value, ICalDataType dataType, ICalParameters parameters, ICalVersion version, List<Warning> warnings) {
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
		protected String _writeText(TestProperty property, ICalVersion version) {
			return property.getValue();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ICalVersion version, List<Warning> warnings) {
			return new TestProperty(value);
		}
	}
}
