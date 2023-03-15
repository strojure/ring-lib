(ns strojure.ring-lib.middleware.csp-test
  (:require [clojure.test :as test :refer [deftest testing]]
            [strojure.ring-lib.middleware.csp :as csp]))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn- wrap-report-uri*
  [opts]
  (let [handler (-> (fn [request]
                      {:body (select-keys request [:scp-nonce])})
                    (csp/wrap-report-uri opts))]
    (handler {:uri (::request-uri opts (:report-uri opts "/csp-report"))})))

(deftest wrap-report-uri-t

  (test/is (= "/csp-report"
              (let [a! (atom :undefined)
                    callback (fn [{:keys [uri]}] (reset! a! uri))]
                (wrap-report-uri* {:report-callback callback})
                @a!)))

  (test/is (= "/custom-csp-report"
              (let [a! (atom :undefined)
                    callback (fn [{:keys [uri]}] (reset! a! uri))]
                (wrap-report-uri* {:report-uri "/custom-csp-report"
                                   :report-callback callback})
                @a!)))

  (test/is (= :undefined
              (let [a! (atom :undefined)
                    callback (fn [{:keys [uri]}] (reset! a! uri))]
                (wrap-report-uri* {:report-callback callback
                                   ::request-uri "/not-csp-report"})
                @a!)))

  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn- wrap-csp*
  [opts]
  (let [opts (assoc opts :random-nonce-fn (constantly "TEST-NONCE"))
        handler (-> (fn [request]
                      {:body (select-keys request [:scp-nonce])})
                    (csp/wrap-csp opts))]
    (handler {})))

(deftest wrap-csp-t

  (test/is (= {:headers {"Content-Security-Policy" "default-src 'none'"}
               :body {}}
              (wrap-csp* {:policy {:default-src :none}})))

  (test/is (= {:headers {"Content-Security-Policy-Report-Only" "default-src 'none'"}
               :body {}}
              (wrap-csp* {:policy {:default-src :none}
                          :report-only true})))

  (test/is (= {:headers {"Content-Security-Policy" "script-src 'nonce-TEST-NONCE'"}
               :body {}}
              (wrap-csp* {:policy {:script-src :nonce}})))

  (test/is (= {:headers {"Content-Security-Policy-Report-Only" "script-src 'nonce-TEST-NONCE'"}
               :body {}}
              (wrap-csp* {:policy {:script-src :nonce}
                          :report-only true})))

  (testing ":report-callback"

    (test/is (= {:headers {"Content-Security-Policy" "report-uri /csp-report"}
                 :body {}}
                (wrap-csp* {:policy {}
                            :report-callback identity})))

    (test/is (= {:headers {"Content-Security-Policy" "report-uri /test-report-uri"}
                 :body {}}
                (wrap-csp* {:policy {"report-uri" "/test-report-uri"}
                            :report-callback identity})))

    (test/is (= (class (csp/wrap-report-uri identity {:report-callback identity}))
                (class (csp/wrap-csp identity {:policy {}
                                               :report-callback identity}))))

    (test/is (= (class (csp/wrap-report-uri identity {:report-callback identity}))
                (class (csp/wrap-csp identity {:policy {"report-uri" "/test-report-uri"}
                                               :report-callback identity}))))

    (test/is (not= (class (csp/wrap-report-uri identity {:report-callback identity}))
                   (class (csp/wrap-csp identity {:policy {}}))))

    )

  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
