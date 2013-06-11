package biweekly.property.marshaller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biweekly.parameter.ICalParameters;
import biweekly.property.RecurrenceRule;
import biweekly.property.RecurrenceRule.DayOfWeek;
import biweekly.property.RecurrenceRule.Frequency;
import biweekly.util.ICalDateFormatter;
import biweekly.util.ISOFormat;
import biweekly.util.ListMultimap;

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
 * Marshals {@link RecurrenceRule} properties.
 * @author Michael Angstadt
 */
public class RecurrenceRuleMarshaller extends ICalPropertyMarshaller<RecurrenceRule> {
	private static final Map<DayOfWeek, String> dayOfWeekStrings;
	static {
		Map<DayOfWeek, String> m = new HashMap<DayOfWeek, String>();
		m.put(DayOfWeek.MONDAY, "MO");
		m.put(DayOfWeek.TUESDAY, "TU");
		m.put(DayOfWeek.WEDNESDAY, "WE");
		m.put(DayOfWeek.THURSDAY, "TH");
		m.put(DayOfWeek.FRIDAY, "FR");
		m.put(DayOfWeek.SATURDAY, "SA");
		m.put(DayOfWeek.SUNDAY, "SU");
		dayOfWeekStrings = Collections.unmodifiableMap(m);
	}

	private static final Map<String, DayOfWeek> dayOfWeekEnums;
	static {
		Map<String, DayOfWeek> m = new HashMap<String, DayOfWeek>();
		for (Map.Entry<DayOfWeek, String> entry : dayOfWeekStrings.entrySet()) {
			m.put(entry.getValue(), entry.getKey());
		}
		dayOfWeekEnums = Collections.unmodifiableMap(m);
	}

	public RecurrenceRuleMarshaller() {
		super(RecurrenceRule.class, "RRULE");
	}

