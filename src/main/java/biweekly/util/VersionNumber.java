package biweekly.util;

import java.util.Arrays;

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
 * Represents a software version number (e.g. "1.8.14").
 * @author Michael Angstadt
 */
public class VersionNumber implements Comparable<VersionNumber> {
	private final Integer parts[];

	/**
	 * Creates a new version number.
	 * @param version the version string (e.g. "1.8.14")
	 * @throws IllegalArgumentException if the version string is invalid
	 */
	public VersionNumber(String version) {
		if (!version.matches("[0-9]+(\\.[0-9]+)*")) {
			throw new IllegalArgumentException("Invalid version format.");
		}

		String parts[] = version.split("\\.");
		this.parts = new Integer[parts.length];
		for (int i = 0; i < parts.length; i++) {
			this.parts[i] = Integer.valueOf(parts[i]);
		}
	}

	public int compareTo(VersionNumber that) {
		int length = Math.max(this.parts.length, that.parts.length);
		for (int i = 0; i < length; i++) {
			int thisPart = (i < this.parts.length) ? this.parts[i] : 0;
			int thatPart = (i < that.parts.length) ? that.parts[i] : 0;

			if (thisPart < thatPart) {
				return -1;
			}
			if (thisPart > thatPart) {
				return 1;
			}
		}
		return 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(parts);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		VersionNumber other = (VersionNumber) obj;
		if (!Arrays.equals(parts, other.parts)) return false;
		return true;
	}

	@Override
	public String toString() {
		return StringUtils.join(Arrays.asList(parts), ".");
	}
}
