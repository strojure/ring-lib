(ns strojure.ring-lib.middleware.params
  "Ring middleware to add params keys in request."
  (:require [strojure.ring-lib.util.codec :as codec]
            [strojure.ring-lib.util.header :as header]
            [strojure.ring-lib.util.io :as io]
            [strojure.ring-lib.util.perf :as perf]
            [strojure.ring-lib.util.request :as request]
            [strojure.zmap.core :as zmap])
  (:import (clojure.lang IDeref)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn params-request-fn
  "Adds delayed keys in request:

  - `:query-params` – a map of params from query string.

  - `:body-params` – a map of body params.
      + Only in POST request with `application/x-www-form-urlencoded` content
        type.

  - `:url-params` – a map of URL params (path params + query params).
      + This middleware merges query params in URL params.

  - `:form-params` – a map of params of HTML forms in GET/POST request.
      - In GET request same as `:query-params`.
      - In POST request same as `:body-params`.
  "
  {:added "1.0"}
  [opts]
  (let [form-decode (codec/form-decode-fn opts)]
    (fn [request]
      (let [query-params-delay
            (when-let [query-string (get request :query-string)]
              (zmap/delay (form-decode query-string)))
            body-params-delay
            (when (request/method-post? request)
              (when-let [content-type (request/content-type request)]
                (when (header/form-urlencoded? content-type)
                  (when-let [body (get request :body)]
                    (zmap/delay
                      (-> body (io/read-all-bytes (header/extract-charset content-type))
                          (form-decode)))))))]
        (if (or query-params-delay body-params-delay)
          (zmap/with-map [m request]
            (cond-> m
              query-params-delay
              (-> (assoc :query-params query-params-delay)
                  (cond-> (request/method-get? m)
                          (assoc :form-params query-params-delay))
                  (zmap/update :url-params #(perf/merge* % (.deref ^IDeref query-params-delay))))
              body-params-delay
              (-> (dissoc :body)
                  (assoc :body-params body-params-delay)
                  (assoc :form-params body-params-delay))))
          request)))))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
