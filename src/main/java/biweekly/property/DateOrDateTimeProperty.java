package biweekly.property;

import java.util.Date;
import java.util.List;

import biweekly.component.ICalComponent;


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
 * Represents a property whose value is a date or a date/time.
 * @author Michael Angstadt
 */
public class DateOrDateTimeProperty extends ICalProperty {
	protected Date value;
	protected boolean hasTime;

	/**
	 * Creates a new property.
	 * @param value the date
	 * @param hasTime true if the date has a time component, false if it is
	 * strictly a date
	 */
	public DateOrDateTimeProperty(Date value, boolean hasTime) {
		setValue(value, hasTime);
	}

	/**
	 * Gets the date.
	 * @return the date
	 */
	public Date getValue() {
		return value;
	}

	/**
	 * Sets the dat.
	 * @param value the date
	 * @param hasTime true if the date has a time component, false if it is
	 * strictly a date
	 */
	public void setValue(Date value, boolean hasTime) {
		this.value = value;
		this.hasTime = hasTime;
	}

	/**
	 * Determines whether the date has a time component.
	 * @return true if the date has a time component, false if it is strictly a
	 * date
	 */
	public boolean hasTime() {
		return hasTime;
	}

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		if (value == null) {
			warnings.add("No value set.");
		}
	}
}
