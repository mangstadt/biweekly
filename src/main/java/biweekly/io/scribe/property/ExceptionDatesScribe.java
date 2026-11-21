package biweekly.io.scribe.property;

import static biweekly.ICalDataType.DATE;
import static biweekly.ICalDataType.DATE_TIME;

import java.util.List;
import java.util.stream.Collectors;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.ExceptionDates;
import biweekly.util.CopyOnModify;
import biweekly.util.ICalDate;

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
 * Marshals {@link ExceptionDates} properties.
 * @author Michael Angstadt
 */
public class ExceptionDatesScribe extends ListPropertyScribe<ExceptionDates, ICalDate> {
	public ExceptionDatesScribe() {
		super(ExceptionDates.class, "EXDATE");
	}

	@Override
	protected ICalDataType _defaultDataType(ICalVersion version) {
		return DATE_TIME;
	}

	@Override
	protected void _prepareParameters(ExceptionDates property, CopyOnModify<ICalParameters> parameters, WriteContext context) {
		if (isInObservance(context)) {
			return;
		}

		boolean hasTime;
		if (property.getValues().isEmpty()) {
			hasTime = false;
		} else {
			hasTime = (dataType(property, context.getVersion()) == DATE_TIME);
		}
		handleTzidParameter(property, parameters, hasTime, context);
	}

	@Override
	protected ICalDataType _dataType(ExceptionDates property, ICalVersion version) {
		List<ICalDate> dates = property.getValues();
		if (!dates.isEmpty()) {
			return dates.get(0).hasTime() ? DATE_TIME : DATE;
		}

		return defaultDataType(version);
	}

	@Override
	protected ExceptionDates newInstance(ICalDataType dataType, ICalParameters parameters) {
		return new ExceptionDates();
	}

	@Override
	protected String writeValue(ExceptionDates property, ICalDate value, WriteContext context) {
		if (isInObservance(context)) {
			return date(value).observance(true).extended(false).write();
		}

		return date(value, property, context).extended(false).write();
	}

	@Override
	protected ICalDate readValue(ExceptionDates property, String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		ICalDate date;
		try {
			boolean hasTime = (dataType == DATE_TIME);
			date = date(value).hasTime(hasTime).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(19);
		}
		context.addDate(date, property, parameters);

		return date;
	}

	@Override
	protected void _writeXml(ExceptionDates property, XCalElement element, WriteContext context) {
		List<ICalDate> values = property.getValues();
		if (values.isEmpty()) {
			element.append(defaultDataType(context.getVersion()), "");
			return;
		}

		if (isInObservance(context)) {
			//@formatter:off
			values.stream()
				.map(value -> date(value).observance(true).extended(true).write())
			.forEach(value -> element.append(DATE_TIME, value));
			//@formatter:on
			return;
		}

		for (ICalDate value : values) {
			ICalDataType dataType = value.hasTime() ? DATE_TIME : DATE;
			String dateStr = date(value, property, context).extended(true).write();
			element.append(dataType, dateStr);
		}
	}

	@Override
	protected ExceptionDates _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		List<String> dateTimeElements = element.all(DATE_TIME);
		List<String> dateElements = element.all(DATE);
		if (dateTimeElements.isEmpty() && dateElements.isEmpty()) {
			throw missingXmlElements(DATE_TIME, DATE);
		}

		ExceptionDates property = new ExceptionDates();
		List<ICalDate> values = property.getValues();
		//@formatter:off
		dateTimeElements.stream()
			.map(value -> readValue(property, value, DATE_TIME, parameters, context))
		.forEach(values::add);
		dateElements.stream()
			.map(value -> readValue(property, value, DATE, parameters, context))
		.forEach(values::add);
		//@formatter:on
		return property;
	}

	@Override
	protected JCalValue _writeJson(ExceptionDates property, WriteContext context) {
		List<ICalDate> values = property.getValues();
		if (values.isEmpty()) {
			return JCalValue.single("");
		}

		//@formatter:off
		return JCalValue.multi(values.stream()
			.map(isInObservance(context)
				? value -> date(value).observance(true).extended(true).write()
				: value -> date(value, property, context).extended(true).write())
		.collect(Collectors.toList()));
		//@formatter:on
	}

	@Override
	protected ExceptionDates _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		List<String> valueStrs = value.asMulti();

		ExceptionDates property = new ExceptionDates();
		List<ICalDate> values = property.getValues();

		//@formatter:off
		valueStrs.stream()
			.map(valueStr -> readValue(property, valueStr, dataType, parameters, context))
		.forEach(values::add);
		//@formatter:on

		return property;
	}
}
