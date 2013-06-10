package biweekly.property.marshaller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.io.CannotParseException;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.RecurrenceDates;
import biweekly.util.Duration;
import biweekly.util.ICalDateFormatter;
import biweekly.util.ISOFormat;
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
public class RecurrenceDatesMarshaller extends ICalPropertyMarshaller<RecurrenceDates> {
	public RecurrenceDatesMarshaller() {
		super(RecurrenceDates.class, "RDATE");
	}

	@Override
	protected void _prepareParameters(RecurrenceDates property, ICalParameters copy) {
		if (property.getDates() != null) {
			if (!property.hasTime()) {
				copy.setValue(Value.DATE);
			}
		} else if (property.getPeriods() != null) {
			copy.setValue(Value.PERIOD);
		}
	}

	@Override
	protected String _writeText(RecurrenceDates property, List<String> warnings) {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		if (property.getDates() != null) {
			ISOFormat format = property.hasTime() ? ISOFormat.UTC_TIME_BASIC : ISOFormat.DATE_BASIC;
			for (Date date : property.getDates()) {
				if (date == null) {
					continue;
				}

				if (first) {
					first = false;
				} else {
					sb.append(',');
				}

				sb.append(ICalDateFormatter.format(date, format));
			}
		} else if (property.getPeriods() != null) {
			for (Period period : property.getPeriods()) {
				if (period == null) {
					continue;
				}

				if (period.getStartDate() != null) {
					sb.append(ICalDateFormatter.format(period.getStartDate(), ISOFormat.UTC_TIME_BASIC));
				}

				sb.append('/');

				if (period.getEndDate() != null) {
					sb.append(ICalDateFormatter.format(period.getEndDate(), ISOFormat.UTC_TIME_BASIC));
				} else if (period.getDuration() != null) {
					sb.append(period.getDuration());
				}

				sb.append(period);
			}
		}

		return sb.toString();
	}

	@Override
	protected RecurrenceDates _parseText(String value, ICalParameters parameters, List<String> warnings) {
		String split[] = parseList(value);

		Value valueParam = parameters.getValue();
		if (valueParam == null || valueParam == Value.DATE || valueParam == Value.DATE_TIME) {
			//parse as dates
			boolean hasTime = (valueParam == null || valueParam == Value.DATE_TIME);
			List<Date> dates = new ArrayList<Date>(split.length);
			for (String s : split) {
				try {
					dates.add(ICalDateFormatter.parse(s));
				} catch (IllegalArgumentException e) {
					throw new CannotParseException("Could not parse date.");
				}
			}
			return new RecurrenceDates(dates, hasTime);
		} else {
			//parse as periods
			List<Period> periods = new ArrayList<Period>(split.length);
			for (String s : split) {
				String timePeriodStrSplit[] = s.split("/");

				String startStr = timePeriodStrSplit[0];
				Date start;
				try {
					start = ICalDateFormatter.parse(startStr);
				} catch (IllegalArgumentException e) {
					throw new CannotParseException("Could not parse start date.");
				}

				Period period = null;
				if (timePeriodStrSplit.length > 1) {
					String endStr = timePeriodStrSplit[1];
					try {
						Date end = ICalDateFormatter.parse(endStr);
						period = new Period(start, end);
					} catch (IllegalArgumentException e) {
						//must be a duration
						try {
							Duration duration = Duration.parse(endStr);
							period = new Period(start, duration);
						} catch (IllegalArgumentException e2) {
							throw new CannotParseException("Could not parse duration value.");
						}
					}
				} else {
					warnings.add("No end date or duration found.");
					period = new Period(start, (Date) null);
				}

				periods.add(period);
			}
			return new RecurrenceDates(periods);
		}
	}
}
