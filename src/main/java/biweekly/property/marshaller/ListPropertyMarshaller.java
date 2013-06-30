package biweekly.property.marshaller;

import java.util.List;

import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.ListProperty;
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
 * Marshals properties that contain a list of values.
 * @author Michael Angstadt
 */
public abstract class ListPropertyMarshaller<T extends ListProperty<V>, V> extends ICalPropertyMarshaller<T> {
	protected Value dataType;

	public ListPropertyMarshaller(Class<T> clazz, String propertyName) {
		this(clazz, propertyName, Value.TEXT);
	}

	public ListPropertyMarshaller(Class<T> clazz, String propertyName, Value dataType) {
		super(clazz, propertyName);
		this.dataType = dataType;
	}

	@Override
	protected String _writeText(final T property) {
		return StringUtils.join(property.getValues(), ",", new JoinCallback<V>() {
			public void handle(StringBuilder sb, V value) {
				String valueStr = writeValue(property, value);
				sb.append(escape(valueStr));
			}
		});
	}

	@Override
	protected T _parseText(String value, ICalParameters parameters, List<String> warnings) {
		T property = newInstance(parameters);

		String split[] = parseList(value);
		for (String s : split) {
			V v = readValue(s, parameters, warnings);
			property.addValue(v);
		}

		return property;
	}

	@Override
	protected void _writeXml(T property, XCalElement element) {
		for (V value : property.getValues()) {
			String valueStr = writeValue(property, value);
			element.append(dataType, valueStr);
		}
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		T property = newInstance(parameters);

		for (String valueStr : element.all(dataType)) {
			V value = readValue(valueStr, parameters, warnings);
			property.addValue(value);
		}

		return property;
	}

	protected abstract T newInstance(ICalParameters parameters);

	protected abstract String writeValue(T property, V value);

	protected abstract V readValue(String value, ICalParameters parameters, List<String> warnings);
}
