// ==UserScript==
// @name         test: GM.setValue, GM.getValue, GM.listValues
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-start
// ==/UserScript==

GM.deleteValue('foo').then(console.log)
GM.setValue('foo', 'hello, foo').then(console.log)
GM.getValue('foo').then(console.log)
GM.listValues().then(console.log)

GM.deleteValue('bar').then(console.log)
GM.setValue('bar', {hello: 'bar'}).then(console.log)
GM.getValue('bar').then(console.log)
GM.listValues().then(console.log)

GM.deleteValue('baz').then(console.log)
GM.getValue('baz', {hello: 'baz'}).then(console.log)
GM.listValues().then(console.log)
