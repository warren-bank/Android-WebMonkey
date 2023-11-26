// ==UserScript==
// @name         test: GM.cookie.set, GM.cookie.delete, GM.cookie.list
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// @grant        GM.cookie
// ==/UserScript==

var init = async function() {
  var callback = console.log.bind(console)

  await GM.cookie.delete({'name': 'foo'})
  await GM.cookie.set({'name': 'foo', 'value': 'hello, foo'})
  await GM.cookie.list().then(callback)

  await GM.cookie.delete({'name': 'bar'})
  await GM.cookie.set({'name': 'bar', 'value': 'hello, bar'})
  await GM.cookie.list().then(callback)

  await GM.cookie.list({'name': 'foo'}).then(callback)
  await GM.cookie.list({'name': 'bar'}).then(callback)

  await GM.cookie.delete({'name': 'foo'})
  await GM.cookie.list().then(callback)

  await GM.cookie.delete({'name': 'bar'})
  await GM.cookie.list().then(callback)

  await GM.cookie.delete({'name': 'baz'})
  await GM.cookie.set({'name': 'baz', 'value': 'hello, baz; hello = baz', 'encode': true})
  await GM.cookie.list({'decode': true}).then(callback)
}

init()
