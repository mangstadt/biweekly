package biweekly.parameter;

import java.util.ArrayList;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.component.VTimezone;
import biweekly.property.FreeBusy;
import biweekly.property.RecurrenceId;
import biweekly.property.RelatedTo;
import biweekly.property.TimezoneId;
import biweekly.property.Trigger;
import biweekly.util.ListMultimap;

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
 * Contains the list of parameters that belong to a property.
 * @author Michael Angstadt
 */
public class ICalParameters extends ListMultimap<String, String> {
	public static final String CN = "CN";
	public static final String ALTREP = "ALTREP";
	public static final String CUTYPE = "CUTYPE";
	public static final String DELEGATED_FROM = "DELEGATED-FROM";
	public static final String DELEGATED_TO = "DELEGATED-TO";
	public static final String DIR = "DIR";
	public static final String ENCODING = "ENCODING";
	public static final String FMTTYPE = "FMTTYPE";
	public static final String FBTYPE = "FBTYPE";
	public static final String LANGUAGE = "LANGUAGE";
	public static final String MEMBER = "MEMBER";
	public static final String PARTSTAT = "PARTSTAT";
	public static final String RANGE = "RANGE";
	public static final String RELATED = "RELATED";
	public static final String RELTYPE = "RELTYPE";
	public static final String ROLE = "ROLE";
	public static final String RSVP = "RSVP";
	public static final String SENT_BY = "SENT-BY";
	public static final String TZID = "TZID";
	public static final String VALUE = "VALUE";

	/**
	 * Creates a parameters list.
	 */
	public ICalParameters() {
		super(0); //initialize map size to 0 because most properties don't use any parameters
	}

	/**
	 * Copies an existing parameters list.
	 * @param parameters the list to copy
	 */
	public ICalParameters(ICalParameters parameters) {
		super(parameters);
	}

	/**
	 * Gets a URI pointing to additional information about the entity
	 * represented by the property.
	 * @return the URI or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-14">RFC 5545
	 * p.14-5</a>
	 */
	public String getAltRepresentation() {
		return first(ALTREP);
	}

	/**
	 * Sets a URI pointing to additional information about the entity
	 * represented by the property.
	 * @param uri the URI or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-14">RFC 5545
	 * p.14-5</a>
	 */
	public void setAltRepresentation(String uri) {
		replace(ALTREP, uri);
	}

	/**
	 * Gets the display name of a person.
	 * @return the display name (e.g. "John Doe") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-15">RFC 5545
	 * p.15-6</a>
	 */
	public String getCommonName() {
		return first(CN);
	}

	/**
	 * Sets the display name of a person.
	 * @param cn the display name (e.g. "John Doe") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-15">RFC 5545
	 * p.15-6</a>
	 */
	public void setCommonName(String cn) {
		replace(CN, cn);
	}

	/**
	 * Gets the type of user an attendee is (for example, an "individual" or a
	 * "room").
	 * @return the calendar user type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-16">RFC 5545
	 * p.16</a>
	 */
	public CalendarUserType getCalendarUserType() {
		String value = first(CUTYPE);
		return (value == null) ? null : CalendarUserType.get(value);
	}

	/**
	 * Sets the type of user an attendee is (for example, an "individual" or a
	 * "room").
	 * @param cutype the calendar user type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-16">RFC 5545
	 * p.16</a>
	 */
	public void setCalendarUserType(CalendarUserType cutype) {
		replace(CUTYPE, (cutype == null) ? null : cutype.getValue());
	}

	/**
	 * Gets the people who have delegated their responsibility to an attendee.
	 * @return the delegators (typically email URIs, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17</a>
	 */
	public List<String> getDelegatedFrom() {
		return get(DELEGATED_FROM);
	}

	/**
	 * Adds a person who has delegated his or her responsibility to an attendee.
	 * @param uri the delegator (typically an email URI, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17</a>
	 */
	public void addDelegatedFrom(String uri) {
		put(DELEGATED_FROM, uri);
	}

	/**
	 * Removes a person who has delegated his or her responsibility to an
	 * attendee.
	 * @param uri the delegator to remove (typically an email URI, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17</a>
	 */
	public void removeDelegatedFrom(String uri) {
		remove(DELEGATED_FROM, uri);
	}

	/**
	 * Removes everyone who has delegated his or her responsibility to an
	 * attendee.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17</a>
	 */
	public void removeDelegatedFrom() {
		removeAll(DELEGATED_FROM);
	}

