package biweekly.io.scribe.component;

import static org.junit.Assert.assertEquals;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import biweekly.component.VFreeBusy;
import biweekly.io.scribe.component.VFreeBusyScribe;
import biweekly.property.Comment;
import biweekly.property.FreeBusy;
import biweekly.property.ICalProperty;

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
public class VFreeBusyScribeTest {
	@Test
	public void sort_freeBusy_properties() throws Throwable {
		DateFormat df = new SimpleDateFormat("yyyyMMdd'T'HHmmss");

		VFreeBusy freebusy = new VFreeBusy();

		FreeBusy fb1 = new FreeBusy();
		fb1.addValue(df.parse("20130605T000000"), df.parse("20130605T010000"));
		freebusy.addFreeBusy(fb1);

		FreeBusy fb2 = new FreeBusy();
		fb2.addValue(df.parse("20130610T000000"), df.parse("20130610T010000"));
		fb2.addValue(df.parse("20130601T000000"), df.parse("20130601T010000"));
		freebusy.addFreeBusy(fb2);

		FreeBusy fb3 = new FreeBusy();
		fb3.addValue(df.parse("20130701T000000"), df.parse("20130701T010000"));
		freebusy.addFreeBusy(fb3);

		Comment comment = freebusy.addComment("comment");

		VFreeBusyScribe m = new VFreeBusyScribe();
		List<ICalProperty> props = m.getProperties(freebusy);

		assertEquals(Arrays.asList(freebusy.getUid(), freebusy.getDateTimeStamp(), fb2, fb1, fb3, comment), props);
	}
}
