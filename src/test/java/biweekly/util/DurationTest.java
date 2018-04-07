package biweekly.util;

import static biweekly.util.TestUtils.assertIntEquals;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.junit.Test;

/*
 Copyright (c) 2013-2018, Michael Angstadt
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

/**
 * @author Michael Angstadt
 */
public class DurationTest {
	@Test
	public void parse_all() {
		Duration duration = Duration.parse("P1W2DT3H4M50S");
		assertEquals(false, duration.isPrior());
		assertIntEquals(1, duration.getWeeks());
		assertIntEquals(2, duration.getDays());
		assertIntEquals(3, duration.getHours());
		assertIntEquals(4, duration.getMinutes());
		assertIntEquals(50, duration.getSeconds());
	}

	@Test
	public void parse_prior() {
		Duration duration = Duration.parse("-P1W");
		assertEquals(true, duration.isPrior());
		assertIntEquals(1, duration.getWeeks());
		assertNull(duration.getDays());
		assertNull(duration.getHours());
		assertNull(duration.getMinutes());
		assertNull(duration.getSeconds());

		duration = Duration.parse("+P1W");
		assertEquals(false, duration.isPrior());
		assertIntEquals(1, duration.getWeeks());
		assertNull(duration.getDays());
		assertNull(duration.getHours());
		assertNull(duration.getMinutes());
		assertNull(duration.getSeconds());

		duration = Duration.parse("P1W");
		assertEquals(false, duration.isPrior());
		assertIntEquals(1, duration.getWeeks());
		assertNull(duration.getDays());
		assertNull(duration.getHours());
		assertNull(duration.getMinutes());
		assertNull(duration.getSeconds());
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		Duration.parse("not valid");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_no_number() {
		Duration.parse("PW");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_unknown_character_before_0() {
		Duration.parse("P5*");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_unknown_character_after_9() {
		Duration.parse("P5Z");
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_empty_string() {
		Duration.parse("");
	}

	@Test
	public void fromMillis() {
		Duration expected = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(5).prior(false).build();
		Duration actual = Duration.fromMillis(788645000);
		assertEquals(expected, actual);

		expected = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(5).prior(false).build();
		actual = Duration.fromMillis(788645123); //(millis % 1000) is not 0
		assertEquals(expected, actual);

		expected = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(5).prior(true).build();
		actual = Duration.fromMillis(-788645000);
		assertEquals(expected, actual);

		expected = Duration.builder().prior(false).build();
		actual = Duration.fromMillis(0);
		assertEquals(expected, actual);
	}

	@Test
	public void diff() throws Throwable {
		Date date1 = date("2013-09-12 09:49:21");
		Date date2 = date("2013-09-13 09:49:21");

		Duration expected = Duration.builder().days(1).prior(false).build();
		Duration actual = Duration.diff(date1, date2);
		assertEquals(expected, actual);

		expected = Duration.builder().days(1).prior(true).build();
		actual = Duration.diff(date2, date1);
		assertEquals(expected, actual);
	}

	@Test
	public void builder() {
		Duration duration = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(50).prior(true).build();
		assertEquals(true, duration.isPrior());
		assertIntEquals(1, duration.getWeeks());
		assertIntEquals(2, duration.getDays());
		assertIntEquals(3, duration.getHours());
		assertIntEquals(4, duration.getMinutes());
		assertIntEquals(50, duration.getSeconds());
	}

	@Test
	public void builder_empty() {
		Duration duration = Duration.builder().build();
		assertEquals(false, duration.isPrior());
		assertNull(duration.getWeeks());
		assertNull(duration.getDays());
		assertNull(duration.getHours());
		assertNull(duration.getMinutes());
		assertNull(duration.getSeconds());
	}

	@Test
	public void builder_copy() {
		Duration duration = Duration.builder().weeks(1).days(2).hours(3).build();
		duration = new Duration.Builder(duration).weeks(1).days(5).minutes(4).build();
		assertEquals(false, duration.isPrior());
		assertIntEquals(1, duration.getWeeks());
		assertIntEquals(5, duration.getDays());
		assertIntEquals(3, duration.getHours());
		assertIntEquals(4, duration.getMinutes());
		assertNull(duration.getSeconds());
	}

	@Test
	public void toString_all() {
		Duration duration = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(50).prior(true).build();
		assertEquals("-P1W2DT3H4M50S", duration.toString());
	}

	@Test
	public void toString_some() {
		Duration duration = Duration.builder().weeks(1).days(2).build();
		assertEquals("P1W2D", duration.toString());
	}

	@Test
	public void toString_just_time() {
		Duration duration = Duration.builder().hours(3).build();
		assertEquals("PT3H", duration.toString());
	}

	@Test
	public void equals_hashCode() {
		Duration duration1 = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(50).prior(true).build();
		Duration duration2 = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(50).prior(true).build();
		assertTrue(duration1.equals(duration2));
		assertTrue(duration2.equals(duration1));
		assertTrue(duration1.hashCode() == duration1.hashCode());

		duration1 = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(50).prior(true).build();
		duration2 = Duration.builder().weeks(2).days(2).hours(3).minutes(4).seconds(50).prior(true).build();
		assertFalse(duration1.equals(duration2));
		assertFalse(duration2.equals(duration1));
	}

	@Test
	public void toMillis() {
		Duration duration = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(5).prior(false).build();
		assertEquals(788645000, duration.toMillis());

		duration = Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(5).prior(true).build();
		assertEquals(-788645000, duration.toMillis());

		duration = Duration.builder().prior(false).build();
		assertEquals(0, duration.toMillis());

		duration = Duration.builder().prior(true).build();
		assertEquals(0, duration.toMillis());

	}

	@Test
	public void add() throws Throwable {
		Date date = date("2013-09-12 09:49:21");

		assertAdd(date, Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(5).prior(false), "2013-09-21 12:53:26");
		assertAdd(date, Duration.builder().weeks(1).days(2).hours(3).minutes(4).seconds(5).prior(true), "2013-09-03 06:45:16");
		assertAdd(date, Duration.builder().prior(false), "2013-09-12 09:49:21");
		assertAdd(date, Duration.builder().prior(true), "2013-09-12 09:49:21");
	}

	private void assertAdd(Date input, Duration.Builder builder, String expectedStr) throws Throwable {
		Duration duration = builder.build();

		Date expected = date(expectedStr);
		Date actual = duration.add(input);
		assertEquals(expected, actual);
	}
}
