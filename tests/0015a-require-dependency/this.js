(function (global) {
  alert('(this == window) = ' + (global == window));

  global.FooBar = {};
})(this);
