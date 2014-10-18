package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import biweekly.io.ParseContext;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.Daylight;
import biweekly.util.DateTimeComponents;
import biweekly.util.UtcOffset;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
public class DaylightScribeTest {
	private final DaylightScribe scribe = new DaylightScribe();
	private final Sensei<Daylight> sensei = new Sensei<Daylight>(scribe);

	private final Daylight empty = new Daylight();
	private final Daylight withAllValues = new Daylight(true, new UtcOffset(-5, 0), new DateTimeComponents(2014, 1, 1, 1, 0, 0, false), new DateTimeComponents(2014, 3, 1, 1, 0, 0, false), "EST", "EDT");
	private final Daylight withNoValues = new Daylight(true, null, null, null, null, null);

	@Test
	public void writeText() {
		sensei.assertWriteText(empty).run("FALSE");
		sensei.assertWriteText(withAllValues).run("TRUE;-0500;20140101T010000;20140301T010000;EST;EDT");
		sensei.assertWriteText(withNoValues).run("TRUE;;;;;");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("FALSE").run(is(empty));
		sensei.assertParseText("true;-0500;20140101T010000;20140301T010000;EST;EDT").run(is(withAllValues));
		sensei.assertParseText("true;invalid;20140101T010000;20140301T010000;EST;EDT").cannotParse();
		sensei.assertParseText("true;-0500;invalid;20140301T010000;EST;EDT").cannotParse();
		sensei.assertParseText("true;-0500;20140101T010000;invalid;EST;EDT").cannotParse();
		sensei.assertParseText("TRUE;;;;;").run(is(withNoValues));
	}

	private Check<Daylight> is(final Daylight expected) {
		return new Check<Daylight>() {
			public void check(Daylight actual, ParseContext context) {
				assertEquals(expected.isDaylight(), actual.isDaylight());
				assertEquals(expected.getOffset(), actual.getOffset());
				assertEquals(expected.getStart(), actual.getStart());
				assertEquals(expected.getEnd(), actual.getEnd());
				assertEquals(expected.getStandardName(), actual.getStandardName());
				assertEquals(expected.getDaylightName(), actual.getDaylightName());
			}
		};
	}
}
