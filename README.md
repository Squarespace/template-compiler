
# Squarespace Template Compiler.

A Java template compiler based on [JSON-Template][jsont].

[![Build Status](https://travis-ci.org/Squarespace/template-compiler.svg?branch=master)](https://travis-ci.org/Squarespace/template-compiler) 
[![Coverage Status](https://coveralls.io/repos/Squarespace/template-compiler/badge.svg?branch=master&service=github)](https://coveralls.io/github/Squarespace/template-compiler?branch=master)

License: [Apache 2.0](LICENSE) ([summary][license-tldr])

Copyright (c) 2014 SQUARESPACE, Inc.

## Overview

Squarespace's template language is based on JSON-Template, a minimal
declarative template language for Python and JavaScript, heavily influenced by
[google-ctemplate][goog-ct].


## Objectives

The project was started with these objectives:

 * Move away from using [Node.js][nodejs] for backend template compilation.
 * Must meet or exceed performance of the legacy system. The compiler can
   potentially execute several times for each page view, so it needs to be
   really fast.
 * Improve performance over the legacy system.
 * Minimize memory usage. For example, parsing can often create and discard a
   high number of temporary strings.
 * Support additional features for server-side compilation.
 * High test coverage.
 * Support syntax error recovery.
 * Support a template validation mode.


## Measured Results

 * Average 20x performance increase over previous Node.js + JSON-Template
   system.


[license-tldr]:
https://tldrlegal.com/license/apache-license-2.0-(apache-2.0) "Apache 2.0 tl;dr"

[jsont]:
http://jsont.squarespace.com/

[nodejs]:
http://nodejs.org

[goog-ct]:
https://code.google.com/p/ctemplate/

