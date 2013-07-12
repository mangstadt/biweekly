package biweekly.io.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.parameter.Value;
import biweekly.property.Categories;
import biweekly.property.RecurrenceRule;
import biweekly.property.RequestStatus;
import biweekly.property.Summary;
import biweekly.util.ListMultimap;

/**
 * Holds the data type and value of a jCal property.
 * @author Michael Angstadt
 */
public class JCalValue {
	private final Value dataType;
	private final List<JsonValue> values;

	/**
	 * Creates a new jCal value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 */
	public JCalValue(Value dataType, List<JsonValue> values) {
		this.dataType = dataType;
		this.values = Collections.unmodifiableList(values);
	}

	/**
	 * Creates a new jCal value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 */
	public JCalValue(Value dataType, JsonValue... values) {
		this.dataType = dataType;
		this.values = Arrays.asList(values); //unmodifiable
	}

	/**
	 * Creates a single-valued value.
	 * @param dataType the data type or null for "unknown"
	 * @param value the value
	 * @return the jCal value
	 */
	public static JCalValue single(Value dataType, Object value) {
		return new JCalValue(dataType, new JsonValue(value));
	}

	/**
	 * Creates a multi-valued value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue multi(Value dataType, Object... values) {
		return multi(dataType, Arrays.asList(values));
	}

	/**
	 * Creates a multi-valued value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue multi(Value dataType, List<?> values) {
		List<JsonValue> multiValues = new ArrayList<JsonValue>(values.size());
		for (Object value : values) {
			multiValues.add(new JsonValue(value));
		}
		return new JCalValue(dataType, multiValues);
	}

	/**
	 * Creates a structured value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue structured(Value dataType, Object... values) {
		return structured(dataType, Arrays.asList(values));
	}

	/**
	 * Creates a structured value.
	 * @param dataType the data type or null for "unknown"
	 * @param values the values
	 * @return the jCal value
	 */
	public static JCalValue structured(Value dataType, List<?> values) {
		//TODO this should accept a "list of lists"
		List<JsonValue> array = new ArrayList<JsonValue>(values.size());
		for (Object value : values) {
			array.add(new JsonValue(value));
		}
		return new JCalValue(dataType, new JsonValue(array));
	}

	/**
	 * Creates an object value.
	 * @param dataType the data type or null for "unknown"
	 * @param value the object
	 * @return the jCal value
	 */
	public static JCalValue object(Value dataType, ListMultimap<String, Object> value) {
		Map<String, JsonValue> object = new LinkedHashMap<String, JsonValue>();
		for (Map.Entry<String, List<Object>> entry : value) {
			String key = entry.getKey();
			List<Object> list = entry.getValue();

			JsonValue v;
			if (list.size() == 1) {
				v = new JsonValue(list.get(0));
			} else {
				List<JsonValue> array = new ArrayList<JsonValue>(list.size());
				for (Object element : list) {
					array.add(new JsonValue(element));
				}
				v = new JsonValue(array);
			}
			object.put(key, v);
		}
		return new JCalValue(dataType, new JsonValue(object));
	}

	/**
	 * Gets the jCard data type
	 * @return the data type or null for "unknown"
	 */
	public Value getDataType() {
		return dataType;
	}

	/**
	 * Gets all the JSON values.
	 * @return the JSON values
	 */
	public List<JsonValue> getValues() {
		return values;
	}

	/**
	 * Gets the value of a single-valued property (such as {@link Summary}).
	 * @return the value or null if not found
	 */
	public String getSingleValued() {
		if (values.isEmpty()) {
			return null;
		}

		JsonValue first = values.get(0);

		if (first.isNull()) {
			return null;
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

		return null;
	}

	/**
	 * Gets the value of a structured property (such as {@link RequestStatus}).
	 * @return the values or empty list if not found
	 */
	public List<String> getStructured() {
		//TODO this should return a "list of lists"
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		JsonValue first = values.get(0);

		//["request-status", {}, "text", ["2.0", "Success"] ]
		List<JsonValue> array = first.getArray();
		if (array != null) {
			List<String> values = new ArrayList<String>(array.size());
			for (JsonValue value : array) {
				if (value.isNull()) {
					values.add(null);
					continue;
				}

				Object obj = value.getValue();
				if (obj != null) {
					values.add(obj.toString());
				}
			}
			return values;
		}

		//get the first value if it's not enclosed in an array
		//["request-status", {}, "text", "2.0"]
		Object obj = first.getValue();
		if (obj != null) {
			List<String> values = new ArrayList<String>(1);
			values.add(obj.toString());
			return values;
		}

		//["request-status", {}, "text", null]
		if (first.isNull()) {
			List<String> values = new ArrayList<String>(1);
			values.add(null);
			return values;
		}

		return Collections.emptyList();
	}

	/**
	 * Gets the value of a multi-valued property (such as {@link Categories}).
	 * @return the values or empty list if not found
	 */
	public List<String> getMultivalued() {
		if (values.isEmpty()) {
			return Collections.emptyList();
		}

		List<String> multi = new ArrayList<String>(values.size());
		for (JsonValue value : values) {
			if (value.isNull()) {
				multi.add(null);
				continue;
			}

			Object obj = value.getValue();
			if (obj != null) {
				multi.add(obj.toString());
			}
		}
		return multi;
	}

	/**
	 * Gets the value of a property whose value is an object (such as the
	 * {@link RecurrenceRule} property).
	 * @return the object or an empty map if not found
	 */
	public ListMultimap<String, String> getObject() {
		if (values.isEmpty()) {
			return new ListMultimap<String, String>(0);
		}

		Map<String, JsonValue> map = values.get(0).getObject();
		if (map == null) {
			return new ListMultimap<String, String>(0);
		}

		ListMultimap<String, String> values = new ListMultimap<String, String>();
		for (Map.Entry<String, JsonValue> entry : map.entrySet()) {
			String key = entry.getKey();
			JsonValue value = entry.getValue();

			if (value.isNull()) {
				values.put(key, null);
				continue;
			}

			Object obj = value.getValue();
			if (obj != null) {
				values.put(key, obj.toString());
				continue;
			}

			List<JsonValue> array = value.getArray();
			if (array != null) {
				for (JsonValue element : array) {
					obj = element.getValue();
					if (obj != null) {
						values.put(key, obj.toString());
					}
				}
			}
		}
		return values;
	}
}
