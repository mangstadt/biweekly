package biweekly.property.marshaller;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import biweekly.ICalendar;
import biweekly.io.CannotParseException;
import biweekly.io.SkipMeException;
import biweekly.io.text.ICalWriter;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.ICalProperty;
import biweekly.util.ICalDateFormatter;
import biweekly.util.ISOFormat;

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
 * @author Michael Angstadt
 */
public abstract class ICalPropertyMarshaller<T extends ICalProperty> {
	private static final String NEWLINE = System.getProperty("line.separator");
	protected final Class<T> clazz;
	protected final String propertyName;
	protected final QName qname;

	/**
	 * Creates a new marshaller.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "VERSION")
	 */
	public ICalPropertyMarshaller(Class<T> clazz, String propertyName) {
		this(clazz, propertyName, new QName(XCAL_NS, propertyName.toLowerCase()));
	}

	/**
	 * Creates a new marshaller.
	 * @param clazz the property class
	 * @param propertyName the property name (e.g. "VERSION")
	 * @param qname the XML element name and namespace (used for xCal documents)
	 */
	public ICalPropertyMarshaller(Class<T> clazz, String propertyName, QName qname) {
		this.clazz = clazz;
		this.propertyName = propertyName;
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
	 * Unmarshals a property's value.
	 * @param value the value
	 * @param parameters the property's parameters
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	public final Result<T> parseText(String value, ICalParameters parameters) {
		List<String> warnings = new ArrayList<String>(0);
		T property = _parseText(value, parameters, warnings);
		property.setParameters(parameters);
		return new Result<T>(property, warnings);
	}

	/**
	 * Unmarshals a property's value from an XML document (xCal).
	 * @param element the property's XML element
	 * @param parameters the property's parameters
	 * @return the unmarshalled property object
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
	 * Sanitizes a property's parameters (called before the property is
	 * written). This should be overridden by child classes when required.
	 * @param property the property
	 * @param copy the list of parameters to make modifications to (it is a copy
	 * of the property's parameters)
	 */
	protected void _prepareParameters(T property, ICalParameters copy) {
		//do nothing
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
	 * Marshals a property's value to an XML element (xCal).
	 * @param property the property
	 * @param element the XML element
	 * @throws SkipMeException if the property should not be written to the data
	 * stream
	 */
	protected void _writeXml(T property, XCalElement element) {
		String value = writeText(property);
		Value dataType = property.getParameters().getValue();
		if (dataType == null) {
			element.appendValueUnknown(value);
		} else {
			element.appendValue(dataType, value);
		}
	}

	/**
	 * Unmarshals a property's value.
	 * @param value the value
	 * @param parameters the property's parameters
	 * @param warnings allows the programmer to alert the user to any
	 * note-worthy (but non-critical) issues that occurred during the
	 * unmarshalling process
	 * @return the unmarshalled property object
	 * @throws CannotParseException if the marshaller could not parse the
	 * property's value
	 * @throws SkipMeException if the property should not be added to the final
	 * {@link ICalendar} object
	 */
	protected abstract T _parseText(String value, ICalParameters parameters, List<String> warnings);

	/**
	 * Unmarshals a property's value from an XML document (xCal).
	 * @param element the property's XML element
	 * @param parameters the property's parameters
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
		throw new UnsupportedOperationException();
	}

	/**
	 * Unescapes all special characters that are escaped with a backslash, as
	 * well as escaped newlines.
	 * @param text the text to unescape
	 * @return the unescaped text
	 */
	protected static String unescape(String text) {
		StringBuilder sb = new StringBuilder(text.length());
		boolean escaped = false;
		for (int i = 0; i < text.length(); i++) {
			char ch = text.charAt(i);
			if (escaped) {
				if (ch == 'n' || ch == 'N') {
					//newlines appear as "\n" or "\N" (see RFC 2426 p.7)
					sb.append(NEWLINE);
				} else {
					sb.append(ch);
				}
				escaped = false;
			} else if (ch == '\\') {
				escaped = true;
			} else {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * Escapes all special characters within a iCalendar value.
	 * <p>
	 * These characters are:
	 * </p>
	 * <ul>
	 * <li>backslashes (<code>\</code>)</li>
	 * <li>commas (<code>,</code>)</li>
	 * <li>semi-colons (<code>;</code>)</li>
	 * <li>(newlines are escaped by {@link ICalWriter})</li>
	 * </ul>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	protected static String escape(String text) {
		String chars = "\\,;";
		for (int i = 0; i < chars.length(); i++) {
			String ch = chars.substring(i, i + 1);
			text = text.replace(ch, "\\" + ch);
		}
		return text;
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
		public String[] split() {
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

			return list.toArray(new String[0]);
		}
	}

	/**
	 * Parses a comma-separated list of values.
	 * @param str the string to parse (e.g. "one,two,th\,ree")
	 * @return the parsed values
	 */
	protected static String[] parseList(String str) {
		return split(str, ",").removeEmpties(true).unescape(true).split();
	}

	/**
	 * Parses a component value.
	 * @param str the string to parse (e.g. "one;two,three;four")
	 * @return the parsed values
	 */
	protected static String[][] parseComponent(String str) {
		String split[] = split(str, ";").split();
		String ret[][] = new String[split.length][];
		int i = 0;
		for (String s : split) {
			String split2[] = parseList(s);
			ret[i++] = split2;
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
		 * value). If the ID does not contain a "/" character, it will be
		 * ignored. If the ID is invalid, the date will be formatted according
		 * to the JVM's default timezone and a warning message will be added to
		 * the provided warnings list.
		 * @param timezoneId the timezone ID
		 * @param warnings if the ID is not recognized, a warning message will
		 * be added to this list
		 * @return this
		 */
		public DateParser tzid(String timezoneId, List<String> warnings) {
			if (timezoneId == null) {
				timezone = null;
				return this;
			}

			if (timezoneId.contains("/")) {
				timezone = ICalDateFormatter.parseTimeZoneId(timezoneId);
				if (timezone == null) {
					timezone = TimeZone.getDefault();
					if (warnings != null) {
						warnings.add("Timezone ID not recognized, parsing with default timezone instead: " + timezoneId);
					}
				}
			} else {
				//TODO support VTIMEZONE
			}
			return this;
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
		 * (default is "true")
		 * @return this
		 */
		public DateWriter time(boolean hasTime) {
			this.hasTime = hasTime;
			return this;
		}

		/**
		 * Sets the ID of the timezone to format the date as (TZID parameter
		 * value). If the ID does not contain a "/" character, it will be
		 * ignored. If the ID is invalid, the date will be formatted according
		 * to the JVM's default timezone. If no timezone is specified, the date
		 * will be formatted as UTC.
		 * @param timezoneId the timezone ID
		 * @return this
		 */
		public DateWriter tzid(String timezoneId) {
			if (timezoneId == null) {
				timezone = null;
				return this;
			}

			if (timezoneId.contains("/")) {
				timezone = ICalDateFormatter.parseTimeZoneId(timezoneId);
			} else {
				//TODO support VTIMEZONE
				timezone = TimeZone.getDefault();
			}
			return this;
		}

		/**
		 * Sets the timezone to format the date as. If no timezone is specified,
		 * the date will be formatted as UTC.
		 * @param timezone the timezone
		 * @return this
		 */
		public DateWriter tz(TimeZone timezone) {
			this.timezone = timezone;
			return this;
		}

		/**
		 * Sets whether to use extended format or basic.
		 * @param extended true to use extended format, false to use basic.
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
			if (hasTime) {
				if (timezone == null) {
					format = extended ? ISOFormat.UTC_TIME_EXTENDED : ISOFormat.UTC_TIME_BASIC;
				} else {
					format = extended ? ISOFormat.TIME_EXTENDED_WITHOUT_TZ : ISOFormat.TIME_BASIC_WITHOUT_TZ;
				}
			} else {
				format = extended ? ISOFormat.DATE_EXTENDED : ISOFormat.DATE_BASIC;
			}

			return ICalDateFormatter.format(date, format, timezone);
		}
	}

	/**
	 * Represents the result of a marshal or unmarshal operation.
	 * @author Michael Angstadt
	 * @param <T> the marshalled/unmarshalled value (e.g. "String" if a property
	 * was marshalled)
	 */
	public static class Result<T> {
		private final T value;
		private final List<String> warnings;

		/**
		 * Creates a new result.
		 * @param value the value
		 * @param warnings the warnings
		 */
		public Result(T value, List<String> warnings) {
			this.value = value;
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
		 * Gets the value.
		 * @return the value
		 */
		public T getValue() {
			return value;
		}
	}
}
