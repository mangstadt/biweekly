package biweekly.io.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import biweekly.util.ListMultimap;

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
 * @author Michael Angstadt
 */
public class JCalValueTest {
	@Test
	public void single() {
		JCalValue value = JCalValue.single("value");

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue("value")
		);
		//@formatter:on
		List<JsonValue> actual = value.getValues();
		assertEquals(expected, actual);
	}

	@Test
	public void single_null() {
		JCalValue value = JCalValue.single(null);

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue((Object)null)
		);
		//@formatter:on
		List<JsonValue> actual = value.getValues();
		assertEquals(expected, actual);
	}

	@Test
	public void asSingle() {
		JCalValue value = new JCalValue(new JsonValue("value1"), new JsonValue("value2"));
		assertEquals("value1", value.asSingle());
	}

	@Test
	public void asSingle_non_string() {
		JCalValue value = new JCalValue(new JsonValue(false));
		assertEquals("false", value.asSingle());
	}

	@Test
	public void asSingle_null() {
		JCalValue value = new JCalValue(new JsonValue((Object) null));
		assertEquals("", value.asSingle());
	}

	@Test
	public void asSingle_array() {
		JCalValue value = new JCalValue(new JsonValue(Arrays.asList(new JsonValue("value1"), new JsonValue("value1"))));
		assertEquals("value1", value.asSingle());
	}

	@Test
	public void asSingle_object() {
		Map<String, JsonValue> object = new HashMap<String, JsonValue>();
		object.put("a", new JsonValue("one"));
		JCalValue value = new JCalValue(new JsonValue(object));
		assertEquals("", value.asSingle());
	}

	@Test
	public void multi() {
		JCalValue value = JCalValue.multi("value", 42, false, null);

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue("value"),
			new JsonValue(42),
			new JsonValue(false),
			new JsonValue((Object)null)
		);
		//@formatter:on
		List<JsonValue> actual = value.getValues();
		assertEquals(expected, actual);
	}

	@Test
	public void asMulti() {
		JCalValue value = new JCalValue(new JsonValue("value1"), new JsonValue(false), new JsonValue((Object) null));
		assertEquals(Arrays.asList("value1", "false", ""), value.asMulti());
	}

	@Test
	public void asMulti_array() {
		JCalValue value = new JCalValue(new JsonValue(Arrays.asList(new JsonValue("value1"), new JsonValue(false))));
		assertEquals(Arrays.asList(), value.asMulti());
	}

	@Test
	public void asMulti_object() {
		Map<String, JsonValue> object = new HashMap<String, JsonValue>();
		object.put("a", new JsonValue("one"));
		JCalValue value = new JCalValue(new JsonValue(object));
		assertEquals(Arrays.asList(), value.asMulti());
	}

	@Test
	public void structured() {
		JCalValue value = JCalValue.structured("value", 42, false, null, Arrays.asList("one", "two"));

		//@formatter:off
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(Arrays.asList(
				new JsonValue("value"),
				new JsonValue(42),
				new JsonValue(false),
				new JsonValue(""),
				new JsonValue(Arrays.asList(
					new JsonValue("one"),
					new JsonValue("two")
				))
			))
		);
		//@formatter:on
		List<JsonValue> actual = value.getValues();
		assertEquals(expected, actual);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void asStructured() {
		//@formatter:off
		JCalValue value = new JCalValue(
			new JsonValue(Arrays.asList(
				new JsonValue("value1"), new JsonValue(false), JsonValue.nullValue()
			))
		);

		assertEquals(Arrays.asList(
			Arrays.asList("value1"),
			Arrays.asList("false"),
			Arrays.asList()),
		value.asStructured());
		//@formatter:on
	}

	@SuppressWarnings("unchecked")
	@Test
	public void asStructured_sub_array() {
		//@formatter:off
		JCalValue value = new JCalValue(
			new JsonValue(Arrays.asList(
				new JsonValue(Arrays.asList(
					new JsonValue("value1"),
					new JsonValue("value2"),
					JsonValue.nullValue(),
					new JsonValue(Arrays.asList(new JsonValue("value3"))) //ignore arrays that are nested this deep
				)),
				new JsonValue(Arrays.asList( //sub arrays that only contain a single empty element are converted to empty sub-arrays
					new JsonValue("")
				)),
				new JsonValue(Arrays.asList( //sub arrays that only contain a single null element are converted to empty sub-arrays
					JsonValue.nullValue()
				))
			))
		);

		assertEquals(Arrays.asList(
			Arrays.asList("value1", "value2", ""),
			Arrays.asList(),
			Arrays.asList()),
		value.asStructured());
		//@formatter:on
	}

	@SuppressWarnings("unchecked")
	@Test
	public void asStructured_single_value() {
		JCalValue value = new JCalValue(new JsonValue("value1"));
		assertEquals(Arrays.asList(Arrays.asList("value1")), value.asStructured());
	}

	@Test
	public void asStructured_object() {
		Map<String, JsonValue> jsonObject = new HashMap<String, JsonValue>();
		jsonObject.put("a", new JsonValue("one"));

		JCalValue value = new JCalValue(new JsonValue(jsonObject));
		assertEquals(Arrays.asList(), value.asStructured()); //JSON objects are ignored

		value = new JCalValue(new JsonValue(Arrays.asList(new JsonValue(jsonObject))));
		assertEquals(Arrays.asList(), value.asStructured()); //JSON objects are ignored
	}

	@SuppressWarnings("unchecked")
	@Test
	public void asStructured_null() {
		JCalValue value = new JCalValue(JsonValue.nullValue());
		assertEquals(Arrays.asList(Arrays.asList()), value.asStructured());
	}

	@Test
	public void asStructured_empty() {
		JCalValue value = new JCalValue();
		assertEquals(Arrays.asList(), value.asStructured());
	}

	@Test
	public void object() {
		ListMultimap<String, Object> object = new ListMultimap<String, Object>();
		object.put("a", "one");
		object.put("b", 2);
		object.put("b", 3.0);
		object.put("b", null);
		JCalValue value = JCalValue.object(object);

		//@formatter:off
		Map<String, JsonValue> expectedMap = new HashMap<String, JsonValue>();
		expectedMap.put("a", new JsonValue("one"));
		expectedMap.put("b", new JsonValue(Arrays.asList(new JsonValue(2), new JsonValue(3.0), JsonValue.nullValue())));
		List<JsonValue> expected = Arrays.asList(
			new JsonValue(expectedMap)
		);
		//@formatter:on
		List<JsonValue> actual = value.getValues();
		assertEquals(expected, actual);
	}

	@Test
	public void asObject() {
		Map<String, JsonValue> object = new LinkedHashMap<String, JsonValue>();
		object.put("a", new JsonValue("one"));
		object.put("b", new JsonValue(Arrays.asList(new JsonValue(2), new JsonValue(3.0), JsonValue.nullValue())));
		object.put("c", new JsonValue((Object) null));
		JCalValue value = new JCalValue(new JsonValue(object));

		ListMultimap<String, Object> expected = new ListMultimap<String, Object>();
		expected.put("a", "one");
		expected.put("b", "2");
		expected.put("b", "3.0");
		expected.put("b", "");
		expected.put("c", "");
		assertEquals(expected, value.asObject());
	}

	@Test
	public void asObject_single_value() {
		JCalValue value = new JCalValue(new JsonValue("value1"));
		assertTrue(value.asObject().isEmpty());
	}

	@Test
	public void asObject_array() {
		JCalValue value = new JCalValue(new JsonValue(Arrays.asList(new JsonValue("value1"), new JsonValue(false))));
		assertTrue(value.asObject().isEmpty());
	}
}
