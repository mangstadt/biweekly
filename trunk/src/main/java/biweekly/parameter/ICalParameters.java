package biweekly.parameter;

import java.util.List;

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
	private static final String CN = "CN";
	private static final String ALTREP = "ALTREP";
	private static final String CUTYPE = "CUTYPE";
	private static final String DELEGATED_FROM = "DELEGATED-FROM";
	private static final String DELEGATED_TO = "DELEGATED-TO";
	private static final String DIR = "DIR";
	private static final String ENCODING = "ENCODING";
	private static final String FMTTYPE = "FMTTYPE";
	private static final String FBTYPE = "FBTYPE";
	private static final String LANGUAGE = "LANGUAGE";
	private static final String MEMBER = "MEMBER";
	private static final String PARTSTAT = "PARTSTAT";
	private static final String RANGE = "RANGE";
	private static final String RELATED = "RELATED";
	private static final String RELTYPE = "RELTYPE";
	private static final String ROLE = "ROLE";
	private static final String RSVP = "RSVP";
	private static final String SENT_BY = "SENT-BY";
	private static final String TZID = "TZID";
	private static final String VALUE = "VALUE";

	public ICalParameters() {
		super(0); //initialize map size to 0 because most properties don't use any parameters
	}

	public ICalParameters(ICalParameters parameters) {
		super(parameters);
	}

	public String getAltRepresentation() {
		return first(ALTREP);
	}

	public void setAltRepresentation(String uri) {
		replace(ALTREP, uri);
	}

	public String getCommonName() {
		return first(CN);
	}

	public void setCommonName(String cn) {
		replace(CN, cn);
	}

	public CalendarUserType getCalendarUserType() {
		String value = first(CUTYPE);
		return (value == null) ? null : CalendarUserType.get(value);
	}

	public void setCalendarUserType(CalendarUserType cutype) {
		replace(CUTYPE, (cutype == null) ? null : cutype.getValue());
	}

	public List<String> getDelegatedFrom() {
		return get(DELEGATED_FROM);
	}

	public void addDelegatedFrom(String delegatedFrom) {
		put(DELEGATED_FROM, delegatedFrom);
	}

	public void removeDelegatedFrom(String delegatedFrom) {
		remove(DELEGATED_FROM, delegatedFrom);
	}

	public void removeAllDelegatedFrom() {
		removeAll(DELEGATED_FROM);
	}

	public List<String> getDelegatedTo() {
		return get(DELEGATED_TO);
	}

	public void addDelegatedTo(String delegatedTo) {
		put(DELEGATED_TO, delegatedTo);
	}

	public void removeDelegatedTo(String delegatedTo) {
		remove(DELEGATED_TO, delegatedTo);
	}

	public void removeAllDelegatedTo() {
		removeAll(DELEGATED_TO);
	}

	public String getDirectoryEntry() {
		return first(DIR);
	}

	public void setDirectoryEntry(String dir) {
		replace(DIR, dir);
	}

	public Encoding getEncoding() {
		String value = first(ENCODING);
		return (value == null) ? null : Encoding.get(value);
	}

	public void setEncoding(Encoding encoding) {
		replace(ENCODING, (encoding == null) ? null : encoding.getValue());
	}

	public String getFormatType() {
		return first(FMTTYPE);
	}

	public void setFormatType(String formatType) {
		replace(FMTTYPE, formatType);
	}

	public FreeBusyType getFreeBusyType() {
		String value = first(FBTYPE);
		return (value == null) ? null : FreeBusyType.get(value);
	}

	public void setFreeBusyType(FreeBusyType fbType) {
		replace(FBTYPE, (fbType == null) ? null : fbType.getValue());
	}

	public String getLanguage() {
		return first(LANGUAGE);
	}

	public void setLanguage(String language) {
		replace(LANGUAGE, language);
	}

	public void addMember(String member) {
		put(MEMBER, member);
	}

	public List<String> getMembers() {
		return get(MEMBER);
	}

	public void removeMember(String member) {
		remove(MEMBER, member);
	}

	public void removeAllMembers() {
		removeAll(MEMBER);
	}

	public ParticipationStatus getParticipationStatus() {
		String value = first(PARTSTAT);
		return (value == null) ? null : ParticipationStatus.get(value);
	}

	public void setParticipationStatus(ParticipationStatus partstat) {
		replace(PARTSTAT, (partstat == null) ? null : partstat.getValue());
	}

	public Range getRange() {
		String value = first(RANGE);
		return (value == null) ? null : Range.get(value);
	}

	public void setRange(Range range) {
		replace(RANGE, (range == null) ? null : range.getValue());
	}

	public Related getRelated() {
		String value = first(RELATED);
		return (value == null) ? null : Related.get(value);
	}

	public void setRelated(Related related) {
		replace(RELATED, (related == null) ? null : related.getValue());
	}

	public RelationshipType getRelationshipType() {
		String value = first(RELTYPE);
		return (value == null) ? null : RelationshipType.get(value);
	}

	public void setRelationshipType(RelationshipType relationshipType) {
		replace(RELTYPE, (relationshipType == null) ? null : relationshipType.getValue());
	}

	public Role getRole() {
		String value = first(ROLE);
		return (value == null) ? null : Role.get(value);
	}

	public void setRole(Role role) {
		replace(ROLE, (role == null) ? null : role.getValue());
	}

	public Boolean getRsvp() {
		String value = first(RSVP);
		return (value == null) ? null : Boolean.valueOf(value);
	}

	public void setRsvp(Boolean rsvp) {
		replace(RSVP, (rsvp == null) ? null : rsvp.toString().toUpperCase());
	}

	public String getSentBy() {
		return first(SENT_BY);
	}

	public void setSentBy(String sentBy) {
		replace(SENT_BY, sentBy);
	}

	public String getTimezoneId() {
		return first(TZID);
	}

	public void setTimezoneId(String timezoneId) {
		replace(TZID, timezoneId);
	}

	public Value getValue() {
		String value = first(VALUE);
		return (value == null) ? null : Value.get(value);
	}

	public void setValue(Value value) {
		replace(VALUE, (value == null) ? null : value.getValue());
	}

	@Override
	protected String sanitizeKey(String key) {
		return key.toUpperCase();
	}
}
