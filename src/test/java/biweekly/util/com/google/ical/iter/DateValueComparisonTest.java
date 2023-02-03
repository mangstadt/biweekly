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
 Copyright (c) 2013-2023, Michael Angstadt
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

package biweekly.util.com.google.ical.iter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class DateValueComparisonTest {
	@Test
	public void comparisonSameAsDateValueImpl() {
		/*
		 * It's more important for DateValueComparison to be a total ordering
		 * (see the class comments) than to be consistent with DateValue.
		 */
		//@formatter:off
		DateValue[] inOrder = {
			new DateValueImpl(2006, 4, 11),
			new DateTimeValueImpl(2006, 4, 11, 0, 0, 0),
			new DateTimeValueImpl(2006, 4, 11, 0, 0, 1),
			new DateTimeValueImpl(2006, 4, 11, 12, 30, 15),
			new DateTimeValueImpl(2006, 4, 11, 23, 59, 59),
			new DateValueImpl(2006, 4, 12),
			new DateValueImpl(2006, 4, 13),
			new DateTimeValueImpl(2006, 4, 14, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 14, 15, 0, 0),
		};
		//@formatter:on

		for (int i = 1; i < inOrder.length; ++i) {
			long prev = DateValueComparison.comparable(inOrder[i - 1]);
			long cur = DateValueComparison.comparable(inOrder[i]);
			assertTrue(prev < cur);
		}
		for (int i = 0; i < inOrder.length; ++i) {
			for (int j = 0; j < inOrder.length; ++j) {
				int cmp1 = sign3(i - j);
				int cmp2 = sign3(inOrder[i].compareTo(inOrder[j]));
				int cmp3 = sign3(DateValueComparison.comparable(inOrder[i]), DateValueComparison.comparable(inOrder[j]));
				assertEquals(cmp1, cmp2);
				assertEquals(cmp2, cmp3);
			}
		}
	}

	private static final int sign3(int i) {
		return i < 0 ? -1 : i != 0 ? 1 : 0;
	}

	private static final int sign3(long a, long b) {
		return a < b ? -1 : a != b ? 1 : 0;
	}
}
