// ==UserScript==
// @name         test: @require
// @description  test: prove that value of 'this' is the global Window object when sandbox is disabled
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// @require      http://192.168.0.2/0015a-require-dependency/this.js
// @flag         noJsSandbox
// ==/UserScript==

var run_test = function() {
  unsafeWindow.alert('typeof unsafeWindow.FooBar = ' + (typeof unsafeWindow.FooBar));
  unsafeWindow.alert('typeof window.FooBar = ' + (typeof window.FooBar));
};

run_test();
