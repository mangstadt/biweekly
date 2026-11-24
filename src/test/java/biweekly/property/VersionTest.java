package biweekly.property;

import static biweekly.property.PropertySensei.assertCopy;
import static biweekly.property.PropertySensei.assertEqualsMethod;
import static biweekly.property.PropertySensei.assertNothingIsEqual;
import static biweekly.util.TestUtils.assertValidate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import biweekly.ICalVersion;
import biweekly.util.VersionNumber;

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
class VersionTest {
	@Test
	void v1_0() {
		Version property = Version.v1_0();
		assertNull(property.getMinVersion());
		assertEquals(new VersionNumber("1.0"), property.getMaxVersion());
	}

	@Test
	void v2_0() {
		Version property = Version.v2_0();
		assertNull(property.getMinVersion());
		assertEquals(new VersionNumber("2.0"), property.getMaxVersion());
	}

	@Test
	void is() {
		Version property = new Version(ICalVersion.V1_0);
		assertTrue(property.isV1_0());
		assertFalse(property.isV2_0());

		property = new Version(ICalVersion.V2_0_DEPRECATED);
		assertFalse(property.isV1_0());
		assertTrue(property.isV2_0());

		property = new Version(ICalVersion.V2_0);
		assertFalse(property.isV1_0());
		assertTrue(property.isV2_0());

		property = new Version("3.0");
		assertFalse(property.isV1_0());
		assertFalse(property.isV2_0());
	}

	@Test
	void toICalVersion() {
		Version property = new Version("1.0");
		assertEquals(ICalVersion.V1_0, property.toICalVersion());

		property = new Version("2.0");
		assertEquals(ICalVersion.V2_0, property.toICalVersion());

		property = new Version("3.0");
		assertNull(property.toICalVersion());

		property = new Version("2.0");
		property.setMinVersion(new VersionNumber("1.0"));
		assertNull(property.toICalVersion());

		property = new Version((String) null);
		assertNull(property.toICalVersion());
	}

	@Test
	void constructors() {
		Version property = new Version((String) null);
		assertNull(property.getMinVersion());
		assertNull(property.getMaxVersion());

		property = new Version((ICalVersion) null);
		assertNull(property.getMinVersion());
		assertNull(property.getMaxVersion());

		property = new Version(ICalVersion.V2_0);
		assertNull(property.getMinVersion());
		assertEquals(new VersionNumber("2.0"), property.getMaxVersion());

		property = new Version("2.0");
		assertNull(property.getMinVersion());
		assertEquals(new VersionNumber("2.0"), property.getMaxVersion());
	}

	@Test
	void set_value() {
		Version property = new Version((String) null);

		property.setMinVersion(new VersionNumber("2.0"));
		assertEquals(new VersionNumber("2.0"), property.getMinVersion());
		assertNull(property.getMaxVersion());

		property.setMaxVersion(new VersionNumber("3.0"));
		assertEquals(new VersionNumber("2.0"), property.getMinVersion());
		assertEquals(new VersionNumber("3.0"), property.getMaxVersion());
	}

	@Test
	void validate() {
		Version property = new Version((String) null);
		assertValidate(property).run(35);

		property = new Version("1.0", null);
		assertValidate(property).run(35);

		property = new Version("2.0");
		assertValidate(property).run();
	}

	@Test
	void toStringValues() {
		Version property = new Version("2.0");
		assertFalse(property.toStringValues().isEmpty());
	}

	@Test
	void copy() {
		Version original = new Version((String) null);
		assertCopy(original);

		original = new Version("2.0");
		assertCopy(original);
	}

	@Test
	void equals() {
		List<ICalProperty> properties = new ArrayList<>();

		Version property = new Version((String) null);
		properties.add(property);

		property = new Version((String) null);
		property.setMinVersion(new VersionNumber("2.0"));
		properties.add(property);

		property = new Version((String) null);
		property.setMinVersion(new VersionNumber("3.0"));
		properties.add(property);

		property = new Version((String) null);
		property.setMaxVersion(new VersionNumber("2.0"));
		properties.add(property);

		property = new Version((String) null);
		property.setMaxVersion(new VersionNumber("3.0"));
		properties.add(property);

		property = new Version((String) null);
		property.setMinVersion(new VersionNumber("2.0"));
		property.setMaxVersion(new VersionNumber("3.0"));
		properties.add(property);

		property = new Version((String) null);
		property.setMinVersion(new VersionNumber("3.0"));
		property.setMaxVersion(new VersionNumber("4.0"));
		properties.add(property);

		assertNothingIsEqual(properties);

		//@formatter:off
		assertEqualsMethod(Version.class, "2.0")
		.constructor(new Class<?>[]{String.class}, (String)null)
			.test()
			.method("setMinVersion", new VersionNumber("3.0")).method("setMaxVersion", new VersionNumber("4.0")).test();
		//@formatter:on
	}
}
