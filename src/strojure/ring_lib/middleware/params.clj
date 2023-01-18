(ns strojure.ring-lib.middleware.params
  "Ring middleware to add `:query-params` and `form-params` in request."
  (:require [strojure.ring-lib.util.codec :as codec]
            [strojure.ring-lib.util.io :as io]
            [strojure.ring-lib.util.perf :as perf]
            [strojure.ring-lib.util.request :as request]
            [strojure.zmap.core :as zmap])
  (:import (clojure.lang Associative IDeref)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn- query-params-request-fn
  "Adds delayed keys in request if it has `:query-string`:

  - `:query-params` – a map of params from query string.

  - `:uri-params` – a map of URI params (path params + query params) with merged
                    query params in.
  "
  {:added "1.0"}
  [form-decode-fn]
  (fn [request]
    (when request
      (if-let [query-string (request :query-string)]
        (let [query-params-delay (zmap/delay (form-decode-fn query-string))]
          (-> request
              (assoc :query-params query-params-delay)
              (assoc :uri-params (zmap/delay
                                   (perf/merge* (request :uri-params)
                                                (.deref ^IDeref query-params-delay))))
              (zmap/wrap)))
        request))))

(defn- form-params-request-fn
  "Adds delayed keys in request:

  - `:form-params` – a map of form params.
      - Query params for GET request.
      - Body params for POST request with `application/x-www-form-urlencoded`
        content.
  "
  {:added "1.0"}
  [form-decode-fn]
  (fn [request]
    (when request
      (cond
        ;; GET request - use value of `:query-params`.
        (and (request/method-get? request)
             (.containsKey ^Associative request :query-params))
        (-> request
            (assoc :form-params (zmap/delay (request :query-params)))
            (zmap/wrap))
        ;; POST request with `application/x-www-form-urlencoded` content — read
        ;; params from request `:body`.
        (request/form-urlencoded? request)
        (-> request
            (dissoc :body)
            (assoc :form-params (zmap/delay
                                  (some-> (request :body)
                                          (io/read-all-bytes (request/content-type-charset request))
                                          (form-decode-fn))))
            (zmap/wrap))
        ;; Don't add `:form-params` key.
        :else
        request))))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn params-request-fn
  "Adds delayed keys in request:

  - `:query-params` – a map of params from query string.

  - `:uri-params`   – a map of URI params (path params + query params) with merged
                      query params in.

  - `:form-params`  – a map of form params.
      - Query params for GET request.
      - Body params for POST request with `application/x-www-form-urlencoded`
        content.
  "
  [opts]
  (let [form-decode (codec/form-decode-fn opts)
        query-params (query-params-request-fn form-decode)
        form-params (form-params-request-fn form-decode)]
    (fn [request]
      (-> request query-params form-params))))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
