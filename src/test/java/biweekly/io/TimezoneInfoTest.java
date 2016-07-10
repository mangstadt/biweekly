package biweekly.io;

import static biweekly.util.TestUtils.assertCollectionContains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import biweekly.component.VTimezone;
import biweekly.property.ICalProperty;
import biweekly.util.TestUtils;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
public class TimezoneInfoTest {
	private TimezoneInfo tzinfo;

	@Before
	public void before() {
		tzinfo = new TimezoneInfo();
	}

	@Test
	public void defaultTimezone() {
		TimeZone timezone = TimeZone.getDefault();
		VTimezone component = new VTimezone("tz");
		TimezoneAssignment assignment = new TimezoneAssignment(timezone, component);

		tzinfo.setDefaultTimezone(assignment);
		assertSame(assignment, tzinfo.getDefaultTimezone());
		assertCollectionContains(tzinfo.getTimezones(), assignment);

		tzinfo.setDefaultTimezone(null);
		assertNull(tzinfo.getDefaultTimezone());
		assertCollectionContains(tzinfo.getTimezones(), assignment);
	}

	@Test
	public void set_getTimezone() {
		ICalProperty property1 = new ICalPropertyImpl();
		ICalProperty property2 = new ICalPropertyImpl();
		ICalProperty property3 = new ICalPropertyImpl();
		ICalProperty property4 = new ICalPropertyImpl();
		ICalProperty property5 = new ICalPropertyImpl();

		TimezoneAssignment defaultTimezone = new TimezoneAssignment(TimeZone.getDefault(), new VTimezone("default"));
		tzinfo.getTimezones().add(defaultTimezone);

		TimezoneAssignment timezone1 = new TimezoneAssignment(TestUtils.buildTimezone(1, 0), new VTimezone("custom"));
		tzinfo.getTimezones().add(timezone1);

		TimezoneAssignment timezone2 = new TimezoneAssignment(TestUtils.buildTimezone(2, 0), "");

		tzinfo.setDefaultTimezone(defaultTimezone);
		tzinfo.setTimezone(property1, timezone1);
		tzinfo.setTimezone(property2, timezone1);
		tzinfo.setFloating(property3, true);
		tzinfo.setTimezone(property5, timezone2);

		assertEquals(timezone1, tzinfo.getTimezone(property1));
		assertEquals(timezone1, tzinfo.getTimezone(property2));
		assertEquals(null, tzinfo.getTimezone(property3));
		assertEquals(null, tzinfo.getTimezone(property4));
		assertEquals(timezone2, tzinfo.getTimezone(property5));

		assertEquals(timezone1, tzinfo.getTimezoneToWriteIn(property1));
		assertEquals(timezone1, tzinfo.getTimezoneToWriteIn(property2));
		assertEquals(defaultTimezone, tzinfo.getTimezoneToWriteIn(property3));
		assertEquals(defaultTimezone, tzinfo.getTimezoneToWriteIn(property4));
		assertEquals(timezone2, tzinfo.getTimezoneToWriteIn(property5));

		assertFalse(tzinfo.isFloating(property1));
		assertFalse(tzinfo.isFloating(property2));
		assertTrue(tzinfo.isFloating(property3));
		assertFalse(tzinfo.isFloating(property4));
		assertFalse(tzinfo.isFloating(property5));

		assertCollectionContains(tzinfo.getTimezones(), defaultTimezone, timezone1, timezone2);
	}

	@Test
	public void removeTimezone() {
		ICalProperty property = new ICalPropertyImpl();

		TimezoneAssignment defaultTimezone = new TimezoneAssignment(TimeZone.getDefault(), new VTimezone("tz"));
		tzinfo.getTimezones().add(defaultTimezone);

		TimezoneAssignment timezone1 = new TimezoneAssignment(TestUtils.buildTimezone(1, 0), new VTimezone("tz2"));
		tzinfo.getTimezones().add(timezone1);

		tzinfo.setDefaultTimezone(defaultTimezone);

		tzinfo.setTimezone(property, timezone1);
		assertEquals(timezone1, tzinfo.getTimezone(property));
		assertEquals(timezone1, tzinfo.getTimezoneToWriteIn(property));
		assertFalse(tzinfo.isFloating(property));

		tzinfo.setTimezone(property, null);
		assertEquals(null, tzinfo.getTimezone(property));
		assertEquals(defaultTimezone, tzinfo.getTimezoneToWriteIn(property));
		assertFalse(tzinfo.isFloating(property));

		tzinfo.setFloating(property, true);
		assertEquals(null, tzinfo.getTimezone(property));
		assertEquals(defaultTimezone, tzinfo.getTimezoneToWriteIn(property));
		assertTrue(tzinfo.isFloating(property));

		tzinfo.setFloating(property, false);
		assertEquals(null, tzinfo.getTimezone(property));
		assertEquals(defaultTimezone, tzinfo.getTimezoneToWriteIn(property));
		assertFalse(tzinfo.isFloating(property));
	}

	private class ICalPropertyImpl extends ICalProperty {
		//empty
	}
}
