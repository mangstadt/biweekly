package biweekly.property;

import java.util.Date;

import biweekly.parameter.Related;
import biweekly.util.Duration;

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
 * Defines when an alarm will be triggered.
 * @author Michael Angstadt
 * @see "RFC 5545 p.133-6"
 */
public class Trigger extends ICalProperty {
	private Duration duration;
	private Date date;

	/**
	 * Creates a trigger property.
	 * @param duration the relative time
	 * @param related the date/time field that the duration is relative to
	 */
	public Trigger(Duration duration, Related related) {
		setDuration(duration, related);
	}

	/**
	 * Creates a trigger property.
	 * @param date the date/time the alarm will trigger.
	 */
	public Trigger(Date date) {
		setDate(date);
	}

	/**
	 * Gets the relative time at which the alarm will trigger.
	 * @return the relative time or null if an absolute time is set
	 */
	public Duration getDuration() {
		return duration;
	}

	/**
	 * Sets a relative time at which the alarm will trigger.
	 * @param duration the relative time
	 * @param related the date/time field that the duration is relative to
	 */
	public void setDuration(Duration duration, Related related) {
		this.date = null;
		this.duration = duration;
		setRelated(related);
	}

	/**
	 * Gets the date/time that the alarm will trigger.
	 * @return the date/time or null if a relative duration is set
	 */
	public Date getDate() {
		return date;
	}

	/**
	 * Sets the date/time that the alarm will trigger.
	 * @param date the date/time the alarm will trigger.
	 */
	public void setDate(Date date) {
		this.date = date;
		this.duration = null;
		setRelated(null);
	}

	/**
	 * Gets the date-time field that the duration is relative to.
	 * @return the field or null if not set
	 * @see "RFC 5545 p.24"
	 */
	public Related getRelated() {
		return parameters.getRelated();
	}

	/**
	 * Sets the date-time field that the duration is relative to.
	 * @param related the field or null to remove
	 * @see "RFC 5545 p.24"
	 */
	public void setRelated(Related related) {
		parameters.setRelated(related);
	}
}
