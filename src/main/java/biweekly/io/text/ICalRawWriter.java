package biweekly.io.text;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import biweekly.ICalVersion;
import biweekly.Messages;
import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;
import biweekly.util.CharacterBitSet;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
 * @see <a href="http://www.imc.org/pdi/pdiproddev.html">1.0 specs</a>
 * @see <a href="https://tools.ietf.org/html/rfc2445">RFC 2445</a>
 * @see <a href="http://tools.ietf.org/html/rfc5545">RFC 5545</a>
 */
public class ICalRawWriter implements Closeable, Flushable {
	/**
	 * If any of these characters are found within a parameter value, then the
	 * entire parameter value must be wrapped in double quotes (applies to
	 * version 2.0 only).
	 */
	private final CharacterBitSet specialParameterCharacters = new CharacterBitSet(",:;");

	/**
	 * Regular expression used to detect newline character sequences.
	 */
	private final Pattern newlineRegex = Pattern.compile("\\r\\n|\\r|\\n");
	private final CharacterBitSet newlineBitSet = new CharacterBitSet("\r\n");

	/**
	 * List of characters which would break the syntax of the iCalendar object
	 * if used inside a property name. The list of characters permitted by the
	 * specification is much more strict, but the goal here is to be as lenient
	 * as possible.
	 */
	private final CharacterBitSet invalidPropertyNameCharacters = new CharacterBitSet(";:\n\r");

	/**
	 * List of characters which would break the syntax of the iCalendar object
	 * if used inside a parameter value with caret encoding disabled. These
	 * characters cannot be escaped or encoded, so they are impossible to
	 * include inside of a parameter value. The list of characters permitted by
	 * the specification is much more strict, but the goal here is to be as
	 * lenient as possible.
	 */
	private final Map<ICalVersion, CharacterBitSet> invalidParamValueChars;
	{
		Map<ICalVersion, CharacterBitSet> map = new EnumMap<ICalVersion, CharacterBitSet>(ICalVersion.class);

		map.put(ICalVersion.V1_0, new CharacterBitSet(",:\n\r")); //note: semicolons can be escaped
		map.put(ICalVersion.V2_0_DEPRECATED, new CharacterBitSet("\"\r\n"));
		map.put(ICalVersion.V2_0, map.get(ICalVersion.V2_0_DEPRECATED));

		invalidParamValueChars = Collections.unmodifiableMap(map);
	}

	/**
	 * List of characters which would break the syntax of the iCalendar object
	 * if used inside a parameter value with caret encoding enabled. These
	 * characters cannot be escaped or encoded, so they are impossible to
	 * include inside of a parameter value. The list of characters permitted by
	 * the specification is much more strict, but the goal here is to be as
	 * lenient as possible.
	 */
	private final Map<ICalVersion, CharacterBitSet> invalidParamValueCharsWithCaretEncoding;
	{
		Map<ICalVersion, CharacterBitSet> map = new EnumMap<ICalVersion, CharacterBitSet>(ICalVersion.class);

		map.put(ICalVersion.V1_0, invalidParamValueChars.get(ICalVersion.V1_0)); //1.0 does not support caret encoding
		map.put(ICalVersion.V2_0_DEPRECATED, new CharacterBitSet(""));
		map.put(ICalVersion.V2_0, map.get(ICalVersion.V2_0_DEPRECATED));

		invalidParamValueCharsWithCaretEncoding = Collections.unmodifiableMap(map);
	}

	private final FoldedLineWriter writer;
	private boolean caretEncodingEnabled = false;
	private ICalVersion version;

