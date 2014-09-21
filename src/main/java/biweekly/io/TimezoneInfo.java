package biweekly.io;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import biweekly.component.VTimezone;
import biweekly.property.ICalProperty;
import biweekly.property.TimezoneId;

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
 * Holds the timezone-related settings of an iCalendar object.
 * @author Michael Angstadt
 */
public class TimezoneInfo {
	private final Map<VTimezone, TimeZone> assignments = new HashMap<VTimezone, TimeZone>();
	private final Map<TimeZone, VTimezone> assignmentsReverse = new HashMap<TimeZone, VTimezone>();
	private final Map<ICalProperty, VTimezone> propertyTimezones = new HashMap<ICalProperty, VTimezone>();
	private final Map<ICalProperty, TimeZone> propertyTimeZones = new HashMap<ICalProperty, TimeZone>();
	private final Set<ICalProperty> floatingProperties = new HashSet<ICalProperty>();
	private TimezoneTranslator translator = new TzUrlDotOrgTranslator(false);

	private TimeZone defaultTimezone;
	private VTimezone defaultTimezoneComponent;
	private boolean useFloatingTime = false;

	/**
	 * Assigns a user-defined {@link VTimezone} component to its Java
	 * {@link TimeZone} equivalent.
	 * @param component the timezone component
	 * @param timezone the timezone object
	 */
	public void assign(VTimezone component, TimeZone timezone) {
		checkForId(component);
		assignments.put(component, timezone);
		assignmentsReverse.put(timezone, component);
	}

	/**
	 * Sets the timezone to format all date/time values in. An attempt will be
	 * made to generate a {@link VTimezone} component if one has not already
	 * been assigned to the given timezone (see
	 * {@link #assign(VTimezone, TimeZone) assign()}).
	 * @param timezone the timezone object or null for UTC (default)
	 * @throw IllegalArgumentException if there is no iCalendar timezone
	 * component associated with the timezone and one could not be generated
	 */
	public void setDefaultTimezone(TimeZone timezone) {
		VTimezone component = null;
		if (timezone != null) {
			component = assignmentsReverse.get(timezone);
			if (component == null) {
				component = translator.toICalVTimezone(timezone);
				assign(component, timezone);
			}
		}

		defaultTimezone = timezone;
		defaultTimezoneComponent = component;
	}

	/**
	 * Sets the timezone to format all date/time values in. An attempy will be
	 * made to generate a Java {@link TimeZone} object if one has not already
	 * been asigned to the given timezone component (see
	 * {@link #assign(VTimezone, TimeZone) assign()}).
	 * @param component the timezone component or null for UTC (default)
	 * @throw IllegalArgumentException if there is no Java timezone object
	 * associated with the timezone component and one could not be generated, or
	 * the timezone component does not have a valid {@link TimezoneId} property
	 */
	public void setDefaultTimezone(VTimezone component) {
		if (component == null) {
			setDefaultTimezone((TimeZone) null);
			return;
		}

		checkForId(component);

		TimeZone timezone = assignments.get(component);
		if (timezone == null) {
			timezone = new ICalTimeZone(component);
			assign(component, timezone);
		}

		defaultTimezone = timezone;
		defaultTimezoneComponent = component;
	}

	/**
	 * Sets whether to format all date/time values as floating times. A floating
	 * time value does not have a timezone associated with it, and is to be
	 * interpreted as being in the local timezone of the parsing computer.
	 * @param enable true to enable, false to disable (default)
	 */
	public void setUseFloatingTime(boolean enable) {
		useFloatingTime = enable;
	}

	/**
	 * Instructs the writer to format a particular property's date/time value in
	 * a specific timezone.
	 * @param property the property
	 * @param timezone the timezone or null to format the property according to
	 * the default timezone (default)
	 */
	public void setTimezone(ICalProperty property, TimeZone timezone) {
		if (timezone == null) {
			propertyTimeZones.remove(property);
			return;
		}

		propertyTimeZones.put(property, timezone);
	}

