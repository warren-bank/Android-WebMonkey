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

package at.pardus.android.webview.gm.run;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.store.ScriptStore;
import at.pardus.android.webview.gm.util.ScriptJsCode;

/**
 * A user script enabled WebViewClient to be used by WebViewGm.
 */
public class WebViewClientGm extends WebViewClient {

  private static final String TAG = WebViewClientGm.class.getName();

  protected static void initStaticResources(Context context) {
    ScriptJsCode.initStaticResources(context);
  }

  private ScriptStore scriptStore;

  private String jsBridgeName;

  private String secret;

  private ScriptJsCode scriptJsCode;

  /**
   * Constructs a new WebViewClientGm with a ScriptStore.
   * 
   * @param scriptStore
   *            the script database to query for scripts to run when a page
   *            starts/finishes loading
   * @param jsBridgeName
   *            the variable name to access the webview GM functions from
   *            javascript code
   * @param secret
   *            a random string that is added to calls of the GM API
   */
  public WebViewClientGm(ScriptStore scriptStore, String jsBridgeName, String secret) {
    this.scriptStore = scriptStore;
    this.jsBridgeName = jsBridgeName;
    this.secret = secret;
    this.scriptJsCode = new ScriptJsCode();
  }

  /**
   * Runs user scripts enabled for a given URL.
   * 
   * Unless a script specifies unwrap it is executed inside an anonymous
   * function to hide it from access from the loaded page. Calls to the global
   * JavaScript bridge methods require a secret that is set inside of each
   * user script's anonymous function.
   * 
   * @param view
   *            the view to load scripts in
   * @param url
   *            the current address
   * @param pageFinished
   *            true if scripts with runAt property set to document-end or
   *            null should be run, false if set to document-start
   * @param jsBeforeScript
   *            JavaScript code to add between the GM API and the start of the
   *            user script code (may be null)
   * @param jsAfterScript
   *            JavaScript code to add after the end of the user script code
   *            (may be null)
   */
  protected void runMatchingScripts(WebView view, String url, boolean pageFinished, String jsBeforeScript, String jsAfterScript) {
    String jsCode = getMatchingScripts(url, pageFinished, jsBeforeScript, jsAfterScript);

    if (TextUtils.isEmpty(jsCode))
      return;

    if (Build.VERSION.SDK_INT >= 19)
      view.evaluateJavascript(jsCode, null);
    else
      view.loadUrl("javascript:\n" + jsCode);
  }

  public String getMatchingScripts(String url, boolean pageFinished, String jsBeforeScript, String jsAfterScript) {
    if (scriptStore == null) {
      Log.w(TAG, "Property scriptStore is null - not running any scripts");
      return null;
    }
    Script[] matchingScripts = scriptStore.get(url);
    if (matchingScripts == null) {
      return null;
    }

    StringBuilder sb = new StringBuilder(4 * 1024);
    int length = 0;

    for (Script script : matchingScripts) {
      sb.append(
        scriptJsCode.getJsCode(script, pageFinished, jsBeforeScript, jsAfterScript, jsBridgeName, secret)
      );

      if (sb.length() > length) {
        length = sb.length();
        Log.i(TAG, "Running script \"" + script.toString() + "\" on " + url);
      }
    }
    return sb.toString();
  }

  @Override
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    runMatchingScripts(view, url, false, null, null);
  }

  @Override
  public void onPageFinished(WebView view, String url) {
    runMatchingScripts(view, url, true, null, null);
  }

  /**
   * @return the scriptStore
   */
  public ScriptStore getScriptStore() {
    return scriptStore;
  }

  /**
   * @param scriptStore
   *            the scriptStore to set
   */
  public void setScriptStore(ScriptStore scriptStore) {
    this.scriptStore = scriptStore;
  }

  /**
   * @return the jsBridgeName
   */
  public String getJsBridgeName() {
    return jsBridgeName;
  }

  /**
   * @param jsBridgeName
   *            the jsBridgeName to set
   */
  public void setJsBridgeName(String jsBridgeName) {
    this.jsBridgeName = jsBridgeName;
  }

  /**
   * @return the secret
   */
  public String getSecret() {
    return secret;
  }

  /**
   * @param secret
   *            the secret to set
   */
  public void setSecret(String secret) {
    this.secret = secret;
  }

  /**
   * @return the scriptJsCode
   */
  public ScriptJsCode getScriptJsCode() {
    return scriptJsCode;
  }

  /**
   * @param scriptJsCode
   *            the scriptJsCode to set
   */
  public void setScriptJsCode(ScriptJsCode scriptJsCode) {
    this.scriptJsCode = scriptJsCode;
  }

}
