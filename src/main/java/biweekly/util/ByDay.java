package biweekly.util;

import java.util.Objects;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
 * Represents a specific day or all days in a month or year.
 * </p>
 * <p>
 * Examples:
 * </p>
 * <ul>
 * <li>{@code new ByDay(3, DayOfWeek.MONDAY)} - The third Monday in
 * the month/year.</li>
 * <li>{@code new ByDay(-1, DayOfWeek.MONDAY)} - The last Monday in the
 * month/year.</li>
 * <li>{@code new ByDay(DayOfWeek.MONDAY)} - Every Monday in the month/year.
 * </li>
 * </ul>
 * @author Michael Angstadt
 */
public class ByDay {
	private final Integer num;
	private final DayOfWeek day;

	/**
	 * Creates a BYDAY rule that represents all days in the month/year.
	 * @param day the day of the week (cannot be null)
	 */
	public ByDay(DayOfWeek day) {
		this(null, day);
	}

	/**
	 * Creates a BYDAY rule.
	 * @param num the number (e.g. 3 for "third Sunday", cannot be zero)
	 * @param day the day of the week (cannot be null)
	 */
	public ByDay(Integer num, DayOfWeek day) {
		this.num = num;
		this.day = day;
	}

	/**
	 * Gets the number.
	 * @return the number (can be null)
	 */
	public Integer getNum() {
		return num;
	}

	/**
	 * Gets the day of the week.
	 * @return the day of the week
	 */
	public DayOfWeek getDay() {
		return day;
	}

	@Override
	public int hashCode() {
		return Objects.hash(day, num);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		ByDay other = (ByDay) obj;
		return day == other.day && Objects.equals(num, other.num);
	}
}