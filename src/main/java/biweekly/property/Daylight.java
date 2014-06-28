package biweekly.property;

import java.util.Date;
import java.util.List;

import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.component.ICalComponent;
import biweekly.util.UtcOffset;

/**
 * Represents daylight savings time information.
 * @author Michael Angstadt
 * @see <a href="http://www.imc.org/pdi/vcal-10.doc">vCal 1.0 p.23</a>
 */
public class Daylight extends ICalProperty {
	private boolean daylight;
	private UtcOffset offset;
	private Date start, end;
	private String standardName, daylightName;

	/**
	 * Creates a daylight savings property which states that the timezone does
	 * not observe daylight savings time.
	 */
	public Daylight() {
		this.daylight = false;
	}

	/**
	 * Creates a daylight savings property.
	 * @param daylight true if the timezone observes daylight savings time,
	 * false if not
	 * @param offset the UTC offset of daylight savings time
	 * @param start the start date of daylight savings time
	 * @param end the end date of daylight savings time
	 * @param standardName the timezone's name for standard time (e.g. "EST")
	 * @param daylightName the timezone's name for daylight savings time (e.g.
	 * "EDT")
	 */
	public Daylight(boolean daylight, UtcOffset offset, Date start, Date end, String standardName, String daylightName) {
		this.daylight = daylight;
		this.offset = offset;
		this.start = start;
		this.end = end;
		this.standardName = standardName;
		this.daylightName = daylightName;
	}

	/**
	 * Gets whether this timezone observes daylight savings time.
	 * @return true if it observes daylight savings time, false if not
	 */
	public boolean isDaylight() {
		return daylight;
	}

	/**
	 * Sets whether this timezone observes daylight savings time.
	 * @param daylight true if it observes daylight savings time, false if not
	 */
	public void setDaylight(boolean daylight) {
		this.daylight = daylight;
	}

	/**
	 * Gets the UTC offset of daylight savings time.
	 * @return the UTC offset
	 */
	public UtcOffset getOffset() {
		return offset;
	}

	/**
	 * Sets the UTC offset of daylight savings time.
	 * @param offset the UTC offset
	 */
	public void setOffset(UtcOffset offset) {
		this.offset = offset;
	}

	/**
	 * Gets the start date of dayight savings time.
	 * @return the start date
	 */
	public Date getStart() {
		return start;
	}

	/**
	 * Sets the start date of dayight savings time.
	 * @param start the start date
	 */
	public void setStart(Date start) {
		this.start = start;
	}

	/**
	 * Gets the end date of daylight savings time.
	 * @return the end date
	 */
	public Date getEnd() {
		return end;
	}

	/**
	 * Sets the end date of daylight savings time.
	 * @param end the end date
	 */
	public void setEnd(Date end) {
		this.end = end;
	}

	/**
	 * Gets the name for standard time.
	 * @return the name (e.g. "EST")
	 */
	public String getStandardName() {
		return standardName;
	}

	/**
	 * Sets the name for standard time.
	 * @param name the name (e.g. "EST")
	 */
	public void setStandardName(String name) {
		this.standardName = name;
	}

	/**
	 * Gets the name of daylight savings time.
	 * @return the name (e.g. "EDT")
	 */
	public String getDaylightName() {
		return daylightName;
	}

	/**
	 * Sets the name of daylight savings time.
	 * @param name the name (e.g. "EDT")
	 */
	public void setDaylightName(String name) {
		this.daylightName = name;
	}

	@Override
	protected void validate(List<ICalComponent> components, ICalVersion version, List<Warning> warnings) {
		if (daylight && (offset == null || start == null || end == null || standardName == null || daylightName == null)) {
			warnings.add(Warning.validate(43));
		}
	}
}
