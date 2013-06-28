package biweekly.property.marshaller;

import java.util.List;

import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.Version;

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
public class VersionMarshaller extends ICalPropertyMarshaller<Version> {
	public VersionMarshaller() {
		super(Version.class, "VERSION");
	}

	@Override
	protected String _writeText(Version property) {
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
	protected Version _parseText(String value, ICalParameters parameters, List<String> warnings) {
		String split[] = split(value, ";").unescape(true).split();

		String min = null, max = null;
		if (split.length == 1) {
			max = split[0];
		} else {
			min = split[0];
			max = split[1];
		}
		return new Version(min, max);
	}

	@Override
	protected void _writeXml(Version property, XCalElement element) {
		element.append(Value.TEXT, property.getMaxVersion());
	}

	@Override
	protected Version _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		return new Version(element.first(Value.TEXT));
	}
}