package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import biweekly.util.ICalDate;
import biweekly.util.UtcOffset;

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
public class DaylightTest {
	@Test
	public void constructors() throws Exception {
		Daylight property = new Daylight();
		assertFalse(property.isDaylight());
		assertNull(property.getOffset());
		assertNull(property.getStart());
		assertNull(property.getEnd());
		assertNull(property.getStandardName());
		assertNull(property.getDaylightName());

		UtcOffset offset = new UtcOffset(true, 1, 0);
		ICalDate start = new ICalDate(date("2016-01-17"));
		ICalDate end = new ICalDate(date("2016-01-18"));
		property = new Daylight(true, offset, start, end, "s", "d");
		assertTrue(property.isDaylight());
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s", property.getStandardName());
		assertEquals("d", property.getDaylightName());
	}

	@Test
	public void set_value() {
		UtcOffset offset = new UtcOffset(true, 1, 0);
		ICalDate start = new ICalDate(date("2016-01-17"));
		ICalDate end = new ICalDate(date("2016-01-18"));
		Daylight property = new Daylight(true, offset, start, end, "s", "d");

		property.setDaylight(false);
		assertFalse(property.isDaylight());
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s", property.getStandardName());
		assertEquals("d", property.getDaylightName());

		property.setOffset(null);
		assertNull(property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s", property.getStandardName());
		assertEquals("d", property.getDaylightName());

		offset = new UtcOffset(true, 2, 0);
		property.setOffset(offset);
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s", property.getStandardName());
		assertEquals("d", property.getDaylightName());

		property.setStart(null);
		assertEquals(offset, property.getOffset());
		assertNull(property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s", property.getStandardName());
		assertEquals("d", property.getDaylightName());

		start = new ICalDate();
		property.setStart(start);
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s", property.getStandardName());
		assertEquals("d", property.getDaylightName());

		property.setEnd(null);
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertNull(property.getEnd());
		assertEquals("s", property.getStandardName());
		assertEquals("d", property.getDaylightName());

		end = new ICalDate();
		property.setEnd(end);
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s", property.getStandardName());
		assertEquals("d", property.getDaylightName());

		property.setStandardName(null);
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertNull(property.getStandardName());
		assertEquals("d", property.getDaylightName());

		property.setStandardName("s2");
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s2", property.getStandardName());
		assertEquals("d", property.getDaylightName());

		property.setDaylightName(null);
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s2", property.getStandardName());
		assertNull(property.getDaylightName());

		property.setDaylightName("d2");
		assertEquals(offset, property.getOffset());
		assertEquals(start, property.getStart());
		assertEquals(end, property.getEnd());
		assertEquals("s2", property.getStandardName());
		assertEquals("d2", property.getDaylightName());
	}

	@Test
	public void validate() {
		Daylight daylight = new Daylight();
		assertValidate(daylight).run();

		daylight.setDaylight(true);
		assertValidate(daylight).run(43);

		daylight.setOffset(new UtcOffset(true, 1, 0));
		assertValidate(daylight).run(43);

		daylight.setStart(new ICalDate());
		assertValidate(daylight).run(43);

		daylight.setEnd(new ICalDate());
		assertValidate(daylight).run(43);

		daylight.setStandardName("s");
		assertValidate(daylight).run(43);

		daylight.setDaylightName("d");
		assertValidate(daylight).run();
	}

	@Test
	public void toStringValues() {
		UtcOffset offset = new UtcOffset(true, 1, 0);
		ICalDate start = new ICalDate(date("2016-01-17"));
		ICalDate end = new ICalDate(date("2016-01-18"));
		Daylight property = new Daylight(true, offset, start, end, "s", "d");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		UtcOffset offset = new UtcOffset(true, 1, 0);
		ICalDate start = new ICalDate(date("2016-01-17"));
		ICalDate end = new ICalDate(date("2016-01-18"));
		Daylight original = new Daylight(true, offset, start, end, "s", "d");
		assertCopy(original).notSame("getStart").notSame("getEnd");

		original = new Daylight(false, null, null, null, null, null);
		assertCopy(original);
	}

	@Test
	public void equals() {
		UtcOffset offset = new UtcOffset(true, 1, 0);
		ICalDate start = new ICalDate(date("2016-01-17"));
		ICalDate end = new ICalDate(date("2016-01-18"));

		//@formatter:off
		assertNothingIsEqual(
			new Daylight(),
			new Daylight(true, null, null, null, null, null),
			new Daylight(false, offset, null, null, null, null),
			new Daylight(false, null, start, null, null, null),
			new Daylight(false, null, null, end, null, null),
			new Daylight(false, null, null, null, "s", null),
			new Daylight(false, null, null, null, null, "d"),
			new Daylight(true, offset, start, end, "s", "d")
		);

		assertEqualsMethod(Daylight.class)
		.constructor().test()
		.constructor(new Class<?>[]{boolean.class, UtcOffset.class, ICalDate.class, ICalDate.class, String.class, String.class}, true, offset, start, end, "s", "d").test();
		//@formatter:on
	}
}
