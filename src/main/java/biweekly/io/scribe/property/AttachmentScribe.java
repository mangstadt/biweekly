package biweekly.io.scribe.property;

import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.Warning;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;
import biweekly.property.Attachment;
import biweekly.util.Base64;

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
 * Marshals {@link Attachment} properties.
 * @author Michael Angstadt
 */
public class AttachmentScribe extends ICalPropertyScribe<Attachment> {
	public AttachmentScribe() {
		super(Attachment.class, "ATTACH", ICalDataType.URI);
	}

	@Override
	protected ICalParameters _prepareParameters(Attachment property, ICalVersion version) {
		ICalParameters copy = new ICalParameters(property.getParameters());

		if (property.getUri() != null) {
			copy.setEncoding(null);
		} else if (property.getData() != null) {
			copy.setEncoding(Encoding.BASE64);
		}

		return copy;
	}

	@Override
	protected ICalDataType _dataType(Attachment property, ICalVersion version) {
		if (property.getUri() != null) {
			return ICalDataType.URI;
		}
		if (property.getData() != null) {
			return ICalDataType.BINARY;
		}
		return defaultDataType;
	}

	@Override
	protected String _writeText(Attachment property, ICalVersion version) {
		String uri = property.getUri();
		if (uri != null) {
			return uri;
		}

		byte data[] = property.getData();
		if (data != null) {
			return Base64.encode(data);
		}

		return "";
	}

	@Override
	protected Attachment _parseText(String value, ICalDataType dataType, ICalParameters parameters, ICalVersion version, List<Warning> warnings) {
		value = unescape(value);

		if (dataType == ICalDataType.BINARY || parameters.getEncoding() == Encoding.BASE64) {
			return new Attachment(null, Base64.decode(value));
		}
		return new Attachment(null, value);
	}

	@Override
	protected void _writeXml(Attachment property, XCalElement element) {
		String uri = property.getUri();
		if (uri != null) {
			element.append(ICalDataType.URI, uri);
			return;
		}

		byte data[] = property.getData();
		if (data != null) {
			element.append(ICalDataType.BINARY, Base64.encode(data));
			return;
		}

		element.append(defaultDataType, "");
	}

	@Override
	protected Attachment _parseXml(XCalElement element, ICalParameters parameters, List<Warning> warnings) {
		String uri = element.first(ICalDataType.URI);
		if (uri != null) {
			return new Attachment(null, uri);
		}

		String base64Data = element.first(ICalDataType.BINARY);
		if (base64Data != null) {
			return new Attachment(null, Base64.decode(base64Data)); //formatType will be set when the parameters are assigned to the property object
		}

		throw missingXmlElements(ICalDataType.URI, ICalDataType.BINARY);
	}

	@Override
	protected JCalValue _writeJson(Attachment property) {
		String uri = property.getUri();
		if (uri != null) {
			return JCalValue.single(uri);
		}

		byte data[] = property.getData();
		if (data != null) {
			return JCalValue.single(Base64.encode(data));
		}

		return JCalValue.single("");
	}

	@Override
	protected Attachment _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, List<Warning> warnings) {
		String valueStr = value.asSingle();

		if (dataType == ICalDataType.BINARY) {
			return new Attachment(null, Base64.decode(valueStr));
		}
		return new Attachment(null, valueStr);
	}
}
