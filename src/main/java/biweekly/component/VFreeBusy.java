package biweekly.component;

import static biweekly.property.ValuedProperty.getValue;

import java.util.Date;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.parameter.FreeBusyType;
import biweekly.property.Attendee;
import biweekly.property.Comment;
import biweekly.property.Contact;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.DateTimeStamp;
import biweekly.property.FreeBusy;
import biweekly.property.LastModified;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.property.RequestStatus;
import biweekly.property.Uid;
import biweekly.property.Url;
import biweekly.util.Duration;
import biweekly.util.ICalDate;

/*
 Copyright (c) 2013-2014, Michael Angstadt
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
 * Defines a collection of time ranges that describe when a person is available
 * and unavailable.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre class="brush:java">
 * VFreeBusy freebusy = new VFreeBusy();
 * 
 * Date start = ...
 * Date end = ...
 * freebusy.addFreeBusy(FreeBusyType.FREE, start, end);
 * 
 * start = ...
 * Duration duration = Duration.builder().hours(2).build();
 * freebusy.addFreeBusy(FreeBusyType.BUSY, start, duration);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @see <a href="http://tools.ietf.org/html/rfc5545#page-59">RFC 5545
 * p.59-62</a>
 */
public class VFreeBusy extends ICalComponent {
	/**
	 * <p>
	 * Creates a new free/busy component.
	 * </p>
	 * <p>
	 * The following properties are auto-generated on object creation. These
	 * properties <b>must</b> be present in order for the free/busy component to
	 * be valid:
	 * <ul>
	 * <li>{@link Uid} - Set to a UUID.</li>
	 * <li>{@link DateTimeStamp} - Set to the current date-time.</li>
	 * </ul>
	 * </p>
	 */
	public VFreeBusy() {
		setUid(Uid.random());
		setDateTimeStamp(new Date());
	}

	/**
	 * Gets the unique identifier for this free/busy entry. This component
	 * object comes populated with a UID on creation. This is a <b>required</b>
	 * property.
	 * @return the UID or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-117">RFC 5545
	 * p.117-8</a>
	 */
	public Uid getUid() {
		return getProperty(Uid.class);
	}

	/**
	 * Sets the unique identifier for this free/busy entry. This component
	 * object comes populated with a UID on creation. This is a <b>required</b>
	 * property.
	 * @param uid the UID or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-117">RFC 5545
	 * p.117-8</a>
	 */
	public void setUid(Uid uid) {
		setProperty(Uid.class, uid);
	}

	/**
	 * Sets the unique identifier for this free/busy entry. This component
	 * object comes populated with a UID on creation. This is a <b>required</b>
	 * property.
	 * @param uid the UID or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-117">RFC 5545
	 * p.117-8</a>
	 */
	public Uid setUid(String uid) {
		Uid prop = (uid == null) ? null : new Uid(uid);
		setUid(prop);
		return prop;
	}

	/**
	 * Gets either (a) the creation date of the iCalendar object (if the
	 * {@link Method} property is defined) or (b) the date that the free/busy
	 * entry was last modified (the {@link LastModified} property also holds
	 * this information). This free/busy object comes populated with a
	 * {@link DateTimeStamp} property that is set to the current time. This is a
	 * <b>required</b> property.
	 * @return the date time stamp or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-137">RFC 5545
	 * p.137-8</a>
	 */
	public DateTimeStamp getDateTimeStamp() {
		return getProperty(DateTimeStamp.class);
	}

	/**
	 * Sets either (a) the creation date of the iCalendar object (if the
	 * {@link Method} property is defined) or (b) the date that the free/busy
	 * entry was last modified (the {@link LastModified} property also holds
	 * this information). This free/busy object comes populated with a
	 * {@link DateTimeStamp} property that is set to the current time. This is a
	 * <b>required</b> property.
	 * @param dateTimeStamp the date time stamp or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-137">RFC 5545
	 * p.137-8</a>
	 */
	public void setDateTimeStamp(DateTimeStamp dateTimeStamp) {
		setProperty(DateTimeStamp.class, dateTimeStamp);
	}

	/**
	 * Sets either (a) the creation date of the iCalendar object (if the
	 * {@link Method} property is defined) or (b) the date that the free/busy
	 * entry was last modified (the {@link LastModified} property also holds
	 * this information). This free/busy object comes populated with a
	 * {@link DateTimeStamp} property that is set to the current time. This is a
	 * <b>required</b> property.
	 * @param dateTimeStamp the date time stamp or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-137">RFC 5545
	 * p.137-8</a>
	 */
	public DateTimeStamp setDateTimeStamp(Date dateTimeStamp) {
		DateTimeStamp prop = (dateTimeStamp == null) ? null : new DateTimeStamp(dateTimeStamp);
		setDateTimeStamp(prop);
		return prop;
	}

