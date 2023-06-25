// ==UserScript==
// @name         test: GM_fetch
// @namespace    WebViewWM
// @match        *://*
// @run-at       document-end
// ==/UserScript==

var clean_dom = function() {
  while(document.body.childNodes.length) {
    document.body.removeChild(document.body.childNodes[0]);
  }
}

var append_results_to_dom = function(text) {
  var div = document.createElement('div');
  var pre = document.createElement('pre');
  var hr  = document.createElement('hr');

  pre.innerText = text;
  div.appendChild(pre);
  document.body.appendChild(div);
  document.body.appendChild(hr);
}

var do_fetch = async function(options) {
  let { url, responseType, ...fetchOptions } = options;
  responseType || (responseType = "json");
  fetchOptions = {
    mode: "cors",
    ...fetchOptions
  };
  let timeout = 12e4;
  options.timeout || (timeout = options.timeout);
  let response = await GM_fetch(url, fetchOptions);
  if (response.ok && response.status >= 200 && response.status < 400) {
    if (responseType === "json")
      return await response.json();
    if (responseType === "text")
      return await response.text();
    if (responseType === "raw") {
      let data = await response.text(), responseHeaders = Object.fromEntries([
        ...response.headers.entries()
      ]), finalUrl = response.url;
      return finalUrl || (response.headers.get("X-Final-URL") ? finalUrl = response.headers.get("X-Final-URL") : finalUrl = url), {
        body: data,
        headers: responseHeaders,
        status: response.status,
        statusText: response.statusText,
        url: finalUrl
      };
    }
  }
  else {
    let details;
    try {
      details = await response.text();
    }
    catch (error) {
      console.error("parse response failed", error);
    }
    details && console.error("fail response", details);
    let shortDetail = "";
    throw details && (shortDetail = details.slice(0, 150)), new Error(
      "fetchError",
      response.status + ": " + (response.statusText || "") + shortDetail,
      details
    );
  }
};

var run_fetch_test = async function(common_options, responseType) {
  var options, response, error;

  try {
    options = Object.assign({}, common_options, {responseType});
    response = await do_fetch(options);
  }
  catch(e) {
    error = e;
  }
  finally {
    var text = JSON.stringify({options, response, error}, null, 2);
    append_results_to_dom(text);
  }
};

var run_fetch_tests = async function() {
  var common_options;

  common_options = {
    'url':          'https://httpbin.org/headers',
    'responseType': '',
    'headers':      {
      'x-foo': 'bar',
      'x-bar': 'baz'
    }
  };

  await run_fetch_test(common_options, 'json');
  await run_fetch_test(common_options, 'text');
  await run_fetch_test(common_options, 'raw');
};

var run_low_level_response_api_tests = async function() {
  var response, value, text;

  try {
    response = new window.Response(
      'hello world',
      {
        status: 200,
        statusText: 'OK',
        headers: {'content-type': 'text/plain'},
        url: 'https://httpbin.org/headers'
      }
    );

    value = await response.text();
    text  = 'response.text(): ' + value;
    append_results_to_dom(text);
  }
  catch(error) {
    text = '[ERROR] response.text():' + "\n" + error.message;
    append_results_to_dom(text);
  }

  try {
    response = new window.Response(
      '{"status": "ok", "message": "hello world"}',
      {
        status: 200,
        statusText: 'OK',
        headers: {'content-type': 'application/json'},
        url: 'https://httpbin.org/headers'
      }
    );

    value = await response.json();
    text  = 'response.json(): ' + JSON.stringify(value, null, 2);
    append_results_to_dom(text);
  }
  catch(error) {
    text = '[ERROR] response.json():' + "\n" + error.message;
    append_results_to_dom(text);
  }
};

var run_all_tests = async function() {
  clean_dom();
  await run_low_level_response_api_tests();
  await run_fetch_tests();
};

run_all_tests();