	@Override
	protected String _writeText(RecurrenceRule property) {
		ListMultimap<String, String> components = new ListMultimap<String, String>();

		if (property.getFrequency() != null) {
			components.put("FREQ", property.getFrequency().name());
		}

		if (property.getUntil() != null) {
			ISOFormat format = property.hasTimeUntilDate() ? ISOFormat.UTC_TIME_BASIC : ISOFormat.DATE_BASIC;
			String s = ICalDateFormatter.format(property.getUntil(), format);
			components.put("UNTIL", s);
		}

		if (property.getCount() != null) {
			components.put("COUNT", property.getCount().toString());
		}

		if (property.getInterval() != null) {
			components.put("INTERVAL", property.getInterval().toString());
		}

		addIntegerListComponent(components, "BYSECOND", property.getBySecond());
		addIntegerListComponent(components, "BYMINUTE", property.getByMinute());
		addIntegerListComponent(components, "BYHOUR", property.getByHour());

		for (int i = 0; i < property.getByDay().size(); i++) {
			DayOfWeek day = property.getByDay().get(i);
			Integer prefix = property.getByDayPrefixes().get(i);

			String value = dayOfWeekStrings.get(day);
			if (prefix != null) {
				value = prefix + value;
			}
			components.put("BYDAY", value);
		}

		addIntegerListComponent(components, "BYMONTHDAY", property.getByMonthDay());
		addIntegerListComponent(components, "BYYEARDAY", property.getByYearDay());
		addIntegerListComponent(components, "BYWEEKNO", property.getByWeekNo());
		addIntegerListComponent(components, "BYMONTH", property.getByMonth());
		addIntegerListComponent(components, "BYSETPOS", property.getBySetPos());

		if (property.getWorkweekStarts() != null) {
			components.put("WKST", dayOfWeekStrings.get(property.getWorkweekStarts()));
		}

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, List<String>> entry : components) {
			String key = entry.getKey();
			List<String> values = entry.getValue();

			if (first) {
				first = false;
			} else {
				sb.append(';');
			}
			sb.append(key).append('=');

			boolean firstInner = true;
			for (String value : values) {
				if (firstInner) {
					firstInner = false;
				} else {
					sb.append(',');
				}
				sb.append(value);
			}
		}
		return sb.toString();
	}

	@Override
	protected RecurrenceRule _parseText(String value, ICalParameters parameters, List<String> warnings) {
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

		RecurrenceRule property = new RecurrenceRule(null);
		String first;
		List<String> values;

		first = components.first("FREQ");
		if (first != null) {
			try {
				property.setFrequency(Frequency.valueOf(first.toUpperCase()));
			} catch (IllegalArgumentException e) {
				warnings.add("Invalid frequency value: " + first);
			}
		}

		first = components.first("UNTIL");
		if (first != null) {
			try {
				boolean hasTime = first.contains("T");
				property.setUntil(ICalDateFormatter.parse(first), hasTime);
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse UNTIL date: " + first);
			}
		}

		first = components.first("COUNT");
		if (first != null) {
			try {
				property.setCount(Integer.valueOf(first));
			} catch (NumberFormatException e) {
				warnings.add("Could not parse COUNT integer: " + first);
			}
		}

		first = components.first("INTERVAL");
		if (first != null) {
			try {
				property.setInterval(Integer.valueOf(first));
			} catch (NumberFormatException e) {
				warnings.add("Could not parse INTERVAL integer: " + first);
			}
		}

		values = components.get("BYSECOND");
		if (!values.isEmpty()) {
			property.setBySecond(toIntegerList("BYSECOND", values, warnings));
		}

		values = components.get("BYMINUTE");
		if (!values.isEmpty()) {
			property.setByMinute(toIntegerList("BYMINUTE", values, warnings));
		}

		values = components.get("BYHOUR");
		if (!values.isEmpty()) {
			property.setByHour(toIntegerList("BYHOUR", values, warnings));
		}

		values = components.get("BYDAY");
		if (!values.isEmpty()) {
			Pattern p = Pattern.compile("^([-+]?\\d+)?(.*)$");
			for (String v : values) {
				Matcher m = p.matcher(v);
				if (m.find()) {
					String prefixStr = m.group(1);
					String dayStr = m.group(2);

					Integer prefix = (prefixStr == null) ? null : Integer.valueOf(prefixStr);
					DayOfWeek day = dayOfWeekEnums.get(dayStr.toUpperCase());
					if (day == null) {
						warnings.add("Invalid day string: " + dayStr);
					}
					property.addByDay(prefix, day);
				} else {
					//should never reach here due to nature of regular expression
					warnings.add("Problem parsing BYDAY value: " + v);
				}
			}
		}

		values = components.get("BYMONTHDAY");
		if (!values.isEmpty()) {
			property.setByMonthDay(toIntegerList("BYMONTHDAY", values, warnings));
		}

		values = components.get("BYYEARDAY");
		if (!values.isEmpty()) {
			property.setByYearDay(toIntegerList("BYYEARDAY", values, warnings));
		}

		values = components.get("BYWEEKNO");
		if (!values.isEmpty()) {
			property.setByWeekNo(toIntegerList("BYWEEKNO", values, warnings));
		}

		values = components.get("BYMONTH");
		if (!values.isEmpty()) {
			property.setByMonth(toIntegerList("BYMONTH", values, warnings));
		}

		values = components.get("BYSETPOS");
		if (!values.isEmpty()) {
			property.setBySetPos(toIntegerList("BYSETPOS", values, warnings));
		}

		first = components.first("WKST");
		if (first != null) {
			DayOfWeek day = dayOfWeekEnums.get(first.toUpperCase());
			if (day == null) {
				warnings.add("Invalid day string: " + first);
			} else {
				property.setWorkweekStarts(day);
			}
		}

		return property;
	}

	private List<Integer> toIntegerList(String name, List<String> values, List<String> warnings) {
		List<Integer> list = new ArrayList<Integer>(values.size());

		for (String value : values) {
			try {
				list.add(Integer.valueOf(value));
			} catch (NumberFormatException e) {
				warnings.add("Ignoring non-numeric value found in " + name + ": " + value);
			}
		}

		return list;
	}

	private void addIntegerListComponent(ListMultimap<String, String> components, String name, List<Integer> values) {
		if (values == null) {
			return;
		}
		for (Integer value : values) {
			components.put(name, value.toString());
		}
	}
}
