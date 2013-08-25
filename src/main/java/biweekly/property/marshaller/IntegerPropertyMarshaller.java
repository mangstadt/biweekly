package biweekly.property.marshaller;

import java.util.List;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.IntegerProperty;

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
 * Marshals properties that have integer values.
 * @author Michael Angstadt
 */
public abstract class IntegerPropertyMarshaller<T extends IntegerProperty> extends ICalPropertyMarshaller<T> {
	public IntegerPropertyMarshaller(Class<T> clazz, String propertyName) {
		super(clazz, propertyName, ICalDataType.INTEGER);
	}

	@Override
	protected String _writeText(T property) {
		Integer value = property.getValue();
		return (value == null) ? "" : value.toString();
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, List<String> warnings) {
		value = unescape(value);
		return parse(value);
	}

	@Override
	protected void _writeXml(T property, XCalElement element) {
		Integer value = property.getValue();
		if (value != null) {
			element.append(getDataType(property), value.toString());
		}
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		return parse(element.first(defaultDataType));
	}

	@Override
	protected JCalValue _writeJson(T property) {
		return JCalValue.single(property.getValue());
	}

	@Override
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, List<String> warnings) {
		return parse(value.getSingleValued());
	}

	private T parse(String value) {
		if (value == null || value.length() == 0) {
			return newInstance(null);
		}

		try {
			Integer intValue = Integer.valueOf(value);
			return newInstance(intValue);
		} catch (NumberFormatException e) {
			throw new CannotParseException("Could not parse integer value.");
		}
	}

	protected abstract T newInstance(Integer value);
}