	/**
	 * @param writer the writer to wrap
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
	 * parameter values (disabled by default, only applies to version 2.0). This
	 * escaping mechanism allows for newlines and double quotes to be included
	 * in parameter values.
	 * </p>
	 * 
	 * <p>
	 * Note that this encoding mechanism is defined separately from the
	 * iCalendar specification and may not be supported by the iCalendar
	 * consumer.
	 * </p>
	 * 
	 * <table class="simpleTable">
	 * <tr>
	 * <th>Raw Character</th>
	 * <th>Encoded Character</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
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
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPittsburgh, PA 15212":40.446816;80.00566
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
	 * parameter values (disabled by default, only applies to version 2.0). This
	 * escaping mechanism allows for newlines and double quotes to be included
	 * in parameter values.
	 * </p>
	 * 
	 * <p>
	 * Note that this encoding mechanism is defined separately from the
	 * iCalendar specification and may not be supported by the iCalendar
	 * consumer.
	 * </p>
	 * 
	 * <table class="simpleTable">
	 * <tr>
	 * <th>Raw Character</th>
	 * <th>Encoded Character</th>
	 * </tr>
	 * <tr>
	 * <td>{@code "}</td>
	 * <td>{@code ^'}</td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td>{@code ^n}</td>
	 * </tr>
	 * <tr>
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
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPittsburgh, PA 15212":40.446816;80.00566
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
	 * Writes a property marking the beginning of a component.
	 * @param componentName the component name (e.g. "VEVENT")
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeBeginComponent(String componentName) throws IOException {
		writeProperty("BEGIN", componentName);
	}

	/**
	 * Writes a property marking the end of a component.
	 * @param componentName the component name (e.g. "VEVENT")
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeEndComponent(String componentName) throws IOException {
		writeProperty("END", componentName);
	}

	/**
	 * Writes a "VERSION" property, setting its value to iCalendar version that
	 * this writer is adhering to.
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeVersion() throws IOException {
		writeProperty("VERSION", version.getVersion());
	}

	/**
	 * Writes a property to the iCalendar data stream.
	 * @param propertyName the property name (e.g. "SUMMARY")
	 * @param value the property value
	 * @throws IllegalArgumentException if the property data contains one or
	 * more characters which break the iCalendar syntax and which cannot be
	 * escaped or encoded
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeProperty(String propertyName, String value) throws IOException {
		writeProperty(propertyName, new ICalParameters(), value);
	}

	/**
	 * Writes a property to the iCalendar data stream.
	 * @param propertyName the property name (e.g. "SUMMARY")
	 * @param parameters the property parameters
	 * @param value the property value
	 * @throws IllegalArgumentException if the property data contains one or
	 * more characters which break the iCalendar syntax and which cannot be
	 * escaped or encoded
	 * @throws IOException if there's a problem writing to the data stream
	 */
	public void writeProperty(String propertyName, ICalParameters parameters, String value) throws IOException {
		//validate the property name
		if (invalidPropertyNameCharacters.containsAny(propertyName)) {
			throw Messages.INSTANCE.getIllegalArgumentException(8, propertyName, printableCharacterList(invalidPropertyNameCharacters.characters()));
		}
		if (beginsWithWhitespace(propertyName)) {
			throw Messages.INSTANCE.getIllegalArgumentException(9, propertyName);
		}

		value = sanitizeValue(parameters, value);

		/*
		 * Determine if the property value must be encoded in quoted printable
		 * encoding. If so, then determine what charset to use for the encoding.
		 */
		boolean useQuotedPrintable = (parameters.getEncoding() == Encoding.QUOTED_PRINTABLE);
		Charset quotedPrintableCharset = null;
		if (useQuotedPrintable) {
			String charsetParam = parameters.getCharset();
			if (charsetParam == null) {
				quotedPrintableCharset = Charset.forName("UTF-8");
			} else {
				try {
					quotedPrintableCharset = Charset.forName(charsetParam);
				} catch (Throwable t) {
					quotedPrintableCharset = Charset.forName("UTF-8");
				}
			}
			parameters.setCharset(quotedPrintableCharset.name());
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
				if (specialParameterCharacters.containsAny(parameterValue)) {
					writer.append('"').append(parameterValue).append('"');
				} else {
					writer.append(parameterValue);
				}

				first = false;
			}
		}

		writer.append(':');

		//write the property value
		writer.append(value, useQuotedPrintable, quotedPrintableCharset);
		writer.append(writer.getNewline());
	}

	/**
	 * Determines if a given string starts with whitespace.
	 * @param string the string
	 * @return true if it starts with whitespace, false if not
	 */
	private boolean beginsWithWhitespace(String string) {
		if (string.length() == 0) {
			return false;
		}
		char first = string.charAt(0);
		return (first == ' ' || first == '\t');
	}

	/**
	 * Sanitizes a property value for safe inclusion in an iCalendar object.
	 * @param parameters the property's parameters
	 * @param value the value to sanitize
	 * @return the sanitized value
	 */
	private String sanitizeValue(ICalParameters parameters, String value) {
		if (value == null) {
			return "";
		}

		if (version == ICalVersion.V1_0 && newlineBitSet.containsAny(value)) {
			/*
			 * 1.0 does not support the "\n" escape sequence (see "Delimiters"
			 * sub-section in section 2 of the specs) so encode the value in
			 * quoted-printable encoding if any newline characters exist.
			 */
			parameters.setEncoding(Encoding.QUOTED_PRINTABLE);
			return value;
		}

		return escapeNewlines(value);
	}

	/**
	 * Sanitizes a parameter value for serialization.
	 * @param parameterValue the parameter value
	 * @param parameterName the parameter name
	 * @param propertyName the name of the property to which the parameter
	 * belongs
	 * @return the sanitized parameter value
	 * @throws IllegalArgumentException if the value contains invalid characters
	 */
	private String sanitizeParameterValue(String parameterValue, String parameterName, String propertyName) {
		CharacterBitSet invalidChars = (caretEncodingEnabled ? invalidParamValueCharsWithCaretEncoding : invalidParamValueChars).get(version);
		if (invalidChars.containsAny(parameterValue)) {
			throw Messages.INSTANCE.getIllegalArgumentException(10, propertyName, parameterName, printableCharacterList(invalidChars.characters()));
		}

		String sanitizedValue = parameterValue;
		switch (version) {
		case V1_0:
			//Note: 1.0 does not support caret encoding.

			//escape backslashes
			sanitizedValue = sanitizedValue.replace("\\", "\\\\");

			//escape semi-colons (see section 2)
			return sanitizedValue.replace(";", "\\;");

		default:
			if (caretEncodingEnabled) {
				return applyCaretEncoding(sanitizedValue);
			}
			return sanitizedValue;
		}
	}

	private String printableCharacterList(String list) {
		return list.replace("\n", "\\n").replace("\r", "\\r");
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
	 * Escapes all newlines in a string.
	 * @param string the string to escape
	 * @return the escaped string
	 */
	private String escapeNewlines(String string) {
		return newlineRegex.matcher(string).replaceAll("\\\\n");
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
	 * @throws IOException if there's a problem closing the writer
	 */
	public void close() throws IOException {
		writer.close();
	}
}
