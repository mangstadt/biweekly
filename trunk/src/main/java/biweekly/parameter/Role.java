package biweekly.parameter;

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
 * Defines the role that a calendar user holds.
 * @author Michael Angstadt
 * @see "RFC 5545 p.25-6"
 */
public class Role extends EnumParameterValue {
	private static final ICalParameterCaseClasses<Role> enums = new ICalParameterCaseClasses<Role>(Role.class);

	public static final Role CHAIR = new Role("CHAIR");

	public static final Role REQ_PARTICIPANT = new Role("REQ-PARTICIPANT");

	public static final Role OPT_PARTICIPANT = new Role("OPT-PARTICIPANT");

	public static final Role NON_PARTICIPANT = new Role("NON-PARTICIPANT");

	private Role(String value) {
		super(value);
	}

	/**
	 * Searches for a static constant.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static Role find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a static constant and creates one if it cannot be found. All
	 * created objects are assured to be unique, so multiple calls to this
	 * method will return the same instance.
	 * @param value the parameter value
	 * @return the object
	 */
	public static Role get(String value) {
		return enums.get(value);
	}
}
