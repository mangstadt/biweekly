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
import biweekly.property.Attachment;
import biweekly.util.org.apache.commons.codec.binary.Base64;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
public class AttachmentScribeTest extends ScribeTest<Attachment> {
	private final String formatType = "image/png";
	private final String url = "http://example.com/image.png";
	private final byte[] data = "data".getBytes();
	private final String base64Data = Base64.encodeBase64String(data);
	private final String contentId = "content-id";

	private final Attachment withUrl = new Attachment(formatType, url);
	private final Attachment withData = new Attachment(formatType, data);
	private final Attachment withContentId = new Attachment(formatType, (String) null);
	{
		withContentId.setContentId(contentId);
	}
	private final Attachment empty = new Attachment(null, (String) null);

	public AttachmentScribeTest() {
		super(new AttachmentScribe());
	}

	@Test
	public void prepareParameters() {
		sensei.assertPrepareParams(withUrl).expected("FMTTYPE", formatType).run();
		sensei.assertPrepareParams(withData).expected("FMTTYPE", formatType).expected("ENCODING", "BASE64").run();
		sensei.assertPrepareParams(withContentId).expected("FMTTYPE", formatType).run();
		sensei.assertPrepareParams(empty).run();
	}

	@Test
	public void dataType() {
		sensei.assertDataType(withUrl).versions(V1_0).run(ICalDataType.URL);
		sensei.assertDataType(withUrl).versions(V2_0_DEPRECATED, V2_0).run(ICalDataType.URI);

		sensei.assertDataType(withData).run(ICalDataType.BINARY);

		sensei.assertDataType(withContentId).versions(V1_0).run(ICalDataType.CONTENT_ID);
		sensei.assertDataType(withContentId).versions(V2_0_DEPRECATED, V2_0).run(ICalDataType.URI);

		sensei.assertDataType(empty).run(ICalDataType.URI);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withUrl).run(url);
		sensei.assertWriteText(withData).run(base64Data);
		sensei.assertWriteText(withContentId).version(V1_0).run('<' + contentId + '>');
		sensei.assertWriteText(withContentId).version(V2_0_DEPRECATED).run("cid:" + contentId);
		sensei.assertWriteText(withContentId).version(V2_0).run("cid:" + contentId);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText(url).dataType(ICalDataType.URI).run(hasUri(url));
		sensei.assertParseText(base64Data).dataType(ICalDataType.BINARY).param("ENCODING", "BASE64").run(has(data));

		sensei.assertParseText(contentId).dataType(ICalDataType.CONTENT_ID).run(hasCid(contentId));
		sensei.assertParseText('<' + contentId + '>').dataType(ICalDataType.CONTENT_ID).run(hasCid(contentId));
		sensei.assertParseText('<' + contentId).dataType(ICalDataType.CONTENT_ID).run(hasCid('<' + contentId));
		sensei.assertParseText(contentId + '>').dataType(ICalDataType.CONTENT_ID).run(hasCid(contentId + '>'));
		sensei.assertParseText("cid:" + contentId).dataType(ICalDataType.CONTENT_ID).run(hasCid(contentId));
		sensei.assertParseText("CID:" + contentId).dataType(ICalDataType.CONTENT_ID).run(hasCid(contentId));
		sensei.assertParseText("cid:" + contentId).run(hasCid(contentId));
		sensei.assertParseText("CID:" + contentId).run(hasCid(contentId));
		sensei.assertParseText("aaa:" + contentId).run(hasUri("aaa:" + contentId));
		sensei.assertParseText("http:" + contentId).run(hasUri("http:" + contentId));
		sensei.assertParseText("").dataType(ICalDataType.CONTENT_ID).run(hasCid(""));
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withUrl).run("<uri>" + url + "</uri>");
		sensei.assertWriteXml(withData).run("<binary>" + base64Data + "</binary>");
		sensei.assertWriteXml(withContentId).run("<uri>cid:" + contentId + "</uri>");
		sensei.assertWriteXml(empty).run("<uri/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<uri>" + url + "</uri>").run(hasUri(url));
		sensei.assertParseXml("<binary>" + base64Data + "</binary>").run(has(data));

		sensei.assertParseXml("<uri>cid:" + contentId + "</uri>").run(hasCid(contentId));
		sensei.assertParseXml("<uri>CID:" + contentId + "</uri>").run(hasCid(contentId));
		sensei.assertParseXml("<uri>aaa:" + contentId + "</uri>").run(hasUri("aaa:" + contentId));
		sensei.assertParseXml("<uri>http:" + contentId + "</uri>").run(hasUri("http:" + contentId));

		sensei.assertParseXml("").cannotParse(23);
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withUrl).run(url);
		sensei.assertWriteJson(withData).run(base64Data);
		sensei.assertWriteJson(withContentId).run("cid:" + contentId);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(url).dataType(ICalDataType.URI).run(hasUri(url));
		sensei.assertParseJson(base64Data).dataType(ICalDataType.BINARY).run(has(data));
		sensei.assertParseJson("cid:" + contentId).dataType(ICalDataType.URI).run(hasCid(contentId));
		sensei.assertParseJson("CID:" + contentId).dataType(ICalDataType.URI).run(hasCid(contentId));
		sensei.assertParseJson("aaa:" + contentId).dataType(ICalDataType.URI).run(hasUri("aaa:" + contentId));
		sensei.assertParseJson("http:" + contentId).dataType(ICalDataType.URI).run(hasUri("http:" + contentId));
	}

	private Check<Attachment> hasUri(final String url) {
		return new Check<Attachment>() {
			public void check(Attachment property, ParseContext context) {
				assertEquals(url, property.getUri());
				assertNull(property.getData());
				assertNull(property.getContentId());
			}
		};
	}

	private Check<Attachment> hasCid(final String cid) {
		return new Check<Attachment>() {
			public void check(Attachment property, ParseContext context) {
				assertNull(property.getUri());
				assertNull(property.getData());
				assertEquals(cid, property.getContentId());
			}
		};
	}

	private Check<Attachment> has(final byte[] data) {
		return new Check<Attachment>() {
			public void check(Attachment property, ParseContext context) {
				assertNull(property.getUri());
				assertArrayEquals(data, property.getData());
				assertNull(property.getContentId());
			}
		};
	}
}
