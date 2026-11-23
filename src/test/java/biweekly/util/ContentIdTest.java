package biweekly.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

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
class ContentIdTest {
	@Test
	void parse() {
		assertEquals("foo", ContentId.parse("cid:foo"));
		assertEquals("foo", ContentId.parse("CID:foo"));
		assertEquals("", ContentId.parse("cid:"));
		assertEquals("foo", ContentId.parse("<foo>"));
		assertEquals("", ContentId.parse("<>"));
		assertNull(ContentId.parse("http://www.google.com"));
		assertNull(ContentId.parse("cid"));
		assertNull(ContentId.parse(""));
	}
	
	@Test
	void parseUri() {
		assertEquals("foo", ContentId.parseUri("cid:foo"));
		assertEquals("foo", ContentId.parseUri("CID:foo"));
		assertEquals("", ContentId.parseUri("cid:"));
		assertNull(ContentId.parseUri("<foo>"));
		assertNull(ContentId.parseUri("<>"));
		assertNull(ContentId.parseUri("http://www.google.com"));
		assertNull(ContentId.parseUri("cid"));
		assertNull(ContentId.parseUri(""));
	}
	
	@Test
	void writeUri() {
		assertEquals("cid:foo", ContentId.writeUri("foo"));
	}
	
	@Test
	void writeOldStyle() {
		assertEquals("<foo>", ContentId.writeOldStyle("foo"));
	}
	
}
