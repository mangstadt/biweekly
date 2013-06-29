package biweekly.property.marshaller;

import java.util.List;

import biweekly.io.CannotParseException;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.DurationProperty;
import biweekly.util.Duration;

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
 * Marshals {@link DurationProperty} properties.
 * @author Michael Angstadt
 */
public class DurationPropertyMarshaller extends ICalPropertyMarshaller<DurationProperty> {
	public DurationPropertyMarshaller() {
		super(DurationProperty.class, "DURATION");
	}

	@Override
	protected String _writeText(DurationProperty property) {
		Duration duration = property.getValue();
		return (duration == null) ? "" : duration.toString();
	}

	@Override
	protected DurationProperty _parseText(String value, ICalParameters parameters, List<String> warnings) {
		value = unescape(value);
		return parse(value);
	}

	@Override
	protected void _writeXml(DurationProperty property, XCalElement element) {
		Duration value = property.getValue();
		if (value != null) {
			element.append(Value.DURATION, value.toString());
		}
	}

	@Override
	protected DurationProperty _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		String value = element.first(Value.DURATION);
		return parse(value);
	}

	private DurationProperty parse(String value) {
		if (value == null) {
			return new DurationProperty(null);
		}

		try {
			Duration duration = Duration.parse(value);
			return new DurationProperty(duration);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException("Could not parse duration value.");
		}
	}
}