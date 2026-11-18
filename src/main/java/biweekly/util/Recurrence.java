package biweekly.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import com.github.mangstadt.vinnie.LaciToken;

import biweekly.property.DateStart;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;
import biweekly.util.com.google.ical.compat.javautil.DateIteratorFactory;
import biweekly.util.com.google.ical.iter.RecurrenceIterator;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
 * Represents a recurrence rule value.
 * </p>
 * <p>
 * This class is immutable. Use the inner class {@link Builder} to construct a
 * new instance.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * </p>
 * 
 * <pre class="brush:java">
 * Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY).interval(2).build();
 * Recurrence copy = new Recurrence.Builder(rrule).interval(3).build();
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-38">RFC 5545
 * p.38-45</a>
 */
public final class Recurrence {
	private final Frequency frequency;
	private final Integer interval;
	private final Integer count;
	private final ICalDate until;
	private final List<Integer> bySecond;
	private final List<Integer> byMinute;
	private final List<Integer> byHour;
	private final List<Integer> byMonthDay;
	private final List<Integer> byYearDay;
	private final List<Integer> byWeekNo;
	private final List<Integer> byMonth;
	private final List<Integer> bySetPos;
	private final List<ByDay> byDay;
	private final DayOfWeek workweekStarts;
	private final Map<LaciToken, List<String>> xrules;

	private Recurrence(Builder builder) {
		frequency = builder.frequency;
		interval = builder.interval;
		count = builder.count;
		until = builder.until;
		bySecond = Collections.unmodifiableList(builder.bySecond);
		byMinute = Collections.unmodifiableList(builder.byMinute);
		byHour = Collections.unmodifiableList(builder.byHour);
		byMonthDay = Collections.unmodifiableList(builder.byMonthDay);
		byYearDay = Collections.unmodifiableList(builder.byYearDay);
		byWeekNo = Collections.unmodifiableList(builder.byWeekNo);
		byMonth = Collections.unmodifiableList(builder.byMonth);
		bySetPos = Collections.unmodifiableList(builder.bySetPos);
		byDay = Collections.unmodifiableList(builder.byDay);
		workweekStarts = builder.workweekStarts;
		xrules = Collections.unmodifiableMap(builder.xrules.getMap());
	}

	/**
	 * Gets the frequency.
	 * @return the frequency or null if not set
	 */
	public Frequency getFrequency() {
		return frequency;
	}

	/**
	 * Gets the date that the recurrence stops.
	 * @return the date or null if not set
	 */
	public ICalDate getUntil() {
		return (until == null) ? null : new ICalDate(until);
	}

	/**
	 * Gets the number of times the rule will be repeated.
	 * @return the number of times to repeat the rule or null if not set
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * Gets how often the rule repeats, in relation to the frequency.
	 * @return the repetition interval or null if not set
	 */
	public Integer getInterval() {
		return interval;
	}

	/**
	 * Gets the BYSECOND rule part.
	 * @return the BYSECOND rule part or empty list if not set
	 */
	public List<Integer> getBySecond() {
		return bySecond;
	}

	/**
	 * Gets the BYMINUTE rule part.
	 * @return the BYMINUTE rule part or empty list if not set
	 */
	public List<Integer> getByMinute() {
		return byMinute;
	}

	/**
	 * Gets the BYHOUR rule part.
	 * @return the BYHOUR rule part or empty list if not set
	 */
	public List<Integer> getByHour() {
		return byHour;
	}

	/**
	 * Gets the day components of the BYDAY rule part.
	 * @return the day components of the BYDAY rule part or empty list if not
	 * set
	 */
	public List<ByDay> getByDay() {
		return byDay;
	}

	/**
	 * Gets the BYMONTHDAY rule part.
	 * @return the BYMONTHDAY rule part or empty list if not set
	 */
	public List<Integer> getByMonthDay() {
		return byMonthDay;
	}

	/**
	 * Gets the BYYEARDAY rule part.
	 * @return the BYYEARDAY rule part or empty list if not set
	 */
	public List<Integer> getByYearDay() {
		return byYearDay;
	}

	/**
	 * Gets the BYWEEKNO rule part.
	 * @return the BYWEEKNO rule part or empty list if not set
	 */
	public List<Integer> getByWeekNo() {
		return byWeekNo;
	}

