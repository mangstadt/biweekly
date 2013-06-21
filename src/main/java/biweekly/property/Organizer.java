package biweekly.property;

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
 * Defines the organizer. This property has different meanings depending on the
 * component it belongs to:
 * <ul>
 * <li>VEVENT, VTODO, VJOURNAL - the organizer of the event</li>
 * <li>VFREEBUSY - the person requesting the free busy time</li>
 * </ul>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-111">RFC 5545
 * p.111-2</a>
 */
public class Organizer extends TextProperty {
	/**
	 * Creates an organizer property
	 * @param uri a URI representing the organizer (typically, an email address,
	 * e.g. "mailto:johndoe@example.com")
	 */
	public Organizer(String uri) {
		super(uri);
	}

	/**
	 * Creates an organizer property using an email address as its value.
	 * @param email the email address (e.g. "johndoe@example.com")
	 * @return the property
	 */
	public static Organizer email(String email) {
		return new Organizer("mailto:" + email);
	}

	@Override
	public String getSentBy() {
		return super.getSentBy();
	}

	@Override
	public void setSentBy(String sentBy) {
		super.setSentBy(sentBy);
	}

	@Override
	public String getCommonName() {
		return super.getCommonName();
	}

	@Override
	public void setCommonName(String commonName) {
		super.setCommonName(commonName);
	}

	@Override
	public String getDirectoryEntry() {
		return super.getDirectoryEntry();
	}

	@Override
	public void setDirectoryEntry(String directoryEntry) {
		super.setDirectoryEntry(directoryEntry);
	}

	/**
	 * Gets the language that the common name parameter is written in.
	 */
	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	/**
	 * Sets the language that the common name parameter is written in.
	 */
	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}
}
