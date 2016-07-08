package biweekly.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.Messages;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.component.VTimezone;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.TimezoneId;
import biweekly.property.ValuedProperty;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
 * Writes iCalendar objects to a data stream.
 * @author Michael Angstadt
 */
public abstract class StreamWriter implements Closeable {
	protected ScribeIndex index = new ScribeIndex();
	protected WriteContext context;
	protected TimeZone globalTimeZone;
	protected VTimezone globalTimeZoneComponent;
	private TimezoneInfo tzinfo;

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
		Collection<Class<?>> unregistered = findScribeless(ical);
		if (!unregistered.isEmpty()) {
			List<String> classNames = new ArrayList<String>(unregistered.size());
			for (Class<?> clazz : unregistered) {
				classNames.add(clazz.getName());
			}
			throw Messages.INSTANCE.getIllegalArgumentException(13, classNames);
		}

		tzinfo = ical.getTimezoneInfo();
		context = new WriteContext(getTargetVersion(), tzinfo, globalTimeZone);
		_write(ical);
	}

	/**
	 * Gets the {@link VTimezone} components that need to be written to the
	 * output stream.
	 * @return the components
	 */
	protected Collection<VTimezone> getTimezoneComponents() {
		return (globalTimeZoneComponent == null) ? tzinfo.getComponents() : Arrays.asList(globalTimeZoneComponent);
	}

	/**
	 * Gets the version that the next iCalendar object will be written as.
	 * @return the version
	 */
	protected abstract ICalVersion getTargetVersion();

	/**
	 * Writes an iCalendar object to the data stream.
	 * @param ical the iCalendar object to write
	 * @throws IOException if there's a problem writing to the data stream
	 */
	protected abstract void _write(ICalendar ical) throws IOException;

	/**
	 * Gets the timezone that all date/time property values will be formatted
	 * in. If set, this setting will override the timezone information
	 * associated with each {@link ICalendar} object.
	 * @return the global timezone or null if not set (defaults to null)
	 */
	public TimeZone getGlobalTimeZone() {
		return globalTimeZone;
	}

	/**
	 * Gets the {@link VTimezone} component that is associated with the global
	 * timezone.
	 * @return the component or null if not set
	 */
	public VTimezone getGlobalTimeZoneComponent() {
		return globalTimeZoneComponent;
	}

	/**
	 * <p>
	 * Sets the timezone that all date/time property values will be formatted
	 * in. This is a convenience method that overrides the timezone information
	 * associated with each {@link ICalendar} object that is passed into this
	 * writer.
	 * </p>
	 * <p>
	 * Note that this method generates a {@link VTimezone} component that will
	 * be inserted into each written iCalendar object. It does this by
	 * downloading a file from <a href="http://tzurl.org">tzurl.org</a>.
	 * However, if the given {@link TimeZone} object is a {@link ICalTimeZone}
	 * instance, then the {@link VTimezone} component associated with the
	 * {@link ICalTimeZone} object will be used instead.
	 * </p>
	 * @param timezone the global timezone or null not to set a global timezone
	 * (defaults to null)
	 * @param outlookCompatible controls whether the downloaded component will
	 * be specifically tailored for Microsoft Outlook email clients. If the
	 * given timezone is null or if it is a {@link ICalTimeZone} instance, the
	 * value of this parameter is ignored.
	 * @throws IllegalArgumentException if an appropriate {@link VTimezone}
	 * component cannot be found at <a href="http://tzurl.org">tzurl.org</a>
	 */
	public void setGlobalTimeZone(TimeZone timezone, boolean outlookCompatible) {
		globalTimeZone = timezone;

		if (timezone == null) {
			globalTimeZoneComponent = null;
			return;
		}

		if (timezone instanceof ICalTimeZone) {
			ICalTimeZone icalTimezone = (ICalTimeZone) timezone;
			globalTimeZoneComponent = icalTimezone.getComponent();
			return;
		}

		VTimezoneGenerator generator = new TzUrlDotOrgGenerator(outlookCompatible);
		globalTimeZoneComponent = generator.generate(timezone);
	}

	/**
	 * Sets the timezone that all date/time property values will be formatted
	 * in. This is a convenience method that overrides the timezone information
	 * associated with each {@link ICalendar} object that is passed into this
	 * writer.
	 * @param timezone the global timezone or null not to set a global timezone
	 * (defaults to null). If the given component is null, the value of this
	 * parameter is ignored.
	 * @param component the VTIMEZONE component that represents the given
	 * timezone. If the given timezone is null, the value of this parameter is
	 * ignored.
	 * @throws IllegalArgumentException if the given {@link VTimezone} 
	 * component's {@link TimezoneId} property is not identical to the given
	 * {@link TimeZone} object's ID
	 */
	public void setGlobalTimeZone(TimeZone timezone, VTimezone component) {
		if (timezone == null || component == null) {
			globalTimeZone = null;
			globalTimeZoneComponent = null;
			return;
		}

		String timezoneId = timezone.getID();
		String componentId = ValuedProperty.getValue(component.getTimezoneId());
		if (!timezoneId.equals(componentId)) { //perform the comparison in this order because componentId is more likely to be null
			throw Messages.INSTANCE.getIllegalArgumentException(27);
		}

		globalTimeZone = timezone;
		globalTimeZoneComponent = component;
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
	 * Gets the component/property classes that don't have scribes associated
	 * with them.
	 * @param ical the iCalendar object
	 * @return the component/property classes
	 */
	private Collection<Class<?>> findScribeless(ICalendar ical) {
		Set<Class<?>> unregistered = new HashSet<Class<?>>();
		LinkedList<ICalComponent> components = new LinkedList<ICalComponent>();
		components.add(ical);

		while (!components.isEmpty()) {
			ICalComponent component = components.removeLast();

			Class<? extends ICalComponent> componentClass = component.getClass();
			if (componentClass != RawComponent.class && index.getComponentScribe(componentClass) == null) {
				unregistered.add(componentClass);
			}

			for (Map.Entry<Class<? extends ICalProperty>, List<ICalProperty>> entry : component.getProperties()) {
				List<ICalProperty> properties = entry.getValue();
				if (properties.isEmpty()) {
					continue;
				}

				Class<? extends ICalProperty> clazz = entry.getKey();
				if (clazz != RawProperty.class && index.getPropertyScribe(clazz) == null) {
					unregistered.add(clazz);
				}
			}

			components.addAll(component.getComponents().values());
		}

		return unregistered;
	}
}
