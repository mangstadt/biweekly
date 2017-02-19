package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.date;

import java.util.Date;

import org.junit.Test;

import biweekly.component.ICalComponent;
import biweekly.component.Observance;
import biweekly.property.DateStart;

/*
 Copyright (c) 2013-2017, Michael Angstadt
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
public class DateStartPropertyScribeTest extends ScribeTest<DateStart> {
	private final Date datetime = date("2013-06-11 13:43:02");
	private final String datetimeStr = "20130611T124302Z";
	private final String datetimeFloatingStr = "20130611T134302";
	private final String datetimeStrExt = "2013-06-11T12:43:02Z";
	private final String datetimeFloatingStrExt = "2013-06-11T13:43:02";

	private final DateStart withDateTime = new DateStart(datetime, true);
	private final ICalComponent parent = new Observance();

	public DateStartPropertyScribeTest() {
		super(new DateStartScribe());
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withDateTime).run(datetimeStr);
		sensei.assertWriteText(withDateTime).parent(parent).run(datetimeFloatingStr);
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withDateTime).run("<date-time>" + datetimeStrExt + "</date-time>");
		sensei.assertWriteXml(withDateTime).parent(parent).run("<date-time>" + datetimeFloatingStrExt + "</date-time>");
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withDateTime).run(datetimeStrExt);
		sensei.assertWriteJson(withDateTime).parent(parent).run(datetimeFloatingStrExt);
	}
}
