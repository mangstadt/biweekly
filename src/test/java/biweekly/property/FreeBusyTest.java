package biweekly.property;

import static biweekly.util.TestUtils.assertValidate;

import java.util.Date;

import org.junit.Test;

import biweekly.util.Duration;

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
public class FreeBusyTest {
	@Test
	public void validate() {
		FreeBusy property = new FreeBusy();
		assertValidate(property).run(38);

		property = new FreeBusy();
		property.addValue(null, (Date) null);
		assertValidate(property).run(39, 40);

		property = new FreeBusy();
		property.addValue(new Date(), (Date) null);
		assertValidate(property).run(40);

		property = new FreeBusy();
		property.addValue(null, new Date());
		assertValidate(property).run(39);

		property = new FreeBusy();
		property.addValue(new Date(), new Date());
		assertValidate(property).run();

		property = new FreeBusy();
		property.addValue(null, (Duration) null);
		assertValidate(property).run(39, 40);

		property = new FreeBusy();
		property.addValue(new Date(), (Duration) null);
		assertValidate(property).run(40);

		property = new FreeBusy();
		property.addValue(null, new Duration.Builder().build());
		assertValidate(property).run(39);

		property = new FreeBusy();
		property.addValue(new Date(), new Duration.Builder().build());
		assertValidate(property).run();
	}
}
