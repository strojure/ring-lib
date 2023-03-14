# ring-lib

Opinionated implementations for Clojure ring handler.

[![Clojars Project](https://img.shields.io/clojars/v/com.github.strojure/ring-lib.svg)](https://clojars.org/com.github.strojure/ring-lib)

[![cljdoc badge](https://cljdoc.org/badge/com.github.strojure/ring-lib)](https://cljdoc.org/d/com.github.strojure/ring-lib)
[![tests](https://github.com/strojure/ring-lib/actions/workflows/tests.yml/badge.svg)](https://github.com/strojure/ring-lib/actions/workflows/tests.yml)

## Motivation

Being not satisfied with some decisions/implementations in existing Ring
libraries.

## API

### Middlewares

The middlewares in this library does not provide `wrap-middleware` function.
There are middleware builder functions in `strojure.ring-lib.middleware.core`
namespace:

```clojure
(require '[strojure.ring-lib.middleware.core :as mw]
         '[strojure.ring-lib.middleware.params :as params])

(-> handler
    (mw/wrap-request (params/params-request-fn {})))
```

#### csp

The implementation of Content Security Policy ([CSP]).

- Supports static CSP headers and headers with nonce.
- Renders policy from Clojure map.
- Follows recommendations for nonce generation.
- Configurable usage of report-only CSP header.

[CSP]: https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP

#### cookies

The alternative implementation of the
[cookies](https://github.com/ring-clojure/ring/blob/master/ring-core/src/ring/middleware/cookies.clj)
middleware.

- Lazy evaluation of the `:cookies` key in request.
- Avoids destructuring of the options map in every request.

#### params

The alternative implementation of the
[params](https://github.com/ring-clojure/ring/blob/master/ring-core/src/ring/middleware/params.clj)
middleware.

- Name dependent detection if param is single value or collection, by default
  only names with `[]` suffix has collection value.
- Lazy evaluation of request keys.
- Allows to customize param names with collection values.
- Allows to define coercion function for param names, i.e. to keywordize them.
- Adds `:form-params` which is `:query-params` for GET requests and
  `:body-params` for POST request with “application/x-www-form-urlencoded”
  content type.
- Adds `:path-or-query-params` which contains params from URL but not body.
- Does not add `:params` as merge of everything else because it is considered confusing.
- Optimized for performance.

## Benchmarks

- [middleware.params](doc/benchmark/middleware_params.clj)
- [util.codec](doc/benchmark/util_codec.clj)
- [util.headers](doc/benchmark/util_headers.clj)
