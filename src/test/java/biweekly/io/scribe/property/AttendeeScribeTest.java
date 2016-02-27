package biweekly.io.scribe.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.io.ParseContext;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.parameter.ParticipationLevel;
import biweekly.parameter.ParticipationStatus;
import biweekly.parameter.Role;
import biweekly.property.Attendee;

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
public class AttendeeScribeTest extends ScribeTest<Attendee> {
	private final String name = "John Doe";
	private final String email = "jdoe@example.com";
	private final String uri = "http://example.com/jdoe";

	private final Attendee empty = new Attendee(null, null);
	private final Attendee withEmail = new Attendee(null, email);
	private final Attendee withName = new Attendee(name, null);
	private final Attendee withNameEmail = new Attendee(name, email);
	private final Attendee withNameEmailUri = new Attendee(name, email, uri);

	private final Attendee withRoleUri = new Attendee(null, null, uri);
	{
		withRoleUri.setRole(Role.ATTENDEE);
	}

	public AttendeeScribeTest() {
		super(new AttendeeScribe());
	}

	@Test
	public void prepareParameters_cn() {
		sensei.assertPrepareParams(withNameEmail).versions(V1_0).run();
		sensei.assertPrepareParams(withNameEmail).versions(V2_0_DEPRECATED, V2_0).expected("CN", name).run();
	}

	@Test
	public void prepareParameters_email() {
		sensei.assertPrepareParams(withNameEmailUri).versions(V1_0).run();
		sensei.assertPrepareParams(withNameEmailUri).versions(V2_0_DEPRECATED, V2_0).expected("CN", name).expected("EMAIL", email).run();
	}

	@Test
	public void prepareParameters_rsvp() {
		Attendee property = new Attendee(null, null, uri);

		property.setRsvp(true);
		sensei.assertPrepareParams(property).versions(V1_0).expected("RSVP", "YES").run();
		sensei.assertPrepareParams(property).versions(V2_0_DEPRECATED, V2_0).expected("RSVP", "TRUE").run();

		property.setRsvp(false);
		sensei.assertPrepareParams(property).versions(V1_0).expected("RSVP", "NO").run();
		sensei.assertPrepareParams(property).versions(V2_0_DEPRECATED, V2_0).expected("RSVP", "FALSE").run();
	}

	@Test
	public void prepareParameters_level() {
		Attendee property = new Attendee(null, null, uri);
		property.setParticipationLevel(ParticipationLevel.OPTIONAL);
		sensei.assertPrepareParams(property).versions(V1_0).expected("EXPECT", "REQUEST").run();
		sensei.assertPrepareParams(property).versions(V2_0_DEPRECATED, V2_0).expected("ROLE", "OPT-PARTICIPANT").run();
	}

	@Test
	public void prepareParameters_level_chair_role() {
		Attendee property = new Attendee(null, null, uri);
		property.setParticipationLevel(ParticipationLevel.OPTIONAL);
		property.setRole(Role.CHAIR);
		sensei.assertPrepareParams(property).versions(V1_0).expected("EXPECT", "REQUEST").expected("ROLE", "CHAIR").run();
		sensei.assertPrepareParams(property).versions(V2_0_DEPRECATED, V2_0).expected("ROLE", "CHAIR").run();
	}

	@Test
	public void prepareParameters_role() {
		Attendee property = new Attendee(null, null, uri);
		property.setRole(Role.ORGANIZER);
		sensei.assertPrepareParams(property).expected("ROLE", "ORGANIZER").run();
	}

	@Test
	public void prepareParameters_status() {
		Attendee property = new Attendee(null, null, uri);
		property.setParticipationStatus(ParticipationStatus.ACCEPTED);
		sensei.assertPrepareParams(property).versions(V1_0).expected("STATUS", "ACCEPTED").run();
		sensei.assertPrepareParams(property).versions(V2_0_DEPRECATED, V2_0).expected("PARTSTAT", "ACCEPTED").run();
	}

	@Test
	public void prepareParameters_status_needs_action() {
		Attendee property = new Attendee(null, null, uri);
		property.setParticipationStatus(ParticipationStatus.NEEDS_ACTION);
		sensei.assertPrepareParams(property).versions(V1_0).expected("STATUS", "NEEDS ACTION").run();
		sensei.assertPrepareParams(property).versions(V2_0_DEPRECATED, V2_0).expected("PARTSTAT", "NEEDS-ACTION").run();
	}

	@Test
	public void dataType() {
		Attendee property = new Attendee(name, email, uri);
		sensei.assertDataType(property).versions(V1_0).run(ICalDataType.URL);
		sensei.assertDataType(property).versions(V2_0_DEPRECATED, V2_0).run(ICalDataType.CAL_ADDRESS);
	}

