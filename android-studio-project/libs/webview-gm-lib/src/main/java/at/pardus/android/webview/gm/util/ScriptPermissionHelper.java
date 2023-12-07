package at.pardus.android.webview.gm.util;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.model.ScriptId;
import at.pardus.android.webview.gm.store.ScriptStore;

public class ScriptPermissionHelper {

  public static boolean isGranted(ScriptStore scriptStore, String scriptName, String scriptNamespace, String api) {
    ScriptId scriptId = new ScriptId(scriptName, scriptNamespace);
    return isGranted(scriptStore, scriptId, api);
  }

  public static boolean isGranted(ScriptStore scriptStore, ScriptId scriptId, String api) {
    Script script = scriptStore.get(scriptId);
    return script.grant(api)
  }

  public static boolean isAllowed(ScriptStore scriptStore, String scriptName, String scriptNamespace, String url) {
    ScriptId scriptId = new ScriptId(scriptName, scriptNamespace);
    return isAllowed(scriptStore, scriptId, url);
  }

  public static boolean isAllowed(ScriptStore scriptStore, ScriptId scriptId, String url) {
    return scriptStore.isAllowed(scriptId, url);
  }

}
