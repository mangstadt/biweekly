package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.Date;

import org.junit.Test;

import biweekly.util.ICalDate;

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
public class DateOrDateTimePropertyTest {
	@Test
	public void constructors() throws Exception {
		DateOrDateTimeProperty property = new DateOrDateTimeProperty((Date) null);
		assertNull(property.getValue());

		property = new DateOrDateTimeProperty(date("2016-01-17"));
		assertEquals(new ICalDate(date("2016-01-17"), true), property.getValue());

		property = new DateOrDateTimeProperty(date("2016-01-17"), false);
		assertEquals(new ICalDate(date("2016-01-17"), false), property.getValue());

		ICalDate icalDate = new ICalDate(date("2016-01-17"));
		property = new DateOrDateTimeProperty(icalDate);
		assertSame(icalDate, property.getValue());

		Date disguisedICalDate = new ICalDate(false);
		property = new DateOrDateTimeProperty(disguisedICalDate);
		assertSame(disguisedICalDate, property.getValue());
	}

	@Test
	public void set_value() {
		DateOrDateTimeProperty property = new DateOrDateTimeProperty(date("2016-01-17"));

		property.setValue(date("2016-01-18"), false);
		assertEquals(new ICalDate(date("2016-01-18"), false), property.getValue());

		property.setValue(date("2016-01-18"), true);
		assertEquals(new ICalDate(date("2016-01-18"), true), property.getValue());

		property.setValue(null, true);
		assertNull(property.getValue());

		property.setValue(new ICalDate(date("2016-01-18"), false));
		assertEquals(new ICalDate(date("2016-01-18"), false), property.getValue());

		property.setValue(null);
		assertNull(property.getValue());
	}

	@Test
	public void validate() {
		DateOrDateTimeProperty property = new DateOrDateTimeProperty((Date) null, false);
		assertValidate(property).run(26);

		property = new DateOrDateTimeProperty(new Date(), false);
		assertValidate(property).run();
	}

	@Test
	public void toStringValues() {
		DateOrDateTimeProperty property = new DateOrDateTimeProperty((ICalDate) null);
		assertFalse(property.toStringValues().isEmpty());

		property = new DateOrDateTimeProperty(new ICalDate(date("2016-01-17")));
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		DateOrDateTimeProperty original = new DateOrDateTimeProperty(new ICalDate());
		assertCopy(original).notSame("getValue");

		original = new DateOrDateTimeProperty((ICalDate) null);
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new DateOrDateTimeProperty((ICalDate)null),
			new DateOrDateTimeProperty(date("2016-01-17"), false),
			new DateOrDateTimeProperty(date("2016-01-18"), false),
			new DateOrDateTimeProperty(date("2016-01-17"), true)
		);

		assertEqualsMethod(DateOrDateTimeProperty.class, new ICalDate())
		.constructor(new Class<?>[]{ICalDate.class}, (ICalDate)null).test()
		.constructor(new ICalDate()).test();
		//@formatter:on
	}
}
