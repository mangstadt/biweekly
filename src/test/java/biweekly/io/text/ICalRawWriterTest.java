package biweekly.io.text;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import biweekly.ICalVersion;
import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;
import biweekly.util.org.apache.commons.codec.net.QuotedPrintableCodec;

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
@SuppressWarnings("resource")
public class ICalRawWriterTest {
	private StringWriter sw;
	private ICalRawWriter writer;

	@Before
	public void before() throws Exception {
		sw = new StringWriter();
		writer = new ICalRawWriter(sw, V2_0);
	}

	@Test
	public void property() throws Exception {
		writer.writeProperty("PROP", "value1");

		ICalParameters params = new ICalParameters();
		params.setLanguage("en");
		writer.writeProperty("PROP", params, "value2");

		writer.close();

		//@formatter:off
		String expected =
		"PROP:value1\r\n" +
		"PROP;LANGUAGE=en:value2\r\n";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalid_property_name_characters() throws Exception {
		writer.writeProperty("INVALID:PROP", "value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalid_property_name_whitespace() throws Exception {
		writer.writeProperty(" INVALIDPROP", "value");
	}

	@Test
	public void writeBeginComponent() throws Exception {
		writer.writeBeginComponent("COMP");
		writer.close();

		//@formatter:off
		String expected =
		"BEGIN:COMP\r\n";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void writeEndComponent() throws Exception {
		writer.writeEndComponent("COMP");
		writer.close();

		//@formatter:off
		String expected =
		"END:COMP\r\n";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void writeVersion() throws Throwable {
		writer.writeVersion();

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"VERSION:2.0\r\n";
		//@formatter:on

		assertEquals(expected, actual);
	}

	@Test
	public void folding() throws Exception {
		writer.writeProperty("PROP", "The use of calendaring and scheduling has grown considerably in the last decade. Enterprise and inter-enterprise business has become dependent      on rapid scheduling of events and actions using this information technology.");
		writer.close();

		//@formatter:off
		String expected =
		"PROP:The use of calendaring and scheduling has grown considerably in the la\r\n" +
		" st decade. Enterprise and inter-enterprise business has become dependent      \r\n" + //ensure that it doesn't fold in the middle of a sequence of spaces
		" on rapid scheduling of events and actions using this information technolog\r\n" +
		" y.\r\n";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void custom_foldingScheme_and_newline() throws Exception {
		writer = new ICalRawWriter(sw, V2_0);
		writer.getFoldedLineWriter().setIndent("\t");
		writer.getFoldedLineWriter().setLineLength(100);
		writer.getFoldedLineWriter().setNewline("*");

		writer.writeProperty("PROP", "The use of calendaring and scheduling has grown considerably in the last decade. Enterprise and inter-enterprise business has become dependent on rapid scheduling of events and actions using this information technology.");
		writer.close();

		//@formatter:off
		String expected =
		"PROP:The use of calendaring and scheduling has grown considerably in the last decade. Enterprise and *" + 
		"\tinter-enterprise business has become dependent on rapid scheduling of events and actions using this *" +
		"\tinformation technology.*";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void no_foldingScheme() throws Exception {
		writer = new ICalRawWriter(sw, V2_0);
		writer.getFoldedLineWriter().setLineLength(null);
		writer.getFoldedLineWriter().setNewline("*");

		writer.writeProperty("PROP", "The use of calendaring and scheduling has grown considerably in the last decade. Enterprise and inter-enterprise business has become dependent on rapid scheduling of events and actions using this information technology.");
		writer.close();

		//@formatter:off
		String expected =
		"PROP:The use of calendaring and scheduling has grown considerably in the last decade. Enterprise and inter-enterprise business has become dependent on rapid scheduling of events and actions using this information technology.*";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void newline() throws Throwable {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw, V1_0);
		writer.getFoldedLineWriter().setNewline("*");

		writer.writeProperty("PROP", "one");
		writer.writeProperty("PROP", "two");

		String actual = sw.toString();

		//@formatter:off
		String expected =
		"PROP:one*" +
		"PROP:two*";
		//@formatter:on

		assertEquals(actual, expected);
	}

	@Test
	public void multiple_parameters() throws Exception {
		ICalParameters params = new ICalParameters();
		params.put("PARAM", "one");
		params.put("PARAM", "two");
		params.put("PARAM", "three,four");
		writer.writeProperty("PROP", params, "value");
		writer.close();

		//@formatter:off
		String expected =
		"PROP;PARAM=one,two,\"three,four\":value\r\n";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void quoted_parameters() throws Exception {
		ICalParameters params = new ICalParameters();
		params.put("PARAM", "three,;:four");
		writer.writeProperty("PROP", params, "value");
		writer.close();

		//@formatter:off
		String expected =
		"PROP;PARAM=\"three,;:four\":value\r\n";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void parameters_special_chars() throws Throwable {
		//1.0 without caret escaping
		//removes , : = [ ] FS
		//replaces \ with \\
		//replaces ; with \;
		//replaces newline with space
		assertParametersSpecialChars(V1_0, false, "PROP;X-TEST=^�\\\\\\;\"\t ;X-TEST=normal:\r\n");

		//1.0 with caret escaping (ignored)
		//removes , : = [ ] FS
		//replaces \ with \\
		//replaces ; with \;
		//replaces newline with space
		assertParametersSpecialChars(V1_0, true, "PROP;X-TEST=^�\\\\\\;\"\t ;X-TEST=normal:\r\n");

		//2.0 without caret escaping
		//removes FS
		//replaces \ with \\
		//replaces newline with space
		//replaces " with '
		//surrounds in double quotes, since it contains , ; or :
		assertParametersSpecialChars(V2_0, false, "PROP;X-TEST=\"^�\\,;:=[]'\t \",normal:\r\n");

		//2.0 with caret escaping
		//removes FS
		//replaces ^ with ^^
		//replaces newline with ^n
		//replaces " with ^'
		//surrounds in double quotes, since it contains , ; or :
		assertParametersSpecialChars(V2_0, true, "PROP;X-TEST=\"^^�\\,;:=[]^'\t^n\",normal:\r\n");
	}

	private void assertParametersSpecialChars(ICalVersion version, boolean caretEncodingEnabled, String expected) throws IOException {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw, version);
		writer.setCaretEncodingEnabled(caretEncodingEnabled);

		ICalParameters parameters = new ICalParameters();
		parameters.put("X-TEST", "^�\\,;:=[]\"\t\n" + ((char) 28));
		parameters.put("X-TEST", "normal");
		writer.writeProperty("PROP", parameters, "");

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	/*
	 * If newline characters exist in a property value in 2.1, then that
	 * property value should be "quoted-printable" encoded. The escape sequence
	 * "\n" should ONLY be used for 3.0 and 4.0. See the "Delimiters" subsection
	 * in section 2 of the 2.1 specs.
	 */
	@Test
	public void newlines_in_property_values() throws Throwable {
		assertNewlinesInPropertyValues(V1_0, "PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:one=0D=0Atwo\r\n");
		assertNewlinesInPropertyValues(V2_0, "PROP:one\\ntwo\r\n");
	}

	private void assertNewlinesInPropertyValues(ICalVersion version, String expected) throws IOException {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw, version);

		writer.writeProperty("PROP", "one\r\ntwo");

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	/*
	 * Property values that use "quoted-printable" encoding must include a "="
	 * at the end of the line if the next line is folded.
	 */
	@Test
	public void quoted_printable_line() throws Throwable {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw, V1_0);
		writer.getFoldedLineWriter().setLineLength(60);

		ICalParameters parameters = new ICalParameters();
		parameters.setEncoding(Encoding.QUOTED_PRINTABLE);

		writer.writeProperty("PROP", parameters, "quoted-printable \r\nline");
		writer.writeProperty("PROP", parameters, "short");
		writer.close();

		//must construct the first line differently, since the length of the CHARSET parameter will vary depending on the local machine
		String firstLine = "PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:quoted-printable =0D=0Aline";
		firstLine = firstLine.substring(0, 59) + "=\r\n " + firstLine.substring(59);

		//@formatter:off
		String expected = firstLine + "\r\n" +
		"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:short\r\n";
		//@formatter:on

		String actual = sw.toString();
		assertEquals(expected, actual);
	}

	@Test
	public void quoted_printable_line_encoding() throws Throwable {
		final String propValue = "\u00e4\u00f6\u00fc\u00df";

		//UTF-8
		{
			StringWriter sw = new StringWriter();
			ICalRawWriter writer = new ICalRawWriter(sw, V1_0);

			ICalParameters parameters = new ICalParameters();
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			parameters.setCharset("UTF-8");

			writer.writeProperty("PROP", parameters, propValue);
			writer.close();

			//@formatter:off
			String expected =
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:=C3=A4=C3=B6=C3=BC=C3=9F\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}

		//ISO-8859-1
		{
			StringWriter sw = new StringWriter();
			ICalRawWriter writer = new ICalRawWriter(sw, V1_0);

			ICalParameters parameters = new ICalParameters();
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			parameters.setCharset("ISO-8859-1");

			writer.writeProperty("PROP", parameters, propValue);
			writer.close();

			//@formatter:off
			String expected =
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=ISO-8859-1:=E4=F6=FC=DF\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}

		//invalid
		{
			StringWriter sw = new StringWriter();
			ICalRawWriter writer = new ICalRawWriter(sw, V1_0);

			ICalParameters parameters = new ICalParameters();
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			parameters.setCharset("invalid");

			writer.writeProperty("PROP", parameters, propValue);
			writer.close();

			QuotedPrintableCodec codec = new QuotedPrintableCodec("UTF-8");
			String encoded = codec.encode(propValue);

			//@formatter:off
			String expected =
			"PROP;ENCODING=QUOTED-PRINTABLE;CHARSET=UTF-8:" + encoded + "\r\n";
			//@formatter:on

			String actual = sw.toString();
			assertEquals(expected, actual);
		}
	}
}
