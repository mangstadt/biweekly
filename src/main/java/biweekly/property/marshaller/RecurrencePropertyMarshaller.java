package biweekly.property.marshaller;

import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biweekly.ICalDataType;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.RecurrenceProperty;
import biweekly.util.ICalDateFormatter;
import biweekly.util.ListMultimap;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;
import biweekly.util.StringUtils;
import biweekly.util.StringUtils.JoinMapCallback;

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
 * Marshals properties whose values are {@link Recurrence}.
 * @author Michael Angstadt
 */
public abstract class RecurrencePropertyMarshaller<T extends RecurrenceProperty> extends ICalPropertyMarshaller<T> {
	public RecurrencePropertyMarshaller(Class<T> clazz, String propertyName) {
		super(clazz, propertyName, ICalDataType.RECUR);
	}

	@Override
	protected String _writeText(T property) {
		Recurrence recur = property.getValue();
		if (recur == null) {
			return "";
		}

		ListMultimap<String, Object> components = buildComponents(recur, false);
		return StringUtils.join(components.getMap(), ";", new JoinMapCallback<String, List<Object>>() {
			public void handle(StringBuilder sb, String key, List<Object> values) {
				sb.append(key).append('=');
				StringUtils.join(values, ",", sb);
			}
		});
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<String> warnings) {
		ListMultimap<String, String> components = new ListMultimap<String, String>();
		for (String component : value.split(";")) {
			String split[] = component.split("=");
			if (split.length < 2) {
				warnings.add("Skipping invalid recurrence rule component: " + component);
				continue;
			}

			String name = split[0];
			String values[] = split[1].split(",");
			components.putAll(name.toUpperCase(), Arrays.asList(values));
		}

		Recurrence.Builder builder = new Recurrence.Builder((Frequency) null);

		parseFreq(components.first("FREQ"), builder, warnings);
		parseUntil(components.first("UNTIL"), builder, warnings);
		parseCount(components.first("COUNT"), builder, warnings);
		parseInterval(components.first("INTERVAL"), builder, warnings);
		parseBySecond(components.get("BYSECOND"), builder, warnings);
		parseByMinute(components.get("BYMINUTE"), builder, warnings);
		parseByHour(components.get("BYHOUR"), builder, warnings);
		parseByDay(components.get("BYDAY"), builder, warnings);
		parseByMonthDay(components.get("BYMONTHDAY"), builder, warnings);
		parseByYearDay(components.get("BYYEARDAY"), builder, warnings);
		parseByWeekNo(components.get("BYWEEKNO"), builder, warnings);
		parseByMonth(components.get("BYMONTH"), builder, warnings);
		parseBySetPos(components.get("BYSETPOS"), builder, warnings);
		parseWkst(components.first("WKST"), builder, warnings);

		return newInstance(builder.build());
	}

	@Override
	protected void _writeXml(T property, XCalElement element) {
		XCalElement recurElement = element.append(getDataType(property));

		Recurrence recur = property.getValue();
		if (recur == null) {
			return;
		}

		ListMultimap<String, Object> components = buildComponents(recur, true);
		for (Map.Entry<String, List<Object>> component : components) {
			String name = component.getKey().toLowerCase();
			for (Object value : component.getValue()) {
				recurElement.append(name, value.toString());
			}
		}
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		XCalElement value = element.child(defaultDataType);
		if (value == null) {
			throw missingXmlElements(defaultDataType);
		}

		Recurrence.Builder builder = new Recurrence.Builder((Frequency) null);

		parseFreq(value.first("freq"), builder, warnings);
		parseUntil(value.first("until"), builder, warnings);
		parseCount(value.first("count"), builder, warnings);
		parseInterval(value.first("interval"), builder, warnings);
		parseBySecond(value.all("bysecond"), builder, warnings);
		parseByMinute(value.all("byminute"), builder, warnings);
		parseByHour(value.all("byhour"), builder, warnings);
		parseByDay(value.all("byday"), builder, warnings);
		parseByMonthDay(value.all("bymonthday"), builder, warnings);
		parseByYearDay(value.all("byyearday"), builder, warnings);
		parseByWeekNo(value.all("byweekno"), builder, warnings);
		parseByMonth(value.all("bymonth"), builder, warnings);
		parseBySetPos(value.all("bysetpos"), builder, warnings);
		parseWkst(value.first("wkst"), builder, warnings);

		return newInstance(builder.build());
	}

