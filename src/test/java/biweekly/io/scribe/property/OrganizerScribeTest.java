package biweekly.io.scribe.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import biweekly.io.ParseContext;
import biweekly.io.DataModelConversionException;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.Organizer;

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
public class OrganizerScribeTest extends ScribeTest<Organizer> {
	private final String name = "John Doe";
	private final String email = "jdoe@example.com";
	private final String uri = "http://example.com/jdoe";

	private final Organizer empty = new Organizer(null, null);
	private final Organizer withEmail = new Organizer(null, email);
	private final Organizer withNameEmail = new Organizer(name, email);
	private final Organizer withNameEmailUri = new Organizer(name, email);
	{
		withNameEmailUri.setUri(uri);
	}

	public OrganizerScribeTest() {
		super(new OrganizerScribe());
	}

	@Test
	public void prepareParameters() {
		sensei.assertPrepareParams(withEmail).run();
		sensei.assertPrepareParams(withNameEmail).expected("CN", name).run();
		sensei.assertPrepareParams(withNameEmailUri).expected("CN", name).run();
	}

	@Test
	public void writeText() {
		try {
			sensei.assertWriteText(withEmail).version(V1_0).run();
		} catch (DataModelConversionException e) {
			assertSame(withEmail, e.getOriginalProperty());
			Attendee expected = new Attendee(null, email);
			expected.setRole(Role.ORGANIZER);
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
		sensei.assertWriteText(withEmail).version(V2_0_DEPRECATED).run("mailto:" + email);
		sensei.assertWriteText(withEmail).version(V2_0).run("mailto:" + email);

		try {
			sensei.assertWriteText(withNameEmail).version(V1_0).run();
		} catch (DataModelConversionException e) {
			assertSame(withNameEmail, e.getOriginalProperty());
			Attendee expected = new Attendee(name, email);
			expected.setRole(Role.ORGANIZER);
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
		sensei.assertWriteText(withNameEmail).version(V2_0_DEPRECATED).run("mailto:" + email);
		sensei.assertWriteText(withNameEmail).version(V2_0).run("mailto:" + email);

		try {
			sensei.assertWriteText(withNameEmailUri).version(V1_0).run();
		} catch (DataModelConversionException e) {
			assertSame(withNameEmailUri, e.getOriginalProperty());
			Attendee expected = new Attendee(name, email);
			expected.setRole(Role.ORGANIZER);
			expected.setUri(uri);
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
		sensei.assertWriteText(withNameEmailUri).version(V2_0_DEPRECATED).run(uri);
		sensei.assertWriteText(withNameEmailUri).version(V2_0).run(uri);

		try {
			sensei.assertWriteText(empty).version(V1_0).run();
		} catch (DataModelConversionException e) {
			assertSame(empty, e.getOriginalProperty());
			Attendee expected = new Attendee(null, null);
			expected.setRole(Role.ORGANIZER);
			assertEquals(Arrays.asList(expected), e.getProperties());
			assertEquals(Arrays.asList(), e.getComponents());
		}
		sensei.assertWriteText(empty).version(V2_0_DEPRECATED).run("");
		sensei.assertWriteText(empty).version(V2_0).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("mailto:" + email).run(check(null, email, null));
		sensei.assertParseText("mailto:" + email).param("CN", name).run(check(name, email, null));
		sensei.assertParseText("MAILTO:" + email).run(check(null, email, null));
		sensei.assertParseText("MAILTO:" + email).param("CN", name).run(check(name, email, null));
		sensei.assertParseText("http:" + email).run(check(null, null, "http:" + email));
		sensei.assertParseText("http:" + email).param("CN", name).run(check(name, null, "http:" + email));
		sensei.assertParseText("mallto:" + email).run(check(null, null, "mallto:" + email));
		sensei.assertParseText("mallto:" + email).param("CN", name).run(check(name, null, "mallto:" + email));
		sensei.assertParseText(uri).run(check(null, null, uri));
	}

	private Check<Organizer> check(final String name, final String email, final String uri) {
		return new Check<Organizer>() {
			public void check(Organizer property, ParseContext context) {
				assertTrue(property.getParameters().isEmpty());
				assertEquals(name, property.getCommonName());
				assertEquals(email, property.getEmail());
				assertEquals(uri, property.getUri());
			}
		};
	}
}
