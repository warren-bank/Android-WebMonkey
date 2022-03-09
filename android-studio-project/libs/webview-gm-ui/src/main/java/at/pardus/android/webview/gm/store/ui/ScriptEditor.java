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
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.model.ScriptId;
import at.pardus.android.webview.gm.store.ScriptStore;

/**
 * Includes the UI to edit and save scripts.
 */
public class ScriptEditor {

  protected ScriptManagerActivity activity;

  protected ScriptStore scriptStore;

  protected Script loadedScript;

  protected View editForm;

  protected EditText scriptContent;

  protected Button saveButton;

  /**
   * Returns the form to edit a script.
   * 
   * @param scriptId
   *            the ID of the script to edit or null to add a new script
   * @return the edit script view
   */
  public View getEditForm(ScriptId scriptId) {
    Script script = null;
    if (scriptId != null) {
      script = scriptStore.get(scriptId);
    }
    if (script != null) {
      scriptContent.setText(script.getContent());
    }
    loadedScript = script;
    return editForm;
  }

  /**
   * Saves an existing or new script.
   */
  protected void saveScript() {
    new Thread() {
      public void run() {
        Script script = Script.parse(
            scriptContent.getText().toString(),
            (loadedScript == null) ? null : loadedScript
                .getDownloadurl());
        if (script == null) {
          makeToastOnUiThread(
              activity.getString(R.string.error_saving_script)
                  + ": "
                  + activity
                      .getString(R.string.syntax_or_dl_fail),
              Toast.LENGTH_LONG);
          return;
        }
        if (loadedScript == null) {
          scriptStore.add(script);
          makeToastOnUiThread(
              activity.getString(R.string.added_new_script) + " "
                  + script.getName(), Toast.LENGTH_LONG);
        } else {
          if (!loadedScript.equals(script)) {
            if (scriptStore.get(script) != null) {
              makeToastOnUiThread(
                  activity.getString(R.string.error_saving_script)
                      + ": "
                      + activity
                          .getString(R.string.new_script_id_exists),
                  Toast.LENGTH_LONG);
              return;
            }
            scriptStore.delete(loadedScript);
          }
          scriptStore.add(script);
          makeToastOnUiThread(
              activity.getString(R.string.edited_script) + " "
                  + script.getName(), Toast.LENGTH_SHORT);
        }
        loadedScript = null;
        openScriptListOnUiThread();
      }
    }.start();
  }

  /**
   * Display a message on the UI thread.
   * 
   * @param text
   *            the message to display
   * @param duration
   *            how long to display the message
   */
  private void makeToastOnUiThread(final CharSequence text, final int duration) {
    activity.runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(activity, text, duration).show();
      }
    });
  }

  /**
   * Switches back to script list on the UI thread.
   */
  private void openScriptListOnUiThread() {
    activity.runOnUiThread(new Runnable() {
      public void run() {
        activity.openScriptList();
      }
    });
  }

  /**
   * Inflates the text area and save button from XML and registers its
   * OnClickListener.
   */
  @SuppressLint("InflateParams")
    private void init() {
    editForm = activity.getLayoutInflater().inflate(
        R.layout.edit_script, null);
    scriptContent = (EditText) editForm.findViewById(R.id.script_content);
    saveButton = (Button) editForm.findViewById(R.id.save_button);
    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        saveScript();
        ((InputMethodManager) activity
            .getSystemService(Context.INPUT_METHOD_SERVICE))
            .hideSoftInputFromWindow(v.getWindowToken(), 0);
      }
    });
  }

  /**
   * Constructor.
   * 
   * @param activity
   *            the application's activity
   * @param scriptStore
   *            the database to use
   */
  public ScriptEditor(ScriptManagerActivity activity, ScriptStore scriptStore) {
    this.activity = activity;
    this.scriptStore = scriptStore;
    init();
  }

}
