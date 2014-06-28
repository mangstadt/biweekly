package biweekly.io.text;

import static biweekly.util.IOUtils.utf8Writer;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.io.SkipMeException;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.DateStart;
import biweekly.property.Daylight;
import biweekly.property.ICalProperty;
import biweekly.property.Version;
import biweekly.util.UtcOffset;

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
 * <pre class="brush:java">
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
public class ICalWriter implements Closeable, Flushable {
	private ScribeIndex index = new ScribeIndex();
	private final ICalRawWriter writer;

	/**
	 * Creates an iCalendar writer that writes to an output stream. Uses the
	 * standard folding scheme and newline sequence.
	 * @param outputStream the output stream to write to
	 * @param version the iCalendar version to adhere to
	 */
	public ICalWriter(OutputStream outputStream, ICalVersion version) {
		this((version == ICalVersion.V1_0) ? new OutputStreamWriter(outputStream) : utf8Writer(outputStream), version);
	}

	/**
	 * Creates an iCalendar writer that writes to an output stream. Uses the
	 * standard newline sequence.
	 * @param outputStream the output stream to write to
	 * @param version the iCalendar version to adhere to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 */
	public ICalWriter(OutputStream outputStream, ICalVersion version, FoldingScheme foldingScheme) {
		this((version == ICalVersion.V1_0) ? new OutputStreamWriter(outputStream) : utf8Writer(outputStream), version, foldingScheme);
	}

	/**
	 * Creates an iCalendar writer that writes to an output stream.
	 * @param outputStream the output stream to write to
	 * @param version the iCalendar version to adhere to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public ICalWriter(OutputStream outputStream, ICalVersion version, FoldingScheme foldingScheme, String newline) {
		this((version == ICalVersion.V1_0) ? new OutputStreamWriter(outputStream) : utf8Writer(outputStream), version, foldingScheme, newline);
	}

	/**
	 * Creates an iCalendar writer that writes to a file. Uses the standard
	 * folding scheme and newline sequence.
	 * @param file the file to write to
	 * @param version the iCalendar version to adhere to
	 * @throws IOException if the file cannot be written to
	 */
	public ICalWriter(File file, ICalVersion version) throws IOException {
		this((version == ICalVersion.V1_0) ? new FileWriter(file) : utf8Writer(file), version);
	}

	/**
	 * Creates an iCalendar writer that writes to a file. Uses the standard
	 * folding scheme and newline sequence.
	 * @param file the file to write to
	 * @param version the iCalendar version to adhere to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @throws IOException if the file cannot be written to
	 */
	public ICalWriter(File file, boolean append, ICalVersion version) throws IOException {
		this((version == ICalVersion.V1_0) ? new FileWriter(file, append) : utf8Writer(file, append), version);
	}

	/**
	 * Creates an iCalendar writer that writes to a file. Uses the standard
	 * newline sequence.
	 * @param file the file to write to
	 * @param version the iCalendar version to adhere to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @throws IOException if the file cannot be written to
	 */
	public ICalWriter(File file, boolean append, ICalVersion version, FoldingScheme foldingScheme) throws IOException {
		this((version == ICalVersion.V1_0) ? new FileWriter(file, append) : utf8Writer(file, append), version, foldingScheme);
	}

	/**
	 * Creates an iCalendar writer that writes to a file.
	 * @param file the file to write to
	 * @param version the iCalendar version to adhere to
	 * @param append true to append to the end of the file, false to overwrite
	 * it
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 * @throws IOException if the file cannot be written to
	 */
	public ICalWriter(File file, boolean append, ICalVersion version, FoldingScheme foldingScheme, String newline) throws IOException {
		this((version == ICalVersion.V1_0) ? new FileWriter(file, append) : utf8Writer(file, append), version, foldingScheme, newline);
	}

	/**
	 * Creates an iCalendar writer that writes to a writer. Uses the standard
	 * folding scheme and newline sequence.
	 * @param writer the writer to the data stream
	 * @param version the iCalendar version to adhere to
	 */
	public ICalWriter(Writer writer, ICalVersion version) {
		this(writer, version, FoldingScheme.DEFAULT);
	}

	/**
	 * Creates an iCalendar writer that writes to a writer. Uses the standard
	 * newline sequence.
	 * @param writer the writer to the data stream
	 * @param version the iCalendar version to adhere to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 */
	public ICalWriter(Writer writer, ICalVersion version, FoldingScheme foldingScheme) {
		this(writer, version, foldingScheme, "\r\n");
	}

	/**
	 * Creates an iCalendar writer that writes to a writer.
	 * @param writer the writer to the data stream
	 * @param version the iCalendar version to adhere to
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public ICalWriter(Writer writer, ICalVersion version, FoldingScheme foldingScheme, String newline) {
		this.writer = new ICalRawWriter(writer, version, foldingScheme, newline);
	}

	/**
	 * Gets the version that the written iCalendar objects will adhere to.
	 * @return the iCalendar version
	 */
	public ICalVersion getTargetVersion() {
		return writer.getVersion();
	}

