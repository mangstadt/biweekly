package biweekly.io;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import biweekly.ICalVersion;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.Observance;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.property.ExceptionDatesScribe;
import biweekly.io.scribe.property.ExceptionRuleScribe;
import biweekly.io.scribe.property.RecurrenceDatesScribe;
import biweekly.io.scribe.property.RecurrenceRuleScribe;
import biweekly.property.DateStart;
import biweekly.property.ExceptionDates;
import biweekly.property.ExceptionRule;
import biweekly.property.RecurrenceDates;
import biweekly.property.RecurrenceRule;
import biweekly.property.UtcOffsetProperty;
import biweekly.util.DateTimeComponents;

import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateTimeValue;
import com.google.ical.values.DateTimeValueImpl;
import com.google.ical.values.DateValue;
import com.google.ical.values.RDateList;
import com.google.ical.values.RRule;

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
	 * @param component the VTIMEZONE component to wrap
	 */
	public ICalTimeZone(VTimezone component) {
		this.component = component;
		setID(component.getTimezoneId().getValue());
	}

	/**
	 * Gets the VTIMEZONE component that is being wrapped. Modifications may be
	 * made to this object.
	 * @return the VTIMEZONE component
	 */
	public VTimezone getComponent() {
		return component;
	}

	@Override
	public int getOffset(int era, int year, int month, int day, int dayOfWeek, int millis) {
		//TODO all this assumes DateStart is non-null and has a non-null DateTimeComponents object!

		int hour = millis / 1000 / 60 / 60;
		millis -= hour * 1000 * 60 * 60;
		int minute = millis / 1000 / 60;
		millis -= minute * 1000 * 60;
		int second = millis / 1000;

		Result result = getObservance(year, month + 1, day, hour, minute, second);
		return result.offset;
	}

	public void printBoundaries() {
		List<Observance> observances = getSortedObservances();
		for (Observance observance : observances) {
			System.out.println(observance.getClass().getSimpleName() + " " + observance.getDateStart().getValue());
			RecurrenceIterator it = createIterator(observance);
			while (it.hasNext()) {
				DateValue value = it.next();
				System.out.println(value);
				if (value.year() >= 2014) {
					break;
				}
			}
		}
	}

	@Override
	public int getRawOffset() {
		Result result = getObservance(new Date());
		Observance observance = result.observance;
		if (observance == null) {
			//return the offset of the first STANDARD component
			List<Observance> observances = getSortedObservances();
			for (Observance o : observances) {
				if (o instanceof StandardTime) {
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
		Result result = getObservance(date);
		Observance observance = result.observance;
		return (observance == null) ? false : observance instanceof DaylightSavingsTime;
	}

	@Override
	public void setRawOffset(int offset) {
		throw new UnsupportedOperationException("Unable to set the raw offset.  Modify the VTIMEZONE component instead.");
	}

	@Override
	public boolean useDaylightTime() {
		return !component.getDaylightSavingsTime().isEmpty();
	}

	private Result getObservance(Date date) {
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")); //TODO should this be local TZ?
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);

		return getObservance(year, month, day, hour, minute, second);
	}

	private Result getObservance(int year, int month, int day, int hour, int minute, int second) {
		DateValue givenTime = new DateTimeValueImpl(year, month, day, hour, minute, second);

		List<Observance> observances = getSortedObservances();
		Observance closest = null;
		DateValue closestValue = null;
		for (Observance observance : observances) {
			//skip observances that don't have DTSTART properties
			DateStart dtstart = observance.getDateStart();
			if (dtstart == null) {
				continue;
			}

			//skip observances that start after the given time
			DateTimeValue dtstartValue = convert(dtstart);
			if (dtstartValue.compareTo(givenTime) > 0) {
				continue;
			}

			//System.out.println("DTSTART:" + dtstartValue);

			RecurrenceIterator it = createIterator(observance);
			DateValue prev = null;
			while (it.hasNext()) {
				DateValue cur = it.next();

				//the iterator seems to apply the default timezone's offset to the DateValue
				//so, undo this
				cur = removeDefaultTzOffset(cur);

				if (givenTime.compareTo(cur) < 0) {
					//break if we have passed the given time
					break;
				}

				prev = cur;
			}

			//System.out.println("prev:" + prev);
			if (prev != null && (closestValue == null || closestValue.compareTo(prev) < 0)) {
				closestValue = prev;
				closest = observance;
			}
		}

		//		System.out.println("\nGiven Date:" + givenTime);
		//		System.out.println("Closest Observance");
		//		System.out.println("   DTSTART:" + closest.getDateStart().getRawComponents());
		//		System.out.println("   FROM:" + closest.getTimezoneOffsetFrom().getValue());
		//		System.out.println("   TO:" + closest.getTimezoneOffsetTo().getValue());
		//		System.out.println("Closest RDATE:" + closestValue);

		UtcOffsetProperty offset;
		if (closest == null) {
			offset = observances.get(0).getTimezoneOffsetFrom();
		} else {
			offset = closest.getTimezoneOffsetTo();
		}

		return new Result(closest, offset.getValue().toMillis());
	}

	private DateValue removeDefaultTzOffset(DateValue value) {
		Calendar c = Calendar.getInstance();
		c.clear();

		c.set(Calendar.YEAR, value.year());
		c.set(Calendar.MONTH, value.month() - 1);
		c.set(Calendar.DATE, value.day());
		if (value instanceof DateTimeValue) {
			DateTimeValue dt = (DateTimeValue) value;
			c.set(Calendar.HOUR_OF_DAY, dt.hour());
			c.set(Calendar.MINUTE, dt.minute());
			c.set(Calendar.SECOND, dt.second());
		}

		Date d = c.getTime();
		TimeZone tz = TimeZone.getDefault();
		int offset = tz.getOffset(d.getTime());

		c.add(Calendar.MILLISECOND, offset);

		//@formatter:off
		return new DateTimeValueImpl(
			c.get(Calendar.YEAR),
			c.get(Calendar.MONTH)+1,
			c.get(Calendar.DATE),
			c.get(Calendar.HOUR_OF_DAY),
			c.get(Calendar.MINUTE),
			c.get(Calendar.SECOND)
		);
		//@formatter:on
	}

	private DateTimeValue toUtc(DateTimeValue value) {
		Calendar c = Calendar.getInstance();
		c.clear();

		c.set(Calendar.YEAR, value.year());
		c.set(Calendar.MONTH, value.month() - 1);
		c.set(Calendar.DATE, value.day());
		c.set(Calendar.HOUR_OF_DAY, value.hour());
		c.set(Calendar.MINUTE, value.minute());
		c.set(Calendar.SECOND, value.second());

		Date d = c.getTime();
		TimeZone tz = TimeZone.getDefault();
		int offset = tz.getOffset(d.getTime());

		c.add(Calendar.MILLISECOND, -offset);

		//@formatter:off
		return new DateTimeValueImpl(
			c.get(Calendar.YEAR),
			c.get(Calendar.MONTH)+1,
			c.get(Calendar.DATE),
			c.get(Calendar.HOUR_OF_DAY),
			c.get(Calendar.MINUTE),
			c.get(Calendar.SECOND)
		);
		//@formatter:on
	}

	/**
	 * Gets all observances sorted by {@link DateStart}.
	 * @return the sorted observances
	 */
	private List<Observance> getSortedObservances() {
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
	private RecurrenceIterator createIterator(Observance observance) {
		DateStart start = observance.getDateStart();
		if (start == null) {
			return new EmptyRecurrenceIterator();
		}

		List<RecurrenceIterator> inclusions = new ArrayList<RecurrenceIterator>();
		List<RecurrenceIterator> exclusions = new ArrayList<RecurrenceIterator>();
		//TimeZone utc = TimeZone.getTimeZone("UTC");
		TimeZone utc = TimeZone.getDefault();

		DateTimeValue dtstart = convert(start);
		dtstart = toUtc(dtstart);
		RDateList dtstartrdate = new RDateList(utc);
		dtstartrdate.setDatesUtc(new DateValue[] { dtstart });
		inclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(dtstartrdate)); //TODO need to include this?

		try {
			for (RecurrenceRule rrule : observance.getProperties(RecurrenceRule.class)) {
				RRule rruleValue = convert(rrule);
				inclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(rruleValue, dtstart, utc)); //TODO should this be UTC or default timezone?
			}
			for (RecurrenceDates rdate : observance.getRecurrenceDates()) {
				RDateList rdateValue = convert(rdate, utc);
				inclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(rdateValue));
			}

			for (ExceptionRule exrule : observance.getProperties(ExceptionRule.class)) {
				RRule exruleValue = convert(exrule);
				exclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(exruleValue, dtstart, utc));
			}
			for (ExceptionDates exdate : observance.getProperties(ExceptionDates.class)) {
				RDateList exdateValue = convert(exdate, utc);
				exclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(exdateValue));
			}
		} catch (ParseException e) {
			throw new RuntimeException("google-rfc-2445 is unable to parse a marshalled property value created by biweekly.", e);
		}

		if (inclusions.isEmpty()) {
			throw new IllegalArgumentException("No inclusion dates found.");
		}

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
		return new DateTimeValueImpl(raw.getYear(), raw.getMonth(), raw.getDate(), raw.getHour(), raw.getMinute(), raw.getSecond());
	}

	/**
	 * Converts a biweekly {@link RecurrenceRule} object to a google-rfc-2445
	 * {@link RRule} object.
	 * @param rrule the biweekly object
	 * @return the google-rfc-2445 object
	 * @throws ParseException if there's a problem with the conversion
	 */
	private RRule convert(RecurrenceRule rrule) throws ParseException {
		RecurrenceRuleScribe scribe = (RecurrenceRuleScribe) new ScribeIndex().getPropertyScribe(RecurrenceRule.class);
		String text = scribe.getPropertyName() + ":" + scribe.writeText(rrule, new WriteContext(ICalVersion.V2_0, new TimezoneInfo()));
		return new RRule(text);
	}

	/**
	 * Converts a biweekly {@link ExceptionRule} object to a google-rfc-2445
	 * {@link RRule} object.
	 * @param exrule the biweekly object
	 * @return the google-rfc-2445 object
	 * @throws ParseException if there's a problem with the conversion
	 */
	private RRule convert(ExceptionRule exrule) throws ParseException {
		ExceptionRuleScribe scribe = (ExceptionRuleScribe) new ScribeIndex().getPropertyScribe(ExceptionRule.class);
		String text = scribe.getPropertyName() + ":" + scribe.writeText(exrule, new WriteContext(ICalVersion.V2_0, new TimezoneInfo()));
		return new RRule(text);
	}

	/**
	 * Converts a biweekly {@link RecurrenceDates} object to a google-rfc-2445
	 * {@link RDateList} object.
	 * @param rdate the biweekly object
	 * @param tz the timezone the date values are in
	 * @return the google-rfc-2445 object
	 * @throws ParseException if there's a problem with the conversion
	 */
	private RDateList convert(RecurrenceDates rdate, TimeZone tz) throws ParseException {
		RecurrenceDatesScribe scribe = (RecurrenceDatesScribe) new ScribeIndex().getPropertyScribe(RecurrenceDates.class);
		String text = scribe.getPropertyName() + ":" + scribe.writeText(rdate, new WriteContext(ICalVersion.V2_0, new TimezoneInfo()));
		return new RDateList(text, tz);
	}

	/**
	 * Converts a biweekly {@link ExceptionDates} object to a google-rfc-2445
	 * {@link RDateList} object.
	 * @param rdate the biweekly object
	 * @param tz the timezone the date values are in
	 * @return the google-rfc-2445 object
	 * @throws ParseException if there's a problem with the conversion
	 */
	private RDateList convert(ExceptionDates exdate, TimeZone tz) throws ParseException {
		ExceptionDatesScribe scribe = (ExceptionDatesScribe) new ScribeIndex().getPropertyScribe(ExceptionDates.class);
		String text = scribe.getPropertyName() + ":" + scribe.writeText(exdate, new WriteContext(ICalVersion.V2_0, new TimezoneInfo()));
		return new RDateList(text, tz);
	}

	private static class Result {
		private final Observance observance;
		private final int offset;

		public Result(Observance observance, int offset) {
			this.observance = observance;
			this.offset = offset;
		}
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
}
