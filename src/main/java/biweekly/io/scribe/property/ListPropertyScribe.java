package biweekly.io.scribe.property;

import java.util.List;
import java.util.stream.Collectors;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

import biweekly.ICalDataType;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.ListProperty;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
 * @param <T> the property class
 * @param <V> the value class
 * @author Michael Angstadt
 */
public abstract class ListPropertyScribe<T extends ListProperty<V>, V> extends ICalPropertyScribe<T> {
	protected ListPropertyScribe(Class<T> clazz, String propertyName) {
		this(clazz, propertyName, ICalDataType.TEXT);
	}

	protected ListPropertyScribe(Class<T> clazz, String propertyName, ICalDataType dataType) {
		super(clazz, propertyName, dataType);
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		//@formatter:off
		List<String> valuesStr = property.getValues().stream()
			.map(value -> writeValue(property, value, context))
		.collect(Collectors.toList());
		//@formatter:on

		switch (context.getVersion()) {
		case V1_0:
			return VObjectPropertyValues.writeSemiStructured(valuesStr, false, true);
		default:
			return VObjectPropertyValues.writeList(valuesStr);
		}
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		List<String> values;
		switch (context.getVersion()) {
		case V1_0:
			values = VObjectPropertyValues.parseSemiStructured(value);
			break;
		default:
			values = VObjectPropertyValues.parseList(value);
			break;
		}

		return parse(values, dataType, parameters, context);
	}

	@Override
	protected void _writeXml(T property, XCalElement element, WriteContext context) {
		for (V value : property.getValues()) {
			ICalDataType dataType = dataType(property, null);
			String valueStr = writeValue(property, value, null);
			element.append(dataType, valueStr);
		}
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		ICalDataType dataType = defaultDataType(context.getVersion());
		List<String> values = element.all(dataType);
		if (!values.isEmpty()) {
			return parse(values, dataType, parameters, context);
		}

		throw missingXmlElements(dataType);
	}

	@Override
	protected JCalValue _writeJson(T property, WriteContext context) {
		List<V> values = property.getValues();
		if (!values.isEmpty()) {
			return JCalValue.multi(property.getValues());
		}

		return JCalValue.single("");
	}

	@Override
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		return parse(value.asMulti(), dataType, parameters, context);
	}

	private T parse(List<String> valueStrs, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		T property = newInstance(dataType, parameters);
		List<V> values = property.getValues();

		//@formatter:off
		valueStrs.stream()
			.map(value -> readValue(property, value, dataType, parameters, context))
		.forEach(values::add);
		//@formatter:on

		return property;
	}

	protected abstract T newInstance(ICalDataType dataType, ICalParameters parameters);

	protected abstract String writeValue(T property, V value, WriteContext context);

	protected abstract V readValue(T property, String value, ICalDataType dataType, ICalParameters parameters, ParseContext context);
}
