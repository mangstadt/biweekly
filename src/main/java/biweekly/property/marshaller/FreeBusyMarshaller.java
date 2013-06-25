package biweekly.property.marshaller;

import java.util.Date;
import java.util.List;

import biweekly.parameter.ICalParameters;
import biweekly.property.FreeBusy;
import biweekly.util.Duration;
import biweekly.util.Period;
import biweekly.util.StringUtils;
import biweekly.util.StringUtils.JoinCallback;

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
	protected String _writeText(FreeBusy property) {
		List<Period> values = property.getValues();
		if (values.isEmpty()) {
			return "";
		}

		return StringUtils.join(values, ",", new JoinCallback<Period>() {
			public void handle(StringBuilder sb, Period timePeriod) {
				if (timePeriod.getStartDate() != null) {
					sb.append(writeDate(timePeriod.getStartDate(), true, null));
				}

				sb.append('/');

				if (timePeriod.getEndDate() != null) {
					sb.append(writeDate(timePeriod.getEndDate(), true, null));
				} else if (timePeriod.getDuration() != null) {
					sb.append(timePeriod.getDuration());
				}
			}
		});
	}

	@Override
	protected FreeBusy _parseText(String value, ICalParameters parameters, List<String> warnings) {
		FreeBusy freebusy = new FreeBusy();

		String timePeriodStrs[] = parseList(value);
		for (String timePeriodStr : timePeriodStrs) {
			String timePeriodStrSplit[] = timePeriodStr.split("/");

			if (timePeriodStrSplit.length < 2) {
				warnings.add("No end date or duration found, skipping time period: " + timePeriodStr);
				continue;
			}

			String startStr = timePeriodStrSplit[0];
			Date start = null;
			try {
				start = parseDate(startStr, parameters.getTimezoneId(), warnings);
			} catch (IllegalArgumentException e) {
				warnings.add("Could not parse start date, skipping time period: " + timePeriodStr);
				continue;
			}

			String endStr = timePeriodStrSplit[1];
			try {
				Date end = parseDate(endStr, parameters.getTimezoneId(), warnings);
				freebusy.addValue(start, end);
			} catch (IllegalArgumentException e) {
				//must be a duration
				try {
					Duration duration = Duration.parse(endStr);
					freebusy.addValue(start, duration);
				} catch (IllegalArgumentException e2) {
					warnings.add("Could not parse end date or duration value, skipping time period: " + timePeriodStr);
					continue;
				}
			}
		}

		return freebusy;
	}
}
