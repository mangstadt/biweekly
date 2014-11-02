package biweekly.io.scribe.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.parameter.ICalParameters;
import biweekly.property.VCalAlarmProperty;
import biweekly.util.Duration;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
 * Marshals {@link VCalAlarmProperty} properties.
 * @author Michael Angstadt
 */
public abstract class VCalAlarmPropertyScribe<T extends VCalAlarmProperty> extends ICalPropertyScribe<T> {
	public VCalAlarmPropertyScribe(Class<T> clazz, String propertyName, ICalDataType defaultDataType) {
		super(clazz, propertyName, defaultDataType);
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		List<String> values = new ArrayList<String>(4);

		Date start = property.getStart();
		String value = date(start, property, context).extended(false).write();
		values.add(value);

		Duration snooze = property.getSnooze();
		value = (snooze == null) ? "" : snooze.toString();
		values.add(value);

		Integer repeat = property.getRepeat();
		value = (repeat == null) ? "" : repeat.toString();
		values.add(value);

		List<String> dataValues = writeData(property);
		values.addAll(dataValues);

		return structured(values.toArray());
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		SemiStructuredIterator it = semistructured(value);

		String next = next(it);
		Date start;
		try {
			start = (next == null) ? null : date(next).parse();
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("");
		}

		next = next(it);
		Duration snooze;
		try {
			snooze = (next == null) ? null : Duration.parse(next);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("");
		}

		next = next(it);
		Integer repeat;
		try {
			repeat = (next == null) ? null : Integer.valueOf(next);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("");
		}

		T property = create(dataType, it);
		property.setStart(start);
		property.setSnooze(snooze);
		property.setRepeat(repeat);
		return property;
	}

	private String next(SemiStructuredIterator it) {
		String next = it.next();
		if (next == null) {
			return null;
		}

		next = next.trim();
		return (next.length() == 0) ? null : next;
	}

	protected abstract List<String> writeData(T property);

	protected abstract T create(ICalDataType dataType, SemiStructuredIterator it);

	@Override
	public Set<ICalVersion> getSupportedVersions() {
		return EnumSet.of(ICalVersion.V1_0);
	}
}
