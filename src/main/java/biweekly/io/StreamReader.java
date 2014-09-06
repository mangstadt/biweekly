package biweekly.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import biweekly.ICalendar;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.io.ParseContext.TimezonedDate;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.property.ICalProperty;
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
 * Parses iCalendar objects from an input stream.
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
	 * Gets the timezone info of the last iCalendar object that was read. This
	 * object is recreated every time a new iCalendar object is read.
	 * @return the timezone info
	 */
	public TimezoneInfo getTimezoneInfo() {
		return tzinfo;
	}

	/**
	 * Reads the next iCalendar object from the data stream.
	 * @return the next iCalendar object or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	public final ICalendar readNext() throws IOException {
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
		//handle timezones
		for (Map.Entry<String, List<TimezonedDate>> entry : context.getTimezonedDates()) {
			//find the VTIMEZONE component with the given TZID
			String tzid = entry.getKey();
			VTimezone component = null;
			for (VTimezone vtimezone : ical.getTimezones()) {
				TimezoneId timezoneId = vtimezone.getTimezoneId();
				if (timezoneId != null && tzid.equals(timezoneId.getValue())) {
					component = vtimezone;
					break;
				}
			}

			TimeZone timezone = null;
			if (component == null) {
				//A VTIMEZONE component couldn't found
				//so treat the TZID parameter value as an Olsen timezone ID
				timezone = ICalDateFormat.parseTimeZoneId(tzid);
				if (timezone == null) {
					warnings.add(null, null, Warning.parse(38, tzid));
				} else {
					warnings.add(null, null, Warning.parse(37, tzid));
				}
			} else {
				//TODO convert the VTIMEZONE component to a Java TimeZone object
				//TODO for now, treat the TZID as an Olsen timezone (which is what biweekly used to do) 
				timezone = ICalDateFormat.parseTimeZoneId(tzid);
				if (timezone == null) {
					timezone = TimeZone.getDefault();
				}
			}

			if (timezone == null) {
				//timezone could not be determined
				continue;
			}

			//assign this VTIMEZONE component to the TimeZone object
			tzinfo.assign(component, timezone);

			List<TimezonedDate> timezonedDates = entry.getValue();
			for (TimezonedDate timezonedDate : timezonedDates) {
				//assign the property to the timezone
				ICalProperty property = timezonedDate.getProperty();
				tzinfo.setTimezone(property, timezone);
				property.getParameters().setTimezoneId(null); //remove the TZID parameter

				//parse the date string again under its real timezone
				Date realDate = ICalDateFormat.parse(timezonedDate.getDateStr(), timezone);

				//update the Date object with the new timestamp
				timezonedDate.getDate().setTime(realDate.getTime()); //the one time I am glad that Date objects are mutable... xD
			}
		}

		for (ICalProperty property : context.getFloatingDates()) {
			tzinfo.setUseFloatingTime(property, true);
		}
	}
}
