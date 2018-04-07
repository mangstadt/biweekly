package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import biweekly.io.ParseContext;
import biweekly.io.json.JCalValue;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.RequestStatus;

/*
 Copyright (c) 2013-2018, Michael Angstadt
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
public class RequestStatusScribeTest extends ScribeTest<RequestStatus> {
	private final String code = "1.2.3;", description = "description;", data = "data;";

	private final RequestStatus withAll = new RequestStatus(code);
	{
		withAll.setDescription(description);
		withAll.setExceptionText(data);
	}
	private final RequestStatus withCodeDescription = new RequestStatus(code);
	{
		withCodeDescription.setDescription(description);
	}
	private final RequestStatus withCodeData = new RequestStatus(code);
	{
		withCodeData.setExceptionText(data);
	}
	private final RequestStatus withDescriptionData = new RequestStatus((String) null);
	{
		withDescriptionData.setDescription(description);
		withDescriptionData.setExceptionText(data);
	}
	private final RequestStatus withCode = new RequestStatus(code);
	private final RequestStatus withDescription = new RequestStatus((String) null);
	{
		withDescription.setDescription(description);
	}
	private final RequestStatus withData = new RequestStatus((String) null);
	{
		withData.setExceptionText(data);
	}
	private final RequestStatus empty = new RequestStatus((String) null);

	public RequestStatusScribeTest() {
		super(new RequestStatusScribe());
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withAll).run("1.2.3\\;;description\\;;data\\;");
		sensei.assertWriteText(withCodeDescription).run("1.2.3\\;;description\\;;");
		sensei.assertWriteText(withCodeData).run("1.2.3\\;;;data\\;");
		sensei.assertWriteText(withDescriptionData).run(";description\\;;data\\;");
		sensei.assertWriteText(withCode).run("1.2.3\\;;;");
		sensei.assertWriteText(withDescription).run(";description\\;;");
		sensei.assertWriteText(withData).run(";;data\\;");
		sensei.assertWriteText(empty).run(";;");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("1.2.3\\;;description\\;;data\\;").run(is(withAll));
		sensei.assertParseText("1.2.3\\;;description\\;").run(is(withCodeDescription));
		sensei.assertParseText("1.2.3\\;;;data\\;").run(is(withCodeData));
		sensei.assertParseText(";description\\;;data\\;").run(is(withDescriptionData));
		sensei.assertParseText("1.2.3\\;").run(is(withCode));
		sensei.assertParseText(";description\\;").run(is(withDescription));
		sensei.assertParseText(";;data\\;").run(is(withData));
		sensei.assertParseText(";;").run(is(empty));
		sensei.assertParseText(";").run(is(empty));
		sensei.assertParseText("").run(is(empty));
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withAll).run("<code>" + code + "</code><description>" + description + "</description><data>" + data + "</data>");
		sensei.assertWriteXml(withCodeDescription).run("<code>" + code + "</code><description>" + description + "</description>");
		sensei.assertWriteXml(withCodeData).run("<code>" + code + "</code><description/><data>" + data + "</data>");
		sensei.assertWriteXml(withDescriptionData).run("<code/><description>" + description + "</description><data>" + data + "</data>");
		sensei.assertWriteXml(withCode).run("<code>" + code + "</code><description/>");
		sensei.assertWriteXml(withDescription).run("<code/><description>" + description + "</description>");
		sensei.assertWriteXml(withData).run("<code/><description/><data>" + data + "</data>");
		sensei.assertWriteXml(empty).run("<code/><description/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<code>" + code + "</code><description>" + description + "</description><data>" + data + "</data>").run(is(withAll));
		sensei.assertParseXml("<code>" + code + "</code><description>" + description + "</description>").run(is(withCodeDescription));
		sensei.assertParseXml("<code>" + code + "</code><data>" + data + "</data>").run(is(withCodeData));
		sensei.assertParseXml("<description>" + description + "</description><data>" + data + "</data>").cannotParse(23);
		sensei.assertParseXml("<code>" + code + "</code>").run(is(withCode));
		sensei.assertParseXml("<description>" + description + "</description>").cannotParse(23);
		sensei.assertParseXml("<data>" + data + "</data>").cannotParse(23);
		sensei.assertParseXml("<code/><description/><data/>").run(is(empty));
		sensei.assertParseXml("").cannotParse(23);
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withAll).run(JCalValue.structured(code, description, data));
		sensei.assertWriteJson(withCodeDescription).run(JCalValue.structured(code, description, ""));
		sensei.assertWriteJson(withCodeData).run(JCalValue.structured(code, "", data));
		sensei.assertWriteJson(withDescriptionData).run(JCalValue.structured("", description, data));
		sensei.assertWriteJson(withCode).run(JCalValue.structured(code, "", ""));
		sensei.assertWriteJson(withDescription).run(JCalValue.structured("", description, ""));
		sensei.assertWriteJson(withData).run(JCalValue.structured("", "", data));
		sensei.assertWriteJson(empty).run((String) null);
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(JCalValue.structured(code, description, data)).run(is(withAll));
		sensei.assertParseJson(JCalValue.structured(code, description)).run(is(withCodeDescription));
		sensei.assertParseJson(JCalValue.structured(code, description, "")).run(is(withCodeDescription));
		sensei.assertParseJson(JCalValue.structured(code, "", data)).run(is(withCodeData));
		sensei.assertParseJson(JCalValue.structured("", description, data)).run(is(withDescriptionData));
		sensei.assertParseJson(JCalValue.structured(code)).run(is(withCode));
		sensei.assertParseJson(JCalValue.structured(code, "")).run(is(withCode));
		sensei.assertParseJson(JCalValue.structured(code, "", "")).run(is(withCode));
		sensei.assertParseJson(JCalValue.structured("", description)).run(is(withDescription));
		sensei.assertParseJson(JCalValue.structured("", description, "")).run(is(withDescription));
		sensei.assertParseJson(JCalValue.structured("", "", data)).run(is(withData));
		sensei.assertParseJson("").run(is(empty));
	}

	private Check<RequestStatus> is(final RequestStatus expected) {
		return new Check<RequestStatus>() {
			public void check(RequestStatus actual, ParseContext context) {
				assertEquals(expected.getStatusCode(), actual.getStatusCode());
				assertEquals(expected.getDescription(), actual.getDescription());
				assertEquals(expected.getExceptionText(), actual.getExceptionText());
			}
		};
	}
}
