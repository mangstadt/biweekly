package biweekly.io.text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ComponentLibrary;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.component.marshaller.RawComponentMarshaller;
import biweekly.io.CannotParseException;
import biweekly.io.IParser;
import biweekly.io.SkipMeException;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
import biweekly.property.marshaller.PropertyLibrary;
import biweekly.property.marshaller.RawPropertyMarshaller;

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
 * 
 * <pre>
 * Reader reader = ...
 * ICalReader icalReader = new ICalReader(reader);
 * ICalendar ical;
 * while ((ical = icalReader.readNext()) != null){
 *   ...
 * }
 * icalReader.close();
 * </pre>
 * @author Michael Angstadt
 */
public class ICalReader implements IParser {
	private final List<String> warnings = new ArrayList<String>();
	private final Map<String, ICalPropertyMarshaller<? extends ICalProperty>> propertyMarshallers = new HashMap<String, ICalPropertyMarshaller<? extends ICalProperty>>(0);
	private final Map<String, ICalComponentMarshaller<? extends ICalComponent>> componentMarshallers = new HashMap<String, ICalComponentMarshaller<? extends ICalComponent>>(0);
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
		this(new InputStreamReader(in));
	}

	/**
	 * Creates a reader that parses iCalendar objects from a file.
	 * @param file the file
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public ICalReader(File file) throws FileNotFoundException {
		this(new FileReader(file));
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

	//@Override
	public void registerMarshaller(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
		propertyMarshallers.put(marshaller.getPropertyName().toUpperCase(), marshaller);
	}

	//@Override
	public void registerMarshaller(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
		componentMarshallers.put(marshaller.getComponentName().toUpperCase(), marshaller);
	}

	//@Override
	public List<String> getWarnings() {
		return new ArrayList<String>(warnings);
	}

	//@Override
	public ICalendar readNext() throws IOException {
		if (reader.eof()) {
			return null;
		}

		warnings.clear();

		ICalDataStreamListenerImpl listener = new ICalDataStreamListenerImpl();
		reader.start(listener);

		if (!listener.dataWasRead) {
			//EOF was reached without reading anything
			return null;
		}

		ICalendar ical;
		if (listener.orphanedComponents.isEmpty()) {
			//there were no components in the iCalendar object
			ical = new ICalendar();
			ical.getProperties().clear(); //clear properties that are created in the constructor
		} else {
			ICalComponent first = listener.orphanedComponents.get(0);
			if (first instanceof ICalendar) {
				//this is the code-path that valid iCalendar objects should reach
				ical = (ICalendar) first;
			} else {
				ical = new ICalendar();
				ical.getProperties().clear(); //clear properties that are created in the constructor
				for (ICalComponent component : listener.orphanedComponents) {
					ical.addComponent(component);
				}
			}
		}

		//add any properties that were not part of a component (will never happen if the iCalendar object is valid)
		for (ICalProperty property : listener.orphanedProperties) {
			ical.addProperty(property);
		}

		return ical;
	}

	/**
	 * Finds a component marshaller.
	 * @param componentName the name of the component
	 * @return the component marshallerd
	 */
	private ICalComponentMarshaller<? extends ICalComponent> findComponentMarshaller(String componentName) {
		ICalComponentMarshaller<? extends ICalComponent> m = componentMarshallers.get(componentName.toUpperCase());
		if (m == null) {
			m = ComponentLibrary.getMarshaller(componentName);
			if (m == null) {
				m = new RawComponentMarshaller(componentName);
			}
		}
		return m;
	}

	/**
	 * Finds a property marshaller.
	 * @param propertyName the name of the property
	 * @return the property marshaller
	 */
	private ICalPropertyMarshaller<? extends ICalProperty> findPropertyMarshaller(String propertyName) {
		ICalPropertyMarshaller<? extends ICalProperty> m = propertyMarshallers.get(propertyName);
		if (m == null) {
			m = PropertyLibrary.getMarshaller(propertyName);
			if (m == null) {
				m = new RawPropertyMarshaller(propertyName);
			}
		}
		return m;
	}

	//TODO how to unmarshal the alarm components (a different class should be created, depending on the ACTION property)
	//TODO buffer properties in a list before the component class is created
	private class ICalDataStreamListenerImpl implements ICalRawReader.ICalDataStreamListener {
		private final String icalComponentName = ComponentLibrary.getMarshaller(ICalendar.class).getComponentName();

		private List<ICalProperty> orphanedProperties = new ArrayList<ICalProperty>();
		private List<ICalComponent> orphanedComponents = new ArrayList<ICalComponent>();

		private List<ICalComponent> componentStack = new ArrayList<ICalComponent>();
		private List<String> componentNamesStack = new ArrayList<String>();
		private boolean dataWasRead = false;

		public void beginComponent(String name) {
			dataWasRead = true;

			ICalComponent parentComponent = getCurrentComponent();

			ICalComponentMarshaller<? extends ICalComponent> m = findComponentMarshaller(name);
			ICalComponent component = m.newInstance();
			component.getProperties().clear(); //clear properties that were created in the constructor
			componentStack.add(component);
			componentNamesStack.add(name);

			if (parentComponent == null) {
				orphanedComponents.add(component);
			} else {
				parentComponent.addComponent(component);
			}
		}

		public void readProperty(String name, ICalParameters parameters, String value) {
			dataWasRead = true;

			ICalPropertyMarshaller<? extends ICalProperty> m = findPropertyMarshaller(name);
			ICalProperty property = null;
			try {
				Result<? extends ICalProperty> result = m.parseText(value, parameters);

				for (String warning : result.getWarnings()) {
					addWarning(warning, name);
				}

				property = result.getValue();
			} catch (SkipMeException e) {
				if (e.getMessage() == null) {
					addWarning("Property has requested that it be skipped.", name);
				} else {
					addWarning("Property has requested that it be skipped: " + e.getMessage(), name);
				}
			} catch (CannotParseException e) {
				if (e.getMessage() == null) {
					addWarning("Property value could not be unmarshalled: " + value, name);
				} else {
					addWarning("Property value could not be unmarshalled.\n  Value: " + value + "\n  Reason: " + e.getMessage(), name);
				}
				property = new RawProperty(name, value);
			}

			if (property != null) {
				ICalComponent parentComponent = getCurrentComponent();
				if (parentComponent == null) {
					orphanedProperties.add(property);
				} else {
					parentComponent.addProperty(property);
				}
			}
		}

		public void endComponent(String name) {
			//stop reading when "END:VCALENDAR" is reached
			if (icalComponentName.equalsIgnoreCase(name)) {
				throw new ICalRawReader.StopReadingException();
			}

			//find the component that this END property matches up with
			int popIndex = -1;
			for (int i = componentStack.size() - 1; i >= 0; i--) {
				String n = componentNamesStack.get(i);
				if (n.equalsIgnoreCase(name)) {
					popIndex = i;
					break;
				}
			}
			if (popIndex == -1) {
				//END property does not match up with any BEGIN properties, so ignore
				addWarning("Ignoring END property that does not match up with any BEGIN properties: " + name, "END");
				return;
			}

			componentStack = componentStack.subList(0, popIndex);
			componentNamesStack = componentNamesStack.subList(0, popIndex);
		}

		public void invalidLine(String line) {
			addWarning("Skipping malformed line: \"" + line + "\"");
		}

		private ICalComponent getCurrentComponent() {
			if (componentStack.isEmpty()) {
				return null;
			}
			return componentStack.get(componentStack.size() - 1);
		}
	}

	private void addWarning(String message) {
		addWarning(message, null);
	}

	private void addWarning(String message, String propertyName) {
		addWarning(message, propertyName, reader.getLineNum());
	}

	private void addWarning(String message, String propertyName, int lineNum) {
		StringBuilder sb = new StringBuilder();
		sb.append("Line ").append(lineNum);
		if (propertyName != null) {
			sb.append(" (").append(propertyName).append(" property)");
		}
		sb.append(": ").append(message);

		warnings.add(sb.toString());
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
