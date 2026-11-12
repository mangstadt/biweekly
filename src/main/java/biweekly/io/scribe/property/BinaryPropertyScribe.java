package biweekly.io.scribe.property;

import static biweekly.util.StringUtils.decodeBase64Lenient;
import static biweekly.util.StringUtils.encodeBase64;

import com.github.mangstadt.vinnie.io.VObjectPropertyValues;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.Encoding;
import biweekly.parameter.ICalParameters;
import biweekly.property.BinaryProperty;

/*
 Copyright (c) 2013-2024, Michael Angstadt
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
 * Marshals {@link BinaryProperty} properties.
 * @author Michael Angstadt
 */
public abstract class BinaryPropertyScribe<T extends BinaryProperty> extends ICalPropertyScribe<T> {
	protected BinaryPropertyScribe(Class<T> clazz, String propertyName) {
		super(clazz, propertyName, ICalDataType.URI);
	}

	@Override
	protected ICalParameters _prepareParameters(T property, WriteContext context) {
		ICalParameters copy = new ICalParameters(property.getParameters());

		if (property.getUri() != null) {
			copy.setEncoding(null);
		} else if (property.getData() != null) {
			copy.setEncoding(Encoding.BASE64);
		}

		return copy;
	}

	@Override
	protected ICalDataType _dataType(T property, ICalVersion version) {
		if (property.getUri() != null) {
			return (version == ICalVersion.V1_0) ? ICalDataType.URL : ICalDataType.URI;
		}
		if (property.getData() != null) {
			return ICalDataType.BINARY;
		}
		return defaultDataType(version);
	}

	@Override
	protected String _writeText(T property, WriteContext context) {
		String uri = property.getUri();
		if (uri != null) {
			return uri;
		}

		byte[] data = property.getData();
		if (data != null) {
			return encodeBase64(data);
		}

		return "";
	}

	@Override
	protected T _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		value = VObjectPropertyValues.unescape(value);

		if (dataType == ICalDataType.BINARY || parameters.getEncoding() == Encoding.BASE64) {
			return newInstanceFromBase64(value);
		}

		return newInstance(value, dataType);
	}

	@Override
	protected void _writeXml(T property, XCalElement element, WriteContext context) {
		String uri = property.getUri();
		if (uri != null) {
			element.append(ICalDataType.URI, uri);
			return;
		}

		byte[] data = property.getData();
		if (data != null) {
			element.append(ICalDataType.BINARY, encodeBase64(data));
			return;
		}

		element.append(defaultDataType(context.getVersion()), "");
	}

	@Override
	protected T _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		String uri = element.first(ICalDataType.URI);
		if (uri != null) {
			return newInstance(uri, ICalDataType.URI);
		}

		String base64Data = element.first(ICalDataType.BINARY);
		if (base64Data != null) {
			return newInstanceFromBase64(base64Data);
		}

		throw missingXmlElements(ICalDataType.URI, ICalDataType.BINARY);
	}

	@Override
	protected JCalValue _writeJson(T property, WriteContext context) {
		String uri = property.getUri();
		if (uri != null) {
			return JCalValue.single(uri);
		}

		byte[] data = property.getData();
		if (data != null) {
			return JCalValue.single(encodeBase64(data));
		}

		return JCalValue.single("");
	}

	@Override
	protected T _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String valueStr = value.asSingle();

		if (dataType == ICalDataType.BINARY) {
			return newInstanceFromBase64(valueStr);
		}

		return newInstance(valueStr, dataType);
	}

	/**
	 * Attempts to create a new property instance from a base64 value.
	 * @param encoded the base64 value
	 * @return the property instance
	 * @throws CannotParseException if there was a problem parsing the base64
	 * value
	 */
	private T newInstanceFromBase64(String encoded) {
		byte[] data;
		try {
			data = decodeBase64Lenient(encoded);
		} catch (IllegalArgumentException e) {
			throw new CannotParseException(44, e.getMessage());
		}

		return newInstance(data);
	}

	/**
	 * Creates a property object from the given binary data.
	 * @param data the data
	 * @return the property object
	 */
	protected abstract T newInstance(byte[] data);

	/**
	 * Creates a property object from the given string value.
	 * @param value the string value
	 * @param dataType the data type
	 * @return the property object
	 */
	protected abstract T newInstance(String value, ICalDataType dataType);
}
