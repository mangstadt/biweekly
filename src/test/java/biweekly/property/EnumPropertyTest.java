package biweekly.property;

import static biweekly.util.TestUtils.assertWarnings;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;

import biweekly.ICalVersion;

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
public class EnumPropertyTest {
	@Test
	public void validate() {
		//null value
		EnumPropertyImpl prop = new EnumPropertyImpl(null);
		assertWarnings(1, prop.validate(null, ICalVersion.V1_0));
		assertWarnings(1, prop.validate(null, ICalVersion.V2_0_DEPRECATED));
		assertWarnings(1, prop.validate(null, ICalVersion.V2_0));

		//invalid value
		prop = new EnumPropertyImpl("three");
		assertWarnings(1, prop.validate(null, ICalVersion.V1_0));
		assertWarnings(1, prop.validate(null, ICalVersion.V2_0_DEPRECATED));
		assertWarnings(1, prop.validate(null, ICalVersion.V2_0));

		prop = new EnumPropertyImpl("");
		assertWarnings(1, prop.validate(null, ICalVersion.V1_0));
		assertWarnings(1, prop.validate(null, ICalVersion.V2_0_DEPRECATED));
		assertWarnings(1, prop.validate(null, ICalVersion.V2_0));

		prop = new EnumPropertyImpl("ONE");
		assertWarnings(0, prop.validate(null, ICalVersion.V1_0));
		assertWarnings(1, prop.validate(null, ICalVersion.V2_0_DEPRECATED));
		assertWarnings(1, prop.validate(null, ICalVersion.V2_0));

		prop = new EnumPropertyImpl("TWO");
		assertWarnings(1, prop.validate(null, ICalVersion.V1_0));
		assertWarnings(0, prop.validate(null, ICalVersion.V2_0_DEPRECATED));
		assertWarnings(0, prop.validate(null, ICalVersion.V2_0));
	}

	private class EnumPropertyImpl extends EnumProperty {
		public EnumPropertyImpl(String value) {
			super(value);
		}

		@Override
		protected Collection<String> getStandardValues(ICalVersion version) {
			switch (version) {
			case V1_0:
				return Arrays.asList("one");
			default:
				return Arrays.asList("TWO");
			}
		}

		@Override
		protected Collection<ICalVersion> getValueSupportedVersions() {
			if (value == null) {
				return Collections.emptyList();
			}

			if ("one".equalsIgnoreCase(value)) {
				return Arrays.asList(ICalVersion.V1_0);
			}
			if ("two".equalsIgnoreCase(value)) {
				return Arrays.asList(ICalVersion.V2_0, ICalVersion.V2_0_DEPRECATED);
			}

			return Collections.emptyList();
		}
	}
}
