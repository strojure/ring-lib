(ns strojure.ring-lib.middleware.params
  "Ring middleware to add params keys in request."
  (:require [strojure.ring-lib.util.codec :as codec]
            [strojure.ring-lib.util.header :as header]
            [strojure.ring-lib.util.io :as io]
            [strojure.ring-lib.util.perf :as perf]
            [strojure.ring-lib.util.request :as request]
            [strojure.zmap.core :as zmap])
  (:import (clojure.lang Associative IDeref)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn params-request-fn
  "Adds delayed keys in request:

  - `:query-params` – a map of params from query string.

  - `:body-params` – a map of body params.
      + Only in request with `application/x-www-form-urlencoded` content type.

  - `:path-or-query-params` – a map of URL params (path params + query params).
      + This middleware merges query params in URL params.
      + Prefers path params over query params.

  - `:form-params` – a map of params of HTML forms in GET/POST request.
      - In GET request same as `:query-params`.
      - In POST request same as `:body-params`.

  The `opts` argument define form decoding behavior:

  - a configuration map with optional keys:
      - `:array-name-fn` – a function `(fn [param-name] array-param-name)`.
          + Returns param name for params which should be collected in vectors.
          + Default is function returning `true` for names with suffix `[]`.
      - `:param-key-fn` – a function `(fn [param-name] ...)`.
          + Converts string name to another type i.e. keyword.
          + Default is not defined.
  - or custom reducing function `rf` instead of configuration map.
  "
  {:arglists '([{:keys [array-name-fn, param-key-fn] :as opts}]
               [rf])
   :added "1.0"}
  [opts]
  (let [form-decode (codec/form-decode-fn opts)]
    (fn [^Associative request]
      (when request
        (let [query-params (when-let [query-string (.valAt request :query-string)]
                             (zmap/delay (form-decode query-string)))
              body-params (when-let [content-type (request/content-type request)]
                            (when (header/form-urlencoded? content-type)
                              (when-let [body (.valAt request :body)]
                                (zmap/delay
                                  (-> body (io/read-all-bytes (header/extract-charset content-type))
                                      (form-decode))))))]
          (if (or query-params body-params)
            (let [request-method (.valAt request :request-method)
                  form-params (cond (.equals :get request-method) query-params
                                    (.equals :post request-method) body-params)]
              (zmap/with-map [m request]
                (cond-> m
                  query-params
                  (-> (assoc :query-params query-params)
                      (zmap/update :path-or-query-params #(perf/merge* (.deref ^IDeref query-params) %)))
                  body-params
                  (-> (dissoc :body)
                      (assoc :body-params body-params))
                  form-params
                  (-> (assoc :form-params form-params)))))
            request))))))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
