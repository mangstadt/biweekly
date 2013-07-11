package biweekly.io.text;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.component.marshaller.ComponentLibrary;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.component.marshaller.RawComponentMarshaller;
import biweekly.io.SkipMeException;
import biweekly.io.text.ICalRawWriter.ParameterValueChangedListener;
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
 * Writes {@link ICalendar} objects to an iCalendar data stream.
 * </p>
 * 
 * <pre>
 * List&lt;ICalendar&gt; icals = ... 
 * Writer writer = ...
 * ICalWriter icalWriter = new ICalWriter(writer);
 * for (ICalendar ical : icals){
 *   icalWriter.write(ical);
 * }
 * icalWriter.close();
 * </pre>
 * @author Michael Angstadt
 */
public class ICalWriter implements Closeable {
	private final List<String> warnings = new ArrayList<String>();
	private final Map<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>> propertyMarshallers = new HashMap<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>>(0);
	private final Map<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>> componentMarshallers = new HashMap<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>>(0);
	private final ICalRawWriter writer;

	/**
	 * Creates an iCalendar writer that writes to an output stream. Uses the
	 * standard folding scheme and newline sequence.
	 * @param outputStream the output stream to write to
	 */
	public ICalWriter(OutputStream outputStream) {
		this(new OutputStreamWriter(outputStream));
	}

	/**
	 * Creates an iCalendar writer that writes to an output stream. Uses the
	 * standard newline sequence.
	 * @param outputStream the output stream to write to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 */
	public ICalWriter(OutputStream outputStream, FoldingScheme foldingScheme) throws IOException {
		this(new OutputStreamWriter(outputStream), foldingScheme);
	}

	/**
	 * Creates an iCalendar writer that writes to an output stream.
	 * @param outputStream the output stream to write to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public ICalWriter(OutputStream outputStream, FoldingScheme foldingScheme, String newline) throws IOException {
		this(new OutputStreamWriter(outputStream), foldingScheme, newline);
	}

	/**
	 * Creates an iCalendar writer that writes to a file. Uses the standard
	 * folding scheme and newline sequence.
	 * @param file the file to write to
	 * @throws IOException if the file cannot be written to
	 */
	public ICalWriter(File file) throws IOException {
		this(new FileWriter(file));
	}

	/**
	 * Creates an iCalendar writer that writes to a file. Uses the standard
	 * newline sequence.
	 * @param file the file to write to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @throws IOException if the file cannot be written to
	 */
	public ICalWriter(File file, FoldingScheme foldingScheme) throws IOException {
		this(new FileWriter(file), foldingScheme);
	}

	/**
	 * Creates an iCalendar writer that writes to a file.
	 * @param file the file to write to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 * @throws IOException if the file cannot be written to
	 */
	public ICalWriter(File file, FoldingScheme foldingScheme, String newline) throws IOException {
		this(new FileWriter(file), foldingScheme, newline);
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
		this.writer.setParameterValueChangedListener(new ParameterValueChangedListener() {
			public void onParameterValueChanged(String propertyName, String parameterName, String originalValue, String modifiedValue) {
				warnings.add("Parameter \"" + parameterName + "\" of property \"" + propertyName + "\" contained one or more characters which are not allowed.  These characters were removed.");
			}
		});
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
	 * Gets the warnings from the last iCal that was written. This list is reset
	 * every time a new iCal is written.
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
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void write(ICalendar ical) throws IOException {
		warnings.clear();
		writeComponent(ical);
	}

	/**
	 * Writes a component to the data stream.
	 * @param component the component to write
	 * @throws IOException if there's a problem writing to the data stream
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeComponent(ICalComponent component) throws IOException {
		ICalComponentMarshaller m = findComponentMarshaller(component);
		if (m == null) {
			warnings.add("No marshaller found for component class \"" + component.getClass().getName() + "\".  This component will not be written.");
			return;
		}

		writer.writeBeginComponent(m.getComponentName());

		for (Object obj : m.getProperties(component)) {
			ICalProperty property = (ICalProperty) obj;
			ICalPropertyMarshaller pm = findPropertyMarshaller(property);
			if (pm == null) {
				warnings.add("No marshaller found for property class \"" + property.getClass().getName() + "\".  This property will not be written.");
				continue;
			}

			//marshal property
			ICalParameters parameters;
			String value;
			try {
				parameters = pm.prepareParameters(property);
				value = pm.writeText(property);
			} catch (SkipMeException e) {
				if (e.getMessage() == null) {
					addWarning("Property has requested that it be skipped.", pm.getPropertyName());
				} else {
					addWarning("Property has requested that it be skipped: " + e.getMessage(), pm.getPropertyName());
				}
				continue;
			}

			//write property to data stream
			try {
				writer.writeProperty(pm.getPropertyName(), parameters, value);
			} catch (IllegalArgumentException e) {
				addWarning("Property could not be written: " + e.getMessage(), pm.getPropertyName());
				continue;
			}
		}

		for (Object obj : m.getComponents(component)) {
			ICalComponent subComponent = (ICalComponent) obj;
			writeComponent(subComponent);
		}

		writer.writeEndComponent(m.getComponentName());
	}

	/**
	 * Finds a component marshaller.
	 * @param component the component being marshalled
	 * @return the component marshaller or null if not found
	 */
	private ICalComponentMarshaller<? extends ICalComponent> findComponentMarshaller(final ICalComponent component) {
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
	 * Closes the underlying {@link Writer} object.
	 */
	public void close() throws IOException {
		writer.close();
	}

	private void addWarning(String message, String propertyName) {
		warnings.add(propertyName + " property: " + message);
	}
}
