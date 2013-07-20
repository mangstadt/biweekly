package biweekly.io.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import biweekly.io.text.ICalRawReader.ICalDataStreamListener;
import biweekly.parameter.ICalParameters;

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
public class ICalRawReaderTest {
	final String NEWLINE = System.getProperty("line.separator");

	@Test
	public void basic() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
		"PRODID:-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN\r\n" +
		"VERSION:2.0\r\n" +
		"BEGIN:VEVENT\r\n" +
		"SUMMARY:Networld+Interop Conference\r\n" +
		"DESCRIPTION:Networld+Interop Conference\r\n" +
		" and Exhibit\\nAtlanta World Congress Center\\n\r\n" +
		" Atlanta\\, Georgia\r\n" +
		"END:VEVENT\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		final Map<Integer, String> begin = new HashMap<Integer, String>();
		begin.put(1, "VCALENDAR");
		begin.put(4, "VEVENT");

		final Map<Integer, String> end = new HashMap<Integer, String>();
		end.put(7, "VEVENT");
		end.put(8, "VCALENDAR");

		final Map<Integer, String[]> props = new HashMap<Integer, String[]>();
		props.put(2, new String[] { "PRODID", "-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN" });
		props.put(3, new String[] { "VERSION", "2.0" });
		props.put(5, new String[] { "SUMMARY", "Networld+Interop Conference" });
		props.put(6, new String[] { "DESCRIPTION", "Networld+Interop Conferenceand Exhibit\\nAtlanta World Congress Center\\nAtlanta\\, Georgia" });

		ICalRawReader reader = new ICalRawReader(new StringReader(ical));
		TestListener listener = new TestListener() {
			@Override
			public void beginComponent_(String actual) {
				String expected = begin.get(line);
				assertNotNull("BEGIN property expected on line " + line, expected);
				assertEquals(expected, actual);
			}

			@Override
			public void readProperty_(String name, ICalParameters parameters, String value) {
				String expected[] = props.get(line);
				assertNotNull("Property expected on line " + line, expected);
				assertEquals(expected[0], name);
				assertEquals(expected[1], value);
				assertTrue(parameters.isEmpty());
			}

			@Override
			public void endComponent_(String actual) {
				String expected = end.get(line);
				assertNotNull("END property expected on line " + line, expected);
				assertEquals(expected, actual);
			}
		};
		reader.start(listener);

