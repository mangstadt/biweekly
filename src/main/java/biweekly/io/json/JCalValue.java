package biweekly.io.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.mangstadt.vinnie.LaciToken;

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
 * Holds the value of a jCal property.
 * @author Michael Angstadt
 */
public class JCalValue {
	private final List<JsonValue> values;

	/**
	 * Creates a new jCal value.
	 * @param values the values
	 */
	public JCalValue(List<JsonValue> values) {
		this.values = Collections.unmodifiableList(values);
	}

	/**
	 * Creates a new jCal value.
	 * @param values the values
	 */
	public JCalValue(JsonValue... values) {
		this.values = Arrays.asList(values); //unmodifiable
	}

	/**
	 * Creates a single-valued value.
	 * @param value the value
	 * @return the jCal value
	 */
	public static JCalValue single(Object value) {
		return new JCalValue(new JsonValue(value));
	}

	/**
	 * Creates a multi-valued value.
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue multi(Object... values) {
		return multi(Arrays.asList(values));
	}

	/**
	 * Creates a multi-valued value.
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue multi(List<?> values) {
		//@formatter:off
		return new JCalValue(values.stream()
			.map(JsonValue::new)
		.collect(Collectors.toList()));
		//@formatter:on
	}

	/**
	 * <p>
	 * Creates a structured value.
	 * </p>
	 * <p>
	 * This method accepts a vararg of {@link Object} instances. {@link List}
	 * objects will be treated as multi-valued components. All other objects.
	 * Null values will be treated as empty components.
	 * </p>
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue structured(Object... values) {
		//@formatter:off
		return structured(Arrays.stream(values)
			.map(value -> (value instanceof List) ? (List<?>) value : Collections.singletonList(value))
		.collect(Collectors.toList()));
		//@formatter:on
	}

	/**
	 * Creates a structured value.
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue structured(List<List<?>> values) {
		List<JsonValue> array = values.stream().map(list -> {
			if (list.isEmpty()) {
				return new JsonValue("");
			}

			if (list.size() == 1) {
				Object value = nullToEmptyString(list.get(0));
				return new JsonValue(value);
			}

			//@formatter:off
			return new JsonValue(list.stream()
				.map(JCalValue::nullToEmptyString)
				.map(JsonValue::new)
			.collect(Collectors.toList()));
			//@formatter:on
		}).collect(Collectors.toList());

		return new JCalValue(new JsonValue(array));
	}
	
	private static Object nullToEmptyString(Object o) {
		return (o == null) ? "" : o;
	}

	/**
	 * Creates an object value.
	 * @param value the object
	 * @return the jCal value
	 */
	public static JCalValue object(ListMultimap<String, Object> value) {
		Map<String, JsonValue> object = new LinkedHashMap<>();
		for (Map.Entry<String, List<Object>> entry : value) {
			String key = entry.getKey();
			List<Object> list = entry.getValue();

			JsonValue v;
			if (list.size() == 1) {
				v = new JsonValue(list.get(0));
			} else {
				List<JsonValue> array = new ArrayList<>(list.size());
				for (Object element : list) {
					array.add(new JsonValue(element));
				}
				v = new JsonValue(array);
			}
			object.put(key, v);
		}
		return new JCalValue(new JsonValue(object));
	}

	/**
	 * Gets the raw JSON values. Use one of the "{@code as*}" methods to parse
	 * the values as one of the standard jCal values.
	 * @return the JSON values
	 */
	public List<JsonValue> getValues() {
		return values;
	}

	/**
	 * Parses this jCal value as a single-valued property value.
	 * @return the value or empty string if not found
	 */
	public String asSingle() {
		if (values.isEmpty()) {
			return "";
		}

		JsonValue first = values.get(0);
		if (first.isNull()) {
			return "";
		}

		Object obj = first.getValue();
		if (obj != null) {
			return obj.toString();
		}

		//get the first element of the array
		List<JsonValue> array = first.getArray();
		if (array != null && !array.isEmpty()) {
			obj = array.get(0).getValue();
			if (obj != null) {
				return obj.toString();
			}
		}

		return "";
	}

	/**
	 * Parses this jCal value as a structured property value.
	 * @return the structured values or empty list if not found
	 */
	public List<List<String>> asStructured() {
		return new StructuredValueConverter().convert();
	}

	private class StructuredValueConverter {
		public List<List<String>> convert() {
			if (values.isEmpty()) {
				return Collections.emptyList();
			}

			JsonValue first = values.get(0);

			//["request-status", {}, "text", ["2.0", "Success"] ]
			List<JsonValue> array = first.getArray();
			if (array != null) {
				return fromArray(array);
			}

			//get the first value if it's not enclosed in an array
			//["request-status", {}, "text", "2.0"]
			Object obj = first.getValue();
			if (obj != null) {
				return Collections.singletonList(fromValue(obj));
			}

			//["request-status", {}, "text", null]
			if (first.isNull()) {
				return Collections.singletonList(fromNull());
			}

			return Collections.emptyList();
		}

		private List<List<String>> fromArray(List<JsonValue> array) {
			//@formatter:off
			return array.stream()
				.map(this::fromArrayValue)
				.filter(v -> v != null)
			.collect(Collectors.toList());
			//@formatter:on
		}

		private List<String> fromArrayValue(JsonValue value) {
			if (value.isNull()) {
				return fromNull();
			}

			Object obj = value.getValue();
			if (obj != null) {
				return fromValue(obj);
			}

			List<JsonValue> subArray = value.getArray();
			if (subArray != null) {
				//@formatter:off
				List<String> component = subArray.stream()
					.map(jsonValue -> jsonValue.isNull() ? "" : jsonValue.getValue())
					.filter(o -> o != null)
					.map(Object::toString)
				.collect(Collectors.toList());
				//@formatter:on

				if (component.size() == 1 && component.get(0).isEmpty()) {
					return Collections.emptyList();
				}
				return component;
			}

			return null;
		}

		private List<String> fromNull() {
			return Collections.emptyList();
		}

		private List<String> fromValue(Object obj) {
			String s = obj.toString();
			return s.isEmpty() ? Collections.emptyList() : Collections.singletonList(s);
		}
	}

	/**
	 * Parses this jCal value as a multi-valued property value.
	 * @return the values or empty list if not found
	 */
	public List<String> asMulti() {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> multi = new ArrayList<>(values.size());
		for (JsonValue value : values) {
			if (value.isNull()) {
				multi.add("");
				continue;
			}

			Object obj = value.getValue();
			if (obj != null) {
				multi.add(obj.toString());
				continue;
			}
		}
		return multi;
	}

	/**
	 * Parses this jCal value as an object property value.
	 * @return the object or an empty map if not found
	 */
	public ListMultimap<LaciToken, String> asObject() {
		if (values.isEmpty()) {
			return new ListMultimap<>(0);
		}

		Map<String, JsonValue> map = values.get(0).getObject();
		if (map == null) {
			return new ListMultimap<>(0);
		}

		ListMultimap<LaciToken, String> values = new ListMultimap<>();
		map.forEach((keyStr, value) -> {
			LaciToken key = new LaciToken(keyStr);

			if (value.isNull()) {
				values.put(key, "");
				return;
			}

			Object obj = value.getValue();
			if (obj != null) {
				values.put(key, obj.toString());
				return;
			}

			List<JsonValue> array = value.getArray();
			if (array != null) {
				for (JsonValue element : array) {
					if (element.isNull()) {
						values.put(key, "");
						continue;
					}

					obj = element.getValue();
					if (obj != null) {
						values.put(key, obj.toString());
					}
				}
			}
		});
		return values;
	}
}
