package biweekly.io.json;

import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.component.VEvent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.io.CannotParseException;
import biweekly.io.SkipMeException;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.Summary;
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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * @author Michael Angstadt
 */
public class JCalReaderTest {
	private final String NEWLINE = System.getProperty("line.separator");

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
		assertEquals(Value.TEXT, company.getParameters().getValue());
		assertEquals("value", company.getValue());

		company = ical.getExperimentalProperty("x-company2");
		assertNull(company.getParameters().getValue());
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
		assertEquals(Value.TEXT, company.getParameters().getValue());
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

	private class CompanyMarshaller extends ICalPropertyMarshaller<Company> {
		public CompanyMarshaller() {
			super(Company.class, "X-COMPANY");
		}

		@Override
		protected String _writeText(Company property) {
			return property.getBoss();
		}

		@Override
		protected Company _parseText(String value, ICalParameters parameters, List<String> warnings) {
			return new Company(value);
		}

		@Override
		protected Company _parseJson(JCalValue value, ICalParameters parameters, List<String> warnings) {
			String boss = value.getSingleValued();
			if (boss.equals("skip-me")) {
				throw new SkipMeException();
			}
			if (boss.equals("don't-parse-me-bro")) {
				throw new CannotParseException();
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

	private class PartyMarshaller extends ICalComponentMarshaller<Party> {
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
