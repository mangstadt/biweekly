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
import java.util.Map;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.Messages;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.io.CannotParseException;
import biweekly.io.SkipMeException;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.component.ICalendarScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.ICalPropertyScribe.Result;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;

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
	private final List<String> warnings = new ArrayList<String>();
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
		return new ArrayList<String>(warnings);
	}

	/**
	 * Reads the next iCalendar object.
	 * @return the next iCalendar object or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	public ICalendar readNext() throws IOException {
		warnings.clear();

		boolean dataWasRead = false;

		List<ICalProperty> orphanedProperties = new ArrayList<ICalProperty>();
		List<ICalComponent> orphanedComponents = new ArrayList<ICalComponent>();

		List<ICalComponent> componentStack = new ArrayList<ICalComponent>();
		List<String> componentNamesStack = new ArrayList<String>();

		while (true) {
			//read next line
			ICalRawLine line;
			try {
				line = reader.readLine();
			} catch (ICalParseException e) {
				addWarning(null, Warning.parse(3, e.getLine()));
				continue;
			}

			//EOF
			if (line == null) {
				break;
			}

			String propertyName = line.getName();

			if ("BEGIN".equalsIgnoreCase(propertyName)) {
				String componentName = line.getValue();
				dataWasRead = true;

				ICalComponent parentComponent = componentStack.isEmpty() ? null : componentStack.get(componentStack.size() - 1);

				ICalComponentScribe<? extends ICalComponent> marshaller = index.getComponentScribe(componentName);
				ICalComponent component = marshaller.emptyInstance();
				componentStack.add(component);
				componentNamesStack.add(componentName);

				if (parentComponent == null) {
					orphanedComponents.add(component);
				} else {
					parentComponent.addComponent(component);
				}

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
					addWarning("END", Warning.parse(2));
				} else {
					componentStack.subList(popIndex, componentStack.size()).clear();
					componentNamesStack.subList(popIndex, componentNamesStack.size()).clear();
				}

				continue;
			}

			dataWasRead = true;

			//check for value-less parameters
			ICalParameters parameters = line.getParameters();
			for (Map.Entry<String, List<String>> entry : parameters) {
				List<String> paramValues = entry.getValue();
				for (String value : paramValues) {
					if (value == null) {
						String paramName = entry.getKey();
						addWarning(propertyName, Warning.parse(4, paramName));
						break;
					}
				}
			}

			ICalPropertyScribe<? extends ICalProperty> marshaller = index.getPropertyScribe(propertyName);

			//get the data type
			ICalDataType dataType = parameters.getValue();
			if (dataType == null) {
				//use the default data type if there is no VALUE parameter
				dataType = marshaller.getDefaultDataType();
			} else {
				//remove VALUE parameter if it is set
				parameters.setValue(null);
			}

			//marshal the property
			ICalProperty property = null;
			String value = line.getValue();
			try {
				Result<? extends ICalProperty> result = marshaller.parseText(value, dataType, parameters);

				for (Warning warning : result.getWarnings()) {
					addWarning(propertyName, warning);
				}

				property = result.getProperty();
			} catch (SkipMeException e) {
				addWarning(propertyName, Warning.parse(0, e.getMessage()));
			} catch (CannotParseException e) {
				addWarning(propertyName, Warning.parse(1, value, e.getMessage()));
				property = new RawProperty(propertyName, dataType, value);
			}

			//add the property to its component
			if (property != null) {
				if (componentStack.isEmpty()) {
					orphanedProperties.add(property);
				} else {
					ICalComponent parentComponent = componentStack.get(componentStack.size() - 1);
					parentComponent.addProperty(property);
				}
			}
		}

		if (!dataWasRead) {
			//EOF was reached without reading anything
			return null;
		}

		ICalendar ical;
		if (orphanedComponents.isEmpty()) {
			//there were no components in the iCalendar object
			ical = icalMarshaller.emptyInstance();
		} else {
			ICalComponent first = orphanedComponents.get(0);
			if (first instanceof ICalendar) {
				//this is the code-path that valid iCalendar objects should reach
				ical = (ICalendar) first;
			} else {
				ical = icalMarshaller.emptyInstance();
				for (ICalComponent component : orphanedComponents) {
					ical.addComponent(component);
				}
			}
		}

		//add any properties that were not part of a component (will never happen if the iCalendar object is valid)
		for (ICalProperty property : orphanedProperties) {
			ical.addProperty(property);
		}

		return ical;
	}

	private void addWarning(String propertyName, Warning warning) {
		String key = (propertyName == null) ? "parse.line" : "parse.lineWithProp";
		int line = reader.getLineNum();

		String message = Messages.INSTANCE.getMessage(key, line, warning, propertyName);
		warnings.add(message);
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
