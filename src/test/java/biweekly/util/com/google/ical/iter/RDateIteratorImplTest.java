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

package biweekly.util.com.google.ical.iter;

import static biweekly.util.TestUtils.assertIterator;

import java.util.Arrays;

import junit.framework.TestCase;
import biweekly.util.com.google.ical.values.DateTimeValueImpl;
import biweekly.util.com.google.ical.values.DateValue;
import biweekly.util.com.google.ical.values.DateValueImpl;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 * @author Michael Angstadt
 */
public class RDateIteratorImplTest extends TestCase {
  public void testOneDate() {
    DateValue[] dates = new DateValue[] {
      new DateValueImpl(2006, 4, 12)
    };
    DateValue[] expected = dates;
    
    RDateIteratorImpl ri = new RDateIteratorImpl(dates);
    assertIterator(Arrays.asList(expected), ri);
  }

  public void testOneDateTime() {
    DateValue[] dates = new DateValue[] {
      new DateTimeValueImpl(2006, 4, 12, 12, 0, 0)
    };
    DateValue[] expected = dates;
    
    RDateIteratorImpl ri = new RDateIteratorImpl(dates);
    assertIterator(Arrays.asList(expected), ri);
  }
  
  public void testSortAndRemoveDuplicates(){
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
  
  public void testAdvanceTo(){
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