		assertEquals(2, listener.calledBeginComponent);
		assertEquals(2, listener.calledEndComponent);
		assertEquals(4, listener.calledReadProperty);
		assertTrue(reader.eof());
	}

	@Test
	public void bad_line() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
		"bad-line\r\n" +
		"VERSION:2.0\r\n" +
		"END:VCALENDAR\r\n";
		//@formatter:on

		TestListener listener = new TestListener() {
			@Override
			public void beginComponent_(String actual) {
				assertEquals("VCALENDAR", actual);
			}

			@Override
			public void readProperty_(String name, ICalParameters parameters, String value) {
				assertEquals("VERSION", name);
				assertEquals("2.0", value);
				assertTrue(parameters.isEmpty());
			}

			@Override
			public void endComponent_(String actual) {
				assertEquals("VCALENDAR", actual);
			}

			@Override
			public void invalidLine_(String line) {
				assertEquals("bad-line", line);
			}
		};
		ICalRawReader reader = new ICalRawReader(new StringReader(ical));
		reader.start(listener);

		assertEquals(1, listener.calledBeginComponent);
		assertEquals(1, listener.calledEndComponent);
		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledInvalidLine);
		assertTrue(reader.eof());
	}

	@Test
	public void stop_and_continue() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:COMP\r\n" +
		"PROP1:one\r\n" +
		"PROP2:two\r\n" +
		"PROP3:three\r\n" +
		"END:COMP\r\n";
		//@formatter:on
		ICalRawReader reader = new ICalRawReader(new StringReader(ical));

		TestListener listener = new TestListener() {
			@Override
			public void beginComponent_(String actual) {
				//empty
			}

			@Override
			public void readProperty_(String name, ICalParameters parameters, String value) {
				if (name.equals("PROP2")) {
					throw new ICalRawReader.StopReadingException();
				}
			}

			@Override
			public void endComponent_(String actual) {
				//empty
			}
		};
		reader.start(listener);

		assertEquals(1, listener.calledBeginComponent);
		assertEquals(2, listener.calledReadProperty);
		assertFalse(reader.eof());

		//////////////////
		//continue reading
		//////////////////

		listener = new TestListener() {
			@Override
			public void readProperty_(String name, ICalParameters parameters, String value) {
				assertEquals("PROP3", name);
			}

			@Override
			public void endComponent_(String actual) {
				assertEquals("COMP", actual);
			}
		};
		reader.start(listener);

		assertEquals(1, listener.calledEndComponent);
		assertEquals(1, listener.calledReadProperty);
		assertTrue(reader.eof());
	}

	@Test
	public void empty() throws Throwable {
		//@formatter:off
		String ical =
		"";
		//@formatter:on
		ICalRawReader reader = new ICalRawReader(new StringReader(ical));

		TestListener listener = new TestListener() {
			//empty
		};
		reader.start(listener);

		assertTrue(reader.eof());
	}

	@Test
	public void component_case_insensitive() throws Throwable {
		//@formatter:off
		String ical =
		"Begin:COMP\r\n" +
		"enD:COMP\r\n";
		//@formatter:on
		ICalRawReader reader = new ICalRawReader(new StringReader(ical));

		TestListener listener = new TestListener() {
			@Override
			public void beginComponent_(String actual) {
				assertEquals("COMP", actual);
			}

			@Override
			public void endComponent_(String actual) {
				assertEquals("COMP", actual);
			}
		};
		reader.start(listener);

		assertEquals(1, listener.calledBeginComponent);
		assertEquals(1, listener.calledEndComponent);
		assertTrue(reader.eof());
	}

	@Test
	public void preserve_case() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:Comp\r\n" +
		"Prop1;Param2=Two:One\r\n" +
		"END:comP\r\n";
		//@formatter:on
		ICalRawReader reader = new ICalRawReader(new StringReader(ical));

		TestListener listener = new TestListener() {
			@Override
			public void beginComponent_(String actual) {
				assertEquals("Comp", actual);
			}

			@Override
			public void readProperty_(String name, ICalParameters parameters, String value) {
				assertEquals("Prop1", name);

				assertEquals(1, parameters.size());
				Map.Entry<String, List<String>> entry = parameters.iterator().next();
				assertEquals("PARAM2", entry.getKey()); //TODO parameter name case *not* preserved
				assertEquals(Arrays.asList("Two"), entry.getValue());

				assertEquals("One", value);
			}

			@Override
			public void endComponent_(String actual) {
				assertEquals("comP", actual);
			}
		};
		reader.start(listener);

		assertEquals(1, listener.calledBeginComponent);
		assertEquals(1, listener.calledEndComponent);
		assertEquals(1, listener.calledReadProperty);
		assertTrue(reader.eof());
	}

	@Test
	public void parameter() throws Throwable {
		//@formatter:off
		String ical =
		"PROP" +
		";PARAM1=one" +
		";PARAM2=\"two,;:^'^n^^^three\"" +
		";PARAM3=four,\"five,;:^'^n^^^six\"" +
		";PARAM4=seven^'^n^^^eight" +
		":value";
		//@formatter:on
		ICalRawReader reader = new ICalRawReader(new StringReader(ical));

		TestListener listener = new TestListener() {
			@Override
			public void readProperty_(String name, ICalParameters parameters, String value) {
				assertEquals("PROP", name);

				assertEquals(5, parameters.size());
				assertEquals(Arrays.asList("one"), parameters.get("PARAM1"));
				assertEquals(Arrays.asList("two,;:\"" + NEWLINE + "^^three"), parameters.get("PARAM2"));
				assertEquals(Arrays.asList("four", "five,;:\"" + NEWLINE + "^^six"), parameters.get("PARAM3"));
				assertEquals(Arrays.asList("seven\"" + NEWLINE + "^^eight"), parameters.get("PARAM4"));

				assertEquals("value", value);
			}
		};
		reader.start(listener);

		assertEquals(1, listener.calledReadProperty);
		assertTrue(reader.eof());
	}

	@Test
	public void parameter_valueless() throws Throwable {
		//@formatter:off
		String ical =
		"PROP;PARAM1;PARAM2=two:value";
		//@formatter:on
		ICalRawReader reader = new ICalRawReader(new StringReader(ical));

		TestListener listener = new TestListener() {
			@Override
			public void readProperty_(String name, ICalParameters parameters, String value) {
				assertEquals("PROP", name);

				assertEquals(2, parameters.size());
				assertNull(parameters.first("PARAM1"));
				assertEquals(Arrays.asList((String) null), parameters.get("PARAM1"));
				assertEquals(Arrays.asList("two"), parameters.get("PARAM2"));

				assertEquals("value", value);
			}

			@Override
			public void valuelessParameter_(String propertyName, String parameterName) {
				assertEquals("PROP", propertyName);
				assertEquals("PARAM1", parameterName);
			}
		};
		reader.start(listener);

		assertEquals(1, listener.calledReadProperty);
		assertEquals(1, listener.calledValuelessParameter);
		assertTrue(reader.eof());
	}

	@Test
	public void caret_encoding_disabled() throws Throwable {
		//@formatter:off
		String ical =
		"PROP;PARAM1=one^'^n^^^two:value\r\n";
		//@formatter:on
		ICalRawReader reader = new ICalRawReader(new StringReader(ical));

		TestListener listener = new TestListener() {
			@Override
			public void readProperty_(String name, ICalParameters parameters, String value) {
				assertEquals("PROP", name);

				assertEquals(1, parameters.size());
				assertEquals(Arrays.asList("one^'^n^^^two"), parameters.get("PARAM1"));

				assertEquals("value", value);
			}
		};
		reader.setCaretDecodingEnabled(false);
		reader.start(listener);

		assertEquals(1, listener.calledReadProperty);
		assertTrue(reader.eof());
	}

	abstract class TestListener implements ICalDataStreamListener {
		int line = 0;
		int calledBeginComponent = 0, calledReadProperty = 0, calledEndComponent = 0, calledInvalidLine = 0, calledValuelessParameter = 0;

		public final void beginComponent(String actual) {
			line++;
			calledBeginComponent++;
			beginComponent_(actual);
		}

		public final void readProperty(String name, ICalParameters parameters, String value) {
			line++;
			calledReadProperty++;
			readProperty_(name, parameters, value);
		}

		public final void endComponent(String actual) {
			line++;
			calledEndComponent++;
			endComponent_(actual);
		}

		public final void invalidLine(String line) {
			this.line++;
			calledInvalidLine++;
			invalidLine_(line);
		}

		public final void valuelessParameter(String propertyName, String parameterName) {
			calledValuelessParameter++;
			valuelessParameter_(propertyName, parameterName);
		}

		protected void beginComponent_(String actual) {
			fail("\"beginComponent\" should not have been called.");
		}

		protected void readProperty_(String name, ICalParameters parameters, String value) {
			fail("\"readProperty\" should not have been called.");
		}

		protected void endComponent_(String actual) {
			fail("\"endComponent\" should not have been called.");
		}

		protected void invalidLine_(String line) {
			fail("\"invalidLine\" should not have been called.");
		}

		protected void valuelessParameter_(String propertyName, String parameterName) {
			fail("\"valuelessParameter\" should not have been called.");
		}
	}
}
