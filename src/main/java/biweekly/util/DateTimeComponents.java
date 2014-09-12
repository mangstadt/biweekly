package biweekly.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <p>
 * Contains the raw components of a date-time value.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre class="brush:java">
 * //July 22, 2013 at 17:25
 * DateTimeComponents components = new DateTimeComponents(2013, 07, 22, 17, 25, 0, false);
 * 
 * //parsing a date string (accepts basic and extended formats)
 * DateTimeComponents components = DateTimeComponents.parse(&quot;20130722T172500&quot;);
 * 
 * //converting to date string
 * DateTimeComponents components = new DateTimeComponents(2013, 07, 22, 17, 25, 0, false);
 * String str = components.toString(true); //&quot;2013-07-22T17:25:00&quot;
 * 
 * //converting to a Date object
 * DateTimeComponents components = new DateTimeComponents(2013, 07, 22, 17, 25, 0, false);
 * Date date = components.toDate();
 * 
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 */
public final class DateTimeComponents implements Comparable<DateTimeComponents> {
	private static final Pattern regex = Pattern.compile("^(\\d{4})-?(\\d{2})-?(\\d{2})(T(\\d{2}):?(\\d{2}):?(\\d{2})(Z?))?.*");
	private final int year, month, date, hour, minute, second;
	private final boolean utc;

	/**
	 * Parses the components out of a date-time string.
	 * @param dateString the date-time string (basic and extended formats
	 * supported, e.g. "20130331T020000" or "2013-03-31T02:00:00")
	 * @return the parsed components
	 * @throws IllegalArgumentException if the date string cannot be parsed
	 */
	public static DateTimeComponents parse(String dateString) {
		Matcher m = regex.matcher(dateString);
		if (!m.find()) {
			throw new IllegalArgumentException("Cannot parse date: " + dateString);
		}

		int i = 1;

		int year = Integer.parseInt(m.group(i++));

		int month = Integer.parseInt(m.group(i++));

		int date = Integer.parseInt(m.group(i++));

		i++; //skip

		String hourStr = m.group(i++);
		int hour = (hourStr == null) ? 0 : Integer.parseInt(hourStr);

		String minuteStr = m.group(i++);
		int minute = (minuteStr == null) ? 0 : Integer.parseInt(minuteStr);

		String secondStr = m.group(i++);
		int second = (secondStr == null) ? 0 : Integer.parseInt(secondStr);

		boolean utc = "Z".equals(m.group(i++));

		return new DateTimeComponents(year, month, date, hour, minute, second, utc);
	}

	/**
	 * Copies an existing DateTimeComponents object.
	 * @param original the object to copy from
	 * @param year the new year value or null not to change
	 * @param month the new month value or null not to change
	 * @param date the new date value or null not to change
	 * @param hour the new hour value or null not to change
	 * @param minute the new minute value or null not to change
	 * @param second the new second value or null not to change
	 * @param utc true if the time is in UTC, false if not, or null not to
	 * change
	 */
	public DateTimeComponents(DateTimeComponents original, Integer year, Integer month, Integer date, Integer hour, Integer minute, Integer second, Boolean utc) {
		//@formatter:off
		this(
			(year == null) ? original.year : year,
			(month == null) ? original.month : month,
			(date == null) ? original.date : date,
			(hour == null) ? original.hour : hour,
			(minute == null) ? original.minute : minute,
			(second == null) ? original.second : second,
			(utc == null) ? original.utc : utc
		);
		//@formatter:on
	}

	/**
	 * Creates a new set of date-time components.
	 * @param year the year (e.g. "2013")
	 * @param month the month (e.g. "1" for January)
	 * @param date the date of the month (e.g. "15")
	 * @param hour the hour (e.g. "13")
	 * @param minute the minute
	 * @param second the second
	 * @param utc true if the time is in UTC, false if not
	 */
	public DateTimeComponents(int year, int month, int date, int hour, int minute, int second, boolean utc) {
		this.year = year;
		this.month = month;
		this.date = date;
		this.hour = hour;
		this.minute = minute;
		this.second = second;
		this.utc = utc;
	}

	/**
	 * Gets the year component.
	 * @return the year
	 */
	public int getYear() {
		return year;
	}

	/**
	 * Gets the month component.
	 * @return the month (e.g. "1" for January)
	 */
	public int getMonth() {
		return month;
	}

	/**
	 * Gets the date component
	 * @return the date
	 */
	public int getDate() {
		return date;
	}

	/**
	 * Gets the hour component
	 * @return the hour
	 */
	public int getHour() {
		return hour;
	}

	/**
	 * Gets the minute component.
	 * @return the minute
	 */
	public int getMinute() {
		return minute;
	}

	/**
	 * Gets the second component.
	 * @return the second
	 */
	public int getSecond() {
		return second;
	}

	/**
	 * Gets whether the time is in UTC or not
	 * @return true if the time is in UTC, false if not
	 */
	public boolean isUtc() {
		return utc;
	}

	/**
	 * Converts the date-time components to a string using "basic" format.
	 * @return the date string
	 */
	@Override
	public String toString() {
		return toString(false);
	}

	/**
	 * Converts the date-time components to a string.
	 * @param extended true to use extended format, false to use basic
	 * @return the date string
	 */
	public String toString(boolean extended) {
		NumberFormat nf = new DecimalFormat("00");
		String dash = extended ? "-" : "";
		String colon = extended ? ":" : "";
		String z = utc ? "Z" : "";

		return year + dash + nf.format(month) + dash + nf.format(date) + "T" + nf.format(hour) + colon + nf.format(minute) + colon + nf.format(second) + z;
	}

	/**
	 * Converts the date-time components to a {@link Date} object.
	 * @return the date object
	 */
	public Date toDate() {
		TimeZone tz = utc ? TimeZone.getTimeZone("UTC") : TimeZone.getDefault();
		Calendar c = Calendar.getInstance(tz);
		c.clear();
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month - 1);
		c.set(Calendar.DATE, date);
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, second);
		return c.getTime();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + date;
		result = prime * result + hour;
		result = prime * result + minute;
		result = prime * result + month;
		result = prime * result + second;
		result = prime * result + (utc ? 1231 : 1237);
		result = prime * result + year;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DateTimeComponents other = (DateTimeComponents) obj;
		if (date != other.date)
			return false;
		if (hour != other.hour)
			return false;
		if (minute != other.minute)
			return false;
		if (month != other.month)
			return false;
		if (second != other.second)
			return false;
		if (utc != other.utc)
			return false;
		if (year != other.year)
			return false;
		return true;
	}

	public int compareTo(DateTimeComponents that) {
		int c = this.year - that.year;
		if (c != 0) {
			return c;
		}

		c = this.month - that.month;
		if (c != 0) {
			return c;
		}

		c = this.date - that.date;
		if (c != 0) {
			return c;
		}

		c = this.hour - that.hour;
		if (c != 0) {
			return c;
		}

		c = this.minute - that.minute;
		if (c != 0) {
			return c;
		}

		c = this.second - that.second;
		if (c != 0) {
			return c;
		}

		return 0;
	}

	public boolean before(DateTimeComponents that) {
		return this.compareTo(that) < 0;
	}

	public boolean after(DateTimeComponents that) {
		return this.compareTo(that) > 0;
	}
}