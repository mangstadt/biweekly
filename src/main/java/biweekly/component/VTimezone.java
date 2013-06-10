package biweekly.component;

import java.util.Date;
import java.util.List;

import biweekly.property.LastModified;
import biweekly.property.TimezoneIdentifier;
import biweekly.property.TimezoneUrl;

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
 * Defines a timezone.
 * @author Michael Angstadt
 * @see "RFC 5545 p.62-71"
 */
public class VTimezone extends ICalComponent {
	/**
	 * Gets the timezone ID. This is a <b>required</b> property. For a list of
	 * valid timezone identifiers, see the <a
	 * href="http://www.twinsun.com/tz/tz-link.htm">TZ database</a>.
	 * @return the timezone ID or null if not set
	 * @see "RFC 5545 p.102-3"
	 */
	public TimezoneIdentifier getTimezoneIdentifier() {
		return getProperty(TimezoneIdentifier.class);
	}

	/**
	 * Sets the timezone ID. This is a <b>required</b> property. For a list of
	 * valid timezone identifiers, see the <a
	 * href="http://www.twinsun.com/tz/tz-link.htm">TZ database</a>.
	 * @param timezoneIdentifier the timezone ID or null to remove
	 * @see "RFC 5545 p.102-3"
	 */
	public void setTimezoneIdentifier(TimezoneIdentifier timezoneIdentifier) {
		setProperty(TimezoneIdentifier.class, timezoneIdentifier);
	}

	/**
	 * Gets the date/time that the timezone data was last changed.
	 * @return the last modified date or null if not set
	 * @see "RFC 5545 p.138"
	 */
	public LastModified getLastModified() {
		return getProperty(LastModified.class);
	}

	/**
	 * Sets the date/time that the timezone data was last changed.
	 * @param lastModified the last modified date or null to remove
	 * @see "RFC 5545 p.138"
	 */
	public void setLastModified(LastModified lastModified) {
		setProperty(LastModified.class, lastModified);
	}

	/**
	 * Sets the date/time that the timezone data was last changed.
	 * @param lastModified the last modified date or null to remove
	 * @return the property that was created
	 * @see "RFC 5545 p.138"
	 */
	public LastModified setLastModified(Date lastModified) {
		LastModified prop = (lastModified == null) ? null : new LastModified(lastModified);
		setLastModified(prop);
		return prop;
	}

	/**
	 * Gets the timezone URL, which points to an iCalendar object that contains
	 * further information on the timezone.
	 * @return the URL or null if not set
	 * @see "RFC 5545 p.106"
	 */
	public TimezoneUrl getTimezoneUrl() {
		return getProperty(TimezoneUrl.class);
	}

	/**
	 * Sets the timezone URL, which points to an iCalendar object that contains
	 * further information on the timezone.
	 * @param url the URL or null to remove
	 * @see "RFC 5545 p.106"
	 */
	public void setTimezoneUrl(TimezoneUrl url) {
		setProperty(TimezoneUrl.class, url);
	}

	/**
	 * Sets the timezone URL, which points to an iCalendar object that contains
	 * further information on the timezone.
	 * @param url the timezone URL (e.g.
	 * "http://example.com/America-New_York.ics") or null to remove
	 * @return the property that was created
	 * @see "RFC 5545 p.106"
	 */
	public TimezoneUrl setTimezoneUrl(String url) {
		TimezoneUrl prop = (url == null) ? null : new TimezoneUrl(url);
		setTimezoneUrl(prop);
		return prop;
	}

	/**
	 * Gets the timezone's "standard" observance time ranges.
	 * @return the "standard" observance time ranges
	 */
	public List<StandardTime> getStandardTimes() {
		return getComponents(StandardTime.class);
	}

	/**
	 * Adds a "standard" observance time range.
	 * @param standardTime the "standard" observance time
	 */
	public void addStandardTime(StandardTime standardTime) {
		addComponent(standardTime);
	}

	/**
	 * Gets the timezone's "daylight savings" observance time ranges.
	 * @return the "daylight savings" observance time ranges
	 */
	public List<DaylightSavingsTime> getDaylightSavingsTime() {
		return getComponents(DaylightSavingsTime.class);
	}

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		if (getTimezoneIdentifier() == null) {
			warnings.add(TimezoneIdentifier.class.getSimpleName() + " is not set (it is required).");
		}
	}
}
