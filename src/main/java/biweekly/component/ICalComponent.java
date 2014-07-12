package biweekly.component;

import java.util.ArrayList;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.ValidationWarnings.WarningsGroup;
import biweekly.Warning;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.Status;
import biweekly.util.ListMultimap;

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
 * Base class for all iCalendar components.
 * @author Michael Angstadt
 */
public abstract class ICalComponent {
	protected final ListMultimap<Class<? extends ICalComponent>, ICalComponent> components = new ListMultimap<Class<? extends ICalComponent>, ICalComponent>();
	protected final ListMultimap<Class<? extends ICalProperty>, ICalProperty> properties = new ListMultimap<Class<? extends ICalProperty>, ICalProperty>();

	/**
	 * Gets the first property of a given class.
	 * @param clazz the property class
	 * @return the property or null if not found
	 */
	public <T extends ICalProperty> T getProperty(Class<T> clazz) {
		return clazz.cast(properties.first(clazz));
	}

	/**
	 * Gets all properties of a given class.
	 * @param clazz the property class
	 * @return the properties
	 */
	public <T extends ICalProperty> List<T> getProperties(Class<T> clazz) {
		List<ICalProperty> props = properties.get(clazz);

		//cast to the requested class
		List<T> ret = new ArrayList<T>(props.size());
		for (ICalProperty property : props) {
			ret.add(clazz.cast(property));
		}
		return ret;
	}

	/**
	 * Gets all the properties associated with this component.
	 * @return the properties
	 */
	public ListMultimap<Class<? extends ICalProperty>, ICalProperty> getProperties() {
		return properties;
	}

	/**
	 * Adds a property to this component.
	 * @param property the property to add
	 */
	public void addProperty(ICalProperty property) {
		properties.put(property.getClass(), property);
	}

	/**
	 * Replaces all existing properties of the given class with a single
	 * property instance.
	 * @param property the property (must not be null)
	 */
	public void setProperty(ICalProperty property) {
		properties.replace(property.getClass(), property);
	}

	/**
	 * Replaces all existing properties of the given class with a single
	 * property instance. If the property instance is null, then all instances
	 * of that property will be removed.
	 * @param clazz the property class (e.g. "DateStart.class")
	 * @param property the property or null to remove
	 */
	public <T extends ICalProperty> void setProperty(Class<T> clazz, T property) {
		properties.replace(clazz, property);
	}

	/**
	 * Removes properties from the iCalendar object.
	 * @param clazz the class of the properties to remove (e.g.
	 * "DateStart.class")
	 */
	public void removeProperties(Class<? extends ICalProperty> clazz) {
		properties.removeAll(clazz);
	}

	/**
	 * Gets the first experimental property with a given name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @return the property or null if none were found
	 */
	public RawProperty getExperimentalProperty(String name) {
		for (RawProperty raw : getProperties(RawProperty.class)) {
			if (raw.getName().equalsIgnoreCase(name)) {
				return raw;
			}
		}
		return null;
	}

	/**
	 * Gets all experimental properties with a given name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @return the properties
	 */
	public List<RawProperty> getExperimentalProperties(String name) {
		List<RawProperty> props = new ArrayList<RawProperty>();

		for (RawProperty raw : getProperties(RawProperty.class)) {
			if (raw.getName().equalsIgnoreCase(name)) {
				props.add(raw);
			}
		}

		return props;
	}

	/**
	 * Gets all experimental properties associated with this component.
	 * @return the properties
	 */
	public List<RawProperty> getExperimentalProperties() {
		return getProperties(RawProperty.class);
	}

	/**
	 * Adds an experimental property to this component.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty addExperimentalProperty(String name, String value) {
		return addExperimentalProperty(name, null, value);
	}

	/**
	 * Adds an experimental property to this component.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param dataType the property's data type (e.g. "text") or null if unknown
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty addExperimentalProperty(String name, ICalDataType dataType, String value) {
		RawProperty raw = new RawProperty(name, dataType, value);
		addProperty(raw);
		return raw;
	}

	/**
	 * Adds an experimental property to this component, removing all existing
	 * properties that have the same name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty setExperimentalProperty(String name, String value) {
		return setExperimentalProperty(name, null, value);
	}

	/**
	 * Adds an experimental property to this component, removing all existing
	 * properties that have the same name.
	 * @param name the property name (e.g. "X-ALT-DESC")
	 * @param dataType the property's data type (e.g. "text") or null if unknown
	 * @param value the property value
	 * @return the property object that was created
	 */
	public RawProperty setExperimentalProperty(String name, ICalDataType dataType, String value) {
		removeExperimentalProperty(name);
		return addExperimentalProperty(name, dataType, value);
	}

