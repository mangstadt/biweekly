package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static biweekly.util.IOUtils.utf8Writer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import biweekly.ICalDataType;
import biweekly.ICalendar;
import biweekly.Messages;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.component.marshaller.ICalendarMarshaller;
import biweekly.io.CannotParseException;
import biweekly.io.ICalMarshallerRegistrar;
import biweekly.io.SkipMeException;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
import biweekly.property.Xml;
import biweekly.property.marshaller.ICalPropertyMarshaller;
import biweekly.property.marshaller.ICalPropertyMarshaller.Result;
import biweekly.util.IOUtils;
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
 */

//@formatter:off
/**
 * <p>
 * Represents an XML document that contains iCalendar objects ("xCal" standard).
 * This class can be used to read and write xCal documents.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre class="brush:java">
 * String xml =
 * "&lt;?xml version=\"1.0\" encoding=\"utf-8\" ?>" +
 * "&lt;icalendar xmlns=\"urn:ietf:params:xml:ns:icalendar-2.0\"&gt;" +
 *   "&lt;vcalendar&gt;" +
 *     "&lt;properties&gt;" +
 *       "&lt;prodid&gt;&lt;text&gt;-//Example Inc.//Example Client//EN&lt;/text&gt;&lt;/prodid&gt;" +
 *       "&lt;version&gt;&lt;text&gt;2.0&lt;/text&gt;&lt;/version&gt;" +
 *     "&lt;/properties&gt;" +
 *     "&lt;components&gt;" +
 *       "&lt;vevent&gt;" +
 *         "&lt;properties&gt;" +
 *           "&lt;dtstart&gt;&lt;date-time&gt;2013-06-27T13:00:00Z&lt;/date-time&gt;&lt;/dtstart&gt;" +
 *           "&lt;dtend&gt;&lt;date-time&gt;2013-06-27T15:00:00Z&lt;/date-time&gt;&lt;/dtend&gt;" +
 *           "&lt;summary&gt;&lt;text&gt;Team Meeting&lt;/text&gt;&lt;/summary&gt;" +
 *         "&lt;/properties&gt;" +
 *       "&lt;/vevent&gt;" +
 *     "&lt;/components&gt;" +
 *   "&lt;/vcalendar&gt;" +
 * "&lt;/icalendar&gt;";
 *     
 * //parsing an existing xCal document
 * XCalDocument xcal = new XCalDocument(xml);
 * List&lt;ICalendar&gt; icals = xcal.parseAll();
 * 
 * //creating an empty xCal document
 * XCalDocument xcal = new XCalDocument();
 * 
 * //ICalendar objects can be added at any time
 * ICalendar ical = new ICalendar();
 * xcal.add(ical);
 * 
 * //retrieving the raw XML DOM
 * Document document = xcal.getDocument();
 * 
 * //call one of the "write()" methods to output the xCal document
 * File file = new File("meeting.xml");
 * xcal.write(file);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @rfc 6321
 */
//@formatter:on
public class XCalDocument {
	private static final ICalendarMarshaller icalMarshaller = ICalMarshallerRegistrar.getICalendarMarshaller();
	private static final XCalNamespaceContext nsContext = new XCalNamespaceContext("xcal");

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

	private ICalMarshallerRegistrar registrar = new ICalMarshallerRegistrar();
	private final List<List<String>> parseWarnings = new ArrayList<List<String>>();
	private Document document;
	private Element root;

