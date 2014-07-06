package biweekly.io.scribe.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.io.CannotParseException;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.ExceptionDates;

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
 * Marshals {@link ExceptionDates} properties.
 * @author Michael Angstadt
 */
public class ExceptionDatesScribe extends ListPropertyScribe<ExceptionDates, Date> {
	public ExceptionDatesScribe() {
		super(ExceptionDates.class, "EXDATE", ICalDataType.DATE_TIME);
	}

	@Override
	protected ICalParameters _prepareParameters(ExceptionDates property, WriteContext context) {
		return handleTzidParameter(property, property.hasTime(), false, context);
	}

	@Override
	protected ICalDataType _dataType(ExceptionDates property, ICalVersion version) {
		return property.hasTime() ? ICalDataType.DATE_TIME : ICalDataType.DATE;
	}

	@Override
	protected ExceptionDates newInstance(ICalDataType dataType, ICalParameters parameters) {
		return new ExceptionDates(dataType != ICalDataType.DATE);
	}

	@Override
	protected String writeValue(ExceptionDates property, Date value, WriteContext context) {
		return date(value).time(property.hasTime()).tz(context.getTimeZone()).write();
	}

	@Override
	protected Date readValue(String value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
		try {
			return date(value).tzid(parameters.getTimezoneId(), warnings).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(19);
		}
	}

	@Override
	protected void _writeXml(ExceptionDates property, XCalElement element, WriteContext context) {
		ICalDataType dataType = dataType(property, null);
		for (Date value : property.getValues()) {
			String dateStr = date(value).time(property.hasTime()).tzid(property.getParameters().getTimezoneId()).extended(true).write();
			element.append(dataType, dateStr);
		}
	}

	@Override
	protected ExceptionDates _parseXml(XCalElement element, ICalParameters parameters, List<Warning> warnings) {
		List<String> values = element.all(ICalDataType.DATE_TIME);
		ICalDataType dataType = values.isEmpty() ? ICalDataType.DATE : ICalDataType.DATE_TIME;
		values.addAll(element.all(ICalDataType.DATE));
		if (values.isEmpty()) {
			throw missingXmlElements(ICalDataType.DATE_TIME, ICalDataType.DATE);
		}

		ExceptionDates prop = new ExceptionDates(dataType == ICalDataType.DATE_TIME);
		for (String value : values) {
			Date date = readValue(value, dataType, parameters, warnings);
			prop.addValue(date);
		}
		return prop;
	}

	@Override
	protected JCalValue _writeJson(ExceptionDates property, WriteContext context) {
		List<Date> values = property.getValues();
		if (values.isEmpty()) {
			return JCalValue.single("");
		}

		List<String> valuesStr = new ArrayList<String>();
		for (Date value : values) {
			String dateStr = date(value).time(property.hasTime()).tzid(property.getParameters().getTimezoneId()).extended(true).write();
			valuesStr.add(dateStr);
		}
		return JCalValue.multi(valuesStr);
	}

	@Override
	protected ExceptionDates _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
		List<String> valueStrs = value.asMulti();

		ExceptionDates prop = new ExceptionDates(dataType == ICalDataType.DATE_TIME);
		for (String valueStr : valueStrs) {
			Date date = readValue(valueStr, dataType, parameters, warnings);
			prop.addValue(date);
		}
		return prop;
	}
}
