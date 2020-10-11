package com.github.warren_bank.webmonkey;

import com.github.warren_bank.webmonkey.settings.SettingsUtils;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.store.ScriptStore;
import at.pardus.android.webview.gm.util.DownloadHelper;

import android.content.Context;

public final class WmScriptUpdateMgr {

  // --------------------

  private final static class WmScriptUpdateRunner implements Runnable {
    private ScriptStore scriptStore;

    WmScriptUpdateRunner(ScriptStore scriptStore) {
      this.scriptStore = scriptStore;
    }

    private int compareVersions(String a, String b) {
      if ((a == null) || (b == null)) {
        return ((a == null) && (b == null))
          ? 0
          : (a == null)
            ? -1
            : 1
        ;
      }

      String[] aParts = a.split("\\.");
      String[] bParts = b.split("\\.");
      int length = Math.max(aParts.length, bParts.length);
      for(int i = 0; i < length; i++) {
        int aPart = (i < aParts.length)
          ? Integer.parseInt(aParts[i])
          : 0
        ;
        int bPart = (i < bParts.length)
          ? Integer.parseInt(bParts[i])
          : 0
        ;
        if(aPart < bPart)
          return -1;
        if(aPart > bPart)
          return 1;
      }
      return 0;
    }

    public void run() {
      Script[] scripts = scriptStore.getAll();
      for (int i=0; i < scripts.length; i++) {
        Script old_script = scripts[i];
        String url = null;
        if (url == null) url = old_script.getUpdateurl();
        if (url == null) url = old_script.getDownloadurl();
        if (url == null) url = old_script.getInstallurl();
        if (url == null) continue;
        String scriptStr = DownloadHelper.downloadScript(url); 
        if (scriptStr == null) continue;
        Script new_script = Script.parse(scriptStr, url); 
        if (new_script == null) continue;
        if (compareVersions(old_script.getVersion(), new_script.getVersion()) == -1)
          scriptStore.add(new_script);
      }
    }
  }

  // --------------------

  private static long MS_PER_DAY = 86400000;  // (1000 ms/sec)(60 sec/min)(60 min/hr)(24 hr/day)

  public static void init(Context context, ScriptStore scriptStore) {
    int intervalDays = SettingsUtils.getUpdateIntervalDays(context);
    if (intervalDays <= 0) return;

    long intervalMs = (long) intervalDays * MS_PER_DAY;
    long lastUpdate = SettingsUtils.getLastUpdateTimestamp(context);
    long thisUpdate = System.currentTimeMillis();
    if (thisUpdate <= (lastUpdate + intervalMs)) return;

    SettingsUtils.setLastUpdateTimestamp(context, thisUpdate);

    WmScriptUpdateRunner runner = new WmScriptUpdateRunner(scriptStore);
    new Thread(runner).start();
  }

}
