package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static biweekly.io.xml.XCalQNames.COMPONENTS;
import static biweekly.io.xml.XCalQNames.ICALENDAR;
import static biweekly.io.xml.XCalQNames.PARAMETERS;
import static biweekly.io.xml.XCalQNames.PROPERTIES;
import static biweekly.util.IOUtils.utf8Writer;
import static biweekly.util.StringUtils.NEWLINE;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.io.SkipMeException;
import biweekly.io.TimezoneInfo;
import biweekly.io.WriteContext;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.Version;
import biweekly.property.Xml;
import biweekly.util.XmlUtils;

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

 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the FreeBSD Project.
 */

/**
 * <p>
 * Writes xCards (XML-encoded vCards) in a streaming fashion.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * VCard vcard1 = ...
 * VCard vcard2 = ...
 * 
 * File file = new File("vcards.xml");
 * XCardWriter xcardWriter = new XCardWriter(file);
 * xcardWriter.write(vcard1);
 * xcardWriter.write(vcard2);
 * xcardWriter.close();
 * </pre>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6351">RFC 6351</a>
 */
public class XCalWriter implements Closeable {
	//how to use SAX to write XML: http://stackoverflow.com/questions/4898590/generating-xml-using-sax-and-java
	private final Document DOC = XmlUtils.createDocument();

	/**
	 * Defines the names of the XML elements that are used to hold each
	 * parameter's value.
	 */
	private final Map<String, ICalDataType> parameterDataTypes = new HashMap<String, ICalDataType>();
	{
		registerParameterDataType(ICalParameters.CN, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.ALTREP, ICalDataType.URI);
		registerParameterDataType(ICalParameters.CUTYPE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.DELEGATED_FROM, ICalDataType.CAL_ADDRESS);
		registerParameterDataType(ICalParameters.DELEGATED_TO, ICalDataType.CAL_ADDRESS);
		registerParameterDataType(ICalParameters.DIR, ICalDataType.URI);
		registerParameterDataType(ICalParameters.ENCODING, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.FMTTYPE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.FBTYPE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.LANGUAGE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.MEMBER, ICalDataType.CAL_ADDRESS);
		registerParameterDataType(ICalParameters.PARTSTAT, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.RANGE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.RELATED, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.RELTYPE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.ROLE, ICalDataType.TEXT);
		registerParameterDataType(ICalParameters.RSVP, ICalDataType.BOOLEAN);
		registerParameterDataType(ICalParameters.SENT_BY, ICalDataType.CAL_ADDRESS);
		registerParameterDataType(ICalParameters.TZID, ICalDataType.TEXT);
	}

	private final Writer writer;
	private final ICalVersion targetVersion = ICalVersion.V2_0;
	private final TransformerHandler handler;
	private final String indent;
	private final boolean icalendarElementExists;
	private int level = 0;
	private boolean textNodeJustPrinted = false, started = false;
	private ScribeIndex index = new ScribeIndex();

	private WriteContext context;
	private TimezoneInfo tzinfo = new TimezoneInfo();

	/**
	 * Creates an xCard writer (UTF-8 encoding will be used).
	 * @param out the output stream to write the xCards to
	 */
	public XCalWriter(OutputStream out) {
		this(utf8Writer(out));
	}

	/**
	 * Creates an xCard writer (UTF-8 encoding will be used).
	 * @param out the output stream to write the xCards to
	 * @param indent the indentation string to use for pretty printing (e.g.
	 * "\t") or null not to pretty print
	 */
	public XCalWriter(OutputStream out, String indent) {
		this(utf8Writer(out), indent);
	}

	/**
	 * Creates an xCard writer (UTF-8 encoding will be used).
	 * @param file the file to write the xCards to
	 * @throws IOException if there's a problem opening the file
	 */
	public XCalWriter(File file) throws IOException {
		this(utf8Writer(file));
	}

	/**
	 * Creates an xCard writer (UTF-8 encoding will be used).
	 * @param file the file to write the xCards to
	 * @param indent the indentation string to use for pretty printing (e.g.
	 * "\t") or null not to pretty print
	 * @throws IOException if there's a problem opening the file
	 */
	public XCalWriter(File file, String indent) throws IOException {
		this(utf8Writer(file), indent);
	}

	/**
	 * Creates an xCard writer.
	 * @param writer the writer to write to
	 */
	public XCalWriter(Writer writer) {
		this(writer, null);
	}

	/**
	 * Creates an xCard writer.
	 * @param writer the writer to write to
	 * @param indent the indentation string to use for pretty printing (e.g.
	 * "\t") or null not to pretty print
	 */
	public XCalWriter(Writer writer, String indent) {
		this(writer, indent, null);
	}

	/**
	 * Creates an xCard writer.
	 * @param parent the DOM node to add child elements to
	 */
	public XCalWriter(Node parent) {
		this(null, null, parent);
	}

