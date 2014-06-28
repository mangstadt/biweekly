package biweekly.property;

import java.util.Date;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.component.VTimezone;
import biweekly.util.UtcOffset;

/**
 * <p>
 * Represents daylight timezone information.
 * </p>
 * <p>
 * <b><i>This property is only used in the deprecated vCalendar standard. Use
 * {@link VTimezone} instead.</i></b>
 * </p>
 * @author Michael Angstadt
 * 
 */
public class Daylight extends ICalProperty {
	private boolean daylight;
	private UtcOffset offset;
	private Date start, end;
	private String standardName, daylightName;

	public Daylight() {
		this.daylight = false;
	}

	public Daylight(boolean daylight, UtcOffset offset, Date start, Date end, String standardName, String daylightName) {
		this.daylight = daylight;
		this.offset = offset;
		this.start = start;
		this.end = end;
		this.standardName = standardName;
		this.daylightName = daylightName;
	}

	public boolean isDaylight() {
		return daylight;
	}

	public void setDaylight(boolean daylight) {
		this.daylight = daylight;
	}

	public UtcOffset getOffset() {
		return offset;
	}

	public void setOffset(UtcOffset offset) {
		this.offset = offset;
	}

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public String getStandardName() {
		return standardName;
	}

	public void setStandardName(String standardName) {
		this.standardName = standardName;
	}

	public String getDaylightName() {
		return daylightName;
	}

	public void setDaylightName(String daylightName) {
		this.daylightName = daylightName;
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		if (daylight && (offset == null || start == null || end == null || standardName == null || daylightName == null)) {
			warnings.add(Warning.validate(43));
		}
	}
}
