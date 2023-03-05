# ring-lib

Opinionated implementations for Clojure ring handler.

[![cljdoc badge](https://cljdoc.org/badge/com.github.strojure/ring-lib)](https://cljdoc.org/d/com.github.strojure/ring-lib)
[![Clojars Project](https://img.shields.io/clojars/v/com.github.strojure/ring-lib.svg)](https://clojars.org/com.github.strojure/ring-lib)

## Motivation

Being not satisfied with come decisions/implementations in existing Ring
libraries.

## API

### Middlewares

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
