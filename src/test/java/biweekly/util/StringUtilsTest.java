package biweekly.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import biweekly.util.StringUtils.JoinCallback;

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
public class StringUtilsTest {
	@Test
	public void repeat_sb() {
		StringBuilder sb = new StringBuilder("a");
		StringUtils.repeat(' ', 2, sb);
		assertEquals("a  ", sb.toString());

		sb = new StringBuilder("a");
		StringUtils.repeat(' ', 0, sb);
		assertEquals("a", sb.toString());

		sb = new StringBuilder("a");
		StringUtils.repeat(' ', -1, sb);
		assertEquals("a", sb.toString());
	}

	@Test
	public void afterPrefixIgnoreCase() {
		String expected = StringUtils.afterPrefixIgnoreCase("MAILTO:email@example.com", "mailto:");
		assertEquals("email@example.com", expected);

		expected = StringUtils.afterPrefixIgnoreCase("http://www.google.com", "mailto:");
		assertNull(expected);

		expected = StringUtils.afterPrefixIgnoreCase("m", "mailto:");
		assertNull(expected);
	}

	@Test
	public void join_multiple() {
		Collection<String> values = Arrays.asList("one", "two", "three");
		assertEquals("ONE,TWO,THREE", StringUtils.join(values, ",", new JoinCallback<String>() {
			public void handle(StringBuilder sb, String str) {
				sb.append(str.toUpperCase());
			}
		}));
	}

	@Test
	public void join_single() {
		Collection<String> values = Arrays.asList("one");
		assertEquals("ONE", StringUtils.join(values, ",", new JoinCallback<String>() {
			public void handle(StringBuilder sb, String str) {
				sb.append(str.toUpperCase());
			}
		}));
	}

	@Test
	public void join_empty() {
		Collection<String> values = Arrays.asList();
		assertEquals("", StringUtils.join(values, ",", new JoinCallback<String>() {
			public void handle(StringBuilder sb, String str) {
				sb.append(str.toUpperCase());
			}
		}));
	}

	@Test
	public void join_objects() {
		Collection<Object> values = Arrays.<Object> asList(false, 1, "two", null);
		assertEquals("false,1,two,null", StringUtils.join(values, ","));
	}
}
