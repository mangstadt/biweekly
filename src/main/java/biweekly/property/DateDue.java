package biweekly.property;

import java.util.Date;

import biweekly.util.DateTimeComponents;

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
 * 
 * //date and time with timezone (Date object converted to the specified timezone when writing the iCalendar object)
 * Date datetime = ... 
 * due = new DateDue(datetime); 
 * due.setTimezoneId("America/New_York");
 * todo.setDateDue(due);
 * 
 * //raw date/time components 
 * DateTimeComponents components = new DateTimeComponents(1999, 4, 4, 2, 0, 0, false);
 * due = new DateDue(components);
 * todo.setDateDue(due);
 * </pre>
 * 
 * </p>
 * 
 * <b>Code sample (retrieving):</b>
 * 
 * <pre class="brush:java">
 * ICalendar ical = ...
 * for (VTodo todo : ical.getTodos()){
 *   DateDue due = todo.getDateDue();
 *   
 *   //get the raw date/time components from the date string
 *   DateTimeComponents components = due.getRawComponents();
 *   int year = components.getYear();
 *   int month = components.getMonth();
 *   //etc.
 *   
 *   //get the Java Date object that was generated based on the provided timezone
 *   Date value = due.getValue();
 *   
 *   if (due.hasTime()){
 *     //the value includes a time component
 *     
 *     if (due.isLocalTime()){
 *       //timezone information was not provided
 *       //Java Date object was parsed under the local computer's default timezone
 *     } else {
 *       //timezone information was provided
 *       //Java Date object was parsed under the provided timezone (if recognized)
 *     }
 *   } else {
 *     //the value is just a date
 *     //Java Date object's time is set to "00:00:00" under the local computer's default timezone
 *   }
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @rfc 5545 p.96-7
 */
public class DateDue extends DateOrDateTimeProperty {
	/**
	 * Creates a due date property.
	 * @param dueDate the due date
	 */
	public DateDue(Date dueDate) {
		this(dueDate, true);
	}

	/**
	 * Creates a due date property.
	 * @param dueDate the due date
	 * @param hasTime true to include the time component of the date, false not
	 * to
	 */
	public DateDue(Date dueDate, boolean hasTime) {
		super(dueDate, hasTime);
	}

	/**
	 * Creates an due date property.
	 * @param components the raw components of the date-time value
	 */
	public DateDue(DateTimeComponents components) {
		super(components);
	}
}
