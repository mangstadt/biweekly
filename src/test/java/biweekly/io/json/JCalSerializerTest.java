package biweekly.io.json;

import static biweekly.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import biweekly.ICalendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/*
 Copyright (c) 2013-2021, Michael Angstadt
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
 * @author Buddy Gorven
 * @author Michael Angstadt
 */
public class JCalSerializerTest {
	private ObjectMapper mapper;

	@Before
	public void before() {
		mapper = new ObjectMapper();
	}

	@Test
	public void serialize_single() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("value1");

		JCalModule module = new JCalModule();
		mapper.registerModule(module);
		String actual = mapper.writeValueAsString(ical);

		//@formatter:off
		String expected =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"prodid\",{},\"text\",\"value1\"]" +
			"]," +
			"[]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void serialize_prettyPrint() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("value1");

		JCalModule module = new JCalModule();
		mapper.registerModule(module);
		mapper.setDefaultPrettyPrinter(new JCalPrettyPrinter());
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		String actual = mapper.writeValueAsString(ical);

		//@formatter:off
		String expected =
		"[" + NEWLINE +
		"  \"vcalendar\"," + NEWLINE +
		"  [" + NEWLINE +
		"    [ \"version\", { }, \"text\", \"2.0\" ]," + NEWLINE +
		"    [ \"prodid\", { }, \"text\", \"value1\" ]" + NEWLINE +
		"  ]," + NEWLINE +
		"  [ ]" + NEWLINE +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	//TODO test setScribe() and setTimezoneInfo()

	@Test
	public void serialize_multiple() throws Exception {
		ICalendar ical1 = new ICalendar();
		ical1.getProperties().clear();
		ical1.setProductId("value1");

		ICalendar ical2 = new ICalendar();
		ical2.getProperties().clear();
		ical2.setProductId("value2");

		JCalModule module = new JCalModule();
		mapper.registerModule(module);
		String actual = mapper.writeValueAsString(Arrays.asList(ical1, ical2));

		//@formatter:off
		String expected =
		"[" +
			"[\"vcalendar\"," +
				"[" +
					"[\"version\",{},\"text\",\"2.0\"]," +
					"[\"prodid\",{},\"text\",\"value1\"]" +
				"]," +
				"[]" +
			"]," +
			"[\"vcalendar\"," +
				"[" +
					"[\"version\",{},\"text\",\"2.0\"]," +
					"[\"prodid\",{},\"text\",\"value2\"]" +
				"]," +
				"[]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void container() throws Exception {
		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setProductId("value1");
		Container container = new Container(ical);

		StringWriter result = new StringWriter();
		mapper.writeValue(result, container);
		String actual = result.toString();

		//@formatter:off
		String expected =
		"{" +
			"\"events\":[\"vcalendar\"," +
				"[" +
					"[\"version\",{},\"text\",\"2.0\"]," +
					"[\"prodid\",{},\"text\",\"value1\"]" +
				"]," +
				"[]" +
			"]" +
		"}";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void container_null() throws Exception {
		Container container = new Container(null);

		StringWriter result = new StringWriter();
		mapper.writeValue(result, container);
		String actual = result.toString();

		//@formatter:off
		String expected =
		"{" +
			"\"events\":null" +
		"}";
		//@formatter:on
		assertEquals(expected, actual);
	}

	private static class Container {
		private final ICalendar events;

		public Container(ICalendar events) {
			this.events = events;
		}

		@JsonSerialize(using = JCalSerializer.class)
		public ICalendar getEvents() {
			return events;
		}
	}
}
