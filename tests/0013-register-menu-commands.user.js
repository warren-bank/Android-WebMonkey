// ==UserScript==
// @name         test: GM_registerMenuCommand
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// ==/UserScript==

var commandFunc = function(text) {
  window.alert('menu command: ' + text);
}

var addMenuItem = function(caption) {
  GM_registerMenuCommand(caption, commandFunc.bind(null, caption));
}

addMenuItem('Home');
addMenuItem('About');
addMenuItem('Locations');
addMenuItem('Contact');
