package biweekly.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
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
	 * Formats a date for inclusion in an iCalendar object.
	 * @param date the date to format
	 * @param format the format to use
	 * @return the formatted date
	 */
	public static String format(Date date, ISOFormat format) {
		return format(date, format, null);
	}

	/**
	 * Formats a date for inclusion in an iCalendar object.
	 * @param date the date to format
	 * @param format the format to use
	 * @param timeZone the timezone to format the date in or null to use the
	 * JVM's default timezone (ignored with "UTC" formats)
	 * @return the formatted date
	 */
	public static String format(Date date, ISOFormat format, TimeZone timeZone) {
		switch (format) {
		case UTC_TIME_BASIC:
		case UTC_TIME_EXTENDED:
			timeZone = TimeZone.getTimeZone("UTC");
			break;
		}

		DateFormat df = format.getFormatDateFormat();
		if (timeZone != null) {
			df.setTimeZone(timeZone);
		}
		String str = df.format(date);

		switch (format) {
		case TIME_EXTENDED:
			//add a colon to the timezone
			//example: converts "2012-07-05T22:31:41-0400" to "2012-07-05T22:31:41-04:00"
			str = str.replaceAll("([-\\+]\\d{2})(\\d{2})$", "$1:$2");
			break;
		}

		return str;
	}

	/**
	 * Parses an iCalendar date.
	 * @param dateStr the date string to parse (e.g. "20130609T181023Z")
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date string isn't in one of the
	 * accepted ISO8601 formats
	 */
	public static Date parse(String dateStr) {
		return parse(dateStr, null);
	}

	/**
	 * Parses an iCalendar date.
	 * @param dateStr the date string to parse (e.g. "20130609T181023Z")
	 * @param timezone the timezone to parse the date as or null to use the
	 * JVM's default timezone (if the date string contains its own timezone,
	 * then that timezone will be used instead)
	 * @return the parsed date
	 * @throws IllegalArgumentException if the date string isn't in one of the
	 * accepted ISO8601 formats
	 */
	public static Date parse(String dateStr, TimeZone timezone) {
		//find out what ISOFormat the date is in
		ISOFormat format = null;
		for (ISOFormat f : ISOFormat.values()) {
			if (f.matches(dateStr)) {
				format = f;
				break;
			}
		}
		if (format == null) {
			throw new IllegalArgumentException("Date string is not in a valid ISO-8601 format.");
		}

		//tweak the date string to make it work with SimpleDateFormat
		switch (format) {
		case TIME_EXTENDED:
		case HCARD_TIME_TAG:
			//SimpleDateFormat doesn't recognize timezone offsets that have colons
			//so remove the colon from the timezone offset
			dateStr = dateStr.replaceAll("([-\\+]\\d{2}):(\\d{2})$", "$1$2");
			break;
		case UTC_TIME_BASIC:
		case UTC_TIME_EXTENDED:
			//SimpleDateFormat doesn't recognize "Z"
			dateStr = dateStr.replace("Z", "+0000");
			break;
		}

		//parse the date
		DateFormat df = format.getParseDateFormat();
		if (timezone != null) {
			df.setTimeZone(timezone);
		}
		try {
			return df.parse(dateStr);
		} catch (ParseException e) {
			//should never be thrown because the string is checked against a regex
			throw new IllegalArgumentException("Date string is not in a valid ISO-8601 format.");
		}
	}

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
