package com.github.warren_bank.webmonkey;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.store.ScriptStoreSQLite;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

public class WmScriptStore extends ScriptStoreSQLite {

  private ArrayList<Script> scripts;

  public WmScriptStore(Context context) {
    super(context);
    scripts = new ArrayList<Script>();
  }

  public boolean addScript(String content) {
    return scripts.add(new Script(
      /* name=        */ WmJsApi.GlobalJsApiNamespace,
      /* namespace=   */ WmJsApi.GlobalJsApiNamespace,
      /* exclude=     */ null,
      /* include=     */ null,
      /* match=       */ null,
      /* description= */ null,
      /* downloadurl= */ null,
      /* updateurl=   */ null,
      /* installurl=  */ null,
      /* icon=        */ null,
      /* runAt=       */ Script.RUNATSTART,
      /* unwrap=      */ false,
      /* version=     */ null,
      /* requires=    */ null,
      /* resources=   */ null,
      /* content=     */ content
    ));
  }

  @Override
  public Script[] get(String url) {
    Script[] matchingScripts = super.get(url);
    if ((matchingScripts != null) && (scripts.size() > 0)) {
      ArrayList<Script> all = new ArrayList<Script>(scripts);
      all.addAll(Arrays.asList(matchingScripts));
      matchingScripts = all.toArray(new Script[all.size()]);
    }
    return matchingScripts;
  }

}
