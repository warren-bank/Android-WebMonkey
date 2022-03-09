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

package at.pardus.android.webview.gm.store.ui;

import android.annotation.SuppressLint;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.model.ScriptId;
import at.pardus.android.webview.gm.store.ScriptStore;

/**
 * Includes the UI to list, enable/disable and delete scripts.
 */
public class ScriptList {

  protected ScriptManagerActivity activity;

  protected ScriptStore scriptStore;

  protected ListView scriptList;

  /**
   * Returns the view listing all installed scripts.
   * 
   * @return the updated list view
   */
  public View getScriptList() {
    // TODO separate look (or list) for disabled scripts
    Script[] scripts = scriptStore.getAll();
    scriptList.setAdapter(new ArrayAdapter<Script>(activity,
        R.layout.script_list_item, scripts));
    scriptList.invalidate();
    return scriptList;
  }

  /**
   * Returns the ScriptId object for a given position in the list.
   * 
   * @param position
   *            the selected position
   * @return the ScriptId object at that position
   */
  public ScriptId getScriptId(int position) {
    return (ScriptId) scriptList.getItemAtPosition(position);
  }

  /**
   * Inflates the ListView from XML and registers its OnItemClickListener and
   * context menu.
   */
  @SuppressLint("InflateParams")
    private void init() {
    scriptList = (ListView) activity.getLayoutInflater().inflate(
        R.layout.script_list, null);
    scriptList.setTextFilterEnabled(true);
    scriptList.setOnItemClickListener(new OnItemClickListener() {
      public void onItemClick(AdapterView<?> parent, View view,
          int position, long id) {
        activity.openScriptEditor(getScriptId(position));
      }
    });
    activity.registerForContextMenu(scriptList);
  }

  /**
   * Constructor.
   * 
   * @param activity
   *            the application's activity
   * @param scriptStore
   *            the database to use
   */
  public ScriptList(ScriptManagerActivity activity, ScriptStore scriptStore) {
    this.activity = activity;
    this.scriptStore = scriptStore;
    init();
  }

}
