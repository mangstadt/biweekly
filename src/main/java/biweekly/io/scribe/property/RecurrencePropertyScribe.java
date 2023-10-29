package biweekly.io.scribe.property;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Element;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.component.ICalComponent;
import biweekly.io.CannotParseException;
import biweekly.io.DataModelConversionException;
import biweekly.io.ParseContext;
import biweekly.io.ParseWarning;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.io.xml.XCalNamespaceContext;
import biweekly.parameter.ICalParameters;
import biweekly.property.DateStart;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.RecurrenceProperty;
import biweekly.util.ByDay;
import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.ICalDate;
import biweekly.util.ListMultimap;
import biweekly.util.Recurrence;
import biweekly.util.XmlUtils;

/*
 Copyright (c) 2013-2023, Michael Angstadt
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
		super(clazz, propertyName);
	}

	@Override
	protected ICalDataType _defaultDataType(ICalVersion version) {
		return ICalDataType.RECUR;
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		//null value
		Recurrence recur = property.getValue();
		if (recur == null) {
			return "";
		}

		switch (context.getVersion()) {
		case V1_0:
			return writeTextV1(property, context);
		default:
			return writeTextV2(property, context);
		}
	}

	private String writeTextV1(T property, WriteContext context) {
		return new RecurrenceWriterV1(property, context).write();
	}

	private String writeTextV2(T property, WriteContext context) {
		ListMultimap<String, Object> components = buildComponents(property, context, false);
		return VObjectPropertyValues.writeMultimap(components.getMap());
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		if (value.isEmpty()) {
			return newInstance(new Recurrence.Builder((Frequency) null).build());
		}

		switch (context.getVersion()) {
		case V1_0:
			return parseTextV1(value, dataType, parameters, context);
		default:
			return parseTextV2(value, dataType, parameters, context);
		}
	}

	/**
	 * @param value the property value
	 * @param dataType the property data type
	 * @param parameters the property parameters
	 * @param context the parse context
	 * @return the parsed property
	 * @throws DataModelConversionException if the property contains multiple
	 * RRULE values
	 */
	private T parseTextV1(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		RecurrenceParserV1 parser = new RecurrenceParserV1(context);

		List<String> rrules = RecurrenceParserV1.splitPropertyValue(value);
		if (rrules.size() == 1) {
			Recurrence recur = parser.parse(value);
			return newInstance(recur, context, parameters);
		}

		/*
		 * Version 1.0 allows multiple RRULE values to be defined inside of the
		 * same property. If there are multiple values, parse them and throw a
		 * DataModelConversionException.
		 */
		DataModelConversionException conversionException = new DataModelConversionException(null);
		for (String rrule : rrules) {
			ICalParameters parametersCopy = new ICalParameters(parameters);

			ICalProperty property;
			try {
				Recurrence recur = parser.parse(rrule);
				property = newInstance(recur, context, parameters);
			} catch (CannotParseException e) {
				//@formatter:off
				context.getWarnings().add(new ParseWarning.Builder(context)
					.message(e)
					.build()
				);
				//@formatter:on
				property = new RawProperty(getPropertyName(context.getVersion()), dataType, rrule);
				property.setParameters(parametersCopy);
			}
			conversionException.getProperties().add(property);
		}

		throw conversionException;
	}

	private T parseTextV2(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		Recurrence.Builder builder = new Recurrence.Builder((Frequency) null);
		ListMultimap<String, String> rules = new ListMultimap<String, String>(VObjectPropertyValues.parseMultimap(value));

		parseFreq(rules, builder, context);
		parseUntil(rules, builder, context);
		parseCount(rules, builder, context);
		parseInterval(rules, builder, context);
		parseBySecond(rules, builder, context);
		parseByMinute(rules, builder, context);
		parseByHour(rules, builder, context);
		parseByDay(rules, builder, context);
		parseByMonthDay(rules, builder, context);
		parseByYearDay(rules, builder, context);
		parseByWeekNo(rules, builder, context);
		parseByMonth(rules, builder, context);
		parseBySetPos(rules, builder, context);
		parseWkst(rules, builder, context);
		parseXRules(rules, builder); //must be called last

		return newInstance(builder.build(), context, parameters);
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
		ICalDataType dataType = defaultDataType(context.getVersion());
		XCalElement value = element.child(dataType);
		if (value == null) {
			throw missingXmlElements(dataType);
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

		parseFreq(rules, builder, context);
		parseUntil(rules, builder, context);
		parseCount(rules, builder, context);
		parseInterval(rules, builder, context);
		parseBySecond(rules, builder, context);
		parseByMinute(rules, builder, context);
		parseByHour(rules, builder, context);
		parseByDay(rules, builder, context);
		parseByMonthDay(rules, builder, context);
		parseByYearDay(rules, builder, context);
		parseByWeekNo(rules, builder, context);
		parseByMonth(rules, builder, context);
		parseBySetPos(rules, builder, context);
		parseWkst(rules, builder, context);
		parseXRules(rules, builder); //must be called last

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

		parseFreq(rules, builder, context);
		parseUntil(rules, builder, context);
		parseCount(rules, builder, context);
		parseInterval(rules, builder, context);
		parseBySecond(rules, builder, context);
		parseByMinute(rules, builder, context);
		parseByHour(rules, builder, context);
		parseByDay(rules, builder, context);
		parseByMonthDay(rules, builder, context);
		parseByYearDay(rules, builder, context);
		parseByWeekNo(rules, builder, context);
		parseByMonth(rules, builder, context);
		parseBySetPos(rules, builder, context);
		parseWkst(rules, builder, context);
		parseXRules(rules, builder); //must be called last

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

	private T newInstance(Recurrence recur, ParseContext context, ICalParameters parameters) {
		T property = newInstance(recur);

		if (recur.getUntil() != null) {
			context.addDate(recur.getUntil(), property, parameters);
		}

		return property;
	}

	private void parseFreq(ListMultimap<String, String> rules, final Recurrence.Builder builder, final ParseContext context) {
		parseFirst(rules, FREQ, new Handler<String>() {
			public void handle(String value) {
				value = value.toUpperCase();
				try {
					builder.frequency(Frequency.valueOf(value));
				} catch (IllegalArgumentException e) {
					context.addWarning(7, FREQ, value);
				}
			}
		});
	}

	private void parseUntil(ListMultimap<String, String> rules, final Recurrence.Builder builder, final ParseContext context) {
		parseFirst(rules, UNTIL, new Handler<String>() {
			public void handle(String value) {
				try {
					builder.until(date(value).parse());
				} catch (IllegalArgumentException e) {
					context.addWarning(7, UNTIL, value);
				}
			}
		});
	}

	private void parseCount(ListMultimap<String, String> rules, final Recurrence.Builder builder, final ParseContext context) {
		parseFirst(rules, COUNT, new Handler<String>() {
			public void handle(String value) {
				try {
					builder.count(Integer.valueOf(value));
				} catch (NumberFormatException e) {
					context.addWarning(7, COUNT, value);
				}
			}
		});
	}

	private void parseInterval(ListMultimap<String, String> rules, final Recurrence.Builder builder, final ParseContext context) {
		parseFirst(rules, INTERVAL, new Handler<String>() {
			public void handle(String value) {
				try {
					builder.interval(Integer.valueOf(value));
				} catch (NumberFormatException e) {
					context.addWarning(7, INTERVAL, value);
				}
			}
		});
	}

	private void parseBySecond(ListMultimap<String, String> rules, final Recurrence.Builder builder, ParseContext context) {
		parseIntegerList(BYSECOND, rules, context, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.bySecond(value);
			}
		});
	}

	private void parseByMinute(ListMultimap<String, String> rules, final Recurrence.Builder builder, ParseContext context) {
		parseIntegerList(BYMINUTE, rules, context, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byMinute(value);
			}
		});
	}

	private void parseByHour(ListMultimap<String, String> rules, final Recurrence.Builder builder, ParseContext context) {
		parseIntegerList(BYHOUR, rules, context, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byHour(value);
			}
		});
	}

	private void parseByDay(ListMultimap<String, String> rules, Recurrence.Builder builder, ParseContext context) {
		Pattern p = Pattern.compile("^([-+]?\\d+)?(.*)$");
		for (String value : rules.removeAll(BYDAY)) {
			Matcher m = p.matcher(value);
			if (!m.find()) {
				//this should never happen
				//the regex contains a "match-all" pattern and should never not find anything
				context.addWarning(7, BYDAY, value);
				continue;
			}

			String dayStr = m.group(2);
			DayOfWeek day = DayOfWeek.valueOfAbbr(dayStr);
			if (day == null) {
				context.addWarning(7, BYDAY, value);
				continue;
			}

			String prefixStr = m.group(1);
			Integer prefix = (prefixStr == null) ? null : Integer.valueOf(prefixStr); //no need to catch NumberFormatException because the regex guarantees that it will be a number

			builder.byDay(prefix, day);
		}
	}

	private void parseByMonthDay(ListMultimap<String, String> rules, final Recurrence.Builder builder, ParseContext context) {
		parseIntegerList(BYMONTHDAY, rules, context, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byMonthDay(value);
			}
		});
	}

	private void parseByYearDay(ListMultimap<String, String> rules, final Recurrence.Builder builder, ParseContext context) {
		parseIntegerList(BYYEARDAY, rules, context, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byYearDay(value);
			}
		});
	}

	private void parseByWeekNo(ListMultimap<String, String> rules, final Recurrence.Builder builder, ParseContext context) {
		parseIntegerList(BYWEEKNO, rules, context, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byWeekNo(value);
			}
		});
	}

	private void parseByMonth(ListMultimap<String, String> rules, final Recurrence.Builder builder, ParseContext context) {
		parseIntegerList(BYMONTH, rules, context, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.byMonth(value);
			}
		});
	}

	private void parseBySetPos(ListMultimap<String, String> rules, final Recurrence.Builder builder, ParseContext context) {
		parseIntegerList(BYSETPOS, rules, context, new Handler<Integer>() {
			public void handle(Integer value) {
				builder.bySetPos(value);
			}
		});
	}

	private void parseWkst(ListMultimap<String, String> rules, final Recurrence.Builder builder, final ParseContext context) {
		parseFirst(rules, WKST, new Handler<String>() {
			public void handle(String value) {
				DayOfWeek day = DayOfWeek.valueOfAbbr(value);
				if (day == null) {
					context.addWarning(7, WKST, value);
					return;
				}

				builder.workweekStarts(day);
			}
		});
	}

	private void parseXRules(ListMultimap<String, String> rules, Recurrence.Builder builder) {
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

		ICalDate until = recur.getUntil();
		if (until != null) {
			components.put(UNTIL, writeUntil(until, context, extended));
		}

		if (recur.getCount() != null) {
			components.put(COUNT, recur.getCount());
		}

		if (recur.getInterval() != null) {
			components.put(INTERVAL, recur.getInterval());
		}

		components.putAll(BYSECOND, recur.getBySecond());
		components.putAll(BYMINUTE, recur.getByMinute());
		components.putAll(BYHOUR, recur.getByHour());

		for (ByDay byDay : recur.getByDay()) {
			Integer prefix = byDay.getNum();
			DayOfWeek day = byDay.getDay();

			String value = day.getAbbr();
			if (prefix != null) {
				value = prefix + value;
			}
			components.put(BYDAY, value);
		}

		components.putAll(BYMONTHDAY, recur.getByMonthDay());
		components.putAll(BYYEARDAY, recur.getByYearDay());
		components.putAll(BYWEEKNO, recur.getByWeekNo());
		components.putAll(BYMONTH, recur.getByMonth());
		components.putAll(BYSETPOS, recur.getBySetPos());

		if (recur.getWorkweekStarts() != null) {
			components.put(WKST, recur.getWorkweekStarts().getAbbr());
		}

		for (Map.Entry<String, List<String>> entry : recur.getXRules().entrySet()) {
			String name = entry.getKey();
			List<String> values = entry.getValue();
			components.putAll(name, values);
		}

		return components;
	}

	private String writeUntil(ICalDate until, WriteContext context, boolean extended) {
		if (!until.hasTime()) {
			return date(until).extended(extended).write();
		}

		/*
		 * RFC 5545 p.41
		 * 
		 * In the case of the "STANDARD" and "DAYLIGHT" sub-components the UNTIL
		 * rule part MUST always be specified as a date with UTC time. If
		 * specified as a DATE-TIME value, then it MUST be specified in a UTC
		 * time format.
		 */

		if (isInObservance(context)) {
			return date(until).utc(true).extended(extended).write();
		}

		/*
		 * RFC 2445 p.42
		 * 
		 * If specified as a date-time value, then it MUST be specified in an
		 * UTC time format.
		 */
		if (context.getVersion() == ICalVersion.V2_0_DEPRECATED) {
			return date(until).extended(extended).utc(true).write();
		}

		/*
		 * RFC 5545 p.41
		 * 
		 * Furthermore, if the "DTSTART" property is specified as a date with
		 * local time, then the UNTIL rule part MUST also be specified as a date
		 * with local time. If the "DTSTART" property is specified as a date
		 * with UTC time or a date with local time and time zone reference, then
		 * the UNTIL rule part MUST be specified as a date with UTC time.
		 */

		ICalComponent parent = context.getParent();
		if (parent == null) {
			return date(until).extended(extended).utc(true).write();
		}

		DateStart dtstart = parent.getProperty(DateStart.class);
		if (dtstart == null) {
			return date(until).extended(extended).utc(true).write();
		}

		/*
		 * If DTSTART is floating, then UNTIL should be floating.
		 */
		TimezoneInfo tzinfo = context.getTimezoneInfo();
		boolean dtstartFloating = tzinfo.isFloating(dtstart);
		if (dtstartFloating) {
			return date(until).extended(extended).tz(true, null).write();
		}

		/*
		 * Otherwise, UNTIL should be UTC.
		 */
		return date(until).extended(extended).utc(true).write();
	}

	private void parseFirst(ListMultimap<String, String> rules, String name, Handler<String> handler) {
		List<String> values = rules.removeAll(name);
		if (values.isEmpty()) {
			return;
		}

		String value = values.get(0);
		handler.handle(value);
	}

	private void parseIntegerList(String name, ListMultimap<String, String> rules, ParseContext context, Handler<Integer> handler) {
		List<String> values = rules.removeAll(name);
		for (String value : values) {
			try {
				handler.handle(Integer.valueOf(value));
			} catch (NumberFormatException e) {
				context.addWarning(8, name, value);
			}
		}
	}

	private interface Handler<T> {
		void handle(T value);
	}
}
