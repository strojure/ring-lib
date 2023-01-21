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

  - `:url-params` – a map of URI params (path params + query params) with merged
                    query params in.
  "
  {:added "1.0"}
  [form-decode-fn]
  (fn [request]
    (if-let [query-string (get request :query-string)]
      (let [query-params-delay (zmap/delay (form-decode-fn query-string))]
        (-> request
            (assoc :query-params query-params-delay)
            (zmap/update :url-params #(perf/merge* % (.deref ^IDeref query-params-delay)))))
      request)))

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
    (cond
      ;; GET request - use value of `:query-params`.
      (request/method-get? request)
      (if (.containsKey ^Associative request :query-params)
        (zmap/with-map [m request]
          (assoc m :form-params (get m :query-params)))
        request)
      ;; POST request with `application/x-www-form-urlencoded` content — read
      ;; params from request `:body`.
      (request/form-urlencoded? request)
      (if-let [body (get request :body)]
        (zmap/with-map [m request]
          (-> m (dissoc :body)
              (assoc :form-params (zmap/delay
                                    (-> body (io/read-all-bytes (request/content-type-charset request))
                                        (form-decode-fn))))))
        request)
      ;; Don't add `:form-params` key.
      :else
      request)))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn params-request-fn
  "Adds delayed keys in request:

  - `:query-params` – a map of params from query string.

  - `:url-params`   – a map of URL params (path params + query params) with
                      merged query params in.

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
