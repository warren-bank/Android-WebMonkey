package com.github.warren_bank.webmonkey;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class WmJsApi {

  public static final String TAG = "WebViewGmApi";

  public String   secret;
  public Activity activity;
  public boolean  useES6;

  public WmJsApi(String secret, Activity activity) {
    this.secret   = secret;
    this.activity = activity;
    this.useES6   = (Build.VERSION.SDK_INT >= 21);  // use ES5 in Android <= 4.4 because WebView is outdated and cannot be updated
  }

  public static final String GlobalJsApiNamespace = "WebViewWM";

  public Object getGlobalJsApi() {
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

            for (int i=0; i < length; i+=2) {
              in.putExtra(extras[i], extras[i+1]);
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

    };
  }

  public String getWrappedJsApi() {
    String jsBridgeName = WmJsApi.GlobalJsApiNamespace;
    String defaultSignature = "\"" + WmJsApi.this.secret + "\"";
    String jsApi = "";

    jsApi += "var GM_toastLong"   + " = function(message) { "                       + jsBridgeName + ".toast("       + defaultSignature + ", " + Toast.LENGTH_LONG  + ", message);"                          + " };\n";
    jsApi += "var GM_toastShort"  + " = function(message) { "                       + jsBridgeName + ".toast("       + defaultSignature + ", " + Toast.LENGTH_SHORT + ", message);"                          + " };\n";
    jsApi += (useES6)
          ? ("var GM_startIntent" + " = function(action, data, type, ...extras) { " + jsBridgeName + ".startIntent(" + defaultSignature + ", action, data, type, extras);"                                   + " };\n")
          : ("var GM_startIntent" + " = function(action, data, type) { "            + jsBridgeName + ".startIntent(" + defaultSignature + ", action, data, type, Array.prototype.slice.call(arguments, 3));" + " };\n")
    ;
    jsApi += "var GM_exit"        + " = function() { "                              + jsBridgeName + ".exit("        + defaultSignature + ");"                                                               + " };\n";

    return jsApi;
  }

}
