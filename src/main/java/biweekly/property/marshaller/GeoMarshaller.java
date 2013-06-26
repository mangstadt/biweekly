package biweekly.property.marshaller;

import java.util.List;

import biweekly.io.CannotParseException;
import biweekly.parameter.ICalParameters;
import biweekly.property.Geo;
import biweekly.util.ICalFloatFormatter;

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
 * Marshals {@link Geo} properties.
 * @author Michael Angstadt
 */
public class GeoMarshaller extends ICalPropertyMarshaller<Geo> {
	public GeoMarshaller() {
		super(Geo.class, "GEO");
	}

	@Override
	protected String _writeText(Geo property) {
		ICalFloatFormatter formatter = new ICalFloatFormatter();
		StringBuilder sb = new StringBuilder();

		if (property.getLatitude() != null) {
			sb.append(formatter.format(property.getLatitude()));
		}

		sb.append(';');

		if (property.getLongitude() != null) {
			sb.append(formatter.format(property.getLongitude()));
		}

		return sb.toString();
	}

	@Override
	protected Geo _parseText(String value, ICalParameters parameters, List<String> warnings) {
		String split[] = value.split(";");

		if (split.length < 2) {
			throw new CannotParseException("Could not parse value.");
		}

		String latitudeStr = split[0];
		String longitudeStr = split[1];

		Double latitude;
		try {
			latitude = Double.valueOf(latitudeStr);
		} catch (NumberFormatException e) {
			throw new CannotParseException("Could not parse latitude: " + latitudeStr);
		}

		Double longitude;
		try {
			longitude = Double.valueOf(longitudeStr);
		} catch (NumberFormatException e) {
			throw new CannotParseException("Could not parse longtude: " + longitudeStr);
		}

		return new Geo(latitude, longitude);
	}
}
