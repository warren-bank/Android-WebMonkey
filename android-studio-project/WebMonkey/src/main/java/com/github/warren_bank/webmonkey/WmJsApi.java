package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.settings.SettingsUtils;
import com.github.warren_bank.webmonkey.settings.WebViewSettingsMgr;
import com.github.warren_bank.webmonkey.util.SaveFileHelper;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.run.WebViewClientGm;
import at.pardus.android.webview.gm.run.WebViewGm;
import at.pardus.android.webview.gm.store.ScriptStore;
import at.pardus.android.webview.gm.util.DownloadHelper;
import at.pardus.android.webview.gm.util.ScriptJsTemplateHelper;
import at.pardus.android.webview.gm.util.ScriptPermissionHelper;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class WmJsApi {

  public static final String TAG = "WebViewGmApi";

  public String    secret;
  public Activity  activity;
  public WebViewGm webview;
  public IBrowser  browser;

  public WmJsApi(String secret, Activity activity, WebViewGm webview, IBrowser browser) {
    this.secret   = secret;
    this.activity = activity;
    this.webview  = webview;
    this.browser  = browser;
  }

  public static final String GlobalJsApiNamespace = "WebViewWM";

  public static String initializeTemplate(String template) {
    HashMap<String, String> replacements = new HashMap();
    replacements.put("{{Toast.LENGTH_SHORT}}", Integer.toString(Toast.LENGTH_SHORT));
    replacements.put("{{Toast.LENGTH_LONG}}",  Integer.toString(Toast.LENGTH_LONG));

    return ScriptJsTemplateHelper.replace(template, replacements);
  }

  public Object getJsInterface() {
    return new Object() {

      private Toast toast = null;

      @JavascriptInterface
      public void toast(String scriptName, String scriptNamespace, String secret, int duration, String message) {
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
      public String getUrl(String scriptName, String scriptNamespace, String secret) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"getUrl\" did not supply correct secret");
          return null;
        }
        return browser.getCurrentUrl();
      }

      @JavascriptInterface
      public String resolveUrl(String scriptName, String scriptNamespace, String secret, String urlRelative, String urlBase) {
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
      public void startIntent(String scriptName, String scriptNamespace, String secret, String action, String data, String type, String[] extras) {
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

          WmJsApi.this.activity.startActivity(in);
        }
        catch(ActivityNotFoundException e) {
          Log.e(WmJsApi.TAG, "Call to \"startIntent\" did not resolve to any matching Activity", e);
        }
        catch(Exception e) {
          Log.e(WmJsApi.TAG, "Call to \"startIntent\" did not supply valid input and raised the following error", e);
        }
      }

      @JavascriptInterface
      public void download(String scriptName, String scriptNamespace, String secret, String cacheUUID, String mimeType, String fileName) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"download\" did not supply correct secret");
          return;
        }

        SaveFileHelper.Download download = new SaveFileHelper.Download(cacheUUID, mimeType);

        SaveFileHelper.showFilePicker(WmJsApi.this.activity, download, fileName);
      }

      @JavascriptInterface
      public void loadUrl(String scriptName, String scriptNamespace, String secret, String url, String[] headers) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"loadUrl\" did not supply correct secret");
          return;
        }
        activity.runOnUiThread(new Runnable() {
          public void run() {
            try {
              webview.stopLoading();
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
      public void loadFrame(String scriptName, String scriptNamespace, String secret, String urlFrame, String urlParent, boolean proxyFrame) {
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
        httpHeaders.put("User-Agent", SettingsUtils.getUserAgent(activity, true));
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
              webview.stopLoading();
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
              webview.stopLoading();
              webview.loadDataWithBaseURL(/* baseUrl= */ urlParent, /* data= */ html, mimeType, encoding, historyUrl);
            }
            catch(Exception e) {
              Log.e(WmJsApi.TAG, "Call to \"loadFrame\" did not supply valid input and raised the following error", e);
            }
          }
        });
      }

      @JavascriptInterface
      public void exit(String scriptName, String scriptNamespace, String secret) {
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
      public String getUserAgent(String scriptName, String scriptNamespace, String secret) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"getUserAgent\" did not supply correct secret");
          return null;
        }
        return SettingsUtils.getUserAgent(/* Context */ WmJsApi.this.activity, true);
      }

      @JavascriptInterface
      public void setUserAgent(String scriptName, String scriptNamespace, String secret, String value) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"setUserAgent\" did not supply correct secret");
          return;
        }
        if (!grant(scriptName, scriptNamespace, "GM_setUserAgent")) {
          return;
        }
        SettingsUtils.setUserAgent(/* Context */ WmJsApi.this.activity, value, /* updateWebViewSettings */ false);
        activity.runOnUiThread(new Runnable() {
          public void run() {
            WebViewSettingsMgr.updateUserAgent();
          }
        });
      }

      @JavascriptInterface
      public void removeAllCookies(String scriptName, String scriptNamespace, String secret) {
        if (!WmJsApi.this.secret.equals(secret)) {
          Log.e(WmJsApi.TAG, "Call to \"removeAllCookies\" did not supply correct secret");
          return;
        }
        if (!grant(scriptName, scriptNamespace, "GM_removeAllCookies")) {
          return;
        }
        WebViewSettingsMgr.removeAllCookies();
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
          StringBuilder sb = new StringBuilder();

          jsCode = webViewClient.getMatchingScripts(url, null, null, Script.RUNATSTART);
          if (!TextUtils.isEmpty(jsCode)) {
            sb.append(jsCode);
          }

          jsCode = webViewClient.getMatchingScripts(url, null, null, Script.RUNATEND);
          if (!TextUtils.isEmpty(jsCode)) {
            sb.append(WebViewClientGm.EMULATE_ON_PAGE_FINISHED_CLOSURE_BEGIN);
            sb.append(jsCode);
            sb.append(WebViewClientGm.EMULATE_ON_PAGE_FINISHED_CLOSURE_END_RUNATEND);
          }

          jsCode = webViewClient.getMatchingScripts(url, null, null, Script.RUNATIDLE);
          if (!TextUtils.isEmpty(jsCode)) {
            sb.append(WebViewClientGm.EMULATE_ON_PAGE_FINISHED_CLOSURE_BEGIN);
            sb.append(jsCode);
            sb.append(WebViewClientGm.EMULATE_ON_PAGE_FINISHED_CLOSURE_END_RUNATIDLE);
          }

          if (sb.length() == 0)
            throw new Exception();

          jsCode = sb.toString();
        }
        catch(Exception e) {
          jsCode = null;
        }
        return jsCode;
      }

    };
  }

  private boolean grant(String scriptName, String scriptNamespace, String api) {
    return ScriptPermissionHelper.isGranted(getScriptStore(), scriptName, scriptNamespace, api);
  }

  private ScriptStore getScriptStore() {
    return webview.getScriptStore();
  }

  public String getJsApi(String template, Script script) {
    String jsBridgeName       = WmJsApi.GlobalJsApiNamespace;
    String secret             = WmJsApi.this.secret;
    boolean addCallbackPrefix = false;

    return ScriptJsTemplateHelper.interpolate(
      template, script, jsBridgeName, secret, addCallbackPrefix
    );
  }

}
