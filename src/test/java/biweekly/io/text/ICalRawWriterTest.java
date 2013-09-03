package biweekly.io.text;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import biweekly.io.text.ICalRawWriter.ParameterValueChangedListener;
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
@SuppressWarnings("resource")
public class ICalRawWriterTest {
	@Test
	public void property() throws Exception {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);

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
	public void invalid_property_name() throws Exception {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);

		writer.writeProperty("INVALID*PROP", "value");
	}

	@Test
	public void writeBeginComponent() throws Exception {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);

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
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);

		writer.writeEndComponent("COMP");
		writer.close();

		//@formatter:off
		String expected =
		"END:COMP\r\n";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void folding() throws Exception {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);

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
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw, new FoldingScheme(100, "\t"), "*");

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
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw, null, "*");

		writer.writeProperty("PROP", "The use of calendaring and scheduling has grown considerably in the last decade. Enterprise and inter-enterprise business has become dependent on rapid scheduling of events and actions using this information technology.");
		writer.close();

		//@formatter:off
		String expected =
		"PROP:The use of calendaring and scheduling has grown considerably in the last decade. Enterprise and inter-enterprise business has become dependent on rapid scheduling of events and actions using this information technology.*";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void caret_encoding_on() throws Exception {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);
		writer.setCaretEncodingEnabled(true);

		ICalParameters params = new ICalParameters();
		params.put("PARAM", "foo\n \"bar\" ^_^");
		writer.writeProperty("PROP", params, "value");
		writer.close();

		//@formatter:off
		String expected =
		"PROP;PARAM=foo^n ^'bar^' ^^_^^:value\r\n";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void caret_encoding_off() throws Exception {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);
		writer.setCaretEncodingEnabled(false);

		ICalParameters params = new ICalParameters();
		params.put("PARAM", "foo\n \"bar\" ^_^");
		writer.writeProperty("PROP", params, "value");
		writer.close();

		//@formatter:off
		String expected =
		"PROP;PARAM=foo  'bar' ^_^:value\r\n";
		//@formatter:on

		assertEquals(expected, sw.toString());
	}

	@Test
	public void multiple_parameters() throws Exception {
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);

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
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);

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
	public void invalid_parameter_value_chars() throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 31; i++) { //control characters should be removed (except for \t, \n, and \r)
			if (i != '\t' && i != '\n' && i != '\r') {
				sb.append((char) i);
			}
		}
		sb.append("\r\n");
		sb.append('"');
		sb.append('\n');
		sb.append('\r');
		final String expectedOriginalValue = sb.toString();

		sb = new StringBuilder();
		sb.append(' ');
		sb.append('\'');
		sb.append(' ');
		sb.append(' ');
		final String expectedModifiedValue = sb.toString();

		final List<Boolean> hit = new ArrayList<Boolean>();
		StringWriter sw = new StringWriter();
		ICalRawWriter writer = new ICalRawWriter(sw);
		writer.setParameterValueChangedListener(new ParameterValueChangedListener() {
			public void onParameterValueChanged(String propertyName, String parameterName, String originalValue, String modifiedValue) {
				assertEquals("PROP", propertyName);
				assertEquals("PARAM", parameterName);
				assertEquals(expectedOriginalValue, originalValue);
				assertEquals(expectedModifiedValue, modifiedValue);
				hit.add(true);
			}
		});

		ICalParameters params = new ICalParameters();
		params.put("PARAM", expectedOriginalValue);
		writer.writeProperty("PROP", params, "value");
		writer.close();

		//@formatter:off
		String expected =
		"PROP;PARAM=" + expectedModifiedValue + ":value\r\n";
		//@formatter:on

		assertEquals(1, hit.size());
		assertEquals(expected, sw.toString());
	}
}
