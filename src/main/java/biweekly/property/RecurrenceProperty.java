package biweekly.property;

import static biweekly.util.Google2445Utils.convert;

import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.Frequency;

import com.google.ical.compat.javautil.DateIterator;
import com.google.ical.compat.javautil.DateIteratorFactory;
import com.google.ical.iter.RecurrenceIterator;
import com.google.ical.iter.RecurrenceIteratorFactory;
import com.google.ical.values.DateValue;
import com.google.ical.values.RRule;

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
 * Represents a property whose value is a recurrence rule.
 * @author Michael Angstadt
 */
public class RecurrenceProperty extends ValuedProperty<Recurrence> {
	/**
	 * Creates a new recurrence property.
	 * @param recur the recurrence value
	 */
	public RecurrenceProperty(Recurrence recur) {
		super(recur);
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RecurrenceProperty(RecurrenceProperty original) {
		super(original);
	}

	/**
	 * Gets the date values of this recurrence property.
	 * @param startDate the date that the recurrence starts (typically, the
	 * value of its accompanying {@link DateStart} property)
	 * @return an iterator containing the dates
	 * @see <a
	 * href="https://code.google.com/p/google-rfc-2445/">google-rfc-2445</a>
	 */
	public DateIterator getDateIterator(Date startDate) {
		return getDateIterator(new ICalDate(startDate));
	}

	/**
	 * Gets the date values of this recurrence property.
	 * @param startDate the date that the recurrence starts (typically, the
	 * value of its accompanying {@link DateStart} property)
	 * @return an iterator containing the dates
	 * @see <a
	 * href="https://code.google.com/p/google-rfc-2445/">google-rfc-2445</a>
	 */
	public DateIterator getDateIterator(ICalDate startDate) {
		Recurrence recur = getValue();
		if (recur == null) {
			return new EmptyDateIterator();
		}

		RRule rruleValue = convert(recur);

		//we need an ICalDate that *doesn't* have any raw date/time components to pass into "convert()"
		//see: https://sourceforge.net/p/biweekly/discussion/help-and-support/thread/faa25306/
		ICalDate startDateCopy = (startDate.getRawComponents() == null) ? startDate : new ICalDate(startDate, null, startDate.hasTime());
		DateValue dtstartValue = convert(startDateCopy);

		RecurrenceIterator it = RecurrenceIteratorFactory.createRecurrenceIterator(rruleValue, dtstartValue, TimeZone.getDefault());
		return DateIteratorFactory.createDateIterator(it);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		super.validate(components, version, warnings);
		if (value == null) {
			return;
		}

		if (value.getFrequency() == null) {
			warnings.add(Warning.validate(30));
		}

		if (value.getUntil() != null && value.getCount() != null) {
			warnings.add(Warning.validate(31));
		}

		switch (version) {
		case V1_0:
			if (!value.getXRules().isEmpty()) {
				warnings.add(new Warning("X-Rules are not supported by vCal."));
			}
			if (!value.getBySetPos().isEmpty()) {
				warnings.add(new Warning("BYSETPOS is not supported by vCal."));
			}
			if (value.getFrequency() == Frequency.SECONDLY) {
				warnings.add(new Warning(Frequency.SECONDLY.name() + " frequency is not supported by vCal."));
			}
			break;

		case V2_0_DEPRECATED:
			//empty
			break;

		case V2_0:
			if (!value.getXRules().isEmpty()) {
				warnings.add(Warning.validate(32));
			}

			break;
		}
	}

	private static class EmptyDateIterator implements DateIterator {
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
}
