package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import biweekly.ICalDataType;
import static biweekly.ICalVersion.*;
import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.StandardTime;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.SkipMeException;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.SkipMeScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.CalendarScale;
import biweekly.property.DateStart;
import biweekly.property.ICalProperty;
import biweekly.property.ProductId;
import biweekly.property.RecurrenceDates;
import biweekly.property.SkipMeProperty;
import biweekly.property.Summary;
import biweekly.property.Xml;
import biweekly.util.DateTimeComponents;
import biweekly.util.Duration;
import biweekly.util.IOUtils;
import biweekly.util.Period;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;
import biweekly.util.XmlUtils;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
public class XCalWriterTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	private StringWriter sw;
	private XCalWriter writer;
	private ICalendar ical;

	@Before
	public void before() {
		sw = new StringWriter();
		writer = new XCalWriter(sw);

		ical = new ICalendar();
		ical.getProperties().clear();
	}

	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void write_single() throws Exception {
		ical.setProductId("value").setParameter("x-foo", "bar");
		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setDescription("value");
		ical.addEvent(event);
		writer.write(ical);

		writer.close();

		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" + NEWLINE +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<prodid>" +
						"<parameters>" +
							"<x-foo><unknown>bar</unknown></x-foo>" + 
						"</parameters>" +
						"<text>value</text>" +
					"</prodid>" +
				"</properties>" +
				"<components>" +
					"<vevent>" +
						"<properties>" +
							"<description>" + 
								"<text>value</text>" +
							"</description>" + 
						"</properties>" +
					"</vevent>" +
				"</components>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_multiple() throws Exception {
		ical.setProductId("value").setParameter("x-foo", "bar");
		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setDescription("value");
		ical.addEvent(event);
		writer.write(ical);

		ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("value");
		writer.write(ical);

		writer.close();

		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" + NEWLINE +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" + 
					"<prodid>" +
						"<parameters>" +
							"<x-foo><unknown>bar</unknown></x-foo>" + 
						"</parameters>" +
						"<text>value</text>" +
					"</prodid>" +
				"</properties>" +
				"<components>" +
					"<vevent>" +
						"<properties>" +
							"<description>" + 
								"<text>value</text>" +
							"</description>" + 
						"</properties>" +
					"</vevent>" +
				"</components>" +
			"</vcalendar>" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<prodid>" +
						"<text>value</text>" +
					"</prodid>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_empty() throws Exception {
		writer.close();

		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\" />";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_xml_property() throws Exception {
		Xml xml = new Xml("<company xmlns=\"http://example.com\"><boss prefix=\"Mr\">John Doe</boss></company>");
		xml.setParameter("x-foo", "bar");
		ical.addProperty(xml);
		writer.write(ical);

		writer.close();

		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<parameters>" +
							"<x-foo><unknown>bar</unknown></x-foo>" +
						"</parameters>" +
						"<m:boss prefix=\"Mr\">John Doe</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_xml_property_null_value() throws Exception {
		Xml xml = new Xml((Document) null);
		ical.addProperty(xml);
		writer.write(ical);

		writer.close();

		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_existing_dom_document() throws Exception {
		Document document = XmlUtils.toDocument("<root><a /><b /></root>");
		XCalWriter writer = new XCalWriter(document);

		ical.setProductId("value");
		writer.write(ical);

		writer.close();

		//@formatter:off
		String xml =
		"<root>" +
			"<a />" +
			"<b />" +
			"<icalendar xmlns=\"" + XCAL_NS + "\">" +
				"<vcalendar>" +
					"<properties>" +
						"<version><text>2.0</text></version>" +
						"<prodid><text>value</text></prodid>" +
					"</properties>" +
				"</vcalendar>" +
			"</icalendar>" +
		"</root>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, document);
	}

	@Test
	public void write_existing_dom_element() throws Exception {
		Document document = XmlUtils.toDocument("<root><a /><b /></root>");
		Node element = document.getFirstChild().getFirstChild();
		XCalWriter writer = new XCalWriter(element);

		ical.setProductId("value");
		writer.write(ical);

		writer.close();

		//@formatter:off
		String xml =
		"<root>" +
			"<a>" +
				"<icalendar xmlns=\"" + XCAL_NS + "\">" +
					"<vcalendar>" +
						"<properties>" +
							"<version><text>2.0</text></version>" +
							"<prodid><text>value</text></prodid>" +
						"</properties>" +
					"</vcalendar>" +
				"</icalendar>" +
			"</a>" +
			"<b />" +
		"</root>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, document);
	}

	@Test
	public void write_existing_icalendar_document() throws Exception {
		Document document = XmlUtils.toDocument("<icalendar xmlns=\"" + XCAL_NS + "\" />");
		XCalWriter writer = new XCalWriter(document);

		ical.setProductId("value");
		writer.write(ical);

		writer.close();

		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<prodid><text>value</text></prodid>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, document);
	}

	@Test
	public void write_existing_icalendar_element() throws Exception {
		Document document = XmlUtils.toDocument("<root><a><icalendar xmlns=\"" + XCAL_NS + "\" /></a><b /></root>");
		Node element = document.getFirstChild().getFirstChild().getFirstChild();
		XCalWriter writer = new XCalWriter(element);

		ical.setProductId("value");
		writer.write(ical);

		writer.close();

		//@formatter:off
		String xml =
		"<root>" +
			"<a>" +
				"<icalendar xmlns=\"" + XCAL_NS + "\">" +
					"<vcalendar>" +
						"<properties>" +
							"<version><text>2.0</text></version>" +
							"<prodid><text>value</text></prodid>" +
						"</properties>" +
					"</vcalendar>" +
				"</icalendar>" +
			"</a>" +
			"<b />" +
		"</root>";
		Document expected = XmlUtils.toDocument(xml);
		//@formatter:on

		assertXMLEqual(expected, document);
	}

	@Test
	public void write_parameters() throws Exception {
		writer.registerParameterDataType("X-INT", ICalDataType.INTEGER);

		ProductId prodId = new ProductId("value");
		prodId.getParameters().setLanguage("en");
		prodId.addParameter("x-int", "1");
		prodId.addParameter("x-int", "2");
		prodId.addParameter("x-foo", "bar");
		ical.addProperty(prodId);
		writer.write(ical);

		writer.close();

		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<prodid>" +
						"<parameters>" +
							"<language><text>en</text></language>" +
							"<x-int><integer>1</integer><integer>2</integer></x-int>" +
							"<x-foo><unknown>bar</unknown></x-foo>" +
						"</parameters>" +
						"<text>value</text>" +
					"</prodid>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void skipMeException() throws Exception {
		writer.registerScribe(new SkipMeScribe());

		ical.addProperty(new SkipMeProperty());
		ical.addExperimentalProperty("X-FOO", "bar");
		writer.write(ical);

		writer.close();

		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<x-foo><unknown>bar</unknown></x-foo>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_no_property_scribe_registered() throws Exception {
		ical.setProductId("value");
		writer.write(ical);

		ical = new ICalendar();
		ical.setProductId("value");
		ical.addProperty(new Company(""));

		try {
			writer.write(ical);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		writer.close();

		//the writer should check for scribes before writing anything to the stream
		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<prodid><text>value</text></prodid>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_no_component_scribe_registered() throws Exception {
		ical.setProductId("value");
		writer.write(ical);

		ical = new ICalendar();
		ical.setProductId("value");
		ical.addComponent(new Party());

		try {
			writer.write(ical);
			fail();
		} catch (IllegalArgumentException e) {
			//should be thrown
		}

		writer.close();

		//the writer should check for scribes before writing anything to the stream
		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<prodid><text>value</text></prodid>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_experimental_properties() throws Exception {
		writer.registerScribe(new CompanyScribe());
		ical.addProperty(new Company("John Doe"));
		writer.write(ical);

		writer.close();

		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<m:boss>John Doe</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_experimental_component() throws Exception {
		writer.registerScribe(new PartyScribe());

		Party party = new Party();
		party.addProperty(new Summary("summary"));
		ical.addComponent(party);
		writer.write(ical);

		writer.close();

		//@formatter:off
		String expected =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
				"</properties>" +
				"<components>" +
					"<x-party>" +
						"<properties>" +
							"<summary><text>summary</text></summary>" +
						"</properties>" +
					"</x-party>" +
				"</components>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_pretty_print() throws Exception {
		writer = new XCalWriter(sw, "  ");

		ProductId prodId = ical.setProductId("value");
		prodId.setParameter("x-foo", "bar");
		VEvent event = new VEvent();
		event.getProperties().clear();
		event.setDescription("description");
		ical.addEvent(event);
		writer.write(ical);

		writer.close();

		//@formatter:off
		String expected =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NEWLINE +
		"<icalendar xmlns=\"" + XCAL_NS + "\">" + NEWLINE +
		"  <vcalendar>" + NEWLINE +	
		"    <properties>" + NEWLINE +
		"      <version>" + NEWLINE +
		"        <text>2.0</text>" + NEWLINE +
		"      </version>" + NEWLINE +
		"      <prodid>" + NEWLINE +
		"        <parameters>" + NEWLINE +
		"          <x-foo>" + NEWLINE + 
		"            <unknown>bar</unknown>" + NEWLINE +
		"          </x-foo>" + NEWLINE + 
		"        </parameters>" + NEWLINE +
		"        <text>value</text>" + NEWLINE +
		"      </prodid>" + NEWLINE +
		"    </properties>" + NEWLINE +
		"    <components>" + NEWLINE +
		"      <vevent>" + NEWLINE +
		"        <properties>" + NEWLINE +
		"          <description>" + NEWLINE + 
		"            <text>description</text>" + NEWLINE +
		"          </description>" + NEWLINE + 
		"        </properties>" + NEWLINE +
		"      </vevent>" + NEWLINE +
		"    </components>" + NEWLINE +
		"  </vcalendar>" + NEWLINE +
		"</icalendar>" + NEWLINE;
		//@formatter:on

		assertOutput(expected);
	}

	@Test
	public void write_utf8() throws Throwable {
		File file = tempFolder.newFile();
		writer = new XCalWriter(file);

		ical.addProperty(new Summary("\u1e66ummary"));
		writer.write(ical);

		writer.close();

		String xml = IOUtils.getFileContents(file, "UTF-8");
		assertTrue(xml.matches("(?i)<\\?xml.*?encoding=\"utf-8\".*?\\?>.*"));
		assertTrue(xml.matches(".*?<summary><text>\u1e66ummary</text></summary>.*"));
	}

	@Test
	public void write_example1() throws Throwable {
		ical.setCalendarScale(CalendarScale.gregorian());
		ical.setProductId("-//Example Inc.//Example Calendar//EN");
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.setDateTimeStamp(utc("2008-02-05 19:12:24"));
			event.setDateStart(new DateStart(date("2008-10-06"), false));
			event.setSummary("Planning meeting");
			event.setUid("4088E990AD89CB3DBB484909");
			ical.addEvent(event);
		}

		assertValidate(ical).versions(V2_0).run();
		assertExample(ical, "rfc6321-example1.xml", new TimezoneInfo());
	}

	@Test
	public void write_example2() throws Throwable {
		TimeZone eastern = TimeZone.getTimeZone("US/Eastern");
		//see: RFC 6321 p.51
		VTimezone usEasternTz;
		ical.setProductId("-//Example Inc.//Example Client//EN");
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utc("2006-02-06 00:11:21"));
			event.setDateStart(date("2006-01-02 12:00:00", eastern));
			event.setDuration(Duration.builder().hours(1).build());

			Recurrence rrule = new Recurrence.Builder(Frequency.DAILY).count(5).build();
			event.setRecurrenceRule(rrule);

			RecurrenceDates rdate = new RecurrenceDates();
			rdate.addPeriod(new Period(date("2006-01-02 15:00:00", eastern), Duration.builder().hours(2).build()));
			event.addRecurrenceDates(rdate);

			event.setSummary("Event #2");
			event.setDescription("We are having a meeting all this week at 12pm for one hour, with an additional meeting on the first day 2 hours long.\nPlease bring your own lunch for the 12 pm meetings.");
			event.setUid("00959BC664CA650E933C892C@example.com");
			ical.addEvent(event);
		}
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utc("2006-02-06 00:11:21"));
			event.setDateStart(date("2006-01-04 14:00:00", eastern));
			event.setDuration(Duration.builder().hours(1).build());

			event.setRecurrenceId(date("2006-01-04 12:00:00", eastern));

			event.setSummary("Event #2 bis");
			event.setUid("00959BC664CA650E933C892C@example.com");
			ical.addEvent(event);
		}

		assertValidate(ical).versions(V2_0).run();

		usEasternTz = new VTimezone("US/Eastern");
		usEasternTz.setLastModified(utc("2004-01-10 03:28:45"));
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

			Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY).byDay(-1, DayOfWeek.SUNDAY).byMonth(10).build();
			standard.setRecurrenceRule(rrule);

			standard.addTimezoneName("EST");
			standard.setTimezoneOffsetFrom(-4, 0);
			standard.setTimezoneOffsetTo(-5, 0);

			usEasternTz.addStandardTime(standard);
		}

		TimezoneInfo tzinfo = new TimezoneInfo();
		tzinfo.assign(usEasternTz, eastern);
		tzinfo.setDefaultTimeZone(eastern);
		assertExample(ical, "rfc6321-example2.xml", tzinfo);
	}

	private void assertOutput(String expected) throws SAXException, IOException {
		String actual = sw.toString();
		String prettyPrinted = XmlUtils.toString(XmlUtils.toDocument(actual), true);
		assertXMLEqual(prettyPrinted, expected, actual);
	}

	private void assertExample(ICalendar ical, String exampleFileName, TimezoneInfo tzinfo) throws SAXException, IOException {
		writer.setTimezoneInfo(tzinfo);
		writer.write(ical);
		writer.close();

		String expected = new String(IOUtils.toByteArray(getClass().getResourceAsStream(exampleFileName)));
		assertOutput(expected);
	}

	private class CompanyScribe extends ICalPropertyScribe<Company> {
		public CompanyScribe() {
			super(Company.class, "X-COMPANY", null, new QName("http://example.com", "company"));
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
		protected void _writeXml(Company property, XCalElement element, WriteContext context) {
			if (property.getBoss().equals("skip-me")) {
				throw new SkipMeException("");
			}
			Element boss = element.getElement().getOwnerDocument().createElementNS(getQName().getNamespaceURI(), "boss");
			boss.setTextContent(property.getBoss());
			element.getElement().appendChild(boss);
		}

		@Override
		protected Company _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
			String boss = XmlUtils.getFirstChildElement(element.getElement()).getTextContent();
			if (boss.equals("skip-me")) {
				throw new SkipMeException("");
			}
			if (boss.equals("don't-parse-me")) {
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

	private class PartyScribe extends ICalComponentScribe<Party> {
		public PartyScribe() {
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
