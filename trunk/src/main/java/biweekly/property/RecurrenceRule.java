package biweekly.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.component.ICalComponent;

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
 * Defines how often a component repeats.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre>
 * //bi-weekly
 * RecurrenceRule rrule = new RecurrenceRule(Frequency.WEEKLY);
 * rrule.setInterval(2);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-122">RFC 5545
 * p.122-32</a>
 */
public class RecurrenceRule extends ICalProperty {
	private Frequency frequency;
	private Integer interval;
	private Integer count;
	private Date until;
	private boolean untilHasTime;
	private List<Integer> bySecond = new ArrayList<Integer>();
	private List<Integer> byMinute = new ArrayList<Integer>();
	private List<Integer> byHour = new ArrayList<Integer>();
	private List<Integer> byMonthDay = new ArrayList<Integer>();
	private List<Integer> byYearDay = new ArrayList<Integer>();
	private List<Integer> byWeekNo = new ArrayList<Integer>();
	private List<Integer> byMonth = new ArrayList<Integer>();
	private List<Integer> bySetPos = new ArrayList<Integer>();
	private List<DayOfWeek> byDay = new ArrayList<DayOfWeek>();
	private List<Integer> byDayPrefixes = new ArrayList<Integer>();
	private DayOfWeek workweekStarts;

	/**
	 * Creates a new recurrence rule property.
	 * @param frequency the frequency of the recurrence rule
	 */
	public RecurrenceRule(Frequency frequency) {
		setFrequency(frequency);
	}

	public Frequency getFrequency() {
		return frequency;
	}

	public void setFrequency(Frequency frequency) {
		this.frequency = frequency;
	}

	public Date getUntil() {
		return until;
	}

	public void setUntil(Date until) {
		setUntil(until, true);
	}

	public void setUntil(Date until, boolean hasTime) {
		this.until = until;
		untilHasTime = hasTime;
	}

	public boolean hasTimeUntilDate() {
		return untilHasTime;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getInterval() {
		return interval;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	public List<Integer> getBySecond() {
		return bySecond;
	}

	public void setBySecond(List<Integer> bySecond) {
		this.bySecond = bySecond;
	}

	public List<Integer> getByMinute() {
		return byMinute;
	}

	public void setByMinute(List<Integer> byMinute) {
		this.byMinute = byMinute;
	}

	public List<Integer> getByHour() {
		return byHour;
	}

	public void setByHour(List<Integer> byHour) {
		this.byHour = byHour;
	}

	public void addByDay(DayOfWeek day) {
		addByDay(null, day);
	}

	public void addByDay(Integer prefix, DayOfWeek day) {
		byDayPrefixes.add(prefix);
		byDay.add(day);
	}

	public List<DayOfWeek> getByDay() {
		return byDay;
	}

	public List<Integer> getByDayPrefixes() {
		return byDayPrefixes;
	}

	public List<Integer> getByMonthDay() {
		return byMonthDay;
	}

	public void setByMonthDay(List<Integer> byMonthDay) {
		this.byMonthDay = byMonthDay;
	}

	public List<Integer> getByYearDay() {
		return byYearDay;
	}

	public void setByYearDay(List<Integer> byYearDay) {
		this.byYearDay = byYearDay;
	}

	public List<Integer> getByWeekNo() {
		return byWeekNo;
	}

	public void setByWeekNo(List<Integer> byWeekNo) {
		this.byWeekNo = byWeekNo;
	}

	public List<Integer> getByMonth() {
		return byMonth;
	}

	public void setByMonth(List<Integer> byMonth) {
		this.byMonth = byMonth;
	}

	public List<Integer> getBySetPos() {
		return bySetPos;
	}

	public void setBySetPos(List<Integer> bySetPos) {
		this.bySetPos = bySetPos;
	}

	public DayOfWeek getWorkweekStarts() {
		return workweekStarts;
	}

	public void setWorkweekStarts(DayOfWeek workweekStarts) {
		this.workweekStarts = workweekStarts;
	}

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		if (frequency == null) {
			warnings.add("Frequency is not set (it is a required field).");
		}
		if (until != null && count != null) {
			warnings.add("\"Until\" and \"count\" cannot both be set.");
		}
	}

	/**
	 * Represents the frequency at which the recurrence rule repeats itself.
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
		MONDAY("MO"), TUESDAY("TU"), WEDNESDAY("WE"), THURSDAY("TH"), FRIDAY("FR"), SATURDAY("SA"), SUNDAY("SU");

		private final String abbr;

		private DayOfWeek(String abbr) {
			this.abbr = abbr;
		}

		/**
		 * Gets the day's abbreviation.
		 * @return the abbreviation (e.g. "MO" for Monday)
		 */
		public String getAbbr() {
			return abbr;
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
}
