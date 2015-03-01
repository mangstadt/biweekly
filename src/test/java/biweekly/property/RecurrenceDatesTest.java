package biweekly.property;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertValidate;

import java.util.Date;

import org.junit.Test;

import biweekly.util.ICalDate;
import biweekly.util.Period;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
public class RecurrenceDatesTest {
	@Test
	public void validate() {
		RecurrenceDates property = new RecurrenceDates();
		assertValidate(property).run(26);

		property = new RecurrenceDates();
		property.addDate(new Date());
		property.addPeriod(new Period(new Date(), new Date()));
		assertValidate(property).versions(V1_0).run(49, 51);
		assertValidate(property).versions(V2_0_DEPRECATED, V2_0).run(49);

		property = new RecurrenceDates();
		property.addDate(new ICalDate(new Date(), true));
		property.addDate(new ICalDate(new Date(), false));
		assertValidate(property).run(50);
	}
}
