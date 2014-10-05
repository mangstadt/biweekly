package biweekly.property;

import static biweekly.util.TestUtils.assertValidate;

import java.util.Date;

import org.junit.Test;

import biweekly.ICalVersion;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.Frequency;

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
public class RecurrencePropertyTest {
	@Test
	public void validate() {
		RecurrenceProperty property = new RecurrenceProperty(null);
		assertValidate(property).run(26);

		property = new RecurrenceProperty(new Recurrence.Builder((Frequency) null).build());
		assertValidate(property).run(30);

		property = new RecurrenceProperty(new Recurrence.Builder((Frequency) null).until(new Date()).count(1).build());
		assertValidate(property).run(30, 31);

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).until(new Date()).count(1).build());
		assertValidate(property).run(31);

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).build());
		assertValidate(property).run();
	}

	@Test
	public void validate_xrules() {
		RecurrenceProperty property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).xrule("foo", "bar").build());
		assertValidate(property).versions(ICalVersion.V1_0).run(new Integer[] { null });

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).xrule("foo", "bar").build());
		assertValidate(property).versions(ICalVersion.V2_0_DEPRECATED).run();

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).xrule("foo", "bar").build());
		assertValidate(property).versions(ICalVersion.V2_0).run(32);
	}

	@Test
	public void validate_bysetpos() {
		RecurrenceProperty property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).bySetPos(-1).build());
		assertValidate(property).versions(ICalVersion.V1_0).run(new Integer[] { null });

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.DAILY).bySetPos(-1).build());
		assertValidate(property).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run();
	}

	@Test
	public void validate_secondly_frequency() {
		RecurrenceProperty property = new RecurrenceProperty(new Recurrence.Builder(Frequency.SECONDLY).build());
		assertValidate(property).versions(ICalVersion.V1_0).run(new Integer[] { null });

		property = new RecurrenceProperty(new Recurrence.Builder(Frequency.SECONDLY).build());
		assertValidate(property).versions(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0).run();
	}
}
