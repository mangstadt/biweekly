package biweekly.javadoc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
 * <pre class="brush:java">
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
	private static final List<Group> groups;
	static {
		List<Group> list = new ArrayList<Group>();

		//page range
		list.add(new Group("^(\\d+) p\\.(\\d+)-(\\d+)?$", "<a href=\"http://tools.ietf.org/html/rfc%s#page-%s\">RFC %s p.%s-%s</a>", 1, 2, 1, 2, 3));

		//single page
		list.add(new Group("^(\\d+) p\\.(\\d+)?$", "<a href=\"http://tools.ietf.org/html/rfc%s#page-%s\">RFC %s p.%s</a>", 1, 2, 1, 2));

		//no pages
		list.add(new Group("^(\\d+)$", "<a href=\"http://tools.ietf.org/html/rfc%s\">RFC %s</a>", 1, 1));

		//section
		list.add(new Group("^(\\d+) ([\\.\\d]+)$", "<a href=\"http://tools.ietf.org/html/rfc%s#section-%s\">RFC %s, Section %s</a>", 1, 2, 1, 2));

		groups = Collections.unmodifiableList(list);
	}

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

		StringBuilder sb = null;
		for (Tag tag : tags) {
			String text = tag.text();

			String html = null;
			for (Group group : groups) {
				html = group.buildHtml(text);
				if (html != null) {
					break;
				}
			}

			if (html == null) {
				logWarning(tag, "Skipping @rfc taglet whose text is incorrectly formatted.");
				continue;
			}

			if (sb == null) {
				sb = new StringBuilder("<dt><b>Specification Reference:</b></dt>");
			}
			sb.append("<dd>").append(html).append("</dd>");
		}

		return (sb == null) ? "" : sb.toString();
	}

	void logWarning(Tag tag, String message) {
		Standard.htmlDoclet.configuration().root.printWarning(tag.position(), message);
	}

	private static class Group {
		private final Pattern regex;
		private final String html;
		private final int[] groups;

		public Group(String regex, String html, int... groups) {
			this.regex = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
			this.html = html;
			this.groups = groups;
		}

		public String buildHtml(String tagText) {
			Matcher m = regex.matcher(tagText);
			if (!m.find()) {
				return null;
			}

			Object formatParams[] = new Object[groups.length];
			for (int i = 0; i < groups.length; i++) {
				formatParams[i] = m.group(groups[i]);
			}

			return String.format(html, formatParams);
		}
	}
}
