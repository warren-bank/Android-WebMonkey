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

package at.pardus.android.webview.gm.model;

import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import at.pardus.android.webview.gm.util.DownloadHelper;

/**
 * Immutable object describing all sections of a user script. The class includes
 * a static function to create a new Script object from a string.
 */
public class Script extends ScriptMetadata {

	private String content;

	public Script(String name, String namespace, String[] exclude,
			String[] include, String[] match, String description,
			String downloadurl, String updateurl, String installurl,
			String icon, String runAt, boolean unwrap, String version,
			ScriptRequire[] requires, ScriptResource[] resources, String content) {
		super(name, namespace, exclude, include, match, description,
				downloadurl, updateurl, installurl, icon, runAt, unwrap,
				version, requires, resources);
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	/**
	 * Extracts a script's properties from its string content and creates a new
	 * Script object with the extracted data.
	 * 
	 * Should not be run on the UI thread due to possible network activity for
	 * any required scripts or resources.
	 * 
	 * Pattern to extract single metadata property taken from Greasemonkey's
	 * parseScript.js (MIT license, Copyright 2004-2007 Aaron Boodman).
	 * 
	 * @param scriptStr
	 *            the string to parse
	 * @param url
	 *            the address the script was downloaded from, to derive the
	 *            script's name/namespace/downloadURL from if those properties
	 *            are not provided
	 * @return the newly created object or null if the string is not a valid
	 *         user script
	 * @see <tt><a href="http://wiki.greasespot.net/Metadata_Block">Metadata Block</a></tt>
	 * @see <tt><a href="https://github.com/greasemonkey/greasemonkey/blob/master/modules/parseScript.js">parseScript.js</a></tt>
	 */
	public static Script parse(String scriptStr, String url) {
		String name = null, namespace = null, description = null, downloadurl = null, updateurl = null, installurl = null, icon = null, runAt = null, version = null;
		boolean unwrap = false;
		if (url != null) {
			int filenameStart = url.lastIndexOf("/") + 1;
			if (filenameStart != 0 && filenameStart != url.length()) {
				name = url.substring(filenameStart).replace(".user.js", "");
			}
			int hostStart = url.indexOf("://");
			if (hostStart != -1) {
				hostStart += 3;
				int hostEnd = url.indexOf("/", hostStart);
				if (hostEnd != -1) {
					namespace = url.substring(hostStart, hostEnd);
				}
			}
			downloadurl = url;
			updateurl = downloadurl;
		}
		Set<String> exclude = new HashSet<String>(), include = new HashSet<String>(), match = new HashSet<String>();
		Set<ScriptRequire> requires = new HashSet<ScriptRequire>();
		Set<ScriptResource> resources = new HashSet<ScriptResource>();

		Pattern pattern = Pattern.compile("// @(\\S+)(?:\\s+(.*))?");
		Scanner scanner = new Scanner(scriptStr);
		boolean inMetaBlock = false;
		boolean metaBlockEnded = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (!inMetaBlock) {
				if (line.startsWith("// ==UserScript==")) {
					inMetaBlock = true;
				}
				continue;
			}
			if (line.startsWith("// ==/UserScript==")) {
				metaBlockEnded = true;
				break;
			}
			line = line.trim();
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				String propertyName = matcher.group(1);
				String propertyValue = matcher.group(2);
				if (propertyValue != null && propertyValue.equals("")) {
					propertyValue = null;
				}
				if (propertyValue != null) {
					if (propertyName.equals("name")) {
						name = propertyValue;
					} else if (propertyName.equals("namespace")) {
						namespace = propertyValue;
					} else if (propertyName.equals("description")) {
						description = propertyValue;
					} else if (propertyName.equals("downloadURL")) {
						downloadurl = propertyValue;
					} else if (propertyName.equals("updateURL")) {
						updateurl = propertyValue;
					} else if (propertyName.equals("installURL")) {
						installurl = propertyValue;
					} else if (propertyName.equals("icon")) {
						icon = DownloadHelper.resolveUrl(propertyValue, url);
					} else if (propertyName.equals("run-at")) {
						if (propertyValue.equals(RUNATSTART)
								|| propertyValue.equals(RUNATEND)) {
							runAt = propertyValue;
						}
					} else if (propertyName.equals("version")) {
						version = propertyValue;
					} else if (propertyName.equals("require")) {
						ScriptRequire require = downloadRequire(
							DownloadHelper.resolveUrl(propertyValue, url)
						);
						if (require == null) {
							return null;
						}
						requires.add(require);
					} else if (propertyName.equals("resource")) {
						Pattern resourcePattern = Pattern
								.compile("(\\S+)\\s+(.*)");
						Matcher resourceMatcher = resourcePattern
								.matcher(propertyValue);
						if (!resourceMatcher.matches()) {
							return null;
						}
						ScriptResource resource = downloadResource(
							resourceMatcher.group(1),
							DownloadHelper.resolveUrl(resourceMatcher.group(2), url)
						);
						if (resource == null) {
							return null;
						}
						resources.add(resource);
					} else if (propertyName.equals("exclude")) {
						exclude.add(propertyValue);
					} else if (propertyName.equals("include")) {
						include.add(propertyValue);
					} else if (propertyName.equals("match")) {
						match.add(propertyValue);
					}
				}
				if (propertyName.equals("unwrap")) {
					unwrap = true;
				}
			}
		}
		if (!metaBlockEnded) {
			return null;
		}
		if (name == null || namespace == null) {
			return null;
		}
		String[] excludeArr = null, includeArr = null, matchArr = null;
		ScriptRequire[] requireArr = null;
		ScriptResource[] resourceArr = null;
		if (requires.size() > 0) {
			requireArr = requires.toArray(new ScriptRequire[requires.size()]);
		}
		if (resources.size() > 0) {
			resourceArr = resources
					.toArray(new ScriptResource[resources.size()]);
		}
		if (exclude.size() > 0) {
			excludeArr = exclude.toArray(new String[exclude.size()]);
		}
		if (include.size() > 0) {
			includeArr = include.toArray(new String[include.size()]);
		}
		if (match.size() > 0) {
			matchArr = match.toArray(new String[match.size()]);
		}
		return new Script(name, namespace, excludeArr, includeArr, matchArr,
				description, downloadurl, updateurl, installurl, icon, runAt,
				unwrap, version, requireArr, resourceArr, scriptStr);
	}

	/**
	 * Downloads a @require'd script for the current script.
	 * 
	 * Not to be run on the UI thread.
	 *
	 * @param requireUrl
	 *            a @require URL indicating where to download a required script
	 *            from.
	 * @return a boolean value indicating whether the download operation was
	 *         successful for all @require entries.
	 * @see <tt><a href="http://wiki.greasespot.net/Metadata_Block">Metadata Block</a></tt>
	 */
	public static ScriptRequire downloadRequire(String requireUrl) {
		String requireContent = DownloadHelper.downloadScript(requireUrl);

		if (requireContent == null) {
			return null;
		}

		return new ScriptRequire(requireUrl, requireContent);
	}

	/**
	 * Downloads @resource'd file for the current script.
	 * 
	 * Not to be run on the UI thread.
	 *
	 * @param resourceName
	 *            a @resource name, to identify the downloaded resource.
	 * @param resourceUrl
	 *            a @resource URL indicating where to download a resource from.
	 * @return a boolean value indicating whether the download operation was
	 *         successful for all @resource entries.
	 * @see <tt><a href="http://wiki.greasespot.net/Metadata_Block">Metadata Block</a></tt>
	 */
	public static ScriptResource downloadResource(String resourceName,
			String resourceUrl) {
		byte[] resourceData = DownloadHelper.downloadBytes(resourceUrl);

		if (resourceData == null) {
			return null;
		}

		return new ScriptResource(resourceName, resourceUrl, resourceData);
	}
}
