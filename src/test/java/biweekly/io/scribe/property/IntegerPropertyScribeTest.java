package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import biweekly.io.ParseContext;
import biweekly.io.json.JCalValue;
import biweekly.io.json.JsonValue;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.IntegerProperty;

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
public class IntegerPropertyScribeTest extends ScribeTest<IntegerProperty> {
	private final IntegerProperty withValue = new IntegerProperty(5);
	private final IntegerProperty empty = new IntegerProperty((Integer) null);

	public IntegerPropertyScribeTest() {
		super(new IntegerPropertyMarshallerImpl());
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).run("5");
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("5").run(has(5));
		sensei.assertParseText("invalid").cannotParse(24);
		sensei.assertParseText("").run(has(null));
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withValue).run("<integer>5</integer>");
		sensei.assertWriteXml(empty).run("<integer/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<integer>5</integer>").run(has(5));
		sensei.assertParseXml("<integer>invalid</integer>").cannotParse(24);
		sensei.assertParseXml("").cannotParse(23);
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withValue).run(new JCalValue(Arrays.asList(new JsonValue(5))));
		sensei.assertWriteJson(empty).run((String) null);
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(new JCalValue(Arrays.asList(new JsonValue(5)))).run(has(5));
		sensei.assertParseJson("5").run(has(5));
		sensei.assertParseJson("invalid").cannotParse(24);
		sensei.assertParseJson("").run(has(null));
	}

	public static class IntegerPropertyMarshallerImpl extends IntegerPropertyScribe<IntegerProperty> {
		public IntegerPropertyMarshallerImpl() {
			super(IntegerProperty.class, "INT");
		}

		@Override
		protected IntegerProperty newInstance(Integer value) {
			return new IntegerProperty(value);
		}
	}

	private Check<IntegerProperty> has(final Integer value) {
		return new Check<IntegerProperty>() {
			public void check(IntegerProperty actual, ParseContext context) {
				assertEquals(value, actual.getValue());
			}
		};
	}
}
