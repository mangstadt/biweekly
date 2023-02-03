package biweekly.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import biweekly.ICalVersion;
import biweekly.ICalendar;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/*
 Copyright (c) 2013-2023, Michael Angstadt
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
public class JCalDeserializerTest {
	private ObjectMapper mapper;

	@Before
	public void before() {
		mapper = new ObjectMapper();
	}

	@Test
	public void deserialize_single() throws Exception {
		//@formatter:off
		String input =
		"[\"vcalendar\"," +
			"[" +
				"[\"version\",{},\"text\",\"2.0\"]," +
				"[\"prodid\",{},\"text\",\"value1\"]" +
			"]," +
			"[]" +
		"]";
		//@formatter:on

		mapper.registerModule(new JCalModule());

		ICalendar expected = new ICalendar();
		expected.getProperties().clear();
		expected.setVersion(ICalVersion.V2_0);
		expected.setProductId("value1");

		ICalendar actual = mapper.readValue(input, ICalendar.class);
		assertEquals(expected, actual);
	}

	@Test
	public void deserialize_multiple() throws Exception {
		//@formatter:off
		String input =
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

		mapper.registerModule(new JCalModule());

		List<ICalendar> expected = new ArrayList<ICalendar>();

		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		ical.setVersion(ICalVersion.V2_0);
		ical.setProductId("value1");
		expected.add(ical);

		ical = new ICalendar();
		ical.getProperties().clear();
		ical.setVersion(ICalVersion.V2_0);
		ical.setProductId("value2");
		expected.add(ical);

		List<ICalendar> actual = mapper.readValue(input, new TypeReference<List<ICalendar>>() {
		});

		assertEquals(expected, actual);
	}

	@Test
	public void container() throws Exception {
		//@formatter:off
		String json =
		"{" +
		  "\"events\":" +
		  "[" +
		    "\"vcalendar\"," +
			  "[" +
			    "[\"version\", {}, \"text\", \"2.0\"]," +
			    "[\"prodid\", {}, \"text\", \"value1\"]" +
			  "]," +
			  "[]" +
		  "]" +
		"}";
		//@formatter:on

		Container container = mapper.readValue(json, Container.class);

		ICalendar expected = new ICalendar();
		expected.getProperties().clear();
		expected.setVersion(ICalVersion.V2_0);
		expected.setProductId("value1");

		ICalendar actual = container.events;
		assertEquals(expected, actual);
	}

	@Test
	public void container_null() throws Exception {
		//@formatter:off
		String json =
		"{" +
		  "\"events\":null" +
		"}";
		//@formatter:on

		Container container = mapper.readValue(json, Container.class);
		ICalendar actual = container.events;
		assertNull(actual);
	}

	private static class Container {
		private ICalendar events;

		@JsonDeserialize(using = JCalDeserializer.class)
		public void setEvents(ICalendar events) {
			this.events = events;
		}
	}
}
