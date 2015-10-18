package biweekly.io.chain;

import java.util.Collection;
import java.util.TimeZone;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.property.ICalProperty;

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
 * Parent class for all chaining writers. This class is package-private in order
 * to hide it from the generated Javadocs.
 * @author Michael Angstadt
 * @param <T> the object instance's type (for method chaining)
 */
class ChainingWriter<T extends ChainingWriter<?>> {
	final Collection<ICalendar> icals;
	ScribeIndex index;
	//	boolean prodId = true;
	//	boolean versionStrict = true;
	TimeZone defaultTimeZone = null;

	@SuppressWarnings("unchecked")
	private final T this_ = (T) this;

	/**
	 * @param icals the iCalendar objects to write
	 */
	ChainingWriter(Collection<ICalendar> icals) {
		this.icals = icals;
	}

	/**
	 * Sets the timezone to use when outputting date values (defaults to UTC).
	 * @param defaultTimeZone the default timezone
	 * @return this
	 */
	T tz(TimeZone defaultTimeZone) {
		this.defaultTimeZone = defaultTimeZone;
		return this_;
	}

	/**
	 * Registers a property scribe.
	 * @param scribe the scribe to register
	 * @return this
	 */
	T register(ICalPropertyScribe<? extends ICalProperty> scribe) {
		if (index == null) {
			index = new ScribeIndex();
		}
		index.register(scribe);
		return this_;
	}

	/**
	 * Registers a component scribe.
	 * @param scribe the scribe to register
	 * @return this
	 */
	T register(ICalComponentScribe<? extends ICalComponent> scribe) {
		if (index == null) {
			index = new ScribeIndex();
		}
		index.register(scribe);
		return this_;
	}
}
