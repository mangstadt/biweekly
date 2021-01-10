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

package biweekly.util.com.google.ical.iter;

import static biweekly.util.TestUtils.assertIterator;

import java.util.Arrays;

import org.junit.Test;

import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
//@formatter:off
public class RDateIteratorImplTest {
	@Test
	public void oneDate() {
		DateValue[] dates = new DateValue[] {
			new DateValueImpl(2006, 4, 12)
		};
		DateValue[] expected = dates;
		
		RDateIteratorImpl ri = new RDateIteratorImpl(dates);
		assertIterator(Arrays.asList(expected), ri);
	}

	@Test
	public void oneDateTime() {
		DateValue[] dates = new DateValue[] {
			new DateTimeValueImpl(2006, 4, 12, 12, 0, 0)
		};
		DateValue[] expected = dates;
		
		RDateIteratorImpl ri = new RDateIteratorImpl(dates);
		assertIterator(Arrays.asList(expected), ri);
	}
	
	@Test
	public void sortAndRemoveDuplicates(){
		DateValue[] dates = new DateValue[] {
			new DateTimeValueImpl(2006, 4, 14, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 12, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 12, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 13, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 12, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 13, 12, 0, 0)
		};
		DateValue[] expected = new DateValue[] {
			new DateTimeValueImpl(2006, 4, 12, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 13, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 14, 12, 0, 0)
		};

		RDateIteratorImpl ri = new RDateIteratorImpl(dates);
		assertIterator(Arrays.asList(expected), ri);
	}
	
	@Test
	public void advanceTo(){
		DateValue[] dates = new DateValue[] {
			new DateTimeValueImpl(2006, 4, 12, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 13, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 14, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 15, 12, 0, 0)
		};
		DateValue[] expected = new DateValue[] {
			new DateTimeValueImpl(2006, 4, 13, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 14, 12, 0, 0),
			new DateTimeValueImpl(2006, 4, 15, 12, 0, 0)
		};
		
		RDateIteratorImpl ri = new RDateIteratorImpl(dates);
		ri.advanceTo(new DateTimeValueImpl(2006, 4, 13, 12, 0, 0));
		assertIterator(Arrays.asList(expected), ri);
	}
}
//@formatter:on
