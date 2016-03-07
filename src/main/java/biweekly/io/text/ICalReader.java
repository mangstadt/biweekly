package biweekly.io.text;

import static biweekly.io.DataModelConverter.convert;
import static biweekly.util.IOUtils.utf8Reader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.io.CannotParseException;
import biweekly.io.SkipMeException;
import biweekly.io.StreamReader;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.ICalPropertyScribe.Result;
import biweekly.io.scribe.property.RawPropertyScribe;
import biweekly.io.scribe.property.RecurrencePropertyScribe;
import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.AudioAlarm;
import biweekly.property.DisplayAlarm;
import biweekly.property.EmailAlarm;
import biweekly.property.ICalProperty;
import biweekly.property.ProcedureAlarm;
import biweekly.util.org.apache.commons.codec.DecoderException;
import biweekly.util.org.apache.commons.codec.net.QuotedPrintableCodec;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
 * <p>
 * Parses {@link ICalendar} objects from a plain-text iCalendar data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * File file = new File("icals.ics");
 * ICalReader reader = null;
 * try {
 *   reader = new ICalReader(file);
 *   ICalendar ical;
 *   while ((ical = reader.readNext()) != null){
 *     ...
 *   }
 * } finally {
 *   if (reader != null) reader.close();
 * }
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Getting timezone information:</b>
 * 
 * <pre class="brush:java">
 * ICalReader reader = ...
 * ICalendar ical = reader.readNext();
 * TimezoneInfo tzinfo = reader.getTimezoneInfo();
 * 
 * //get the VTIMEZONE components that were parsed
 * //the VTIMEZONE components will NOT be in the ICalendar object
 * Collection&ltVTimezone&gt; vtimezones = tzinfo.getComponents();
 * 
 * //get the timezone that a property was originally formatted in
 * DateStart dtstart = ical.getEvents().get(0).getDateStart();
 * TimeZone tz = tzinfo.getTimeZone(dtstart);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/pdiproddev.html">1.0 specs</a>
 * @see <a href="https://tools.ietf.org/html/rfc2445">RFC 2445</a>
 * @see <a href="http://tools.ietf.org/html/rfc5545">RFC 5545</a>
 */
public class ICalReader extends StreamReader {
	private static final String ICAL_COMPONENT_NAME = ScribeIndex.getICalendarScribe().getComponentName();

	private final ICalRawReader reader;
	private Charset defaultQuotedPrintableCharset;

	/**
	 * @param str the string to read from
	 */
	public ICalReader(String str) {
		this(new StringReader(str));
	}

	/**
	 * @param in the input stream to read from
	 */
	public ICalReader(InputStream in) {
		this(utf8Reader(in));
	}

	/**
	 * @param file the file to read from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public ICalReader(File file) throws FileNotFoundException {
		this(new BufferedReader(utf8Reader(file)));
	}

	/**
	 * @param reader the reader to read from
	 */
	public ICalReader(Reader reader) {
		this.reader = new ICalRawReader(reader);
		defaultQuotedPrintableCharset = this.reader.getEncoding();
		if (defaultQuotedPrintableCharset == null) {
			defaultQuotedPrintableCharset = Charset.defaultCharset();
		}
	}

	/**
	 * Gets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * @return true if circumflex accent decoding is enabled, false if not
	 * @see ICalRawReader#isCaretDecodingEnabled()
	 */
	public boolean isCaretDecodingEnabled() {
		return reader.isCaretDecodingEnabled();
	}

	/**
	 * Sets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values. This only
	 * applies to version 2.0 iCalendar objects.
	 * @param enable true to use circumflex accent decoding, false not to
	 * @see ICalRawReader#setCaretDecodingEnabled(boolean)
	 */
	public void setCaretDecodingEnabled(boolean enable) {
		reader.setCaretDecodingEnabled(enable);
	}

	/**
	 * <p>
	 * Gets the character set to use when decoding quoted-printable values if
	 * the property has no CHARSET parameter, or if the CHARSET parameter is not
	 * a valid character set.
	 * </p>
	 * <p>
	 * By default, the Reader's character encoding will be used. If the Reader
	 * has no character encoding, then the system's default character encoding
	 * will be used.
	 * </p>
	 * @return the character set
	 */
	public Charset getDefaultQuotedPrintableCharset() {
		return defaultQuotedPrintableCharset;
	}

	/**
	 * <p>
	 * Sets the character set to use when decoding quoted-printable values if
	 * the property has no CHARSET parameter, or if the CHARSET parameter is not
	 * a valid character set.
	 * </p>
	 * <p>
	 * By default, the Reader's character encoding will be used. If the Reader
	 * has no character encoding, then the system's default character encoding
	 * will be used.
	 * </p>
	 * @param charset the character set
	 */
	public void setDefaultQuotedPrintableCharset(Charset charset) {
		defaultQuotedPrintableCharset = charset;
	}

