package biweekly.io.scribe.property;

import static biweekly.ICalVersion.V2_0;
import static biweekly.util.TestUtils.assertWarnings;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.util.XmlUtils;

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
 * Utility class used for unit testing property marshallers.
 * @param <T> the property class
 * @author Michael Angstadt
 */
public class Sensei<T extends ICalProperty> {
	private final ICalPropertyScribe<T> scribe;

	/**
	 * Creates a new sensei.
	 * @param scribe the property scribe
	 */
	public Sensei(ICalPropertyScribe<T> scribe) {
		this.scribe = scribe;
	}

	/**
	 * Asserts the {@link ICalPropertyScribe#parseText} method.
	 * @param value the value to parse
	 * @return the tester object
	 */
	public ParseTextTest assertParseText(String value) {
		return new ParseTextTest(value);
	}

	/**
	 * Asserts the {@link ICalPropertyScribe#parseXml} method.
	 * @param innerXml the inner XML of the xCal element to parse
	 * @return the tester object
	 */
	public ParseXmlTest assertParseXml(String innerXml) {
		return new ParseXmlTest(innerXml);
	}

	/**
	 * Asserts the {@link ICalPropertyScribe#parseJson} method.
	 * @param value the jCal value to parse
	 * @return the tester object
	 */
	public ParseJsonTest assertParseJson(String value) {
		return assertParseJson(JCalValue.single(value));
	}

	/**
	 * Asserts the {@link ICalPropertyScribe#parseJson} method.
	 * @param value the jCal value to parse
	 * @return the tester object
	 */
	public ParseJsonTest assertParseJson(JCalValue value) {
		return new ParseJsonTest(value);
	}

	/**
	 * Asserts the {@link ICalPropertyScribe#dataType} method.
	 * @param property the property to marshal
	 * @return the tester object
	 */
	public DataTypeTest assertDataType(T property) {
		return new DataTypeTest(property);
	}

	/**
	 * Asserts the {@link ICalPropertyScribe#prepareParameters} method.
	 * @param property the property to marshal
	 * @return the tester object
	 */
	public PrepareParamsTest assertPrepareParams(T property) {
		return new PrepareParamsTest(property);
	}

	/**
	 * Asserts the {@link ICalPropertyScribe#writeText} method.
	 * @param property the property to marshal
	 * @return the tester object
	 */
	public WriteTextTest assertWriteText(T property) {
		return new WriteTextTest(property);
	}

	/**
	 * Asserts the {@link ICalPropertyScribe#writeXml} method.
	 * @param property the property to marshal
	 * @return the tester object
	 */
	public WriteXmlTest assertWriteXml(T property) {
		return new WriteXmlTest(property);
	}

	/**
	 * Asserts the {@link ICalPropertyScribe#writeJson} method.
	 * @param property the property to marshal
	 * @return the tester object
	 */
	public WriteJsonTest assertWriteJson(T property) {
		return new WriteJsonTest(property);
	}

	/**
	 * Tester class used for testing the {@link ICalPropertyScribe#dataType}
	 * method.
	 */
	public class DataTypeTest {
		protected final T property;
		private ICalVersion versions[] = ICalVersion.values();

		public DataTypeTest(T property) {
			this.property = property;
		}

		public DataTypeTest versions(ICalVersion... versions) {
			this.versions = versions;
			return this;
		}

		/**
		 * Runs the test.
		 * @param expected the expected data type
		 */
		public void run(ICalDataType expected) {
			for (ICalVersion version : versions) {
				ICalDataType actual = scribe.dataType(property, version);
				assertEquals(expected, actual);
			}
		}
	}

	/**
	 * Tester class used for testing the
	 * {@link ICalPropertyScribe#prepareParameters} method.
	 */
	public class PrepareParamsTest {
		protected final T property;
		private ICalVersion versions[] = ICalVersion.values();
		private TimezoneInfo tzinfo = new TimezoneInfo();
		private ICalParameters expected = new ICalParameters();
		private ICalComponent parent;

		public PrepareParamsTest(T property) {
			this.property = property;
		}

		/**
		 * Adds an expected parameter.
		 * @param name the parameter name
		 * @param values the parameter value
		 * @return this
		 */
		public PrepareParamsTest expected(String name, String... values) {
			for (String value : values) {
				expected.put(name, value);
			}
			return this;
		}

		public PrepareParamsTest versions(ICalVersion... versions) {
			this.versions = versions;
			return this;
		}

		public PrepareParamsTest tz(TimezoneInfo tzinfo) {
			this.tzinfo = tzinfo;
			return this;
		}

		public PrepareParamsTest parent(ICalComponent parent) {
			this.parent = parent;
			return this;
		}

