package biweekly.util;

import java.util.Date;

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
 * A period of time.
 * @author Michael Angstadt
 */
public final class Period {
	private final Date startDate;
	private final Date endDate;
	private final Duration duration;

	/**
	 * Creates a new time period.
	 * @param startDate the start date
	 * @param endDate the end date
	 */
	public Period(Date startDate, Date endDate) {
		this.startDate = copy(startDate);
		this.endDate = copy(endDate);
		duration = null;
	}

	/**
	 * Creates a new time period.
	 * @param startDate the start date
	 * @param duration the length of time after the start date
	 */
	public Period(Date startDate, Duration duration) {
		this.startDate = copy(startDate);
		this.duration = duration;
		endDate = null;
	}

	/**
	 * Copies an existing time period.
	 * @param period the period to copy
	 */
	public Period(Period period) {
		this.startDate = period.startDate;
		this.endDate = period.endDate;
		this.duration = period.duration;
	}

	/**
	 * Gets the start date.
	 * @return the start date
	 */
	public Date getStartDate() {
		return copy(startDate);
	}

	/**
	 * Gets the end date. This will be null if a duration was defined.
	 * @return the end date or null if not set
	 */
	public Date getEndDate() {
		return copy(endDate);
	}

	/**
	 * Gets the length of time after the start date. This will be null if an end
	 * date was defined.
	 * @return the duration or null if not set
	 */
	public Duration getDuration() {
		return duration;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Period other = (Period) obj;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		return true;
	}

	private Date copy(Date date) {
		return (date == null) ? null : new Date(date.getTime());
	}
}
