package biweekly.io.scribe.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;

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
public class ICalComponentScribeTest {
	private final PartyMarshaller marshaller = new PartyMarshaller();

	@Test
	public void emptyInstance() {
		Party comp = marshaller.emptyInstance();
		assertTrue(comp.getProperties().isEmpty());
		assertTrue(comp.getComponents().isEmpty());
	}

	@Test
	public void getProperties() {
		Party comp = new Party();
		RawProperty rawProp = comp.addExperimentalProperty("X-DJ", "Jonny D");

		Collection<ICalProperty> props = marshaller.getProperties(comp);
		assertEquals(1, props.size());
		assertTrue(props.contains(rawProp));
	}

	@Test
	public void getComponents() {
		Party comp = new Party();
		RawComponent rawComp = comp.addExperimentalComponent("X-MUSIC");

		Collection<ICalComponent> comps = marshaller.getComponents(comp);
		assertEquals(1, comps.size());
		assertTrue(comps.contains(rawComp));
	}

	private class PartyMarshaller extends ICalComponentScribe<Party> {
		public PartyMarshaller() {
			super(Party.class, "X-PARTY");
		}

		@Override
		protected Party _newInstance() {
			Party party = new Party();
			party.addExperimentalProperty("X-DJ", "Jonny D");
			party.addExperimentalComponent("X-MUSIC");
			return party;
		}
	}

	private class Party extends ICalComponent {
		//empty
	}
}
