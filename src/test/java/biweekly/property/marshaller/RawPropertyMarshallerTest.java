package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.RawProperty;
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
public class RawPropertyMarshallerTest {
	private final RawPropertyMarshaller marshaller = new RawPropertyMarshaller("RAW");

	@Test
	public void writeText() {
		RawProperty prop = new RawProperty("RAW", "value");

		String actual = marshaller.writeText(prop);

		String expected = "value";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		RawProperty prop = new RawProperty("RAW", null);

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "value";
		ICalParameters params = new ICalParameters();

		Result<RawProperty> result = marshaller.parseText(value, null, params);

		assertEquals("value", result.getValue().getValue());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml() {
		Result<RawProperty> result = parseXCalProperty("<text>text</text>", marshaller);

		RawProperty prop = result.getValue();
		assertEquals("text", prop.getValue());
		assertEquals(Value.TEXT, prop.getDataType());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_unknown_tag() {
		Result<RawProperty> result = parseXCalProperty("<foo>text</foo>", marshaller);

		RawProperty prop = result.getValue();
		assertEquals("text", prop.getValue());
		assertNull(prop.getParameters().getValue());
		assertWarnings(0, result.getWarnings());
	}
}
