package biweekly;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

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
public class ICalDataTypeTest {
	@Test
	public void get() {
		assertSame(ICalDataType.TEXT, ICalDataType.get("tExT"));

		ICalDataType test = ICalDataType.get("test");
		ICalDataType test2 = ICalDataType.get("tEsT");
		assertEquals("test", test2.getName());
		assertSame(test, test2);
	}

	@Test
	public void find() {
		assertSame(ICalDataType.TEXT, ICalDataType.find("tExT"));

		//find() ignores runtime-defined objects
		ICalDataType.get("test");
		assertNull(ICalDataType.find("test"));
	}

	@Test
	public void all() {
		ICalDataType.get("test"); //all() ignores runtime-defined objects
		Collection<ICalDataType> all = ICalDataType.all();

		assertEquals(16, all.size());
		assertTrue(all.contains(ICalDataType.BINARY));
		assertTrue(all.contains(ICalDataType.BOOLEAN));
		assertTrue(all.contains(ICalDataType.CAL_ADDRESS));
		assertTrue(all.contains(ICalDataType.CONTENT_ID));
		assertTrue(all.contains(ICalDataType.DATE));
		assertTrue(all.contains(ICalDataType.DATE_TIME));
		assertTrue(all.contains(ICalDataType.DURATION));
		assertTrue(all.contains(ICalDataType.FLOAT));
		assertTrue(all.contains(ICalDataType.INTEGER));
		assertTrue(all.contains(ICalDataType.PERIOD));
		assertTrue(all.contains(ICalDataType.RECUR));
		assertTrue(all.contains(ICalDataType.TEXT));
		assertTrue(all.contains(ICalDataType.TIME));
		assertTrue(all.contains(ICalDataType.URI));
		assertTrue(all.contains(ICalDataType.URL));
		assertTrue(all.contains(ICalDataType.UTC_OFFSET));
	}

	@Test
	public void contentId() {
		assertEquals(ICalDataType.CONTENT_ID, ICalDataType.find("CID"));
		assertEquals(ICalDataType.CONTENT_ID, ICalDataType.get("CID"));
	}
}
