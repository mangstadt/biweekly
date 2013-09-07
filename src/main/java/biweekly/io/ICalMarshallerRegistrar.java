package biweekly.io;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.RawComponent;
import biweekly.component.marshaller.DaylightSavingsTimeMarshaller;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.component.marshaller.ICalendarMarshaller;
import biweekly.component.marshaller.RawComponentMarshaller;
import biweekly.component.marshaller.StandardTimeMarshaller;
import biweekly.component.marshaller.VAlarmMarshaller;
import biweekly.component.marshaller.VEventMarshaller;
import biweekly.component.marshaller.VFreeBusyMarshaller;
import biweekly.component.marshaller.VJournalMarshaller;
import biweekly.component.marshaller.VTimezoneMarshaller;
import biweekly.component.marshaller.VTodoMarshaller;
import biweekly.io.xml.XCalNamespaceContext;
import biweekly.property.ICalProperty;
import biweekly.property.RawProperty;
import biweekly.property.Xml;
import biweekly.property.marshaller.ActionMarshaller;
import biweekly.property.marshaller.AttachmentMarshaller;
import biweekly.property.marshaller.AttendeeMarshaller;
import biweekly.property.marshaller.CalendarScaleMarshaller;
import biweekly.property.marshaller.CategoriesMarshaller;
import biweekly.property.marshaller.ClassificationMarshaller;
import biweekly.property.marshaller.CommentMarshaller;
import biweekly.property.marshaller.CompletedMarshaller;
import biweekly.property.marshaller.ContactMarshaller;
import biweekly.property.marshaller.CreatedMarshaller;
import biweekly.property.marshaller.DateDueMarshaller;
import biweekly.property.marshaller.DateEndMarshaller;
import biweekly.property.marshaller.DateStartMarshaller;
import biweekly.property.marshaller.DateTimeStampMarshaller;
import biweekly.property.marshaller.DescriptionMarshaller;
import biweekly.property.marshaller.DurationPropertyMarshaller;
import biweekly.property.marshaller.ExceptionDatesMarshaller;
import biweekly.property.marshaller.ExceptionRuleMarshaller;
import biweekly.property.marshaller.FreeBusyMarshaller;
import biweekly.property.marshaller.GeoMarshaller;
import biweekly.property.marshaller.ICalPropertyMarshaller;
import biweekly.property.marshaller.LastModifiedMarshaller;
import biweekly.property.marshaller.LocationMarshaller;
import biweekly.property.marshaller.MethodMarshaller;
import biweekly.property.marshaller.OrganizerMarshaller;
import biweekly.property.marshaller.PercentCompleteMarshaller;
import biweekly.property.marshaller.PriorityMarshaller;
import biweekly.property.marshaller.ProductIdMarshaller;
import biweekly.property.marshaller.RawPropertyMarshaller;
import biweekly.property.marshaller.RecurrenceDatesMarshaller;
import biweekly.property.marshaller.RecurrenceIdMarshaller;
import biweekly.property.marshaller.RecurrenceRuleMarshaller;
import biweekly.property.marshaller.RelatedToMarshaller;
import biweekly.property.marshaller.RepeatMarshaller;
import biweekly.property.marshaller.RequestStatusMarshaller;
import biweekly.property.marshaller.ResourcesMarshaller;
import biweekly.property.marshaller.SequenceMarshaller;
import biweekly.property.marshaller.StatusMarshaller;
import biweekly.property.marshaller.SummaryMarshaller;
import biweekly.property.marshaller.TimezoneIdMarshaller;
import biweekly.property.marshaller.TimezoneNameMarshaller;
import biweekly.property.marshaller.TimezoneOffsetFromMarshaller;
import biweekly.property.marshaller.TimezoneOffsetToMarshaller;
import biweekly.property.marshaller.TimezoneUrlMarshaller;
import biweekly.property.marshaller.TransparencyMarshaller;
import biweekly.property.marshaller.TriggerMarshaller;
import biweekly.property.marshaller.UidMarshaller;
import biweekly.property.marshaller.UrlMarshaller;
import biweekly.property.marshaller.VersionMarshaller;
import biweekly.property.marshaller.XmlMarshaller;

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
 * <p>
 * Manages a listing of component and property marshallers. This is useful for
 * injecting the marshallers of any experimental components or properties you
 * have defined into a reader or writer object. The same object instance can be
 * reused and injected into multiple reader/writer classes.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * //init the registrar
 * ICalMarshallerRegistrar registrar = new ICalMarshallerRegistrar();
 * registrar.register(new CustomPropertyMarshaller());
 * registrar.register(new AnotherCustomPropertyMarshaller());
 * registrar.register(new CustomComponentMarshaller());
 * 
 * //inject into a reader class
 * ICalReader textReader = new ICalReader(...);
 * textReader.setRegistrar(registrar);
 * List&lt;ICalendar&gt; icals = new ArrayList&lt;ICalendar&gt;();
 * ICalendar ical;
 * while ((ical = textReader.readNext()) != null){
 *   icals.add(ical);
 * }
 * 
 * //inject the same instance in another reader/writer class
 * JCalWriter writer = new JCalWriter(...);
 * writer.setRegistrar(registrar);
 * for (ICalendar ical : icals){
 *   writer.write(ical);
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 */
public class ICalMarshallerRegistrar {
	//define standard component marshallers
	private static final Map<String, ICalComponentMarshaller<? extends ICalComponent>> standardCompByName = new HashMap<String, ICalComponentMarshaller<? extends ICalComponent>>();
	private static final Map<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>> standardCompByClass = new HashMap<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>>();
	static {
		registerStandard(new ICalendarMarshaller());
		registerStandard(new VAlarmMarshaller());
		registerStandard(new VEventMarshaller());
		registerStandard(new VFreeBusyMarshaller());
		registerStandard(new VJournalMarshaller());
		registerStandard(new VTodoMarshaller());
		registerStandard(new VTimezoneMarshaller());
		registerStandard(new StandardTimeMarshaller());
		registerStandard(new DaylightSavingsTimeMarshaller());
	}

	//define standard property marshallers
	private static final Map<String, ICalPropertyMarshaller<? extends ICalProperty>> standardPropByName = new HashMap<String, ICalPropertyMarshaller<? extends ICalProperty>>();
	private static final Map<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>> standardPropByClass = new HashMap<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>>();
	private static final Map<QName, ICalPropertyMarshaller<? extends ICalProperty>> standardPropByQName = new HashMap<QName, ICalPropertyMarshaller<? extends ICalProperty>>();
	static {
		//RFC 5545
		registerStandard(new ActionMarshaller());
		registerStandard(new AttachmentMarshaller());
		registerStandard(new AttendeeMarshaller());
		registerStandard(new CalendarScaleMarshaller());
		registerStandard(new CategoriesMarshaller());
		registerStandard(new ClassificationMarshaller());
		registerStandard(new CommentMarshaller());
		registerStandard(new CompletedMarshaller());
		registerStandard(new ContactMarshaller());
		registerStandard(new CreatedMarshaller());
		registerStandard(new DateDueMarshaller());
		registerStandard(new DateEndMarshaller());
		registerStandard(new DateStartMarshaller());
		registerStandard(new DateTimeStampMarshaller());
		registerStandard(new DescriptionMarshaller());
		registerStandard(new DurationPropertyMarshaller());
		registerStandard(new ExceptionDatesMarshaller());
		registerStandard(new FreeBusyMarshaller());
		registerStandard(new GeoMarshaller());
		registerStandard(new LastModifiedMarshaller());
		registerStandard(new LocationMarshaller());
		registerStandard(new MethodMarshaller());
		registerStandard(new OrganizerMarshaller());
		registerStandard(new PercentCompleteMarshaller());
		registerStandard(new PriorityMarshaller());
		registerStandard(new ProductIdMarshaller());
		registerStandard(new RecurrenceDatesMarshaller());
		registerStandard(new RecurrenceIdMarshaller());
		registerStandard(new RecurrenceRuleMarshaller());
		registerStandard(new RelatedToMarshaller());
		registerStandard(new RepeatMarshaller());
		registerStandard(new RequestStatusMarshaller());
		registerStandard(new ResourcesMarshaller());
		registerStandard(new SequenceMarshaller());
		registerStandard(new StatusMarshaller());
		registerStandard(new SummaryMarshaller());
		registerStandard(new TimezoneIdMarshaller());
		registerStandard(new TimezoneNameMarshaller());
		registerStandard(new TimezoneOffsetFromMarshaller());
		registerStandard(new TimezoneOffsetToMarshaller());
		registerStandard(new TimezoneUrlMarshaller());
		registerStandard(new TransparencyMarshaller());
		registerStandard(new TriggerMarshaller());
		registerStandard(new UidMarshaller());
		registerStandard(new UrlMarshaller());
		registerStandard(new VersionMarshaller());

		//RFC 6321
		registerStandard(new XmlMarshaller());

		//RFC 2445
		registerStandard(new ExceptionRuleMarshaller());
	}

	private final Map<String, ICalComponentMarshaller<? extends ICalComponent>> experimentalCompByName = new HashMap<String, ICalComponentMarshaller<? extends ICalComponent>>(0);
	private final Map<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>> experimentalCompByClass = new HashMap<Class<? extends ICalComponent>, ICalComponentMarshaller<? extends ICalComponent>>(0);

	private final Map<String, ICalPropertyMarshaller<? extends ICalProperty>> experimentalPropByName = new HashMap<String, ICalPropertyMarshaller<? extends ICalProperty>>(0);
	private final Map<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>> experimentalPropByClass = new HashMap<Class<? extends ICalProperty>, ICalPropertyMarshaller<? extends ICalProperty>>(0);
	private final Map<QName, ICalPropertyMarshaller<? extends ICalProperty>> experimentalPropByQName = new HashMap<QName, ICalPropertyMarshaller<? extends ICalProperty>>(0);

	/**
	 * Gets a component marshaller by name.
	 * @param componentName the component name (e.g. "VEVENT")
	 * @return the component marshaller or null if not found
	 */
	public ICalComponentMarshaller<? extends ICalComponent> getComponentMarshaller(String componentName) {
		componentName = componentName.toUpperCase();

		ICalComponentMarshaller<? extends ICalComponent> marshaller = experimentalCompByName.get(componentName);
		if (marshaller != null) {
			return marshaller;
		}

		marshaller = standardCompByName.get(componentName);
		if (marshaller != null) {
			return marshaller;
		}

		return new RawComponentMarshaller(componentName);
	}

	/**
	 * Gets a property marshaller by name.
	 * @param propertyName the component name (e.g. "VERSION")
	 * @return the property marshaller or null if not found
	 */
	public ICalPropertyMarshaller<? extends ICalProperty> getPropertyMarshaller(String propertyName) {
		propertyName = propertyName.toUpperCase();

		ICalPropertyMarshaller<? extends ICalProperty> marshaller = experimentalPropByName.get(propertyName);
		if (marshaller != null) {
			return marshaller;
		}

		marshaller = standardPropByName.get(propertyName);
		if (marshaller != null) {
			return marshaller;
		}

		return new RawPropertyMarshaller(propertyName);
	}

	/**
	 * Gets a component marshaller by class.
	 * @param clazz the component class
	 * @return the component marshaller or null if not found
	 */
	public ICalComponentMarshaller<? extends ICalComponent> getComponentMarshaller(Class<? extends ICalComponent> clazz) {
		ICalComponentMarshaller<? extends ICalComponent> marshaller = experimentalCompByClass.get(clazz);
		if (marshaller != null) {
			return marshaller;
		}

		return standardCompByClass.get(clazz);
	}

	/**
	 * Gets a property marshaller by class.
	 * @param clazz the property class
	 * @return the property marshaller or null if not found
	 */
	public ICalPropertyMarshaller<? extends ICalProperty> getPropertyMarshaller(Class<? extends ICalProperty> clazz) {
		ICalPropertyMarshaller<? extends ICalProperty> marshaller = experimentalPropByClass.get(clazz);
		if (marshaller != null) {
			return marshaller;
		}

		return standardPropByClass.get(clazz);
	}

	/**
	 * Gets the appropriate component marshaller for a given component instance.
	 * @param component the component instance
	 * @return the component marshaller or null if not found
	 */
	public ICalComponentMarshaller<? extends ICalComponent> getComponentMarshaller(ICalComponent component) {
		if (component instanceof RawComponent) {
			RawComponent raw = (RawComponent) component;
			return new RawComponentMarshaller(raw.getName());
		}

		return getComponentMarshaller(component.getClass());
	}

	/**
	 * Gets the appropriate property marshaller for a given property instance.
	 * @param property the property instance
	 * @return the property marshaller or null if not found
	 */
	public ICalPropertyMarshaller<? extends ICalProperty> getPropertyMarshaller(ICalProperty property) {
		if (property instanceof RawProperty) {
			RawProperty raw = (RawProperty) property;
			return new RawPropertyMarshaller(raw.getName());
		}

		return getPropertyMarshaller(property.getClass());
	}

	/**
	 * Gets a property marshaller by XML local name and namespace.
	 * @param qname the XML local name and namespace
	 * @return the property marshaller or null if not found
	 */
	public ICalPropertyMarshaller<? extends ICalProperty> getPropertyMarshaller(QName qname) {
		ICalPropertyMarshaller<? extends ICalProperty> marshaller = experimentalPropByQName.get(qname);
		if (marshaller != null) {
			return marshaller;
		}

		marshaller = standardPropByQName.get(qname);
		if (marshaller != null) {
			return marshaller;
		}

		if (XCalNamespaceContext.XCAL_NS.equals(qname.getNamespaceURI())) {
			return new RawPropertyMarshaller(qname.getLocalPart().toUpperCase());
		}

		return getPropertyMarshaller(Xml.class);
	}

	/**
	 * Registers a component marshaller.
	 * @param marshaller the marshaller to register
	 */
	public void register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
		experimentalCompByName.put(marshaller.getComponentName().toUpperCase(), marshaller);
		experimentalCompByClass.put(marshaller.getComponentClass(), marshaller);
	}

	/**
	 * Registers a property marshaller.
	 * @param marshaller the marshaller to register
	 */
	public void register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
		experimentalPropByName.put(marshaller.getPropertyName().toUpperCase(), marshaller);
		experimentalPropByClass.put(marshaller.getPropertyClass(), marshaller);
		experimentalPropByQName.put(marshaller.getQName(), marshaller);
	}

	/**
	 * Unregisters a component marshaller.
	 * @param marshaller the marshaller to unregister
	 */
	public void unregister(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
		experimentalCompByName.remove(marshaller.getComponentName().toUpperCase());
		experimentalCompByClass.remove(marshaller.getComponentClass());
	}

	/**
	 * Unregisters a property marshaller
	 * @param marshaller the marshaller to unregister
	 */
	public void unregister(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
		experimentalPropByName.remove(marshaller.getPropertyName().toUpperCase());
		experimentalPropByClass.remove(marshaller.getPropertyClass());
		experimentalPropByQName.remove(marshaller.getQName());
	}

	/**
	 * Convenience method for getting the marshaller of the root iCalendar
	 * component ("VCALENDAR").
	 * @return the marshaller
	 */
	public static ICalendarMarshaller getICalendarMarshaller() {
		return (ICalendarMarshaller) standardCompByClass.get(ICalendar.class);
	}

	private static void registerStandard(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
		standardCompByName.put(marshaller.getComponentName().toUpperCase(), marshaller);
		standardCompByClass.put(marshaller.getComponentClass(), marshaller);
	}

	private static void registerStandard(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
		standardPropByName.put(marshaller.getPropertyName().toUpperCase(), marshaller);
		standardPropByClass.put(marshaller.getPropertyClass(), marshaller);
		standardPropByQName.put(marshaller.getQName(), marshaller);
	}
}
