package biweekly.io;

import static biweekly.property.ValuedProperty.getValue;
import static biweekly.util.Google2445Utils.convert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import biweekly.Messages;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.Observance;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.property.DateStart;
import biweekly.property.ExceptionDates;
import biweekly.property.ExceptionRule;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.property.TimezoneName;
import biweekly.property.UtcOffsetProperty;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;

import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.RRule;

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
 * A timezone that is based on an iCalendar {@link VTimezone} component.
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class ICalTimeZone extends TimeZone {
	private final VTimezone component;

	/**
	 * Creates a new timezone based on an iCalendar VTIMEZONE component.
	 * @param component the VTIMEZONE component to wrap
	 */
	public ICalTimeZone(VTimezone component) {
		this.component = component;

		String id = getValue(component.getTimezoneId());
		if (id != null) {
			setID(id);
		}
	}

	@Override
	public String getDisplayName(boolean daylight, int style, Locale locale) {
		List<Observance> observances = getSortedObservances();
		ListIterator<Observance> it = observances.listIterator(observances.size());
		while (it.hasPrevious()) {
			Observance observance = it.previous();

			if (daylight && observance instanceof DaylightSavingsTime) {
				List<TimezoneName> names = observance.getTimezoneNames();
				if (!names.isEmpty()) {
					TimezoneName name = names.get(0);
					return name.getValue();
				}
			}

			if (!daylight && observance instanceof StandardTime) {
				List<TimezoneName> names = observance.getTimezoneNames();
				if (!names.isEmpty()) {
					TimezoneName name = names.get(0);
					return name.getValue();
				}
			}
		}

		return super.getDisplayName(daylight, style, locale);
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
			//find the first observance that has a DTSTART property and a TZOFFSETFROM property
			for (Observance obs : getSortedObservances()) {
				if (hasDateStart(obs) && hasTimezoneOffsetFrom(obs)) {
					return (int) obs.getTimezoneOffsetFrom().getValue().getMillis();
				}
			}
			return 0;
		}

		return hasTimezoneOffsetTo(observance) ? (int) observance.getTimezoneOffsetTo().getValue().getMillis() : 0;
	}

	@Override
	public int getRawOffset() {
		Observance observance = getObservance(new Date());
		if (observance == null) {
			//return the offset of the first STANDARD component
			for (Observance obs : getSortedObservances()) {
				if (obs instanceof StandardTime && hasTimezoneOffsetTo(obs)) {
					return (int) obs.getTimezoneOffsetTo().getValue().getMillis();
				}
			}
			return 0;
		}

		UtcOffsetProperty offset = (observance instanceof StandardTime) ? observance.getTimezoneOffsetTo() : observance.getTimezoneOffsetFrom();
		return (int) offset.getValue().getMillis();
	}

	@Override
	public boolean inDaylightTime(Date date) {
		if (!useDaylightTime()) {
			return false;
		}

		Observance observance = getObservance(date);
		return (observance == null) ? false : (observance instanceof DaylightSavingsTime);
	}

	/**
	 * This method is not supported by this class.
	 * @throws UnsupportedOperationException
	 */
	@Override
	public void setRawOffset(int offset) {
		throw new UnsupportedOperationException(Messages.INSTANCE.getExceptionMessage(12));
	}

	@Override
	public boolean useDaylightTime() {
		return !component.getDaylightSavingsTime().isEmpty();
	}

	/**
	 * Gets the timezone information of a date.
	 * @param date the date
	 * @return the timezone information
	 */
	public Boundary getObservanceBoundary(Date date) {
		Calendar cal = Calendar.getInstance(utc());
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);

		return getObservanceBoundary(year, month, day, hour, minute, second);
	}

	/**
	 * Gets the observance that a date is effected by.
	 * @param date the date
	 * @return the observance or null if an observance cannot be found
	 */
	public Observance getObservance(Date date) {
		Boundary boundary = getObservanceBoundary(date);
		return (boundary == null) ? null : boundary.getObservanceIn();
	}

	/**
	 * Gets the VTIMEZONE component that is being wrapped. Modifications made to
	 * the component will effect this timezone object.
	 * @return the VTIMEZONE component
	 */
	public VTimezone getComponent() {
		return component;
	}

	/**
	 * Gets the observance that a date is effected by.
	 * @param year the year
	 * @param month the month (1-12)
	 * @param day the day of the month
	 * @param hour the hour
	 * @param minute the minute
	 * @param second the second
	 * @return the observance or null if an observance cannot be found
	 */
	private Observance getObservance(int year, int month, int day, int hour, int minute, int second) {
		Boundary boundary = getObservanceBoundary(year, month, day, hour, minute, second);
		return (boundary == null) ? null : boundary.getObservanceIn();
	}

	/**
	 * Gets the observance information of a date.
	 * @param year the year
	 * @param month the month (1-12)
	 * @param day the day of the month
	 * @param hour the hour
	 * @param minute the minute
	 * @param second the second
	 * @return the observance information or null if none was found
	 */
	private Boundary getObservanceBoundary(int year, int month, int day, int hour, int minute, int second) {
		List<Observance> observances = getSortedObservances();
		if (observances.isEmpty()) {
			return null;
		}

		DateValue givenTime = new DateTimeValueImpl(year, month, day, hour, minute, second);
		int closestIndex = -1;
		Observance closest = null;
		DateTimeValue closestValue = null;
		for (int i = 0; i < observances.size(); i++) {
			Observance observance = observances.get(i);

			//skip observances that start after the given time
			ICalDate dtstart = getValue(observance.getDateStart());
			if (dtstart != null) {
				DateValue dtstartValue = convert(dtstart);
				if (dtstartValue.compareTo(givenTime) > 0) {
					continue;
				}
			}

			RecurrenceIterator it = createIterator(observance);
			DateTimeValue prev = null;
			while (it.hasNext()) {
				DateTimeValue cur = (DateTimeValue) it.next();
				if (givenTime.compareTo(cur) < 0) {
					//break if we have passed the given time
					break;
				}

				prev = cur;
			}

			if (prev != null && (closestValue == null || closestValue.compareTo(prev) < 0)) {
				closestValue = prev;
				closest = observance;
				closestIndex = i;
			}
		}

		Observance observanceIn = closest;
		DateTimeValue observanceInStart = closestValue;
		Observance observanceAfter = null;
		DateTimeValue observanceAfterStart = null;
		if (closestIndex < observances.size() - 1) {
			observanceAfter = observances.get(closestIndex + 1);

			RecurrenceIterator it = createIterator(observanceAfter);
			while (it.hasNext()) {
				DateTimeValue cur = (DateTimeValue) it.next();
				if (givenTime.compareTo(cur) < 0) {
					observanceAfterStart = cur;
					break;
				}
			}
		}

		return new Boundary(observanceInStart, observanceIn, observanceAfterStart, observanceAfter);
	}

	private static boolean hasDateStart(Observance observance) {
		return getValue(observance.getDateStart()) != null;
	}

	private static boolean hasTimezoneOffsetFrom(Observance observance) {
		return getValue(observance.getTimezoneOffsetFrom()) != null;
	}

	private static boolean hasTimezoneOffsetTo(Observance observance) {
		return getValue(observance.getTimezoneOffsetTo()) != null;
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
				ICalDate startLeft = getValue(left.getDateStart());
				ICalDate startRight = getValue(right.getDateStart());
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

		ICalDate dtstart = getValue(observance.getDateStart());
		if (dtstart != null) {
			DateValue dtstartValue = convert(dtstart);

			//add DTSTART property
			inclusions.add(new DateValueRecurrenceIterator(Arrays.asList(dtstartValue)));

			TimeZone utc = utc();

			//add RRULE properties
			for (RecurrenceRule rrule : observance.getProperties(RecurrenceRule.class)) {
				Recurrence recur = rrule.getValue();
				if (recur != null) {
					RRule rruleValue = convert(recur);
					inclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(rruleValue, dtstartValue, utc));
				}
			}

			//add EXRULE properties
			for (ExceptionRule exrule : observance.getProperties(ExceptionRule.class)) {
				Recurrence recur = exrule.getValue();
				if (recur != null) {
					RRule exruleValue = convert(recur);
					exclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(exruleValue, dtstartValue, utc));
				}
			}
		}

		//add RDATE properties
		List<ICalDate> rdates = new ArrayList<ICalDate>();
		for (RecurrenceDates rdate : observance.getRecurrenceDates()) {
			rdates.addAll(rdate.getDates());
		}
		Collections.sort(rdates);
		inclusions.add(new DateRecurrenceIterator(rdates));

		//add EXDATE properties
		List<ICalDate> exdates = new ArrayList<ICalDate>();
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

	private static TimeZone utc() {
		return TimeZone.getTimeZone("UTC");
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

	/**
	 * A recurrence iterator that takes a collection of {@link DateValue}
	 * objects.
	 */
	private static class DateValueRecurrenceIterator extends IteratorWrapper<DateValue> {
		public DateValueRecurrenceIterator(Collection<DateValue> dates) {
			super(dates.iterator());
		}

		public DateValue next() {
			return it.next();
		}
	}

	/**
	 * A recurrence iterator that takes a collection of {@link ICalDate}
	 * objects.
	 */
	private static class DateRecurrenceIterator extends IteratorWrapper<ICalDate> {
		public DateRecurrenceIterator(Collection<ICalDate> dates) {
			super(dates.iterator());
		}

		public DateValue next() {
			ICalDate value = it.next();
			return convert(value);
		}
	}

	/**
	 * A recurrence iterator that wraps an {@link Iterator}.
	 */
	private static abstract class IteratorWrapper<T> implements RecurrenceIterator {
		protected final Iterator<T> it;

		public IteratorWrapper(Iterator<T> it) {
			this.it = it;
		}

		public boolean hasNext() {
			return it.hasNext();
		}

		public void advanceTo(DateValue newStartUtc) {
			throw new UnsupportedOperationException();
		}

		public void remove() {
			//RecurrenceIterator does not support this method
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Holds the timezone observance information of a particular date.
	 */
	public static class Boundary {
		private final DateTimeValue observanceInStart, observanceAfterStart;
		private final Observance observanceIn, observanceAfter;

		public Boundary(DateTimeValue observanceInStart, Observance observanceIn, DateTimeValue observanceAfterStart, Observance observanceAfter) {
			this.observanceInStart = observanceInStart;
			this.observanceAfterStart = observanceAfterStart;
			this.observanceIn = observanceIn;
			this.observanceAfter = observanceAfter;
		}

		/**
		 * Gets start time of the observance that the date resides in.
		 * @return the time
		 */
		public DateTimeValue getObservanceInStart() {
			return observanceInStart;
		}

		/**
		 * Gets the start time the observance that comes after the observance
		 * that the date resides in.
		 * @return the time
		 */
		public DateTimeValue getObservanceAfterStart() {
			return observanceAfterStart;
		}

		/**
		 * Gets the observance that the date resides in.
		 * @return the observance
		 */
		public Observance getObservanceIn() {
			return observanceIn;
		}

		/**
		 * Gets the observance that comes after the observance that the date
		 * resides in.
		 * @return the observance
		 */
		public Observance getObservanceAfter() {
			return observanceAfter;
		}
	}
}
