package biweekly.io.scribe.property;

import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.io.CannotParseException;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.Version;
import biweekly.util.VersionNumber;

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
 * Marshals {@link Version} properties.
 * @author Michael Angstadt
 */
public class VersionScribe extends ICalPropertyScribe<Version> {
	public VersionScribe() {
		super(Version.class, "VERSION", ICalDataType.TEXT);
	}

	@Override
	protected String _writeText(Version property, WriteContext context) {
		StringBuilder sb = new StringBuilder();

		if (property.getMinVersion() != null) {
			sb.append(property.getMinVersion()).append(';');
		}
		if (property.getMaxVersion() != null) {
			sb.append(property.getMaxVersion());
		}

		return sb.toString();
	}

	@Override
	protected Version _parseText(String value, ICalDataType dataType, ICalParameters parameters, ICalVersion version, List<Warning> warnings) {
		SemiStructuredIterator it = semistructured(value, true);
		String one = it.next();
		String two = it.next();

		String min = null, max = null;
		if (two == null) {
			max = one;
		} else {
			min = one;
			max = two;
		}

		return parse(min, max);
	}

	@Override
	protected void _writeXml(Version property, XCalElement element, WriteContext context) {
		VersionNumber max = property.getMaxVersion();
		String value = (max == null) ? null : max.toString();
		element.append(dataType(property, null), value);
	}

	@Override
	protected Version _parseXml(XCalElement element, ICalParameters parameters, List<Warning> warnings) {
		String value = element.first(defaultDataType);
		if (value != null) {
			return parse(null, value);
		}

		throw missingXmlElements(defaultDataType);
	}

	@Override
	protected JCalValue _writeJson(Version property, WriteContext context) {
		VersionNumber max = property.getMaxVersion();
		String value = (max == null) ? null : max.toString();
		return JCalValue.single(value);
	}

	@Override
	protected Version _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
		return parse(null, value.asSingle());
	}

	private Version parse(String min, String max) {
		try {
			return new Version(min, max);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(30);
		}
	}
}