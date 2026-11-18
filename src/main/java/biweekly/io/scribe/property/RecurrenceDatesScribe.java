package biweekly.io.scribe.property;

import static biweekly.ICalDataType.DATE;
import static biweekly.ICalDataType.DATE_TIME;
import static biweekly.ICalDataType.PERIOD;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.RecurrenceDates;
import biweekly.util.Duration;
import biweekly.util.ICalDate;
import biweekly.util.Period;

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
 * Marshals {@link RecurrenceDates} properties.
 * @author Michael Angstadt
 */
public class RecurrenceDatesScribe extends ICalPropertyScribe<RecurrenceDates> {
	public RecurrenceDatesScribe() {
		super(RecurrenceDates.class, "RDATE", DATE_TIME);
	}

	@Override
	protected ICalParameters _prepareParameters(RecurrenceDates property, WriteContext context) {
		if (isInObservance(context)) {
			return property.getParameters();
		}

		List<Period> periods = property.getPeriods();
		List<ICalDate> dates = property.getDates();
		boolean hasTime;
		if (periods.isEmpty() && dates.isEmpty()) {
			hasTime = false;
		} else {
			ICalDataType dataType = dataType(property, context.getVersion());
			hasTime = (dataType == DATE_TIME || dataType == PERIOD);
		}
		return handleTzidParameter(property, hasTime, context);
	}

	@Override
	protected ICalDataType _dataType(RecurrenceDates property, ICalVersion version) {
		List<ICalDate> dates = property.getDates();
		if (!dates.isEmpty()) {
			return dates.get(0).hasTime() ? DATE_TIME : DATE;
		}

		if (!property.getPeriods().isEmpty()) {
			return PERIOD;
		}

		return defaultDataType(version);
	}

	@Override
	protected String _writeText(RecurrenceDates property, WriteContext context) {
		return VObjectPropertyValues.writeList(writeValues(property, context, false));
	}

