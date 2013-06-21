package biweekly.property;

import java.util.List;

import biweekly.component.ICalComponent;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.component.VJournal;
import biweekly.component.VTodo;

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
 * Represents a binary resource that is associated with a {@link VEvent VEVENT},
 * {@link VTodo VTODO}, {@link VJournal VJOURNAL}, or {@link VAlarm VALARM}
 * component.
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-80">RFC 5545 p.80-1</a>
 */
public class Attachment extends ICalProperty {
	private byte[] data;
	private String uri;

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
	 * Sets the attachment's binary data. If the attachment has a URI associated
	 * with it, the URI will be set to null.
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
	 * Sets the attachment's URI. If the attachment has binary data associated
	 * with it, the binary data will be set to null.
	 * @param uri the URI (e.g. "http://example.com/image.png")
	 */
	public void setUri(String uri) {
		this.uri = uri;
		data = null;
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
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		if (uri == null && data == null) {
			warnings.add("No URI or data specified.");
		}
	}
}
