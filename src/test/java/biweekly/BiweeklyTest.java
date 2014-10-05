package biweekly;

import static biweekly.util.StringUtils.NEWLINE;
import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.assertRegex;
import static biweekly.util.TestUtils.assertWarningsLists;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import biweekly.component.ICalComponent;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.ProductId;
import biweekly.util.XmlUtils;

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
public class BiweeklyTest {
	@Test
	public void parse_first() {
		//@formatter:off
		String icalStr =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2.0\r\n" +
		"PRODID:prodid\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on
		List<List<String>> warnings = new ArrayList<List<String>>();

		ICalendar ical = Biweekly.parse(icalStr).warnings(warnings).first();

		assertEquals(ICalVersion.V2_0, ical.getVersion());
		assertEquals("prodid", ical.getProductId().getValue());
		assertWarningsLists(warnings, 0);
	}

	@Test
	public void parse_all() {
		//@formatter:off
		String icalStr =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2.0\r\n" +
		"PRODID:one\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2.0\r\n" +
		"PRODID:two\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on
		List<List<String>> warnings = new ArrayList<List<String>>();

		List<ICalendar> icals = Biweekly.parse(icalStr).warnings(warnings).all();
		Iterator<ICalendar> it = icals.iterator();

		ICalendar ical = it.next();
		assertEquals(ICalVersion.V2_0, ical.getVersion());
		assertEquals("one", ical.getProductId().getValue());

		ical = it.next();
		assertEquals(ICalVersion.V2_0, ical.getVersion());
		assertEquals("two", ical.getProductId().getValue());

		assertWarningsLists(warnings, 0, 0);
		assertFalse(it.hasNext());
	}

	@Test
	public void parse_register() {
		//@formatter:off
		String icalStr =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2.0\r\n" +
		"PRODID:prodid\r\n" +
		"X-TEST:one\r\n" +
		"BEGIN:X-VPARTY\r\n" +
		"END:X-VPARTY\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		ICalendar ical = Biweekly.parse(icalStr).register(new TestPropertyMarshaller()).register(new PartyMarshaller()).first();

		assertIntEquals(1, ical.getProperty(TestProperty.class).getNumber());
		assertEquals(0, ical.getExperimentalProperties().size());
		assertNotNull(ical.getComponent(Party.class));
		assertEquals(0, ical.getExperimentalComponents().size());
	}

	@Test
	public void parse_caretDecoding() {
		//@formatter:off
		String icalStr =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2.0\r\n" +
		"PRODID;X-TEST=the ^'best^' app:prodid\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		//defaults to true
		ICalendar ical = Biweekly.parse(icalStr).first();
		assertEquals("the \"best\" app", ical.getProductId().getParameter("X-TEST"));

		ical = Biweekly.parse(icalStr).caretDecoding(true).first();
		assertEquals("the \"best\" app", ical.getProductId().getParameter("X-TEST"));

		ical = Biweekly.parse(icalStr).caretDecoding(false).first();
		assertEquals("the ^'best^' app", ical.getProductId().getParameter("X-TEST"));
	}

	@Test
	public void parseXml_first() throws Throwable {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		"<icalendar xmlns=\"urn:ietf:params:xml:ns:icalendar-2.0\">" +
		  "<vcalendar>" +
		    "<properties>" +
		      "<prodid><text>one</text></prodid>" +
		      "<version><text>2.0</text></version>" +
		    "</properties>" +
		  "</vcalendar>" +
		  "<vcalendar>" +
		    "<properties>" +
		      "<prodid><text>two</text></prodid>" +
		      "<version><text>2.0</text></version>" +
		    "</properties>" +
		  "</vcalendar>" +
		"</icalendar>";
		//@formatter:on
		List<List<String>> warnings = new ArrayList<List<String>>();

		ICalendar ical = Biweekly.parseXml(xml).warnings(warnings).first();

		assertEquals(ICalVersion.V2_0, ical.getVersion());
		assertEquals("one", ical.getProductId().getValue());
		assertWarningsLists(warnings, 0);
	}

	@Test
	public void parseXml_all() throws Throwable {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		"<icalendar xmlns=\"urn:ietf:params:xml:ns:icalendar-2.0\">" +
		  "<vcalendar>" +
		    "<properties>" +
		      "<prodid><text>one</text></prodid>" +
		      "<version><text>2.0</text></version>" +
		    "</properties>" +
		  "</vcalendar>" +
		  "<vcalendar>" +
		    "<properties>" +
		      "<prodid><text>two</text></prodid>" +
		      "<version><text>2.0</text></version>" +
		    "</properties>" +
		  "</vcalendar>" +
		"</icalendar>";
		//@formatter:on
		List<List<String>> warnings = new ArrayList<List<String>>();

		List<ICalendar> icals = Biweekly.parseXml(xml).warnings(warnings).all();
		Iterator<ICalendar> it = icals.iterator();

		ICalendar ical = it.next();
		assertEquals(ICalVersion.V2_0, ical.getVersion());
		assertEquals("one", ical.getProductId().getValue());

		ical = it.next();
		assertEquals(ICalVersion.V2_0, ical.getVersion());
		assertEquals("two", ical.getProductId().getValue());

		assertWarningsLists(warnings, 0, 0);
		assertFalse(it.hasNext());
	}

