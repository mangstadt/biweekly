package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
public class GeoTest {
	@Test
	public void constructors() throws Exception {
		Geo property = new Geo(null, null);
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());

		property = new Geo(12.34, 56.78);
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);
	}

	@Test
	public void set_value() {
		Geo property = new Geo(null, null);

		property.setLatitude(12.34);
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertNull(property.getLongitude());

		property.setLongitude(56.78);
		assertEquals(12.34, property.getLatitude(), 0.1);
		assertEquals(56.78, property.getLongitude(), 0.1);

		property.setLatitude(null);
		assertNull(property.getLatitude());
		assertEquals(56.78, property.getLongitude(), 0.1);

		property.setLongitude(null);
		assertNull(property.getLatitude());
		assertNull(property.getLongitude());
	}

	@Test
	public void toDecimal() {
		assertEquals(100.502777, Geo.toDecimal(100, 30, 10), 0.1);
	}

	@Test
	public void validate() {
		Geo property = new Geo(null, null);
		assertValidate(property).run(41, 42);

		property = new Geo(1.1, null);
		assertValidate(property).run(42);

		property = new Geo(null, 1.1);
		assertValidate(property).run(41);

		property = new Geo(1.1, 1.1);
		assertValidate(property).run();
	}

	@Test
	public void toStringValues() {
		Geo property = new Geo(12.34, 56.78);
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	public void copy() {
		Geo original = new Geo(12.34, 56.78);
		assertCopy(original);
	}

	@Test
	public void equals() {
		//@formatter:off
		assertNothingIsEqual(
			new Geo(null, null),
			new Geo(12.34, null),
			new Geo(null, 56.78),
			new Geo(12.34, 56.78),
			new Geo(90.12, 34.56)
		);

		assertEqualsMethod(Geo.class, 12.34, 56.78)
		.constructor(new Class<?>[]{Double.class, Double.class}, null, null).test()
		.constructor(12.34, 56.78).test();
		//@formatter:on
	}
}
