package biweekly.io.text;

import java.io.Closeable;
import java.io.IOException;
import java.io.Writer;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
 */
public class ICalRawWriter implements Closeable {
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
	private static final BitSet invalidParamValueChars;
	static {
		invalidParamValueChars = new BitSet(128);
		invalidParamValueChars.set(0, 31);
		invalidParamValueChars.set(127);
		invalidParamValueChars.set('\t', false); //allow
		invalidParamValueChars.set('\n', false); //allow
		invalidParamValueChars.set('\r', false); //allow
	}

	private final String newline;
	private boolean caretEncodingEnabled = false;
	private final FoldingScheme foldingScheme;
	private final Writer writer;
	private ParameterValueChangedListener parameterValueChangedListener;

	/**
	 * Creates an iCalendar raw writer using the standard folding scheme and
	 * newline sequence.
	 * @param writer the writer to the data stream
	 */
	public ICalRawWriter(Writer writer) {
		this(writer, FoldingScheme.DEFAULT);
	}

	/**
	 * Creates an iCalendar raw writer using the standard newline sequence.
	 * @param writer the writer to the data stream
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 */
	public ICalRawWriter(Writer writer, FoldingScheme foldingScheme) {
		this(writer, foldingScheme, "\r\n");
	}

	/**
	 * Creates an iCalendar raw writer.
	 * @param writer the writer to the data stream
	 * @param foldingScheme the folding scheme to use or null not to fold at all
	 * @param newline the newline sequence to use
	 */
	public ICalRawWriter(Writer writer, FoldingScheme foldingScheme, String newline) {
		if (foldingScheme == null) {
			this.writer = writer;
		} else {
			this.writer = new FoldedLineWriter(writer, foldingScheme.getLineLength(), foldingScheme.getIndent(), newline);
		}
		this.foldingScheme = foldingScheme;
		this.newline = newline;
	}

	/**
	 * <p>
	 * Gets whether the writer will use circumflex accent encoding. This
	 * escaping mechanism allows for newlines and double quotes to be included
	 * in parameter values.
	 * </p>
	 * 
	 * <p>
	 * This setting is <b>disabled</b> by default. When disabled, the writer
	 * will replace newlines with spaces and double quotes with single quotes.
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
	 * <td><code>"</code></td>
	 * <td><code>'</code></td>
	 * <td><code>^'</code></td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td><code><i>space</i></code></td>
	 * <td><code>^n</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>^</code></td>
	 * <td><code>^</code></td>
	 * <td><code>^^</code></td>
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
	 * Sets whether the writer will use circumflex accent encoding. This
	 * escaping mechanism allows for newlines and double quotes to be included
	 * in parameter values.
	 * </p>
	 * 
	 * <p>
	 * This setting is <b>disabled</b> by default. When disabled, the writer
	 * will replace newlines with spaces and double quotes with single quotes.
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
	 * <td><code>"</code></td>
	 * <td><code>'</code></td>
	 * <td><code>^'</code></td>
	 * </tr>
	 * <tr>
	 * <td><i>newline</i></td>
	 * <td><code><i>space</i></code></td>
	 * <td><code>^n</code></td>
	 * </tr>
	 * <tr>
	 * <td><code>^</code></td>
	 * <td><code>^</code></td>
	 * <td><code>^^</code></td>
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
	 * Gets the newline sequence that is used to separate lines.
	 * @return the newline sequence
	 */
	public String getNewline() {
		return newline;
	}

	/**
	 * Gets the listener which will be invoked when a parameter's value is
	 * changed due to containing invalid characters.
	 * @return the listener or null if not set
	 */
	public ParameterValueChangedListener getParameterValueChangedListener() {
		return parameterValueChangedListener;
	}

	/**
	 * Sets the listener which will be invoked when a parameter's value is
	 * changed due to containing invalid characters.
	 * @param parameterValueChangedListener the listener or null to remove
	 */
	public void setParameterValueChangedListener(ParameterValueChangedListener parameterValueChangedListener) {
		this.parameterValueChangedListener = parameterValueChangedListener;
	}

