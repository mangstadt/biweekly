package biweekly;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
import biweekly.io.text.ICalRawReader;
import biweekly.io.text.ICalRawWriter;
import biweekly.io.text.ICalReader;
import biweekly.io.text.ICalWriter;
import biweekly.io.xml.XCalDocument;
import biweekly.parameter.Value;
import biweekly.property.ICalProperty;
import biweekly.property.marshaller.ICalPropertyMarshaller;
import biweekly.util.IOUtils;

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
 * objects. For data streaming, use the {@link ICalReader} and
 * {@link ICalWriter} classes.
 * </p>
 * 
 * <p>
 * <b>Writing an iCalendar object</b>
 * 
 * <pre>
 * ICalendar ical = new ICalendar();
 * 
 * //string
 * String icalString = Biweekly.write(ical).go();
 * 
 * //file
 * File file = new File("meeting.ics");
 * Biweekly.write(ical).go(file);
 * 
 * //writer
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
 * <pre>
 * ICalendar ical1 = new ICalendar();
 * ICalendar ical2 = new ICalendar();
 * 
 * String icalString = Biweekly.write(ical1, ical2).go();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Reading an iCalendar object</b>
 * 
 * <pre>
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
 * //reader
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
 * <pre>
 * String icalStr = ...
 * List&lt;ICalendar&gt; icals = Biweekly.parse(icalStr).all();
 * </pre>
 * 
 * </p>
 * 
 * <p>
 * <b>Retrieving parser warnings</b>
 * 
 * <pre>
 * String icalStr = ...
 * List&lt;List&lt;String&gt;&gt; warnings = new ArrayList&lt;List&lt;String&gt;&gt;();
 * ICalendar ical = Biweekly.parse(icalStr).warnings(warnings).first();
 * for (List&lt;String&gt; warningsList : warnings){
 *   System.out.println("iCal warnings:");
 *   for (String warning : warningsList){
 *     System.out.println(warning);
 *   }
 * }
 * </pre>
 * 
 * </p>
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
	 * @return chainer object for completing the parse operation
	 * @throws FileNotFoundException if the file does not exist or cannot be
	 * accessed
	 */
	public static ParserChainTextReader parse(File file) throws FileNotFoundException {
		return new ParserChainTextReader(new FileReader(file), true); //close the FileReader, since we created it
	}

	/**
	 * Parses an iCalendar data stream.
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainTextReader parse(InputStream in) {
		return parse(new InputStreamReader(in));
	}

	/**
	 * Parses an iCalendar data stream.
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainTextReader parse(Reader reader) {
		return new ParserChainTextReader(reader, false); //do not close the Reader, since we didn't create it
	}

	/**
	 * Writes an iCalendar object to a data stream.
	 * @param ical the iCalendar object to write
	 * @return chainer object for completing the write operation
	 */
	public static WriterChainTextSingle write(ICalendar ical) {
		return new WriterChainTextSingle(ical);
	}

	/**
	 * Writes multiple iCalendar objects to a data stream.
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static WriterChainTextMulti write(ICalendar... icals) {
		return write(Arrays.asList(icals));
	}

	/**
	 * Writes multiple iCalendar objects to a data stream.
	 * @param icals the iCalendar objects to write
	 * @return chainer object for completing the write operation
	 */
	public static WriterChainTextMulti write(Collection<ICalendar> icals) {
		return new WriterChainTextMulti(icals);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects).
	 * @param xml the XML string
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainXmlString parseXml(String xml) {
		return new ParserChainXmlString(xml);
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects).
	 * @param file the XML file
	 * @return chainer object for completing the parse operation
	 * @throws FileNotFoundException if the file does not exist or cannot be
	 * accessed
	 */
	public static ParserChainXmlReader parseXml(File file) throws FileNotFoundException {
		return new ParserChainXmlReader(file); //close the FileReader, since we created it
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects).
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainXmlReader parseXml(InputStream in) {
		return parseXml(new InputStreamReader(in));
	}

	/**
	 * Parses an xCal document (XML-encoded iCalendar objects).
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

	static abstract class ParserChain<T> {
		//Note: "package" level is used so various fields/methods don't show up in the Javadocs, but are still visible to child classes
		final List<ICalPropertyMarshaller<? extends ICalProperty>> propertyMarshallers = new ArrayList<ICalPropertyMarshaller<? extends ICalProperty>>(0);
		final List<ICalComponentMarshaller<? extends ICalComponent>> componentMarshallers = new ArrayList<ICalComponentMarshaller<? extends ICalComponent>>(0);

		@SuppressWarnings("unchecked")
		final T this_ = (T) this;

		List<List<String>> warnings;

		/**
		 * Registers a property marshaller.
		 * @param marshaller the marshaller
		 * @return this
		 */
		public T register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			propertyMarshallers.add(marshaller);
			return this_;
		}

		/**
		 * Registers a component marshaller.
		 * @param marshaller the marshaller
		 * @return this
		 */
		public T register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			componentMarshallers.add(marshaller);
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

		private ICalReader constructReader() {
			ICalReader parser = _constructReader();
			for (ICalPropertyMarshaller<? extends ICalProperty> marshaller : propertyMarshallers) {
				parser.registerMarshaller(marshaller);
			}
			for (ICalComponentMarshaller<? extends ICalComponent> marshaller : componentMarshallers) {
				parser.registerMarshaller(marshaller);
			}
			parser.setCaretDecodingEnabled(caretDecoding);
			return parser;
		}

		abstract ICalReader _constructReader();
	}

	/**
	 * Chainer class for parsing plain text iCalendar data streams.
	 * @see Biweekly#parse(InputStream)
	 * @see Biweekly#parse(File)
	 * @see Biweekly#parse(Reader)
	 */
	public static class ParserChainTextReader extends ParserChainText<ParserChainTextReader> {
		private final Reader reader;

		private ParserChainTextReader(Reader reader, boolean closeWhenDone) {
			super(closeWhenDone);
			this.reader = reader;
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
		ICalReader _constructReader() {
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
			for (ICalPropertyMarshaller<? extends ICalProperty> marshaller : propertyMarshallers) {
				parser.registerMarshaller(marshaller);
			}
			for (ICalComponentMarshaller<? extends ICalComponent> marshaller : componentMarshallers) {
				parser.registerMarshaller(marshaller);
			}
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

		public ParserChainXmlString(String xml) {
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
		private final Reader reader;
		private final File file;

		public ParserChainXmlReader(Reader reader) {
			this.reader = reader;
			this.file = null;
		}

		public ParserChainXmlReader(File file) {
			this.reader = null;
			this.file = file;
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
		XCalDocument _constructDocument() throws IOException, SAXException {
			return (reader == null) ? new XCalDocument(file) : new XCalDocument(reader);
		}
	}

	/**
	 * Chainer class for parsing XML-encoded iCalendar objects (xCal).
	 * @see Biweekly#parseXml(Document)
	 */
	public static class ParserChainXmlDocument extends ParserChainXml<ParserChainXmlDocument> {
		private final Document document;

		public ParserChainXmlDocument(Document document) {
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

	static abstract class WriterChain<T> {
		final Collection<ICalendar> icals;
		final List<ICalPropertyMarshaller<? extends ICalProperty>> propertyMarshallers = new ArrayList<ICalPropertyMarshaller<? extends ICalProperty>>(0);
		final List<ICalComponentMarshaller<? extends ICalComponent>> componentMarshallers = new ArrayList<ICalComponentMarshaller<? extends ICalComponent>>(0);

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
			propertyMarshallers.add(marshaller);
			return this_;
		}

		/**
		 * Registers a component marshaller.
		 * @param marshaller the marshaller
		 * @return this
		 */
		public T register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			componentMarshallers.add(marshaller);
			return this_;
		}
	}

	///////////////////////////////////////////////////////
	// plain-text
	///////////////////////////////////////////////////////

	static abstract class WriterChainText<T> extends WriterChain<T> {
		boolean caretEncoding = false;

		WriterChainText(Collection<ICalendar> icals) {
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
		public T caretEncoding(boolean enable) {
			this.caretEncoding = enable;
			return this_;
		}

		/**
		 * Writes the iCalendar objects to a string.
		 * @return the iCalendar string
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
		 * @throws IOException if there's a problem writing to the output stream
		 */
		public void go(OutputStream out) throws IOException {
			go(new OutputStreamWriter(out));
		}

		/**
		 * Writes the iCalendar objects to a file.
		 * @param file the file to write to
		 * @throws IOException if there's a problem writing to the file
		 */
		public void go(File file) throws IOException {
			FileWriter writer = null;
			try {
				writer = new FileWriter(file);
				go(writer);
			} finally {
				IOUtils.closeQuietly(writer);
			}
		}

		/**
		 * Writes the iCalendar objects to a data stream.
		 * @param writer the writer to write to
		 * @throws IOException if there's a problem writing to the writer
		 */
		public void go(Writer writer) throws IOException {
			ICalWriter icalWriter = new ICalWriter(writer);
			for (ICalPropertyMarshaller<? extends ICalProperty> marshaller : propertyMarshallers) {
				icalWriter.registerMarshaller(marshaller);
			}
			for (ICalComponentMarshaller<? extends ICalComponent> marshaller : componentMarshallers) {
				icalWriter.registerMarshaller(marshaller);
			}
			icalWriter.setCaretEncodingEnabled(caretEncoding);

			for (ICalendar ical : icals) {
				icalWriter.write(ical);
				addWarnings(icalWriter.getWarnings());
			}
		}

		abstract void addWarnings(List<String> warnings);
	}

	/**
	 * Chainer class for writing to plain text iCalendar data streams.
	 * @see Biweekly#write(Collection)
	 * @see Biweekly#write(ICalendar...)
	 */
	public static class WriterChainTextMulti extends WriterChainText<WriterChainTextMulti> {
		private List<List<String>> warnings;

		private WriterChainTextMulti(Collection<ICalendar> icals) {
			super(icals);
		}

		@Override
		public WriterChainTextMulti caretEncoding(boolean enable) {
			return super.caretEncoding(enable);
		}

		/**
		 * Provides a list for putting the marshal warnings into.
		 * @param warnings the list object to populate (it is a
		 * "list of lists"--each {@link ICalendar} object has its own warnings
		 * list)
		 * @return this
		 */
		public WriterChainTextMulti warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		public WriterChainTextMulti register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public WriterChainTextMulti register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.add(warnings);
			}
		}
	}

	/**
	 * Chainer class for writing to plain text iCalendar data streams.
	 * @see Biweekly#write(ICalendar)
	 */
	public static class WriterChainTextSingle extends WriterChainText<WriterChainTextSingle> {
		private List<String> warnings;

		private WriterChainTextSingle(ICalendar ical) {
			super(Arrays.asList(ical));
		}

		@Override
		public WriterChainTextSingle caretEncoding(boolean enable) {
			return super.caretEncoding(enable);
		}

		/**
		 * Provides a list for putting the parser warnings into.
		 * @param warnings the list object to populate
		 * @return this
		 */
		public WriterChainTextSingle warnings(List<String> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		public WriterChainTextSingle register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			return super.register(marshaller);
		}

		@Override
		public WriterChainTextSingle register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			return super.register(marshaller);
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.addAll(warnings);
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
		final Map<String, Value> parameterDataTypes = new HashMap<String, Value>(0);

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
		public WriterChainXml register(String parameterName, Value dataType) {
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
		 * @throws TransformerException if there's a problem writing the XML
		 */
		public void go(OutputStream out) throws TransformerException {
			go(new OutputStreamWriter(out));
		}

		/**
		 * Writes the xCal document to a file..
		 * @param file the file to write to
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

			for (ICalPropertyMarshaller<? extends ICalProperty> marshaller : propertyMarshallers) {
				document.registerMarshaller(marshaller);
			}
			for (ICalComponentMarshaller<? extends ICalComponent> marshaller : componentMarshallers) {
				document.registerMarshaller(marshaller);
			}
			for (Map.Entry<String, Value> entry : parameterDataTypes.entrySet()) {
				document.registerParameterDataType(entry.getKey(), entry.getValue());
			}

			for (ICalendar ical : icals) {
				document.add(ical);
			}

			return document;
		}
	}

	private Biweekly() {
		//hide
	}
}