		/**
		 * Runs the test.
		 */
		public void run() {
			for (ICalVersion version : versions) {
				WriteContext context = new WriteContext(version, tzinfo, null, null);
				context.setParent(parent);
				ICalParameters actual = scribe.prepareParameters(property, context);
				assertEquals("Actual: " + actual, expected.size(), actual.size());

				for (Map.Entry<String, List<String>> entry : expected) {
					String expectedKey = entry.getKey();
					List<String> expectedValues = entry.getValue();

					List<String> actualValues = actual.get(expectedKey);
					assertEquals("Actual: " + actual, expectedValues.size(), actualValues.size());
					for (String expectedValue : expectedValues) {
						assertTrue("Actual: " + actual, actualValues.contains(expectedValue));
					}
				}
			}
		}
	}

	public abstract class WriteTest<U> {
		protected final T property;
		protected TimezoneInfo tzinfo = new TimezoneInfo();
		protected TimeZone globalTimeZone;
		protected VTimezone globalTimeZoneComponent;
		protected ICalComponent parent;

		@SuppressWarnings("unchecked")
		protected final U this_ = (U) this;

		public WriteTest(T property) {
			this.property = property;
		}

		public U tz(TimezoneInfo tzinfo) {
			this.tzinfo = tzinfo;
			return this_;
		}

		public U globalTz(TimeZone globalTimeZone, VTimezone globalTimeZoneComponent) {
			this.globalTimeZone = globalTimeZone;
			this.globalTimeZoneComponent = globalTimeZoneComponent;
			return this_;
		}

		public U parent(ICalComponent parent) {
			this.parent = parent;
			return this_;
		}

		public abstract void run(String expected);
	}

	/**
	 * Tester class used for testing the {@link ICalPropertyScribe#writeText}
	 * method.
	 */
	public class WriteTextTest extends WriteTest<WriteTextTest> {
		protected ICalVersion version = V2_0;

		public WriteTextTest(T property) {
			super(property);
		}

		public WriteTextTest version(ICalVersion version) {
			this.version = version;
			return this;
		}

		/**
		 * Runs the test.
		 * @return the marshalled value
		 */
		public String run() {
			WriteContext context = new WriteContext(version, tzinfo, globalTimeZone, globalTimeZoneComponent);
			context.setParent(parent);
			return scribe.writeText(property, context);
		}

		/**
		 * Runs the test.
		 * @param expected the expected property value
		 */
		@Override
		public void run(String expected) {
			String actual = run();
			assertEquals(expected, actual);
		}
	}

	/**
	 * Tester class used for testing the {@link ICalPropertyScribe#writeXml}
	 * method.
	 */
	public class WriteXmlTest extends WriteTest<WriteXmlTest> {
		public WriteXmlTest(T property) {
			super(property);
		}

		/**
		 * Runs the test.
		 * @param expectedInnerXml the expected inner XML of the xCal property
		 * element
		 */
		@Override
		public void run(String expectedInnerXml) {
			Document actual = createXCalElement();
			WriteContext context = new WriteContext(V2_0, tzinfo, globalTimeZone, globalTimeZoneComponent);
			context.setParent(parent);
			scribe.writeXml(property, actual.getDocumentElement(), context);

			Document expected = createXCalElement(expectedInnerXml);

			assertXMLEqual(XmlUtils.toString(actual), expected, actual);
		}
	}

	/**
	 * Tester class used for testing the {@link ICalPropertyScribe#writeJson}
	 * method.
	 */
	public class WriteJsonTest extends WriteTest<WriteJsonTest> {
		public WriteJsonTest(T property) {
			super(property);
		}

		/**
		 * Runs the test.
		 * @return the marshalled value
		 */
		public JCalValue run() {
			WriteContext context = new WriteContext(V2_0, tzinfo, globalTimeZone, globalTimeZoneComponent);
			context.setParent(parent);
			return scribe.writeJson(property, context);
		}

		/**
		 * Runs the test.
		 * @param expected the expected jCal value
		 */
		public void run(JCalValue expected) {
			JCalValue value = run();
			assertEquals(expected.getValues(), value.getValues());
		}

		/**
		 * Runs the test.
		 * @param expected the expected jCal value
		 */
		@Override
		public void run(String expected) {
			JCalValue value = run();
			assertEquals(1, value.getValues().size());
			assertEquals(expected, value.getValues().get(0).getValue());
		}
	}

	/**
	 * Parent class for the parser testers.
	 */
	private abstract class ParseTest<U extends ParseTest<U>> {
		protected ICalParameters parameters = new ICalParameters();
		protected int warnings = 0;

		@SuppressWarnings("unchecked")
		private final U this_ = (U) this;

		/**
		 * Adds a parameter.
		 * @param name the parameter name
		 * @param value the parameter value
		 * @return this
		 */
		public U param(String name, String value) {
			parameters.put(name, value);
			return this_;
		}

		/**
		 * Sets the parameters.
		 * @param parameters the parameters
		 * @return this
		 */
		public U params(ICalParameters parameters) {
			this.parameters = parameters;
			return this_;
		}

		/**
		 * Sets the expected number of warnings (defaults to "0").
		 * @param warnings the expected number of warnings
		 * @return this
		 */
		public U warnings(int warnings) {
			this.warnings = warnings;
			return this_;
		}

		/**
		 * Runs the test, without testing the returned property object.
		 */
		public void run() {
			run(null, null);
		}

