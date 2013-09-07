package biweekly.property;

import biweekly.component.VTimezone;

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
 * Defines a unique identifier for a {@link VTimezone} component. The identifier
 * must be unique within the scope of the iCalendar object.
 * </p>
 * <p>
 * Date-time properties that support timezones (such as {@link DateStart}) can
 * format their date-time values according to the rules defined in the
 * {@link VTimezone} component, and then use this ID to reference the component
 * by assigning the ID to a TZID parameter.
 * </p>
 * <p>
 * All properties that support timezones will have
 * {@code get/setTimezoneId()} methods. If a property has no timezone
 * assigned to it, it is written in UTC.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre>
 * VTimezone timezone = new VTimezone(&quot;Eastern&quot;);
 * 
 * Date start = ...;
 * DateStart dtstart = new DateStart(start);
 * dtStart.setTimezoneId(&quot;Eastern&quot;);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-102">RFC 5545
 * p.102-3</a>
 */
public class TimezoneId extends TextProperty {
	/**
	 * Creates a timezone identifier property.
	 * @param timezone the timezone identifier
	 */
	public TimezoneId(String timezone) {
		super(timezone);
	}
}
