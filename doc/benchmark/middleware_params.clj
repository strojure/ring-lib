(ns benchmark.middleware-params
  (:require [ring.middleware.params :as ring]
            [strojure.ring-lib.middleware.params :as impl])
  (:import (java.io ByteArrayInputStream)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^:private impl-params (impl/params-request-fn {}))
(def ^:private ring-params ring/params-request)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Apply middleware - GET

(-> {:request-method :get :query-string "q=1"}
    (impl-params))
:=>
{:request-method :get,
 :query-string "q=1",
 :query-params {"q" "1"},
 :form-params {"q" "1"},
 :path-or-query-params {"q" "1"}}
;             Execution time mean : 217.785660 ns
;    Execution time std-deviation : 14.516993 ns
;   Execution time lower quantile : 203.612957 ns ( 2.5%)
;   Execution time upper quantile : 237.043257 ns (97.5%)

(-> {:request-method :get :query-string "q=1"}
    (ring-params))
:=>
{:request-method :get,
 :query-string "q=1",
 :form-params {},
 :params {"q" "1"},
 :query-params {"q" "1"}}
;             Execution time mean : 617.852639 ns
;    Execution time std-deviation : 26.923518 ns
;   Execution time lower quantile : 596.556914 ns ( 2.5%)
;   Execution time upper quantile : 660.254840 ns (97.5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Lookup keys - GET

(-> {:request-method :get :query-string "q=1"}
    (impl-params)
    :query-params)
;             Execution time mean : 357.655923 ns
;    Execution time std-deviation : 25.678788 ns
;   Execution time lower quantile : 332.332038 ns ( 2.5%)
;   Execution time upper quantile : 395.295261 ns (97.5%)

(-> {:request-method :get :query-string "q=1"}
    (ring-params)
    :query-params)
;             Execution time mean : 610.079953 ns
;    Execution time std-deviation : 28.706407 ns
;   Execution time lower quantile : 584.847541 ns ( 2.5%)
;   Execution time upper quantile : 655.431434 ns (97.5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Apply middleware - POST

(-> {:request-method :post :query-string "q=1"
     :headers {"content-type" "application/x-www-form-urlencoded"}
     :body (ByteArrayInputStream. (.getBytes "f=1"))}
    (impl-params))
:=>
{:request-method :post,
 :query-string "q=1",
 :headers {"content-type" "application/x-www-form-urlencoded"},
 :query-params {"q" "1"},
 :path-or-query-params {"q" "1"},
 :body-params {"f" "1"},
 :form-params {"f" "1"}}
;             Execution time mean : 506.940050 ns
;    Execution time std-deviation : 23.565473 ns
;   Execution time lower quantile : 489.896429 ns ( 2.5%)
;   Execution time upper quantile : 540.559665 ns (97.5%)

(-> {:request-method :post :query-string "q=1"
     :headers {"content-type" "application/x-www-form-urlencoded"}
     :body (ByteArrayInputStream. (.getBytes "f=1"))}
    (ring-params))
;=>
;{:request-method :post,
; :query-string "q=1",
; :headers {"content-type" "application/x-www-form-urlencoded"},
; :body #object[java.io.ByteArrayInputStream 0x7fbc2b8 "java.io.ByteArrayInputStream@7fbc2b8"],
; :form-params {"f" "1"},
; :params {"f" "1", "q" "1"},
; :query-params {"q" "1"}}
;             Execution time mean : 6.966158 µs
;    Execution time std-deviation : 2.597735 µs
;   Execution time lower quantile : 4.330295 µs ( 2.5%)
;   Execution time upper quantile : 9.921497 µs (97.5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Lookup keys - POST

(-> {:request-method :post :query-string "q=1"
     :headers {"content-type" "application/x-www-form-urlencoded"}
     :body (ByteArrayInputStream. (.getBytes "f=1"))}
    (impl-params)
    (select-keys [:query-params :body-params]))
;             Execution time mean : 1.199134 µs
;    Execution time std-deviation : 58.802694 ns
;   Execution time lower quantile : 1.156433 µs ( 2.5%)
;   Execution time upper quantile : 1.278958 µs (97.5%)

(-> {:request-method :post :query-string "q=1"
     :headers {"content-type" "application/x-www-form-urlencoded"}
     :body (ByteArrayInputStream. (.getBytes "f=1"))}
    (ring-params)
    (select-keys [:query-params :form-params]))
;             Execution time mean : 7.221133 µs
;    Execution time std-deviation : 2.142971 µs
;   Execution time lower quantile : 5.161390 µs ( 2.5%)
;   Execution time upper quantile : 10.451656 µs (97.5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
