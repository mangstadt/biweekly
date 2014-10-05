package biweekly.io;

import static biweekly.io.DataModelConverter.convert;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext.TimezonedDate;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.property.Daylight;
import biweekly.property.ICalProperty;
import biweekly.property.Timezone;
import biweekly.property.TimezoneId;
import biweekly.util.ICalDateFormat;

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
 * Parses iCalendar objects from a data stream.
 * @author Michael Angstadt
 */
public abstract class StreamReader implements Closeable {
	protected final ParseWarnings warnings = new ParseWarnings();
	protected TimezoneInfo tzinfo;
	protected ScribeIndex index = new ScribeIndex();
	protected ParseContext context;

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
	 * Gets the warnings from the last iCalendar object that was read. This list
	 * is reset every time a new iCalendar object is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	public List<String> getWarnings() {
		return warnings.copy();
	}

	/**
	 * Gets the timezone info of the last iCalendar object that was read.
	 * @return the timezone info
	 */
	public TimezoneInfo getTimezoneInfo() {
		return tzinfo;
	}

	/**
	 * Reads all iCalendar objects from the data stream.
	 * @return the iCalendar objects
	 * @throws IOException if there's a problem reading from the stream
	 */
	public List<ICalendar> readAll() throws IOException {
		List<ICalendar> icals = new ArrayList<ICalendar>();
		ICalendar ical = null;
		while ((ical = readNext()) != null) {
			icals.add(ical);
		}
		return icals;
	}

	/**
	 * Reads the next iCalendar object from the data stream.
	 * @return the next iCalendar object or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	public ICalendar readNext() throws IOException {
		warnings.clear();
		context = new ParseContext();
		tzinfo = new TimezoneInfo();

		ICalendar ical = _readNext();
		if (ical == null) {
			return null;
		}

		ical.setVersion(context.getVersion());
		handleTimezones(ical);
		return ical;
	}

	/**
	 * Reads the next iCalendar object from the data stream.
	 * @return the next iCalendar object or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	protected abstract ICalendar _readNext() throws IOException;

	private void handleTimezones(ICalendar ical) {
		//convert vCalendar DAYLIGHT and TZ properties to a VTIMEZONE component
		VTimezone vcalComponent;
		{
			List<Daylight> daylights = ical.getProperties(Daylight.class);
			Timezone tz = ical.getProperty(Timezone.class);

			vcalComponent = convert(daylights, tz);
			if (vcalComponent != null) {
				TimeZone timezone = new ICalTimeZone(vcalComponent);
				tzinfo.assign(vcalComponent, timezone);
				tzinfo.setDefaultTimeZone(timezone);
			}

			ical.removeProperties(Daylight.class);
			ical.removeProperties(Timezone.class);
		}

		//assign a TimeZone object to each VTIMEZONE component.
		List<ICalComponent> toKeep = new ArrayList<ICalComponent>(0);
		for (VTimezone component : ical.getComponents(VTimezone.class)) {
			//make sure the component has an ID
			TimezoneId id = component.getTimezoneId();
			if (id == null || id.getValue() == null) {
				warnings.add(null, null, 39);
				toKeep.add(component);
				continue;
			}

			TimeZone timezone = new ICalTimeZone(component);
			tzinfo.assign(component, timezone);
		}

		//remove the VTIMEZONE components from the iCalendar object
		if (toKeep.isEmpty()) {
			ical.removeComponents(VTimezone.class);
		} else {
			//keep the VTIMEZONE components that don't have IDs
			ical.getComponents().replace(VTimezone.class, toKeep);
		}

		if (vcalComponent != null) {
			//vCal: parse floating dates according to the DAYLIGHT and TZ properties (which were converted to a VTIMEZONE component)
			TimeZone timezone = tzinfo.getTimeZoneByComponent(vcalComponent);
			for (TimezonedDate timezonedDate : context.getFloatingDates()) {
				//parse the date string again under its real timezone
				Date realDate = ICalDateFormat.parse(timezonedDate.getDateStr(), timezone);

				//update the Date object with the new timestamp
				timezonedDate.getDate().setTime(realDate.getTime());
			}
		} else {
			for (TimezonedDate timezonedDate : context.getFloatingDates()) {
				tzinfo.setFloating(timezonedDate.getProperty(), true);
			}
		}

		for (Map.Entry<String, List<TimezonedDate>> entry : context.getTimezonedDates()) {
			//find the VTIMEZONE component with the given TZID
			String tzid = entry.getKey();
			TimeZone timezone;
			if (tzid.startsWith("/")) {
				//treat the TZID parameter value as an Olsen timezone ID
				timezone = ICalDateFormat.parseTimeZoneId(tzid.substring(1));
				if (timezone == null) {
					//timezone could not be determined
					warnings.add(null, null, 38, tzid);
					continue;
				}
			} else {
				timezone = tzinfo.getTimeZoneById(tzid);
				if (timezone == null) {
					//A VTIMEZONE component couldn't found
					//so treat the TZID parameter value as an Olsen timezone ID
					timezone = ICalDateFormat.parseTimeZoneId(tzid);
					if (timezone == null) {
						//timezone could not be determined
						warnings.add(null, null, 38, tzid);
						continue;
					}

					warnings.add(null, null, 37, tzid);
				}
			}

			List<TimezonedDate> timezonedDates = entry.getValue();
			for (TimezonedDate timezonedDate : timezonedDates) {
				//assign the property to the timezone
				ICalProperty property = timezonedDate.getProperty();
				tzinfo.setTimeZoneReader(property, timezone);

				//parse the date string again under its real timezone
				Date realDate = ICalDateFormat.parse(timezonedDate.getDateStr(), timezone);

				//update the Date object with the new timestamp
				timezonedDate.getDate().setTime(realDate.getTime());

				//remove the TZID parameter
				property.getParameters().setTimezoneId(null);
			}
		}
	}
}
