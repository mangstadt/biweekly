package biweekly.component.marshaller;

import java.util.HashMap;
import java.util.Map;

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
 * Contains the listing of all component marshallers.
 * @author Michael Angstadt
 */
public class ComponentLibrary {
	private static final Map<String, ICalComponentMarshaller<? extends ICalComponent>> byPropName = new HashMap<String, ICalComponentMarshaller<? extends ICalComponent>>();
	private static final Map<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>> byClass = new HashMap<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>>();
	static {
		addMarshaller(new ICalendarMarshaller());
		addMarshaller(new VAlarmMarshaller());
		addMarshaller(new VEventMarshaller());
		addMarshaller(new VFreeBusyMarshaller());
		addMarshaller(new VTodoMarshaller());
		addMarshaller(new VTimezoneMarshaller());
		addMarshaller(new StandardTimeMarshaller());
		addMarshaller(new DaylightSavingsTimeMarshaller());
	}

	/**
	 * Gets a component marshaller by name.
	 * @param componentName the component name (e.g. "VEVENT")
	 * @return the component marshaller or null if not found
	 */
	public static ICalComponentMarshaller<? extends ICalComponent> getMarshaller(String componentName) {
		return byPropName.get(componentName.toUpperCase());
	}

	/**
	 * Gets a component marshaller by class.
	 * @param clazz the component class
	 * @return the component marshaller or null if not found
	 */
	public static ICalComponentMarshaller<? extends ICalComponent> getMarshaller(Class<? extends ICalComponent> clazz) {
		return byClass.get(clazz);
	}

	private static void addMarshaller(ICalComponentMarshaller<? extends ICalComponent> m) {
		byPropName.put(m.getComponentName().toUpperCase(), m);
		byClass.put(m.getComponentClass(), m);
	}

	private ComponentLibrary() {
		//hide
	}
}
