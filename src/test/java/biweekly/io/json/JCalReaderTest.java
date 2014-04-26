package biweekly.io.json;

import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertDateEquals;
import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Writer;
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
import biweekly.component.RawComponent;
import biweekly.component.StandardTime;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.io.CannotParseException;
import biweekly.io.SkipMeException;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.RecurrenceDates;
import biweekly.property.Summary;
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

		assertEquals(2, ical.getProperties().size());
		assertEquals("-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN", ical.getProductId().getValue());
		assertEquals("2.0", ical.getVersion().getMaxVersion());

		assertEquals(1, ical.getComponents().size());
		VEvent event = ical.getEvents().get(0);
		assertEquals(2, event.getProperties().size());
		assertEquals("Networld+Interop Conference", event.getSummary().getValue());
		assertEquals("Networld+Interop Conference" + NEWLINE + "and Exhibit" + NEWLINE + "Atlanta World Congress Center" + NEWLINE + "Atlanta, Georgia", event.getDescription().getValue());

		assertNull(reader.readNext());
		assertWarnings(0, reader.getWarnings());
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

			assertEquals(2, ical.getProperties().size());
			assertEquals("prodid1", ical.getProductId().getValue());
			assertEquals("2.0", ical.getVersion().getMaxVersion());

			assertEquals(1, ical.getComponents().size());
			VEvent event = ical.getEvents().get(0);
			assertEquals(2, event.getProperties().size());
			assertEquals("summary1", event.getSummary().getValue());
			assertEquals("description1", event.getDescription().getValue());

			assertWarnings(0, reader.getWarnings());
		}

		{
			ICalendar ical = reader.readNext();

			assertEquals(2, ical.getProperties().size());
			assertEquals("prodid2", ical.getProductId().getValue());
			assertEquals("2.0", ical.getVersion().getMaxVersion());

			assertEquals(1, ical.getComponents().size());
			VEvent event = ical.getEvents().get(0);
			assertEquals(2, event.getProperties().size());
			assertEquals("summary2", event.getSummary().getValue());
			assertEquals("description2", event.getDescription().getValue());

			assertWarnings(0, reader.getWarnings());
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

		assertEquals(0, ical.getProperties().size());

		assertEquals(1, ical.getComponents().size());
		VEvent event = ical.getEvents().get(0);
		assertEquals(1, event.getProperties().size());
		assertEquals("summary", event.getSummary().getValue());

		assertWarnings(0, reader.getWarnings());

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

		assertEquals(1, ical.getProperties().size());
		assertEquals("prodid", ical.getProductId().getValue());

		assertEquals(0, ical.getComponents().size());

		assertWarnings(0, reader.getWarnings());

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

		assertEquals(0, ical.getProperties().size());
		assertEquals(0, ical.getComponents().size());

		assertWarnings(0, reader.getWarnings());

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

		assertEquals(0, ical.getProperties().size());

		assertEquals(1, ical.getComponents().size());
		RawComponent party = ical.getExperimentalComponent("x-party");
		assertEquals(1, party.getProperties().size());
		assertEquals("summary", party.getProperty(Summary.class).getValue());

		assertWarnings(0, reader.getWarnings());

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
		reader.registerMarshaller(new PartyMarshaller());
		ICalendar ical = reader.readNext();

		assertEquals(0, ical.getProperties().size());

		assertEquals(1, ical.getComponents().size());
		Party party = ical.getComponent(Party.class);
		assertEquals(1, party.getProperties().size());
		assertEquals("summary", party.getProperty(Summary.class).getValue());

		assertWarnings(0, reader.getWarnings());

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

		assertEquals(2, ical.getProperties().size());

		RawProperty company = ical.getExperimentalProperty("x-company");
		assertEquals(ICalDataType.TEXT, company.getDataType());
		assertEquals("value", company.getValue());

		company = ical.getExperimentalProperty("x-company2");
		assertNull(company.getDataType());
		assertEquals("value", company.getValue());

		assertEquals(0, ical.getComponents().size());

		assertWarnings(0, reader.getWarnings());

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
		reader.registerMarshaller(new CompanyMarshaller());
		ICalendar ical = reader.readNext();

		assertEquals(1, ical.getProperties().size());

		Company company = ical.getProperty(Company.class);
		assertEquals("value", company.getBoss());

		assertEquals(0, ical.getComponents().size());

		assertWarnings(0, reader.getWarnings());

		assertNull(reader.readNext());
	}

	@Test
	public void skipMeException() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"x-company\", {}, \"text\", \"skip-me\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		reader.registerMarshaller(new CompanyMarshaller());
		ICalendar ical = reader.readNext();

		assertEquals(0, ical.getProperties().size());
		assertEquals(0, ical.getComponents().size());

		assertWarnings(1, reader.getWarnings());

		assertNull(reader.readNext());
	}

	@Test
	public void cannotParseException() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"x-company\", {}, \"text\", \"don't-parse-me-bro\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalReader reader = new JCalReader(json);
		reader.registerMarshaller(new CompanyMarshaller());
		ICalendar ical = reader.readNext();

		assertEquals(1, ical.getProperties().size());
		assertNull(ical.getProperty(Company.class));
		RawProperty company = ical.getExperimentalProperty("x-company");
		assertEquals(ICalDataType.TEXT, company.getDataType());
		assertEquals("don't-parse-me-bro", company.getValue());

		assertEquals(0, ical.getComponents().size());

		assertWarnings(1, reader.getWarnings());

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
		assertEquals("\u1e66ummary", icalendar.getProperty(Summary.class).getValue());

		assertWarnings(0, reader.getWarnings());
		assertNull(reader.readNext());
	}

	@Test
	public void jcal_draft_example1() throws Throwable {
		//see: http://tools.ietf.org/html/draft-ietf-jcardcal-jcal-05#page-25
		JCalReader reader = new JCalReader(getClass().getResourceAsStream("jcal-draft-example1.json"));
		ICalendar ical = reader.readNext();

		assertEquals(3, ical.getProperties().size());
		assertEquals("-//Example Inc.//Example Calendar//EN", ical.getProductId().getValue());
		assertEquals("2.0", ical.getVersion().getMaxVersion());
		assertTrue(ical.getCalendarScale().isGregorian());

		assertEquals(1, ical.getComponents().size());
		{
			VEvent event = ical.getEvents().get(0);

			assertEquals(4, event.getProperties().size());
			assertDateEquals("20080205T191224Z", event.getDateTimeStamp().getValue());
			assertDateEquals("20081006", event.getDateStart().getValue());
			assertFalse(event.getDateStart().hasTime());
			assertEquals("Planning meeting", event.getSummary().getValue());
			assertEquals("4088E990AD89CB3DBB484909", event.getUid().getValue());

			assertEquals(0, event.getComponents().size());
		}

		assertValidate(ical.validate());

		assertNull(reader.readNext());
	}

	@Test
	public void jcal_draft_example2() throws Throwable {
		//see: http://tools.ietf.org/html/draft-ietf-jcardcal-jcal-05#page-27
		JCalReader reader = new JCalReader(getClass().getResourceAsStream("jcal-draft-example2.json"));
		ICalendar ical = reader.readNext();

		DateFormat usEastern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		usEastern.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

		assertEquals(2, ical.getProperties().size());
		assertEquals("-//Example Corp.//Example Client//EN", ical.getProductId().getValue());
		assertEquals("2.0", ical.getVersion().getMaxVersion());

		assertEquals(3, ical.getComponents().size());
		{
			VTimezone timezone = ical.getTimezones().get(0);

			assertEquals(2, timezone.getProperties().size());
			assertDateEquals("20040110T032845Z", timezone.getLastModified().getValue());
			assertEquals("US/Eastern", timezone.getTimezoneId().getValue());

			assertEquals(2, timezone.getComponents().size());
			{
				DaylightSavingsTime daylight = timezone.getDaylightSavingsTime().get(0);
				assertEquals(5, daylight.getProperties().size());
				assertDateEquals("20000404T020000", daylight.getDateStart().getValue());
				assertEquals(new DateTimeComponents(2000, 4, 4, 2, 0, 0, false), daylight.getDateStart().getRawComponents());

				Recurrence rrule = daylight.getRecurrenceRule().getValue();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(DayOfWeek.SUNDAY), rrule.getByDay());
				assertEquals(Arrays.asList(1), rrule.getByDayPrefixes());
				assertEquals(Arrays.asList(4), rrule.getByMonth());

				assertEquals("EDT", daylight.getTimezoneNames().get(0).getValue());
				assertIntEquals(-5, daylight.getTimezoneOffsetFrom().getHourOffset());
				assertIntEquals(0, daylight.getTimezoneOffsetFrom().getMinuteOffset());

				assertIntEquals(-4, daylight.getTimezoneOffsetTo().getHourOffset());
				assertIntEquals(0, daylight.getTimezoneOffsetTo().getMinuteOffset());

				assertEquals(0, daylight.getComponents().size());
			}
			{
				StandardTime standard = timezone.getStandardTimes().get(0);
				assertEquals(5, standard.getProperties().size());
				assertDateEquals("20001026T020000", standard.getDateStart().getValue());
				assertEquals(new DateTimeComponents(2000, 10, 26, 2, 0, 0, false), standard.getDateStart().getRawComponents());

				Recurrence rrule = standard.getRecurrenceRule().getValue();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(DayOfWeek.SUNDAY), rrule.getByDay());
				assertEquals(Arrays.asList(1), rrule.getByDayPrefixes());
				assertEquals(Arrays.asList(10), rrule.getByMonth());

				assertEquals("EST", standard.getTimezoneNames().get(0).getValue());
				assertIntEquals(-4, standard.getTimezoneOffsetFrom().getHourOffset());
				assertIntEquals(0, standard.getTimezoneOffsetFrom().getMinuteOffset());

				assertIntEquals(-5, standard.getTimezoneOffsetTo().getHourOffset());
				assertIntEquals(0, standard.getTimezoneOffsetTo().getMinuteOffset());

				assertEquals(0, standard.getComponents().size());
			}
		}
		{
			VEvent event = ical.getEvents().get(0);

			assertEquals(8, event.getProperties().size());
			assertDateEquals("20060206T001121Z", event.getDateTimeStamp().getValue());
			assertEquals(usEastern.parse("2006-01-02T12:00:00"), event.getDateStart().getValue());
			assertEquals("US/Eastern", event.getDateStart().getTimezoneId());
			assertEquals(Duration.builder().hours(1).build(), event.getDuration().getValue());

			Recurrence rrule = event.getRecurrenceRule().getValue();
			assertEquals(Frequency.DAILY, rrule.getFrequency());
			assertIntEquals(5, rrule.getCount());

			RecurrenceDates rdate = event.getRecurrenceDates().get(0);
			assertNull(rdate.getDates());
			assertEquals(1, rdate.getPeriods().size());
			assertEquals(new Period(usEastern.parse("2006-01-02T15:00:00"), Duration.builder().hours(2).build()), rdate.getPeriods().get(0));
			assertEquals("US/Eastern", rdate.getTimezoneId());

			assertEquals("Event #2", event.getSummary().getValue());
			assertEquals("We are having a meeting all this week at 12 pm for one hour, with an additional meeting on the first day 2 hours long." + NEWLINE + "Please bring your own lunch for the 12 pm meetings.", event.getDescription().getValue());
			assertEquals("00959BC664CA650E933C892C@example.com", event.getUid().getValue());

			assertEquals(0, event.getComponents().size());
		}
		{
			VEvent event = ical.getEvents().get(1);

			assertEquals(6, event.getProperties().size());
			assertDateEquals("20060206T001121Z", event.getDateTimeStamp().getValue());
			assertEquals(usEastern.parse("2006-01-02T14:00:00"), event.getDateStart().getValue());
			assertEquals("US/Eastern", event.getDateStart().getTimezoneId());
			assertEquals(Duration.builder().hours(1).build(), event.getDuration().getValue());

			assertEquals(usEastern.parse("2006-01-04T12:00:00"), event.getRecurrenceId().getValue());
			assertEquals("US/Eastern", event.getRecurrenceId().getTimezoneId());
			assertEquals("Event #2", event.getSummary().getValue());
			assertEquals("00959BC664CA650E933C892C@example.com", event.getUid().getValue());

			assertEquals(0, event.getComponents().size());
		}

		assertValidate(ical.validate());

		assertNull(reader.readNext());
	}

	private class CompanyMarshaller extends ICalPropertyScribe<Company> {
		public CompanyMarshaller() {
			super(Company.class, "X-COMPANY", null);
		}

		@Override
		protected String _writeText(Company property) {
			return property.getBoss();
		}

		@Override
		protected Company _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
			return new Company(value);
		}

		@Override
		protected Company _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
			String boss = value.asSingle();
			if (boss.equals("skip-me")) {
				throw new SkipMeException("");
			}
			if (boss.equals("don't-parse-me-bro")) {
				throw new CannotParseException("");
			}
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
