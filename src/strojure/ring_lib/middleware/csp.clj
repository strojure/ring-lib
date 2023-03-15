(ns strojure.ring-lib.middleware.csp
  "Ring middleware to add [CSP] header in response.

  [CSP]: https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP
  "
  (:require [strojure.web-security.csp :as csp]))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^:const report-uri-default
  "Default value of the CSP report URI."
  "/csp-report")

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn wrap-report-uri
  "Handles CSP report URI and invokes `:report-callback` function with ring
  request as argument. Respond with `{:status 200}`. Used by [[wrap-csp]] when
  `:report-callback` option is defined.

  Configuration params:

  - `:report-callback` – a function `(fn callback [request] ...)`.
      + Required.
      + Invoked when request `:uri` is report URI
      + Callback should read CSP report JSON from the request `:body` stream
        itself.
      + The return value is ignored.

  - `:report-uri` – a string with request `:uri` to match for.
      + Exact value is matched.
      + Default value is \"/csp-report\".
  "
  {:added "1.2"}
  [handler {:keys [report-callback, report-uri]}]
  (assert (fn? report-callback))
  (let [report-uri (or report-uri report-uri-default)]
    (assert (string? report-uri) (str "Expect string in `:report-uri`: " report-uri))
    (fn
      ([request]
       (if (.equals ^String report-uri (get request :uri))
         (do (report-callback request)
             {:status 200})
         (handler request)))
      ([request respond raise]
       (if (.equals ^String report-uri (get request :uri))
         (try
           (report-callback request)
           (respond {:status 200})
           (catch Throwable t (raise t)))
         (handler request))))))

(defn wrap-csp
  "Adds [CSP] header in ring response. If header uses nonce then `:csp-nonce`
  key is being added in ring request to be used in response body.

  [CSP]: https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP

  Configuration params:

  - `:policy` – a map of directive names (string, keyword) and directive values
                (string, keyword, collection of strings and keywords)
      + The `:nonce` keyword in directive values represents nonce placeholder

  - `:report-only` – optional boolean flag if report-only CSP header name should
                     be used.

  - `:random-nonce-fn` – optional 0-arity function to generate nonce for every
                         request.

  - `:report-callback` – a function `(fn callback [request] ...)` to handle
    `report-uri` directive.
      + When presented then handler is wrapped with [[wrap-report-uri]].
      + If policy map does not have `report-uri` directive then it is added with
        default value \"/csp-report\".

  Static header example:

      (def -handler (-> (fn [_] {}) (wrap-csp {:policy {:default-src :none}})))

      (-handler {})
      :=> {:headers {\"Content-Security-Policy\" \"default-src 'none'\"}}

  Example header with nonce:

      (def -handler (-> (fn [_] {}) (wrap-csp {:policy {:script-src :nonce}})))

      (-handler {})
      :=> {:headers {\"Content-Security-Policy\" \"script-src 'nonce-k6JADK2qxoFO4bfKnZI0vyZv'\"}}
  "
  {:added "1.2"}
  [handler {:keys [policy, report-only, random-nonce-fn, report-callback]}]
  (let [header-name (csp/header-name report-only)
        report-uri (and report-callback (csp/find-directive :report-uri policy))
        policy (cond-> policy (and report-callback (not report-uri))
                              (assoc :report-uri report-uri-default))
        header-value-fn (csp/header-value-fn policy)
        nonce-fn (and (csp/requires-nonce? header-value-fn)
                      (or random-nonce-fn (csp/random-nonce-fn)))]
    (cond->
      (if nonce-fn
        (fn nonce-csp
          ([request]
           (let [nonce (nonce-fn)]
             (-> (handler (assoc request :csp-nonce nonce))
                 (update :headers assoc header-name (header-value-fn nonce)))))
          ([request respond raise]
           (try
             (let [nonce (nonce-fn)
                   header-value (header-value-fn nonce)]
               (handler (assoc request :csp-nonce nonce)
                        (fn [response]
                          (respond (update response :headers assoc header-name header-value)))
                        raise))
             (catch Throwable t (raise t)))))
        (fn static-csp
          ([request]
           (-> (handler request)
               (update :headers assoc header-name (header-value-fn))))
          ([request respond raise]
           (try
             (let [header-value (header-value-fn)]
               (handler request
                        (fn [response]
                          (respond (update response :headers assoc header-name header-value)))
                        raise))
             (catch Throwable t (raise t))))))
      report-callback
      (wrap-report-uri {:report-callback report-callback
                        :report-uri report-uri}))))

(comment

  (def -handler (-> (fn [_] {}) (wrap-csp {:policy {:default-src :none}})))

  (-handler {})
  :=> {:headers {"Content-Security-Policy" "default-src 'none'"}}
  ;             Execution time mean : 108.680653 ns
  ;    Execution time std-deviation : 8.797700 ns
  ;   Execution time lower quantile : 98.504567 ns ( 2.5%)
  ;   Execution time upper quantile : 122.755120 ns (97.5%)

  (def -handler (-> (fn [_] {}) (wrap-csp {:policy {:script-src :nonce}})))

  (-handler {})
  :=> {:headers {"Content-Security-Policy" "script-src 'nonce-k6JADK2qxoFO4bfKnZI0vyZv'"}}
  ;             Execution time mean : 1.228093 µs
  ;    Execution time std-deviation : 34.702482 ns
  ;   Execution time lower quantile : 1.191501 µs ( 2.5%)
  ;   Execution time upper quantile : 1.265153 µs (97.5%)

  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
