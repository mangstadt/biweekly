package biweekly.io.xml;

import static biweekly.ICalVersion.V2_0;
import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertParseWarnings;
import static biweekly.util.TestUtils.assertSize;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;

import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.component.StandardTime;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.io.CannotParseException;
import biweekly.io.ICalTimeZone;
import biweekly.io.ParseContext;
import biweekly.io.SkipMeException;
import biweekly.io.StreamReader;
import biweekly.io.StreamWriter;
import biweekly.io.TimezoneAssignment;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.CannotParseScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.SkipMeScribe;
import biweekly.io.xml.XCalDocument.XCalDocumentStreamWriter;
import biweekly.parameter.ICalParameters;
import biweekly.property.CalendarScale;
import biweekly.property.DateStart;
import biweekly.property.ICalProperty;
import biweekly.property.ProductId;
import biweekly.property.RawProperty;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceId;
import biweekly.property.SkipMeProperty;
import biweekly.property.Summary;
import biweekly.property.Xml;
import biweekly.util.ByDay;
import biweekly.util.DateTimeComponents;
import biweekly.util.DayOfWeek;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.Duration;
import biweekly.util.Frequency;
import biweekly.util.Gobble;
import biweekly.util.Period;
import biweekly.util.Recurrence;
import biweekly.util.UtcOffset;
import biweekly.util.Utf8Writer;
import biweekly.util.XmlUtils;

