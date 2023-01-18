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
#_{:request-method :get,
   :query-string "q=1",
   :query-params {"q" "1"},
   :uri-params {"q" "1"},
   :form-params {"q" "1"}}
;             Execution time mean : 250,869665 ns
;    Execution time std-deviation : 30,807845 ns
;   Execution time lower quantile : 219,180040 ns ( 2,5%)
;   Execution time upper quantile : 286,761290 ns (97,5%)

(-> {:request-method :get :query-string "q=1"}
    (ring-params))
#_{:request-method :get,
   :query-string "q=1",
   :form-params {},
   :params {"q" "1"},
   :query-params {"q" "1"}}
;             Execution time mean : 599,129221 ns
;    Execution time std-deviation : 12,944601 ns
;   Execution time lower quantile : 584,852419 ns ( 2,5%)
;   Execution time upper quantile : 611,355730 ns (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Lookup keys - GET

(-> {:request-method :get :query-string "q=1"}
    (impl-params)
    :query-params)
;             Execution time mean : 391,232837 ns
;    Execution time std-deviation : 37,099766 ns
;   Execution time lower quantile : 355,255987 ns ( 2,5%)
;   Execution time upper quantile : 430,901654 ns (97,5%)

(-> {:request-method :get :query-string "q=1"}
    (ring-params)
    :query-params)
;             Execution time mean : 588,617528 ns
;    Execution time std-deviation : 20,829582 ns
;   Execution time lower quantile : 558,563206 ns ( 2,5%)
;   Execution time upper quantile : 611,718646 ns (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Apply middleware - POST

(-> {:request-method :post :query-string "q=1"
     :headers {"content-type" "application/x-www-form-urlencoded"}
     :body (ByteArrayInputStream. (.getBytes "f=1"))}
    (impl-params))
#_{:request-method :post,
   :query-string "q=1",
   :headers {"content-type" "application/x-www-form-urlencoded"},
   :query-params {"q" "1"},
   :uri-params {"q" "1"},
   :form-params {"f" "1"}}
;             Execution time mean : 380,589550 ns
;    Execution time std-deviation : 34,356178 ns
;   Execution time lower quantile : 343,336534 ns ( 2,5%)
;   Execution time upper quantile : 416,263286 ns (97,5%)

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
;             Execution time mean : 6,816102 µs
;    Execution time std-deviation : 2,860772 µs
;   Execution time lower quantile : 4,385659 µs ( 2,5%)
;   Execution time upper quantile : 10,522573 µs (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Lookup keys - POST

(-> {:request-method :post :query-string "q=1"
     :headers {"content-type" "application/x-www-form-urlencoded"}
     :body (ByteArrayInputStream. (.getBytes "f=1"))}
    (impl-params)
    (select-keys [:query-params :form-params]))
;             Execution time mean : 1,205899 µs
;    Execution time std-deviation : 87,360964 ns
;   Execution time lower quantile : 1,127964 µs ( 2,5%)
;   Execution time upper quantile : 1,315553 µs (97,5%)

(-> {:request-method :post :query-string "q=1"
     :headers {"content-type" "application/x-www-form-urlencoded"}
     :body (ByteArrayInputStream. (.getBytes "f=1"))}
    (ring-params)
    (select-keys [:query-params :form-params]))
;             Execution time mean : 7,042565 µs
;    Execution time std-deviation : 2,526389 µs
;   Execution time lower quantile : 4,835979 µs ( 2,5%)
;   Execution time upper quantile : 10,008641 µs (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
