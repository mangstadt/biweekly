package biweekly.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import biweekly.component.DaylightSavingsTime;
import biweekly.component.Observance;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.property.DateStart;
import biweekly.property.ExceptionDates;
import biweekly.property.ExceptionRule;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceProperty;
import biweekly.property.RecurrenceRule;
import biweekly.property.UtcOffsetProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.ByDay;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;

import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.RRule;
import com.google.ical.values.Weekday;
import com.google.ical.values.WeekdayNum;

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
 * Converts a {@link VTimezone} component to a Java {@link TimeZone} object.
 * @author Michael Angstadt
 */
public class ICalTimeZone extends TimeZone {
	private static final long serialVersionUID = 1529307906194773299L;

	private final VTimezone component;

	/**
	 * Creates a new timezone based on an iCalendar VTIMEZONE component.
	 * @param component the VTIMEZONE component to wrap
	 */
	public ICalTimeZone(VTimezone component) {
		this.component = component;
		setID(component.getTimezoneId().getValue());
	}

	/**
	 * Gets the VTIMEZONE component that is being wrapped. Modifications made to
	 * the component will effect this timezone object.
	 * @return the VTIMEZONE component
	 */
	public VTimezone getComponent() {
		return component;
	}

	@Override
	public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
		int hour = millis / 1000 / 60 / 60;
		millis -= hour * 1000 * 60 * 60;
		int minute = millis / 1000 / 60;
		millis -= minute * 1000 * 60;
		int second = millis / 1000;

		Observance observance = getObservance(year, month + 1, day, hour, minute, second);
		if (observance == null) {
			//find the first observance that has a DTSTART property and has a TZOFFSETFROM property
			for (Observance o : getSortedObservances()) {
				if (hasDateStart(o) && hasTimezoneOffsetFrom(o)) {
					return o.getTimezoneOffsetFrom().getValue().toMillis();
				}
			}
			return 0;
		}

