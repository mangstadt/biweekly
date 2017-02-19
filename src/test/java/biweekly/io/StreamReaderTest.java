package biweekly.io;

import static biweekly.util.TestUtils.assertParseWarnings;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.icalDate;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.ClassRule;
import org.junit.Test;

import biweekly.ICalendar;
import biweekly.component.DaylightSavingsTime;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.property.ICalProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.DefaultTimezoneRule;
import biweekly.util.ICalDate;
import biweekly.util.UtcOffset;

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
public class StreamReaderTest {
	@ClassRule
	public static final DefaultTimezoneRule tzRule = new DefaultTimezoneRule(-1, 0);

	@Test
	public void timezones() throws Exception {
		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				VTimezone timezone = new VTimezone("tz");
				{
					StandardTime standard = new StandardTime();
					standard.setDateStart(new DateTimeComponents(2014, 9, 1, 2, 0, 0, false));
					standard.setTimezoneOffsetFrom(new UtcOffset(true, 10, 0));
					standard.setTimezoneOffsetTo(new UtcOffset(true, 9, 0));
					timezone.addStandardTime(standard);

					DaylightSavingsTime daylight = new DaylightSavingsTime();
					daylight.setDateStart(new DateTimeComponents(2014, 1, 1, 2, 0, 0, false));
					daylight.setTimezoneOffsetFrom(new UtcOffset(true, 9, 0));
					daylight.setTimezoneOffsetTo(new UtcOffset(true, 10, 0));
					timezone.addDaylightSavingsTime(daylight);
				}
				ical.addComponent(timezone);

				ical.addComponent(new VTimezone((String) null));

				TestProperty floating = new TestProperty(icalDate("2014-09-21T10:22:00"));
				context.addFloatingDate(floating, floating.date);
				ical.addProperty(floating);

				String tzid = timezone.getTimezoneId().getValue();
				TestProperty timezoned = new TestProperty(icalDate("2014-10-01T13:07:00"));
				timezoned.getParameters().setTimezoneId(tzid);
				context.addTimezonedDate(tzid, timezoned, timezoned.date);
				ical.addProperty(timezoned);

				tzid = timezone.getTimezoneId().getValue();
				timezoned = new TestProperty(icalDate("2014-08-01T13:07:00"));
				timezoned.getParameters().setTimezoneId(tzid);
				context.addTimezonedDate(tzid, timezoned, timezoned.date);
				ical.addProperty(timezoned);

				tzid = timezone.getTimezoneId().getValue();
				timezoned = new TestProperty(icalDate("2013-12-01T13:07:00"));
				timezoned.getParameters().setTimezoneId(tzid);
				context.addTimezonedDate(tzid, timezoned, timezoned.date);
				ical.addProperty(timezoned);

				tzid = "/America/New_York";
				timezoned = new TestProperty(icalDate("2014-07-04T09:00:00"));
				timezoned.getParameters().setTimezoneId(tzid);
				context.addTimezonedDate(tzid, timezoned, timezoned.date);
				ical.addProperty(timezoned);

				tzid = "America/New_York";
				timezoned = new TestProperty(icalDate("2014-07-04T09:00:00"));
				timezoned.getParameters().setTimezoneId(tzid);
				context.addTimezonedDate(tzid, timezoned, timezoned.date);
				ical.addProperty(timezoned);

				tzid = "foobar";
				timezoned = new TestProperty(icalDate("2014-06-11T14:00:00"));
				timezoned.getParameters().setTimezoneId(tzid);
				context.addTimezonedDate(tzid, timezoned, timezoned.date);
				ical.addProperty(timezoned);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		Collection<TimezoneAssignment> components = tzinfo.getTimezones();
		assertEquals(3, components.size());
		assertEquals(1, ical.getComponents(VTimezone.class).size());

		Iterator<TestProperty> it = ical.getProperties(TestProperty.class).iterator();

		//floating-time property
		TestProperty property = it.next();
		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertTrue(tzinfo.isFloating(property));
		assertNull(assignment);
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(date("2014-09-21 10:22:00"), property.date);

		//timezoned property
		property = it.next();
		assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertEquals("tz", assignment.getComponent().getTimezoneId().getValue());
		assertTrue(assignment.getTimeZone() instanceof ICalTimeZone);
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-10-01 04:07:00"), property.date);

		//timezoned property
		property = it.next();
		assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertEquals("tz", assignment.getComponent().getTimezoneId().getValue());
		assertTrue(assignment.getTimeZone() instanceof ICalTimeZone);
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-08-01 03:07:00"), property.date);

		//timezoned property
		property = it.next();
		assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertEquals("tz", assignment.getComponent().getTimezoneId().getValue());
		assertTrue(assignment.getTimeZone() instanceof ICalTimeZone);
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2013-12-01 04:07:00"), property.date);

		//property with Olsen TZID
		property = it.next();
		assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment.getComponent());
		assertEquals("America/New_York", assignment.getTimeZone().getID());
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-07-04 13:00:00"), property.date);

		//property with Olsen TZID that doesn't point to a VTIMEZONE component
		property = it.next();
		assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment.getComponent());
		assertEquals("America/New_York", assignment.getTimeZone().getID());
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-07-04 13:00:00"), property.date);

		//property with TZID that doesn't point to a VTIMEZONE component
		property = it.next();
		assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment);
		assertEquals("foobar", property.getParameters().getTimezoneId());
		assertEquals(date("2014-06-11 14:00:00"), property.date);

		assertFalse(it.hasNext());
		assertParseWarnings(reader, 39, 37, 38);
	}

	@Test
	public void timezone_without_id() throws Exception {
		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				VTimezone timezone = new VTimezone((String) null);
				{
					StandardTime standard = new StandardTime();
					standard.setDateStart(new DateTimeComponents(2014, 9, 1, 2, 0, 0, false));
					standard.setTimezoneOffsetFrom(new UtcOffset(true, 10, 0));
					standard.setTimezoneOffsetTo(new UtcOffset(true, 9, 0));
					timezone.addStandardTime(standard);

					DaylightSavingsTime daylight = new DaylightSavingsTime();
					daylight.setDateStart(new DateTimeComponents(2014, 1, 1, 2, 0, 0, false));
					daylight.setTimezoneOffsetFrom(new UtcOffset(true, 9, 0));
					daylight.setTimezoneOffsetTo(new UtcOffset(true, 10, 0));
					timezone.addDaylightSavingsTime(daylight);
				}
				ical.addComponent(timezone);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();
		assertEquals(0, tzinfo.getTimezones().size());
		assertEquals(1, ical.getComponents(VTimezone.class).size());
		assertParseWarnings(reader, 39);
	}

	private abstract class StreamReaderImpl extends StreamReader {
		public void close() throws IOException {
			//empty
		}
	}

	private class TestProperty extends ICalProperty {
		private final ICalDate date;

		public TestProperty(ICalDate date) {
			this.date = date;
		}
	}
}
