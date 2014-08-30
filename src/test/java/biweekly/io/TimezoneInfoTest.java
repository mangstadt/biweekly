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
public class TimezoneInfoTest {
	private TimezoneInfo tzinfo;

	@Before
	public void before() {
		tzinfo = new TimezoneInfo();
	}

	@Test(expected = IllegalArgumentException.class)
	public void assign_no_tzid() {
		VTimezone component = new VTimezone(null);
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
	public void setDefaultTimezone() {
		TimeZone timezone = TimeZone.getDefault();
		VTimezone component = new VTimezone("tz");

		TimezoneTranslator translator = mock(TimezoneTranslator.class);
		doReturn(component).when(translator).toICalVTimezone(timezone);
		tzinfo.setTranslator(translator);

		tzinfo.setDefaultTimezone(timezone);

		Collection<VTimezone> components = tzinfo.getComponents();
		assertEquals(1, components.size());
		assertTrue(components.contains(component));
		verify(translator).toICalVTimezone(timezone);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setDefaultTimezone_no_tzid() {
		VTimezone component = new VTimezone(null);
		tzinfo.setDefaultTimezone(component);
	}

	@Test(expected = IllegalArgumentException.class)
	public void setDefaultTimezone_empty_tzid() {
		VTimezone component = new VTimezone("  ");
		tzinfo.setDefaultTimezone(component);
	}

	@Test
	public void set_getTimezone() {
		ICalProperty property1 = new ICalPropertyImpl();
		ICalProperty property2 = new ICalPropertyImpl();
		ICalProperty property3 = new ICalPropertyImpl();
		ICalProperty property4 = new ICalPropertyImpl();

		VTimezone defaultComponent = new VTimezone("default");
		TimeZone defaultTimezone = TimeZone.getDefault();
		tzinfo.assign(defaultComponent, defaultTimezone);

		TimeZone property1Timezone = TestUtils.buildTimezone(1, 0);
		VTimezone property1Component = new VTimezone("custom");
		tzinfo.assign(property1Component, property1Timezone);

		tzinfo.setDefaultTimezone(defaultTimezone);
		tzinfo.setTimezone(property1, property1Component);
		tzinfo.setTimezone(property2, property1Timezone);
		tzinfo.setUseFloatingTime(property3, true);

		assertEquals(property1Timezone, tzinfo.getTimeZone(property1));
		assertEquals(property1Timezone, tzinfo.getTimeZone(property2));
		assertEquals(defaultTimezone, tzinfo.getTimeZone(property3));
		assertEquals(defaultTimezone, tzinfo.getTimeZone(property4));

		assertEquals("custom", tzinfo.getTimezoneId(property1));
		assertEquals("custom", tzinfo.getTimezoneId(property2));
		assertEquals("default", tzinfo.getTimezoneId(property3));
		assertEquals("default", tzinfo.getTimezoneId(property4));

		assertFalse(tzinfo.usesFloatingTime(property1));
		assertFalse(tzinfo.usesFloatingTime(property2));
		assertTrue(tzinfo.usesFloatingTime(property3));
		assertFalse(tzinfo.usesFloatingTime(property4));

		Collection<VTimezone> components = tzinfo.getComponents();
		assertEquals(2, components.size());
		assertTrue(components.contains(defaultComponent));
		assertTrue(components.contains(property1Component));
	}

	@Test
	public void removeTimezone() {
		ICalProperty property = new ICalPropertyImpl();

		VTimezone defaultComponent = new VTimezone("tz");
		TimeZone defaultTimezone = TimeZone.getDefault();
		tzinfo.assign(defaultComponent, defaultTimezone);

		TimeZone property1Timezone = TestUtils.buildTimezone(1, 0);
		VTimezone property1Component = new VTimezone("tz2");
		tzinfo.assign(property1Component, property1Timezone);

		tzinfo.setDefaultTimezone(defaultTimezone);

		tzinfo.setTimezone(property, property1Component);
		assertEquals(property1Timezone, tzinfo.getTimeZone(property));
		assertEquals("tz2", tzinfo.getTimezoneId(property));
		assertFalse(tzinfo.usesFloatingTime(property));

		tzinfo.setTimezone(property, (TimeZone) null);
		assertEquals(defaultTimezone, tzinfo.getTimeZone(property));
		assertEquals("tz", tzinfo.getTimezoneId(property));
		assertFalse(tzinfo.usesFloatingTime(property));

		tzinfo.setUseFloatingTime(property, true);
		assertEquals(defaultTimezone, tzinfo.getTimeZone(property));
		assertEquals("tz", tzinfo.getTimezoneId(property));
		assertTrue(tzinfo.usesFloatingTime(property));

		tzinfo.setUseFloatingTime(property, false);
		assertEquals(defaultTimezone, tzinfo.getTimeZone(property));
		assertEquals("tz", tzinfo.getTimezoneId(property));
		assertFalse(tzinfo.usesFloatingTime(property));
	}

	private class ICalPropertyImpl extends ICalProperty {
		//empty
	}
}
