package biweekly.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import biweekly.component.VTimezone;

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
public class TzUrlDotOrgGeneratorTest {
	private final TimeZone timezone = TimeZone.getTimeZone("America/New_York");

	@Before
	public void before() {
		TzUrlDotOrgGenerator.clearCache();
	}

	@Test
	public void url() throws Exception {
		TzUrlDotOrgGenerator generator = spy(new TzUrlDotOrgGenerator(false));
		doReturn(ok()).when(generator).getInputStream(any(URI.class));

		VTimezone component = generator.generate(timezone);
		assertEquals("TEST", component.getTimezoneId().getValue());
		verify(generator).getInputStream(new URI("http://www.tzurl.org/zoneinfo/America/New_York"));
	}

	@Test
	public void url_outlook() throws Exception {
		TzUrlDotOrgGenerator generator = spy(new TzUrlDotOrgGenerator(true));
		doReturn(ok()).when(generator).getInputStream(any(URI.class));

		VTimezone component = generator.generate(timezone);
		assertEquals("TEST", component.getTimezoneId().getValue());
		verify(generator).getInputStream(new URI("http://www.tzurl.org/zoneinfo-outlook/America/New_York"));
	}

	@Test
	public void url_bad() throws Exception {
		TzUrlDotOrgGenerator generator = new TzUrlDotOrgGenerator(false);
		try {
			generator.generate(new SimpleTimeZone(0, "spaces not allowed"));
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(e.getCause() instanceof URISyntaxException);
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void not_found() throws Exception {
		TzUrlDotOrgGenerator generator = spy(new TzUrlDotOrgGenerator(false));
		doThrow(new FileNotFoundException()).when(generator).getInputStream(any(URI.class));

		generator.generate(timezone);
	}

	@Test(expected = IllegalArgumentException.class)
	public void no_icalendar() throws Exception {
		TzUrlDotOrgGenerator generator = spy(new TzUrlDotOrgGenerator(false));
		doReturn(noICalendar()).when(generator).getInputStream(any(URI.class));

		generator.generate(timezone);
	}

	@Test(expected = IllegalArgumentException.class)
	public void no_vtimezone() throws Exception {
		TzUrlDotOrgGenerator generator = spy(new TzUrlDotOrgGenerator(false));
		doReturn(noVTimezone()).when(generator).getInputStream(any(URI.class));

		generator.generate(timezone);
	}

	@Test
	public void no_tzid() throws Exception {
		TzUrlDotOrgGenerator generator = spy(new TzUrlDotOrgGenerator(false));
		doReturn(noTimezoneId()).when(generator).getInputStream(any(URI.class));

		VTimezone component = generator.generate(timezone);
		assertEquals(timezone.getID(), component.getTimezoneId().getValue());
	}

	@Test
	public void empty_tzid() throws Exception {
		TzUrlDotOrgGenerator generator = spy(new TzUrlDotOrgGenerator(false));
		doReturn(emptyTimezoneId()).when(generator).getInputStream(any(URI.class));

		VTimezone component = generator.generate(timezone);
		assertEquals(timezone.getID(), component.getTimezoneId().getValue());
	}

	@Test
	public void cache() throws Exception {
		TzUrlDotOrgGenerator generator = spy(new TzUrlDotOrgGenerator(true));
		doReturn(ok()).when(generator).getInputStream(any(URI.class));

		VTimezone component1 = generator.generate(timezone);
		VTimezone component2 = generator.generate(timezone);
		VTimezone component3 = generator.generate(timezone);
		assertNotSame(component1, component2);
		assertNotSame(component2, component3);
		assertNotSame(component1, component3);
		assertEquals("TEST", component1.getTimezoneId().getValue());
		assertEquals("TEST", component2.getTimezoneId().getValue());
		assertEquals("TEST", component3.getTimezoneId().getValue());
		verify(generator).getInputStream(any(URI.class));
	}

	private static InputStream noICalendar() {
		return mockInputStream("<html><title>500 Server Error</title></html>");
	}

	private static InputStream noVTimezone() {
		//@formatter:off
		return mockInputStream(
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
		"END:VCALENDAR\r\n"
		);
		//@formatter:on
	}

	private static InputStream noTimezoneId() {
		//@formatter:off
		return mockInputStream(
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VTIMEZONE\r\n" +
			"END:VTIMEZONE\r\n" +
		"END:VCALENDAR\r\n"
		);
		//@formatter:on
	}

	private static InputStream emptyTimezoneId() {
		//@formatter:off
		return mockInputStream(
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VTIMEZONE\r\n" +
				"TZID: \r\n" +
			"END:VTIMEZONE\r\n" +
		"END:VCALENDAR\r\n"
		);
		//@formatter:on
	}

	private static InputStream ok() {
		//@formatter:off
		return mockInputStream(
		"BEGIN:VCALENDAR\r\n" +
			"VERSION:2.0\r\n" +
			"BEGIN:VTIMEZONE\r\n" +
				"TZID:TEST\r\n" +
			"END:VTIMEZONE\r\n" +
		"END:VCALENDAR\r\n"
		);
		//@formatter:on
	}

	private static InputStream mockInputStream(String response) {
		return new ByteArrayInputStream(response.getBytes());
	}
}
