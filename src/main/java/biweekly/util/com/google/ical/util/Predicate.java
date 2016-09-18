// CopyrightGoogle Inc. All rights reserved.

package biweekly.util.com.google.ical.util;

import java.io.Serializable;

/**
 * A function with a boolean return value. Useful for filtering.
 */
public interface Predicate<T> extends Serializable {
  /**
   * Applies this predicate to the given object.
   * @param input the input
   * @return the value of this predicate when applied to the input
   */
  boolean apply(T input);
}
