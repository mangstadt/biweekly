package biweekly.io.text;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertParseWarnings;
import static biweekly.util.TestUtils.assertSize;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.assertVersion;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TimeZone;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.component.StandardTime;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.component.VFreeBusy;
import biweekly.component.VJournal;
import biweekly.component.VTimezone;
import biweekly.component.VTodo;
import biweekly.io.ICalTimeZone;
import biweekly.io.ParseContext;
import biweekly.io.ParseWarning;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.CannotParseScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.SkipMeScribe;
import biweekly.parameter.CalendarUserType;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.ParticipationLevel;
import biweekly.parameter.ParticipationStatus;
import biweekly.parameter.Role;
import biweekly.property.Attachment;
import biweekly.property.Attendee;
import biweekly.property.Created;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.ICalProperty;
import biweekly.property.ProductId;
import biweekly.property.RawProperty;
import biweekly.property.RecurrenceRule;
import biweekly.property.Summary;
import biweekly.property.Version;
import biweekly.util.ByDay;
import biweekly.util.DateTimeComponents;
import biweekly.util.DayOfWeek;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.Duration;
import biweekly.util.Frequency;
import biweekly.util.ICalDate;
import biweekly.util.Period;
import biweekly.util.Recurrence;
import biweekly.util.UtcOffset;
import biweekly.util.Utf8Writer;

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
public class ICalReaderTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(-1, 0);

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void basic() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN\r\n" +
			"VERSION:2.0\r\n" +
			"X-TEST:a test\r\n" +
			"BEGIN:VEVENT\r\n" +
				"SUMMARY;LANGUAGE=en:Networld+Interop Conference\r\n" +
				"DESCRIPTION:Networld+Interop Conference\r\n" +
				" and Exhibit\\nAtlanta World Congress Center\\n\r\n" +
				" Atlanta\\, Georgia\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 1, 2);

		assertEquals("-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);
		assertEquals("a test", icalendar.getExperimentalProperty("X-TEST").getValue());

		{
			VEvent event = icalendar.getEvents().get(0);
			assertSize(event, 0, 2);

			assertEquals("Networld+Interop Conference", event.getSummary().getValue());
			assertEquals("en", event.getSummary().getLanguage());

			assertEquals("Networld+Interop Conferenceand Exhibit" + NEWLINE + "Atlanta World Congress Center" + NEWLINE + "Atlanta, Georgia", event.getDescription().getValue());
		}

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void read_multiple() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VEVENT\r\n" +
				"SUMMARY:event summary\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VTODO\r\n" +
				"SUMMARY:todo summary\r\n" +
			"END:VTODO\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);

		{
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 1, 1);

			assertEquals("prodid", icalendar.getProductId().getValue());
			assertVersion(V2_0, icalendar);

			VEvent event = icalendar.getEvents().get(0);
			assertSize(event, 0, 1);
			assertEquals("event summary", event.getSummary().getValue());

			assertParseWarnings(reader);
		}

		{
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 1, 1);

			assertEquals("prodid", icalendar.getProductId().getValue());
			assertVersion(V2_0, icalendar);

			VTodo todo = icalendar.getTodos().get(0);
			assertSize(todo, 0, 1);
			assertEquals("todo summary", todo.getSummary().getValue());

			assertParseWarnings(reader);
		}

		assertNull(reader.readNext());
	}

	@Test
	public void unrecognized_version_number() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0;3.0\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);

		assertEquals(new Version("2.0", "3.0"), icalendar.getProperty(Version.class));
		assertEquals(V2_0, icalendar.getVersion()); //default version

		assertParseWarnings(reader, (Integer) null);
		assertNull(reader.readNext());
	}

	@Test
	public void invalid_version_value() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:invalid\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);

		assertEquals("invalid", icalendar.getExperimentalProperty("VERSION").getValue());
		assertEquals(V2_0, icalendar.getVersion()); //default version

		assertParseWarnings(reader, null, 30);
		assertNull(reader.readNext());
	}

	@Test
	public void defaultVersion() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:value\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"PRODID:value\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"PRODID:value\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:value\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		{
			ICalReader reader = new ICalReader(ical);
			//default version defaults to 2.0

			ICalendar icalendar = reader.readNext();
			assertVersion(V2_0, icalendar);
			icalendar = reader.readNext();
			assertVersion(V1_0, icalendar);
			icalendar = reader.readNext();
			assertVersion(V2_0, icalendar);
			icalendar = reader.readNext();
			assertVersion(V2_0, icalendar);
			assertNull(reader.readNext());
		}

		{
			ICalReader reader = new ICalReader(ical, V1_0);

			ICalendar icalendar = reader.readNext();
			assertVersion(V1_0, icalendar);
			icalendar = reader.readNext();
			assertVersion(V1_0, icalendar);
			icalendar = reader.readNext();
			assertVersion(V2_0, icalendar);
			icalendar = reader.readNext();
			assertVersion(V1_0, icalendar);
			assertNull(reader.readNext());
		}
	}

	@Test
	public void caret_encoding_enabled() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"PRODID;X-TEST=^'test^':prodid\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.setCaretDecodingEnabled(true);
		reader.registerScribe(new TestPropertyMarshaller());

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertEquals("\"test\"", icalendar.getProductId().getParameter("X-TEST"));
		assertVersion(V2_0, icalendar);

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void caret_encoding_disabled() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"PRODID;X-TEST=^'test^':prodid\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.setCaretDecodingEnabled(false);
		reader.registerScribe(new TestPropertyMarshaller());

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertEquals("^'test^'", icalendar.getProductId().getParameter("X-TEST"));
		assertVersion(V2_0, icalendar);

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void missing_vcalendar_component_no_components() throws Throwable {
		//@formatter:off
		String ical =
		"PRODID:prodid\r\n" +
		"VERSION:2.0\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		assertNull(reader.readNext());
	}

	@Test
	public void missing_vcalendar_component_with_component() throws Throwable {
		//@formatter:off
		String ical =
		"PRODID:prodid\r\n" +
		"VERSION:2.0\r\n" +
		"BEGIN:VEVENT\r\n" +
			"SUMMARY:summary\r\n" +
		"END:VEVENT\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		assertNull(reader.readNext());
	}

	@Test
	public void vcalendar_component_not_the_first_line() throws Throwable {
		//@formatter:off
		String ical =
		"PRODID:prodid\r\n" +
		"VERSION:1.0\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid2\r\n" +
			"VERSION:2.0\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);

		assertEquals("prodid2", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);
	}

	@Test
	public void missing_end_property() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VEVENT\r\n" +
				"SUMMARY:summary\r\n" +
				"BEGIN:VTODO\r\n" +
					"SUMMARY:one\r\n" +
			  //"END:VTODO\r\n" + missing END property
			"END:VEVENT\r\n" +		
			"BEGIN:VTODO\r\n" +
				"SUMMARY:two\r\n" +
			"END:VTODO\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 2, 1);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);

		VEvent event = icalendar.getEvents().get(0);
		assertSize(event, 1, 1);
		assertEquals("summary", event.getSummary().getValue());

		VTodo todo = icalendar.getTodos().get(0);
		assertSize(todo, 0, 1);
		assertEquals("two", todo.getSummary().getValue());

		assertEquals("one", event.getComponent(VTodo.class).getSummary().getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void invalid_end_property() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VEVENT\r\n" +
				"SUMMARY:summary\r\n" +
				"END:FOOBAR\r\n" + //END property does not correspond to a BEGIN property
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 1, 1);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);

		VEvent event = icalendar.getEvents().get(0);
		assertSize(event, 0, 1);
		assertEquals("summary", event.getSummary().getValue());

		assertParseWarnings(reader, (Integer) null);
		assertNull(reader.readNext());
	}

	@Test
	public void experimental_property() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"X-TEST1:one\r\n" +
			"X-TEST1:one point five\r\n" +
			"BEGIN:VEVENT\r\n" +
				"SUMMARY:summary\r\n" +
				"X-TEST2:two\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 1, 3);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);
		assertEquals("one", icalendar.getExperimentalProperties("X-TEST1").get(0).getValue());
		assertEquals("one point five", icalendar.getExperimentalProperties("X-TEST1").get(1).getValue());

		VEvent event = icalendar.getEvents().get(0);
		assertSize(event, 0, 2);
		assertEquals("summary", event.getSummary().getValue());
		assertEquals("two", event.getExperimentalProperty("X-TEST2").getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void experiemental_property_marshaller() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"X-TEST:one\r\n" +
			"X-TEST:two\r\n" +
			"BEGIN:VEVENT\r\n" +
				"SUMMARY:summary\r\n" +
				"X-TEST:three\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new TestPropertyMarshaller());

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 1, 3);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);
		assertIntEquals(1, icalendar.getProperties(TestProperty.class).get(0).number);
		assertIntEquals(2, icalendar.getProperties(TestProperty.class).get(1).number);

		VEvent event = icalendar.getEvents().get(0);
		assertSize(event, 0, 2);
		assertEquals("summary", event.getSummary().getValue());
		assertIntEquals(3, event.getProperty(TestProperty.class).number);

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void experimental_component() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:X-VPARTY\r\n" +
				"X-DJ:Johnny D\r\n" +
			"END:X-VPARTY\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 1, 1);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);

		RawComponent component = icalendar.getExperimentalComponent("X-VPARTY");
		assertSize(component, 0, 1);
		assertEquals("Johnny D", component.getExperimentalProperty("X-DJ").getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void experiemental_component_marshaller() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:X-VPARTY\r\n" +
				"X-DJ:Johnny D\r\n" +
			"END:X-VPARTY\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new PartyMarshaller());
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 1, 1);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);

		Party component = icalendar.getComponent(Party.class);
		assertSize(component, 0, 1);
		assertEquals("Johnny D", component.getExperimentalProperty("X-DJ").getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void override_property_marshaller() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:the product id\r\n" +
			"VERSION:2.0\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new MyProductIdMarshaller());
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);

		assertEquals("THE PRODUCT ID", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void override_component_marshaller() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VEVENT\r\n" +
				"SUMMARY:event summary\r\n" +
			"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new MyEventMarshaller());

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 1, 1);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);

		MyVEvent event = icalendar.getComponent(MyVEvent.class);
		assertSize(event, 0, 1);
		assertEquals("event summary", event.getProperty(Summary.class).getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void invalid_line() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"bad-line\r\n" +
			"VERSION:2.0\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new MyEventMarshaller());

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);

		assertParseWarnings(reader, (Integer) null);
		assertNull(reader.readNext());
	}

	@Test
	public void property_warning() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"X-TEST:four\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new TestPropertyMarshaller());

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 2);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);
		assertIntEquals(4, icalendar.getProperty(TestProperty.class).number);

		assertParseWarnings(reader, (Integer) null);
		assertNull(reader.readNext());
	}

	@Test
	public void warnings_cleared_between_reads() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"X-TEST:four\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"X-TEST:four\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new TestPropertyMarshaller());

		{
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 2);

			assertEquals("prodid", icalendar.getProductId().getValue());
			assertVersion(V2_0, icalendar);
			assertIntEquals(4, icalendar.getProperty(TestProperty.class).number);

			assertParseWarnings(reader, (Integer) null);
		}

		{
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 2);

			assertEquals("prodid", icalendar.getProductId().getValue());
			assertVersion(V2_0, icalendar);
			assertIntEquals(4, icalendar.getProperty(TestProperty.class).number);

			assertParseWarnings(reader, (Integer) null);
		}

		assertNull(reader.readNext());
	}

	@Test
	public void skipMeException() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"SKIPME:value\r\n" +
			"X-FOO:bar\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new SkipMeScribe());

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);

		RawProperty property = icalendar.getExperimentalProperty("X-FOO");
		assertEquals(null, property.getDataType());
		assertEquals("X-FOO", property.getName());
		assertEquals("bar", property.getValue());

		assertParseWarnings(reader, 0);
		assertNull(reader.readNext());
	}

	@Test
	public void cannotParseException() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"CANNOTPARSE:value\r\n" +
			"X-FOO:bar\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new CannotParseScribe());

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 2);

		RawProperty property = icalendar.getExperimentalProperty("CANNOTPARSE");
		assertEquals(null, property.getDataType());
		assertEquals("CANNOTPARSE", property.getName());
		assertEquals("value", property.getValue());

		property = icalendar.getExperimentalProperty("X-FOO");
		assertEquals(null, property.getDataType());
		assertEquals("X-FOO", property.getName());
		assertEquals("bar", property.getValue());

		assertParseWarnings(reader, (Integer) null);
		assertNull(reader.readNext());
	}

	@Test
	public void valueless_parameter() throws Throwable {
		//1.0
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"PRODID;PARAM:value\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);

			assertEquals("value", icalendar.getProductId().getValue());
			assertEquals(Arrays.asList(), icalendar.getProductId().getParameters(null));
			assertEquals(Arrays.asList("PARAM"), icalendar.getProductId().getParameters("TYPE"));

			assertParseWarnings(reader);
			assertNull(reader.readNext());
		}

		//2.0
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"PRODID;PARAM:value\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);

			assertEquals("value", icalendar.getProductId().getValue());
			assertEquals(Arrays.asList(), icalendar.getProductId().getParameters(null));
			assertEquals(Arrays.asList("PARAM"), icalendar.getProductId().getParameters("TYPE"));

			assertParseWarnings(reader, 4);
			assertNull(reader.readNext());
		}
	}

	@Test
	public void data_types() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"PRODID:prodid\r\n" +
			"VERSION:2.0\r\n" +
			"X-TEST:one\r\n" +
			"X-TEST;VALUE=INTEGER:one\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		reader.registerScribe(new TestPropertyMarshaller());

		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 3);

		assertEquals("prodid", icalendar.getProductId().getValue());
		assertVersion(V2_0, icalendar);

		Iterator<TestProperty> it = icalendar.getProperties(TestProperty.class).iterator();

		TestProperty prop = it.next();
		assertEquals(ICalDataType.TEXT, prop.parsedDataType);
		assertNull(prop.getParameters().getValue());

		prop = it.next();
		assertEquals(ICalDataType.INTEGER, prop.parsedDataType);
		assertNull(prop.getParameters().getValue());

		assertFalse(it.hasNext());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void utf8() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"SUMMARY:\u1e66ummary\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on
		File file = tempFolder.newFile();
		Writer writer = new Utf8Writer(file);
		writer.write(ical);
		writer.close();

		ICalReader reader = new ICalReader(file);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);
		assertEquals("\u1e66ummary", icalendar.getProperty(Summary.class).getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	//see: http://stackoverflow.com/questions/33901/best-icalendar-library-for-java/17325369?noredirect=1#comment31110671_17325369
	@Test
	public void large_ical_file_stackoverflow_fix() throws Throwable {
		StringBuilder sb = new StringBuilder();
		sb.append("BEGIN:VCALENDAR\r\n");
		for (int i = 0; i < 100000; i++) {
			sb.append("BEGIN:VEVENT\r\nDESCRIPTION:test\r\n");
		}
		for (int i = 0; i < 100000; i++) {
			sb.append("END:VEVENT\r\n");
		}
		sb.append("END:VCALENDAR\r\n");

		ICalReader reader = new ICalReader(sb.toString());
		reader.readNext();
	}

	@Test
	public void vcal_AALARM_property() throws Throwable {
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"AALARM;VALUE=URL:20140101T010000;PT10M;5;http://example.com\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 1, 0);
			assertVersion(V1_0, icalendar);

			VAlarm valarm = icalendar.getComponent(VAlarm.class);
			assertSize(valarm, 0, 5);
			assertTrue(valarm.getAction().isAudio());
			assertEquals(date("2014-01-01 01:00:00"), valarm.getTrigger().getDate());
			assertEquals(new Duration.Builder().minutes(10).build(), valarm.getDuration().getValue());
			assertIntEquals(5, valarm.getRepeat().getValue());
			assertEquals("http://example.com", valarm.getAttachments().get(0).getUri());
		}

		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"AALARM;VALUE=URL:20140101T010000;PT10M;5;http://example.com\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);
			assertVersion(V2_0, icalendar);
			assertEquals("20140101T010000;PT10M;5;http://example.com", icalendar.getExperimentalProperty("AALARM").getValue());
		}
	}

	@Test
	public void vcal_DALARM_property() throws Throwable {
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"DALARM:20140101T010000;PT10M;5;display-text\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 1, 0);
			assertVersion(V1_0, icalendar);

			VAlarm valarm = icalendar.getComponent(VAlarm.class);
			assertSize(valarm, 0, 5);
			assertTrue(valarm.getAction().isDisplay());
			assertEquals(date("2014-01-01 01:00:00"), valarm.getTrigger().getDate());
			assertEquals(new Duration.Builder().minutes(10).build(), valarm.getDuration().getValue());
			assertIntEquals(5, valarm.getRepeat().getValue());
			assertEquals("display-text", valarm.getDescription().getValue());
		}

		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"DALARM:20140101T010000;PT10M;5;display-text\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);
			assertVersion(V2_0, icalendar);
			assertEquals("20140101T010000;PT10M;5;display-text", icalendar.getExperimentalProperty("DALARM").getValue());
		}
	}

	@Test
	public void vcal_MALARM_property() throws Throwable {
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"MALARM:20140101T010000;PT10M;5;jdoe@example.com;note\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 1, 0);
			assertVersion(V1_0, icalendar);

			VAlarm valarm = icalendar.getComponent(VAlarm.class);
			assertSize(valarm, 0, 6);
			assertTrue(valarm.getAction().isEmail());
			assertEquals(date("2014-01-01 01:00:00"), valarm.getTrigger().getDate());
			assertEquals(new Duration.Builder().minutes(10).build(), valarm.getDuration().getValue());
			assertIntEquals(5, valarm.getRepeat().getValue());
			assertEquals("note", valarm.getDescription().getValue());
			assertEquals("jdoe@example.com", valarm.getAttendees().get(0).getEmail());
		}

		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"MALARM:20140101T010000;PT10M;5;jdoe@example.com;note\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);
			assertVersion(V2_0, icalendar);
			assertEquals("20140101T010000;PT10M;5;jdoe@example.com;note", icalendar.getExperimentalProperty("MALARM").getValue());
		}
	}

	@Test
	public void vcal_PALARM_property() throws Throwable {
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"PALARM:20140101T010000;PT10M;5;file:///bin/ls\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 1, 0);
			assertVersion(V1_0, icalendar);

			VAlarm valarm = icalendar.getComponent(VAlarm.class);
			assertSize(valarm, 0, 5);
			assertTrue(valarm.getAction().isProcedure());
			assertEquals(date("2014-01-01 01:00:00"), valarm.getTrigger().getDate());
			assertEquals(new Duration.Builder().minutes(10).build(), valarm.getDuration().getValue());
			assertIntEquals(5, valarm.getRepeat().getValue());
			assertEquals("file:///bin/ls", valarm.getDescription().getValue());
		}

		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"PALARM:20140101T010000;PT10M;5;file:///bin/ls\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);
			assertVersion(V2_0, icalendar);
			assertEquals("20140101T010000;PT10M;5;file:///bin/ls", icalendar.getExperimentalProperty("PALARM").getValue());
		}
	}

	@Test
	public void vcal_rrule() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"RRULE:MD1 1 #1 D2  20000101T000000Z  M3\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 3);
		assertVersion(V1_0, icalendar);

		Iterator<RecurrenceRule> rrules = icalendar.getProperties(RecurrenceRule.class).iterator();
		Recurrence expected = new Recurrence.Builder(Frequency.MONTHLY).interval(1).byMonthDay(1).count(1).build();
		assertEquals(expected, rrules.next().getValue());
		expected = new Recurrence.Builder(Frequency.DAILY).interval(2).until(new ICalDate(utc("2000-01-01 00:00:00"))).build();
		assertEquals(expected, rrules.next().getValue());
		expected = new Recurrence.Builder(Frequency.MINUTELY).interval(3).count(2).build();
		assertEquals(expected, rrules.next().getValue());
		assertFalse(rrules.hasNext());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void vcal_DAYLIGHT_true() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DAYLIGHT:TRUE;-0400;20140309T020000;20141102T020000;EST;EDT\r\n" +
			"DTSTART:20140928T120000Z\r\n" +
			"DTSTART:20140308T120000\r\n" +
			"DTSTART:20140928T120000\r\n" +
			"DTSTART:20141103T120000\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 4);
		assertVersion(V1_0, icalendar);

		Iterator<DateStart> dtstart = icalendar.getProperties(DateStart.class).iterator();
		assertEquals(utc("2014-09-28 12:00:00"), dtstart.next().getValue());
		assertEquals(utc("2014-03-08 17:00:00"), dtstart.next().getValue());
		assertEquals(utc("2014-09-28 16:00:00"), dtstart.next().getValue());
		assertEquals(utc("2014-11-03 17:00:00"), dtstart.next().getValue());

		TimezoneInfo tzinfo = icalendar.getTimezoneInfo();
		assertEquals(1, tzinfo.getComponents().size());
		VTimezone timezone = tzinfo.getComponents().iterator().next();
		{
			assertSize(timezone, 2, 1);
			assertEquals("TZ", timezone.getTimezoneId().getValue());

			{
				StandardTime standard = timezone.getStandardTimes().get(0);
				assertSize(standard, 0, 4);

				assertEquals(new DateTimeComponents(2014, 11, 2, 2, 0, 0, false), standard.getDateStart().getValue().getRawComponents());
				assertEquals(new UtcOffset(false, 4, 0), standard.getTimezoneOffsetFrom().getValue());
				assertEquals(new UtcOffset(false, 5, 0), standard.getTimezoneOffsetTo().getValue());
				assertEquals("EST", standard.getTimezoneNames().get(0).getValue());
			}

			{
				DaylightSavingsTime daylight = timezone.getDaylightSavingsTime().get(0);
				assertSize(daylight, 0, 4);

				assertEquals(new DateTimeComponents(2014, 3, 9, 2, 0, 0, false), daylight.getDateStart().getValue().getRawComponents());
				assertEquals(new UtcOffset(false, 5, 0), daylight.getTimezoneOffsetFrom().getValue());
				assertEquals(new UtcOffset(false, 4, 0), daylight.getTimezoneOffsetTo().getValue());
				assertEquals("EDT", daylight.getTimezoneNames().get(0).getValue());
			}
		}

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void vcal_DAYLIGHT_false() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"DAYLIGHT:FALSE\r\n" +
			"DTSTART:20140928T120000Z\r\n" +
			"DTSTART:20140308T120000\r\n" +
			"DTSTART:20140928T120000\r\n" +
			"DTSTART:20141103T120000\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 4);
		assertVersion(V1_0, icalendar);

		Iterator<DateStart> dtstart = icalendar.getProperties(DateStart.class).iterator();
		assertEquals(utc("2014-09-28 12:00:00"), dtstart.next().getValue());
		assertEquals(date("2014-03-08 12:00:00"), dtstart.next().getValue());
		assertEquals(date("2014-09-28 12:00:00"), dtstart.next().getValue());
		assertEquals(date("2014-11-03 12:00:00"), dtstart.next().getValue());

		TimezoneInfo tzinfo = icalendar.getTimezoneInfo();
		assertEquals(0, tzinfo.getComponents().size());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void vcal_TZ_property() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"TZ:-0500\r\n" +
			"DTSTART:20140928T120000Z\r\n" +
			"DTSTART:20140308T120000\r\n" +
			"DTSTART:20140928T120000\r\n" +
			"DTSTART:20141103T120000\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalReader reader = new ICalReader(ical);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 4);
		assertVersion(V1_0, icalendar);

		Iterator<DateStart> dtstart = icalendar.getProperties(DateStart.class).iterator();
		assertEquals(utc("2014-09-28 12:00:00"), dtstart.next().getValue());
		assertEquals(utc("2014-03-08 17:00:00"), dtstart.next().getValue());
		assertEquals(utc("2014-09-28 17:00:00"), dtstart.next().getValue());
		assertEquals(utc("2014-11-03 17:00:00"), dtstart.next().getValue());

		TimezoneInfo tzinfo = icalendar.getTimezoneInfo();
		assertEquals(1, tzinfo.getComponents().size());
		VTimezone timezone = tzinfo.getComponents().iterator().next();
		{
			assertSize(timezone, 1, 1);
			assertEquals("TZ", timezone.getTimezoneId().getValue());

			{
				StandardTime standard = timezone.getStandardTimes().get(0);
				assertSize(standard, 0, 2);

				assertEquals(null, standard.getDateStart());
				assertEquals(new UtcOffset(false, 5, 0), standard.getTimezoneOffsetFrom().getValue());
				assertEquals(new UtcOffset(false, 5, 0), standard.getTimezoneOffsetTo().getValue());
				assertEquals(0, standard.getTimezoneNames().size());
			}
		}

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void vcal_DCREATED_property() throws Throwable {
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"DCREATED:20140101T010000\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);
			assertVersion(V1_0, icalendar);
			assertEquals(date("2014-01-01 01:00:00"), icalendar.getProperty(Created.class).getValue());

			assertParseWarnings(reader);
			assertNull(reader.readNext());
		}

		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"DCREATED:20140101T010000\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);
			assertVersion(V2_0, icalendar);
			assertEquals("20140101T010000", icalendar.getExperimentalProperty("DCREATED").getValue());

			assertParseWarnings(reader);
			assertNull(reader.readNext());
		}
	}

	@Test
	public void marshal_as_raw_when_version_does_not_match() throws Throwable {
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"DTSTAMP:20140101T010000Z\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);
			assertVersion(V1_0, icalendar);
			assertEquals("20140101T010000Z", icalendar.getExperimentalProperty("DTSTAMP").getValue());

			assertParseWarnings(reader);
			assertNull(reader.readNext());
		}

		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"DAYLIGHT:FALSE\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 0, 1);
			assertVersion(V2_0, icalendar);
			assertEquals("FALSE", icalendar.getExperimentalProperty("DAYLIGHT").getValue());

			assertParseWarnings(reader);
			assertNull(reader.readNext());
		}

		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"BEGIN:VJOURNAL\r\n" +
				"END:VJOURNAL\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on

			ICalReader reader = new ICalReader(ical);
			ICalendar icalendar = reader.readNext();
			assertSize(icalendar, 1, 0);
			assertVersion(V1_0, icalendar);
			assertNotNull(icalendar.getExperimentalComponent("VJOURNAL"));

			assertParseWarnings(reader);
			assertNull(reader.readNext());
		}
	}

	@Test
	public void outlook2010() throws Throwable {
		ICalReader reader = read("outlook-2010.ics");
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 3);

		assertEquals("-//Microsoft Corporation//Outlook 14.0 MIMEDIR//EN", ical.getProductId().getValue());
		assertVersion(V2_0, ical);
		assertEquals("REQUEST", ical.getMethod().getValue());
		assertEquals("TRUE", ical.getExperimentalProperty("X-MS-OLK-FORCEINSPECTOROPEN").getValue());

		{
			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 24);

			Attendee attendee = event.getAttendees().get(0);
			assertEquals("Doe, John", attendee.getCommonName());
			assertEquals(ParticipationLevel.OPTIONAL, attendee.getParticipationLevel());
			assertEquals(Boolean.FALSE, attendee.getRsvp());
			assertEquals("johndoe@example.com", attendee.getEmail());

			attendee = event.getAttendees().get(1);
			assertEquals("Doe, Jane", attendee.getCommonName());
			assertEquals(Role.CHAIR, attendee.getRole());
			assertEquals(Boolean.TRUE, attendee.getRsvp());
			assertEquals("janedoe@example.com", attendee.getEmail());

			assertEquals("PUBLIC", event.getClassification().getValue());
			assertEquals(utc("2013-06-08 20:04:10"), event.getCreated().getValue());
			assertEquals("Meeting will discuss objectives for next project." + NEWLINE + "Will include a presentation and food.", event.getDescription().getValue());

			assertEquals(utc("2013-06-10 17:00:00"), event.getDateEnd().getValue());
			assertNull(event.getDateEnd().getParameters().getTimezoneId());

			assertEquals(utc("2013-04-25 15:58:07"), event.getDateTimeStamp().getValue());

			assertEquals(utc("2013-06-10 16:00:00"), event.getDateStart().getValue());
			assertNull(event.getDateStart().getParameters().getTimezoneId());

			assertEquals(utc("2013-06-08 20:04:10"), event.getLastModified().getValue());

			assertEquals("Auditorium 16", event.getLocation().getValue());

			assertEquals("bobsmith@example.com", event.getOrganizer().getEmail());
			assertEquals("Smith, Bob", event.getOrganizer().getCommonName());

			assertIntEquals(5, event.getPriority().getValue());
			assertIntEquals(1, event.getSequence().getValue());

			assertEquals("Team Meeting", event.getSummary().getValue());
			assertEquals("en-us", event.getSummary().getLanguage());

			assertEquals(true, event.getTransparency().isOpaque());
			assertEquals("040000009200E00074C5B7101A82E00800000000C0383BE68041CE0100000000000000001000000070D00A2F625AC34BB6542DE0D19E67E1", event.getUid().getValue());
			//@formatter:off
			assertEquals(
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2//EN\">\\n" +
			"<HTML>\\n" +
			"<HEAD>\\n" +
			"<META NAME=\"Generator\" CONTENT=\"MS Exchange Server version 14.02.5004.000\">\\n" +
			"<TITLE></TITLE>\\n" +
			"</HEAD>\\n" +
			"<BODY>\\n" +
			"<!-- Converted from text/rtf format -->\\n" +
			"\\n" +
			"<P DIR=LTR><SPAN LANG=\"en-us\"><B><FONT COLOR=\"#0000FF\" FACE=\"Arial\">Meeting will discuss objectives for next project.\\n" +
			"Will include a presentation and food.</FONT></B></SPAN></P></BODY>\\n" +
			"</HTML>",
			event.getExperimentalProperty("X-ALT-DESC").getValue());
			//@formatter:on
			assertEquals("text/html", event.getExperimentalProperty("X-ALT-DESC").getParameter("FMTTYPE"));

			assertEquals("TENTATIVE", event.getExperimentalProperty("X-MICROSOFT-CDO-BUSYSTATUS").getValue());
			assertEquals("1", event.getExperimentalProperty("X-MICROSOFT-CDO-IMPORTANCE").getValue());
			assertEquals("BUSY", event.getExperimentalProperty("X-MICROSOFT-CDO-INTENDEDSTATUS").getValue());
			assertEquals("TRUE", event.getExperimentalProperty("X-MICROSOFT-DISALLOW-COUNTER").getValue());
			assertEquals("1", event.getExperimentalProperty("X-MS-OLK-APPTLASTSEQUENCE").getValue());
			assertEquals("20130425T124303Z", event.getExperimentalProperty("X-MS-OLK-APPTSEQTIME").getValue());
			assertEquals("0", event.getExperimentalProperty("X-MS-OLK-CONFTYPE").getValue());
		}

		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		{
			Iterator<VTimezone> it = tzinfo.getComponents().iterator();

			VTimezone timezone = it.next();
			assertSize(timezone, 2, 1);

			assertEquals("Eastern Standard Time", timezone.getTimezoneId().getValue());
			{
				StandardTime standard = timezone.getStandardTimes().get(0);
				assertSize(standard, 0, 4);

				assertEquals(date("1601-11-04 02:00:00"), standard.getDateStart().getValue());

				Recurrence rrule = standard.getRecurrenceRule().getValue();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(new ByDay(1, DayOfWeek.SUNDAY)), rrule.getByDay());
				assertEquals(Arrays.asList(11), rrule.getByMonth());

				assertEquals(new UtcOffset(false, 4, 0), standard.getTimezoneOffsetFrom().getValue());
				assertEquals(new UtcOffset(false, 5, 0), standard.getTimezoneOffsetTo().getValue());
			}
			{
				DaylightSavingsTime daylight = timezone.getDaylightSavingsTime().get(0);
				assertSize(daylight, 0, 4);

				assertEquals(date("1601-03-11 02:00:00"), daylight.getDateStart().getValue());

				Recurrence rrule = daylight.getRecurrenceRule().getValue();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(new ByDay(2, DayOfWeek.SUNDAY)), rrule.getByDay());
				assertEquals(Arrays.asList(3), rrule.getByMonth());

				assertEquals(new UtcOffset(false, 5, 0), daylight.getTimezoneOffsetFrom().getValue());
				assertEquals(new UtcOffset(false, 4, 0), daylight.getTimezoneOffsetTo().getValue());
			}

			assertFalse(it.hasNext());
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();

		VTimezone timezone = tzinfo.getComponents().iterator().next();
		VEvent event = ical.getEvents().get(0);

		DateStart dtstart = event.getDateStart();
		assertEquals(timezone, tzinfo.getTimezone(dtstart).getComponent());
		TimeZone dtstartTz = tzinfo.getTimezone(dtstart).getTimeZone();
		assertTrue(dtstartTz instanceof ICalTimeZone);

		DateEnd dtend = event.getDateEnd();
		assertEquals(timezone, tzinfo.getTimezone(dtend).getComponent());
		assertEquals(dtstartTz, tzinfo.getTimezone(dtend).getTimeZone());

		assertNull(reader.readNext());
	}

	@Test
	public void example1() throws Throwable {
		ICalReader reader = read("rfc5545-example1.ics");
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 1);

		assertEquals("-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN", ical.getProductId().getValue());
		assertVersion(V2_0, ical);

		{
			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 9);

			assertEquals(utc("1996-07-04 12:00:00"), event.getDateTimeStamp().getValue());
			assertEquals("uid1@example.com", event.getUid().getValue());
			assertEquals("jsmith@example.com", event.getOrganizer().getEmail());
			assertEquals(utc("1996-09-18 14:30:00"), event.getDateStart().getValue());
			assertEquals(utc("1996-09-20 22:00:00"), event.getDateEnd().getValue());
			assertTrue(event.getStatus().isConfirmed());
			assertEquals(Arrays.asList("CONFERENCE"), event.getCategories().get(0).getValues());
			assertEquals("Networld+Interop Conference", event.getSummary().getValue());
			assertEquals("Networld+Interop Conferenceand Exhibit\nAtlanta World Congress Center\nAtlanta, Georgia", event.getDescription().getValue());
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();

		assertNull(reader.readNext());
	}

	@Test
	public void example2() throws Throwable {
		ICalReader reader = read("rfc5545-example2.ics");
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 1);

		assertEquals("-//RDU Software//NONSGML HandCal//EN", ical.getProductId().getValue());
		assertVersion(V2_0, ical);
		{
			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 12);

			assertEquals(utc("1998-03-09 23:10:00"), event.getDateTimeStamp().getValue());
			assertEquals("guid-1.example.com", event.getUid().getValue());
			assertEquals("mrbig@example.com", event.getOrganizer().getEmail());

			Attendee attendee = event.getAttendees().get(0);
			assertEquals("employee-A@example.com", attendee.getEmail());
			assertTrue(attendee.getRsvp());
			assertEquals(ParticipationLevel.REQUIRED, attendee.getParticipationLevel());
			assertEquals(CalendarUserType.GROUP, attendee.getCalendarUserType());

			assertEquals("Project XYZ Review Meeting", event.getDescription().getValue());
			assertEquals(Arrays.asList("MEETING"), event.getCategories().get(0).getValues());
			assertTrue(event.getClassification().isPublic());
			assertEquals(utc("1998-03-09 13:00:00"), event.getCreated().getValue());
			assertEquals("XYZ Project Review", event.getSummary().getValue());

			assertEquals(utc("1998-03-12 13:30:00"), event.getDateStart().getValue());
			assertNull(event.getDateStart().getParameters().getTimezoneId());

			assertEquals(utc("1998-03-12 14:30:00"), event.getDateEnd().getValue());
			assertNull(event.getDateEnd().getParameters().getTimezoneId());

			assertEquals("1CP Conference Room 4350", event.getLocation().getValue());
		}

		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		{
			Iterator<VTimezone> it = tzinfo.getComponents().iterator();

			VTimezone timezone = it.next();
			assertSize(timezone, 2, 1);

			assertEquals("America/New_York", timezone.getTimezoneId().getValue());

			{
				StandardTime standard = timezone.getStandardTimes().get(0);
				assertSize(standard, 0, 4);

				assertEquals(date("1998-10-25 02:00:00"), standard.getDateStart().getValue());
				assertEquals(new DateTimeComponents(1998, 10, 25, 2, 0, 0, false), standard.getDateStart().getValue().getRawComponents());

				assertEquals(new UtcOffset(false, 4, 0), standard.getTimezoneOffsetFrom().getValue());
				assertEquals(new UtcOffset(false, 5, 0), standard.getTimezoneOffsetTo().getValue());

				assertEquals("EST", standard.getTimezoneNames().get(0).getValue());
			}
			{
				DaylightSavingsTime daylight = timezone.getDaylightSavingsTime().get(0);
				assertSize(daylight, 0, 4);

				assertEquals(date("1999-04-04 02:00:00"), daylight.getDateStart().getValue());
				assertEquals(new DateTimeComponents(1999, 04, 04, 2, 0, 0, false), daylight.getDateStart().getValue().getRawComponents());

				assertEquals(new UtcOffset(false, 5, 0), daylight.getTimezoneOffsetFrom().getValue());
				assertEquals(new UtcOffset(false, 4, 0), daylight.getTimezoneOffsetTo().getValue());

				assertEquals("EDT", daylight.getTimezoneNames().get(0).getValue());
			}

			assertFalse(it.hasNext());
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();

		VTimezone timezone = tzinfo.getComponents().iterator().next();
		VEvent event = ical.getEvents().get(0);

		DateStart dtstart = event.getDateStart();
		assertEquals(timezone, tzinfo.getTimezone(dtstart).getComponent());
		TimeZone dtstartTz = tzinfo.getTimezone(dtstart).getTimeZone();
		assertEquals("America/New_York", dtstartTz.getID());
		assertTrue(dtstartTz instanceof ICalTimeZone);

		DateEnd dtend = event.getDateEnd();
		assertEquals(timezone, tzinfo.getTimezone(dtend).getComponent());
		assertEquals(dtstartTz, tzinfo.getTimezone(dtend).getTimeZone());

		assertNull(reader.readNext());
	}

	@Test
	public void example3() throws Throwable {
		ICalReader reader = read("rfc5545-example3.ics");
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 2);

		assertEquals("xyz", ical.getMethod().getValue());
		assertEquals("-//ABC Corporation//NONSGML My Product//EN", ical.getProductId().getValue());
		assertVersion(V2_0, ical);

		{
			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 13);

			assertEquals(utc("1997-03-24 12:00:00"), event.getDateTimeStamp().getValue());
			assertIntEquals(0, event.getSequence().getValue());
			assertEquals("uid3@example.com", event.getUid().getValue());
			assertEquals("jdoe@example.com", event.getOrganizer().getEmail());
			assertEquals(utc("1997-03-24 12:30:00"), event.getDateStart().getValue());
			assertEquals(utc("1997-03-24 21:00:00"), event.getDateEnd().getValue());
			assertEquals(Arrays.asList("MEETING", "PROJECT"), event.getCategories().get(0).getValues());
			assertTrue(event.getClassification().isPublic());
			assertEquals("Calendaring Interoperability Planning Meeting", event.getSummary().getValue());
			assertEquals("Discuss how we can test c&s interoperability" + NEWLINE + "using iCalendar and other IETF standards.", event.getDescription().getValue());
			assertEquals("LDB Lobby", event.getLocation().getValue());

			Attachment attach = event.getAttachments().get(0);
			assertEquals("ftp://example.com/pub/conf/bkgrnd.ps", attach.getUri());
			assertEquals("application/postscript", attach.getFormatType());
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();

		assertNull(reader.readNext());
	}

	@Test
	public void example4() throws Throwable {
		ICalReader reader = read("rfc5545-example4.ics");
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 1);

		assertVersion(V2_0, ical);
		assertEquals("-//ABC Corporation//NONSGML My Product//EN", ical.getProductId().getValue());

		{
			VTodo todo = ical.getTodos().get(0);
			assertSize(todo, 1, 8);

			assertEquals(utc("1998-01-30 13:45:00"), todo.getDateTimeStamp().getValue());
			assertIntEquals(2, todo.getSequence().getValue());
			assertEquals("uid4@example.com", todo.getUid().getValue());
			assertEquals("unclesam@example.com", todo.getOrganizer().getEmail());

			Attendee attendee = todo.getAttendees().get(0);
			assertEquals("jqpublic@example.com", attendee.getEmail());
			assertEquals(ParticipationStatus.ACCEPTED, attendee.getParticipationStatus());

			assertEquals(date("1998-04-15"), todo.getDateDue().getValue());
			assertTrue(todo.getStatus().isNeedsAction());
			assertEquals("Submit Income Taxes", todo.getSummary().getValue());

			{
				VAlarm alarm = todo.getAlarms().get(0);
				assertSize(alarm, 0, 5);

				assertTrue(alarm.getAction().isAudio());
				assertEquals(utc("1998-04-03 12:00:00"), alarm.getTrigger().getDate());

				Attachment attach = alarm.getAttachments().get(0);
				assertEquals("http://example.com/pub/audio-files/ssbanner.aud", attach.getUri());
				assertEquals("audio/basic", attach.getFormatType());

				assertIntEquals(4, alarm.getRepeat().getValue());
				assertEquals(Duration.builder().hours(1).build(), alarm.getDuration().getValue());
			}
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();

		assertNull(reader.readNext());
	}

	@Test
	public void example5() throws Throwable {
		ICalReader reader = read("rfc5545-example5.ics");
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 1);

		assertVersion(V2_0, ical);
		assertEquals("-//ABC Corporation//NONSGML My Product//EN", ical.getProductId().getValue());

		{
			VJournal journal = ical.getJournals().get(0);
			assertSize(journal, 0, 7);

			assertEquals(utc("1997-03-24 12:00:00"), journal.getDateTimeStamp().getValue());
			assertEquals("uid5@example.com", journal.getUid().getValue());
			assertEquals("jsmith@example.com", journal.getOrganizer().getEmail());
			assertTrue(journal.getStatus().isDraft());
			assertTrue(journal.getClassification().isPublic());
			assertEquals(Arrays.asList("Project Report", "XYZ", "Weekly Meeting"), journal.getCategories().get(0).getValues());
			assertEquals("Project xyz Review Meeting Minutes" + NEWLINE + "Agenda" + NEWLINE + "1. Review of project version 1.0 requirements." + NEWLINE + "2.Definitionof project processes." + NEWLINE + "3. Review of project schedule." + NEWLINE + "Participants: John Smith, Jane Doe, Jim Dandy" + NEWLINE + "-It wasdecided that the requirements need to be signed off byproduct marketing." + NEWLINE + "-Project processes were accepted." + NEWLINE + "-Project schedule needs to account for scheduled holidaysand employee vacation time. Check with HR for specificdates." + NEWLINE + "-New schedule will be distributed by Friday." + NEWLINE + "-Next weeks meeting is cancelled. No meeting until 3/23.", journal.getDescriptions().get(0).getValue());
		}

		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();

		assertNull(reader.readNext());
	}

	@Test
	public void example6() throws Throwable {
		ICalReader reader = read("rfc5545-example6.ics");
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 1);

		assertVersion(V2_0, ical);
		assertEquals("-//RDU Software//NONSGML HandCal//EN", ical.getProductId().getValue());

		VFreeBusy freebusy = ical.getFreeBusies().get(0);
		{
			assertSize(freebusy, 0, 7);

			assertEquals("jsmith@example.com", freebusy.getOrganizer().getEmail());
			assertEquals(utc("1998-03-13 14:17:11"), freebusy.getDateStart().getValue());
			assertEquals(utc("1998-04-10 14:17:11"), freebusy.getDateEnd().getValue());
			assertEquals(Arrays.asList(new Period(utc("1998-03-14 23:30:00"), utc("1998-03-15 00:30:00"))), freebusy.getFreeBusy().get(0).getValues());
			assertEquals(Arrays.asList(new Period(utc("1998-03-16 15:30:00"), utc("1998-03-16 16:30:00"))), freebusy.getFreeBusy().get(1).getValues());
			assertEquals(Arrays.asList(new Period(utc("1998-03-18 03:00:00"), utc("1998-03-18 04:00:00"))), freebusy.getFreeBusy().get(2).getValues());
			assertEquals("http://www.example.com/calendar/busytime/jsmith.ifb", freebusy.getUrl().getValue());
		}

		//UID and DTSTAMP are missing from VFREEBUSY
		assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).warn(freebusy, 2, 2).run();

		assertNull(reader.readNext());
	}

	private ICalReader read(String file) {
		return new ICalReader(getClass().getResourceAsStream(file));
	}

	private class TestPropertyMarshaller extends ICalPropertyScribe<TestProperty> {
		public TestPropertyMarshaller() {
			super(TestProperty.class, "X-TEST", ICalDataType.TEXT);
		}

		@Override
		protected String _writeText(TestProperty property, WriteContext context) {
			return property.number.toString();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			TestProperty prop = new TestProperty();
			Integer number = null;
			if (value.equals("one")) {
				number = 1;
			} else if (value.equals("two")) {
				number = 2;
			} else if (value.equals("three")) {
				number = 3;
			} else if (value.equals("four")) {
				number = 4;
				//@formatter:off
				context.getWarnings().add(new ParseWarning.Builder(context)
					.message("too high")
					.build()
				);
				//@formatter:on
			}
			prop.number = number;
			prop.parsedDataType = dataType;
			return prop;
		}
	}

	private class TestProperty extends ICalProperty {
		private Integer number;
		private ICalDataType parsedDataType;
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

	private class MyProductIdMarshaller extends ICalPropertyScribe<ProductId> {
		public MyProductIdMarshaller() {
			super(ProductId.class, "PRODID", ICalDataType.TEXT);
		}

		@Override
		protected String _writeText(ProductId property, WriteContext context) {
			return property.getValue();
		}

		@Override
		protected ProductId _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return new ProductId(value.toUpperCase());
		}
	}

	private class MyVEvent extends ICalComponent {
		//empty
	}

	private class MyEventMarshaller extends ICalComponentScribe<MyVEvent> {
		public MyEventMarshaller() {
			super(MyVEvent.class, "VEVENT");
		}

		@Override
		protected MyVEvent _newInstance() {
			return new MyVEvent();
		}
	}
}
