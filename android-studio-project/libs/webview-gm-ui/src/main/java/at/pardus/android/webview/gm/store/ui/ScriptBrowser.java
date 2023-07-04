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
import android.graphics.Bitmap;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.run.WebViewClientGm;
import at.pardus.android.webview.gm.run.WebViewGm;
import at.pardus.android.webview.gm.store.ScriptStore;
import at.pardus.android.webview.gm.util.DownloadHelper;

/**
 * Combines an address field and a WebView intercepting .user.js file downloads
 * to add them to a provided script store.
 */
public class ScriptBrowser {

  private static final String TAG = ScriptBrowser.class.getName();

  protected ScriptManagerActivity activity;

  protected ScriptStore scriptStore;

  private String startUrl;

  private String previousUrl;
  private String currentUrl;

  protected View browser;

  protected WebViewGm webView;

  protected EditText addressField;

  /**
   * Installs a new script.
   *
   * Not to be run on the UI thread.
   *
   * @param url
   *            the location of the script to install
   */
  protected void installScript(String url) {
    makeToastOnUiThread(activity.getString(R.string.starting_download_of) + " " + url, Toast.LENGTH_SHORT);
    String scriptStr = DownloadHelper.downloadScript(url);
    if (scriptStr == null) {
      makeToastOnUiThread(activity.getString(R.string.error_downloading_from) + " " + url, Toast.LENGTH_LONG);
      return;
    }
    Script script = Script.parse(scriptStr, url);
    if (script == null) {
      Log.d(TAG, "Error parsing script:\n" + scriptStr);
      makeToastOnUiThread(activity.getString(R.string.error_parsing_at) + " " + url, Toast.LENGTH_LONG);
      return;
    }

    scriptStore.add(script);
    makeToastOnUiThread(activity.getString(R.string.added_new_script) + " " + script, Toast.LENGTH_LONG);
  }

  protected boolean checkDownload(final String url) {
    if (url.endsWith(".user.js")) {
      // TODO ask before installing new script
      new Thread() {
        public void run() {
          installScript(url);
        }
      }.start();
      return true;
    }
    return false;
  }

