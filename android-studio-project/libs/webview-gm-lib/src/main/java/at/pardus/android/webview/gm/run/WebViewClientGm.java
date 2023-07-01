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

import java.util.UUID;

import at.pardus.android.webview.gm.R;
import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.model.ScriptRequire;
import at.pardus.android.webview.gm.store.ScriptStore;
import at.pardus.android.webview.gm.util.ResourceHelper;
import at.pardus.android.webview.gm.util.ScriptInfo;

/**
 * A user script enabled WebViewClient to be used by WebViewGm.
 */
public class WebViewClientGm extends WebViewClient {

  private static final String TAG = WebViewClientGm.class.getName();

  private static final String JSCONTAINERSTART = "(function() {\n";

  private static final String JSCONTAINEREND = "\n})()";

  private static final String JSUNSAFEWINDOW = "var unsafeWindow = window.window; window.wrappedJSObject = window.wrappedJSObject || unsafeWindow || {};\n";

  private static final String JSMISSINGFUNCTIONS =
        "var GM_notImplemented = "            + "function(method_name) { GM_log((method_name ? method_name : 'Called') + ' function not yet implemented'); };\n"

      + "var GM_addValueChangeListener = "    + "GM_notImplemented.bind(null, 'GM_addValueChangeListener');\n"
      + "var GM_download = "                  + "GM_notImplemented.bind(null, 'GM_download');\n"
      + "var GM_getTab = "                    + "GM_notImplemented.bind(null, 'GM_getTab');\n"
      + "var GM_getTabs = "                   + "GM_notImplemented.bind(null, 'GM_getTabs');\n"
      + "var GM_info = "                      + "GM_notImplemented.bind(null, 'GM_info');\n"
      + "var GM_notification = "              + "GM_notImplemented.bind(null, 'GM_notification');\n"
      + "var GM_openInTab = "                 + "GM_notImplemented.bind(null, 'GM_openInTab');\n"
      + "var GM_removeValueChangeListener = " + "GM_notImplemented.bind(null, 'GM_removeValueChangeListener');\n"
      + "var GM_saveTab = "                   + "GM_notImplemented.bind(null, 'GM_saveTab');\n"
      + "var GM_setClipboard = "              + "GM_notImplemented.bind(null, 'GM_setClipboard');\n"
      + "var GM_webRequest = "                + "GM_notImplemented.bind(null, 'GM_webRequest');\n"

      + "var GM_cookie = {\n"
      + "  \"list\":   "                      + "GM_notImplemented.bind(null, 'GM_cookie.list'),\n"
      + "  \"set\":    "                      + "GM_notImplemented.bind(null, 'GM_cookie.set'),\n"
      + "  \"delete\": "                      + "GM_notImplemented.bind(null, 'GM_cookie.delete')\n"
      + "};\n";

  private static String JSAPIHELPERFUNCTIONS = "";

  protected static void initStaticResources(Context context) {
    if (TextUtils.isEmpty(JSAPIHELPERFUNCTIONS)) {
      try {
        JSAPIHELPERFUNCTIONS = ResourceHelper.getRawStringResource(context, R.raw.js_api_helper_functions);
      }
      catch(Exception e) {}
    }

    ScriptInfo.initStaticResources(context);
  }

  private ScriptStore scriptStore;

  private String jsBridgeName;

  private String secret;

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

