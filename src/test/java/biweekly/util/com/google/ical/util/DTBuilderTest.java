// Copyright (C) 2006 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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

package biweekly.util.com.google.ical.util;

import junit.framework.TestCase;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class DTBuilderTest extends TestCase {
	public void testEquals() {
		assertTrue(!new DTBuilder(2006, 1, 2).equals(null));
		assertTrue(!new DTBuilder(2006, 1, 2).equals(new Object()));
		assertTrue(!new DTBuilder(2006, 1, 2).equals(new DTBuilder(2006, 1, 2).toString()));

		assertTrue(new DTBuilder(2006, 1, 2).equals(new DTBuilder(2006, 1, 2)));
		assertTrue(new DTBuilder(2006, 1, 2, 12, 30, 0).equals(new DTBuilder(2006, 1, 2, 12, 30, 0)));
		assertTrue(!new DTBuilder(2005, 1, 2, 12, 30, 0).equals(new DTBuilder(2006, 1, 2, 12, 30, 0)));
		assertTrue(!new DTBuilder(2006, 3, 2, 12, 30, 0).equals(new DTBuilder(2006, 1, 2, 12, 30, 0)));
		assertTrue(!new DTBuilder(2006, 1, 3, 12, 30, 0).equals(new DTBuilder(2006, 1, 2, 12, 30, 0)));
		assertTrue(!new DTBuilder(2006, 1, 2, 13, 30, 0).equals(new DTBuilder(2006, 1, 2, 12, 30, 0)));
		assertTrue(!new DTBuilder(2006, 1, 2, 12, 45, 0).equals(new DTBuilder(2006, 1, 2, 12, 30, 0)));
		assertTrue(!new DTBuilder(2006, 1, 2, 12, 30, 1).equals(new DTBuilder(2006, 1, 2, 12, 30, 0)));
		assertEquals(new DTBuilder(2006, 1, 2).hashCode(), new DTBuilder(2006, 1, 2).hashCode());
		assertEquals(new DTBuilder(0, 0, 0), new DTBuilder(0, 0, 0, 0, 0, 0));
	}

	public void testToDate() {
		assertEquals(new DateValueImpl(2006, 1, 2), new DTBuilder(2006, 1, 2).toDate());
		assertEquals(new DateValueImpl(2006, 1, 2), new DTBuilder(2006, 1, 2, 12, 30, 45).toDate());
		//test normalization
		assertEquals(new DateValueImpl(2006, 1, 2), new DTBuilder(2005, 12, 33).toDate());
	}

	public void testToDateTime() {
		assertEquals(new DateTimeValueImpl(2006, 1, 2, 0, 0, 0), new DTBuilder(2006, 1, 2).toDateTime());
		assertEquals(new DateTimeValueImpl(2006, 1, 2, 12, 30, 45), new DTBuilder(2006, 1, 2, 12, 30, 45).toDateTime());
		//test normalization
		assertEquals(new DateTimeValueImpl(2006, 1, 2, 0, 0, 0), new DTBuilder(2005, 12, 33, 0, 0, 0).toDateTime());
		assertEquals(new DateTimeValueImpl(2006, 1, 2, 12, 0, 0), new DTBuilder(2005, 12, 31, 60, 0, 0).toDateTime());
	}

	public void testCompareTo() {
		assertTrue(new DTBuilder(2005, 6, 15).compareTo(new DateValueImpl(2005, 6, 15)) == 0);
		assertTrue(new DTBuilder(2005, 6, 15).compareTo(new DateValueImpl(2006, 6, 15)) < 0);
		assertTrue(new DTBuilder(2005, 6, 15).compareTo(new DateValueImpl(2005, 7, 15)) < 0);
		assertTrue(new DTBuilder(2005, 6, 15).compareTo(new DateValueImpl(2005, 6, 16)) < 0);
		assertTrue(new DTBuilder(2006, 6, 15).compareTo(new DateValueImpl(2005, 6, 15)) > 0);
		assertTrue(new DTBuilder(2005, 7, 15).compareTo(new DateValueImpl(2005, 6, 15)) > 0);
		assertTrue(new DTBuilder(2005, 6, 16).compareTo(new DateValueImpl(2005, 6, 15)) > 0);
		assertTrue(new DTBuilder(2005, 6, 15, 12, 0, 0).compareTo(new DateTimeValueImpl(2005, 6, 15, 12, 0, 0)) == 0);
		assertTrue(new DTBuilder(2005, 6, 15, 11, 0, 0).compareTo(new DateTimeValueImpl(2005, 6, 15, 12, 0, 0)) < 0);
		assertTrue(new DTBuilder(2005, 6, 15, 13, 0, 0).compareTo(new DateTimeValueImpl(2005, 6, 15, 12, 0, 0)) > 0);
	}

	public void testNormalize() {
		DTBuilder dtb = new DTBuilder(2006, 1, 1);
		assertDtBuilder(2006, 1, 1, 0, 0, 0, dtb);

		dtb.day -= 1;
		dtb.normalize();
		assertDtBuilder(2005, 12, 31, 0, 0, 0, dtb);

		dtb.day -= 61;
		dtb.normalize();
		assertDtBuilder(2005, 10, 31, 0, 0, 0, dtb);

		dtb.day -= 365;
		dtb.normalize();
		assertDtBuilder(2004, 10, 31, 0, 0, 0, dtb);

		dtb.month += 25; //+ 24 -> 2006-10-31, + 1 -> 2006-11-31 -> 2006-12-1
		dtb.normalize();
		assertDtBuilder(2006, 12, 1, 0, 0, 0, dtb);

		dtb.month -= 13;
		dtb.normalize();
		assertDtBuilder(2005, 11, 1, 0, 0, 0, dtb);

		dtb.month += 2;
		dtb.normalize();
		assertDtBuilder(2006, 1, 1, 0, 0, 0, dtb);

		dtb.day += 398; //1 year + 1 month + 2 days
		dtb.normalize();
		assertDtBuilder(2007, 2, 3, 0, 0, 0, dtb);

		dtb.hour += 252;
		dtb.normalize();
		assertDtBuilder(2007, 2, 13, 12, 0, 0, dtb);

		dtb.hour -= 365 * 24 - 8;
		dtb.normalize();
		assertDtBuilder(2006, 2, 13, 20, 0, 0, dtb);

		dtb.minute -= 24 * 60;
		dtb.normalize();
		assertDtBuilder(2006, 2, 12, 20, 0, 0, dtb);

		dtb.second -= 12 * 60 * 60;
		dtb.normalize();
		assertDtBuilder(2006, 2, 12, 8, 0, 0, dtb);
	}

	private static void assertDtBuilder(int year, int month, int day, int hour, int minute, int second, DTBuilder dtBuilder) {
		assertEquals(year, dtBuilder.year);
		assertEquals(month, dtBuilder.month);
		assertEquals(day, dtBuilder.day);
		assertEquals(hour, dtBuilder.hour);
		assertEquals(minute, dtBuilder.minute);
		assertEquals(second, dtBuilder.second);
	}
}
