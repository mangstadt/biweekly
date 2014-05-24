package biweekly.io.xml;

import static biweekly.io.xml.XCalNamespaceContext.XCAL_NS;
import static biweekly.io.xml.XCalQNames.COMPONENTS;
import static biweekly.io.xml.XCalQNames.ICALENDAR;
import static biweekly.io.xml.XCalQNames.PARAMETERS;
import static biweekly.io.xml.XCalQNames.PROPERTIES;
import static biweekly.io.xml.XCalQNames.VCALENDAR;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;

import javax.xml.namespace.QName;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import biweekly.ICalendar;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.io.CannotParseException;
import biweekly.io.ParseWarnings;
import biweekly.io.SkipMeException;
import biweekly.io.scribe.ScribeIndex;
import biweekly.io.scribe.component.ICalComponentScribe;
import biweekly.io.scribe.property.ICalPropertyScribe;
import biweekly.io.scribe.property.ICalPropertyScribe.Result;
import biweekly.parameter.ICalParameters;
import biweekly.property.ICalProperty;
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
 * Reads xCals (XML-encoded vCards) in a streaming fashion.
 * </p>
 * <p>
 * <b>Example:</b>
 * 
 * <pre class="brush:java">
 * File file = new File("xcals.xml");
 * final List&lt;ICAlendar&gt; icals = new ArrayList&lt;ICalendar&gt;();
 * XCalReader xcalReader = new XCalReader(file);
 * xcalReader.read(new XCalistener(){
 *   public void icalRead(ICalendar ical, List&lt;String&gt; warnings) throws StopReadingException{
 *     icals.add(ical);
 *     //throw a "StopReadingException" to stop parsing early
 *   }
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc6321">RFC 6321</a>
 */
public class XCalReader implements Closeable {
	private final Source source;
	private final Closeable stream;
	private final ParseWarnings warnings = new ParseWarnings();
	private ScribeIndex index = new ScribeIndex();
	private XCalListener listener;

	/**
	 * Creates an xCal reader.
	 * @param str the string to read the xCals from
	 */
	public XCalReader(String str) {
		this(new StringReader(str));
	}

	/**
	 * Creates an xCal reader.
	 * @param in the input stream to read the xCals from
	 */
	public XCalReader(InputStream in) {
		source = new StreamSource(in);
		stream = in;
	}

	/**
	 * Creates an xCal reader.
	 * @param file the file to read the xCals from
	 * @throws FileNotFoundException if the file doesn't exist
	 */
	public XCalReader(File file) throws FileNotFoundException {
		this(new FileInputStream(file));
	}

	/**
	 * Creates an xCal reader.
	 * @param reader the reader to read from
	 */
	public XCalReader(Reader reader) {
		source = new StreamSource(reader);
		stream = reader;
	}

	/**
	 * Creates an xCal reader.
	 * @param node the DOM node to read from
	 */
	public XCalReader(Node node) {
		source = new DOMSource(node);
		stream = null;
	}

	/**
	 * <p>
	 * Registers a property scribe. This is the same as calling:
	 * </p>
	 * <p>
	 * {@code getScribeIndex().register(scribe)}
	 * </p>
	 * @param scribe the scribe to register
	 */
	public void registerScribe(ICalPropertyScribe<? extends ICalProperty> scribe) {
		index.register(scribe);
	}

	/**
	 * Gets the scribe index.
	 * @return the scribe index
	 */
	public ScribeIndex getScribeIndex() {
		return index;
	}

	/**
	 * Sets the scribe index.
	 * @param index the scribe index
	 */
	public void setScribeIndex(ScribeIndex index) {
		this.index = index;
	}

	/**
	 * Starts parsing the XML document. This method blocks until the entire
	 * input stream or DOM is consumed, or until a {@link StopReadingException}
	 * is thrown from the given {@link XCalListener}.
	 * @param listener used for retrieving the parsed vCards
	 * @throws TransformerException if there's a problem reading from the input
	 * stream or a problem parsing the XML
	 */
	public void read(XCalListener listener) throws TransformerException {
		this.listener = listener;

		//create the transformer
		Transformer transformer;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			//no complex configurations
			throw new RuntimeException(e);
		} catch (TransformerFactoryConfigurationError e) {
			//no complex configurations
			throw new RuntimeException(e);
		}

		//prevent error messages from being printed to stderr
		transformer.setErrorListener(new ErrorListener() {
			public void error(TransformerException e) {
				//empty
			}

			public void fatalError(TransformerException e) {
				//empty
			}

			public void warning(TransformerException e) {
				//empty
			}
		});

		//start parsing
		ContentHandlerImpl handler = new ContentHandlerImpl();
		SAXResult result = new SAXResult(handler);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof StopReadingException) {
				//ignore StopReadingException because it signals that the user canceled the parsing operation
			} else {
				throw e;
			}
		}
	}

	private class ContentHandlerImpl extends DefaultHandler {
		private final Document DOC = XmlUtils.createDocument();

		private boolean inICalendar, inProperties, inParameters;
		private String paramName, paramDataType;
		private StringBuilder characterBuffer = new StringBuilder();
		private int dupNested = 0;
		private Element propertyElement, parent;
		private LinkedList<ICalComponent> componentStack = new LinkedList<ICalComponent>();
		private LinkedList<QName> componentQNamesStack = new LinkedList<QName>();

		private ICalendar ical;
		private ICalComponent curComponent;
		private QName componentQName;
		private ICalParameters parameters;

		@Override
		public void characters(char[] buffer, int start, int length) throws SAXException {
			characterBuffer.append(buffer, start, length);
		}

		@Override
		public void startElement(String namespace, String localName, String qName, Attributes attributes) throws SAXException {
			QName qname = new QName(namespace, localName);
			String textContent = characterBuffer.toString();
			characterBuffer.setLength(0);

			if (!inICalendar) {
				if (ICALENDAR.equals(qname)) {
					inICalendar = true;
				}
				return;
			}

			if (ical == null) {
				if (VCALENDAR.equals(qname)) {
					ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(localName);
					ICalComponent component = scribe.emptyInstance();

					curComponent = component;
					componentQName = qname;
					ical = (ICalendar) component;
				}
				return;
			}

			if (!inProperties) {
				if (PROPERTIES.equals(qname)) {
					inProperties = true;
					return;
				}
				if (COMPONENTS.equals(qname)) {
					componentStack.add(curComponent);
					componentQNamesStack.add(componentQName);
					curComponent = null;
					componentQName = null;
					return;
				}
				if (curComponent == null && XCAL_NS.equals(namespace)) {
					ICalComponentScribe<? extends ICalComponent> scribe = index.getComponentScribe(localName);
					curComponent = scribe.emptyInstance();
					componentQName = qname;

					ICalComponent parent = componentStack.getLast();
					parent.addComponent(curComponent);
					return;
				}
				return;
			}

			//we're parsing a property
			if (propertyElement == null) {
				propertyElement = createElement(namespace, localName, attributes);
				parameters = new ICalParameters();
				parent = propertyElement;
				return;
			}

			if (!inParameters && PARAMETERS.equals(qname)) {
				inParameters = true;
				return;
			}

			if (inParameters) {
				if (paramName == null) {
					paramName = localName;
				} else if (paramDataType == null) {
					paramDataType = localName;
				}
				return;
			}

			if (textContent.length() > 0) {
				parent.appendChild(DOC.createTextNode(textContent));
			}

			if (propertyElement.getNamespaceURI().equals(namespace) && propertyElement.getLocalName().equals(localName)) {
				//incase a child element of the property element has the same qname as the property element
				//e.g. a <duration> property can have a <duration> data value.
				dupNested++;
			}
			Element element = createElement(namespace, localName, attributes);
			parent.appendChild(element);
			parent = element;
		}

		@Override
		public void endElement(String namespace, String localName, String qName) throws SAXException {
			QName qname = new QName(namespace, localName);
			String textContent = characterBuffer.toString();
			characterBuffer.setLength(0);

			if (paramDataType != null && localName.equals(paramDataType)) {
				parameters.put(paramName, textContent);
				paramDataType = null;
				return;
			}

			if (paramName != null && localName.equals(paramName)) {
				paramName = null;
				return;
			}

			if (inParameters) {
				if (PARAMETERS.equals(qname)) {
					inParameters = false;
				}
				return;
			}

			if (propertyElement != null && namespace.equals(propertyElement.getNamespaceURI()) && localName.equals(propertyElement.getLocalName())) {
				if (dupNested > 0) {
					dupNested--;
				} else {
					propertyElement.appendChild(DOC.createTextNode(textContent));

					String propertyName = localName;
					ICalPropertyScribe<? extends ICalProperty> scribe = index.getPropertyScribe(qname);
					try {
						Result<? extends ICalProperty> result = scribe.parseXml(propertyElement, parameters);
						ICalProperty property = result.getProperty();

						curComponent.addProperty(property);
						for (Warning warning : result.getWarnings()) {
							warnings.add(null, propertyName, warning);
						}
					} catch (SkipMeException e) {
						warnings.add(null, propertyName, 22, e.getMessage());
					} catch (CannotParseException e) {
						String xml = XmlUtils.toString(propertyElement);
						warnings.add(null, propertyName, 33, xml, e.getMessage());

						scribe = index.getPropertyScribe(Xml.class);
						Result<? extends ICalProperty> result = scribe.parseXml(propertyElement, parameters);
						ICalProperty property = result.getProperty();
						curComponent.addProperty(property);
					}

					propertyElement = null;
					return;
				}
			}

			if (curComponent != null && componentQName.equals(qname)) {
				curComponent = null;
				componentQName = null;

				if (VCALENDAR.equals(qname)) {
					listener.icalRead(ical, warnings.copy());
					warnings.clear();
					ical = null;
				}
				return;
			}

			if (inProperties && PROPERTIES.equals(qname)) {
				inProperties = false;
				return;
			}

			if (curComponent == null && COMPONENTS.equals(qname)) {
				curComponent = componentStack.removeLast();
				componentQName = componentQNamesStack.removeLast();
				return;
			}

			if (inICalendar && ICALENDAR.equals(qname)) {
				inICalendar = false;
				return;
			}

			if (parent == null) {
				return;
			}

			if (textContent.length() > 0) {
				parent.appendChild(DOC.createTextNode(textContent));
			}
			parent = (Element) parent.getParentNode();
		}

		private Element createElement(String namespace, String localName, Attributes attributes) {
			Element element = DOC.createElementNS(namespace, localName);
			for (int i = 0; i < attributes.getLength(); i++) {
				String qname = attributes.getQName(i);
				if (qname.startsWith("xmlns:")) {
					continue;
				}

				String name = attributes.getLocalName(i);
				String value = attributes.getValue(i);
				element.setAttribute(name, value);
			}
			return element;
		}
	}

	/**
	 * Closes the underlying input stream.
	 */
	public void close() throws IOException {
		if (stream != null) {
			stream.close();
		}
	}
}
