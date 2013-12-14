package biweekly.component;

import java.util.Date;
import java.util.List;

import biweekly.Warning;
import biweekly.property.Comment;
import biweekly.property.DateStart;
import biweekly.property.ExceptionDates;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.property.TimezoneName;
import biweekly.property.TimezoneOffsetFrom;
import biweekly.property.TimezoneOffsetTo;
import biweekly.util.DateTimeComponents;
import biweekly.util.Recurrence;

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
 * @rfc 5545 p.62-71
 */
public abstract class Observance extends ICalComponent {
	/**
	 * Gets the date that the timezone observance starts.
	 * @return the start date or null if not set
	 * @rfc 5545 p.97-8
	 */
	public DateStart getDateStart() {
		return getProperty(DateStart.class);
	}

	/**
	 * Sets the date that the timezone observance starts.
	 * @param dateStart the start date or null to remove
	 * @rfc 5545 p.97-8
	 */
	public void setDateStart(DateStart dateStart) {
		if (dateStart != null) {
			dateStart.setLocalTime(true);
		}
		setProperty(DateStart.class, dateStart);
	}

	/**
	 * Sets the date that the timezone observance starts.
	 * @param components the raw components of the start date or null to remove
	 * @return the property that was created
	 * @rfc 5545 p.97-8
	 */
	public DateStart setDateStart(DateTimeComponents components) {
		DateStart prop = (components == null) ? null : new DateStart(components);
		setDateStart(prop);
		return prop;
	}

	/**
	 * Gets the UTC offset that the timezone observance transitions to.
	 * @return the UTC offset or null if not set
	 * @rfc 5545 p.105-6
	 */
	public TimezoneOffsetTo getTimezoneOffsetTo() {
		return getProperty(TimezoneOffsetTo.class);
	}

	/**
	 * Sets the UTC offset that the timezone observance transitions to.
	 * @param timezoneOffsetTo the UTC offset or null to remove
	 * @rfc 5545 p.105-6
	 */
	public void setTimezoneOffsetTo(TimezoneOffsetTo timezoneOffsetTo) {
		setProperty(TimezoneOffsetTo.class, timezoneOffsetTo);
	}

	/**
	 * Sets the UTC offset that the timezone observance transitions to.
	 * @param hour the hour offset (e.g. "-5")
	 * @param minute the minute offset (e.g. "0")
	 * @return the property that was created
	 * @rfc 5545 p.105-6
	 */
	public TimezoneOffsetTo setTimezoneOffsetTo(Integer hour, Integer minute) {
		TimezoneOffsetTo prop = new TimezoneOffsetTo(hour, minute);
		setTimezoneOffsetTo(prop);
		return prop;
	}

	/**
	 * Gets the UTC offset that the timezone observance transitions from.
	 * @return the UTC offset or null if not set
	 * @rfc 5545 p.104-5
	 */
	public TimezoneOffsetFrom getTimezoneOffsetFrom() {
		return getProperty(TimezoneOffsetFrom.class);
	}

	/**
	 * Sets the UTC offset that the timezone observance transitions from.
	 * @param timezoneOffsetFrom the UTC offset or null to remove
	 * @rfc 5545 p.104-5
	 */
	public void setTimezoneOffsetFrom(TimezoneOffsetFrom timezoneOffsetFrom) {
		setProperty(TimezoneOffsetFrom.class, timezoneOffsetFrom);
	}

	/**
	 * Sets the UTC offset that the timezone observance transitions from.
	 * @param hour the hour offset (e.g. "-5")
	 * @param minute the minute offset (e.g. "0")
	 * @return the property that was created
	 * @rfc 5545 p.104-5
	 */
	public TimezoneOffsetFrom setTimezoneOffsetFrom(Integer hour, Integer minute) {
		TimezoneOffsetFrom prop = new TimezoneOffsetFrom(hour, minute);
		setTimezoneOffsetFrom(prop);
		return prop;
	}

	/**
	 * Gets how often the timezone observance repeats.
	 * @return the recurrence rule or null if not set
	 * @rfc 5545 p.122-32
	 */
	public RecurrenceRule getRecurrenceRule() {
		return getProperty(RecurrenceRule.class);
	}

