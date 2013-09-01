package biweekly.io.json;

import static biweekly.util.StringUtils.NEWLINE;

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

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.component.marshaller.ICalendarMarshaller;
import biweekly.io.CannotParseException;
import biweekly.io.ICalMarshallerRegistrar;
import biweekly.io.SkipMeException;
import biweekly.io.json.JCalRawReader.JCalDataStreamListener;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
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
 * <p>
 * Parses {@link ICalendar} objects from a jCal data stream (JSON).
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * Reader reader = ...
 * JCalReader jcalReader = new JCalReader(reader);
 * ICalendar ical;
 * while ((ical = jcalReader.readNext()) != null){
 *   ...
 * }
 * jcalReader.close();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcal-05">jCal
 * draft</a>
 */
public class JCalReader implements Closeable {
	private static final ICalendarMarshaller icalMarshaller = ICalMarshallerRegistrar.getICalendarMarshaller();
	private ICalMarshallerRegistrar registrar = new ICalMarshallerRegistrar();
	private final JCalRawReader reader;
	private final List<String> warnings = new ArrayList<String>();

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

		public void readProperty(List<String> componentHierarchy, String propertyName, ICalParameters parameters, ICalDataType dataType, JCalValue value) {
			//get the component that the property belongs to
			ICalComponent parent = components.get(componentHierarchy);

			//unmarshal the property
			ICalPropertyMarshaller<? extends ICalProperty> m = registrar.getPropertyMarshaller(propertyName);
			ICalProperty property = null;
			try {
				Result<? extends ICalProperty> result = m.parseJson(value, dataType, parameters);

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
				Result<? extends ICalProperty> result = new RawPropertyMarshaller(propertyName).parseJson(value, dataType, parameters);
				for (String warning : result.getWarnings()) {
					addWarning(warning, propertyName);
				}
				property = result.getValue();

				String valueStr = ((RawProperty) property).getValue();
				if (e.getMessage() == null) {
					addWarning("Property value could not be unmarshalled: " + valueStr, propertyName);
				} else {
					addWarning("Property value could not be unmarshalled." + NEWLINE + "  Value: " + valueStr + NEWLINE + "  Reason: " + e.getMessage(), propertyName);
				}
			}

			if (property != null) {
				parent.addProperty(property);
			}
		}

		public void readComponent(List<String> parentHierarchy, String componentName) {
			ICalComponentMarshaller<? extends ICalComponent> m = registrar.getComponentMarshaller(componentName);
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
