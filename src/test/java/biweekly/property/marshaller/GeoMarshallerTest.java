package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
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
	public void writeText_missing_latitude() {
		Geo prop = new Geo(null, 56.78);
		String actual = marshaller.writeText(prop);
		String expected = ";56.78";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_missing_longitude() {
		Geo prop = new Geo(12.34, null);
		String actual = marshaller.writeText(prop);
		String expected = "12.34;";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_missing_both() {
		Geo prop = new Geo(null, null);
		String actual = marshaller.writeText(prop);
		String expected = ";";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "12.34;56.78";
		ICalParameters params = new ICalParameters();

		Result<Geo> result = marshaller.parseText(value, Value.FLOAT, params);

		Geo prop = result.getValue();
		assertEquals(12.34, prop.getLatitude(), 0.001);
		assertEquals(56.78, prop.getLongitude(), 0.001);
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_no_longitude() {
		String value = "12.34";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, Value.FLOAT, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_empty() {
		String value = "";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, Value.FLOAT, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_bad_latitude() {
		String value = "bad;56.78";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, Value.FLOAT, params);
	}

	@Test(expected = CannotParseException.class)
	public void parseText_bad_longitude() {
		String value = "12.34;bad";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, Value.FLOAT, params);
	}

	@Test
	public void writeXml() {
		Geo prop = new Geo(12.34, 56.78);
		assertWriteXml("<latitude>12.34</latitude><longitude>56.78</longitude>", prop, marshaller);
	}

	@Test
	public void writeXml_missing_latitude() {
		Geo prop = new Geo(null, 56.78);
		assertWriteXml("<longitude>56.78</longitude>", prop, marshaller);
	}

	@Test
	public void writeXml_missing_longitude() {
		Geo prop = new Geo(12.34, null);
		assertWriteXml("<latitude>12.34</latitude>", prop, marshaller);
	}

	@Test
	public void writeXml_missing_both() {
		Geo prop = new Geo(null, null);
		assertWriteXml("", prop, marshaller);
	}

	@Test
	public void parseXml() {
		Result<Geo> result = parseXCalProperty("<latitude>12.34</latitude><longitude>56.78</longitude>", marshaller);

		Geo prop = result.getValue();
		assertEquals(12.34, prop.getLatitude(), 0.001);
		assertEquals(56.78, prop.getLongitude(), 0.001);
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_missing_latitude() {
		Result<Geo> result = parseXCalProperty("<longitude>56.78</longitude>", marshaller);

		Geo prop = result.getValue();
		assertNull(prop.getLatitude());
		assertEquals(56.78, prop.getLongitude(), 0.001);
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_missing_longitude() {
		Result<Geo> result = parseXCalProperty("<latitude>12.34</latitude>", marshaller);

		Geo prop = result.getValue();
		assertEquals(12.34, prop.getLatitude(), 0.001);
		assertNull(prop.getLongitude());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_missing_both() {
		Result<Geo> result = parseXCalProperty("", marshaller);

		Geo prop = result.getValue();
		assertNull(prop.getLatitude());
		assertNull(prop.getLongitude());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_bad_latitude() {
		parseXCalProperty("<latitude>bad</latitude><longitude>56.78</longitude>", marshaller);
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_bad_longitude() {
		parseXCalProperty("<latitude>12.34</latitude><longitude>bad</longitude>", marshaller);
	}

	@Test
	public void writeJson() {
		Geo prop = new Geo(12.34, 56.78);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(12.34, actual.getValues().get(0).getArray().get(0).getValue());
		assertEquals(56.78, actual.getValues().get(0).getArray().get(1).getValue());
	}

	@Test
	public void writeJson_missing_latitude() {
		Geo prop = new Geo(null, 56.78);

		JCalValue actual = marshaller.writeJson(prop);
		assertTrue(actual.getValues().get(0).getArray().get(0).isNull());
		assertEquals(56.78, actual.getValues().get(0).getArray().get(1).getValue());
	}

	@Test
	public void writeJson_missing_longitude() {
		Geo prop = new Geo(12.34, null);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(12.34, actual.getValues().get(0).getArray().get(0).getValue());
		assertTrue(actual.getValues().get(0).getArray().get(1).isNull());
	}

	@Test
	public void writeJson_missing_both() {
		Geo prop = new Geo(null, null);

		JCalValue actual = marshaller.writeJson(prop);
		assertTrue(actual.getValues().get(0).getArray().get(0).isNull());
		assertTrue(actual.getValues().get(0).getArray().get(1).isNull());
	}

	@Test
	public void parseJson() {
		Result<Geo> result = marshaller.parseJson(JCalValue.structured("12.34", "56.78"), Value.FLOAT, new ICalParameters());

		Geo prop = result.getValue();
		assertEquals(12.34, prop.getLatitude(), 0.001);
		assertEquals(56.78, prop.getLongitude(), 0.001);
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseJson_missing_longitude() {
		Result<Geo> result = marshaller.parseJson(JCalValue.structured("12.34"), Value.FLOAT, new ICalParameters());

		Geo prop = result.getValue();
		assertEquals(12.34, prop.getLatitude(), 0.001);
		assertNull(prop.getLongitude());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseJson_missing_both() {
		Result<Geo> result = marshaller.parseJson(JCalValue.structured(), Value.FLOAT, new ICalParameters());

		Geo prop = result.getValue();
		assertNull(prop.getLatitude());
		assertNull(prop.getLongitude());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseJson_bad_latitude() {
		marshaller.parseJson(JCalValue.structured("bad", "56.78"), Value.FLOAT, new ICalParameters());
	}

	@Test(expected = CannotParseException.class)
	public void parseJson_bad_longitude() {
		marshaller.parseJson(JCalValue.structured("12.34", "bad"), Value.FLOAT, new ICalParameters());
	}
}
