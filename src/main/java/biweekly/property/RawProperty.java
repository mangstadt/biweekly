package biweekly.property;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.util.CharacterBitSet;

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
 * Represents a property that does not have a scribe associated with it.
 * @author Michael Angstadt
 */
public class RawProperty extends ICalProperty {
	private String name;
	private ICalDataType dataType;
	private String value;

	/**
	 * Creates a raw property.
	 * @param name the property name (e.g. "X-MS-ANNIVERSARY")
	 * @param value the property value
	 */
	public RawProperty(String name, String value) {
		this(name, null, value);
	}

	/**
	 * Creates a raw property.
	 * @param name the property name (e.g. "X-MS-ANNIVERSARY")
	 * @param dataType the property value's data type
	 * @param value the property value
	 */
	public RawProperty(String name, ICalDataType dataType, String value) {
		this.name = name;
		this.dataType = dataType;
		this.value = value;
	}

	/**
	 * Copy constructor.
	 * @param original the property to make a copy of
	 */
	public RawProperty(RawProperty original) {
		super(original);
		name = original.name;
		dataType = original.dataType;
		value = original.value;
	}

	/**
	 * Gets the property value.
	 * @return the property value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Gets the property value's data type.
	 * @return the data type
	 */
	public ICalDataType getDataType() {
		return dataType;
	}

	/**
	 * Gets the property name.
	 * @return the property name (e.g. "X-MS-ANNIVERSARY")
	 */
	public String getName() {
		return name;
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		CharacterBitSet validCharacters = new CharacterBitSet("-a-zA-Z0-9");
		if (!validCharacters.containsOnly(name)) {
			warnings.add(Warning.validate(52, name));
		}
	}

	@Override
	protected Map<String, Object> toStringValues() {
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put("name", name);
		values.put("value", value);
		values.put("dataType", dataType);
		return values;
	}

	@Override
	public RawProperty copy() {
		return new RawProperty(this);
	}
}
