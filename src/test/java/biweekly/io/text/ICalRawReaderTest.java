package biweekly.io.text;

import static biweekly.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.StringReader;

import org.junit.Test;

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
		ICalRawReader reader = create(ical);

		assertEquals(line("BEGIN").value("VCALENDAR").build(), reader.readLine());
		assertEquals(line("PRODID").value("-//xyz Corp//NONSGML PDA Calendar Version 1.0//EN").build(), reader.readLine());
		assertEquals(line("VERSION").value("2.0").build(), reader.readLine());
		assertEquals(line("BEGIN").value("VEVENT").build(), reader.readLine());
		assertEquals(line("SUMMARY").value("Networld+Interop Conference").build(), reader.readLine());
		assertEquals(line("DESCRIPTION").value("Networld+Interop Conferenceand Exhibit\\nAtlanta World Congress Center\\nAtlanta\\, Georgia").build(), reader.readLine());
		assertEquals(line("END").value("VEVENT").build(), reader.readLine());
		assertEquals(line("END").value("VCALENDAR").build(), reader.readLine());
		assertNull(reader.readLine());
	}

	@Test(expected = ICalParseException.class)
	public void bad_line() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:VCALENDAR\r\n" +
		"bad-line\r\n";
		//@formatter:on
		ICalRawReader reader = create(ical);

		assertEquals(line("BEGIN").value("VCALENDAR").build(), reader.readLine());
		reader.readLine();
	}

	@Test
	public void empty() throws Throwable {
		String ical = "";
		ICalRawReader reader = create(ical);

		assertNull(reader.readLine());
	}

	@Test
	public void component_case_insensitive() throws Throwable {
		//@formatter:off
		String ical =
		"Begin:COMP\r\n" +
		"enD:COMP\r\n";
		//@formatter:on
		ICalRawReader reader = create(ical);

		assertEquals(line("Begin").value("COMP").build(), reader.readLine());
		assertEquals(line("enD").value("COMP").build(), reader.readLine());
		assertNull(reader.readLine());
	}

	@Test
	public void preserve_case() throws Throwable {
		//@formatter:off
		String ical =
		"BEGIN:Comp\r\n" +
		"Prop1;Param2=Two:One\r\n" +
		"END:comP\r\n";
		//@formatter:on
		ICalRawReader reader = create(ical);

		assertEquals(line("BEGIN").value("Comp").build(), reader.readLine());
		assertEquals(line("Prop1").param("Param2", "Two").value("One").build(), reader.readLine());
		assertEquals(line("END").value("comP").build(), reader.readLine());
		assertNull(reader.readLine());
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
		ICalRawReader reader = create(ical);

		ICalRawLine.Builder builder = line("PROP");
		builder.param("PARAM1", "one");
		builder.param("PARAM2", "two,;:\"" + NEWLINE + "^^three");
		builder.param("PARAM3", "four", "five,;:\"" + NEWLINE + "^^six");
		builder.param("PARAM4", "seven\"" + NEWLINE + "^^eight");
		builder.value("value");
		ICalRawLine expected = builder.build();

		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void parameter_valueless() throws Throwable {
		//@formatter:off
		String ical =
		"PROP;PARAM1;PARAM2=two:value";
		//@formatter:on
		ICalRawReader reader = create(ical);

		ICalRawLine expected = line("PROP").param("PARAM1", (String) null).param("PARAM2", "two").value("value").build();
		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void caret_encoding_disabled() throws Throwable {
		//@formatter:off
		String ical =
		"PROP;PARAM1=one^'^n^^^two:value\r\n";
		//@formatter:on
		ICalRawReader reader = create(ical);
		reader.setCaretDecodingEnabled(false);

		ICalRawLine expected = line("PROP").param("PARAM1", "one^'^n^^^two").value("value").build();
		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	private static ICalRawReader create(String vcard) {
		return new ICalRawReader(new StringReader(vcard));
	}

	private static ICalRawLine.Builder line(String name) {
		return new ICalRawLine.Builder().name(name);
	}
}