/*
 Copyright (c) 2013-2018, Michael Angstadt
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
	/**
	 * Set the timezone to something other than US/Eastern, since some examples
	 * use this timezone.
	 */
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(1, 0);

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@BeforeClass
	public static void beforeClass() {
		XMLUnit.setIgnoreAttributeOrder(true);
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void parse_multiple() throws Exception {
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
		StreamReader reader = xcal.reader();

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
			assertEquals(V2_0, ical.getVersion());

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 1);
			assertEquals("Team Meeting", event.getSummary().getValue());

			assertParseWarnings(reader);
		}

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
			assertEquals(V2_0, ical.getVersion());

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 1);
			assertEquals("Team Happy Hour", event.getSummary().getValue());

			assertParseWarnings(reader);
		}

		assertNull(reader.readNext());
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
		StreamReader reader = xcal.reader();
		ICalendar ical = reader.readNext();
		assertNull(ical);
		assertParseWarnings(reader);
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
		StreamReader reader = xcal.reader();
		ICalendar ical = reader.readNext();
		assertNull(ical);

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void parse_ignore_other_namespaces() throws Exception {
		//@formatter:off
		String xml =
		"<root>" +
			"<ignore xmlns=\"one\">text</ignore>" +
			"<icalendar xmlns=\"" + XCAL_NS + "\">" +
				"<ignore xmlns=\"two\">text</ignore>" +
				"<vcalendar>" +
					"<ignore xmlns=\"three\">text</ignore>" +
					"<properties>" +
						"<prodid>" +
							"<parameters>" +
								"<ignore xmlns=\"three\">text</ignore>" +
								"<x-foo><ignore xmlns=\"three\">text</ignore><text>bar</text></x-foo>" +
							"</parameters>" +
							"<text>-//Example Inc.//Example Client//EN</text>" +
						"</prodid>" +
						"<version><text>2.0</text></version>" +
					"</properties>" +
					"<components>" +
						"<ignore xmlns=\"four\">text</ignore>" +
						"<vevent>" +
							"<ignore xmlns=\"five\">text</ignore>" +
							"<properties>" +
								"<summary><text>Team Meeting</text></summary>" +
							"</properties>" +
							"<ignore xmlns=\"six\">text</ignore>" +
						"</vevent>" +
					"</components>" +
				"</vcalendar>" +
			"</icalendar>" + 
		"</root>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		StreamReader reader = xcal.reader();

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			ProductId productId = ical.getProductId();
			assertEquals("-//Example Inc.//Example Client//EN", productId.getValue());
			assertEquals("bar", productId.getParameter("x-foo"));

			assertEquals(V2_0, ical.getVersion());

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 1);
			assertEquals("Team Meeting", event.getSummary().getValue());
		}

		assertParseWarnings(reader);
		assertNull(reader.readNext());
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
		StreamReader reader = xcal.reader();

		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		Summary prop = ical.getProperty(Summary.class);
		assertEquals("  This \t  is \n   a   note ", prop.getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
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
		StreamReader reader = xcal.reader();

		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 4);

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

		assertParseWarnings(reader);
		assertNull(reader.readNext());
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
		StreamReader reader = xcal.reader();

		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 0);

		RawComponent comp = ical.getExperimentalComponent("x-party");
		assertSize(comp, 0, 1);
		Summary prop = comp.getProperty(Summary.class);
		assertEquals("Party", prop.getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
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
		StreamReader reader = xcal.reader();

		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 0);

		assertNotNull(ical.getExperimentalComponent("x-party"));

		assertParseWarnings(reader);
		assertNull(reader.readNext());
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
		StreamReader reader = xcal.reader();

		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		RawProperty prop = ical.getExperimentalProperty("X-BOSS");
		assertEquals("John Doe", prop.getValue());
		assertEquals(ICalDataType.TEXT, prop.getDataType());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
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
		StreamReader reader = xcal.reader();
		reader.registerScribe(new CompanyScribe());

		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		Company prop = ical.getProperty(Company.class);
		assertEquals("John Doe", prop.getBoss());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
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
		StreamReader reader = xcal.reader();

		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		Xml prop = ical.getProperty(Xml.class);
		Document expected = XmlUtils.toDocument("<m:company xmlns:m=\"http://example.com\"><m:boss>John Doe</m:boss></m:company>");
		assertXMLEqual(expected, prop.getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void parse_skipMeException() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<skipme><unknown>value</unknown></skipme>" +
					"<x-foo><unknown>bar</unknown></x-foo>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		StreamReader reader = xcal.reader();
		reader.registerScribe(new SkipMeScribe());

		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		RawProperty property = ical.getExperimentalProperty("X-FOO");
		assertNull(property.getDataType());
		assertEquals("X-FOO", property.getName());
		assertEquals("bar", property.getValue());

		assertParseWarnings(reader, 0);
		assertNull(reader.readNext());
	}

	@Test
	public void parse_cannotParseException() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<cannotparse><unknown>value</unknown></cannotparse>" +
					"<x-foo><unknown>bar</unknown></x-foo>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalDocument xcal = new XCalDocument(xml);
		StreamReader reader = xcal.reader();
		reader.registerScribe(new CannotParseScribe());

		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 2);

		RawProperty property = ical.getExperimentalProperty("x-foo");
		assertNull(property.getDataType());
		assertEquals("X-FOO", property.getName());
		assertEquals("bar", property.getValue());

		Xml prop = ical.getProperty(Xml.class);
		Document expected = XmlUtils.toDocument("<cannotparse xmlns=\"" + XCAL_NS + "\"><unknown>value</unknown></cannotparse>");
		assertXMLEqual(expected, prop.getValue());

		assertParseWarnings(reader, 1);
		assertNull(reader.readNext());
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
		StreamReader reader = xcal.reader();

		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		Summary prop = ical.getProperty(Summary.class);
		assertEquals("summary", prop.getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
	}

	@Test
	public void parse_utf8() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<summary><text>\u1e66ummary</text></summary>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on
		File file = tempFolder.newFile();
		Writer writer = new Utf8Writer(file);
		writer.write(xml);
		writer.close();

		XCalDocument xcal = new XCalDocument(file);
		StreamReader reader = xcal.reader();

		ICalendar ical = reader.readNext();
		assertSize(ical, 0, 1);

		Summary prop = ical.getProperty(Summary.class);
		assertEquals("\u1e66ummary", prop.getValue());

		assertParseWarnings(reader);
		assertNull(reader.readNext());
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
		xml.addParameter("name", "value");
		ical.addProperty(xml);

		XCalDocument xcal = new XCalDocument();
		xcal.addICalendar(ical);

		Document actual = xcal.getDocument();

		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<company xmlns=\"http://example.com\">" +
						"<parameters xmlns=\"" + XCAL_NS + "\">" +
							"<name><unknown>value</unknown></name>" +
						"</parameters>" +
						"<boss>John Doe</boss>" +
					"</company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>");
		//@formatter:on

		/*
		 * When using xalan as the JAXP parser, XMLUnit thinks the <name>
		 * element in the "actual" DOM has the wrong namespace. But when you
		 * inspect the DOM yourself, the <name> element *does* have the correct
		 * namespace!
		 * 
		 * As a workaround, let's compare the string versions of the two DOMs.
		 */
		assertEquals(XmlUtils.toString(expected), XmlUtils.toString(actual));
		//assertXMLEqual(expected, actual);
	}

	@Test
	public void add_xml_property_null_value() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		Xml xml = new Xml((Document) null);
		ical.addProperty(xml);

		XCalDocument xcal = new XCalDocument();
		xcal.addICalendar(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
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
		xcal.addICalendar(ical);
	}

	@Test(expected = IllegalArgumentException.class)
	public void add_component_marshaller_not_found() {
		ICalendar ical = new ICalendar();
		ical.addComponent(new Party());

		XCalDocument xcal = new XCalDocument();
		xcal.addICalendar(ical);
	}

	@Test
	public void add_experimental_component() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		Party party = new Party();
		party.addProperty(new Summary("summary"));
		ical.addComponent(party);

		XCalDocument xcal = new XCalDocument();
		StreamWriter writer = xcal.writer();
		writer.registerScribe(new PartyScribe());
		writer.write(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
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
		StreamWriter writer = xcal.writer();
		writer.registerScribe(new CompanyScribe());
		writer.write(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
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
		XCalDocumentStreamWriter writer = xcal.writer();
		writer.registerParameterDataType("X-ONE", ICalDataType.TEXT);
		writer.write(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
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
		ical.addProperty(new SkipMeProperty());
		ical.addExperimentalProperty("X-FOO", "bar");

		XCalDocument xcal = new XCalDocument();
		StreamWriter writer = xcal.writer();
		writer.registerScribe(new SkipMeScribe());
		writer.write(ical);

		Document actual = xcal.getDocument();
		//@formatter:off
		Document expected = XmlUtils.toDocument(
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
					"<x-foo><unknown>bar</unknown></x-foo>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>");
		//@formatter:on
		assertXMLEqual(expected, actual);
	}

	@Test
	public void add_no_existing_icalendar_element() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addExperimentalProperty("x-foo", "bar");

		XCalDocument xcal = new XCalDocument("<root />");

		//constructor shouldn't modify document
		Document actual = xcal.getDocument();
		Document expected = XmlUtils.toDocument("<root/>");
		assertXMLEqual(expected, actual);

		xcal.addICalendar(ical);

		actual = xcal.getDocument();
		//@formatter:off
		expected = XmlUtils.toDocument(
		"<root>" +
			"<icalendar xmlns=\"" + XCAL_NS + "\">" +
				"<vcalendar>" +
					"<properties>" +
						"<version><text>2.0</text></version>" +
						"<x-foo><unknown>bar</unknown></x-foo>" +
					"</properties>" +
				"</vcalendar>" +
			"</icalendar>" +
		"</root>");
		//@formatter:on
		assertXMLEqual(expected, actual);
	}

	@Test
	public void add_existing_icalendar_element() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addExperimentalProperty("x-foo", "bar");

		XCalDocument xcal = new XCalDocument("<root><icalendar xmlns=\"" + XCAL_NS + "\"><foo/></icalendar></root>");

		//constructor shouldn't modify document
		Document actual = xcal.getDocument();
		Document expected = XmlUtils.toDocument("<root><icalendar xmlns=\"" + XCAL_NS + "\"><foo/></icalendar></root>");
		assertXMLEqual(expected, actual);

		xcal.addICalendar(ical);

		actual = xcal.getDocument();
		//@formatter:off
		expected = XmlUtils.toDocument(
		"<root>" +
			"<icalendar xmlns=\"" + XCAL_NS + "\">" +
				"<foo />" +
				"<vcalendar>" +
					"<properties>" +
						"<version><text>2.0</text></version>" +
						"<x-foo><unknown>bar</unknown></x-foo>" +
					"</properties>" +
				"</vcalendar>" +
			"</icalendar>" +
		"</root>");
		//@formatter:on
		assertXMLEqual(expected, actual);
	}

	@Test
	public void write() {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new Summary("summary"));

		XCalDocument xcal = new XCalDocument();
		xcal.addICalendar(ical);

		String actual = xcal.write();
		//@formatter:off
		String expected =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<version><text>2.0</text></version>" +
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
		xcal.addICalendar(ical);

		String actual = xcal.write(2);

		//@formatter:off
		String expected =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?><icalendar xmlns=\"" + XCAL_NS + "\">" + NEWLINE +
		"  <vcalendar>" + NEWLINE +	
		"    <properties>" + NEWLINE +
		"      <version>" + NEWLINE + 
		"        <text>2.0</text>" + NEWLINE +
		"      </version>" + NEWLINE +
		"      <summary>" + NEWLINE +
		"        <text>summary</text>" + NEWLINE +
		"      </summary>" + NEWLINE +
		"    </properties>" + NEWLINE +
		"  </vcalendar>" + NEWLINE +
		"</icalendar>" + NEWLINE;
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void write_xmlVerison_default() throws Throwable {
		ICalendar ical = new ICalendar();
		XCalDocument xcal = new XCalDocument();
		xcal.addICalendar(ical);

		String xml = xcal.write();
		assertTrue(xml.matches("(?i)<\\?xml.*?version=\"1.0\".*?\\?>.*"));
	}

	@Test
	public void write_xmlVerison_1_1() throws Throwable {
		ICalendar ical = new ICalendar();
		XCalDocument xcal = new XCalDocument();
		xcal.addICalendar(ical);

		String xml = xcal.write(null, "1.1");
		assertTrue(xml.matches("(?i)<\\?xml.*?version=\"1.1\".*?\\?>.*"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void write_xmlVerison_invalid() throws Throwable {
		ICalendar ical = new ICalendar();
		XCalDocument xcal = new XCalDocument();
		xcal.addICalendar(ical);

		xcal.write(-1, "10.17");
	}

	@Test
	public void write_utf8() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.addProperty(new Summary("\u1e66ummary"));

		XCalDocument xcal = new XCalDocument();
		xcal.addICalendar(ical);

		File file = tempFolder.newFile();
		xcal.write(file);

		String xml = new Gobble(file).asString("UTF-8");
		assertTrue(xml.matches("(?i)<\\?xml.*?encoding=\"utf-8\".*?\\?>.*"));
		assertTrue(xml.matches(".*?<summary><text>\u1e66ummary</text></summary>.*"));
	}

	@Test
	public void read_example1() throws Throwable {
		XCalDocument xcal = read("rfc6321-example1.xml");
		StreamReader reader = xcal.reader();

		ICalendar ical = reader.readNext();
		assertSize(ical, 1, 2);
		assertTrue(ical.getCalendarScale().isGregorian());
		assertEquals("-//Example Inc.//Example Calendar//EN", ical.getProductId().getValue());
		assertEquals(V2_0, ical.getVersion());

		{
			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 4);

			assertEquals(utc("2008-02-05 19:12:24"), event.getDateTimeStamp().getValue());
			assertEquals(date("2008-10-06"), event.getDateStart().getValue());
			assertEquals("Planning meeting", event.getSummary().getValue());
			assertEquals("4088E990AD89CB3DBB484909", event.getUid().getValue());
		}

		assertValidate(ical).versions(V2_0).run();

		assertNull(reader.readNext());
	}

	@Test
	public void write_example1() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
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
		assertExample(ical, "rfc6321-example1.xml");
	}

	@Test
	public void read_example2() throws Throwable {
		XCalDocument xcal = read("rfc6321-example2.xml");
		StreamReader reader = xcal.reader();

		//assert the iCalendar object
		ICalendar ical = reader.readNext();
		assertSize(ical, 2, 1);
		assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
		assertEquals(V2_0, ical.getVersion());
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
			assertEquals(0, rdate.getDates().size());
			assertEquals(1, rdate.getPeriods().size());
			assertEquals(new Period(utc("2006-01-02 20:00:00"), Duration.builder().hours(2).build()), rdate.getPeriods().get(0));
			assertNull(rdate.getParameters().getTimezoneId());

			assertEquals("Event #2", event.getSummary().getValue());
			assertEquals("We are having a meeting all this week at 12pm for one hour, with an additional meeting on the first day 2 hours long.\nPlease bring your own lunch for the 12 pm meetings.", event.getDescription().getValue());
			assertEquals("00959BC664CA650E933C892C@example.com", event.getUid().getValue());
		}
		{
			VEvent event = ical.getEvents().get(1);
			assertSize(event, 0, 6);

			assertEquals(utc("2006-02-06 00:11:21"), event.getDateTimeStamp().getValue());
			assertEquals(utc("2006-01-04 19:00:00"), event.getDateStart().getValue());
			assertNull(event.getDateStart().getParameters().getTimezoneId());
			assertEquals(Duration.builder().hours(1).build(), event.getDuration().getValue());

			assertEquals(utc("2006-01-04 17:00:00"), event.getRecurrenceId().getValue());
			assertNull(event.getRecurrenceId().getParameters().getTimezoneId());
			assertEquals("Event #2 bis", event.getSummary().getValue());
			assertEquals("00959BC664CA650E933C892C@example.com", event.getUid().getValue());
		}

		//assert the timezone info
		TimezoneInfo tzinfo = ical.getTimezoneInfo();
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
				assertEquals(new DateTimeComponents(2000, 4, 4, 2, 0, 0, false), daylight.getDateStart().getValue().getRawComponents());

				Recurrence rrule = daylight.getRecurrenceRule().getValue();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(new ByDay(1, DayOfWeek.SUNDAY)), rrule.getByDay());
				assertEquals(Arrays.asList(4), rrule.getByMonth());

				assertEquals("EDT", daylight.getTimezoneNames().get(0).getValue());
				assertEquals(new UtcOffset(false, 5, 0), daylight.getTimezoneOffsetFrom().getValue());
				assertEquals(new UtcOffset(false, 4, 0), daylight.getTimezoneOffsetTo().getValue());
			}
			{
				StandardTime standard = timezone.getStandardTimes().get(0);
				assertSize(standard, 0, 5);
				assertEquals(date("2000-10-26 02:00:00"), standard.getDateStart().getValue());
				assertEquals(new DateTimeComponents(2000, 10, 26, 2, 0, 0, false), standard.getDateStart().getValue().getRawComponents());

				Recurrence rrule = standard.getRecurrenceRule().getValue();
				assertEquals(Frequency.YEARLY, rrule.getFrequency());
				assertEquals(Arrays.asList(new ByDay(-1, DayOfWeek.SUNDAY)), rrule.getByDay());
				assertEquals(Arrays.asList(10), rrule.getByMonth());

				assertEquals("EST", standard.getTimezoneNames().get(0).getValue());
				assertEquals(new UtcOffset(false, 4, 0), standard.getTimezoneOffsetFrom().getValue());
				assertEquals(new UtcOffset(false, 5, 0), standard.getTimezoneOffsetTo().getValue());
			}

			assertFalse(it.hasNext());
		}

		VTimezone timezone = tzinfo.getComponents().iterator().next();
		VEvent event = ical.getEvents().get(0);

		DateStart dtstart = event.getDateStart();
		assertEquals(timezone, tzinfo.getTimezone(dtstart).getComponent());
		TimeZone dtstartTz = tzinfo.getTimezone(dtstart).getTimeZone();
		assertEquals("US/Eastern", dtstartTz.getID());
		assertTrue(dtstartTz instanceof ICalTimeZone);

		RecurrenceDates rdate = event.getRecurrenceDates().get(0);
		assertEquals(timezone, tzinfo.getTimezone(rdate).getComponent());
		assertEquals(dtstartTz, tzinfo.getTimezone(rdate).getTimeZone());

		VEvent event2 = ical.getEvents().get(1);

		dtstart = event2.getDateStart();
		assertEquals(timezone, tzinfo.getTimezone(dtstart).getComponent());
		assertEquals(dtstartTz, tzinfo.getTimezone(dtstart).getTimeZone());

		RecurrenceId rid = event2.getRecurrenceId();
		assertEquals(timezone, tzinfo.getTimezone(rid).getComponent());
		assertEquals(dtstartTz, tzinfo.getTimezone(rid).getTimeZone());

		assertValidate(ical).versions(V2_0).run();

		assertNull(reader.readNext());
	}

	@Test
	public void write_example2() throws Throwable {
		TimeZone eastern = TimeZone.getTimeZone("US/Eastern");

		//see: RFC 6321 p.51
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("-//Example Inc.//Example Client//EN");
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

		VTimezone usEasternTz = new VTimezone("US/Eastern");
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

			Recurrence rrule = new Recurrence.Builder(Frequency.YEARLY).byDay(-1, DayOfWeek.SUNDAY).byMonth(10).build();
			standard.setRecurrenceRule(rrule);

			standard.addTimezoneName("EST");
			standard.setTimezoneOffsetFrom(new UtcOffset(false, 4, 0));
			standard.setTimezoneOffsetTo(new UtcOffset(false, 5, 0));

			usEasternTz.addStandardTime(standard);
		}

		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		tzinfo.setDefaultTimezone(new TimezoneAssignment(eastern, usEasternTz));
		assertExample(ical, "rfc6321-example2.xml");
	}

	private XCalDocument read(String file) throws SAXException, IOException {
		return new XCalDocument(getClass().getResourceAsStream(file));
	}

	private void assertExample(ICalendar ical, String exampleFileName) {
		XCalDocument xcal = new XCalDocument();
		XCalDocumentStreamWriter writer = xcal.writer();
		writer.write(ical);

		try {
			Document expected = XmlUtils.toDocument(getClass().getResourceAsStream(exampleFileName));
			Document actual = xcal.getDocument();

			Map<String, String> properties = new HashMap<String, String>();
			properties.put(OutputKeys.INDENT, "yes");
			properties.put("{http://xml.apache.org/xslt}indent-amount", "3");
			String expectedStr = XmlUtils.toString(expected, properties);
			String actualStr = XmlUtils.toString(actual, properties);

			assertXMLEqual("Expected:\n" + expectedStr + "\n\nActual:\n" + actualStr, expected, actual);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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
