package biweekly.io.scribe.property;

import java.util.Arrays;
import java.util.List;

import biweekly.ICalDataType;
import biweekly.ICalVersion;
import biweekly.property.AudioAlarm;
import biweekly.util.org.apache.commons.codec.binary.Base64;

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
 * Marshals {@link AudioAlarm} properties.
 * @author Michael Angstadt
 */
public class AudioAlarmScribe extends VCalAlarmPropertyScribe<AudioAlarm> {
	public AudioAlarmScribe() {
		super(AudioAlarm.class, "AALARM", null);
	}

	@Override
	protected ICalDataType _dataType(AudioAlarm property, ICalVersion version) {
		if (property.getUri() != null) {
			return ICalDataType.URL;
		}
		if (property.getData() != null) {
			return ICalDataType.BINARY;
		}
		if (property.getContentId() != null) {
			return ICalDataType.CONTENT_ID;
		}
		return null;
	}

	@Override
	protected List<String> writeData(AudioAlarm property) {
		String uri = property.getUri();
		if (uri != null) {
			return Arrays.asList(uri);
		}

		byte data[] = property.getData();
		if (data != null) {
			String base64Str = Base64.encodeBase64String(data);
			return Arrays.asList(base64Str);
		}

		String contentId = property.getContentId();
		if (contentId != null) {
			return Arrays.asList(contentId);
		}

		return Arrays.asList();
	}

	@Override
	protected AudioAlarm create(ICalDataType dataType, SemiStructuredIterator it) {
		AudioAlarm aalarm = new AudioAlarm();
		String next = it.next();
		if (next == null) {
			return aalarm;
		}

		if (dataType == ICalDataType.BINARY) {
			byte[] data = Base64.decodeBase64(next);
			aalarm.setData(data);
		} else if (dataType == ICalDataType.URL) {
			aalarm.setUri(next);
		} else if (dataType == ICalDataType.CONTENT_ID) {
			aalarm.setContentId(next);
		} else {
			aalarm.setUri(next);
		}

		return aalarm;
	}
}
