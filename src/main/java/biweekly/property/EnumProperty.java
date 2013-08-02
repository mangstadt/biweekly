package biweekly.property;

import java.util.Collection;
import java.util.List;

import biweekly.component.ICalComponent;

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
 * Parent class for properties that have a defined set of acceptable values (for
 * example, {@link Action}).
 * @author Michael Angstadt
 */
public abstract class EnumProperty extends TextProperty {
	/**
	 * Creates an enum property.
	 * @param value the property value
	 */
	public EnumProperty(String value) {
		super(value);
	}

	/**
	 * Compares the property's value with a given string (case-insensitive).
	 * @param value the string
	 * @return true if it's equal, false if not
	 */
	protected boolean is(String value) {
		return value.equalsIgnoreCase(this.value);
	}

	/**
	 * Gets the list of acceptable values for this property.
	 * @return the list of acceptable values
	 */
	protected abstract Collection<String> getStandardValues();

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		super.validate(components, warnings);
		if (value == null) {
			return;
		}

		Collection<String> standardValues = getStandardValues();
		for (String standardValue : standardValues) {
			if (value.equalsIgnoreCase(standardValue)) {
				//found, value is OK
				return;
			}
		}

		warnings.add("Non-standard value \"" + value + "\".  Standard values are: " + standardValues);
	}
}
