package biweekly.io.text;

import static biweekly.io.DataModelConverter.convert;

import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;

import com.github.mangstadt.vinnie.LaciToken;
import com.github.mangstadt.vinnie.VObjectParameters;
import com.github.mangstadt.vinnie.io.FoldedLineWriter;
import com.github.mangstadt.vinnie.io.VObjectWriter;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.io.DataModelConversionException;
import biweekly.io.DataModelConverter.VCalTimezoneProperties;
import biweekly.io.SkipMeException;
import biweekly.io.StreamWriter;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.Daylight;
import biweekly.property.ICalProperty;
import biweekly.property.Timezone;
import biweekly.util.Utf8Writer;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
 * Writes {@link ICalendar} objects to a plain-text iCalendar data stream.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ICalendar ical1 = ...
 * ICalendar ical2 = ...
 * File file = new File("icals.ics");
 * ICalWriter writer = null;
 * try {
 *   writer = new ICalWriter(file, ICalVersion.V2_0);
 *   writer.write(ical1);
 *   writer.write(ical2);
 * } finally {
 *   if (writer != null) writer.close();
 * }
 * </pre>
 * 
 * <p>
 * <b>Changing the line folding settings:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * //disable line folding
 * ICalWriter writer = ICalWriter.withoutLineFolding(...);
 * 
 * //set custom line folding settings
 * Writer w = ...
 * FoldedLineWriter flw = new FoldedLineWriter(w, 50, "\t");
 * ICalWriter writer = new ICalWriter(flw, ...);
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/pdiproddev.html">1.0 specs</a>
 * @see <a href="https://tools.ietf.org/html/rfc2445">RFC 2445</a>
 * @see <a href="http://tools.ietf.org/html/rfc5545">RFC 5545</a>
 */
public class ICalWriter extends StreamWriter implements Flushable {
	private final VObjectWriter writer;
	private ICalVersion targetVersion;

	/**
	 * Creates a new iCalendar writer.
	 * @param out the output stream to write to
	 * @param targetVersion the iCalendar version to adhere to
	 */
	public ICalWriter(OutputStream out, ICalVersion targetVersion) {
		this((targetVersion == ICalVersion.V1_0) ? new OutputStreamWriter(out) : new Utf8Writer(out), targetVersion);
	}

	/**
	 * Creates a new iCalendar writer.
	 * @param file the file to write to
	 * @param targetVersion the iCalendar version to adhere to
	 * @throws IOException if the file cannot be written to
	 */
	public ICalWriter(File file, ICalVersion targetVersion) throws IOException {
		this(file, false, targetVersion);
	}

	/**
	 * Creates a new iCalendar writer.
	 * @param file the file to write to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @param targetVersion the iCalendar version to adhere to
	 * @throws IOException if the file cannot be written to
	 */
	public ICalWriter(File file, boolean append, ICalVersion targetVersion) throws IOException {
		this(writerFromFile(file, append, targetVersion), targetVersion);
	}

	/**
	 * Creates a new iCalendar writer.
	 * @param writer the writer to write to
	 * @param targetVersion the iCalendar version to adhere to
	 */
	public ICalWriter(Writer writer, ICalVersion targetVersion) {
		this(writer, targetVersion, true);
	}

	/**
	 * Creates a new iCalendar writer with custom line folding settings.
	 * @param writer the folded line writer
	 * @param targetVersion the iCalendar version to adhere to
	 */
	public ICalWriter(FoldedLineWriter writer, ICalVersion targetVersion) {
		this((Writer) writer, targetVersion);
	}

	/**
	 * Creates a new iCalendar writer that doesn't use line folding.
	 * @param file the file to write to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @param targetVersion the iCalendar version to adhere to
	 * @return the writer
	 * @throws IOException if the file cannot be written to
	 */
	public static ICalWriter withoutLineFolding(File file, boolean append, ICalVersion targetVersion) throws IOException {
		Writer writer = writerFromFile(file, append, targetVersion);
		return withoutLineFolding(writer, targetVersion);
	}

	/**
	 * Creates a iCalendar writer that doesn't use line folding.
	 * @param writer the writer to write to
	 * @param targetVersion the iCalendar version to adhere to
	 * @return the writer
	 */
	public static ICalWriter withoutLineFolding(Writer writer, ICalVersion targetVersion) {
		return new ICalWriter(writer, targetVersion, false);
	}

	private static Writer writerFromFile(File file, boolean append, ICalVersion targetVersion) throws IOException {
		return (targetVersion == ICalVersion.V1_0) ? new FileWriter(file, append) : new Utf8Writer(file, append);
	}

	/**
	 * @param writer the writer to write to
	 * @param targetVersion the iCalendar version to adhere to
	 */
	private ICalWriter(Writer writer, ICalVersion targetVersion, boolean useLineFolding) {
		if (useLineFolding) {
			this.writer = new VObjectWriter(writer, targetVersion.getSyntaxStyle());
		} else {
			this.writer = VObjectWriter.withoutLineFolding(writer, targetVersion.getSyntaxStyle());
		}

		this.targetVersion = targetVersion;
	}

	/**
	 * Gets the writer object that is used internally to write to the output
	 * stream.
	 * @return the raw writer
	 */
	public VObjectWriter getVObjectWriter() {
		return writer;
	}

