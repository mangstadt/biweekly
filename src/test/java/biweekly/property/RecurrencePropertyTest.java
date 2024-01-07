package biweekly.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertIterator;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Test;

import biweekly.util.Frequency;
import biweekly.util.ICalDate;
import biweekly.util.Recurrence;
import biweekly.util.com.google.ical.compat.javautil.DateIterator;

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
public class RecurrencePropertyTest {
	@Test
	public void getDateIterator_empty() {
		RecurrenceProperty property = new RecurrenceProperty((Recurrence) null);
		Date start = date(2014, 11, 22, 10, 0, 0);
		DateIterator it = property.getDateIterator(start, TimeZone.getDefault());
		assertFalse(it.hasNext());
	}
	
	@Test
	public void getDateIterator() {
		Recurrence recur = new Recurrence.Builder(Frequency.DAILY).count(5).build();
		RecurrenceProperty property = new RecurrenceProperty(recur);
		Date start = date(2014, 11, 22, 10, 0, 0);

		//@formatter:off
		List<Date> expected = Arrays.asList(
			date(2014, 11, 22, 10, 0, 0),
			date(2014, 11, 23, 10, 0, 0),
			date(2014, 11, 24, 10, 0, 0),
			date(2014, 11, 25, 10, 0, 0),
			date(2014, 11, 26, 10, 0, 0)
		);
		//@formatter:on

		DateIterator it = property.getDateIterator(start, TimeZone.getDefault());
		assertIterator(expected, it);
	}

	@Test
	public void validate() {
		RecurrenceProperty property = new RecurrenceProperty((Recurrence) null);
		assertValidate(property).run(26);

		property = new RecurrenceProperty(new Recurrence.Builder((Frequency) null).build());
		assertValidate(property).run(30);

		property = new RecurrenceProperty(new Recurrence.Builder((Frequency) null).until(new ICalDate()).count(1).build());
		assertValidate(property).run(30, 31);

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).until(new ICalDate()).count(1).build());
		assertValidate(property).run(31);

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).build());
		assertValidate(property).run();
	}

	@Test
	public void validate_xrules() {
		RecurrenceProperty property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).xrule("foo", "bar").build());
		assertValidate(property).versions(V1_0).run(new Integer[] { null });

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).xrule("foo", "bar").build());
		assertValidate(property).versions(V2_0_DEPRECATED).run();

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).xrule("foo", "bar").build());
		assertValidate(property).versions(V2_0).run(32);
	}

	@Test
	public void validate_bysetpos() {
		RecurrenceProperty property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).bySetPos(-1).build());
		assertValidate(property).versions(V1_0).run(new Integer[] { null });

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).bySetPos(-1).build());
		assertValidate(property).versions(V2_0_DEPRECATED, V2_0).run();
	}

	@Test
	public void validate_secondly_frequency() {
		RecurrenceProperty property = new RecurrenceProperty(new Recurrence.Builder(Frequency.SECONDLY).build());
		assertValidate(property).versions(V1_0).run(new Integer[] { null });

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.SECONDLY).build());
		assertValidate(property).versions(V2_0_DEPRECATED, V2_0).run();
	}
}
