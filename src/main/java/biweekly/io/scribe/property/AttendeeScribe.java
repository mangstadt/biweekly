package biweekly.io.scribe.property;

import java.util.Iterator;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.DataModelConversionException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.ParticipationLevel;
import biweekly.parameter.ParticipationStatus;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.Organizer;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
 * Marshals {@link Attendee} properties.
 * @author Michael Angstadt
 */
public class AttendeeScribe extends ICalPropertyScribe<Attendee> {
	public AttendeeScribe() {
		super(Attendee.class, "ATTENDEE");
	}

	@Override
	protected ICalDataType _defaultDataType(ICalVersion version) {
		switch (version) {
		case V1_0:
			return null;
		default:
			return ICalDataType.CAL_ADDRESS;
		}
	}

	@Override
	protected ICalDataType _dataType(Attendee property, ICalVersion version) {
		if (version == ICalVersion.V1_0 && property.getUri() != null) {
			return ICalDataType.URL;
		}
		return defaultDataType(version);
	}

	@Override
	protected ICalParameters _prepareParameters(Attendee property, WriteContext context) {
		/*
		 * Note: Parameter values are assigned using "put()" instead of the
		 * appropriate "setter" methods so that any existing parameter values
		 * are not overwritten.
		 */

		ICalParameters copy = new ICalParameters(property.getParameters());

		prepareRsvpParameter(property, copy, context);
		prepareRoleAndExpectParameters(property, copy, context);
		prepareParticipationStatusParameter(property, copy, context);
		prepareCommonNameParameter(property, copy, context);
		prepareEmailParameter(property, copy, context);

		return copy;
	}

	private void prepareRsvpParameter(Attendee property, ICalParameters copy, WriteContext context) {
		Boolean rsvp = property.getRsvp();
		if (rsvp == null) {
			return;
		}

		String value;
		switch (context.getVersion()) {
		case V1_0:
			value = rsvp ? "YES" : "NO";
			break;

		default:
			value = rsvp ? "TRUE" : "FALSE";
			break;
		}

		copy.put(ICalParameters.RSVP, value);
	}

	private void prepareRoleAndExpectParameters(Attendee property, ICalParameters copy, WriteContext context) {
		Role role = property.getRole();
		ParticipationLevel level = property.getParticipationLevel();

		switch (context.getVersion()) {
		case V1_0:
			//1.0 - Uses ROLE and EXPECT
			if (role != null) {
				copy.put(ICalParameters.ROLE, role.getValue());
			}
			if (level != null) {
				copy.put(ICalParameters.EXPECT, level.getValue(context.getVersion()));
			}
			return;

		default:
			//2.0 - Uses only ROLE
			String value = null;
			if (role == Role.CHAIR) {
				value = role.getValue();
			} else if (level != null) {
				value = level.getValue(context.getVersion());
			} else if (role != null) {
				value = role.getValue();
			}

			if (value != null) {
				copy.put(ICalParameters.ROLE, value);
			}
			return;
		}
	}

	private void prepareParticipationStatusParameter(Attendee property, ICalParameters copy, WriteContext context) {
		ParticipationStatus partStat = property.getParticipationStatus();
		if (partStat == null) {
			return;
		}

		String name;
		String value;
		switch (context.getVersion()) {
		case V1_0:
			//1.0 - Calls the parameter "STATUS"
			//1.0 - "NEEDS ACTION" value has no hyphen
			name = ICalParameters.STATUS;
			value = (partStat == ParticipationStatus.NEEDS_ACTION) ? "NEEDS ACTION" : partStat.getValue();
			break;

		default:
			//2.0 - Calls the parameter "PARTSTAT"
			name = ICalParameters.PARTSTAT;
			value = partStat.getValue();
			break;
		}

		copy.put(name, value);
	}

	private void prepareCommonNameParameter(Attendee property, ICalParameters copy, WriteContext context) {
		if (context.getVersion() == ICalVersion.V1_0) {
			return;
		}

		String name = property.getCommonName();
		if (name == null) {
			return;
		}

		copy.put(ICalParameters.CN, name);
	}

	private void prepareEmailParameter(Attendee property, ICalParameters copy, WriteContext context) {
		if (context.getVersion() == ICalVersion.V1_0) {
			return;
		}

		String uri = property.getUri();
		String email = property.getEmail();
		if (uri == null || email == null) {
			return;
		}

		copy.put(ICalParameters.EMAIL, email);
	}

