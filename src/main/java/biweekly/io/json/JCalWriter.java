package biweekly.io.json;

import static biweekly.util.IOUtils.utf8Writer;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.io.ICalMarshallerRegistrar;
import biweekly.io.SkipMeException;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;

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
 * OutputStream out = ...
 * JCalWriter jcalWriter = new JCalWriter(out);
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
	private ICalMarshallerRegistrar registrar = new ICalMarshallerRegistrar();
	private final JCalRawWriter writer;

	/**
	 * Creates a jCal writer that writes to an output stream.
	 * @param outputStream the output stream to write to
	 */
	public JCalWriter(OutputStream outputStream) {
		this(utf8Writer(outputStream));
	}

	/**
	 * Creates a jCal writer that writes to an output stream.
	 * @param outputStream the output stream to write to
	 * @param wrapInArray true to wrap all iCalendar objects in a parent array,
	 * false not to (useful when writing more than one iCalendar object)
	 */
	public JCalWriter(OutputStream outputStream, boolean wrapInArray) throws IOException {
		this(utf8Writer(outputStream), wrapInArray);
	}

	/**
	 * Creates a jCal writer that writes to a file.
	 * @param file the file to write to
	 * @throws IOException if the file cannot be written to
	 */
	public JCalWriter(File file) throws IOException {
		this(utf8Writer(file));
	}

	/**
	 * Creates a jCal writer that writes to a file.
	 * @param file the file to write to
	 * @param wrapInArray true to wrap all iCalendar objects in a parent array,
	 * false not to (useful when writing more than one iCalendar object)
	 * @throws IOException if the file cannot be written to
	 */
	public JCalWriter(File file, boolean wrapInArray) throws IOException {
		this(utf8Writer(file), wrapInArray);
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
	 * <p>
	 * Registers an experimental property marshaller. Can also be used to
	 * override the marshaller of a standard property (such as DTSTART). Calling
	 * this method is the same as calling:
	 * </p>
	 * <p>
	 * {@code getRegistrar().register(marshaller)}.
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
	 * {@code getRegistrar().register(marshaller)}.
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
	 * Gets whether or not the JSON will be pretty-printed.
	 * @return true if it will be pretty-printed, false if not (defaults to
	 * false)
	 */
	public boolean isIndent() {
		return writer.isIndent();
	}

	/**
	 * Sets whether or not to pretty-print the JSON.
	 * @param indent true to pretty-print it, false not to (defaults to false)
	 */
	public void setIndent(boolean indent) {
		writer.setIndent(indent);
	}

	/**
	 * Writes an iCalendar object to the data stream.
	 * @param ical the iCalendar object to write
	 * @throws IllegalArgumentException if the marshaller class for a component
	 * or property object cannot be found (only happens when an experimental
	 * property/component marshaller is not registered with the
	 * {@code registerMarshaller} method.)
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
	 * {@code registerMarshaller} method.)
	 * @throws IOException if there's a problem writing to the data stream
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeComponent(ICalComponent component) throws IOException {
		ICalComponentMarshaller compMarshaller = registrar.getComponentMarshaller(component);
		if (compMarshaller == null) {
			throw new IllegalArgumentException("No marshaller found for component class \"" + component.getClass().getName() + "\".");
		}

		writer.writeStartComponent(compMarshaller.getComponentName().toLowerCase());

		//write properties
		for (Object obj : compMarshaller.getProperties(component)) {
			ICalProperty property = (ICalProperty) obj;
			ICalPropertyMarshaller propMarshaller = registrar.getPropertyMarshaller(property);
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

			//get the data type
			ICalDataType dataType = propMarshaller.dataType(property);

			//write property
			writer.writeProperty(propertyName, parameters, dataType, value);
		}

		//write sub-components
		for (Object obj : compMarshaller.getComponents(component)) {
			ICalComponent subComponent = (ICalComponent) obj;
			writeComponent(subComponent);
		}

		writer.writeEndComponent();
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