    String jsCode = "";
    StringBuilder sb;
    if (jsBeforeScript == null) {
      jsBeforeScript = "";
    }
    if (jsAfterScript == null) {
      jsAfterScript = "";
    }
    for (Script script : matchingScripts) {
      if (
        (!pageFinished && Script.RUNATSTART.equals(script.getRunAt())) ||
        (pageFinished && (script.getRunAt() == null || Script.RUNATEND.equals(script.getRunAt())))
      ) {
        Log.i(TAG, "Running script \"" + script + "\" on " + url);

        // defaultSignature
        sb = new StringBuilder(1 * 1024);
        sb.append("\"");
        sb.append(script.getName().replace("\"", "\\\""));
        sb.append("\", \"");
        sb.append(script.getNamespace().replace("\"", "\\\""));
        sb.append("\", \"");
        sb.append(secret);
        sb.append("\"");
        String defaultSignature = sb.toString();
        sb = null;

        // callbackPrefix
        sb = new StringBuilder(1 * 1024);
        sb.append("GM_");
        sb.append(script.getName());
        sb.append(script.getNamespace());
        sb.append(UUID.randomUUID().toString());
        String callbackPrefix = sb.toString().replaceAll("[^0-9a-zA-Z_]", "");
        sb = null;

        // jsApi
        sb = new StringBuilder(4 * 1024);
        sb.append(JSUNSAFEWINDOW);

        // --------------------------------
        // TODO implement missing functions
        // --------------------------------
        sb.append(JSMISSINGFUNCTIONS);

        // -------------------------
        // Greasemonkey API (legacy)
        // -------------------------

        sb.append("var GM_info = ");
        sb.append(ScriptInfo.toJSONString(script));
        sb.append(";");
        sb.append("\n");

        sb.append("var GM_listValues = function() { return ");
        sb.append(jsBridgeName);
        sb.append(".listValues(");
        sb.append(defaultSignature);
        sb.append(").split(\",\"); };");
        sb.append("\n");

        sb.append("var GM_getValue = function(name, defaultValue) { ");
        sb.append("if (defaultValue === undefined) {defaultValue = null;} ");
        sb.append("defaultValue = JSON.stringify(defaultValue); ");
        sb.append("return JSON.parse(");
        sb.append(jsBridgeName);
        sb.append(".getValue(");
        sb.append(defaultSignature);
        sb.append(", name, defaultValue)");
        sb.append("); };");
        sb.append("\n");

        sb.append("var GM_setValue = function(name, value) { ");
        sb.append("if (value === undefined) {value = null;} ");
        sb.append("value = JSON.stringify(value); ");
        sb.append(jsBridgeName);
        sb.append(".setValue(");
        sb.append(defaultSignature);
        sb.append(", name, value); };");
        sb.append("\n");

        sb.append("var GM_deleteValue = function(name) { ");
        sb.append(jsBridgeName);
        sb.append(".deleteValue(");
        sb.append(defaultSignature);
        sb.append(", name); };");
        sb.append("\n");

        sb.append("var GM_log = function(message) { ");
        sb.append(jsBridgeName);
        sb.append(".log(");
        sb.append(defaultSignature);
        sb.append(", message); };");
        sb.append("\n");

        sb.append("var GM_getResourceURL = function(resourceName) { return ");
        sb.append(jsBridgeName);
        sb.append(".getResourceURL(");
        sb.append(defaultSignature);
        sb.append(", resourceName); };");
        sb.append("\n");

        sb.append("var GM_getResourceText = function(resourceName) { return ");
        sb.append(jsBridgeName);
        sb.append(".getResourceText(");
        sb.append(defaultSignature);
        sb.append(", resourceName); };");
        sb.append("\n");

        sb.append("var GM_xmlhttpRequest = function(details) { \n");
        // onabort
        sb.append("if (details.onabort) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_onAbortCallback = details.onabort;\n");
        sb.append("details.onabort = '");
        sb.append(callbackPrefix);
        sb.append("GM_onAbortCallback'; }\n");
        // onerror
        sb.append("if (details.onerror) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_onErrorCallback = details.onerror;\n");
        sb.append("details.onerror = '");
        sb.append(callbackPrefix);
        sb.append("GM_onErrorCallback'; }\n");
        // onload
        sb.append("if (details.onload) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_onLoadCallback = details.onload;\n");
        sb.append("details.onload = '");
        sb.append(callbackPrefix);
        sb.append("GM_onLoadCallback'; }\n");
        // onprogress
        sb.append("if (details.onprogress) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_onProgressCallback = details.onprogress;\n");
        sb.append("details.onprogress = '");
        sb.append(callbackPrefix);
        sb.append("GM_onProgressCallback'; }\n");
        // onreadystatechange
        sb.append("if (details.onreadystatechange) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_onReadyStateChange = details.onreadystatechange;\n");
        sb.append("details.onreadystatechange = '");
        sb.append(callbackPrefix);
        sb.append("GM_onReadyStateChange'; }\n");
        // ontimeout
        sb.append("if (details.ontimeout) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_onTimeoutCallback = details.ontimeout;\n");
        sb.append("details.ontimeout = '");
        sb.append(callbackPrefix);
        sb.append("GM_onTimeoutCallback'; }\n");
        // upload
        sb.append("if (details.upload) {\n");
        // upload.onabort
        sb.append("if (details.upload.onabort) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_uploadOnAbortCallback = details.upload.onabort;\n");
        sb.append("details.upload.onabort = '");
        sb.append(callbackPrefix);
        sb.append("GM_uploadOnAbortCallback'; }\n");
        // upload.onerror
        sb.append("if (details.upload.onerror) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_uploadOnErrorCallback = details.upload.onerror;\n");
        sb.append("details.upload.onerror = '");
        sb.append(callbackPrefix);
        sb.append("GM_uploadOnErrorCallback'; }\n");
        // upload.onload
        sb.append("if (details.upload.onload) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_uploadOnLoadCallback = details.upload.onload;\n");
        sb.append("details.upload.onload = '");
        sb.append(callbackPrefix);
        sb.append("GM_uploadOnLoadCallback'; }\n");
        // upload.onprogress
        sb.append("if (details.upload.onprogress) { window.wrappedJSObject.");
        sb.append(callbackPrefix);
        sb.append("GM_uploadOnProgressCallback = details.upload.onprogress;\n");
        sb.append("details.upload.onprogress = '");
        sb.append(callbackPrefix);
        sb.append("GM_uploadOnProgressCallback'; }\n");
        // upload
        sb.append("}\n");
        // return value: WebViewXmlHttpResponse.toJSONString()
        sb.append("return JSON.parse(");
        sb.append(jsBridgeName);
        sb.append(".xmlHttpRequest(");
        sb.append(defaultSignature);
        sb.append(", JSON.stringify(details))); };");
        sb.append("\n");

        // -----------------------
        // static helper functions
        // -----------------------
        sb.append(JSAPIHELPERFUNCTIONS);

        String jsApi = sb.toString();
        sb = null;

        // Get @require'd scripts to inject for this script.
        // jsAllRequires
        sb = new StringBuilder(4 * 1024);
        ScriptRequire[] requires = script.getRequires();
        if (requires != null) {
          for (ScriptRequire currentRequire : requires) {
            sb.append(currentRequire.getContent());
            sb.append("\n");
          }
        }
        String jsAllRequires = sb.toString();
        sb = null;

        // jsCode
        sb = new StringBuilder(4 * 1024);
        if (!script.isUnwrap()) {
          sb.append(JSCONTAINERSTART);
        }
        sb.append(jsApi);
        sb.append(jsAllRequires);
        sb.append(jsBeforeScript);
        sb.append(script.getContent());
        sb.append(jsAfterScript);
        if (!script.isUnwrap()) {
          sb.append(JSCONTAINEREND);
        }
        jsCode += sb.toString();
        sb = null;
      }
    }
    return jsCode;
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

}
