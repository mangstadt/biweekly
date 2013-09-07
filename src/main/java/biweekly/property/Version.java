package biweekly.property;

import java.util.List;

import biweekly.component.ICalComponent;

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
 * <p>
 * Specifies the min/max versions a consumer must support in order to
 * successfully parse the iCalendar object.
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre>
 * //the default iCal version
 * Version version = Version.v2_0();
 * 
 * if (verison.isV2_0()) {
 * 	//version is &quot;2.0&quot;
 * }
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @rfc 5545 p.79-80
 */
public class Version extends ICalProperty {
	private static final String DEFAULT = "2.0";

	private String minVersion, maxVersion;

	/**
	 * Creates a new version property.
	 * @param version the version that a consumer must support in order to
	 * successfully parse the iCalendar object
	 */
	public Version(String version) {
		this(null, version);
	}

	/**
	 * Creates a new version property.
	 * @param minVersion the minimum version that a consumer must support in
	 * order to successfully parse the iCalendar object
	 * @param maxVersion the maximum version that a consumer must support in
	 * order to successfully parse the iCalendar object
	 */
	public Version(String minVersion, String maxVersion) {
		this.minVersion = minVersion;
		this.maxVersion = maxVersion;
	}

	/**
	 * Creates a version property that is set to the default iCalendar version
	 * (2.0).
	 * @return the property instance
	 */
	public static Version v2_0() {
		return new Version(DEFAULT);
	}

	/**
	 * Determines if this version is the default iCalendar version.
	 * @return true if the version is "2.0", false if not
	 */
	public boolean isV2_0() {
		return DEFAULT.equalsIgnoreCase(maxVersion);
	}

	/**
	 * Gets the minimum version that a consumer must support in order to
	 * successfully parse the iCalendar object.
	 * @return the minimum version or null if not set
	 */
	public String getMinVersion() {
		return minVersion;
	}

	/**
	 * Sets the minimum version that a consumer must support in order to
	 * successfully parse the iCalendar object.
	 * @param minVersion the minimum version or null to remove
	 */
	public void setMinVersion(String minVersion) {
		this.minVersion = minVersion;
	}

	/**
	 * Gets the maximum version that a consumer must support in order to
	 * successfully parse the iCalendar object.
	 * @return the maximum version or null if not set
	 */
	public String getMaxVersion() {
		return maxVersion;
	}

	/**
	 * Sets the maximum version that a consumer must support in order to
	 * successfully parse the iCalendar object.
	 * @param maxVersion the maximum version (this field is <b>required</b>)
	 */
	public void setMaxVersion(String maxVersion) {
		this.maxVersion = maxVersion;
	}

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		if (maxVersion == null) {
			warnings.add("A maximum version must be specified.");
		}
	}
}
