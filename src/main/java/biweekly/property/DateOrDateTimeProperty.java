package biweekly.property;

import java.util.Date;
import java.util.List;

import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDateFormatter;

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
 * Represents a property whose value is a date or a date-time.
 * @author Michael Angstadt
 */
public class DateOrDateTimeProperty extends ICalProperty {
	protected Date value;
	protected DateTimeComponents rawComponents;
	protected boolean hasTime;
	protected boolean localTime;

	/**
	 * Creates a new property.
	 * @param rawComponents the raw components of the date-time value
	 */
	public DateOrDateTimeProperty(DateTimeComponents rawComponents) {
		setRawComponents(rawComponents);
	}

	/**
	 * Creates a new property.
	 * @param value the date-time value
	 * @param hasTime true if the value has a time component, false if it is
	 * strictly a date
	 */
	public DateOrDateTimeProperty(Date value, boolean hasTime) {
		setValue(value, hasTime);
	}

	/**
	 * Gets the date-time value.
	 * @return the date-time value
	 */
	public Date getValue() {
		return value;
	}

	/**
	 * Sets the date-time value.
	 * @param value the date-time value
	 * @param hasTime true if the value has a time component, false if it is
	 * strictly a date
	 */
	public void setValue(Date value, boolean hasTime) {
		this.value = value;
		this.hasTime = hasTime;
	}

	/**
	 * Gets the raw components of the date-time value.
	 * @return the raw components
	 */
	public DateTimeComponents getRawComponents() {
		return rawComponents;
	}

	/**
	 * Sets the raw components of the date-time value.
	 * @param rawComponents the raw components
	 */
	public void setRawComponents(DateTimeComponents rawComponents) {
		this.rawComponents = rawComponents;
	}

	/**
	 * Determines whether the date-time value has a time component.
	 * @return true if the value has a time component, false if it is strictly a
	 * date
	 */
	public boolean hasTime() {
		return hasTime;
	}

	/**
	 * Gets whether the date should be outputted in local time (without a
	 * timezone).
	 * @return true to output the date without a timezone, false to include a
	 * timezone
	 */
	public boolean isLocalTime() {
		return localTime;
	}

	/**
	 * Sets whether the date should be outputted in local time (without a
	 * timezone). Use {@link #setTimezoneId(String)} to specify a timezone.
	 * Dates are written in UTC time by default.
	 * @param localTime true to output the date without a timezone, false to
	 * include a timezone (defaults to false)
	 */
	public void setLocalTime(boolean localTime) {
		this.localTime = localTime;
		if (localTime) {
			setTimezoneId(null);
		}
	}

	@Override
	public String getTimezoneId() {
		return super.getTimezoneId();
	}

	@Override
	public void setTimezoneId(String timezoneId) {
		super.setTimezoneId(timezoneId);
	}

	@Override
	public void setTimezone(VTimezone timezone) {
		super.setTimezone(timezone);
	}

	@Override
	protected void validate(List<ICalComponent> components, List<Warning> warnings) {
		if (value == null && components == null) {
			warnings.add(Warning.validate(26));
		}

		String tzid = getTimezoneId();
		if (tzid != null && tzid.contains("/") && ICalDateFormatter.parseTimeZoneId(tzid) == null) {
			warnings.add(Warning.validate(27, tzid));
		}
	}
}
