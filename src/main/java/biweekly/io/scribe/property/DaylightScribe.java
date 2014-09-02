package biweekly.io.scribe.property;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.parameter.ICalParameters;
import biweekly.property.Daylight;
import biweekly.util.UtcOffset;

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
 * Marshals {@link Daylight} properties.
 * @author Michael Angstadt
 */
public class DaylightScribe extends ICalPropertyScribe<Daylight> {
	public DaylightScribe() {
		super(Daylight.class, "DAYLIGHT", null);
	}

	@Override
	protected String _writeText(Daylight property, WriteContext context) {
		if (!property.isDaylight()) {
			return "FALSE";
		}

		List<String> values = new ArrayList<String>();
		values.add("TRUE");

		UtcOffset offset = property.getOffset();
		values.add((offset == null) ? "" : offset.toString());

		Date start = property.getStart();
		values.add((start == null) ? "" : date(start).floating(true).write());

		Date end = property.getEnd();
		values.add((end == null) ? "" : date(end).floating(true).write());

		String standardName = property.getStandardName();
		values.add((standardName == null) ? "" : standardName);

		String daylightName = property.getDaylightName();
		values.add((daylightName == null) ? "" : daylightName);

		return structured(values.toArray());
	}

	@Override
	protected Daylight _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		StructuredIterator it = structured(value);

		String next = it.nextString();
		boolean flag = (next == null) ? false : Boolean.parseBoolean(next);

		UtcOffset offset = null;
		next = it.nextString();
		if (next != null) {
			try {
				offset = UtcOffset.parse(next);
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(33, next);
			}
		}

		Date start = null;
		next = it.nextString();
		if (next != null) {
			try {
				start = date(next).parse();
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(34, next);
			}
		}

		Date end = null;
		next = it.nextString();
		if (next != null) {
			try {
				end = date(next).parse();
			} catch (IllegalArgumentException e) {
				throw new CannotParseException(35, next);
			}
		}

		String standardName = it.nextString();
		String daylightName = it.nextString();

		return new Daylight(flag, offset, start, end, standardName, daylightName);
	}
}
