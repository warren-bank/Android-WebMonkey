// ==UserScript==
// @name         test: @require
// @description  test: React.js without sandbox
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// @require      https://unpkg.com/react@16.14.0/umd/react.production.min.js
// @require      https://unpkg.com/react-dom@16.14.0/umd/react-dom.production.min.js
// @flag         noJsSandbox
// ==/UserScript==

// https://legacy.reactjs.org/docs/components-and-props.html
// https://legacy.reactjs.org/docs/react-without-jsx.html

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
  class App extends React.Component {
    render() {
      return React.createElement('h1', null, this.props.title);
    }
  }

  ReactDOM.render(
    React.createElement(App, {title: 'Hello, React.js'}, null),
    document.getElementById('root')
  );
}

var run_test = function() {
  unsafeWindow.alert('typeof unsafeWindow.React = ' + (typeof unsafeWindow.React));
  unsafeWindow.alert('typeof window.React = ' + (typeof window.React));
  unsafeWindow.alert('typeof React = ' + (typeof React));

  unsafeWindow.alert('typeof unsafeWindow.ReactDOM = ' + (typeof unsafeWindow.ReactDOM));
  unsafeWindow.alert('typeof window.ReactDOM = ' + (typeof window.ReactDOM));
  unsafeWindow.alert('typeof ReactDOM = ' + (typeof ReactDOM));

  alert('typeof ReactDOM.render = ' + (typeof ReactDOM.render));
  alert('typeof React.createElement = ' + (typeof React.createElement));

  clean_dom();
  init_dom();
  init_react();
};

run_test();
