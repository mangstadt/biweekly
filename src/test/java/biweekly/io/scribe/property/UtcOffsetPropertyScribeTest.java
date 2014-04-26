package biweekly.io.scribe.property;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import biweekly.io.scribe.property.UtcOffsetPropertyScribe;
import biweekly.io.scribe.property.Sensei.Check;
import biweekly.property.UtcOffsetProperty;
import biweekly.util.UtcOffset;

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
public class UtcOffsetPropertyScribeTest {
	private final UtcOffsetPropertyMarshallerImpl marshaller = new UtcOffsetPropertyMarshallerImpl();
	private final Sensei<UtcOffsetPropertyImpl> sensei = new Sensei<UtcOffsetPropertyImpl>(marshaller);

	private final UtcOffsetPropertyImpl withValue = new UtcOffsetPropertyImpl(new UtcOffset(1, 30));
	private final UtcOffsetPropertyImpl empty = new UtcOffsetPropertyImpl(null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withValue).run("+0130");
		sensei.assertWriteText(empty).run("");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("+0130").run(is(withValue));
		sensei.assertParseText("invalid").cannotParse();
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withValue).run("<utc-offset>+01:30</utc-offset>");
		sensei.assertWriteXml(empty).run("<utc-offset/>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<utc-offset>+01:30</utc-offset>").run(is(withValue));
		sensei.assertParseXml("<utc-offset>invalid</utc-offset>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withValue).run("+01:30");
		sensei.assertWriteJson(empty).run("");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson("+01:30").run(is(withValue));
		sensei.assertParseJson("invalid").cannotParse();
		sensei.assertParseJson("").cannotParse();
	}

	private class UtcOffsetPropertyMarshallerImpl extends UtcOffsetPropertyScribe<UtcOffsetPropertyImpl> {
		public UtcOffsetPropertyMarshallerImpl() {
			super(UtcOffsetPropertyImpl.class, "UTC");
		}

		@Override
		protected UtcOffsetPropertyImpl newInstance(UtcOffset offset) {
			return new UtcOffsetPropertyImpl(offset);
		}
	}

	private class UtcOffsetPropertyImpl extends UtcOffsetProperty {
		public UtcOffsetPropertyImpl(UtcOffset offset) {
			super(offset);
		}
	}

	private Check<UtcOffsetPropertyImpl> is(final UtcOffsetPropertyImpl expected) {
		return new Check<UtcOffsetPropertyImpl>() {
			public void check(UtcOffsetPropertyImpl actual) {
				assertEquals(expected.getValue(), actual.getValue());
			}
		};
	}
}