	/**
	 * Parses an xCal document from a string.
	 * @param xml the xCal document in the form of a string
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCalDocument(String xml) throws SAXException {
		this(XmlUtils.toDocument(xml));
	}

	/**
	 * Parses an xCal document from an input stream.
	 * @param in the input stream to read the the xCal document from
	 * @throws IOException if there's a problem reading from the input stream
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCalDocument(InputStream in) throws SAXException, IOException {
		this(XmlUtils.toDocument(in));
	}

	/**
	 * Parses an xCal document from a file.
	 * @param file the file containing the xCal document
	 * @throws IOException if there's a problem reading from the file
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCalDocument(File file) throws SAXException, IOException {
		InputStream in = new FileInputStream(file);
		try {
			init(XmlUtils.toDocument(in));
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * <p>
	 * Parses an xCal document from a reader.
	 * </p>
	 * <p>
	 * Note that use of this constructor is discouraged. It ignores the
	 * character encoding that is defined within the XML document itself, and
	 * should only be used if the encoding is undefined or if the encoding needs
	 * to be ignored for whatever reason. The {@link #XCalDocument(InputStream)}
	 * constructor should be used instead, since it takes the XML document's
	 * character encoding into account when parsing.
	 * </p>
	 * @param reader the reader to read the xCal document from
	 * @throws IOException if there's a problem reading from the reader
	 * @throws SAXException if there's a problem parsing the XML
	 */
	public XCalDocument(Reader reader) throws SAXException, IOException {
		this(XmlUtils.toDocument(reader));
	}

	/**
	 * Wraps an existing XML DOM object.
	 * @param document the XML DOM that contains the xCal document
	 */
	public XCalDocument(Document document) {
		init(document);
	}

	/**
	 * Creates an empty xCal document.
	 */
	public XCalDocument() {
		document = XmlUtils.createDocument();
		root = document.createElementNS(XCAL_NS, "icalendar");
		document.appendChild(root);
	}

