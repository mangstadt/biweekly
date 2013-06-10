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
import java.util.List;
import java.util.Properties;

import org.xml.sax.SAXException;

import biweekly.component.ICalComponent;
import biweekly.component.marshaller.ICalComponentMarshaller;
import biweekly.io.IParser;
import biweekly.io.text.ICalReader;
import biweekly.io.text.ICalWriter;
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
 * Contains static, chaining factory methods for reading/writing iCalendar
 * objects. The methods make use of the core {@link ICalReader} and
 * {@link ICalWriter} classes.
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
	 * Parses an iCalendar object string. Use {@link ICalReader} to stream the
	 * data.
	 * @param ical the iCalendar data
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainTextString parse(String ical) {
		return new ParserChainTextString(ical);
	}

	/**
	 * Parses an iCalendar file. Use {@link ICalReader} to stream the data.
	 * @param file the file
	 * @return chainer object for completing the parse operation
	 * @throws FileNotFoundException if the file does not exist or cannot be
	 * accessed
	 */
	public static ParserChainTextReader parse(File file) throws FileNotFoundException {
		return parse(new FileReader(file));
	}

	/**
	 * Parses an iCalendar data stream. Use {@link ICalReader} to stream the
	 * data.
	 * @param in the input stream
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainTextReader parse(InputStream in) {
		return parse(new InputStreamReader(in));
	}

	/**
	 * Parses an iCalendar data stream. Use {@link ICalReader} to stream the
	 * data.
	 * @param reader the reader
	 * @return chainer object for completing the parse operation
	 */
	public static ParserChainTextReader parse(Reader reader) {
		return new ParserChainTextReader(reader);
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

	static abstract class ParserChain<T, U extends IParser> {
		final List<ICalPropertyMarshaller<? extends ICalProperty>> propertyMarshallers = new ArrayList<ICalPropertyMarshaller<? extends ICalProperty>>(0);
		final List<ICalComponentMarshaller<? extends ICalComponent>> componentMarshallers = new ArrayList<ICalComponentMarshaller<? extends ICalComponent>>(0);
		List<List<String>> warnings;

		/**
		 * Registers a property marshaller.
		 * @param marshaller the marshaller
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T register(ICalPropertyMarshaller<? extends ICalProperty> marshaller) {
			propertyMarshallers.add(marshaller);
			return (T) this;
		}

		/**
		 * Registers a component marshaller.
		 * @param marshaller the marshaller
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T register(ICalComponentMarshaller<? extends ICalComponent> marshaller) {
			componentMarshallers.add(marshaller);
			return (T) this;
		}

		/**
		 * Provides a list object that any unmarshal warnings will be put into.
		 * @param warnings the list object that will be populated with the
		 * warnings of each unmarshalled iCalendar object. Each element of the
		 * list is the list of warnings for one of the unmarshalled iCalendar
		 * objects. Therefore, the size of this list will be equal to the number
		 * of parsed iCalendar objects. If an iCalendar object does not have any
		 * warnings, then its warning list will be empty.
		 * @return this
		 */
		@SuppressWarnings("unchecked")
		public T warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return (T) this;
		}

		/**
		 * Creates the parser.
		 * @return the parser object
		 */
		abstract U init() throws IOException, SAXException;

		U ready() throws IOException, SAXException {
			U parser = init();
			for (ICalPropertyMarshaller<? extends ICalProperty> marshaller : propertyMarshallers) {
				parser.registerMarshaller(marshaller);
			}
			for (ICalComponentMarshaller<? extends ICalComponent> marshaller : componentMarshallers) {
				parser.registerMarshaller(marshaller);
			}
			return parser;
		}

		/**
		 * Reads the first iCalendar object from the stream.
		 * @return the first iCalendar object or null if there are no iCalendar
		 * objects
		 * @throws IOException if there's an I/O problem
		 * @throws SAXException if there's a problem parsing the XML
		 */
		public ICalendar first() throws IOException, SAXException {
			IParser parser = ready();
			ICalendar ical = parser.readNext();
			if (warnings != null) {
				warnings.add(parser.getWarnings());
			}
			return ical;
		}

		/**
		 * Reads all iCalendar objects from the stream.
		 * @return the parsed iCalendar objects
		 * @throws IOException if there's an I/O problem
		 * @throws SAXException if there's a problem parsing the XML
		 */
		public List<ICalendar> all() throws IOException, SAXException {
			IParser parser = ready();
			List<ICalendar> icals = new ArrayList<ICalendar>();
			ICalendar ical;
			while ((ical = parser.readNext()) != null) {
				if (warnings != null) {
					warnings.add(parser.getWarnings());
				}
				icals.add(ical);
			}
			return icals;
		}

	}

	static abstract class ParserChainText<T> extends ParserChain<T, ICalReader> {
		boolean caretDecoding = true;

		/**
		 * Sets whether the reader will decode characters in parameter values
		 * that use circumflex accent encoding (enabled by default).
		 * 
		 * @param enable true to use circumflex accent decoding, false not to
		 * @see ICalReader#setCaretDecodingEnabled(boolean)
		 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
		 */
		@SuppressWarnings("unchecked")
		public T caretDecoding(boolean enable) {
			caretDecoding = enable;
			return (T) this;
		}

		@Override
		ICalReader ready() throws IOException, SAXException {
			ICalReader parser = super.ready();
			parser.setCaretDecodingEnabled(caretDecoding);
			return parser;
		}

		@Override
		public ICalendar first() throws IOException {
			try {
				return super.first();
			} catch (SAXException e) {
				//not parsing XML
			}
			return null;
		}

		@Override
		public List<ICalendar> all() throws IOException {
			try {
				return super.all();
			} catch (SAXException e) {
				//not parsing XML
			}
			return null;
		}
	}

	/**
	 * Convenience chainer class for parsing plain text iCalendar data streams.
	 */
	public static class ParserChainTextReader extends ParserChainText<ParserChainTextReader> {
		private Reader reader;

		private ParserChainTextReader(Reader reader) {
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
		ICalReader init() {
			return new ICalReader(reader);
		}
	}

	/**
	 * Convenience chainer class for parsing plain text iCalendar data streams.
	 */
	public static class ParserChainTextString extends ParserChainText<ParserChainTextString> {
		private String text;

		private ParserChainTextString(String text) {
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
		ICalReader init() {
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

	static abstract class WriterChain {
		final Collection<ICalendar> icals;

		WriterChain(Collection<ICalendar> icals) {
			this.icals = icals;
		}
	}

	static abstract class WriterChainText<T> extends WriterChain {
		boolean caretEncoding = false;

		WriterChainText(Collection<ICalendar> icals) {
			super(icals);
		}

		/**
		 * Sets whether the writer will use circumflex accent encoding for
		 * parameter values (disabled by default).
		 * @param enable true to use circumflex accent encoding, false not to
		 * @see ICalWriter#setCaretEncodingEnabled(boolean)
		 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
		 */
		@SuppressWarnings("unchecked")
		public T caretEncoding(boolean enable) {
			this.caretEncoding = enable;
			return (T) this;
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
			icalWriter.setCaretEncodingEnabled(caretEncoding);

			for (ICalendar ical : icals) {
				icalWriter.write(ical);
				addWarnings(icalWriter.getWarnings());
			}
		}

		abstract void addWarnings(List<String> warnings);
	}

	/**
	 * Convenience chainer class for writing to plain text iCalendar data
	 * streams.
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
		 * Provides a list object that any marshal warnings will be put into.
		 * @param warnings the list object that will be populated with the
		 * warnings of each marshalled iCalendar object. Each element of the
		 * list is the list of warnings for one of the marshalled iCalendar
		 * objects. Therefore, the size of this list will be equal to the number
		 * of written iCalendar objects. If an iCalendar object does not have
		 * any warnings, then its warning list will be empty.
		 * @return this
		 */
		public WriterChainTextMulti warnings(List<List<String>> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.add(warnings);
			}
		}
	}

	/**
	 * Convenience chainer class for writing to plain text iCalendar data
	 * streams.
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
		 * Provides a list object that any marshal warnings will be put into.
		 * @param warnings the list object that will be populated with the
		 * warnings of the marshalled iCalendar object.
		 * @return this
		 */
		public WriterChainTextSingle warnings(List<String> warnings) {
			this.warnings = warnings;
			return this;
		}

		@Override
		void addWarnings(List<String> warnings) {
			if (this.warnings != null) {
				this.warnings.addAll(warnings);
			}
		}
	}
}
