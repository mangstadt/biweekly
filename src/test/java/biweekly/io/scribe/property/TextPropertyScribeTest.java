package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.scribe.property.TextPropertyScribe;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.TextProperty;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
public class TextPropertyScribeTest {
	private final TextPropertyMarshallerImpl marshaller = new TextPropertyMarshallerImpl();
	private final Sensei<TextPropertyImpl> sensei = new Sensei<TextPropertyImpl>(marshaller);

	private final String value = "the;text";
	private final TextPropertyImpl withValue = new TextPropertyImpl(value);
	private final TextPropertyImpl empty = new TextPropertyImpl(null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).run("the\\;text");
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("the\\;text").run(has(value));
		sensei.assertParseText(value).run(has(value));
		sensei.assertParseText("").run(has(""));
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withValue).run("<text>" + value + "</text>");
		sensei.assertWriteXml(empty).run("<text/>");
	}

	@Test
	public void writeXml_data_type() {
		TextPropertyMarshallerImpl marshaller = new TextPropertyMarshallerImpl(ICalDataType.CAL_ADDRESS);
		Sensei<TextPropertyImpl> sensei = new Sensei<TextPropertyImpl>(marshaller);

		sensei.assertWriteXml(withValue).run("<cal-address>" + value + "</cal-address>");
		sensei.assertWriteXml(empty).run("<cal-address/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<text>" + value + "</text>").run(has(value));
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseXml_data_type() {
		TextPropertyMarshallerImpl marshaller = new TextPropertyMarshallerImpl(ICalDataType.CAL_ADDRESS);
		Sensei<TextPropertyImpl> sensei = new Sensei<TextPropertyImpl>(marshaller);

		sensei.assertParseXml("<cal-address>" + value + "</cal-address>").run(has(value));
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withValue).run(value);
		sensei.assertWriteJson(empty).run((String) null);
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(value).run(has(value));
		sensei.assertParseJson("").run(has(""));
	}

	private class TextPropertyMarshallerImpl extends TextPropertyScribe<TextPropertyImpl> {
		public TextPropertyMarshallerImpl() {
			super(TextPropertyImpl.class, "TEXT");
		}

		public TextPropertyMarshallerImpl(ICalDataType dataType) {
			super(TextPropertyImpl.class, "TEXT", dataType);
		}

		@Override
		protected TextPropertyImpl newInstance(String value, ICalVersion version) {
			return new TextPropertyImpl(value);
		}
	}

	private class TextPropertyImpl extends TextProperty {
		public TextPropertyImpl(String value) {
			super(value);
		}
	}

	private Check<TextPropertyImpl> has(final String expected) {
		return new Check<TextPropertyImpl>() {
			public void check(TextPropertyImpl actual) {
				assertEquals(expected, actual.getValue());
			}
		};
	}
}