	private XCalWriter(Writer writer, String indent, Node parent) {
		this.writer = writer;
		this.indent = indent;

		if (parent instanceof Document) {
			Node root = parent.getFirstChild();
			if (root != null) {
				parent = root;
			}
		}
		this.icalendarElementExists = isICalendarElement(parent);

		try {
			SAXTransformerFactory factory = (SAXTransformerFactory) TransformerFactory.newInstance();
			handler = factory.newTransformerHandler();
		} catch (TransformerConfigurationException e) {
			throw new RuntimeException(e);
		}

		Result result = (writer == null) ? new DOMResult(parent) : new StreamResult(writer);
		handler.setResult(result);
	}

	private boolean isICalendarElement(Node node) {
		if (node == null) {
			return false;
		}

		if (!(node instanceof Element)) {
			return false;
		}

		return ICALENDAR.getNamespaceURI().equals(node.getNamespaceURI()) && ICALENDAR.getLocalPart().equals(node.getLocalName());
	}

	/**
	 * <p>
	 * Registers an experimental property scribe. Can also be used to override
	 * the scribe of a standard property (such as DTSTART). Calling this method
	 * is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}.
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(ICalPropertyScribe<? extends ICalProperty> scribe) {
		index.register(scribe);
	}

	/**
	 * <p>
	 * Registers an experimental component scribe. Can also be used to override
	 * the scribe of a standard component (such as VEVENT). Calling this method
	 * is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}.
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(ICalComponentScribe<? extends ICalComponent> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the object that manages the component/property scribes.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the object that manages the component/property scribes.
	 * @param scribe the scribe index
	 */
	public void setScribeIndex(ScribeIndex scribe) {
		this.index = scribe;
	}

	/**
	 * Registers the data type of an experimental parameter. Experimental
	 * parameters use the "unknown" data type by default.
	 * @param parameterName the parameter name (e.g. "x-foo")
	 * @param dataType the data type or null to remove
	 */
	public void registerParameterDataType(String parameterName, ICalDataType dataType) {
		parameterName = parameterName.toLowerCase();
		if (dataType == null) {
			parameterDataTypes.remove(parameterName);
		} else {
			parameterDataTypes.put(parameterName, dataType);
		}
	}

	/**
	 * Gets the timezone-related info for this writer.
	 * @return the timezone-related info
	 */
	public TimezoneInfo getTimezoneInfo() {
		return tzinfo;
	}

	/**
	 * Sets the timezone-related info for this writer.
	 * @param tzinfo the timezone-related info
	 */
	public void setTimezoneInfo(TimezoneInfo tzinfo) {
		this.tzinfo = tzinfo;
	}

