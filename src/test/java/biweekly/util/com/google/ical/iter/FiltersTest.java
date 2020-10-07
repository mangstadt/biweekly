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
 Copyright (c) 2013-2020, Michael Angstadt
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

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import biweekly.util.DayOfWeek;
import biweekly.util.com.google.ical.util.Predicate;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class FiltersTest {
	@Test
	public void weekIntervalFilter() {
		// *s match those that are in the weeks that should pass the filter

		Predicate<? super DateValue> f1 = Filters.weekIntervalFilter(2, DayOfWeek.MONDAY, new DateValueImpl(2005, 9, 11));
		// FOR f1
		//    September 2005
		//  Su  Mo  Tu  We  Th  Fr  Sa
		//                   1   2   3
		//   4  *5  *6  *7  *8  *9 *10
		// *11  12  13  14  15  16  17
		//  18 *19 *20 *21 *22 *23 *24
		// *25  26  27  28  29  30
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 9)));
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 10)));
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 11)));
		assertTrue(!f1.apply(new DateValueImpl(2005, 9, 12)));
		assertTrue(!f1.apply(new DateValueImpl(2005, 9, 13)));
		assertTrue(!f1.apply(new DateValueImpl(2005, 9, 14)));
		assertTrue(!f1.apply(new DateValueImpl(2005, 9, 15)));
		assertTrue(!f1.apply(new DateValueImpl(2005, 9, 16)));
		assertTrue(!f1.apply(new DateValueImpl(2005, 9, 17)));
		assertTrue(!f1.apply(new DateValueImpl(2005, 9, 18)));
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 19)));
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 20)));
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 21)));
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 22)));
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 23)));
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 24)));
		assertTrue(f1.apply(new DateValueImpl(2005, 9, 25)));
		assertTrue(!f1.apply(new DateValueImpl(2005, 9, 26)));

		Predicate<? super DateValue> f2 = Filters.weekIntervalFilter(2, DayOfWeek.SUNDAY, new DateValueImpl(2005, 9, 11));
		// FOR f2
		//    September 2005
		//  Su  Mo  Tu  We  Th  Fr  Sa
		//                   1   2   3
		//   4   5   6   7   8   9  10
		// *11 *12 *13 *14 *15 *16 *17
		//  18  19  20  21  22  23  24
		// *25 *26 *27 *28 *29 *30
		assertTrue(!f2.apply(new DateValueImpl(2005, 9, 9)));
		assertTrue(!f2.apply(new DateValueImpl(2005, 9, 10)));
		assertTrue(f2.apply(new DateValueImpl(2005, 9, 11)));
		assertTrue(f2.apply(new DateValueImpl(2005, 9, 12)));
		assertTrue(f2.apply(new DateValueImpl(2005, 9, 13)));
		assertTrue(f2.apply(new DateValueImpl(2005, 9, 14)));
		assertTrue(f2.apply(new DateValueImpl(2005, 9, 15)));
		assertTrue(f2.apply(new DateValueImpl(2005, 9, 16)));
		assertTrue(f2.apply(new DateValueImpl(2005, 9, 17)));
		assertTrue(!f2.apply(new DateValueImpl(2005, 9, 18)));
		assertTrue(!f2.apply(new DateValueImpl(2005, 9, 19)));
		assertTrue(!f2.apply(new DateValueImpl(2005, 9, 20)));
		assertTrue(!f2.apply(new DateValueImpl(2005, 9, 21)));
		assertTrue(!f2.apply(new DateValueImpl(2005, 9, 22)));
		assertTrue(!f2.apply(new DateValueImpl(2005, 9, 23)));
		assertTrue(!f2.apply(new DateValueImpl(2005, 9, 24)));
		assertTrue(f2.apply(new DateValueImpl(2005, 9, 25)));
		assertTrue(f2.apply(new DateValueImpl(2005, 9, 26)));
	}
}