	/**
	 * Gets the people to which an attendee has delegated his or her
	 * responsibility.
	 * @return the delegatees (typically email URIs, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17-8</a>
	 */
	public List<String> getDelegatedTo() {
		return get(DELEGATED_TO);
	}

	/**
	 * Adds a person to which an attendee has delegated his or her
	 * responsibility.
	 * @param uri the delegatee (typically an email URI, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17-8</a>
	 */
	public void addDelegatedTo(String uri) {
		put(DELEGATED_TO, uri);
	}

	/**
	 * Removes a person to which an attendee has delegated his or her
	 * responsibility.
	 * @param uri the delegatee to remove (typically an email URI, e.g.
	 * "mailto:janedoe@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17-8</a>
	 */
	public void removeDelegatedTo(String uri) {
		remove(DELEGATED_TO, uri);
	}

	/**
	 * Removes everyone to which an attendee has delegated his or her
	 * responsibility.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-17">RFC 5545
	 * p.17-8</a>
	 */
	public void removeDelegatedTo() {
		removeAll(DELEGATED_TO);
	}

	/**
	 * Gets a URI that contains additional information about the person.
	 * @return the URI (e.g. an LDAP URI) or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18</a>
	 */
	public String getDirectoryEntry() {
		return first(DIR);
	}

	/**
	 * Sets a URI that contains additional information about the person.
	 * @param uri the URI (e.g. an LDAP URI) or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18</a>
	 */
	public void setDirectoryEntry(String uri) {
		replace(DIR, uri);
	}

	/**
	 * Gets the encoding of the property value (for example, "base64").
	 * @return the encoding or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18-9</a>
	 */
	public Encoding getEncoding() {
		String value = first(ENCODING);
		return (value == null) ? null : Encoding.get(value);
	}

	/**
	 * Sets the encoding of the property value (for example, "base64").
	 * @param encoding the encoding or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-18">RFC 5545
	 * p.18-9</a>
	 */
	public void setEncoding(Encoding encoding) {
		replace(ENCODING, (encoding == null) ? null : encoding.getValue());
	}

	/**
	 * Gets the content-type of the property's value.
	 * @return the content type (e.g. "image/png") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-19">RFC 5545
	 * p.19-20</a>
	 */
	public String getFormatType() {
		return first(FMTTYPE);
	}

	/**
	 * Sets the content-type of the property's value.
	 * @param formatType the content type (e.g. "image/png") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-19">RFC 5545
	 * p.19-20</a>
	 */
	public void setFormatType(String formatType) {
		replace(FMTTYPE, formatType);
	}

	/**
	 * Gets the person's status over the time periods that are specified in a
	 * {@link FreeBusy} property (for example, "free" or "busy"). If not set,
	 * the user should be considered "busy".
	 * @return the type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-20">RFC 5545
	 * p.20</a>
	 */
	public FreeBusyType getFreeBusyType() {
		String value = first(FBTYPE);
		return (value == null) ? null : FreeBusyType.get(value);
	}

	/**
	 * Sets the person's status over the time periods that are specified in a
	 * {@link FreeBusy} property (for example, "free" or "busy"). If not set,
	 * the user should be considered "busy".
	 * @param fbType the type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-20">RFC 5545
	 * p.20</a>
	 */
	public void setFreeBusyType(FreeBusyType fbType) {
		replace(FBTYPE, (fbType == null) ? null : fbType.getValue());
	}

	/**
	 * Gets the language that the property value is written in.
	 * @return the language (e.g. "en" for English) or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21</a>
	 */
	public String getLanguage() {
		return first(LANGUAGE);
	}

	/**
	 * Sets the language that the property value is written in.
	 * @param language the language (e.g. "en" for English) or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21</a>
	 */
	public void setLanguage(String language) {
		replace(LANGUAGE, language);
	}

	/**
	 * Adds a group that an attendee is a member of.
	 * @param uri the group URI (typically, an email address URI, e.g.
	 * "mailto:mailinglist@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21-2</a>
	 */
	public void addMember(String uri) {
		put(MEMBER, uri);
	}

	/**
	 * Gets the groups that an attendee is a member of.
	 * @return the group URIs (typically, these are email address URIs, e.g.
	 * "mailto:mailinglist@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21-2</a>
	 */
	public List<String> getMembers() {
		return get(MEMBER);
	}

