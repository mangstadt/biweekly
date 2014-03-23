package biweekly.util;

import java.util.TimeZone;

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
 * Helper class that formats and parses iCalendar dates. iCalendar dates adhere
 * to the ISO8601 date format standard.
 * @author Michael Angstadt
 */
public class ICalDateFormatter {
	/**
	 * Determines whether a date string has a time component.
	 * @param dateStr the date string (e.g. "20130601T120000")
	 * @return true if it has a time component, false if not
	 */
	public static boolean dateHasTime(String dateStr) {
		return dateStr.contains("T");
	}

	/**
	 * Determines whether a date string is in UTC time or has a timezone offset.
	 * @param dateStr the date string (e.g. "20130601T120000Z",
	 * "20130601T120000-0400")
	 * @return true if it has a timezone, false if not
	 */
	public static boolean dateHasTimezone(String dateStr) {
		return dateStr.endsWith("Z") || dateStr.matches(".*?[-+]\\d\\d:?\\d\\d");
	}

	/**
	 * Gets the {@link TimeZone} object that corresponds to the given ID.
	 * @param timezoneId the timezone ID (e.g. "America/New_York")
	 * @return the timezone object or null if not found
	 */
	public static TimeZone parseTimeZoneId(String timezoneId) {
		TimeZone timezone = TimeZone.getTimeZone(timezoneId);
		return "GMT".equals(timezone.getID()) ? null : timezone;
	}

	private ICalDateFormatter() {
		//hide constructor
	}
}
