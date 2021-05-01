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

package at.pardus.android.webview.gm.store;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.model.ScriptId;

/**
 * Offers functions to add new user scripts and get/enable/disable/remove
 * existing ones. Also allows the storage of values set by user scripts.
 */
public interface ScriptStore {

	/**
	 * Gets any enabled scripts with match/include/exclude metadata matching the
	 * given URL.
	 * 
	 * @param url
	 *            the URL to compare the metadata with
	 * @return an array of matching scripts or an empty array if none found;
	 *         null on any error
	 */
    Script[] get(String url);

	/**
	 * Gets the script identified by the parameter.
	 * 
	 * @param id
	 *            the ID to search for
	 * @return the script if found, null else
	 */
    Script get(ScriptId id);

	/**
	 * Gets all installed scripts.
	 * 
	 * @return an array of all scripts or an empty array if none installed; null
	 *         on any error
	 */
    Script[] getAll();

	/**
	 * Adds and enables a user script. If the ID already exists, the existing
	 * script is replaced.
	 * 
	 * @param script
	 *            the script to add (or overwrite)
	 */
    void add(Script script);

	/**
	 * Enables a user script.
	 * 
	 * @param id
	 *            the ID of the script to enable
	 */
    void enable(ScriptId id);

	/**
	 * Disables a user script.
	 * 
	 * @param id
	 *            the ID of the script to disable
	 */
    void disable(ScriptId id);

	/**
	 * Disables and deletes a user script.
	 * 
	 * @param id
	 *            the ID of the script to delete
	 */
    void delete(ScriptId id);

	/**
	 * Gets all names of values stored by a user script.
	 * 
	 * @param id
	 *            the owner script
	 * @return an array of all names or an empty array if none found; null on
	 *         any error
	 */
    String[] getValueNames(ScriptId id);

	/**
	 * Gets a value identified by its name and the owner script.
	 * 
	 * @param id
	 *            the owner script
	 * @param name
	 *            the key
	 * @return a String value or null if none found
	 */
    String getValue(ScriptId id, String name);

	/**
	 * Sets a name-value pair for a user script.
	 * 
	 * @param id
	 *            the owner script
	 * @param name
	 *            the key
	 * @param value
	 *            its value
	 */
    void setValue(ScriptId id, String name, String value);

	/**
	 * Deletes a name-value pair stored by a user script.
	 * 
	 * @param id
	 *            the owner script
	 * @param name
	 *            the key
	 */
    void deleteValue(ScriptId id, String name);

}
