package biweekly.io.scribe.property;

import static biweekly.io.scribe.property.ICalPropertyScribe.date;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;

/**
 * Parses iCal 1.0 RRULE values.
 * @author Michael Angstadt
 */
class RecurrenceParserV1 {
	private final ParseContext context;

	public RecurrenceParserV1(ParseContext context) {
		this.context = context;
	}

	/**
	 * Parses an iCal 1.0 RRULE value.
	 * @param value the RRULE value
	 * @return the parsed recurrence
	 * @throws CannotParseException if there is a problem parsing the value
	 */
	public Recurrence parse(String value) {
		Recurrence.Builder builder = new Recurrence.Builder((Frequency) null);
		LinkedList<String> tokens = splitTokens(value);

		String frequencyStr = parseFrequencyAndInterval(tokens, builder);
		parseCountAndUntil(tokens, builder);

		TokenHandler tokenHandler = getTokenHandler(frequencyStr);
		builder.frequency(tokenHandler.frequency());
		for (String token : tokens) {
			//TODO Don't know how to handle the "$" symbol, ignore it.
			if (token.endsWith("$")) {
				context.addWarning(36, token);
				token = removeLastChar(token);
			}

			tokenHandler.processToken(token, builder);
		}
		tokenHandler.noMoreTokens(builder);

		return builder.build();
	}

	private String removeLastChar(String s) {
		return s.substring(0, s.length() - 1);
	}

	private String parseFrequencyAndInterval(LinkedList<String> tokens, Recurrence.Builder builder) {
		String token = tokens.remove(0);

		Pattern p = Pattern.compile("^([A-Z]+)(\\d+)$");
		Matcher m = p.matcher(token);
		if (!m.find()) {
			throw new CannotParseException(40, token);
		}

		builder.interval(integerValueOf(m.group(2)));
		return m.group(1);
	}

	private void parseCountAndUntil(LinkedList<String> tokens, Recurrence.Builder builder) {
		final int DEFAULT_COUNT = 2;

		if (tokens.isEmpty()) {
			builder.count(DEFAULT_COUNT);
			return;
		}

		String lastToken = tokens.getLast();

		//is the last token COUNT?
		if (lastToken.startsWith("#")) {
			String countStr = lastToken.substring(1);
			Integer count = integerValueOf(countStr);
			if (count == 0) {
				//infinite
			} else {
				builder.count(count);
			}

			tokens.removeLast();
			return;
		}

		//is the last token UNTIL?
		try {
			builder.until(date(lastToken).parse());
			tokens.removeLast();
		} catch (IllegalArgumentException e) {
			//last token is a regular value
			builder.count(DEFAULT_COUNT);
		}
	}

	private TokenHandler getTokenHandler(String frequencyStr) {
		if ("YD".equals(frequencyStr)) {
			return new YDHandler();
		} else if ("YM".equals(frequencyStr)) {
			return new YMHandler();
		} else if ("MD".equals(frequencyStr)) {
			return new MDHandler();
		} else if ("MP".equals(frequencyStr)) {
			return new MPHandler();
		} else if ("W".equals(frequencyStr)) {
			return new WHandler();
		} else if ("D".equals(frequencyStr)) {
			return new DHandler();
		} else if ("M".equals(frequencyStr)) {
			return new MHandler();
		}

		throw new CannotParseException(41, frequencyStr);
	}

	private LinkedList<String> splitTokens(String value) {
		String valueUpper = value.toUpperCase();
		String[] split = valueUpper.split("\\s+");
		return new LinkedList<String>(Arrays.asList(split));
	}

	private interface TokenHandler {
		Frequency frequency();

		void processToken(String token, Recurrence.Builder builder);

		void noMoreTokens(Recurrence.Builder builder);
	}

	private class YDHandler implements TokenHandler {
		@Override
		public Frequency frequency() {
			return Frequency.YEARLY;
		}

		@Override
		public void processToken(String token, Recurrence.Builder builder) {
			Integer dayOfYear = integerValueOf(token);
			builder.byYearDay(dayOfYear);
		}

		@Override
		public void noMoreTokens(Recurrence.Builder builder) {
			//empty
		}
	}

	private class YMHandler implements TokenHandler {
		@Override
		public Frequency frequency() {
			return Frequency.YEARLY;
		}

		@Override
		public void processToken(String token, Recurrence.Builder builder) {
			Integer month = integerValueOf(token);
			builder.byMonth(month);
		}

		@Override
		public void noMoreTokens(Recurrence.Builder builder) {
			//empty
		}
	}

	private class MDHandler implements TokenHandler {
		@Override
		public Frequency frequency() {
			return Frequency.MONTHLY;
		}

