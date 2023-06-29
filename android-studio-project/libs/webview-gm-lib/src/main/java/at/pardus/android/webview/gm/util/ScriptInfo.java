package at.pardus.android.webview.gm.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.model.ScriptResource;

public class ScriptInfo {

  private static final JSONArray getJsonStringArray(String[] values) {
    try {
      if ((values == null) || (values.length == 0))
        throw new JSONException("");

      return new JSONArray(values);
    }
    catch (JSONException e) {
      return new JSONArray();
    }
  }

  private static final JSONArray getJsonScriptResources(ScriptResource[] resources) {
    JSONArray jsonResources = new JSONArray();

    if ((resources != null) && (resources.length > 0)) {
      for (ScriptResource resource : resources) {
        try {
          JSONObject jsonRes = new JSONObject();

          jsonRes.put(
            "name",
            resource.getName()
          );

          jsonRes.put(
            "url",
            resource.getUrl()
          );

          jsonResources.put(jsonRes);
        }
        catch (JSONException e) {}
      }
    }

    return jsonResources;
  }

  private static String APP_PACKAGE_NAME = "";
  private static String APP_VERSION_NAME = "";

  public static void initStaticResources(Context context) {
    if (TextUtils.isEmpty(APP_PACKAGE_NAME)) {
      try {
        APP_PACKAGE_NAME = context.getPackageName();

        PackageInfo info = context.getPackageManager().getPackageInfo(APP_PACKAGE_NAME, 0);
        APP_VERSION_NAME = info.versionName;
      }
      catch(Exception e) {}
    }
  }

  public static String toJSONString(Script script) {
    try {
      JSONObject jsonInfo     = new JSONObject();
      JSONObject jsonPlatform = new JSONObject();
      JSONObject jsonScript   = new JSONObject();

      jsonInfo.put(
        "uuid",
        String.valueOf(script.hashCode())
      );
      jsonInfo.put(
        "scriptMetaStr",
        script.getMetaStr()
      );
      jsonInfo.put(
        "scriptWillUpdate",
        (script.isEnabled() && !TextUtils.isEmpty(script.getUpdateurl()))
      );
      jsonInfo.put(
        "scriptHandler",
        APP_PACKAGE_NAME
      );
      jsonInfo.put(
        "version",
        APP_VERSION_NAME
      );
      jsonInfo.put(
        "platform",
        jsonPlatform
      );
      jsonInfo.put(
        "script",
        jsonScript
      );

      jsonPlatform.put(
        "arch",
        Build.CPU_ABI
      );
      jsonPlatform.put(
        "browserName",
        "WebView"
      );
      jsonPlatform.put(
        "browserVersion",
        ""
      );
      jsonPlatform.put(
        "os",
        "android"
      );

      jsonScript.put(
        "description",
        script.getDescription()
      );
      jsonScript.put(
        "excludes",
        getJsonStringArray(script.getExclude())
      );
      jsonScript.put(
        "includes",
        getJsonStringArray(script.getInclude())
      );
      jsonScript.put(
        "matches",
        getJsonStringArray(script.getMatch())
      );
      jsonScript.put(
        "name",
        script.getName()
      );
      jsonScript.put(
        "namespace",
        script.getNamespace()
      );
      jsonScript.put(
        "resources",
        getJsonScriptResources(script.getResources())
      );
      jsonScript.put(
        "runAt",
        script.getRunAt()
      );
      jsonScript.put(
        "version",
        script.getVersion()
      );

      return jsonInfo.toString();
    }
    catch (JSONException e) {
      return "{}";
    }
  }

}
