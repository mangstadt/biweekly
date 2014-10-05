package biweekly.component;

import static biweekly.util.TestUtils.assertValidate;

import java.util.Date;

import org.junit.Test;

import biweekly.property.DateStart;
import biweekly.property.LastModified;
import biweekly.property.TimezoneUrl;

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
public class VTimezoneTest {
	@Test
	public void validate_required() {
		VTimezone component = new VTimezone(null);
		assertValidate(component).run(2, 21);
	}

	@Test
	public void validate_optional() {
		VTimezone component = new VTimezone("");
		component.addProperty(new LastModified(new Date()));
		component.addProperty(new LastModified(new Date()));
		component.addProperty(new TimezoneUrl(""));
		component.addProperty(new TimezoneUrl(""));
		assertValidate(component).run(3, 3, 21);
	}

	@Test
	public void validate_observance_required() {
		StandardTime standard = new StandardTime();
		standard.setDateStart(new DateStart(new Date()));
		standard.setTimezoneOffsetFrom(1, 0);
		standard.setTimezoneOffsetTo(1, 0);
		VTimezone component = new VTimezone("");
		component.addStandardTime(standard);
		assertValidate(component).run();

		DaylightSavingsTime daylight = new DaylightSavingsTime();
		daylight.setDateStart(new DateStart(new Date()));
		daylight.setTimezoneOffsetFrom(1, 0);
		daylight.setTimezoneOffsetTo(1, 0);
		component = new VTimezone("");
		component.addDaylightSavingsTime(daylight);
		assertValidate(component).run();
	}
}
