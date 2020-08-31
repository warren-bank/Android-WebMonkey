package com.github.warren_bank.webmonkey;

import at.pardus.android.webview.gm.model.Script;
import at.pardus.android.webview.gm.store.ScriptStoreSQLite;

import android.content.Context;

public class WmScriptStore extends ScriptStoreSQLite {

  private String scripts;

  public WmScriptStore(Context context) {
    super(context);
    scripts = "";
  }

  public void addScript(String content) {
    scripts += content + "\n";
  }

  private Script[] injectScripts(Script[] pre) {
    Script[] post = new Script[pre.length];

    for (int i=0; i < pre.length; i++) {
      Script old = pre[i];

      post[i] = new Script(
        /* name        = */ old.getName(),
        /* namespace   = */ old.getNamespace(),
        /* exclude     = */ old.getExclude(),
        /* include     = */ old.getInclude(),
        /* match       = */ old.getMatch(),
        /* description = */ old.getDescription(),
        /* downloadurl = */ old.getDownloadurl(),
        /* updateurl   = */ old.getUpdateurl(),
        /* installurl  = */ old.getInstallurl(),
        /* icon        = */ old.getIcon(),
        /* runAt       = */ old.getRunAt(),
        /* unwrap      = */ old.isUnwrap(),
        /* version     = */ old.getVersion(),
        /* requires    = */ old.getRequires(),
        /* resources   = */ old.getResources(),
        /* content     = */ (scripts + old.getContent())
      );
    }

    return post;
  }

  @Override
  public Script[] get(String url) {
    Script[] matchingScripts = super.get(url);
    if ((matchingScripts != null) && (matchingScripts.length > 0) && (scripts.length() > 0)) {
      matchingScripts = injectScripts(matchingScripts);
    }
    return matchingScripts;
  }

}
