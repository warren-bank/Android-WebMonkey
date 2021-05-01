/*
 *    Copyright 2012 Werner Bayer
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package at.pardus.android.webview.gm.util;

/**
 * Class offering static functions to compare a script's exclude/include/match
 * criteria with a URL.
 */
public class CriterionMatcher {

	/**
	 * Tests a URL against a criterion (may be a regex or simple glob-type
	 * pattern). Case-insensitive.
	 * 
	 * @param criterion
	 *            the pattern to test against
	 * @param url
	 *            the URL to test
	 * @return true if the URL matches the criterion, false else
	 * @see <tt><a href="http://wiki.greasespot.net/Include_and_exclude_rules">Rules</a></tt>
	 * @see <tt><a href="http://code.google.com/chrome/extensions/match_patterns.html">Match Syntax</a></tt>
	 */
	public static boolean test(String criterion, String url) {
		if (criterion.length() == 0) {
			return true;
		}
		criterion = criterion.toLowerCase();
		url = url.toLowerCase();
		if (isRegExp(url)) {
			return url.matches(".*" + convertJsRegExp(criterion) + ".*");
		}
		return testGlob(criterion, url);
	}

	/**
	 * Tests a string against a glob-type pattern (supporting only * and the
	 * escape character \).
	 * 
	 * @param pattern
	 *            the glob pattern
	 * @param str
	 *            the string to match against the pattern
	 * @return true if the string matches the pattern, false else
	 */
	private static boolean testGlob(String pattern, String str) {
		return testGlob(pattern, 0, str, 0);
	}

	/**
	 * Recursively tests a string against a glob-type pattern (supporting only *
	 * and the escape character \).
	 * 
	 * @param pattern
	 *            the glob pattern
	 * @param pInd
	 *            the index in the pattern to start testing from
	 * @param str
	 *            the string to match against the pattern
	 * @param sInd
	 *            the index in the string to start testing from
	 * @return true if the string from the given index to its end matches the
	 *         pattern from the given index to its end, false else
	 */
	private static boolean testGlob(String pattern, int pInd, String str,
			int sInd) {
		int pLen = pattern.length();
		int sLen = str.length();
		while (true) {
			if (pInd == pLen) {
				return sInd == sLen;
			}
			char pChar = pattern.charAt(pInd);
			if (pChar == '*') {
				pInd++;
				if (pInd >= pLen) {
					return true;
				}
				while (true) {
					if (testGlob(pattern, pInd, str, sInd)) {
						return true;
					}
					if (sInd == sLen) {
						return false;
					}
					sInd++;
				}
			}
			if (sInd == sLen) {
				return false;
			}
			if (pChar == '\\') {
				pInd++;
				if (pInd >= pLen) {
					return false;
				}
				pChar = pattern.charAt(pInd);
			}
			char sChar = str.charAt(sInd);
			if (pChar != sChar) {
				return false;
			}
			pInd++;
			sInd++;
		}
	}

	/**
	 * Converts a JS RegExp to a Java string to be used in pattern matching.
	 * 
	 * @param jsRegExp
	 *            the JS RegExp
	 * @return the JS regular expression as Java-compatible string
	 */
	private static String convertJsRegExp(String jsRegExp) {
        return jsRegExp.substring(1, jsRegExp.length() - 1);
	}

	/**
	 * Tests whether a given string is a JS RegExp.
	 * 
	 * @param str
	 *            the string to test
	 * @return true if the string starts with a / and ends with another /
	 */
	private static boolean isRegExp(String str) {
		return str.length() >= 2 && str.startsWith("/") && str.endsWith("/");
	}

	/**
	 * Private constructor.
	 */
	private CriterionMatcher() {

	}

}
