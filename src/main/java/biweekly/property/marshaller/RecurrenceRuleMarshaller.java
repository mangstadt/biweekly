package biweekly.property.marshaller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.RecurrenceRule;
import biweekly.property.RecurrenceRule.DayOfWeek;
import biweekly.property.RecurrenceRule.Frequency;
import biweekly.util.ListMultimap;
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
 * Marshals {@link RecurrenceRule} properties.
 * @author Michael Angstadt
 */
public class RecurrenceRuleMarshaller extends ICalPropertyMarshaller<RecurrenceRule> {
	public RecurrenceRuleMarshaller() {
		super(RecurrenceRule.class, "RRULE");
	}

	@Override
	protected String _writeText(RecurrenceRule property) {
		ListMultimap<String, Object> components = buildComponents(property, false);

		return StringUtils.join(components.getMap(), ";", new JoinMapCallback<String, List<Object>>() {
			public void handle(StringBuilder sb, String key, List<Object> values) {
				sb.append(key).append('=').append(StringUtils.join(values, ","));
			}
		});
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

		parseFreq(components.first("FREQ"), property, warnings);
		parseUntil(components.first("UNTIL"), property, warnings);
		parseCount(components.first("COUNT"), property, warnings);
		parseInterval(components.first("INTERVAL"), property, warnings);
		parseBySecond(components.get("BYSECOND"), property, warnings);
		parseByMinute(components.get("BYMINUTE"), property, warnings);
		parseByHour(components.get("BYHOUR"), property, warnings);
		parseByDay(components.get("BYDAY"), property, warnings);
		parseByMonthDay(components.get("BYMONTHDAY"), property, warnings);
		parseByYearDay(components.get("BYYEARDAY"), property, warnings);
		parseByWeekNo(components.get("BYWEEKNO"), property, warnings);
		parseByMonth(components.get("BYMONTH"), property, warnings);
		parseBySetPos(components.get("BYSETPOS"), property, warnings);
		parseWkst(components.first("WKST"), property, warnings);

		return property;
	}

	@Override
	protected void _writeXml(RecurrenceRule property, XCalElement element) {
		ListMultimap<String, Object> components = buildComponents(property, true);

		XCalElement recur = element.append(Value.RECUR);
		for (Map.Entry<String, List<Object>> component : components) {
			String name = component.getKey().toLowerCase();
			for (Object value : component.getValue()) {
				recur.append(name, value.toString());
			}
		}
	}

	@Override
	protected RecurrenceRule _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		RecurrenceRule property = new RecurrenceRule(null);

		XCalElement recur = element.child(Value.RECUR);
		if (recur == null) {
			return property;
		}

		parseFreq(recur.first("freq"), property, warnings);
		parseUntil(recur.first("until"), property, warnings);
		parseCount(recur.first("count"), property, warnings);
		parseInterval(recur.first("interval"), property, warnings);
		parseBySecond(recur.all("bysecond"), property, warnings);
		parseByMinute(recur.all("byminute"), property, warnings);
		parseByHour(recur.all("byhour"), property, warnings);
		parseByDay(recur.all("byday"), property, warnings);
		parseByMonthDay(recur.all("bymonthday"), property, warnings);
		parseByYearDay(recur.all("byyearday"), property, warnings);
		parseByWeekNo(recur.all("byweekno"), property, warnings);
		parseByMonth(recur.all("bymonth"), property, warnings);
		parseBySetPos(recur.all("bysetpos"), property, warnings);
		parseWkst(recur.first("wkst"), property, warnings);