	/**
	 * Gets the BYMONTH rule part.
	 * @return the BYMONTH rule part or empty list if not set
	 */
	public List<Integer> getByMonth() {
		return byMonth;
	}

	/**
	 * Gets the BYSETPOS rule part.
	 * @return the BYSETPOS rule part or empty list if not set
	 */
	public List<Integer> getBySetPos() {
		return bySetPos;
	}

	/**
	 * Gets the day that the work week starts.
	 * @return the day that the work week starts or null if not set
	 */
	public DayOfWeek getWorkweekStarts() {
		return workweekStarts;
	}

	/**
	 * Gets the non-standard rule parts.
	 * @return the non-standard rule parts
	 */
	public Map<LaciToken, List<String>> getXRules() {
		return xrules;
	}

	/**
	 * Creates an iterator that computes the dates defined by this recurrence.
	 * @param startDate the date that the recurrence starts (typically, the
	 * value of the {@link DateStart} property)
	 * @param timezone the timezone to iterate in (typically, the timezone
	 * associated with the {@link DateStart} property). This is needed in order
	 * to adjust for when the iterator passes over a daylight savings boundary.
	 * @return the iterator
	 * @see <a
	 * href="https://code.google.com/p/google-rfc-2445/">google-rfc-2445</a>
	 */
	public DateIterator getDateIterator(Date startDate, TimeZone timezone) {
		return getDateIterator(new ICalDate(startDate), timezone);
	}

	/**
	 * Creates an iterator that computes the dates defined by this recurrence.
	 * @param startDate the date that the recurrence starts (typically, the
	 * value of the {@link DateStart} property)
	 * @param timezone the timezone to iterate in (typically, the timezone
	 * associated with the {@link DateStart} property). This is needed in order
	 * to adjust for when the iterator passes over a daylight savings boundary.
	 * @return the iterator
	 * @see <a
	 * href="https://code.google.com/p/google-rfc-2445/">google-rfc-2445</a>
	 */
	public DateIterator getDateIterator(ICalDate startDate, TimeZone timezone) {
		RecurrenceIterator iterator = Google2445Utils.createRecurrenceIterator(this, startDate, timezone);
		return DateIteratorFactory.createDateIterator(iterator);
	}

	@Override
	public int hashCode() {
		return Objects.hash(byDay, byHour, byMinute, byMonth, byMonthDay, bySecond, bySetPos, byWeekNo, byYearDay, count, frequency, interval, until, workweekStarts, xrules);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Recurrence other = (Recurrence) obj;
		return Objects.equals(byDay, other.byDay) && Objects.equals(byHour, other.byHour) && Objects.equals(byMinute, other.byMinute) && Objects.equals(byMonth, other.byMonth) && Objects.equals(byMonthDay, other.byMonthDay) && Objects.equals(bySecond, other.bySecond) && Objects.equals(bySetPos, other.bySetPos) && Objects.equals(byWeekNo, other.byWeekNo) && Objects.equals(byYearDay, other.byYearDay) && Objects.equals(count, other.count) && frequency == other.frequency && Objects.equals(interval, other.interval) && Objects.equals(until, other.until) && workweekStarts == other.workweekStarts && Objects.equals(xrules, other.xrules);
	}

	/**
	 * Constructs {@link Recurrence} objects.
	 * @author Michael Angstadt
	 */
	public static class Builder {
		private Frequency frequency;
		private Integer interval;
		private Integer count;
		private ICalDate until;
		private List<Integer> bySecond;
		private List<Integer> byMinute;
		private List<Integer> byHour;
		private List<ByDay> byDay;
		private List<Integer> byMonthDay;
		private List<Integer> byYearDay;
		private List<Integer> byWeekNo;
		private List<Integer> byMonth;
		private List<Integer> bySetPos;
		private DayOfWeek workweekStarts;
		private ListMultimap<LaciToken, String> xrules;

		/**
		 * Constructs a new builder.
		 * @param frequency the recurrence frequency
		 */
		public Builder(Frequency frequency) {
			this.frequency = frequency;
			bySecond = new ArrayList<>(0);
			byMinute = new ArrayList<>(0);
			byHour = new ArrayList<>(0);
			byDay = new ArrayList<>(0);
			byMonthDay = new ArrayList<>(0);
			byYearDay = new ArrayList<>(0);
			byWeekNo = new ArrayList<>(0);
			byMonth = new ArrayList<>(0);
			bySetPos = new ArrayList<>(0);
			xrules = new ListMultimap<>(0);
		}

