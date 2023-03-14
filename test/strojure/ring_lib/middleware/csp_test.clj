(ns strojure.ring-lib.middleware.csp-test
  (:require [clojure.test :as test :refer [deftest]]
            [strojure.ring-lib.middleware.csp :as csp]))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn- t
  [opts]
  (let [opts (assoc opts :random-nonce-fn (constantly "TEST-NONCE"))
        handler (-> (fn [request]
                      {:body (select-keys request [:scp-nonce])})
                    (csp/wrap-csp opts))]
    (handler {})))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(deftest wrap-csp-t

  (test/is (= {:headers {"Content-Security-Policy" "default-src 'none'"}
               :body {}}
              (t {:policy {:default-src :none}})))

  (test/is (= {:headers {"Content-Security-Policy-Report-Only" "default-src 'none'"}
               :body {}}
              (t {:policy {:default-src :none}
                  :report-only true})))

  (test/is (= {:headers {"Content-Security-Policy" "script-src 'nonce-TEST-NONCE'"}
               :body {}}
              (t {:policy {:script-src :nonce}})))

  (test/is (= {:headers {"Content-Security-Policy-Report-Only" "script-src 'nonce-TEST-NONCE'"}
               :body {}}
              (t {:policy {:script-src :nonce}
                  :report-only true})))

  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
