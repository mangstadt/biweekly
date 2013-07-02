package biweekly.component.marshaller;

import java.util.Collection;

import biweekly.component.ICalComponent;
import biweekly.property.ICalProperty;

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
 * Base class for iCalendar component marshallers.
 * @author Michael Angstadt
 */
public abstract class ICalComponentMarshaller<T extends ICalComponent> {
	protected final Class<T> clazz;
	protected final String componentName;

	/**
	 * Creates a new component marshaller.
	 * @param clazz the component's class
	 * @param componentName the component's name (e.g. "VEVENT")
	 */
	public ICalComponentMarshaller(Class<T> clazz, String componentName) {
		this.clazz = clazz;
		this.componentName = componentName;
	}

	/**
	 * Gets the component class.
	 * @return the component class.
	 */
	public Class<T> getComponentClass() {
		return clazz;
	}

	/**
	 * Gets the component's name.
	 * @return the compent's name (e.g. "VEVENT")
	 */
	public String getComponentName() {
		return componentName;
	}

	/**
	 * Creates a new instance of the component class that doesn't have any
	 * properties or sub-components.
	 * @return the new instance
	 */
	public T emptyInstance() {
		T component = _newInstance();

		//remove any properties/components that were created in the constructor
		component.getProperties().clear();
		component.getComponents().clear();

		return component;
	}

	/**
	 * Creates a new instance of the component class.
	 * @return the new instance
	 */
	protected abstract T _newInstance();

	/**
	 * Gets the sub-components to marshal. Child classes can override this for
	 * better control over which components are marshalled.
	 * @param component the component
	 * @return the sub-components to marshal
	 */
	public Collection<ICalComponent> getComponents(T component) {
		return component.getComponents().values();
	}

	/**
	 * Gets the properties to marshal. Child classes can override this for
	 * better control over which properties are marshalled.
	 * @param component the component
	 * @return the properties to marshal
	 */
	public Collection<ICalProperty> getProperties(T component) {
		return component.getProperties().values();
	}
}
