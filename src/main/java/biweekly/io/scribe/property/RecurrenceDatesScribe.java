package biweekly.io.scribe.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.RecurrenceDates;
import biweekly.util.Duration;
import biweekly.util.ICalDateFormat;
import biweekly.util.Period;

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
 * Marshals {@link RecurrenceDates} properties.
 * @author Michael Angstadt
 */
public class RecurrenceDatesScribe extends ICalPropertyScribe<RecurrenceDates> {
	public RecurrenceDatesScribe() {
		super(RecurrenceDates.class, "RDATE", ICalDataType.DATE_TIME);
	}

	@Override
	protected ICalParameters _prepareParameters(RecurrenceDates property, WriteContext context) {
		boolean hasTime = property.hasTime() || !property.getPeriods().isEmpty();
		return handleTzidParameter(property, hasTime, context);
	}

	@Override
	protected ICalDataType _dataType(RecurrenceDates property, ICalVersion version) {
		if (property.getDates() != null) {
			return property.hasTime() ? ICalDataType.DATE_TIME : ICalDataType.DATE;
		}
		if (property.getPeriods() != null) {
			return ICalDataType.PERIOD;
		}
		return defaultDataType(version);
	}

	@Override
	protected String _writeText(final RecurrenceDates property, final WriteContext context) {
		List<Date> dates = property.getDates();
		if (dates != null) {
			return list(dates, new ListCallback<Date>() {
				public String asString(Date date) {
					return date(date).time(property.hasTime()).tz(context.getTimezoneInfo().isFloating(property), context.getTimezoneInfo().getTimeZoneToWriteIn(property)).write();
				}
			});
		}

		//TODO vCal does not support periods
		List<Period> periods = property.getPeriods();
		if (periods != null) {
			return list(periods, new ListCallback<Period>() {
				public String asString(Period period) {
					StringBuilder sb = new StringBuilder();

					if (period.getStartDate() != null) {
						String date = date(period.getStartDate()).tz(context.getTimezoneInfo().isFloating(property), context.getTimezoneInfo().getTimeZoneToWriteIn(property)).write();
						sb.append(date);
					}

					sb.append('/');

					if (period.getEndDate() != null) {
						String date = date(period.getEndDate()).tz(context.getTimezoneInfo().isFloating(property), context.getTimezoneInfo().getTimeZoneToWriteIn(property)).write();
						sb.append(date);
					} else if (period.getDuration() != null) {
						sb.append(period.getDuration());
					}

					return sb.toString();
				}
			});
		}

		return "";
	}

