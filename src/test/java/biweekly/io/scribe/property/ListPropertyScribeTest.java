package biweekly.io.scribe.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.scribe.property.ListPropertyScribeTest.ListPropertyImpl;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.parameter.ICalParameters;
import biweekly.property.ListProperty;

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
public class ListPropertyScribeTest extends ScribeTest<ListPropertyImpl> {
	public ListPropertyScribeTest() {
		super(new ListPropertyMarshallerImpl());
	}

	private final ListPropertyImpl withMultiple = new ListPropertyImpl();
	{
		withMultiple.getValues().addAll(Arrays.asList("one", "two", "three,four"));
	}
	private final ListPropertyImpl withSingle = new ListPropertyImpl();
	{
		withSingle.getValues().add("one");
	}
	private final ListPropertyImpl empty = new ListPropertyImpl();

	@Test
	public void writeText() {
		sensei.assertWriteText(withMultiple).version(V1_0).run("one;two;three\\,four");
		sensei.assertWriteText(withMultiple).version(V2_0_DEPRECATED).run("one,two,three\\,four");
		sensei.assertWriteText(withMultiple).version(V2_0).run("one,two,three\\,four");

		sensei.assertWriteText(withSingle).run("one");
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("one;two;three\\,four").versions(V1_0).run(is(withMultiple));
		sensei.assertParseText("one,two,three\\,four").versions(V2_0_DEPRECATED, V2_0).run(is(withMultiple));

		sensei.assertParseText("one").run(is(withSingle));
		sensei.assertParseText("").run(is(empty));
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withMultiple).run("<text>one</text><text>two</text><text>three,four</text>");
		sensei.assertWriteXml(withSingle).run("<text>one</text>");
		sensei.assertWriteXml(empty).run("");
	}

	@Test
	public void writeXml_data_type() {
		ListPropertyMarshallerImpl marshaller = new ListPropertyMarshallerImpl(ICalDataType.INTEGER);
		Sensei<ListPropertyImpl> sensei = new Sensei<ListPropertyImpl>(marshaller);

		sensei.assertWriteXml(withMultiple).run("<integer>one</integer><integer>two</integer><integer>three,four</integer>");
		sensei.assertWriteXml(withSingle).run("<integer>one</integer>");
		sensei.assertWriteXml(empty).run("");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<text>one</text><text>two</text><float>2.5</float><text>three,four</text>").run(is(withMultiple));
		sensei.assertParseXml("<text>one</text>").run(is(withSingle));
		sensei.assertParseXml("<float>2.5</float><text>one</text>").run(is(withSingle));
		sensei.assertParseXml("<float>2.5</float>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void parseXml_data_type() {
		ListPropertyMarshallerImpl marshaller = new ListPropertyMarshallerImpl(ICalDataType.INTEGER);
		Sensei<ListPropertyImpl> sensei = new Sensei<ListPropertyImpl>(marshaller);

		sensei.assertParseXml("<integer>one</integer><integer>two</integer><float>2.5</float><integer>three,four</integer>").run(is(withMultiple));
		sensei.assertParseXml("<integer>one</integer>").run(is(withSingle));
		sensei.assertParseXml("<float>2.5</float><integer>one</integer>").run(is(withSingle));
		sensei.assertParseXml("<float>2.5</float>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withMultiple).run(JCalValue.multi("one", "two", "three,four"));
		sensei.assertWriteJson(withSingle).run("one");
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson(JCalValue.multi("one", "two", "three,four")).run(is(withMultiple));
		sensei.assertParseJson("one").run(is(withSingle));
		sensei.assertParseJson("").run(has(""));
	}

	public static class ListPropertyMarshallerImpl extends ListPropertyScribe<ListPropertyImpl, String> {
		public ListPropertyMarshallerImpl() {
			super(ListPropertyImpl.class, "LIST");
		}

		public ListPropertyMarshallerImpl(ICalDataType dataType) {
			super(ListPropertyImpl.class, "LIST", dataType);
		}

		@Override
		protected ListPropertyImpl newInstance(ICalDataType dataType, ICalParameters parameters) {
			return new ListPropertyImpl();
		}

		@Override
		protected String writeValue(ListPropertyImpl property, String value, WriteContext context) {
			return value;
		}

		@Override
		protected String readValue(ListPropertyImpl property, String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return value;
		}
	}

	public static class ListPropertyImpl extends ListProperty<String> {
		private static final long serialVersionUID = 1L;
		//empty
	}

	private Check<ListPropertyImpl> is(final ListPropertyImpl expected) {
		return new Check<ListPropertyImpl>() {
			public void check(ListPropertyImpl actual, ParseContext context) {
				assertEquals(expected.getValues(), actual.getValues());
			}
		};
	}

	private Check<ListPropertyImpl> has(final String... values) {
		return new Check<ListPropertyImpl>() {
			public void check(ListPropertyImpl actual, ParseContext context) {
				assertEquals(Arrays.asList(values), actual.getValues());
			}
		};
	}
}