	@Override
	protected Attendee _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		switch (context.getVersion()) {
		case V1_0:
			return parseTextV1(value, dataType, parameters);

		default:
			return parseTextV2(value, parameters);
		}
	}

	private Attendee parseTextV1(String value, ICalDataType dataType, ICalParameters parameters) {
		Attendee attendee = parseValueV1(value, dataType);

		Boolean rsvp = parseAndRemoveRsvpParameter(parameters, "YES", "NO");
		attendee.setRsvp(rsvp);

		String roleStr = parameters.first(ICalParameters.ROLE);
		if (roleStr != null) {
			attendee.setRole(Role.get(roleStr));
			parameters.remove(ICalParameters.ROLE, roleStr);
		}

		String expectStr = parameters.getExpect();
		if (expectStr != null) {
			attendee.setParticipationLevel(ParticipationLevel.get(expectStr));
			parameters.remove(ICalParameters.EXPECT, expectStr);
		}

		String statusStr = parameters.getStatus();
		if (statusStr != null) {
			attendee.setParticipationStatus(ParticipationStatus.get(statusStr));
			parameters.remove(ICalParameters.STATUS, statusStr);
		}

		if (attendee.getRole() == Role.ORGANIZER) {
			Organizer organizer = new Organizer(attendee.getCommonName(), attendee.getEmail());
			organizer.setUri(attendee.getUri());
			organizer.setParameters(parameters);

			attendee.setParameters(parameters);
			DataModelConversionException conversionException = new DataModelConversionException(attendee);
			conversionException.getProperties().add(organizer);
			throw conversionException;
		}

		return attendee;
	}

	private Attendee parseValueV1(String value, ICalDataType dataType) {
		int bracketStart = value.lastIndexOf('<');
		int bracketEnd = value.lastIndexOf('>');
		if (bracketStart >= 0 && bracketEnd >= 0 && bracketStart < bracketEnd) {
			String name = value.substring(0, bracketStart).trim();
			String email = value.substring(bracketStart + 1, bracketEnd).trim();
			return new Attendee(name, email);
		}

		if (dataType == ICalDataType.URL) {
			return new Attendee(null, null, value);
		}

		return new Attendee(null, value);
	}

	private Attendee parseTextV2(String value, ICalParameters parameters) {
		String name = parameters.getCommonName();
		if (name != null) {
			parameters.remove(ICalParameters.CN, name);
		}

		String email = parameters.getEmail();
		if (email != null) {
			parameters.remove(ICalParameters.EMAIL, email);
		}

		String uri = value;
		if (email == null) {
			email = parseEmailFromMailtoUri(value);
			if (email != null) {
				uri = null;
			}
		}

		Attendee attendee = new Attendee(name, email, uri);

		Boolean rsvp = parseAndRemoveRsvpParameter(parameters, "TRUE", "FALSE");
		attendee.setRsvp(rsvp);

		String roleStr = parameters.first(ICalParameters.ROLE);
		if (roleStr != null) {
			if (roleStr.equalsIgnoreCase(Role.CHAIR.getValue())) {
				attendee.setRole(Role.CHAIR);
			} else {
				ParticipationLevel participationLevel = ParticipationLevel.find(roleStr);
				if (participationLevel == null) {
					attendee.setRole(Role.get(roleStr));
				} else {
					attendee.setParticipationLevel(participationLevel);
				}
			}
			parameters.remove(ICalParameters.ROLE, roleStr);
		}

		String participationStatusStr = parameters.getParticipationStatus();
		if (participationStatusStr != null) {
			attendee.setParticipationStatus(ParticipationStatus.get(participationStatusStr));
			parameters.remove(ICalParameters.PARTSTAT, participationStatusStr);
		}

		return attendee;
	}

	private String parseEmailFromMailtoUri(String value) {
		String mailtoScheme = "mailto";
		int colon = value.indexOf(':');
		if (colon == mailtoScheme.length()) {
			String scheme = value.substring(0, colon);
			if (mailtoScheme.equalsIgnoreCase(scheme)) {
				return value.substring(colon + 1);
			}
		}

		return null;
	}

	private Boolean parseAndRemoveRsvpParameter(ICalParameters parameters, String trueValue, String falseValue) {
		Iterator<String> it = parameters.get(ICalParameters.RSVP).iterator();
		while (it.hasNext()) {
			String rsvpStr = it.next();

			if (trueValue.equalsIgnoreCase(rsvpStr)) {
				it.remove();
				return true;
			}

			if (falseValue.equalsIgnoreCase(rsvpStr)) {
				it.remove();
				return false;
			}
		}

		return null;
	}

	@Override
	protected String _writeText(Attendee property, WriteContext context) {
		String uri = property.getUri();
		if (uri != null) {
			return uri;
		}

		String name = property.getCommonName();
		String email = property.getEmail();
		switch (context.getVersion()) {
		case V1_0:
			if (email != null) {
				String value = (name == null) ? email : name + " <" + email + ">";
				return VObjectPropertyValues.escape(value);
			}

			break;

		default:
			if (email != null) {
				return "mailto:" + email;
			}
			break;
		}

		return "";
	}
}
