package biweekly.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * Represents a period of time (for example, "2 hours and 30 minutes").
 * </p>
 * <p>
 * This class is immutable. Use the {@link #builder} method to construct a new
 * instance, or the {@link #parse} method to parse a duration string.
 * </p>
 * 
 * <p>
 * <b>Examples:</b>
 * 
 * <pre>
 * Duration duration = Duration.builder().hours(2).minutes(30).build();
 * Duration duration = Duration.parse(&quot;PT2H30M&quot;);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 */
public class Duration {
	private final Integer weeks, days, hours, minutes, seconds;
	private final boolean prior;

	private Duration(Builder b) {
		weeks = b.weeks;
		days = b.days;
		hours = b.hours;
		minutes = b.minutes;
		seconds = b.seconds;
		prior = b.prior;
	}

	/**
	 * Parses a duration string.
	 * @param value the duration string (e.g. "P30DT10H")
	 * @return the parsed duration
	 * @throws IllegalArgumentException if the duration string is invalid
	 */
	public static Duration parse(String value) {
		if (!value.matches("-?P.*")) {
			throw new IllegalArgumentException("Invalid duration string: " + value);
		}

		//@formatter:off
		return Duration.builder()
		.prior(value.startsWith("-"))
		.weeks(parseComponent(value, 'W'))
		.days(parseComponent(value, 'D'))
		.hours(parseComponent(value, 'H'))
		.minutes(parseComponent(value, 'M'))
		.seconds(parseComponent(value, 'S'))
		.build();
		//@formatter:on
	}

	/**
	 * Creates a builder object for constructing new instances of this class.
	 * @return the builder object
	 */
	public static Builder builder() {
		return new Builder();
	}

	private static Integer parseComponent(String value, char ch) {
		Pattern p = Pattern.compile("(\\d+)" + ch);
		Matcher m = p.matcher(value);
		return m.find() ? Integer.valueOf(m.group(1)) : null;
	}

	/**
	 * Gets whether the duration is negative.
	 * @return true if it's negative, false if not
	 */
	public boolean isPrior() {
		return prior;
	}

	/**
	 * Gets the number of weeks.
	 * @return the number of weeks or null if not set
	 */
	public Integer getWeeks() {
		return weeks;
	}

	/**
	 * Gets the number of days.
	 * @return the number of days or null if not set
	 */
	public Integer getDays() {
		return days;
	}

	/**
	 * Gets the number of hours.
	 * @return the number of hours or null if not set
	 */
	public Integer getHours() {
		return hours;
	}

	/**
	 * Gets the number of minutes.
	 * @return the number of minutes or null if not set
	 */
	public Integer getMinutes() {
		return minutes;
	}

	/**
	 * Gets the number of seconds.
	 * @return the number of seconds or null if not set
	 */
	public Integer getSeconds() {
		return seconds;
	}

	/**
	 * Determines if any time components are present.
	 * @return true if the duration has at least one time component, false if
	 * not
	 */
	public boolean hasTime() {
		return hours != null || minutes != null || seconds != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((days == null) ? 0 : days.hashCode());
		result = prime * result + ((hours == null) ? 0 : hours.hashCode());
		result = prime * result + ((minutes == null) ? 0 : minutes.hashCode());
		result = prime * result + (prior ? 1231 : 1237);
		result = prime * result + ((seconds == null) ? 0 : seconds.hashCode());
		result = prime * result + ((weeks == null) ? 0 : weeks.hashCode());
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
		Duration other = (Duration) obj;
		if (days == null) {
			if (other.days != null)
				return false;
		} else if (!days.equals(other.days))
			return false;
		if (hours == null) {
			if (other.hours != null)
				return false;
		} else if (!hours.equals(other.hours))
			return false;
		if (minutes == null) {
			if (other.minutes != null)
				return false;
		} else if (!minutes.equals(other.minutes))
			return false;
		if (prior != other.prior)
			return false;
		if (seconds == null) {
			if (other.seconds != null)
				return false;
		} else if (!seconds.equals(other.seconds))
			return false;
		if (weeks == null) {
			if (other.weeks != null)
				return false;
		} else if (!weeks.equals(other.weeks))
			return false;
		return true;
	}

	/**
	 * Converts the duration to its string representation.
	 * @return the string representation (e.g. "P4DT1H" for "4 days and 1 hour")
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		if (prior) {
			sb.append('-');
		}
		sb.append('P');

		if (weeks != null) {
			sb.append(weeks).append('W');
		}

		if (days != null) {
			sb.append(days).append('D');
		}

		if (hasTime()) {
			sb.append('T');

			if (hours != null) {
				sb.append(hours).append('H');
			}

			if (minutes != null) {
				sb.append(minutes).append('M');
			}

			if (seconds != null) {
				sb.append(seconds).append('S');
			}
		}

		return sb.toString();
	}

	/**
	 * Builds {@link Duration} objects.
	 */
	public static class Builder {
		private Integer weeks, days, hours, minutes, seconds;
		private boolean prior = false;

		/**
		 * Creates a new {@link Duration} builder.
		 */
		public Builder() {
			//empty
		}

		/**
		 * Creates a new {@link Duration} builder.
		 * @param source the object to copy from
		 */
		public Builder(Duration source) {
			weeks = source.weeks;
			days = source.days;
			hours = source.hours;
			minutes = source.minutes;
			seconds = source.seconds;
			prior = source.prior;
		}

		/**
		 * Sets the number of weeks.
		 * @param weeks the number of weeks
		 * @return this
		 */
		public Builder weeks(Integer weeks) {
			this.weeks = weeks;
			return this;
		}

		/**
		 * Sets the number of days
		 * @param days the number of days
		 * @return this
		 */
		public Builder days(Integer days) {
			this.days = days;
			return this;
		}

		/**
		 * Sets the number of hours
		 * @param hours the number of hours
		 * @return this
		 */
		public Builder hours(Integer hours) {
			this.hours = hours;
			return this;
		}

		/**
		 * Sets the number of minutes
		 * @param minutes the number of minutes
		 * @return this
		 */
		public Builder minutes(Integer minutes) {
			this.minutes = minutes;
			return this;
		}

		/**
		 * Sets the number of seconds.
		 * @param seconds the number of seconds
		 * @return this
		 */
		public Builder seconds(Integer seconds) {
			this.seconds = seconds;
			return this;
		}

		/**
		 * Sets whether the duration should be negative.
		 * @param prior true to be negative, false not to be
		 * @return this
		 */
		public Builder prior(boolean prior) {
			this.prior = prior;
			return this;
		}

		/**
		 * Builds the final {@link Duration} object.
		 * @return the object
		 */
		public Duration build() {
			return new Duration(this);
		}
	}
}