		@Override
		public void processToken(String token, Recurrence.Builder builder) {
			try {
				Integer date = "LD".equals(token) ? -1 : parseVCalInt(token);
				builder.byMonthDay(date);
			} catch (NumberFormatException e) {
				throw new CannotParseException(40, token);
			}
		}

		@Override
		public void noMoreTokens(Recurrence.Builder builder) {
			//empty
		}
	}

	private class MPHandler implements TokenHandler {
		private final List<Integer> nums = new ArrayList<Integer>();
		private final List<DayOfWeek> days = new ArrayList<DayOfWeek>();
		private boolean readNum = false;

		@Override
		public Frequency frequency() {
			return Frequency.MONTHLY;
		}

		@Override
		public void processToken(String token, Recurrence.Builder builder) {
			if (token.matches("\\d{4}")) {
				readNum = false;

				Integer hour = integerValueOf(token.substring(0, 2));
				builder.byHour(hour);

				Integer minute = integerValueOf(token.substring(2, 4));
				builder.byMinute(minute);

				return;
			}

			try {
				Integer curNum = parseVCalInt(token);

				if (!readNum) {
					//reset lists, new segment
					for (Integer num : nums) {
						for (DayOfWeek day : days) {
							builder.byDay(num, day);
						}
					}
					nums.clear();
					days.clear();

					readNum = true;
				}

				nums.add(curNum);
			} catch (NumberFormatException e) {
				readNum = false;
				days.add(parseDay(token));
			}
		}

		@Override
		public void noMoreTokens(Recurrence.Builder builder) {
			for (Integer num : nums) {
				for (DayOfWeek day : days) {
					builder.byDay(num, day);
				}
			}
		}
	}

	private class WHandler implements TokenHandler {
		@Override
		public Frequency frequency() {
			return Frequency.WEEKLY;
		}

		@Override
		public void processToken(String token, Recurrence.Builder builder) {
			DayOfWeek day = parseDay(token);
			builder.byDay(day);
		}

		@Override
		public void noMoreTokens(Recurrence.Builder builder) {
			//empty
		}
	}

	private class DHandler implements TokenHandler {
		@Override
		public Frequency frequency() {
			return Frequency.DAILY;
		}

		@Override
		public void processToken(String token, Recurrence.Builder builder) {
			Integer hour = integerValueOf(token.substring(0, 2));
			builder.byHour(hour);

			Integer minute = integerValueOf(token.substring(2, 4));
			builder.byMinute(minute);
		}

		@Override
		public void noMoreTokens(Recurrence.Builder builder) {
			//empty
		}
	}

	private class MHandler implements TokenHandler {
		@Override
		public Frequency frequency() {
			return Frequency.MINUTELY;
		}

		@Override
		public void processToken(String token, Recurrence.Builder builder) {
			//TODO can this ever have values?
		}

		@Override
		public void noMoreTokens(Recurrence.Builder builder) {
			//empty
		}
	}

	/**
	 * Same as {@link Integer#valueOf(String)}, but throws a
	 * {@link CannotParseException} when it fails.
	 * @param value the string to parse
	 * @return the parsed integer
	 * @throws CannotParseException if the string cannot be parsed
	 */
	private Integer integerValueOf(String value) {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			throw new CannotParseException(40, value);
		}
	}

	/**
	 * Parses an integer string, where the sign is at the end of the string
	 * instead of at the beginning.
	 * @param value the integer string (e.g. "5-")
	 * @return the value (e.g. -5)
	 * @throws NumberFormatException if the string cannot be parsed as an
	 * integer
	 */
	private int parseVCalInt(String value) {
		int negate;
		String num;
		if (value.endsWith("+")) {
			num = removeLastChar(value);
			negate = 1;
		} else if (value.endsWith("-")) {
			num = removeLastChar(value);
			negate = -1;
		} else {
			num = value;
			negate = 1;
		}

		return Integer.parseInt(num) * negate;
	}

	private DayOfWeek parseDay(String value) {
		DayOfWeek day = DayOfWeek.valueOfAbbr(value);
		if (day == null) {
			throw new CannotParseException(42, value);
		}

		return day;
	}

	/**
	 * iCal version 1.0 allows multiple RRULE values to be defined inside of the
	 * same property. This method extracts each RRULE value from the property
	 * value.
	 * @param value the property value
	 * @return the RRULE values
	 */
	public static List<String> splitPropertyValue(String value) {
		List<String> values = new ArrayList<String>();
		Pattern p = Pattern.compile("#\\d+|\\d{8}T\\d{6}Z?");
		Matcher m = p.matcher(value);

		int prevIndex = 0;
		while (m.find()) {
			int end = m.end();
			String subValue = value.substring(prevIndex, end).trim();
			values.add(subValue);
			prevIndex = end;
		}
		String subValue = value.substring(prevIndex).trim();
		if (subValue.length() > 0) {
			values.add(subValue);
		}

		return values;
	}
}