	@Override
	protected JCalValue _writeJson(T property) {
		Recurrence recur = property.getValue();
		if (recur == null) {
			return JCalValue.object(new ListMultimap<String, Object>(0));
		}

		ListMultimap<String, Object> components = buildComponents(recur, true);

		//lower-case all the keys
		ListMultimap<String, Object> object = new ListMultimap<String, Object>();
		for (Map.Entry<String, List<Object>> entry : components) {
			String key = entry.getKey().toLowerCase();
			object.putAll(key, entry.getValue());
		}

		return JCalValue.object(object);
	}

	@Override
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, List<String> warnings) {
		Recurrence.Builder builder = new Recurrence.Builder((Frequency) null);

		ListMultimap<String, String> object = value.getObject();
		parseFreq(object.first("freq"), builder, warnings);
		parseUntil(object.first("until"), builder, warnings);
		parseCount(object.first("count"), builder, warnings);
		parseInterval(object.first("interval"), builder, warnings);
		parseBySecond(object.get("bysecond"), builder, warnings);
		parseByMinute(object.get("byminute"), builder, warnings);
		parseByHour(object.get("byhour"), builder, warnings);
		parseByDay(object.get("byday"), builder, warnings);
		parseByMonthDay(object.get("bymonthday"), builder, warnings);
		parseByYearDay(object.get("byyearday"), builder, warnings);
		parseByWeekNo(object.get("byweekno"), builder, warnings);
		parseByMonth(object.get("bymonth"), builder, warnings);
		parseBySetPos(object.get("bysetpos"), builder, warnings);
		parseWkst(object.first("wkst"), builder, warnings);

		return newInstance(builder.build());
	}

	/**
	 * Creates a new instance of the recurrence property.
	 * @param recur the recurrence value
	 * @return the new instance
	 */
	protected abstract T newInstance(Recurrence recur);

	private void parseFreq(String value, Recurrence.Builder builder, List<String> warnings) {
		if (value == null) {
			return;
		}

		try {
			builder.frequency(Frequency.valueOf(value.toUpperCase()));
		} catch (IllegalArgumentException e) {
			warnings.add("Unable to parse FREQ value: " + value);
		}
	}

	private void parseUntil(String value, Recurrence.Builder builder, List<String> warnings) {
		if (value == null) {
			return;
		}

		try {
			Date date = date(value).parse();
			boolean hasTime = ICalDateFormatter.dateHasTime(value);
			builder.until(date, hasTime);
		} catch (IllegalArgumentException e) {
			warnings.add("Unable to parse UNTIL value: " + value);
		}
	}

	private void parseCount(String value, Recurrence.Builder builder, List<String> warnings) {
		if (value == null) {
			return;
		}

		try {
			builder.count(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			warnings.add("Unable to parse COUNT value: " + value);
		}
	}

	private void parseInterval(String value, Recurrence.Builder builder, List<String> warnings) {
		if (value == null) {
			return;
		}

		try {
			builder.interval(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			warnings.add("Unable to parse INTERVAL value: " + value);
		}
	}

	private void parseBySecond(List<String> values, final Recurrence.Builder builder, List<String> warnings) {
		parseIntegerList("BYSECOND", values, warnings, new ListHandler() {
			public void handle(Integer value) {
				builder.bySecond(value);
			}
		});
	}

	private void parseByMinute(List<String> values, final Recurrence.Builder builder, List<String> warnings) {
		parseIntegerList("BYMINUTE", values, warnings, new ListHandler() {
			public void handle(Integer value) {
				builder.byMinute(value);
			}
		});
	}

	private void parseByHour(List<String> values, final Recurrence.Builder builder, List<String> warnings) {
		parseIntegerList("BYHOUR", values, warnings, new ListHandler() {
			public void handle(Integer value) {
				builder.byHour(value);
			}
		});
	}

	private void parseByDay(List<String> values, Recurrence.Builder builder, List<String> warnings) {
		Pattern p = Pattern.compile("^([-+]?\\d+)?(.*)$");
		for (String value : values) {
			Matcher m = p.matcher(value);
			if (!m.find()) {
				//this should never happen
				//the regex contains a "match-all" pattern and should never not find anything
				warnings.add("Unable to parse BYDAY value (invalid format): " + value);
				continue;
			}

			String dayStr = m.group(2);
			DayOfWeek day = DayOfWeek.valueOfAbbr(dayStr);
			if (day == null) {
				warnings.add("Unable to parse BYDAY value (invalid day of the week): " + value);
				continue;
			}

			String prefixStr = m.group(1);
			Integer prefix = (prefixStr == null) ? null : Integer.valueOf(prefixStr); //no need to catch NumberFormatException because the regex guarantees that it will be a number

			builder.byDay(prefix, day);
		}
	}

	private void parseByMonthDay(List<String> values, final Recurrence.Builder builder, List<String> warnings) {
		parseIntegerList("BYMONTHDAY", values, warnings, new ListHandler() {
			public void handle(Integer value) {
				builder.byMonthDay(value);
			}
		});
	}

	private void parseByYearDay(List<String> values, final Recurrence.Builder builder, List<String> warnings) {
		parseIntegerList("BYYEARDAY", values, warnings, new ListHandler() {
			public void handle(Integer value) {
				builder.byYearDay(value);
			}
		});
	}

	private void parseByWeekNo(List<String> values, final Recurrence.Builder builder, List<String> warnings) {
		parseIntegerList("BYWEEKNO", values, warnings, new ListHandler() {
			public void handle(Integer value) {
				builder.byWeekNo(value);
			}
		});
	}

	private void parseByMonth(List<String> values, final Recurrence.Builder builder, List<String> warnings) {
		parseIntegerList("BYMONTH", values, warnings, new ListHandler() {
			public void handle(Integer value) {
				builder.byMonth(value);
			}
		});
	}

	private void parseBySetPos(List<String> values, final Recurrence.Builder builder, List<String> warnings) {
		parseIntegerList("BYSETPOS", values, warnings, new ListHandler() {
			public void handle(Integer value) {
				builder.bySetPos(value);
			}
		});
	}

	private void parseWkst(String value, Recurrence.Builder builder, List<String> warnings) {
		if (value == null) {
			return;
		}

		DayOfWeek day = DayOfWeek.valueOfAbbr(value);
		if (day == null) {
			warnings.add("Unable to parse WKST (invalid day of the week): " + value);
			return;
		}

		builder.workweekStarts(day);
	}

	private ListMultimap<String, Object> buildComponents(Recurrence recur, boolean extended) {
		ListMultimap<String, Object> components = new ListMultimap<String, Object>();

		//FREQ must come first
		if (recur.getFrequency() != null) {
			components.put("FREQ", recur.getFrequency().name());
		}

		if (recur.getUntil() != null) {
			String s = date(recur.getUntil()).time(recur.hasTimeUntilDate()).extended(extended).write();
			components.put("UNTIL", s);
		}

		if (recur.getCount() != null) {
			components.put("COUNT", recur.getCount());
		}

		if (recur.getInterval() != null) {
			components.put("INTERVAL", recur.getInterval());
		}

		addIntegerListComponent(components, "BYSECOND", recur.getBySecond());
		addIntegerListComponent(components, "BYMINUTE", recur.getByMinute());
		addIntegerListComponent(components, "BYHOUR", recur.getByHour());

		Iterator<Integer> prefixIt = recur.getByDayPrefixes().iterator();
		Iterator<DayOfWeek> dayIt = recur.getByDay().iterator();
		while (prefixIt.hasNext() && dayIt.hasNext()) {
			Integer prefix = prefixIt.next();
			DayOfWeek day = dayIt.next();

			String value = day.getAbbr();
			if (prefix != null) {
				value = prefix + value;
			}
			components.put("BYDAY", value);
		}

		addIntegerListComponent(components, "BYMONTHDAY", recur.getByMonthDay());
		addIntegerListComponent(components, "BYYEARDAY", recur.getByYearDay());
		addIntegerListComponent(components, "BYWEEKNO", recur.getByWeekNo());
		addIntegerListComponent(components, "BYMONTH", recur.getByMonth());
		addIntegerListComponent(components, "BYSETPOS", recur.getBySetPos());

		if (recur.getWorkweekStarts() != null) {
			components.put("WKST", recur.getWorkweekStarts().getAbbr());
		}

		return components;
	}

	private void addIntegerListComponent(ListMultimap<String, Object> components, String name, List<Integer> values) {
		for (Integer value : values) {
			components.put(name, value);
		}
	}

	private void parseIntegerList(String name, List<String> values, List<String> warnings, ListHandler handler) {
		for (String value : values) {
			try {
				handler.handle(Integer.valueOf(value));
			} catch (NumberFormatException e) {
				warnings.add("Ignoring non-numeric value found in " + name + ": " + value);
			}
		}
	}

	private static interface ListHandler {
		void handle(Integer value);
	}
}
