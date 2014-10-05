package biweekly.io.scribe;

import static biweekly.util.TestUtils.each;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;

import org.junit.Before;
import org.junit.Test;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.component.ICalComponent;
import biweekly.component.VEvent;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.component.RawComponentScribe;
import biweekly.io.scribe.component.VEventScribe;
import biweekly.io.scribe.component.VFreeBusyScribe;
import biweekly.io.scribe.property.CreatedScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.RawPropertyScribe;
import biweekly.io.scribe.property.UidScribe;
import biweekly.io.scribe.property.XmlScribe;
import biweekly.io.xml.XCalNamespaceContext;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.Uid;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
public class ScribeIndexTest {
	private ScribeIndex index;

	@Before
	public void before() {
		index = new ScribeIndex();
	}

	@Test
	public void getComponentScribe_name() {
		ICalComponentScribe<? extends ICalComponent> scribe;
		String name;

		name = "VEVENT";
		for (ICalVersion version : ICalVersion.values()) {
			scribe = index.getComponentScribe(name, version);
			assertTrue(scribe instanceof VEventScribe);
		}
		scribe = index.getComponentScribe(name, null);
		assertTrue(scribe instanceof VEventScribe);

		name = "VFREEBUSY";
		scribe = index.getComponentScribe(name, ICalVersion.V1_0);
		assertTrue(scribe instanceof RawComponentScribe);
		for (ICalVersion version : each(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0)) {
			scribe = index.getComponentScribe(name, version);
			assertTrue(scribe instanceof VFreeBusyScribe);
		}
		scribe = index.getComponentScribe(name, null);
		assertTrue(scribe instanceof VFreeBusyScribe);

		name = "VFOO";
		for (ICalVersion version : ICalVersion.values()) {
			scribe = index.getComponentScribe(name, version);
			assertTrue(scribe instanceof RawComponentScribe);
		}
		scribe = index.getComponentScribe(name, null);
		assertTrue(scribe instanceof RawComponentScribe);
	}

	@Test
	public void getComponentScribe_class() {
		ICalComponentScribe<? extends ICalComponent> scribe;

		scribe = index.getComponentScribe(VEvent.class);
		assertTrue(scribe instanceof VEventScribe);

		scribe = index.getComponentScribe(ICalComponentImpl.class);
		assertNull(scribe);
	}

	@Test
	public void getPropertyScribe_name() {
		ICalPropertyScribe<? extends ICalProperty> scribe;
		String name;

		name = "UID";
		for (ICalVersion version : ICalVersion.values()) {
			scribe = index.getPropertyScribe(name, version);
			assertTrue(scribe instanceof UidScribe);
		}
		scribe = index.getPropertyScribe(name, null);
		assertTrue(scribe instanceof UidScribe);

		name = "DCREATED";
		scribe = index.getPropertyScribe(name, ICalVersion.V1_0);
		assertTrue(scribe instanceof CreatedScribe);
		for (ICalVersion version : each(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0)) {
			scribe = index.getPropertyScribe(name, version);
			assertTrue(scribe instanceof RawPropertyScribe);
		}
		scribe = index.getPropertyScribe(name, null);
		assertTrue(scribe instanceof CreatedScribe);

		name = "CREATED";
		scribe = index.getPropertyScribe(name, ICalVersion.V1_0);
		assertTrue(scribe instanceof RawPropertyScribe);
		for (ICalVersion version : each(ICalVersion.V2_0_DEPRECATED, ICalVersion.V2_0)) {
			scribe = index.getPropertyScribe(name, version);
			assertTrue(scribe instanceof CreatedScribe);
		}
		scribe = index.getPropertyScribe(name, null);
		assertTrue(scribe instanceof CreatedScribe);
	}

	@Test
	public void getPropertyScribe_class() {
		ICalPropertyScribe<? extends ICalProperty> scribe;

		scribe = index.getPropertyScribe(Uid.class);
		assertTrue(scribe instanceof UidScribe);

		scribe = index.getPropertyScribe(ICalPropertyImpl.class);
		assertNull(scribe);
	}

	@Test
	public void getPropertyScribe_qname() {
		ICalPropertyScribe<? extends ICalProperty> scribe;

		scribe = index.getPropertyScribe(new QName(XCalNamespaceContext.XCAL_NS, "uid"));
		assertTrue(scribe instanceof UidScribe);

		scribe = index.getPropertyScribe(new QName(XCalNamespaceContext.XCAL_NS, "foo"));
		assertTrue(scribe instanceof RawPropertyScribe);

		scribe = index.getPropertyScribe(new QName("ns", "foo"));
		assertTrue(scribe instanceof XmlScribe);
	}

	@Test
	public void register_component() {
		ICalComponentScribe<? extends ICalComponent> scribe;

		scribe = index.getComponentScribe(ICalComponentImpl.class);
		assertNull(scribe);

		ICalComponentScribe<? extends ICalComponent> customScribe = new ICalComponentImplScribe();
		index.register(customScribe);

		scribe = index.getComponentScribe(ICalComponentImpl.class);
		assertEquals(customScribe, scribe);

		index.unregister(customScribe);

		scribe = index.getComponentScribe(ICalComponentImpl.class);
		assertNull(scribe);
	}

	@Test
	public void register_property() {
		ICalPropertyScribe<? extends ICalProperty> scribe;

		scribe = index.getPropertyScribe(ICalPropertyImpl.class);
		assertNull(scribe);

		ICalPropertyScribe<? extends ICalProperty> customScribe = new ICalPropertyImplScribe();
		index.register(customScribe);

		scribe = index.getPropertyScribe(ICalPropertyImpl.class);
		assertEquals(customScribe, scribe);

		index.unregister(customScribe);

		scribe = index.getPropertyScribe(ICalPropertyImpl.class);
		assertNull(scribe);
	}

	private class ICalComponentImpl extends ICalComponent {
		//empty
	}

	private class ICalComponentImplScribe extends ICalComponentScribe<ICalComponentImpl> {
		public ICalComponentImplScribe() {
			super(ICalComponentImpl.class, "");
		}

		@Override
		protected ICalComponentImpl _newInstance() {
			return null;
		}
	}

	private class ICalPropertyImpl extends ICalProperty {
		//empty
	}

	private class ICalPropertyImplScribe extends ICalPropertyScribe<ICalPropertyImpl> {
		public ICalPropertyImplScribe() {
			super(ICalPropertyImpl.class, "", null);
		}

		@Override
		protected String _writeText(ICalPropertyImpl property, WriteContext context) {
			return null;
		}

		@Override
		protected ICalPropertyImpl _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
			return null;
		}
	}
}
