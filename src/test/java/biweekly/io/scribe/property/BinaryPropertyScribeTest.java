package biweekly.io.scribe.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.ParseContext;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.BinaryProperty;
import biweekly.util.org.apache.commons.codec.binary.Base64;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
public class BinaryPropertyScribeTest extends ScribeTest<BinaryProperty> {
	private final String formatType = "image/png";
	private final String url = "http://example.com/image.png";
	private final byte[] data = "data".getBytes();
	private final String base64Data = Base64.encodeBase64String(data);

	private final BinaryProperty withUrl = new BinaryProperty(url);
	{
		withUrl.setFormatType(formatType);
	}
	private final BinaryProperty withData = new BinaryProperty(data);
	{
		withData.setFormatType(formatType);
	}
	private final BinaryProperty empty = new BinaryProperty((String) null);

	public BinaryPropertyScribeTest() {
		super(new BinaryPropertyScribeImpl());
	}

	@Test
	public void prepareParameters() {
		sensei.assertPrepareParams(withUrl).expected("FMTTYPE", formatType).run();
		sensei.assertPrepareParams(withData).expected("FMTTYPE", formatType).expected("ENCODING", "BASE64").run();
		sensei.assertPrepareParams(empty).run();
	}

	@Test
	public void dataType() {
		sensei.assertDataType(withUrl).versions(V1_0).run(ICalDataType.URL);
		sensei.assertDataType(withUrl).versions(V2_0_DEPRECATED, V2_0).run(ICalDataType.URI);
		sensei.assertDataType(withData).run(ICalDataType.BINARY);
		sensei.assertDataType(empty).run(ICalDataType.URI);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withUrl).run(url);
		sensei.assertWriteText(withData).run(base64Data);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(url).dataType(ICalDataType.URI).run(has(url));

		sensei.assertParseText(base64Data).dataType(ICalDataType.BINARY).run(has(data));
		sensei.assertParseText(base64Data).dataType(ICalDataType.BINARY).param("ENCODING", "BASE64").run(has(data));
		sensei.assertParseText(base64Data).dataType(ICalDataType.URI).param("ENCODING", "BASE64").run(has(data));

		String base64DataWithWhitespace = base64Data.substring(0, base64Data.length() / 2) + "    " + base64Data.substring(base64Data.length() / 2);
		sensei.assertParseText(base64DataWithWhitespace).dataType(ICalDataType.BINARY).run(has(data));
		sensei.assertParseText(base64DataWithWhitespace).dataType(ICalDataType.BINARY).param("ENCODING", "BASE64").run(has(data));
		sensei.assertParseText(base64DataWithWhitespace).dataType(ICalDataType.URI).param("ENCODING", "BASE64").run(has(data));

		//if data type is URI and no ENCODING parameter is present, it treats the value as a URI
		sensei.assertParseText(base64Data).dataType(ICalDataType.URI).run(has(base64Data));

		sensei.assertParseText("").dataType(ICalDataType.URI).run(has(""));
		sensei.assertParseText("").dataType(ICalDataType.BINARY).run(has(new byte[0]));
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withUrl).run("<uri>" + url + "</uri>");
		sensei.assertWriteXml(withData).run("<binary>" + base64Data + "</binary>");
		sensei.assertWriteXml(empty).run("<uri/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<uri>" + url + "</uri>").run(has(url));
		sensei.assertParseXml("<binary>" + base64Data + "</binary>").run(has(data));

		//<uri> is preferred
		sensei.assertParseXml("<uri>" + url + "</uri><binary>" + base64Data + "</binary>").run(has(url));

		sensei.assertParseXml("").cannotParse(23);
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withUrl).run(url);
		sensei.assertWriteJson(withData).run(base64Data);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(url).dataType(ICalDataType.URI).run(has(url));
		sensei.assertParseJson("").dataType(ICalDataType.URI).run(has(""));
		sensei.assertParseJson(base64Data).dataType(ICalDataType.BINARY).run(has(data));
		sensei.assertParseJson("").dataType(ICalDataType.BINARY).run(has(new byte[0]));
	}

	private Check<BinaryProperty> has(final String url) {
		return new Check<BinaryProperty>() {
			public void check(BinaryProperty property, ParseContext context) {
				assertEquals(url, property.getUri());
				assertNull(property.getData());
			}
		};
	}

	private Check<BinaryProperty> has(final byte[] data) {
		return new Check<BinaryProperty>() {
			public void check(BinaryProperty property, ParseContext context) {
				assertNull(property.getUri());
				assertArrayEquals(data, property.getData());
			}
		};
	}

	private static class BinaryPropertyScribeImpl extends BinaryPropertyScribe<BinaryProperty> {
		public BinaryPropertyScribeImpl() {
			super(BinaryProperty.class, "BINARY");
		}

		@Override
		protected BinaryProperty newInstance(byte[] data) {
			return new BinaryProperty(data);
		}

		@Override
		protected BinaryProperty newInstance(String value, ICalDataType dataTye) {
			return new BinaryProperty(value);
		}
	}
}
