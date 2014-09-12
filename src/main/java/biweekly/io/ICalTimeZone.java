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
		millis %= 1000 / 60 / 60;
		int minute = millis / 1000 / 60;
		millis %= 1000 / 60;
		int second = millis / 1000;
		Observance bounds[] = getBoundaries(year, month + 1, day, hour, minute, second);

		UtcOffsetProperty offset = (bounds[1] == null) ? bounds[0].getTimezoneOffsetTo() : bounds[1].getTimezoneOffsetFrom();
		return (int) offset.getValue().toMillis();
	}

	@Override
	public int getRawOffset() {
		Observance[] bounds = getBoundaries(new Date());
		Observance standard = (bounds[0] instanceof StandardTime) ? bounds[0] : bounds[1];
		return (int) standard.getTimezoneOffsetTo().getValue().toMillis();
	}

	@Override
	public boolean inDaylightTime(Date date) {
		Observance bounds[] = getBoundaries(date);
		Observance left = bounds[0];
		if (left != null) {
			return left instanceof DaylightSavingsTime;
		}

		Observance right = bounds[0];
		return right instanceof StandardTime;
	}

	@Override
	public void setRawOffset(int offset) {
		throw new UnsupportedOperationException("Unable to set the raw offset.  Modify the VTIMEZONE component instead.");
	}

	@Override
	public boolean useDaylightTime() {
		return !component.getDaylightSavingsTime().isEmpty();
	}

	/**
	 * Gets the timezone observances that surround a given date.
	 * @param date the date
	 * @return the timezone observances surrounding the date. The array will
	 * always have a length of 2. The elements may be null. [index 0] &lt;
	 * givenDate &lt; [index 1]
	 */
	private Observance[] getBoundaries(Date date) {
		Calendar cal = Calendar.getInstance(this);
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DATE);
		int hour = cal.get(Calendar.HOUR);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);

		return getBoundaries(year, month, day, hour, minute, second);
	}

	/**
	 * Gets the timezone observances that surround a given date.
	 * @param year the year
	 * @param month the month (1-12)
	 * @param day the date
	 * @param hour the hour
	 * @param minute the minute
	 * @param second the second
	 * @return the timezone observances surrounding the date. The array will
	 * always have a length of 2. The elements may be null. [index 0] &lt;
	 * givenDate &lt; [index 1]
	 */
	private Observance[] getBoundaries(int year, int month, int day, int hour, int minute, int second) {
		DateValue givenTime = new DateTimeValueImpl(year, month, day, hour, minute, second);

		Observance left = null, right = null;
		List<Observance> observances = getSortedObservances();
		for (Observance observance : observances) {
			RecurrenceIterator it = createIterator(observance);
			while (it.hasNext()) {
				DateValue value = it.next();
				if (givenTime.compareTo(value) < 0) {
					//if givenTime is before value
					right = observance;
					break;
				}
			}

			if (right != null) {
				break;
			}

			left = observance;
		}

		return new Observance[] { left, right };
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

	private RecurrenceIterator createIterator(Observance observance) {
		DateStart start = observance.getDateStart();
		if (start == null) {
			return new EmptyRecurrenceIterator();
		}

		List<RecurrenceIterator> inclusions = new ArrayList<RecurrenceIterator>();
		List<RecurrenceIterator> exclusions = new ArrayList<RecurrenceIterator>();
		TimeZone utc = TimeZone.getTimeZone("UTC");

		DateTimeValue dtstart = convert(start);
		RDateList dtstartrdate = new RDateList(utc);
		dtstartrdate.setDatesUtc(new DateValue[] { dtstart });
		inclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(dtstartrdate));

		try {
			for (RecurrenceRule rrule : observance.getProperties(RecurrenceRule.class)) {
				RRule rruleValue = convert(rrule);
				inclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(rruleValue, dtstart, utc));
			}
			for (RecurrenceDates rdate : observance.getRecurrenceDates()) {
				RDateList rdateValue = convert(rdate);
				inclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(rdateValue));
			}

			for (ExceptionRule exrule : observance.getProperties(ExceptionRule.class)) {
				RRule exruleValue = convert(exrule);
				exclusions.add(RecurrenceIteratorFactory.createRecurrenceIterator(exruleValue, dtstart, utc));
			}
			for (ExceptionDates exdate : observance.getProperties(ExceptionDates.class)) {
				RDateList exdateValue = convert(exdate);
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

	private DateTimeValue convert(DateStart dtstart) {
		DateTimeComponents raw = dtstart.getRawComponents();
		return new DateTimeValueImpl(raw.getYear(), raw.getMonth(), raw.getDate(), raw.getHour(), raw.getMinute(), raw.getSecond());
	}

	private RRule convert(RecurrenceRule rrule) throws ParseException {
		RecurrenceRuleScribe scribe = (RecurrenceRuleScribe) new ScribeIndex().getPropertyScribe(RecurrenceRule.class);
		String text = scribe.writeText(rrule, new WriteContext(ICalVersion.V2_0, new TimezoneInfo()));
		return new RRule(text);
	}

	private RRule convert(ExceptionRule exrule) throws ParseException {
		ExceptionRuleScribe scribe = (ExceptionRuleScribe) new ScribeIndex().getPropertyScribe(ExceptionRule.class);
		String text = scribe.writeText(exrule, new WriteContext(ICalVersion.V2_0, new TimezoneInfo()));
		return new RRule(text);
	}

	private RDateList convert(RecurrenceDates rdate) throws ParseException {
		RecurrenceDatesScribe scribe = (RecurrenceDatesScribe) new ScribeIndex().getPropertyScribe(RecurrenceDates.class);
		String text = scribe.writeText(rdate, new WriteContext(ICalVersion.V2_0, new TimezoneInfo()));
		return new RDateList(text, TimeZone.getTimeZone("UTC"));
	}

	private RDateList convert(ExceptionDates exdate) throws ParseException {
		ExceptionDatesScribe scribe = (ExceptionDatesScribe) new ScribeIndex().getPropertyScribe(ExceptionDates.class);
		String text = scribe.writeText(exdate, new WriteContext(ICalVersion.V2_0, new TimezoneInfo()));
		return new RDateList(text, TimeZone.getTimeZone("UTC"));
	}

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