	private void init(Document document) {
		this.document = document;

		XPath xpath = XPathFactory.newInstance().newXPath();
		xpath.setNamespaceContext(nsContext);

		try {
			//find the <icalendar> element
			String prefix = nsContext.getPrefix();
			root = (Element) xpath.evaluate("//" + prefix + ":icalendar", document, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			//never thrown, xpath expression is hard coded
		}
	}

	/**
	 * <p>
	 * Registers an experimental property marshaller. Can also be used to
	 * override the marshaller of a standard property (such as DTSTART). Calling
	 * this method is the same as calling:
	 * </p>
	 * <p>
	 * {@code getRegistrar().register(marshaller)}.
	 * </p>
	 * @param marshaller the marshaller to register
	 */
	public void registerMarshaller(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
		registrar.register(marshaller);
	}

	/**
	 * <p>
	 * Registers an experimental component marshaller. Can also be used to
	 * override the marshaller of a standard component (such as VEVENT). Calling
	 * this method is the same as calling:
	 * </p>
	 * <p>
	 * {@code getRegistrar().register(marshaller)}.
	 * </p>
	 * @param marshaller the marshaller to register
	 */
	public void registerMarshaller(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
		registrar.register(marshaller);
	}

	/**
	 * Gets the object that manages the component/property marshaller objects.
	 * @return the marshaller registrar
	 */
	public ICalMarshallerRegistrar getRegistrar() {
		return registrar;
	}

	/**
	 * Sets the object that manages the component/property marshaller objects.
	 * @param registrar the marshaller registrar
	 */
	public void setRegistrar(ICalMarshallerRegistrar registrar) {
		this.registrar = registrar;
	}

	/**
	 * Registers the data type of an experimental parameter. Experimental
	 * parameters use the "unknown" xCal data type by default.
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
	 * Gets the raw XML DOM object.
	 * @return the XML DOM
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Gets the warnings from the last parse operation.
	 * @return the warnings (it is a "list of lists"--each parsed
	 * {@link ICalendar} object has its own warnings list)
	 * @see #parseAll
	 * @see #parseFirst
	 */
	public List<List<String>> getParseWarnings() {
		return parseWarnings;
	}

	/**
	 * Parses all the {@link ICalendar} objects from the xCal document.
	 * @return the iCalendar objects
	 */
	public List<ICalendar> parseAll() {
		parseWarnings.clear();

		if (root == null) {
			return Collections.emptyList();
		}

		List<ICalendar> icals = new ArrayList<ICalendar>();
		for (Element vcalendarElement : getVCalendarElements()) {
			List<String> warnings = new ArrayList<String>();
			ICalendar ical = parseICal(vcalendarElement, warnings);
			icals.add(ical);
			this.parseWarnings.add(warnings);
		}

		return icals;
	}

	/**
	 * Parses the first {@link ICalendar} object from the xCal document.
	 * @return the iCalendar object or null if there are none
	 */
	public ICalendar parseFirst() {
		parseWarnings.clear();

		if (root == null) {
			return null;
		}

		List<String> warnings = new ArrayList<String>();
		parseWarnings.add(warnings);

		List<Element> vcalendarElements = getVCalendarElements();
		if (vcalendarElements.isEmpty()) {
			return null;
		}
		return parseICal(vcalendarElements.get(0), warnings);
	}

	/**
	 * Adds an iCalendar object to the xCal document. This marshals the
	 * {@link ICalendar} object to the XML DOM. This means that any changes that
	 * are made to the {@link ICalendar} object after calling this method will
	 * NOT be applied to the xCal document.
	 * @param ical the iCalendar object to add
	 * @throws IllegalArgumentException if the marshaller class for a component
	 * or property object cannot be found (only happens when an experimental
	 * property/component marshaller is not registered with the
	 * {@code registerMarshaller} method.)
	 */
	public void add(ICalendar ical) {
		Element element = buildComponentElement(ical);
		if (root == null) {
			root = document.createElementNS(XCAL_NS, "icalendar");
			document.appendChild(root);
		}
		root.appendChild(element);
	}

	/**
	 * Writes the xCal document to a string without pretty-printing it.
	 * @return the XML string
	 */
	public String write() {
		return write(-1);
	}

	/**
	 * Writes the xCal document to a string and pretty-prints it.
	 * @param indent the number of indent spaces to use for pretty-printing
	 * @return the XML string
	 */
	public String write(int indent) {
		StringWriter sw = new StringWriter();
		try {
			write(sw, indent);
		} catch (TransformerException e) {
			//writing to string
		}
		return sw.toString();
	}

	/**
	 * Writes the xCal document to an output stream without pretty-printing it.
	 * @param out the output stream
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out) throws TransformerException {
		write(out, -1);
	}

	/**
	 * Writes the xCal document to an output stream and pretty-prints it.
	 * @param out the output stream
	 * @param indent the number of indent spaces to use for pretty-printing
	 * @throws TransformerException if there's a problem writing to the output
	 * stream
	 */
	public void write(OutputStream out, int indent) throws TransformerException {
		write(utf8Writer(out), indent);
	}

	/**
	 * Writes the xCal document to a file without pretty-printing it.
	 * @param file the file
	 * @throws IOException if there's a problem writing to the file
	 * @throws TransformerException if there's a problem writing the XML
	 */
	public void write(File file) throws TransformerException, IOException {
		write(file, -1);
	}

	/**
	 * Writes the xCal document to a file and pretty-prints it.
	 * @param file the file stream
	 * @param indent the number of indent spaces to use for pretty-printing
	 * @throws IOException if there's a problem writing to the file
	 * @throws TransformerException if there's a problem writing the XML
	 */
	public void write(File file, int indent) throws TransformerException, IOException {
		Writer writer = utf8Writer(file);
		try {
			write(writer, indent);
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	/**
	 * Writes the xCal document to a writer without pretty-printing it.
	 * @param writer the writer
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer) throws TransformerException {
		write(writer, -1);
	}

	/**
	 * Writes the xCal document to a writer and pretty-prints it.
	 * @param writer the writer
	 * @param indent the number of indent spaces to use for pretty-printing
	 * @throws TransformerException if there's a problem writing to the writer
	 */
	public void write(Writer writer, int indent) throws TransformerException {
		Map<String, String> properties = new HashMap<String, String>();
		if (indent >= 0) {
			properties.put(OutputKeys.INDENT, "yes");
			properties.put("{http://xml.apache.org/xslt}indent-amount", indent + "");
		}
		XmlUtils.toWriter(document, writer, properties);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Element buildComponentElement(ICalComponent component) {
		ICalComponentMarshaller m = registrar.getComponentMarshaller(component);
		if (m == null) {
			throw new IllegalArgumentException("No marshaller found for component class \"" + component.getClass().getName() + "\".");
		}

		Element componentElement = buildElement(m.getComponentName().toLowerCase());

		Element propertiesWrapperElement = buildElement("properties");
		for (Object obj : m.getProperties(component)) {
			ICalProperty property = (ICalProperty) obj;

			//create property element
			Element propertyElement = buildPropertyElement(property);
			if (propertyElement != null) {
				propertiesWrapperElement.appendChild(propertyElement);
			}
		}
		if (propertiesWrapperElement.hasChildNodes()) {
			componentElement.appendChild(propertiesWrapperElement);
		}

		Element componentsWrapperElement = buildElement("components");
		for (Object obj : m.getComponents(component)) {
			ICalComponent subComponent = (ICalComponent) obj;
			Element subComponentElement = buildComponentElement(subComponent);
			if (subComponentElement != null) {
				componentsWrapperElement.appendChild(subComponentElement);
			}
		}
		if (componentsWrapperElement.hasChildNodes()) {
			componentElement.appendChild(componentsWrapperElement);
		}

		return componentElement;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Element buildPropertyElement(ICalProperty property) {
		Element propertyElement;
		ICalParameters parameters;

		if (property instanceof Xml) {
			Xml xml = (Xml) property;

			Document value = xml.getValue();
			if (value == null) {
				return null;
			}

			//import the XML element into the xCal DOM
			propertyElement = XmlUtils.getRootElement(value);
			propertyElement = (Element) document.importNode(propertyElement, true);

			//get parameters
			parameters = property.getParameters();
		} else {
			ICalPropertyMarshaller pm = registrar.getPropertyMarshaller(property);
			if (pm == null) {
				throw new IllegalArgumentException("No marshaller found for property class \"" + property.getClass().getName() + "\".");
			}

			propertyElement = buildElement(pm.getQName());

			//marshal value
			try {
				pm.writeXml(property, propertyElement);
			} catch (SkipMeException e) {
				return null;
			}

			//get parameters
			parameters = pm.prepareParameters(property);
		}

		//build parameters
		Element parametersWrapperElement = buildParametersElement(parameters);
		if (parametersWrapperElement.hasChildNodes()) {
			propertyElement.insertBefore(parametersWrapperElement, propertyElement.getFirstChild());
		}

		return propertyElement;
	}

	private Element buildParametersElement(ICalParameters parameters) {
		Element parametersWrapperElement = buildElement("parameters");

		for (Map.Entry<String, List<String>> parameter : parameters) {
			String name = parameter.getKey().toLowerCase();
			ICalDataType dataType = parameterDataTypes.get(name);
			String dataTypeStr = (dataType == null) ? "unknown" : dataType.getName().toLowerCase();

			Element parameterElement = buildAndAppendElement(name, parametersWrapperElement);
			for (String parameterValue : parameter.getValue()) {
				Element parameterValueElement = buildAndAppendElement(dataTypeStr, parameterElement);
				parameterValueElement.setTextContent(parameterValue);
			}
		}

		return parametersWrapperElement;
	}

	private ICalendar parseICal(Element icalElement, List<String> warnings) {
		ICalComponent root = parseComponent(icalElement, warnings);

		ICalendar ical;
		if (root instanceof ICalendar) {
			ical = (ICalendar) root;
		} else {
			//shouldn't happen, since only <vcalendar> elements are passed into this method
			ical = icalMarshaller.emptyInstance();
			ical.addComponent(root);
		}
		return ical;
	}

	private ICalComponent parseComponent(Element componentElement, List<String> warnings) {
		//create the component object
		ICalComponentMarshaller<? extends ICalComponent> m = registrar.getComponentMarshaller(componentElement.getLocalName());
		ICalComponent component = m.emptyInstance();

		//parse properties
		for (Element propertyWrapperElement : getChildElements(componentElement, "properties")) { //there should be only one <properties> element, but parse them all incase there are more
			for (Element propertyElement : XmlUtils.toElementList(propertyWrapperElement.getChildNodes())) {
				ICalProperty property = parseProperty(propertyElement, warnings);
				if (property != null) {
					component.addProperty(property);
				}
			}
		}

		//parse sub-components
		for (Element componentWrapperElement : getChildElements(componentElement, "components")) { //there should be only one <components> element, but parse them all incase there are more
			for (Element subComponentElement : XmlUtils.toElementList(componentWrapperElement.getChildNodes())) {
				if (!XCAL_NS.equals(subComponentElement.getNamespaceURI())) {
					continue;
				}

				ICalComponent subComponent = parseComponent(subComponentElement, warnings);
				component.addComponent(subComponent);
			}
		}

		return component;
	}

	private ICalProperty parseProperty(Element propertyElement, List<String> warnings) {
		ICalParameters parameters = parseParameters(propertyElement);
		String propertyName = propertyElement.getLocalName();
		QName qname = new QName(propertyElement.getNamespaceURI(), propertyName);

		ICalPropertyMarshaller<? extends ICalProperty> m = registrar.getPropertyMarshaller(qname);

		ICalProperty property = null;
		try {
			Result<? extends ICalProperty> result = m.parseXml(propertyElement, parameters);

			for (Warning warning : result.getWarnings()) {
				addWarning(propertyName, warnings, warning);
			}

			property = result.getProperty();
		} catch (SkipMeException e) {
			addWarning(propertyName, warnings, Warning.parse(0, e.getMessage()));
			return null;
		} catch (CannotParseException e) {
			addWarning(propertyName, warnings, Warning.parse(16, e.getMessage()));
		}

		//unmarshal as an XML property
		if (property == null) {
			m = registrar.getPropertyMarshaller(Xml.class);

			Result<? extends ICalProperty> result = m.parseXml(propertyElement, parameters);

			for (Warning warning : result.getWarnings()) {
				addWarning(propertyName, warnings, warning);
			}

			property = result.getProperty();
		}

		return property;
	}

	private ICalParameters parseParameters(Element propertyElement) {
		ICalParameters parameters = new ICalParameters();

		for (Element parametersElement : getChildElements(propertyElement, "parameters")) { //there should be only one <parameters> element, but parse them all incase there are more
			List<Element> paramElements = XmlUtils.toElementList(parametersElement.getChildNodes());
			for (Element paramElement : paramElements) {
				String name = paramElement.getLocalName().toUpperCase();
				List<Element> valueElements = XmlUtils.toElementList(paramElement.getChildNodes());
				if (valueElements.isEmpty()) { //this should never be true if the xCal follows the specs
					String value = paramElement.getTextContent();
					parameters.put(name, value);
				} else {
					for (Element valueElement : valueElements) {
						String value = valueElement.getTextContent();
						parameters.put(name, value);
					}
				}
			}
		}

		return parameters;
	}

	private Element buildElement(String localName) {
		return buildElement(new QName(XCAL_NS, localName));
	}

	private Element buildElement(QName qname) {
		return document.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
	}

	private Element buildAndAppendElement(String localName, Element parent) {
		return buildAndAppendElement(new QName(XCAL_NS, localName), parent);
	}

	private Element buildAndAppendElement(QName qname, Element parent) {
		Element child = document.createElementNS(qname.getNamespaceURI(), qname.getLocalPart());
		parent.appendChild(child);
		return child;
	}

	private List<Element> getVCalendarElements() {
		return getChildElements(root, "vcalendar");
	}

	private List<Element> getChildElements(Element parent, String localName) {
		List<Element> elements = new ArrayList<Element>();
		for (Element child : XmlUtils.toElementList(parent.getChildNodes())) {
			if (localName.equals(child.getLocalName()) && XCAL_NS.equals(child.getNamespaceURI())) {
				elements.add(child);
			}
		}
		return elements;
	}

	private void addWarning(String propertyName, List<String> warnings, Warning warning) {
		String message = Messages.INSTANCE.getMessage("parse.xml", propertyName, warning);
		warnings.add(message);
	}

	@Override
	public String toString() {
		return write(2);
	}
}