	/**
	 * Removes all experimental properties that have the given name.
	 * @param name the component name (e.g. "X-ALT-DESC")
	 */
	public void removeExperimentalProperty(String name) {
		List<RawProperty> xproperties = getExperimentalProperties(name);
		for (RawProperty xproperty : xproperties) {
			properties.remove(xproperty.getClass(), xproperty);
		}
	}

	/**
	 * Gets the first component of a given class.
	 * @param clazz the component class
	 * @return the component or null if not found
	 */
	public <T extends ICalComponent> T getComponent(Class<T> clazz) {
		return clazz.cast(components.first(clazz));
	}

	/**
	 * Gets all components of a given class.
	 * @param clazz the component class
	 * @return the components
	 */
	public <T extends ICalComponent> List<T> getComponents(Class<T> clazz) {
		List<ICalComponent> comp = components.get(clazz);

		//cast to the requested class
		List<T> ret = new ArrayList<T>(comp.size());
		for (ICalComponent property : comp) {
			ret.add(clazz.cast(property));
		}
		return ret;
	}

	/**
	 * Gets all the sub-components associated with this component.
	 * @return the sub-components
	 */
	public ListMultimap<Class<? extends ICalComponent>, ICalComponent> getComponents() {
		return components;
	}

	/**
	 * Adds a sub-component to this component.
	 * @param component the component to add
	 */
	public void addComponent(ICalComponent component) {
		components.put(component.getClass(), component);
	}

	/**
	 * Replaces all components of a given class with the given component.
	 * @param component the component (must not be null)
	 */
	public void setComponent(ICalComponent component) {
		components.replace(component.getClass(), component);
	}

	/**
	 * Replaces all components of a given class with the given component. If the
	 * component instance is null, then all instances of that component will be
	 * removed.
	 * @param clazz the component's class
	 * @param component the component or null to remove
	 */
	public <T extends ICalComponent> void setComponent(Class<T> clazz, T component) {
		components.replace(clazz, component);
	}

	/**
	 * Gets the first experimental sub-component with a given name.
	 * @param name the component name (e.g. "X-PARTY")
	 * @return the component or null if none were found
	 */
	public RawComponent getExperimentalComponent(String name) {
		for (RawComponent raw : getComponents(RawComponent.class)) {
			if (raw.getName().equalsIgnoreCase(name)) {
				return raw;
			}
		}
		return null;
	}

	/**
	 * Gets all experimental sub-component with a given name.
	 * @param name the component name (e.g. "X-PARTY")
	 * @return the components
	 */
	public List<RawComponent> getExperimentalComponents(String name) {
		List<RawComponent> props = new ArrayList<RawComponent>();

		for (RawComponent raw : getComponents(RawComponent.class)) {
			if (raw.getName().equalsIgnoreCase(name)) {
				props.add(raw);
			}
		}

		return props;
	}

	/**
	 * Gets all experimental sub-components associated with this component.
	 * @return the sub-components
	 */
	public List<RawComponent> getExperimentalComponents() {
		return getComponents(RawComponent.class);
	}

	/**
	 * Adds an experimental sub-component to this component.
	 * @param name the component name (e.g. "X-PARTY")
	 * @return the component object that was created
	 */
	public RawComponent addExperimentalComponent(String name) {
		RawComponent raw = new RawComponent(name);
		addComponent(raw);
		return raw;
	}

	/**
	 * Adds an experimental sub-component to this component, removing all
	 * existing components that have the same name.
	 * @param name the component name (e.g. "X-PARTY")
	 * @return the component object that was created
	 */
	public RawComponent setExperimentalComponents(String name) {
		removeExperimentalComponents(name);
		return addExperimentalComponent(name);
	}

