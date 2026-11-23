package biweekly.io.scribe.property;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.DurationProperty;
import java.time.Duration;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
class DurationPropertyScribeTest extends ScribeTest<DurationProperty> {
	private final Duration duration = Duration.ofMinutes(90);
	private final String durationStr = duration.toString();

	private final DurationProperty withDuration = new DurationProperty(duration);
	private final DurationProperty empty = new DurationProperty((Duration) null);

	public DurationPropertyScribeTest() {
		super(new DurationPropertyScribe());
	}

	@Test
	void writeText() {
		sensei.assertWriteText(withDuration).run(durationStr);
		sensei.assertWriteText(empty).run("");
	}

	@Test
	void parseText() {
		sensei.assertParseText(durationStr).run(hasDuration);
		sensei.assertParseText("invalid").cannotParse(18);
		sensei.assertParseText("").cannotParse(18);
	}

	@Test
	void writeXml() {
		sensei.assertWriteXml(withDuration).run("<duration>" + durationStr + "</duration>");
		sensei.assertWriteXml(empty).run("<duration/>");
	}

	@Test
	void parseXml() {
		sensei.assertParseXml("<duration>" + durationStr + "</duration>").run(hasDuration);
		sensei.assertParseXml("<duration>invalid</duration>").cannotParse(18);
		sensei.assertParseXml("").cannotParse(23);
	}

	@Test
	void writeJson() {
		sensei.assertWriteJson(withDuration).run(durationStr);
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	void parseJson() {
		sensei.assertParseJson(durationStr).run(hasDuration);
		sensei.assertParseJson("invalid").cannotParse(18);
		sensei.assertParseJson("").cannotParse(18);
	}

	private final Check<DurationProperty> hasDuration = (property, context) -> {
		assertEquals(duration, property.getValue());
	};
}
