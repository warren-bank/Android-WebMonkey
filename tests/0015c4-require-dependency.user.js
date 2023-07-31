// ==UserScript==
// @name         test: @require
// @description  test: React.js with sandbox, and explicit injection of React object to sandbox namespace. JSX with Babel standalone compiler.
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// @require      https://unpkg.com/react@0.14.10/dist/react-with-addons.min.js
// @require      https://unpkg.com/babel-standalone@6.26.0/babel.min.js
// ==/UserScript==

// https://legacy.reactjs.org/docs/components-and-props.html
// https://babeljs.io/docs/babel-standalone#api

var React = window.React;

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
  var jsx = `
    class App extends React.Component {
      render() {
        return <h1>{this.props.title}</h1>;
      }
    }

    React.render(
      <App title="Hello, React.js" />,
      document.getElementById('root')
    );
`;

  eval(
    Babel.transform(jsx, { presets: ["react"] }).code
  );
}

var run_test = function() {
  unsafeWindow.alert('typeof unsafeWindow.React = ' + (typeof unsafeWindow.React));
  unsafeWindow.alert('typeof window.React = ' + (typeof window.React));
  unsafeWindow.alert('typeof React = ' + (typeof React));

  alert('typeof React.render = ' + (typeof React.render));
  alert('typeof React.createElement = ' + (typeof React.createElement));

  unsafeWindow.alert('typeof unsafeWindow.Babel = ' + (typeof unsafeWindow.Babel));
  unsafeWindow.alert('typeof window.Babel = ' + (typeof window.Babel));
  unsafeWindow.alert('typeof Babel = ' + (typeof Babel));

  clean_dom();
  init_dom();
  init_react();
};

run_test();
