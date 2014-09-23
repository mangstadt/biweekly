package biweekly.io.text;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import biweekly.ICalVersion;
import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;

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
 * Writes data to an iCalendar data stream.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545">RFC 5545</a>
 */
public class ICalRawWriter implements Closeable, Flushable {
	/**
	 * Regular expression used to determine if a parameter value needs to be
	 * quoted.
	 */
	private static final Pattern quoteMeRegex = Pattern.compile(".*?[,:;].*");

	/**
	 * Regular expression used to detect newline character sequences.
	 */
	private static final Pattern newlineRegex = Pattern.compile("\\r\\n|\\r|\\n");

	/**
	 * Regular expression used to determine if a property name contains any
	 * invalid characters.
	 */
	private static final Pattern propertyNameRegex = Pattern.compile("(?i)[-a-z0-9]+");

	/**
	 * The characters that are not valid in parameter values and that should be
	 * removed.
	 */
	private static final Map<ICalVersion, BitSet> invalidParamValueChars;
	static {
		BitSet controlChars = new BitSet(128);
		controlChars.set(0, 31);
		controlChars.set(127);
		controlChars.set('\t', false); //allow
		controlChars.set('\n', false); //allow
		controlChars.set('\r', false); //allow

		Map<ICalVersion, BitSet> map = new HashMap<ICalVersion, BitSet>();

		//1.0
		{
			BitSet bitSet = new BitSet(128);
			bitSet.or(controlChars);

			bitSet.set(',');
			bitSet.set('.');
			bitSet.set(':');
			bitSet.set('=');
			bitSet.set('[');
			bitSet.set(']');

			map.put(ICalVersion.V1_0, bitSet);
		}

		//2.0
		{
			BitSet bitSet = new BitSet(128);
			bitSet.or(controlChars);

			map.put(ICalVersion.V2_0_DEPRECATED, bitSet);
			map.put(ICalVersion.V2_0, bitSet);
		}

		invalidParamValueChars = Collections.unmodifiableMap(map);
	}

	private final FoldedLineWriter writer;
	private boolean caretEncodingEnabled = false;
	private ICalVersion version;

	/**
	 * Creates an iCalendar raw writer.
	 * @param writer the writer to the data stream
	 * @param version the version to adhere to
	 */
	public ICalRawWriter(Writer writer, ICalVersion version) {
		this.writer = new FoldedLineWriter(writer);
		this.version = version;
	}

	/**
	 * Gets the writer that this object wraps.
	 * @return the folded line writer
	 */
	public FoldedLineWriter getFoldedLineWriter() {
		return writer;
	}

	/**
	 * <p>
	 * Gets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values.
	 * </p>
	 * 
	 * <p>
	 * When disabled, the writer will replace newlines with spaces and double
	 * quotes with single quotes.
	 * </p>
	 * 
	 * <table border="1">
	 * <tr>
	 * <th>Character</th>
	 * <th>Replacement<br>
	 * (when disabled)</th>
	 * <th>Replacement<br>
	 * (when enabled)</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code '}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td><code><i>space</i></code></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^}</td>
	 * <td>{@code ^}</td>
	 * <td>{@code ^^}</td>
	 * </tr>
	 * </table>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPitt
	 *  sburgh, PA 15212":40.446816;80.00566
	 * </pre>
	 * 
	 * @return true if circumflex accent encoding is enabled, false if not
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public boolean isCaretEncodingEnabled() {
		return caretEncodingEnabled;
	}

	/**
	 * <p>
	 * Sets whether the writer will apply circumflex accent encoding on
	 * parameter values (disabled by default). This escaping mechanism allows
	 * for newlines and double quotes to be included in parameter values.
	 * </p>
	 * 
	 * <p>
	 * When disabled, the writer will replace newlines with spaces and double
	 * quotes with single quotes.
	 * </p>
	 * 
	 * <table border="1">
	 * <tr>
	 * <th>Character</th>
	 * <th>Replacement<br>
	 * (when disabled)</th>
	 * <th>Replacement<br>
	 * (when enabled)</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code '}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td><code><i>space</i></code></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
	 * <td>{@code ^}</td>
	 * <td>{@code ^}</td>
	 * <td>{@code ^^}</td>
	 * </tr>
	 * </table>
	 * 
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPitt
	 *  sburgh, PA 15212":40.446816;80.00566
	 * </pre>
	 * 
	 * @param enable true to use circumflex accent encoding, false not to
	 * @see <a href="http://tools.ietf.org/html/rfc6868">RFC 6868</a>
	 */
	public void setCaretEncodingEnabled(boolean enable) {
		caretEncodingEnabled = enable;
	}

