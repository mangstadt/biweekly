package biweekly.io;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.property.ICalProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;

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
 * Defines the common methods that all iCalendar reader classes must have.
 * @author Michael Angstadt
 */
public interface IParser extends Closeable {
	/**
	 * Registers a marshaller for an experimental property.
	 * @param marshaller the marshaller to register
	 */
	void registerMarshaller(ICalPropertyMarshaller<? extends ICalProperty> marshaller);

	/**
	 * Registers a marshaller for an experimental component.
	 * @param marshaller the marshaller to register
	 */
	void registerMarshaller(ICalComponentMarshaller<? extends ICalComponent> marshaller);

	/**
	 * Gets the warnings from the last iCalendar object that was unmarshalled.
	 * This list is reset every time a new iCalendar object is read.
	 * @return the warnings or empty list if there were no warnings
	 */
	List<String> getWarnings();

	/**
	 * Reads the next iCalendar object.
	 * @return the next iCalendar object or null if there are no more
	 * @throws IOException if there's a problem reading from the stream
	 */
	ICalendar readNext() throws IOException;
}
