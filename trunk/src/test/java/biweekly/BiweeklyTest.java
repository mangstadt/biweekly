package biweekly;

import static biweekly.util.TestUtils.assertRegex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.io.CannotParseException;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
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

		assertEquals("2.0", ical.getVersion().getMaxVersion());
		assertEquals("prodid", ical.getProductId().getValue());
		assertEquals(1, warnings.size());
	}

	@Test
	public void parse_all() {
		//@formatter:off
		String icalStr =
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2.0\r\n" +
		"PRODID:prodid1\r\n" +
		"END:VCALENDAR\r\n" +
		"BEGIN:VCALENDAR\r\n" +
		"VERSION:2.0\r\n" +
		"PRODID:prodid2\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on
		List<List<String>> warnings = new ArrayList<List<String>>();

		List<ICalendar> icals = Biweekly.parse(icalStr).warnings(warnings).all();
		Iterator<ICalendar> it = icals.iterator();

		ICalendar ical = it.next();
		assertEquals("2.0", ical.getVersion().getMaxVersion());
		assertEquals("prodid1", ical.getProductId().getValue());

		ical = it.next();
		assertEquals("2.0", ical.getVersion().getMaxVersion());
		assertEquals("prodid2", ical.getProductId().getValue());

		assertEquals(2, warnings.size());
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

		assertEquals(Integer.valueOf(1), ical.getProperty(TestProperty.class).getNumber());
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
		"PRODID;X-TEST=the ^'best^' app:prodid1\r\n" +
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
	public void write_one_with_warnings() {
		ICalendar ical = new ICalendar();
		ical.addProperty(new TestProperty()); //no marshaller registered

		List<String> warnings = new ArrayList<String>();
		Biweekly.write(ical).warnings(warnings).go();

		assertFalse(warnings.isEmpty());
	}

	@Test
	public void write_one_without_warnings() {
		ICalendar ical = new ICalendar();

		List<String> warnings = new ArrayList<String>();
		Biweekly.write(ical).warnings(warnings).go();

		assertTrue(warnings.isEmpty());
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
	public void write_multiple_warnings() {
		ICalendar ical1 = new ICalendar();

		ICalendar ical2 = new ICalendar();
		ical2.addProperty(new TestProperty());

		ICalendar ical3 = new ICalendar();

		List<List<String>> warnings = new ArrayList<List<String>>();
		Biweekly.write(ical1, ical2, ical3).warnings(warnings).go();

		assertEquals(3, warnings.size());
		assertTrue(warnings.get(0).isEmpty());
		assertFalse(warnings.get(1).isEmpty()); //no marshaller
		assertTrue(warnings.get(2).isEmpty());
	}

	@Test
	public void write_caretEncoding() throws Exception {
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

	class TestProperty extends ICalProperty {
		Integer number;

		Integer getNumber() {
			return number;
		}

		void setNumber(Integer number) {
			this.number = number;
		}
	}

	class TestPropertyMarshaller extends ICalPropertyMarshaller<TestProperty> {
		TestPropertyMarshaller() {
			super(TestProperty.class, "X-TEST");
		}

		@Override
		protected String _writeText(TestProperty property) {
			return property.getNumber().toString();
		}

		@Override
		protected TestProperty _parseText(String value, ICalParameters parameters, List<String> warnings) {
			Integer number;
			if (value.equals("one")) {
				number = 1;
			} else {
				throw new CannotParseException("wat");
			}

			TestProperty prop = new TestProperty();
			prop.setNumber(number);
			return prop;
		}
	}

	class PartyMarshaller extends ICalComponentMarshaller<Party> {
		PartyMarshaller() {
			super(Party.class, "X-VPARTY");
		}

		@Override
		public Party newInstance() {
			return new Party();
		}
	}

	class Party extends ICalComponent {
		//empty
	}
}
