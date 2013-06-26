package biweekly.property;

import java.util.List;

import biweekly.component.VAlarm;
import biweekly.parameter.CalendarUserType;
import biweekly.parameter.ParticipationStatus;
import biweekly.parameter.Role;

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
 * Defines an attendee (such as a person attending an event). This property has
 * different meanings depending on the component that it belongs to:
 * <ul>
 * <li>{@link VAlarm} (EMAIL action) - an email address that is to receive the
 * alarm</li>
 * <li>all others - an attendee</li>
 * </ul>
 * </p>
 * 
 * <p>
 * <b>Examples:</b>
 * 
 * <pre>
 * Attendee attendee = Attendee.email("johndoe@example.com")
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-107">RFC 5545
 * p.107-9</a>
 */
public class Attendee extends TextProperty {
	/**
	 * Creates an attendee property.
	 * @param uri a URI representing the attendee (typically, an email address,
	 * e.g. "mailto:johndoe@example.com")
	 */
	public Attendee(String uri) {
		super(uri);
	}

	/**
	 * Creates an attendee property using an email address as its value.
	 * @param email the email address (e.g. "johndoe@example.com")
	 * @return the property
	 */
	public static Attendee email(String email) {
		return new Attendee("mailto:" + email);
	}

	/**
	 * Gets the type of user the attendee is (for example, an "individual" or a
	 * "room").
	 * @return the calendar user type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-16">RFC 5545
	 * p.16</a>
	 */
	public CalendarUserType getCalendarUserType() {
		return parameters.getCalendarUserType();
	}

	/**
	 * Sets the type of user the attendee is (for example, an "individual" or a
	 * "room").
	 * @param cutype the calendar user type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-16">RFC 5545
	 * p.16</a>
	 */
	public void setCalendarUserType(CalendarUserType cutype) {
		parameters.setCalendarUserType(cutype);
	}

	/**
	 * Gets the groups that the attendee is a member of.
	 * @return the group URIs (typically, these are email address URIs, e.g.
	 * "mailto:mailinglist@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21-2</a>
	 */
	public List<String> getMembers() {
		return parameters.getMembers();
	}

	/**
	 * Adds a group that the attendee is a member of.
	 * @param uri the group URI (typically, an email address URI, e.g.
	 * "mailto:mailinglist@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21-2</a>
	 */
	public void addMember(String uri) {
		parameters.addMember(uri);
	}

	/**
	 * Gets the attendee's role (for example, "chair" or
	 * "required participant").
	 * @return the role or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25-6</a>
	 */
	public Role getRole() {
		return parameters.getRole();
	}

	/**
	 * Sets the attendee's role (for example, "chair" or
	 * "required participant").
	 * @param role the role or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25-6</a>
	 */
	public void setRole(Role role) {
		parameters.setRole(role);
	}

	/**
	 * Gets the attendee's level of participation.
	 * @return the participation status or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545
	 * p.22-3</a>
	 */
	public ParticipationStatus getParticipationStatus() {
		return parameters.getParticipationStatus();
	}

	/**
	 * Sets the attendee's level of participation.
	 * @param status the participation status or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545
	 * p.22-3</a>
	 */
	public void setParticipationStatus(ParticipationStatus status) {
		parameters.setParticipationStatus(status);
	}

	/**
	 * Gets whether the organizer requests a response from the attendee.
	 * @return true if an RSVP is requested, false if not, null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-26">RFC 5545
	 * p.26-7</a>
	 */
	public Boolean getRsvp() {
		return parameters.getRsvp();
	}

	/**
	 * Sets whether the organizer requests a response from the attendee.
	 * @param rsvp true if an RSVP has been requested, false if not, null to
	 * remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-26">RFC 5545
	 * p.26-7</a>
	 */
	public void setRsvp(Boolean rsvp) {
		parameters.setRsvp(rsvp);
	}

	/**
	 * Gets the people who have delegated their responsibility to the attendee.
	 * @return the delegators (typically email URIs, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17</a>
	 */
	public List<String> getDelegatedFrom() {
		return parameters.getDelegatedFrom();
	}

	/**
	 * Adds a person who has delegated his or her responsibility to the
	 * attendee.
	 * @param uri the delegator (typically an email URI, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17</a>
	 */
	public void addDelegatedFrom(String uri) {
		parameters.addDelegatedFrom(uri);
	}

	/**
	 * Gets the people to which the attendee has delegated his or her
	 * responsibility.
	 * @return the delegatees (typically email URIs, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17-8</a>
	 */
	public List<String> getDelegatedTo() {
		return parameters.getDelegatedTo();
	}

	/**
	 * Adds a person to which the attendee has delegated his or her
	 * responsibility.
	 * @param uri the delegatee (typically an email URI, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17-8</a>
	 */
	public void addDelegatedTo(String uri) {
		parameters.addDelegatedTo(uri);
	}

	@Override
	public String getSentBy() {
		return super.getSentBy();
	}

	@Override
	public void setSentBy(String uri) {
		super.setSentBy(uri);
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
	public void setDirectoryEntry(String uri) {
		super.setDirectoryEntry(uri);
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
