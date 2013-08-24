package biweekly.property.marshaller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
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
public class ExceptionDatesMarshaller extends ListPropertyMarshaller<ExceptionDates, Date> {
	public ExceptionDatesMarshaller() {
		super(ExceptionDates.class, "EXDATE", Value.DATE_TIME);
	}

	@Override
	protected Value _getDataType(ExceptionDates property) {
		return property.hasTime() ? Value.DATE_TIME : Value.DATE;
	}

	@Override
	protected ExceptionDates newInstance(Value dataType, ICalParameters parameters) {
		return new ExceptionDates(dataType != Value.DATE);
	}

	@Override
	protected String writeValue(ExceptionDates property, Date value) {
		return date(value).time(property.hasTime()).tzid(property.getParameters().getTimezoneId()).write();
	}

	@Override
	protected Date readValue(String value, Value dataType, ICalParameters parameters, List<String> warnings) {
		try {
			return date(value).tzid(parameters.getTimezoneId(), warnings).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("Could not parse date value.");
		}
	}

	@Override
	protected void _writeXml(ExceptionDates property, XCalElement element) {
		Value dataType = getDataType(property);
		for (Date value : property.getValues()) {
			String dateStr = date(value).time(property.hasTime()).tzid(property.getParameters().getTimezoneId()).extended(true).write();
			element.append(dataType, dateStr);
		}
	}

	@Override
	protected ExceptionDates _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		List<String> values = element.all(Value.DATE_TIME);
		Value dataType = values.isEmpty() ? Value.DATE : Value.DATE_TIME;
		values.addAll(element.all(Value.DATE));

		ExceptionDates prop = new ExceptionDates(dataType == Value.DATE_TIME);
		for (String value : values) {
			Date date = readValue(value, dataType, parameters, warnings);
			prop.addValue(date);
		}
		return prop;
	}

	@Override
	protected JCalValue _writeJson(ExceptionDates property) {
		List<String> values = new ArrayList<String>();
		for (Date value : property.getValues()) {
			String dateStr = date(value).time(property.hasTime()).tzid(property.getParameters().getTimezoneId()).extended(true).write();
			values.add(dateStr);
		}
		return JCalValue.multi(values);
	}

	@Override
	protected ExceptionDates _parseJson(JCalValue value, Value dataType, ICalParameters parameters, List<String> warnings) {
		List<String> valueStrs = value.getMultivalued();

		ExceptionDates prop = new ExceptionDates(dataType == Value.DATE_TIME);
		for (String valueStr : valueStrs) {
			Date date = readValue(valueStr, dataType, parameters, warnings);
			prop.addValue(date);
		}
		return prop;
	}
}
