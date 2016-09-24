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
