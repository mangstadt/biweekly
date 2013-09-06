package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.DurationProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
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
 * @author Michael Angstadt
 */
public class DurationPropertyMarshallerTest {
	private final DurationPropertyMarshaller marshaller = new DurationPropertyMarshaller();
	private final Duration duration = Duration.builder().hours(1).minutes(30).build();

	@Test
	public void writeText() {
		DurationProperty prop = new DurationProperty(duration);

		String actual = marshaller.writeText(prop);

		String expected = "PT1H30M";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		DurationProperty prop = new DurationProperty(null);

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "PT1H30M";
		ICalParameters params = new ICalParameters();

		Result<DurationProperty> result = marshaller.parseText(value, ICalDataType.DURATION, params);

		assertEquals(duration, result.getProperty().getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseText_invalid() {
		String value = "invalid";
		ICalParameters params = new ICalParameters();

		marshaller.parseText(value, ICalDataType.DURATION, params);
	}

	@Test
	public void writeXml() {
		DurationProperty prop = new DurationProperty(duration);
		assertWriteXml("<duration>PT1H30M</duration>", prop, marshaller);
	}

	@Test
	public void writeXml_null() {
		DurationProperty prop = new DurationProperty(null);
		assertWriteXml("", prop, marshaller);
	}

	@Test
	public void parseXml() {
		Result<DurationProperty> result = parseXCalProperty("<duration>PT1H30M</duration>", marshaller);

		DurationProperty prop = result.getProperty();
		assertEquals(duration, prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_invalid() {
		parseXCalProperty("<duration>invalid</duration>", marshaller);
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_empty() {
		parseXCalProperty("", marshaller);
	}

	@Test
	public void writeJson() {
		DurationProperty prop = new DurationProperty(duration);

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals("PT1H30M", actual.getSingleValued());
	}

	@Test
	public void writeJson_null() {
		DurationProperty prop = new DurationProperty(null);

		JCalValue actual = marshaller.writeJson(prop);
		assertTrue(actual.getValues().get(0).isNull());
	}

	@Test
	public void parseJson() {
		Result<DurationProperty> result = marshaller.parseJson(JCalValue.single("PT1H30M"), ICalDataType.DURATION, new ICalParameters());

		DurationProperty prop = result.getProperty();
		assertEquals(duration, prop.getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseJson_invalid() {
		marshaller.parseJson(JCalValue.single("invalid"), ICalDataType.DURATION, new ICalParameters());
	}
}
