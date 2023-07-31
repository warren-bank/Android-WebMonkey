// ==UserScript==
// @name         test: @require
// @description  test: React.js without sandbox, and explicit injection of React object to global namespace. JSX with HTM (Hyperscript Tagged Markup) standalone compiler, and explicit injection of HTM object to global namespace.
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// @require      https://unpkg.com/react@0.14.10/dist/react-with-addons.min.js
// @require      https://unpkg.com/htm@3.1.1/dist/htm.js
// @flag         noJsSandbox
// ==/UserScript==

// https://legacy.reactjs.org/docs/components-and-props.html
// https://dev.to/devalnor/running-jsx-in-your-browser-without-babel-1agc

var React = window.React;
var htm   = window.htm;

var clean_dom = function() {
  while(document.body.childNodes.length) {
    document.body.removeChild(document.body.childNodes[0]);
  }
}

var init_dom = function() {
  var div = document.createElement('div');

  div.setAttribute('id', 'root');
  document.body.appendChild(div);
}

var init_react = function() {
  var html = htm.bind(React.createElement);

  var App = function(props) {
    return html`<h1>${props.title}</h1>`;
  }

  React.render(
    html`<${App} title="Hello, React.js" />`,
    document.getElementById('root')
  );
}

var run_test = function() {
  unsafeWindow.alert('typeof unsafeWindow.React = ' + (typeof unsafeWindow.React));
  unsafeWindow.alert('typeof window.React = ' + (typeof window.React));
  unsafeWindow.alert('typeof React = ' + (typeof React));

  alert('typeof React.render = ' + (typeof React.render));
  alert('typeof React.createElement = ' + (typeof React.createElement));

  unsafeWindow.alert('typeof unsafeWindow.htm = ' + (typeof unsafeWindow.htm));
  unsafeWindow.alert('typeof window.htm = ' + (typeof window.htm));
  unsafeWindow.alert('typeof htm = ' + (typeof htm));

  clean_dom();
  init_dom();
  init_react();
};

run_test();
