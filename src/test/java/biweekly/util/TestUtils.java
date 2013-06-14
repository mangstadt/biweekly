package biweekly.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

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
	public static void assertWarnings(int expectedSize, List<String> warnings) {
		assertEquals(warnings.toString(), expectedSize, warnings.size());
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
	 * @throws ParseException
	 */
	public static void assertDateEquals(String expected, Date actual) throws ParseException {
		if (expected.contains("Z")) {
			expected = expected.replace("Z", "+0000");
		}

		SimpleDateFormat df;
		if (expected.contains("T")) {
			if (expected.contains("-") || expected.contains("+")) {
				df = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ");
			} else {
				df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
			}
		} else {
			df = new SimpleDateFormat("yyyyMMdd");
		}

		assertEquals(df.parse(expected), actual);
	}

	/**
	 * Asserts the value of an {@link Integer} object.
	 * @param expected the expected value
	 * @param actual the actual value
	 */
	public static void assertIntEquals(int expected, Integer actual) {
		assertEquals(Integer.valueOf(expected), actual);
	}
}
