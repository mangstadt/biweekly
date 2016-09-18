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
 * A time of day.
 * @author Neal Gafter
 */
public interface TimeValue {
  /**
   * Gets the hour (in the range 0-24).
   * @return the hour
   */
  int hour();

  /**
   * Gets the minute (in the range 0-59). If the hour is 24, then this method
   * should return zero.
   * @return the minute
   */
  int minute();

  /**
   * Gets the second (in the range 0 through 59). If the hour is 24, then this
   * method should return zero.
   * @return the second
   */
  int second();
}
