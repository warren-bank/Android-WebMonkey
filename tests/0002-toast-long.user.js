// ==UserScript==
// @name         test: GM_toastLong
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

GM_toastLong("Hello from " + unsafeWindow.location.href);
