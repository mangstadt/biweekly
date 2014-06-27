package biweekly.util;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import biweekly.ICalVersion;
import biweekly.ValidationWarnings.WarningsGroup;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.ICalPropertyScribe.Result;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;

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
 * Utility classes for unit tests.
 * @author Michael Angstadt
 */
public class TestUtils {
	/**
	 * Asserts that a warnings list is a certain size.
	 * @param expectedSize the expected size of the warnings list
	 * @param warnings the warnings list
	 */
	public static void assertWarnings(int expectedSize, List<?> warnings) {
		assertEquals(warnings.toString(), expectedSize, warnings.size());
	}

	/**
	 * Asserts the sizes of each warnings list within a list of warnings lists.
	 * @param warningsLists the list of warnings lists
	 * @param expectedSizes the expected sizes of each warnings list
	 */
	public static void assertWarningsLists(List<List<String>> warningsLists, int... expectedSizes) {
		assertEquals(warningsLists.toString(), expectedSizes.length, warningsLists.size());

		for (int i = 0; i < expectedSizes.length; i++) {
			int expectedSize = expectedSizes[i];
			List<String> warnings = warningsLists.get(i);

			assertWarnings(expectedSize, warnings);
		}
	}

	/**
	 * Asserts that a string matches a regular expression.
	 * @param regex the regular expression
	 * @param string the string
	 */
	public static void assertRegex(String regex, String string) {
		Pattern p = Pattern.compile(regex);
		assertTrue(string, p.matcher(string).matches());
	}

