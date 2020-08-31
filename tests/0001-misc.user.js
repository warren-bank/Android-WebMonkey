// ==UserScript==
// @name         test: misc
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

// ================================
// logcat tag: "WebViewGmApi"
// ================================

GM_log("typeof GM_log = "           + (typeof GM_log));            // assertion: "function"
GM_log("typeof GM_toastLong = "     + (typeof GM_toastLong));      // assertion: "function"

GM_log("typeof WebViewWM = "        + (typeof WebViewWM));         // assertion: "Object"
GM_log("typeof WebViewWM.toast = "  + (typeof WebViewWM.toast));   // assertion: "function"

WebViewWM.toast("secret", "Hello", 0);                             // assertion: throws Exception because "secret" is incorrect
