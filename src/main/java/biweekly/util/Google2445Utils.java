package biweekly.util;

import java.util.ArrayList;
import java.util.List;

import biweekly.property.DateStart;
import biweekly.util.Recurrence.ByDay;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;

import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.RRule;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
 * Contains utility methods related to the google-rfc-2445 project.
 * @author Michael Angstadt
 * @see <a href="https://code.google.com/p/google-rfc-2445/">google-rfc-2445</a>
 */
public class Google2445Utils {
	/**
	 * Converts a biweekly {@link DateStart} object to a google-rfc-2445
	 * {@link DateTimeValue} object.
	 * @param dtstart the biweekly object
	 * @return the google-rfc-2445 object
	 */
	public static DateTimeValue convert(DateStart dtstart) {
		ICalDate value = dtstart.getValue();
		if (value == null) {
			return null;
		}

		DateTimeComponents raw = value.getRawComponents();
		if (raw == null) {
			raw = new DateTimeComponents(value);
		}
		return new DateTimeValueImpl(raw.getYear(), raw.getMonth(), raw.getDate(), raw.getHour(), raw.getMinute(), raw.getSecond());
	}

	/**
	 * Converts a biweekly {@link Recurrence} object to a google-rfc-2445
	 * {@link RRule} object.
	 * @param recur the biweekly recurrence object
	 * @return the google-rfc-2445 object
	 */
	public static RRule convert(Recurrence recur) {
		RRule googleRRule = new RRule();

		List<WeekdayNum> weekdayNums = new ArrayList<WeekdayNum>();
		for (ByDay byDay : recur.getByDay()) {
			Integer prefix = byDay.getNum();
			if (prefix == null) {
				prefix = 0;
			}

			weekdayNums.add(new WeekdayNum(prefix, convert(byDay.getDay())));
		}
		googleRRule.setByDay(weekdayNums);

		googleRRule.setByYearDay(toArray(recur.getByYearDay()));
		googleRRule.setByMonth(toArray(recur.getByMonth()));
		googleRRule.setByWeekNo(toArray(recur.getByWeekNo()));
		googleRRule.setByMonthDay(toArray(recur.getByMonthDay()));
		googleRRule.setByHour(toArray(recur.getByHour()));
		googleRRule.setByMinute(toArray(recur.getByMinute()));
		googleRRule.setBySecond(toArray(recur.getBySecond()));
		googleRRule.setBySetPos(toArray(recur.getBySetPos()));

		Integer count = recur.getCount();
		if (count != null) {
			googleRRule.setCount(count);
		}

		Frequency freq = recur.getFrequency();
		if (freq != null) {
			googleRRule.setFreq(convert(freq));
		}

		Integer interval = recur.getInterval();
		if (interval != null) {
			googleRRule.setInterval(interval);
		}

		ICalDate until = recur.getUntil();
		if (until != null) {
			googleRRule.setUntil(convert(until));
		}

		DayOfWeek workweekStarts = recur.getWorkweekStarts();
		if (workweekStarts != null) {
			googleRRule.setWkSt(convert(workweekStarts));
		}

		return googleRRule;
	}

	/**
	 * Converts a biweekly {@link DayOfWeek} object to a google-rfc-2445
	 * {@link Weekday} object.
	 * @param day the biweekly object
	 * @return the google-rfc-2445 object
	 */
	public static Weekday convert(DayOfWeek day) {
		switch (day) {
		case SUNDAY:
			return Weekday.SU;
		case MONDAY:
			return Weekday.MO;
		case TUESDAY:
			return Weekday.TU;
		case WEDNESDAY:
			return Weekday.WE;
		case THURSDAY:
			return Weekday.TH;
		case FRIDAY:
			return Weekday.FR;
		case SATURDAY:
			return Weekday.SA;
		default:
			return null;
		}
	}

	/**
	 * Converts a biweekly {@link Frequency} object to a google-rfc-2445
	 * {@link com.google.ical.values.Frequency Frequency} object.
	 * @param freq the biweekly object
	 * @return the google-rfc-2445 object
	 */
	public static com.google.ical.values.Frequency convert(Frequency freq) {
		switch (freq) {
		case YEARLY:
			return com.google.ical.values.Frequency.YEARLY;
		case MONTHLY:
			return com.google.ical.values.Frequency.MONTHLY;
		case WEEKLY:
			return com.google.ical.values.Frequency.WEEKLY;
		case DAILY:
			return com.google.ical.values.Frequency.DAILY;
		case HOURLY:
			return com.google.ical.values.Frequency.HOURLY;
		case MINUTELY:
			return com.google.ical.values.Frequency.MINUTELY;
		case SECONDLY:
			return com.google.ical.values.Frequency.SECONDLY;
		default:
			return null;
		}
	}

	/**
	 * Converts an {@link ICalDate} object to a google-rfc-2445
	 * {@link DateValue} object.
	 * @param date the Java date object
	 * @return the google-rfc-2445 object
	 */
	public static DateValue convert(ICalDate date) {
		DateTimeComponents raw = date.getRawComponents();
		if (raw == null) {
			raw = new DateTimeComponents(date);
		}

		//@formatter:off
		return new DateTimeValueImpl(
			raw.getYear(),
			raw.getMonth(),
			raw.getDate(),
			raw.getHour(),
			raw.getMinute(),
			raw.getSecond()
		);
		//@formatter:on
	}

	/**
	 * Converts an Integer list to an int array.
	 * @param list the Integer list
	 * @return the int array
	 */
	private static int[] toArray(List<Integer> list) {
		int[] array = new int[list.size()];
		int i = 0;
		for (Integer intObj : list) {
			array[i++] = (intObj == null) ? 0 : intObj;
		}
		return array;
	}

	private Google2445Utils() {
		//hide
	}
}