	/**
	 * Asserts the value of a {@link Date} object.
	 * @param expected the expected value of the date, in string form (e.g.
	 * "20130610T102301")
	 * @param actual the actual date object
	 */
	public static void assertDateEquals(String expected, Date actual) {
		if (expected.contains("Z")) {
			expected = expected.replace("Z", "+0000");
		}

		SimpleDateFormat df;
		if (ICalDateFormat.dateHasTime(expected)) {
			if (expected.contains("-") || expected.contains("+")) {
				df = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
			} else {
				df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
			}
		} else {
			df = new SimpleDateFormat("yyyyMMdd");
		}

		try {
			assertEquals(df.parse(expected), actual);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Asserts the value of an {@link Integer} object.
	 * @param expected the expected value
	 * @param actual the actual value
	 */
	public static void assertIntEquals(int expected, Integer actual) {
		assertEquals(Integer.valueOf(expected), actual);
	}

	/**
	 * Asserts that a component has a specific number of sub-components and
	 * properties.
	 * @param component the component to check
	 * @param subComponents the expected number of sub-components
	 * @param properties the expected number of properties
	 */
	public static void assertSize(ICalComponent component, int subComponents, int properties) {
		assertEquals(subComponents, component.getComponents().size());
		assertEquals(properties, component.getProperties().size());
	}

	/**
	 * Builds a timezone object with the given offset.
	 * @param hours the hour offset
	 * @param minutes the minute offset
	 * @return the timezone object
	 */
	public static TimeZone buildTimezone(int hours, int minutes) {
		int hourMillis = 1000 * 60 * 60 * hours;

		int minuteMillis = 1000 * 60 * minutes;
		if (hours < 0) {
			minuteMillis *= -1;
		}

		return new SimpleTimeZone(hourMillis + minuteMillis, "");
	}

	/**
	 * Builds an XML document that contains an empty xCal property element.
	 * @param marshaller the property marshaller
	 * @return the document
	 */
	public static Document xcalProperty(ICalPropertyScribe<? extends ICalProperty> marshaller) {
		QName qname = marshaller.getQName();
		Document document = XmlUtils.createDocument();
		Element element = document.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
		document.appendChild(element);
		return document;
	}

	/**
	 * Builds an XML document that contains a xCal property element.
	 * @param marshaller the property marshaller
	 * @param body the XML of the element body
	 * @return the document
	 */
	public static Document xcalProperty(ICalPropertyScribe<? extends ICalProperty> marshaller, String body) {
		QName qname = marshaller.getQName();
		try {
			return XmlUtils.toDocument("<" + qname.getLocalPart() + " xmlns=\"" + qname.getNamespaceURI() + "\">" + body + "</" + qname.getLocalPart() + ">");
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Tests to see if an xCal property was marshalled correctly.
	 * @param expectedInnerXml the expected inner XML of the property element
	 * @param propertyToWrite the property to marshal
	 * @param marshaller the marshaller object
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void assertWriteXml(String expectedInnerXml, ICalProperty propertyToWrite, ICalPropertyScribe marshaller) {
		Document actual = xcalProperty(marshaller);
		marshaller.writeXml(propertyToWrite, XmlUtils.getRootElement(actual));

		Document expected = xcalProperty(marshaller, expectedInnerXml);
		assertXMLEqual(expected, actual);
	}

	/**
	 * Unmarshals an xCal property element.
	 * @param <T> the property class
	 * @param innerXml the inner XML of the property element
	 * @param marshaller the marshaller object
	 * @return the unmarshal result
	 */
	public static <T extends ICalProperty> Result<T> parseXCalProperty(String innerXml, ICalPropertyScribe<T> marshaller) {
		Document document = xcalProperty(marshaller, innerXml);
		Element element = XmlUtils.getRootElement(document);
		return marshaller.parseXml(element, new ICalParameters());
	}

	//@formatter:off
	private static DateFormat dfs[] = new DateFormat[]{
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z"),
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
		new SimpleDateFormat("yyyy-MM-dd")
	};
	//@formatter:on

	/**
	 * Creates a {@link Date} object.
	 * @param text the date string (e.g. "2000-01-30", see code for acceptable
	 * formats)
	 * @return the parsed date or null if it couldn't be parsed
	 */
	public static Date date(String text) {
		for (DateFormat df : dfs) {
			try {
				return df.parse(text);
			} catch (ParseException e) {
				//try the next date formatter
			}
		}
		return null;
	}

	/**
	 * Asserts the validation of a property object.
	 * @param property the property object
	 * @return the validation checker object
	 */
	public static PropValidateChecker assertValidate(ICalProperty property) {
		return new PropValidateChecker(property);
	}

	public static class PropValidateChecker {
		private final ICalProperty property;
		private List<ICalComponent> components = new ArrayList<ICalComponent>();
		private ICalVersion[] versions = ICalVersion.values();

		public PropValidateChecker(ICalProperty property) {
			this.property = property;
		}

		/**
		 * Defines the parent components of this property (defaults to no
		 * parents).
		 * @param components the parent components
		 * @return this
		 */
		public PropValidateChecker parents(List<ICalComponent> components) {
			this.components = components;
			return this;
		}

		public PropValidateChecker versions(ICalVersion... versions) {
			this.versions = versions;
			return this;
		}

		/**
		 * Performs the validation check.
		 * @param expectedCodes the expected warning codes
		 */
		public void run(Integer... expectedCodes) {
			for (ICalVersion version : versions) {
				List<Warning> warnings = property.validate(components, version);
				boolean passed = checkCodes(warnings, expectedCodes);
				if (!passed) {
					fail(version.name() + ": Expected codes were " + Arrays.toString(expectedCodes) + " but were actually:\n" + warnings);
				}
			}
		}
	}

	/**
	 * Asserts the validation of a component object.
	 * @param component the component object
	 * @return the validation checker object
	 */
	public static CompValidateChecker assertValidate(ICalComponent component) {
		return new CompValidateChecker(component);
	}

	public static class CompValidateChecker {
		private final ICalComponent component;
		private List<ICalComponent> components = new ArrayList<ICalComponent>();
		private Map<ICalProperty, Integer[]> propertyWarnings = new IntegerArrayMap<ICalProperty>();
		private Map<ICalComponent, Integer[]> componentWarnings = new IntegerArrayMap<ICalComponent>();

		public CompValidateChecker(ICalComponent component) {
			this.component = component;
		}

		/**
		 * Defines the parent components of this property (defaults to no
		 * parents).
		 * @param components the parent components
		 * @return this
		 */
		public CompValidateChecker parents(ICalComponent... components) {
			this.components = Arrays.asList(components);
			return this;
		}

		/**
		 * Sets the expected warning codes for a specific property instance.
		 * @param property the property instance
		 * @param codes the expected warning codes
		 * @return this
		 */
		public CompValidateChecker warn(ICalProperty property, Integer... codes) {
			propertyWarnings.put(property, codes);
			return this;
		}

		/**
		 * Sets the expected warning codes for a specific component instance.
		 * @param component the component instance
		 * @param codes the expected warning codes
		 * @return this
		 */
		public CompValidateChecker warn(ICalComponent component, Integer... codes) {
			componentWarnings.put(component, codes);
			return this;
		}

		/**
		 * Performs the validation check.
		 * @param codes the warning codes for this component (can be empty)
		 */
		public void run(Integer... codes) {
			if (codes.length > 0) {
				warn(component, codes);
			}

			int count = 0;
			List<WarningsGroup> groups = component.validate(components, ICalVersion.V2_0);
			for (WarningsGroup group : groups) {
				List<Warning> warnings = group.getWarnings();

				ICalComponent comp = group.getComponent();
				if (comp != null) {
					Integer[] expectedCodes = componentWarnings.get(comp);
					if (expectedCodes == null) {
						failed(groups);
					}

					boolean passed = checkCodes(warnings, expectedCodes);
					if (!passed) {
						failed(groups);
					}
					count++;
					continue;
				}

				ICalProperty prop = group.getProperty();
				if (prop != null) {
					Integer[] expectedCodes = propertyWarnings.get(prop);
					if (expectedCodes == null) {
						failed(groups);
					}

					boolean passed = checkCodes(warnings, expectedCodes);
					if (!passed) {
						failed(groups);
					}
					count++;
					continue;
				}
			}

			if (count != componentWarnings.size() + propertyWarnings.size()) {
				failed(groups);
			}
		}

		private void failed(List<WarningsGroup> groups) {
			fail("Expected: Properties: " + propertyWarnings + " Components: " + componentWarnings + ", actual: " + groups);
		}
	}

	@SuppressWarnings("serial")
	private static class IntegerArrayMap<T> extends HashMap<T, Integer[]> {
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder("{");
			boolean first = true;
			for (Map.Entry<T, Integer[]> entry : entrySet()) {
				if (!first) {
					sb.append(", ");
				}
				first = false;

				sb.append(entry.getKey().getClass().getSimpleName()).append("=");
				sb.append(Arrays.toString(entry.getValue())).append(", ");
			}
			sb.append("}");

			return sb.toString();
		}
	}

	private static boolean checkCodes(List<Warning> warnings, Integer... expectedCodes) {
		if (warnings.size() != expectedCodes.length) {
			return false;
		}

		List<Integer> actualCodes = new ArrayList<Integer>(); //don't use a Set because there can be multiple warnings with the same code
		for (Warning warning : warnings) {
			actualCodes.add(warning.getCode());
		}

		for (Integer code : expectedCodes) {
			boolean found = actualCodes.remove((Object) code);
			if (!found) {
				return false;
			}
		}

		return true;
	}

	private TestUtils() {
		//hide
	}
}
