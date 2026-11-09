package biweekly.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonToken;
import com.github.mangstadt.vinnie.LaciToken;

import biweekly.ICalDataType;
import biweekly.io.json.JCalRawReader.JCalDataStreamListener;
import biweekly.parameter.ICalParameters;
import biweekly.util.ListMultimap;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
@SuppressWarnings("resource")
public class JCalRawReaderTest {
	@Test
	public void basic() throws Throwable {
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

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prodid"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN", value.asSingle());
					break;
				case 2:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("version"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("2.0", value.asSingle());
					break;
				case 3:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("vevent")), componentHierarchy);
					assertEquals(new LaciToken("summary"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("Networld+Interop Conference", value.asSingle());
					break;
				case 4:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("vevent")), componentHierarchy);
					assertEquals(new LaciToken("description"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("Networld+Interop Conference\nand Exhibit\nAtlanta World Congress Center\nAtlanta, Georgia", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				case 2:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), parentHierarchy);
					assertEquals(new LaciToken("vevent"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(4, listener.calledReadProperty);
		assertEquals(2, listener.calledReadComponent);
	}

	@Test
	public void read_multiple() throws Throwable {
		//@formatter:off
		String json =
		"[" +
			"[\"vcalendar\"," +
				"[" +
					"[\"prodid\", {}, \"text\", \"prodid1\"]" +
				"]," +
				"[" +
				"]" +
			"]," +
			"[\"vcalendar\"," +
				"[" +
					"[\"prodid\", {}, \"text\", \"prodid2\"]" +
				"]," +
				"[" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prodid"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("prodid1", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};

		reader.readNext(listener);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);

		//it should continue to read the rest of the iCals

		listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prodid"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("prodid2", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void ignore_other_json() throws Throwable {
		//@formatter:off
		String json =
		"{" +
			"\"website\": \"example.com\"," +
			"\"ical\":" +
				"[\"vcalendar\"," +
					"[" +
						"[\"prodid\", {}, \"text\", \"prodid\"]" +
					"]," +
					"[" +
					"]" +
				"]" +
		"}";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prodid"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("prodid", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};

		reader.readNext(listener);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void bad_snytax() throws Throwable {
		//@formatter:off
		String json =
		"[" +
			"[\"vcalendar\"," +
				"[" +
					"[\"prodid\", {}, \"text\", \"prodid\"]," +
					"[\"version\", {}, []]" +
				"]," +
				"[" +
				"]" +
			"]," +
			"[\"vcalendar\"," +
				"[" +
					"[\"prodid\", {}, \"text\", \"prodid\"]," +
					"[\"version\", {}, \"text\", \"2.0\"]" +
				"]," +
				"[" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prodid"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("prodid", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};

		try {
			reader.readNext(listener);
		} catch (JCalParseException e) {
			assertEquals(JsonToken.VALUE_STRING, e.getExpectedToken());
			assertEquals(JsonToken.START_ARRAY, e.getActualToken());
		}

		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);

		//it should continue to read the rest of the iCalendars

		listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prodid"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("prodid", value.asSingle());
					break;
				case 2:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("version"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("2.0", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(2, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void empty_properties_array() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
			"]," +
			"[" +
				"[\"vevent\"," +
					"[" +
						"[\"summary\", {}, \"text\", \"summary-value\"]" +
					"]," +
					"[" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("vevent")), componentHierarchy);
					assertEquals(new LaciToken("summary"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("summary-value", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				case 2:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), parentHierarchy);
					assertEquals(new LaciToken("vevent"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(2, listener.calledReadComponent);
	}

	@Test
	public void empty_components_array() throws Throwable {
		//@formatter:off
		String json =
		"[" +
			"[\"vcalendar\"," +
				"[" +
					"[\"prodid\", {}, \"text\", \"prodid1\"]" +
				"]," +
				"[" +
				"]" +
			"]," +
			
			//make sure it can still read the next iCalendar object
			"[\"vcalendar\"," +
				"[" +
					"[\"prodid\", {}, \"text\", \"prodid2\"]" +
				"]," +
				"[" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prodid"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("prodid1", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);

		listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prodid"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("prodid2", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void empty_properties_and_components_arrays() throws Throwable {
		//@formatter:off
		String json =
		"[" +
			"[\"vcalendar\"," +
				"[" +
				"]," +
				"[" +
				"]" +
			"]," +
			
			//make sure it can still read the next iCalendar object
			"[\"vcalendar\"," +
				"[" +
				"]," +
				"[" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				//empty
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(0, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);

		listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				//empty
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(0, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void read_components_deep() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"prop1\", {}, \"text\", \"value1\"]" +
			"]," +
			"[" +
				"[\"comp1\"," +
					"[" +
						"[\"prop2\", {}, \"text\", \"value2\"]" +
					"]," +
					"[" +
						"[\"comp2\"," +
							"[" +
								"[\"prop3\", {}, \"text\", \"value3\"]" +
							"]," +
							"[" +
								"[\"comp3\"," +
									"[" +
										"[\"prop4\", {}, \"text\", \"value4\"]" +
									"]," +
									"[" +
									"]" +
								"]" +
							"]" +
						"]," +
						"[\"comp4\"," +
							"[" +
								"[\"prop5\", {}, \"text\", \"value5\"]" +
							"]," +
							"[" +
							"]" +
						"]" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prop1"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("value1", value.asSingle());
					break;
				case 2:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("comp1")), componentHierarchy);
					assertEquals(new LaciToken("prop2"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("value2", value.asSingle());
					break;
				case 3:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("comp1"), new LaciToken("comp2")), componentHierarchy);
					assertEquals(new LaciToken("prop3"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("value3", value.asSingle());
					break;
				case 4:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("comp1"), new LaciToken("comp2"), new LaciToken("comp3")), componentHierarchy);
					assertEquals(new LaciToken("prop4"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("value4", value.asSingle());
					break;
				case 5:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("comp1"), new LaciToken("comp4")), componentHierarchy);
					assertEquals(new LaciToken("prop5"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("value5", value.asSingle());
					break;
				}

			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				case 2:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), parentHierarchy);
					assertEquals(new LaciToken("comp1"), name);
					break;
				case 3:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("comp1")), parentHierarchy);
					assertEquals(new LaciToken("comp2"), name);
					break;
				case 4:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("comp1"), new LaciToken("comp2")), parentHierarchy);
					assertEquals(new LaciToken("comp3"), name);
					break;
				case 5:
					assertEquals(Arrays.asList(new LaciToken("vcalendar"), new LaciToken("comp1")), parentHierarchy);
					assertEquals(new LaciToken("comp4"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(5, listener.calledReadProperty);
		assertEquals(5, listener.calledReadComponent);
	}

	@Test
	public void structured_value() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"request-status\", {}, \"text\", [\"3.7\", \"Invalid Calendar User\", \"ATTENDEE:mailto:jsmith@example.org\"] ]," +
				"[\"request-status\", {}, \"text\", [] ]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("request-status"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);

					List<List<String>> expected = new ArrayList<List<String>>();
					expected.add(Arrays.asList("3.7"));
					expected.add(Arrays.asList("Invalid Calendar User"));
					expected.add(Arrays.asList("ATTENDEE:mailto:jsmith@example.org"));
					assertEquals(expected, value.asStructured());
					break;
				case 2:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("request-status"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals(Arrays.asList(), value.asStructured());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};

		reader.readNext(listener);

		assertEquals(2, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void multi_value() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"categories\", {}, \"text\", \"one\", \"two\", \"three\" ]," +
				"[\"categories\", {}, \"text\", \"one\" ]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("categories"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals(Arrays.asList("one", "two", "three"), value.asMulti());
					break;
				case 2:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("categories"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals(Arrays.asList("one"), value.asMulti());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};

		reader.readNext(listener);

		assertEquals(2, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void object_value() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"rrule\", {}, \"text\", { \"freq\":\"WEEKLY\", \"count\":5, \"byday\":[\"-1SU\", \"2MO\"] } ]," +
				"[\"rrule\", {}, \"text\", {} ]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("rrule"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);

					ListMultimap<LaciToken, String> expected = new ListMultimap<LaciToken, String>();
					expected.put(new LaciToken("freq"), "WEEKLY");
					expected.put(new LaciToken("count"), "5");
					expected.put(new LaciToken("byday"), "-1SU");
					expected.put(new LaciToken("byday"), "2MO");
					assertEquals(expected, value.asObject());
					break;
				case 2:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("rrule"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);

					expected = new ListMultimap<LaciToken, String>();
					assertEquals(expected, value.asObject());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};

		reader.readNext(listener);

		assertEquals(2, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void different_data_types() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"prop\", {}, \"text\", false, true, 1.1, 1, null, \"text\" ]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prop"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);

					List<JsonValue> expected = new ArrayList<JsonValue>();
					expected.add(new JsonValue(false));
					expected.add(new JsonValue(true));
					expected.add(new JsonValue(1.1));
					expected.add(new JsonValue(1L));
					expected.add(new JsonValue((Object) null));
					expected.add(new JsonValue("text"));
					assertEquals(expected, value.getValues());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};

		reader.readNext(listener);

		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void complex_value() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"prop\", {}, \"text\", {" +
					"\"a\":[\"one\",\"two\"]," +
					"\"b\":{" +
						"\"c\":[\"three\"]," +
						"\"d\":{}" +
					"}" +
				"}, \"four\" ]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prop"), name);
					assertTrue(parameters.isEmpty());
					assertEquals(ICalDataType.TEXT, dataType);

					List<JsonValue> expected = new ArrayList<JsonValue>();
					Map<String, JsonValue> m = new HashMap<String, JsonValue>();
					m.put("a", new JsonValue(Arrays.asList(new JsonValue("one"), new JsonValue("two"))));
					Map<String, JsonValue> m2 = new HashMap<String, JsonValue>();
					m2.put("c", new JsonValue(Arrays.asList(new JsonValue("three"))));
					m2.put("d", new JsonValue(new HashMap<String, JsonValue>()));
					m.put("b", new JsonValue(m2));
					expected.add(new JsonValue(m));
					expected.add(new JsonValue("four"));

					assertEquals(expected, value.getValues());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};

		reader.readNext(listener);

		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void data_type_unknown() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"prop\", {}, \"unknown\", \"value\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prop"), name);
					assertTrue(parameters.isEmpty());
					assertNull(dataType);
					assertEquals("value", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void data_type_unrecognized() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"prop\", {}, \"foo\", \"value\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prop"), name);
					assertTrue(parameters.isEmpty());
					assertSame(ICalDataType.get("foo"), dataType);
					assertEquals("value", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	@Test
	public void empty() throws Throwable {
		//@formatter:off
		String json =
		"";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				//empty
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				//empty
			}
		};
		reader.readNext(listener);
		assertEquals(0, listener.calledReadProperty);
		assertEquals(0, listener.calledReadComponent);
	}

	@Test
	public void no_ical() throws Throwable {
		//@formatter:off
		String json =
		"{" +
			"\"foo\": \"bar\"" +
		"}";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				//empty
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				//empty
			}
		};
		reader.readNext(listener);
		assertEquals(0, listener.calledReadProperty);
		assertEquals(0, listener.calledReadComponent);
	}

	@Test
	public void parameters() throws Throwable {
		//@formatter:off
		String json =
		"[\"vcalendar\"," +
			"[" +
				"[\"prop\", { \"a\": \"one\", \"b\": [\"two\"], \"c\": [\"three\", \"four\"] }, \"text\", \"value\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on

		JCalRawReader reader = new JCalRawReader(new StringReader(json));

		TestListener listener = new TestListener() {
			@Override
			protected void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
				switch (calledReadProperty) {
				case 1:
					assertEquals(Arrays.asList(new LaciToken("vcalendar")), componentHierarchy);
					assertEquals(new LaciToken("prop"), name);
					assertEquals(4, parameters.size());
					assertEquals(Arrays.asList("one"), parameters.get("a"));
					assertEquals(Arrays.asList("two"), parameters.get("b"));
					assertEquals(Arrays.asList("three", "four"), parameters.get("c"));
					assertEquals(ICalDataType.TEXT, dataType);
					assertEquals("value", value.asSingle());
					break;
				}
			}

			@Override
			protected void readComponent_(List<LaciToken> parentHierarchy, LaciToken name) {
				switch (calledReadComponent) {
				case 1:
					assertEquals(Arrays.asList(), parentHierarchy);
					assertEquals(new LaciToken("vcalendar"), name);
					break;
				}
			}
		};
		reader.readNext(listener);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledReadComponent);
	}

	private abstract class TestListener implements JCalDataStreamListener {
		protected int calledReadProperty = 0, calledReadComponent = 0;

		public final void readProperty(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
			calledReadProperty++;
			readProperty_(componentHierarchy, name, parameters, dataType, value);
		}

		public final void readComponent(List<LaciToken> parentHierarchy, LaciToken name) {
			calledReadComponent++;
			readComponent_(parentHierarchy, name);
		}

		protected abstract void readProperty_(List<LaciToken> componentHierarchy, LaciToken name, ICalParameters parameters, ICalDataType dataType, JCalValue value);

		protected abstract void readComponent_(List<LaciToken> parentHierarchy, LaciToken name);
	}
}
