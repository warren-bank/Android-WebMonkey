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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.model.ScriptCriteria;
import at.pardus.android.webview.gm.model.ScriptId;
import at.pardus.android.webview.gm.model.ScriptRequire;
import at.pardus.android.webview.gm.model.ScriptResource;

/**
 * Implements a ScriptStore using an SQLite database to persist user scripts and
 * values.
 * 
 * Uses an LRU cache of user scripts matching URLs and a cache of all available
 * and enabled user script matching criteria to improve performance.
 */
public class ScriptStoreSQLite implements ScriptStore {

	private static final String TAG = ScriptStoreSQLite.class.getName();

	private Context context;

	private ScriptDbHelper dbHelper;

	private ScriptCache cache;

	@Override
	public Script[] get(String url) {
		Script[] scripts = cache.get(url);
		if (scripts == null) {
			if (dbHelper == null) {
				Log.w(TAG, "Cannot get user scripts (database not available)");
				return null;
			}
			ScriptId[] matchingIds = cache.getMatchingScriptIds(url);
			scripts = dbHelper.selectScripts(matchingIds, true);
			cache.put(url, scripts);
		}
		return scripts;
	}

	@Override
	public Script get(ScriptId id) {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot get user script (database not available)");
			return null;
		}
		Script[] scripts = dbHelper.selectScripts(new ScriptId[] { id }, null);
		if (scripts.length == 0) {
			return null;
		}
		return scripts[0];
	}

	@Override
	public Script[] getAll() {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot get user script (database not available)");
			return null;
		}
        return dbHelper.selectScripts(null, null);
	}

	@Override
	public void add(Script script) {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot add user script (database not available)");
			return;
		}
		dbHelper.deleteScript(script);
		dbHelper.insertScript(script);
		initCache();
	}

	@Override
	public void enable(ScriptId id) {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot enable user script (database not available)");
			return;
		}
		dbHelper.updateScriptEnabled(id, true);
		initCache();
	}

	@Override
	public void disable(ScriptId id) {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot disable user script (database not available)");
			return;
		}
		dbHelper.updateScriptEnabled(id, false);
		initCache();
	}

	@Override
	public void delete(ScriptId id) {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot delete user script (database not available)");
			return;
		}
		dbHelper.deleteScript(id);
		initCache();
	}

	@Override
	public String[] getValueNames(ScriptId id) {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot get value names (database not available)");
			return null;
		}
		return dbHelper.selectValueNames(id);
	}

	@Override
	public String getValue(ScriptId id, String name) {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot get value (database not available)");
			return null;
		}
		return dbHelper.selectValue(id, name);
	}

	@Override
	public void setValue(ScriptId id, String name, String value) {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot set value (database not available)");
			return;
		}
		dbHelper.updateOrInsertValue(id, name, value);
	}

	@Override
	public void deleteValue(ScriptId id, String name) {
		if (dbHelper == null) {
			Log.e(TAG, "Cannot delete value (database not available)");
			return;
		}
		dbHelper.deleteValue(id, name);
	}

	/**
	 * Creates a new SQLite-backed ScriptStore object.
	 * 
	 * Call open to enable further access.
	 * 
	 * @param context
	 *            the application's context
	 */
	public ScriptStoreSQLite(Context context) {
		this.context = context;
	}

	/**
	 * Opens access to the database and prepares the cache.
	 * 
	 * Synchronized since this method should not be run on the UI thread.
	 */
	public synchronized void open() {
		if (dbHelper != null) {
			return;
		}
		dbHelper = new ScriptDbHelper(context);
		initCache();
	}

	/**
	 * Closes access to the database.
	 * 
	 * Synchronized since the database may be in the process of being opened in
	 * a different thread.
	 */
	public synchronized void close() {
		dbHelper.close();
		dbHelper = null;
	}

	/**
	 * Creates an empty ScriptCache object and initializes its cache of all
	 * available and enabled user script matching criteria.
	 */
	private void initCache() {
		cache = new ScriptCache();
		cache.setScriptCriteriaArr(dbHelper.selectScriptCriteria(null, true));
	}

	/**
	 * Private class to manage the database access.
	 */
	private static class ScriptDbHelper extends SQLiteOpenHelper {

		// V2 added tables for @require and @resource metadata directive.
		private static final int DB_SCHEMA_VERSION_2 = 2;
		private static final int DB_VERSION = DB_SCHEMA_VERSION_2;

		private static final String DB = "webviewgm";

		private static final String TBL_SCRIPT = "script";
		private static final String COL_NAME = "name";
		private static final String COL_NAMESPACE = "namespace";
		private static final String COL_DOWNLOADURL = "downloadurl";
		private static final String COL_UPDATEURL = "updateurl";
		private static final String COL_INSTALLURL = "installurl";
		private static final String COL_DESCRIPTION = "description";
		private static final String COL_ICON = "icon";
		private static final String COL_RUNAT = "runat";
		private static final String COL_UNWRAP = "unwrap";
		private static final String COL_VERSION = "version";
		private static final String COL_CONTENT = "content";
		private static final String COL_ENABLED = "enabled";
		private static final String TBL_SCRIPT_CREATE = "CREATE TABLE "
				+ TBL_SCRIPT + " (" + COL_NAME + " TEXT NOT NULL" + ", "
				+ COL_NAMESPACE + " TEXT NOT NULL" + ", " + COL_DESCRIPTION
				+ " TEXT" + ", " + COL_DOWNLOADURL + " TEXT" + ", "
				+ COL_UPDATEURL + " TEXT" + ", " + COL_INSTALLURL + " TEXT"
				+ ", " + COL_ICON + " TEXT" + ", " + COL_RUNAT + " TEXT" + ", "
				+ COL_UNWRAP + " INTEGER" + ", " + COL_VERSION + " TEXT" + ", "
				+ COL_CONTENT + " TEXT NOT NULL" + ", " + COL_ENABLED
				+ " INTEGER NOT NULL DEFAULT 1" + ", PRIMARY KEY (" + COL_NAME
				+ ", " + COL_NAMESPACE + "));";

		private static final String COL_PATTERN = "pattern";

		private static final String TBL_EXCLUDE = TBL_SCRIPT + "_has_exclude";
		private static final String TBL_EXCLUDE_CREATE = "CREATE TABLE "
				+ TBL_EXCLUDE + " (" + COL_NAME + " TEXT NOT NULL" + ", "
				+ COL_NAMESPACE + " TEXT NOT NULL" + ", " + COL_PATTERN
				+ " TEXT NOT NULL, PRIMARY KEY (" + COL_NAME + ", "
				+ COL_NAMESPACE + ", " + COL_PATTERN + "), FOREIGN KEY ("
				+ COL_NAME + ", " + COL_NAMESPACE + ") REFERENCES "
				+ TBL_SCRIPT + " (" + COL_NAME + ", " + COL_NAMESPACE
				+ ") ON UPDATE CASCADE ON DELETE CASCADE);";

		private static final String TBL_INCLUDE = TBL_SCRIPT + "_has_include";
		private static final String TBL_INCLUDE_CREATE = "CREATE TABLE "
				+ TBL_INCLUDE + " (" + COL_NAME + " TEXT NOT NULL" + ", "
				+ COL_NAMESPACE + " TEXT NOT NULL" + ", " + COL_PATTERN
				+ " TEXT NOT NULL, PRIMARY KEY (" + COL_NAME + ", "
				+ COL_NAMESPACE + ", " + COL_PATTERN + "), FOREIGN KEY ("
				+ COL_NAME + ", " + COL_NAMESPACE + ") REFERENCES "
				+ TBL_SCRIPT + " (" + COL_NAME + ", " + COL_NAMESPACE
				+ ") ON UPDATE CASCADE ON DELETE CASCADE);";

		private static final String TBL_MATCH = TBL_SCRIPT + "_has_match";
		private static final String TBL_MATCH_CREATE = "CREATE TABLE "
				+ TBL_MATCH + " (" + COL_NAME + " TEXT NOT NULL" + ", "
				+ COL_NAMESPACE + " TEXT NOT NULL" + ", " + COL_PATTERN
				+ " TEXT NOT NULL, PRIMARY KEY (" + COL_NAME + ", "
				+ COL_NAMESPACE + ", " + COL_PATTERN + "), FOREIGN KEY ("
				+ COL_NAME + ", " + COL_NAMESPACE + ") REFERENCES "
				+ TBL_SCRIPT + " (" + COL_NAME + ", " + COL_NAMESPACE
				+ ") ON UPDATE CASCADE ON DELETE CASCADE);";

		private static final String TBL_REQUIRE = TBL_SCRIPT + "_has_require";
		private static final String TBL_REQUIRE_CREATE = "CREATE TABLE IF NOT EXISTS "
				+ TBL_REQUIRE
				+ " ("
				+ COL_NAME
				+ " TEXT NOT NULL"
				+ ", "
				+ COL_NAMESPACE
				+ " TEXT NOT NULL, "
				+ COL_DOWNLOADURL
				+ " TEXT NOT NULL, "
				+ COL_CONTENT
				+ " TEXT NOT NULL, PRIMARY KEY ("
				+ COL_NAME
				+ ", "
				+ COL_NAMESPACE
				+ ", "
				+ COL_DOWNLOADURL
				+ "), FOREIGN KEY ("
				+ COL_NAME
				+ ", "
				+ COL_NAMESPACE
				+ ") REFERENCES "
				+ TBL_SCRIPT
				+ " ("
				+ COL_NAME
				+ ", "
				+ COL_NAMESPACE
				+ ") ON UPDATE CASCADE ON DELETE CASCADE);";

		private static final String TBL_RESOURCE = TBL_SCRIPT + "_has_resource";
		private static final String COL_DATA = "data";
		private static final String COL_RESOURCENAME = "resource_name";
		private static final String TBL_RESOURCE_CREATE = "CREATE TABLE IF NOT EXISTS "
				+ TBL_RESOURCE
				+ " ("
				+ COL_NAME
				+ " TEXT NOT NULL, "
				+ COL_NAMESPACE
				+ " TEXT NOT NULL, "
				+ COL_RESOURCENAME
				+ " TEXT NOT NULL, "
				+ COL_DOWNLOADURL
				+ " TEXT NOT NULL, "
				+ COL_DATA
				+ " BLOB NOT NULL, PRIMARY KEY ("
				+ COL_NAME
				+ ", "
				+ COL_NAMESPACE
				+ ", "
				+ COL_RESOURCENAME
				+ "), FOREIGN KEY ("
				+ COL_NAME
				+ ", "
				+ COL_NAMESPACE
				+ ") REFERENCES "
				+ TBL_SCRIPT
				+ " ("
				+ COL_NAME
				+ ", "
				+ COL_NAMESPACE
				+ ") ON UPDATE CASCADE ON DELETE CASCADE);";

		private static final String TBL_VALUE = TBL_SCRIPT + "_has_value";
		private static final String COL_VALUENAME = "valuename";
		private static final String COL_VALUE = "value";
		private static final String TBL_VALUE_CREATE = "CREATE TABLE "
				+ TBL_VALUE + " (" + COL_NAME + " TEXT NOT NULL" + ", "
				+ COL_NAMESPACE + " TEXT NOT NULL" + ", " + COL_VALUENAME
				+ " TEXT NOT NULL" + ", " + COL_VALUE
				+ " TEXT NOT NULL, PRIMARY KEY (" + COL_NAME + ", "
				+ COL_NAMESPACE + ", " + COL_VALUENAME + "), FOREIGN KEY ("
				+ COL_NAME + ", " + COL_NAMESPACE + ") REFERENCES "
				+ TBL_SCRIPT + " (" + COL_NAME + ", " + COL_NAMESPACE
				+ ") ON UPDATE CASCADE ON DELETE CASCADE);";

		private static final String[] COLS_ID = new String[] { COL_NAME,
				COL_NAMESPACE };
		private static final String[] COLS_PATTERN = new String[] { COL_NAME,
				COL_NAMESPACE, COL_PATTERN };
		private static final String[] COLS_REQUIRE = new String[] { COL_NAME,
				COL_NAMESPACE, COL_DOWNLOADURL, COL_CONTENT };
		private static final String[] COLS_RESOURCE = new String[] { COL_NAME,
				COL_NAMESPACE, COL_DOWNLOADURL, COL_RESOURCENAME, COL_DATA };
		private static final String[] COLS_SCRIPT = new String[] { COL_NAME,
				COL_NAMESPACE, COL_DESCRIPTION, COL_DOWNLOADURL, COL_UPDATEURL,
				COL_INSTALLURL, COL_ICON, COL_RUNAT, COL_UNWRAP, COL_VERSION,
				COL_CONTENT, COL_ENABLED };

		private SQLiteDatabase db;

		public ScriptDbHelper(Context context) {
			super(context, DB, null, DB_VERSION);
			db = getWritableDatabase();
			db.execSQL("PRAGMA foreign_keys = ON;");
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(TBL_SCRIPT_CREATE);
			db.execSQL(TBL_EXCLUDE_CREATE);
			db.execSQL(TBL_INCLUDE_CREATE);
			db.execSQL(TBL_MATCH_CREATE);
			db.execSQL(TBL_VALUE_CREATE);
			db.execSQL(TBL_REQUIRE_CREATE);
			db.execSQL(TBL_RESOURCE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.i(TAG, "Upgrading database " + DB + " from version "
					+ oldVersion + " to " + newVersion);
			for (int v = oldVersion; v <= newVersion; v++) {
				if (v == DB_SCHEMA_VERSION_2) {
					db.execSQL(TBL_REQUIRE_CREATE);
					db.execSQL(TBL_RESOURCE_CREATE);
				}
			}
		}

		/**
		 * Retrieves scripts including their exclude/include/match criteria from
		 * the database.
		 * 
		 * @param ids
		 *            the script IDs to match; an empty array to match none;
		 *            null to get all
		 * @param enabled
		 *            true to only get enabled scripts; false to only get
		 *            disabled scripts; null to get all
		 * @return an array of matching script objects; an empty array if none
		 *         found
		 */
		public Script[] selectScripts(ScriptId[] ids, Boolean enabled) {
			String selectionStr = null, selectionIdStr = null;
			String[] selectionArgsArr = null, selectionIdArgsArr = null;
			if (ids != null || enabled != null) {
				StringBuilder selection = new StringBuilder();
				List<String> selectionArgs = new ArrayList<String>();
				if (ids != null) {
					if (ids.length == 0) {
						return new Script[0];
					}
					makeScriptIdSelectionArgs(ids, selection, selectionArgs);
					selectionIdStr = selection.toString();
					selectionIdArgsArr = selectionArgs
							.toArray(new String[selectionArgs.size()]);
				}
				if (enabled != null) {
					if (ids != null) {
						selection.insert(0, "(").append(")").append(" AND ");
					}
					selection.append(COL_ENABLED).append(" = ?");
					selectionArgs.add((enabled) ? "1" : "0");
				}
				selectionStr = selection.toString();
				selectionArgsArr = selectionArgs
						.toArray(new String[selectionArgs.size()]);
			}
			Map<ScriptId, List<String>> excludes = selectPatterns(TBL_EXCLUDE,
					selectionIdStr, selectionIdArgsArr);
			Map<ScriptId, List<String>> includes = selectPatterns(TBL_INCLUDE,
					selectionIdStr, selectionIdArgsArr);
			Map<ScriptId, List<String>> matches = selectPatterns(TBL_MATCH,
					selectionIdStr, selectionIdArgsArr);
			Map<ScriptId, List<ScriptRequire>> requires = selectRequires(
					TBL_REQUIRE, selectionIdStr, selectionIdArgsArr);
			Map<ScriptId, List<ScriptResource>> resources = selectResources(
					TBL_RESOURCE, selectionIdStr, selectionIdArgsArr);
			Cursor cursor = db.query(TBL_SCRIPT, COLS_SCRIPT, selectionStr,
					selectionArgsArr, null, null, null);
			Script[] scriptsArr = new Script[cursor.getCount()];
			int i = 0;
			while (cursor.moveToNext()) {
				String name = cursor.getString(0);
				String namespace = cursor.getString(1);
				ScriptId id = new ScriptId(name, namespace);
				List<String> exclude = excludes.get(id);
				String[] excludeArr = (exclude == null) ? null : exclude
						.toArray(new String[exclude.size()]);
				List<String> include = includes.get(id);
				String[] includeArr = (include == null) ? null : include
						.toArray(new String[include.size()]);
				List<String> match = matches.get(id);
				String[] matchArr = (match == null) ? null : match
						.toArray(new String[match.size()]);
				String description = cursor.getString(2);
				String downloadurl = cursor.getString(3);
				String updateurl = cursor.getString(4);
				String installurl = cursor.getString(5);
				String icon = cursor.getString(6);
				String runat = cursor.getString(7);
				int unwrap = cursor.getInt(8);
				String version = cursor.getString(9);
				List<ScriptRequire> require = requires.get(id);
				ScriptRequire[] requireArr = (require == null) ? null : require
						.toArray(new ScriptRequire[require.size()]);
				List<ScriptResource> resource = resources.get(id);
				ScriptResource[] resourceArr = (resource == null) ? null
						: resource.toArray(new ScriptResource[resource.size()]);
				String content = cursor.getString(10);
				scriptsArr[i] = new Script(name, namespace, excludeArr,
						includeArr, matchArr, description, downloadurl,
						updateurl, installurl, icon, runat, unwrap == 1,
						version, requireArr, resourceArr, content);
				i++;
			}
			cursor.close();
			return scriptsArr;
		}

		/**
		 * Retrieves script criteria objects from the database.
		 * 
		 * @param ids
		 *            the script IDs to match; an empty array to match none;
		 *            null to get all
		 * @param enabled
		 *            true to only get enabled scripts; false to only get
		 *            disabled scripts; null to get all
		 * @return an array of matching script criteria objects; an empty array
		 *         if none found
		 */
		public ScriptCriteria[] selectScriptCriteria(ScriptId[] ids,
				Boolean enabled) {
			String selectionStr = null, selectionIdStr = null;
			String[] selectionArgsArr = null, selectionIdArgsArr = null;
			if (ids != null || enabled != null) {
				StringBuilder selection = new StringBuilder();
				List<String> selectionArgs = new ArrayList<String>();
				if (ids != null) {
					if (ids.length == 0) {
						return new ScriptCriteria[0];
					}
					makeScriptIdSelectionArgs(ids, selection, selectionArgs);
					selectionIdStr = selection.toString();
					selectionIdArgsArr = selectionArgs
							.toArray(new String[selectionArgs.size()]);
				}
				if (enabled != null) {
					if (ids != null) {
						selection.insert(0, "(").append(")").append(" AND ");
					}
					selection.append(COL_ENABLED).append(" = ?");
					selectionArgs.add((enabled) ? "1" : "0");
				}
				selectionStr = selection.toString();
				selectionArgsArr = selectionArgs
						.toArray(new String[selectionArgs.size()]);
			}
			Map<ScriptId, List<String>> excludes = selectPatterns(TBL_EXCLUDE,
					selectionIdStr, selectionIdArgsArr);
			Map<ScriptId, List<String>> includes = selectPatterns(TBL_INCLUDE,
					selectionIdStr, selectionIdArgsArr);
			Map<ScriptId, List<String>> matches = selectPatterns(TBL_MATCH,
					selectionIdStr, selectionIdArgsArr);
			Cursor cursor = db.query(TBL_SCRIPT, COLS_ID, selectionStr,
					selectionArgsArr, null, null, null);
			ScriptCriteria[] scriptCriteriaArr = new ScriptCriteria[cursor
					.getCount()];
			int i = 0;
			while (cursor.moveToNext()) {
				String name = cursor.getString(0);
				String namespace = cursor.getString(1);
				ScriptId id = new ScriptId(name, namespace);
				List<String> exclude = excludes.get(id);
				String[] excludeArr = (exclude == null) ? null : exclude
						.toArray(new String[exclude.size()]);
				List<String> include = includes.get(id);
				String[] includeArr = (include == null) ? null : include
						.toArray(new String[include.size()]);
				List<String> match = matches.get(id);
				String[] matchArr = (match == null) ? null : match
						.toArray(new String[match.size()]);
				scriptCriteriaArr[i] = new ScriptCriteria(name, namespace,
						excludeArr, includeArr, matchArr);
				i++;
			}
			cursor.close();
			return scriptCriteriaArr;
		}

		/**
		 * Retrieves criteria patterns from the database.
		 * 
		 * @param tblName
		 *            the name of the table to query
		 * @param selection
		 *            the selection string (WHERE part of the query with
		 *            arguments replaced by ?)
		 * @param selectionArgs
		 *            the arguments to use in the selection string
		 * @return matching patterns found in the table mapped to script IDs; an
		 *         empty map if none found
		 */
		private Map<ScriptId, List<String>> selectPatterns(String tblName,
				String selection, String[] selectionArgs) {
			Map<ScriptId, List<String>> patterns = new HashMap<ScriptId, List<String>>();
			Cursor cursor = db.query(tblName, COLS_PATTERN, selection,
					selectionArgs, null, null, null);
			while (cursor.moveToNext()) {
				ScriptId id = new ScriptId(cursor.getString(0),
						cursor.getString(1));
				List<String> pattern = patterns.get(id);
				if (pattern == null) {
					pattern = new ArrayList<String>();
					patterns.put(id, pattern);
				}
				pattern.add(cursor.getString(2));
			}
			cursor.close();
			return patterns;
		}

		/**
		 * Retrieves require content from the database.
		 *
		 * @param tblName
		 *            the name of the table to query
		 * @param selection
		 *            the selection string (WHERE part of the query with
		 *            arguments replaced by ?)
		 * @param selectionArgs
		 *            the arguments to use in the selection string
		 * @return matching requires found in the table mapped to script IDs; an
		 *         empty map if none found
		 */
		private Map<ScriptId, List<ScriptRequire>> selectRequires(
				String tblName, String selection, String[] selectionArgs) {
			Map<ScriptId, List<ScriptRequire>> contents = new HashMap<ScriptId, List<ScriptRequire>>();
			Cursor cursor = db.query(tblName, COLS_REQUIRE, selection,
					selectionArgs, null, null, null);
			while (cursor.moveToNext()) {
				ScriptId id = new ScriptId(cursor.getString(cursor
						.getColumnIndex(COL_NAME)), cursor.getString(cursor
						.getColumnIndex(COL_NAMESPACE)));
				List<ScriptRequire> content = contents.get(id);
				if (content == null) {
					content = new ArrayList<ScriptRequire>();
					contents.put(id, content);
				}
				String requireUrl = cursor.getString(cursor
						.getColumnIndex(COL_DOWNLOADURL));
				String requireContent = cursor.getString(cursor
						.getColumnIndex(COL_CONTENT));
				content.add(new ScriptRequire(requireUrl, requireContent));
			}
			cursor.close();
			return contents;
		}

		/**
		 * Retrieves resource content from the database.
		 *
		 * @param tblName
		 *            the name of the table to query
		 * @param selection
		 *            the selection string (WHERE part of the query with
		 *            arguments replaced by ?)
		 * @param selectionArgs
		 *            the arguments to use in the selection string
		 * @return matching resources found in the table mapped to script IDs;
		 *         an empty map if none found
		 */
		private Map<ScriptId, List<ScriptResource>> selectResources(
				String tblName, String selection, String[] selectionArgs) {
			Map<ScriptId, List<ScriptResource>> contents = new HashMap<ScriptId, List<ScriptResource>>();
			Cursor cursor = db.query(tblName, COLS_RESOURCE, selection,
					selectionArgs, null, null, null);
			while (cursor.moveToNext()) {
				ScriptId id = new ScriptId(cursor.getString(cursor
						.getColumnIndex(COL_NAME)), cursor.getString(cursor
						.getColumnIndex(COL_NAMESPACE)));
				List<ScriptResource> content = contents.get(id);
				if (content == null) {
					content = new ArrayList<ScriptResource>();
					contents.put(id, content);
				}
				String resourceName = cursor.getString(cursor
						.getColumnIndex(COL_RESOURCENAME));
				String resourceUrl = cursor.getString(cursor
						.getColumnIndex(COL_DOWNLOADURL));
				byte[] resourceData = cursor.getBlob(cursor
						.getColumnIndex(COL_DATA));
				content.add(new ScriptResource(resourceName, resourceUrl,
						resourceData));
			}
			cursor.close();
			return contents;
		}

		/**
		 * Fills the selection string and arguments for queries searching for
		 * script IDs.
		 * 
		 * @param ids
		 *            the script IDs to use as selection arguments (input)
		 * @param selection
		 *            the selection string to fill (output)
		 * @param selectionArgs
		 *            the arguments to use in the selection string (output)
		 */
		private void makeScriptIdSelectionArgs(ScriptId[] ids,
				StringBuilder selection, List<String> selectionArgs) {
			for (ScriptId id : ids) {
				selection.append(" OR (").append(COL_NAME).append(" = ? AND ")
						.append(COL_NAMESPACE).append(" = ?)");
				selectionArgs.add(id.getName());
				selectionArgs.add(id.getNamespace());
			}
			selection.delete(0, 4);
		}

		/**
		 * Inserts a script into the database.
		 * 
		 * @param script
		 *            the script to insert
		 */
		public void insertScript(Script script) {
			ContentValues fieldsId = new ContentValues();
			fieldsId.put(COL_NAME, script.getName());
			fieldsId.put(COL_NAMESPACE, script.getNamespace());
			List<ContentValues> fieldsExcludes = new ArrayList<ContentValues>();
			String[] excludes = script.getExclude();
			if (excludes != null) {
				for (String pattern : excludes) {
					ContentValues fieldsExclude = new ContentValues(fieldsId);
					fieldsExclude.put(COL_PATTERN, pattern);
					fieldsExcludes.add(fieldsExclude);
				}
			}
			List<ContentValues> fieldsIncludes = new ArrayList<ContentValues>();
			String[] includes = script.getInclude();
			if (includes != null) {
				for (String pattern : includes) {
					ContentValues fieldsInclude = new ContentValues(fieldsId);
					fieldsInclude.put(COL_PATTERN, pattern);
					fieldsIncludes.add(fieldsInclude);
				}
			}
			List<ContentValues> fieldsMatches = new ArrayList<ContentValues>();
			String[] matches = script.getMatch();
			if (matches != null) {
				for (String pattern : matches) {
					ContentValues fieldsMatch = new ContentValues(fieldsId);
					fieldsMatch.put(COL_PATTERN, pattern);
					fieldsMatches.add(fieldsMatch);
				}
			}
			List<ContentValues> fieldsRequires = new ArrayList<ContentValues>();
			ScriptRequire[] requires = script.getRequires();
			if (requires != null) {
				for (ScriptRequire require : requires) {
					ContentValues fieldsRequire = new ContentValues(fieldsId);
					fieldsRequire.put(COL_DOWNLOADURL, require.getUrl());
					fieldsRequire.put(COL_CONTENT, require.getContent());
					fieldsRequires.add(fieldsRequire);
				}
			}
			List<ContentValues> fieldsResources = new ArrayList<ContentValues>();
			ScriptResource[] resources = script.getResources();
			if (resources != null) {
				for (ScriptResource resource : resources) {
					ContentValues fieldsResource = new ContentValues(fieldsId);
					fieldsResource.put(COL_RESOURCENAME, resource.getName());
					fieldsResource.put(COL_DOWNLOADURL, resource.getUrl());
					fieldsResource.put(COL_DATA, resource.getData());
					fieldsResources.add(fieldsResource);
				}
			}
			ContentValues fieldsScript = new ContentValues(fieldsId);
			fieldsScript.put(COL_DESCRIPTION, script.getDescription());
			fieldsScript.put(COL_DOWNLOADURL, script.getDownloadurl());
			fieldsScript.put(COL_UPDATEURL, script.getUpdateurl());
			fieldsScript.put(COL_INSTALLURL, script.getInstallurl());
			fieldsScript.put(COL_ICON, script.getIcon());
			fieldsScript.put(COL_RUNAT, script.getRunAt());
			fieldsScript.put(COL_UNWRAP, script.isUnwrap());
			fieldsScript.put(COL_VERSION, script.getVersion());
			fieldsScript.put(COL_CONTENT, script.getContent());
			fieldsScript.put(COL_ENABLED, true);
			db.beginTransaction();
			try {
				if (db.insert(TBL_SCRIPT, null, fieldsScript) == -1) {
					Log.e(TAG,
							"Error inserting new script into the database (table "
									+ TBL_SCRIPT + ")");
					return;
				}
				for (ContentValues fieldsExclude : fieldsExcludes) {
					if (db.insert(TBL_EXCLUDE, null, fieldsExclude) == -1) {
						Log.e(TAG,
								"Error inserting new script into the database (table "
										+ TBL_EXCLUDE + ")");
						return;
					}
				}
				for (ContentValues fieldsInclude : fieldsIncludes) {
					if (db.insert(TBL_INCLUDE, null, fieldsInclude) == -1) {
						Log.e(TAG,
								"Error inserting new script into the database (table "
										+ TBL_INCLUDE + ")");
						return;
					}
				}
				for (ContentValues fieldsMatch : fieldsMatches) {
					if (db.insert(TBL_MATCH, null, fieldsMatch) == -1) {
						Log.e(TAG,
								"Error inserting new script into the database (table "
										+ TBL_MATCH + ")");
						return;
					}
				}
				for (ContentValues fieldsRequire : fieldsRequires) {
					if (db.insert(TBL_REQUIRE, null, fieldsRequire) == -1) {
						Log.e(TAG,
								"Error inserting new script into the database (table "
										+ TBL_REQUIRE + ")");
						return;
					}
				}
				for (ContentValues fieldsResource : fieldsResources) {
					if (db.insert(TBL_RESOURCE, null, fieldsResource) == -1) {
						Log.e(TAG,
								"Error inserting new script into the database (table "
										+ TBL_RESOURCE + ")");
						return;
					}
				}
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}

		/**
		 * Deletes a script from the database.
		 * 
		 * @param id
		 *            the ID of the script to delete
		 */
		public void deleteScript(ScriptId id) {
			db.beginTransaction();
			try {
				db.delete(TBL_SCRIPT, COL_NAME + " = ? AND " + COL_NAMESPACE
						+ " = ?",
						new String[] { id.getName(), id.getNamespace() });
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}

		/**
		 * Updates the enabled column of a script in the database.
		 * 
		 * @param id
		 *            the ID of the script to update
		 * @param enabled
		 *            the new enabled value
		 */
		public void updateScriptEnabled(ScriptId id, boolean enabled) {
			ContentValues fields = new ContentValues();
			fields.put(COL_ENABLED, (enabled) ? 1 : 0);
			db.beginTransaction();
			try {
				db.update(TBL_SCRIPT, fields, COL_NAME + " = ? AND "
						+ COL_NAMESPACE + " = ?", new String[] { id.getName(),
						id.getNamespace() });
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}

		/**
		 * Retrieves all names of values owned by id.
		 * 
		 * @param id
		 *            the owner script
		 * @return an array of all names or an empty array if none found
		 */
		public String[] selectValueNames(ScriptId id) {
			String selection = COL_NAME + " = ? AND " + COL_NAMESPACE + " = ?";
			String[] selectionArgs = new String[] { id.getName(),
					id.getNamespace() };
			Cursor cursor = db.query(TBL_VALUE, new String[] { COL_VALUENAME },
					selection, selectionArgs, null, null, null);
			ArrayList<String> valueNames = new ArrayList<String>();
			while (cursor.moveToNext()) {
				valueNames.add(cursor.getString(0));
			}
			cursor.close();
			return valueNames.toArray(new String[valueNames.size()]);
		}

		/**
		 * Retrieves the value identified by name owned by id.
		 * 
		 * @param id
		 *            the owner script
		 * @param name
		 *            the key
		 * @return the value belonging to key and script, null if none found
		 */
		public String selectValue(ScriptId id, String name) {
			String selection = COL_NAME + " = ? AND " + COL_NAMESPACE
					+ " = ? AND " + COL_VALUENAME + " = ?";
			String[] selectionArgs = new String[] { id.getName(),
					id.getNamespace(), name };
			Cursor cursor = db.query(TBL_VALUE, new String[] { COL_VALUE },
					selection, selectionArgs, null, null, null);
			try {
				if (cursor.moveToFirst()) {
					return cursor.getString(0);
				}
			} finally {
				cursor.close();
			}
			return null;
		}

		/**
		 * Updates or inserts a name/value pair owned by id in the database.
		 * 
		 * @param id
		 *            the owner script
		 * @param name
		 *            the key
		 * @param value
		 *            the updated or new value
		 */
		public void updateOrInsertValue(ScriptId id, String name, String value) {
			String selection = COL_NAME + " = ? AND " + COL_NAMESPACE
					+ " = ? AND " + COL_VALUENAME + " = ?";
			String[] selectionArgs = new String[] { id.getName(),
					id.getNamespace(), name };
			ContentValues fields = new ContentValues();
			fields.put(COL_VALUE, value);
			db.beginTransaction();
			try {
				if (db.update(TBL_VALUE, fields, selection, selectionArgs) != 1) {
					fields.put(COL_NAME, id.getName());
					fields.put(COL_NAMESPACE, id.getNamespace());
					fields.put(COL_VALUENAME, name);
					if (db.insert(TBL_VALUE, null, fields) == -1) {
						Log.e(TAG,
								"Error inserting new value into the database (table "
										+ TBL_VALUE + ")");
						return;
					}
					db.setTransactionSuccessful();
				}
			} finally {
				db.endTransaction();
			}
		}

		/**
		 * Deletes a name/value pair owned by id from the database.
		 * 
		 * @param id
		 *            the owner script
		 * @param name
		 *            the key
		 */
		public void deleteValue(ScriptId id, String name) {
			String selection = COL_NAME + " = ? AND " + COL_NAMESPACE
					+ " = ? AND " + COL_VALUENAME + " = ?";
			String[] selectionArgs = new String[] { id.getName(),
					id.getNamespace(), name };
			db.beginTransaction();
			try {
				db.delete(TBL_VALUE, selection, selectionArgs);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}

	}

	/**
	 * Cache of user scripts matching most recently accessed URLs and all
	 * available and enabled user script matching criteria.
	 */
	private static class ScriptCache {

		private static final int CACHE_SIZE = 62;

		private LinkedHashMap<String, Script[]> urlScripts = new LinkedHashMap<String, Script[]>(
				CACHE_SIZE + 2, 1.0f, true) {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(
					Entry<String, Script[]> eldest) {
				return size() > CACHE_SIZE;
			}

		};

		private ScriptCriteria[] scriptCriteriaArr;

		/**
		 * Looks if the given URL has a cache of matching user scripts.
		 * 
		 * @param url
		 *            the URL to look up
		 * @return if the URL is cached either the found user scripts or an
		 *         empty array; if the URL is not cached then null
		 */
		public synchronized Script[] get(String url) {
			return urlScripts.get(url);
		}

		/**
		 * Caches a URL and its matching user scripts.
		 * 
		 * @param url
		 *            the URL to cache
		 * @param scripts
		 *            the user scripts to execute at that URL
		 */
		public synchronized void put(String url, Script[] scripts) {
			urlScripts.put(url, scripts);
		}

		/**
		 * Goes through all user script criteria to find all that need to be run
		 * for the given URL.
		 * 
		 * @param url
		 *            the URL to match
		 * @return an array of matching user script IDs; an empty array if none
		 *         matched
		 */
		public ScriptId[] getMatchingScriptIds(String url) {
			List<ScriptId> matches = new ArrayList<ScriptId>();
			ScriptCriteria[] criteriaArr = scriptCriteriaArr;
			for (ScriptCriteria c : criteriaArr) {
				if (c.testUrl(url)) {
					matches.add(c);
				}
			}
			return matches.toArray(new ScriptId[matches.size()]);
		}

		/**
		 * Caches the array of user script criteria to be used when matching
		 * URLs.
		 * 
		 * @param scriptCriteriaArr
		 *            the array to cache
		 */
		public void setScriptCriteriaArr(ScriptCriteria[] scriptCriteriaArr) {
			this.scriptCriteriaArr = scriptCriteriaArr;
		}

	}

}
