package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.Version;
import biweekly.util.VersionNumber;

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
public class VersionScribeTest {
	private final VersionScribe marshaller = new VersionScribe();
	private final Sensei<Version> sensei = new Sensei<Version>(marshaller);

	private final Version withMinMax = new Version("1.0", "2.0");
	private final Version withMax = new Version("2.0");
	private final Version empty = new Version((String) null);

	@Test
	public void writeText_min_max() {
		sensei.assertWriteText(withMinMax).run("1.0;2.0");
		sensei.assertWriteText(withMax).run("2.0");
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText_min_max() {
		sensei.assertParseText("1.0;2.0").run(has("1.0", "2.0"));
		sensei.assertParseText("2.0").run(has(null, "2.0"));
		sensei.assertParseText("").run(has(null, null));
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withMinMax).run("<text>2.0</text>");
		sensei.assertWriteXml(withMax).run("<text>2.0</text>");
		sensei.assertWriteXml(empty).run("<text/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<text>2.0</text>").run(has(null, "2.0"));
		sensei.assertParseXml("<text/>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withMinMax).run("2.0");
		sensei.assertWriteJson(withMax).run("2.0");
		sensei.assertWriteJson(empty).run((String) null);
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson("2.0").run(has(null, "2.0"));
		sensei.assertParseJson("").cannotParse();
	}

	private Check<Version> has(final String min, final String max) {
		return new Check<Version>() {
			public void check(Version actual) {
				VersionNumber minNumber = (min == null) ? null : new VersionNumber(min);
				VersionNumber maxNumber = (max == null) ? null : new VersionNumber(max);

				assertEquals(minNumber, actual.getMinVersion());
				assertEquals(maxNumber, actual.getMaxVersion());
			}
		};
	}
}
