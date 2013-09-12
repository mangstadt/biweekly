package biweekly.util;

import static biweekly.util.TestUtils.assertIntEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/*
 Copyright (c) 2013, Michael Angstadt
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
		Duration duration = Duration.parse("P1W2D3H4M50S");
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
	}

	@Test(expected = IllegalArgumentException.class)
	public void parse_invalid() {
		Duration.parse("not valid");
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
}
