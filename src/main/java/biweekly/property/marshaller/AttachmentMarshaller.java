package biweekly.property.marshaller;

import java.util.List;

import org.apache.commons.codec.binary.Base64;

import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;
import biweekly.parameter.Value;
import biweekly.property.Attachment;

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
public class AttachmentMarshaller extends ICalPropertyMarshaller<Attachment> {
	public AttachmentMarshaller() {
		super(Attachment.class, "ATTACH");
	}

	@Override
	protected void _prepareParameters(Attachment property, ICalParameters copy) {
		if (property.getUri() != null) {
			copy.setEncoding(null);
			copy.setValue(null);
		} else if (property.getData() != null) {
			copy.setEncoding(Encoding.BASE64);
			copy.setValue(Value.BINARY);
		}
	}

	@Override
	protected String _writeText(Attachment property) {
		if (property.getUri() != null) {
			return property.getUri();
		}
		if (property.getData() != null) {
			return Base64.encodeBase64String(property.getData());
		}
		return null;
	}

	@Override
	protected Attachment _parseText(String value, ICalParameters parameters, List<String> warnings) {
		value = unescape(value);

		Attachment attachment = new Attachment(null, (String) null);
		if (parameters.getValue() == Value.BINARY || parameters.getEncoding() == Encoding.BASE64) {
			attachment.setData(Base64.decodeBase64(value));
		} else {
			attachment.setUri(value);
		}
		return attachment;
	}

	@Override
	protected void _writeXml(Attachment property, XCalElement element) {
		if (property.getUri() != null) {
			element.append(Value.URI, property.getUri());
		} else if (property.getData() != null) {
			element.append(Value.BINARY, Base64.encodeBase64String(property.getData()));
		}
	}

	@Override
	protected Attachment _parseXml(XCalElement element, ICalParameters parameters, List<String> warnings) {
		Attachment attachment = new Attachment(null, (String) null);

		String value = element.first(Value.BINARY);
		if (value != null) {
			attachment.setData(Base64.decodeBase64(value));
		} else {
			value = element.first(Value.URI);
			attachment.setUri(value);
		}

		return attachment;
	}

	@Override
	protected Attachment _parseJson(JCalValue value, ICalParameters parameters, List<String> warnings) {
		Attachment attachment = new Attachment(null, (String) null);

		String valueStr = value.getSingleValued();
		if (value.getDataType() == Value.BINARY) {
			attachment.setData(Base64.decodeBase64(valueStr));
		} else {
			attachment.setUri(valueStr);
		}

		return attachment;
	}
}
