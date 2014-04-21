package biweekly.io.text;

import static biweekly.util.StringUtils.NEWLINE;

import java.io.Closeable;
import java.io.IOException;
import java.io.Reader;

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
 * Parses an iCalendar data stream.
 * @author Michael Angstadt
 * @rfc 5545
 */
public class ICalRawReader implements Closeable {
	private final FoldedLineReader reader;
	private boolean caretDecodingEnabled = true;

	/**
	 * Creates a new reader.
	 * @param reader the reader to the data stream
	 */
	public ICalRawReader(Reader reader) {
		this.reader = new FoldedLineReader(reader);
	}

	/**
	 * Gets the line number of the last line that was read.
	 * @return the line number
	 */
	public int getLineNum() {
		return reader.getLineNum();
	}

	/**
	 * Parses the next line of the iCalendar file.
	 * @return the next line or null if there are no more lines
	 * @throws ICalParseException if a line cannot be parsed
	 * @throws IOException if there's a problem reading from the input stream
	 */
	public ICalRawLine readLine() throws IOException {
		String line = reader.readLine();
		if (line == null) {
			return null;
		}

		String propertyName = null;
		ICalParameters parameters = new ICalParameters();
		String value = null;

		char escapeChar = 0; //is the next char escaped?
		boolean inQuotes = false; //are we inside of double quotes?
		StringBuilder buffer = new StringBuilder();
		String curParamName = null;
		for (int i = 0; i < line.length(); i++) {
			char ch = line.charAt(i);

			if (escapeChar != 0) {
				//this character was escaped
				if (escapeChar == '\\') {
					//backslash escaping in parameter values is not part of the standard
					if (ch == '\\') {
						buffer.append(ch);
					} else if (ch == 'n' || ch == 'N') {
						//newlines
						buffer.append(NEWLINE);
					} else if (ch == '"') {
						//incase a double quote is escaped with a backslash
						buffer.append(ch);
					} else {
						//treat the escape character as a normal character because it's not a valid escape sequence
						buffer.append(escapeChar).append(ch);
					}
				} else if (escapeChar == '^') {
					if (ch == '^') {
						buffer.append(ch);
					} else if (ch == 'n') {
						buffer.append(NEWLINE);
					} else if (ch == '\'') {
						buffer.append('"');
					} else {
						//treat the escape character as a normal character because it's not a valid escape sequence
						buffer.append(escapeChar).append(ch);
					}
				}
				escapeChar = 0;
				continue;
			}

			if (ch == '\\' || (ch == '^' && caretDecodingEnabled)) {
				//an escape character was read
				escapeChar = ch;
				continue;
			}

			if ((ch == ';' || ch == ':') && !inQuotes) {
				if (propertyName == null) {
					//property name
					propertyName = buffer.toString();
				} else if (curParamName == null) {
					//value-less parameter (bad iCal syntax)
					String parameterName = buffer.toString();
					parameters.put(parameterName, null);
				} else {
					//parameter value
					String paramValue = buffer.toString();
					parameters.put(curParamName, paramValue);
					curParamName = null;
				}
				buffer.setLength(0);

				if (ch == ':') {
					//the rest of the line is the property value
					if (i < line.length() - 1) {
						value = line.substring(i + 1);
					} else {
						value = "";
					}
					break;
				}
				continue;
			}

			if (ch == ',' && !inQuotes) {
				//multi-valued parameter
				parameters.put(curParamName, buffer.toString());
				buffer.setLength(0);
				continue;
			}

			if (ch == '=' && curParamName == null) {
				//parameter name
				curParamName = buffer.toString();
				buffer.setLength(0);
				continue;
			}

			if (ch == '"') {
				inQuotes = !inQuotes;
				continue;
			}

			buffer.append(ch);
		}

		if (propertyName == null || value == null) {
			throw new ICalParseException(line);
		}

		return new ICalRawLine(propertyName, parameters, value);
	}

	/**
	 * <p>
	 * Gets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * </p>
	 * 
	 * <table border="1">
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
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPitt
	 *  sburgh, PA 15212":40.446816;80.00566
	 * </pre>
	 * 
	 * @return true if circumflex accent decoding is enabled, false if not
	 * @rfc 6868
	 */
	public boolean isCaretDecodingEnabled() {
		return caretDecodingEnabled;
	}

	/**
	 * <p>
	 * Sets whether the reader will decode parameter values that use circumflex
	 * accent encoding (enabled by default). This escaping mechanism allows
	 * newlines and double quotes to be included in parameter values.
	 * </p>
	 * 
	 * <table border="1">
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
	 * GEO;X-ADDRESS="Pittsburgh Pirates^n115 Federal St^nPitt
	 *  sburgh, PA 15212":geo:40.446816,-80.00566
	 * </pre>
	 * 
	 * @param enable true to use circumflex accent decoding, false not to
	 * @rfc 6868
	 */
	public void setCaretDecodingEnabled(boolean enable) {
		caretDecodingEnabled = enable;
	}

	/**
	 * Closes the underlying {@link Reader} object.
	 */
	public void close() throws IOException {
		reader.close();
	}
}
