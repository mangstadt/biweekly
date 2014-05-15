package biweekly.io.json;

import static biweekly.util.IOUtils.utf8Reader;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.io.CannotParseException;
import biweekly.io.ParseWarnings;
import biweekly.io.SkipMeException;
import biweekly.io.json.JCalRawReader.JCalDataStreamListener;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.component.ICalendarScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.ICalPropertyScribe.Result;
import biweekly.io.scribe.property.RawPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;

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
 * <pre class="brush:java">
 * InputStream in = ...
 * JCalReader jcalReader = new JCalReader(in);
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
	private static final ICalendarScribe icalScribe = ScribeIndex.getICalendarScribe();
	private ScribeIndex index = new ScribeIndex();
	private final JCalRawReader reader;
	private final ParseWarnings warnings = new ParseWarnings();

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
		this(utf8Reader(in));
	}

	/**
	 * Creates a jCard reader.
	 * @param file the file to read the vCards from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public JCalReader(File file) throws FileNotFoundException {
		this(utf8Reader(file));
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
		return warnings.copy();
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
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
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
			ICalPropertyScribe<? extends ICalProperty> scribe = index.getPropertyScribe(propertyName);
			try {
				Result<? extends ICalProperty> result = scribe.parseJson(value, dataType, parameters);
				for (Warning warning : result.getWarnings()) {
					warnings.add(reader.getLineNum(), propertyName, warning);
				}
				ICalProperty property = result.getProperty();
				parent.addProperty(property);
			} catch (SkipMeException e) {
				warnings.add(reader.getLineNum(), propertyName, 0, e.getMessage());
			} catch (CannotParseException e) {
				Result<? extends ICalProperty> result = new RawPropertyScribe(propertyName).parseJson(value, dataType, parameters);
				ICalProperty property = result.getProperty();
				parent.addProperty(property);

				String valueStr = ((RawProperty) property).getValue();
				warnings.add(reader.getLineNum(), propertyName, 1, valueStr, e.getMessage());
			}
		}

		public void readComponent(List<String> parentHierarchy, String componentName) {
			ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(componentName);
			ICalComponent component = scribe.emptyInstance();

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

			ICalComponent component = components.get(Arrays.asList(icalScribe.getComponentName().toLowerCase()));
			if (component == null) {
				//should never happen because the parser always looks for a "vcalendar" component
				return null;
			}

			if (component instanceof ICalendar) {
				//should happen every time
				return (ICalendar) component;
			}

			//this will only happen if the user decides to override the ICalendarScribe for some reason
			ICalendar ical = icalScribe.emptyInstance();
			ical.addComponent(component);
			return ical;
		}
	}
}
