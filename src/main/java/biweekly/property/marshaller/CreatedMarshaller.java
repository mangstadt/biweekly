package biweekly.property.marshaller;

import java.util.Date;
import java.util.List;

import biweekly.parameter.ICalParameters;
import biweekly.property.Created;
import biweekly.util.ICalDateFormatter;
import biweekly.util.ISOFormat;


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
 * Marshals {@link Created} properties.
 * @author Michael Angstadt
 */
public class CreatedMarshaller extends ICalPropertyMarshaller<Created> {
	public CreatedMarshaller() {
		super(Created.class, "CREATED");
	}

	@Override
	protected String _writeText(Created property, List<String> warnings) {
		Date value = property.getValue();
		if (value == null) {
			return "";
		}

		return ICalDateFormatter.format(value, ISOFormat.UTC_TIME_BASIC);
	}

	@Override
	protected Created _parseText(String value, ICalParameters parameters, List<String> warnings) {
		value = unescape(value);

		Date date = null;
		try {
			date = ICalDateFormatter.parse(value);
		} catch (IllegalArgumentException e) {
			//TODO marshal as RawProperty instead so data isn't lost
			warnings.add("Could not parse date value: " + value);
		}

		return new Created(date);
	}
}
