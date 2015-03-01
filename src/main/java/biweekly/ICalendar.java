package biweekly;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import biweekly.ValidationWarnings.WarningsGroup;
import biweekly.component.ICalComponent;
import biweekly.component.VEvent;
import biweekly.component.VFreeBusy;
import biweekly.component.VJournal;
import biweekly.component.VTodo;
import biweekly.property.CalendarScale;
import biweekly.property.Geo;
import biweekly.property.Method;
import biweekly.property.ProductId;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
 * Represents an iCalendar object.
 * </p>
 * 
 * <p>
 * <b>Examples:</b>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * 
 * VEvent event = new VEvent();
 * event.setSummary("Team Meeting");
 * Date start = ...;
 * event.setDateStart(start);
 * Date end = ...;
 * event.setDateEnd(end);
 * ical.addEvent(event);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545">RFC 5545</a>
 */
public class ICalendar extends ICalComponent {
	private ICalVersion version;

	/**
	 * <p>
	 * Creates a new iCalendar object.
	 * </p>
	 * <p>
	 * The following properties are auto-generated on object creation. These
	 * properties <b>must</b> be present in order for the iCalendar object to be
	 * valid:
	 * <ul>
	 * <li>{@link ProductId} - Set to a value that represents this library.</li>
	 * </ul>
	 * </p>
	 */
	public ICalendar() {
		setProductId(ProductId.biweekly());
	}

	/**
	 * Gets the version of this iCalendar object.
	 * @return the version
	 */
	public ICalVersion getVersion() {
		return version;
	}

	/**
	 * Sets the version of this iCalendar object.
	 * @param version the version
	 */
	public void setVersion(ICalVersion version) {
		this.version = version;
	}

	/**
	 * Gets the name of the application that created the iCalendar object. All
	 * {@link ICalendar} objects are initialized with a product ID representing
	 * this library. It is a <b>required</b> property.
	 * @return the property instance or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-78">RFC 5545
	 * p.78-9</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.24</a>
	 */
	public ProductId getProductId() {
		return getProperty(ProductId.class);
	}

	/**
	 * Sets the name of the application that created the iCalendar object. All
	 * {@link ICalendar} objects are initialized with a product ID representing
	 * this library. It is a <b>required</b> property.
	 * @param prodId the property instance or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-78">RFC 5545
	 * p.78-9</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.24</a>
	 */
	public void setProductId(ProductId prodId) {
		setProperty(ProductId.class, prodId);
	}

	/**
	 * Sets the application that created the iCalendar object. All
	 * {@link ICalendar} objects are initialized with a product ID representing
	 * this library.
	 * @param prodId a unique string representing the application (e.g.
	 * "-//Company//Application//EN") or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-78">RFC 5545
	 * p.78-9</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.24</a>
	 */
	public ProductId setProductId(String prodId) {
		ProductId prop = (prodId == null) ? null : new ProductId(prodId);
		setProductId(prop);
		return prop;
	}

	/**
	 * Gets the calendar system that this iCalendar object uses. If none is
	 * specified, then the calendar is assumed to be in Gregorian format.
	 * @return the calendar system or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-76">RFC 5545
	 * p.76-7</a>
	 */
	public CalendarScale getCalendarScale() {
		return getProperty(CalendarScale.class);
	}

	/**
	 * Sets the calendar system that this iCalendar object uses. If none is
	 * specified, then the calendar is assumed to be in Gregorian format.
	 * @param calendarScale the calendar system or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-76">RFC 5545
	 * p.76-7</a>
	 */
	public void setCalendarScale(CalendarScale calendarScale) {
		setProperty(CalendarScale.class, calendarScale);
	}

	/**
	 * Gets the value of the Content-Type "method" parameter if the iCalendar
	 * object is defined as a MIME message entity.
	 * @return the property or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-77">RFC 5545
	 * p.77-8</a>
	 */
	public Method getMethod() {
		return getProperty(Method.class);
	}

	/**
	 * Sets the value of the Content-Type "method" parameter if the iCalendar
	 * object is defined as a MIME message entity.
	 * @param method the property or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-77">RFC 5545
	 * p.77-8</a>
	 */
	public void setMethod(Method method) {
		setProperty(Method.class, method);
	}