	@Override
	protected ICalendar _readNext() throws IOException {
		ICalendar ical = null;
		List<String> values = new ArrayList<String>();
		ComponentStack stack = new ComponentStack();

		while (true) {
			//read next line
			ICalRawLine line;
			try {
				line = reader.readLine();
			} catch (ICalParseException e) {
				warnings.add(e.getLineNumber(), null, 3, e.getMessage(), e.getLine());
				continue;
			}

			//EOF
			if (line == null) {
				break;
			}

			context.setVersion(reader.getVersion());
			String propertyName = line.getName();

			if ("BEGIN".equalsIgnoreCase(propertyName)) {
				String componentName = line.getValue();
				if (ical == null && !ICAL_COMPONENT_NAME.equalsIgnoreCase(componentName)) {
					//keep reading until a VCALENDAR component is found
					continue;
				}

				ICalComponent parentComponent = stack.peek();

				ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(componentName, reader.getVersion());
				ICalComponent component = scribe.emptyInstance();
				stack.push(component, componentName);

				if (parentComponent == null) {
					ical = (ICalendar) component;
				} else {
					parentComponent.addComponent(component);
				}

				continue;
			}

			if (ical == null) {
				//VCALENDAR component hasn't been found yet
				continue;
			}

			if ("END".equalsIgnoreCase(propertyName)) {
				String componentName = line.getValue();

				//stop reading when "END:VCALENDAR" is reached
				if (ICAL_COMPONENT_NAME.equalsIgnoreCase(componentName)) {
					break;
				}

				//find the component that this END property matches up with
				boolean found = stack.popThrough(componentName);
				if (!found) {
					//END property does not match up with any BEGIN properties, so ignore
					warnings.add(reader.getLineNumber(), "END", 2);
				}

				continue;
			}

			ICalParameters parameters = line.getParameters();
			String value = line.getValue();

			ICalPropertyScribe<? extends ICalProperty> scribe = index.getPropertyScribe(propertyName, reader.getVersion());

			//process nameless parameters
			processNamelessParameters(parameters, propertyName);

			//decode property value from quoted-printable
			if (parameters.getEncoding() == Encoding.QUOTED_PRINTABLE) {
				try {
					value = decodeQuotedPrintableValue(propertyName, parameters.getCharset(), value);
				} catch (DecoderException e) {
					warnings.add(reader.getLineNumber(), propertyName, 31, e.getMessage());
				}
				parameters.setEncoding(null);
			}

			//get the data type (VALUE parameter)
			ICalDataType dataType = parameters.getValue();
			if (dataType == null) {
				//use the default data type if there is no VALUE parameter
				dataType = scribe.defaultDataType(reader.getVersion());
			} else {
				//remove VALUE parameter if it is set
				parameters.setValue(null);
			}

			//determine how many properties should be parsed from this property value
			values.clear();
			if (reader.getVersion() == ICalVersion.V1_0 && scribe instanceof RecurrencePropertyScribe) {
				//extract each RRULE from the value string (there can be multiple)
				Pattern p = Pattern.compile("#\\d+|\\d{8}T\\d{6}Z?");
				Matcher m = p.matcher(value);

				int prevIndex = 0;
				while (m.find()) {
					int end = m.end() + 1;
					String subValue = value.substring(prevIndex, end).trim();
					values.add(subValue);
					prevIndex = end;
				}
				String subValue = value.substring(prevIndex).trim();
				if (subValue.length() > 0) {
					values.add(subValue);
				}
			} else {
				values.add(value);
			}

			context.getWarnings().clear();
			List<ICalProperty> propertiesToAdd = new ArrayList<ICalProperty>();
			List<Result<? extends ICalComponent>> componentsToAdd = new ArrayList<Result<? extends ICalComponent>>();
			for (String v : values) {
				try {
					ICalProperty property = scribe.parseText(v, dataType, parameters, context);
					propertiesToAdd.add(property);
				} catch (SkipMeException e) {
					warnings.add(reader.getLineNumber(), propertyName, 0, e.getMessage());
					continue;
				} catch (CannotParseException e) {
					warnings.add(reader.getLineNumber(), propertyName, 1, v, e.getMessage());

					ICalProperty property = new RawPropertyScribe(propertyName).parseText(v, dataType, parameters, context);
					propertiesToAdd.add(property);
				}
			}

			//add the properties to the iCalendar object
			ICalComponent parentComponent = stack.peek();
			boolean isVCal = reader.getVersion() == null || reader.getVersion() == ICalVersion.V1_0;
			for (ICalProperty property : propertiesToAdd) {
				for (Warning warning : context.getWarnings()) {
					warnings.add(reader.getLineNumber(), propertyName, warning);
				}

				if (isVCal) {
					Object obj = convertVCalProperty(property);
					if (obj instanceof ICalComponent) {
						parentComponent.addComponent((ICalComponent) obj);
						continue;
					}
					if (obj instanceof ICalProperty) {
						property = (ICalProperty) obj;
					}
				}

				parentComponent.addProperty(property);
			}

			//add the components to the iCalendar object
			for (Result<? extends ICalComponent> result : componentsToAdd) {
				for (Warning warning : result.getWarnings()) {
					warnings.add(reader.getLineNumber(), propertyName, warning);
				}

				parentComponent.addComponent(result.getProperty());
			}
		}

		return ical;
	}