		/**
		 * Constructs a new builder
		 * @param recur the recurrence object to copy from
		 */
		public Builder(Recurrence recur) {
			frequency = recur.frequency;
			interval = recur.interval;
			count = recur.count;
			until = recur.until;
			bySecond = new ArrayList<>(recur.bySecond);
			byMinute = new ArrayList<>(recur.byMinute);
			byHour = new ArrayList<>(recur.byHour);
			byDay = new ArrayList<>(recur.byDay);
			byMonthDay = new ArrayList<>(recur.byMonthDay);
			byYearDay = new ArrayList<>(recur.byYearDay);
			byWeekNo = new ArrayList<>(recur.byWeekNo);
			byMonth = new ArrayList<>(recur.byMonth);
			bySetPos = new ArrayList<>(recur.bySetPos);
			workweekStarts = recur.workweekStarts;

			Map<LaciToken, List<String>> map = new HashMap<>(recur.xrules);
			xrules = new ListMultimap<>(map);
		}

		/**
		 * Sets the frequency.
		 * @param frequency the frequency
		 * @return this
		 */
		public Builder frequency(Frequency frequency) {
			this.frequency = frequency;
			return this;
		}

		/**
		 * Sets the date that the recurrence stops. Note that the UNTIL and
		 * COUNT fields cannot both be defined within the same rule.
		 * @param until the date that the recurrence stops
		 * @return this
		 */
		public Builder until(ICalDate until) {
			this.until = (until == null) ? null : new ICalDate(until);
			return this;
		}

		/**
		 * Sets the date that the recurrence stops. Note that the UNTIL and
		 * COUNT fields cannot both be defined within the same rule.
		 * @param until the date (time component will be included)
		 * @return this
		 */
		public Builder until(Date until) {
			return until(until, true);
		}

		/**
		 * Sets the date that the recurrence stops. Note that the UNTIL and
		 * COUNT fields cannot both be defined within the same rule.
		 * @param until the date
		 * @param hasTime true to include the time component, false if it's
		 * strictly a date
		 * @return this
		 */
		public Builder until(Date until, boolean hasTime) {
			this.until = new ICalDate(until, hasTime);
			return this;
		}

		/**
		 * Sets the number of times the rule will be repeated. Note that the
		 * UNTIL and COUNT fields cannot both be defined within the same rule.
		 * @param count the number of times to repeat the rule
		 * @return this
		 */
		public Builder count(Integer count) {
			this.count = count;
			return this;
		}

		/**
		 * Sets how often the rule repeats in relation to the frequency.
		 * @param interval the repetition interval
		 * @return this
		 */
		public Builder interval(Integer interval) {
			this.interval = interval;
			return this;
		}

		/**
		 * Adds one or more BYSECOND rule parts.
		 * @param seconds the seconds to add
		 * @return this
		 */
		public Builder bySecond(Integer... seconds) {
			return bySecond(Arrays.asList(seconds));
		}

		/**
		 * Adds one or more BYSECOND rule parts.
		 * @param seconds the seconds to add
		 * @return this
		 */
		public Builder bySecond(Collection<Integer> seconds) {
			bySecond.addAll(seconds);
			return this;
		}

		/**
		 * Adds one or more BYMINUTE rule parts.
		 * @param minutes the minutes to add
		 * @return this
		 */
		public Builder byMinute(Integer... minutes) {
			return byMinute(Arrays.asList(minutes));
		}

		/**
		 * Adds one or more BYMINUTE rule parts.
		 * @param minutes the minutes to add
		 * @return this
		 */
		public Builder byMinute(Collection<Integer> minutes) {
			byMinute.addAll(minutes);
			return this;
		}

		/**
		 * Adds one or more BYHOUR rule parts.
		 * @param hours the hours to add
		 * @return this
		 */
		public Builder byHour(Integer... hours) {
			return byHour(Arrays.asList(hours));
		}

		/**
		 * Adds one or more BYHOUR rule parts.
		 * @param hours the hours to add
		 * @return this
		 */
		public Builder byHour(Collection<Integer> hours) {
			this.byHour.addAll(hours);
			return this;
		}

		/**
		 * Adds one or more BYMONTHDAY rule parts.
		 * @param monthDays the month days to add
		 * @return this
		 */
		public Builder byMonthDay(Integer... monthDays) {
			return byMonthDay(Arrays.asList(monthDays));
		}

