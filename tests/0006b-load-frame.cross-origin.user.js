// ==UserScript==
// @name         test: GM_loadFrame
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

const urlParent = "http://example.com/parent_window.html"

if (window.location.href !== urlParent)
  GM_loadFrame(/* urlFrame= */ "http://gitcdn.link/cdn/warren-bank/Android-WebMonkey/master/tests/0006-load-frame.html", /* urlParent= */ urlParent);
