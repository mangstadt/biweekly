package biweekly.util;

import java.util.Base64;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
 * Helper class for dealing with strings.
 * @author Michael Angstadt
 */
public final class StringUtils {
	/**
	 * The local computer's newline character sequence.
	 */
	public static final String NEWLINE = System.getProperty("line.separator");

	/**
	 * <p>
	 * Returns a substring of the given string that comes after the given
	 * prefix. Prefix matching is case-insensitive.
	 * </p>
	 * <p>
	 * <b>Example:</b>
	 * </p>
	 * 
	 * <pre class="brush:java">
	 * String result = StringUtils.afterPrefixIgnoreCase("MAILTO:email@example.com", "mailto:");
	 * assertEquals("email@example.com", result);
	 * 
	 * result = StringUtils.afterPrefixIgnoreCase("http://www.google.com", "mailto:");
	 * assertNull(result);
	 * </pre>
	 * 
	 * @param string the string
	 * @param prefix the prefix
	 * @return the string or null if the prefix was not found
	 */
	public static String afterPrefixIgnoreCase(String string, String prefix) {
		return startsWithIgnoreCase(string, prefix) ? string.substring(prefix.length()) : null;
	}

	/**
	 * Creates a string consisting of "count" occurrences of char "c".
	 * @param c the character to repeat
	 * @param count the number of times to repeat the character
	 * @param sb the character sequence to append the characters to
	 */
	public static void repeat(char c, int count, StringBuilder sb) {
		for (int i = 0; i < count; i++) {
			sb.append(c);
		}
	}

	/**
	 * Like {@link String#startsWith}, but using case-insensitive comparison.
	 * @param str the string
	 * @param prefix the prefix
	 * @return true if the string begins with the given prefix using
	 * case-insensitive comparison, false if not
	 */
	public static boolean startsWithIgnoreCase(String str, String prefix) {
		if (str.length() < prefix.length()) {
			return false;
		}

		return str.substring(0, prefix.length()).equalsIgnoreCase(prefix);
	}

	/**
	 * <p>
	 * Decodes a base64 string, silently ignoring all invalid characters.
	 * </p>
	 * <p>
	 * Some iCal producers use multiple whitespace characters to indent folded
	 * base64 values. According to the 2.0 specs, these extra characters are to
	 * be considered part of the property value, so whitespace characters could
	 * be scattered throughout the string.
	 * </p>
	 * <p>
	 * Whitespace characters are not valid base64 characters.
	 * {@link Base64#getMimeDecoder} returns a decoder that silently ignores all
	 * invalid characters. It can still throw an exception for other reasons
	 * though.
	 * </p>
	 * <p>
	 * See the {@link Base64} class javadoc for descriptions of the different
	 * decoders.
	 * </p>
	 * @param encoded the base64-encoded value
	 * @return the decoded data
	 * @throws IllegalArgumentException if there is a problem decoding the value
	 */
	public static byte[] decodeBase64Lenient(String encoded) throws IllegalArgumentException {
		return Base64.getMimeDecoder().decode(encoded);
	}

	/**
	 * Encodes the given data in base64.
	 * @param data the data
	 * @return the base64 string
	 */
	public static String encodeBase64(byte[] data) {
		return Base64.getEncoder().encodeToString(data);
	}

	private StringUtils() {
		//hide
	}
}
