package biweekly.io.text;

import static biweekly.util.IOUtils.utf8Writer;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
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
 * Writes {@link ICalendar} objects to an iCalendar data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * List&lt;ICalendar&gt; icals = ... 
 * OutputStream out = ...
 * ICalWriter icalWriter = new ICalWriter(out);
 * for (ICalendar ical : icals){
 *   icalWriter.write(ical);
 * }
 * icalWriter.close();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545">RFC 5545</a>
 */
public class ICalWriter implements Closeable {
	private ICalMarshallerRegistrar registrar = new ICalMarshallerRegistrar();
	private final ICalRawWriter writer;

	/**
	 * Creates an iCalendar writer that writes to an output stream. Uses the
	 * standard folding scheme and newline sequence.
	 * @param outputStream the output stream to write to
	 */
	public ICalWriter(OutputStream outputStream) {
		this(utf8Writer(outputStream));
	}

	/**
	 * Creates an iCalendar writer that writes to an output stream. Uses the
	 * standard newline sequence.
	 * @param outputStream the output stream to write to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 */
	public ICalWriter(OutputStream outputStream, FoldingScheme foldingScheme) throws IOException {
		this(utf8Writer(outputStream), foldingScheme);
	}

	/**
	 * Creates an iCalendar writer that writes to an output stream.
	 * @param outputStream the output stream to write to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public ICalWriter(OutputStream outputStream, FoldingScheme foldingScheme, String newline) throws IOException {
		this(utf8Writer(outputStream), foldingScheme, newline);
	}

	/**
	 * Creates an iCalendar writer that writes to a file. Uses the standard
	 * folding scheme and newline sequence.
	 * @param file the file to write to
	 * @throws FileNotFoundException if the file cannot be written to
	 */
	public ICalWriter(File file) throws FileNotFoundException {
		this(utf8Writer(file));
	}

	/**
	 * Creates an iCalendar writer that writes to a file. Uses the standard
	 * folding scheme and newline sequence.
	 * @param file the file to write to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @throws FileNotFoundException if the file cannot be written to
	 */
	public ICalWriter(File file, boolean append) throws FileNotFoundException {
		this(utf8Writer(file, append));
	}

	/**
	 * Creates an iCalendar writer that writes to a file. Uses the standard
	 * newline sequence.
	 * @param file the file to write to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @throws FileNotFoundException if the file cannot be written to
	 */
	public ICalWriter(File file, boolean append, FoldingScheme foldingScheme) throws FileNotFoundException {
		this(utf8Writer(file, append), foldingScheme);
	}

	/**
	 * Creates an iCalendar writer that writes to a file.
	 * @param file the file to write to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 * @throws FileNotFoundException if the file cannot be written to
	 */
	public ICalWriter(File file, boolean append, FoldingScheme foldingScheme, String newline) throws FileNotFoundException {
		this(utf8Writer(file, append), foldingScheme, newline);
	}

	/**
	 * Creates an iCalendar writer that writes to a writer. Uses the standard
	 * folding scheme and newline sequence.
	 * @param writer the writer to the data stream
	 */
	public ICalWriter(Writer writer) {
		this(writer, FoldingScheme.DEFAULT);
	}

	/**
	 * Creates an iCalendar writer that writes to a writer. Uses the standard
	 * newline sequence.
	 * @param writer the writer to the data stream
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 */
	public ICalWriter(Writer writer, FoldingScheme foldingScheme) {
		this(writer, foldingScheme, "\r\n");
	}

	/**
	 * Creates an iCalendar writer that writes to a writer.
	 * @param writer the writer to the data stream
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public ICalWriter(Writer writer, FoldingScheme foldingScheme, String newline) {
		this.writer = new ICalRawWriter(writer, foldingScheme, newline);
	}

	/**
	 * <p>
	 * Gets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values.
	 * </p>
	 * 
	 * <p>
	 * When disabled, the writer will replace newlines with spaces and double
	 * quotes with single quotes.
	 * </p>
	 * @return true if circumflex accent encoding is enabled, false if not
	 * @see ICalRawWriter#isCaretEncodingEnabled()
	 */
	public boolean isCaretEncodingEnabled() {
		return writer.isCaretEncodingEnabled();
	}

	/**
	 * <p>
	 * Sets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values.
	 * </p>
	 * 
	 * <p>
	 * When disabled, the writer will replace newlines with spaces and double
	 * quotes with single quotes.
	 * </p>
	 * @param enable true to use circumflex accent encoding, false not to
	 * @see ICalRawWriter#setCaretEncodingEnabled(boolean)
	 */
	public void setCaretEncodingEnabled(boolean enable) {
		writer.setCaretEncodingEnabled(enable);
	}

	/**
	 * Gets the newline sequence that is used to separate lines.
	 * @return the newline sequence
	 */
	public String getNewline() {
		return writer.getNewline();
	}

	/**
	 * Gets the rules for how each line is folded.
	 * @return the folding scheme or null if the lines are not folded
	 */
	public FoldingScheme getFoldingScheme() {
		return writer.getFoldingScheme();
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
	 * @throws IOException if there's a problem writing to the data stream
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeComponent(ICalComponent component) throws IOException {
		ICalComponentMarshaller m = registrar.getComponentMarshaller(component);
		if (m == null) {
			throw new IllegalArgumentException("No marshaller found for component class \"" + component.getClass().getName() + "\".");
		}

		writer.writeBeginComponent(m.getComponentName());

		for (Object obj : m.getProperties(component)) {
			ICalProperty property = (ICalProperty) obj;
			ICalPropertyMarshaller pm = registrar.getPropertyMarshaller(property);
			if (pm == null) {
				throw new IllegalArgumentException("No marshaller found for property class \"" + property.getClass().getName() + "\".");
			}

			//marshal property
			ICalParameters parameters;
			String value;
			try {
				parameters = pm.prepareParameters(property);
				value = pm.writeText(property);
			} catch (SkipMeException e) {
				continue;
			}

			//set the data type
			ICalDataType dataType = pm.getDataType(property);
			if (dataType != null && dataType != pm.getDefaultDataType()) {
				//only add a VALUE parameter if the data type is (1) not "unknown" and (2) different from the property's default data type
				parameters.setValue(dataType);
			}

			//write property to data stream
			writer.writeProperty(pm.getPropertyName(), parameters, value);
		}

		for (Object obj : m.getComponents(component)) {
			ICalComponent subComponent = (ICalComponent) obj;
			writeComponent(subComponent);
		}

		writer.writeEndComponent(m.getComponentName());
	}

	/**
	 * Closes the underlying {@link Writer} object.
	 */
	public void close() throws IOException {
		writer.close();
	}
}
