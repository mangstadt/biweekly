package biweekly.io.json;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ComponentLibrary;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.component.marshaller.ICalendarMarshaller;
import biweekly.component.marshaller.RawComponentMarshaller;
import biweekly.io.CannotParseException;
import biweekly.io.SkipMeException;
import biweekly.io.json.JCalRawReader.JCalDataStreamListener;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
import biweekly.property.marshaller.PropertyLibrary;
import biweekly.property.marshaller.RawPropertyMarshaller;

import com.fasterxml.jackson.core.JsonParseException;

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
 * Parses {@link ICalendar} objects from a jCal data stream (JSON).
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcal-05">jCal
 * draft</a>
 */
public class JCalReader implements Closeable {
	private static final ICalendarMarshaller icalMarshaller = (ICalendarMarshaller) ComponentLibrary.getMarshaller(ICalendar.class);
	private final JCalRawReader reader;
	private final List<String> warnings = new ArrayList<String>();
	private final Map<String, ICalPropertyMarshaller<? extends ICalProperty>> propertyMarshallers = new HashMap<String, ICalPropertyMarshaller<? extends ICalProperty>>(0);
	private final Map<String, ICalComponentMarshaller<? extends ICalComponent>> componentMarshallers = new HashMap<String, ICalComponentMarshaller<? extends ICalComponent>>(0);

	/**
	 * Creates a jCard reader.
	 * @param json the JSON string
	 */
	public JCalReader(String json) {
		this(new StringReader(json));
	}

	/**
	 * Creates a jCard reader.
	 * @param in the input stream to read the vCards from
	 */
	public JCalReader(InputStream in) {
		this(new InputStreamReader(in));
	}

	/**
	 * Creates a jCard reader.
	 * @param file the file to read the vCards from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public JCalReader(File file) throws FileNotFoundException {
		this(new FileReader(file));
	}

	/**
	 * Creates a jCard reader.
	 * @param reader the reader to read the vCards from
	 */
	public JCalReader(Reader reader) {
		this.reader = new JCalRawReader(reader);
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
	 * Registers a marshaller for an experimental property.
	 * @param marshaller the marshaller to register
	 */
	public void registerMarshaller(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
		propertyMarshallers.put(marshaller.getPropertyName().toLowerCase(), marshaller);
	}

	/**
	 * Registers a marshaller for an experimental component.
	 * @param marshaller the marshaller to register
	 */
	public void registerMarshaller(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
		componentMarshallers.put(marshaller.getComponentName().toLowerCase(), marshaller);
	}

	/**
	 * Reads the next iCalendar object from the JSON data stream.
	 * @return the iCalendar object or null if there are no more
	 * @throws JCalParseException if the jCal syntax is incorrect (the JSON
	 * syntax may be valid, but it is not in the correct jCal format).
	 * @throws JsonParseException if the JSON syntax is incorrect
	 * @throws IOException if there is a problem reading from the data stream
	 */
	public ICalendar readNext() throws IOException {
		if (reader.eof()) {
			return null;
		}

		warnings.clear();

		JCalDataStreamListenerImpl listener = new JCalDataStreamListenerImpl();
		reader.readNext(listener);
		return listener.getICalendar();
	}

	/**
	 * Finds a component marshaller.
	 * @param componentName the name of the component
	 * @return the component marshallerd
	 */
	private ICalComponentMarshaller<? extends ICalComponent> findComponentMarshaller(String componentName) {
		ICalComponentMarshaller<? extends ICalComponent> m = componentMarshallers.get(componentName.toLowerCase());
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
		ICalPropertyMarshaller<? extends ICalProperty> m = propertyMarshallers.get(propertyName.toLowerCase());
		if (m == null) {
			m = PropertyLibrary.getMarshaller(propertyName);
			if (m == null) {
				m = new RawPropertyMarshaller(propertyName);
			}
		}
		return m;
	}

	private void addWarning(String message, String propertyName) {
		StringBuilder sb = new StringBuilder();
		sb.append("Line ").append(reader.getLineNum());
		if (propertyName != null) {
			sb.append(" (").append(propertyName).append(" property)");
		}
		sb.append(": ").append(message);

		warnings.add(sb.toString());
	}

	//@Override
	public void close() throws IOException {
		reader.close();
	}

	private class JCalDataStreamListenerImpl implements JCalDataStreamListener {
		private final Map<List<String>, ICalComponent> components = new HashMap<List<String>, ICalComponent>();

		public void readProperty(List<String> componentHierarchy, String propertyName, ICalParameters parameters, JCalValue value) {
			//get the component that the property belongs to
			ICalComponent parent = components.get(componentHierarchy);

			//unmarshal the property
			ICalPropertyMarshaller<? extends ICalProperty> m = findPropertyMarshaller(propertyName);
			ICalProperty property = null;
			try {
				Result<? extends ICalProperty> result = m.parseJson(value, parameters);

				for (String warning : result.getWarnings()) {
					addWarning(warning, propertyName);
				}

				property = result.getValue();
			} catch (SkipMeException e) {
				if (e.getMessage() == null) {
					addWarning("Property has requested that it be skipped.", propertyName);
				} else {
					addWarning("Property has requested that it be skipped: " + e.getMessage(), propertyName);
				}
			} catch (CannotParseException e) {
				if (e.getMessage() == null) {
					addWarning("Property value could not be unmarshalled: " + value, propertyName);
				} else {
					addWarning("Property value could not be unmarshalled.\n  Value: " + value + "\n  Reason: " + e.getMessage(), propertyName);
				}

				Result<? extends ICalProperty> result = new RawPropertyMarshaller(propertyName).parseJson(value, parameters);
				for (String warning : result.getWarnings()) {
					addWarning(warning, propertyName);
				}
				property = result.getValue();
			}

			if (property != null) {
				parent.addProperty(property);
			}
		}

		public void readComponent(List<String> parentHierarchy, String componentName) {
			ICalComponentMarshaller<? extends ICalComponent> m = findComponentMarshaller(componentName);
			ICalComponent component = m.emptyInstance();

			ICalComponent parent = components.get(parentHierarchy);
			if (parent != null) {
				parent.addComponent(component);
			}

			List<String> hierarchy = new ArrayList<String>(parentHierarchy);
			hierarchy.add(componentName);
			components.put(hierarchy, component);
		}

		public ICalendar getICalendar() {
			if (components.isEmpty()) {
				//EOF
				return null;
			}

			ICalComponent component = components.get(Arrays.asList(icalMarshaller.getComponentName().toLowerCase()));
			if (component == null) {
				//should never happen because the parser always looks for a "vcalendar" component
				return null;
			}

			if (component instanceof ICalendar) {
				//should happen every time
				return (ICalendar) component;
			}

			//this will only happen if the user decides to override the ICalendarMarshaller for some reason
			ICalendar ical = icalMarshaller.emptyInstance();
			ical.addComponent(component);
			return ical;
		}
	}
}
