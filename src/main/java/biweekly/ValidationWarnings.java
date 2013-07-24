package biweekly;

import java.util.List;

import biweekly.component.ICalComponent;
import biweekly.property.ICalProperty;
import biweekly.util.StringUtils;
import biweekly.util.StringUtils.JoinCallback;

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
 * Holds the validation warnings of a property or component.
 * @author Michael Angstadt
 */
public class ValidationWarnings {
	private final ICalProperty property;
	private final ICalComponent component;
	private final List<ICalComponent> componentHierarchy;
	private final List<String> messages;

	/**
	 * Creates a new set of validation warnings for a property.
	 * @param property the property that caused the warnings
	 * @param componentHierarchy the hierarchy of components that the property
	 * belongs to
	 * @param messages the warning messages
	 */
	public ValidationWarnings(ICalProperty property, List<ICalComponent> componentHierarchy, List<String> messages) {
		this(null, property, componentHierarchy, messages);
	}

	/**
	 * Creates a new set of validation warnings for a component.
	 * @param component the component that caused the warnings
	 * @param componentHierarchy the hierarchy of components that the component
	 * belongs to
	 * @param messages the warning messages
	 */
	public ValidationWarnings(ICalComponent component, List<ICalComponent> componentHierarchy, List<String> messages) {
		this(component, null, componentHierarchy, messages);
	}

	private ValidationWarnings(ICalComponent component, ICalProperty property, List<ICalComponent> componentHierarchy, List<String> messages) {
		this.component = component;
		this.property = property;
		this.componentHierarchy = componentHierarchy;
		this.messages = messages;
	}

	/**
	 * Gets the property object that caused the validation warnings.
	 * @return the property object or null if a component cause the warnings.
	 */
	public ICalProperty getProperty() {
		return property;
	}

	/**
	 * Gets the component object that caused the validation warnings.
	 * @return the component object or null if a property caused the warnings.
	 */
	public ICalComponent getComponent() {
		return component;
	}

	/**
	 * Gets the hierarchy of components that the property or component belongs
	 * to.
	 * @return the component hierarchy
	 */
	public List<ICalComponent> getComponentHierarchy() {
		return componentHierarchy;
	}

	/**
	 * Gets the warning messages.
	 * @return the warning messages
	 */
	public List<String> getMessages() {
		return messages;
	}

	@Override
	public String toString() {
		final String prefix = "[" + buildPath() + "]: ";
		return StringUtils.join(messages, StringUtils.NEWLINE, new JoinCallback<String>() {
			public void handle(StringBuilder sb, String message) {
				sb.append(prefix).append(message);
			}
		});
	}

	private String buildPath() {
		StringBuilder sb = new StringBuilder();

		if (!componentHierarchy.isEmpty()) {
			String delimitor = " > ";

			StringUtils.join(componentHierarchy, delimitor, sb, new JoinCallback<ICalComponent>() {
				public void handle(StringBuilder sb, ICalComponent component) {
					sb.append(component.getClass().getSimpleName());
				}
			});
			sb.append(delimitor);
		}

		if (property != null) {
			sb.append(property.getClass().getSimpleName());
		} else {
			sb.append(component.getClass().getSimpleName());
		}

		return sb.toString();
	}
}
