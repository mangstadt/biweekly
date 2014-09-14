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

package com.google.ical.values;

/**
 * An instant in time.
 *
 * @author Neal Gafter
 */
public class DateTimeValueImpl
    extends DateValueImpl
    implements DateTimeValue {
  private final int hour, minute, second;

  public DateTimeValueImpl(int year, int month, int day,
                           int hour, int minute, int second) {
    super(year, month, day);
    this.hour = hour;
    this.minute = minute;
    this.second = second;
  }

  public int hour() {
    return hour;
  }

  public int minute() {
    return minute;
  }

  public int second() {
    return second;
  }

  @Override
  public int hashCode() {
    return super.hashCode() ^
      ((this.hour << 12) + (this.minute << 6) + this.second);
  }

  @Override
  public String toString() {
    return String.format("%sT%02d%02d%02d",
                         super.toString(),
                         hour, minute, second);
  }
}





