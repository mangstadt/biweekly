package biweekly.property.marshaller;

import java.util.Date;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.Warning;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.DateTimeProperty;

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
 * Marshals properties that have "date-time" values.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public abstract class DateTimePropertyMarshaller<T extends DateTimeProperty> extends ICalPropertyMarshaller<T> {
	public DateTimePropertyMarshaller(Class<T> clazz, String propertyName) {
		super(clazz, propertyName, ICalDataType.DATE_TIME);
	}

	@Override
	protected String _writeText(T property) {
		Date value = property.getValue();
		if (value != null) {
			return date(value).write(); //should always be in UTC time
		}

		return "";
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
		value = unescape(value);
		return parse(value, parameters, warnings);
	}

	@Override
	protected void _writeXml(T property, XCalElement element) {
		String dateStr = null;

		Date value = property.getValue();
		if (value != null) {
			dateStr = date(value).extended(true).write(); //should always be in UTC time
		}

		element.append(dataType(property), dateStr);
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, List<Warning> warnings) {
		String value = element.first(defaultDataType);
		if (value != null) {
			return parse(value, parameters, warnings);
		}

		throw missingXmlElements(defaultDataType);
	}

	@Override
	protected JCalValue _writeJson(T property) {
		Date value = property.getValue();
		if (value != null) {
			return JCalValue.single(date(value).extended(true).write());
		}

		return JCalValue.single("");
	}

	@Override
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
		String valueStr = value.asSingle();
		return parse(valueStr, parameters, warnings);
	}

	private T parse(String value, ICalParameters parameters, List<Warning> warnings) {
		try {
			Date date = date(value).tzid(parameters.getTimezoneId(), warnings).parse();
			return newInstance(date);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(17);
		}
	}

	protected abstract T newInstance(Date date);
}