	/**
	 * Sets the value of the Content-Type "method" parameter if the iCalendar
	 * object is defined as a MIME message entity.
	 * @param method the method or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-77">RFC 5545
	 * p.77-8</a>
	 */
	public Method setMethod(String method) {
		Method prop = (method == null) ? null : new Method(method);
		setMethod(prop);
		return prop;
	}

	/**
	 * Gets the events.
	 * @return the events
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-52">RFC 5545
	 * p.52-5</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.13</a>
	 */
	public List<VEvent> getEvents() {
		return getComponents(VEvent.class);
	}

	/**
	 * Adds an event.
	 * @param event the event
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-52">RFC 5545
	 * p.52-5</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.13</a>
	 */
	public void addEvent(VEvent event) {
		addComponent(event);
	}

	/**
	 * Gets the to-dos.
	 * @return the to-dos
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-55">RFC 5545
	 * p.55-7</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.14</a>
	 */
	public List<VTodo> getTodos() {
		return getComponents(VTodo.class);
	}

	/**
	 * Adds a to-do.
	 * @param todo the to-do
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-55">RFC 5545
	 * p.55-7</a>
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.14</a>
	 */
	public void addTodo(VTodo todo) {
		addComponent(todo);
	}

	/**
	 * Gets the journal entries.
	 * @return the journal entries
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-55">RFC 5545
	 * p.57-9</a>
	 */
	public List<VJournal> getJournals() {
		return getComponents(VJournal.class);
	}

	/**
	 * Adds a journal entry.
	 * @param journal the journal entry
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-55">RFC 5545
	 * p.57-9</a>
	 */
	public void addJournal(VJournal journal) {
		addComponent(journal);
	}

	/**
	 * Gets the free/busy entries.
	 * @return the free/busy entries
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-59">RFC 5545
	 * p.59-62</a>
	 */
	public List<VFreeBusy> getFreeBusies() {
		return getComponents(VFreeBusy.class);
	}

	/**
	 * Adds a free/busy entry.
	 * @param freeBusy the free/busy entry
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-59">RFC 5545
	 * p.59-62</a>
	 */
	public void addFreeBusy(VFreeBusy freeBusy) {
		addComponent(freeBusy);
	}

	/**
	 * Checks this iCalendar object for data consistency problems or deviations
	 * from the spec. These problems will not prevent the iCalendar object from
	 * being written to a data stream, but may prevent it from being parsed
	 * correctly by the consuming application. These problems can largely be
	 * avoided by reading the Javadocs of the component and property classes, or
	 * by being familiar with the iCalendar standard.
	 * @param version the version to validate against
	 * @return the validation warnings
	 */
	public ValidationWarnings validate(ICalVersion version) {
		List<WarningsGroup> warnings = validate(new ArrayList<ICalComponent>(0), version);
		return new ValidationWarnings(warnings);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		if (version != ICalVersion.V1_0) {
			checkRequiredCardinality(warnings, ProductId.class);

			if (this.components.isEmpty()) {
				warnings.add(Warning.validate(4));
			}

			if (getProperty(Geo.class) != null) {
				warnings.add(Warning.validate(44));
			}
		}
	}

	/**
	 * Marshals this iCalendar object to its plain text representation.
	 * @return the plain text representation
	 */
	public String write() {
		ICalVersion version = (this.version == null) ? ICalVersion.V2_0 : this.version;
		return Biweekly.write(this).version(version).go();
	}

	/**
	 * Marshals this iCalendar object to its plain text representation.
	 * @param file the file to write to
	 * @throws IOException if there's an I/O problem
	 */
	public void write(File file) throws IOException {
		ICalVersion version = (this.version == null) ? ICalVersion.V2_0 : this.version;
		Biweekly.write(this).version(version).go(file);
	}

	/**
	 * Marshals this iCalendar object to its plain text representation.
	 * @param out the data stream to write to
	 * @throws IOException if there's an I/O problem
	 */
	public void write(OutputStream out) throws IOException {
		ICalVersion version = (this.version == null) ? ICalVersion.V2_0 : this.version;
		Biweekly.write(this).version(version).go(out);
	}

	/**
	 * Marshals this iCalendar object to its plain text representation.
	 * @param writer the data stream to write to
	 * @throws IOException if there's an I/O problem
	 */
	public void write(Writer writer) throws IOException {
		ICalVersion version = (this.version == null) ? ICalVersion.V2_0 : this.version;
		Biweekly.write(this).version(version).go(writer);
	}

