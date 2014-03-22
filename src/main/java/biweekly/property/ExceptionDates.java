package biweekly.property;

import java.util.Date;
import java.util.List;

import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.util.ICalDateFormatter;

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
 * Defines a list of exceptions to the dates specified in the
 * {@link RecurrenceRule} property.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * //dates with time components
 * ExceptionDates exdate = new ExceptionDates(true);
 * Date datetime1 = ...;
 * exdate.addValue(datetime1);
 * Date datetime2 = ...;
 * exdate.addValue(datetime2);
 * event.addExceptionDates(exdate);
 * 
 * //dates without time components
 * exdate = new ExceptionDates(false);
 * Date date1 = ...;
 * exdate.addValue(date1);
 * Date date2 = ...;
 * exdate.addValue(date2);
 * event.addExceptionDates(exdate);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @rfc 5545 p.118-20
 */
public class ExceptionDates extends ListProperty<Date> {
	private boolean hasTime = true;

	/**
	 * Creates an exception dates property.
	 * @param hasTime true if the dates have a time component, false if they are
	 * strictly dates
	 */
	public ExceptionDates(boolean hasTime) {
		setHasTime(hasTime);
	}

	/**
	 * Gets whether the dates have time components.
	 * @return true if the dates have time components, false if they are
	 * strictly dates
	 */
	public boolean hasTime() {
		return hasTime;
	}

	/**
	 * Sets whether the dates have time components.
	 * @param hasTime true if the dates have time components, false if they are
	 * strictly dates
	 */
	public void setHasTime(boolean hasTime) {
		this.hasTime = hasTime;
	}

	@Override
	public String getTimezoneId() {
		return super.getTimezoneId();
	}

	@Override
	public void setTimezoneId(String timezoneId) {
		super.setTimezoneId(timezoneId);
	}

	@Override
	public void setTimezone(VTimezone timezone) {
		super.setTimezone(timezone);
	}

	@Override
	protected void validate(List<ICalComponent> components, List<Warning> warnings) {
		String tzid = getTimezoneId();
		if (tzid != null && tzid.contains("/") && ICalDateFormatter.parseTimeZoneId(tzid) == null) {
			warnings.add(Warning.validate(27, tzid));
		}
	}
}