		return property;
	}

	@Override
	protected JCalValue _writeJson(RecurrenceRule property) {
		ListMultimap<String, Object> components = buildComponents(property, true);

		//lower-case all the keys
		ListMultimap<String, Object> object = new ListMultimap<String, Object>();
		for (Map.Entry<String, List<Object>> entry : components) {
			String key = entry.getKey().toLowerCase();
			object.putAll(key, entry.getValue());
		}

		return JCalValue.object(Value.RECUR, object);
	}

	@Override
	protected RecurrenceRule _parseJson(JCalValue value, ICalParameters parameters, List<String> warnings) {
		RecurrenceRule property = new RecurrenceRule(null);

		ListMultimap<String, String> object = value.getObject();
		if (object == null) {
			return property;
		}

		parseFreq(object.first("freq"), property, warnings);
		parseUntil(object.first("until"), property, warnings);
		parseCount(object.first("count"), property, warnings);
		parseInterval(object.first("interval"), property, warnings);
		parseBySecond(object.get("bysecond"), property, warnings);
		parseByMinute(object.get("byminute"), property, warnings);
		parseByHour(object.get("byhour"), property, warnings);
		parseByDay(object.get("byday"), property, warnings);
		parseByMonthDay(object.get("bymonthday"), property, warnings);
		parseByYearDay(object.get("byyearday"), property, warnings);
		parseByWeekNo(object.get("byweekno"), property, warnings);
		parseByMonth(object.get("bymonth"), property, warnings);
		parseBySetPos(object.get("bysetpos"), property, warnings);
		parseWkst(object.first("wkst"), property, warnings);

		return property;
	}

	private void parseFreq(String value, RecurrenceRule property, List<String> warnings) {
		if (value != null) {
			try {
				property.setFrequency(Frequency.valueOf(value.toUpperCase()));
			} catch (IllegalArgumentException e) {
				warnings.add("Invalid frequency value: " + value);
			}
		}
	}

	private void parseUntil(String value, RecurrenceRule property, List<String> warnings) {
		if (value != null) {
			try {
				Date date = date(value).parse();
				boolean hasTime = value.contains("T");
				property.setUntil(date, hasTime);
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse UNTIL date: " + value);
			}
		}
	}

	private void parseCount(String value, RecurrenceRule property, List<String> warnings) {
		if (value != null) {
			try {
				property.setCount(Integer.valueOf(value));
			} catch (NumberFormatException e) {
				warnings.add("Could not parse COUNT integer: " + value);
			}
		}
	}

	private void parseInterval(String value, RecurrenceRule property, List<String> warnings) {
		if (value != null) {
			try {
				property.setInterval(Integer.valueOf(value));
			} catch (NumberFormatException e) {
				warnings.add("Could not parse INTERVAL integer: " + value);
			}
		}
	}

	private void parseBySecond(List<String> values, RecurrenceRule property, List<String> warnings) {
		if (!values.isEmpty()) {
			property.setBySecond(toIntegerList("BYSECOND", values, warnings));
		}
	}

	private void parseByMinute(List<String> values, RecurrenceRule property, List<String> warnings) {
		if (!values.isEmpty()) {
			property.setByMinute(toIntegerList("BYMINUTE", values, warnings));
		}
	}

	private void parseByHour(List<String> values, RecurrenceRule property, List<String> warnings) {
		if (!values.isEmpty()) {
			property.setByHour(toIntegerList("BYHOUR", values, warnings));
		}

	}

	private void parseByDay(List<String> values, RecurrenceRule property, List<String> warnings) {
		if (!values.isEmpty()) {
			Pattern p = Pattern.compile("^([-+]?\\d+)?(.*)$");
			for (String v : values) {
				Matcher m = p.matcher(v);
				if (m.find()) {
					String prefixStr = m.group(1);
					String dayStr = m.group(2);

					DayOfWeek day = DayOfWeek.valueOfAbbr(dayStr);
					if (day == null) {
						warnings.add("Ignoring invalid day string: " + dayStr);
						continue;
					}

					Integer prefix = (prefixStr == null) ? null : Integer.valueOf(prefixStr);
					property.addByDay(prefix, day);
				} else {
					//should never reach here due to nature of regular expression
					warnings.add("Problem parsing BYDAY value: " + v);
				}
			}
		}
	}

	private void parseByMonthDay(List<String> values, RecurrenceRule property, List<String> warnings) {
		if (!values.isEmpty()) {
			property.setByMonthDay(toIntegerList("BYMONTHDAY", values, warnings));
		}
	}

	private void parseByYearDay(List<String> values, RecurrenceRule property, List<String> warnings) {
		if (!values.isEmpty()) {
			property.setByYearDay(toIntegerList("BYYEARDAY", values, warnings));
		}
	}

	private void parseByWeekNo(List<String> values, RecurrenceRule property, List<String> warnings) {
		if (!values.isEmpty()) {
			property.setByWeekNo(toIntegerList("BYWEEKNO", values, warnings));
		}
	}

	private void parseByMonth(List<String> values, RecurrenceRule property, List<String> warnings) {
		if (!values.isEmpty()) {
			property.setByMonth(toIntegerList("BYMONTH", values, warnings));
		}
	}

	private void parseBySetPos(List<String> values, RecurrenceRule property, List<String> warnings) {
		if (!values.isEmpty()) {
			property.setBySetPos(toIntegerList("BYSETPOS", values, warnings));
		}
	}

	private void parseWkst(String value, RecurrenceRule property, List<String> warnings) {
		if (value != null) {
			DayOfWeek day = DayOfWeek.valueOfAbbr(value);
			if (day == null) {
				warnings.add("Invalid day string: " + value);
			} else {
				property.setWorkweekStarts(day);
			}
		}
	}

	private ListMultimap<String, Object> buildComponents(RecurrenceRule property, boolean extended) {
		ListMultimap<String, Object> components = new ListMultimap<String, Object>();

		if (property.getFrequency() != null) {
			components.put("FREQ", property.getFrequency().name());
		}

		if (property.getUntil() != null) {
			String s = date(property.getUntil()).time(property.hasTimeUntilDate()).extended(extended).write();
			components.put("UNTIL", s);
		}

		if (property.getCount() != null) {
			components.put("COUNT", property.getCount());
		}

		if (property.getInterval() != null) {
			components.put("INTERVAL", property.getInterval());
		}

		addIntegerListComponent(components, "BYSECOND", property.getBySecond());
		addIntegerListComponent(components, "BYMINUTE", property.getByMinute());
		addIntegerListComponent(components, "BYHOUR", property.getByHour());

		for (int i = 0; i < property.getByDay().size(); i++) {
			DayOfWeek day = property.getByDay().get(i);
			Integer prefix = property.getByDayPrefixes().get(i);

			String value = day.getAbbr();
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
			components.put("WKST", property.getWorkweekStarts().getAbbr());
		}

		return components;
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

	private void addIntegerListComponent(ListMultimap<String, Object> components, String name, List<Integer> values) {
		if (values == null) {
			return;
		}
		for (Integer value : values) {
			components.put(name, value);
		}
	}
}
