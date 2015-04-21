package biweekly.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import biweekly.component.DaylightSavingsTime;
import biweekly.component.ICalComponent;
import biweekly.component.Observance;
import biweekly.component.StandardTime;
import biweekly.component.VAlarm;
import biweekly.component.VTimezone;
import biweekly.io.ICalTimeZone.Boundary;
import biweekly.parameter.Related;
import biweekly.parameter.Role;
import biweekly.property.Action;
import biweekly.property.Attachment;
import biweekly.property.Attendee;
import biweekly.property.AudioAlarm;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.Daylight;
import biweekly.property.Description;
import biweekly.property.DisplayAlarm;
import biweekly.property.DurationProperty;
import biweekly.property.EmailAlarm;
import biweekly.property.Organizer;
import biweekly.property.ProcedureAlarm;
import biweekly.property.Repeat;
import biweekly.property.Timezone;
import biweekly.property.Trigger;
import biweekly.property.UtcOffsetProperty;
import biweekly.property.VCalAlarmProperty;
import biweekly.util.DateTimeComponents;
import biweekly.util.Duration;
import biweekly.util.ICalDate;
import biweekly.util.UtcOffset;

import com.google.ical.values.DateTimeValue;

/*
 Copyright (c) 2013-2015, Michael Angstadt
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
	 * Converts vCalendar timezone information to am iCalendar {@link VTimezone}
	 * component.
	 * @param daylights the DAYLIGHT properties
	 * @param tz the TZ property
	 * @return the VTIMEZONE component
	 */
	public static VTimezone convert(List<Daylight> daylights, Timezone tz) {
		UtcOffset tzOffset = (tz == null) ? null : tz.getValue();
		if (daylights.isEmpty() && tzOffset == null) {
			return null;
		}

		VTimezone timezone = new VTimezone("TZ");
		if (daylights.isEmpty() && tzOffset != null) {
			StandardTime st = new StandardTime();
			st.setTimezoneOffsetFrom(tzOffset);
			st.setTimezoneOffsetTo(tzOffset);
			timezone.addStandardTime(st);
			return timezone;
		}

		for (Daylight daylight : daylights) {
			if (!daylight.isDaylight()) {
				continue;
			}

			UtcOffset daylightOffset = daylight.getOffset();
			UtcOffset standardOffset = new UtcOffset(daylightOffset.getHour() - 1, daylightOffset.getMinute());

			DaylightSavingsTime dst = new DaylightSavingsTime();
			dst.setDateStart(daylight.getStart());
			dst.setTimezoneOffsetFrom(standardOffset);
			dst.setTimezoneOffsetTo(daylightOffset);
			dst.addTimezoneName(daylight.getDaylightName());
			timezone.addDaylightSavingsTime(dst);

			StandardTime st = new StandardTime();
			st.setDateStart(daylight.getEnd());
			st.setTimezoneOffsetFrom(daylightOffset);
			st.setTimezoneOffsetTo(standardOffset);
			st.addTimezoneName(daylight.getStandardName());
			timezone.addStandardTime(st);
		}

		return timezone.getComponents().isEmpty() ? null : timezone;
	}

	/**
	 * Converts an iCalendar {@link VTimezone} component into the appropriate
	 * vCalendar properties.
	 * @param timezone the TIMEZONE component
	 * @param dates the date values in the vCalendar object that are effected by
	 * the timezone.
	 * @return the vCalendar properties
	 */
	public static VCalTimezoneProperties convert(VTimezone timezone, List<Date> dates) {
		List<Daylight> daylights = new ArrayList<Daylight>();
		Timezone tz = null;
		if (dates.isEmpty()) {
			return new VCalTimezoneProperties(daylights, tz);
		}

		ICalTimeZone icalTz = new ICalTimeZone(timezone);
		Collections.sort(dates);
		Set<DateTimeValue> daylightStartDates = new HashSet<DateTimeValue>();
		boolean zeroObservanceUsed = false;
		for (Date date : dates) {
			Boundary boundary = icalTz.getObservanceBoundary(date);
			Observance observance = boundary.getObservanceIn();
			Observance observanceAfter = boundary.getObservanceAfter();
			if (observance == null && observanceAfter == null) {
				continue;
			}

			if (observance == null) {
				//the date comes before the earliest observance
				if (observanceAfter instanceof StandardTime && !zeroObservanceUsed) {
					UtcOffset offset = getOffset(observanceAfter.getTimezoneOffsetFrom());
					DateTimeValue start = null;
					DateTimeValue end = boundary.getObservanceAfterStart();
					String standardName = icalTz.getDisplayName(false, TimeZone.SHORT);
					String daylightName = icalTz.getDisplayName(true, TimeZone.SHORT);

					Daylight daylight = new Daylight(true, offset, convert(start), convert(end), standardName, daylightName);
					daylights.add(daylight);
					zeroObservanceUsed = true;
				}

				if (observanceAfter instanceof DaylightSavingsTime) {
					UtcOffset offset = getOffset(observanceAfter.getTimezoneOffsetFrom());
					if (offset != null) {
						tz = new Timezone(offset);
					}
				}

				continue;
			}

			if (observance instanceof StandardTime) {
				UtcOffset offset = getOffset(observance.getTimezoneOffsetTo());
				if (offset != null) {
					tz = new Timezone(offset);
				}
				continue;
			}

			if (observance instanceof DaylightSavingsTime && !daylightStartDates.contains(boundary.getObservanceInStart())) {
				UtcOffset offset = getOffset(observance.getTimezoneOffsetTo());
				DateTimeValue start = boundary.getObservanceInStart();
				DateTimeValue end = null;
				if (observanceAfter != null) {
					end = boundary.getObservanceAfterStart();
				}

				String standardName = icalTz.getDisplayName(false, TimeZone.SHORT);
				String daylightName = icalTz.getDisplayName(true, TimeZone.SHORT);

				Daylight daylight = new Daylight(true, offset, convert(start), convert(end), standardName, daylightName);
				daylights.add(daylight);
				daylightStartDates.add(start);
				continue;
			}
		}

		if (tz == null) {
			int rawOffset = icalTz.getRawOffset();
			UtcOffset offset = new UtcOffset(rawOffset);
			tz = new Timezone(offset);
		}

		if (daylights.isEmpty()) {
			Daylight daylight = new Daylight();
			daylight.setDaylight(false);
			daylights.add(daylight);
		}

		return new VCalTimezoneProperties(daylights, tz);
	}

	private static UtcOffset getOffset(UtcOffsetProperty property) {
		return (property == null) ? null : property.getValue();
	}

	private static ICalDate convert(DateTimeValue value) {
		if (value == null) {
			return null;
		}

		//@formatter:off
		DateTimeComponents components = new DateTimeComponents(
			value.year(),
			value.month(),
			value.day(),
			value.hour(),
			value.minute(),
			value.second(),
			false
		);
		//@formatter:on

		return new ICalDate(components, true);
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

	/**
	 * Converts a {@link AudioAlarm} property to a {@link VAlarm} component.
	 * @param aalarm the AALARM property
	 * @return the VALARM component
	 */
	public static VAlarm convert(AudioAlarm aalarm) {
		Trigger trigger = new Trigger(aalarm.getStart());
		VAlarm valarm = new VAlarm(Action.audio(), trigger);

		valarm.addAttachment(buildAttachment(aalarm));
		valarm.setDuration(aalarm.getSnooze());
		valarm.setRepeat(aalarm.getRepeat());

		return valarm;
	}

	/**
	 * Converts a {@link DisplayAlarm} property to a {@link VAlarm} component.
	 * @param dalarm the DALARM property
	 * @return the VALARM component
	 */
	public static VAlarm convert(DisplayAlarm dalarm) {
		Trigger trigger = new Trigger(dalarm.getStart());
		VAlarm valarm = new VAlarm(Action.display(), trigger);

		valarm.setDescription(dalarm.getText());
		valarm.setDuration(dalarm.getSnooze());
		valarm.setRepeat(dalarm.getRepeat());

		return valarm;
	}

	/**
	 * Converts a {@link EmailAlarm} property to a {@link VAlarm} component.
	 * @param malarm the MALARM property
	 * @return the VALARM component
	 */
	public static VAlarm convert(EmailAlarm malarm) {
		Trigger trigger = new Trigger(malarm.getStart());
		VAlarm valarm = new VAlarm(Action.email(), trigger);

		String email = malarm.getEmail();
		if (email != null) {
			valarm.addAttendee(new Attendee(null, email));
		}
		valarm.setDescription(malarm.getNote());
		valarm.setDuration(malarm.getSnooze());
		valarm.setRepeat(malarm.getRepeat());

		return valarm;
	}

	/**
	 * Converts a {@link ProcedureAlarm} property to a {@link VAlarm} component.
	 * @param dalarm the PALARM property
	 * @return the VALARM component
	 */
	public static VAlarm convert(ProcedureAlarm dalarm) {
		Trigger trigger = new Trigger(dalarm.getStart());
		VAlarm valarm = new VAlarm(Action.procedure(), trigger);

		valarm.setDescription(dalarm.getPath());
		valarm.setDuration(dalarm.getSnooze());
		valarm.setRepeat(dalarm.getRepeat());

		return valarm;
	}

	private static Attachment buildAttachment(AudioAlarm aalarm) {
		String type = aalarm.getParameter("TYPE");
		String contentType = (type == null) ? null : "audio/" + type.toLowerCase();
		byte data[] = aalarm.getData();
		if (data != null) {
			return new Attachment(contentType, data);
		}

		String contentId = aalarm.getContentId();
		String uri = (contentId == null) ? aalarm.getUri() : "CID:" + contentId;
		return new Attachment(contentType, uri);
	}

	/**
	 * Converts a {@link VAlarm} component to a vCal alarm property.
	 * @param valarm the VALARM component
	 * @param parent the component that holds the VALARM component
	 * @return the alarm property
	 */
	public static VCalAlarmProperty convert(VAlarm valarm, ICalComponent parent) {
		Action action = valarm.getAction();
		if (action == null) {
			return null;
		}

		if (action.isAudio()) {
			AudioAlarm aalarm = new AudioAlarm();
			aalarm.setStart(determineStartDate(valarm, parent));

			List<Attachment> attaches = valarm.getAttachments();
			if (!attaches.isEmpty()) {
				Attachment attach = attaches.get(0);

				String formatType = attach.getFormatType();
				aalarm.setParameter("TYPE", formatType);

				byte[] data = attach.getData();
				if (data != null) {
					aalarm.setData(data);
				}

				String uri = attach.getUri();
				if (uri != null) {
					if (uri.toUpperCase().startsWith("CID:")) {
						String contentId = uri.substring(4);
						aalarm.setContentId(contentId);
					} else {
						aalarm.setUri(uri);
					}
				}
			}

			DurationProperty duration = valarm.getDuration();
			if (duration != null) {
				aalarm.setSnooze(duration.getValue());
			}

			Repeat repeat = valarm.getRepeat();
			if (repeat != null) {
				aalarm.setRepeat(repeat.getValue());
			}

			return aalarm;
		}

		if (action.isDisplay()) {
			Description description = valarm.getDescription();
			String text = (description == null) ? null : description.getValue();
			return new DisplayAlarm(text);
		}

		if (action.isEmail()) {
			List<Attendee> attendees = valarm.getAttendees();
			String email = attendees.isEmpty() ? null : attendees.get(0).getEmail();
			EmailAlarm malarm = new EmailAlarm(email);

			Description description = valarm.getDescription();
			String note = (description == null) ? null : description.getValue();
			malarm.setNote(note);

			return malarm;
		}

		if (action.isProcedure()) {
			Description description = valarm.getDescription();
			String path = (description == null) ? null : description.getValue();
			return new ProcedureAlarm(path);
		}

		return null;
	}

	private static Date determineStartDate(VAlarm valarm, ICalComponent parent) {
		Trigger trigger = valarm.getTrigger();
		if (trigger == null) {
			return null;
		}

		Date start = trigger.getDate();
		if (start != null) {
			return start;
		}

		Duration triggerDuration = trigger.getDuration();
		if (triggerDuration == null) {
			return null;
		}

		Related related = trigger.getRelated();
		if (related == Related.START) {
			DateStart parentDateStart = parent.getProperty(DateStart.class);
			if (parentDateStart == null) {
				return null;
			}

			Date date = parentDateStart.getValue();
			return (date == null) ? null : triggerDuration.add(date);
		}

		if (related == Related.END) {
			DateEnd parentDateEnd = parent.getProperty(DateEnd.class);
			if (parentDateEnd != null) {
				Date date = parentDateEnd.getValue();
				return (date == null) ? null : triggerDuration.add(date);
			}

			DateStart parentDateStart = parent.getProperty(DateStart.class);
			DurationProperty parentDuration = parent.getProperty(DurationProperty.class);
			if (parentDuration == null || parentDateStart == null) {
				return null;
			}

			Duration duration = parentDuration.getValue();
			Date date = parentDateStart.getValue();
			return (duration == null || date == null) ? null : duration.add(date);
		}

		return null;
	}

	public static class VCalTimezoneProperties {
		private final List<Daylight> daylights;
		private final Timezone tz;

		public VCalTimezoneProperties(List<Daylight> daylights, Timezone tz) {
			this.daylights = daylights;
			this.tz = tz;
		}

		public List<Daylight> getDaylights() {
			return daylights;
		}

		public Timezone getTz() {
			return tz;
		}

	}
}
