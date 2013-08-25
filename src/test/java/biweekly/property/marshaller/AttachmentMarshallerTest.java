package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.json.JCalValue;
import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;
import biweekly.property.Attachment;
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
public class AttachmentMarshallerTest {
	private final AttachmentMarshaller marshaller = new AttachmentMarshaller();

	@Test
	public void prepareParameters_uri() {
		Attachment prop = new Attachment("image/png", "http://example.com/image.png");
		prop.getParameters().setEncoding(Encoding.BASE64); //should be cleared

		ICalParameters params = marshaller.prepareParameters(prop);

		assertEquals("image/png", params.getFormatType());
		assertNull(params.getEncoding());
	}

	@Test
	public void prepareParameters_data() {
		Attachment prop = new Attachment("image/png", "data".getBytes());

		ICalParameters params = marshaller.prepareParameters(prop);

		assertEquals("image/png", params.getFormatType());
		assertEquals(Encoding.BASE64, params.getEncoding());
	}

	@Test
	public void getDataType_uri() {
		Attachment prop = new Attachment("image/png", "http://example.com/image.png");
		assertEquals(ICalDataType.URI, marshaller.getDataType(prop));
	}

	@Test
	public void getDataType_data() {
		Attachment prop = new Attachment("image/png", "data".getBytes());
		assertEquals(ICalDataType.BINARY, marshaller.getDataType(prop));
	}

	@Test
	public void writeText_uri() {
		Attachment prop = new Attachment("image/png", "http://example.com/image.png");

		String actual = marshaller.writeText(prop);

		String expected = "http://example.com/image.png";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_data() {
		Attachment prop = new Attachment("image/png", "data".getBytes());

		String actual = marshaller.writeText(prop);

		String expected = Base64.encodeBase64String("data".getBytes());
		assertEquals(expected, actual);
	}

	@Test
	public void parseText_uri() {
		String value = "http://example.com/image.png";
		ICalParameters params = new ICalParameters();

		Result<Attachment> result = marshaller.parseText(value, ICalDataType.URI, params);

		Attachment prop = result.getValue();
		assertEquals("http://example.com/image.png", prop.getUri());
		assertNull(prop.getData());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_data() {
		String value = Base64.encodeBase64String("data".getBytes());
		ICalParameters params = new ICalParameters();

		params.setEncoding(null);
		Result<Attachment> result = marshaller.parseText(value, ICalDataType.BINARY, params);
		Attachment prop = result.getValue();
		assertNull(prop.getUri());
		assertArrayEquals("data".getBytes(), prop.getData());
		assertWarnings(0, result.getWarnings());

		params.setEncoding(Encoding.BASE64);
		result = marshaller.parseText(value, ICalDataType.URI, params);
		prop = result.getValue();
		assertNull(prop.getUri());
		assertArrayEquals("data".getBytes(), prop.getData());
		assertWarnings(0, result.getWarnings());

		params.setEncoding(Encoding.BASE64);
		result = marshaller.parseText(value, ICalDataType.BINARY, params);
		prop = result.getValue();
		assertNull(prop.getUri());
		assertArrayEquals("data".getBytes(), prop.getData());
		assertWarnings(0, result.getWarnings());

		//treats base64 as a URI if no parameters are set
		params.setEncoding(null);
		result = marshaller.parseText(value, ICalDataType.URI, params);
		prop = result.getValue();
		assertEquals(Base64.encodeBase64String("data".getBytes()), prop.getUri());
		assertNull(prop.getData());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeXml_uri() {
		Attachment prop = new Attachment("image/png", "http://example.com/image.png");
		assertWriteXml("<uri>http://example.com/image.png</uri>", prop, marshaller);
	}

	@Test
	public void writeXml_data() {
		Attachment prop = new Attachment("image/png", "data".getBytes());
		assertWriteXml("<binary>" + Base64.encodeBase64String("data".getBytes()) + "</binary>", prop, marshaller);
	}

	@Test
	public void parseXml_uri() {
		Result<Attachment> result = parseXCalProperty("<uri>http://example.com/image.png</uri>", marshaller);

		Attachment prop = result.getValue();
		assertEquals("http://example.com/image.png", prop.getUri());
		assertNull(prop.getData());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_data() {
		Result<Attachment> result = parseXCalProperty("<binary>" + Base64.encodeBase64String("data".getBytes()) + "</binary>", marshaller);

		Attachment prop = result.getValue();
		assertNull(prop.getUri());
		assertArrayEquals("data".getBytes(), prop.getData());

		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeJson_uri() {
		Attachment prop = new Attachment("image/png", "http://example.com/image.png");

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals("http://example.com/image.png", actual.getSingleValued());
	}

	@Test
	public void writeJson_data() {
		Attachment prop = new Attachment("image/png", "data".getBytes());

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Base64.encodeBase64String("data".getBytes()), actual.getSingleValued());
	}

	@Test
	public void parseJson_uri() {
		Result<Attachment> result = marshaller.parseJson(JCalValue.single("http://example.com/image.png"), ICalDataType.URI, new ICalParameters());

		Attachment prop = result.getValue();
		assertEquals("http://example.com/image.png", prop.getUri());
		assertNull(prop.getData());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseJson_data() {
		Result<Attachment> result = marshaller.parseJson(JCalValue.single(Base64.encodeBase64String("data".getBytes())), ICalDataType.BINARY, new ICalParameters());

		Attachment prop = result.getValue();
		assertNull(prop.getUri());
		assertArrayEquals("data".getBytes(), prop.getData());

		assertWarnings(0, result.getWarnings());
	}

}
