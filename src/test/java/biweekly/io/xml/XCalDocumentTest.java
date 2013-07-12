package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static biweekly.util.TestUtils.assertDateEquals;
import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.buildTimezone;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.component.StandardTime;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.io.CannotParseException;
import biweekly.io.SkipMeException;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.CalendarScale;
import biweekly.property.DateStart;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.property.RecurrenceRule.DayOfWeek;
import biweekly.property.RecurrenceRule.Frequency;
import biweekly.property.Summary;
import biweekly.property.Version;
import biweekly.property.Xml;
import biweekly.property.marshaller.ICalPropertyMarshaller;
import biweekly.util.Duration;
import biweekly.util.IOUtils;
import biweekly.util.Period;
import biweekly.util.XmlUtils;

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
public class XCalDocumentTest {
	private static TimeZone defaultTz;

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
	private final DateFormat localFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");

	@BeforeClass
	public static void beforeClass() {
		//change the default timezone because my timezone is "US/Eastern", which is what the example jCal documents use
		defaultTz = TimeZone.getDefault();
		TimeZone.setDefault(buildTimezone(1, 0));

		XMLUnit.setIgnoreWhitespace(true);
	}

	@AfterClass
	public static void afterClass() {
		TimeZone.setDefault(defaultTz);
	}

	@Test
	public void parseAll() throws Exception {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<prodid><text>-//Example Inc.//Example Client//EN</text></prodid>" +
					"<version><text>2.0</text></version>" +
				"</properties>" +
				"<components>" +
					"<vevent>" +
						"<properties>" +
							"<summary><text>Team Meeting</text></summary>" +
						"</properties>" +
					"</vevent>" +
				"</components>" +
			"</vcalendar>" +
			"<vcalendar>" +
			"<properties>" +
				"<prodid><text>-//Example Inc.//Example Client//EN</text></prodid>" +
				"<version><text>2.0</text></version>" +
			"</properties>" +
			"<components>" +
				"<vevent>" +
					"<properties>" +
						"<summary><text>Team Happy Hour</text></summary>" +
					"</properties>" +
				"</vevent>" +
			"</components>" +
		"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		Iterator<ICalendar> it = xcal.parseAll().iterator();
		assertEquals(2, xcal.getParseWarnings().size());
		assertWarnings(0, xcal.getParseWarnings().get(0));

		ICalendar ical = it.next();
		assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
		assertEquals("2.0", ical.getVersion().getMaxVersion());

		VEvent event = ical.getEvents().get(0);
		assertEquals("Team Meeting", event.getSummary().getValue());

		ical = it.next();
		assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
		assertEquals("2.0", ical.getVersion().getMaxVersion());

		event = ical.getEvents().get(0);
		assertEquals("Team Happy Hour", event.getSummary().getValue());

		assertFalse(it.hasNext());
	}

