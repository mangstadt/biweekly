/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biweekly.util.org.apache.commons.codec.net;

import biweekly.util.org.apache.commons.codec.DecoderException;

/**
 * Utility methods for this package.
 * 
 * @author <a href="mailto:ggregory@seagullsw.com">Gary Gregory</a>
 * @version $Id: Utils.java 1157192 2011-08-12 17:27:38Z ggregory $
 * @since 1.4
 */
class Utils {
    
    private Utils() {
        throw new AssertionError("Must not instantiate this class");
    }

    /**
     * Returns the numeric value of the character <code>b</code> in radix 16.
     * 
     * @param b
     *            The byte to be converted.
     * @return The numeric value represented by the character in radix 16.
     * 
     * @throws DecoderException
     *             Thrown when the byte is not valid per {@link Character#digit(char,int)}
     */
    static int digit16(byte b) throws DecoderException {
        int i = Character.digit((char) b, 16);
        if (i == -1) {
            throw new DecoderException("Invalid URL encoding: not a valid digit (radix " + URLCodec.RADIX + "): " + b);
        }
        return i;
    }

}
