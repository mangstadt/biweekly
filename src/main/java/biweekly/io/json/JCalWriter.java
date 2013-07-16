package biweekly.io.json;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.component.marshaller.ComponentLibrary;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.component.marshaller.RawComponentMarshaller;
import biweekly.io.SkipMeException;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;
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
 * Writes {@link ICalendar} objects to a JSON data stream (jCal).
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * List&lt;ICalendar&gt; icals = ... 
 * Writer writer = ...
 * JCalWriter jcalWriter = new JCalWriter(writer);
 * for (ICalendar ical : icals){
 *   jcalWriter.write(ical);
 * }
 * jcalWriter.close();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/draft-ietf-jcardcal-jcal-05">jCal
 * draft</a>
 */
public class JCalWriter implements Closeable {
	private final Map<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>> propertyMarshallers = new HashMap<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>>(0);
	private final Map<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>> componentMarshallers = new HashMap<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>>(0);
	private final JCalRawWriter writer;

	/**
	 * Creates a jCal writer that writes to an output stream.
	 * @param outputStream the output stream to write to
	 */
	public JCalWriter(OutputStream outputStream) {
		this(new OutputStreamWriter(outputStream));
	}

	/**
	 * Creates a jCal writer that writes to an output stream.
	 * @param outputStream the output stream to write to
	 * @param wrapInArray true to wrap all iCalendar objects in a parent array,
	 * false not to (useful when writing more than one iCalendar object)
	 */
	public JCalWriter(OutputStream outputStream, boolean wrapInArray) throws IOException {
		this(new OutputStreamWriter(outputStream), wrapInArray);
	}

	/**
	 * Creates a jCal writer that writes to a file.
	 * @param file the file to write to
	 * @throws IOException if the file cannot be written to
	 */
	public JCalWriter(File file) throws IOException {
		this(new FileWriter(file));
	}

	/**
	 * Creates a jCal writer that writes to a file.
	 * @param file the file to write to
	 * @param wrapInArray true to wrap all iCalendar objects in a parent array,
	 * false not to (useful when writing more than one iCalendar object)
	 * @throws IOException if the file cannot be written to
	 */
	public JCalWriter(File file, boolean wrapInArray) throws IOException {
		this(new FileWriter(file), wrapInArray);
	}

	/**
	 * Creates a jCal writer that writes to a writer.
	 * @param writer the writer to the data stream
	 */
	public JCalWriter(Writer writer) {
		this(writer, false);
	}

	/**
	 * Creates a jCal writer that writes to a writer.
	 * @param writer the writer to the data stream
	 * @param wrapInArray true to wrap all iCalendar objects in a parent array,
	 * false not to (useful when writing more than one iCalendar object)
	 */
	public JCalWriter(Writer writer, boolean wrapInArray) {
		this.writer = new JCalRawWriter(writer, wrapInArray);
	}

	/**
	 * Registers a marshaller for an experimental property.
	 * @param marshaller the marshaller to register
	 */
	public void registerMarshaller(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
		propertyMarshallers.put(marshaller.getPropertyClass(), marshaller);
	}

	/**
	 * Registers a marshaller for an experimental component.
	 * @param marshaller the marshaller to register
	 */
	public void registerMarshaller(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
		componentMarshallers.put(marshaller.getComponentClass(), marshaller);
	}

	/**
	 * Writes an iCalendar object to the data stream.
	 * @param ical the iCalendar object to write
	 * @throws IllegalArgumentException if the marshaller class for a component
	 * or property object cannot be found (only happens when an experimental
	 * property/component marshaller is not registered with the
	 * <code>registerMarshaller</code> method.)
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void write(ICalendar ical) throws IOException {
		writeComponent(ical);
	}

	/**
	 * Writes a component to the data stream.
	 * @param component the component to write
	 * @throws IllegalArgumentException if the marshaller class for a component
	 * or property object cannot be found (only happens when an experimental
	 * property/component marshaller is not registered with the
	 * <code>registerMarshaller</code> method.)
	 * @throws IOException if there's a problem writing to the data stream
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeComponent(ICalComponent component) throws IOException {
		ICalComponentMarshaller compMarshaller = findComponentMarshaller(component);
		if (compMarshaller == null) {
			throw new IllegalArgumentException("No marshaller found for component class \"" + component.getClass().getName() + "\".");
		}

		writer.writeStartComponent(compMarshaller.getComponentName().toLowerCase());

		//write properties
		for (Object obj : compMarshaller.getProperties(component)) {
			ICalProperty property = (ICalProperty) obj;
			ICalPropertyMarshaller propMarshaller = findPropertyMarshaller(property);
			if (propMarshaller == null) {
				throw new IllegalArgumentException("No marshaller found for property class \"" + property.getClass().getName() + "\".");
			}

			//marshal property
			String propertyName = propMarshaller.getPropertyName().toLowerCase();
			ICalParameters parameters;
			JCalValue value;
			try {
				parameters = propMarshaller.prepareParameters(property);
				value = propMarshaller.writeJson(property);
			} catch (SkipMeException e) {
				continue;
			}

			//write property
			parameters.setValue(null);
			writer.writeProperty(propertyName, parameters, value);
		}

		//write sub-components
		for (Object obj : compMarshaller.getComponents(component)) {
			ICalComponent subComponent = (ICalComponent) obj;
			writeComponent(subComponent);
		}

		writer.writeEndComponent();
	}

	/**
	 * Finds a component marshaller.
	 * @param component the component being marshalled
	 * @return the component marshaller or null if not found
	 */
	private ICalComponentMarshaller<? extends ICalComponent> findComponentMarshaller(ICalComponent component) {
		ICalComponentMarshaller<? extends ICalComponent> m = componentMarshallers.get(component.getClass());
		if (m == null) {
			m = ComponentLibrary.getMarshaller(component.getClass());
			if (m == null) {
				if (component instanceof RawComponent) {
					RawComponent raw = (RawComponent) component;
					m = new RawComponentMarshaller(raw.getName());
				}
			}
		}
		return m;
	}

	/**
	 * Finds a property marshaller.
	 * @param property the property being marshalled
	 * @return the property marshaller or null if not found
	 */
	private ICalPropertyMarshaller<? extends ICalProperty> findPropertyMarshaller(ICalProperty property) {
		ICalPropertyMarshaller<? extends ICalProperty> m = propertyMarshallers.get(property.getClass());
		if (m == null) {
			m = PropertyLibrary.getMarshaller(property.getClass());
			if (m == null) {
				if (property instanceof RawProperty) {
					RawProperty raw = (RawProperty) property;
					m = new RawPropertyMarshaller(raw.getName());
				}
			}
		}
		return m;
	}

	/**
	 * Finishes writing the JSON document and closes the underlying
	 * {@link Writer}.
	 * @throws IOException if there's a problem closing the stream
	 */
	public void close() throws IOException {
		writer.close();
	}

	/**
	 * Finishes writing the JSON document so that it is syntactically correct.
	 * No more iCalendar objects can be written once this method is called.
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void closeJsonStream() throws IOException {
		writer.closeJsonStream();
	}
}
