package biweekly.property;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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
public class ParameterBackingListTest {
	@Test
	public void test() {
		ParameterBackingListProperty property = new ParameterBackingListProperty();
		List<String> tags = property.getTags();

		tags.add("A");
		assertEquals(Arrays.asList("A"), property.getParameters().get("NAME"));

		tags.add("B");
		assertEquals(Arrays.asList("A", "B"), property.getParameters().get("NAME"));

		property.getParameters().removeAll("NAME");
		assertEquals(Arrays.asList(), tags);

		tags.add("A");
		assertEquals(Arrays.asList("A"), property.getParameters().get("NAME"));

		tags.clear();
		assertEquals(Arrays.asList(), property.getParameters().get("NAME"));
	}

	private static class ParameterBackingListProperty extends ICalProperty {
		public List<String> getTags() {
			return new ParameterBackingList("NAME");
		}
	}
}
