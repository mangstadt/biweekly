package biweekly.component;

import java.util.Date;
import java.util.List;

import biweekly.property.Comment;
import biweekly.property.DateStart;
import biweekly.property.ExceptionDates;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.property.TimezoneName;
import biweekly.property.TimezoneOffsetFrom;
import biweekly.property.TimezoneOffsetTo;

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
 * Parent class for the "daylight" and "standard" timezone observances.
 * @author Michael Angstadt
 * @see DaylightSavingsTime
 * @see StandardTime
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-62">RFC 5545
 * p.62-71</a>
 */
public abstract class Observance extends ICalComponent {
	/**
	 * Gets the date that the timezone starts.
	 * @return the start date or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 */
	public DateStart getDateStart() {
		return getProperty(DateStart.class);
	}

	/**
	 * Sets the date that the timezone starts.
	 * @param dateStart the start date or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 */
	public void setDateStart(DateStart dateStart) {
		if (dateStart != null) {
			dateStart.setLocalTime(true);
		}
		setProperty(DateStart.class, dateStart);
	}

	/**
	 * Sets the date that the timezone starts.
	 * @param dateStart the start date or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 */
	public DateStart setDateStart(Date dateStart) {
		DateStart prop = (dateStart == null) ? null : new DateStart(dateStart);
		setDateStart(prop);
		return prop;
	}

	/**
	 * Gets the timezone offset that is currently in use in the timezone
	 * observance.
	 * @return the timezone offset or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-105">RFC 5545
	 * p.105-6</a>
	 */
	public TimezoneOffsetTo getTimezoneOffsetTo() {
		return getProperty(TimezoneOffsetTo.class);
	}

	/**
	 * Sets the timezone offset that is currently in use in the timezone
	 * observance.
	 * @param timezoneOffsetTo the timezone offset or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-105">RFC 5545
	 * p.105-6</a>
	 */
	public void setTimezoneOffsetTo(TimezoneOffsetTo timezoneOffsetTo) {
		setProperty(TimezoneOffsetTo.class, timezoneOffsetTo);
	}

	/**
	 * Sets the timezone offset that is currently in use in the timezone
	 * observance.
	 * @param hour the hour offset (e.g. "-5")
	 * @param minute the minute offset (e.g. "0")
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-105">RFC 5545
	 * p.105-6</a>
	 */
	public TimezoneOffsetTo setTimezoneOffsetTo(Integer hour, Integer minute) {
		TimezoneOffsetTo prop = new TimezoneOffsetTo(hour, minute);
		setTimezoneOffsetTo(prop);
		return prop;
	}

	/**
	 * Gets the timezone offset that was in use before the timezone observance.
	 * @return the timezone offset or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-104">RFC 5545
	 * p.104-5</a>
	 */
	public TimezoneOffsetFrom getTimezoneOffsetFrom() {
		return getProperty(TimezoneOffsetFrom.class);
	}

	/**
	 * Sets the timezone offset that was in use before the timezone observance.
	 * @param timezoneOffsetFrom the timezone offset or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-104">RFC 5545
	 * p.104-5</a>
	 */
	public void setTimezoneOffsetFrom(TimezoneOffsetFrom timezoneOffsetFrom) {
		setProperty(TimezoneOffsetFrom.class, timezoneOffsetFrom);
	}

	/**
	 * Sets the timezone offset that was in use before the timezone observance.
	 * @param hour the hour offset (e.g. "-5")
	 * @param minute the minute offset (e.g. "0")
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-104">RFC 5545
	 * p.104-5</a>
	 */
	public TimezoneOffsetFrom setTimezoneOffsetFrom(Integer hour, Integer minute) {
		TimezoneOffsetFrom prop = new TimezoneOffsetFrom(hour, minute);
		setTimezoneOffsetFrom(prop);
		return prop;
	}

	/**
	 * Gets how often the observance repeats.
	 * @return the recurrence rule or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-122">RFC 5545
	 * p.122-32</a>
	 */
	public RecurrenceRule getRecurrenceRule() {
		return getProperty(RecurrenceRule.class);
	}

	/**
	 * Sets how often the observance repeats.
	 * @param recurrenceRule the recurrence rule or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-122">RFC 5545
	 * p.122-32</a>
	 */
	public void setRecurrenceRule(RecurrenceRule recurrenceRule) {
		setProperty(RecurrenceRule.class, recurrenceRule);
	}

	/**
	 * Gets the comments attached to the timezone.
	 * @return the comments
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-83">RFC 5545
	 * p.83-4</a>
	 */
	public List<Comment> getComments() {
		return getProperties(Comment.class);
	}

	/**
	 * Adds a comment to the timezone.
	 * @param comment the comment to add
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-83">RFC 5545
	 * p.83-4</a>
	 */
	public void addComment(Comment comment) {
		addProperty(comment);
	}

	/**
	 * Adds a comment to the timezone.
	 * @param comment the comment to add
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-83">RFC 5545
	 * p.83-4</a>
	 */
	public Comment addComment(String comment) {
		Comment prop = new Comment(comment);
		addComment(prop);
		return prop;
	}

	/**
	 * Gets the list of dates/periods that help define the recurrence rule of
	 * this observance (if one is defined).
	 * @return the recurrence dates
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-120">RFC 5545
	 * p.120-2</a>
	 */
	public List<RecurrenceDates> getRecurrenceDates() {
		return getProperties(RecurrenceDates.class);
	}

	/**
	 * Adds a list of dates/periods that help define the recurrence rule of this
	 * observance (if one is defined).
	 * @param recurrenceDates the recurrence dates
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-120">RFC 5545
	 * p.120-2</a>
	 */
	public void addRecurrenceDates(RecurrenceDates recurrenceDates) {
		addProperty(recurrenceDates);
	}

	/**
	 * Gets the traditional, non-standard names for the timezone.
	 * @return the timezone names
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-103">RFC 5545
	 * p.103-4</a>
	 */
	public List<TimezoneName> getTimezoneNames() {
		return getProperties(TimezoneName.class);
	}

	/**
	 * Adds a traditional, non-standard name for the timezone.
	 * @param timezoneName the timezone name
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-103">RFC 5545
	 * p.103-4</a>
	 */
	public void addTimezoneName(TimezoneName timezoneName) {
		addProperty(timezoneName);
	}

	/**
	 * Adds a traditional, non-standard name for the timezone.
	 * @param timezoneName the timezone name (e.g. "EST")
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-103">RFC 5545
	 * p.103-4</a>
	 */
	public TimezoneName addTimezoneName(String timezoneName) {
		TimezoneName prop = new TimezoneName(timezoneName);
		addTimezoneName(prop);
		return prop;
	}

	/**
	 * Gets the list of exceptions to the observance.
	 * @return the list of exceptions
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-118">RFC 5545
	 * p.118-20</a>
	 */
	public List<ExceptionDates> getExceptionDates() {
		return getProperties(ExceptionDates.class);
	}

	/**
	 * Adds a list of exceptions to the observance. Note that this property can
	 * contain multiple dates.
	 * @param exceptionDates the list of exceptions
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-118">RFC 5545
	 * p.118-20</a>
	 */
	public void addExceptionDates(ExceptionDates exceptionDates) {
		addProperty(exceptionDates);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		checkRequiredCardinality(warnings, DateStart.class, TimezoneOffsetTo.class, TimezoneOffsetFrom.class);
	}
}
