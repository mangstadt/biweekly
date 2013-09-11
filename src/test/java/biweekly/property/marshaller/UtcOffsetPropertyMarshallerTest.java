package biweekly.property.marshaller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import biweekly.property.UtcOffsetProperty;
import biweekly.property.marshaller.Sensei.Check;

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
public class UtcOffsetPropertyMarshallerTest {
	private final UtcOffsetPropertyMarshallerImpl marshaller = new UtcOffsetPropertyMarshallerImpl();
	private final Sensei<UtcOffsetPropertyImpl> sensei = new Sensei<UtcOffsetPropertyImpl>(marshaller);

	private final UtcOffsetPropertyImpl withHourMinute = new UtcOffsetPropertyImpl(1, 30);
	private final UtcOffsetPropertyImpl withHour = new UtcOffsetPropertyImpl(1, null);
	private final UtcOffsetPropertyImpl withMinute = new UtcOffsetPropertyImpl(null, 30);
	private final UtcOffsetPropertyImpl empty = new UtcOffsetPropertyImpl(null, null);

	@Test
	public void writeText() {
		sensei.assertWriteText(withHourMinute).run("+0130");
		sensei.assertWriteText(withHour).run("+0100");
		sensei.assertWriteText(withMinute).run("+0030");
		sensei.assertWriteText(empty).run("+0000");
	}

	@Test
	public void parseText() {
		sensei.assertParseText("+0130").run(has(1, 30));
		sensei.assertParseText("+0100").run(has(1, 0));
		sensei.assertParseText("+0030").run(has(0, 30));
		sensei.assertParseText("+0000").run(has(0, 0));
		sensei.assertParseText("invalid").cannotParse();
	}

	@Test
	public void writeXml() {
		sensei.assertWriteXml(withHourMinute).run("<utc-offset>+01:30</utc-offset>");
		sensei.assertWriteXml(withHour).run("<utc-offset>+01:00</utc-offset>");
		sensei.assertWriteXml(withMinute).run("<utc-offset>+00:30</utc-offset>");
		sensei.assertWriteXml(empty).run("<utc-offset>+00:00</utc-offset>");
	}

	@Test
	public void parseXml() {
		sensei.assertParseXml("<utc-offset>+01:30</utc-offset>").run(has(1, 30));
		sensei.assertParseXml("<utc-offset>+01:00</utc-offset>").run(has(1, 0));
		sensei.assertParseXml("<utc-offset>+00:30</utc-offset>").run(has(0, 30));
		sensei.assertParseXml("<utc-offset>+00:00</utc-offset>").run(has(0, 0));
		sensei.assertParseXml("<utc-offset>invalid</utc-offset>").cannotParse();
		sensei.assertParseXml("").cannotParse();
	}

	@Test
	public void writeJson() {
		sensei.assertWriteJson(withHourMinute).run("+01:30");
		sensei.assertWriteJson(withHour).run("+01:00");
		sensei.assertWriteJson(withMinute).run("+00:30");
		sensei.assertWriteJson(empty).run("+00:00");
	}

	@Test
	public void parseJson() {
		sensei.assertParseJson("+01:30").run(has(1, 30));
		sensei.assertParseJson("+01:00").run(has(1, 0));
		sensei.assertParseJson("+00:30").run(has(0, 30));
		sensei.assertParseJson("+00:00").run(has(0, 0));
		sensei.assertParseJson("invalid").cannotParse();
		sensei.assertParseJson("").cannotParse();
	}

	private class UtcOffsetPropertyMarshallerImpl extends UtcOffsetPropertyMarshaller<UtcOffsetPropertyImpl> {
		public UtcOffsetPropertyMarshallerImpl() {
			super(UtcOffsetPropertyImpl.class, "UTC");
		}

		@Override
		protected UtcOffsetPropertyImpl newInstance(Integer hourOffset, Integer minuteOffset) {
			return new UtcOffsetPropertyImpl(hourOffset, minuteOffset);
		}
	}

	private class UtcOffsetPropertyImpl extends UtcOffsetProperty {
		public UtcOffsetPropertyImpl(Integer hourOffset, Integer minuteOffset) {
			super(hourOffset, minuteOffset);
		}
	}

	private Check<UtcOffsetPropertyImpl> has(final Integer hour, final Integer minute) {
		return new Check<UtcOffsetPropertyImpl>() {
			public void check(UtcOffsetPropertyImpl actual) {
				assertEquals(hour, actual.getHourOffset());
				assertEquals(minute, actual.getMinuteOffset());
			}
		};
	}
}
