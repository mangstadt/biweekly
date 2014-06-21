package biweekly.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

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
public class VersionNumberTest {
	@Test(expected = IllegalArgumentException.class)
	public void invalid_value() {
		new VersionNumber("1.2-beta");
	}

	@Test
	public void toString_() {
		VersionNumber version = new VersionNumber("1.23.0.4");
		assertEquals("1.23.0.4", version.toString());
	}

	@Test
	public void compareTo() {
		VersionNumber a = new VersionNumber("1.23.0.4");
		VersionNumber b = new VersionNumber("1.23.0.4");
		assertEquals(0, a.compareTo(b));

		a = new VersionNumber("1.23.1.4");
		b = new VersionNumber("1.23.0.4");
		assertEquals(1, a.compareTo(b));

		a = new VersionNumber("1.23");
		b = new VersionNumber("1.23.0.4");
		assertEquals(-1, a.compareTo(b));
	}

	@Test
	public void equals_() {
		VersionNumber a = new VersionNumber("1.2");
		VersionNumber b = new VersionNumber("1.2");
		assertTrue(a.equals(b));

		a = new VersionNumber("1.3");
		b = new VersionNumber("1.2");
		assertFalse(a.equals(b));

		a = new VersionNumber("1.3");
		b = null;
		assertFalse(a.equals(b));

		a = new VersionNumber("1.3");
		assertFalse(a.equals(""));
	}
}
