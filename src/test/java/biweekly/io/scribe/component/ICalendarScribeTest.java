package biweekly.io.scribe.component;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import biweekly.ICalendar;
import biweekly.property.ICalProperty;
import biweekly.property.ProductId;
import biweekly.property.RawProperty;
import biweekly.property.Version;

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
public class ICalendarScribeTest {
	@Test
	public void getProperties() {
		ICalendarScribe scribe = new ICalendarScribe();

		ICalendar ical = new ICalendar();
		ical.getProperties().clear();
		RawProperty property1 = ical.addExperimentalProperty("X-NAME", "value1");
		ProductId prodId = ical.setProductId("value");
		RawProperty property2 = ical.addExperimentalProperty("X-NAME", "value2");
		Version version = new Version("2.0");
		ical.addProperty(version);

		List<ICalProperty> expected = Arrays.asList(version, prodId, property1, property2);
		List<ICalProperty> actual = scribe.getProperties(ical);
		assertEquals(expected, actual);
	}
}
