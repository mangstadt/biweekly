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
 * Defines the hierarchical relationship that a component has with another
 * component.
 * @author Michael Angstadt
 * @see "RFC 5545 p.25"
 */
public class RelationshipType extends EnumParameterValue {
	private static final ICalParameterCaseClasses<RelationshipType> enums = new ICalParameterCaseClasses<RelationshipType>(RelationshipType.class);

	public static final RelationshipType PARENT = new RelationshipType("PARENT");

	public static final RelationshipType CHILD = new RelationshipType("CHILD");

	public static final RelationshipType SIBLING = new RelationshipType("SIBLING");

	private RelationshipType(String value) {
		super(value);
	}

	/**
	 * Searches for a static constant.
	 * @param value the parameter value
	 * @return the object or null if not found
	 */
	public static RelationshipType find(String value) {
		return enums.find(value);
	}

	/**
	 * Searches for a static constant and creates one if it cannot be found. All
	 * created objects are assured to be unique, so multiple calls to this
	 * method will return the same instance.
	 * @param value the parameter value
	 * @return the object
	 */
	public static RelationshipType get(String value) {
		return enums.get(value);
	}
}