	@Override
	protected RecurrenceDates _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return parse(VObjectPropertyValues.parseList(value), dataType, parameters, context);
	}

	@Override
	protected void _writeXml(RecurrenceDates property, XCalElement element, WriteContext context) {
		ICalDataType dataType = dataType(property, context.getVersion());
		List<ICalDate> dates = property.getDates();
		if (!dates.isEmpty()) {
			//@formatter:off
			dates.stream()
				.map(date -> writeDateListDate(date, property, context, true))
			.forEach(dateStr -> element.append(dataType, dateStr));
			//@formatter:on
			return;
		}

		List<Period> periods = property.getPeriods();
		if (!periods.isEmpty()) {
			for (Period period : periods) {
				XCalElement periodElement = element.append(dataType);

				Date start = period.getStartDate();
				if (start != null) {
					String dateStr = date(start, property, context).extended(true).write();
					periodElement.append("start", dateStr);
				}

				Date end = period.getEndDate();
				if (end != null) {
					String dateStr = date(end, property, context).extended(true).write();
					periodElement.append("end", dateStr);
				}

				Duration duration = period.getDuration();
				if (duration != null) {
					periodElement.append("duration", duration.toString());
				}
			}
			return;
		}

		element.append(defaultDataType(context.getVersion()), "");
	}

	@Override
	protected RecurrenceDates _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		List<XCalElement> periodElements = element.children(PERIOD);
		List<String> dateTimeElements = element.all(DATE_TIME);
		List<String> dateElements = element.all(DATE);
		if (periodElements.isEmpty() && dateTimeElements.isEmpty() && dateElements.isEmpty()) {
			throw missingXmlElements(PERIOD, DATE_TIME, DATE);
		}

		RecurrenceDates property = new RecurrenceDates();

		//parse periods
		periodElements.stream().map(this::parsePeriod).forEach(period -> {
			property.getPeriods().add(period);

			context.addDate(period.getStartDate(), property, parameters);
			if (period.getEndDate() != null) {
				context.addDate(period.getEndDate(), property, parameters);
			}
		});

		//parse date-times
		dateTimeElements.stream().map(this::parseDateTime).forEach(date -> {
			property.getDates().add(date);
			context.addDate(date, property, parameters);
		});

		//parse dates
		dateElements.stream().map(this::parseDate).forEach(date -> property.getDates().add(date));

		return property;
	}

	@Override
	protected JCalValue _writeJson(RecurrenceDates property, WriteContext context) {
		return JCalValue.multi(writeValues(property, context, true));
	}

	@Override
	protected RecurrenceDates _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return parse(value.asMulti(), dataType, parameters, context);
	}

	private RecurrenceDates parse(List<String> valueStrs, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		RecurrenceDates property = new RecurrenceDates();

		if (dataType == PERIOD) {
			//parse as periods
			valueStrs.stream().map(this::parsePeriod).forEach(period -> {
				property.getPeriods().add(period);

				context.addDate(period.getStartDate(), property, parameters);
				if (period.getEndDate() != null) {
					context.addDate(period.getEndDate(), property, parameters);
				}
			});
			return property;
		}

		//parse as dates
		boolean hasTime = (dataType == DATE_TIME);
		valueStrs.stream().map(value -> parseDateTime(value, hasTime)).forEach(date -> {
			property.getDates().add(date);
			context.addDate(date, property, parameters);
		});

		return property;
	}

	private Period parsePeriod(String str) {
		int slash = str.indexOf('/');
		if (slash < 0) {
			throw new CannotParseException(13);
		}

		String beforeSlash = str.substring(0, slash);
		ICalDate start;
		try {
			start = date(beforeSlash).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(10, beforeSlash);
		}

		String afterSlash = str.substring(slash + 1);
		try {
			ICalDate end = date(afterSlash).parse();
			return new Period(start, end);
		} catch (IllegalArgumentException ignore) {
			//attempt to parse as a duration instead
		}

		try {
			Duration duration = Duration.parse(afterSlash);
			return new Period(start, duration);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(14, afterSlash);
		}
	}

	private Period parsePeriod(XCalElement element) {
		String startStr = element.first("start");
		if (startStr == null) {
			throw new CannotParseException(9);
		}

		ICalDate start;
		try {
			start = date(startStr).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(10, startStr);
		}

		String endStr = element.first("end");
		if (endStr != null) {
			try {
				ICalDate end = date(endStr).parse();
				return new Period(start, end);
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(11, endStr);
			}
		}

		String durationStr = element.first("duration");
		if (durationStr != null) {
			try {
				Duration duration = Duration.parse(durationStr);
				return new Period(start, duration);
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(12, durationStr);
			}
		}

		throw new CannotParseException(13);
	}

	private ICalDate parseDateTime(String value) {
		return parseDateTime(value, true);
	}

	private ICalDate parseDate(String value) {
		return parseDateTime(value, false);
	}

	private ICalDate parseDateTime(String value, boolean hasTime) {
		try {
			return date(value).hasTime(hasTime).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(15, value);
		}
	}

	private List<String> writeValues(RecurrenceDates property, WriteContext context, boolean extended) {
		List<ICalDate> dates = property.getDates();
		List<Period> periods = property.getPeriods();
		Stream<String> stream;

		if (!dates.isEmpty()) {
			stream = dates.stream().map(date -> writeDateListDate(date, property, context, extended));
		} else if (!periods.isEmpty()) {
			//TODO vCal does not support periods
			stream = periods.stream().map(period -> writePeriod(period, property, context, extended));
		} else {
			stream = Stream.of(""); //write empty value
		}

		return stream.collect(Collectors.toList());
	}

	private String writeDateListDate(ICalDate date, RecurrenceDates property, WriteContext context, boolean extended) {
		//@formatter:off
		DateWriter dw = isInObservance(context) ?
			date(date).observance(true) :
			date(date, property, context);
		//@formatter:on

		return dw.extended(extended).write();
	}

	private String writePeriod(Period period, RecurrenceDates property, WriteContext context, boolean extended) {
		Date start = period.getStartDate();
		String beforeSlash = (start == null) ? "" : date(start, property, context).extended(extended).write();

		String afterSlash;
		Date end = period.getEndDate();
		Duration duration = period.getDuration();
		if (end != null) {
			afterSlash = date(end, property, context).extended(extended).write();
		} else if (duration != null) {
			afterSlash = duration.toString();
		} else {
			afterSlash = "";
		}

		return beforeSlash + "/" + afterSlash;
	}
}
