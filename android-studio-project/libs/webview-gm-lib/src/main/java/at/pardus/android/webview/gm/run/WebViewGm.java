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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.UUID;

import at.pardus.android.webview.gm.store.ScriptStore;

/**
 * A user script enabled WebView.
 * 
 * Initializes the WebView with a WebViewClientGm. If this object is inflated by
 * XML run setScriptStore to enable script support.
 */
public class WebViewGm extends WebView {

  private static final String JSBRIDGENAME = "WebViewGM";

  private ScriptStore scriptStore;

  private WebViewClientGm webViewClient;

  /**
   * Constructs a new WebViewGm initializing it with a ScriptStore.
   * 
   * @param context
   *            the application's context
   * @param scriptStore
   *            the script database to use
   */
  public WebViewGm(Context context, ScriptStore scriptStore) {
    super(context);
    init(context);
    setScriptStore(scriptStore);
  }

  /**
   * Constructs a new WebViewGm with a Context object.
   * 
   * @param context
   *            the application's context
   */
  public WebViewGm(Context context) {
    super(context);
    init(context);
  }

  /**
   * Constructs a new WebViewGm with layout parameters.
   * 
   * @param context
   *            the application's context
   * @param attrs
   *            layout parameters
   */
  public WebViewGm(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context);
  }

  /**
   * Constructs a new WebViewGm with layout parameters and a default style.
   * 
   * @param context
   *            the application's context
   * @param attrs
   *            layout parameters
   * @param defStyle
   *            default style resource ID
   */
  public WebViewGm(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init(context);
  }

  /**
   * Called by the constructors to set up the WebView to enable user scripts.
   */
  @SuppressLint("SetJavaScriptEnabled")
  private void init(Context context) {
    WebSettings settings = getSettings();
    settings.setJavaScriptEnabled(true);

    WebViewClientGm.setJsApiHelperFunctions(context);
    webViewClient = new WebViewClientGm(scriptStore, JSBRIDGENAME, generateSecret());
    setWebViewClient(webViewClient);
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
    @SuppressLint("AddJavascriptInterface")
    public void setScriptStore(ScriptStore scriptStore) {
    this.scriptStore = scriptStore;
    addJavascriptInterface(new WebViewGmApi(this, scriptStore,
        webViewClient.getSecret()), JSBRIDGENAME);
    webViewClient.setScriptStore(scriptStore);
  }

  /**
   * @return the webViewClient
   */
  public WebViewClientGm getWebViewClient() {
    return webViewClient;
  }

  /**
   * @param webViewClient
   *            the WebViewClientGm to set as WebViewClient
   */
  public void setWebViewClient(WebViewClientGm webViewClient) {
    this.webViewClient = webViewClient;
    super.setWebViewClient(webViewClient);
  }

  /**
   * @return a random string to use in GM API calls
   */
  private static String generateSecret() {
    return UUID.randomUUID().toString();
  }

}
