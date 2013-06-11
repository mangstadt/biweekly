package biweekly.io.text;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
public class FoldedLineReaderTest {
	@Test
	public void readLine_single_spaced_folding_enabled() throws Exception {
		//@formatter:off
		String ical =

		//unfolded line
		"ATTENDEE:mailto:johndoe@example.com\n" +

		//empty lines should be ignored
		"\n" +

		//this line is folded
		"COMMENT:folded \n" +
		" line\n" +

		//this line is folded with multiple whitespace characters used for folding
		"COMMENT:one \n" +
		"\ttwo \n" +
		"\t  three\n" +
		"\t four";
		//@formatter:on

		FoldedLineReader reader = new FoldedLineReader(ical);
		assertEquals("ATTENDEE:mailto:johndoe@example.com", reader.readLine());
		assertEquals("COMMENT:folded line", reader.readLine());
		assertEquals("COMMENT:one two   three four", reader.readLine());
		assertNull(reader.readLine());
	}

	@Test
	public void readLine_single_spaced_folding_disabled() throws Exception {
		//@formatter:off
		String ical =

		//unfolded line
		"ATTENDEE:mailto:johndoe@example.com\n" +

		//empty lines should be ignored
		"\n" +

		//this line is folded
		"COMMENT:folded \n" +
		" line\n" +

		//this line is folded with multiple whitespace characters
		"COMMENT:one \n" +
		" two \n" +
		"  three \n" +
		" \t four";
		//@formatter:on

		FoldedLineReader reader = new FoldedLineReader(ical);
		reader.setSingleSpaceFoldingEnabled(false);
		assertEquals("ATTENDEE:mailto:johndoe@example.com", reader.readLine());
		assertEquals("COMMENT:folded line", reader.readLine());
		assertEquals("COMMENT:one two three four", reader.readLine());
		assertNull(reader.readLine());
	}

	@Test
	public void getLineNum() throws Exception {
		//@formatter:off
		String ical =
		"COMMENT:one\n" +
		" two\n" +
		"\n" +
		"COMMENT:three\n" +
		" four\n";
		//@formatter:on

		FoldedLineReader reader = new FoldedLineReader(ical);

		assertEquals(0, reader.getLineNum());

		reader.readLine();
		assertEquals(1, reader.getLineNum());

		reader.readLine();
		assertEquals(4, reader.getLineNum());

		assertNull(reader.readLine());
	}
}