	/**
	 * Gets the iCalendar version that the writer is adhering to.
	 * @return the version
	 */
	public ICalVersion getVersion() {
		return version;
	}

	/**
	 * Sets the iCalendar version that the writer should adhere to.
	 * @param version the version
	 */
	public void setVersion(ICalVersion version) {
		this.version = version;
	}

	/**
	 * Writes a property marking the beginning of a component (in other words,
	 * writes a "BEGIN:NAME" property).
	 * @param componentName the component name (e.g. "VEVENT")
	 * @throws IOException if there's an I/O problem
	 */
	public void writeBeginComponent(String componentName) throws IOException {
		writeProperty("BEGIN", componentName);
	}

	/**
	 * Writes a property marking the end of a component (in other words, writes
	 * a "END:NAME" property).
	 * @param componentName the component name (e.g. "VEVENT")
	 * @throws IOException if there's an I/O problem
	 */
	public void writeEndComponent(String componentName) throws IOException {
		writeProperty("END", componentName);
	}

	/**
	 * Writes a "VERSION" property, based on the iCalendar version that the
	 * writer is adhering to.
	 * @throws IOException if there's an I/O problem
	 */
	public void writeVersion() throws IOException {
		writeProperty("VERSION", version.getVersion());
	}

	/**
	 * Writes a property to the iCalendar data stream.
	 * @param propertyName the property name (e.g. "VERSION")
	 * @param value the property value (e.g. "2.0")
	 * @throws IllegalArgumentException if the property name contains invalid
	 * characters
	 * @throws IOException if there's an I/O problem
	 */
	public void writeProperty(String propertyName, String value) throws IOException {
		writeProperty(propertyName, new ICalParameters(), value);
	}

	/**
	 * Writes a property to the iCalendar data stream.
	 * @param propertyName the property name (e.g. "VERSION")
	 * @param parameters the property parameters
	 * @param value the property value (e.g. "2.0")
	 * @throws IllegalArgumentException if the property name contains invalid
	 * characters
	 * @throws IOException if there's an I/O problem
	 */
	public void writeProperty(String propertyName, ICalParameters parameters, String value) throws IOException {
		//validate the property name
		if (!propertyNameRegex.matcher(propertyName).matches()) {
			throw new IllegalArgumentException("Property name invalid.  Property names can only contain letters, numbers, and hyphens.");
		}

		value = sanitizeValue(parameters, value);

		//determine if the property value must be encoded in quoted printable
		//and determine the charset to use when encoding to quoted-printable
		boolean quotedPrintable = (parameters.getEncoding() == Encoding.QUOTED_PRINTABLE);
		Charset charset = null;
		if (quotedPrintable) {
			String charsetParam = parameters.getCharset();
			if (charsetParam == null) {
				charset = Charset.forName("UTF-8");
			} else {
				try {
					charset = Charset.forName(charsetParam);
				} catch (Throwable t) {
					charset = Charset.forName("UTF-8");
				}
			}
			parameters.setCharset(charset.name());
		}

		//write the property name
		writer.append(propertyName);

		//write the parameters
		for (Map.Entry<String, List<String>> subType : parameters) {
			String parameterName = subType.getKey();
			List<String> parameterValues = subType.getValue();
			if (parameterValues.isEmpty()) {
				continue;
			}

			if (version == ICalVersion.V1_0) {
				//e.g. ADR;FOO=bar;FOO=car:
				for (String parameterValue : parameterValues) {
					parameterValue = sanitizeParameterValue(parameterValue, parameterName, propertyName);
					writer.append(';').append(parameterName).append('=').append(parameterValue);
				}
				continue;
			}

			//e.g. ADR;TYPE=home,work,"another,value":
			boolean first = true;
			writer.append(';').append(parameterName).append('=');
			for (String parameterValue : parameterValues) {
				if (!first) {
					writer.append(',');
				}

				parameterValue = sanitizeParameterValue(parameterValue, parameterName, propertyName);

				//surround with double quotes if contains special chars
				if (quoteMeRegex.matcher(parameterValue).matches()) {
					writer.append('"');
					writer.append(parameterValue);
					writer.append('"');
				} else {
					writer.append(parameterValue);
				}

				first = false;
			}
		}

		writer.append(':');

		//write the property value
		writer.append(value, quotedPrintable, charset);
		writer.append(writer.getNewline());
	}