	/**
	 * Sets the version that the written iCalendar objects will adhere to.
	 * @param targetVersion the iCalendar version
	 */
	public void setTargetVersion(ICalVersion targetVersion) {
		writer.setVersion(targetVersion);
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
	 * @param scribe the scribe index
	 */
	public void setScribeIndex(ScribeIndex scribe) {
		this.index = scribe;
	}

	/**
	 * Writes an iCalendar object to the data stream.
	 * @param ical the iCalendar object to write
	 * @throws IllegalArgumentException if the scribe class for a component or
	 * property object cannot be found (only happens when an experimental
	 * property/component scribe is not registered with the
	 * {@code registerScribe} method.)
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void write(ICalendar ical) throws IOException {
		index.hasScribesFor(ical);
		writeComponent(ical);
	}

	/**
	 * Writes a component to the data stream.
	 * @param component the component to write
	 * @throws IOException if there's a problem writing to the data stream
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeComponent(ICalComponent component) throws IOException {
		if (writer.getVersion() == ICalVersion.V1_0) {
			if (component instanceof VTimezone) {
				//VTIMEZONE component => DAYLIGHT property
				VTimezone timezone = (VTimezone) component;
				List<Daylight> daylights = convertTimezoneToDaylight(timezone);
				for (Daylight daylight : daylights) {
					writeProperty(daylight);
				}
				return;
			}
		}

		ICalComponentScribe componentScribe = index.getComponentScribe(component);
		writer.writeBeginComponent(componentScribe.getComponentName());

		for (Object propertyObj : componentScribe.getProperties(component)) {
			ICalProperty property = (ICalProperty) propertyObj;
			writeProperty(property);
		}

		for (Object subComponentObj : componentScribe.getComponents(component)) {
			ICalComponent subComponent = (ICalComponent) subComponentObj;
			writeComponent(subComponent);
		}

		writer.writeEndComponent(componentScribe.getComponentName());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void writeProperty(ICalProperty property) throws IOException {
		if (writer.getVersion() == ICalVersion.V2_0 || writer.getVersion() == ICalVersion.V2_0_DEPRECATED) {
			if (property instanceof Daylight) {
				//DAYLIGHT property => VTIMEZONE component
				Daylight daylight = (Daylight) property;
				VTimezone timezone = convertDaylightToTimezone(daylight);
				writeComponent(timezone);
				return;
			}
		}

		ICalPropertyScribe propertyScribe = index.getPropertyScribe(property);

		String value;
		if (property instanceof Version) {
			value = writer.getVersion().getVersion();
		} else {
			//marshal property
			try {
				value = propertyScribe.writeText(property, writer.getVersion());
			} catch (SkipMeException e) {
				return;
			}
		}

		//get parameters
		ICalParameters parameters = propertyScribe.prepareParameters(property, writer.getVersion());

		//set the data type
		ICalDataType dataType = propertyScribe.dataType(property, writer.getVersion());
		if (dataType != null && dataType != propertyScribe.defaultDataType(writer.getVersion())) {
			//only add a VALUE parameter if the data type is (1) not "unknown" and (2) different from the property's default data type
			parameters = new ICalParameters(parameters);
			parameters.setValue(dataType);
		}

		//write property to data stream
		writer.writeProperty(propertyScribe.getPropertyName(), parameters, value);
	}

	private List<Daylight> convertTimezoneToDaylight(VTimezone timezone) {
		List<DaylightSavingsTime> daylightSavingsTimes = timezone.getDaylightSavingsTime();
		List<StandardTime> standardTimes = timezone.getStandardTimes();

		List<Daylight> daylights = new ArrayList<Daylight>();
		int len = Math.max(daylightSavingsTimes.size(), standardTimes.size());
		for (int i = 0; i < len; i++) {
			DaylightSavingsTime daylightSavings = (i < daylightSavingsTimes.size()) ? daylightSavingsTimes.get(i) : null;
			StandardTime standard = (i < standardTimes.size()) ? standardTimes.get(i) : null;

			if (daylightSavings == null) {
				//there is no accompanying DAYLIGHT component, which means that daylight savings time is not observed
				daylights.add(new Daylight());
				continue;
			}

			if (standard == null) {
				//there is no accompanying STANDARD component, which makes no sense
				continue;
			}

			UtcOffset offset = daylightSavings.getTimezoneOffsetTo().getValue();
			Date start = daylightSavings.getDateStart().getValue();
			Date end = standard.getDateStart().getValue();
			String daylightName = (daylightSavings.getTimezoneNames().isEmpty()) ? null : daylightSavings.getTimezoneNames().get(0).getValue();
			String standardName = (standard.getTimezoneNames().isEmpty()) ? null : standard.getTimezoneNames().get(0).getValue();

			daylights.add(new Daylight(true, offset, start, end, standardName, daylightName));
		}
		return daylights;
	}

	/**
	 * Converts a DAYLIGHT property to a VTIMEZONE component.
	 * @param daylight the DAYLIGHT property
	 * @return the VTIMEZONE component
	 */
	private VTimezone convertDaylightToTimezone(Daylight daylight) {
		VTimezone timezone = new VTimezone("TZ1");
		if (!daylight.isDaylight()) {
			return timezone;
		}

		UtcOffset offset = daylight.getOffset();

		//TODO convert all local dates to this timezone
		DaylightSavingsTime dst = new DaylightSavingsTime();
		dst.setDateStart(new DateStart(daylight.getStart()));
		dst.setTimezoneOffsetFrom(offset.getHour() - 1, offset.getMinute());
		dst.setTimezoneOffsetTo(offset.getHour(), offset.getMinute());
		dst.addTimezoneName(daylight.getDaylightName());
		timezone.addDaylightSavingsTime(dst);

		StandardTime st = new StandardTime();
		st.setDateStart(new DateStart(daylight.getEnd()));
		st.setTimezoneOffsetFrom(offset.getHour(), offset.getMinute());
		st.setTimezoneOffsetTo(offset.getHour() - 1, offset.getMinute());
		st.addTimezoneName(daylight.getStandardName());
		timezone.addStandardTime(st);

		return timezone;
	}

	/**
	 * Flushes the stream.
	 * @throws IOException if there's a problem flushing the stream
	 */
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Closes the underlying {@link Writer} object.
	 */
	public void close() throws IOException {
		writer.close();
	}
}
