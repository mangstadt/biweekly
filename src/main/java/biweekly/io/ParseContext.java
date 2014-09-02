package biweekly.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.property.ICalProperty;
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
 * Stores information used during the parsing of an iCalendar object.
 * @author Michael Angstadt
 */
public class ParseContext {
	private ICalVersion version;
	private List<Warning> warnings = new ArrayList<Warning>();
	private ListMultimap<String, TimezonedDate> timezonedDates = new ListMultimap<String, TimezonedDate>();
	private Set<ICalProperty> floatingDates = new HashSet<ICalProperty>();

	/**
	 * Gets the version of the iCalendar object being parsed.
	 * @return the iCalendar version
	 */
	public ICalVersion getVersion() {
		return version;
	}

	/**
	 * Sets the version of the iCalendar object being parsed.
	 * @param version the iCalendar version
	 */
	public void setVersion(ICalVersion version) {
		this.version = version;
	}

	/**
	 * Keeps track of a date-time property value that uses a timezone so it can
	 * be parsed later. Timezones cannot be handled until the entire iCalendar
	 * object has been parsed.
	 * @param tzid the timezone ID (TZID parameter)
	 * @param property the property
	 * @param date the date object that was assigned to the property object
	 * (should be parsed under the JVM's default timezone)
	 * @param dateStr the raw date string (e.g. "20140901T120000")
	 */
	public void addTimezonedDate(String tzid, ICalProperty property, Date date, String dateStr) {
		timezonedDates.put(tzid, new TimezonedDate(dateStr, date, property));
	}

	/**
	 * Gets the list of date-time property values that use a timezone.
	 * @return the date-time property values that use a timezone (key = TZID;
	 * value = the property)
	 */
	public ListMultimap<String, TimezonedDate> getTimezonedDates() {
		return timezonedDates;
	}

	/**
	 * Keeps track of a date-time property that does not have a timezone
	 * (floating time), so it can be added to the {@link TimezoneInfo} object
	 * after the iCalendar object is parsed.
	 * @param property the property
	 */
	public void addFloatingDate(ICalProperty property) {
		floatingDates.add(property);
	}

	/**
	 * Gets the date-time properties that are in floating time (lacking a
	 * timezone).
	 * @return the floating date-time properties
	 */
	public Set<ICalProperty> getFloatingDates() {
		return floatingDates;
	}

	/**
	 * Adds a parse warning.
	 * @param code the warning code
	 * @param args the warning message arguments
	 */
	public void addWarning(int code, Object... args) {
		warnings.add(Warning.parse(code, args));
	}

	/**
	 * Adds a parse warning.
	 * @param message the warning message
	 */
	public void addWarning(String message) {
		warnings.add(new Warning(message));
	}

	/**
	 * Gets the parse warnings.
	 * @return the parse warnings
	 */
	public List<Warning> getWarnings() {
		return warnings;
	}

	/**
	 * Represents a property whose date-time value has a timezone.
	 * @author Michael Angstadt
	 */
	public static class TimezonedDate {
		private final String dateStr;
		private final Date date;
		private final ICalProperty property;

		/**
		 * @param dateStr the raw date string (e.g. "20140901T120000")
		 * @param date the date object that was assigned to the property object
		 * (should be parsed under the JVM's default timezone)
		 * @param property the property object
		 */
		public TimezonedDate(String dateStr, Date date, ICalProperty property) {
			this.dateStr = dateStr;
			this.date = date;
			this.property = property;
		}

		/**
		 * Gets the raw date string.
		 * @return the raw date string (e.g. "20140901T120000")
		 */
		public String getDateStr() {
			return dateStr;
		}

		/**
		 * Gets the date object that was assigned to the property object (should
		 * be parsed under the JVM's default timezone)
		 * @return the date object
		 */
		public Date getDate() {
			return date;
		}

		/**
		 * Gets the property object.
		 * @return the property
		 */
		public ICalProperty getProperty() {
			return property;
		}
	}
}