		/**
		 * Adds one or more BYMONTHDAY rule parts.
		 * @param monthDays the month days to add
		 * @return this
		 */
		public Builder byMonthDay(Collection<Integer> monthDays) {
			byMonthDay.addAll(monthDays);
			return this;
		}

		/**
		 * Adds one or more BYYEARDAY rule parts.
		 * @param yearDays the year days to add
		 * @return this
		 */
		public Builder byYearDay(Integer... yearDays) {
			return byYearDay(Arrays.asList(yearDays));
		}

		/**
		 * Adds one or more BYYEARDAY rule parts.
		 * @param yearDays the year days to add
		 * @return this
		 */
		public Builder byYearDay(Collection<Integer> yearDays) {
			byYearDay.addAll(yearDays);
			return this;
		}

		/**
		 * Adds one or more BYWEEKNO rule parts.
		 * @param weekNumbers the week numbers to add
		 * @return this
		 */
		public Builder byWeekNo(Integer... weekNumbers) {
			return byWeekNo(Arrays.asList(weekNumbers));
		}

		/**
		 * Adds one or more BYWEEKNO rule parts.
		 * @param weekNumbers the week numbers to add
		 * @return this
		 */
		public Builder byWeekNo(Collection<Integer> weekNumbers) {
			byWeekNo.addAll(weekNumbers);
			return this;
		}

		/**
		 * Adds one or more BYMONTH rule parts.
		 * @param months the months to add
		 * @return this
		 */
		public Builder byMonth(Integer... months) {
			return byMonth(Arrays.asList(months));
		}

		/**
		 * Adds one or more BYMONTH rule parts.
		 * @param months the months to add
		 * @return this
		 */
		public Builder byMonth(Collection<Integer> months) {
			byMonth.addAll(months);
			return this;
		}

		/**
		 * Adds one or more BYSETPOS rule parts.
		 * @param positions the values to add
		 * @return this
		 */
		public Builder bySetPos(Integer... positions) {
			return bySetPos(Arrays.asList(positions));
		}

		/**
		 * Adds one or more BYSETPOS rule parts.
		 * @param positions the values to add
		 * @return this
		 */
		public Builder bySetPos(Collection<Integer> positions) {
			bySetPos.addAll(positions);
			return this;
		}

		/**
		 * Adds one or more BYDAY rule parts.
		 * @param days the days to add
		 * @return this
		 */
		public Builder byDay(DayOfWeek... days) {
			return byDay(Arrays.asList(days));
		}

		/**
		 * Adds one or more BYDAY rule parts.
		 * @param days the days to add
		 * @return this
		 */
		public Builder byDay(Collection<DayOfWeek> days) {
			for (DayOfWeek day : days) {
				byDay(null, day);
			}
			return this;
		}

		/**
		 * Adds a BYDAY rule part.
		 * @param num the numeric component
		 * @param day the day to add
		 * @return this
		 */
		public Builder byDay(Integer num, DayOfWeek day) {
			byDay.add(new ByDay(num, day));
			return this;
		}

		/**
		 * Sets the day that the work week starts.
		 * @param day the day
		 * @return this
		 */
		public Builder workweekStarts(DayOfWeek day) {
			workweekStarts = day;
			return this;
		}

		/**
		 * Adds a non-standard rule part.
		 * @param name the name
		 * @param value the value or null to remove the rule part
		 * @return this
		 */
		public Builder xrule(String name, String value) {
			return xrule(new LaciToken(name), value);
		}

		/**
		 * Adds a non-standard rule part.
		 * @param name the name
		 * @param value the value or null to remove the rule part
		 * @return this
		 */
		public Builder xrule(LaciToken name, String value) {
			if (value == null) {
				xrules.removeAll(name);
			} else {
				xrules.put(name, value);
			}

			return this;
		}
		
		/**
		 * Adds non-standard rule parts.
		 * @param name the name
		 * @param values the values or null to remove the rule part
		 * @return this
		 */
		public Builder xrule(LaciToken name, List<String> values) {
			if (values == null) {
				xrules.removeAll(name);
			} else {
				xrules.putAll(name, values);
			}

			return this;
		}

		/**
		 * Builds the final {@link Recurrence} object.
		 * @return the object
		 */
		public Recurrence build() {
			return new Recurrence(this);
		}
	}
}
