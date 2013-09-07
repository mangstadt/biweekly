package biweekly.javadoc;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;
import com.sun.tools.doclets.standard.Standard;

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
 * Taglet for defining references to RFC documents. Builds an HTML link that
 * points to the HTML version of the specified document.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre>
 * /**
 *  * @rfc 5545 p.45-47
 *  * @rfc 5545 p.45
 *  * @rfc 5545 4.8.8
 *  * @rfc 5545
 *  &#42;/
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a
 * href="http://docs.oracle.com/javase/1.4.2/docs/tooldocs/javadoc/taglet/overview.html">Taglet
 * tutorial</a>
 */
public class RfcTaglet implements Taglet {
	/**
	 * The HTML to write when only a start page is given.
	 */
	private static final String startFormat = "<a href=\"http://tools.ietf.org/html/rfc%s#page-%s\">RFC %s p.%s</a>";

	/**
	 * The HTML to write when a start and end page is given.
	 */
	private static final String startEndFormat = "<a href=\"http://tools.ietf.org/html/rfc%s#page-%s\">RFC %s p.%s-%s</a>";

	/**
	 * The HTML to write when a section is given.
	 */
	private static final String sectionFormat = "<a href=\"http://tools.ietf.org/html/rfc%s#section-%s\">RFC %s, Section %s</a>";

	/**
	 * The HTML to write when a section is given.
	 */
	private static final String numberFormat = "<a href=\"http://tools.ietf.org/html/rfc%s\">RFC %s</a>";

	/**
	 * Regular expression for parsing a taglet value that contains a page range.
	 */
	private static final Pattern pageRegex = Pattern.compile("^(\\d+) p\\.(\\d+)(-(\\d+))?$", Pattern.CASE_INSENSITIVE);

	/**
	 * Regular expression for parsing a taglet value that contains a section.
	 */
	private static final Pattern sectionRegex = Pattern.compile("^(\\d+) ([\\.\\d]+)$", Pattern.CASE_INSENSITIVE);

	/**
	 * Regular expression for parsing a taglet value that only contains the RFC
	 * number.
	 */
	private static final Pattern numberRegex = Pattern.compile("^(\\d+)$", Pattern.CASE_INSENSITIVE);

	public String getName() {
		return "rfc";
	}

	public boolean inField() {
		return true;
	}

	public boolean inConstructor() {
		return true;
	}

	public boolean inMethod() {
		return true;
	}

	public boolean inOverview() {
		return true;
	}

	public boolean inPackage() {
		return true;
	}

	public boolean inType() {
		return true;
	}

	public boolean isInlineTag() {
		return false;
	}

	public static void register(Map<String, Taglet> tagletMap) {
		RfcTaglet tag = new RfcTaglet();
		Taglet t = tagletMap.get(tag.getName());
		if (t != null) {
			tagletMap.remove(tag.getName());
		}
		tagletMap.put(tag.getName(), tag);
	}

	public String toString(Tag tag) {
		return toString(new Tag[] { tag });
	}

	public String toString(Tag[] tags) {
		if (tags.length == 0) {
			return "";
		}

		StringBuilder sb = new StringBuilder();

		sb.append("<dt><b>Specification Reference:</b></dt>");
		for (Tag tag : tags) {
			String text = tag.text();

			Ref ref = Ref.parse(text);
			if (ref == null) {
				Standard.htmlDoclet.configuration().root.printWarning(tag.position(), "Skipping @rfc taglet whose text is incorrectly formatted.");
				continue;
			}

			sb.append("<dd>");

			if (ref.section == null && ref.pageStart == null && ref.pageStop == null) {
				sb.append(String.format(numberFormat, ref.number, ref.number));
			} else if (ref.section == null) {
				if (ref.pageStop == null) {
					sb.append(String.format(startFormat, ref.number, ref.pageStart, ref.number, ref.pageStart));
				} else {
					sb.append(String.format(startEndFormat, ref.number, ref.pageStart, ref.number, ref.pageStart, ref.pageStop));
				}
			} else {
				sb.append(String.format(sectionFormat, ref.number, ref.section, ref.number, ref.section));
			}

			sb.append("</dd>");
		}

		return sb.toString();
	}

	private static class Ref {
		private final String number;
		private final String section;
		private final Integer pageStart, pageStop;

		private Ref(String number, String section, Integer pageStart, Integer pageStop) {
			this.number = number;
			this.section = section;
			this.pageStart = pageStart;
			this.pageStop = pageStop;
		}

		private static Ref parse(String tagText) {
			Matcher m = pageRegex.matcher(tagText);
			if (m.find()) {
				return parseAsPage(m);
			}

			m = sectionRegex.matcher(tagText);
			if (m.find()) {
				return parseAsSection(m);
			}

			m = numberRegex.matcher(tagText);
			if (m.find()) {
				return parseAsNumber(m);
			}

			return null;
		}

		private static Ref parseAsPage(Matcher m) {
			String number = m.group(1);

			String pageStartStr = m.group(2);
			Integer pageStart = Integer.parseInt(pageStartStr); //NumberFormatException should never be thrown because the regex looks for numbers only

			String pageStopStr = m.group(4);
			Integer pageStop = null;
			if (pageStopStr != null) {
				pageStop = Integer.parseInt(pageStopStr);
			}

			return new Ref(number, null, pageStart, pageStop);
		}

		private static Ref parseAsSection(Matcher m) {
			String number = m.group(1);
			String section = m.group(2);
			return new Ref(number, section, null, null);
		}

		private static Ref parseAsNumber(Matcher m) {
			String number = m.group(1);
			return new Ref(number, null, null, null);
		}
	}
}
