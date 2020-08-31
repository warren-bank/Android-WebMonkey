// ==UserScript==
// @name         test: GM_toastShort
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

GM_toastShort("Hello from " + unsafeWindow.location.href);
