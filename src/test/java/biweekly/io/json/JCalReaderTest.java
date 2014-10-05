package biweekly.io.json;

import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertSize;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TimeZone;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.component.StandardTime;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.io.ICalTimeZone;
import biweekly.io.ParseContext;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.CannotParseScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.SkipMeScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.DateStart;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceId;
import biweekly.property.Summary;
import biweekly.util.DateTimeComponents;
import biweekly.util.Duration;
import biweekly.util.IOUtils;
import biweekly.util.Period;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.ByDay;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
@SuppressWarnings("resource")
public class JCalReaderTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void read_single() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"prodid\", {}, \"text\", \"-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN\"]," +
				"[\"version\", {}, \"text\", \"2.0\"]" +
			"]," +
			"[" +
				"[\"vevent\"," +
					"[" +
						"[\"summary\", {}, \"text\", \"Networld+Interop Conference\"]," +
						"[\"description\", {}, \"text\", \"Networld+Interop Conference\\nand Exhibit\\nAtlanta World Congress Center\\nAtlanta, Georgia\"]" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 1);

		assertEquals("-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN", ical.getProductId().getValue());
		assertEquals(ICalVersion.V2_0, ical.getVersion());

		VEvent event = ical.getEvents().get(0);
		assertSize(event, 0, 2);

		assertEquals("Networld+Interop Conference", event.getSummary().getValue());
		assertEquals("Networld+Interop Conference" + NEWLINE + "and Exhibit" + NEWLINE + "Atlanta World Congress Center" + NEWLINE + "Atlanta, Georgia", event.getDescription().getValue());

		assertNull(reader.readNext());
		assertWarnings(0, reader);
	}

	@Test
	public void read_multiple() throws Throwable {
		//@formatter:off
		String json =
		"[" +
			"[\"vcalendar\"," +
				"[" +
					"[\"prodid\", {}, \"text\", \"prodid1\"]," +
					"[\"version\", {}, \"text\", \"2.0\"]" +
				"]," +
				"[" +
					"[\"vevent\"," +
						"[" +
							"[\"summary\", {}, \"text\", \"summary1\"]," +
							"[\"description\", {}, \"text\", \"description1\"]" +
						"]," +
						"[" +
						"]" +
					"]" +
				"]" +
			"]," +
			"[\"vcalendar\"," +
				"[" +
					"[\"prodid\", {}, \"text\", \"prodid2\"]," +
					"[\"version\", {}, \"text\", \"2.0\"]" +
				"]," +
				"[" +
					"[\"vevent\"," +
						"[" +
							"[\"summary\", {}, \"text\", \"summary2\"]," +
							"[\"description\", {}, \"text\", \"description2\"]" +
						"]," +
						"[" +
						"]" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			assertEquals("prodid1", ical.getProductId().getValue());
			assertEquals(ICalVersion.V2_0, ical.getVersion());

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 2);
			assertEquals("summary1", event.getSummary().getValue());
			assertEquals("description1", event.getDescription().getValue());

			assertWarnings(0, reader);
		}

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			assertEquals("prodid2", ical.getProductId().getValue());
			assertEquals(ICalVersion.V2_0, ical.getVersion());

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 2);
			assertEquals("summary2", event.getSummary().getValue());
			assertEquals("description2", event.getDescription().getValue());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
	}

	@Test
	public void no_properties() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
			"]," +
			"[" +
				"[\"vevent\"," +
					"[" +
						"[\"summary\", {}, \"text\", \"summary\"]" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 0);

		VEvent event = ical.getEvents().get(0);
		assertSize(event, 0, 1);
		assertEquals("summary", event.getSummary().getValue());

		assertWarnings(0, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void no_components() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"prodid\", {}, \"text\", \"prodid\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		assertEquals("prodid", ical.getProductId().getValue());

		assertWarnings(0, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void no_properties_or_components() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 0);

		assertWarnings(0, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void experimental_component() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
			"]," +
			"[" +
				"[\"x-party\"," +
					"[" +
						"[\"summary\", {}, \"text\", \"summary\"]" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 0);

		RawComponent party = ical.getExperimentalComponent("x-party");
		assertSize(party, 0, 1);
		assertEquals("summary", party.getProperty(Summary.class).getValue());

		assertWarnings(0, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void experimental_component_registered() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
			"]," +
			"[" +
				"[\"x-party\"," +
					"[" +
						"[\"summary\", {}, \"text\", \"summary\"]" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		reader.registerScribe(new PartyMarshaller());
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 0);

		Party party = ical.getComponent(Party.class);
		assertSize(party, 0, 1);
		assertEquals("summary", party.getProperty(Summary.class).getValue());

		assertWarnings(0, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void experimental_property() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"x-company\", {}, \"text\", \"value\"]," +
				"[\"x-company2\", {}, \"unknown\", \"value\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 2);

		RawProperty company = ical.getExperimentalProperty("x-company");
		assertEquals(ICalDataType.TEXT, company.getDataType());
		assertEquals("value", company.getValue());

		company = ical.getExperimentalProperty("x-company2");
		assertNull(company.getDataType());
		assertEquals("value", company.getValue());

		assertWarnings(0, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void experimental_property_registered() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"x-company\", {}, \"text\", \"value\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		reader.registerScribe(new CompanyMarshaller());
		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		Company company = ical.getProperty(Company.class);
		assertEquals("value", company.getBoss());

		assertWarnings(0, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void skipMeException() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"skipme\", {}, \"text\", \"value\"]," +
				"[\"x-foo\", {}, \"text\", \"bar\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		reader.registerScribe(new SkipMeScribe());
		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		RawProperty property = ical.getExperimentalProperty("x-foo");
		assertEquals(ICalDataType.TEXT, property.getDataType());
		assertEquals("X-FOO", property.getName());
		assertEquals("bar", property.getValue());

		assertWarnings(1, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void cannotParseException() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"cannotparse\", {}, \"text\", \"value\"]," +
				"[\"x-foo\", {}, \"text\", \"bar\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		reader.registerScribe(new CannotParseScribe());
		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 2);

		RawProperty property = ical.getExperimentalProperty("cannotparse");
		assertEquals(ICalDataType.TEXT, property.getDataType());
		assertEquals("cannotparse", property.getName());
		assertEquals("value", property.getValue());

		property = ical.getExperimentalProperty("x-foo");
		assertEquals(ICalDataType.TEXT, property.getDataType());
		assertEquals("X-FOO", property.getName());
		assertEquals("bar", property.getValue());

		assertWarnings(1, reader);

		assertNull(reader.readNext());
	}

	@Test
	public void empty() throws Throwable {
		//@formatter:off
		String json =
		"";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		assertNull(reader.readNext());
	}

	@Test
	public void utf8() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"summary\", {}, \"text\", \"\u1e66ummary\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		File file = tempFolder.newFile();
		Writer writer = IOUtils.utf8Writer(file);
		writer.write(json);
		writer.close();

		JCalReader reader = new JCalReader(file);
		ICalendar icalendar = reader.readNext();
		assertSize(icalendar, 0, 1);
		assertEquals("\u1e66ummary", icalendar.getProperty(Summary.class).getValue());

		assertWarnings(0, reader);
		assertNull(reader.readNext());
	}

	@Test
	public void jcal_draft_example1() throws Throwable {
		JCalReader reader = read("jcal-draft-example1.json");
		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 2);

		assertEquals("-//Example Inc.//Example Calendar//EN", ical.getProductId().getValue());
		assertEquals(ICalVersion.V2_0, ical.getVersion());
		assertTrue(ical.getCalendarScale().isGregorian());

		{
			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 4);

			assertEquals(utc("2008-02-05 19:12:24"), event.getDateTimeStamp().getValue());
			assertEquals(date("2008-10-06"), event.getDateStart().getValue());
			assertFalse(event.getDateStart().hasTime());
			assertEquals("Planning meeting", event.getSummary().getValue());
			assertEquals("4088E990AD89CB3DBB484909", event.getUid().getValue());
		}

		assertValidate(ical).versions(ICalVersion.V2_0).run();

		assertNull(reader.readNext());
	}

	@Test
	public void jcal_draft_example2() throws Throwable {
		JCalReader reader = read("jcal-draft-example2.json");
		ICalendar ical = reader.readNext();
		assertSize(ical, 2, 1);

		assertEquals("-//Example Corp.//Example Client//EN", ical.getProductId().getValue());
		assertEquals(ICalVersion.V2_0, ical.getVersion());

		{
			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 8);

			assertEquals(utc("2006-02-06 00:11:21"), event.getDateTimeStamp().getValue());
			assertEquals(utc("2006-01-02 17:00:00"), event.getDateStart().getValue());
			assertNull(event.getDateStart().getParameters().getTimezoneId());
			assertEquals(Duration.builder().hours(1).build(), event.getDuration().getValue());

			Recurrence rrule = event.getRecurrenceRule().getValue();
			assertEquals(Frequency.DAILY, rrule.getFrequency());
			assertIntEquals(5, rrule.getCount());

			RecurrenceDates rdate = event.getRecurrenceDates().get(0);
			assertNull(rdate.getDates());
			assertEquals(1, rdate.getPeriods().size());
			assertEquals(new Period(utc("2006-01-02 20:00:00"), Duration.builder().hours(2).build()), rdate.getPeriods().get(0));
			assertNull(rdate.getParameters().getTimezoneId());

			assertEquals("Event #2", event.getSummary().getValue());
			assertEquals("We are having a meeting all this week at 12 pm for one hour, with an additional meeting on the first day 2 hours long." + NEWLINE + "Please bring your own lunch for the 12 pm meetings.", event.getDescription().getValue());
			assertEquals("00959BC664CA650E933C892C@example.com", event.getUid().getValue());
		}
		{
			VEvent event = ical.getEvents().get(1);
			assertSize(event, 0, 6);

			assertEquals(utc("2006-02-06 00:11:21"), event.getDateTimeStamp().getValue());
			assertEquals(utc("2006-01-02 19:00:00"), event.getDateStart().getValue());
			assertNull(event.getDateStart().getParameters().getTimezoneId());
			assertEquals(Duration.builder().hours(1).build(), event.getDuration().getValue());

			assertEquals(utc("2006-01-04 17:00:00"), event.getRecurrenceId().getValue());
			assertNull(event.getRecurrenceId().getParameters().getTimezoneId());
			assertEquals("Event #2", event.getSummary().getValue());
			assertEquals("00959BC664CA650E933C892C@example.com", event.getUid().getValue());
		}

		TimezoneInfo tzinfo = reader.getTimezoneInfo();
		{
			Iterator<VTimezone> it = tzinfo.getComponents().iterator();

			VTimezone timezone = it.next();
			assertSize(timezone, 2, 2);

			assertEquals(utc("2004-01-10 03:28:45"), timezone.getLastModified().getValue());
			assertEquals("US/Eastern", timezone.getTimezoneId().getValue());

			{
				DaylightSavingsTime daylight = timezone.getDaylightSavingsTime().get(0);
				assertSize(daylight, 0, 5);

				assertEquals(date("2000-04-04 02:00:00"), daylight.getDateStart().getValue());
				assertEquals(new DateTimeComponents(2000, 4, 4, 2, 0, 0, false), daylight.getDateStart().getRawComponents());

				Recurrence rrule = daylight.getRecurrenceRule().getValue();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(new ByDay(1, DayOfWeek.SUNDAY)), rrule.getByDay());
				assertEquals(Arrays.asList(4), rrule.getByMonth());

				assertEquals("EDT", daylight.getTimezoneNames().get(0).getValue());
				assertIntEquals(-5, daylight.getTimezoneOffsetFrom().getHourOffset());
				assertIntEquals(0, daylight.getTimezoneOffsetFrom().getMinuteOffset());

				assertIntEquals(-4, daylight.getTimezoneOffsetTo().getHourOffset());
				assertIntEquals(0, daylight.getTimezoneOffsetTo().getMinuteOffset());
			}
			{
				StandardTime standard = timezone.getStandardTimes().get(0);
				assertSize(standard, 0, 5);

				assertEquals(date("2000-10-26 02:00:00"), standard.getDateStart().getValue());
				assertEquals(new DateTimeComponents(2000, 10, 26, 2, 0, 0, false), standard.getDateStart().getRawComponents());

				Recurrence rrule = standard.getRecurrenceRule().getValue();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(new ByDay(1, DayOfWeek.SUNDAY)), rrule.getByDay());
				assertEquals(Arrays.asList(10), rrule.getByMonth());

				assertEquals("EST", standard.getTimezoneNames().get(0).getValue());
				assertIntEquals(-4, standard.getTimezoneOffsetFrom().getHourOffset());
				assertIntEquals(0, standard.getTimezoneOffsetFrom().getMinuteOffset());

				assertIntEquals(-5, standard.getTimezoneOffsetTo().getHourOffset());
				assertIntEquals(0, standard.getTimezoneOffsetTo().getMinuteOffset());
			}

			assertFalse(it.hasNext());
		}

		VTimezone timezone = tzinfo.getComponents().iterator().next();
		VEvent event = ical.getEvents().get(0);

		DateStart dtstart = event.getDateStart();
		assertEquals(timezone, tzinfo.getComponent(dtstart));
		TimeZone dtstartTz = tzinfo.getTimeZone(dtstart);
		assertEquals("US/Eastern", dtstartTz.getID());
		assertTrue(dtstartTz instanceof ICalTimeZone);

		RecurrenceDates rdate = event.getRecurrenceDates().get(0);
		assertEquals(timezone, tzinfo.getComponent(rdate));
		assertEquals(dtstartTz, tzinfo.getTimeZone(rdate));

		VEvent event2 = ical.getEvents().get(1);

		dtstart = event2.getDateStart();
		assertEquals(timezone, tzinfo.getComponent(dtstart));
		assertEquals(dtstartTz, tzinfo.getTimeZone(dtstart));

		RecurrenceId rid = event2.getRecurrenceId();
		assertEquals(timezone, tzinfo.getComponent(rid));
		assertEquals(dtstartTz, tzinfo.getTimeZone(rid));

		assertValidate(ical).versions(ICalVersion.V2_0).run();

		assertNull(reader.readNext());
	}

	private JCalReader read(String file) {
		return new JCalReader(getClass().getResourceAsStream(file));
	}

	private class CompanyMarshaller extends ICalPropertyScribe<Company> {
		public CompanyMarshaller() {
			super(Company.class, "X-COMPANY", null);
		}

		@Override
		protected String _writeText(Company property, WriteContext context) {
			return property.getBoss();
		}

		@Override
		protected Company _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return new Company(value);
		}

		@Override
		protected Company _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			String boss = value.asSingle();
			return new Company(boss);
		}
	}

	private class Company extends ICalProperty {
		private String boss;

		public Company(String boss) {
			this.boss = boss;
		}

		public String getBoss() {
			return boss;
		}
	}

	private class PartyMarshaller extends ICalComponentScribe<Party> {
		public PartyMarshaller() {
			super(Party.class, "X-PARTY");
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