	/**
	 * Removes a group that an attendee is a member of.
	 * @param uri the group URI to remove (typically, an email address URI, e.g.
	 * "mailto:mailinglist@example.com")
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21-2</a>
	 */
	public void removeMember(String uri) {
		remove(MEMBER, uri);
	}

	/**
	 * Removes all groups that an attendee is a member of.
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-21">RFC 5545
	 * p.21-2</a>
	 */
	public void removeMembers() {
		removeAll(MEMBER);
	}

	/**
	 * Gets an attendee's level of participation.
	 * @return the participation status or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545
	 * p.22-3</a>
	 */
	public ParticipationStatus getParticipationStatus() {
		String value = first(PARTSTAT);
		return (value == null) ? null : ParticipationStatus.get(value);
	}

	/**
	 * Sets an attendee's level of participation.
	 * @param status the participation status or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-22">RFC 5545
	 * p.22-3</a>
	 */
	public void setParticipationStatus(ParticipationStatus status) {
		replace(PARTSTAT, (status == null) ? null : status.getValue());
	}

	/**
	 * Gets the effective range of recurrence instances from the instance
	 * specified by a {@link RecurrenceId} property.
	 * @return the range or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-23">RFC 5545
	 * p.23-4</a>
	 */
	public Range getRange() {
		String value = first(RANGE);
		return (value == null) ? null : Range.get(value);
	}

	/**
	 * Sets the effective range of recurrence instances from the instance
	 * specified by a {@link RecurrenceId} property.
	 * @param range the range or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-23">RFC 5545
	 * p.23-4</a>
	 */
	public void setRange(Range range) {
		replace(RANGE, (range == null) ? null : range.getValue());
	}

	/**
	 * Gets the date-time field that the duration in a {@link Trigger} property
	 * is relative to.
	 * @return the field or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-24">RFC 5545
	 * p.24</a>
	 */
	public Related getRelated() {
		String value = first(RELATED);
		return (value == null) ? null : Related.get(value);
	}

	/**
	 * Sets the date-time field that the duration in a {@link Trigger} property
	 * is relative to.
	 * @param related the field or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-24">RFC 5545
	 * p.24</a>
	 */
	public void setRelated(Related related) {
		replace(RELATED, (related == null) ? null : related.getValue());
	}

	/**
	 * Gets the relationship type of a {@link RelatedTo} property.
	 * @return the relationship type (e.g. "child") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 */
	public RelationshipType getRelationshipType() {
		String value = first(RELTYPE);
		return (value == null) ? null : RelationshipType.get(value);
	}

	/**
	 * Sets the relationship type of a {@link RelatedTo} property.
	 * @param relationshipType the relationship type (e.g. "child") or null to
	 * remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25</a>
	 */
	public void setRelationshipType(RelationshipType relationshipType) {
		replace(RELTYPE, (relationshipType == null) ? null : relationshipType.getValue());
	}

	/**
	 * Gets an attendee's role (for example, "chair" or "required participant").
	 * @return the role or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25-6</a>
	 */
	public Role getRole() {
		String value = first(ROLE);
		return (value == null) ? null : Role.get(value);
	}

	/**
	 * Sets an attendee's role (for example, "chair" or "required participant").
	 * @param role the role or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-25">RFC 5545
	 * p.25-6</a>
	 */
	public void setRole(Role role) {
		replace(ROLE, (role == null) ? null : role.getValue());
	}

	/**
	 * Gets whether the organizer requests a response from an attendee.
	 * @throws IllegalStateException if the parameter value is malformed and
	 * cannot be parsed
	 * @return true if an RSVP is requested, false if not, null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-26">RFC 5545
	 * p.26-7</a>
	 */
	public Boolean getRsvp() {
		String value = first(RSVP);

		if (value == null) {
			return null;
		}
		if ("true".equalsIgnoreCase(value)) {
			return true;
		}
		if ("false".equalsIgnoreCase(value)) {
			return false;
		}
		throw new IllegalStateException(RSVP + " parameter value is malformed and could not be parsed. Retrieve its raw text value instead.");
	}

	/**
	 * Sets whether the organizer requests a response from an attendee.
	 * @param rsvp true if an RSVP has been requested, false if not, null to
	 * remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-26">RFC 5545
	 * p.26-7</a>
	 */
	public void setRsvp(Boolean rsvp) {
		replace(RSVP, (rsvp == null) ? null : rsvp.toString().toUpperCase());
	}

