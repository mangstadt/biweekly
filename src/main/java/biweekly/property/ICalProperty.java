package biweekly.property;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.parameter.ICalParameters;

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
 * Represents an iCalendar property (such as "VERSION").
 * @author Michael Angstadt
 */
public abstract class ICalProperty {
	/**
	 * The property parameters.
	 */
	protected ICalParameters parameters;

	public ICalProperty() {
		this(new ICalParameters());
	}

	public ICalProperty(ICalParameters parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets the property's parameters.
	 * @return the parameters
	 */
	public ICalParameters getParameters() {
		return parameters;
	}

	/**
	 * Sets the property's parameters
	 * @param parameters the parameters
	 */
	public void setParameters(ICalParameters parameters) {
		this.parameters = parameters;
	}

	/**
	 * Gets the first value of the parameter with the given name.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @return the parameter value or null if not found
	 */
	public String getParameter(String name) {
		return parameters.first(name);
	}

	/**
	 * Gets all values of the parameter with the given name.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @return the parameter values
	 */
	public List<String> getParameters(String name) {
		return parameters.get(name);
	}

	/**
	 * Adds a value to a parameter.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @param value the parameter value
	 */
	public void addParameter(String name, String value) {
		parameters.put(name, value);
	}

	/**
	 * Sets a parameter's value, replacing all pre-existing values.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @param value the parameter value
	 */
	public void setParameter(String name, String value) {
		parameters.replace(name, value);
	}

	/**
	 * Sets a parameter's values, replacing all pre-existing values.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @param values the parameter values
	 */
	public void setParameter(String name, Collection<String> values) {
		parameters.replace(name, values);
	}

	/**
	 * Gets a URI pointing to additional information about the entity
	 * represented by the property.
	 * @return the URI or null if not set
	 * @see "RFC 5545 p.14-5"
	 */
	protected String getAltRepresentation() {
		return parameters.getAltRepresentation();
	}

	/**
	 * Sets a URI pointing to additional information about the entity
	 * represented by the property.
	 * @param uri the URI or null to remove
	 * @see "RFC 5545 p.14-5"
	 */
	protected void setAltRepresentation(String uri) {
		parameters.setAltRepresentation(uri);
	}

	/**
	 * Gets the content type of the property's value.
	 * @return the content type of the property's value (e.g. "image/png") or
	 * null if not set
	 * @see "RFC 5545 p.19-20"
	 */
	protected String getFormatType() {
		return parameters.getFormatType();
	}

	/**
	 * Sets the content type of the property's value.
	 * @param formatType the content type of the property's value (e.g.
	 * "image/png") or null to remove
	 * @see "RFC 5545 p.19-20"
	 */
	protected void setFormatType(String formatType) {
		parameters.setFormatType(formatType);
	}

	/**
	 * Gets the language that the property value is written in.
	 * @return the language (e.g. "en" for English) or null if not set
	 * @see "RFC 5545 p.21"
	 */
	protected String getLanguage() {
		return parameters.getLanguage();
	}

	/**
	 * Sets the language that the property value is written in.
	 * @param language the language (e.g. "en" for English) or null to remove
	 * @see "RFC 5545 p.21"
	 */
	protected void setLanguage(String language) {
		parameters.setLanguage(language);
	}

	/**
	 * Gets the timezone identifier. This either (a) references the
	 * {@link TimezoneId} property of a {@link VTimezone} component, or (b)
	 * specifies a globally-defined timezone (e.g. "America/New_York"). For a
	 * list of globally-defined timezones, see the <a
	 * href="http://www.twinsun.com/tz/tz-link.htm">TZ database</a>.
	 * @return the timezone identifier or null if not set
	 * @see "RFC 5545 p.27-8"
	 */
	protected String getTimezoneId() {
		return parameters.getTimezoneId();
	}

	/**
	 * Sets the timezone in the form of a globally-defined timezone (e.g.
	 * "America/New_York"). For a list of globally-defined timezones, see the <a
	 * href="http://www.twinsun.com/tz/tz-link.htm">TZ database</a>. Use
	 * {@link #setTimezone(VTimezone)} to use a timezone that's defined in the
	 * iCalendar object.
	 * @param timezoneId the timezone identifier (e.g. "America/New_York") or
	 * null to remove
	 * @see "RFC 5545 p.27-8"
	 */
	protected void setTimezoneId(String timezoneId) {
		parameters.setTimezoneId(timezoneId);
	}

	/**
	 * Sets the timezone that this property uses. Use
	 * {@link #setTimezoneId(String)} to set a globally-defined timezone (e.g.
	 * "America/New_York").
	 * @param timezone the timezone component that defines the timezone or null
	 * to remove
	 * @see "RFC 5545 p.27-8"
	 */
	protected void setTimezone(VTimezone timezone) {
		if (timezone == null) {
			setTimezoneId(null);
			return;
		}

		TimezoneId tzid = timezone.getTimezoneId();
		if (tzid != null) {
			setTimezoneId(tzid.getValue());
		}
	}

	/**
	 * Gets a person that is acting on behalf of the person defined in the
	 * property.
	 * @return a URI representing the person (typically, an email URI, e.g.
	 * "mailto:janedoe@example.com") or null if not set
	 * @see "RFC 5545 p.27"
	 */
	protected String getSentBy() {
		return parameters.getSentBy();
	}

	/**
	 * Sets a person that is acting on behalf of the person defined in the
	 * property.
	 * @param uri a URI representing the person (typically, an email URI, e.g.
	 * "mailto:janedoe@example.com") or null to remove
	 * @see "RFC 5545 p.27"
	 */
	protected void setSentBy(String uri) {
		parameters.setSentBy(uri);
	}

	/**
	 * Gets the display name of the person.
	 * @return the display name (e.g. "John Doe") or null if not set
	 * @see "RFC 5545 p.15-6"
	 */
	protected String getCommonName() {
		return parameters.getCommonName();
	}

	/**
	 * Sets the display name of the person.
	 * @param commonName the display name (e.g. "John Doe") or null to remove
	 * @see "RFC 5545 p.15-6"
	 */
	protected void setCommonName(String commonName) {
		parameters.setCommonName(commonName);
	}

	/**
	 * Gets a URI that contains additional information about the person.
	 * @return the URI (e.g. an LDAP URI) or null if not set
	 * @see "RFC 5545 p.18"
	 */
	protected String getDirectoryEntry() {
		return parameters.getDirectoryEntry();
	}

	/**
	 * Sets a URI that contains additional information about the person.
	 * @param uri the URI (e.g. an LDAP URI) or null to remove
	 * @see "RFC 5545 p.18"
	 */
	protected void setDirectoryEntry(String uri) {
		parameters.setDirectoryEntry(uri);
	}

	/**
	 * Checks the property for data consistency problems or deviations from the
	 * spec. These problems will not prevent the property from being written to
	 * a data stream, but may prevent it from being parsed correctly by the
	 * consuming application. These problems can largely be avoided by reading
	 * the Javadocs of the property class, or by being familiar with the
	 * iCalendar standard.
	 * @param components the hierarchy of components that the property belongs
	 * to
	 * @see ICalendar#validate
	 * @return a list of warnings or an empty list if no problems were found
	 */
	public final List<String> validate(List<ICalComponent> components) {
		List<String> warnings = new ArrayList<String>(0);
		validate(components, warnings);
		return warnings;
	}

	/**
	 * Checks the property for data consistency problems or deviations from the
	 * spec. Meant to be overridden by child classes.
	 * @param components the hierarchy of components that the property belongs
	 * to
	 * @param warnings the list to add the warnings to
	 */
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		//do nothing
	}
}
