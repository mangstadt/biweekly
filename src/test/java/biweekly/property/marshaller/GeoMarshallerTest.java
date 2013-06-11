package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import biweekly.io.CannotParseException;
import biweekly.parameter.ICalParameters;
import biweekly.property.Geo;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;

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
 * @author Michael Angstadt
 */
public class GeoMarshallerTest {
	private final GeoMarshaller marshaller = new GeoMarshaller();

	@Test
	public void writeText() {
		Geo prop = new Geo(12.34, 56.78);

		String actual = marshaller.writeText(prop);

		String expected = "12.34;56.78";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		Geo prop = new Geo(null, 56.78);
		String actual = marshaller.writeText(prop);
		String expected = ";56.78";
		assertEquals(expected, actual);

		prop = new Geo(12.34, null);
		actual = marshaller.writeText(prop);
		expected = "12.34;";
		assertEquals(expected, actual);

		prop = new Geo(null, null);
		actual = marshaller.writeText(prop);
		expected = ";";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "12.34;56.78";
		ICalParameters params = new ICalParameters();

		Result<Geo> result = marshaller.parseText(value, params);

		assertEquals(12.34, result.getValue().getLatitude(), 0.001);
		assertEquals(56.78, result.getValue().getLongitude(), 0.001);
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_no_longitude() {
		String value = "12.34";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_empty() {
		String value = "";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_bad_latitude() {
		String value = "bad;56.78";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_bad_longitude() {
		String value = "12.34;bad";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, params);
	}
}
