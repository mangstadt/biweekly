package biweekly.io.text;

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
 * Specifies how an iCalendar object should be folded when written to a string.
 * @author Michael Angstadt
 */
public class FoldingScheme {
	/**
	 * Folds lines according to RFC specification (75 characters not including
	 * CRLF, with 1 space as indentation).
	 */
	public static final FoldingScheme DEFAULT = new FoldingScheme(75, " ");

	private final int lineLength;
	private final String indent;

	/**
	 * @param lineLength the maximum number of characters that can exist on a
	 * line before needing to be folded (not including the newline)
	 * @param indent the string to use for indentation
	 * @throws IllegalArgumentException if the line length is &lt;= 0, or if the
	 * line length is less than the length of the indentation string
	 */
	public FoldingScheme(int lineLength, String indent) {
		if (lineLength <= 0) {
			throw new IllegalArgumentException("The line length must be greater than 0.");
		}
		if (indent.length() > lineLength) {
			throw new IllegalArgumentException("The line length must be greater than the length of the indentation string.");
		}
		this.lineLength = lineLength;
		this.indent = indent;
	}

	/**
	 * Gets the maximum number of characters that can exist on a line before
	 * needing to be folded (not including the newline).
	 * @return the max line length
	 */
	public int getLineLength() {
		return lineLength;
	}

	/**
	 * Gets the string that is used to indent the folded line.
	 * @return the indent string
	 */
	public String getIndent() {
		return indent;
	}
}
