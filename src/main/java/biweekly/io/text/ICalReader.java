package biweekly.io.text;

import static biweekly.util.IOUtils.utf8Reader;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
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
import biweekly.io.ParseWarnings;
import biweekly.io.SkipMeException;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.component.ICalendarScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.ICalPropertyScribe.Result;
import biweekly.io.scribe.property.RawPropertyScribe;
import biweekly.io.scribe.property.RecurrencePropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;

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
 * <p>
 * Parses {@link ICalendar} objects from an iCalendar data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * InputStream in = ...
 * ICalReader icalReader = new ICalReader(in);
 * ICalendar ical;
 * while ((ical = icalReader.readNext()) != null){
 *   ...
 * }
 * icalReader.close();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545">RFC 5545</a>
 */
public class ICalReader implements Closeable {
	private static final ICalendarScribe icalMarshaller = ScribeIndex.getICalendarScribe();
	private static final String icalComponentName = icalMarshaller.getComponentName();
	private final ParseWarnings warnings = new ParseWarnings();
	private ScribeIndex index = new ScribeIndex();
	private final ICalRawReader reader;

	/**
	 * Creates a reader that parses iCalendar objects from a string.
	 * @param string the string
	 */
	public ICalReader(String string) {
		this(new StringReader(string));
	}

	/**
	 * Creates a reader that parses iCalendar objects from an input stream.
	 * @param in the input stream
	 */
	public ICalReader(InputStream in) {
		this(utf8Reader(in));
	}

