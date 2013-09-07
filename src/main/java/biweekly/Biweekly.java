package biweekly;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.io.ICalMarshallerRegistrar;
import biweekly.io.json.JCalParseException;
import biweekly.io.json.JCalReader;
import biweekly.io.json.JCalWriter;
import biweekly.io.text.ICalRawReader;
import biweekly.io.text.ICalRawWriter;
import biweekly.io.text.ICalReader;
import biweekly.io.text.ICalWriter;
import biweekly.io.xml.XCalDocument;
import biweekly.property.ICalProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;
import biweekly.util.IOUtils;

import com.fasterxml.jackson.core.JsonParseException;

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
 * Contains static chaining factory methods for reading/writing iCalendar
 * objects.
 * </p>
 * 
 * <p>
 * <b>Writing an iCalendar object</b>
 * 
 * <pre class="brush:java">
 * ICalendar ical = new ICalendar();
 * 
 * //string
 * String icalString = Biweekly.write(ical).go();
 * 
 * //file
 * File file = new File("meeting.ics");
 * Biweekly.write(ical).go(file);
 * 
 * //output stream
 * OutputStream out = ...
 * Biweekly.write(ical).go(out);
 * out.close();
 * 
 * //writer (should be configured to use UTF-8 encoding)
 * Writer writer = ...
 * Biweekly.write(ical).go(writer);
 * writer.close();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Writing multiple iCalendar objects</b>
 * 
 * <pre class="brush:java">
 * ICalendar ical1 = new ICalendar();
 * ICalendar ical2 = new ICalendar();
 * 
 * String icalString = Biweekly.write(ical1, ical2).go();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Writing an XML-encoded iCalendar object (xCal)</b><br>
 * 
 * <pre class="brush:java">
 * //Call writeXml() instead of write()
 * ICalendar ical = new ICalendar();
 * String xml = Biweekly.writeXml(ical).indent(2).go();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Writing a JSON-encoded iCalendar object (jCal)</b><br>
 * 
 * <pre class="brush:java">
 * //Call writeJson() instead of write()
 * ICalendar ical = new ICalendar();
 * String json = Biweekly.writeJson(ical).go();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Reading an iCalendar object</b>
 * 
 * <pre class="brush:java">
 * ICalendar ical;
 * 
 * //string
 * String icalStr = ...
 * ical = Biweekly.parse(icalStr).first();
 * 
 * //file
 * File file = new File("meeting.ics");
 * ical = Biweekly.parse(file).first();
 * 
 * //input stream
 * InputStream in = ...
 * ical = Biweekly.parse(in).first();
 * in.close();  
 * 
 * //reader (should be configured to read UTF-8)
 * Reader reader = ...
 * ical = Biweekly.parse(reader).first();
 * reader.close();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Reading multiple iCalendar objects</b>
 * 
 * <pre class="brush:java">
 * String icalStr = ...
 * List&lt;ICalendar&gt; icals = Biweekly.parse(icalStr).all();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Reading an XML-encoded iCalendar object (xCal)</b><br>
 * 
 * <pre class="brush:java">
 * //Call parseXml() instead of parse()
 * String xml = ...
 * ICalendar ical = Biweekly.parseXml(xml).first();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Reading a JSON-encoded iCalendar object (Cal)</b><br>
 * 
 * <pre class="brush:java">
 * //Call parseJson() instead of parse()
 * String json = ...
 * ICalendar ical = Biweekly.parseJson(json).first();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Retrieving parser warnings</b>
 * 
 * <pre class="brush:java">
 * String icalStr = ...
 * List&lt;List&lt;String&gt;&gt; warnings = new ArrayList&lt;List&lt;String&gt;&gt;();
 * List&lt;ICalendar&gt; icals = Biweekly.parse(icalStr).warnings(warnings).all();
 * int i = 0;
 * for (List&lt;String&gt; icalWarnings : warnings){
 *   System.out.println("iCal #" + (i++) + " warnings:");
 *   for (String warning : icalWarnings){
 *     System.out.println(warning);
 *   }
 * }
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * The methods in this class make use of the following classes. These classes
 * can be used if greater control over the read/write operation is required:
 * </p>
 * 
 * <style> table.t td, table.t th {border:1px solid #000;} </style>
 * <table class="t" cellpadding="5" style="border-collapse:collapse;">
 * <tr>
 * <th></th>
 * <th>Classes</th>
 * <th>Supports<br>
 * streaming?</th>
 * </tr>
 * <tr>
 * <th>Text</th>
 * <td>{@link ICalReader} / {@link ICalWriter}</td>
 * <td>yes</td>
 * </tr>
 * <tr>
 * <th>XML</th>
 * <td>{@link XCalDocument}</td>
 * <td>no</td>
 * </tr>
 * <tr>
 * <th>JSON</th>
 * <td>{@link JCalReader} / {@link JCalWriter}</td>
 * <td>yes</td>
 * </tr>
 * </table>
 * @author Michael Angstadt
 */
public class Biweekly {
	/**
	 * The version of the library.
	 */
	public static final String VERSION;

	/**
	 * The project webpage.
	 */
	public static final String URL;

	static {
		InputStream in = null;
		try {
			in = Biweekly.class.getResourceAsStream("/biweekly.properties");
			Properties props = new Properties();
			props.load(in);

			VERSION = props.getProperty("version");
			URL = props.getProperty("url");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Parses an iCalendar object string.
	 * @param ical the iCalendar data
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainTextString parse(String ical) {
		return new ParserChainTextString(ical);
	}

	/**
	 * Parses an iCalendar file.
	 * @param file the iCalendar file
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainTextReader parse(File file) {
		return new ParserChainTextReader(file);
	}

	/**
	 * Parses an iCalendar data stream.
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainTextReader parse(InputStream in) {
		return new ParserChainTextReader(in);
	}

	/**
	 * Parses an iCalendar data stream.
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainTextReader parse(Reader reader) {
		return new ParserChainTextReader(reader);
	}

	/**
	 * Writes multiple iCalendar objects to a data stream.
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static WriterChainText write(ICalendar... icals) {
		return write(Arrays.asList(icals));
	}

	/**
	 * Writes multiple iCalendar objects to a data stream.
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static WriterChainText write(Collection<ICalendar> icals) {
		return new WriterChainText(icals);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects) from a string.
	 * @param xml the XML string
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainXmlString parseXml(String xml) {
		return new ParserChainXmlString(xml);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects) from a file.
	 * @param file the XML file
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainXmlReader parseXml(File file) {
		return new ParserChainXmlReader(file);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects) from an input
	 * stream.
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainXmlReader parseXml(InputStream in) {
		return new ParserChainXmlReader(in);
	}

	/**
	 * <p>
	 * Parses an xCal document (XML-encoded iCalendar objects) from a reader.
	 * </p>
	 * <p>
	 * Note that use of this method is discouraged. It ignores the character
	 * encoding that is defined within the XML document itself, and should only
	 * be used if the encoding is undefined or if the encoding needs to be
	 * ignored for whatever reason. The {@link #parseXml(InputStream)} method
	 * should be used instead, since it takes the XML document's character
	 * encoding into account when parsing.
	 * </p>
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainXmlReader parseXml(Reader reader) {
		return new ParserChainXmlReader(reader);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects).
	 * @param document the XML document
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainXmlDocument parseXml(Document document) {
		return new ParserChainXmlDocument(document);
	}

	/**
	 * Writes an xCal document (XML-encoded iCalendar objects).
	 * @param icals the iCalendar object(s) to write
	 * @return chainer object for completing the write operation
	 */
	public static WriterChainXml writeXml(ICalendar... icals) {
		return writeXml(Arrays.asList(icals));
	}

	/**
	 * Writes an xCal document (XML-encoded iCalendar objects).
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static WriterChainXml writeXml(Collection<ICalendar> icals) {
		return new WriterChainXml(icals);
	}

	/**
	 * Parses a jCal data stream (JSON-encoded iCalendar objects).
	 * @param json the JSON data
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainJsonString parseJson(String json) {
		return new ParserChainJsonString(json);
	}

	/**
	 * Parses a jCal data stream (JSON-encoded iCalendar objects).
	 * @param file the JSON file
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainJsonReader parseJson(File file) {
		return new ParserChainJsonReader(file);
	}

	/**
	 * Parses a jCal data stream (JSON-encoded iCalendar objects).
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainJsonReader parseJson(InputStream in) {
		return new ParserChainJsonReader(in);
	}

	/**
	 * Parses a jCal data stream (JSON-encoded iCalendar objects).
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainJsonReader parseJson(Reader reader) {
		return new ParserChainJsonReader(reader);
	}

	/**
	 * Writes an xCal document (XML-encoded iCalendar objects).
	 * @param icals the iCalendar object(s) to write
	 * @return chainer object for completing the write operation
	 */
	public static WriterChainJson writeJson(ICalendar... icals) {
		return writeJson(Arrays.asList(icals));
	}

	/**
	 * Writes an xCal document (XML-encoded iCalendar objects).
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static WriterChainJson writeJson(Collection<ICalendar> icals) {
		return new WriterChainJson(icals);
	}

	static abstract class ParserChain<T> {
		//Note: "package" level is used so various fields/methods don't show up in the Javadocs, but are still visible to child classes
		final ICalMarshallerRegistrar registrar = new ICalMarshallerRegistrar();

		@SuppressWarnings("unchecked")
		final T this_ = (T) this;

		List<List<String>> warnings;

		/**
		 * Registers a property marshaller.
		 * @param marshaller the marshaller
		 * @return this
		 */
		public T register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			registrar.register(marshaller);
			return this_;
		}

		/**
		 * Registers a component marshaller.
		 * @param marshaller the marshaller
		 * @return this
		 */
		public T register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			registrar.register(marshaller);
			return this_;
		}

		/**
		 * Provides a list for putting the parser warnings into.
		 * @param warnings the list object to populate (it is a
		 * "list of lists"--each parsed {@link ICalendar} object has its own
		 * warnings list)
		 * @return this
		 */
		public T warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this_;
		}

		/**
		 * Reads the first iCalendar object from the data stream.
		 * @return the first iCalendar object or null if there are none
		 * @throws IOException if there a problem reading from the data stream
		 * @throws SAXException if there's a problem parsing the XML
		 */
		public abstract ICalendar first() throws IOException, SAXException;

		/**
		 * Reads all iCalendar objects from the data stream.
		 * @return the parsed iCalendar objects
		 * @throws IOException if there's a problem reading from the data stream
		 * @throws SAXException if there's a problem parsing the XML
		 */
		public abstract List<ICalendar> all() throws IOException, SAXException;
	}

	///////////////////////////////////////////////////////
	// plain-text
	///////////////////////////////////////////////////////

	static abstract class ParserChainText<T> extends ParserChain<T> {
		boolean caretDecoding = true;
		final boolean closeWhenDone;

		private ParserChainText(boolean closeWhenDone) {
			this.closeWhenDone = closeWhenDone;
		}

		/**
		 * Sets whether the reader will decode parameter values that use
		 * circumflex accent encoding (enabled by default). This escaping
		 * mechanism allows newlines and double quotes to be included in
		 * parameter values.
		 * @param enable true to use circumflex accent decoding, false not to
		 * @see ICalRawReader#setCaretDecodingEnabled(boolean)
		 */
		public T caretDecoding(boolean enable) {
			caretDecoding = enable;
			return this_;
		}

		@Override
		public ICalendar first() throws IOException {
			ICalReader parser = constructReader();

			try {
				ICalendar ical = parser.readNext();
				if (warnings != null) {
					warnings.add(parser.getWarnings());
				}
				return ical;
			} finally {
				if (closeWhenDone) {
					IOUtils.closeQuietly(parser);
				}
			}
		}

		@Override
		public List<ICalendar> all() throws IOException {
			ICalReader parser = constructReader();

			try {
				List<ICalendar> icals = new ArrayList<ICalendar>();
				ICalendar ical;
				while ((ical = parser.readNext()) != null) {
					if (warnings != null) {
						warnings.add(parser.getWarnings());
					}
					icals.add(ical);
				}
				return icals;
			} finally {
				if (closeWhenDone) {
					IOUtils.closeQuietly(parser);
				}
			}
		}

		private ICalReader constructReader() throws IOException {
			ICalReader parser = _constructReader();
			parser.setRegistrar(registrar);
			parser.setCaretDecodingEnabled(caretDecoding);
			return parser;
		}

		abstract ICalReader _constructReader() throws IOException;
	}

	/**
	 * Chainer class for parsing plain text iCalendar data streams.
	 * @see Biweekly#parse(InputStream)
	 * @see Biweekly#parse(File)
	 * @see Biweekly#parse(Reader)
	 */
	public static class ParserChainTextReader extends ParserChainText<ParserChainTextReader> {
		private final InputStream in;
		private final File file;
		private final Reader reader;

		private ParserChainTextReader(InputStream in) {
			super(false);
			this.in = in;
			this.reader = null;
			this.file = null;
		}

		private ParserChainTextReader(File file) {
			super(true);
			this.in = null;
			this.reader = null;
			this.file = file;
		}

		private ParserChainTextReader(Reader reader) {
			super(false);
			this.in = null;
			this.reader = reader;
			this.file = null;
		}

		@Override
		public ParserChainTextReader register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainTextReader register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainTextReader warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		public ParserChainTextReader caretDecoding(boolean enable) {
			return super.caretDecoding(enable);
		}

		@Override
		ICalReader _constructReader() throws IOException {
			if (in != null) {
				return new ICalReader(in);
			}
			if (file != null) {
				return new ICalReader(file);
			}
			return new ICalReader(reader);
		}
	}

	/**
	 * Chainer class for parsing plain text iCalendar strings.
	 * @see Biweekly#parse(String)
	 */
	public static class ParserChainTextString extends ParserChainText<ParserChainTextString> {
		private final String text;

		private ParserChainTextString(String text) {
			super(false);
			this.text = text;
		}

		@Override
		public ParserChainTextString register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainTextString register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainTextString warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		public ParserChainTextString caretDecoding(boolean enable) {
			return super.caretDecoding(enable);
		}

		@Override
		ICalReader _constructReader() {
			return new ICalReader(text);
		}

		@Override
		public ICalendar first() {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}

		@Override
		public List<ICalendar> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}
	}

	///////////////////////////////////////////////////////
	// XML
	///////////////////////////////////////////////////////

	static abstract class ParserChainXml<T> extends ParserChain<T> {
		@Override
		public ICalendar first() throws IOException, SAXException {
			XCalDocument document = constructDocument();
			ICalendar ical = document.parseFirst();
			if (warnings != null) {
				warnings.addAll(document.getParseWarnings());
			}
			return ical;
		}

		@Override
		public List<ICalendar> all() throws IOException, SAXException {
			XCalDocument document = constructDocument();
			List<ICalendar> icals = document.parseAll();
			if (warnings != null) {
				warnings.addAll(document.getParseWarnings());
			}
			return icals;
		}

		private XCalDocument constructDocument() throws SAXException, IOException {
			XCalDocument parser = _constructDocument();
			parser.setRegistrar(registrar);
			return parser;
		}

		abstract XCalDocument _constructDocument() throws IOException, SAXException;
	}

	/**
	 * Chainer class for parsing XML-encoded iCalendar objects (xCal).
	 * @see Biweekly#parseXml(String)
	 */
	public static class ParserChainXmlString extends ParserChainXml<ParserChainXmlString> {
		private final String xml;

		private ParserChainXmlString(String xml) {
			this.xml = xml;
		}

		@Override
		public ParserChainXmlString register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainXmlString register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainXmlString warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		XCalDocument _constructDocument() throws SAXException {
			return new XCalDocument(xml);
		}

		@Override
		public ICalendar first() throws SAXException {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}

		@Override
		public List<ICalendar> all() throws SAXException {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}
	}

	/**
	 * Chainer class for parsing XML-encoded iCalendar objects (xCal).
	 * @see Biweekly#parseXml(InputStream)
	 * @see Biweekly#parseXml(File)
	 * @see Biweekly#parseXml(Reader)
	 */
	public static class ParserChainXmlReader extends ParserChainXml<ParserChainXmlReader> {
		private final InputStream in;
		private final File file;
		private final Reader reader;

		private ParserChainXmlReader(InputStream in) {
			this.in = in;
			this.reader = null;
			this.file = null;
		}

		private ParserChainXmlReader(File file) {
			this.in = null;
			this.reader = null;
			this.file = file;
		}

		private ParserChainXmlReader(Reader reader) {
			this.in = null;
			this.reader = reader;
			this.file = null;
		}

		@Override
		public ParserChainXmlReader register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainXmlReader register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainXmlReader warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		XCalDocument _constructDocument() throws IOException, SAXException {
			if (in != null) {
				return new XCalDocument(in);
			}
			if (file != null) {
				return new XCalDocument(file);
			}
			return new XCalDocument(reader);
		}
	}

	/**
	 * Chainer class for parsing XML-encoded iCalendar objects (xCal).
	 * @see Biweekly#parseXml(Document)
	 */
	public static class ParserChainXmlDocument extends ParserChainXml<ParserChainXmlDocument> {
		private final Document document;

		private ParserChainXmlDocument(Document document) {
			this.document = document;
		}

		@Override
		public ParserChainXmlDocument register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainXmlDocument register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainXmlDocument warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		XCalDocument _constructDocument() {
			return new XCalDocument(document);
		}

		@Override
		public ICalendar first() {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from string
			} catch (SAXException e) {
				//reading from Document
			}
			return null;
		}

		@Override
		public List<ICalendar> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from string
			} catch (SAXException e) {
				//reading from Document
			}
			return null;
		}
	}

	///////////////////////////////////////////////////////
	// JSON
	///////////////////////////////////////////////////////

	static abstract class ParserChainJson<T> extends ParserChain<T> {
		final boolean closeWhenDone;

		private ParserChainJson(boolean closeWhenDone) {
			this.closeWhenDone = closeWhenDone;
		}

		/**
		 * @throws JCalParseException if the jCal syntax is incorrect (the JSON
		 * syntax may be valid, but it is not in the correct jCal format).
		 * @throws JsonParseException if the JSON syntax is incorrect
		 */
		@Override
		public ICalendar first() throws IOException {
			JCalReader parser = constructReader();

			try {
				ICalendar ical = parser.readNext();
				if (warnings != null) {
					warnings.add(parser.getWarnings());
				}
				return ical;
			} finally {
				if (closeWhenDone) {
					IOUtils.closeQuietly(parser);
				}
			}
		}

		/**
		 * @throws JCalParseException if the jCal syntax is incorrect (the JSON
		 * syntax may be valid, but it is not in the correct jCal format).
		 * @throws JsonParseException if the JSON syntax is incorrect
		 */
		@Override
		public List<ICalendar> all() throws IOException {
			JCalReader parser = constructReader();

			try {
				List<ICalendar> icals = new ArrayList<ICalendar>();
				ICalendar ical;
				while ((ical = parser.readNext()) != null) {
					if (warnings != null) {
						warnings.add(parser.getWarnings());
					}
					icals.add(ical);
				}
				return icals;
			} finally {
				if (closeWhenDone) {
					IOUtils.closeQuietly(parser);
				}
			}
		}

		private JCalReader constructReader() throws IOException {
			JCalReader parser = _constructReader();
			parser.setRegistrar(registrar);
			return parser;
		}

		abstract JCalReader _constructReader() throws IOException;
	}

	/**
	 * Chainer class for parsing JSON-encoded iCalendar data streams (jCal).
	 * @see Biweekly#parseJson(InputStream)
	 * @see Biweekly#parseJson(File)
	 * @see Biweekly#parseJson(Reader)
	 */
	public static class ParserChainJsonReader extends ParserChainJson<ParserChainJsonReader> {
		private final InputStream in;
		private final File file;
		private final Reader reader;

		private ParserChainJsonReader(InputStream in) {
			super(false);
			this.in = in;
			this.reader = null;
			this.file = null;
		}

		private ParserChainJsonReader(File file) {
			super(true);
			this.in = null;
			this.reader = null;
			this.file = file;
		}

		private ParserChainJsonReader(Reader reader) {
			super(false);
			this.in = null;
			this.reader = reader;
			this.file = null;
		}

		@Override
		public ParserChainJsonReader register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainJsonReader register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainJsonReader warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		JCalReader _constructReader() throws IOException {
			if (in != null) {
				return new JCalReader(in);
			}
			if (file != null) {
				return new JCalReader(file);
			}
			return new JCalReader(reader);
		}
	}

	/**
	 * Chainer class for parsing JSON-encoded iCalendar strings (jCal).
	 * @see Biweekly#parseJson(String)
	 */
	public static class ParserChainJsonString extends ParserChainJson<ParserChainJsonString> {
		private final String text;

		private ParserChainJsonString(String text) {
			super(false);
			this.text = text;
		}

		@Override
		public ParserChainJsonString register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainJsonString register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public ParserChainJsonString warnings(List<List<String>> warnings) {
			return super.warnings(warnings);
		}

		@Override
		JCalReader _constructReader() {
			return new JCalReader(text);
		}

		@Override
		public ICalendar first() {
			try {
				return super.first();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}

		@Override
		public List<ICalendar> all() {
			try {
				return super.all();
			} catch (IOException e) {
				//reading from string
			}
			return null;
		}
	}

	static abstract class WriterChain<T> {
		final Collection<ICalendar> icals;
		final ICalMarshallerRegistrar registrar = new ICalMarshallerRegistrar();

		@SuppressWarnings("unchecked")
		final T this_ = (T) this;

		WriterChain(Collection<ICalendar> icals) {
			this.icals = icals;
		}

		/**
		 * Registers a property marshaller.
		 * @param marshaller the marshaller
		 * @return this
		 */
		public T register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			registrar.register(marshaller);
			return this_;
		}

		/**
		 * Registers a component marshaller.
		 * @param marshaller the marshaller
		 * @return this
		 */
		public T register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			registrar.register(marshaller);
			return this_;
		}
	}

	///////////////////////////////////////////////////////
	// plain-text
	///////////////////////////////////////////////////////

	/**
	 * Chainer class for writing to plain text iCalendar data streams.
	 * @see Biweekly#write(Collection)
	 * @see Biweekly#write(ICalendar...)
	 */
	public static class WriterChainText extends WriterChain<WriterChainText> {
		boolean caretEncoding = false;

		private WriterChainText(Collection<ICalendar> icals) {
			super(icals);
		}

		/**
		 * <p>
		 * Sets whether the writer will apply circumflex accent encoding on
		 * parameter values (disabled by default). This escaping mechanism
		 * allows for newlines and double quotes to be included in parameter
		 * values.
		 * </p>
		 * 
		 * <p>
		 * When disabled, the writer will replace newlines with spaces and
		 * double quotes with single quotes.
		 * </p>
		 * @param enable true to use circumflex accent encoding, false not to
		 * @see ICalRawWriter#setCaretEncodingEnabled(boolean)
		 */
		public WriterChainText caretEncoding(boolean enable) {
			this.caretEncoding = enable;
			return this_;
		}

		/**
		 * Writes the iCalendar objects to a string.
		 * @return the iCalendar string
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 */
		public String go() {
			StringWriter sw = new StringWriter();
			try {
				go(sw);
			} catch (IOException e) {
				//writing to a string
			}
			return sw.toString();
		}

		/**
		 * Writes the iCalendar objects to a data stream.
		 * @param out the output stream to write to
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws IOException if there's a problem writing to the output stream
		 */
		public void go(OutputStream out) throws IOException {
			go(new ICalWriter(out));
		}

		/**
		 * Writes the iCalendar objects to a file.
		 * @param file the file to write to
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws IOException if there's a problem writing to the file
		 */
		public void go(File file) throws IOException {
			go(file, false);
		}

		/**
		 * Writes the iCalendar objects to a file.
		 * @param file the file to write to
		 * @param append true to append to the end of the file, false to
		 * overwrite it
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws IOException if there's a problem writing to the file
		 */
		public void go(File file, boolean append) throws IOException {
			ICalWriter icalWriter = new ICalWriter(file, append);
			try {
				go(icalWriter);
			} finally {
				IOUtils.closeQuietly(icalWriter);
			}
		}

		/**
		 * Writes the iCalendar objects to a data stream.
		 * @param writer the writer to write to
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws IOException if there's a problem writing to the writer
		 */
		public void go(Writer writer) throws IOException {
			go(new ICalWriter(writer));
		}

		private void go(ICalWriter icalWriter) throws IOException {
			icalWriter.setRegistrar(registrar);
			icalWriter.setCaretEncodingEnabled(caretEncoding);

			for (ICalendar ical : icals) {
				icalWriter.write(ical);
			}
		}
	}

	///////////////////////////////////////////////////////
	// XML
	///////////////////////////////////////////////////////

	/**
	 * Chainer class for writing xCal documents (XML-encoded iCalendar objects).
	 * @see Biweekly#writeXml(Collection)
	 * @see Biweekly#writeXml(ICalendar...)
	 */
	public static class WriterChainXml extends WriterChain<WriterChainXml> {
		int indent = -1;
		final Map<String, ICalDataType> parameterDataTypes = new HashMap<String, ICalDataType>(0);

		WriterChainXml(Collection<ICalendar> icals) {
			super(icals);
		}

		@Override
		public WriterChainXml register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public WriterChainXml register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		/**
		 * Registers the data type of an experimental parameter. Experimental
		 * parameters use the "unknown" xCal data type by default.
		 * @param parameterName the parameter name (e.g. "x-foo")
		 * @param dataType the data type
		 */
		public WriterChainXml register(String parameterName, ICalDataType dataType) {
			parameterDataTypes.put(parameterName, dataType);
			return this_;
		}

		/**
		 * Sets the number of indent spaces to use for pretty-printing. If not
		 * set, then the XML will not be pretty-printed.
		 * @param indent the number of spaces
		 * @return this
		 */
		public WriterChainXml indent(int indent) {
			this.indent = indent;
			return this_;
		}

		/**
		 * Writes the xCal document to a string.
		 * @return the XML string
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 */
		public String go() {
			StringWriter sw = new StringWriter();
			try {
				go(sw);
			} catch (TransformerException e) {
				//writing to a string
			}
			return sw.toString();
		}

		/**
		 * Writes the xCal document to an output stream.
		 * @param out the output stream to write to
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws TransformerException if there's a problem writing the XML
		 */
		public void go(OutputStream out) throws TransformerException {
			XCalDocument document = constructDocument();
			document.write(out, indent);
		}

		/**
		 * Writes the xCal document to a file.
		 * @param file the file to write to
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws TransformerException if there's a problem writing the XML
		 * @throws IOException if there's a problem writing to the file
		 */
		public void go(File file) throws TransformerException, IOException {
			XCalDocument document = constructDocument();
			document.write(file, indent);
		}

		/**
		 * Writes the xCal document to a writer.
		 * @param writer the writer to write to
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws TransformerException if there's a problem writing the XML
		 */
		public void go(Writer writer) throws TransformerException {
			XCalDocument document = constructDocument();
			document.write(writer, indent);
		}

		/**
		 * Writes the xCal document to an XML DOM.
		 * @return the XML DOM
		 */
		public Document dom() {
			XCalDocument document = constructDocument();
			return document.getDocument();
		}

		private XCalDocument constructDocument() {
			XCalDocument document = new XCalDocument();
			document.setRegistrar(registrar);
			for (Map.Entry<String, ICalDataType> entry : parameterDataTypes.entrySet()) {
				document.registerParameterDataType(entry.getKey(), entry.getValue());
			}

			for (ICalendar ical : icals) {
				document.add(ical);
			}

			return document;
		}
	}

	///////////////////////////////////////////////////////
	// JSON
	///////////////////////////////////////////////////////

	/**
	 * Chainer class for writing to JSON-encoded iCalendar data streams (jCal).
	 * @see Biweekly#writeJson(Collection)
	 * @see Biweekly#writeJson(ICalendar...)
	 */
	public static class WriterChainJson extends WriterChain<WriterChainJson> {
		private boolean indent = false;

		private WriterChainJson(Collection<ICalendar> icals) {
			super(icals);
		}

		/**
		 * Sets whether or not to pretty-print the JSON.
		 * @param indent true to pretty-print it, false not to (defaults to
		 * false)
		 * @return this
		 */
		public WriterChainJson indent(boolean indent) {
			this.indent = indent;
			return this_;
		}

		/**
		 * Writes the iCalendar objects to a string.
		 * @return the iCalendar string
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 */
		public String go() {
			StringWriter sw = new StringWriter();
			try {
				go(sw);
			} catch (IOException e) {
				//writing to a string
			}
			return sw.toString();
		}

		/**
		 * Writes the iCalendar objects to a data stream.
		 * @param out the output stream to write to
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws IOException if there's a problem writing to the output stream
		 */
		public void go(OutputStream out) throws IOException {
			go(new JCalWriter(out, icals.size() > 1));
		}

		/**
		 * Writes the iCalendar objects to a file.
		 * @param file the file to write to
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws IOException if there's a problem writing to the file
		 */
		public void go(File file) throws IOException {
			JCalWriter jcalWriter = new JCalWriter(file, icals.size() > 1);
			try {
				go(jcalWriter);
			} finally {
				IOUtils.closeQuietly(jcalWriter);
			}
		}

		/**
		 * Writes the iCalendar objects to a data stream.
		 * @param writer the writer to write to
		 * @throws IllegalArgumentException if the marshaller class for a
		 * component or property object cannot be found (only happens when an
		 * experimental property/component marshaller is not registered with the
		 * {@code register} method.)
		 * @throws IOException if there's a problem writing to the writer
		 */
		public void go(Writer writer) throws IOException {
			go(new JCalWriter(writer, icals.size() > 1));
		}

		private void go(JCalWriter jcalWriter) throws IOException {
			jcalWriter.setRegistrar(registrar);
			jcalWriter.setIndent(indent);

			for (ICalendar ical : icals) {
				jcalWriter.write(ical);
			}
			jcalWriter.closeJsonStream();
		}
	}

	private Biweekly() {
		//hide
	}
}
