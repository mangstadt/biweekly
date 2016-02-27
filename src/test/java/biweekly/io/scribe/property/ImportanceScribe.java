package biweekly.io.scribe.property;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.io.CannotParseException;
import biweekly.io.ParseContext;
import biweekly.io.SkipMeException;
import biweekly.io.WriteContext;
import biweekly.io.json.JCalValue;
import biweekly.io.xml.XCalElement;
import biweekly.parameter.ICalParameters;
import biweekly.property.Importance;

/*
 Copyright (c) 2013-2016, Michael Angstadt
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
 * Example marshaller class used on the Wiki.
 * @author Michael Angstadt
 */
public class ImportanceScribe extends ICalPropertyScribe<Importance> {
	public ImportanceScribe() {
		super(Importance.class, "X-IMPORTANCE", ICalDataType.INTEGER);
	}

	//optional
	//determines the iCal data type of the property's value
	@Override
	protected ICalDataType _dataType(Importance property, ICalVersion version) {
		if (property.getText() != null) {
			return ICalDataType.TEXT;
		}
		return ICalDataType.INTEGER;
	}

	//optional
	//tweaks the parameters before the property is written
	@Override
	protected ICalParameters _prepareParameters(Importance property, WriteContext context) {
		ICalParameters copy = new ICalParameters(property.getParameters());
		Integer value = property.getNumber();
		if (value != null && value >= 10) {
			copy.put("X-MESSAGE", "very important!!");
		}
		return copy;
	}

	//required
	//writes the property to a plain-text iCal
	@Override
	protected String _writeText(Importance property, WriteContext context) {
		return write(property);
	}

	//required
	//parses the property from a plain-text iCal
	@Override
	protected Importance _parseText(String value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		value = unescape(value);
		return parse(value, dataType);
	}

	//optional
	//writes the property to an XML document (xCal)
	@Override
	protected void _writeXml(Importance property, XCalElement element, WriteContext context) {
		Integer value = property.getNumber();
		if (value != null) {
			if (value > 100) {
				throw new SkipMeException("Way too high.");
			}
			element.append(ICalDataType.INTEGER, value.toString()); //writes: <x-importance><integer>1</integer></x-importance>
			return;
		}

		String text = property.getText();
		if (text != null) {
			element.append(ICalDataType.TEXT, text); //writes: <x-importance><text>high</text></x-importance>
		}

	}

	//optional
	//reads the property from an XML document (xCal)
	@Override
	protected Importance _parseXml(XCalElement element, ICalParameters parameters, ParseContext context) {
		String text = element.first(ICalDataType.TEXT);
		if (text != null) {
			return new Importance(text);
		}

		String number = element.first(ICalDataType.INTEGER);
		if (number != null) {
			try {
				return new Importance(Integer.valueOf(number));
			} catch (NumberFormatException e) {
				throw new CannotParseException("Numeric value expected: " + number);
			}
		}

		return new Importance(0);
	}

	//optional
	//writes the property to a JSON document (jCal)
	@Override
	protected JCalValue _writeJson(Importance property, WriteContext context) {
		return JCalValue.single(write(property));
	}

	//optional
	//reads the property from a JSON document (jCal)
	@Override
	protected Importance _parseJson(JCalValue value, ICalDataType dataType, ICalParameters parameters, ParseContext context) {
		String valueStr = value.asSingle();
		return parse(valueStr, dataType);
	}

	private Importance parse(String value, ICalDataType dataType) {
		if (dataType == ICalDataType.TEXT) {
			return new Importance(value);
		}

		try {
			return new Importance(Integer.valueOf(value));
		} catch (NumberFormatException e) {
			throw new CannotParseException("Numeric value expected: " + value);
		}
	}

	private String write(Importance property) {
		String text = property.getText();
		if (text != null) {
			return text;
		}

		Integer number = property.getNumber();
		if (number != null) {
			return number.toString();
		}

		return "";
	}
}