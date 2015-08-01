package biweekly.io;

import java.util.ArrayList;
import java.util.List;

import biweekly.Messages;
import biweekly.Warning;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * Records iCalendar parse warnings.
 * @author Michael Angstadt
 */
public class ParseWarnings {
	private final List<String> warnings = new ArrayList<String>();

	/**
	 * Adds a parse warning.
	 * @param line the line number or null if unknown
	 * @param propertyName the property name or null if N/A
	 * @param code the message code from the resource bundle
	 * @param args the message arguments
	 */
	public void add(Integer line, String propertyName, int code, Object... args) {
		Warning warning = Warning.parse(code, args);
		add(line, propertyName, warning);
	}

	/**
	 * Adds a parse warning.
	 * @param line the line number or null if unknown
	 * @param propertyName the property name or null if N/A
	 * @param message the warning message
	 */
	public void add(Integer line, String propertyName, Warning message) {
		if (line == null && propertyName == null) {
			warnings.add(message.toString());
			return;
		}

		String key;
		if (line == null && propertyName != null) {
			key = "parse.prop";
		} else if (line != null && propertyName == null) {
			key = "parse.line";
		} else {
			key = "parse.lineWithProp";
		}

		String warning = Messages.INSTANCE.getMessage(key, line, propertyName, message);
		warnings.add(warning);
	}

	/**
	 * Creates a copy of this warnings list.
	 * @return the copy
	 */
	public List<String> copy() {
		return new ArrayList<String>(warnings);
	}

	/**
	 * Clears the warnings list.
	 */
	public void clear() {
		warnings.clear();
	}
}
