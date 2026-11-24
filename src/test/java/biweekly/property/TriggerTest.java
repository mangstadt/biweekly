package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.util.Date;

import org.junit.jupiter.api.Test;

import biweekly.parameter.Related;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
class TriggerTest {
	@Test
	void constructors() {
		Trigger property = new Trigger((Date) null);
		assertNull(property.getDate());
		assertNull(property.getDuration());
		assertNull(property.getRelated());

		Date date = new Date();
		property = new Trigger(date);
		assertEquals(date, property.getDate());
		assertNull(property.getDuration());
		assertNull(property.getRelated());

		Duration duration = Duration.ofHours(1);
		property = new Trigger(duration, Related.START);
		assertNull(property.getDate());
		assertEquals(duration, property.getDuration());
		assertEquals(Related.START, property.getRelated());
	}

	@Test
	void set_value() {
		Trigger property = new Trigger((Date) null);

		Date date = new Date();
		property.setDate(date);
		assertEquals(date, property.getDate());
		assertNull(property.getDuration());
		assertNull(property.getRelated());

		Duration duration = Duration.ofHours(1);
		property.setDuration(duration, Related.START);
		assertNull(property.getDate());
		assertEquals(duration, property.getDuration());
		assertEquals(Related.START, property.getRelated());

		property.setRelated(Related.END);
		assertNull(property.getDate());
		assertEquals(duration, property.getDuration());
		assertEquals(Related.END, property.getRelated());

		property.setDate(date);
		assertEquals(date, property.getDate());
		assertNull(property.getDuration());
		assertNull(property.getRelated());

		property.setRelated(Related.END);
		assertEquals(date, property.getDate());
		assertNull(property.getDuration());
		assertEquals(Related.END, property.getRelated());

	}

	@Test
	void validate() {
		Trigger property = new Trigger((Date) null);
		assertValidate(property).run(33);

		property = new Trigger(new Date());
		assertValidate(property).run();

		property = new Trigger(Duration.ZERO, null);
		assertValidate(property).run(10);

		property = new Trigger(Duration.ZERO, Related.END);
		assertValidate(property).run();
	}

	@Test
	void toStringValues() {
		Trigger property = new Trigger(new Date());
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	void copy() {
		Trigger original = new Trigger((Date) null);
		assertCopy(original);

		original = new Trigger(new Date());
		assertCopy(original).notSame("getDate");

		original = new Trigger(Duration.ofHours(1), Related.START);
		assertCopy(original);
	}

	@Test
	void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new Trigger((Date)null),
			new Trigger(date(2016, 1, 21)),
			new Trigger(date(2016, 1, 22)),
			new Trigger(Duration.ofHours(1), null),
			new Trigger(Duration.ofHours(1), Related.START),
			new Trigger(Duration.ofHours(1), Related.END),
			new Trigger(Duration.ofHours(2), Related.START)
		);

		assertEqualsMethod(Trigger.class, new Date())
		.constructor(new Class<?>[]{Date.class}, (Date)null).test()
		.constructor(new Date()).test()
		.constructor(Duration.ofHours(1), Related.START).test();
		//@formatter:on
	}
}