	/**
	 * Assigns names to all nameless parameters. v2.0 requires all parameters to
	 * have names, but v1.0 does not.
	 * @param parameters the parameters
	 * @param propertyName the property name
	 */
	private void processNamelessParameters(ICalParameters parameters, String propertyName) {
		List<String> namelessParamValues = parameters.removeAll(null);
		if (namelessParamValues.isEmpty()) {
			return;
		}

		if (reader.getVersion() != ICalVersion.V1_0) {
			warnings.add(reader.getLineNumber(), propertyName, 4, namelessParamValues);
		}

		for (String paramValue : namelessParamValues) {
			String paramName = guessParameterName(paramValue);
			parameters.put(paramName, paramValue);
		}
	}

	/**
	 * Makes a guess as to what a parameter value's name should be.
	 * @param value the parameter value
	 * @return the guessed name
	 */
	private String guessParameterName(String value) {
		if (ICalDataType.find(value) != null) {
			return ICalParameters.VALUE;
		}

		if (Encoding.find(value) != null) {
			return ICalParameters.ENCODING;
		}

		//otherwise, assume it's a TYPE
		return ICalParameters.TYPE;
	}

	/**
	 * Decodes the property value if it's encoded in quoted-printable encoding.
	 * Quoted-printable encoding is only supported in v1.0.
	 * @param propertyName the property name
	 * @param charsetParam the value of the CHARSET parameter
	 * @param value the property value
	 * @return the decoded property value
	 * @throws DecoderException if the value couldn't be decoded
	 */
	private String decodeQuotedPrintableValue(String propertyName, String charsetParam, String value) throws DecoderException {
		//determine the character set
		Charset charset;
		if (charsetParam == null) {
			charset = defaultQuotedPrintableCharset;
		} else {
			try {
				charset = Charset.forName(charsetParam);
			} catch (Throwable t) {
				charset = defaultQuotedPrintableCharset;

				//the given charset was invalid, so add a warning
				warnings.add(reader.getLineNumber(), propertyName, 32, charsetParam, charset.name());
			}
		}

		QuotedPrintableCodec codec = new QuotedPrintableCodec(charset.name());
		return codec.decode(value);
	}

	/**
	 * Converts a vCal property to the iCalendar data model.
	 * @param property the vCal property
	 * @return the converted iCalendar property/component, or the same property
	 * that was passed in if no conversion was necessary
	 */
	private Object convertVCalProperty(ICalProperty property) {
		//ATTENDEE with "organizer" role => ORGANIZER property
		if (property instanceof Attendee) {
			Attendee attendee = (Attendee) property;
			return (attendee.getRole() == Role.ORGANIZER) ? convert(attendee) : property;
		}

		//AALARM property => VALARM component
		if (property instanceof AudioAlarm) {
			AudioAlarm aalarm = (AudioAlarm) property;
			return convert(aalarm);
		}

		//DALARM property => VALARM component
		if (property instanceof DisplayAlarm) {
			DisplayAlarm dalarm = (DisplayAlarm) property;
			return convert(dalarm);
		}

		//MALARM property => VALARM component
		if (property instanceof EmailAlarm) {
			EmailAlarm malarm = (EmailAlarm) property;
			return convert(malarm);
		}

		//PALARM property => VALARM component
		if (property instanceof ProcedureAlarm) {
			ProcedureAlarm palarm = (ProcedureAlarm) property;
			return convert(palarm);
		}

		return property;
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}

	private static class ComponentStack {
		private final List<ICalComponent> components = new ArrayList<ICalComponent>();
		private final List<String> names = new ArrayList<String>();

		/**
		 * Gets the component on the top of the stack.
		 * @return the component or null if the stack is empty
		 */
		public ICalComponent peek() {
			return components.isEmpty() ? null : components.get(components.size() - 1);
		}

		/**
		 * Adds a component to the stack
		 * @param component the component
		 * @param name the component's name (e.g. "VEVENT")
		 */
		public void push(ICalComponent component, String name) {
			components.add(component);
			names.add(name);
		}

		/**
		 * Removes all components that come after the given component, including
		 * the given component itself.
		 * @param name the component's name (e.g. "VEVENT")
		 * @return true if the component was found, false if not
		 */
		public boolean popThrough(String name) {
			for (int i = components.size() - 1; i >= 0; i--) {
				String curName = names.get(i);
				if (curName.equalsIgnoreCase(name)) {
					components.subList(i, components.size()).clear();
					names.subList(i, names.size()).clear();
					return true;
				}
			}

			return false;
		}
	}
}