		return hasTimezoneOffsetTo(observance) ? observance.getTimezoneOffsetTo().getValue().toMillis() : 0;
	}

	@Override
	public int getRawOffset() {
		Observance observance = getObservance(new Date());
		if (observance == null) {
			//return the offset of the first STANDARD component
			for (Observance o : getSortedObservances()) {
				if (o instanceof StandardTime && hasTimezoneOffsetTo(o)) {
					return o.getTimezoneOffsetTo().getValue().toMillis();
				}
			}
			return 0;
		}

		UtcOffsetProperty offset;
		if (observance instanceof StandardTime) {
			offset = observance.getTimezoneOffsetTo();
		} else {
			offset = observance.getTimezoneOffsetFrom();
		}

		return offset.getValue().toMillis();
	}

	@Override
	public boolean inDaylightTime(Date date) {
		if (!useDaylightTime()) {
			return false;
		}

		Observance observance = getObservance(date);
		return (observance == null) ? false : (observance instanceof DaylightSavingsTime);

		//		if (observance != null) {
		//			return (observance instanceof DaylightSavingsTime);
		//		}
		//
		//		//find the first observance that has a DTSTART property
		//		for (Observance o : getSortedObservances()) {
		//			if (hasDateStart(o)) {
		//				return !(o instanceof DaylightSavingsTime);
		//			}
		//		}
		//		return false;
	}

	@Override
	public void setRawOffset(int offset) {
		throw new UnsupportedOperationException("Unable to set the raw offset.  Modify the VTIMEZONE component instead.");
	}

	@Override
	public boolean useDaylightTime() {
		return !component.getDaylightSavingsTime().isEmpty();
	}

	private Observance getObservance(Date date) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")); //TODO should this be local TZ?
		//Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);

		return getObservance(year, month, day, hour, minute, second);
	}

	private Observance getObservance(int year, int month, int day, int hour, int minute, int second) {
		List<Observance> observances = getSortedObservances();
		if (observances.isEmpty()) {
			return null;
		}

		//find the observance that is closest to the given date
		Observance closest = null;
		{
			DateValue givenTime = new DateTimeValueImpl(year, month, day, hour, minute, second);
			DateValue closestValue = null;
			for (Observance observance : observances) {
				//skip observances that start after the given time
				DateStart dtstart = observance.getDateStart();
				if (dtstart != null) {
					DateTimeValue dtstartValue = convert(dtstart);
					if (dtstartValue != null && dtstartValue.compareTo(givenTime) > 0) {
						continue;
					}
				}

				RecurrenceIterator it = createIterator(observance);
				DateValue prev = null;
				while (it.hasNext()) {
					DateValue cur = it.next();
					if (givenTime.compareTo(cur) < 0) {
						//break if we have passed the given time
						break;
					}

					prev = cur;
				}

				if (prev != null && (closestValue == null || closestValue.compareTo(prev) < 0)) {
					closestValue = prev;
					closest = observance;
				}
			}
		}

		return closest;

		//		if (closest == null) {
		//			//find the first observance that has a DTSTART property and has a TZOFFSETFROM property
		//			for (Observance observance : observances) {
		//				if (hasDateStart(observance) && hasTimezoneOffsetFrom(observance)) {
		//					return new Result(observance, observance.getTimezoneOffsetFrom().getValue().toMillis());
		//				}
		//			}
		//			return null;
		//		}
		//
		//		return new Result(closest, closest.getTimezoneOffsetTo().getValue().toMillis());
	}

	private boolean hasDateStart(Observance observance) {
		DateStart dtstart = observance.getDateStart();
		return dtstart != null && (dtstart.getRawComponents() != null || dtstart.getValue() != null);
	}

	private boolean hasTimezoneOffsetFrom(Observance observance) {
		UtcOffsetProperty offset = observance.getTimezoneOffsetFrom();
		return offset != null && offset.getValue() != null;
	}

	private boolean hasTimezoneOffsetTo(Observance observance) {
		UtcOffsetProperty offset = observance.getTimezoneOffsetTo();
		return offset != null && offset.getValue() != null;
	}

	/**
	 * Gets all observances sorted by {@link DateStart}.
	 * @return the sorted observances
	 */
	List<Observance> getSortedObservances() {
		List<Observance> observances = new ArrayList<Observance>();
		observances.addAll(component.getStandardTimes());
		observances.addAll(component.getDaylightSavingsTime());

		Collections.sort(observances, new Comparator<Observance>() {
			public int compare(Observance left, Observance right) {
				DateStart startLeft = left.getDateStart();
				DateStart startRight = right.getDateStart();
				if (startLeft == null && startRight == null) {
					return 0;
				}
				if (startLeft == null) {
					return -1;
				}
				if (startRight == null) {
					return 1;
				}

				return startLeft.getRawComponents().compareTo(startRight.getRawComponents());
			}
		});

		return observances;
	}

	/**
	 * Creates an iterator which iterates over each of the dates in an
	 * observance.
	 * @param observance the observance
	 * @return the iterator
	 */
	RecurrenceIterator createIterator(Observance observance) {
		List<RecurrenceIterator> inclusions = new ArrayList<RecurrenceIterator>();
		List<RecurrenceIterator> exclusions = new ArrayList<RecurrenceIterator>();

		//add DTSTART property
		DateStart dtstart = observance.getDateStart();
		DateValue dtstartValue = null;
		if (dtstart != null) {
			Date dtstartDate = dtstart.getValue();
			DateTimeComponents dtstartRaw = dtstart.getRawComponents();
			if (dtstartRaw == null && dtstartDate != null) {
				dtstartRaw = new DateTimeComponents(dtstartDate);
			}

			inclusions.add(new RawDateRecurrenceIterator(Arrays.asList(dtstartRaw)));
			dtstartValue = convert(dtstart);

			TimeZone utc = TimeZone.getTimeZone("UTC");

			//add RRULE properties
			for (RecurrenceRule rrule : observance.getProperties(RecurrenceRule.class)) {
				RRule rruleValue = convert(rrule);
				inclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(rruleValue, dtstartValue, utc));
			}

			//add EXRULE properties
			for (ExceptionRule exrule : observance.getProperties(ExceptionRule.class)) {
				RRule exruleValue = convert(exrule);
				exclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(exruleValue, dtstartValue, utc));
			}
		}

		//add RDATE properties
		List<Date> rdates = new ArrayList<Date>();
		for (RecurrenceDates rdate : observance.getRecurrenceDates()) {
			rdates.addAll(rdate.getDates());
			//TODO handle periods
		}
		Collections.sort(rdates);
		inclusions.add(new DateRecurrenceIterator(rdates));

		//add EXDATE properties
		List<Date> exdates = new ArrayList<Date>();
		for (ExceptionDates exdate : observance.getProperties(ExceptionDates.class)) {
			exdates.addAll(exdate.getValues());
		}
		Collections.sort(exdates);
		exclusions.add(new DateRecurrenceIterator(exdates));

		RecurrenceIterator included = join(inclusions);
		if (exclusions.isEmpty()) {
			return included;
		}

		RecurrenceIterator excluded = join(exclusions);
		return RecurrenceIteratorFactory.except(included, excluded);
	}

	private RecurrenceIterator join(List<RecurrenceIterator> iterators) {
		if (iterators.isEmpty()) {
			return new EmptyRecurrenceIterator();
		}

		RecurrenceIterator first = iterators.get(0);
		if (iterators.size() == 1) {
			return first;
		}

		List<RecurrenceIterator> theRest = iterators.subList(1, iterators.size());
		return RecurrenceIteratorFactory.join(first, theRest.toArray(new RecurrenceIterator[0]));
	}

	/**
	 * Converts a biweekly {@link DateStart} object to a google-rfc-2445
	 * {@link DateTimeValue} object.
	 * @param dtstart the biweekly object
	 * @return the google-rfc-2445 object
	 */
	private DateTimeValue convert(DateStart dtstart) {
		DateTimeComponents raw = dtstart.getRawComponents();
		Date value = dtstart.getValue();
		if (raw == null && value == null) {
			return null;
		}

		if (raw == null) {
			raw = new DateTimeComponents(value);
		}
		return new DateTimeValueImpl(raw.getYear(), raw.getMonth(), raw.getDate(), raw.getHour(), raw.getMinute(), raw.getSecond());
	}

	/**
	 * Converts a biweekly {@link RecurrenceProperty} object to a
	 * google-rfc-2445 {@link RRule} object.
	 * @param biweeklyRRule the biweekly object
	 * @return the google-rfc-2445 object
	 */
	private RRule convert(RecurrenceProperty biweeklyRRule) {
		RRule googleRRule = new RRule();
		Recurrence recur = biweeklyRRule.getValue();
		if (recur == null) {
			return googleRRule;
		}

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

		Date until = recur.getUntil();
		if (until != null) {
			googleRRule.setUntil(convert(until));
		}

		DayOfWeek workweekStarts = recur.getWorkweekStarts();
		if (workweekStarts != null) {
			googleRRule.setWkSt(convert(workweekStarts));
		}

		return googleRRule;
	}

	private Weekday convert(DayOfWeek day) {
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

	private com.google.ical.values.Frequency convert(Frequency freq) {
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

	private DateValue convert(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		//@formatter:off
		return new DateTimeValueImpl(
			cal.get(Calendar.YEAR),
			cal.get(Calendar.MONTH)+1,
			cal.get(Calendar.DATE),
			cal.get(Calendar.HOUR_OF_DAY),
			cal.get(Calendar.MINUTE),
			cal.get(Calendar.SECOND)
		);
		//@formatter:on
	}

	private int[] toArray(List<Integer> list) {
		int[] array = new int[list.size()];
		Iterator<Integer> it = list.iterator();
		int i = 0;
		while (it.hasNext()) {
			Integer next = it.next();
			array[i++] = (next == null) ? 0 : next;
		}
		return array;
	}

	/**
	 * A recurrence iterator that doesn't have any elements.
	 */
	private static class EmptyRecurrenceIterator implements RecurrenceIterator {
		public boolean hasNext() {
			return false;
		}

		public DateValue next() {
			throw new NoSuchElementException();
		}

		public void advanceTo(DateValue newStartUtc) {
			//empty
		}

		public void remove() {
			//empty
		}
	}

	private static class RawDateRecurrenceIterator extends ListRecurrenceIterator<DateTimeComponents> {
		public RawDateRecurrenceIterator(Collection<DateTimeComponents> dates) {
			super(dates.iterator());
		}

		public DateValue next() {
			DateTimeComponents value = it.next();

			//@formatter:off
			return new DateTimeValueImpl(
				value.getYear(),
				value.getMonth(),
				value.getDate(),
				value.getHour(),
				value.getMinute(),
				value.getSecond()
			);
			//@formatter:on
		}
	}

	private static class DateRecurrenceIterator extends ListRecurrenceIterator<Date> {
		private final Calendar cal = Calendar.getInstance();

		public DateRecurrenceIterator(Collection<Date> dates) {
			super(dates.iterator());
		}

		public DateValue next() {
			Date value = it.next();
			cal.setTime(value);

			//@formatter:off
			return new DateTimeValueImpl(
				cal.get(Calendar.YEAR),
				cal.get(Calendar.MONTH)+1,
				cal.get(Calendar.DATE),
				cal.get(Calendar.HOUR_OF_DAY),
				cal.get(Calendar.MINUTE),
				cal.get(Calendar.SECOND)
			);
			//@formatter:on
		}
	}

	private static abstract class ListRecurrenceIterator<T> implements RecurrenceIterator {
		protected final Iterator<T> it;

		public ListRecurrenceIterator(Iterator<T> it) {
			this.it = it;
		}

		public boolean hasNext() {
			return it.hasNext();
		}

		public void advanceTo(DateValue newStartUtc) {
			throw new UnsupportedOperationException();
		}

		public void remove() {
			it.remove();
		}
	}
}