	/**
	 * Removes all experimental sub-components that have the given name.
	 * @param name the component name (e.g. "X-PARTY")
	 */
	public void removeExperimentalComponents(String name) {
		List<RawComponent> xcomponents = getExperimentalComponents(name);
		for (RawComponent xcomponent : xcomponents) {
			components.remove(xcomponent.getClass(), xcomponent);
		}
	}

	/**
	 * Checks the component for data consistency problems or deviations from the
	 * spec. These problems will not prevent the component from being written to
	 * a data stream, but may prevent it from being parsed correctly by the
	 * consuming application. These problems can largely be avoided by reading
	 * the Javadocs of the component class, or by being familiar with the
	 * iCalendar standard.
	 * @param hierarchy the hierarchy of components that the component belongs
	 * to
	 * @param version the version to validate against
	 * @see ICalendar#validate
	 * @return a list of warnings or an empty list if no problems were found
	 */
	public final List<WarningsGroup> validate(List<ICalComponent> hierarchy, ICalVersion version) {
		List<WarningsGroup> warnings = new ArrayList<WarningsGroup>();

		//validate this component
		List<Warning> warningsBuf = new ArrayList<Warning>(0);
		validate(hierarchy, version, warningsBuf);
		if (!warningsBuf.isEmpty()) {
			warnings.add(new WarningsGroup(this, hierarchy, warningsBuf));
		}

		//add this component to the hierarchy list
		//copy the list so other validate() calls aren't effected
		hierarchy = new ArrayList<ICalComponent>(hierarchy);
		hierarchy.add(this);

		//validate properties
		for (ICalProperty property : properties.values()) {
			List<Warning> propWarnings = property.validate(hierarchy, version);
			if (!propWarnings.isEmpty()) {
				warnings.add(new WarningsGroup(property, hierarchy, propWarnings));
			}
		}

		//validate sub-components
		for (ICalComponent component : components.values()) {
			warnings.addAll(component.validate(hierarchy, version));
		}

		return warnings;
	}

	/**
	 * <p>
	 * Checks the component for data consistency problems or deviations from the
	 * spec.
	 * </p>
	 * <p>
	 * This method should be overridden by child classes that wish to provide
	 * validation logic. The default implementation of this method does nothing.
	 * </p>
	 * @param components the hierarchy of components that the component belongs
	 * to
	 * @param version the version to validate against
	 * @param warnings the list to add the warnings to
	 */
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		//do nothing
	}

	/**
	 * Utility method for validating that there is exactly one instance of each
	 * of the given properties.
	 * @param warnings the list to add the warnings to
	 * @param classes the properties to check
	 */
	protected void checkRequiredCardinality(List<Warning> warnings, Class<? extends ICalProperty>... classes) {
		for (Class<? extends ICalProperty> clazz : classes) {
			List<? extends ICalProperty> props = getProperties(clazz);

			if (props.isEmpty()) {
				warnings.add(Warning.validate(2, clazz.getSimpleName()));
				continue;
			}

			if (props.size() > 1) {
				warnings.add(Warning.validate(3, clazz.getSimpleName()));
				continue;
			}
		}
	}

	/**
	 * Utility method for validating that there is no more than one instance of
	 * each of the given properties.
	 * @param warnings the list to add the warnings to
	 * @param classes the properties to check
	 */
	protected void checkOptionalCardinality(List<Warning> warnings, Class<? extends ICalProperty>... classes) {
		for (Class<? extends ICalProperty> clazz : classes) {
			List<? extends ICalProperty> props = getProperties(clazz);

			if (props.size() > 1) {
				warnings.add(Warning.validate(3, clazz.getSimpleName()));
				continue;
			}
		}
	}

	/**
	 * Utility method for validating the {@link Status} property of a component.
	 * @param warnings the list to add the warnings to
	 * @param allowed the valid statuses
	 */
	protected void checkStatus(List<Warning> warnings, Status... allowed) {
		Status actual = getProperty(Status.class);
		if (actual == null) {
			return;
		}

		List<String> allowedValues = new ArrayList<String>(allowed.length);
		for (Status status : allowed) {
			String value = status.getValue().toLowerCase();
			allowedValues.add(value);
		}

		String actualValue = actual.getValue().toLowerCase();
		if (!allowedValues.contains(actualValue)) {
			warnings.add(Warning.validate(13, actual.getValue(), allowedValues));
		}
	}
}
