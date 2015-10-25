package biweekly.io.text;

import static biweekly.ICalVersion.V2_0;
import static biweekly.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.StringReader;

import org.junit.Test;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
		assertEquals(line("BEGIN").value("VEVENT").build(), reader.readLine());
		assertEquals(line("SUMMARY").value("Networld+Interop Conference").build(), reader.readLine());
		assertEquals(line("DESCRIPTION").value("Networld+Interop Conferenceand Exhibit\\nAtlanta World Congress Center\\nAtlanta\\, Georgia").build(), reader.readLine());
		assertEquals(line("END").value("VEVENT").build(), reader.readLine());
		assertEquals(line("END").value("VCALENDAR").build(), reader.readLine());
		assertNull(reader.readLine());
	}

	@Test
	public void version() throws Throwable {
		//@formatter:off
		String ical =
		"VERSION:2.0\r\n" +
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:invalid\r\n" +
			"VERSION:2.0\r\n" +
			"PRODID:value\r\n" +
			"BEGIN:VEVENT\r\n" +
				"VERSION:1.0\r\n" +
				"SUMMARY:value\r\n" +
			//missing END:VEVENT
		"END:VCALENDAR\r\n" +
		"VERSION:1.0\r\n";
		//@formatter:on
		ICalRawReader reader = create(ical);
		assertEquals(null, reader.getVersion());

		assertEquals(line("VERSION").value("2.0").build(), reader.readLine());
		assertEquals(null, reader.getVersion());

		assertEquals(line("BEGIN").value("VCALENDAR").build(), reader.readLine());
		assertEquals(null, reader.getVersion());

		assertEquals(line("VERSION").value("invalid").build(), reader.readLine());
		assertEquals(null, reader.getVersion());

		assertEquals(line("PRODID").value("value").build(), reader.readLine());
		assertEquals(V2_0, reader.getVersion());

		assertEquals(line("BEGIN").value("VEVENT").build(), reader.readLine());
		assertEquals(V2_0, reader.getVersion());

		assertEquals(line("VERSION").value("1.0").build(), reader.readLine());
		assertEquals(V2_0, reader.getVersion());

		assertEquals(line("SUMMARY").value("value").build(), reader.readLine());
		assertEquals(V2_0, reader.getVersion());

		assertEquals(line("END").value("VCALENDAR").build(), reader.readLine());
		assertEquals(V2_0, reader.getVersion());

		assertEquals(line("VERSION").value("1.0").build(), reader.readLine());
		assertEquals(V2_0, reader.getVersion());

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
	public void empty_value() throws Throwable {
		String ical = "COMMENT:";
		ICalRawReader reader = create(ical);

		ICalRawLine expected = line("COMMENT").value("").build();
		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void empty_just_newlines() throws Throwable {
		String ical = "\r\n\r\n\r\n";
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
	public void parameter_valueless() throws Throwable {
		//@formatter:off
		String ical =
		"PROP;PARAM1;PARAM2=two:value";
		//@formatter:on
		ICalRawReader reader = create(ical);

		ICalRawLine expected = line("PROP").param(null, "PARAM1").param("PARAM2", "two").value("value").build();
		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void parameters_with_whitespace_around_equals() throws Throwable {
		//1.0 (removes)
		{
			//@formatter:off
			String ical = 
			"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"ADR;TYPE\t= WOrK;TYPE \t=  dOM:;;123 Main Str;Austin;TX;12345;US\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.readLine();

			ICalRawLine expected = line("ADR").param("TYPE", "WOrK").param("TYPE", "dOM").value(";;123 Main Str;Austin;TX;12345;US").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}

		//2.0 (keeps)
		{
			//@formatter:off
			String ical = 
			"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"ADR;TYPE\t= WOrK;TYPE \t=  dOM:;;123 Main Str;Austin;TX;12345;US\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.readLine();

			ICalRawLine expected = line("ADR").param("TYPE\t", " WOrK").param("TYPE \t", "  dOM").value(";;123 Main Str;Austin;TX;12345;US").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}
	}

	@Test
	public void multi_valued_parameters() throws Throwable {
		//1.0 (doesn't recognize them)
		{
			//@formatter:off
			String ical = 
			"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			"ADR;TYPE=dom,\"foo,bar\\;baz\",work,foo=bar;PREF=1:;;123 Main Str;Austin;TX;12345;US\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.readLine();

			ICalRawLine expected = line("ADR").param("TYPE", "dom,\"foo,bar;baz\",work,foo=bar").param("PREF", "1").value(";;123 Main Str;Austin;TX;12345;US").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}

		//2.0
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"ADR;TYPE=dom,\"foo,bar;baz\",work,foo=bar;PREF=1:;;123 Main Str;Austin;TX;12345;US\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.readLine();

			ICalRawLine expected = line("ADR").param("TYPE", "dom", "foo,bar;baz", "work", "foo=bar").param("PREF", "1").value(";;123 Main Str;Austin;TX;12345;US").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}
	}

	@Test
	public void character_escaping_in_parameters() throws Throwable {
		//1.0 without caret escaping
		{
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: backslash-escaped semi-colon (must be escaped in 2.1)
			//5: caret-escaped newline (uppercase N)
			//6: backslash-escaped newline (lowercase n)
			//7: backslash-escaped newline (uppercase N)
			//8: caret-escaped double quote
			//9: un-escaped double quote (no special meaning in 2.1)
			//a: caret that doesn't escape anything
			//@formatter:off
			String ical = 
			"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			//          1    2     2     3        4     5            6        7  8       8   9   9     a
			"ADR;LABEL=1\\23 ^^Main^^ St.^nSection\\; 12^NBuilding 20\\nApt 10\\N^'Austin^', \"TX\" 123^45:;;123 Main Str;Austin;TX;12345;US\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.setCaretDecodingEnabled(false);
			reader.readLine();

			ICalRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}

		//1.0 with caret escaping (no difference)
		{
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: backslash-escaped semi-colon (must be escaped in 2.1)
			//5: caret-escaped newline (uppercase N)
			//6: backslash-escaped newline (lowercase n)
			//7: backslash-escaped newline (uppercase N)
			//8: caret-escaped double quote
			//9: un-escaped double quote (no special meaning in 2.1)
			//a: caret that doesn't escape anything
			//@formatter:off
			String ical = 
			"BEGIN:VCALENDAR\r\n" +
			"VERSION:1.0\r\n" +
			//          1    2     2     3        4     5            6        7  8       8   9   9     a
			"ADR;LABEL=1\\23 ^^Main^^ St.^nSection\\; 12^NBuilding 20\\nApt 10\\N^'Austin^', \"TX\" 123^45:;;123 Main Str;Austin;TX;12345;US\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.setCaretDecodingEnabled(true);
			reader.readLine();

			ICalRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}

		//2.0 without caret escaping
		{
			//0: value double quoted because of semi-colon and comma chars
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: caret-escaped newline (uppercase N)
			//5: backslash-escaped newline (lowercase n)
			//6: backslash-escaped newline (uppercase N)
			//7: caret-escaped double quote
			//8: backslash-escaped double quote (not part of the standard, included for interoperability)
			//9: caret that doesn't escape anything
			//@formatter:off
			String ical = 
			"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\":;;123 Main Str;Austin;TX;12345;US\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.setCaretDecodingEnabled(false);
			reader.readLine();

			ICalRawLine expected = line("ADR").param("LABEL", "1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "^'Austin^', \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}

		//2.0 with caret escaping
		{
			//0: value double quoted because of semi-colon and comma chars
			//1: backslash that doesn't escape anything
			//2: caret-escaped caret
			//3: caret-escaped newline (lowercase n)
			//4: caret-escaped newline (uppercase N)
			//5: backslash-escaped newline (lowercase n)
			//6: backslash-escaped newline (uppercase N)
			//7: caret-escaped double quote
			//8: backslash-escaped double quote (not part of the standard, included for interoperability)
			//9: caret that doesn't escape anything
			//@formatter:off
			String ical = 
			"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			//         0  1    2     2     3        0   4            5        6  7       7 0 8     8       9  0
			"ADR;LABEL=\"1\\23 ^^Main^^ St.^nSection; 12^NBuilding 20\\nApt 10\\N^'Austin^', \\\"TX\\\" 123^45\":;;123 Main Str;Austin;TX;12345;US\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.setCaretDecodingEnabled(true);
			reader.readLine();

			ICalRawLine expected = line("ADR").param("LABEL", "1\\23 ^Main^ St." + NEWLINE + "Section; 12^NBuilding 20" + NEWLINE + "Apt 10" + NEWLINE + "\"Austin\", \"TX\" 123^45").value(";;123 Main Str;Austin;TX;12345;US").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}
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

	@Test
	public void non_standard_newlines() throws Throwable {
		//@formatter:off
		String ical =
		"NOTE:note0\r\n" + //standard newline
		"NOTE:note1\n" +
		"NOTE:note2\r" +
		"NOTE:note3"; //no trailing newline
		//@formatter:on
		ICalRawReader reader = create(ical);

		assertEquals(line("NOTE").value("note0").build(), reader.readLine());
		assertEquals(line("NOTE").value("note1").build(), reader.readLine());
		assertEquals(line("NOTE").value("note2").build(), reader.readLine());
		assertEquals(line("NOTE").value("note3").build(), reader.readLine());
		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding() throws Throwable {
		//@formatter:off
		String ical =

		//unfolded line
		"FN:Michael Angstadt\r\n" +

		//empty lines should be ignored
		"\r\n" +

		//this line is folded
		"NOTE:folded \r\n" +
		" line\r\n" +

		//this line is folded with multiple whitespace characters
		"NOTE:one \r\n" +
		" two \r\n" +
		"  three \r\n" +
		"\t \tfour\r\n";
		//@formatter:on

		ICalRawReader reader = create(ical);

		ICalRawLine expected = line("FN").value("Michael Angstadt").build();
		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		expected = line("NOTE").value("folded line").build();
		actual = reader.readLine();
		assertEquals(expected, actual);

		expected = line("NOTE").value("one two  three  \tfour").build();
		actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding_empty_folded_line() throws Throwable {
		//@formatter:off
		String ical =
		"NOTE:line1\r\n" +
		" \r\n" +
		" line2\r\n";
		//@formatter:on
		ICalRawReader reader = create(ical);

		ICalRawLine expected = line("NOTE").value("line1line2").build();
		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding_one_character_per_line() throws Throwable {
		//@formatter:off
		String ical =
		"N\r\n" +
		" O\r\n" +
		" T\r\n" +
		" E\r\n" +
		" :\r\n" +
		" v\r\n" +
		" a\r\n" +
		" l\r\n" +
		" u\r\n" +
		" e\r\n";
		//@formatter:on
		ICalRawReader reader = create(ical);

		ICalRawLine expected = line("NOTE").value("value").build();
		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding_parameters_on_multiple_lines() throws Throwable {
		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:1.0\r\n" +
				"NOTE;ONE=1;TWO=the nu\\;m\r\n" +
				" ber two:value\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.readLine();

			ICalRawLine expected = line("NOTE").param("ONE", "1").param("TWO", "the nu;mber two").value("value").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}

		{
			//@formatter:off
			String ical =
			"BEGIN:VCALENDAR\r\n" +
				"VERSION:2.0\r\n" +
				"NOTE;ONE=1;TWO=\"the nu;m\r\n" +
				" ber two\":value\r\n" +
			"END:VCALENDAR\r\n";
			//@formatter:on
			ICalRawReader reader = create(ical);
			reader.readLine();

			ICalRawLine expected = line("NOTE").param("ONE", "1").param("TWO", "the nu;mber two").value("value").build();
			ICalRawLine actual = reader.readLine();
			assertEquals(expected, actual);

			reader.readLine();
			assertNull(reader.readLine());
		}
	}

	/**
	 * Tests the issues that Outlook has with folded lines that are
	 * quoted-printable.
	 */
	@Test
	public void line_unfolding_quoted_printable() throws Throwable {
		//@formatter:off
		String ical =
		
		//one line
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5\r\n" +

		//two lines
		//(without "ENCODING" subtype name)
		"LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"New York, New York  12345\r\n" +

		//two lines with an empty line at the end
		"LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"New York, New York  12345=0D=0A=\r\n" +
		"\r\n" +

		//two lines with an empty line in the middle
		"LABEL;HOME;QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"=\r\n" +
		"New York, New York  12345\r\n" +

		//three lines
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"New York, New York  12345=0D=0A=\r\n" +
		"USA\r\n" +

		//four lines
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		"New York, New York  12345=0D=0A=\r\n" +
		"USA=0D=0A=\r\n" +
		"4th line\r\n" +

		//a quoted-printable line whose additional lines *are* folded where the lines end in "="
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A=\r\n" +
		" New York, New York  12345=0D=0A=\r\n" +
		"  USA\r\n" +

		//a quoted-printable line whose additional lines *are* folded, where the lines don't end in "="
		"LABEL;HOME;ENCODING=QUOTED-PRINTABLE:Silicon Alley 5,=0D=0A\r\n" +
		" New York, New York  12345=0D=0A\r\n" +
		"  USA\r\n";
		//@formatter:on

		ICalRawReader reader = create(ical);

		//@formatter:off
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param(null, "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param(null, "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0A")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param(null, "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0AUSA")
			.build(),
		reader.readLine());

		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0AUSA=0D=0A4th line")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0A USA")
			.build(),
		reader.readLine());
		
		assertEquals(line("LABEL")
			.param(null, "HOME")
			.param("ENCODING", "QUOTED-PRINTABLE")
			.value("Silicon Alley 5,=0D=0ANew York, New York  12345=0D=0A USA")
			.build(),
		reader.readLine());

		//@formatter:on

		assertNull(reader.readLine());
	}

	@Test
	public void line_unfolding_quoted_printable_parameters_folded() throws Throwable {
		//@formatter:off
		String ical =
		"NOTE;ENCOD\r\n" +
		" ING=quoted-printable:foo=\r\n" +
		"bar\r\n";
		//@formatter:on
		ICalRawReader reader = create(ical);

		ICalRawLine expected = line("NOTE").param("ENCODING", "quoted-printable").value("foobar").build();
		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	/**
	 * Tests to make sure it's not just looking for the string
	 * "quoted-printable" somewhere before the property value (like it used to
	 * do).
	 */
	@Test
	public void quoted_printable_detection_using_parameter() throws Throwable {
		//@formatter:off
		String ical =
		"NOTE;PARAM=quoted-printable:foo=\r\n" +
		"bar\r\n" +
		"NOTE;quoted-printable:foo=\r\n" +
		"bar\r\n" +
		"NOTE;encoding=quoted-printable:foo=\r\n" +
		"bar\r\n";
		//@formatter:on
		ICalRawReader reader = create(ical);

		ICalRawLine expected = line("NOTE").param("PARAM", "quoted-printable").value("foo=").build();
		ICalRawLine actual = reader.readLine();
		assertEquals(expected, actual);

		try {
			reader.readLine();
			fail();
		} catch (ICalParseException e) {
			//expected
		}

		expected = line("NOTE").param(null, "quoted-printable").value("foobar").build();
		actual = reader.readLine();
		assertEquals(expected, actual);

		expected = line("NOTE").param("ENCODING", "quoted-printable").value("foobar").build();
		actual = reader.readLine();
		assertEquals(expected, actual);

		assertNull(reader.readLine());
	}

	@Test
	public void getLineNumber() throws Exception {
		//@formatter:off
		String ical =
		"NOTE:one\n" +
		" two\n" +
		"\n" +
		"NOTE:three\n" +
		" four\n";
		//@formatter:on

		ICalRawReader reader = create(ical);

		assertEquals(1, reader.getLineNumber());

		reader.readLine();
		assertEquals(1, reader.getLineNumber());

		reader.readLine();
		assertEquals(4, reader.getLineNumber());

		assertNull(reader.readLine());
	}

	@Test(expected = ICalParseException.class)
	public void invalid_line() throws Throwable {
		String ical = "This is not a valid ical line.";
		ICalRawReader reader = create(ical);
		reader.readLine();
	}

	private static ICalRawReader create(String ical) {
		return new ICalRawReader(new StringReader(ical));
	}

	private static ICalRawLine.Builder line(String name) {
		return new ICalRawLine.Builder().name(name);
	}
}
