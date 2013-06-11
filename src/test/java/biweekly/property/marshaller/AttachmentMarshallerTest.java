package biweekly.property.marshaller;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.junit.Test;

import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
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
	@Test
	public void prepareParameters_uri() {
		Attachment prop = new Attachment("image/png", "http://example.com/image.png");
		AttachmentMarshaller m = new AttachmentMarshaller();

		ICalParameters params = m.prepareParameters(prop);

		assertEquals("image/png", params.getFormatType());
		assertNull(params.getValue());
		assertNull(params.getEncoding());
	}

	@Test
	public void prepareParameters_data() {
		Attachment prop = new Attachment("image/png", "data".getBytes());
		AttachmentMarshaller m = new AttachmentMarshaller();

		ICalParameters params = m.prepareParameters(prop);

		assertEquals("image/png", params.getFormatType());
		assertEquals(Value.BINARY, params.getValue());
		assertEquals(Encoding.BASE64, params.getEncoding());
	}

	@Test
	public void writeText_uri() {
		Attachment prop = new Attachment("image/png", "http://example.com/image.png");
		AttachmentMarshaller m = new AttachmentMarshaller();

		String actual = m.writeText(prop);

		String expected = "http://example.com/image.png";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_data() {
		Attachment prop = new Attachment("image/png", "data".getBytes());
		AttachmentMarshaller m = new AttachmentMarshaller();

		String actual = m.writeText(prop);

		String expected = Base64.encodeBase64String("data".getBytes());
		assertEquals(expected, actual);
	}

	@Test
	public void parseText_uri() {
		String value = "http://example.com/image.png";
		ICalParameters params = new ICalParameters();
		AttachmentMarshaller m = new AttachmentMarshaller();

		Result<Attachment> result = m.parseText(value, params);

		assertEquals("http://example.com/image.png", result.getValue().getUri());
		assertNull(result.getValue().getData());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_data() {
		String value = Base64.encodeBase64String("data".getBytes());
		ICalParameters params = new ICalParameters();
		AttachmentMarshaller m = new AttachmentMarshaller();

		params.setValue(Value.BINARY);
		params.setEncoding(null);
		Result<Attachment> result = m.parseText(value, params);
		assertNull(result.getValue().getUri());
		assertArrayEquals("data".getBytes(), result.getValue().getData());
		assertWarnings(0, result.getWarnings());

		params.setValue(null);
		params.setEncoding(Encoding.BASE64);
		result = m.parseText(value, params);
		assertNull(result.getValue().getUri());
		assertArrayEquals("data".getBytes(), result.getValue().getData());
		assertWarnings(0, result.getWarnings());

		params.setValue(Value.BINARY);
		params.setEncoding(Encoding.BASE64);
		result = m.parseText(value, params);
		assertNull(result.getValue().getUri());
		assertArrayEquals("data".getBytes(), result.getValue().getData());
		assertWarnings(0, result.getWarnings());

		//treats base64 as a URI if no parameters are set
		params.setValue(null);
		params.setEncoding(null);
		result = m.parseText(value, params);
		assertEquals(Base64.encodeBase64String("data".getBytes()), result.getValue().getUri());
		assertNull(result.getValue().getData());
		assertWarnings(0, result.getWarnings());
	}

	private static void assertWarnings(int expectedSize, List<String> warnings) {
		assertEquals(warnings.toString(), expectedSize, warnings.size());
	}
}
