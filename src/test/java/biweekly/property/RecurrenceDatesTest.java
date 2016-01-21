package biweekly.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import biweekly.util.ICalDate;
import biweekly.util.Period;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
public class RecurrenceDatesTest {
	@Test
	public void constructors() throws Exception {
		RecurrenceDates property = new RecurrenceDates();
		assertEquals(Arrays.asList(), property.getDates());
		assertEquals(Arrays.asList(), property.getPeriods());
	}

	@Test
	public void set_value() {
		RecurrenceDates property = new RecurrenceDates();

		Date date = new Date();
		property.addDate(date);
		assertEquals(Arrays.asList(new ICalDate(date, true)), property.getDates());
		assertEquals(Arrays.asList(), property.getPeriods());

		ICalDate icalDate = new ICalDate();
		property.addDate(icalDate);
		assertEquals(Arrays.asList(new ICalDate(date, true), icalDate), property.getDates());
		assertEquals(Arrays.asList(), property.getPeriods());

		Date icalDateDisguised = new ICalDate();
		property.addDate(icalDateDisguised);
		assertEquals(Arrays.asList(new ICalDate(date, true), icalDate, icalDateDisguised), property.getDates());
		assertEquals(Arrays.asList(), property.getPeriods());

		Period period = new Period(new Date(), new Date());
		property.addPeriod(period);
		assertEquals(Arrays.asList(new ICalDate(date, true), icalDate, icalDateDisguised), property.getDates());
		assertEquals(Arrays.asList(period), property.getPeriods());
	}

	@Test
	public void validate() {
		RecurrenceDates property = new RecurrenceDates();
		assertValidate(property).run(26);

		property = new RecurrenceDates();
		property.addDate(new Date());
		property.addPeriod(new Period(new Date(), new Date()));
		assertValidate(property).versions(V1_0).run(49, 51);
		assertValidate(property).versions(V2_0_DEPRECATED, V2_0).run(49);

		property = new RecurrenceDates();
		property.addDate(new ICalDate(new Date(), true));
		property.addDate(new ICalDate(new Date(), false));
		assertValidate(property).run(50);

		property = new RecurrenceDates();
		property.addDate(new Date());
		property.addDate(new Date());
		assertValidate(property).run();

		property = new RecurrenceDates();
		property.addPeriod(new Period(new Date(), new Date()));
		property.addPeriod(new Period(new Date(), new Date()));
		assertValidate(property).versions(V1_0).run(51);
		assertValidate(property).versions(V2_0_DEPRECATED, V2_0).run();
	}

	@Test
	public void toStringValues() {
		RecurrenceDates property = new RecurrenceDates();
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		RecurrenceDates original = new RecurrenceDates();
		assertCopy(original).notSameDeep("getDates").notSameDeep("getPeriods");

		original = new RecurrenceDates();
		original.addDate(new Date());
		original.addDate(new ICalDate());
		original.addPeriod(new Period(new Date(), new Date()));
		assertCopy(original).notSameDeep("getDates").notSameDeep("getPeriods");
	}

	@Test
	public void equals() {
		List<ICalProperty> properties = new ArrayList<ICalProperty>();

		RecurrenceDates property = new RecurrenceDates();
		properties.add(property);

		property = new RecurrenceDates();
		property.addDate(date("2016-01-21"));
		properties.add(property);

		property = new RecurrenceDates();
		property.addDate(date("2016-01-22"));
		properties.add(property);

		property = new RecurrenceDates();
		property.addDate(date("2016-01-21"));
		property.addDate(date("2016-01-22"));
		properties.add(property);

		property = new RecurrenceDates();
		property.addPeriod(new Period(date("2016-01-21"), new Date()));
		properties.add(property);

		property = new RecurrenceDates();
		property.addPeriod(new Period(date("2016-01-22"), new Date()));
		properties.add(property);

		property = new RecurrenceDates();
		property.addPeriod(new Period(date("2016-01-21"), new Date()));
		property.addPeriod(new Period(date("2016-01-22"), new Date()));
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(RecurrenceDates.class)
		.constructor()
			.test()
			.method("addDate", new Date()).test()
			.method("addPeriod", new Period(new Date(), new Date())).test();
		//@formatter:on
	}
}