	@Override
	protected RecurrenceDates _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return parse(list(value), dataType, parameters, context);
	}

	@Override
	protected void _writeXml(RecurrenceDates property, XCalElement element, WriteContext context) {
		List<Date> dates = property.getDates();
		if (dates != null) {
			ICalDataType dataType = property.hasTime() ? ICalDataType.DATE_TIME : ICalDataType.DATE;
			if (dates.isEmpty()) {
				element.append(dataType, "");
			} else {
				for (Date date : dates) {
					String dateStr = date(date).time(property.hasTime()).tz(context.getTimezoneInfo().isFloating(property), context.getTimezoneInfo().getTimeZoneToWriteIn(property)).extended(true).write();
					element.append(dataType, dateStr);
				}
			}
			return;
		}

		List<Period> periods = property.getPeriods();
		if (periods != null) {
			if (periods.isEmpty()) {
				element.append(ICalDataType.PERIOD, "");
			} else {
				for (Period period : periods) {
					XCalElement periodElement = element.append(ICalDataType.PERIOD);

					Date start = period.getStartDate();
					if (start != null) {
						periodElement.append("start", date(start).tz(context.getTimezoneInfo().isFloating(property), context.getTimezoneInfo().getTimeZoneToWriteIn(property)).extended(true).write());
					}

					Date end = period.getEndDate();
					if (end != null) {
						periodElement.append("end", date(end).tz(context.getTimezoneInfo().isFloating(property), context.getTimezoneInfo().getTimeZoneToWriteIn(property)).extended(true).write());
					}

					Duration duration = period.getDuration();
					if (duration != null) {
						periodElement.append("duration", duration.toString());
					}
				}
			}
			return;
		}

		element.append(defaultDataType, "");
	}

	@Override
	protected RecurrenceDates _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		String tzid = parameters.getTimezoneId();

		//parse as periods
		List<XCalElement> periodElements = element.children(ICalDataType.PERIOD);
		if (!periodElements.isEmpty()) {
			List<Period> periods = new ArrayList<Period>(periodElements.size());
			RecurrenceDates property = new RecurrenceDates(periods);
			for (XCalElement periodElement : periodElements) {
				String startStr = periodElement.first("start");
				if (startStr == null) {
					context.addWarning(9);
					continue;
				}

				Date start = null;
				try {
					start = ICalDateFormat.parse(startStr);
				} catch (IllegalArgumentException e) {
					context.addWarning(10, startStr);
					continue;
				}

				String endStr = periodElement.first("end");
				if (endStr != null) {
					try {
						Date end = ICalDateFormat.parse(endStr);
						periods.add(new Period(start, end));

						if (!ICalDateFormat.isUTC(startStr)) {
							if (tzid == null) {
								context.addFloatingDate(property);
							} else {
								context.addTimezonedDate(tzid, property, start, startStr);
							}
						}

						if (end != null && !ICalDateFormat.isUTC(endStr)) {
							if (tzid == null) {
								context.addFloatingDate(property);
							} else {
								context.addTimezonedDate(tzid, property, end, endStr);
							}
						}
					} catch (IllegalArgumentException e) {
						context.addWarning(11, endStr);
					}
					continue;
				}

				String durationStr = periodElement.first("duration");
				if (durationStr != null) {
					try {
						Duration duration = Duration.parse(durationStr);
						periods.add(new Period(start, duration));

						if (!ICalDateFormat.isUTC(startStr)) {
							if (tzid == null) {
								context.addFloatingDate(property);
							} else {
								context.addTimezonedDate(tzid, property, start, startStr);
							}
						}
					} catch (IllegalArgumentException e) {
						context.addWarning(12, durationStr);
					}
					continue;
				}

				context.addWarning(13);
			}
			return property;
		}

		//parse as dates
		List<String> dateStrs = element.all(ICalDataType.DATE_TIME);
		boolean hasTime = !dateStrs.isEmpty();
		dateStrs.addAll(element.all(ICalDataType.DATE));
		if (!dateStrs.isEmpty()) {
			List<Date> dates = new ArrayList<Date>(dateStrs.size());
			RecurrenceDates property = new RecurrenceDates(dates, hasTime);
			for (String dateStr : dateStrs) {
				try {
					Date date = ICalDateFormat.parse(dateStr);
					dates.add(date);

					if (hasTime && !ICalDateFormat.isUTC(dateStr)) {
						if (tzid == null) {
							context.addFloatingDate(property);
						} else {
							context.addTimezonedDate(tzid, property, date, dateStr);
						}
					}
				} catch (IllegalArgumentException e) {
					context.addWarning(15, dateStr);
				}
			}
			return property;
		}

		throw missingXmlElements(ICalDataType.PERIOD, ICalDataType.DATE_TIME, ICalDataType.DATE);
	}

	@Override
	protected JCalValue _writeJson(RecurrenceDates property, WriteContext context) {
		List<String> values = new ArrayList<String>();

		List<Date> dates = property.getDates();
		List<Period> periods = property.getPeriods();
		if (dates != null) {
			for (Date date : dates) {
				String dateStr = date(date).time(property.hasTime()).tz(context.getTimezoneInfo().isFloating(property), context.getTimezoneInfo().getTimeZoneToWriteIn(property)).extended(true).write();
				values.add(dateStr);
			}
		} else if (periods != null) {
			for (Period period : property.getPeriods()) {
				StringBuilder sb = new StringBuilder();
				if (period.getStartDate() != null) {
					String value = date(period.getStartDate()).tz(context.getTimezoneInfo().isFloating(property), context.getTimezoneInfo().getTimeZoneToWriteIn(property)).extended(true).write();
					sb.append(value);
				}

				sb.append('/');

				if (period.getEndDate() != null) {
					String value = date(period.getEndDate()).tz(context.getTimezoneInfo().isFloating(property), context.getTimezoneInfo().getTimeZoneToWriteIn(property)).extended(true).write();
					sb.append(value);
				} else if (period.getDuration() != null) {
					sb.append(period.getDuration());
				}

				values.add(sb.toString());
			}
		}

		if (values.isEmpty()) {
			values.add("");
		}
		return JCalValue.multi(values);
	}

	@Override
	protected RecurrenceDates _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return parse(value.asMulti(), dataType, parameters, context);
	}

	private RecurrenceDates parse(List<String> valueStrs, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String tzid = parameters.getTimezoneId();

		if (dataType == ICalDataType.PERIOD) {
			//parse as periods
			List<Period> periods = new ArrayList<Period>(valueStrs.size());
			RecurrenceDates property = new RecurrenceDates(periods);
			for (String timePeriodStr : valueStrs) {
				String timePeriodStrSplit[] = timePeriodStr.split("/");

				if (timePeriodStrSplit.length < 2) {
					context.addWarning(13);
					continue;
				}

				String startStr = timePeriodStrSplit[0];
				Date start;
				try {
					start = ICalDateFormat.parse(startStr);
				} catch (IllegalArgumentException e) {
					context.addWarning(10, startStr);
					continue;
				}

				String endStr = timePeriodStrSplit[1];
				Date end = null;
				try {
					end = ICalDateFormat.parse(endStr);
					periods.add(new Period(start, end));
				} catch (IllegalArgumentException e) {
					//must be a duration
					try {
						Duration duration = Duration.parse(endStr);
						periods.add(new Period(start, duration));
					} catch (IllegalArgumentException e2) {
						context.addWarning(14, endStr);
						continue;
					}
				}

				if (!ICalDateFormat.isUTC(startStr)) {
					if (tzid == null) {
						context.addFloatingDate(property);
					} else {
						context.addTimezonedDate(tzid, property, start, startStr);
					}
				}

				if (end != null && !ICalDateFormat.isUTC(endStr)) {
					if (tzid == null) {
						context.addFloatingDate(property);
					} else {
						context.addTimezonedDate(tzid, property, end, endStr);
					}
				}
			}
			return property;
		}

		//parse as dates
		boolean hasTime = (dataType == ICalDataType.DATE_TIME);
		List<Date> dates = new ArrayList<Date>(valueStrs.size());
		RecurrenceDates property = new RecurrenceDates(dates, hasTime);
		for (String s : valueStrs) {
			Date date;
			try {
				date = ICalDateFormat.parse(s);
			} catch (IllegalArgumentException e) {
				context.addWarning(15, s);
				continue;
			}

			dates.add(date);

			if (hasTime && !ICalDateFormat.isUTC(s)) {
				if (tzid == null) {
					context.addFloatingDate(property);
				} else {
					context.addTimezonedDate(tzid, property, date, s);
				}
			}
		}
		return property;
	}
}
