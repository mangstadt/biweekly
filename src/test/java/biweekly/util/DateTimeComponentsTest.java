package biweekly.util;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import biweekly.util.TestUtils.Tests;

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
public class DateTimeComponentsTest {
	@Test
	public void parse() {
		Tests tests = new Tests();
		tests.add(new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), "20130722T150630");
		tests.add(new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), "2013-07-22T15:06:30");
		tests.add(new DateTimeComponents(2013, 7, 22, 15, 6, 30, true), "20130722T150630Z");
		tests.add(new DateTimeComponents(2013, 7, 22, 15, 6, 30, true), "2013-07-22T15:06:30Z");
		tests.add(new DateTimeComponents(2013, 7, 22, 0, 0, 0, false), "20130722");
		tests.add(new DateTimeComponents(2013, 7, 22, 0, 0, 0, false), "2013-07-22");

		for (Object[] test : tests) {
			DateTimeComponents expected = (DateTimeComponents) test[0];
			String input = (String) test[1];

			DateTimeComponents actual = DateTimeComponents.parse(input);
			assertEquals(expected, actual);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		DateTimeComponents.parse("invalid");
	}

	@Test
	public void toString_() {
		Tests tests = new Tests();
		tests.add("20130722T150630", "2013-07-22T15:06:30", new DateTimeComponents(2013, 7, 22, 15, 6, 30, false));
		tests.add("20130722T150630Z", "2013-07-22T15:06:30Z", new DateTimeComponents(2013, 7, 22, 15, 6, 30, true));
		tests.add("20130722T000000", "2013-07-22T00:00:00", new DateTimeComponents(2013, 7, 22, 0, 0, 0, false));

		for (Object[] test : tests) {
			String expectedBasic = (String) test[0];
			String expectedExtended = (String) test[1];
			DateTimeComponents components = (DateTimeComponents) test[2];

			assertEquals(expectedBasic, components.toString(false));
			assertEquals(expectedExtended, components.toString(true));
		}
	}

	@Test
	public void toDate() throws Throwable {
		Tests tests = new Tests();
		tests.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-07-22 15:06:30"), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false));
		tests.add(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse("2013-07-22 15:06:30 +0000"), new DateTimeComponents(2013, 7, 22, 15, 6, 30, true));

		for (Object[] test : tests) {
			Date expected = (Date) test[0];
			DateTimeComponents components = (DateTimeComponents) test[1];

			assertEquals(expected, components.toDate());
		}
	}

	@Test
	public void copy() {
		Tests tests = new Tests();
		tests.add(new DateTimeComponents(2014, 7, 22, 15, 6, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), 2014, null, null, null, null, null, null);
		tests.add(new DateTimeComponents(2013, 8, 22, 15, 6, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, 8, null, null, null, null, null);
		tests.add(new DateTimeComponents(2013, 7, 23, 15, 6, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, 23, null, null, null, null);
		tests.add(new DateTimeComponents(2013, 7, 22, 16, 6, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, null, 16, null, null, null);
		tests.add(new DateTimeComponents(2013, 7, 22, 15, 7, 30, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, null, null, 7, null, null);
		tests.add(new DateTimeComponents(2013, 7, 22, 15, 6, 31, false), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, null, null, null, 31, null);
		tests.add(new DateTimeComponents(2013, 7, 22, 15, 6, 30, true), new DateTimeComponents(2013, 7, 22, 15, 6, 30, false), null, null, null, null, null, null, true);

		for (Object[] test : tests) {
			DateTimeComponents expected = (DateTimeComponents) test[0];
			DateTimeComponents original = (DateTimeComponents) test[1];

			int i = 2;
			assertEquals(expected, new DateTimeComponents(original, (Integer) test[i++], (Integer) test[i++], (Integer) test[i++], (Integer) test[i++], (Integer) test[i++], (Integer) test[i++], (Boolean) test[i++]));
		}
	}
}
