(ns strojure.ring-lib.middleware.csp
  "Ring middleware to add [CSP] header in response.

  [CSP]: https://developer.mozilla.org/en-US/docs/Web/HTTP/CSP
  "
  (:require [strojure.web-security.csp :as csp]))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

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
  [handler {:keys [policy, report-only, random-nonce-fn]}]
  (let [header-name (csp/header-name report-only)
        header-value-fn (csp/header-value-fn policy)
        nonce-fn (and (csp/requires-nonce? header-value-fn)
                      (or random-nonce-fn (csp/random-nonce-fn)))]
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
           (catch Throwable t (raise t))))))))

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
