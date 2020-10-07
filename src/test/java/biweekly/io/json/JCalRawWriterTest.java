package biweekly.io.json;

import static biweekly.util.StringUtils.NEWLINE;
import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.parameter.ICalParameters;

/*
 Copyright (c) 2013-2020, Michael Angstadt
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
public class JCalRawWriterTest {
	@Test
	public void write_multiple() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, true);

		writer.writeStartComponent("comp");
		writer.writeProperty("prop", ICalDataType.TEXT, JCalValue.single("value"));
		writer.writeEndComponent();
		writer.writeStartComponent("comp");
		writer.writeProperty("prop", ICalDataType.TEXT, JCalValue.single("value"));
		writer.writeEndComponent();
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[" +
			"[\"comp\"," +
				"[" +
					"[\"prop\",{},\"text\",\"value\"]" +
				"]," +
				"[" +
				"]" +
			"]," +
			"[\"comp\"," +
				"[" +
					"[\"prop\",{},\"text\",\"value\"]" +
				"]," +
				"[" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void writeProperty() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		writer.writeStartComponent("comp");
		writer.writeProperty("prop", ICalDataType.TEXT, JCalValue.single("value\nvalue"));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"comp\"," +
			"[" +
				"[\"prop\",{},\"text\",\"value\\nvalue\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void parameters() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		writer.writeStartComponent("comp");
		ICalParameters parameters = new ICalParameters();
		parameters.put("a", "value1");
		parameters.put("b", "value2");
		parameters.put("b", "value3");
		writer.writeProperty("prop", parameters, ICalDataType.TEXT, JCalValue.single("value"));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"comp\"," +
			"[" +
				"[\"prop\",{\"a\":\"value1\",\"b\":[\"value2\",\"value3\"]},\"text\",\"value\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void complex_value() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		writer.writeStartComponent("comp");
		List<JsonValue> jsonValues = new ArrayList<JsonValue>();
		Map<String, JsonValue> m = new LinkedHashMap<String, JsonValue>();
		m.put("a", new JsonValue(Arrays.asList(new JsonValue("one"), new JsonValue("two"))));
		Map<String, JsonValue> m2 = new LinkedHashMap<String, JsonValue>();
		m2.put("c", new JsonValue(Arrays.asList(new JsonValue("three"))));
		m2.put("d", new JsonValue(new LinkedHashMap<String, JsonValue>()));
		m.put("b", new JsonValue(m2));
		jsonValues.add(new JsonValue(m));
		jsonValues.add(new JsonValue("four"));
		writer.writeProperty("prop", ICalDataType.TEXT, new JCalValue(jsonValues));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"comp\"," +
			"[" +
				"[\"prop\",{},\"text\",{" +
					"\"a\":[\"one\",\"two\"]," +
					"\"b\":{" +
						"\"c\":[\"three\"]," +
						"\"d\":{}" +
					"}" +
				"},\"four\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void data_type_unknown() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		writer.writeStartComponent("comp");
		writer.writeProperty("prop", null, JCalValue.single("value"));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"comp\"," +
			"[" +
				"[\"prop\",{},\"unknown\",\"value\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void different_value_types() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		writer.writeStartComponent("comp");
		writer.writeProperty("prop", ICalDataType.TEXT, JCalValue.multi(false, true, 1.1, 1, null, "text"));
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"comp\"," +
			"[" +
				"[\"prop\",{},\"text\",false,true,1.1,1,null,\"text\"]" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void empty() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test(expected = IllegalStateException.class)
	public void write_property_without_component() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		writer.writeProperty("prop", ICalDataType.TEXT, JCalValue.single("value"));
	}

	@Test(expected = IllegalStateException.class)
	public void write_property_after_ending_component() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		writer.writeStartComponent("comp");
		writer.writeStartComponent("comp");
		writer.writeEndComponent();
		writer.writeProperty("prop", ICalDataType.TEXT, JCalValue.single("value"));
	}

	@Test
	public void writeEmptyComponent() throws Exception {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		writer.writeStartComponent("comp");
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"comp\"," +
			"[" +
			"]," +
			"[" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void write_components_deep() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, false);

		//@formatter:off
		writer.writeStartComponent("comp1");
			writer.writeProperty("prop1", ICalDataType.TEXT, JCalValue.single("value1"));
			writer.writeStartComponent("comp2");
				writer.writeProperty("prop2", ICalDataType.TEXT, JCalValue.single("value2"));
				writer.writeStartComponent("comp3");
					writer.writeProperty("prop3", ICalDataType.TEXT, JCalValue.single("value3"));
					writer.writeStartComponent("comp4");
						writer.writeProperty("prop4", ICalDataType.TEXT, JCalValue.single("value4"));
					writer.writeEndComponent();
				writer.writeEndComponent();
				writer.writeStartComponent("comp5");
					writer.writeProperty("prop5", ICalDataType.TEXT, JCalValue.single("value5"));
				writer.writeEndComponent();
			writer.writeEndComponent();
		writer.writeEndComponent();
		//@formatter:on
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =
		"[\"comp1\"," +
			"[" +
				"[\"prop1\",{},\"text\",\"value1\"]" +
			"]," +
			"[" +
				"[\"comp2\"," +
					"[" +
						"[\"prop2\",{},\"text\",\"value2\"]" +
					"]," +
					"[" +
						"[\"comp3\"," +
							"[" +
								"[\"prop3\",{},\"text\",\"value3\"]" +
							"]," +
							"[" +
								"[\"comp4\"," +
									"[" +
										"[\"prop4\",{},\"text\",\"value4\"]" +
									"]," +
									"[" +
									"]" +
								"]" +
							"]" +
						"]," +
						"[\"comp5\"," +
							"[" +
								"[\"prop5\",{},\"text\",\"value5\"]" +
							"]," +
							"[" +
							"]" +
						"]" +
					"]" +
				"]" +
			"]" +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}

	@Test
	public void prettyPrint() throws Throwable {
		StringWriter sw = new StringWriter();
		JCalRawWriter writer = new JCalRawWriter(sw, true);
		writer.setPrettyPrint(true);

		//@formatter:off
		writer.writeStartComponent("empty");
		writer.writeEndComponent();
		writer.writeStartComponent("comp1");
			writer.writeProperty("prop1", ICalDataType.TEXT, JCalValue.single("value1"));
			writer.writeProperty("prop2", ICalDataType.TEXT, JCalValue.single("value2"));
			writer.writeStartComponent("comp2");
				writer.writeStartComponent("comp3");
				writer.writeEndComponent();
				writer.writeStartComponent("comp4");
					writer.writeProperty("prop3", ICalDataType.TEXT, JCalValue.single("value3"));
				writer.writeEndComponent();
			writer.writeEndComponent();
		writer.writeEndComponent();
		writer.writeStartComponent("comp4");
			writer.writeProperty("prop1", ICalDataType.TEXT, JCalValue.single("value1"));
			writer.writeProperty("prop2", ICalDataType.TEXT, JCalValue.single("value2"));
		writer.writeEndComponent();
		//@formatter:on
		writer.close();

		String actual = sw.toString();
		//@formatter:off
		String expected =

		"[" + NEWLINE +
		"  [" + NEWLINE +
		"    \"empty\"," + NEWLINE +
		"    [ ]," + NEWLINE +
		"    [ ]" + NEWLINE +
		"  ]," + NEWLINE +
		"  [" + NEWLINE +
		"    \"comp1\"," + NEWLINE +
		"    [" + NEWLINE +
		"      [ \"prop1\", { }, \"text\", \"value1\" ]," + NEWLINE +
		"      [ \"prop2\", { }, \"text\", \"value2\" ]" + NEWLINE +
		"    ]," + NEWLINE +
		"    [" + NEWLINE +
		"      [" + NEWLINE +
		"        \"comp2\"," + NEWLINE +
		"        [ ]," + NEWLINE +
		"        [" + NEWLINE +
		"          [" + NEWLINE +
		"            \"comp3\"," + NEWLINE +
		"            [ ]," + NEWLINE +
		"            [ ]" + NEWLINE +
		"          ]," + NEWLINE +
		"          [" + NEWLINE +
		"            \"comp4\"," + NEWLINE +
		"            [" + NEWLINE +
		"              [ \"prop3\", { }, \"text\", \"value3\" ]" + NEWLINE +
		"            ]," + NEWLINE +
		"            [ ]" + NEWLINE +
		"          ]" + NEWLINE +
		"        ]" + NEWLINE +
		"      ]" + NEWLINE +
		"    ]" + NEWLINE +
		"  ]," + NEWLINE +
		"  [" + NEWLINE +
		"    \"comp4\"," + NEWLINE +
		"    [" + NEWLINE +
		"      [ \"prop1\", { }, \"text\", \"value1\" ]," + NEWLINE +
		"      [ \"prop2\", { }, \"text\", \"value2\" ]" + NEWLINE +
		"    ]," + NEWLINE +
		"    [ ]" + NEWLINE +
		"  ]" + NEWLINE +
		"]";
		//@formatter:on
		assertEquals(expected, actual);
	}
}