	/**
	 * Gets a person that is acting on behalf of the person defined in the
	 * property.
	 * @return a URI representing the person (typically, an email URI, e.g.
	 * "mailto:janedoe@example.com") or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27</a>
	 */
	public String getSentBy() {
		return first(SENT_BY);
	}

	/**
	 * Sets a person that is acting on behalf of the person defined in the
	 * property.
	 * @param uri a URI representing the person (typically, an email URI, e.g.
	 * "mailto:janedoe@example.com") or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27</a>
	 */
	public void setSentBy(String uri) {
		replace(SENT_BY, uri);
	}

	/**
	 * Gets the timezone identifier. This either (a) references the
	 * {@link TimezoneId} property of a {@link VTimezone} component, or (b)
	 * specifies a globally-defined timezone (e.g. "America/New_York"). For a
	 * list of globally-defined timezones, see the <a
	 * href="http://www.twinsun.com/tz/tz-link.htm">TZ database</a>.
	 * @return the timezone identifier or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27-8</a>
	 */
	public String getTimezoneId() {
		return first(TZID);
	}

	/**
	 * Sets the timezone identifier. This either (a) references the
	 * {@link TimezoneId} property of a {@link VTimezone} component, or (b)
	 * specifies a globally-defined timezone (e.g. "America/New_York"). For a
	 * list of globally-defined timezones, see the <a
	 * href="http://www.twinsun.com/tz/tz-link.htm">TZ database</a>.
	 * @param timezoneId the timezone identifier or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27-8</a>
	 */
	public void setTimezoneId(String timezoneId) {
		replace(TZID, timezoneId);
	}

	/**
	 * Gets the data type of the property's value (for example, "text" or
	 * "datetime").
	 * @return the data type or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-29">RFC 5545
	 * p.29-50</a>
	 */
	public ICalDataType getValue() {
		String value = first(VALUE);
		return (value == null) ? null : ICalDataType.get(value);
	}

	/**
	 * Sets the data type of the property's value (for example, "text" or
	 * "datetime").
	 * @param value the data type or null to remove
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-29">RFC 5545
	 * p.29-50</a>
	 */
	public void setValue(ICalDataType value) {
		replace(VALUE, (value == null) ? null : value.getName());
	}

	/**
	 * Checks this parameters list for data consistency problems or deviations
	 * from the spec. These problems will not prevent the iCalendar object from
	 * being written to a data stream, but may prevent it from being parsed
	 * correctly by the consuming application.
	 * @return a list of warnings or an empty list if no problems were found
	 */
	public List<String> validate() {
		List<String> warnings = new ArrayList<String>(0);
		String message = "%s parameter has a non-standard value (\"%s\").  Standard values are: %s";

		String value = first(RSVP);
		if (value != null && !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
			warnings.add(String.format(message, RSVP, value, "[TRUE, FALSE]"));
		}

		value = first(CUTYPE);
		if (value != null && CalendarUserType.find(value) == null) {
			warnings.add(String.format(message, CUTYPE, value, CalendarUserType.all()));
		}

		value = first(ENCODING);
		if (value != null && Encoding.find(value) == null) {
			warnings.add(String.format(message, ENCODING, value, Encoding.all()));
		}

		value = first(FBTYPE);
		if (value != null && FreeBusyType.find(value) == null) {
			warnings.add(String.format(message, FBTYPE, value, FreeBusyType.all()));
		}

		value = first(PARTSTAT);
		if (value != null && ParticipationStatus.find(value) == null) {
			warnings.add(String.format(message, PARTSTAT, value, ParticipationStatus.all()));
		}

		value = first(RANGE);
		if (value != null && Range.find(value) == null) {
			warnings.add(String.format(message, RANGE, value, Range.all()));
		}

		value = first(RELATED);
		if (value != null && Related.find(value) == null) {
			warnings.add(String.format(message, RELATED, value, Related.all()));
		}

		value = first(RELTYPE);
		if (value != null && RelationshipType.find(value) == null) {
			warnings.add(String.format(message, RELTYPE, value, RelationshipType.all()));
		}

		value = first(ROLE);
		if (value != null && Role.find(value) == null) {
			warnings.add(String.format(message, ROLE, value, Role.all()));
		}

		value = first(VALUE);
		if (value != null && ICalDataType.find(value) == null) {
			warnings.add(String.format(message, VALUE, value, ICalDataType.all()));
		}

		return warnings;
	}

	@Override
	protected String sanitizeKey(String key) {
		return key.toUpperCase();
	}
}