	/**
	 * Writes an iCalendar object.
	 * @param ical the iCalendar object to write
	 * @throws SAXException if there's a problem writing the iCalendar object
	 * @throws IllegalArgumentException if the scribe index is missing scribes
	 * for one or more properties/components.
	 */
	public void write(ICalendar ical) throws SAXException {
		index.hasScribesFor(ical);
		context = new WriteContext(targetVersion, tzinfo);

		if (!started) {
			handler.startDocument();

			if (!icalendarElementExists) {
				//don't output a <icalendar> element if the parent is a <icalendar> element
				start(ICALENDAR);
				level++;
			}

			started = true;
		}

		write((ICalComponent) ical);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void write(ICalComponent component) throws SAXException {
		ICalComponentScribe scribe = index.getComponentScribe(component);
		String name = scribe.getComponentName().toLowerCase();

		start(name);
		level++;

		List properties = scribe.getProperties(component);
		if (component instanceof ICalendar && component.getProperty(Version.class) == null) {
			properties.add(0, new Version(targetVersion));
		}

		if (!properties.isEmpty()) {
			start(PROPERTIES);
			level++;

			for (Object propertyObj : properties) {
				context.setParent(component); //set parent here incase a scribe resets the parent
				ICalProperty property = (ICalProperty) propertyObj;
				write(property);
			}

			level--;
			end(PROPERTIES);
		}

		Collection subComponents = scribe.getComponents(component);
		if (component instanceof ICalendar) {
			//add the VTIMEZONE components that were auto-generated by TimezoneOptions
			Collection<VTimezone> tzs = tzinfo.getComponents();
			for (VTimezone tz : tzs) {
				if (!subComponents.contains(tz)) {
					subComponents.add(tz);
				}
			}
		}
		if (!subComponents.isEmpty()) {
			start(COMPONENTS);
			level++;

			for (Object subComponentObj : subComponents) {
				ICalComponent subComponent = (ICalComponent) subComponentObj;
				write(subComponent);
			}

			level--;
			end(COMPONENTS);
		}

		level--;
		end(name);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void write(ICalProperty property) throws SAXException {
		ICalPropertyScribe scribe = index.getPropertyScribe(property);
		ICalParameters parameters = scribe.prepareParameters(property, context);

		//get the property element to write
		Element propertyElement;
		if (property instanceof Xml) {
			Xml xml = (Xml) property;
			Document value = xml.getValue();
			if (value == null) {
				return;
			}
			propertyElement = XmlUtils.getRootElement(value);
		} else {
			QName qname = scribe.getQName();
			propertyElement = DOC.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
			try {
				scribe.writeXml(property, propertyElement, context);
			} catch (SkipMeException e) {
				return;
			}
		}

		start(propertyElement);
		level++;

		write(parameters);
		write(propertyElement);

		level--;
		end(propertyElement);
	}

	private void write(Element propertyElement) throws SAXException {
		NodeList children = propertyElement.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if (child instanceof Element) {
				Element element = (Element) child;

				if (element.hasChildNodes()) {
					start(element);
					level++;

					write(element);

					level--;
					end(element);
				} else {
					//make childless elements appear as "<foo />" instead of "<foo></foo>"
					childless(element);
				}

				continue;
			}

			if (child instanceof Text) {
				Text text = (Text) child;
				text(text.getTextContent());
				continue;
			}
		}
	}

	private void write(ICalParameters parameters) throws SAXException {
		if (parameters.isEmpty()) {
			return;
		}

		start(PARAMETERS);
		level++;

		for (Map.Entry<String, List<String>> parameter : parameters) {
			String parameterName = parameter.getKey().toLowerCase();
			start(parameterName);
			level++;

			for (String parameterValue : parameter.getValue()) {
				ICalDataType dataType = parameterDataTypes.get(parameterName);
				String dataTypeElementName = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();

				start(dataTypeElementName);
				text(parameterValue);
				end(dataTypeElementName);
			}

			level--;
			end(parameterName);
		}

		level--;
		end(PARAMETERS);
	}

	private void indent() throws SAXException {
		if (indent == null) {
			return;
		}

		StringBuilder sb = new StringBuilder(NEWLINE);
		for (int i = 0; i < level; i++) {
			sb.append(indent);
		}

		String str = sb.toString();
		handler.ignorableWhitespace(str.toCharArray(), 0, str.length());
	}

	private void childless(Element element) throws SAXException {
		Attributes attributes = getElementAttributes(element);
		indent();
		handler.startElement(element.getNamespaceURI(), "", element.getLocalName(), attributes);
		handler.endElement(element.getNamespaceURI(), "", element.getLocalName());
	}

	private void start(Element element) throws SAXException {
		Attributes attributes = getElementAttributes(element);
		start(element.getNamespaceURI(), element.getLocalName(), attributes);
	}

	private void start(String element) throws SAXException {
		start(element, null);
	}

	private void start(QName qname) throws SAXException {
		start(qname, null);
	}

	private void start(QName qname, Attributes attributes) throws SAXException {
		start(qname.getNamespaceURI(), qname.getLocalPart(), attributes);
	}

	private void start(String element, Attributes attributes) throws SAXException {
		start(XCAL_NS, element, attributes);
	}

	private void start(String namespace, String element, Attributes attributes) throws SAXException {
		indent();
		handler.startElement(namespace, "", element, attributes);
	}

	private void end(Element element) throws SAXException {
		end(element.getNamespaceURI(), element.getLocalName());
	}

	private void end(String element) throws SAXException {
		end(XCAL_NS, element);
	}

	private void end(QName qname) throws SAXException {
		end(qname.getNamespaceURI(), qname.getLocalPart());
	}

	private void end(String namespace, String element) throws SAXException {
		if (!textNodeJustPrinted) {
			indent();
		}

		handler.endElement(namespace, "", element);
		textNodeJustPrinted = false;
	}

	private void text(String text) throws SAXException {
		handler.characters(text.toCharArray(), 0, text.length());
		textNodeJustPrinted = true;
	}

	private Attributes getElementAttributes(Element element) {
		AttributesImpl attributes = new AttributesImpl();
		NamedNodeMap attributeNodes = element.getAttributes();
		for (int i = 0; i < attributeNodes.getLength(); i++) {
			Node node = attributeNodes.item(i);
			attributes.addAttribute(node.getNamespaceURI(), "", node.getLocalName(), "", node.getNodeValue());
		}
		return attributes;
	}

	/**
	 * Terminates the XML document and closes the output stream.
	 */
	public void close() throws IOException {
		try {
			if (!started) {
				handler.startDocument();

				if (!icalendarElementExists) {
					//don't output a <icalendar> element if the parent is a <icalendar> element
					start(ICALENDAR);
					level++;
				}
			}

			if (!icalendarElementExists) {
				level--;
				end(ICALENDAR);
			}
			handler.endDocument();
		} catch (SAXException e) {
			throw new IOException(e);
		}

		if (writer != null) {
			writer.close();
		}
	}
}
