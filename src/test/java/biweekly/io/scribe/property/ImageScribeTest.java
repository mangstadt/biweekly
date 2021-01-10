package biweekly.io.scribe.property;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.ParseContext;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.Image;
import biweekly.util.org.apache.commons.codec.binary.Base64;

/*
 Copyright (c) 2013-2021, Michael Angstadt
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
public class ImageScribeTest extends ScribeTest<Image> {
	private final String url = "http://example.com/image.png";
	private final byte[] data = "data".getBytes();
	private final String base64Data = Base64.encodeBase64String(data);

	public ImageScribeTest() {
		super(new ImageScribe());
	}

	@Test
	public void parseText() {
		sensei.assertParseText(url).dataType(ICalDataType.URI).run(hasUri(url));
		sensei.assertParseText(base64Data).dataType(ICalDataType.BINARY).param("ENCODING", "BASE64").run(has(data));
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<uri>" + url + "</uri>").run(hasUri(url));
		sensei.assertParseXml("<binary>" + base64Data + "</binary>").run(has(data));
		sensei.assertParseXml("").cannotParse(23);
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(url).dataType(ICalDataType.URI).run(hasUri(url));
		sensei.assertParseJson(base64Data).dataType(ICalDataType.BINARY).run(has(data));
	}

	private Check<Image> hasUri(final String url) {
		return new Check<Image>() {
			public void check(Image property, ParseContext context) {
				assertEquals(url, property.getUri());
				assertNull(property.getData());
			}
		};
	}

	private Check<Image> has(final byte[] data) {
		return new Check<Image>() {
			public void check(Image property, ParseContext context) {
				assertNull(property.getUri());
				assertArrayEquals(data, property.getData());
			}
		};
	}
}
