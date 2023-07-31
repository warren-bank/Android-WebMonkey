// ==UserScript==
// @name         test: @require
// @description  test: Preact without sandbox. JSX with HTM (Hyperscript Tagged Markup) standalone compiler.
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// @require      https://unpkg.com/htm@3.1.1/preact/standalone.umd.js
// @flag         noJsSandbox
// ==/UserScript==

// https://github.com/preactjs/preact#getting-started
// https://github.com/developit/htm#example

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

var init_preact = function() {
  const {html, Component, render} = htmPreact;

  class App extends Component {
    render(props) {
      return html`<h1>${props.title}</h1>`;
    }
  }

  render(
    html`<${App} title="Hello, Preact" />`,
    document.getElementById('root')
  );
}

var run_test = function() {
  unsafeWindow.alert('typeof unsafeWindow.htmPreact = ' + (typeof unsafeWindow.htmPreact));
  unsafeWindow.alert('typeof window.htmPreact = ' + (typeof window.htmPreact));
  unsafeWindow.alert('typeof htmPreact = ' + (typeof htmPreact));

  clean_dom();
  init_dom();
  init_preact();
};

run_test();
