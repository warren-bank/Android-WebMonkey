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
		makeToastOnUiThread(activity.getString(R.string.starting_download_of)
				+ " " + url, Toast.LENGTH_SHORT);
		String scriptStr = DownloadHelper.downloadScript(url);
		if (scriptStr == null) {
			makeToastOnUiThread(
					activity.getString(R.string.error_downloading_from) + " "
							+ url, Toast.LENGTH_LONG);
			return;
		}
		Script script = Script.parse(scriptStr, url);
		if (script == null) {
			Log.d(TAG, "Error parsing script:\n" + scriptStr);
			makeToastOnUiThread(activity.getString(R.string.error_parsing_at)
					+ " " + url, Toast.LENGTH_LONG);
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
		browser = activity.getLayoutInflater().inflate(
				R.layout.script_browser, null);
		webView = (WebViewGm) browser.findViewById(R.id.webView);
		webView.setScriptStore(scriptStore);
		addressField = (EditText) browser.findViewById(R.id.addressField);
		addressField
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_GO
								|| actionId == EditorInfo.IME_NULL) {
							currentUrl = v.getText().toString();
							webView.loadUrl(currentUrl);
							webView.requestFocus();
							((InputMethodManager) activity
									.getSystemService(Context.INPUT_METHOD_SERVICE))
									.hideSoftInputFromWindow(
											v.getWindowToken(), 0);
							return true;
						}
						return false;
					}
				});
		webView.setWebViewClient(new ScriptBrowserWebViewClientGm(scriptStore,
				webView.getWebViewClient().getJsBridgeName(), webView
						.getWebViewClient().getSecret(), this));
		webView.setDownloadListener(new ScriptBrowserDownloadListener(this));
		webView.setWebChromeClient(new ScriptBrowserWebChromeClient(this));
		loadUrl(startUrl);
	}

	public void changeAddressField(String url) {
		currentUrl = url;
		addressField.setText(url);
	}

	/**
	 * Load the given URL.
	 *
	 * @param url
	 *            the address to load
	 */
	public void loadUrl(String url) {
		changeAddressField(url);
		webView.loadUrl(url);
	}

	/**
	 * @return the browser's last loaded address
	 */
	public String getUrl() {
		currentUrl = webView.getUrl();
		return currentUrl;
	}

	/**
	 * @return the browser's last loaded address
	 *
	 * Same as getUrl(), but can be called from a non-UI thread.
	 */
	public String getCurrentUrl() {
		return currentUrl;
	}

	/**
	 * Constructor.
	 *
	 * @param activity
	 *            the application's activity
	 * @param scriptStore
	 *            the database to use
	 */
	public ScriptBrowser(ScriptManagerActivity activity,
			ScriptStore scriptStore, String startUrl) {
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
		public ScriptBrowserWebViewClientGm(ScriptStore scriptStore,
				String jsBridgeName, String secret, ScriptBrowser scriptBrowser) {
			super(scriptStore, jsBridgeName, secret);
			this.scriptBrowser = scriptBrowser;
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, final String url) {
			return scriptBrowser.checkDownload(url);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			scriptBrowser.changeAddressField(url);
			scriptBrowser.checkDownload(url);
			super.onPageStarted(view, url, favicon);
		}

		@SuppressWarnings("deprecation")
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			Toast.makeText(
					scriptBrowser.activity,
					scriptBrowser.activity
							.getString(R.string.error_while_loading)
							+ " "
							+ failingUrl + ": " + errorCode + " " + description,
					Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * DownloadListener for .user.js downloads.
	 */
	public static class ScriptBrowserDownloadListener implements
			DownloadListener {

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
		public void onDownloadStart(final String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
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
			scriptBrowser.activity.setTitle(title);
		}

	}

}
