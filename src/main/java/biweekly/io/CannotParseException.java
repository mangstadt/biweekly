package biweekly.io;

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
 */

/**
 * Thrown during the unmarshalling of an iCalendar property to signal that the
 * property's value could not be parsed (for example, being unable to parse a
 * date string).
 * @author Michael Angstadt
 */
@SuppressWarnings("serial")
public class CannotParseException extends RuntimeException {
	private final Warning warning;

	/**
	 * Creates a new "cannot parse" exception.
	 * @param code the warning message code
	 * @param args the warning message arguments
	 */
	public CannotParseException(int code, Object... args) {
		this(Warning.parse(code, args));
	}

	/**
	 * Creates a new "cannot parse" exception.
	 * @param reason the reason why the property value cannot be parsed
	 */
	public CannotParseException(String reason) {
		this(new Warning(reason));
	}

	private CannotParseException(Warning warning) {
		super(warning.toString());
		this.warning = warning;
	}

	public Warning getWarning() {
		return warning;
	}
}
