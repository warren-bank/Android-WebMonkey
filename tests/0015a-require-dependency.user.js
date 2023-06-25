// ==UserScript==
// @name         test: @require
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

try {
  var string_0_old = 'abc';
  var string_0_new = 'def';
  var string_1 = `${string_0_old} ${string_0_old} ${string_0_old}`;
  var string_2 = string_1.replaceAll(string_0_old, string_0_new);

  GM_toastLong(`replaceAll: ${string_1} => ${string_2}`);
}
catch(error) {
  GM_toastLong(`replaceAll error: ${error.message}`);
}
