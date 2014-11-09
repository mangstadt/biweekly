package biweekly.property;

import java.util.Date;

import biweekly.util.ICalDate;

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
 * Defines the due date of a to-do task.
 * </p>
 * 
 * <p>
 * <b>Code sample (creating):</b>
 * 
 * <pre class="brush:java">
 * VTodo todo = new VTodo();
 * 
 * //date and time
 * Date datetime = ...
 * DateDue due = new DateDue(datetime);
 * todo.setDateDue(due);
 * 
 * //date (without time component)
 * Date date = ...
 * due = new DateDue(date, false);
 * todo.setDateDue(due);
 * </pre>
 * 
 * </p>
 * 
 * <b>Code sample (reading):</b>
 * 
 * <pre class="brush:java">
 * ICalReader reader = ...
 * ICalendar ical = reader.readNext();
 * TimezoneInfo tzinfo = reader.getTimezoneInfo();
 * 
 * for (VTodo todo : ical.getTodos()){
 *   DateDue due = todo.getDateDue();
 *   
 *   //get property value (ICalDate extends java.util.Date)
 *   ICalDate value = due.getValue();
 *   
 *   if (value.hasTime()){
 *     //the value includes a time component
 *   } else {
 *     //the value is just a date
 *     //date object's time is set to "00:00:00" under local computer's default timezone
 *   }
 *   
 *   //gets the timezone that the property value was parsed under if you are curious about that
 *   TimeZone tz = tzinfo.getTimeZone(due);
 * }
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Code sample (using timezones):</b>
 * 
 * <pre class="brush:java">
 * DateDue due = new DateDue(...);
 * 
 * ICalendar ical = new ICalendar();
 * VTodo todo = new VTodo();
 * Date datetime = ...
 * todo.setDateDue(due);
 * ical.addTodo(todo);
 * 
 * java.util.TimeZone tz = ...
 * ICalWriter writer = new ICalWriter(...);
 * 
 * //set the timezone of all date-time property values
 * //date-time property values are written in UTC by default
 * writer.getTimezoneInfo().setDefaultTimeZone(tz);
 * 
 * //set the timezone just for this property
 * writer.getTimezoneInfo().setTimeZone(due, tz);
 * 
 * //finally, write the iCalendar object
 * writer.write(ical);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-96">RFC 5545 p.96-7</a>
 * @see <a href="http://tools.ietf.org/html/rfc2445#page-92">RFC 2445 p.92-3</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.30</a>
 */
public class DateDue extends DateOrDateTimeProperty {
	/**
	 * Creates a due date property.
	 * @param dueDate the due date
	 */
	public DateDue(Date dueDate) {
		super(dueDate);
	}

	/**
	 * Creates a due date property.
	 * @param dueDate the due date
	 * @param hasTime true if the value has a time component, false if it is
	 * strictly a date
	 */
	public DateDue(Date dueDate, boolean hasTime) {
		super(dueDate, hasTime);
	}

	/**
	 * Creates a due date property.
	 * @param dueDate the due date
	 */
	public DateDue(ICalDate dueDate) {
		super(dueDate);
	}
}