	@Test
	public void dataType_uri() {
		Attendee property = new Attendee(null, null, uri);
		sensei.assertDataType(property).versions(V1_0).run(ICalDataType.URL);
		sensei.assertDataType(property).versions(V2_0_DEPRECATED, V2_0).run(ICalDataType.CAL_ADDRESS);
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(withEmail).version(V1_0).run(email);
		sensei.assertWriteText(withEmail).version(V2_0_DEPRECATED).run("mailto:" + email);
		sensei.assertWriteText(withEmail).version(V2_0).run("mailto:" + email);

		sensei.assertWriteText(empty).run("");
		sensei.assertWriteText(withName).run("");

		sensei.assertWriteText(withNameEmail).version(V1_0).run(name + " <" + email + ">");
		sensei.assertWriteText(withNameEmail).version(V2_0_DEPRECATED).run("mailto:" + email);
		sensei.assertWriteText(withNameEmail).version(V2_0).run("mailto:" + email);

		sensei.assertWriteText(withNameEmailUri).version(V1_0).run(uri);
		sensei.assertWriteText(withNameEmailUri).version(V2_0_DEPRECATED).run(uri);
		sensei.assertWriteText(withNameEmailUri).version(V2_0).run(uri);
	}

	@Test
	public void parseText() {
		sensei.assertParseText(uri).versions(V1_0).dataType(ICalDataType.URL).run(check(null, null, uri));
		sensei.assertParseText(uri).versions(V1_0).run(check(null, uri, null));

		sensei.assertParseText(name + " <" + email + ">").versions(V1_0).run(check(name, email, null));
		sensei.assertParseText(name + " <" + email + ">").versions(V2_0_DEPRECATED, V2_0).run(check(null, null, name + " <" + email + ">"));

		sensei.assertParseText(name + " <" + email + ">").param("STATUS", "ACCEPTED").versions(V1_0).run(new Check<Attendee>() {
			public void check(Attendee property, ParseContext context) {
				assertTrue(property.getParameters().isEmpty());
				assertEquals(name, property.getCommonName());
				assertEquals(email, property.getEmail());
				assertNull(property.getUri());
				assertEquals(ParticipationStatus.ACCEPTED, property.getParticipationStatus());
			}
		});

		sensei.assertParseText("mailto:" + email).param("PARTSTAT", "ACCEPTED").versions(V2_0_DEPRECATED, V2_0).run(new Check<Attendee>() {
			public void check(Attendee property, ParseContext context) {
				assertTrue(property.getParameters().isEmpty());
				assertNull(property.getCommonName());
				assertEquals(email, property.getEmail());
				assertNull(property.getUri());
				assertEquals(ParticipationStatus.ACCEPTED, property.getParticipationStatus());
			}
		});

		sensei.assertParseText(name + " >" + email + "<").versions(V1_0).run(check(null, name + " >" + email + "<", null));
		sensei.assertParseText(name + " >" + email + "<").versions(V1_0).dataType(ICalDataType.URL).run(check(null, null, name + " >" + email + "<"));
		sensei.assertParseText(name + " <" + email).versions(V1_0).run(check(null, name + " <" + email, null));
		sensei.assertParseText(name + email + ">").versions(V1_0).run(check(null, name + email + ">", null));

		sensei.assertParseText("mailto:" + email).versions(V1_0).run(check(null, "mailto:" + email, null));
		sensei.assertParseText("mailto:" + email).param("CN", name).versions(V2_0_DEPRECATED, V2_0).run(check(name, email, null));
		sensei.assertParseText("MAILTO:" + email).versions(V1_0).run(check(null, "MAILTO:" + email, null));
		sensei.assertParseText("MAILTO:" + email).param("CN", name).versions(V2_0_DEPRECATED, V2_0).run(check(name, email, null));
		sensei.assertParseText("mallto:" + email).versions(V1_0).run(check(null, "mallto:" + email, null));
		sensei.assertParseText("mallto:" + email).param("CN", name).versions(V2_0_DEPRECATED, V2_0).run(check(name, null, "mallto:" + email));
		sensei.assertParseText("http:" + email).versions(V1_0).run(check(null, "http:" + email, null));
		sensei.assertParseText("http:" + email).param("CN", name).param("EMAIL", email).versions(V2_0_DEPRECATED, V2_0).run(check(name, email, "http:" + email));
	}

	private Check<Attendee> check(final String name, final String email, final String uri) {
		return new Check<Attendee>() {
			public void check(Attendee property, ParseContext context) {
				assertTrue(property.getParameters().isEmpty());
				assertEquals(name, property.getCommonName());
				assertEquals(email, property.getEmail());
				assertEquals(uri, property.getUri());
			}
		};
	}

