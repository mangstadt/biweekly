package biweekly.property.marshaller;

import java.util.Date;
import java.util.List;

import biweekly.io.CannotParseException;
import biweekly.parameter.ICalParameters;
import biweekly.property.FreeBusy;
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
 * Marshals {@link FreeBusy} properties.
 * @author Michael Angstadt
 */
public class FreeBusyMarshaller extends ICalPropertyMarshaller<FreeBusy> {
	public FreeBusyMarshaller() {
		super(FreeBusy.class, "FREEBUSY");
	}

	@Override
	protected String _writeText(FreeBusy property, List<String> warnings) {
		List<Period> values = property.getValues();
		if (values.isEmpty()) {
			return "";
		}

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Period timePeriod : values) {
			if (first) {
				first = false;
			} else {
				sb.append(',');
			}

			if (timePeriod.getStartDate() != null) {
				sb.append(ICalDateFormatter.format(timePeriod.getStartDate(), ISOFormat.UTC_TIME_BASIC));
			}

			sb.append('/');

			if (timePeriod.getEndDate() != null) {
				sb.append(ICalDateFormatter.format(timePeriod.getEndDate(), ISOFormat.UTC_TIME_BASIC));
			} else if (timePeriod.getDuration() != null) {
				sb.append(timePeriod.getDuration());
			}
		}

		return sb.toString();
	}

	@Override
	protected FreeBusy _parseText(String value, ICalParameters parameters, List<String> warnings) {
		FreeBusy freebusy = new FreeBusy();

		String timePeriodStrs[] = parseList(value);
		for (String timePeriodStr : timePeriodStrs) {
			String timePeriodStrSplit[] = timePeriodStr.split("/");

			String startStr = timePeriodStrSplit[0];
			Date start = null;
			try {
				start = ICalDateFormatter.parse(startStr);
			} catch (IllegalArgumentException e) {
				throw new CannotParseException("Could not parse start date.");
			}

			if (timePeriodStrSplit.length > 1) {
				String endStr = timePeriodStrSplit[1];
				try {
					Date end = ICalDateFormatter.parse(endStr);
					freebusy.addValue(start, end);
				} catch (IllegalArgumentException e) {
					//must be a duration
					try {
						Duration duration = Duration.parse(endStr);
						freebusy.addValue(start, duration);
					} catch (IllegalArgumentException e2) {
						throw new CannotParseException("Could not parse duration value.");
					}
				}
			} else {
				warnings.add("No end date or duration found.");
				if (start != null) {
					freebusy.addValue(start, (Date) null);
				}
			}
		}

		return freebusy;
	}
}
