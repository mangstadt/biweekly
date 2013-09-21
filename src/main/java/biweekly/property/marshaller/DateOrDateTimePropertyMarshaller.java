package biweekly.property.marshaller;

import java.util.Date;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.DateOrDateTimeProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.ICalDateFormatter;

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
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class DateOrDateTimePropertyMarshaller<T extends DateOrDateTimeProperty> extends ICalPropertyMarshaller<T> {
	public DateOrDateTimePropertyMarshaller(Class<T> clazz, String propertyName) {
		super(clazz, propertyName, ICalDataType.DATE_TIME);
	}

	@Override
	protected ICalDataType _dataType(T property) {
		return (property.getRawComponents() != null || property.getValue() == null || property.hasTime()) ? ICalDataType.DATE_TIME : ICalDataType.DATE;
	}

	@Override
	protected String _writeText(T property) {
		DateTimeComponents components = property.getRawComponents();
		if (components != null) {
			return components.toString(false);
		}

		Date value = property.getValue();
		if (value != null) {
			return date(value).time(property.hasTime()).tz(property.isLocalTime(), property.getTimezoneId()).write();
		}

		return "";
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<String> warnings) {
		value = unescape(value);
		return parse(value, parameters, warnings);
	}

	@Override
	protected void _writeXml(T property, XCalElement element) {
		String dateStr = null;

		Date value = property.getValue();
		DateTimeComponents components = property.getRawComponents();
		if (components != null) {
			dateStr = components.toString(true);
		} else if (value != null) {
			dateStr = date(value).time(property.hasTime()).tz(property.isLocalTime(), property.getTimezoneId()).extended(true).write();
		}

		element.append(dataType(property), dateStr);
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		String value = element.first(ICalDataType.DATE_TIME);
		if (value == null) {
			value = element.first(ICalDataType.DATE);
		}

		if (value != null) {
			return parse(value, parameters, warnings);
		}

		throw missingXmlElements(ICalDataType.DATE_TIME, ICalDataType.DATE);
	}

	@Override
	protected JCalValue _writeJson(T property) {
		DateTimeComponents components = property.getRawComponents();
		if (components != null) {
			return JCalValue.single(components.toString(true));
		}

		Date value = property.getValue();
		if (value != null) {
			return JCalValue.single(date(value).time(property.hasTime()).tz(property.isLocalTime(), property.getTimezoneId()).extended(true).write());
		}

		return JCalValue.single("");
	}

	@Override
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, List<String> warnings) {
		String valueStr = value.asSingle();
		return parse(valueStr, parameters, warnings);
	}

	protected abstract T newInstance(Date date, boolean hasTime);

	private T parse(String value, ICalParameters parameters, List<String> warnings) {
		if (value == null) {
			return newInstance(null, true);
		}

		Date date;
		try {
			date = date(value).tzid(parameters.getTimezoneId(), warnings).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("Could not parse date-time value.");
		}

		DateTimeComponents components;
		try {
			components = DateTimeComponents.parse(value);
		} catch (IllegalArgumentException e) {
			warnings.add("Could not parse the raw date-time components: " + value);
			components = null;
		}

		boolean hasTime = ICalDateFormatter.dateHasTime(value);
		boolean localTz = !ICalDateFormatter.dateHasTimezone(value) && parameters.getTimezoneId() == null;

		T prop = newInstance(date, hasTime);
		prop.setRawComponents(components);
		prop.setLocalTime(localTz);
		return prop;
	}
}