	@Test
	public void parseText_level() {
		sensei.assertParseText(uri).param("EXPECT", "REQUIRE").versions(V1_0).run(checkLevel(ParticipationLevel.REQUIRED));
		sensei.assertParseText(uri).param("EXPECT", "REQUEST").versions(V1_0).run(checkLevel(ParticipationLevel.OPTIONAL));
		sensei.assertParseText(uri).param("EXPECT", "FYI").versions(V1_0).run(checkLevel(ParticipationLevel.FYI));
		sensei.assertParseText(uri).param("EXPECT", "invalid").versions(V1_0).run(checkLevel(ParticipationLevel.get("invalid")));
		sensei.assertParseText(uri).param("EXPECT", "REQUIRE").versions(V2_0_DEPRECATED, V2_0).run(new Check<Attendee>() {
			public void check(Attendee property, ParseContext context) {
				assertEquals("REQUIRE", property.getParameter("EXPECT"));
				assertNull(property.getParticipationLevel());
			}
		});

		sensei.assertParseText(uri).param("ROLE", "REQ-PARTICIPANT").versions(V2_0_DEPRECATED, V2_0).run(checkLevel(ParticipationLevel.REQUIRED));
		sensei.assertParseText(uri).param("ROLE", "OPT-PARTICIPANT").versions(V2_0_DEPRECATED, V2_0).run(checkLevel(ParticipationLevel.OPTIONAL));
		sensei.assertParseText(uri).param("ROLE", "NON-PARTICIPANT").versions(V2_0_DEPRECATED, V2_0).run(checkLevel(ParticipationLevel.FYI));
		sensei.assertParseText(uri).param("ROLE", "invalid").versions(V2_0_DEPRECATED, V2_0).run(new Check<Attendee>() {
			public void check(Attendee property, ParseContext context) {
				assertTrue(property.getParameters().isEmpty());
				assertNull(property.getParticipationLevel());
				assertEquals(Role.get("invalid"), property.getRole());
			}
		});
		sensei.assertParseText(uri).param("ROLE", "REQ-PARTICIPANT").versions(V1_0).run(new Check<Attendee>() {
			public void check(Attendee property, ParseContext context) {
				assertTrue(property.getParameters().isEmpty());
				assertEquals(Role.get("REQ-PARTICIPANT"), property.getRole());
				assertNull(property.getParticipationLevel());
			}
		});
	}

	private Check<Attendee> checkLevel(final ParticipationLevel level) {
		return new Check<Attendee>() {
			public void check(Attendee property, ParseContext context) {
				assertTrue(property.getParameters().isEmpty());
				assertEquals(level, property.getParticipationLevel());
			}
		};
	}

	@Test
	public void parseText_role() {
		sensei.assertParseText(uri).param("ROLE", "OPT-PARTICIPANT").versions(V2_0_DEPRECATED, V2_0).run(checkRole(ParticipationLevel.OPTIONAL, null));
		sensei.assertParseText(uri).param("ROLE", "CHAIR").versions(V2_0_DEPRECATED, V2_0).run(checkRole(null, Role.CHAIR));
		sensei.assertParseText(uri).param("ROLE", "ATTENDEE").run(checkRole(null, Role.ATTENDEE));
		sensei.assertParseText(uri).param("ROLE", "invalid").run(checkRole(null, Role.get("invalid")));
	}

	private Check<Attendee> checkRole(final ParticipationLevel level, final Role role) {
		return new Check<Attendee>() {
			public void check(Attendee property, ParseContext context) {
				assertNull(property.getParameter("ROLE"));
				assertEquals(level, property.getParticipationLevel());
				assertEquals(role, property.getRole());
			}
		};
	}

	@Test
	public void parseText_rsvp() {
		sensei.assertParseText(uri).run(checkRsvp(null, null));
		sensei.assertParseText(uri).param("RSVP", "YES").versions(V1_0).run(checkRsvp(null, true));
		sensei.assertParseText(uri).param("RSVP", "NO").versions(V1_0).run(checkRsvp(null, false));
		sensei.assertParseText(uri).param("RSVP", "TRUE").versions(V2_0_DEPRECATED, V2_0).run(checkRsvp(null, true));
		sensei.assertParseText(uri).param("RSVP", "FALSE").versions(V2_0_DEPRECATED, V2_0).run(checkRsvp(null, false));
		sensei.assertParseText(uri).param("RSVP", "invalid").run(checkRsvp("invalid", null));
	}

	private Check<Attendee> checkRsvp(final String paramValue, final Boolean value) {
		return new Check<Attendee>() {
			public void check(Attendee property, ParseContext context) {
				assertEquals(paramValue, property.getParameter("RSVP"));
				assertEquals(value, property.getRsvp());
			}
		};
	}
}
