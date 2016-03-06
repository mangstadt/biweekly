package biweekly.parameter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.property.FreeBusy;
import biweekly.property.RecurrenceId;
import biweekly.property.RelatedTo;
import biweekly.property.Trigger;
import biweekly.util.CharacterBitSet;
import biweekly.util.ListMultimap;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
	public static final String CHARSET = "CHARSET"; //1.0 only
	public static final String CUTYPE = "CUTYPE";
	public static final String DELEGATED_FROM = "DELEGATED-FROM";
	public static final String DELEGATED_TO = "DELEGATED-TO";
	public static final String DIR = "DIR";
	public static final String DISPLAY = "DISPLAY";
	public static final String EMAIL = "EMAIL";
	public static final String ENCODING = "ENCODING";
	public static final String FEATURE = "FEATURE";
	public static final String FMTTYPE = "FMTTYPE";
	public static final String FBTYPE = "FBTYPE";
	public static final String LABEL = "LABEL";
	public static final String LANGUAGE = "LANGUAGE";
	public static final String MEMBER = "MEMBER";
	public static final String PARTSTAT = "PARTSTAT";
	public static final String RANGE = "RANGE";
	public static final String RELATED = "RELATED";
	public static final String RELTYPE = "RELTYPE";
	public static final String ROLE = "ROLE";
	public static final String RSVP = "RSVP";
	public static final String SENT_BY = "SENT-BY";
	public static final String TYPE = "TYPE"; //1.0 only
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
	 * Gets the character set that the property value is encoded in.
	 * @return the character set or null if not set
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.16</a>
	 */
	public String getCharset() {
		return first(CHARSET);
	}

	/**
	 * Sets the character set that the property value is encoded in.
	 * @param charset the character set or null to remove
	 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.16</a>
	 */
	public void setCharset(String charset) {
		replace(CHARSET, charset);
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
	 * Gets a human-readable label for the property.
	 * @return the label or null if not set
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-16">draft-ietf-calext-extensions-01
	 * p.16</a>
	 */
	public String getLabel() {
		return first(LABEL);
	}

	/**
	 * Sets a human-readable label for the property.
	 * @param label the label or null to remove
	 * @see <a
	 * href="http://tools.ietf.org/html/draft-ietf-calext-extensions-01#page-16">draft-ietf-calext-extensions-01
	 * p.16</a>
	 */
	public void setLabel(String label) {
		replace(LABEL, label);
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
	 * Gets the TZID property, which defines the timezone that this property is
	 * formatted in. It is either the ID of the VTIMEZONE component which
	 * contains the timezone definition, or globally unique ID (if it starts
	 * with a "/" character).
	 * @return the timezone ID or null if not set
	 * @see <a href="http://tools.ietf.org/html/rfc5545#page-27">RFC 5545
	 * p.27-8</a>
	 */
	public String getTimezoneId() {
		return first(TZID);
	}

	/**
	 * Sets the TZID property, which defines the timezone that this property is
	 * formatted in. It is either the ID of the VTIMEZONE component which
	 * contains the timezone definition, or globally unique ID (if it starts
	 * with a "/" character).
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
	 * @param version the version to validate against
	 * @return a list of warnings or an empty list if no problems were found
	 */
	public List<Warning> validate(ICalVersion version) {
		List<Warning> warnings = new ArrayList<Warning>(0);

		/*
		 * Check for invalid characters in names and values.
		 */
		{
			BitSet invalidValueChars = new BitSet(128);
			invalidValueChars.set(0, 31);
			invalidValueChars.set(127);
			invalidValueChars.set('\t', false); //allow
			invalidValueChars.set('\n', false); //allow
			invalidValueChars.set('\r', false); //allow
			if (version == ICalVersion.V1_0) {
				invalidValueChars.set(',');
				invalidValueChars.set('.');
				invalidValueChars.set(':');
				invalidValueChars.set('=');
				invalidValueChars.set('[');
				invalidValueChars.set(']');
			}

			CharacterBitSet validNameChars = new CharacterBitSet("-a-zA-Z0-9");
			for (Map.Entry<String, List<String>> entry : this) {
				String name = entry.getKey();

				//check the parameter name
				if (!validNameChars.containsOnly(name)) {
					warnings.add(Warning.validate(54, name));
				}

				//check the parameter value(s)
				List<String> values = entry.getValue();
				for (String value : values) {
					for (int i = 0; i < value.length(); i++) {
						char c = value.charAt(i);
						if (invalidValueChars.get(c)) {
							warnings.add(Warning.validate(53, name, value, (int) c, i));
							break;
						}
					}
				}
			}
		}

		final int nonStandardCode = 1, deprecated = 47;

		String value = first(RSVP);
		if (value != null) {
			value = value.toLowerCase();
			List<String> validValues = Arrays.asList("true", "false", "yes", "no");
			if (!validValues.contains(value)) {
				warnings.add(Warning.validate(nonStandardCode, RSVP, value, validValues));
			}
		}

		value = first(CUTYPE);
		if (value != null && CalendarUserType.find(value) == null) {
			warnings.add(Warning.validate(nonStandardCode, CUTYPE, value, CalendarUserType.all()));
		}

		value = first(ENCODING);
		if (value != null && Encoding.find(value) == null) {
			warnings.add(Warning.validate(nonStandardCode, ENCODING, value, Encoding.all()));
		}

		value = first(FBTYPE);
		if (value != null && FreeBusyType.find(value) == null) {
			warnings.add(Warning.validate(nonStandardCode, FBTYPE, value, FreeBusyType.all()));
		}

		value = first(PARTSTAT);
		if (value != null && ParticipationStatus.find(value) == null) {
			warnings.add(Warning.validate(nonStandardCode, PARTSTAT, value, ParticipationStatus.all()));
		}

		value = first(RANGE);
		if (value != null) {
			Range range = Range.find(value);

			if (range == null) {
				warnings.add(Warning.validate(nonStandardCode, RANGE, value, Range.all()));
			}

			if (range == Range.THIS_AND_PRIOR && version == ICalVersion.V2_0) {
				warnings.add(Warning.validate(deprecated, RANGE, value));
			}
		}

		value = first(RELATED);
		if (value != null && Related.find(value) == null) {
			warnings.add(Warning.validate(nonStandardCode, RELATED, value, Related.all()));
		}

		value = first(RELTYPE);
		if (value != null && RelationshipType.find(value) == null) {
			warnings.add(Warning.validate(nonStandardCode, RELTYPE, value, RelationshipType.all()));
		}

		value = first(ROLE);
		if (value != null && Role.find(value) == null) {
			warnings.add(Warning.validate(nonStandardCode, ROLE, value, Role.all()));
		}

		value = first(VALUE);
		if (value != null && ICalDataType.find(value) == null) {
			warnings.add(Warning.validate(nonStandardCode, VALUE, value, ICalDataType.all()));
		}

		return warnings;
	}

	@Override
	protected String sanitizeKey(String key) {
		return (key == null) ? null : key.toUpperCase();
	}

	@Override
	public int hashCode() {
		/*
		 * Remember: Keys are case-insensitive, key order does not matter, and
		 * value order does not matter
		 */
		final int prime = 31;
		int result = 1;

		for (Map.Entry<String, List<String>> entry : this) {
			String key = entry.getKey();
			List<String> value = entry.getValue();

			int valueHash = 1;
			for (String v : value) {
				valueHash += v.toLowerCase().hashCode();
			}

			int entryHash = 1;
			entryHash += prime * entryHash + key.toLowerCase().hashCode();
			entryHash += prime * entryHash + valueHash;

			result += entryHash;
		}

		return result;
	}

	/**
	 * <p>
	 * Determines whether the given object is logically equivalent to this list
	 * of iCalendar parameters.
	 * </p>
	 * <p>
	 * iCalendar parameters are case-insensitive. Also, the order in which they
	 * are defined does not matter.
	 * </p>
	 * @param obj the object to compare to
	 * @return true if the objects are equal, false if not
	 */
	@Override
	public boolean equals(Object obj) {
		/*
		 * Remember: Keys are case-insensitive, key order does not matter, and
		 * value order does not matter
		 */
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		ICalParameters other = (ICalParameters) obj;
		if (size() != other.size()) return false;

		for (Map.Entry<String, List<String>> entry : this) {
			String key = entry.getKey();
			List<String> value = entry.getValue();
			List<String> otherValue = other.get(key);

			if (value.size() != otherValue.size()) {
				return false;
			}

			List<String> valueLower = new ArrayList<String>(value.size());
			for (String v : value) {
				valueLower.add(v.toLowerCase());
			}
			Collections.sort(valueLower);

			List<String> otherValueLower = new ArrayList<String>(otherValue.size());
			for (String v : otherValue) {
				otherValueLower.add(v.toLowerCase());
			}
			Collections.sort(otherValueLower);

			if (!valueLower.equals(otherValueLower)) {
				return false;
			}
		}

		return true;
	}
}
