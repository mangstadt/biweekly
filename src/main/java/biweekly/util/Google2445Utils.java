package biweekly.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import biweekly.component.ICalComponent;
import biweekly.property.DateStart;
import biweekly.property.ExceptionDates;
import biweekly.property.ExceptionRule;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.property.ValuedProperty;
import biweekly.util.Recurrence.ByDay;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;

import com.google.ical.compat.javautil.DateIterator;
import com.google.ical.compat.javautil.DateIteratorFactory;
import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.DateValueImpl;
import com.google.ical.values.RDateList;
import com.google.ical.values.RRule;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
public final class Google2445Utils {
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
	 * Converts an {@link ICalDate} object to a google-rfc-2445
	 * {@link DateValue} object. The {@link DateValue} object contains the date
	 * value formatted in UTC.
	 * @param date the date object
	 * @return the google-rfc-2445 object (in UTC)
	 */
	public static DateValue convertUtc(ICalDate date) {
		Calendar c = Calendar.getInstance(utc());
		c.setTime(date);
		if (date.hasTime()) {
			return new DateTimeValueImpl(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND));
		} else {
			return new DateValueImpl(c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE));
		}
	}

	/**
	 * Creates a recurrence iterator based on the given recurrence rule.
	 * @param recurrence the recurrence rule
	 * @param start the start date
	 * @return the recurrence iterator
	 */
	public static RecurrenceIterator createRecurrenceIterator(Recurrence recurrence, ICalDate start) {
		DateValue startValue = convertUtc(start);
		RRule googleRecurrence = convert(recurrence);
		return RecurrenceIteratorFactory.createRecurrenceIterator(googleRecurrence, startValue, utc());
	}

	/**
	 * Creates a recurrence iterator based on the given list of dates.
	 * @param dates the dates
	 * @return the recurrence iterator
	 */
	public static RecurrenceIterator createRecurrenceIterator(List<ICalDate> dates) {
		RDateList list = new RDateList(utc());
		List<DateValue> dateValues = new ArrayList<DateValue>(dates.size());
		for (ICalDate date : dates) {
			dateValues.add(convertUtc(date));
		}
		list.setDatesUtc(dateValues.toArray(new DateValue[0]));

		return RecurrenceIteratorFactory.createRecurrenceIterator(list);
	}

	/**
	 * <p>
	 * Creates an iterator that computes the dates defined by the
	 * {@link RecurrenceRule} and {@link RecurrenceDates} properties (if
	 * present), and excludes those dates which are defined by the
	 * {@link ExceptionRule} and {@link ExceptionDates} properties (if present).
	 * </p>
	 * <p>
	 * In order for {@link RecurrenceRule} and {@link ExceptionRule} properties
	 * to be included in this iterator, a {@link DateStart} property must be
	 * defined.
	 * </p>
	 * <p>
	 * {@link Period} values in {@link RecurrenceDates} properties are not
	 * supported and are ignored.
	 * </p>
	 * @return the iterator
	 */
	public static DateIterator getDateIterator(ICalComponent component) {
		DateStart dtstart = component.getProperty(DateStart.class);
		ICalDate start = ValuedProperty.getValue(dtstart);

		/////////////INCLUDE/////////////

		List<RecurrenceIterator> include = new ArrayList<RecurrenceIterator>();

		if (start != null) {
			for (RecurrenceRule rrule : component.getProperties(RecurrenceRule.class)) {
				Recurrence recurrence = ValuedProperty.getValue(rrule);
				if (recurrence != null) {
					include.add(createRecurrenceIterator(recurrence, start));
				}
			}
		}

		List<ICalDate> allDates = new ArrayList<ICalDate>();
		for (RecurrenceDates rdate : component.getProperties(RecurrenceDates.class)) {
			allDates.addAll(rdate.getDates());
		}
		if (!allDates.isEmpty()) {
			include.add(createRecurrenceIterator(allDates));
		}

		if (include.isEmpty()) {
			if (start == null) {
				return new EmptyDateIterator();
			}
			include.add(createRecurrenceIterator(Arrays.asList(start)));
		}

		/////////////EXCLUDE/////////////

		List<RecurrenceIterator> exclude = new ArrayList<RecurrenceIterator>();

		if (start != null) {
			for (ExceptionRule exrule : component.getProperties(ExceptionRule.class)) {
				Recurrence recurrence = ValuedProperty.getValue(exrule);
				if (recurrence != null) {
					exclude.add(createRecurrenceIterator(recurrence, start));
				}
			}
		}

		allDates = new ArrayList<ICalDate>();
		for (ExceptionDates exdate : component.getProperties(ExceptionDates.class)) {
			allDates.addAll(exdate.getValues());
		}
		if (!allDates.isEmpty()) {
			exclude.add(createRecurrenceIterator(allDates));
		}

		/////////////JOIN/////////////

		RecurrenceIterator includeJoined = join(include);
		if (exclude.isEmpty()) {
			return DateIteratorFactory.createDateIterator(includeJoined);
		}

		RecurrenceIterator excludeJoined = join(exclude);
		RecurrenceIterator iterator = RecurrenceIteratorFactory.except(includeJoined, excludeJoined);
		return DateIteratorFactory.createDateIterator(iterator);
	}

	/**
	 * Creates a single {@link RecurrenceIterator} that is a union of the given
	 * iterators.
	 * @param iterators the iterators
	 * @return the union of the given iterators
	 * @see RecurrenceIteratorFactory#join
	 */
	private static RecurrenceIterator join(List<RecurrenceIterator> iterators) {
		if (iterators.size() == 1) {
			return iterators.get(0);
		}
		RecurrenceIterator first = iterators.get(0);
		List<RecurrenceIterator> rest = iterators.subList(1, iterators.size());
		return RecurrenceIteratorFactory.join(first, rest.toArray(new RecurrenceIterator[0]));
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

	private static TimeZone utc() {
		return TimeZone.getTimeZone("UTC");
	}

	/**
	 * A {@link DateIterator} with nothing in it.
	 */
	public static class EmptyDateIterator implements DateIterator {
		public boolean hasNext() {
			return false;
		}

		public Date next() {
			throw new NoSuchElementException();
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}

		public void advanceTo(Date newStartUtc) {
			//empty
		}
	}

	private Google2445Utils() {
		//hide
	}
}
