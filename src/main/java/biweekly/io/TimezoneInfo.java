package biweekly.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import biweekly.Messages;
import biweekly.component.VTimezone;
import biweekly.property.ICalProperty;
import biweekly.property.TimezoneId;

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
 * Holds the timezone-related settings of an iCalendar object.
 * @author Michael Angstadt
 */
public class TimezoneInfo {
	private final Map<VTimezone, TimeZone> assignments = new IdentityHashMap<VTimezone, TimeZone>();
	private final Map<TimeZone, VTimezone> assignmentsReverse = new HashMap<TimeZone, VTimezone>();
	private final Map<String, TimeZone> timezonesById = new HashMap<String, TimeZone>();

	private final Map<ICalProperty, TimeZone> propertyTimeZones = new IdentityHashMap<ICalProperty, TimeZone>();
	private final List<ICalProperty> hasSolidusTimezone = new ArrayList<ICalProperty>();
	private final List<ICalProperty> floatingProperties = new ArrayList<ICalProperty>();

	private VTimezoneGenerator generator = new TzUrlDotOrgGenerator(false);

	private TimeZone defaultTimezone;
	private boolean globalFloatingTime = false;

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
		timezonesById.put(component.getTimezoneId().getValue(), timezone);
	}

	/**
	 * Sets the timezone to format all date/time values in. An attempt will be
	 * made to generate a {@link VTimezone} component if one has not already
	 * been assigned to the given timezone (see
	 * {@link #assign(VTimezone, TimeZone) assign()}).
	 * @param timezone the timezone object or null for UTC (default)
	 * @throws IllegalArgumentException if a {@link VTimezone} component could
	 * not be generated
	 */
	public void setDefaultTimeZone(TimeZone timezone) {
		if (timezone == null) {
			defaultTimezone = null;
			return;
		}

		VTimezone component = assignmentsReverse.get(timezone);
		if (component == null) {
			component = generator.generate(timezone);
			assign(component, timezone);
		}
		defaultTimezone = timezone;
	}

	/**
	 * Sets whether to format all date/time values as floating times. A floating
	 * time value does not have a timezone associated with it, and is to be
	 * interpreted as being in the local timezone of the parsing computer.
	 * @param enable true to enable, false to disable (default)
	 */
	public void setGlobalFloatingTime(boolean enable) {
		globalFloatingTime = enable;
	}

	/**
	 * Instructs the writer to format an individual property's date/time value
	 * in a specific timezone.
	 * @param property the property
	 * @param timezone the timezone or null to format the property according to
	 * the default timezone (default)
	 */
	public void setTimeZone(ICalProperty property, TimeZone timezone) {
		setTimeZone(property, timezone, true);
	}

	/**
	 * Instructs the writer to format an individual property's date/time value
	 * in a specific timezone.
	 * @param property the property
	 * @param timezone the timezone or null to format the property according to
	 * the default timezone (default)
	 * @param generateComponent true to associate the property with a
	 * {@link VTimezone} component containing the timezone definition
	 * (recommended), false not to. If the "timezone" parameter is null, then
	 * this parameter has no effect
	 */
	public void setTimeZone(ICalProperty property, TimeZone timezone, boolean generateComponent) {
		if (timezone == null) {
			propertyTimeZones.remove(property);
			removeIdentity(hasSolidusTimezone, property);
			return;
		}

		if (generateComponent) {
			VTimezone component = assignmentsReverse.get(timezone);
			if (component == null) {
				component = generator.generate(timezone);
				assign(component, timezone);
			}
		} else {
			hasSolidusTimezone.add(property);
		}

		propertyTimeZones.put(property, timezone);
	}

	/* package */void setTimeZoneReader(ICalProperty property, TimeZone timezone, boolean solidus) {
		if (solidus) {
			hasSolidusTimezone.add(property);
		}
		propertyTimeZones.put(property, timezone);
	}

	/**
	 * Gets the timezone that is assigned to a property.
	 * @param property the property
	 * @return the timezone or null if no timezone is assigned to the property
	 */
	public TimeZone getTimeZone(ICalProperty property) {
		return propertyTimeZones.get(property);
	}

	/**
	 * Determines if the given property has a solidus timezone (a globally
	 * unique timezone ID).
	 * @param property the property
	 * @return true if the property has a solidus timezone, false if not
	 */
	public boolean hasSolidusTimezone(ICalProperty property) {
		return containsIdentity(hasSolidusTimezone, property);
	}

	/**
	 * Gets the timezone that a property should be formatted in when written.
	 * You should call {@link #isFloating} first, to check to see if the
	 * property's value is floating (without a timezone).
	 * @param property the property
	 * @return the timezone or null for UTC
	 */
	public TimeZone getTimeZoneToWriteIn(ICalProperty property) {
		TimeZone timezone = getTimeZone(property);
		return (timezone == null) ? defaultTimezone : timezone;
	}

	/**
	 * Gets a timezone with a given ID.
	 * @param id the ID
	 * @return the timezone or null if not found
	 */
	public TimeZone getTimeZoneById(String id) {
		return timezonesById.get(id);
	}

	/**
	 * Gets the {@link TimeZone} object that is assigned to a {@link VTimezone}
	 * component.
	 * @param component the component
	 * @return the timezone object or null if none were found
	 */
	public TimeZone getTimeZoneByComponent(VTimezone component) {
		return assignments.get(component);
	}

	/**
	 * Gets the {@link VTimezone} component that a property is assigned to.
	 * @param property the property
	 * @return the component or null if it is not assigned to one
	 */
	public VTimezone getComponent(ICalProperty property) {
		if (containsIdentity(hasSolidusTimezone, property)) {
			return null;
		}

		TimeZone timezone = getTimeZone(property);
		return assignmentsReverse.get(timezone);
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
	public void setFloating(ICalProperty property, boolean enable) {
		if (enable) {
			floatingProperties.add(property);
		} else {
			removeIdentity(floatingProperties, property);
		}
	}

	/**
	 * Determines if a property value should be formatted in floating time or
	 * not.
	 * @param property the property
	 * @return true to format in floating time, false not to
	 */
	public boolean isFloating(ICalProperty property) {
		if (containsIdentity(floatingProperties, property)) {
			return true;
		}

		if (propertyTimeZones.containsKey(property)) {
			return false;
		}

		return globalFloatingTime;
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
	 * Gets the timezone generator.
	 * @return the timezone generator
	 */
	public VTimezoneGenerator getGenerator() {
		return generator;
	}

	/**
	 * Sets the timezone generator.
	 * @param generator the timezone generator
	 */
	public void setGenerator(VTimezoneGenerator generator) {
		this.generator = generator;
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
			throw Messages.INSTANCE.getIllegalArgumentException(14);
		}
	}

	private <T> void removeIdentity(List<T> list, T object) {
		Iterator<T> it = list.iterator();
		while (it.hasNext()) {
			if (object == it.next()) {
				it.remove();
			}
		}
	}

	private <T> boolean containsIdentity(List<T> list, T object) {
		for (T item : list) {
			if (item == object) {
				return true;
			}
		}
		return false;
	}
}