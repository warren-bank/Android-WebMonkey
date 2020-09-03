// ==UserScript==
// @name         test: GM_startIntent
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

// ==========================================
// https://httpbin.org/#/operations/Request%20inspection/get_headers
// ==========================================

if (unsafeWindow.location.hostname !== "httpbin.org")
  GM_loadUrl(/* url= */ "https://httpbin.org/headers", /* headers: */ "Refer", "https://WebMonkey.com/", "User-Agent", "WebMonkey", "X-Requested-With", "WebMonkey");
