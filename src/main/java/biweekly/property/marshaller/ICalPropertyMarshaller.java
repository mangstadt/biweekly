package biweekly.property.marshaller;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.io.CannotParseException;
import biweekly.io.SkipMeException;
import biweekly.io.json.JCalValue;
import biweekly.io.text.ICalRawWriter;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.util.ICalDateFormatter;
import biweekly.util.ISOFormat;
import biweekly.util.ListMultimap;
import biweekly.util.StringUtils;
import biweekly.util.StringUtils.JoinCallback;
import biweekly.util.StringUtils.JoinMapCallback;
import biweekly.util.XmlUtils;

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
 * Base class for iCalendar property marshallers.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class ICalPropertyMarshaller<T extends ICalProperty> {
	protected final Class<T> clazz;
	protected final String propertyName;
	protected final ICalDataType defaultDataType;
	protected final QName qname;

	/**
	 * Creates a new marshaller.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "VERSION")
	 * @param defaultDataType the property's default data type (e.g. "text") or
	 * null if unknown
	 */
	public ICalPropertyMarshaller(Class<T> clazz, String propertyName, ICalDataType defaultDataType) {
		this(clazz, propertyName, defaultDataType, new QName(XCAL_NS, propertyName.toLowerCase()));
	}

	/**
	 * Creates a new marshaller.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "VERSION")
	 * @param defaultDataType the property's default data type (e.g. "text") or
	 * null if unknown
	 * @param qname the XML element name and namespace to use for xCal documents
	 * (by default, the XML element name is set to the lower-cased property
	 * name, and the element namespace is set to the xCal namespace)
	 */
	public ICalPropertyMarshaller(Class<T> clazz, String propertyName, ICalDataType defaultDataType, QName qname) {
		this.clazz = clazz;
		this.propertyName = propertyName;
		this.defaultDataType = defaultDataType;
		this.qname = qname;
	}

	/**
	 * Gets the property class.
	 * @return the property class
	 */
	public Class<T> getPropertyClass() {
		return clazz;
	}

	/**
	 * Gets the property name.
	 * @return the property name (e.g. "VERSION")
	 */
	public String getPropertyName() {
		return propertyName;
	}

	/**
	 * Gets the property's default data type.
	 * @return the default data type (e.g. "text") or null if unknown
	 */
	public ICalDataType getDefaultDataType() {
		return defaultDataType;
	}

	/**
	 * Gets this property's local name and namespace for xCal documents.
	 * @return the XML local name and namespace
	 */
	public QName getQName() {
		return qname;
	}

	/**
	 * Sanitizes a property's parameters (called before the property is
	 * written). Note that a copy of the parameters is returned so that the
	 * property object does not get modified.
	 * @param property the property
	 * @return the sanitized parameters
	 */
	public final ICalParameters prepareParameters(T property) {
		//make a copy because the property should not get modified when it is marshalled
		ICalParameters copy = new ICalParameters(property.getParameters());
		_prepareParameters(property, copy);
		return copy;
	}

	/**
	 * Determines the data type of a property instance.
	 * @param property the property
	 * @return the data type or null if unknown
	 */
	public final ICalDataType dataType(T property) {
		return _dataType(property);
	}

	/**
	 * Marshals a property's value to a string.
	 * @param property the property
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	public final String writeText(T property) {
		return _writeText(property);
	}

	/**
	 * Marshals a property's value to an XML element (xCal).
	 * @param property the property
	 * @param element the property's XML element
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	public final void writeXml(T property, Element element) {
		XCalElement xcalElement = new XCalElement(element);
		_writeXml(property, xcalElement);
	}

	/**
	 * Marshals a property's value to a JSON data stream (jCal).
	 * @param property the property
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	public final JCalValue writeJson(T property) {
		return _writeJson(property);
	}

	/**
	 * Unmarshals a property from a plain-text iCalendar data stream.
	 * @param value the value as read off the wire
	 * @param dataType the data type of the property value. The property's VALUE
	 * parameter is used to determine the data type. If the property has no
	 * VALUE parameter, then this parameter will be set to the property's
	 * default datatype. Note that the VALUE parameter is removed from the
	 * property's parameter list after it has been read.
	 * @param parameters the parsed parameters
	 * @return the unmarshalled property and its warnings
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	public final Result<T> parseText(String value, ICalDataType dataType, ICalParameters parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseText(value, dataType, parameters, warnings);
		property.setParameters(parameters);
		return new Result<T>(property, warnings);
	}

	/**
	 * Unmarshals a property's value from an XML document (xCal).
	 * @param element the property's XML element
	 * @param parameters the property's parameters
	 * @return the unmarshalled property and its warnings
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	public final Result<T> parseXml(Element element, ICalParameters parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseXml(new XCalElement(element), parameters, warnings);
		property.setParameters(parameters);
		return new Result<T>(property, warnings);
	}

	/**
	 * Unmarshals a property's value from a JSON data stream (jCal).
	 * @param value the property's JSON value
	 * @param dataType the data type
	 * @param parameters the parsed parameters
	 * @return the unmarshalled property and its warnings
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	public final Result<T> parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseJson(value, dataType, parameters, warnings);
		property.setParameters(parameters);
		return new Result<T>(property, warnings);
	}

	/**
	 * <p>
	 * Sanitizes a property's parameters before the property is written.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to tweak the
	 * property's parameters before the property is written. The default
	 * implementation of this method does nothing.
	 * </p>
	 * @param property the property
	 * @param copy the list of parameters to make modifications to (it is a copy
	 * of the property's parameters)
	 */
	protected void _prepareParameters(T property, ICalParameters copy) {
		//do nothing
	}

	/**
	 * <p>
	 * Determines the data type of a property instance.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes if a property's data
	 * type changes depending on its value. The default implementation of this
	 * method returns the property's default data type.
	 * </p>
	 * @param property the property
	 * @return the data type or null if unknown
	 */
	protected ICalDataType _dataType(T property) {
		return defaultDataType;
	}

	/**
	 * Marshals a property's value to a string.
	 * @param property the property
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected abstract String _writeText(T property);

	/**
	 * <p>
	 * Marshals a property's value to an XML element (xCal).
	 * <p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * xCal. The default implementation of this method will append one child
	 * element to the property's XML element. The child element's name will be
	 * that of the property's data type (retrieved using the {@link #dataType}
	 * method), and the child element's text content will be set to the
	 * property's marshalled plain-text value (retrieved using the
	 * {@link #writeText} method).
	 * </p>
	 * @param property the property
	 * @param element the property's XML element
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected void _writeXml(T property, XCalElement element) {
		String value = writeText(property);
		ICalDataType dataType = dataType(property);
		element.append(dataType, value);
	}

	/**
	 * <p>
	 * Marshals a property's value to a JSON data stream (jCal).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * jCal. The default implementation of this method will create a jCard
	 * property that has a single JSON string value (generated by the
	 * {@link #writeText} method).
	 * </p>
	 * @param property the property
	 * @return the marshalled value
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected JCalValue _writeJson(T property) {
		String value = writeText(property);
		return JCalValue.single(value);
	}

	/**
	 * Unmarshals a property from a plain-text iCalendar data stream.
	 * @param value the value as read off the wire
	 * @param dataType the data type of the property value. The property's VALUE
	 * parameter is used to determine the data type. If the property has no
	 * VALUE parameter, then this parameter will be set to the property's
	 * default datatype. Note that the VALUE parameter is removed from the
	 * property's parameter list after it has been read.
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	protected abstract T _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<String> warnings);

	/**
	 * <p>
	 * Unmarshals a property from an XML document (xCal).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * xCal. The default implementation of this method will find the first child
	 * element with the xCal namespace. The element's name will be used as the
	 * property's data type and its text content will be passed into the
	 * {@link #_parseText} method. If no such child element is found, then the
	 * parent element's text content will be passed into {@link #_parseText} and
	 * the data type will be null.
	 * </p>
	 * @param element the property's XML element
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	protected T _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		String value = null;
		ICalDataType dataType = null;
		Element rawElement = element.getElement();

		//get the text content of the first child element with the xCard namespace
		List<Element> children = XmlUtils.toElementList(rawElement.getChildNodes());
		for (Element child : children) {
			if (!XCAL_NS.equals(child.getNamespaceURI())) {
				continue;
			}

			dataType = ICalDataType.get(child.getLocalName());
			value = child.getTextContent();
			break;
		}

		if (dataType == null) {
			//get the text content of the property element
			value = rawElement.getTextContent();
		}

		value = escape(value);
		return _parseText(value, dataType, parameters, warnings);
	}

	/**
	 * /**
	 * <p>
	 * Unmarshals a property from a JSON data stream (jCal).
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to support
	 * jCal. The default implementation of this method will convert the jCal
	 * property value to a string and pass it into the {@link #_parseText}
	 * method.
	 * </p>
	 * 
	 * <hr>
	 * 
	 * <p>
	 * The following paragraphs describe the way in which this method's default
	 * implementation converts a jCal value to a string:
	 * </p>
	 * <p>
	 * If the jCal value consists of a single, non-array, non-object value, then
	 * the value is converted to a string. Special characters (backslashes,
	 * commas, and semicolons) are escaped in order to simulate what the value
	 * might look like in a plain-text iCalendar object.<br>
	 * <code>["x-foo", {}, "text", "the;value"] --&gt; "the\;value"</code><br>
	 * <code>["x-foo", {}, "text", 2] --&gt; "2"</code>
	 * </p>
	 * <p>
	 * If the jCal value consists of multiple, non-array, non-object values,
	 * then all the values are appended together in a single string, separated
	 * by commas. Special characters (backslashes, commas, and semicolons) are
	 * escaped for each value in order to prevent commas from being treated as
	 * delimiters, and to simulate what the value might look like in a
	 * plain-text iCalendar object.<br>
	 * <code>["x-foo", {}, "text", "one", "two,three"] --&gt;
	 * "one,two\,three"</code>
	 * </p>
	 * <p>
	 * If the jCal value is a single array, then this array is treated as a
	 * "structured value", and converted its plain-text representation. Special
	 * characters (backslashes, commas, and semicolons) are escaped for each
	 * value in order to prevent commas and semicolons from being treated as
	 * delimiters.<br>
	 * <code>["x-foo", {}, "text", ["one", ["two", "three"], "four;five"]]
	 * --&gt; "one;two,three;four\;five"</code>
	 * </p>
	 * <p>
	 * If the jCal value starts with a JSON object, then the object is converted
	 * to a format identical to the one used in the RRULE and EXRULE properties.
	 * Special characters (backslashes, commas, semicolons, and equal signs) are
	 * escaped for each value in order to preserve the syntax of the string
	 * value.<br>
	 * <code>["x-foo", {}, "text", {"one": 1, "two": [2, 2.5]}] --&gt; "ONE=1;TWO=2,2.5"</code>
	 * </p>
	 * <p>
	 * For all other cases, behavior is undefined.
	 * </p>
	 * @param value the property's JSON value
	 * @param dataType the data type
	 * @param parameters the parsed parameters. These parameters will be
	 * assigned to the property object once this method returns. Therefore, do
	 * not assign any parameters to the property object itself whilst inside of
	 * this method, or else they will be overwritten.
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, List<String> warnings) {
		return _parseText(jcalValueToString(value), dataType, parameters, warnings);
	}

	protected String jcalValueToString(JCalValue value) {
		if (value.getValues().size() > 1) {
			List<String> multi = value.asMulti();
			if (!multi.isEmpty()) {
				return StringUtils.join(multi, ",", new JoinCallback<String>() {
					public void handle(StringBuilder sb, String value) {
						sb.append(escape(value));
					}
				});
			}
		}

		if (value.getValues().get(0).getArray() != null) {
			List<String> structured = value.asStructured();
			//TODO properly implement a structured value
			//TODO create helper methods for creating lists, semi-structured, structured values
			if (!structured.isEmpty()) {
				return StringUtils.join(structured, ";", new JoinCallback<String>() {
					public void handle(StringBuilder sb, String value) {
						sb.append(escape(value));
					}
				});
			}
		}

		if (value.getValues().get(0).getObject() != null) {
			ListMultimap<String, String> object = value.asObject();
			if (!object.isEmpty()) {
				return StringUtils.join(object.getMap(), ";", new JoinMapCallback<String, List<String>>() {
					public void handle(StringBuilder sb, String key, List<String> value) {
						sb.append(key).append('=');
						StringUtils.join(value, ",", sb, new JoinCallback<String>() {
							public void handle(StringBuilder sb, String value) {
								if (value == null) {
									return;
								}
								value = escape(value);
								value = value.replace("=", "\\=");
								sb.append(value);
							}
						});
					}
				});
			}
		}

		return escape(value.asSingle());
	}

	/**
	 * Unescapes all special characters that are escaped with a backslash, as
	 * well as escaped newlines.
	 * @param text the text to unescape
	 * @return the unescaped text
	 */
	protected static String unescape(String text) {
		if (text == null) {
			return text;
		}

		StringBuilder sb = null;
		boolean escaped = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);

			if (escaped) {
				if (sb == null) {
					sb = new StringBuilder(text.length());
					sb.append(text.substring(0, i - 1));
				}

				escaped = false;

				if (ch == 'n' || ch == 'N') {
					//newlines appear as "\n" or "\N" (see RFC 5545 p.46)
					sb.append(StringUtils.NEWLINE);
					continue;
				}

				sb.append(ch);
				continue;
			}

			if (ch == '\\') {
				escaped = true;
				continue;
			}

			if (sb != null) {
				sb.append(ch);
			}
		}
		return (sb == null) ? text : sb.toString();
	}

	/**
	 * <p>
	 * Escapes all special characters within a iCalendar value. These characters
	 * are:
	 * </p>
	 * <ul>
	 * <li>backslashes ({@code \})</li>
	 * <li>commas ({@code ,})</li>
	 * <li>semi-colons ({@code ;})</li>
	 * </ul>
	 * <p>
	 * Newlines are not escaped by this method. They are escaped when the
	 * iCalendar object is serialized (in the {@link ICalRawWriter} class).
	 * </p>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	protected static String escape(String text) {
		if (text == null) {
			return text;
		}

		String chars = "\\,;";
		StringBuilder sb = null;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (chars.indexOf(ch) >= 0) {
				if (sb == null) {
					sb = new StringBuilder(text.length());
					sb.append(text.substring(0, i));
				}
				sb.append('\\');
			}

			if (sb != null) {
				sb.append(ch);
			}
		}
		return (sb == null) ? text : sb.toString();
	}

	/**
	 * Splits a string by a delimiter.
	 * @param string the string to split (e.g. "one,two,three")
	 * @param delimiter the delimiter (e.g. ",")
	 * @return the factory object
	 */
	protected static Splitter split(String string, String delimiter) {
		return new Splitter(string, delimiter);
	}

	/**
	 * Factory class for splitting strings.
	 */
	protected static class Splitter {
		private String string;
		private String delimiter;
		private boolean removeEmpties = false;
		private boolean unescape = false;

		/**
		 * Creates a new splitter object.
		 * @param string the string to split (e.g. "one,two,three")
		 * @param delimiter the delimiter (e.g. ",")
		 */
		public Splitter(String string, String delimiter) {
			this.string = string;
			this.delimiter = delimiter;
		}

		/**
		 * Sets whether to remove empty elements.
		 * @param removeEmpties true to remove empty elements, false not to
		 * (default is false)
		 * @return this
		 */
		public Splitter removeEmpties(boolean removeEmpties) {
			this.removeEmpties = removeEmpties;
			return this;
		}

		/**
		 * Sets whether to unescape each split string.
		 * @param unescape true to unescape, false not to (default is false)
		 * @return this
		 */
		public Splitter unescape(boolean unescape) {
			this.unescape = unescape;
			return this;
		}

		/**
		 * Performs the split operation.
		 * @return the split string
		 */
		public List<String> split() {
			//from: http://stackoverflow.com/q/820172">http://stackoverflow.com/q/820172
			String split[] = string.split("\\s*(?<!\\\\)" + Pattern.quote(delimiter) + "\\s*", -1);

			List<String> list = new ArrayList<String>(split.length);
			for (String s : split) {
				if (s.length() == 0 && removeEmpties) {
					continue;
				}

				if (unescape) {
					s = ICalPropertyMarshaller.unescape(s);
				}

				list.add(s);
			}
			return list;
		}
	}

	/**
	 * Parses a comma-separated list of values.
	 * @param str the string to parse (e.g. "one,two,th\,ree")
	 * @return the parsed values
	 */
	protected static List<String> parseList(String str) {
		return split(str, ",").removeEmpties(true).unescape(true).split();
	}

	/**
	 * Parses a component value.
	 * @param str the string to parse (e.g. "one;two,three;four")
	 * @return the parsed values
	 */
	protected static List<List<String>> parseComponent(String str) {
		List<String> split = split(str, ";").split();
		List<List<String>> ret = new ArrayList<List<String>>(split.size());
		for (String s : split) {
			List<String> split2 = parseList(s);
			ret.add(split2);
		}
		return ret;
	}

	/**
	 * Parses a date string.
	 * @param value the date string
	 * @return the factory object
	 */
	protected static DateParser date(String value) {
		return new DateParser(value);
	}

	/**
	 * Formats a {@link Date} object as a string.
	 * @param date the date
	 * @return the factory object
	 */
	protected static DateWriter date(Date date) {
		return new DateWriter(date);
	}

	/**
	 * Factory class for parsing dates.
	 */
	protected static class DateParser {
		private String value;
		private TimeZone timezone;

		/**
		 * Creates a new date writer object.
		 * @param value the date string to parse
		 */
		public DateParser(String value) {
			this.value = value;
		}

		/**
		 * Sets the ID of the timezone to parse the date as (TZID parameter
		 * value). If the ID does not contain a "/" character, it will be
		 * ignored.
		 * @param timezoneId the timezone ID
		 * @return this
		 */
		public DateParser tzid(String timezoneId) {
			return tzid(timezoneId, null);
		}

		/**
		 * Sets the ID of the timezone to parse the date as (TZID parameter
		 * value).
		 * @param timezoneId the timezone ID. If the ID is global (contains a
		 * "/" character), it will attempt to look up the timezone in Java's
		 * timezone registry and parse the date according to that timezone. If
		 * the timezone is not found, the date will be parsed according to the
		 * JVM's default timezone and a warning message will be added to the
		 * provided warnings list. If the ID is not global, it will be parsed
		 * according to the JVM's default timezone. Whichever timezone is chosen
		 * here, it will be ignored if the date string is in UTC time or
		 * contains an offset.
		 * @param warnings if the ID is global and is not recognized, a warning
		 * message will be added to this list
		 * @return this
		 */
		public DateParser tzid(String timezoneId, List<String> warnings) {
			if (timezoneId == null) {
				return tz(null);
			}

			if (timezoneId.contains("/")) {
				TimeZone timezone = ICalDateFormatter.parseTimeZoneId(timezoneId);
				if (timezone == null) {
					timezone = TimeZone.getDefault();
					if (warnings != null) {
						warnings.add("Timezone ID not recognized, parsing with default timezone instead: " + timezoneId);
					}
				}
				return tz(timezone);
			}

			//TODO parse according to the associated VTIMEZONE component
			return tz(TimeZone.getDefault());
		}

		/**
		 * Sets the timezone to parse the date as.
		 * @param timezone the timezone
		 * @return this
		 */
		public DateParser tz(TimeZone timezone) {
			this.timezone = timezone;
			return this;
		}

		/**
		 * Parses the date string.
		 * @return the parsed date
		 * @throws IllegalArgumentException if the date string is invalid
		 */
		public Date parse() {
			return ICalDateFormatter.parse(value, timezone);
		}
	}

	/**
	 * Factory class for writing dates.
	 */
	protected static class DateWriter {
		private Date date;
		private boolean hasTime = true;
		private TimeZone timezone;
		private boolean extended = false;

		/**
		 * Creates a new date writer object.
		 * @param date the date to format
		 */
		public DateWriter(Date date) {
			this.date = date;
		}

		/**
		 * Sets whether to output the date's time component.
		 * @param hasTime true include the time, false if it's strictly a date
		 * (defaults to "true")
		 * @return this
		 */
		public DateWriter time(boolean hasTime) {
			this.hasTime = hasTime;
			return this;
		}

		/**
		 * Sets the ID of the timezone to format the date as (TZID parameter
		 * value).
		 * @param timezoneId the timezone ID. If the ID is global (contains a
		 * "/" character), it will attempt to look up the timezone in Java's
		 * timezone registry and format the date according to that timezone. If
		 * the timezone is not found, the date will be formatted in UTC. If the
		 * ID is not global, it will be formatted according to the JVM's default
		 * timezone. If no timezone preference is specified, the date will be
		 * formatted as UTC.
		 * @return this
		 */
		public DateWriter tzid(String timezoneId) {
			if (timezoneId == null) {
				return tz(null);
			}

			if (timezoneId.contains("/")) {
				return tz(ICalDateFormatter.parseTimeZoneId(timezoneId));
			}

			//TODO format according to the associated VTIMEZONE component
			return tz(TimeZone.getDefault());
		}

		/**
		 * Outputs the date in local time (without a timezone). If no timezone
		 * preference is specified, the date will be formatted as UTC.
		 * @param localTz true to use local time, false not to
		 * @return this
		 */
		public DateWriter localTz(boolean localTz) {
			return localTz ? tz(TimeZone.getDefault()) : this;
		}

		/**
		 * Convenience method that combines {@link #localTz(boolean)} and
		 * {@link #tzid(String)} into one method.
		 * @param localTz true to use local time, false not to
		 * @param timezoneId the timezone ID
		 * @return this
		 */
		public DateWriter tz(boolean localTz, String timezoneId) {
			return localTz ? localTz(true) : tzid(timezoneId);
		}

		/**
		 * Sets the timezone to format the date as. If no timezone preference is
		 * specified, the date will be formatted as UTC.
		 * @param timezone the timezone
		 * @return this
		 */
		public DateWriter tz(TimeZone timezone) {
			this.timezone = timezone;
			return this;
		}

		/**
		 * Sets whether to use extended format or basic.
		 * @param extended true to use extended format, false to use basic
		 * (defaults to "false")
		 * @return this
		 */
		public DateWriter extended(boolean extended) {
			this.extended = extended;
			return this;
		}

		/**
		 * Creates the date string.
		 * @return the date string
		 */
		public String write() {
			ISOFormat format;
			TimeZone timezone = this.timezone;
			if (hasTime) {
				if (timezone == null) {
					format = extended ? ISOFormat.UTC_TIME_EXTENDED : ISOFormat.UTC_TIME_BASIC;
				} else {
					format = extended ? ISOFormat.TIME_EXTENDED_WITHOUT_TZ : ISOFormat.TIME_BASIC_WITHOUT_TZ;
				}
			} else {
				format = extended ? ISOFormat.DATE_EXTENDED : ISOFormat.DATE_BASIC;
				timezone = null;
			}

			return ICalDateFormatter.format(date, format, timezone);
		}
	}

	/**
	 * Creates a {@link CannotParseException}, indicating that the XML elements
	 * that the parser expected to find are missing from the property's XML
	 * element.
	 * @param dataTypes the expected data types (null for "unknown")
	 */
	protected static CannotParseException missingXmlElements(ICalDataType... dataTypes) {
		String[] elements = new String[dataTypes.length];
		for (int i = 0; i < dataTypes.length; i++) {
			ICalDataType dataType = dataTypes[i];
			elements[i] = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();
		}
		return missingXmlElements(elements);
	}

	/**
	 * Creates a {@link CannotParseException}, indicating that the XML elements
	 * that the parser expected to find are missing from property's XML element.
	 * @param elements the names of the expected XML elements.
	 */
	protected static CannotParseException missingXmlElements(String... elements) {
		String message;

		switch (elements.length) {
		case 0:
			message = "Property value empty.";
			break;
		case 1:
			message = "Property value empty (no <" + elements[0] + "> element found).";
			break;
		case 2:
			message = "Property value empty (no <" + elements[0] + "> or <" + elements[1] + "> elements found).";
			break;
		default:
			StringBuilder sb = new StringBuilder();

			sb.append("Property value empty (no ");
			StringUtils.join(Arrays.asList(elements).subList(0, elements.length - 1), ", ", sb, new JoinCallback<String>() {
				public void handle(StringBuilder sb, String value) {
					sb.append('<').append(value).append('>');
				}
			});
			sb.append(", or <").append(elements[elements.length - 1]).append("> elements found).");

			message = sb.toString();
			break;
		}

		return new CannotParseException(message);
	}

	/**
	 * Represents the result of an unmarshal operation.
	 * @author Michael Angstadt
	 * @param <T> the unmarshalled property class
	 */
	public static class Result<T extends ICalProperty> {
		private final T property;
		private final List<String> warnings;

		/**
		 * Creates a new result.
		 * @param property the property object
		 * @param warnings the warnings
		 */
		public Result(T property, List<String> warnings) {
			this.property = property;
			this.warnings = warnings;
		}

		/**
		 * Gets the warnings.
		 * @return the warnings
		 */
		public List<String> getWarnings() {
			return warnings;
		}

		/**
		 * Gets the property object.
		 * @return the property object
		 */
		public T getProperty() {
			return property;
		}
	}
}
