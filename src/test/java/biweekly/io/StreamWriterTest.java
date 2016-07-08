package biweekly.io;

import static biweekly.ICalVersion.V2_0;
import static biweekly.util.TestUtils.vtimezoneNewYork;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.property.ICalProperty;

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
public class StreamWriterTest {
	private StreamWriterImpl writer;
	private ICalendar ical;

	@Before
	public void before() {
		writer = spy(new StreamWriterImpl());
		ical = new ICalendar();
	}

	@Test
	public void empty_ical() throws Exception {
		writer.write(ical);

		verify(writer)._write(ical);
	}

	@Test
	public void unregistered_property() throws Exception {
		ical.addProperty(new TestProperty());

		try {
			writer.write(ical);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			//expected
		}

		verify(writer, never())._write(ical);
	}

	@Test
	public void registered_property() throws Exception {
		ical.setProductId("value");
		writer.write(ical);

		verify(writer)._write(ical);
	}

	@Test
	public void unregistered_component() throws Exception {
		ical.addComponent(new TestComponent());

		try {
			writer.write(ical);
			fail("Expected IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			//expected
		}

		verify(writer, never())._write(ical);
	}

	@Test
	public void registered_component() throws Exception {
		ical.addEvent(new VEvent());
		writer.write(ical);

		verify(writer)._write(ical);
	}

	@Test
	public void setGlobalTimezone() {
		TimeZone timezone = TimeZone.getTimeZone("America/New_York");
		VTimezone component = new VTimezone(timezone.getID());

		writer.setGlobalTimeZone(timezone, component);
		assertSame(timezone, writer.getGlobalTimeZone());
		assertSame(component, writer.getGlobalTimeZoneComponent());

		writer.setGlobalTimeZone(null, null);
		assertNull(writer.getGlobalTimeZone());
		assertNull(writer.getGlobalTimeZoneComponent());

		writer.setGlobalTimeZone(timezone, component);
		writer.setGlobalTimeZone(null, component);
		assertNull(writer.getGlobalTimeZone());
		assertNull(writer.getGlobalTimeZoneComponent());

		writer.setGlobalTimeZone(timezone, component);
		writer.setGlobalTimeZone(timezone, null);
		assertNull(writer.getGlobalTimeZone());
		assertNull(writer.getGlobalTimeZoneComponent());

		component.getTimezoneId().setValue("foobar");
		try {
			writer.setGlobalTimeZone(timezone, component);
			fail();
		} catch (IllegalArgumentException e) {
			//expected
		}

		component.getTimezoneId().setValue(null);
		try {
			writer.setGlobalTimeZone(timezone, component);
			fail();
		} catch (IllegalArgumentException e) {
			//expected
		}

		component.setTimezoneId((String) null);
		try {
			writer.setGlobalTimeZone(timezone, component);
			fail();
		} catch (IllegalArgumentException e) {
			//expected
		}

		component = vtimezoneNewYork();
		timezone = new ICalTimeZone(component);

		writer.setGlobalTimeZone(timezone, false);
		assertSame(timezone, writer.getGlobalTimeZone());
		assertSame(component, writer.getGlobalTimeZoneComponent());

		writer.setGlobalTimeZone(null, false);
		assertNull(writer.getGlobalTimeZone());
		assertNull(writer.getGlobalTimeZoneComponent());
	}

	private class StreamWriterImpl extends StreamWriter {
		@Override
		protected ICalVersion getTargetVersion() {
			return V2_0;
		}

		@Override
		protected void _write(ICalendar ical) throws IOException {
			//empty
		}

		public void close() throws IOException {
			//empty
		}
	}

	private class TestProperty extends ICalProperty {
		//empty
	}

	private class TestComponent extends ICalComponent {
		//empty
	}
}
