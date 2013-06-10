package biweekly.property.marshaller;

import java.util.List;

import biweekly.parameter.ICalParameters;
import biweekly.property.RequestStatus;

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
 * Marshals {@link RequestStatus} properties.
 * @author Michael Angstadt
 */
public class RequestStatusMarshaller extends ICalPropertyMarshaller<RequestStatus> {
	public RequestStatusMarshaller() {
		super(RequestStatus.class, "REQUEST-STATUS");
	}

	@Override
	protected String _writeText(RequestStatus property) {
		StringBuilder sb = new StringBuilder();

		if (property.getStatusCode() != null) {
			sb.append(escape(property.getStatusCode()));
		}

		if (property.getDescription() != null) {
			sb.append(';');
			sb.append(escape(property.getDescription()));
		}

		if (property.getExceptionText() != null) {
			sb.append(';');
			sb.append(escape(property.getExceptionText()));
		}

		return sb.toString();
	}

	@Override
	protected RequestStatus _parseText(String value, ICalParameters parameters, List<String> warnings) {
		String split[] = splitBy(value, ';', false, true);
		RequestStatus requestStatus = new RequestStatus();

		requestStatus.setStatusCode(split[0]);
		if (split.length > 1) {
			requestStatus.setDescription(split[1]);
		}
		if (split.length > 2) {
			requestStatus.setExceptionText(split[2]);
		}

		return requestStatus;
	}
}