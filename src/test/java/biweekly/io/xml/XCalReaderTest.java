package biweekly.io.xml;

import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertSize;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.assertVersion;
import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.utc;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TimeZone;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

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
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.CannotParseScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.SkipMeScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.DateStart;
import biweekly.property.ICalProperty;
import biweekly.property.ProductId;
import biweekly.property.RawProperty;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceId;
import biweekly.property.Summary;
import biweekly.property.Xml;
import biweekly.util.DateTimeComponents;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.Duration;
import biweekly.util.IOUtils;
import biweekly.util.Period;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.ByDay;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;
import biweekly.util.UtcOffset;
import biweekly.util.XmlUtils;

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
public class XCalReaderTest {
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
		XMLUnit.setIgnoreWhitespace(true);
	}

	@Test
	public void read_single() throws Exception {
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
		"</icalendar>";
		//@formatter:on

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
			assertEquals(V2_0, ical.getVersion());

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 1);
			assertEquals("Team Meeting", event.getSummary().getValue());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_multiple() throws Exception {
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

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
			assertVersion(V2_0, ical);

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 1);
			assertEquals("Team Meeting", event.getSummary().getValue());

			assertWarnings(0, reader);
		}

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
			assertVersion(V2_0, ical);

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 1);
			assertEquals("Team Happy Hour", event.getSummary().getValue());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_default_namespace() throws Exception {
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

		XCalReader reader = new XCalReader(xml);

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_wrong_namespace() throws Exception {
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

		XCalReader reader = new XCalReader(xml);

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_namespace_prefix() throws Exception {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		"<x:icalendar xmlns:x=\"" + XCAL_NS + "\">" +
			"<x:vcalendar>" +
				"<x:properties>" +
					"<x:prodid><x:text>-//Example Inc.//Example Client//EN</x:text></x:prodid>" +
					"<x:version><x:text>2.0</x:text></x:version>" +
				"</x:properties>" +
				"<x:components>" +
					"<x:vevent>" +
						"<x:properties>" +
							"<x:summary><x:text>Team Meeting</x:text></x:summary>" +
						"</x:properties>" +
					"</x:vevent>" +
				"</x:components>" +
			"</x:vcalendar>" +
		"</x:icalendar>";
		//@formatter:on

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
			assertVersion(V2_0, ical);

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 1);
			assertEquals("Team Meeting", event.getSummary().getValue());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_preserve_whitespace() throws Exception {
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

		XCalReader reader = new XCalReader(xml);
		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 1);

			Summary prop = ical.getProperty(Summary.class);
			assertEquals("  This \t  is \n   a   note ", prop.getValue());
			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_ignore_other_namespaces() throws Exception {
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

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 1);

			ProductId productId = ical.getProductId();
			assertEquals("-//Example Inc.//Example Client//EN", productId.getValue());
			assertEquals("bar", productId.getParameter("x-foo"));

			assertVersion(V2_0, ical);

			VEvent event = ical.getEvents().get(0);
			assertSize(event, 0, 1);
			assertEquals("Team Meeting", event.getSummary().getValue());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_identical_element_names() throws Exception {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<icalendar><icalendar>value1</icalendar></icalendar>" +
					"<vcalendar><vcalendar>value2</vcalendar></vcalendar>" +
					"<properties><properties>value3</properties></properties>" +
					"<components><components>value4</components></components>" +
					"<parameters>" +
						"<parameters>" +
							"<parameters><parameters>param</parameters></parameters>" +
						"</parameters>" +
						"<text>value5</text>" +
					"</parameters>" +
				"</properties>" +
				"<components>" +
					"<components>" +
						"<properties>" +
							"<icalendar><icalendar>value1</icalendar></icalendar>" +
							"<vcalendar><vcalendar>value2</vcalendar></vcalendar>" +
							"<properties><properties>value3</properties></properties>" +
							"<components><components>value4</components></components>" +
							"<parameters>" +
								"<parameters>" +
									"<parameters><parameters>param</parameters></parameters>" +
								"</parameters>" +
								"<text>value5</text>" +
							"</parameters>" +
						"</properties>" +
					"</components>" +
				"</components>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 5);

			assertEquals("value1", ical.getExperimentalProperty("icalendar").getValue());
			assertEquals("value2", ical.getExperimentalProperty("vcalendar").getValue());
			assertEquals("value3", ical.getExperimentalProperty("properties").getValue());
			assertEquals("value4", ical.getExperimentalProperty("components").getValue());
			RawProperty property = ical.getExperimentalProperty("parameters");

			assertEquals("value5", property.getValue());
			assertEquals("param", property.getParameter("parameters"));

			{
				RawComponent component = ical.getExperimentalComponent("components");
				assertSize(component, 0, 5);

				assertEquals("value1", component.getExperimentalProperty("icalendar").getValue());
				assertEquals("value2", component.getExperimentalProperty("vcalendar").getValue());
				assertEquals("value3", component.getExperimentalProperty("properties").getValue());
				assertEquals("value4", component.getExperimentalProperty("components").getValue());
				property = component.getExperimentalProperty("parameters");

				assertEquals("value5", property.getValue());
				assertEquals("param", property.getParameter("parameters"));
			}

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_bad_xml() throws Exception {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<prodid><text>-//Example Inc.//Example Client//EN</prodid>" +
					"<version><text>2.0</text></version>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalReader reader = new XCalReader(xml);

		try {
			reader.readNext();
			fail();
		} catch (IOException e) {
			Throwable cause = e.getCause();
			assertTrue(cause instanceof TransformerException);

			cause = cause.getCause();
			assertTrue(cause instanceof SAXException);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_multiple_icalendar_elements() throws Exception {
		//@formatter:off
		String xml =
		"<root>" +
			"<icalendar xmlns=\"" + XCAL_NS + "\">" +
				"<vcalendar>" +
					"<properties>" +
						"<prodid><text>value1</text></prodid>" +
						"<version><text>2.0</text></version>" +
					"</properties>" +
				"</vcalendar>" +
			"</icalendar>" +
			"<icalendar xmlns=\"" + XCAL_NS + "\">" +
				"<vcalendar>" +
					"<properties>" +
						"<prodid><text>value2</text></prodid>" +
						"<version><text>2.0</text></version>" +
					"</properties>" +
				"</vcalendar>" +
			"</icalendar>" +
		"</root>";
		//@formatter:on

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 1);

			assertEquals("value1", ical.getProductId().getValue());
			assertVersion(V2_0, ical);

			assertWarnings(0, reader);
		}

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 1);

			assertEquals("value2", ical.getProductId().getValue());
			assertVersion(V2_0, ical);

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_parameters() throws Exception {
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

		XCalReader reader = new XCalReader(xml);

		{
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

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_experimental_component() throws Exception {
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

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 0);

			RawComponent comp = ical.getExperimentalComponent("x-party");
			assertSize(comp, 0, 1);
			Summary prop = comp.getProperty(Summary.class);
			assertEquals("Party", prop.getValue());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_experimental_component_with_marshaller() throws Exception {
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

		XCalReader reader = new XCalReader(xml);
		reader.registerScribe(new PartyMarshaller());

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 0);

			Party comp = ical.getComponent(Party.class);
			assertSize(comp, 0, 1);
			Summary prop = comp.getProperty(Summary.class);
			assertEquals("Party", prop.getValue());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_experimental_property() throws Exception {
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

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 1);

			RawProperty prop = ical.getExperimentalProperty("X-BOSS");
			assertEquals("John Doe", prop.getValue());
			assertEquals(ICalDataType.TEXT, prop.getDataType());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_experimental_property_with_namespace_with_marshaller() throws Exception {
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

		XCalReader reader = new XCalReader(xml);
		reader.registerScribe(new CompanyMarshaller());

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 1);

			Company prop = ical.getProperty(Company.class);
			assertEquals("John Doe", prop.getBoss());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_experimental_property_with_namespace_without_marshaller() throws Exception {
		//@formatter:off
		String xml =
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<m:company xmlns:m=\"http://example.com\">" +
						"<parameters>" +
							"<x-foo><text>bar</text></x-foo>" +
						"</parameters>" +
						"<m:boss prefix=\"Mr\">John Doe</m:boss>" +
					"</m:company>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 1);

			Xml prop = ical.getProperty(Xml.class);
			Document expected = XmlUtils.toDocument("<m:company xmlns:m=\"http://example.com\"><m:boss prefix=\"Mr\">John Doe</m:boss></m:company>");
			assertXMLEqual(expected, prop.getValue());
			assertEquals("bar", prop.getParameter("x-foo"));

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void skipMeException() throws Exception {
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

		XCalReader reader = new XCalReader(xml);
		reader.registerScribe(new SkipMeScribe());

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 1);

			RawProperty property = ical.getExperimentalProperty("X-FOO");
			assertNull(property.getDataType());
			assertEquals("X-FOO", property.getName());
			assertEquals("bar", property.getValue());
			assertWarnings(1, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void cannotParseException() throws Exception {
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

		XCalReader reader = new XCalReader(xml);
		reader.registerScribe(new CannotParseScribe());

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 2);

			RawProperty property = ical.getExperimentalProperty("x-foo");
			assertEquals(null, property.getDataType());
			assertEquals("X-FOO", property.getName());
			assertEquals("bar", property.getValue());

			Xml prop = ical.getProperty(Xml.class);
			Document expected = XmlUtils.toDocument("<cannotparse xmlns=\"" + XCAL_NS + "\"><unknown>value</unknown></cannotparse>");
			assertXMLEqual(expected, prop.getValue());
			assertWarnings(1, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void close_before_stream_ends() throws Exception {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		"<icalendar xmlns=\"" + XCAL_NS + "\">" +
			"<vcalendar>" +
				"<properties>" +
					"<prodid><text>value1</text></prodid>" +
				"</properties>" +
			"</vcalendar>" +
			"<vcalendar>" +
				"<properties>" +
					"<prodid><text>value2</text></prodid>" +
				"</properties>" +
			"</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 1);

			assertEquals("value1", ical.getProductId().getValue());

			assertWarnings(0, reader);
		}

		reader.close();
		assertNull(reader.readNext());
	}

	@Test
	public void read_utf8() throws Exception {
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
		Writer writer = IOUtils.utf8Writer(file);
		writer.write(xml);
		writer.close();

		XCalReader reader = new XCalReader(xml);

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 0, 1);

			Summary prop = ical.getProperty(Summary.class);
			assertEquals("\u1e66ummary", prop.getValue());

			assertWarnings(0, reader);
		}

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_empty() throws Exception {
		String xml = "<icalendar xmlns=\"" + XCAL_NS + "\"/>";
		XCalReader reader = new XCalReader(xml);

		assertNull(reader.readNext());
		reader.close();
	}

	@Test
	public void read_example1() throws Throwable {
		XCalReader reader = read("rfc6321-example1.xml");

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 1, 2);
			assertTrue(ical.getCalendarScale().isGregorian());
			assertEquals("-//Example Inc.//Example Calendar//EN", ical.getProductId().getValue());
			assertVersion(V2_0, ical);

			{
				VEvent event = ical.getEvents().get(0);
				assertSize(event, 0, 4);

				assertEquals(utc("2008-02-05 19:12:24"), event.getDateTimeStamp().getValue());
				assertEquals(date("2008-10-06"), event.getDateStart().getValue());
				assertEquals("Planning meeting", event.getSummary().getValue());
				assertEquals("4088E990AD89CB3DBB484909", event.getUid().getValue());
			}

			assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();
		}

		assertNull(reader.readNext());
	}

	@Test
	public void read_example2() throws Throwable {
		XCalReader reader = read("rfc6321-example2.xml");

		{
			ICalendar ical = reader.readNext();
			assertSize(ical, 2, 1);
			assertEquals("-//Example Inc.//Example Client//EN", ical.getProductId().getValue());
			assertVersion(V2_0, ical);
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

			assertValidate(ical).versions(V2_0_DEPRECATED, V2_0).run();
		}

		assertNull(reader.readNext());
	}

	private XCalReader read(String file) throws SAXException, IOException {
		return new XCalReader(getClass().getResourceAsStream(file));
	}

	private class CompanyMarshaller extends ICalPropertyScribe<Company> {
		public CompanyMarshaller() {
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