	/**
	 * Creates a reader that parses iCalendar objects from a file.
	 * @param file the file
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public ICalReader(File file) throws FileNotFoundException {
		this(utf8Reader(file));
	}

	/**
	 * Creates a reader that parses iCalendar objects from a reader.
	 * @param reader the reader
	 */
	public ICalReader(Reader reader) {
		this.reader = new ICalRawReader(reader);
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
	 * newlines and double quotes to be included in parameter values.
	 * @param enable true to use circumflex accent decoding, false not to
	 * @see ICalRawReader#setCaretDecodingEnabled(boolean)
	 */
	public void setCaretDecodingEnabled(boolean enable) {
		reader.setCaretDecodingEnabled(enable);
	}

	/**
	 * <p>
	 * Registers an experimental property scribe. Can also be used to override
	 * the scribe of a standard property (such as DTSTART). Calling this method
	 * is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}.
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(ICalPropertyScribe<? extends ICalProperty> scribe) {
		index.register(scribe);
	}

	/**
	 * <p>
	 * Registers an experimental component scribe. Can also be used to override
	 * the scribe of a standard component (such as VEVENT). Calling this method
	 * is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}.
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(ICalComponentScribe<? extends ICalComponent> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the object that manages the component/property scribes.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the object that manages the component/property scribes.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
	}

	/**
	 * Gets the warnings from the last iCalendar object that was unmarshalled.
	 * This list is reset every time a new iCalendar object is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return warnings.copy();
	}

	/**
	 * Reads the next iCalendar object.
	 * @return the next iCalendar object or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	public ICalendar readNext() throws IOException {
		warnings.clear();

		ICalendar ical = null;
		List<ICalComponent> componentStack = new ArrayList<ICalComponent>();
		List<String> componentNamesStack = new ArrayList<String>();

		while (true) {
			//read next line
			ICalRawLine line;
			try {
				line = reader.readLine();
			} catch (ICalParseException e) {
				warnings.add(reader.getLineNum(), null, 3, e.getLine());
				continue;
			}

			//EOF
			if (line == null) {
				break;
			}

			String propertyName = line.getName();

			if ("BEGIN".equalsIgnoreCase(propertyName)) {
				String componentName = line.getValue();
				if (ical == null && !icalComponentName.equals(componentName)) {
					//keep reading until a VCALENDAR component is found
					continue;
				}

				ICalComponent parentComponent = componentStack.isEmpty() ? null : componentStack.get(componentStack.size() - 1);

				ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(componentName);
				ICalComponent component = scribe.emptyInstance();
				componentStack.add(component);
				componentNamesStack.add(componentName);

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
				if (icalComponentName.equalsIgnoreCase(componentName)) {
					break;
				}

				//find the component that this END property matches up with
				int popIndex = -1;
				for (int i = componentStack.size() - 1; i >= 0; i--) {
					String name = componentNamesStack.get(i);
					if (name.equalsIgnoreCase(componentName)) {
						popIndex = i;
						break;
					}
				}
				if (popIndex == -1) {
					//END property does not match up with any BEGIN properties, so ignore
					warnings.add(reader.getLineNum(), "END", 2);
				} else {
					componentStack.subList(popIndex, componentStack.size()).clear();
					componentNamesStack.subList(popIndex, componentNamesStack.size()).clear();
				}

				continue;
			}

			//check for name-less parameters
			ICalParameters parameters = line.getParameters();
			if (reader.getVersion() != ICalVersion.V1_0) {
				List<String> values = parameters.get(null);
				if (!values.isEmpty()) {
					warnings.add(reader.getLineNum(), propertyName, 4, values);
				}
			}

			ICalPropertyScribe<? extends ICalProperty> scribe = index.getPropertyScribe(propertyName);

			//get the data type
			ICalDataType dataType = parameters.getValue();
			if (dataType == null) {
				//use the default data type if there is no VALUE parameter
				dataType = scribe.defaultDataType(reader.getVersion());
			} else {
				//remove VALUE parameter if it is set
				parameters.setValue(null);
			}

			String value = line.getValue();

			List<Result<? extends ICalProperty>> propertiesToAdd = new ArrayList<Result<? extends ICalProperty>>();
			List<Result<? extends ICalComponent>> componentsToAdd = new ArrayList<Result<? extends ICalComponent>>();

			//handle "difficult" vCal properties
			if (reader.getVersion() == ICalVersion.V1_0) {
				try {
					if (scribe instanceof RecurrencePropertyScribe) {
						propertiesToAdd.addAll(handleVCalRRule(value, scribe, propertyName, dataType, parameters, reader.getVersion()));
					}
				} catch (SkipMeException e) {
					warnings.add(reader.getLineNum(), propertyName, 0, e.getMessage());
					continue;
				} catch (CannotParseException e) {
					warnings.add(reader.getLineNum(), propertyName, 1, value, e.getMessage());

					Result<? extends ICalProperty> result = new RawPropertyScribe(propertyName).parseText(value, dataType, parameters, reader.getVersion());
					propertiesToAdd.add(result);
				}
			}

			if (propertiesToAdd.isEmpty() && componentsToAdd.isEmpty()) {
				try {
					Result<? extends ICalProperty> result = scribe.parseText(value, dataType, parameters, reader.getVersion());
					propertiesToAdd.add(result);
				} catch (SkipMeException e) {
					warnings.add(reader.getLineNum(), propertyName, 0, e.getMessage());
					continue;
				} catch (CannotParseException e) {
					warnings.add(reader.getLineNum(), propertyName, 1, value, e.getMessage());

					Result<? extends ICalProperty> result = new RawPropertyScribe(propertyName).parseText(value, dataType, parameters, reader.getVersion());
					propertiesToAdd.add(result);
				}
			}

			//add the properties/components to the iCalendar object
			ICalComponent parentComponent = componentStack.get(componentStack.size() - 1);
			for (Result<? extends ICalProperty> result : propertiesToAdd) {
				for (Warning warning : result.getWarnings()) {
					warnings.add(reader.getLineNum(), propertyName, warning);
				}

				parentComponent.addProperty(result.getProperty());
			}
			for (Result<? extends ICalComponent> result : componentsToAdd) {
				for (Warning warning : result.getWarnings()) {
					warnings.add(reader.getLineNum(), propertyName, warning);
				}

				parentComponent.addComponent(result.getProperty());
			}
		}

		return ical;
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}

	private List<Result<? extends ICalProperty>> handleVCalRRule(String value, ICalPropertyScribe<? extends ICalProperty> scribe, String propertyName, ICalDataType dataType, ICalParameters parameters, ICalVersion version) {
		Pattern p = Pattern.compile("#\\d+|\\d{8}T\\d{6}Z?");
		Matcher m = p.matcher(value);

		//extract each RRULE from the version string (there can be multiple)
		List<String> subValues = new ArrayList<String>();
		{
			int prevIndex = 0;
			while (m.find()) {
				int end = m.end() + 1;
				String subValue = value.substring(prevIndex, end).trim();
				subValues.add(subValue);
				prevIndex = end;
			}
			String subValue = value.substring(prevIndex).trim();
			if (subValue.length() > 0) {
				subValues.add(subValue);
			}
		}

		List<Result<? extends ICalProperty>> results = new ArrayList<Result<? extends ICalProperty>>();
		for (String subValue : subValues) {
			Result<? extends ICalProperty> result = scribe.parseText(subValue, dataType, parameters, version);
			results.add(result);
		}
		return results;
	}
}