	@Test
	public void parseXml_register() throws SAXException {
		//@formatter:off
		String xml =
		"<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		"<icalendar xmlns=\"urn:ietf:params:xml:ns:icalendar-2.0\">" +
		  "<vcalendar>" +
		    "<properties>" +
		      "<prodid><text>prodid</text></prodid>" +
		      "<version><text>2.0</text></version>" +
		      "<x-test><text>one</text></x-test>" +
		    "</properties>" +
		    "<components>" +
			    "<x-vparty>" +
			    "</x-vparty>" +
		    "</components>" +
		  "</vcalendar>" +
		"</icalendar>";
		//@formatter:on

		ICalendar ical = Biweekly.parseXml(xml).register(new TestPropertyMarshaller()).register(new PartyMarshaller()).first();

		assertIntEquals(1, ical.getProperty(TestProperty.class).getNumber());
		assertEquals(0, ical.getExperimentalProperties().size());
		assertNotNull(ical.getComponent(Party.class));
		assertEquals(0, ical.getExperimentalComponents().size());
	}

	@Test(expected = SAXException.class)
	public void parseXml_invalid() throws Throwable {
		String xml = "invalid-xml";
		Biweekly.parseXml(xml).first();
	}

	@Test
	public void write_one() {
		ICalendar ical = new ICalendar();

		//@formatter:off
		String expected =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2\\.0\r\n" +
		"PRODID:.*?\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = Biweekly.write(ical).go();

		assertRegex(expected, actual);
	}

	@Test
	public void write_multiple() {
		ICalendar ical1 = new ICalendar();

		ICalendar ical2 = new ICalendar();
		ical2.addExperimentalProperty("X-TEST1", "value1");

		ICalendar ical3 = new ICalendar();
		ical3.addExperimentalProperty("X-TEST2", "value2");

		//@formatter:off
		String expected =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2\\.0\r\n" +
		"PRODID:.*?\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2\\.0\r\n" +
		"PRODID:.*?\r\n" +
		"X-TEST1:value1\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2\\.0\r\n" +
		"PRODID:.*?\r\n" +
		"X-TEST2:value2\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = Biweekly.write(ical1, ical2, ical3).go();

		assertRegex(expected, actual);
	}

	@Test
	public void write_caretEncoding() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId("prodid");
		ical.getProductId().addParameter("X-TEST", "the \"best\" app");

		//default is "false"
		//@formatter:off
		String expected =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2\\.0\r\n" +
		"PRODID;X-TEST=the 'best' app:prodid\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on
		String actual = Biweekly.write(ical).go();
		assertRegex(expected, actual);

		//@formatter:off
		expected =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2\\.0\r\n" +
		"PRODID;X-TEST=the \\^'best\\^' app:prodid\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on
		actual = Biweekly.write(ical).caretEncoding(true).go();
		assertRegex(expected, actual);

