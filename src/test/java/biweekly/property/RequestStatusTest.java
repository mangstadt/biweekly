package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

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
public class RequestStatusTest {
	@Test
	public void constructors() throws Exception {
		RequestStatus property = new RequestStatus((String) null);
		assertNull(property.getStatusCode());
		assertNull(property.getDescription());
		assertNull(property.getExceptionText());

		property = new RequestStatus("code");
		assertEquals("code", property.getStatusCode());
		assertNull(property.getDescription());
		assertNull(property.getExceptionText());
	}

	@Test
	public void set_value() {
		RequestStatus property = new RequestStatus((String) null);

		property.setStatusCode("code");
		assertEquals("code", property.getStatusCode());
		assertNull(property.getDescription());
		assertNull(property.getExceptionText());

		property.setDescription("description");
		assertEquals("code", property.getStatusCode());
		assertEquals("description", property.getDescription());
		assertNull(property.getExceptionText());

		property.setExceptionText("exception");
		assertEquals("code", property.getStatusCode());
		assertEquals("description", property.getDescription());
		assertEquals("exception", property.getExceptionText());
	}

	@Test
	public void validate() {
		RequestStatus property = new RequestStatus((String) null);
		assertValidate(property).run(36);

		property = new RequestStatus("1.1.1");
		assertValidate(property).run();
	}

	@Test
	public void toStringValues() {
		RequestStatus property = new RequestStatus("code");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		RequestStatus original = new RequestStatus((String) null);
		assertCopy(original);

		original = new RequestStatus("code");
		original.setDescription("description");
		original.setExceptionText("exception");
		assertCopy(original);
	}

	@Test
	public void equals() {
		List<ICalProperty> properties = new ArrayList<ICalProperty>();

		RequestStatus property = new RequestStatus((String) null);
		properties.add(property);

		property = new RequestStatus("code");
		properties.add(property);

		property = new RequestStatus("code2");
		properties.add(property);

		property = new RequestStatus((String) null);
		property.setDescription("description");
		properties.add(property);

		property = new RequestStatus((String) null);
		property.setDescription("description2");
		properties.add(property);

		property = new RequestStatus((String) null);
		property.setExceptionText("exception");
		properties.add(property);

		property = new RequestStatus((String) null);
		property.setExceptionText("exception2");
		properties.add(property);

		property = new RequestStatus("code");
		property.setDescription("description");
		property.setExceptionText("exception");
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(RequestStatus.class, "code")
		.constructor("code")
			.test()
			.method("setDescription", "description").method("setExceptionText", "exception").test();
		//@formatter:on
	}
}