	/**
	 * Instructs the writer to format a particular property's date/time value in
	 * a specific timezone.
	 * @param property the property
	 * @param component the timezone component or null to format the property
	 * according to the default timezone (default)
	 * @throws IllegalArgumentException if the given VTIMEZONE component has not
	 * been associated with a Java {@link TimeZone} object (see
	 * {@link #assign(VTimezone, TimeZone)})
	 */
	public void setTimezone(ICalProperty property, VTimezone component) {
		if (component == null) {
			propertyTimezones.remove(property);
			return;
		}

		checkForId(component); //TODO remove?
		propertyTimezones.put(property, component);
	}

	/**
	 * Gets the timezone to format a property in.
	 * @param property the property
	 * @return the timezone or null for UTC
	 */
	public TimeZone getTimeZone(ICalProperty property) {
		TimeZone tz = propertyTimeZones.get(property);
		return (tz == null) ? defaultTimezone : tz;
	}

	/**
	 * Gets the {@link VTimezone} component that a property is assigned to.
	 * @param property the property
	 * @return the component or null if it is not assigned to one
	 */
	public VTimezone getComponent(ICalProperty property) {
		return propertyTimezones.get(property);
	}

	/**
	 * Gets the ID of the {@link VTimezone} component that the given property is
	 * linked to.
	 * @param property the property
	 * @return the ID (to be used as the value of the TZID parameter) or null
	 * for UTC
	 */
	public String getTimezoneId(ICalProperty property) {
		VTimezone component = getComponent(property);
		if (component == null) {
			component = defaultTimezoneComponent;
		}

		return (component == null) ? null : component.getTimezoneId().getValue();
	}

	/**
	 * Instructs the writer to format a particular property's date/time value in
	 * floating time. A floating time value does not have a timezone associated
	 * with it, and is to be interpreted as being in the local timezone of the
	 * parsing computer.
	 * @param property the property whose value should be formatted as a
	 * floating time value
	 * @param enable true to enable floating time for this property, false to
	 * disable (default)
	 */
	public void setUseFloatingTime(ICalProperty property, boolean enable) {
		if (enable) {
			floatingProperties.add(property);
		} else {
			floatingProperties.remove(property);
		}
	}

	/**
	 * Determines if a property value should be formatted in floating time or
	 * not.
	 * @param property the property
	 * @return true to format in floating time, false not to
	 */
	public boolean usesFloatingTime(ICalProperty property) {
		if (floatingProperties.contains(property)) {
			return true;
		}

		if (propertyTimezones.containsKey(property)) {
			return false;
		}

		return useFloatingTime;
	}

	/**
	 * Gets all of the iCalendar {@link VTimezone} components that have been
	 * registered or generated by this class.
	 * @return the timezone components
	 */
	public Collection<VTimezone> getComponents() {
		return assignments.keySet();
	}

	/**
	 * Gets the timezone translator, which is responsible for converting
	 * {@link VTimezone} components to Java {@link TimeZone} objects and vice
	 * versa.
	 * @return the timezone translator
	 */
	public TimezoneTranslator getTranslator() {
		return translator;
	}

	/**
	 * Sets the timezone translator, which is responsible for converting
	 * {@link VTimezone} components to Java {@link TimeZone} objects and vice
	 * versa.
	 * @param translator the timezone translator
	 */
	public void setTranslator(TimezoneTranslator translator) {
		this.translator = translator;
	}

	/**
	 * Checks a {@link VTimezone} component to see if it has a valid
	 * {@link TimezoneId} property.
	 * @param timezone the timezone component
	 * @throws IllegalArgumentException if the component does not have a valid
	 * {@link TimezoneId} property
	 */
	private void checkForId(VTimezone timezone) {
		TimezoneId id = timezone.getTimezoneId();
		if (id == null || id.getValue() == null || id.getValue().trim().length() == 0) {
			throw new IllegalArgumentException("VTimezone component must have a non-empty TimezoneId property");
		}
	}
}