	/**
	 * Sets how often the timezone observance repeats.
	 * @param recur the recurrence rule or null to remove
	 * @return the property that was created
	 * @rfc 5545 p.122-32
	 */
	public RecurrenceRule setRecurrenceRule(Recurrence recur) {
		RecurrenceRule prop = (recur == null) ? null : new RecurrenceRule(recur);
		setRecurrenceRule(prop);
		return prop;
	}

	/**
	 * Sets how often the timezone observance repeats.
	 * @param recurrenceRule the recurrence rule or null to remove
	 * @rfc 5545 p.122-32
	 */
	public void setRecurrenceRule(RecurrenceRule recurrenceRule) {
		setProperty(RecurrenceRule.class, recurrenceRule);
	}

	/**
	 * Gets the comments attached to the timezone observance.
	 * @return the comments
	 * @rfc 5545 p.83-4
	 */
	public List<Comment> getComments() {
		return getProperties(Comment.class);
	}

	/**
	 * Adds a comment to the timezone observance.
	 * @param comment the comment to add
	 * @rfc 5545 p.83-4
	 */
	public void addComment(Comment comment) {
		addProperty(comment);
	}

	/**
	 * Adds a comment to the timezone observance.
	 * @param comment the comment to add
	 * @return the property that was created
	 * @rfc 5545 p.83-4
	 */
	public Comment addComment(String comment) {
		Comment prop = new Comment(comment);
		addComment(prop);
		return prop;
	}

	/**
	 * Gets the list of dates/periods that help define the recurrence rule of
	 * this timezone observance (if one is defined).
	 * @return the recurrence dates
	 * @rfc 5545 p.120-2
	 */
	public List<RecurrenceDates> getRecurrenceDates() {
		return getProperties(RecurrenceDates.class);
	}

	/**
	 * Adds a list of dates/periods that help define the recurrence rule of this
	 * timezone observance (if one is defined).
	 * @param recurrenceDates the recurrence dates
	 * @rfc 5545 p.120-2
	 */
	public void addRecurrenceDates(RecurrenceDates recurrenceDates) {
		addProperty(recurrenceDates);
	}

	/**
	 * Gets the traditional, non-standard names for the timezone observance.
	 * @return the timezone observance names
	 * @rfc 5545 p.103-4
	 */
	public List<TimezoneName> getTimezoneNames() {
		return getProperties(TimezoneName.class);
	}

	/**
	 * Adds a traditional, non-standard name for the timezone observance.
	 * @param timezoneName the timezone observance name
	 * @rfc 5545 p.103-4
	 */
	public void addTimezoneName(TimezoneName timezoneName) {
		addProperty(timezoneName);
	}

	/**
	 * Adds a traditional, non-standard name for the timezone observance.
	 * @param timezoneName the timezone observance name (e.g. "EST")
	 * @return the property that was created
	 * @rfc 5545 p.103-4
	 */
	public TimezoneName addTimezoneName(String timezoneName) {
		TimezoneName prop = new TimezoneName(timezoneName);
		addTimezoneName(prop);
		return prop;
	}

	/**
	 * Gets the list of exceptions to the timezone observance.
	 * @return the list of exceptions
	 * @rfc 5545 p.118-20
	 */
	public List<ExceptionDates> getExceptionDates() {
		return getProperties(ExceptionDates.class);
	}

	/**
	 * Adds a list of exceptions to the timezone observance. Note that this
	 * property can contain multiple dates.
	 * @param exceptionDates the list of exceptions
	 * @rfc 5545 p.118-20
	 */
	public void addExceptionDates(ExceptionDates exceptionDates) {
		addProperty(exceptionDates);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void validate(List<ICalComponent> components, List<Warning> warnings) {
		checkRequiredCardinality(warnings, DateStart.class, TimezoneOffsetTo.class, TimezoneOffsetFrom.class);

		//RFC 5545 p. 167
		DateStart dateStart = getDateStart();
		RecurrenceRule rrule = getRecurrenceRule();
		if (dateStart != null && rrule != null) {
			Date start = dateStart.getValue();
			Recurrence recur = rrule.getValue();
			if (start != null && recur != null) {
				if (!dateStart.hasTime() && (!recur.getByHour().isEmpty() || !recur.getByMinute().isEmpty() || !recur.getBySecond().isEmpty())) {
					warnings.add(new Warning(5));
				}
			}
		}

		//RFC 5545 p. 167
		if (getProperties(RecurrenceRule.class).size() > 1) {
			warnings.add(new Warning(6));
		}
	}
}