		/**
		 * Runs the test, expecting a {@link CannotParseException} to be thrown.
		 */
		public void cannotParse() {
			run(null, CannotParseException.class);
		}

		/**
		 * Runs the test.
		 * @param check object for validating the parsed property object
		 */
		public void run(Check<T> check) {
			run(check, null);
		}

		/**
		 * Runs the test.
		 * @param check object for validating the parsed property object or null
		 * not to validate the property
		 * @param exception the exception that is expected to be thrown or null
		 * if no exception is expected
		 */
		protected abstract void run(Check<T> check, Class<? extends RuntimeException> exception);
	}

	/**
	 * Tester class used for testing the {@link ICalPropertyScribe#parseText}
	 * method.
	 */
	public class ParseTextTest extends ParseTest<ParseTextTest> {
		private final String value;
		private ICalDataType dataType = scribe.defaultDataType(V2_0);
		private ICalVersion versions[] = ICalVersion.values();

		/**
		 * @param value the text to parse
		 */
		public ParseTextTest(String value) {
			this.value = value;
		}

		/**
		 * Sets the data type (defaults to the property's default data type)
		 * @param dataType the data type
		 * @return this
		 */
		public ParseTextTest dataType(ICalDataType dataType) {
			this.dataType = dataType;
			return this;
		}

		public ParseTextTest versions(ICalVersion... versions) {
			this.versions = versions;
			return this;
		}

		@Override
		protected void run(Check<T> check, Class<? extends RuntimeException> exception) {
			for (ICalVersion version : versions) {
				ParseContext context = new ParseContext();
				context.setVersion(version);
				try {
					T property = scribe.parseText(value, dataType, new ICalParameters(parameters), context);

					if (exception != null) {
						fail("Expected " + exception.getSimpleName() + " to be thrown.");
					}
					if (check != null) {
						check.check(property, context);
					}

					assertWarnings(warnings, context.getWarnings());
				} catch (RuntimeException t) {
					if (exception == null) {
						throw t;
					}
					assertEquals(exception, t.getClass());
				}
			}
		}
	}

	/**
	 * Tester class used for testing the {@link ICalPropertyScribe#parseXml}
	 * method.
	 */
	public class ParseXmlTest extends ParseTest<ParseXmlTest> {
		private final String innerXml;

		/**
		 * @param innerXml the inner XML of the xCal property element to parse
		 */
		public ParseXmlTest(String innerXml) {
			this.innerXml = innerXml;
		}

		@Override
		protected void run(Check<T> check, Class<? extends RuntimeException> exception) {
			try {
				ParseContext context = new ParseContext();
				Document document = createXCalElement(innerXml);
				Element element = document.getDocumentElement();
				T property = scribe.parseXml(element, parameters, context);

				if (exception != null) {
					fail("Expected " + exception.getSimpleName() + " to be thrown.");
				}
				if (check != null) {
					check.check(property, context);
				}

				assertWarnings(warnings, context.getWarnings());
			} catch (RuntimeException t) {
				if (exception == null) {
					throw t;
				}
				assertEquals(exception, t.getClass());
			}
		}
	}

	/**
	 * Tester class used for testing the {@link ICalPropertyScribe#parseJson}
	 * method.
	 */
	public class ParseJsonTest extends ParseTest<ParseJsonTest> {
		private final JCalValue value;
		private ICalDataType dataType = scribe.defaultDataType(V2_0);

		/**
		 * @param value the jCal value to parse
		 */
		public ParseJsonTest(JCalValue value) {
			this.value = value;
		}

		/**
		 * Sets the data type (defaults to the property's default data type).
		 * @param dataType the data type
		 * @return this
		 */
		public ParseJsonTest dataType(ICalDataType dataType) {
			this.dataType = dataType;
			return this;
		}

		@Override
		protected void run(Check<T> check, Class<? extends RuntimeException> exception) {
			try {
				ParseContext context = new ParseContext();
				T property = scribe.parseJson(value, dataType, parameters, context);

				if (exception != null) {
					fail("Expected " + exception.getSimpleName() + " to be thrown.");
				}
				if (check != null) {
					check.check(property, context);
				}

				assertWarnings(warnings, context.getWarnings());
			} catch (RuntimeException t) {
				if (exception == null) {
					throw t;
				}
				assertEquals(exception, t.getClass());
			}
		}
	}

	/**
	 * Used for validating the contents of a parsed property object.
	 * @param <T> the property class
	 */
	public static interface Check<T extends ICalProperty> {
		/**
		 * Validates the contents of the parsed property object.
		 * @param property the parsed property object
		 * @param context the parse context
		 */
		void check(T property, ParseContext context);
	}

	private Document createXCalElement() {
		return createXCalElement("");
	}

	private Document createXCalElement(String innerXml) {
		QName qname = scribe.getQName();
		String localName = qname.getLocalPart();
		String ns = qname.getNamespaceURI();

		try {
			return XmlUtils.toDocument("<" + localName + " xmlns=\"" + ns + "\">" + innerXml + "</" + localName + ">");
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}
}
