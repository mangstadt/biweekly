/*
 * Copyright (C) 2006 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * All Rights Reserved.
 */

package biweekly.util.com.google.ical.values;

/**
 * A calendar date.
 * @author Neal Gafter
 */
public interface DateValue extends Comparable<DateValue> {
  /**
   * Gets the Gregorian year (for example, 2004).
   * @return the year
   */
  int year();

  /**
   * Gets the Gregorian month (in the range 1-12).
   * @return the month
   */
  int month();

  /**
   * Gets the Gregorian day of the month (in the range 1-31).
   * @return the day of the month
   */
  int day();
}
