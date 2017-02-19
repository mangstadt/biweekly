package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;

import biweekly.parameter.Related;
import biweekly.util.Duration;

/*
 Copyright (c) 2013-2017, Michael Angstadt
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
public class TriggerTest {
	@Test
	public void constructors() throws Exception {
		Trigger property = new Trigger((Date) null);
		assertNull(property.getDate());
		assertNull(property.getDuration());
		assertNull(property.getRelated());

		Date date = new Date();
		property = new Trigger(date);
		assertEquals(date, property.getDate());
		assertNull(property.getDuration());
		assertNull(property.getRelated());

		Duration duration = new Duration.Builder().hours(1).build();
		property = new Trigger(duration, Related.START);
		assertNull(property.getDate());
		assertEquals(duration, property.getDuration());
		assertEquals(Related.START, property.getRelated());
	}

	@Test
	public void set_value() {
		Trigger property = new Trigger((Date) null);

		Date date = new Date();
		property.setDate(date);
		assertEquals(date, property.getDate());
		assertNull(property.getDuration());
		assertNull(property.getRelated());

		Duration duration = new Duration.Builder().hours(1).build();
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
	public void validate() {
		Trigger property = new Trigger((Date) null);
		assertValidate(property).run(33);

		property = new Trigger(new Date());
		assertValidate(property).run();

		property = new Trigger(new Duration.Builder().build(), null);
		assertValidate(property).run(10);

		property = new Trigger(new Duration.Builder().build(), Related.END);
		assertValidate(property).run();
	}

	@Test
	public void toStringValues() {
		Trigger property = new Trigger(new Date());
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Trigger original = new Trigger((Date) null);
		assertCopy(original);

		original = new Trigger(new Date());
		assertCopy(original).notSame("getDate");

		original = new Trigger(new Duration.Builder().hours(1).build(), Related.START);
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new Trigger((Date)null),
			new Trigger(date("2016-01-21")),
			new Trigger(date("2016-01-22")),
			new Trigger(new Duration.Builder().hours(1).build(), null),
			new Trigger(new Duration.Builder().hours(1).build(), Related.START),
			new Trigger(new Duration.Builder().hours(1).build(), Related.END),
			new Trigger(new Duration.Builder().hours(2).build(), Related.START)
		);

		assertEqualsMethod(Trigger.class, new Date())
		.constructor(new Class<?>[]{Date.class}, (Date)null).test()
		.constructor(new Date()).test()
		.constructor(new Duration.Builder().hours(1).build(), Related.START).test();
		//@formatter:on
	}
}
