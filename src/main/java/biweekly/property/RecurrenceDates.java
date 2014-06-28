package biweekly.property;

import java.util.Date;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.util.ICalDateFormat;
import biweekly.util.Period;

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
 * Defines a list of dates or time periods that help define a recurrence rule.
 * It must contain either dates or time periods. It cannot contain a combination
 * of both.
 * </p>
 * <p>
 * <b>Code sample:</b>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * //date-time values
 * Date datetime1 = ...
 * Date datetime2 = ...
 * List&lt;Date&gt; datetimes = Arrays.asList(datetime1, datetime2);
 * RecurrenceDates rdate = new RecurrenceDates(datetimes, true);
 * event.addRecurrenceDates(rdate);
 * 
 * //date values
 * Date date1 = ...
 * Date date2 = ...
 * List&lt;Date&gt; dates = Arrays.asList(date1, date2);
 * rdate = new RecurrenceDates(dates, false);
 * event.addRecurrenceDates(rdate);
 * 
 * //periods
 * Period period1 = ...
 * Period period2 = ...
 * List&lt;Period&gt; periods = Arrays.asList(period1, period2);
 * rdate = new RecurrenceDates(periods, true);
 * event.addRecurrenceDates(rdate);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-120">RFC 5545
 * p.120-2</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.34</a>
 */
public class RecurrenceDates extends ICalProperty {
	private List<Date> dates;
	private boolean hasTime;
	private List<Period> periods;

	/**
	 * Creates a recurrence dates property.
	 * @param dates the recurrence dates
	 * @param hasTime true if the dates have a time component, false if they are
	 * strictly dates
	 */
	public RecurrenceDates(List<Date> dates, boolean hasTime) {
		this.dates = dates;
		this.hasTime = hasTime;
	}

	/**
	 * Creates a recurrence dates property.
	 * @param periods the time periods
	 */
	public RecurrenceDates(List<Period> periods) {
		this.periods = periods;
	}

	/**
	 * Gets the recurrence dates.
	 * @return the dates or null if this property contains periods
	 */
	public List<Date> getDates() {
		return dates;
	}

	/**
	 * Gets whether the recurrence dates have time components.
	 * @return true if the dates have a time component, false if they are
	 * strictly dates
	 */
	public boolean hasTime() {
		return hasTime;
	}

	/**
	 * Gets the time periods.
	 * @return the time periods or null if this property contains dates
	 */
	public List<Period> getPeriods() {
		return periods;
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
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		String tzid = getTimezoneId();
		if (tzid != null && tzid.contains("/") && ICalDateFormat.parseTimeZoneId(tzid) == null) {
			warnings.add(Warning.validate(27, tzid));
		}
	}
}
