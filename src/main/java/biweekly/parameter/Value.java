package biweekly.parameter;

import java.util.Collection;

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
 * Defines the data type of a property's value.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-29">RFC 5545
 * p.29-50</a>
 */
public class Value extends EnumParameterValue {
	private static final ICalParameterCaseClasses<Value> enums = new ICalParameterCaseClasses<Value>(Value.class);

	public static final Value BINARY = new Value("BINARY");
	public static final Value BOOLEAN = new Value("BOOLEAN");
	public static final Value CAL_ADDRESS = new Value("CAL-ADDRESS");
	public static final Value DATE = new Value("DATE");
	public static final Value DATE_TIME = new Value("DATE-TIME");
	public static final Value DURATION = new Value("DURATION");
	public static final Value FLOAT = new Value("FLOAT");
	public static final Value INTEGER = new Value("INTEGER");
	public static final Value PERIOD = new Value("PERIOD");
	public static final Value RECUR = new Value("RECUR");
	public static final Value TEXT = new Value("TEXT");
	public static final Value TIME = new Value("TIME");
	public static final Value URI = new Value("URI");
	public static final Value UTC_OFFSET = new Value("UTC-OFFSET");

	private Value(String value) {
		super(value);
	}

	/**
	 * Searches for a parameter value that is defined as a static constant in
	 * this class.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static Value find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a parameter value and creates one if it cannot be found. All
	 * objects are guaranteed to be unique, so they can be compared with
	 * <code>==</code> equality.
	 * @param value the parameter value
	 * @return the object
	 */
	public static Value get(String value) {
		return enums.get(value);
	}

	/**
	 * Gets all of the parameter values that are defined as static constants in
	 * this class.
	 * @return the parameter values
	 */
	public static Collection<Value> all() {
		return enums.all();
	}
}