	/**
	 * Marshals this iCalendar object to its XML representation (xCal). If the
	 * iCalendar object contains user-defined property or component objects, use
	 * the {@link Biweekly} class instead, in order to register the scribe
	 * classes.
	 * @return the XML document
	 * @throws IllegalArgumentException if the iCalendar object contains
	 * user-defined property or component objects
	 */
	public String writeXml() {
		return Biweekly.writeXml(this).indent(2).go();
	}

	/**
	 * Marshals this iCalendar object to its XML representation (xCal). If the
	 * iCalendar object contains user-defined property or component objects, use
	 * the {@link Biweekly} class instead, in order to register the scribe
	 * classes.
	 * @param file the file to write to
	 * @throws IllegalArgumentException if the iCalendar object contains
	 * user-defined property or component objects
	 * @throws TransformerException if there's an I/O problem
	 * @throws IOException if the file cannot be written to
	 */
	public void writeXml(File file) throws TransformerException, IOException {
		Biweekly.writeXml(this).indent(2).go(file);
	}

	/**
	 * Marshals this iCalendar object to its XML representation (xCal). If the
	 * iCalendar object contains user-defined property or component objects, use
	 * the {@link Biweekly} class instead, in order to register the scribe
	 * classes.
	 * @param out the data stream to write to
	 * @throws IllegalArgumentException if the iCalendar object contains
	 * user-defined property or component objects
	 * @throws TransformerException if there's an I/O problem
	 */
	public void writeXml(OutputStream out) throws TransformerException {
		Biweekly.writeXml(this).indent(2).go(out);
	}

	/**
	 * Marshals this iCalendar object to its XML representation (xCal). If the
	 * iCalendar object contains user-defined property or component objects, use
	 * the {@link Biweekly} class instead, in order to register the scribe
	 * classes.
	 * @param writer the data stream to write to
	 * @throws IllegalArgumentException if the iCalendar object contains
	 * user-defined property or component objects
	 * @throws TransformerException if there's an I/O problem
	 */
	public void writeXml(Writer writer) throws TransformerException {
		Biweekly.writeXml(this).indent(2).go(writer);
	}

	/**
	 * Marshals this iCalendar object to its JSON representation (jCal). If the
	 * iCalendar object contains user-defined property or component objects, use
	 * the {@link Biweekly} class instead, in order to register the scribe
	 * classes.
	 * @return the JSON string
	 * @throws IllegalArgumentException if the iCalendar object contains
	 * user-defined property or component objects
	 */
	public String writeJson() {
		return Biweekly.writeJson(this).go();
	}

	/**
	 * Marshals this iCalendar object to its JSON representation (jCal). If the
	 * iCalendar object contains user-defined property or component objects, use
	 * the {@link Biweekly} class instead, in order to register the scribe
	 * classes.
	 * @param file the file to write to
	 * @throws IllegalArgumentException if the iCalendar object contains
	 * user-defined property or component objects
	 * @throws IOException if there's a problem writing to the file
	 */
	public void writeJson(File file) throws IOException {
		Biweekly.writeJson(this).go(file);
	}

	/**
	 * Marshals this iCalendar object to its JSON representation (jCal). If the
	 * iCalendar object contains user-defined property or component objects, use
	 * the {@link Biweekly} class instead, in order to register the scribe
	 * classes.
	 * @param out the data stream to write to
	 * @throws IllegalArgumentException if the iCalendar object contains
	 * user-defined property or component objects
	 * @throws IOException if there's a problem writing to the output stream
	 */
	public void writeJson(OutputStream out) throws IOException {
		Biweekly.writeJson(this).go(out);
	}

	/**
	 * Marshals this iCalendar object to its JSON representation (jCal). If the
	 * iCalendar object contains user-defined property or component objects, use
	 * the {@link Biweekly} class instead, in order to register the scribe
	 * classes.
	 * @param writer the data stream to write to
	 * @throws IllegalArgumentException if the iCalendar object contains
	 * user-defined property or component objects
	 * @throws IOException if there's a problem writing to the writer
	 */
	public void writeJson(Writer writer) throws IOException {
		Biweekly.writeJson(this).go(writer);
	}
}
