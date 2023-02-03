package biweekly.component;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertValidate;
import static biweekly.util.TestUtils.date;

import java.util.Date;

import org.junit.Test;

import biweekly.property.Contact;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.Organizer;
import biweekly.property.Url;

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
public class VFreeBusyTest {
	@Test
	public void validate_cardinality_required() {
		VFreeBusy component = new VFreeBusy();
		component.getProperties().clear();
		assertValidate(component).versions(V1_0).run(48, 2, 2);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(2, 2);

		component.setUid("");
		component.setDateTimeStamp(new Date());
		assertValidate(component).versions(V1_0).run(48);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run();
	}

	@Test
	public void validate_cardinality_optional() {
		VFreeBusy component = new VFreeBusy();
		component.addProperty(new Contact(""));
		component.addProperty(new DateStart(date("2000-01-01")));
		component.addProperty(new DateEnd(date("2000-01-10")));
		component.addProperty(new Organizer(null, null));
		component.addProperty(new Url(""));
		assertValidate(component).versions(V1_0).run(48);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run();

		component.addProperty(new Contact(""));
		component.addProperty(new DateStart(date("2000-01-01")));
		component.addProperty(new DateEnd(date("2000-01-10")));
		component.addProperty(new Organizer("", ""));
		component.addProperty(new Url(""));
		assertValidate(component).versions(V1_0).run(48, 3, 3, 3, 3, 3);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(3, 3, 3, 3, 3);
	}

	@Test
	public void validate_no_startDate() {
		VFreeBusy component = new VFreeBusy();
		component.setDateEnd(new DateEnd(date("2000-01-10"), true));
		assertValidate(component).versions(V1_0).run(48, 15);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(15);
	}

	@Test
	public void validate_dates_do_not_have_times() {
		VFreeBusy component = new VFreeBusy();
		component.setDateStart(new DateStart(date("2000-01-01"), false));
		component.setDateEnd(new DateEnd(date("2000-01-10"), false));
		assertValidate(component).versions(V1_0).run(48, 20, 20);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(20, 20);
	}

	@Test
	public void validate_startDate_before_endDate() {
		VFreeBusy component = new VFreeBusy();
		component.setDateStart(new DateStart(date("2000-01-10"), true));
		component.setDateEnd(new DateEnd(date("2000-01-01"), true));
		assertValidate(component).versions(V1_0).run(48, 16);
		assertValidate(component).versions(V2_0_DEPRECATED, V2_0).run(16);
	}
}
