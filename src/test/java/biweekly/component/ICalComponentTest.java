package biweekly.component;

import static biweekly.util.TestUtils.assertWarnings;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import biweekly.Warning;
import biweekly.property.Description;
import biweekly.property.Location;
import biweekly.property.Status;
import biweekly.property.Summary;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
public class ICalComponentTest {
	@SuppressWarnings("unchecked")
	@Test
	public void checkRequiredCardinality() {
		TestComponent comp = new TestComponent();
		comp.addProperty(new Summary(""));
		comp.addProperty(new Description(""));
		comp.addProperty(new Description(""));

		List<Warning> warnings = new ArrayList<Warning>();
		comp.checkRequiredCardinality(warnings, Summary.class, Description.class, Location.class);

		//too many instances of Description and no instances of Location
		assertWarnings(2, warnings);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void checkOptionalCardinality() {
		TestComponent comp = new TestComponent();
		comp.addProperty(new Summary(""));
		comp.addProperty(new Description(""));
		comp.addProperty(new Description(""));

		List<Warning> warnings = new ArrayList<Warning>();
		comp.checkOptionalCardinality(warnings, Summary.class, Description.class, Location.class);

		//too many instances of Description
		assertWarnings(1, warnings);
	}

	@Test
	public void checkStatus_valid() {
		TestComponent comp = new TestComponent();
		comp.addProperty(Status.cancelled());

		List<Warning> warnings = new ArrayList<Warning>();
		comp.checkStatus(warnings, Status.cancelled());

		assertWarnings(0, warnings);
	}

	@Test
	public void checkStatus_invalid() {
		TestComponent comp = new TestComponent();
		comp.addProperty(Status.cancelled());

		List<Warning> warnings = new ArrayList<Warning>();
		comp.checkStatus(warnings, Status.completed());

		assertWarnings(1, warnings);
	}

	@Test
	public void checkStatus_null() {
		TestComponent comp = new TestComponent();

		List<Warning> warnings = new ArrayList<Warning>();
		comp.checkStatus(warnings, Status.cancelled());

		assertWarnings(0, warnings);
	}

	private class TestComponent extends ICalComponent {
		//empty
	}
}
