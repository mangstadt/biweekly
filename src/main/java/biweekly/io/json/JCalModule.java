package biweekly.io.json;

import java.util.TimeZone;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VTimezone;
import biweekly.io.ICalTimeZone;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.property.ICalProperty;
import biweekly.property.TimezoneId;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.module.SimpleModule;

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
 * <p>
 * Module for jackson-databind that serializes and deserializes jCals.
 * </p>
 * <p>
 * <b>Example:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * ObjectMapper mapper = new ObjectMapper();
 * mapper.registerModule(new JCalModule());
 * ICalendar result = mapper.readValue(..., ICalendar.class);
 * </pre>
 * @author Buddy Gorven
 * @author Michael Angstadt
 */
public class JCalModule extends SimpleModule {
	private static final long serialVersionUID = 4861448845613769559L;
	private static final String MODULE_NAME = "biweekly-jcal";
	private static final Version MODULE_VERSION = moduleVersion();

	private final JCalDeserializer deserializer = new JCalDeserializer();
	private final JCalSerializer serializer = new JCalSerializer();

	private ScribeIndex index;

	/**
	 * Creates the module.
	 */
	public JCalModule() {
		super(MODULE_NAME, MODULE_VERSION);

		setScribeIndex(new ScribeIndex());
		addSerializer(serializer);
		addDeserializer(ICalendar.class, deserializer);
	}

	private static Version moduleVersion() {
		String[] split = Biweekly.VERSION.split("[.-]");
		if (split.length < 3) {
			/*
			 * This can happen during development if the "biweekly.properties"
			 * file has not been filtered by Maven.
			 */
			return new Version(0, 0, 0, "", Biweekly.GROUP_ID, Biweekly.ARTIFACT_ID);
		}

		int major = Integer.parseInt(split[0]);
		int minor = Integer.parseInt(split[1]);
		int patch = Integer.parseInt(split[2]);
		String snapshot = (split.length > 3) ? split[3] : "RELEASE";

		return new Version(major, minor, patch, snapshot, Biweekly.GROUP_ID, Biweekly.ARTIFACT_ID);
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
	 * Gets the scribe index used by the serializer and deserializer.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the scribe index for the serializer and deserializer to use.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
		serializer.setScribeIndex(index);
		deserializer.setScribeIndex(index);
	}

	/**
	 * Gets the timezone that all date/time property values will be formatted
	 * in. If set, this setting will override the timezone information
	 * associated with each {@link ICalendar} object.
	 * @return the global timezone or null if not set (defaults to null)
	 */
	public TimeZone getGlobalTimeZone() {
		return serializer.getGlobalTimeZone();
	}

	/**
	 * Gets the {@link VTimezone} component that is associated with the global
	 * timezone.
	 * @return the component or null if not set
	 */
	public VTimezone getGlobalTimeZoneComponent() {
		return serializer.getGlobalTimeZoneComponent();
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
		serializer.setGlobalTimeZone(timezone, outlookCompatible);
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
		serializer.setGlobalTimeZone(timezone, component);
	}
}
