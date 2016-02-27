package biweekly.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalVersion;
import biweekly.Messages;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.parameter.FreeBusyType;
import biweekly.util.Duration;
import biweekly.util.Period;

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
 * <p>
 * Defines a person's availability over certain time periods (for example,
 * "busy" between 1pm-3pm and 4pm-5pm). Note that this property can contain
 * multiple time periods, but only one availability type may be defined (e.g.
 * "busy" or "free").
 * </p>
 * <p>
 * <b>Code sample:</b>
 * 
 * <pre class="brush:java">
 * VFreeBusy fb = new VFreeBusy();
 * 
 * FreeBusy freebusy = new FreeBusy();
 * freebusy.setType(FreeBusyType.BUSY);
 * 
 * Date onePM = ...
 * Date threePM = ...
 * freebusy.addValue(onePM, threePM);
 * 
 * Date fourPM = ...
 * Duration oneHour = Duration.builder().hours(1).build();
 * freeBusy.addValue(fourPM, oneHour);
 * 
 * fb.addFreeBusy(freebusy);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-100">RFC 5545
 * p.100-1</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-95">RFC 2445 p.95-6</a>
 */
public class FreeBusy extends ICalProperty {
	private final List<Period> values;

	public FreeBusy() {
		values = new ArrayList<Period>();
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public FreeBusy(FreeBusy original) {
		super(original);
		values = new ArrayList<Period>(original.values.size());
		for (Period period : original.values) {
			values.add(new Period(period));
		}
	}

	/**
	 * Adds a time period.
	 * @param start the start date
	 * @param end the end date
	 */
	public void addValue(Date start, Date end) {
		addValue(new Period(start, end));
	}

	/**
	 * Adds a time period.
	 * @param start the start date
	 * @param duration the duration
	 */
	public void addValue(Date start, Duration duration) {
		addValue(new Period(start, duration));
	}

	/**
	 * Adds a time period.
	 * @param period the time period to add (cannot be null)
	 */
	public void addValue(Period period) {
		if (period == null) {
			throw new NullPointerException(Messages.INSTANCE.getExceptionMessage(15));
		}
		values.add(period);
	}

	/**
	 * Gets all time periods.
	 * @return the time periods
	 */
	public List<Period> getValues() {
		return values;
	}

	/**
	 * Gets the person's status over the time periods that are specified in this
	 * property (for example, "free" or "busy"). If not set, the user should be
	 * considered "busy".
	 * @return the type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-20">RFC 5545
	 * p.20</a>
	 */
	public FreeBusyType getType() {
		return parameters.getFreeBusyType();
	}

	/**
	 * Sets the person's status over the time periods that are specified in this
	 * property (for example, "free" or "busy"). If not set, the user should be
	 * considered "busy".
	 * @param fbType the type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-20">RFC 5545
	 * p.20</a>
	 */
	public void setType(FreeBusyType fbType) {
		parameters.setFreeBusyType(fbType);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		if (values.isEmpty()) {
			warnings.add(Warning.validate(38));
			return;
		}

		for (Period timePeriod : values) {
			if (timePeriod.getStartDate() == null) {
				warnings.add(Warning.validate(39));
				break;
			}
		}

		for (Period timePeriod : values) {
			if (timePeriod.getEndDate() == null && timePeriod.getDuration() == null) {
				warnings.add(Warning.validate(40));
				break;
			}
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("values", this.values);
		return values;
	}

	@Override
	public FreeBusy copy() {
		return new FreeBusy(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + values.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		FreeBusy other = (FreeBusy) obj;
		if (!values.equals(other.values)) return false;
		return true;
	}
}
