package biweekly.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collection;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import biweekly.component.VTimezone;
import biweekly.property.ICalProperty;
import biweekly.util.TestUtils;

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
public class TimezoneInfoTest {
	private TimezoneInfo tzinfo;

	@Before
	public void before() {
		tzinfo = new TimezoneInfo();
	}

	@Test(expected = IllegalArgumentException.class)
	public void assign_no_tzid() {
		VTimezone component = new VTimezone((String) null);
		TimeZone timezone = TimeZone.getDefault();
		tzinfo.assign(component, timezone);
	}

	@Test(expected = IllegalArgumentException.class)
	public void assign_empty_tzid() {
		VTimezone component = new VTimezone("  ");
		TimeZone timezone = TimeZone.getDefault();
		tzinfo.assign(component, timezone);
	}

	@Test
	public void setDefaultTimeZone() {
		TimeZone timezone = TimeZone.getDefault();
		VTimezone component = new VTimezone("tz");

		VTimezoneGenerator generator = mock(VTimezoneGenerator.class);
		doReturn(component).when(generator).generate(timezone);
		tzinfo.setGenerator(generator);

		tzinfo.setDefaultTimeZone(timezone);

		Collection<VTimezone> components = tzinfo.getComponents();
		assertEquals(1, components.size());
		assertTrue(components.contains(component));
		verify(generator).generate(timezone);
	}

	@Test
	public void set_getTimezone() {
		ICalProperty property1 = new ICalPropertyImpl();
		ICalProperty property2 = new ICalPropertyImpl();
		ICalProperty property3 = new ICalPropertyImpl();
		ICalProperty property4 = new ICalPropertyImpl();
		ICalProperty property5 = new ICalPropertyImpl();

		VTimezone defaultComponent = new VTimezone("default");
		TimeZone defaultTimezone = TimeZone.getDefault();
		tzinfo.assign(defaultComponent, defaultTimezone);

		TimeZone timezone1 = TestUtils.buildTimezone(1, 0);
		VTimezone component1 = new VTimezone("custom");
		tzinfo.assign(component1, timezone1);

		tzinfo.setDefaultTimeZone(defaultTimezone);
		tzinfo.setTimeZone(property1, timezone1);
		tzinfo.setTimeZone(property2, timezone1);
		tzinfo.setFloating(property3, true);
		tzinfo.setTimeZone(property5, TestUtils.buildTimezone(2, 0), false);

		assertEquals(timezone1, tzinfo.getTimeZone(property1));
		assertEquals(timezone1, tzinfo.getTimeZone(property2));
		assertEquals(null, tzinfo.getTimeZone(property3));
		assertEquals(null, tzinfo.getTimeZone(property4));

		assertEquals(timezone1, tzinfo.getTimeZoneToWriteIn(property1));
		assertEquals(timezone1, tzinfo.getTimeZoneToWriteIn(property2));
		assertEquals(defaultTimezone, tzinfo.getTimeZoneToWriteIn(property3));
		assertEquals(defaultTimezone, tzinfo.getTimeZoneToWriteIn(property4));

		assertEquals(component1, tzinfo.getComponent(property1));
		assertEquals(component1, tzinfo.getComponent(property2));
		assertEquals(null, tzinfo.getComponent(property3));
		assertEquals(null, tzinfo.getComponent(property4));

		assertFalse(tzinfo.isFloating(property1));
		assertFalse(tzinfo.isFloating(property2));
		assertTrue(tzinfo.isFloating(property3));
		assertFalse(tzinfo.isFloating(property4));

		assertFalse(tzinfo.hasSolidusTimezone(property1));
		assertFalse(tzinfo.hasSolidusTimezone(property2));
		assertFalse(tzinfo.hasSolidusTimezone(property3));
		assertFalse(tzinfo.hasSolidusTimezone(property4));
		assertTrue(tzinfo.hasSolidusTimezone(property5));

		Collection<VTimezone> components = tzinfo.getComponents();
		assertEquals(2, components.size());
		assertTrue(components.contains(defaultComponent));
		assertTrue(components.contains(component1));
	}

	@Test
	public void removeTimezone() {
		ICalProperty property = new ICalPropertyImpl();

		VTimezone defaultComponent = new VTimezone("tz");
		TimeZone defaultTimezone = TimeZone.getDefault();
		tzinfo.assign(defaultComponent, defaultTimezone);

		TimeZone timezone1 = TestUtils.buildTimezone(1, 0);
		VTimezone component1 = new VTimezone("tz2");
		tzinfo.assign(component1, timezone1);

		tzinfo.setDefaultTimeZone(defaultTimezone);

		tzinfo.setTimeZone(property, timezone1);
		assertEquals(timezone1, tzinfo.getTimeZone(property));
		assertEquals(timezone1, tzinfo.getTimeZoneToWriteIn(property));
		assertEquals(component1, tzinfo.getComponent(property));
		assertFalse(tzinfo.isFloating(property));

		tzinfo.setTimeZone(property, (TimeZone) null);
		assertEquals(null, tzinfo.getTimeZone(property));
		assertEquals(defaultTimezone, tzinfo.getTimeZoneToWriteIn(property));
		assertEquals(null, tzinfo.getComponent(property));
		assertFalse(tzinfo.isFloating(property));

		tzinfo.setFloating(property, true);
		assertEquals(null, tzinfo.getTimeZone(property));
		assertEquals(defaultTimezone, tzinfo.getTimeZoneToWriteIn(property));
		assertEquals(null, tzinfo.getComponent(property));
		assertTrue(tzinfo.isFloating(property));

		tzinfo.setFloating(property, false);
		assertEquals(null, tzinfo.getTimeZone(property));
		assertEquals(defaultTimezone, tzinfo.getTimeZoneToWriteIn(property));
		assertEquals(null, tzinfo.getComponent(property));
		assertFalse(tzinfo.isFloating(property));
	}

	private class ICalPropertyImpl extends ICalProperty {
		//empty
	}
}
