package biweekly.io.scribe.property;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.ParticipationLevel;
import biweekly.parameter.ParticipationStatus;
import biweekly.parameter.Role;
import biweekly.property.Attendee;

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
 * Marshals {@link Attendee} properties.
 * @author Michael Angstadt
 */
public class AttendeeScribe extends ICalPropertyScribe<Attendee> {
	public AttendeeScribe() {
		super(Attendee.class, "ATTENDEE", ICalDataType.CAL_ADDRESS);
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
		ICalParameters copy = new ICalParameters(property.getParameters());

		//RSVP parameter
		//1.0 - Uses the values "YES" and "NO"
		//2.0 - Uses the values "TRUE" and "FALSE"
		Boolean rsvp = property.getRsvp();
		if (rsvp != null) {
			String value = null;
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

		//ROLE and EXPECT parameters
		//1.0 - Uses ROLE and EXPECT
		//2.0 - Uses only ROLE
		Role role = property.getRole();
		ParticipationLevel level = property.getParticipationLevel();
		switch (context.getVersion()) {
		case V1_0:
			if (role != null) {
				copy.put("ROLE", role.getValue());
			}
			if (level != null) {
				copy.put("EXPECT", level.getValue(context.getVersion()));
			}
			break;

		default:
			String value = null;
			if (role == Role.CHAIR) {
				value = role.getValue();
			} else if (level != null) {
				value = level.getValue(context.getVersion());
			} else if (role != null) {
				value = role.getValue();
			}

			if (value != null) {
				copy.put("ROLE", value);
			}
			break;
		}

		//PARTSTAT vs STATUS
		//1.0 - Calls the parameter "STATUS"
		//2.0 - Calls the parameter "PARTSTAT"
		ParticipationStatus partStat = property.getParticipationStatus();
		if (partStat != null) {
			String paramName;
			String paramValue;

			switch (context.getVersion()) {
			case V1_0:
				paramName = "STATUS";
				paramValue = (partStat == ParticipationStatus.NEEDS_ACTION) ? "NEEDS ACTION" : partStat.getValue();
				break;

			default:
				paramName = "PARTSTAT";
				paramValue = partStat.getValue();
				break;
			}

			copy.put(paramName, paramValue);
		}

		//CN parameter
		String name = property.getCommonName();
		if (name != null && context.getVersion() != ICalVersion.V1_0) {
			copy.put(ICalParameters.CN, name);
		}

		return copy;
	}

	@Override
	protected Attendee _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String uri = null, name = null, email = null;

		Boolean rsvp = null;
		String rsvpStr = parameters.first(ICalParameters.RSVP);

		Role role = null;
		String roleStr = parameters.first(ICalParameters.ROLE);

		ParticipationLevel level = null;
		ParticipationStatus status = null;

		switch (context.getVersion()) {
		case V1_0:
			if (rsvpStr != null) {
				if ("YES".equalsIgnoreCase(rsvpStr)) {
					rsvp = Boolean.TRUE;
				} else if ("NO".equalsIgnoreCase(rsvpStr)) {
					rsvp = Boolean.FALSE;
				}
			}

			if (roleStr != null) {
				role = Role.get(roleStr);
			}

			String expect = parameters.first("EXPECT");
			if (expect != null) {
				parameters.remove("EXPECT", expect);
				level = ParticipationLevel.get(expect);
			}

			String statusStr = parameters.first("STATUS");
			if (statusStr != null) {
				parameters.remove("STATUS", statusStr);
				status = ParticipationStatus.get(statusStr);
			}

			Pattern p = Pattern.compile("^(.*?)<(.*?)>$");
			Matcher m = p.matcher(value);
			if (m.find()) {
				name = m.group(1).trim();
				email = m.group(2).trim();
			} else if (dataType == ICalDataType.URL) {
				uri = value;
			} else {
				email = value;
			}

			break;

		default:
			if (rsvpStr != null) {
				if ("TRUE".equalsIgnoreCase(rsvpStr)) {
					rsvp = Boolean.TRUE;
				} else if ("FALSE".equalsIgnoreCase(rsvpStr)) {
					rsvp = Boolean.FALSE;
				}
			}

			if (roleStr != null) {
				if (roleStr.equalsIgnoreCase(Role.CHAIR.getValue())) {
					role = Role.CHAIR;
				} else {
					ParticipationLevel l = ParticipationLevel.find(roleStr);
					if (l == null) {
						role = Role.get(roleStr);
					} else {
						level = l;
					}
				}
			}

			String partStat = parameters.first("PARTSTAT");
			if (partStat != null) {
				parameters.remove("PARTSTAT", partStat);
				status = ParticipationStatus.get(partStat);
			}

			name = parameters.first(ICalParameters.CN);
			if (name != null) {
				parameters.remove(ICalParameters.CN, name);
			}

			p = Pattern.compile("^mailto:(.*?)$");
			m = p.matcher(value);
			if (m.find()) {
				email = m.group(1);
			} else {
				uri = value;
			}

			break;
		}

		if (rsvp != null) {
			parameters.removeAll(ICalParameters.RSVP);
		}
		if (roleStr != null) {
			parameters.remove(ICalParameters.ROLE, roleStr);
		}

		Attendee attendee = new Attendee(name, email);
		attendee.setParticipationStatus(status);
		attendee.setParticipationLevel(level);
		attendee.setRole(role);
		attendee.setRsvp(rsvp);
		attendee.setUri(uri);

		return attendee;
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
			if (name != null && email != null) {
				return escape(name + " <" + email + ">");
			}
			if (email != null) {
				return escape(email);
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