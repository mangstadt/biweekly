package biweekly.component;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertValidate;

import java.util.Date;

import org.junit.Test;

import biweekly.property.DateStart;
import biweekly.property.RecurrenceRule;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;
import biweekly.util.UtcOffset;

/*
 Copyright (c) 2013-2018, Michael Angstadt
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
public class ObservanceTest {
	@Test
	public void validate_required() {
		Observance component = new Observance();
		assertValidate(component).versions(V1_0).run(48, 2, 2, 2);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(2, 2, 2);

		component.setDateStart(new DateStart(new Date()));
		component.setTimezoneOffsetFrom(new UtcOffset(0));
		component.setTimezoneOffsetTo(new UtcOffset(0));
		assertValidate(component).versions(V1_0).run(48);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run();
	}

	@Test
	public void validate_time_in_rrule() {
		//@formatter:off
		Recurrence[] recurrences = {
			new Recurrence.Builder(Frequency.DAILY).byHour(1).build(),
			new Recurrence.Builder(Frequency.DAILY).byMinute(1).build(),
			new Recurrence.Builder(Frequency.DAILY).bySecond(1).build()
		};
		//@formatter:on
		for (Recurrence recurrence : recurrences) {
			Observance component = new Observance();
			component.setTimezoneOffsetFrom(new UtcOffset(true, 1, 0));
			component.setTimezoneOffsetTo(new UtcOffset(true, 1, 0));
			component.setDateStart(new DateStart(new Date(), false));
			component.setRecurrenceRule(recurrence);
			assertValidate(component).versions(V1_0).run(48, 5);
			assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(5);
		}
	}

	@Test
	public void validate_multiple_rrules() {
		Observance component = new Observance();
		component.setDateStart(new DateStart(new Date()));
		component.setTimezoneOffsetFrom(new UtcOffset(true, 1, 0));
		component.setTimezoneOffsetTo(new UtcOffset(true, 1, 0));
		component.addProperty(new RecurrenceRule(new Recurrence.Builder(Frequency.DAILY).build()));
		component.addProperty(new RecurrenceRule(new Recurrence.Builder(Frequency.DAILY).build()));
		assertValidate(component).versions(V1_0).run(48, 6);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(6);
	}
}
