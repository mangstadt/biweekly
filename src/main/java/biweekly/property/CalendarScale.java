package biweekly.property;

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
 * Specifies the calendar system that this iCalendar object uses. If none is
 * specified, then the calendar is assumed to be in "gregorian" format.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre>
 * //creating a new property
 * CalendarScale calscale = CalendarScale.gregorian();
 * 
 * if (calscale.isGregorian()) {
 * 	//its value is &quot;GREGORIAN&quot;
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-76">RFC 5545 p.76-7</a>
 */
public class CalendarScale extends TextProperty {
	private static final String GREGORIAN = "GREGORIAN";

	/**
	 * Creates a new calendar scale property. Use of this constructor is
	 * discouraged and may put the property in an invalid state. Use one of the
	 * static factory methods instead.
	 * @param value the value of the property (e.g. "gregorian")
	 */
	public CalendarScale(String value) {
		super(value);
	}

	/**
	 * Creates a new property whose value is set to "gregorian".
	 * @return the new property
	 */
	public static CalendarScale gregorian() {
		return new CalendarScale(GREGORIAN);
	}

	/**
	 * Determines whether the property is set to "gregorian".
	 * @return true if it's set to "gregorian", false if not
	 */
	public boolean isGregorian() {
		return GREGORIAN.equalsIgnoreCase(value);
	}
}