	/**
	 * Gets the contact associated with the free/busy entry.
	 * @return the contact or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-109">RFC 5545
	 * p.109-11</a>
	 */
	public Contact getContact() {
		return getProperty(Contact.class);
	}

	/**
	 * Sets the contact for the free/busy entry.
	 * @param contact the contact or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-109">RFC 5545
	 * p.109-11</a>
	 */
	public void setContact(Contact contact) {
		setProperty(Contact.class, contact);
	}

	/**
	 * Sets the contact for the free/busy entry.
	 * @param contact the contact (e.g. "ACME Co - (123) 555-1234")
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-109">RFC 5545
	 * p.109-11</a>
	 */
	public Contact addContact(String contact) {
		Contact prop = new Contact(contact);
		setContact(prop);
		return prop;
	}

	/**
	 * Gets the date that the free/busy entry starts.
	 * @return the start date or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 */
	public DateStart getDateStart() {
		return getProperty(DateStart.class);
	}

	/**
	 * Sets the date that the free/busy entry starts.
	 * @param dateStart the start date or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 */
	public void setDateStart(DateStart dateStart) {
		setProperty(DateStart.class, dateStart);
	}

	/**
	 * Sets the date that the free/busy entry starts.
	 * @param dateStart the start date or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-97">RFC 5545
	 * p.97-8</a>
	 */
	public DateStart setDateStart(Date dateStart) {
		DateStart prop = (dateStart == null) ? null : new DateStart(dateStart);
		setDateStart(prop);
		return prop;
	}

	/**
	 * Gets the date that the free/busy entry ends.
	 * @return the end date or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-95">RFC 5545
	 * p.95-6</a>
	 */
	public DateEnd getDateEnd() {
		return getProperty(DateEnd.class);
	}

	/**
	 * Sets the date that the free/busy entry ends.
	 * @param dateEnd the end date or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-95">RFC 5545
	 * p.95-6</a>
	 */
	public void setDateEnd(DateEnd dateEnd) {
		setProperty(DateEnd.class, dateEnd);
	}

	/**
	 * Sets the date that the free/busy entry ends.
	 * @param dateEnd the end date or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-95">RFC 5545
	 * p.95-6</a>
	 */
	public DateEnd setDateEnd(Date dateEnd) {
		DateEnd prop = (dateEnd == null) ? null : new DateEnd(dateEnd);
		setDateEnd(prop);
		return prop;
	}

	/**
	 * Gets the person requesting the free/busy time.
	 * @return the person requesting the free/busy time or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-111">RFC 5545
	 * p.111-2</a>
	 */
	public Organizer getOrganizer() {
		return getProperty(Organizer.class);
	}

	/**
	 * Sets the person requesting the free/busy time.
	 * @param organizer the person requesting the free/busy time or null to
	 * remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-111">RFC 5545
	 * p.111-2</a>
	 */
	public void setOrganizer(Organizer organizer) {
		setProperty(Organizer.class, organizer);
	}

	/**
	 * Sets the person requesting the free/busy time.
	 * @param email the email address of the person requesting the free/busy
	 * time (e.g. "johndoe@example.com") or null to remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-111">RFC 5545
	 * p.111-2</a>
	 */
	public Organizer setOrganizer(String email) {
		Organizer prop = (email == null) ? null : new Organizer(null, email);
		setOrganizer(prop);
		return prop;
	}

	/**
	 * Gets a URL to a resource that contains additional information about the
	 * free/busy entry.
	 * @return the URL or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-116">RFC 5545
	 * p.116-7</a>
	 */
	public Url getUrl() {
		return getProperty(Url.class);
	}

	/**
	 * Sets a URL to a resource that contains additional information about the
	 * free/busy entry.
	 * @param url the URL or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-116">RFC 5545
	 * p.116-7</a>
	 */
	public void setUrl(Url url) {
		setProperty(Url.class, url);
	}

	/**
	 * Sets a URL to a resource that contains additional information about the
	 * free/busy entry.
	 * @param url the URL (e.g. "http://example.com/resource.ics") or null to
	 * remove
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-116">RFC 5545
	 * p.116-7</a>
	 */
	public Url setUrl(String url) {
		Url prop = (url == null) ? null : new Url(url);
		setUrl(prop);
		return prop;
	}

	//
	//zero or more
	//	private List<Attendee> attendees;
	//	private List<Comment> comments;
	//	private List<FreeBusy> freeBusy;
	//	private List<Rstatus> rstatus;

	/**
	 * Gets the people who are involved in the free/busy entry.
	 * @return the attendees
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-107">RFC 5545
	 * p.107-9</a>
	 */
	public List<Attendee> getAttendees() {
		return getProperties(Attendee.class);
	}

	/**
	 * Adds a person who is involved in the free/busy entry.
	 * @param attendee the attendee
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-107">RFC 5545
	 * p.107-9</a>
	 */
	public void addAttendee(Attendee attendee) {
		addProperty(attendee);
	}

