package biweekly.io.json;

import java.io.IOException;
import java.util.TimeZone;

import biweekly.ICalendar;
import biweekly.Messages;
import biweekly.component.VTimezone;
import biweekly.io.ICalTimeZone;
import biweekly.io.TzUrlDotOrgGenerator;
import biweekly.io.VTimezoneGenerator;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.property.ICalProperty;
import biweekly.property.TimezoneId;
import biweekly.property.ValuedProperty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

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
 * Serializes jCals within the jackson-databind framework.
 * @author Buddy Gorven
 * @author Michael Angstadt
 */
@JsonFormat
public class JCalSerializer extends StdSerializer<ICalendar> {
	private static final long serialVersionUID = -8879354015298785358L;
	private ScribeIndex index = new ScribeIndex();
	protected TimeZone globalTimeZone;
	protected VTimezone globalTimeZoneComponent;

	public JCalSerializer() {
		super(ICalendar.class);
	}

	@Override
	public void serialize(ICalendar value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
		@SuppressWarnings("resource")
		JCalWriter writer = new JCalWriter(gen);
		writer.setScribeIndex(getScribeIndex());
		writer.setGlobalTimeZone(globalTimeZone, globalTimeZoneComponent);
		writer.write(value);
	}

	/**
	 * <p>
	 * Registers a property scribe. This is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(ICalPropertyScribe<? extends ICalProperty> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the scribe index.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the scribe index.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
	}

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
}
