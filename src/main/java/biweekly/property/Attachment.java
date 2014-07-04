package biweekly.property;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.component.ICalComponent;
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
 * Defines a binary resource that is associated with the component to which it
 * belongs (such as an image or document).
 * </p>
 * 
 * <p>
 * <b>Code sample:</b>
 * 
 * <pre class="brush:java">
 * VEvent event = new VEvent();
 * 
 * //from a byte array
 * byte[] data = ...
 * Attachment attach = new Attachment(&quot;image/png&quot;, data);
 * event.addAttachment(attach);
 * 
 * //from a file 
 * File file = new File(&quot;image.png&quot;);
 * attach = new Attachment(&quot;image/png&quot;, file);
 * event.addAttachment(attach);
 * 
 * //referencing a URL
 * attach = new Attachment(&quot;image/png&quot;, &quot;http://example.com/image.png&quot;);
 * event.addAttachment(attach);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-80">RFC 5545 p.80-1</a>
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.25</a>
 */
public class Attachment extends ICalProperty {
	private byte[] data;
	private String uri, contentId;

	/**
	 * Creates a new attachment.
	 * @param formatType the content-type of the data (e.g. "image/png")
	 * @param file the file to attach
	 * @throws IOException if there's a problem reading from the file
	 */
	public Attachment(String formatType, File file) throws IOException {
		this.data = IOUtils.toByteArray(new FileInputStream(file), true);
		setFormatType(formatType);
	}

	/**
	 * Creates a new attachment.
	 * @param formatType the content-type of the data (e.g. "image/png")
	 * @param data the binary data
	 */
	public Attachment(String formatType, byte[] data) {
		this.data = data;
		setFormatType(formatType);
	}

	/**
	 * Creates a new attachment.
	 * @param formatType the content-type of the data (e.g. "image/png")
	 * @param uri a URL pointing to the resource (e.g.
	 * "http://example.com/image.png")
	 */
	public Attachment(String formatType, String uri) {
		this.uri = uri;
		setFormatType(formatType);
	}

	/**
	 * Gets the attachment's binary data.
	 * @return the binary data or null if not set
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Assigns binary data to the attachment's value.
	 * @param data the binary data
	 */
	public void setData(byte[] data) {
		this.data = data;
		uri = null;
	}

	/**
	 * Gets the attachment's URI.
	 * @return the URI (e.g. "http://example.com/image.png") or null if not set
	 */
	public String getUri() {
		return uri;
	}

	/**
	 * Assigns a URI to the attachment's value.
	 * @param uri the URI (e.g. "http://example.com/image.png")
	 */
	public void setUri(String uri) {
		this.uri = uri;
		data = null;
	}

	/**
	 * Gets the attachment's content ID.
	 * @return the content ID
	 */
	public String getContentId() {
		return contentId;
	}

	/**
	 * Assigns a content ID to the attachment's value.
	 * @param contentId the content ID
	 */
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}

	@Override
	public String getFormatType() {
		return super.getFormatType();
	}

	@Override
	public void setFormatType(String formatType) {
		super.setFormatType(formatType);
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		if (uri == null && data == null && contentId == null) {
			warnings.add(Warning.validate(26));
		}
	}
}
