package biweekly.property.marshaller;

import java.util.HashMap;
import java.util.Map;

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
 * Contains the listing of all property marshallers.
 * @author Michael Angstadt
 */
public class PropertyLibrary {
	private static final Map<String, ICalPropertyMarshaller<? extends ICalProperty>> byPropName = new HashMap<String, ICalPropertyMarshaller<? extends ICalProperty>>();
	private static final Map<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>> byClass = new HashMap<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>>();
	static {
		addMarshaller(new ActionMarshaller());
		addMarshaller(new AttachmentMarshaller());
		addMarshaller(new AttendeeMarshaller());
		addMarshaller(new CalendarScaleMarshaller());
		addMarshaller(new CategoriesMarshaller());
		addMarshaller(new ClassificationMarshaller());
		addMarshaller(new CommentMarshaller());
		addMarshaller(new CompletedMarshaller());
		addMarshaller(new ContactMarshaller());
		addMarshaller(new CreatedMarshaller());
		addMarshaller(new DateDueMarshaller());
		addMarshaller(new DateEndMarshaller());
		addMarshaller(new DateStartMarshaller());
		addMarshaller(new DateTimeStampMarshaller());
		addMarshaller(new DescriptionMarshaller());
		addMarshaller(new DurationPropertyMarshaller());
		addMarshaller(new ExceptionDatesMarshaller());
		addMarshaller(new FreeBusyMarshaller());
		addMarshaller(new GeoMarshaller());
		addMarshaller(new LastModifiedMarshaller());
		addMarshaller(new LocationMarshaller());
		addMarshaller(new MethodMarshaller());
		addMarshaller(new OrganizerMarshaller());
		addMarshaller(new PercentCompleteMarshaller());
		addMarshaller(new PriorityMarshaller());
		addMarshaller(new ProductIdMarshaller());
		addMarshaller(new RecurrenceDatesMarshaller());
		addMarshaller(new RecurrenceIdMarshaller());
		addMarshaller(new RecurrenceRuleMarshaller());
		addMarshaller(new RelatedToMarshaller());
		addMarshaller(new RepeatMarshaller());
		addMarshaller(new RequestStatusMarshaller());
		addMarshaller(new ResourcesMarshaller());
		addMarshaller(new SequenceMarshaller());
		addMarshaller(new StatusMarshaller());
		addMarshaller(new SummaryMarshaller());
		addMarshaller(new TimezoneIdMarshaller());
		addMarshaller(new TimezoneNameMarshaller());
		addMarshaller(new TimezoneOffsetFromMarshaller());
		addMarshaller(new TimezoneOffsetToMarshaller());
		addMarshaller(new TimezoneUrlMarshaller());
		addMarshaller(new TransparencyMarshaller());
		addMarshaller(new TriggerMarshaller());
		addMarshaller(new UidMarshaller());
		addMarshaller(new UrlMarshaller());
		addMarshaller(new VersionMarshaller());
	}

	/**
	 * Gets a property marshaller by name.
	 * @param propertyName the component name (e.g. "VERSION")
	 * @return the property marshaller or null if not found
	 */
	public static ICalPropertyMarshaller<? extends ICalProperty> getMarshaller(String propertyName) {
		return byPropName.get(propertyName.toUpperCase());
	}

	/**
	 * Gets a property marshaller by class.
	 * @param clazz the property class
	 * @return the property marshaller or null if not found
	 */
	public static ICalPropertyMarshaller<? extends ICalProperty> getMarshaller(Class<? extends ICalProperty> clazz) {
		return byClass.get(clazz);
	}

	private static void addMarshaller(ICalPropertyMarshaller<? extends ICalProperty> m) {
		byPropName.put(m.getPropertyName().toUpperCase(), m);
		byClass.put(m.getPropertyClass(), m);
	}

	private PropertyLibrary() {
		//hide
	}
}
