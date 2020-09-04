// ==UserScript==
// @name         test: GM_exit
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

var counter = 5;

unsafeWindow.setTimeout(
  GM_exit,
  (counter * 1000)
);

var timer = unsafeWindow.setInterval(
  function() {
    if (counter > 0) {
      GM_toastShort("WebMonkey will exit in " + counter + " seconds..");
      counter--;
    }
    else {
      unsafeWindow.clearInterval(timer);
    }
  },
  1000
);
