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

import android.app.Activity;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

import at.pardus.android.webview.gm.model.ScriptId;
import at.pardus.android.webview.gm.store.ScriptStoreSQLite;

/**
 * Extend this class to use this package's UI to manage user scripts.
 */
public abstract class ScriptManagerActivity extends Activity {

  protected ScriptStoreSQLite scriptStore;

  protected ScriptList scriptList;

  protected ScriptEditor scriptEditor;

  protected ScriptBrowser scriptBrowser;

  /**
   * Displays the list of all installed user scripts.
   */
  public abstract void openScriptList();

  /**
   * Displays a form to edit an existing or create a new script.
   * 
   * @param scriptId
   *            the script to edit or create if null
   */
  public abstract void openScriptEditor(ScriptId scriptId);

  /**
   * Displays the browser configured to handle user script downloads.
   */
  public abstract void openScriptBrowser();

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v,
      ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (scriptList != null && v.equals(scriptList.getScriptList())) {
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.script_list_menu, menu);
    }
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
        .getMenuInfo();
    ScriptId scriptId = scriptList.getScriptId(info.position);
    if (item.getItemId() == R.id.menu_edit) {
      openScriptEditor(scriptId);
      return true;
    } else if (item.getItemId() == R.id.menu_delete) {
      scriptStore.delete(scriptId);
      openScriptList();
      Toast.makeText(this,
          getString(R.string.deleted_script) + " " + scriptId,
          Toast.LENGTH_SHORT).show();
      return true;
    } else if (item.getItemId() == R.id.menu_disable) {
      scriptStore.disable(scriptId);
      openScriptList();
      Toast.makeText(this,
          getString(R.string.disabled_script) + " " + scriptId,
          Toast.LENGTH_SHORT).show();
      return true;
    } else if (item.getItemId() == R.id.menu_enable) {
      scriptStore.enable(scriptId);
      openScriptList();
      Toast.makeText(this,
          getString(R.string.enabled_script) + " " + scriptId,
          Toast.LENGTH_SHORT).show();
      return true;
    } else {
      return super.onContextItemSelected(item);
    }
  }

}
