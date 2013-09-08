package biweekly.javadoc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Test;

import com.sun.javadoc.Doc;
import com.sun.javadoc.SourcePosition;
import com.sun.javadoc.Tag;

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
public class RfcTagletTest {
	private int warnings = 0;
	private final RfcTaglet taglet = new RfcTaglet() {
		@Override
		void logWarning(Tag tag, String message) {
			warnings++;
		}
	};

	@After
	public void after() {
		warnings = 0;
	}

	@Test
	public void toString_() {
		Tag tags[] = new Tag[] { new TagImpl("5545"), new TagImpl("invalid"), new TagImpl("5545 p.43"), new TagImpl("5545 p.43-44"), new TagImpl("5545 4.3.2") };
		String html = taglet.toString(tags);

		assertTrue(html.contains("<a href=\"http://tools.ietf.org/html/rfc5545\">RFC 5545</a>"));
		assertTrue(html.contains("<a href=\"http://tools.ietf.org/html/rfc5545#page-43\">RFC 5545 p.43</a>"));
		assertTrue(html.contains("<a href=\"http://tools.ietf.org/html/rfc5545#page-43\">RFC 5545 p.43-44</a>"));
		assertTrue(html.contains("<a href=\"http://tools.ietf.org/html/rfc5545#section-4.3.2\">RFC 5545, Section 4.3.2</a>"));
		assertEquals(1, warnings);
	}

	@Test
	public void toString_no_tags() {
		String expected = "";
		String actual = taglet.toString(new Tag[0]);
		assertEquals(expected, actual);
		assertEquals(0, warnings);
	}

	@Test
	public void toString_no_valid_tags() {
		Tag tags[] = new Tag[] { new TagImpl("invalid"), new TagImpl("invalid") };

		String expected = "";
		String actual = taglet.toString(tags);
		assertEquals(expected, actual);
		assertEquals(2, warnings);
	}

	private static class TagImpl implements Tag {
		private final String text;

		public TagImpl(String text) {
			this.text = text;
		}

		public Tag[] firstSentenceTags() {
			return null;
		}

		public Doc holder() {
			return null;
		}

		public Tag[] inlineTags() {
			return null;
		}

		public String kind() {
			return null;
		}

		public String name() {
			return "rfc";
		}

		public SourcePosition position() {
			return new SourcePosition() {
				public int column() {
					return 0;
				}

				public File file() {
					return new File("MyFile.java");
				}

				public int line() {
					return 12;
				}
			};
		}

		public String text() {
			return text;
		}
	}
}