	/**
	 * Gets the rules for how each line is folded.
	 * @return the folding scheme or null if the lines are not folded
	 */
	public FoldingScheme getFoldingScheme() {
		return foldingScheme;
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

		//write the property name
		writer.append(propertyName);

		//write the parameters
		for (Map.Entry<String, List<String>> subType : parameters) {
			String parameterName = subType.getKey();
			List<String> parameterValues = subType.getValue();
			if (!parameterValues.isEmpty()) {
				//e.g. ADR;TYPE=home,work,"another,value":

				boolean first = true;
				writer.append(';').append(parameterName).append('=');
				for (String subTypeValue : parameterValues) {
					if (!first) {
						writer.append(',');
					}

					subTypeValue = sanitizeParameterValue(subTypeValue, parameterName, propertyName);

					//surround with double quotes if contains special chars
					if (quoteMeRegex.matcher(subTypeValue).matches()) {
						writer.append('"');
						writer.append(subTypeValue);
						writer.append('"');
					} else {
						writer.append(subTypeValue);
					}

					first = false;
				}
			}
		}

		writer.append(':');

		//write the property value
		if (value == null) {
			value = "";
		} else {
			value = escapeNewlines(value);
		}
		writer.append(value);

		writer.append(newline);
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
		boolean valueChanged = false;
		String modifiedValue = removeInvalidParameterValueChars(parameterValue);

		if (caretEncodingEnabled) {
			valueChanged = (modifiedValue != parameterValue);
			modifiedValue = applyCaretEncoding(modifiedValue);
		} else {
			//replace double quotes with single quotes
			modifiedValue = modifiedValue.replace('"', '\'');

			//replace newlines with spaces
			modifiedValue = newlineRegex.matcher(modifiedValue).replaceAll(" ");

			valueChanged = (modifiedValue != parameterValue);
		}

		if (valueChanged && parameterValueChangedListener != null) {
			parameterValueChangedListener.onParameterValueChanged(propertyName, parameterName, parameterValue, modifiedValue);
		}

		return modifiedValue;
	}

	/**
	 * Removes invalid characters from a parameter value.
	 * @param value the parameter value
	 * @return the sanitized parameter value
	 */
	private String removeInvalidParameterValueChars(String value) {
		StringBuilder sb = new StringBuilder(value.length());

		for (int i = 0; i < value.length(); i++) {
			char ch = value.charAt(i);
			if (!invalidParamValueChars.get(ch)) {
				sb.append(ch);
			}
		}

		return (sb.length() == value.length()) ? value : sb.toString();
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
	 * <li><code>\r\n</code></li>
	 * <li><code>\r</code></li>
	 * <li><code>\n</code></li>
	 * </ul>
	 * @param text the text to escape
	 * @return the escaped text
	 */
	private String escapeNewlines(String text) {
		return newlineRegex.matcher(text).replaceAll("\\\\n");
	}

	/**
	 * Closes the underlying {@link Writer} object.
	 */
	public void close() throws IOException {
		writer.close();
	}

	/**
	 * Allows you to respond to when a parameter's value is changed due to it
	 * containing invalid characters. If a character can be escaped (such as the
	 * "^" character when caret encoding is enabled), then this does not count
	 * as the parameter being modified because it can be decoded without losing
	 * any information.
	 * @author Michael Angstadt
	 */
	public static interface ParameterValueChangedListener {
		/**
		 * Called when a parameter value is changed.
		 * @param propertyName the name of the property to which the parameter
		 * belongs
		 * @param parameterName the parameter name
		 * @param originalValue the original parameter value
		 * @param modifiedValue the modified parameter value
		 */
		void onParameterValueChanged(String propertyName, String parameterName, String originalValue, String modifiedValue);
	}
}
