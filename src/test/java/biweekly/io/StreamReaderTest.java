package biweekly.io;

import static biweekly.util.TestUtils.assertParseWarnings;
import static biweekly.util.TestUtils.buildTimezone;
import static biweekly.util.TestUtils.date;
import static biweekly.util.TestUtils.icalDate;
import static biweekly.util.TestUtils.utc;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.TimeZone;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

/*
 Copyright (c) 2013-2020, Michael Angstadt
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
	public void floating_date() throws Exception {
		final ICalPropertyImpl floating = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addFloatingDate(floating, floating.date);
				ical.addProperty(floating);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(floating);
		assertTrue(tzinfo.isFloating(floating));
		assertNull(assignment);
		assertNull(floating.getParameters().getTimezoneId());
		assertEquals(date("2014-09-02 10:22:00"), floating.date); //should be parsed under default timezone

		assertParseWarnings(reader);
	}

	@Test
	public void floating_date_different_default_timezone() throws Exception {
		final ICalPropertyImpl floating = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addFloatingDate(floating, floating.date);
				ical.addProperty(floating);

				return ical;
			}
		};

		reader.setDefaultTimezone(buildTimezone(1, 0));

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(floating);
		assertTrue(tzinfo.isFloating(floating));
		assertNull(assignment);
		assertNull(floating.getParameters().getTimezoneId());
		assertEquals(date("2014-09-02 08:22:00"), floating.date);

		assertParseWarnings(reader);
	}

	@Test
	public void date_assigned_to_vtimezone_component() throws Exception {
		final VTimezone vtimezone = new VTimezone("tz");
		{
			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(2014, 9, 1, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(new UtcOffset(true, 10, 0));
			standard.setTimezoneOffsetTo(new UtcOffset(true, 9, 0));
			vtimezone.addStandardTime(standard);

			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setDateStart(new DateTimeComponents(2014, 1, 1, 2, 0, 0, false));
			daylight.setTimezoneOffsetFrom(new UtcOffset(true, 9, 0));
			daylight.setTimezoneOffsetTo(new UtcOffset(true, 10, 0));
			vtimezone.addDaylightSavingsTime(daylight);
		}

		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId(vtimezone.getTimezoneId().getValue());

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				ical.addComponent(vtimezone);

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		//VTIMEZONE component should be removed from the iCalendar object
		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertSame(vtimezone, assignment.getComponent());
		assertNull(assignment.getGlobalId());
		assertTrue(assignment.getTimeZone() instanceof ICalTimeZone);
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-09-02 01:22:00"), property.date);

		assertParseWarnings(reader);
	}

	@Test
	public void date_with_olsen_id() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("/America/New_York");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment.getComponent());
		assertEquals("America/New_York", assignment.getGlobalId());
		assertEquals("America/New_York", assignment.getTimeZone().getID());
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-09-02 14:22:00"), property.date);

		assertParseWarnings(reader);
	}

	@Test
	public void date_with_olsen_id_with_mozilla_prefix() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("/mozilla.org/20050126_1/America/New_York");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment.getComponent());
		assertEquals("mozilla.org/20050126_1/America/New_York", assignment.getGlobalId());
		assertEquals("America/New_York", assignment.getTimeZone().getID());
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-09-02 14:22:00"), property.date);

		assertParseWarnings(reader);
	}

	@Test
	public void date_with_olsen_id_missing_solidus() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("America/New_York");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment.getComponent());
		assertEquals("America/New_York", assignment.getGlobalId());
		assertEquals("America/New_York", assignment.getTimeZone().getID());
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-09-02 14:22:00"), property.date);

		assertParseWarnings(reader, 37);
	}

	@Test
	public void date_with_olsen_id_with_mozilla_prefix_missing_solidus() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("mozilla.org/20050126_1/America/New_York");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment.getComponent());
		assertEquals("mozilla.org/20050126_1/America/New_York", assignment.getGlobalId());
		assertEquals("America/New_York", assignment.getTimeZone().getID());
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-09-02 14:22:00"), property.date);

		assertParseWarnings(reader, 37);
	}

	@Test
	public void date_with_olsen_id_that_also_matches_vtimezone_component() throws Exception {
		/*
		 * This timezone definition will be ignored because its TZID is
		 * formatted as a valid Olsen ID. Because the Olsen ID is valid,
		 * biweekly will not look for a VTIMEZONE component that matches it.
		 */
		final VTimezone vtimezone = new VTimezone("/Europe/Paris");
		{
			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(2014, 9, 1, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(new UtcOffset(true, 10, 0));
			standard.setTimezoneOffsetTo(new UtcOffset(true, 9, 0));
			vtimezone.addStandardTime(standard);

			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setDateStart(new DateTimeComponents(2014, 1, 1, 2, 0, 0, false));
			daylight.setTimezoneOffsetFrom(new UtcOffset(true, 9, 0));
			daylight.setTimezoneOffsetTo(new UtcOffset(true, 10, 0));
			vtimezone.addDaylightSavingsTime(daylight);
		}

		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("/Europe/Paris");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				ical.addComponent(vtimezone);

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment.getComponent());
		assertEquals("Europe/Paris", assignment.getGlobalId());
		assertEquals("Europe/Paris", assignment.getTimeZone().getID());
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-09-02 08:22:00"), property.date);

		assertParseWarnings(reader);
	}

	@Test
	public void date_with_unrecognized_olsen_id_that_matches_vtimezone_component() throws Exception {
		final VTimezone vtimezone = new VTimezone("/Foo/Bar");
		{
			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(2014, 9, 1, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(new UtcOffset(true, 10, 0));
			standard.setTimezoneOffsetTo(new UtcOffset(true, 9, 0));
			vtimezone.addStandardTime(standard);

			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setDateStart(new DateTimeComponents(2014, 1, 1, 2, 0, 0, false));
			daylight.setTimezoneOffsetFrom(new UtcOffset(true, 9, 0));
			daylight.setTimezoneOffsetTo(new UtcOffset(true, 10, 0));
			vtimezone.addDaylightSavingsTime(daylight);
		}

		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("/Foo/Bar");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				ical.addComponent(vtimezone);

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertSame(vtimezone, assignment.getComponent());
		assertNull(assignment.getGlobalId());
		assertTrue(assignment.getTimeZone() instanceof ICalTimeZone);
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-09-02 01:22:00"), property.date);

		assertParseWarnings(reader, 43);
	}

	@Test
	public void date_with_unrecognized_olsen_id_that_does_not_match_vtimezone_component() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("/Invalid/TZID");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment);
		assertEquals("/Invalid/TZID", property.getParameters().getTimezoneId());
		assertEquals(date("2014-09-02 10:22:00"), property.date); //should be parsed under default timezone

		assertParseWarnings(reader, 38);
	}

	@Test
	public void date_with_unrecognized_olsen_id_that_does_not_match_vtimezone_component_different_default_timezone() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("/Invalid/TZID");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		reader.setDefaultTimezone(buildTimezone(1, 0));

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment);
		assertEquals("/Invalid/TZID", property.getParameters().getTimezoneId());
		assertEquals(date("2014-09-02 08:22:00"), property.date); //should be parsed under default timezone

		assertParseWarnings(reader, 38);
	}

	@Test
	public void date_with_tzid_that_does_not_match_anything() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("invalid");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment);
		assertEquals("invalid", property.getParameters().getTimezoneId());
		assertEquals(date("2014-09-02 10:22:00"), property.date); //should be parsed under default timezone

		assertParseWarnings(reader, 38);
	}

	@Test
	public void date_with_tzid_that_does_not_match_anything_different_default_timezone() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("invalid");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		reader.setDefaultTimezone(buildTimezone(1, 0));

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(0, ical.getComponents(VTimezone.class).size());

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment);
		assertEquals("invalid", property.getParameters().getTimezoneId());
		assertEquals(date("2014-09-02 08:22:00"), property.date);

		assertParseWarnings(reader, 38);
	}

	@Test
	public void date_with_global_id_using_custom_resolver() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("/New York City");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		GlobalTimezoneIdResolver resolver = mock(GlobalTimezoneIdResolver.class);
		when(resolver.resolve("New York City")).thenReturn(TimeZone.getTimeZone("America/New_York"));
		reader.setGlobalTimezoneIdResolver(resolver);

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment.getComponent());
		assertEquals("New York City", assignment.getGlobalId());
		assertEquals("America/New_York", assignment.getTimeZone().getID());
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-09-02 14:22:00"), property.date);
		verify(resolver).resolve("New York City");

		assertParseWarnings(reader);
	}

	@Test
	public void date_with_global_id_using_custom_resolver_missing_solidus() throws Exception {
		final ICalPropertyImpl property = new ICalPropertyImpl(icalDate("2014-09-02T10:22:00"));
		property.getParameters().setTimezoneId("New York City");

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				context.addTimezonedDate(property.getParameters().getTimezoneId(), property, property.date);
				ical.addProperty(property);

				return ical;
			}
		};

		GlobalTimezoneIdResolver resolver = mock(GlobalTimezoneIdResolver.class);
		when(resolver.resolve("New York City")).thenReturn(TimeZone.getTimeZone("America/New_York"));
		reader.setGlobalTimezoneIdResolver(resolver);

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		TimezoneAssignment assignment = tzinfo.getTimezone(property);
		assertFalse(tzinfo.isFloating(property));
		assertNull(assignment.getComponent());
		assertEquals("New York City", assignment.getGlobalId());
		assertEquals("America/New_York", assignment.getTimeZone().getID());
		assertNull(property.getParameters().getTimezoneId());
		assertEquals(utc("2014-09-02 14:22:00"), property.date);
		verify(resolver).resolve("New York City");

		assertParseWarnings(reader, 37);
	}

	@Test
	public void timezone_component_without_id() throws Exception {
		final VTimezone vtimezone = new VTimezone((String) null);
		{
			StandardTime standard = new StandardTime();
			standard.setDateStart(new DateTimeComponents(2014, 9, 1, 2, 0, 0, false));
			standard.setTimezoneOffsetFrom(new UtcOffset(true, 10, 0));
			standard.setTimezoneOffsetTo(new UtcOffset(true, 9, 0));
			vtimezone.addStandardTime(standard);

			DaylightSavingsTime daylight = new DaylightSavingsTime();
			daylight.setDateStart(new DateTimeComponents(2014, 1, 1, 2, 0, 0, false));
			daylight.setTimezoneOffsetFrom(new UtcOffset(true, 9, 0));
			daylight.setTimezoneOffsetTo(new UtcOffset(true, 10, 0));
			vtimezone.addDaylightSavingsTime(daylight);
		}

		StreamReader reader = new StreamReaderImpl() {
			@Override
			protected ICalendar _readNext() {
				ICalendar ical = new ICalendar();

				ical.addComponent(vtimezone);

				return ical;
			}
		};

		ICalendar ical = reader.readNext();
		TimezoneInfo tzinfo = ical.getTimezoneInfo();

		assertEquals(1, ical.getComponents(VTimezone.class).size());

		assertEquals(0, tzinfo.getTimezones().size());

		assertParseWarnings(reader, 39);
	}

	private abstract class StreamReaderImpl extends StreamReader {
		@Override
		public void close() throws IOException {
			//empty
		}
	}

	private class ICalPropertyImpl extends ICalProperty {
		private final ICalDate date;

		public ICalPropertyImpl(ICalDate date) {
			this.date = date;
		}
	}
}
