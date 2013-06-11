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
 * Defines a set of geographical coordinates.
 * @author Michael Angstadt
 * @see "RFC 5545 p.85-7"
 */
public class Geo extends ICalProperty {
	private Double latitude;
	private Double longitude;

	/**
	 * Creates a new geo property.
	 * @param latitude the latitude
	 * @param longitude the longitude
	 */
	public Geo(Double latitude, Double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	/**
	 * Gets the latitude.
	 * @return the latitude
	 */
	public Double getLatitude() {
		return latitude;
	}

	/**
	 * Sets the latitude.
	 * @param latitude the latitude
	 */
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	/**
	 * Gets the longitude.
	 * @return the longitude
	 */
	public Double getLongitude() {
		return longitude;
	}

	/**
	 * Sets the longitude.
	 * @param longitude the longitude
	 */
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	/**
	 * Converts a coordinate in the degrees-minutes-seconds format into its
	 * decimal equivalent.
	 * @param degrees the degrees
	 * @param minutes the minutes
	 * @param seconds the seconds
	 * @return the decimal value
	 */
	public static double toDecimal(int degrees, int minutes, int seconds) {
		return degrees + (minutes / 60.0) + (seconds / 3600.0);
	}

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		if (latitude == null) {
			warnings.add("Latitude is not set.");
		}
		if (longitude == null) {
			warnings.add("Longitude is not set.");
		}
	}
}
