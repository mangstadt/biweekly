package biweekly.property.marshaller;

import java.util.Date;
import java.util.List;

import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.DateOrDateTimeProperty;

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
 * Marshals properties that have either "date" or "date-time" values.
 * @author Michael Angstadt
 */
public abstract class DateOrDateTimePropertyMarshaller<T extends DateOrDateTimeProperty> extends ICalPropertyMarshaller<T> {
	public DateOrDateTimePropertyMarshaller(Class<T> clazz, String propertyName) {
		super(clazz, propertyName);
	}

	@Override
	protected void _prepareParameters(T property, ICalParameters copy) {
		Value value = (property.getValue() == null || property.hasTime()) ? null : Value.DATE;
		copy.setValue(value);

		//TODO "dtstart" needs to be outputted without a "Z" for "daylight" and "standard" components
		if ("local".equals(copy.getTimezoneId())) {
			copy.setTimezoneId(null);
		}
	}

	@Override
	protected String _writeText(T property) {
		Date value = property.getValue();
		if (value == null) {
			return "";
		}

		return date(value).time(property.hasTime()).tzid(property.getParameters().getTimezoneId()).write();
	}

	@Override
	protected T _parseText(String value, ICalParameters parameters, List<String> warnings) {
		value = unescape(value);

		return parse(value, parameters, warnings);
	}

	@Override
	protected void _writeXml(DateOrDateTimeProperty property, XCalElement element) {
		Date value = property.getValue();
		if (value == null) {
			return;
		}

		Value dataType = property.hasTime() ? Value.DATE_TIME : Value.DATE;
		String dateStr = date(value).time(property.hasTime()).tzid(property.getParameters().getTimezoneId()).extended(true).write();
		element.append(dataType, dateStr);
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		String value = element.first(Value.DATE_TIME);
		if (value == null) {
			value = element.first(Value.DATE);
		}
		return parse(value, parameters, warnings);
	}

	@Override
	protected JCalValue _writeJson(T property) {
		Date value = property.getValue();
		if (value == null) {
			return JCalValue.single(Value.DATE_TIME, null);
		}

		Value dataType = property.hasTime() ? Value.DATE_TIME : Value.DATE;
		String dateStr = date(value).time(property.hasTime()).tzid(property.getParameters().getTimezoneId()).extended(true).write();
		return JCalValue.single(dataType, dateStr);
	}

	@Override
	protected T _parseJson(JCalValue value, ICalParameters parameters, List<String> warnings) {
		String valueStr = value.getSingleValued();
		return parse(valueStr, parameters, warnings);
	}

	protected abstract T newInstance(Date date, boolean hasTime);

	private T parse(String value, ICalParameters parameters, List<String> warnings) {
		if (value == null) {
			return newInstance(null, true);
		}

		try {
			Date date = date(value).tzid(parameters.getTimezoneId(), warnings).parse();
			boolean hasTime = value.contains("T");
			return newInstance(date, hasTime);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("Could not parse date value.");
		}
	}
}
