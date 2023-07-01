package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.settings.SettingsUtils;

import at.pardus.android.webview.gm.run.WebViewClientGm;
import at.pardus.android.webview.gm.util.DownloadHelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class WmJsApi {

  public static final String TAG = "WebViewGmApi";

  public String   secret;
  public Activity activity;
  public WebView  webview;
  public IBrowser browser;
  public boolean  useES6;

  public WmJsApi(String secret, Activity activity, WebView webview, IBrowser browser) {
    this.secret   = secret;
    this.activity = activity;
    this.webview  = webview;
    this.browser  = browser;
    this.useES6   = (Build.VERSION.SDK_INT >= 21);  // use ES5 in Android <= 4.4 because WebView is outdated and cannot be updated
  }

  public static final String GlobalJsApiNamespace = "WebViewWM";

  public Object getJsInterface() {
    return new Object() {

      private Toast toast = null;

      @JavascriptInterface
      public void toast(String secret, int duration, String message) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"toast\" did not supply correct secret");
          return;
        }
        try {
          if (toast != null)
            toast.cancel();

          toast = Toast.makeText(WmJsApi.this.activity, message, duration);
          toast.show();
        }
        catch(Exception e) {
          Log.e(WmJsApi.TAG, "Call to \"toast\" did not supply valid input and raised the following error", e);
        }
      }

      @JavascriptInterface
      public String getUrl(String secret) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"getUrl\" did not supply correct secret");
          return null;
        }
        return browser.getCurrentUrl();
      }

      @JavascriptInterface
      public String resolveUrl(String secret, String urlRelative, String urlBase) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"resolveUrl\" did not supply correct secret");
          return null;
        }
        if ((urlBase == null) || (urlBase.length() == 0)) {
          urlBase = browser.getCurrentUrl();
        }
        return DownloadHelper.resolveUrl(urlRelative, urlBase);
      }

      @JavascriptInterface
      public void startIntent(String secret, String action, String data, String type, String[] extras) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"startIntent\" did not supply correct secret");
          return;
        }
        try {
          Intent in = new Intent();

          if ((action != null) && (action.length() > 0))
            in.setAction(action);

          if ((data != null) && (data.length() > 0)) {
            if ((type != null) && (type.length() > 0))
              in.setDataAndType(Uri.parse(data), type);
            else
              in.setData(Uri.parse(data));
          }
          else if ((type != null) && (type.length() > 0)) {
            in.setType(type);
          }

          if ((extras != null) && (extras.length >= 2)) {
            int length = (extras.length % 2 == 0)
              ? extras.length
              : (extras.length - 1)
            ;

            HashMap<String, ArrayList<String>> extrasMap = new HashMap<String, ArrayList<String>>();
            String key;
            String val;
            ArrayList<String> arrayList;
            String[] vals;

            for (int i=0; i < length; i+=2) {
              key = extras[i];
              val = extras[i+1];

              if (!extrasMap.containsKey(key))
                extrasMap.put(key, new ArrayList<String>());

              arrayList = (ArrayList<String>) extrasMap.get(key);
              arrayList.add(val);
            }

            for (Iterator<String> iterator = extrasMap.keySet().iterator(); iterator.hasNext();) {
              key       = iterator.next();
              arrayList = (ArrayList<String>) extrasMap.get(key);

              if (arrayList.size() == 1) {
                val = (String) arrayList.get(0);

                in.putExtra(key, val);
              }
              else {
                vals = arrayList.toArray(new String[arrayList.size()]);

                in.putExtra(key, vals);
              }
            }
          }

          if (in.resolveActivity(WmJsApi.this.activity.getPackageManager()) != null) {
            WmJsApi.this.activity.startActivity(in);
          }
        }
        catch(Exception e) {
          Log.e(WmJsApi.TAG, "Call to \"startIntent\" did not supply valid input and raised the following error", e);
        }
      }

      @JavascriptInterface
      public void loadUrl(String secret, String url, String[] headers) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"loadUrl\" did not supply correct secret");
          return;
        }
        activity.runOnUiThread(new Runnable() {
          public void run() {
            try {
              if ((headers != null) && (headers.length >= 2)) {
                int length = (headers.length % 2 == 0)
                  ? headers.length
                  : (headers.length - 1)
                ;

                HashMap<String, String> httpHeaders = new HashMap<String, String>();

                for (int i=0; i < length; i+=2) {
                  httpHeaders.put(headers[i], headers[i+1]);
                }

                webview.loadUrl(url, httpHeaders);
              }
              else {
                webview.loadUrl(url);
              }
              browser.setCurrentUrl(url);
            }
            catch(Exception e) {
              Log.e(WmJsApi.TAG, "Call to \"loadUrl\" did not supply valid input and raised the following error", e);
            }
          }
        });
      }

      @JavascriptInterface
      public void loadFrame(String secret, String urlFrame, String urlParent, boolean proxyFrame) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"loadFrame\" did not supply correct secret");
          return;
        }
        if (
             (urlFrame  == null)
          || (urlParent == null)
          || (urlParent.length() <= 4)
          || (urlParent.substring(0, 4).toLowerCase().equals("http") == false)
        ) {
          Log.e(WmJsApi.TAG, "Call to \"loadFrame\" did not supply valid input");
          return;
        }
        if (proxyFrame)
          loadFrame_srcdoc(urlFrame, urlParent);
        else
          loadFrame_src(urlFrame, urlParent);
      }

      private void loadFrame_srcdoc(final String urlFrame, final String urlParent) {
        HashMap<String, String> httpHeaders = new HashMap<String, String>();
        httpHeaders.put("User-Agent", activity.getString(R.string.user_agent));
        httpHeaders.put("Referer", urlParent);
        String docFrame = DownloadHelper.downloadUrl(urlFrame, httpHeaders);
        if (docFrame == null) return;

        // add <base> tag to resolve relative URLs
        // note: (iframe.contentWindow.location.href === 'about:srcdoc')
        docFrame = docFrame.replaceFirst("(<\\s*head[^>]*>)", "$1<base href='" + urlFrame + "'/>");

        // serialize and escape
        final JSONObject jsonObject = new JSONObject();
        try {
          jsonObject.put("srcdoc", docFrame);
        }
        catch(Exception e) {
          return;
        }

        activity.runOnUiThread(new Runnable() {
          public void run() {
            try {
              String html = ""
              +        "<html>"
              + "\n" + "  <head>"
              + "\n" + "    <style>iframe {width:100%;}</style>"
              + "\n" + "  </head>"
              + "\n" + "  <body>"
              + "\n" + "    <iframe allowfullscreen='true' scrolling='no' frameborder='0'></iframe>"
              + "\n" + "    <script>"
              + "\n" + "     (function(){"
              + "\n" + "        var json = " + jsonObject.toString() + ";"
              + "\n" + "        var iframe = document.querySelector('iframe');"
              + "\n" + "        iframe.style.height = window.innerHeight + 'px';"
              + "\n" + "        iframe.srcdoc = json.srcdoc;"
              + "\n" + "        iframe.setAttribute('src', '" + urlFrame + "');"
              + "\n" + "     })()"
              + "\n" + "    </script>"
              + "\n" + "  </body>"
              + "\n" + "</html>";

              String mimeType   = "text/html; charset=utf-8";
              String encoding   = "UTF-8";
              String historyUrl = null;

              browser.setCurrentUrl(urlFrame);
              webview.loadDataWithBaseURL(/* baseUrl= */ urlParent, /* data= */ html, mimeType, encoding, historyUrl);
            }
            catch(Exception e) {
              Log.e(WmJsApi.TAG, "Call to \"loadFrame\" did not supply valid input and raised the following error", e);
            }
          }
        });
      }

      private void loadFrame_src(final String urlFrame, final String urlParent) {
        activity.runOnUiThread(new Runnable() {
          public void run() {
            try {
              String html = ""
              +        "<html>"
              + "\n" + "  <head>"
              + "\n" + "    <style>iframe {width:100%;}</style>"
              + "\n" + "  </head>"
              + "\n" + "  <body>"
              + "\n" + "    <iframe src='" + urlFrame + "' allowfullscreen='true' scrolling='no' frameborder='0'></iframe>"
              + "\n" + "    <script>document.querySelector('iframe').style.height = window.innerHeight + 'px'</script>"
              + "\n" + "  </body>"
              + "\n" + "</html>";

              String mimeType   = "text/html; charset=utf-8";
              String encoding   = "UTF-8";
              String historyUrl = null;

              browser.setCurrentUrl(urlFrame);
              webview.loadDataWithBaseURL(/* baseUrl= */ urlParent, /* data= */ html, mimeType, encoding, historyUrl);
            }
            catch(Exception e) {
              Log.e(WmJsApi.TAG, "Call to \"loadFrame\" did not supply valid input and raised the following error", e);
            }
          }
        });
      }

      @JavascriptInterface
      public void exit(String secret) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"exit\" did not supply correct secret");
          return;
        }
        try {
          if (WmJsApi.this.activity instanceof BrowserActivity)
            ((BrowserActivity) WmJsApi.this.activity).exit();
          else
            WmJsApi.this.activity.finish();
        }
        catch(Exception e) {
        }
      }

      @JavascriptInterface
      public String getUserscriptJS(String shared_secret_assertion, String url) {
        String shared_secret_value = SettingsUtils.getSharedSecretPreference(activity);
        if (
          (shared_secret_value     == null) || shared_secret_value.isEmpty()     ||
          (shared_secret_assertion == null) || shared_secret_assertion.isEmpty() ||
          !shared_secret_value.equals(shared_secret_assertion)
        ) {
          Log.e(WmJsApi.TAG, "Call to \"getUserscriptJS\" did not supply correct shared secret");
          return null;
        }

        String jsCode = null;
        try {
          WebViewClientGm webViewClient = (WebViewClientGm) webview.getWebViewClient();
          jsCode = (
            webViewClient.getMatchingScripts(url, false, null, null) + "\n" +
            "window.addEventListener('DOMContentLoaded', function(event) {" + "\n" +
            webViewClient.getMatchingScripts(url, true,  null, null) + "\n" +
            "})" + "\n"
          );
        }
        catch(Exception e) {
        }
        return jsCode;
      }

    };
  }

  public String getJsApi() {
    String jsBridgeName     = WmJsApi.GlobalJsApiNamespace;
    String defaultSignature = "\"" + WmJsApi.this.secret + "\"";
    StringBuilder sb;

    // jsApi
    sb = new StringBuilder(4 * 1024);

    sb.append("var GM_toastLong = function(message) { ");
    sb.append(jsBridgeName);
    sb.append(".toast(");
    sb.append(defaultSignature);
    sb.append(", ");
    sb.append(Toast.LENGTH_LONG);
    sb.append(", message); };");
    sb.append("\n");

    sb.append("var GM_toastShort = function(message) { ");
    sb.append(jsBridgeName);
    sb.append(".toast(");
    sb.append(defaultSignature);
    sb.append(", ");
    sb.append(Toast.LENGTH_SHORT);
    sb.append(", message); };");
    sb.append("\n");

    sb.append("var GM_getUrl = function() { return ");
    sb.append(jsBridgeName);
    sb.append(".getUrl(");
    sb.append(defaultSignature);
    sb.append("); };");
    sb.append("\n");

    sb.append("var GM_resolveUrl = function(urlRelative, urlBase) { return ");
    sb.append(jsBridgeName);
    sb.append(".resolveUrl(");
    sb.append(defaultSignature);
    sb.append(", urlRelative, urlBase); };");
    sb.append("\n");

    if (useES6) {
      sb.append("var GM_startIntent = function(action, data, type, ...extras) { ");
      sb.append(jsBridgeName);
      sb.append(".startIntent(");
      sb.append(defaultSignature);
      sb.append(", action, data, type, extras); };");
      sb.append("\n");
    }
    else {
      sb.append("var GM_startIntent = function(action, data, type) { ");
      sb.append(jsBridgeName);
      sb.append(".startIntent(");
      sb.append(defaultSignature);
      sb.append(", action, data, type, Array.prototype.slice.call(arguments, 3)); };");
      sb.append("\n");
    }

    if (useES6) {
      sb.append("var GM_loadUrl = function(url, ...headers) { ");
      sb.append(jsBridgeName);
      sb.append(".loadUrl(");
      sb.append(defaultSignature);
      sb.append(", url, headers); };");
      sb.append("\n");
    }
    else {
      sb.append("var GM_loadUrl = function(url) { ");
      sb.append(jsBridgeName);
      sb.append(".loadUrl(");
      sb.append(defaultSignature);
      sb.append(", url, Array.prototype.slice.call(arguments, 1)); };");
      sb.append("\n");
    }

    sb.append("var GM_loadFrame = function(urlFrame, urlParent, proxyFrame) { ");
    sb.append(jsBridgeName);
    sb.append(".loadFrame(");
    sb.append(defaultSignature);
    sb.append(", urlFrame, urlParent, !!proxyFrame); };");
    sb.append("\n");

    sb.append("var GM_exit = function() { ");
    sb.append(jsBridgeName);
    sb.append(".exit(");
    sb.append(defaultSignature);
    sb.append("); };");
    sb.append("\n");

    String jsApi = sb.toString();
    sb = null;

    return jsApi;
  }

}
