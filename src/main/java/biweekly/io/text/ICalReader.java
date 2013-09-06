package biweekly.io.text;

import static biweekly.util.IOUtils.utf8Reader;
import static biweekly.util.StringUtils.NEWLINE;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.component.marshaller.ICalendarMarshaller;
import biweekly.io.CannotParseException;
import biweekly.io.ICalMarshallerRegistrar;
import biweekly.io.SkipMeException;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;

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
 * <pre>
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
	private static final ICalendarMarshaller icalMarshaller = ICalMarshallerRegistrar.getICalendarMarshaller();
	private final List<String> warnings = new ArrayList<String>();
	private ICalMarshallerRegistrar registrar = new ICalMarshallerRegistrar();
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
	 * Registers an experimental property marshaller. Can also be used to
	 * override the marshaller of a standard property (such as DTSTART). Calling
	 * this method is the same as calling:
	 * </p>
	 * <p>
	 * <code>getRegistrar().register(marshaller)</code>.
	 * </p>
	 * @param marshaller the marshaller to register
	 */
	public void registerMarshaller(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
		registrar.register(marshaller);
	}

	/**
	 * <p>
	 * Registers an experimental component marshaller. Can also be used to
	 * override the marshaller of a standard component (such as VEVENT). Calling
	 * this method is the same as calling:
	 * </p>
	 * <p>
	 * <code>getRegistrar().register(marshaller)</code>.
	 * </p>
	 * @param marshaller the marshaller to register
	 */
	public void registerMarshaller(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
		registrar.register(marshaller);
	}

	/**
	 * Gets the object that manages the component/property marshaller objects.
	 * @return the marshaller registrar
	 */
	public ICalMarshallerRegistrar getRegistrar() {
		return registrar;
	}

	/**
	 * Sets the object that manages the component/property marshaller objects.
	 * @param registrar the marshaller registrar
	 */
	public void setRegistrar(ICalMarshallerRegistrar registrar) {
		this.registrar = registrar;
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
			ical = icalMarshaller.emptyInstance();
		} else {
			ICalComponent first = listener.orphanedComponents.get(0);
			if (first instanceof ICalendar) {
				//this is the code-path that valid iCalendar objects should reach
				ical = (ICalendar) first;
			} else {
				ical = icalMarshaller.emptyInstance();
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

	//TODO how to unmarshal the alarm components (a different class should be created, depending on the ACTION property)
	//TODO buffer properties in a list before the component class is created
	private class ICalDataStreamListenerImpl implements ICalRawReader.ICalDataStreamListener {
		private final String icalComponentName = icalMarshaller.getComponentName();

		private List<ICalProperty> orphanedProperties = new ArrayList<ICalProperty>();
		private List<ICalComponent> orphanedComponents = new ArrayList<ICalComponent>();

		private List<ICalComponent> componentStack = new ArrayList<ICalComponent>();
		private List<String> componentNamesStack = new ArrayList<String>();
		private boolean dataWasRead = false;

		public void beginComponent(String name) {
			dataWasRead = true;

			ICalComponent parentComponent = getCurrentComponent();

			ICalComponentMarshaller<? extends ICalComponent> m = registrar.getComponentMarshaller(name);
			ICalComponent component = m.emptyInstance();
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

			ICalPropertyMarshaller<? extends ICalProperty> m = registrar.getPropertyMarshaller(name);

			//get the data type
			ICalDataType dataType = parameters.getValue();
			if (dataType == null) {
				//use the default data type if there is no VALUE parameter
				dataType = m.getDefaultDataType();
			} else {
				//remove VALUE parameter if it is set
				parameters.setValue(null);
			}

			ICalProperty property = null;
			try {
				Result<? extends ICalProperty> result = m.parseText(value, dataType, parameters);

				for (String warning : result.getWarnings()) {
					addWarning(warning, name);
				}

				property = result.getProperty();
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
					addWarning("Property value could not be unmarshalled." + NEWLINE + "  Value: " + value + NEWLINE + "  Reason: " + e.getMessage(), name);
				}
				property = new RawProperty(name, dataType, value);
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

		public void valuelessParameter(String propertyName, String parameterName) {
			addWarning("Value-less parameter encountered: " + parameterName, propertyName);
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
	//@Override
	public void close() throws IOException {
		reader.close();
	}
}
