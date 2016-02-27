package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import static biweekly.ICalVersion.*;
import biweekly.io.ParseContext;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.Transparency;

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
public class TransparencyScribeTest extends ScribeTest<Transparency> {
	public TransparencyScribeTest() {
		super(new TransparencyScribe());
	}

	@Test
	public void writeText() {
		Transparency opaque = Transparency.opaque();
		sensei.assertWriteText(opaque).version(V1_0).run("0");
		sensei.assertWriteText(opaque).version(V2_0_DEPRECATED).run("OPAQUE");
		sensei.assertWriteText(opaque).version(V2_0).run("OPAQUE");

		Transparency transparent = Transparency.transparent();
		sensei.assertWriteText(transparent).version(V1_0).run("1");
		sensei.assertWriteText(transparent).version(V2_0_DEPRECATED).run("TRANSPARENT");
		sensei.assertWriteText(transparent).version(V2_0).run("TRANSPARENT");

		Transparency other = new Transparency("2");
		sensei.assertWriteText(other).run("2");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("0").versions(V1_0).run(isOpaque(true));
		sensei.assertParseText("0").versions(V2_0_DEPRECATED, V2_0).run(isOpaque(false));

		sensei.assertParseText("1").versions(V1_0).run(isTransparent(true));
		sensei.assertParseText("1").versions(V2_0_DEPRECATED, V2_0).run(isTransparent(false));

		sensei.assertParseText("OPAQUE").versions(V1_0).run(isOpaque(true));
		sensei.assertParseText("OPAQUE").versions(V2_0_DEPRECATED, V2_0).run(isOpaque(true));

		sensei.assertParseText("TRANSPARENT").versions(V1_0).run(isTransparent(true));
		sensei.assertParseText("TRANSPARENT").versions(V2_0_DEPRECATED, V2_0).run(isTransparent(true));

		sensei.assertParseText("2").versions(V1_0).run(is("2"));
		sensei.assertParseText("2").versions(V2_0_DEPRECATED, V2_0).run(is("2"));
	}

	private Check<Transparency> isOpaque(final boolean isOpaque) {
		return new Check<Transparency>() {
			public void check(Transparency actual, ParseContext context) {
				assertEquals(isOpaque, actual.isOpaque());
			}
		};
	}

	private Check<Transparency> isTransparent(final boolean isTransparent) {
		return new Check<Transparency>() {
			public void check(Transparency actual, ParseContext context) {
				assertEquals(isTransparent, actual.isTransparent());
			}
		};
	}

	private Check<Transparency> is(final String value) {
		return new Check<Transparency>() {
			public void check(Transparency actual, ParseContext context) {
				assertEquals(value, actual.getValue());
			}
		};
	}
}
