package biweekly.util;

import java.util.Collection;
import java.util.Map;

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
 * Contains miscellaneous string utilities.
 * @author Michael Angstadt
 */
public class StringUtils {
	/**
	 * Joins a collection of strings into a delimited list.
	 * @param collection the collection of strings
	 * @param delimiter the delimiter (e.g. ",")
	 * @return the final string
	 */
	public static String join(Collection<String> collection, String delimiter) {
		return join(collection, delimiter, new JoinCallback<String>() {
			public void handle(StringBuilder sb, String string) {
				sb.append(string);
			}
		});
	}

	/**
	 * Joins a collection of values into a delimited list.
	 * @param collection the collection of values
	 * @param delimiter the delimiter (e.g. ",")
	 * @param join callback function to call on every element in the collection
	 * @return the final string
	 */
	public static <T> String join(Collection<T> collection, String delimiter, JoinCallback<T> join) {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (T element : collection) {
			if (first) {
				first = false;
			} else {
				sb.append(delimiter);
			}
			join.handle(sb, element);
		}

		return sb.toString();
	}

	/**
	 * Joins a map into a delimited list.
	 * @param map the map
	 * @param delimiter the delimiter (e.g. ",")
	 * @param join callback function to call on every element in the collection
	 * @return the final string
	 */
	public static <K, V> String join(Map<K, V> map, String delimiter, final JoinMapCallback<K, V> join) {
		return join(map.entrySet(), delimiter, new JoinCallback<Map.Entry<K, V>>() {
			public void handle(StringBuilder sb, Map.Entry<K, V> entry) {
				join.handle(sb, entry.getKey(), entry.getValue());
			}
		});
	}

	/**
	 * Callback interface used with the
	 * {@link StringUtils#join(Collection, String, JoinCallback)} method.
	 * @author Michael Angstadt
	 * @param <T> the value type
	 */
	public static interface JoinCallback<T> {
		void handle(StringBuilder sb, T value);
	}

	/**
	 * Callback interface used with the
	 * {@link StringUtils#join(Map, String, JoinMapCallback)} method.
	 * @author Michael Angstadt
	 * @param <K> the key class
	 * @param <V> the value class
	 */
	public static interface JoinMapCallback<K, V> {
		void handle(StringBuilder sb, K key, V value);
	}
}
