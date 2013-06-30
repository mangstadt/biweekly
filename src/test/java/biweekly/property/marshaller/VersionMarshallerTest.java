package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import biweekly.parameter.ICalParameters;
import biweekly.property.Version;
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
public class VersionMarshallerTest {
	private final VersionMarshaller marshaller = new VersionMarshaller();

	@Test
	public void writeText_min_max() {
		Version prop = new Version("1.0", "2.0");

		String actual = marshaller.writeText(prop);

		String expected = "1.0;2.0";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_max() {
		Version prop = new Version("2.0");

		String actual = marshaller.writeText(prop);

		String expected = "2.0";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_null() {
		Version prop = new Version(null);

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText_min_max() {
		String value = "1.0;2.0";
		ICalParameters params = new ICalParameters();

		Result<Version> result = marshaller.parseText(value, params);

		Version prop = result.getValue();
		assertEquals("1.0", prop.getMinVersion());
		assertEquals("2.0", prop.getMaxVersion());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_max() {
		String value = "2.0";
		ICalParameters params = new ICalParameters();

		Result<Version> result = marshaller.parseText(value, params);

		Version prop = result.getValue();
		assertNull(prop.getMinVersion());
		assertEquals("2.0", prop.getMaxVersion());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeXml() {
		Version prop = new Version("2.0");
		assertWriteXml("<text>2.0</text>", prop, marshaller);
	}

	@Test
	public void parseXml() {
		Result<Version> result = parseXCalProperty("<text>2.0</text>", marshaller);

		Version prop = result.getValue();
		assertNull(prop.getMinVersion());
		assertEquals("2.0", prop.getMaxVersion());
		assertWarnings(0, result.getWarnings());
	}
}
