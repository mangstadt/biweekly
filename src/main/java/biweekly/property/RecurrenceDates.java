package biweekly.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.util.ICalDate;
import biweekly.util.Period;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
 * Date datetime = ...
 * RecurrenceDates rdate = new RecurrenceDates();
 * rdate.addDate(new ICalDate(datetime, true));
 * event.addRecurrenceDates(rdate);
 * 
 * //date values
 * Date date = ...
 * RecurrenceDates rdate = new RecurrenceDates();
 * rdate.addDate(new ICalDate(date, false));
 * event.addRecurrenceDates(rdate);
 * 
 * //periods
 * Period period = ...
 * rdate = new RecurrenceDates();
 * rdate.addPeriod(period);
 * event.addRecurrenceDates(rdate);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-120">RFC 5545
 * p.120-2</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-115">RFC 2445
 * p.115-7</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.34</a>
 */
public class RecurrenceDates extends ICalProperty {
	private List<ICalDate> dates = new ArrayList<ICalDate>();
	private List<Period> periods = new ArrayList<Period>();

	/**
	 * Gets the recurrence dates.
	 * @return the dates
	 */
	public List<ICalDate> getDates() {
		return dates;
	}

	/**
	 * Adds a date.
	 * @param date the date to add
	 */
	public void addDate(ICalDate date) {
		dates.add(date);
	}

	/**
	 * Adds a date
	 * @param date the date to add
	 */
	public void addDate(Date date) {
		addDate(new ICalDate(date, true));
	}

	/**
	 * Gets the time periods.
	 * @return the time periods
	 */
	public List<Period> getPeriods() {
		return periods;
	}

	/**
	 * Adds a period
	 * @param period the period to add
	 */
	public void addPeriod(Period period) {
		periods.add(period);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		if (dates.isEmpty() && periods.isEmpty()) {
			//no value
			warnings.add(Warning.validate(26));
		}

		if (!dates.isEmpty() && !periods.isEmpty()) {
			//can't mix dates and periods
			warnings.add(Warning.validate(49));
		}

		if (version == ICalVersion.V1_0 && !periods.isEmpty()) {
			//1.0 doesn't support periods
			warnings.add(Warning.validate(51));
		}

		if (!dates.isEmpty()) {
			//can't mix date and date-time values
			boolean hasTime = dates.get(0).hasTime();
			for (ICalDate date : dates.subList(1, dates.size())) {
				if (date.hasTime() != hasTime) {
					warnings.add(Warning.validate(50));
					break;
				}
			}
		}
	}
}