		//@formatter:off
		expected =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2\\.0\r\n" +
		"PRODID;X-TEST=the 'best' app:prodid\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on
		actual = Biweekly.write(ical).caretEncoding(false).go();
		assertRegex(expected, actual);
	}

	@Test
	public void write_register() {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty(1));
		ical.addComponent(new Party());

		//@formatter:off
		String expected =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2\\.0\r\n" +
		"PRODID:.*?\r\n" +
		"X-TEST:one\r\n" +
		"BEGIN:X-VPARTY\r\n" +
		"END:X-VPARTY\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		String actual = Biweekly.write(ical).register(new TestPropertyMarshaller()).register(new PartyMarshaller()).go();

		assertRegex(expected, actual);
	}

	@Test
	public void writeXml() throws Throwable {
		ICalendar ical1 = new ICalendar();
		ical1.setProductId((String) null);

		ICalendar ical2 = new ICalendar();
		ical2.setProductId((String) null);
		ical2.addExperimentalProperty("X-TEST1", "value1");

		ICalendar ical3 = new ICalendar();
		ical3.setProductId((String) null);
		ical3.addExperimentalProperty("X-TEST2", "value2");

		//@formatter:off
		Document expected = XmlUtils.toDocument(
		 "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		 "<icalendar xmlns=\"urn:ietf:params:xml:ns:icalendar-2.0\">" +
		   "<vcalendar>" +
		     "<properties>" +
		       "<version><text>2.0</text></version>" +
		     "</properties>" +
		   "</vcalendar>" +
		   "<vcalendar>" +
		     "<properties>" +
		       "<version><text>2.0</text></version>" +
		       "<x-test1><unknown>value1</unknown></x-test1>" +
		     "</properties>" +
		   "</vcalendar>" +
		   "<vcalendar>" +
		     "<properties>" +
		       "<version><text>2.0</text></version>" +
		       "<x-test2><unknown>value2</unknown></x-test2>" +
		     "</properties>" +
		   "</vcalendar>" +
		 "</icalendar>"
		);
		//@formatter:on

		Document actual = Biweekly.writeXml(ical1, ical2, ical3).dom();

		assertXMLEqual(expected, actual);
	}

	@Test
	public void writeXml_register() throws Throwable {
		ICalendar ical = new ICalendar();

		ProductId prodId = new ProductId("value");
		prodId.setParameter("X-TEST1", "value1");
		prodId.setParameter("X-TEST2", "value2");
		ical.setProductId(prodId);

		ical.addProperty(new TestProperty(1));
		ical.addComponent(new Party());

		//@formatter:off
		Document expected = XmlUtils.toDocument(
		 "<?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
		 "<icalendar xmlns=\"urn:ietf:params:xml:ns:icalendar-2.0\">" +
		   "<vcalendar>" +
		     "<properties>" +
		     	"<version><text>2.0</text></version>" +
		     	"<prodid>" +
		     		"<parameters>" +
		     			"<x-test1><unknown>value1</unknown></x-test1>" +
		     			"<x-test2><text>value2</text></x-test2>" +
		     		"</parameters>" +
		     		"<text>value</text>" +
		     	"</prodid>" +
		     	"<x-test><unknown>one</unknown></x-test>" +
		     "</properties>" +
		     "<components>" +
		     	"<x-vparty/>" +
		     "</components>" +
		   "</vcalendar>" +
		 "</icalendar>"
		);
		//@formatter:on

		Document actual = Biweekly.writeXml(ical).register(new TestPropertyMarshaller()).register(new PartyMarshaller()).register("X-TEST2", ICalDataType.TEXT).dom();

		assertXMLEqual(expected, actual);
	}

	@Test
	public void writeJson_single() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId((String) null);

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		String actual = Biweekly.writeJson(ical).go();

		assertEquals(expected, actual);
	}

	@Test
	public void writeJson_multiple() throws Throwable {
		ICalendar ical1 = new ICalendar();
		ical1.setProductId((String) null);

		ICalendar ical2 = new ICalendar();
		ical2.setProductId((String) null);
		ical2.addExperimentalProperty("X-TEST1", "value1");

		ICalendar ical3 = new ICalendar();
		ical3.setProductId((String) null);
		ical3.addExperimentalProperty("X-TEST2", "value2");

		//@formatter:off
		String expected =
		"[" +
			"[\"vcalendar\"," +
				"[" +
					"[\"version\",{},\"text\",\"2.0\"]" +
				"]," +
				"[" +
				"]" +
			"]," +
			"[\"vcalendar\"," +
				"[" +
					"[\"version\",{},\"text\",\"2.0\"]," +
					"[\"x-test1\",{},\"unknown\",\"value1\"]" +
				"]," +
				"[" +
				"]" +
			"]," +
			"[\"vcalendar\"," +
				"[" +
					"[\"version\",{},\"text\",\"2.0\"]," +
					"[\"x-test2\",{},\"unknown\",\"value2\"]" +
				"]," +
				"[" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		String actual = Biweekly.writeJson(ical1, ical2, ical3).go();

		assertEquals(expected, actual);
	}

	@Test
	public void writeJson_indent() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId((String) null);

		//@formatter:off
		String expected =
		"[" + NEWLINE +
		"\"vcalendar\",[[" + NEWLINE +
		"  \"version\",{},\"text\",\"2.0\"]],[]]";
		//@formatter:on

		String actual = Biweekly.writeJson(ical).indent(true).go();

		assertEquals(expected, actual);
	}

	@Test
	public void writeJson_register() throws Throwable {
		ICalendar ical = new ICalendar();
		ical.setProductId((String) null);
		ical.addProperty(new TestProperty(1));
		ical.addComponent(new Party());

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"x-test\",{},\"unknown\",\"one\"]" +
			"]," +
			"[" +
				"[\"x-vparty\"," +
					"[" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		String actual = Biweekly.writeJson(ical).register(new TestPropertyMarshaller()).register(new PartyMarshaller()).go();

		assertEquals(expected, actual);
	}

	private class TestProperty extends ICalProperty {
		private Integer number;

		public TestProperty(Integer number) {
			this.number = number;
		}

		private Integer getNumber() {
			return number;
		}
	}

	private class TestPropertyMarshaller extends ICalPropertyScribe<TestProperty> {
		private TestPropertyMarshaller() {
			super(TestProperty.class, "X-TEST", null);
		}

		@Override
		protected String _writeText(TestProperty property, WriteContext context) {
			Integer value = property.getNumber();
			return (value == 1) ? "one" : value.toString();
		}

		@Override
		protected TestProperty _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			Integer number;
			if (value.equals("one")) {
				number = 1;
			} else {
				throw new CannotParseException("wat");
			}

			TestProperty prop = new TestProperty(number);
			return prop;
		}

		@Override
		protected TestProperty _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
			return _parseText(element.first(ICalDataType.TEXT), null, parameters, null);
		}
	}

	private class PartyMarshaller extends ICalComponentScribe<Party> {
		private PartyMarshaller() {
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
}
