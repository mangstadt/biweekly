package biweekly.property;

import static biweekly.util.TestUtils.assertEqualsAndHash;
import static biweekly.util.TestUtils.assertEqualsMethodEssentials;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import org.junit.Test;

import biweekly.parameter.ICalParameters;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
public class ICalPropertyTest {
	@Test
	public void copy() {
		CopyConstructorTest property = new CopyConstructorTest("value");
		property.getParameters().setLanguage("en-us");
		CopyConstructorTest copy = (CopyConstructorTest) property.copy();

		assertNotSame(property, copy);
		assertEquals(property.value, copy.value);

		assertNotSame(property.getParameters(), copy.getParameters());
		assertEquals(property.getParameters(), copy.getParameters());
	}

	@Test
	public void copy_constructor_throws_exception() {
		RuntimeException exception = new RuntimeException();
		CopyConstructorThrowsExceptionTest property = new CopyConstructorThrowsExceptionTest(exception);
		try {
			property.copy();
			fail("Expected an exception to be thrown.");
		} catch (UnsupportedOperationException e) {
			assertTrue(e.getCause() instanceof InvocationTargetException);
			assertSame(e.getCause().getCause(), exception);
		}
	}

	@Test
	public void copy_no_copy_constructor() {
		ICalPropertyImpl property = new ICalPropertyImpl();
		try {
			property.copy();
			fail("Expected an exception to be thrown.");
		} catch (UnsupportedOperationException e) {
			assertTrue(e.getCause() instanceof NoSuchMethodException);
		}
	}

	@Test
	public void equals_essentials() {
		ICalPropertyImpl one = new ICalPropertyImpl();
		assertEqualsMethodEssentials(one);
	}

	@Test
	public void equals_different_parameters() {
		ICalPropertyImpl one = new ICalPropertyImpl();
		one.addParameter("one", "value");
		ICalPropertyImpl two = new ICalPropertyImpl();
		two.addParameter("two", "value");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals() {
		ICalPropertyImpl one = new ICalPropertyImpl();
		one.addParameter("one", "value");
		ICalPropertyImpl two = new ICalPropertyImpl();
		two.addParameter("one", "value");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void parameters() {
		ICalPropertyImpl property = new ICalPropertyImpl();
		assertEquals(new ICalParameters(), property.getParameters());

		try {
			property.setParameters(null);
			fail("NPE expected.");
		} catch (NullPointerException e) {
			//expected
		}

		ICalParameters parameters = new ICalParameters();
		property.setParameters(parameters);
		assertSame(parameters, property.getParameters());

		property.addParameter("PARAM", "value");
		property.addParameter("PARAM", "value2");
		assertEquals("value", property.getParameter("PARAM"));
		assertEquals(Arrays.asList("value", "value2"), property.getParameters("PARAM"));
		ICalParameters expected = new ICalParameters();
		expected.put("PARAM", "value");
		expected.put("PARAM", "value2");
		assertEquals(expected, property.getParameters());

		property.setParameter("PARAM", "one");
		assertEquals("one", property.getParameter("PARAM"));
		assertEquals(Arrays.asList("one"), property.getParameters("PARAM"));
		expected = new ICalParameters();
		expected.put("PARAM", "one");
		assertEquals(expected, property.getParameters());

		property.removeParameter("PARAM");
		assertNull(property.getParameter("PARAM"));
		assertEquals(Arrays.asList(), property.getParameters("PARAM"));
		expected = new ICalParameters();
		assertEquals(expected, property.getParameters());
	}

	private static class CopyConstructorTest extends ICalProperty {
		private String value;

		public CopyConstructorTest(String value) {
			this.value = value;
		}

		@SuppressWarnings("unused")
		public CopyConstructorTest(CopyConstructorTest original) {
			super(original);
			value = original.value;
		}
	}

	private static class CopyConstructorThrowsExceptionTest extends ICalProperty {
		private final RuntimeException e;

		public CopyConstructorThrowsExceptionTest(RuntimeException e) {
			this.e = e;
		}

		@SuppressWarnings("unused")
		public CopyConstructorThrowsExceptionTest(CopyConstructorThrowsExceptionTest original) {
			throw original.e;
		}
	}

	private static class ICalPropertyImpl extends ICalProperty {
		//empty
	}
}
