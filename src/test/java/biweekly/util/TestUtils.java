package biweekly.util;

import static biweekly.ICalVersion.V2_0;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Iterator;
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
import biweekly.ICalendar;
import biweekly.ValidationWarnings.WarningsGroup;
import biweekly.ValidationWarning;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.io.ParseWarning;
import biweekly.io.StreamReader;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.text.ICalReader;
import biweekly.property.ICalProperty;

/*
 Copyright (c) 2013-2021, Michael Angstadt
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
	 * Tests the version assigned to a {@link ICalendar} object.
	 * @param expected the expected version
	 * @param ical the iCalendar object
	 */
	public static void assertVersion(ICalVersion expected, ICalendar ical) {
		ICalVersion actual = ical.getVersion();
		assertEquals(expected, actual);
	}

	/**
	 * Asserts a list of parse warnings.
	 * @param warnings the parse warnings
	 * @param expectedCodes the expected warning codes (order does not matter,
	 * use "null" for warnings that do not have a code)
	 */
	public static void assertParseWarnings(List<ParseWarning> warnings, Integer... expectedCodes) {
		List<Integer> actualWarnings = new ArrayList<Integer>(warnings.size());
		for (ParseWarning warning : warnings) {
			actualWarnings.add(warning.getCode());
		}

		if (actualWarnings.size() != expectedCodes.length) {
			fail("Expected these warnings " + Arrays.toString(expectedCodes) + ", but was this: " + actualWarnings + ".  Actual warnings: " + warnings);
		}

		List<Integer> expectedWarnings = new ArrayList<Integer>(Arrays.asList(expectedCodes));
		for (Integer actualWarning : actualWarnings) {
			if (!expectedWarnings.remove(actualWarning)) {
				fail("Expected these warnings " + Arrays.toString(expectedCodes) + ", but was this: " + actualWarnings + ".  Actual warnings: " + warnings);
			}
		}
	}

	/**
	 * Asserts the parse warnings of a {@link StreamReader}.
	 * @param reader the {@link StreamReader}
	 * @param expectedCodes the expected warning codes (order does not matter,
	 * use "null" for warnings that do not have a code)
	 */
	public static void assertParseWarnings(StreamReader reader, Integer... expectedCodes) {
		assertParseWarnings(reader.getWarnings(), expectedCodes);
	}

	/**
	 * Asserts that a list is a certain size.
	 * @param expectedSize the expected size of the list
	 * @param list the list
	 */
	public static void assertListSize(int expectedSize, List<?> list) {
		int actualSize = list.size();
		assertEquals(list.toString(), expectedSize, actualSize);
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
	 * Asserts the value of an {@link Integer} object.
	 * @param expected the expected value
	 * @param actual the actual value
	 */
	public static void assertIntEquals(int expected, Integer actual) {
		assertEquals(Integer.valueOf(expected), actual);
	}

	/**
	 * Asserts the contents of a collection. Does not check for order.
	 * @param actual the actual collection
	 * @param expectedElements the elements that are expected to be in the
	 * collection (order does not matter)
	 */
	public static <T> void assertCollectionContains(Collection<T> actual, T... expectedElements) {
		assertEquals(expectedElements.length, actual.size());

		Collection<T> actualCopy = new ArrayList<T>(actual);
		for (T expectedElement : expectedElements) {
			assertTrue("Collection did not contain: " + expectedElement, actualCopy.remove(expectedElement));
		}
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
	 * Asserts the contents of an iterator.
	 * @param expected the expected contents of the iterator
	 * @param it the iterator
	 */
	public static <T> void assertIterator(List<T> expected, Iterator<T> it) {
		assertIterator(expected, it, true);
	}

	/**
	 * Asserts the contents of an iterator.
	 * @param expected the expected contents of the iterator
	 * @param it the iterator
	 * @param terminating true to keep pulling items off the iterator until it
	 * runs out of items, false to stop pulling items off the iterator when the
	 * number of items retrieved equals the length of the expected list
	 */
	public static <T> void assertIterator(List<T> expected, Iterator<T> it, boolean terminating) {
		List<T> actual = new ArrayList<T>();
		while (it.hasNext() && (terminating || actual.size() < expected.size())) {
			actual.add(it.next());
		}
		assertEquals(expected, actual);
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
		marshaller.writeXml(propertyToWrite, actual.getDocumentElement(), new WriteContext(V2_0, new TimezoneInfo(), null));

		Document expected = xcalProperty(marshaller, expectedInnerXml);
		assertXMLEqual(expected, actual);
	}

	public static <T> T[] each(T... values) {
		return values;
	}

	//@formatter:off
	private static DateFormat dfs[] = new DateFormat[]{
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z"),
		new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
		new SimpleDateFormat("yyyy-MM-dd")
	};
	//@formatter:on

	/**
	 * <p>
	 * Creates a {@link Date} object.
	 * </p>
	 * <p>
	 * The following date string formats are accepted.
	 * </p>
	 * <ul>
	 * <li>yyyy-MM-dd</li>
	 * <li>yyyy-MM-dd HH:mm:ss</li>
	 * <li>yyyy-MM-dd HH:mm:ss Z</li>
	 * </ul>
	 * <p>
	 * If no UTC offset is specified, the default timezone will be used.
	 * </p>
	 * @param text the date string to parse
	 * @return the parsed date
	 * @throws IllegalArgumentException if it couldn't be parsed
	 */
	public static Date date(String text) {
		return date(text, TimeZone.getDefault());
	}

	/**
	 * <p>
	 * Creates a {@link Date} object.
	 * </p>
	 * <p>
	 * The following date string formats are accepted.
	 * </p>
	 * <ul>
	 * <li>yyyy-MM-dd</li>
	 * <li>yyyy-MM-dd HH:mm:ss</li>
	 * <li>yyyy-MM-dd HH:mm:ss Z</li>
	 * </ul>
	 * @param text the date string
	 * @param timezone the timezone the date string is in (ignored if the date
	 * string contains a UTC offset)
	 * @return the parsed date
	 * @throws IllegalArgumentException if it couldn't be parsed
	 */
	public static Date date(String text, TimeZone timezone) {
		for (DateFormat df : dfs) {
			try {
				df.setTimeZone(timezone);
				return df.parse(text);
			} catch (ParseException e) {
				//try the next date formatter
			}
		}
		throw new IllegalArgumentException("Invalid date string: " + text);
	}

	public static ICalDate icalDate(String text) {
		DateTimeComponents components = DateTimeComponents.parse(text);
		Date date = components.toDate();
		boolean hasTime = components.hasTime();
		return new ICalDate(date, components, hasTime);
	}

	/**
	 * Creates a {@link Date} object.
	 * @param text the date string (e.g. "2000-01-30 02:21:00", see code for
	 * acceptable formats)
	 * @return the parsed date in the UTC timezone or null if it couldn't be
	 * parsed
	 * @throws IllegalArgumentException if it couldn't be parsed
	 */
	public static Date utc(String text) {
		return date(text + " +0000");
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
				List<ValidationWarning> warnings = property.validate(components, version);
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
		private ICalVersion versions[] = ICalVersion.values();

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

		public CompValidateChecker versions(ICalVersion... versions) {
			this.versions = versions;
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

			for (ICalVersion version : versions) {
				int count = 0;
				List<WarningsGroup> groups = component.validate(components, version);
				for (WarningsGroup group : groups) {
					List<ValidationWarning> warnings = group.getWarnings();

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
		}

		private void failed(List<WarningsGroup> groups) {
			fail("Expected: Properties: " + propertyWarnings + " Components: " + componentWarnings + ", actual: " + groups);
		}
	}

	@SuppressWarnings("serial")
	private static class IntegerArrayMap<T> extends IdentityHashMap<T, Integer[]> {
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

	public static boolean checkCodes(List<ValidationWarning> warnings, Integer... expectedCodes) {
		if (warnings.size() != expectedCodes.length) {
			return false;
		}

		List<Integer> actualCodes = new ArrayList<Integer>(); //don't use a Set because there can be multiple warnings with the same code
		for (ValidationWarning warning : warnings) {
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

	/**
	 * <p>
	 * Asserts some of the basic rules for the equals() method:
	 * </p>
	 * <ul>
	 * <li>The same object instance is equal to itself.</li>
	 * <li>Passing {@code null} into the method returns false.</li>
	 * <li>Passing an instance of a different class into the method returns
	 * false.</li>
	 * </ul>
	 * @param object an instance of the class to test.
	 */
	public static void assertEqualsMethodEssentials(Object object) {
		assertEquals(object, object);
		assertFalse(object.equals(null));
		assertFalse(object.equals("other class"));
	}

	/**
	 * Asserts that two objects are equal according to their equals() method.
	 * Also asserts that their hash codes are the same.
	 * @param one the first object
	 * @param two the second object
	 */
	public static void assertEqualsAndHash(Object one, Object two) {
		assertEquals(one, two);
		assertEquals(two, one);
		assertEquals(one.hashCode(), two.hashCode());
	}

	/**
	 * Asserts that calling {@code one.equals(two)} and {@code two.equals(one)}
	 * will both return false.
	 * @param one the first object
	 * @param two the second object
	 */
	public static void assertNotEqualsBothWays(Object one, Object two) {
		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	/**
	 * Asserts that none of the given objects are equal to each other.
	 * @param objects the objects
	 */
	public static void assertNothingIsEqual(Object... objects) {
		assertNothingIsEqual(Arrays.asList(objects));
	}

	/**
	 * Asserts that none of the given objects are equal to each other.
	 * @param objects the objects
	 */
	public static void assertNothingIsEqual(Iterable<Object> objects) {
		for (Object object1 : objects) {
			for (Object object2 : objects) {
				if (object1 != object2) {
					assertNotEquals("Objects should not be equal:\n" + object1 + "\n" + object2, object1, object2);
				}
			}
		}
	}

	/**
	 * Gets a VTIMEZONE component for the "America/New_York" timezone. Timezone
	 * definition downloaded from tzurl.org.
	 * @return the component
	 */
	public static VTimezone vtimezoneNewYork() {
		ICalReader reader = new ICalReader(TestUtils.class.getResourceAsStream("New_York.ics"));
		ICalendar ical;
		try {
			ical = reader.readNext();
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		return ical.getTimezoneInfo().getComponents().iterator().next();
	}

	private TestUtils() {
		//hide
	}
}
