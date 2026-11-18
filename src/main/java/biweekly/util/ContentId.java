package biweekly.util;

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
 * Parses and writes content ID values.
 * @author Michael Angstadt
 */
public final class ContentId {
	private static final String SCHEME = "cid:";

	/**
	 * Converts the content ID to a URI
	 * @param contentId the content ID
	 * @return the URI
	 */
	public static String writeUri(String contentId) {
		return SCHEME + contentId;
	}

	/**
	 * Formats the content ID in old-style syntax.
	 * @param contentId the content ID
	 * @return the formatted value
	 */
	public static String writeOldStyle(String contentId) {
		return '<' + contentId + '>';
	}

	/**
	 * Parses the content ID out of the given URI.
	 * @param uri the URI
	 * @return the content ID or null if the URI is not a CID URI
	 */
	public static String parseUri(String uri) {
		return StringUtils.afterPrefixIgnoreCase(uri, SCHEME);
	}

	/**
	 * Parses the content ID out of the given string. Accounts for URIs and
	 * old-style syntax.
	 * @param s the string
	 * @return the content ID or null if not found
	 */
	public static String parse(String s) {
		String contentId = parseUri(s);
		if (contentId != null) {
			return contentId;
		}

		if (!s.isEmpty() && s.charAt(0) == '<' && s.charAt(s.length() - 1) == '>') {
			return s.substring(1, s.length() - 1);
		}

		return null;
	}

	private ContentId() {
		//hide
	}
}