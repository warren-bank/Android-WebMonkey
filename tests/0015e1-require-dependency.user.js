// ==UserScript==
// @name         test: @require
// @description  test: Less.js with sandbox
// @namespace    WebViewWM
// @match        *://*duckduckgo.com/*
// @match        *://*
// @run-at       document-end
// @require      https://www.unpkg.com/less@4.1.3/dist/less.min.js
// @resource     duckCommonStyle  https://ibaidu.tujidu.com/newcss/duckCommonStyle.less?t=26.05
// @resource     duckOnePageStyle https://ibaidu.tujidu.com/newcss/duckOnePageStyle.less?t=26.05
// @resource     duckTwoPageStyle https://ibaidu.tujidu.com/newcss/duckTwoPageStyle.less?t=26.05
// ==/UserScript==

// https://lesscss.org/usage/#programmatic-usage

var addStyle = function(resourceName) {
  less.render(
    GM_getResourceText(resourceName),
    function(error, output) {
      if (error)
        unsafeWindow.alert('failed to compile Less resource: ' + resourceName);
      else
        GM_addStyle(output.css);
    }
  );
}

var init_less = function() {
  addStyle('duckCommonStyle');
  addStyle('duckOnePageStyle');
  addStyle('duckTwoPageStyle');
}

var run_test = function() {
  unsafeWindow.alert('typeof unsafeWindow.less = ' + (typeof unsafeWindow.less));
  unsafeWindow.alert('typeof window.less = ' + (typeof window.less));
  unsafeWindow.alert('typeof less = ' + (typeof less));

  init_less();
};

var init_page = function() {
  if (location.hostname.toLowerCase().endsWith('duckduckgo.com'))
    run_test();
  else
    location.href = 'https://duckduckgo.com/?q=hello+world';
}

init_page();
