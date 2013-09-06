package biweekly.property.marshaller;

import static biweekly.util.TestUtils.assertWarnings;
import static biweekly.util.TestUtils.assertWriteXml;
import static biweekly.util.TestUtils.parseXCalProperty;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.CannotParseException;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.ListProperty;
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
public class ListPropertyMarshallerTest {
	private final ListPropertyMarshallerImpl marshaller = new ListPropertyMarshallerImpl();

	@Test
	public void writeText_multiple() {
		ListPropertyImpl prop = new ListPropertyImpl();
		prop.addValue("one");
		prop.addValue("two");
		prop.addValue("three,four");

		String actual = marshaller.writeText(prop);

		String expected = "one,two,three\\,four";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_single() {
		ListPropertyImpl prop = new ListPropertyImpl();
		prop.addValue("one");

		String actual = marshaller.writeText(prop);

		String expected = "one";
		assertEquals(expected, actual);
	}

	@Test
	public void writeText_empty() {
		ListPropertyImpl prop = new ListPropertyImpl();

		String actual = marshaller.writeText(prop);

		String expected = "";
		assertEquals(expected, actual);
	}

	@Test
	public void parseText() {
		String value = "one,two,three\\,four";
		ICalParameters params = new ICalParameters();

		Result<ListPropertyImpl> result = marshaller.parseText(value, ICalDataType.TEXT, params);

		assertEquals(Arrays.asList("one", "two", "three,four"), result.getProperty().getValues());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseText_empty() {
		String value = "";
		ICalParameters params = new ICalParameters();

		Result<ListPropertyImpl> result = marshaller.parseText(value, ICalDataType.TEXT, params);

		assertEquals(0, result.getProperty().getValues().size());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void writeXml() {
		ListPropertyImpl prop = new ListPropertyImpl();
		prop.addValue("one");
		prop.addValue("two");
		prop.addValue("three");

		assertWriteXml("<text>one</text><text>two</text><text>three</text>", prop, marshaller);
	}

	@Test
	public void writeXml_data_type() {
		ListPropertyMarshallerImpl marshaller = new ListPropertyMarshallerImpl(ICalDataType.INTEGER);
		ListPropertyImpl prop = new ListPropertyImpl();
		prop.addValue("1");
		prop.addValue("2");
		prop.addValue("3");

		assertWriteXml("<integer>1</integer><integer>2</integer><integer>3</integer>", prop, marshaller);
	}

	@Test
	public void parseXml() {
		Result<ListPropertyImpl> result = parseXCalProperty("<text>one</text><text>two</text><text>three</text>", marshaller);

		ListPropertyImpl prop = result.getProperty();
		assertEquals(Arrays.asList("one", "two", "three"), prop.getValues());
		assertWarnings(0, result.getWarnings());
	}

	@Test
	public void parseXml_data_type() {
		ListPropertyMarshallerImpl marshaller = new ListPropertyMarshallerImpl(ICalDataType.INTEGER);
		Result<ListPropertyImpl> result = parseXCalProperty("<integer>1</integer><integer>2</integer><text>ignore</text><integer>3</integer>", marshaller);

		ListPropertyImpl prop = result.getProperty();
		assertEquals(Arrays.asList("1", "2", "3"), prop.getValues());
		assertWarnings(0, result.getWarnings());
	}

	@Test(expected = CannotParseException.class)
	public void parseXml_empty() {
		parseXCalProperty("<integer>ignore</integer>", marshaller);
	}

	@Test
	public void writeJson() {
		ListPropertyImpl prop = new ListPropertyImpl();
		prop.addValue("one");
		prop.addValue("two");
		prop.addValue("three");

		JCalValue actual = marshaller.writeJson(prop);
		assertEquals(Arrays.asList("one", "two", "three"), actual.getMultivalued());
	}

	@Test
	public void parseJson() {
		Result<ListPropertyImpl> result = marshaller.parseJson(JCalValue.multi("one", "two", "three"), ICalDataType.TEXT, new ICalParameters());

		ListPropertyImpl prop = result.getProperty();
		assertEquals(Arrays.asList("one", "two", "three"), prop.getValues());
		assertWarnings(0, result.getWarnings());
	}

	private class ListPropertyMarshallerImpl extends ListPropertyMarshaller<ListPropertyImpl, String> {
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
		protected String writeValue(ListPropertyImpl property, String value) {
			return value;
		}

		@Override
		protected String readValue(String value, ICalDataType dataType, ICalParameters parameters, List<String> warnings) {
			return value;
		}
	}

	private class ListPropertyImpl extends ListProperty<String> {
		//empty
	}
}