  /**
   * Inflates the WebViewGm from XML and sets up its WebViewClient,
   * WebChromeClient and DownloadListener. Also inflates and sets up the
   * address field EditText component.
   */
  @SuppressLint("InflateParams")
  private void init() {
    browser = activity.getLayoutInflater().inflate(R.layout.script_browser, null);
    webView = (WebViewGm) browser.findViewById(R.id.webView);
    webView.setScriptStore(scriptStore);
    addressField = (EditText) browser.findViewById(R.id.addressField);
    addressField
        .setOnEditorActionListener(new EditText.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if ((actionId == EditorInfo.IME_ACTION_GO) || (actionId == EditorInfo.IME_NULL)) {
              String url = v.getText().toString();
              loadUrl(url, /* reloadCurrentUrl= */ true);
              webView.requestFocus();
              ((InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE))
                  .hideSoftInputFromWindow(v.getWindowToken(), 0);
              return true;
            }
            return false;
          }
        });
    webView.setWebViewClient(
      new ScriptBrowserWebViewClientGm(
        scriptStore,
        webView.getWebViewClient().getJsBridgeName(),
        webView.getWebViewClient().getSecret(),
        this
      )
    );
    webView.setDownloadListener(new ScriptBrowserDownloadListener(this));
    webView.setWebChromeClient(new ScriptBrowserWebChromeClient(this));
    loadUrl(startUrl);
  }

  public void changeAddressField(String url) {
    if ((url != null) && !url.isEmpty() && !url.equals(currentUrl)) {
      previousUrl = currentUrl;
      currentUrl = url;
      addressField.setText(url);
    }
  }

  /**
   * Load the given URL.
   *
   * @param url
   *            the address to load
   */
  public void loadUrl(String url) {
    loadUrl(url, /* reloadCurrentUrl= */ false);
  }

  protected void loadUrl(String url, boolean reloadCurrentUrl) {
    if ((url != null) && !url.isEmpty()) {
      if (!url.equals(currentUrl)) {
        changeAddressField(url);

        webView.loadUrl(url);
      }
      else if (reloadCurrentUrl) {
        webView.reload();
      }
    }
  }

  public String getCurrentUrl() {
    return currentUrl;
  }

  protected String getPreviousUrl() {
    return previousUrl;
  }

  protected void clearPreviousUrl() {
    previousUrl = null;
  }

  /**
   * Constructor.
   *
   * @param activity
   *            the application's activity
   * @param scriptStore
   *            the database to use
   */
  public ScriptBrowser(ScriptManagerActivity activity, ScriptStore scriptStore, String startUrl) {
    this.activity = activity;
    this.scriptStore = scriptStore;
    this.startUrl = startUrl;
    init();
  }

  /**
   * Goes back to the previous browser page.
   *
   * @return false if the browser history is empty
   */
  public boolean back() {
    if (webView.canGoBack()) {
      webView.goBack();
      return true;
    }
    return false;
  }

  /**
   * Stops any browser activity.
   */
  public void pause() {
    webView.stopLoading();
    webView.pauseTimers();
  }

  /**
   * Resumes browser timers.
   */
  public void resume() {
    webView.resumeTimers();
  }

  /**
   * @return the browser view group
   */
  public View getBrowser() {
    return browser;
  }

  /**
   * @return the webView
   */
  public WebViewGm getWebView() {
    return webView;
  }

  /**
   * Displays a message created on the UI thread.
   *
   * @param message
   *            the message to show
   * @param length
   *            the duration (use Toast.LENGTH_ constants)
   */
  private void makeToastOnUiThread(final String message, final int length) {
    activity.runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(activity, message, length).show();
      }
    });
  }

  /**
   * Detect whether a data URI is loaded in WebView.
   *
   * This occurs when:
   * - GM_loadFrame() is called by userscript
   *     reference: https://github.com/warren-bank/Android-WebMonkey/blob/v01.00.23/android-studio-project/WebMonkey/src/main/java/com/github/warren_bank/webmonkey/WmJsApi.java#L334
   * - loadFrame() handles call, by deferring to either..
   *     reference: https://github.com/warren-bank/Android-WebMonkey/blob/v01.00.23/android-studio-project/WebMonkey/src/main/java/com/github/warren_bank/webmonkey/WmJsApi.java#L191
   * - loadFrame_srcdoc()
   *     reference: https://github.com/warren-bank/Android-WebMonkey/blob/v01.00.23/android-studio-project/WebMonkey/src/main/java/com/github/warren_bank/webmonkey/WmJsApi.java#L211
   *       - title: https://github.com/warren-bank/Android-WebMonkey/blob/v01.00.23/android-studio-project/WebMonkey/src/main/java/com/github/warren_bank/webmonkey/WmJsApi.java#L236
   *       - mime:  https://github.com/warren-bank/Android-WebMonkey/blob/v01.00.23/android-studio-project/WebMonkey/src/main/java/com/github/warren_bank/webmonkey/WmJsApi.java#L253
   * - loadFrame_src()
   *     reference: https://github.com/warren-bank/Android-WebMonkey/blob/v01.00.23/android-studio-project/WebMonkey/src/main/java/com/github/warren_bank/webmonkey/WmJsApi.java#L267
   *       - title: https://github.com/warren-bank/Android-WebMonkey/blob/v01.00.23/android-studio-project/WebMonkey/src/main/java/com/github/warren_bank/webmonkey/WmJsApi.java#L273
   *       - mime:  https://github.com/warren-bank/Android-WebMonkey/blob/v01.00.23/android-studio-project/WebMonkey/src/main/java/com/github/warren_bank/webmonkey/WmJsApi.java#L282
   *
   * Summary:
   * - HTML content does not include a <title> tag
   * - MIME is: "text/html; charset=utf-8"
   * - data URI will begin with: "data:text/html; charset=utf-8,<HTML content>"
   * - WebView will report a truncated portion of the data URI for its title
   */
  protected static boolean didWebViewLoadData(WebView view) {
    String title = view.getTitle();
    return ((title != null) && title.startsWith("data:text/html"));
  }

  /**
   * WebViewClientGm component for the ScriptBrowser intercepting .user.js
   * downloads.
   */
  public static class ScriptBrowserWebViewClientGm extends WebViewClientGm {

    private ScriptBrowser scriptBrowser;

    /**
     * Constructor.
     *
     * @param scriptStore
     *            the script database to query for scripts to run when a
     *            page starts/finishes loading
     * @param jsBridgeName
     *            the variable name to access the webview GM functions from
     *            javascript code
     * @param secret
     *            a random string that is added to calls of the GM API
     * @param scriptBrowser
     *            reference to its enclosing ScriptBrowser
     */
    public ScriptBrowserWebViewClientGm(ScriptStore scriptStore, String jsBridgeName, String secret, ScriptBrowser scriptBrowser) {
      super(scriptStore, jsBridgeName, secret);
      this.scriptBrowser = scriptBrowser;
    }

    public ScriptBrowser getScriptBrowser() {
      return scriptBrowser;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, final String url) {
      return (!ScriptBrowser.didWebViewLoadData(view))
        ? scriptBrowser.checkDownload(url)
        : false;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
      handlePageNavigation(view, url);
      super.onPageStarted(view, url, favicon);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
      handlePageNavigation(view, url);
      super.doUpdateVisitedHistory(view, url, isReload);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      if ((url != null) && !url.isEmpty() && url.equals(scriptBrowser.getPreviousUrl())) {
        scriptBrowser.clearPreviousUrl();
      }
      super.onPageFinished(view, url);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
      String text = scriptBrowser.activity.getString(R.string.error_while_loading)
        + " "  + failingUrl
        + ": " + errorCode
        + " "  + description;

      Toast.makeText(
        scriptBrowser.activity,
        text,
        Toast.LENGTH_LONG
      ).show();
    }

    private void handlePageNavigation(WebView view, String url) {
      handlePageNavigation(view, url, true, true);
    }

    private void handlePageNavigation(WebView view, String url, boolean ignoreCurrentUrl, boolean ignorePreviousUrl) {
      if (!ScriptBrowser.didWebViewLoadData(view)) {
        if ((url != null) && !url.isEmpty()) {
          if (ignoreCurrentUrl && url.equals(scriptBrowser.getCurrentUrl()))
            return;
          if (ignorePreviousUrl && url.equals(scriptBrowser.getPreviousUrl()))
            return;

          scriptBrowser.loadUrl(url);
          scriptBrowser.checkDownload(url);
        }
      }
    }

  }

  /**
   * DownloadListener for .user.js downloads.
   */
  public static class ScriptBrowserDownloadListener implements DownloadListener {

    private ScriptBrowser scriptBrowser;

    /**
     * Constructor.
     *
     * @param scriptBrowser
     *            reference to its enclosing ScriptBrowser
     */
    public ScriptBrowserDownloadListener(ScriptBrowser scriptBrowser) {
      this.scriptBrowser = scriptBrowser;
    }

    @Override
    public void onDownloadStart(final String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
      scriptBrowser.checkDownload(url);
    }

  }

  /**
   * WebChromeClient setting the app's title and progress.
   */
  public static class ScriptBrowserWebChromeClient extends WebChromeClient {

    private ScriptBrowser scriptBrowser;

    /**
     * Constructor.
     *
     * @param scriptBrowser
     *            reference to its enclosing ScriptBrowser
     */
    public ScriptBrowserWebChromeClient(ScriptBrowser scriptBrowser) {
      this.scriptBrowser = scriptBrowser;
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
      scriptBrowser.activity.setProgress(progress * 100);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
      if (!ScriptBrowser.didWebViewLoadData(view)) {
        scriptBrowser.activity.setTitle(title);
      }
    }

  }

}
