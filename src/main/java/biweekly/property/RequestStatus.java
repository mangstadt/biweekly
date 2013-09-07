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
 * <p>
 * Represents a response to a scheduling request, describing whether the request
 * was successfully processed or not.
 * </p>
 * <p>
 * Each property instance has a status code. The following status code families
 * are defined:
 * <ul>
 * <li><b>1.x</b> - The request has been received, but is still being processed.
 * </li>
 * <li><b>2.x</b> - The request was processed successfully.</li>
 * <li><b>3.x</b> - There is a client-side problem with the request (such as
 * some incorrect syntax).</li>
 * <li><b>4.x</b> - A server-side error occurred.</li>
 * </ul>
 * </p>
 * <p>
 * <b>Examples:</b>
 * 
 * <pre class="brush:java">
 * RequestStatus requestStatus = new RequestStatus(&quot;2.0&quot;);
 * requestStatus.setDescription(&quot;Success&quot;);
 * </pre>
 * 
 * </p>
 * @author Michael Angstadt
 * @rfc 5545 p.141-3
 */
public class RequestStatus extends ICalProperty {
	private String statusCode, description, exceptionText;

	/**
	 * Creates a request status property.
	 * @param statusCode the status code (e.g. "1.1.3")
	 */
	public RequestStatus(String statusCode) {
		setStatusCode(statusCode);
	}

	/**
	 * Gets the status code. The following status code families are defined:
	 * <ul>
	 * <li><b>1.x</b> - The request has been received, but is still being
	 * processed.</li>
	 * <li><b>2.x</b> - The request was processed successfully.</li>
	 * <li><b>3.x</b> - There is a client-side problem with the request (such as
	 * some incorrect syntax).</li>
	 * <li><b>4.x</b> - A server-side error occurred.</li>
	 * </ul>
	 * @return the status code (e.g. "1.1.3")
	 */
	public String getStatusCode() {
		return statusCode;
	}

	/**
	 * Sets a status code. The following status code families are defined:
	 * <ul>
	 * <li><b>1.x</b> - The request has been received, but is still being
	 * processed.</li>
	 * <li><b>2.x</b> - The request was processed successfully.</li>
	 * <li><b>3.x</b> - There is a client-side problem with the request (such as
	 * some incorrect syntax).</li>
	 * <li><b>4.x</b> - A server-side error occurred.</li>
	 * </ul>
	 * @param statusCode the status code (e.g. "1.1.3")
	 */
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * Gets the human-readable description of the status.
	 * @return the description (e.g. "Success") or null if not set
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets a human-readable description of the status.
	 * @param description the description (e.g. "Success") or null to remove
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Gets any additional data related to the response.
	 * @return the additional data or null if not set
	 */
	public String getExceptionText() {
		return exceptionText;
	}

	/**
	 * Sets any additional data related to the response.
	 * @param exceptionText the additional data or null to remove
	 */
	public void setExceptionText(String exceptionText) {
		this.exceptionText = exceptionText;
	}

	@Override
	public String getLanguage() {
		return super.getLanguage();
	}

	@Override
	public void setLanguage(String language) {
		super.setLanguage(language);
	}

	@Override
	protected void validate(List<ICalComponent> components, List<String> warnings) {
		if (statusCode == null) {
			warnings.add("No status code is set.");
		}
	}
}
