package biweekly.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
 * 
 * <pre class="brush:java">
 * Recurrence rrule = new Recurrence.Builder(Frequency.WEEKLY).interval(2).build();
 * Recurrence copy = new Recurrence.Builder(rrule).interval(3).build();
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-38">RFC 5545
 * p.38-45</a>
 */
public final class Recurrence {
	private final Frequency frequency;
	private final Integer interval;
	private final Integer count;
	private final Date until;
	private final boolean untilHasTime;
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
	private final Map<String, List<String>> xrules;

	private Recurrence(Builder builder) {
		frequency = builder.frequency;
		interval = builder.interval;
		count = builder.count;
		until = builder.until;
		untilHasTime = builder.untilHasTime;
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

		Map<String, List<String>> map = builder.xrules.getMap();
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			String key = entry.getKey();
			List<String> value = entry.getValue();

			map.put(key, Collections.unmodifiableList(value));
		}
		xrules = Collections.unmodifiableMap(map);
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
	public Date getUntil() {
		return (until == null) ? null : new Date(until.getTime());
	}

	/**
	 * Determines whether the UNTIL date has a time component.
	 * @return true if it has a time component, false if it is strictly a date
	 */
	public boolean hasTimeUntilDate() {
		return untilHasTime;
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
	public Map<String, List<String>> getXRules() {
		return xrules;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((byDay == null) ? 0 : byDay.hashCode());
		result = prime * result + ((byHour == null) ? 0 : byHour.hashCode());
		result = prime * result + ((byMinute == null) ? 0 : byMinute.hashCode());
		result = prime * result + ((byMonth == null) ? 0 : byMonth.hashCode());
		result = prime * result + ((byMonthDay == null) ? 0 : byMonthDay.hashCode());
		result = prime * result + ((bySecond == null) ? 0 : bySecond.hashCode());
		result = prime * result + ((bySetPos == null) ? 0 : bySetPos.hashCode());
		result = prime * result + ((byWeekNo == null) ? 0 : byWeekNo.hashCode());
		result = prime * result + ((byYearDay == null) ? 0 : byYearDay.hashCode());
		result = prime * result + ((count == null) ? 0 : count.hashCode());
		result = prime * result + ((xrules == null) ? 0 : xrules.hashCode());
		result = prime * result + ((frequency == null) ? 0 : frequency.hashCode());
		result = prime * result + ((interval == null) ? 0 : interval.hashCode());
		result = prime * result + ((until == null) ? 0 : until.hashCode());
		result = prime * result + (untilHasTime ? 1231 : 1237);
		result = prime * result + ((workweekStarts == null) ? 0 : workweekStarts.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Recurrence other = (Recurrence) obj;
		if (byDay == null) {
			if (other.byDay != null) return false;
		} else if (!byDay.equals(other.byDay)) return false;
		if (byHour == null) {
			if (other.byHour != null) return false;
		} else if (!byHour.equals(other.byHour)) return false;
		if (byMinute == null) {
			if (other.byMinute != null) return false;
		} else if (!byMinute.equals(other.byMinute)) return false;
		if (byMonth == null) {
			if (other.byMonth != null) return false;
		} else if (!byMonth.equals(other.byMonth)) return false;
		if (byMonthDay == null) {
			if (other.byMonthDay != null) return false;
		} else if (!byMonthDay.equals(other.byMonthDay)) return false;
		if (bySecond == null) {
			if (other.bySecond != null) return false;
		} else if (!bySecond.equals(other.bySecond)) return false;
		if (bySetPos == null) {
			if (other.bySetPos != null) return false;
		} else if (!bySetPos.equals(other.bySetPos)) return false;
		if (byWeekNo == null) {
			if (other.byWeekNo != null) return false;
		} else if (!byWeekNo.equals(other.byWeekNo)) return false;
		if (byYearDay == null) {
			if (other.byYearDay != null) return false;
		} else if (!byYearDay.equals(other.byYearDay)) return false;
		if (count == null) {
			if (other.count != null) return false;
		} else if (!count.equals(other.count)) return false;
		if (xrules == null) {
			if (other.xrules != null) return false;
		} else if (!xrules.equals(other.xrules)) return false;
		if (frequency != other.frequency) return false;
		if (interval == null) {
			if (other.interval != null) return false;
		} else if (!interval.equals(other.interval)) return false;
		if (until == null) {
			if (other.until != null) return false;
		} else if (!until.equals(other.until)) return false;
		if (untilHasTime != other.untilHasTime) return false;
		if (workweekStarts != other.workweekStarts) return false;
		return true;
	}

	/**
	 * Represents the frequency at which a recurrence rule repeats itself.
	 * @author Michael Angstadt
	 */
	public static enum Frequency {
		SECONDLY, MINUTELY, HOURLY, DAILY, WEEKLY, MONTHLY, YEARLY
	}

	/**
	 * Represents each of the seven days of the week.
	 * @author Michael Angstadt
	 */
	public static enum DayOfWeek {
		MONDAY("MO", Calendar.MONDAY), TUESDAY("TU", Calendar.TUESDAY), WEDNESDAY("WE", Calendar.WEDNESDAY), THURSDAY("TH", Calendar.THURSDAY), FRIDAY("FR", Calendar.FRIDAY), SATURDAY("SA", Calendar.SATURDAY), SUNDAY("SU", Calendar.SUNDAY);

		private final String abbr;
		private final int calendarConstant;

		private DayOfWeek(String abbr, int calendarConstant) {
			this.abbr = abbr;
			this.calendarConstant = calendarConstant;
		}

		/**
		 * Gets the day's abbreviation.
		 * @return the abbreviation (e.g. "MO" for Monday)
		 */
		public String getAbbr() {
			return abbr;
		}

		public int getCalendarConstant() {
			return calendarConstant;
		}

		/**
		 * Gets a day by its abbreviation.
		 * @param abbr the abbreviation (case-insensitive, e.g. "MO" for Monday)
		 * @return the day or null if not found
		 */
		public static DayOfWeek valueOfAbbr(String abbr) {
			for (DayOfWeek day : values()) {
				if (day.abbr.equalsIgnoreCase(abbr)) {
					return day;
				}
			}
			return null;
		}
	}

	public static class ByDay {
		private final Integer num;
		private final DayOfWeek day;

		public ByDay(DayOfWeek day) {
			this(null, day);
		}

		public ByDay(Integer num, DayOfWeek day) {
			this.num = num;
			this.day = day;
		}

		public Integer getNum() {
			return num;
		}

		public DayOfWeek getDay() {
			return day;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((day == null) ? 0 : day.hashCode());
			result = prime * result + ((num == null) ? 0 : num.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			ByDay other = (ByDay) obj;
			if (day != other.day) return false;
			if (num == null) {
				if (other.num != null) return false;
			} else if (!num.equals(other.num)) return false;
			return true;
		}
	}

	/**
	 * Constructs {@link Recurrence} objects.
	 * @author Michael Angstadt
	 */
	public static class Builder {
		private Frequency frequency;
		private Integer interval;
		private Integer count;
		private Date until;
		private boolean untilHasTime;
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
		private ListMultimap<String, String> xrules;

		/**
		 * Constructs a new builder.
		 * @param frequency the recurrence frequency
		 */
		public Builder(Frequency frequency) {
			this.frequency = frequency;
			bySecond = new ArrayList<Integer>(0);
			byMinute = new ArrayList<Integer>(0);
			byHour = new ArrayList<Integer>(0);
			byDay = new ArrayList<ByDay>(0);
			byMonthDay = new ArrayList<Integer>(0);
			byYearDay = new ArrayList<Integer>(0);
			byWeekNo = new ArrayList<Integer>(0);
			byMonth = new ArrayList<Integer>(0);
			bySetPos = new ArrayList<Integer>(0);
			xrules = new ListMultimap<String, String>(0);
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
			untilHasTime = recur.untilHasTime;
			bySecond = new ArrayList<Integer>(recur.bySecond);
			byMinute = new ArrayList<Integer>(recur.byMinute);
			byHour = new ArrayList<Integer>(recur.byHour);
			byDay = new ArrayList<ByDay>(recur.byDay);
			byMonthDay = new ArrayList<Integer>(recur.byMonthDay);
			byYearDay = new ArrayList<Integer>(recur.byYearDay);
			byWeekNo = new ArrayList<Integer>(recur.byWeekNo);
			byMonth = new ArrayList<Integer>(recur.byMonth);
			bySetPos = new ArrayList<Integer>(recur.bySetPos);
			workweekStarts = recur.workweekStarts;
			xrules = new ListMultimap<String, String>(recur.xrules);
		}

		/**
		 * Sets the frequency
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
		 * @param until the date (time component is included)
		 * @return this
		 */
		public Builder until(Date until) {
			return until(until, until != null);
		}

		/**
		 * Sets the date that the recurrence stops. Note that the UNTIL and
		 * COUNT fields cannot both be defined within the same rule.
		 * @param until the date
		 * @param hasTime true if the date has a time component, false if it's
		 * strictly a date
		 * @return this
		 */
		public Builder until(Date until, boolean hasTime) {
			if (until == null) {
				this.until = null;
				this.untilHasTime = false;
			} else {
				this.until = new Date(until.getTime());
				this.untilHasTime = hasTime;
			}
			return this;
		}

		/**
		 * Gets the number of times the rule will be repeated. Note that the
		 * UNTIL and COUNT fields cannot both be defined within the same rule.
		 * @param count the number of times to repeat the rule
		 * @return this
		 */
		public Builder count(Integer count) {
			this.count = count;
			return this;
		}

		/**
		 * Gets how often the rule repeats, in relation to the frequency.
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
			name = name.toUpperCase();

			if (value == null) {
				xrules.removeAll(name);
			} else {
				xrules.put(name, value);
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
