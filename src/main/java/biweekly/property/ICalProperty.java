package biweekly.property;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.parameter.ICalParameters;

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
 * Base class for all iCalendar properties.
 * @author Michael Angstadt
 */
public abstract class ICalProperty {
	/**
	 * The property parameters.
	 */
	protected ICalParameters parameters;

	public ICalProperty() {
		parameters = new ICalParameters();
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	protected ICalProperty(ICalProperty original) {
		parameters = new ICalParameters(original.parameters);
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
	 * Gets the first value of a parameter with the given name.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @return the parameter value or null if not found
	 */
	public String getParameter(String name) {
		return parameters.first(name);
	}

	/**
	 * Gets all values of a parameter with the given name.
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
	 * Replaces all existing values of a parameter with the given value.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @param value the parameter value
	 */
	public void setParameter(String name, String value) {
		parameters.replace(name, value);
	}

	/**
	 * Replaces all existing values of a parameter with the given values.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 * @param values the parameter values
	 */
	public void setParameter(String name, Collection<String> values) {
		parameters.replace(name, values);
	}

	/**
	 * Removes a parameter from the property.
	 * @param name the parameter name (case insensitive, e.g. "LANGUAGE")
	 */
	public void removeParameter(String name) {
		parameters.removeAll(name);
	}

	//Note: The following parameter helper methods are package-scoped to prevent them from cluttering up the Javadocs

	/**
	 * Gets a URI pointing to additional information about the entity
	 * represented by the property.
	 * @return the URI or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-14">RFC 5545
	 * p.14-5</a>
	 */
	String getAltRepresentation() {
		return parameters.getAltRepresentation();
	}

	/**
	 * Sets a URI pointing to additional information about the entity
	 * represented by the property.
	 * @param uri the URI or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-14">RFC 5545
	 * p.14-5</a>
	 */
	void setAltRepresentation(String uri) {
		parameters.setAltRepresentation(uri);
	}

	/**
	 * Gets the content-type of the property's value.
	 * @return the content type (e.g. "image/png") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-19">RFC 5545
	 * p.19-20</a>
	 */
	String getFormatType() {
		return parameters.getFormatType();
	}

	/**
	 * Sets the content-type of the property's value.
	 * @param formatType the content type (e.g. "image/png") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-19">RFC 5545
	 * p.19-20</a>
	 */
	void setFormatType(String formatType) {
		parameters.setFormatType(formatType);
	}

	/**
	 * Gets the language that the property value is written in.
	 * @return the language (e.g. "en" for English) or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21</a>
	 */
	String getLanguage() {
		return parameters.getLanguage();
	}

	/**
	 * Sets the language that the property value is written in.
	 * @param language the language (e.g. "en" for English) or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21</a>
	 */
	void setLanguage(String language) {
		parameters.setLanguage(language);
	}

	/**
	 * <p>
	 * Gets a person that is acting on behalf of the person defined in the
	 * property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @return a URI representing the person (typically, an email URI, e.g.
	 * "mailto:janedoe@example.com") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27</a>
	 */
	String getSentBy() {
		return parameters.getSentBy();
	}

	/**
	 * <p>
	 * Sets a person that is acting on behalf of the person defined in the
	 * property.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @param uri a URI representing the person (typically, an email URI, e.g.
	 * "mailto:janedoe@example.com") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27</a>
	 */
	void setSentBy(String uri) {
		parameters.setSentBy(uri);
	}

	/**
	 * <p>
	 * Gets the display name of the person.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @return the display name (e.g. "John Doe") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-15">RFC 5545
	 * p.15-6</a>
	 */
	String getCommonName() {
		return parameters.getCommonName();
	}

	/**
	 * <p>
	 * Sets the display name of the person.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @param commonName the display name (e.g. "John Doe") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-15">RFC 5545
	 * p.15-6</a>
	 */
	void setCommonName(String commonName) {
		parameters.setCommonName(commonName);
	}

	/**
	 * <p>
	 * Gets a URI that contains additional information about the person.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @return the URI (e.g. an LDAP URI) or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18</a>
	 */
	String getDirectoryEntry() {
		return parameters.getDirectoryEntry();
	}

	/**
	 * <p>
	 * Sets a URI that contains additional information about the person.
	 * </p>
	 * <p>
	 * <b>Supported versions:</b> {@code 2.0}
	 * </p>
	 * @param uri the URI (e.g. an LDAP URI) or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18</a>
	 */
	void setDirectoryEntry(String uri) {
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
	 * @param version the version to validate against
	 * @see ICalendar#validate
	 * @return a list of warnings or an empty list if no problems were found
	 */
	public final List<Warning> validate(List<ICalComponent> components, ICalVersion version) {
		//validate property value
		List<Warning> warnings = new ArrayList<Warning>(0);
		validate(components, version, warnings);

		//validate parameters
		warnings.addAll(parameters.validate(version));

		return warnings;
	}

	/**
	 * <p>
	 * Checks the property for data consistency problems or deviations from the
	 * spec.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to provide
	 * validation logic. The default implementation of this method does nothing.
	 * </p>
	 * @param components the hierarchy of components that the property belongs
	 * to
	 * @param version the version to validate against
	 * @param warnings the list to add the warnings to
	 */
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		//do nothing
	}

	/**
	 * <p>
	 * Gets string representations of the class's fields for the
	 * {@link #toString} method.
	 * </p>
	 * <p>
	 * Meant to be overridden by child classes. The default implementation
	 * returns an empty map.
	 * </p>
	 * @return the values of the class's fields (key = field name, value = field
	 * value)
	 */
	protected Map<String, Object> toStringValues() {
		return Collections.emptyMap();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getName());
		sb.append(" [parameters=").append(parameters);
		for (Map.Entry<String, Object> field : toStringValues().entrySet()) {
			String fieldName = field.getKey();
			Object fieldValue = field.getValue();
			sb.append(" | ").append(fieldName).append('=').append(fieldValue);
		}
		sb.append(']');
		return sb.toString();
	}

	/**
	 * <p>
	 * Creates a copy of this property object.
	 * </p>
	 * <p>
	 * The default implementation of this method uses reflection to look for a
	 * copy constructor. Child classes should override this method to avoid the
	 * performance overhead involved in using reflection.
	 * </p>
	 * <p>
	 * The child class's copy constructor, if present, MUST invoke the
	 * {@link #ICalProperty(ICalProperty)} super constructor to ensure that the
	 * parameters are also copied.
	 * </p>
	 * <p>
	 * This method MUST be overridden by the child class if the child class does
	 * not have a copy constructor. Otherwise, an
	 * {@link UnsupportedOperationException} will be thrown when an attempt is
	 * made to copy the property (such as in the
	 * {@link ICalendar#ICalendar(ICalendar) ICalendar class's copy constructor}
	 * ).
	 * </p>
	 * @return the copy
	 * @throws UnsupportedOperationException if the class does not have a copy
	 * constructor or there is a problem invoking it
	 */
	public ICalProperty copy() {
		Class<? extends ICalProperty> clazz = getClass();

		try {
			Constructor<? extends ICalProperty> copyConstructor = clazz.getConstructor(clazz);
			return copyConstructor.newInstance(this);
		} catch (Exception e) {
			throw new UnsupportedOperationException("A problem occurred attempting to invoke the copy constructor of property class " + clazz.getName() + ".", e);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parameters == null) ? 0 : parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ICalProperty other = (ICalProperty) obj;
		if (parameters == null) {
			if (other.parameters != null) return false;
		} else if (!parameters.equals(other.parameters)) return false;
		return true;
	}
}