	/**
	 * Gets the comments attached to the free/busy entry.
	 * @return the comments
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-83">RFC 5545
	 * p.83-4</a>
	 */
	public List<Comment> getComments() {
		return getProperties(Comment.class);
	}

	/**
	 * Adds a comment to the free/busy entry.
	 * @param comment the comment to add
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-83">RFC 5545
	 * p.83-4</a>
	 */
	public void addComment(Comment comment) {
		addProperty(comment);
	}

	/**
	 * Adds a comment to the free/busy entry.
	 * @param comment the comment to add
	 * @return the property that was created
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-83">RFC 5545
	 * p.83-4</a>
	 */
	public Comment addComment(String comment) {
		Comment prop = new Comment(comment);
		addComment(prop);
		return prop;
	}

	/**
	 * Gets the person's availabilities over certain time periods (for example,
	 * "free" between 1pm-3pm, but "busy" between 3pm-4pm).
	 * @return the availabilities
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-100">RFC 5545
	 * p.100-1</a>
	 */
	public List<FreeBusy> getFreeBusy() {
		return getProperties(FreeBusy.class);
	}

	/**
	 * Adds a list of time periods for which the person is free or busy (for
	 * example, "free" between 1pm-3pm and 4pm-5pm). Note that a
	 * {@link FreeBusy} property can contain multiple time periods, but only one
	 * availability type (e.g. "busy").
	 * @param freeBusy the availabilities
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-100">RFC 5545
	 * p.100-1</a>
	 */
	public void addFreeBusy(FreeBusy freeBusy) {
		addProperty(freeBusy);
	}

	/**
	 * Adds a single time period for which the person is free or busy (for
	 * example, "free" between 1pm-3pm). This method will look for an existing
	 * property that has the given {@link FreeBusyType} and add the time period
	 * to it, or create a new property is one cannot be found.
	 * @param type the availability type (e.g. "free" or "busy")
	 * @param start the start date-time
	 * @param end the end date-time
	 * @return the property that was created/modified
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-100">RFC 5545
	 * p.100-1</a>
	 */
	public FreeBusy addFreeBusy(FreeBusyType type, Date start, Date end) {
		FreeBusy found = findByFbType(type);
		found.addValue(start, end);
		return found;
	}

	/**
	 * Adds a single time period for which the person is free or busy (for
	 * example, "free" for 2 hours after 1pm). This method will look for an
	 * existing property that has the given {@link FreeBusyType} and add the
	 * time period to it, or create a new property is one cannot be found.
	 * @param type the availability type (e.g. "free" or "busy")
	 * @param start the start date-time
	 * @param duration the length of time
	 * @return the property that was created/modified
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-100">RFC 5545
	 * p.100-1</a>
	 */
	public FreeBusy addFreeBusy(FreeBusyType type, Date start, Duration duration) {
		FreeBusy found = findByFbType(type);
		found.addValue(start, duration);
		return found;
	}

	private FreeBusy findByFbType(FreeBusyType type) {
		FreeBusy found = null;

		for (FreeBusy fb : getFreeBusy()) {
			if (fb.getType() == type) {
				found = fb;
				break;
			}
		}

		if (found == null) {
			found = new FreeBusy();
			found.setType(type);
			addFreeBusy(found);
		}
		return found;
	}

	/**
	 * Gets the response to a scheduling request.
	 * @return the response
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-141">RFC 5545
	 * p.141-3</a>
	 */
	public RequestStatus getRequestStatus() {
		return getProperty(RequestStatus.class);
	}

	/**
	 * Sets the response to a scheduling request.
	 * @param requestStatus the response
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-141">RFC 5545
	 * p.141-3</a>
	 */
	public void setRequestStatus(RequestStatus requestStatus) {
		setProperty(RequestStatus.class, requestStatus);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		if (version == ICalVersion.V1_0) {
			warnings.add(Warning.validate(47, version));
		}

		checkRequiredCardinality(warnings, Uid.class, DateTimeStamp.class);
		checkOptionalCardinality(warnings, Contact.class, DateStart.class, DateEnd.class, Organizer.class, Url.class);

		ICalDate dateStart = getValue(getDateStart());
		ICalDate dateEnd = getValue(getDateEnd());

		//DTSTART is required if DTEND exists
		if (dateEnd != null && dateStart == null) {
			warnings.add(Warning.validate(15));
		}

		//DTSTART and DTEND must contain a time component
		if (dateStart != null && !dateStart.hasTime()) {
			warnings.add(Warning.validate(20, DateStart.class.getSimpleName()));
		}
		if (dateEnd != null && !dateEnd.hasTime()) {
			warnings.add(Warning.validate(20, DateEnd.class.getSimpleName()));
		}

		//DTSTART must come before DTEND
		if (dateStart != null && dateEnd != null && dateStart.compareTo(dateEnd) >= 0) {
			warnings.add(Warning.validate(16));
		}
	}
}
