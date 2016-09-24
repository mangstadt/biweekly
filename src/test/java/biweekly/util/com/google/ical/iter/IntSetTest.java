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

package biweekly.util.com.google.ical.iter;

import java.util.Arrays;

import junit.framework.TestCase;

/**
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
public class IntSetTest extends TestCase {
  public void testAddAndContainsAndSize() {
    IntSet a = new IntSet();

    assertTrue(!a.contains(-2));
    assertTrue(!a.contains(-1));
    assertTrue(!a.contains(0));
    assertTrue(!a.contains(1));
    assertTrue(!a.contains(2));
    assertEquals(0, a.size());

    a.add(1);

    assertTrue(!a.contains(-2));
    assertTrue(!a.contains(-1));
    assertTrue(!a.contains(0));
    assertTrue( a.contains(1));
    assertTrue(!a.contains(2));
    assertEquals(1, a.size());

    a.add(1);

    assertTrue(!a.contains(-2));
    assertTrue(!a.contains(-1));
    assertTrue(!a.contains(0));
    assertTrue( a.contains(1));
    assertTrue(!a.contains(2));
    assertEquals(1, a.size());

    a.add(-2);

    assertTrue( a.contains(-2));
    assertTrue(!a.contains(-1));
    assertTrue(!a.contains(0));
    assertTrue( a.contains(1));
    assertTrue(!a.contains(2));
    assertEquals(2, a.size());
  }

  public void testToIntArray() {
    IntSet a = new IntSet();
    int[] expected = {};
    int[] actual = a.toIntArray();
    assertTrue(Arrays.equals(expected, actual));

    a.add(17);
    a.add(0);
    a.add(0);
    a.add(-24);
    a.add(-12);
    a.add(4);

    expected = new int[]{-24, -12, 0, 4, 17};
    actual = a.toIntArray();
    assertTrue(Arrays.equals(expected, actual));
  }
}