	/**
	 * Gets the version that the written iCalendar objects will adhere to.
	 * @return the iCalendar version
	 */
	@Override
	public ICalVersion getTargetVersion() {
		return targetVersion;
	}

	/**
	 * Sets the version that the written iCalendar objects will adhere to.
	 * @param targetVersion the iCalendar version
	 */
	public void setTargetVersion(ICalVersion targetVersion) {
		this.targetVersion = targetVersion;
		writer.setSyntaxStyle(targetVersion.getSyntaxStyle());
	}

	/**
	 * <p>
	 * Gets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values. It is
	 * only supported by iCalendar version 2.0.
	 * </p>
	 * @return true if circumflex accent encoding is enabled, false if not
	 * @see VObjectWriter#isCaretEncodingEnabled()
	 */
	public boolean isCaretEncodingEnabled() {
		return writer.isCaretEncodingEnabled();
	}

	/**
	 * <p>
	 * Sets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values. It is
	 * only supported by iCalendar version 2.0.
	 * </p>
	 * <p>
	 * Note that this encoding mechanism is defined separately from the
	 * iCalendar specification and may not be supported by the consumer of the
	 * iCalendar object.
	 * </p>
	 * @param enable true to use circumflex accent encoding, false not to
	 * @see VObjectWriter#setCaretEncodingEnabled(boolean)
	 */
	public void setCaretEncodingEnabled(boolean enable) {
		writer.setCaretEncodingEnabled(enable);
	}

	@Override
	protected void _write(ICalendar ical) throws IOException {
		writeComponent(ical, null);
	}

	/**
	 * Writes a component to the data stream.
	 * @param component the component to write
	 * @param parent the parent component
	 * @throws IOException if there's a problem writing to the data stream
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeComponent(ICalComponent component, ICalComponent parent) throws IOException {
		ICalComponentScribe componentScribe = index.getComponentScribe(component);
		try {
			componentScribe.checkForDataModelConversions(component, parent, getTargetVersion());
		} catch (DataModelConversionException e) {
			for (ICalComponent c : e.getComponents()) {
				writeComponent(c, parent);
			}
			for (ICalProperty p : e.getProperties()) {
				writeProperty(p);
			}
			return;
		}

		String componentNameUpper = componentScribe.getComponentName().toUpperCase();
		writer.writeBeginComponent(componentNameUpper);

		List propertyObjs = componentScribe.getProperties(component);
		addVersionPropertyIfMissing(component, propertyObjs);

		for (Object propertyObj : propertyObjs) {
			context.setParent(component); //set parent here incase a scribe resets the parent
			ICalProperty property = (ICalProperty) propertyObj;
			writeProperty(property);
		}

		List subComponents = componentScribe.getComponents(component);
		if (getTargetVersion() != ICalVersion.V1_0) {
			addTimezoneComponentsIfMissing(component, subComponents);
		}

		for (Object subComponentObj : subComponents) {
			ICalComponent subComponent = (ICalComponent) subComponentObj;
			writeComponent(subComponent, component);
		}

		boolean inVCalRoot = (component instanceof ICalendar) && (getTargetVersion() == ICalVersion.V1_0);
		if (inVCalRoot) {
			writeVCalTimezones();
		}

		writer.writeEndComponent(componentNameUpper);
	}

	private void writeVCalTimezones() throws IOException {
		Collection<VTimezone> timezones = getTimezoneComponents();
		if (timezones.isEmpty()) {
			return;
		}

		VTimezone timezone = timezones.iterator().next();
		VCalTimezoneProperties props = convert(timezone, context.getDates());

		Timezone tz = props.getTz();
		if (tz != null) {
			writeProperty(tz);
		}
		for (Daylight daylight : props.getDaylights()) {
			writeProperty(daylight);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeProperty(ICalProperty property) throws IOException {
		ICalPropertyScribe scribe = index.getPropertyScribe(property);

		//marshal property
		String value;
		try {
			value = scribe.writeText(property, context);
		} catch (SkipMeException e) {
			return;
		} catch (DataModelConversionException e) {
			for (ICalComponent c : e.getComponents()) {
				writeComponent(c, context.getParent());
			}
			for (ICalProperty p : e.getProperties()) {
				writeProperty(p);
			}
			return;
		}

		//get parameters
		ICalParameters parameters = scribe.prepareParameters(property, context);

		/*
		 * Set the property's data type.
		 * 
		 * Only add a VALUE parameter if the data type is: (1) not "unknown" (2)
		 * different from the property's default data type
		 */
		ICalDataType dataType = scribe.dataType(property, targetVersion);
		if (dataType != null && dataType != scribe.defaultDataType(targetVersion)) {
			parameters = new ICalParameters(parameters);
			parameters.setValue(dataType);
		}

		//get the property name
		LaciToken propertyName = scribe.getPropertyName(getTargetVersion());

		//write property to data stream
		writer.writeProperty(null, propertyName.toUpperCase(), new VObjectParameters(parameters.getMap()), value);
	}

	/**
	 * Flushes the output stream.
	 * @throws IOException if there's a problem flushing the output stream
	 */
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Closes the output stream.
	 * @throws IOException if there's a problem closing the output stream
	 */
	public void close() throws IOException {
		writer.close();
	}
}
