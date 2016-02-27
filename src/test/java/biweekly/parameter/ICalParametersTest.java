package biweekly.parameter;

import static biweekly.ICalVersion.V1_0;
import static biweekly.ICalVersion.V2_0;
import static biweekly.ICalVersion.V2_0_DEPRECATED;
import static biweekly.util.TestUtils.assertEqualsAndHash;
import static biweekly.util.TestUtils.assertEqualsMethodEssentials;
import static biweekly.util.TestUtils.assertWarnings;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import org.junit.Before;
import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.ICalVersion;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
public class ICalParametersTest {
	private ICalParameters params;

	@Before
	public void before() {
		params = new ICalParameters();
	}

	@Test
	public void case_insensitive() {
		//tests to make sure sanitizeKey() is implemented correctly
		//ListMultimapTest tests the rest of the get/put/remove methods
		params.put("NUMBERS", "1");
		assertEquals("1", params.first("numbers"));
	}

	@Test
	public void validate_empty() {
		assertWarnings(0, params.validate(V2_0));
	}

	@Test
	public void validate_rsvp() {
		params.replace(ICalParameters.RSVP, "foo");
		assertWarnings(1, params.validate(V2_0));

		params.replace(ICalParameters.RSVP, "true");
		assertWarnings(0, params.validate(V2_0));

		params.replace(ICalParameters.RSVP, "false");
		assertWarnings(0, params.validate(V2_0));

		params.replace(ICalParameters.RSVP, "TRUE");
		assertWarnings(0, params.validate(V2_0));

		params.replace(ICalParameters.RSVP, "FALSE");
		assertWarnings(0, params.validate(V2_0));
	}

	@Test
	public void validate_bad_values() {
		params.put(ICalParameters.CUTYPE, "foo");
		params.put(ICalParameters.FBTYPE, "foo");
		params.put(ICalParameters.PARTSTAT, "foo");
		params.put(ICalParameters.RANGE, "foo");
		params.put(ICalParameters.RELATED, "foo");
		params.put(ICalParameters.RELTYPE, "foo");
		params.put(ICalParameters.ROLE, "foo");
		params.put(ICalParameters.VALUE, "foo");

		assertWarnings(8, params.validate(V2_0));
	}

	@Test
	public void validate_good_values() {
		params.put(ICalParameters.CUTYPE, CalendarUserType.GROUP.getValue());
		params.put(ICalParameters.FBTYPE, FreeBusyType.BUSY.getValue());
		params.put(ICalParameters.PARTSTAT, ParticipationStatus.ACCEPTED.getValue());
		params.put(ICalParameters.RANGE, Range.THIS_AND_FUTURE.getValue());
		params.put(ICalParameters.RELATED, Related.END.getValue());
		params.put(ICalParameters.RELTYPE, RelationshipType.CHILD.getValue());
		params.put(ICalParameters.ROLE, Role.CHAIR.getValue());
		params.put(ICalParameters.VALUE, ICalDataType.BINARY.getName());

		assertWarnings(0, params.validate(V2_0));
	}

	@Test
	public void validate_deprecated_values() {
		params.put(ICalParameters.RANGE, Range.THIS_AND_PRIOR.getValue());
		assertWarnings(0, params.validate(V1_0));
		assertWarnings(0, params.validate(V2_0_DEPRECATED));
		assertWarnings(1, params.validate(V2_0));
	}

	@Test
	public void validate_parameter_name() {
		params.replace("YES/NO", "value");
		for (ICalVersion version : ICalVersion.values()) {
			assertWarnings(1, params.validate(version));
		}
	}

	@Test
	public void validate_parameter_value_characters() {
		for (char c : ",.:=[]".toCharArray()) {
			params.replace("NAME", "value" + c);
			assertWarnings(1, params.validate(V1_0));
		}

		char c = (char) 7;
		params.replace("NAME", "value" + c);
		for (ICalVersion version : ICalVersion.values()) {
			assertWarnings(1, params.validate(version));
		}
	}

	@Test
	public void equals_essentials() {
		ICalParameters one = new ICalParameters();
		one.put("foo", "bar");
		assertEqualsMethodEssentials(one);
	}

	@Test
	public void equals_different_number_of_parameters() {
		ICalParameters one = new ICalParameters();
		one.put("foo", "one");

		ICalParameters two = new ICalParameters();
		two.put("foo", "one");
		two.put("foo", "two");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_case_insensitive() {
		ICalParameters one = new ICalParameters();
		one.put("foo", "bar");

		ICalParameters two = new ICalParameters();
		two.put("FOO", "BAR");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void equals_order_does_not_matter() {
		ICalParameters one = new ICalParameters();
		one.put("foo", "one");
		one.put("foo", "two");
		one.put("foo", "three");

		ICalParameters two = new ICalParameters();
		two.put("foo", "TWO");
		two.put("foo", "one");
		two.put("foo", "three");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void equals_duplicate_values() {
		ICalParameters one = new ICalParameters();
		one.put("foo", "one");
		one.put("foo", "one");
		one.put("foo", "two");

		ICalParameters two = new ICalParameters();
		two.put("foo", "one");
		two.put("foo", "one");
		two.put("foo", "two");

		assertEqualsAndHash(one, two);
	}

	@Test
	public void equals_different_duplicates_same_value_size() {
		ICalParameters one = new ICalParameters();
		one.put("foo", "one");
		one.put("foo", "one");
		one.put("foo", "two");

		ICalParameters two = new ICalParameters();
		two.put("foo", "one");
		two.put("foo", "two");
		two.put("foo", "two");

		assertNotEquals(one, two);
		assertNotEquals(two, one);
	}

	@Test
	public void equals_multiple_keys() {
		ICalParameters one = new ICalParameters();
		one.put("foo", "BAR");
		one.put("super", "man");
		one.put("super", "bad");
		one.put("hello", "world");

		ICalParameters two = new ICalParameters();
		two.put("hello", "world");
		two.put("super", "MAN");
		two.put("foo", "bar");
		two.put("super", "bad");

		assertEqualsAndHash(one, two);
	}
}
