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

import java.util.BitSet;

/**
 * A set of integers in a small range.
 * @author mikesamuel+svn@gmail.com (Mike Samuel)
 */
final class IntSet {
  private final BitSet ints = new BitSet();

  /**
   * Adds an integer to the set.
   * @param n the integer to add
   */
  void add(int n) {
    ints.set(encode(n));
  }

  /**
   * Determines if an integer is contained within the set.
   * @param n the integer to look for
   * @return true if it's in the set, false if not
   */
  boolean contains(int n) {
    return ints.get(encode(n));
  }

  /**
   * Gets the number of integers in the set.
   * @return the number of integers
   */
  int size() { return ints.cardinality(); }

  /**
   * Converts this set to a new integer array that is sorted in ascending order.
   * @return the array (sorted in ascending order)
   */
  int[] toIntArray() {
    int[] out = new int[size()];
    int a = 0, b = out.length;
    for (int i = -1; (i = ints.nextSetBit(i + 1)) >= 0;) {
      int n = decode(i);
      if (n < 0) {
        out[a++] = n;
      } else {
        out[--b] = n;
      }
    }

    //if it contains  -3, -1, 0, 1, 2, 4
    //then out will be -1, -3, 4, 2, 1, 0
    reverse(out, 0, a);
    reverse(out, a, out.length);

    return out;
  }
  
  /**
   * Encodes an integer so it can be inserted into the set.
   * @param n the integer to insert
   * @return the encoded value to insert into the set
   */
  private static int encode(int n) {
    return n < 0 ? ((-n << 1) + 1) : (n << 1);
  }

  /**
   * Decodes an integer from the set.
   * @param i the integer to decode
   * @return the decoded integer
   */
  private static int decode(int i) {
    return (i >>> 1) * (-(i & 1) | 1);
  }

  /**
   * Reverses an array.
   * @param array the array
   * @param start the index to start at
   * @param end the index to end at
   */
  private static void reverse(int[] array, int start, int end) {
    for (int i = start, j = end; i < --j; ++i) {
      int t = array[i];
      array[i] = array[j];
      array[j] = t;
    }
  }
}
