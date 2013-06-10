package biweekly.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.component.ICalComponent;
import biweekly.parameter.FreeBusyType;
import biweekly.util.Duration;
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
 * Defines a person's availability over certain time periods (for example,
 * "free" between 1pm-3pm and 4pm-5pm). Note that this property can contain
 * multiple time periods, but only one availability type (e.g. "busy").
 * @author Michael Angstadt
 * @see "RFC 5545 p.100-1"
 */
public class FreeBusy extends ICalProperty {
	private final List<Period> values = new ArrayList<Period>();

	/**
	 * Adds a time period.
	 * @param start the start date
	 * @param end the end date
	 */
	public void addValue(Date start, Date end) {
		values.add(new Period(start, end));
	}

	/**
	 * Adds a time period.
	 * @param start the start date
	 * @param duration the duration
	 */
	public void addValue(Date start, Duration duration) {
		values.add(new Period(start, duration));
	}

	/**
	 * Gets all time periods.
	 * @return the time periods
	 */
	public List<Period> getValues() {
		return values;
	}

	/**
	 * Gets the person's status over these time periods. If not set, the user
	 * should be considered "BUSY" during these time periods.
	 * @return the type or null if not set
	 */
	public FreeBusyType getType() {
		return parameters.getFreeBusyType();
	}

	/**
	 * Sets the person's status over these time periods. If not set, the user
	 * should be considered "BUSY" during these time periods.
	 * @param fbType the type or null to remove
	 */
	public void setType(FreeBusyType fbType) {
		parameters.setFreeBusyType(fbType);
	}

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		if (values.isEmpty()) {
			warnings.add("No time periods are defined.");
		} else {
			for (Period timePeriod : values) {
				if (timePeriod.getStartDate() == null) {
					warnings.add("One or more time periods do not have start dates.");
					break;
				}
			}

			for (Period timePeriod : values) {
				if (timePeriod.getEndDate() == null && timePeriod.getDuration() == null) {
					warnings.add("One or more time periods do not have either an end date or a duration.");
					break;
				}
			}
		}
	}
}