	@Test
	public void parse_default_namespace() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar>" +
			"<vcalendar>" +
				"<properties>" +
					"<prodid><text>-//Example Inc.//Example Client//EN</text></prodid>" +
					"<version><text>2.0</text></version>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		ICalendar ical = xcal.parseFirst();
		assertNull(ical);
		assertEquals(0, xcal.getParseWarnings().size());
	}

	@Test
	public void parse_wrong_namespace() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"http://example.com\">" +
			"<vcalendar>" +
				"<properties>" +
					"<prodid><text>-//Example Inc.//Example Client//EN</text></prodid>" +
					"<version><text>2.0</text></version>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		ICalendar ical = xcal.parseFirst();
		assertNull(ical);
		assertEquals(0, xcal.getParseWarnings().size());
	}

	@Test
	public void parse_preserve_whitespace() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<summary><text>  This \t  is \n   a   note </text></summary>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		ICalendar ical = xcal.parseFirst();
		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(0, xcal.getParseWarnings().get(0));

		Summary prop = ical.getProperty(Summary.class);
		assertEquals("  This \t  is \n   a   note ", prop.getValue());
	}

	@Test
	public void parse_parameters() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					//zero params
					"<summary>" +
						"<text>summary 1</text>" +
					"</summary>" +
					
					//one param
					"<summary>" +
						"<parameters>" +
							"<language><text>en</text></language>" +
						"</parameters>" +
						"<text>summary 2</text>" +
					"</summary>" +
					
					//two params
					"<summary>" +
						"<parameters>" +
							"<language><text>en</text></language>" +
							"<x-foo><text>bar</text></x-foo>" +
						"</parameters>" +
						"<text>summary 3</text>" +
					"</summary>" +
					
					//a param with multiple values
					"<summary>" +
						"<parameters>" +
							"<x-foo>" +
								"<text>a</text>" +
								"<text>b</text>" +
							"</x-foo>" +
						"</parameters>" +
						"<text>summary 4</text>" +
					"</summary>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		ICalendar ical = xcal.parseFirst();
		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(0, xcal.getParseWarnings().get(0));

		Iterator<Summary> propIt = ical.getProperties(Summary.class).iterator();

		Summary prop = propIt.next();
		assertEquals("summary 1", prop.getValue());
		assertTrue(prop.getParameters().isEmpty());

		prop = propIt.next();
		assertEquals("summary 2", prop.getValue());
		assertEquals(1, prop.getParameters().size());
		assertEquals("en", prop.getLanguage());

		prop = propIt.next();
		assertEquals("summary 3", prop.getValue());
		assertEquals(2, prop.getParameters().size());
		assertEquals("en", prop.getLanguage());
		assertEquals("bar", prop.getParameter("X-FOO"));

		prop = propIt.next();
		assertEquals("summary 4", prop.getValue());
		assertEquals(2, prop.getParameters().size());
		assertEquals(Arrays.asList("a", "b"), prop.getParameters("X-FOO"));

		assertFalse(propIt.hasNext());
	}

	@Test
	public void parse_experimental_component() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<components>" +
					"<x-party>" +
						"<properties>" +
							"<summary><text>Party</text></summary>" +
						"</properties>" +
					"</x-party>" +
				"</components>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		ICalendar ical = xcal.parseFirst();

		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(0, xcal.getParseWarnings().get(0));

		RawComponent comp = ical.getExperimentalComponent("x-party");
		assertEquals(1, comp.getProperties().size());
		Summary prop = comp.getProperty(Summary.class);
		assertEquals("Party", prop.getValue());
	}

	@Test
	public void parse_skip_component_without_xcal_namespace() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<components>" +
					"<foo xmlns=\"http://example.com\">" +
					"</foo>" +
					"<x-party>" +
					"</x-party>" +
				"</components>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		ICalendar ical = xcal.parseFirst();

		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(0, xcal.getParseWarnings().get(0));
		assertEquals(1, ical.getComponents().size());

		assertNotNull(ical.getExperimentalComponent("x-party"));
	}

	@Test
	public void parse_experimental_property() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<x-boss><text>John Doe</text></x-boss>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		ICalendar ical = xcal.parseFirst();

		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(0, xcal.getParseWarnings().get(0));

		RawProperty prop = ical.getExperimentalProperty("X-BOSS");
		assertEquals("John Doe", prop.getValue());
		assertEquals(Value.TEXT, prop.getParameters().getValue());
	}

	@Test
	public void parse_experimental_property_with_namespace_with_marshaller() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<m:boss>John Doe</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		xcal.registerMarshaller(new CompanyMarshaller());
		ICalendar ical = xcal.parseFirst();

		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(0, xcal.getParseWarnings().get(0));

		Company prop = ical.getProperty(Company.class);
		assertEquals("John Doe", prop.getBoss());
	}

	@Test
	public void parse_experimental_property_with_namespace_without_marshaller() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<m:boss>John Doe</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		ICalendar ical = xcal.parseFirst();

		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(0, xcal.getParseWarnings().get(0));

		Xml prop = ical.getProperty(Xml.class);
		Document expected = XmlUtils.toDocument("<m:company xmlns:m=\"http://example.com\"><m:boss>John Doe</m:boss></m:company>");
		assertXMLEqual(expected, prop.getValue());
	}

	@Test
	public void parse_skipMeException() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<m:boss>skip-me</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		xcal.registerMarshaller(new CompanyMarshaller());
		ICalendar ical = xcal.parseFirst();

		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(1, xcal.getParseWarnings().get(0));

		Company prop = ical.getProperty(Company.class);
		assertNull(prop);
		assertEquals(0, ical.getProperties().size());
	}

	@Test
	public void parse_cannotParseException() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<m:boss>don't-parse-me</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		xcal.registerMarshaller(new CompanyMarshaller());
		ICalendar ical = xcal.parseFirst();

		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(1, xcal.getParseWarnings().get(0));

		assertNull(ical.getProperty(Company.class));

		Xml prop = ical.getProperty(Xml.class);
		Document expected = XmlUtils.toDocument("<m:company xmlns:m=\"http://example.com\"><m:boss>don't-parse-me</m:boss></m:company>");
		assertXMLEqual(expected, prop.getValue());
	}

	@Test
	public void parse_unsupportedOperationException() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<m:boss>UnsupportedOperationException</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		xcal.registerMarshaller(new CompanyMarshaller());
		ICalendar ical = xcal.parseFirst();

		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(1, xcal.getParseWarnings().get(0));

		assertNull(ical.getProperty(Company.class));

		Xml prop = ical.getProperty(Xml.class);
		Document expected = XmlUtils.toDocument("<m:company xmlns:m=\"http://example.com\"><m:boss>UnsupportedOperationException</m:boss></m:company>");
		assertXMLEqual(expected, prop.getValue());
	}

	@Test
	public void parse_icalendar_element_is_not_root() throws Exception {
		//@formatter:off
		String xml =
		"<foo xmlns=\"http://example.com\">" +
			"<xcal:icalendar xmlns:xcal=\"" + XCAL_NS + "\">" +
				"<xcal:vcalendar>" +
					"<xcal:properties>" +
						"<xcal:summary><xcal:text>summary</xcal:text></xcal:summary>" +
					"</xcal:properties>" +
				"</xcal:vcalendar>" +
			"</xcal:icalendar>" +
		"</foo>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		ICalendar ical = xcal.parseFirst();
		assertEquals(1, xcal.getParseWarnings().size());
		assertWarnings(0, xcal.getParseWarnings().get(0));

		Summary prop = ical.getProperty(Summary.class);
		assertEquals("summary", prop.getValue());
	}

	@Test
	public void empty() throws Exception {
		XCalDocument xcal = new XCalDocument();

		Document actual = xcal.getDocument();
		Document expected = XmlUtils.toDocument("<icalendar xmlns=\"" + XCAL_NS + "\"/>");
		assertXMLEqual(expected, actual);
	}

	@Test
	public void add_xml_property() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		Xml xml = new Xml("<company xmlns=\"http://example.com\"><boss>John Doe</boss></company>");
		ical.addProperty(xml);

		XCalDocument xcal = new XCalDocument();
		xcal.add(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<m:boss>John Doe</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>");
		//@formatter:on
		assertXMLEqual(expected, actual);
	}

	@Test(expected = IllegalArgumentException.class)
	public void add_property_marshaller_not_found() {
		ICalendar ical = new ICalendar();
		ical.addProperty(new Company(""));

		XCalDocument xcal = new XCalDocument();
		xcal.add(ical);
	}

	@Test(expected = IllegalArgumentException.class)
	public void add_component_marshaller_not_found() {
		ICalendar ical = new ICalendar();
		ical.addComponent(new Party());

		XCalDocument xcal = new XCalDocument();
		xcal.add(ical);
	}

	@Test
	public void add_experimental_component() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		Party party = new Party();
		party.addProperty(new Summary("summary"));
		ical.addComponent(party);

		XCalDocument xcal = new XCalDocument();
		xcal.registerMarshaller(new PartyMarshaller());
		xcal.add(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<components>" +
					"<x-party>" +
						"<properties>" +
							"<summary><text>summary</text></summary>" +
						"</properties>" +
					"</x-party>" +
				"</components>" +
			"</vcalendar>" +
		"</icalendar>");
		//@formatter:on
		assertXMLEqual(expected, actual);
	}

	@Test
	public void add_experimental_property() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new Company("John Doe"));

		XCalDocument xcal = new XCalDocument();
		xcal.registerMarshaller(new CompanyMarshaller());
		xcal.add(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<m:boss>John Doe</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>");
		//@formatter:on
		assertXMLEqual(expected, actual);
	}

	@Test
	public void add_experimental_parameter() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		Summary summary = new Summary("summary");
		summary.setParameter("X-ONE", "one");
		summary.setParameter("X-TWO", "two");
		ical.addProperty(summary);

		XCalDocument xcal = new XCalDocument();
		xcal.registerParameterDataType("X-ONE", Value.TEXT);
		xcal.add(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<summary>" +
						"<parameters>" +
							"<x-one><text>one</text></x-one>" +
							"<x-two><unknown>two</unknown></x-two>" +
						"</parameters>" +
						"<text>summary</text>" +
					"</summary>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>");
		//@formatter:on
		assertXMLEqual(expected, actual);
	}

	@Test
	public void add_skipMeException() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new Company("skip-me"));

		XCalDocument xcal = new XCalDocument();
		xcal.registerMarshaller(new CompanyMarshaller());
		xcal.add(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
			"</vcalendar>" +
		"</icalendar>");
		//@formatter:on
		assertXMLEqual(expected, actual);
	}

	@Test
	public void write() {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new Summary("summary"));

		XCalDocument xcal = new XCalDocument();
		xcal.add(ical);

		String actual = xcal.write();
		//@formatter:off
		String expected =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<summary><text>summary</text></summary>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void write_pretty_print() {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new Summary("summary"));

		XCalDocument xcal = new XCalDocument();
		xcal.add(ical);

		String newline = System.getProperty("line.separator");
		String actual = xcal.write(2);
		//@formatter:off
		String expected =
		"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + newline +
		"<icalendar xmlns=\"" + XCAL_NS + "\">" + newline +
		"  <vcalendar>" + newline +	
		"    <properties>" + newline +
		"      <summary>" + newline +
		"        <text>summary</text>" + newline +
		"      </summary>" + newline +
		"    </properties>" + newline +
		"  </vcalendar>" + newline +
		"</icalendar>" + newline;
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void read_example1() throws Throwable {
		String xml = new String(IOUtils.toByteArray(getClass().getResourceAsStream("rfc6321-example1.xml")));
		XCalDocument xcal = new XCalDocument(xml);

		List<ICalendar> icals = xcal.parseAll();
		assertEquals(1, icals.size());

		ICalendar ical = icals.get(0);
		assertEquals(3, ical.getProperties().size());
		assertTrue(ical.getCalendarScale().isGregorian());
		assertEquals("-//Example Inc.//Example Calendar//EN", ical.getProductId().getValue());
		assertEquals("2.0", ical.getVersion().getMaxVersion());

		assertEquals(1, ical.getComponents().size());
		{
			VEvent event = ical.getEvents().get(0);
			assertEquals(4, event.getProperties().size());
			assertDateEquals("20080205T191224Z", event.getDateTimeStamp().getValue());
			assertDateEquals("20081006", event.getDateStart().getValue());
			assertEquals("Planning meeting", event.getSummary().getValue());
			assertEquals("4088E990AD89CB3DBB484909", event.getUid().getValue());
		}

		assertWarnings(0, ical.validate());
	}

	@Test
	public void write_example1() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setCalendarScale(CalendarScale.gregorian());
		ical.setProductId("-//Example Inc.//Example Calendar//EN");
		ical.setVersion(Version.v2_0());
		{
			VEvent event = new VEvent();
			event.getProperties().clear();
			event.setDateTimeStamp(utcFormatter.parse("2008-02-05T19:12:24"));
			event.setDateStart(new DateStart(dateFormatter.parse("2008-10-06"), false));
			event.setSummary("Planning meeting");
			event.setUid("4088E990AD89CB3DBB484909");
			ical.addEvent(event);
		}

		assertWarnings(0, ical.validate());
		assertExample(ical, "rfc6321-example1.xml");
	}

	@Test
	public void read_example2() throws Throwable {
		//see: RFC 6321 p.49
		DateFormat usEastern = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		usEastern.setTimeZone(TimeZone.getTimeZone("US/Eastern"));

		String xml = new String(IOUtils.toByteArray(getClass().getResourceAsStream("rfc6321-example2.xml")));
		XCalDocument xcal = new XCalDocument(xml);

		List<ICalendar> icals = xcal.parseAll();
		assertEquals(1, icals.size());

		ICalendar ical = icals.get(0);
		assertEquals(2, ical.getProperties().size());
		assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
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

				RecurrenceRule rrule = daylight.getRecurrenceRule();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(DayOfWeek.SUNDAY), rrule.getByDay());
				assertEquals(Arrays.asList(1), rrule.getByDayPrefixes());
				assertEquals(Arrays.asList(4), rrule.getByMonth());

				assertEquals("EDT", daylight.getTimezoneNames().get(0).getValue());
				assertIntEquals(-5, daylight.getTimezoneOffsetFrom().getHourOffset());
				assertIntEquals(0, daylight.getTimezoneOffsetFrom().getMinuteOffset());

				assertIntEquals(-4, daylight.getTimezoneOffsetTo().getHourOffset());
				assertIntEquals(0, daylight.getTimezoneOffsetTo().getMinuteOffset());
			}
			{
				StandardTime standard = timezone.getStandardTimes().get(0);
				assertEquals(5, standard.getProperties().size());
				assertDateEquals("20001026T020000", standard.getDateStart().getValue());

				RecurrenceRule rrule = standard.getRecurrenceRule();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(DayOfWeek.SUNDAY), rrule.getByDay());
				assertEquals(Arrays.asList(-1), rrule.getByDayPrefixes());
				assertEquals(Arrays.asList(10), rrule.getByMonth());

				assertEquals("EST", standard.getTimezoneNames().get(0).getValue());
				assertIntEquals(-4, standard.getTimezoneOffsetFrom().getHourOffset());
				assertIntEquals(0, standard.getTimezoneOffsetFrom().getMinuteOffset());

				assertIntEquals(-5, standard.getTimezoneOffsetTo().getHourOffset());
				assertIntEquals(0, standard.getTimezoneOffsetTo().getMinuteOffset());
			}
		}
		{
			VEvent event = ical.getEvents().get(0);

			assertEquals(8, event.getProperties().size());
			assertDateEquals("20060206T001121Z", event.getDateTimeStamp().getValue());
			assertEquals(usEastern.parse("2006-01-02T12:00:00"), event.getDateStart().getValue());
			assertEquals("US/Eastern", event.getDateStart().getTimezoneId());
			assertEquals(new Duration.Builder().hours(1).build(), event.getDuration().getValue());

			RecurrenceRule rrule = event.getRecurrenceRule();
			assertEquals(Frequency.DAILY, rrule.getFrequency());
			assertIntEquals(5, rrule.getCount());

			RecurrenceDates rdate = event.getRecurrenceDates().get(0);
			assertNull(rdate.getDates());
			assertEquals(1, rdate.getPeriods().size());
			assertEquals(new Period(usEastern.parse("2006-01-02T15:00:00"), new Duration.Builder().hours(2).build()), rdate.getPeriods().get(0));
			assertEquals("US/Eastern", rdate.getTimezoneId());

			assertEquals("Event #2", event.getSummary().getValue());
			assertEquals("We are having a meeting all this week at 12pm for one hour, with an additional meeting on the first day 2 hours long.\nPlease bring your own lunch for the 12 pm meetings.", event.getDescription().getValue());
			assertEquals("00959BC664CA650E933C892C@example.com", event.getUid().getValue());
		}
		{
			VEvent event = ical.getEvents().get(1);

			assertEquals(6, event.getProperties().size());
			assertDateEquals("20060206T001121Z", event.getDateTimeStamp().getValue());
			assertEquals(usEastern.parse("2006-01-04T14:00:00"), event.getDateStart().getValue());
			assertEquals("US/Eastern", event.getDateStart().getTimezoneId());
			assertEquals(new Duration.Builder().hours(1).build(), event.getDuration().getValue());

			assertEquals(usEastern.parse("2006-01-04T12:00:00"), event.getRecurrenceId().getValue());
			assertEquals("US/Eastern", event.getRecurrenceId().getTimezoneId());
			assertEquals("Event #2 bis", event.getSummary().getValue());
			assertEquals("00959BC664CA650E933C892C@example.com", event.getUid().getValue());
		}

		assertWarnings(0, ical.validate());
	}

	@Test
	public void write_example2() throws Throwable {
		//see: RFC 6321 p.51
		VTimezone usEasternTz;
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//Example Inc.//Example Client//EN");
		ical.setVersion(Version.v2_0());
		{
			usEasternTz = new VTimezone(null);
			usEasternTz.setLastModified(utcFormatter.parse("2004-01-10T03:28:45"));
			usEasternTz.setTimezoneId("US/Eastern");
			{
				DaylightSavingsTime daylight = new DaylightSavingsTime();
				daylight.setDateStart(localFormatter.parse("2000-04-04T02:00:00")).setLocalTime(true);

				RecurrenceRule rrule = new RecurrenceRule(Frequency.YEARLY);
				rrule.addByDay(1, DayOfWeek.SUNDAY);
				rrule.addByMonth(4);
				daylight.setRecurrenceRule(rrule);

				daylight.addTimezoneName("EDT");
				daylight.setTimezoneOffsetFrom(-5, 0);
				daylight.setTimezoneOffsetTo(-4, 0);

				usEasternTz.addDaylightSavingsTime(daylight);
			}
			{
				StandardTime standard = new StandardTime();
				standard.setDateStart(localFormatter.parse("2000-10-26T02:00:00")).setLocalTime(true);

				RecurrenceRule rrule = new RecurrenceRule(Frequency.YEARLY);
				rrule.addByDay(-1, DayOfWeek.SUNDAY);
				rrule.addByMonth(10);
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
			event.setDuration(new Duration.Builder().hours(1).build());

			RecurrenceRule rrule = new RecurrenceRule(Frequency.DAILY);
			rrule.setCount(5);
			event.setRecurrenceRule(rrule);

			RecurrenceDates rdate = new RecurrenceDates(Arrays.asList(new Period(usEasternFormatter.parse("2006-01-02T15:00:00"), new Duration.Builder().hours(2).build())));
			rdate.setTimezone(usEasternTz);
			event.addRecurrenceDates(rdate);

			event.setSummary("Event #2");
			event.setDescription("We are having a meeting all this week at 12pm for one hour, with an additional meeting on the first day 2 hours long.\nPlease bring your own lunch for the 12 pm meetings.");
			event.setUid("00959BC664CA650E933C892C@example.com");
			ical.addEvent(event);
		}
		{
			VEvent event = new VEvent();
			event.setDateTimeStamp(utcFormatter.parse("2006-02-06T00:11:21"));
			event.setDateStart(usEasternFormatter.parse("2006-01-04T14:00:00")).setTimezone(usEasternTz);
			event.setDuration(new Duration.Builder().hours(1).build());

			event.setRecurrenceId(usEasternFormatter.parse("2006-01-04T12:00:00")).setTimezone(usEasternTz);

			event.setSummary("Event #2 bis");
			event.setUid("00959BC664CA650E933C892C@example.com");
			ical.addEvent(event);
		}

		assertWarnings(0, ical.validate());
		assertExample(ical, "rfc6321-example2.xml");
	}

	private void assertExample(ICalendar ical, String exampleFileName) throws IOException, SAXException {
		XCalDocument xcal = new XCalDocument();
		xcal.add(ical);

		Document expected = XmlUtils.toDocument(new InputStreamReader(getClass().getResourceAsStream(exampleFileName)));
		Document actual = xcal.getDocument();

		assertXMLEqual(XmlUtils.toString(actual), expected, actual);
	}

	private class CompanyMarshaller extends ICalPropertyMarshaller<Company> {
		public CompanyMarshaller() {
			super(Company.class, "X-COMPANY", new QName("http://example.com", "company"));
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
		protected void _writeXml(Company property, XCalElement element) {
			if (property.getBoss().equals("skip-me")) {
				throw new SkipMeException();
			}
			Element boss = element.getElement().getOwnerDocument().createElementNS(getQName().getNamespaceURI(), "boss");
			boss.setTextContent(property.getBoss());
			element.getElement().appendChild(boss);
		}

		@Override
		protected Company _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
			String boss = XmlUtils.getFirstChildElement(element.getElement()).getTextContent();
			if (boss.equals("skip-me")) {
				throw new SkipMeException();
			}
			if (boss.equals("don't-parse-me")) {
				throw new CannotParseException();
			}
			if (boss.equals("UnsupportedOperationException")) {
				return super._parseXml(element, parameters, warnings);
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
