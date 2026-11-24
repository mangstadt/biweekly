package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertEqualsAndHash;
import static biweekly.util.TestUtils.assertEqualsMethodEssentials;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import biweekly.parameter.FreeBusyType;
import java.time.Duration;
import biweekly.util.Period;

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
class FreeBusyTest {
	@Test
	void constructors() {
		FreeBusy property = new FreeBusy();
		assertEquals(Arrays.asList(), property.getValues());
	}

	@Test
	void set_value() {
		FreeBusy property = new FreeBusy();
		Date start = new Date();
		Duration duration = Duration.ofHours(1);

		Period period = new Period(start, duration);
		property.getValues().add(period);
		assertEquals(Arrays.asList(period), property.getValues());
		assertNull(property.getType());

		Period period2 = new Period(start, (Date) null);
		property.getValues().add(period2);
		assertEquals(Arrays.asList(period, period2), property.getValues());
		assertNull(property.getType());

		Period period3 = new Period(start, (Duration) null);
		property.getValues().add(period3);
		assertEquals(Arrays.asList(period, period2, period3), property.getValues());
		assertNull(property.getType());

		property.setType(FreeBusyType.BUSY);
		assertEquals(Arrays.asList(period, period2, period3), property.getValues());
		assertEquals(FreeBusyType.BUSY, property.getType());

		property.setType(null);
		assertEquals(Arrays.asList(period, period2, period3), property.getValues());
		assertNull(property.getType());
	}

	@Test
	void validate() {
		FreeBusy property = new FreeBusy();
		assertValidate(property).run(26);

		property = new FreeBusy();
		property.getValues().add(new Period(null, (Date) null));
		assertValidate(property).run(39, 40);

		property = new FreeBusy();
		property.getValues().add(new Period(new Date(), (Date) null));
		assertValidate(property).run(40);

		property = new FreeBusy();
		property.getValues().add(new Period(null, new Date()));
		assertValidate(property).run(39);

		property = new FreeBusy();
		property.getValues().add(new Period(new Date(), new Date()));
		assertValidate(property).run();

		property = new FreeBusy();
		property.getValues().add(new Period(null, (Duration) null));
		assertValidate(property).run(39, 40);

		property = new FreeBusy();
		property.getValues().add(new Period(new Date(), (Duration) null));
		assertValidate(property).run(40);

		property = new FreeBusy();
		property.getValues().add(new Period(null, Duration.ZERO));
		assertValidate(property).run(39);

		property = new FreeBusy();
		property.getValues().add(new Period(new Date(), Duration.ZERO));
		assertValidate(property).run();
	}

	@Test
	void toStringValues() {
		FreeBusy property = new FreeBusy();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	void copy() {
		FreeBusy original = new FreeBusy();
		assertCopy(original).notSameDeep("getValues");

		original = new FreeBusy();
		original.getValues().add(new Period(new Date(), new Date()));
		original.getValues().add(new Period(new Date(), (Date) null));
		original.getValues().add(new Period(new Date(), Duration.ZERO));
		original.getValues().add(new Period(new Date(), (Duration) null));
		assertCopy(original).notSameDeep("getValues");
	}

	@Test
	void equals() {
		Date start = new Date();
		Date end = new Date();
		Duration duration = Duration.ofHours(1);

		List<ICalProperty> properties = new ArrayList<>();

		FreeBusy property = new FreeBusy();
		properties.add(property);

		property = new FreeBusy();
		property.getValues().add(new Period(start, end));
		properties.add(property);

		property = new FreeBusy();
		property.getValues().add(new Period(start, duration));
		properties.add(property);

		property = new FreeBusy();
		property.getValues().add(new Period(start, end));
		property.getValues().add(new Period(start, end));
		properties.add(property);

		assertNothingIsEqual(properties);

		assertEqualsMethodEssentials(new FreeBusy());

		FreeBusy one = new FreeBusy();
		FreeBusy two = new FreeBusy();
		assertEqualsAndHash(one, two);

		one.getValues().add(new Period(start, end));
		two.getValues().add(new Period(start, end));
		assertEqualsAndHash(one, two);
	}
}