	/**
	 * Sanitizes a property value for safe inclusion in a vCard.
	 * @param parameters the parameters
	 * @param value the value to sanitize
	 * @return the sanitized value
	 */
	private String sanitizeValue(ICalParameters parameters, String value) {
		if (value == null) {
			return "";
		}

		if (version == ICalVersion.V1_0 && containsNewlines(value)) {
			//1.0 does not support the "\n" escape sequence (see "Delimiters" sub-section in section 2 of the specs)
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			return value;
		}

		return escapeNewlines(value);
	}

	/**
	 * Removes or escapes all invalid characters in a parameter value.
	 * @param parameterValue the parameter value
	 * @param parameterName the parameter name
	 * @param propertyName the name of the property to which the parameter
	 * belongs
	 * @return the sanitized parameter value
	 */
	private String sanitizeParameterValue(String parameterValue, String parameterName, String propertyName) {
		//remove invalid characters
		parameterValue = removeInvalidParameterValueChars(parameterValue);

		switch (version) {
		case V1_0:
			//replace newlines with spaces
			parameterValue = newlineRegex.matcher(parameterValue).replaceAll(" ");

			//escape backslashes
			parameterValue = parameterValue.replace("\\", "\\\\");

			//escape semi-colons (see section 2)
			parameterValue = parameterValue.replace(";", "\\;");

			break;

		default:
			if (caretEncodingEnabled) {
				//apply caret encoding
				parameterValue = applyCaretEncoding(parameterValue);
			} else {
				//replace double quotes with single quotes
				parameterValue = parameterValue.replace('"', '\'');

				//replace newlines with spaces
				parameterValue = newlineRegex.matcher(parameterValue).replaceAll(" ");
			}

			break;
		}

		return parameterValue;
	}

	/**
	 * Removes invalid characters from a parameter value.
	 * @param value the parameter value
	 * @return the sanitized parameter value
	 */
	private String removeInvalidParameterValueChars(String value) {
		BitSet invalidChars = invalidParamValueChars.get(version);
		StringBuilder sb = null;

		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (invalidChars.get(ch)) {
				if (sb == null) {
					sb = new StringBuilder(value.length());
					sb.append(value.substring(0, i));
				}
				continue;
			}

			if (sb != null) {
				sb.append(ch);
			}
		}

		return (sb == null) ? value : sb.toString();
	}

	/**
	 * Applies circumflex accent encoding to a string.
	 * @param value the string
	 * @return the encoded string
	 */
	private String applyCaretEncoding(String value) {
		value = value.replace("^", "^^");
		value = newlineRegex.matcher(value).replaceAll("^n");
		value = value.replace("\"", "^'");
		return value;
	}

	/**
	 * Escapes all newline characters.
	 * <p>
	 * This method escapes the following newline sequences:
	 * </p>
	 * <ul>
	 * <li>{@code \r\n}</li>
	 * <li>{@code \r}</li>
	 * <li>{@code \n}</li>
	 * </ul>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	private String escapeNewlines(String text) {
		return newlineRegex.matcher(text).replaceAll("\\\\n");
	}

	/**
	 * <p>
	 * Determines if a string has at least one newline character sequence. The
	 * newline character sequences are:
	 * </p>
	 * <ul>
	 * <li>{@code \r\n}</li>
	 * <li>{@code \r}</li>
	 * <li>{@code \n}</li>
	 * </ul>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	private boolean containsNewlines(String text) {
		return newlineRegex.matcher(text).find();
	}

	/**
	 * Flushes the underlying {@link Writer} object.
	 * @throws IOException if there's a problem flushing the writer
	 */
	public void flush() throws IOException {
		writer.flush();
	}

	/**
	 * Closes the underlying {@link Writer} object.
	 */
	public void close() throws IOException {
		writer.close();
	}
}
