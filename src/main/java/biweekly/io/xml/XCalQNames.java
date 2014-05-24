package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;

import javax.xml.namespace.QName;

/**
 * Contains the XML element names of some of the standard xCard elements.
 * @author Michael Angstadt
 */
public interface XCalQNames {
	public static final QName ICALENDAR = new QName(XCAL_NS, "icalendar");
	public static final QName VCALENDAR = new QName(XCAL_NS, "vcalendar");
	public static final QName COMPONENTS = new QName(XCAL_NS, "components");
	public static final QName PROPERTIES = new QName(XCAL_NS, "properties");
	public static final QName PARAMETERS = new QName(XCAL_NS, "parameters");
}
