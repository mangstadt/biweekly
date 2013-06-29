package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import biweekly.parameter.ICalParameters;
import biweekly.property.RequestStatus;
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
public class RequestStatusMarshallerTest {
	private final RequestStatusMarshaller marshaller = new RequestStatusMarshaller();

	@Test
	public void writeText() {
		RequestStatus prop = new RequestStatus(null);
		String actual = marshaller.writeText(prop);
		String expected = "";
		assertEquals(expected, actual);

		prop = new RequestStatus("1.2.3");
		actual = marshaller.writeText(prop);
		expected = "1.2.3";
		assertEquals(expected, actual);

		prop = new RequestStatus("1.2.3");
		prop.setDescription("description;here");
		actual = marshaller.writeText(prop);
		expected = "1.2.3;description\\;here";
		assertEquals(expected, actual);

		prop = new RequestStatus("1.2.3");
		prop.setDescription("description;here");
		prop.setExceptionText("data;here");
		actual = marshaller.writeText(prop);
		expected = "1.2.3;description\\;here;data\\;here";
		assertEquals(expected, actual);

		prop = new RequestStatus("1.2.3");
		prop.setDescription(null);
		prop.setExceptionText("data;here");
		actual = marshaller.writeText(prop);
		expected = "1.2.3;;data\\;here";
		assertEquals(expected, actual);

		prop = new RequestStatus(null);
		prop.setDescription("description;here");
		prop.setExceptionText("data;here");
		actual = marshaller.writeText(prop);
		expected = ";description\\;here;data\\;here";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		ICalParameters params = new ICalParameters();

		String value = "1.2.3;description\\;here;data\\;here";
		Result<RequestStatus> result = marshaller.parseText(value, params);
		RequestStatus prop = result.getValue();
		assertEquals("1.2.3", prop.getStatusCode());
		assertEquals("description;here", prop.getDescription());
		assertEquals("data;here", prop.getExceptionText());
		assertWarnings(0, result.getWarnings());

		value = "1.2.3;description\\;here";
		result = marshaller.parseText(value, params);
		prop = result.getValue();
		assertEquals("1.2.3", prop.getStatusCode());
		assertEquals("description;here", prop.getDescription());
		assertEquals(null, prop.getExceptionText());
		assertWarnings(0, result.getWarnings());

		value = "1.2.3";
		result = marshaller.parseText(value, params);
		prop = result.getValue();
		assertEquals("1.2.3", prop.getStatusCode());
		assertEquals(null, prop.getDescription());
		assertEquals(null, prop.getExceptionText());
		assertWarnings(0, result.getWarnings());

		value = "";
		result = marshaller.parseText(value, params);
		prop = result.getValue();
		assertEquals("", prop.getStatusCode());
		assertEquals(null, prop.getDescription());
		assertEquals(null, prop.getExceptionText());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeXml() {
		RequestStatus prop = new RequestStatus("1.2.3");
		prop.setDescription("description");
		prop.setExceptionText("data");
		assertWriteXml("<code>1.2.3</code><description>description</description><data>data</data>", prop, marshaller);
	}

	@Test
	public void parseXml() {
		Result<RequestStatus> result = parseXCalProperty("<code>1.2.3</code><description>description</description><data>data</data>", marshaller);

		RequestStatus prop = result.getValue();
		assertEquals("1.2.3", prop.getStatusCode());
		assertEquals("description", prop.getDescription());
		assertEquals("data", prop.getExceptionText());
		assertWarnings(0, result.getWarnings());
	}
}
