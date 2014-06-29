package biweekly.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import biweekly.component.DaylightSavingsTime;
import biweekly.component.StandardTime;
import biweekly.component.VTimezone;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.DateStart;
import biweekly.property.Daylight;
import biweekly.property.Organizer;
import biweekly.util.UtcOffset;

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
 * Converts various properties/components into other properties/components for
 * vCalendar-iCalendar compatibility.
 * @author Michael Angstadt
 */
public class DataModelConverter {
	/**
	 * Converts a {@link Daylight} property to a {@link VTimezone} component.
	 * @param daylight the DAYLIGHT property
	 * @return the VTIMEZONE component
	 */
	public static VTimezone convert(Daylight daylight) {
		VTimezone timezone = new VTimezone("TZ1");
		if (!daylight.isDaylight()) {
			return timezone;
		}

		UtcOffset offset = daylight.getOffset();

		//TODO convert all local dates to this timezone
		DaylightSavingsTime dst = new DaylightSavingsTime();
		dst.setDateStart(new DateStart(daylight.getStart()));
		dst.setTimezoneOffsetFrom(offset.getHour() - 1, offset.getMinute());
		dst.setTimezoneOffsetTo(offset.getHour(), offset.getMinute());
		dst.addTimezoneName(daylight.getDaylightName());
		timezone.addDaylightSavingsTime(dst);

		StandardTime st = new StandardTime();
		st.setDateStart(new DateStart(daylight.getEnd()));
		st.setTimezoneOffsetFrom(offset.getHour(), offset.getMinute());
		st.setTimezoneOffsetTo(offset.getHour() - 1, offset.getMinute());
		st.addTimezoneName(daylight.getStandardName());
		timezone.addStandardTime(st);

		return timezone;
	}

	/**
	 * Converts a {@link VTimezone} component into a list of {@link Daylight}
	 * properties.
	 * @param timezone the TIMEZONE component
	 * @return the DAYLIGHT properties
	 */
	public static List<Daylight> convert(VTimezone timezone) {
		List<DaylightSavingsTime> daylightSavingsTimes = timezone.getDaylightSavingsTime();
		List<StandardTime> standardTimes = timezone.getStandardTimes();
		List<Daylight> daylights = new ArrayList<Daylight>();

		int len = Math.max(daylightSavingsTimes.size(), standardTimes.size());
		for (int i = 0; i < len; i++) {
			DaylightSavingsTime daylightSavings = (i < daylightSavingsTimes.size()) ? daylightSavingsTimes.get(i) : null;
			StandardTime standard = (i < standardTimes.size()) ? standardTimes.get(i) : null;

			if (daylightSavings == null) {
				//there is no accompanying DAYLIGHT component, which means that daylight savings time is not observed
				daylights.add(new Daylight());
				continue;
			}

			if (standard == null) {
				//there is no accompanying STANDARD component, which makes no sense
				continue;
			}

			UtcOffset offset = daylightSavings.getTimezoneOffsetTo().getValue();
			Date start = daylightSavings.getDateStart().getValue();
			Date end = standard.getDateStart().getValue();
			String daylightName = (daylightSavings.getTimezoneNames().isEmpty()) ? null : daylightSavings.getTimezoneNames().get(0).getValue();
			String standardName = (standard.getTimezoneNames().isEmpty()) ? null : standard.getTimezoneNames().get(0).getValue();

			daylights.add(new Daylight(true, offset, start, end, standardName, daylightName));
		}

		return daylights;
	}

	/**
	 * Converts a {@link Attendee} property to a {@link Organizer} property.
	 * @param attendee the ATTENDEE property
	 * @return the ORGANIZER property
	 */
	public static Organizer convert(Attendee attendee) {
		Organizer organizer = new Organizer(attendee.getCommonName(), attendee.getEmail());
		organizer.setUri(attendee.getUri());
		organizer.setParameters(attendee.getParameters());
		return organizer;
	}

	/**
	 * Converts a {@link Organizer} property to a {@link Attendee} property.
	 * @param organizer the ORGANIZER property
	 * @return the ATTENDEE property
	 */
	public static Attendee convert(Organizer organizer) {
		Attendee attendee = new Attendee(organizer.getCommonName(), organizer.getEmail());
		attendee.setRole(Role.ORGANIZER);
		attendee.setUri(organizer.getUri());
		attendee.setParameters(organizer.getParameters());
		return attendee;
	}
}
