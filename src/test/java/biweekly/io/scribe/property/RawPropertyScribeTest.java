package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.ParseContext;
import biweekly.io.json.JCalValue;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.RawProperty;

/*
 Copyright (c) 2013-2023, Michael Angstadt
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
public class RawPropertyScribeTest extends ScribeTest<RawProperty> {
	private final RawProperty withValue = new RawProperty("RAW", "value");
	private final RawProperty withEscapedChar = new RawProperty("RAW", "one\\,two");
	private final RawProperty empty = new RawProperty("RAW", null);

	public RawPropertyScribeTest() {
		super(new RawPropertyScribe("RAW"));
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).run("value");
		sensei.assertWriteText(withEscapedChar).run("one\\,two");
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("value").run(has("RAW", "value", null));
		sensei.assertParseText("value").dataType(ICalDataType.TEXT).run(has("RAW", "value", ICalDataType.TEXT));
		sensei.assertParseText("one\\,two").run(has("RAW", "one\\,two", null));
		sensei.assertParseText("one\\,two").dataType(ICalDataType.TEXT).run(has("RAW", "one\\,two", ICalDataType.TEXT));
		sensei.assertParseText("").run(has("RAW", "", null));
		sensei.assertParseText("").dataType(ICalDataType.TEXT).run(has("RAW", "", ICalDataType.TEXT));
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<text>one\\,two</text>").run(has("RAW", "one\\,two", ICalDataType.TEXT));
	}

	@Test
	public void parseJson() {
		JCalValue value = JCalValue.single("one\\,two");
		sensei.assertParseJson(value).dataType(ICalDataType.TEXT).run(has("RAW", "one\\,two", ICalDataType.TEXT));

		value = JCalValue.multi("one", "two,three");
		sensei.assertParseJson(value).dataType(ICalDataType.TEXT).run(has("RAW", "one,two\\,three", ICalDataType.TEXT));

		value = JCalValue.structured(Arrays.asList("one", "two"), Arrays.asList("three,four"));
		sensei.assertParseJson(value).dataType(ICalDataType.TEXT).run(has("RAW", "one,two;three\\,four", ICalDataType.TEXT));
	}

	private Check<RawProperty> has(final String name, final String value, final ICalDataType dataType) {
		return new Check<RawProperty>() {
			public void check(RawProperty property, ParseContext context) {
				assertEquals(name, property.getName());
				assertEquals(value, property.getValue());
				assertEquals(dataType, property.getDataType());
			}
		};
	}
}
