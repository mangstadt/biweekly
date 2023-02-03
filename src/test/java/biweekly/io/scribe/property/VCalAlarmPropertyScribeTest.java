package biweekly.io.scribe.property;

import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues.SemiStructuredValueIterator;

import biweekly.ICalDataType;
import biweekly.component.VAlarm;
import biweekly.io.DataModelConversionException;
import biweekly.io.scribe.property.VCalAlarmPropertyScribeTest.VCalAlarmPropertyImpl;
import biweekly.property.Action;
import biweekly.property.Trigger;
import biweekly.property.VCalAlarmProperty;
import biweekly.util.Duration;

/*
 Copyright (c) 2013-2023, Michael Angstadt
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
public class VCalAlarmPropertyScribeTest extends ScribeTest<VCalAlarmPropertyImpl> {
	private final Date start = date("2014-01-01 01:00:00 +0000");

	private final VCalAlarmPropertyImpl empty = new VCalAlarmPropertyImpl();
	private final VCalAlarmPropertyImpl withValues = new VCalAlarmPropertyImpl("one", "two");
	{
		withValues.setRepeat(5);
		withValues.setSnooze(new Duration.Builder().minutes(10).build());
		withValues.setStart(start);
	}

	public VCalAlarmPropertyScribeTest() {
		super(new VCalAlarmPropertyScribeImpl());
	}

	@Test
	public void writeText() {
		sensei.assertWriteText(empty).run(";;");
		sensei.assertWriteText(withValues).run("20140101T010000Z;PT10M;5;one;two");
	}

	@Test
	public void parseText() {
		try {
			sensei.assertParseText("").run();
			fail();
		} catch (DataModelConversionException e) {
			assertEquals(empty, e.getOriginalProperty());

			VAlarm expected = new VAlarm(new Action("TEST"), new Trigger((Date) null));
			expected.setDescription("test");
			assertEquals(Arrays.asList(expected), e.getComponents());
		}

		try {
			sensei.assertParseText("; ;  ").run();
			fail();
		} catch (DataModelConversionException e) {
			assertEquals(empty, e.getOriginalProperty());

			VAlarm expected = new VAlarm(new Action("TEST"), new Trigger((Date) null));
			expected.setDescription("test");
			assertEquals(Arrays.asList(expected), e.getComponents());
		}

		try {
			sensei.assertParseText("20140101T010000Z;PT10M;5;one;two").run();
			fail();
		} catch (DataModelConversionException e) {
			assertEquals(withValues, e.getOriginalProperty());

			VAlarm expected = new VAlarm(new Action("TEST"), new Trigger(withValues.getStart()));
			expected.setDuration(withValues.getSnooze());
			expected.setRepeat(withValues.getRepeat());
			expected.setDescription("test");
			assertEquals(Arrays.asList(expected), e.getComponents());
		}

		sensei.assertParseText("invalid;;").cannotParse(27);
		sensei.assertParseText("20140101T010000Z;invalid;").cannotParse(26);
		sensei.assertParseText("20140101T010000Z;PT10M;invalid").cannotParse(24);
	}

	public static class VCalAlarmPropertyImpl extends VCalAlarmProperty {
		private final List<String> dataValues;

		public VCalAlarmPropertyImpl(String... dataValues) {
			this.dataValues = Arrays.asList(dataValues);
		}
	}

	public static class VCalAlarmPropertyScribeImpl extends VCalAlarmPropertyScribe<VCalAlarmPropertyImpl> {
		public VCalAlarmPropertyScribeImpl() {
			super(VCalAlarmPropertyImpl.class, "NAME", null);
		}

		@Override
		protected List<String> writeData(VCalAlarmPropertyImpl property) {
			return property.dataValues;
		}

		@Override
		protected VCalAlarmPropertyImpl create(ICalDataType dataType, SemiStructuredValueIterator it) {
			List<String> dataValues = new ArrayList<String>();
			while (it.hasNext()) {
				dataValues.add(it.next());
			}
			return new VCalAlarmPropertyImpl(dataValues.toArray(new String[0]));
		}

		@Override
		protected void toVAlarm(VAlarm valarm, VCalAlarmPropertyImpl property) {
			valarm.setDescription("test");
		}

		@Override
		protected Action action() {
			return new Action("TEST");
		}
	}
}
