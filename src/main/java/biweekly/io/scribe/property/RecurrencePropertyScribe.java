package biweekly.io.scribe.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.io.xml.XCalNamespaceContext;
import biweekly.parameter.ICalParameters;
import biweekly.property.RecurrenceProperty;
import biweekly.util.ICalDate;
import biweekly.util.ListMultimap;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.ByDay;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;
import biweekly.util.XmlUtils;

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
 * Marshals properties whose values are {@link Recurrence}.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class RecurrencePropertyScribe<T extends RecurrenceProperty> extends ICalPropertyScribe<T> {
	private static final String FREQ = "FREQ";
	private static final String UNTIL = "UNTIL";
	private static final String COUNT = "COUNT";
	private static final String INTERVAL = "INTERVAL";
	private static final String BYSECOND = "BYSECOND";
	private static final String BYMINUTE = "BYMINUTE";
	private static final String BYHOUR = "BYHOUR";
	private static final String BYDAY = "BYDAY";
	private static final String BYMONTHDAY = "BYMONTHDAY";
	private static final String BYYEARDAY = "BYYEARDAY";
	private static final String BYWEEKNO = "BYWEEKNO";
	private static final String BYMONTH = "BYMONTH";
	private static final String BYSETPOS = "BYSETPOS";
	private static final String WKST = "WKST";

	public RecurrencePropertyScribe(Class<T> clazz, String propertyName) {
		super(clazz, propertyName, ICalDataType.RECUR);
	}

	@Override
	protected ICalParameters _prepareParameters(T property, WriteContext context) {
		if (isInObservance(context)) {
			return property.getParameters();
		}

		Recurrence recur = property.getValue();
		if (recur == null || recur.getUntil() == null) {
			return property.getParameters();
		}

		return handleTzidParameter(property, recur.getUntil().hasTime(), context);
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		//null value
		Recurrence recur = property.getValue();
		if (recur == null) {
			return "";
		}

		//iCal 2.0
		if (context.getVersion() != ICalVersion.V1_0) {
			ListMultimap<String, Object> components = buildComponents(property, context, false);
			return object(components.getMap());
		}

		//vCal 1.0
		Frequency frequency = recur.getFrequency();
		if (frequency == null) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		Integer interval = recur.getInterval();
		if (interval == null) {
			interval = 1;
		}

		switch (frequency) {
		case YEARLY:
			if (!recur.getByMonth().isEmpty()) {
				sb.append("YM").append(interval);
				for (Integer month : recur.getByMonth()) {
					sb.append(' ').append(month);
				}
			} else {
				sb.append("YD").append(interval);
				for (Integer day : recur.getByYearDay()) {
					sb.append(' ').append(day);
				}
			}
			break;

		case MONTHLY:
			if (!recur.getByMonthDay().isEmpty()) {
				sb.append("MD").append(interval);
				for (Integer day : recur.getByMonthDay()) {
					sb.append(' ').append(writeVCalInt(day));
				}
			} else {
				sb.append("MP").append(interval);
				for (ByDay byDay : recur.getByDay()) {
					DayOfWeek day = byDay.getDay();
					Integer prefix = byDay.getNum();
					if (prefix == null) {
						prefix = 1;
					}

					sb.append(' ').append(writeVCalInt(prefix)).append(' ').append(day.getAbbr());
				}
			}
			break;

		case WEEKLY:
			sb.append("W").append(interval);
			for (ByDay byDay : recur.getByDay()) {
				sb.append(' ').append(byDay.getDay().getAbbr());
			}
			break;

		case DAILY:
			sb.append("D").append(interval);
			break;

		case HOURLY:
			sb.append("M").append(interval * 60);
			break;

		case MINUTELY:
			sb.append("M").append(interval);
			break;

		default:
			return "";
		}

		Integer count = recur.getCount();
		ICalDate until = recur.getUntil();
		sb.append(' ');

		if (count != null) {
			sb.append('#').append(count);
		} else if (until != null) {
			TimezoneInfo tzinfo = context.getTimezoneInfo();
			boolean hasTime = until.hasTime();
			boolean floating = tzinfo.isFloating(property);
			TimeZone tz = tzinfo.getTimeZoneToWriteIn(property);
			context.addDate(hasTime, floating, tz, until);
			sb.append(date(until).time(hasTime).tz(floating, tz).write());
		} else {
			sb.append("#0");
		}

		return sb.toString();
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		final Recurrence.Builder builder = new Recurrence.Builder((Frequency) null);

		if (value.length() == 0) {
			return newInstance(builder.build());
		}

		if (context.getVersion() != ICalVersion.V1_0) {
			ListMultimap<String, String> rules = object(value);

			List<Warning> warnings = context.getWarnings();
			parseFreq(rules, builder, warnings);
			parseUntil(rules, builder, warnings);
			parseCount(rules, builder, warnings);
			parseInterval(rules, builder, warnings);
			parseBySecond(rules, builder, warnings);
			parseByMinute(rules, builder, warnings);
			parseByHour(rules, builder, warnings);
			parseByDay(rules, builder, warnings);
			parseByMonthDay(rules, builder, warnings);
			parseByYearDay(rules, builder, warnings);
			parseByWeekNo(rules, builder, warnings);
			parseByMonth(rules, builder, warnings);
			parseBySetPos(rules, builder, warnings);
			parseWkst(rules, builder, warnings);
			parseXRules(rules, builder, warnings); //must be called last

			T property = newInstance(builder.build());

			ICalDate until = property.getValue().getUntil();
			if (until != null) {
				context.addDate(until, property, parameters);
			}

			return property;
		}

		String splitValues[] = value.toUpperCase().split("\\s+");

		//parse the frequency and interval from the first token (e.g. "W2")
		String frequencyStr;
		Integer interval;
		{
			String firstToken = splitValues[0];
			Pattern p = Pattern.compile("^([A-Z]+)(\\d+)$");
			Matcher m = p.matcher(firstToken);
			if (!m.find()) {
				throw new CannotParseException("Invalid token: " + firstToken);
			}

			frequencyStr = m.group(1);
			interval = Integer.valueOf(m.group(2));
			splitValues = Arrays.copyOfRange(splitValues, 1, splitValues.length);
		}
		builder.interval(interval);

		Integer count = null;
		ICalDate until = null;
		if (splitValues.length == 0) {
			count = 2;
		} else {
			String lastToken = splitValues[splitValues.length - 1];
			if (lastToken.startsWith("#")) {
				String countStr = lastToken.substring(1, lastToken.length());
				count = Integer.valueOf(countStr);
				if (count == 0) {
					//infinite
					count = null;
				}

				splitValues = Arrays.copyOfRange(splitValues, 0, splitValues.length - 1);
			} else {
				try {
					//see if the value is an "until" date
					until = date(lastToken).parse();
					splitValues = Arrays.copyOfRange(splitValues, 0, splitValues.length - 1);
				} catch (IllegalArgumentException e) {
					//last token is a regular value
					count = 2;
				}
			}
		}
		builder.count(count);
		builder.until(until);

		//determine what frequency enum to use and how to treat each tokenized value
		Frequency frequency = null;
		Handler<String> handler = null;
		if ("YD".equals(frequencyStr)) {
			frequency = Frequency.YEARLY;
			handler = new Handler<String>() {
				public void handle(String value) {
					if (value == null) {
						return;
					}

					Integer dayOfYear = Integer.valueOf(value);
					builder.byYearDay(dayOfYear);
				}
			};
		} else if ("YM".equals(frequencyStr)) {
			frequency = Frequency.YEARLY;
			handler = new Handler<String>() {
				public void handle(String value) {
					if (value == null) {
						return;
					}

					Integer month = Integer.valueOf(value);
					builder.byMonth(month);
				}
			};
		} else if ("MD".equals(frequencyStr)) {
			frequency = Frequency.MONTHLY;
			handler = new Handler<String>() {
				public void handle(String value) {
					if (value == null) {
						return;
					}

					Integer date = "LD".equals(value) ? -1 : parseVCalInt(value);
					builder.byMonthDay(date);
				}
			};
		} else if ("MP".equals(frequencyStr)) {
			frequency = Frequency.MONTHLY;
			handler = new Handler<String>() {
				private final List<Integer> nums = new ArrayList<Integer>();
				private final List<DayOfWeek> days = new ArrayList<DayOfWeek>();
				private boolean readNum = false;

				public void handle(String value) {
					if (value == null) {
						//end of list
						for (Integer num : nums) {
							for (DayOfWeek day : days) {
								builder.byDay(num, day);
							}
						}
						return;
					}

					if (value.matches("\\d{4}")) {
						readNum = false;

						Integer hour = Integer.valueOf(value.substring(0, 2));
						builder.byHour(hour);

						Integer minute = Integer.valueOf(value.substring(2, 4));
						builder.byMinute(minute);
						return;
					}

					try {
						Integer curNum = parseVCalInt(value);

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

						DayOfWeek day = parseDay(value);
						days.add(day);
					}
				}
			};
		} else if ("W".equals(frequencyStr)) {
			frequency = Frequency.WEEKLY;
			handler = new Handler<String>() {
				public void handle(String value) {
					if (value == null) {
						return;
					}

					DayOfWeek day = parseDay(value);
					builder.byDay(day);
				}
			};
		} else if ("D".equals(frequencyStr)) {
			frequency = Frequency.DAILY;
			handler = new Handler<String>() {
				public void handle(String value) {
					if (value == null) {
						return;
					}

					Integer hour = Integer.valueOf(value.substring(0, 2));
					builder.byHour(hour);

					Integer minute = Integer.valueOf(value.substring(2, 4));
					builder.byMinute(minute);
				}
			};
		} else if ("M".equals(frequencyStr)) {
			frequency = Frequency.MINUTELY;
			handler = new Handler<String>() {
				public void handle(String value) {
					//TODO can this ever have values?
				}
			};
		} else {
			throw new CannotParseException("Unrecognized frequency: " + frequencyStr);
		}

		builder.frequency(frequency);

		//parse the rest of the tokens
		for (String splitValue : splitValues) {
			//TODO not sure how to handle the "$" symbol, ignore it
			if (splitValue.endsWith("$")) {
				context.addWarning(36, splitValue);
				splitValue = splitValue.substring(0, splitValue.length() - 1);
			}

			handler.handle(splitValue);
		}
		handler.handle(null);

		T property = newInstance(builder.build());
		if (until != null) {
			context.addDate(until, property, parameters);
		}

		return property;
	}

	private Integer parseVCalInt(String value) {
		int negate = 1;
		if (value.endsWith("+")) {
			value = value.substring(0, value.length() - 1);
		} else if (value.endsWith("-")) {
			value = value.substring(0, value.length() - 1);
			negate = -1;
		}

		return Integer.valueOf(value) * negate;
	}

	private String writeVCalInt(Integer value) {
		if (value > 0) {
			return value + "+";
		}

		if (value < 0) {
			return Math.abs(value) + "-";
		}

		return value + "";
	}

	private DayOfWeek parseDay(String value) {
		DayOfWeek day = DayOfWeek.valueOfAbbr(value);
		if (day == null) {
			throw new CannotParseException("Invalid day: " + value);
		}

		return day;
	}

	@Override
	protected void _writeXml(T property, XCalElement element, WriteContext context) {
		XCalElement recurElement = element.append(dataType(property, null));

		Recurrence recur = property.getValue();
		if (recur == null) {
			return;
		}

		ListMultimap<String, Object> components = buildComponents(property, context, true);
		for (Map.Entry<String, List<Object>> component : components) {
			String name = component.getKey().toLowerCase();
			for (Object value : component.getValue()) {
				recurElement.append(name, value.toString());
			}
		}
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		XCalElement value = element.child(defaultDataType);
		if (value == null) {
			throw missingXmlElements(defaultDataType);
		}

		ListMultimap<String, String> rules = new ListMultimap<String, String>();
		for (Element child : XmlUtils.toElementList(value.getElement().getChildNodes())) {
			if (!XCalNamespaceContext.XCAL_NS.equals(child.getNamespaceURI())) {
				continue;
			}

			String name = child.getLocalName().toUpperCase();
			String text = child.getTextContent();
			rules.put(name, text);
		}

		Recurrence.Builder builder = new Recurrence.Builder((Frequency) null);

		List<Warning> warnings = context.getWarnings();
		parseFreq(rules, builder, warnings);
		parseUntil(rules, builder, warnings);
		parseCount(rules, builder, warnings);
		parseInterval(rules, builder, warnings);
		parseBySecond(rules, builder, warnings);
		parseByMinute(rules, builder, warnings);
		parseByHour(rules, builder, warnings);
		parseByDay(rules, builder, warnings);
		parseByMonthDay(rules, builder, warnings);
		parseByYearDay(rules, builder, warnings);
		parseByWeekNo(rules, builder, warnings);
		parseByMonth(rules, builder, warnings);
		parseBySetPos(rules, builder, warnings);
		parseWkst(rules, builder, warnings);
		parseXRules(rules, builder, warnings); //must be called last

		T property = newInstance(builder.build());

		ICalDate until = property.getValue().getUntil();
		if (until != null) {
			context.addDate(until, property, parameters);
		}

		return property;
	}

	@Override
	protected JCalValue _writeJson(T property, WriteContext context) {
		Recurrence recur = property.getValue();
		if (recur == null) {
			return JCalValue.object(new ListMultimap<String, Object>(0));
		}

		ListMultimap<String, Object> components = buildComponents(property, context, true);

		//lower-case all the keys
		ListMultimap<String, Object> object = new ListMultimap<String, Object>(components.keySet().size());
		for (Map.Entry<String, List<Object>> entry : components) {
			String key = entry.getKey().toLowerCase();
			object.putAll(key, entry.getValue());
		}

		return JCalValue.object(object);
	}

	@Override
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		Recurrence.Builder builder = new Recurrence.Builder((Frequency) null);

		//upper-case the keys
		ListMultimap<String, String> object = value.asObject();
		ListMultimap<String, String> rules = new ListMultimap<String, String>(object.keySet().size());
		for (Map.Entry<String, List<String>> entry : object) {
			String key = entry.getKey().toUpperCase();
			rules.putAll(key, entry.getValue());
		}

		List<Warning> warnings = context.getWarnings();
		parseFreq(rules, builder, warnings);
		parseUntil(rules, builder, warnings);
		parseCount(rules, builder, warnings);
		parseInterval(rules, builder, warnings);
		parseBySecond(rules, builder, warnings);
		parseByMinute(rules, builder, warnings);
		parseByHour(rules, builder, warnings);
		parseByDay(rules, builder, warnings);
		parseByMonthDay(rules, builder, warnings);
		parseByYearDay(rules, builder, warnings);
		parseByWeekNo(rules, builder, warnings);
		parseByMonth(rules, builder, warnings);
		parseBySetPos(rules, builder, warnings);
		parseWkst(rules, builder, warnings);
		parseXRules(rules, builder, warnings); //must be called last

		T property = newInstance(builder.build());

		ICalDate until = property.getValue().getUntil();
		if (until != null) {
			context.addDate(until, property, parameters);
		}

		return property;
	}

	/**
	 * Creates a new instance of the recurrence property.
	 * @param recur the recurrence value
	 * @return the new instance
	 */
	protected abstract T newInstance(Recurrence recur);

	private void parseFreq(ListMultimap<String, String> rules, final Recurrence.Builder builder, final List<Warning> warnings) {
		parseFirst(rules, FREQ, new Handler<String>() {
			public void handle(String value) {
				value = value.toUpperCase();
				try {
					Frequency frequency = Frequency.valueOf(value);
					builder.frequency(frequency);
				} catch (IllegalArgumentException e) {
					warnings.add(Warning.parse(7, FREQ, value));
				}
			}
		});
	}

	private void parseUntil(ListMultimap<String, String> rules, final Recurrence.Builder builder, final List<Warning> warnings) {
		parseFirst(rules, UNTIL, new Handler<String>() {
			public void handle(String value) {
				try {
					ICalDate date = date(value).parse();
					builder.until(date);
				} catch (IllegalArgumentException e) {
					warnings.add(Warning.parse(7, UNTIL, value));
				}
			}
		});
	}

	private void parseCount(ListMultimap<String, String> rules, final Recurrence.Builder builder, final List<Warning> warnings) {
		parseFirst(rules, COUNT, new Handler<String>() {
			public void handle(String value) {
				try {
					builder.count(Integer.valueOf(value));
				} catch (NumberFormatException e) {
					warnings.add(Warning.parse(7, COUNT, value));
				}
			}
		});
	}

	private void parseInterval(ListMultimap<String, String> rules, final Recurrence.Builder builder, final List<Warning> warnings) {
		parseFirst(rules, INTERVAL, new Handler<String>() {
			public void handle(String value) {
				try {
					builder.interval(Integer.valueOf(value));
				} catch (NumberFormatException e) {
					warnings.add(Warning.parse(7, INTERVAL, value));
				}
			}
		});
	}

	private void parseBySecond(ListMultimap<String, String> rules, final Recurrence.Builder builder, List<Warning> warnings) {
		parseIntegerList(BYSECOND, rules, warnings, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.bySecond(value);
			}
		});
	}

	private void parseByMinute(ListMultimap<String, String> rules, final Recurrence.Builder builder, List<Warning> warnings) {
		parseIntegerList(BYMINUTE, rules, warnings, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byMinute(value);
			}
		});
	}

	private void parseByHour(ListMultimap<String, String> rules, final Recurrence.Builder builder, List<Warning> warnings) {
		parseIntegerList(BYHOUR, rules, warnings, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byHour(value);
			}
		});
	}

	private void parseByDay(ListMultimap<String, String> rules, Recurrence.Builder builder, List<Warning> warnings) {
		Pattern p = Pattern.compile("^([-+]?\\d+)?(.*)$");
		for (String value : rules.removeAll(BYDAY)) {
			Matcher m = p.matcher(value);
			if (!m.find()) {
				//this should never happen
				//the regex contains a "match-all" pattern and should never not find anything
				warnings.add(Warning.parse(7, BYDAY, value));
				continue;
			}

			String dayStr = m.group(2);
			DayOfWeek day = DayOfWeek.valueOfAbbr(dayStr);
			if (day == null) {
				warnings.add(Warning.parse(7, BYDAY, value));
				continue;
			}

			String prefixStr = m.group(1);
			Integer prefix = (prefixStr == null) ? null : Integer.valueOf(prefixStr); //no need to catch NumberFormatException because the regex guarantees that it will be a number

			builder.byDay(prefix, day);
		}
	}

	private void parseByMonthDay(ListMultimap<String, String> rules, final Recurrence.Builder builder, List<Warning> warnings) {
		parseIntegerList(BYMONTHDAY, rules, warnings, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byMonthDay(value);
			}
		});
	}

	private void parseByYearDay(ListMultimap<String, String> rules, final Recurrence.Builder builder, List<Warning> warnings) {
		parseIntegerList(BYYEARDAY, rules, warnings, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byYearDay(value);
			}
		});
	}

	private void parseByWeekNo(ListMultimap<String, String> rules, final Recurrence.Builder builder, List<Warning> warnings) {
		parseIntegerList(BYWEEKNO, rules, warnings, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byWeekNo(value);
			}
		});
	}

	private void parseByMonth(ListMultimap<String, String> rules, final Recurrence.Builder builder, List<Warning> warnings) {
		parseIntegerList(BYMONTH, rules, warnings, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byMonth(value);
			}
		});
	}

	private void parseBySetPos(ListMultimap<String, String> rules, final Recurrence.Builder builder, List<Warning> warnings) {
		parseIntegerList(BYSETPOS, rules, warnings, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.bySetPos(value);
			}
		});
	}

	private void parseWkst(ListMultimap<String, String> rules, final Recurrence.Builder builder, final List<Warning> warnings) {
		parseFirst(rules, WKST, new Handler<String>() {
			public void handle(String value) {
				DayOfWeek day = DayOfWeek.valueOfAbbr(value);
				if (day == null) {
					warnings.add(Warning.parse(7, WKST, value));
					return;
				}

				builder.workweekStarts(day);
			}
		});
	}

	private void parseXRules(ListMultimap<String, String> rules, Recurrence.Builder builder, List<Warning> warnings) {
		for (Map.Entry<String, List<String>> rule : rules) {
			String name = rule.getKey();
			for (String value : rule.getValue()) {
				builder.xrule(name, value);
			}
		}
	}

	private ListMultimap<String, Object> buildComponents(T property, WriteContext context, boolean extended) {
		ListMultimap<String, Object> components = new ListMultimap<String, Object>();
		Recurrence recur = property.getValue();

		//FREQ must come first
		if (recur.getFrequency() != null) {
			components.put(FREQ, recur.getFrequency().name());
		}

		if (recur.getUntil() != null) {
			ICalDate value = recur.getUntil();
			TimezoneInfo tzinfo = context.getTimezoneInfo();
			boolean hasTime = value.hasTime();
			boolean floating = tzinfo.isFloating(property);
			TimeZone tz = tzinfo.getTimeZoneToWriteIn(property);
			context.addDate(hasTime, floating, tz, value);
			String s = date(value).time(hasTime).tz(floating, tz).extended(extended).write();
			components.put(UNTIL, s);
		}

		if (recur.getCount() != null) {
			components.put(COUNT, recur.getCount());
		}

		if (recur.getInterval() != null) {
			components.put(INTERVAL, recur.getInterval());
		}

		addIntegerListComponent(components, BYSECOND, recur.getBySecond());
		addIntegerListComponent(components, BYMINUTE, recur.getByMinute());
		addIntegerListComponent(components, BYHOUR, recur.getByHour());

		for (ByDay byDay : recur.getByDay()) {
			Integer prefix = byDay.getNum();
			DayOfWeek day = byDay.getDay();

			String value = day.getAbbr();
			if (prefix != null) {
				value = prefix + value;
			}
			components.put(BYDAY, value);
		}

		addIntegerListComponent(components, BYMONTHDAY, recur.getByMonthDay());
		addIntegerListComponent(components, BYYEARDAY, recur.getByYearDay());
		addIntegerListComponent(components, BYWEEKNO, recur.getByWeekNo());
		addIntegerListComponent(components, BYMONTH, recur.getByMonth());
		addIntegerListComponent(components, BYSETPOS, recur.getBySetPos());

		if (recur.getWorkweekStarts() != null) {
			components.put(WKST, recur.getWorkweekStarts().getAbbr());
		}

		for (Map.Entry<String, List<String>> entry : recur.getXRules().entrySet()) {
			String name = entry.getKey();
			for (String value : entry.getValue()) {
				components.put(name, value);
			}
		}

		return components;
	}

	private void addIntegerListComponent(ListMultimap<String, Object> components, String name, List<Integer> values) {
		for (Integer value : values) {
			components.put(name, value);
		}
	}

	private void parseFirst(ListMultimap<String, String> rules, String name, Handler<String> handler) {
		List<String> values = rules.removeAll(name);
		if (values.isEmpty()) {
			return;
		}

		String value = values.get(0);
		handler.handle(value);
	}

	private void parseIntegerList(String name, ListMultimap<String, String> rules, List<Warning> warnings, Handler<Integer> handler) {
		List<String> values = rules.removeAll(name);
		for (String value : values) {
			try {
				handler.handle(Integer.valueOf(value));
			} catch (NumberFormatException e) {
				warnings.add(Warning.parse(8, name, value));
			}
		}
	}

	private interface Handler<T> {
		void handle(T value);
	}
}
