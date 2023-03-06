(ns benchmark.util-headers
  (:require [ring.util.request :as ring]
            [strojure.ring-lib.util.header :as header]
            [strojure.ring-lib.util.request :as request]))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Extract charset

;; ### Content-Type with charset

(-> (request/content-type {:headers {"content-type" "application/x-www-form-urlencoded; charset=windows-1251"}})
    (header/extract-charset))
;=> #object[sun.nio.cs.MS1251 0x76afac16 "windows-1251"]
;             Execution time mean : 113.972264 ns
;    Execution time std-deviation : 2.940270 ns
;   Execution time lower quantile : 109.645685 ns ( 2.5%)
;   Execution time upper quantile : 116.789714 ns (97.5%)

(ring/character-encoding {:headers {"content-type" "application/x-www-form-urlencoded; charset=windows-1251"}})
:=> "windows-1251"
;             Execution time mean : 1.036313 µs
;    Execution time std-deviation : 82.922718 ns
;   Execution time lower quantile : 972.619401 ns ( 2.5%)
;   Execution time upper quantile : 1.143364 µs (97.5%)

;; ### Content-Type without charset

(-> (request/content-type {:headers {"content-type" "application/x-www-form-urlencoded"}})
    (header/extract-charset))
:=> nil
;             Execution time mean : 70.989346 ns
;    Execution time std-deviation : 2.760128 ns
;   Execution time lower quantile : 66.915850 ns ( 2.5%)
;   Execution time upper quantile : 73.785903 ns (97.5%)

(ring/character-encoding {:headers {"content-type" "application/x-www-form-urlencoded"}})
:=> nil
;             Execution time mean : 249.956953 ns
;    Execution time std-deviation : 24.991674 ns
;   Execution time lower quantile : 224.162263 ns ( 2.5%)
;   Execution time upper quantile : 284.965162 ns (97.5%)

;; ### No Content-Type

(-> (request/content-type {:headers {}})
    (header/extract-charset))
:=> nil
;             Execution time mean : 20.405617 ns
;    Execution time std-deviation : 6.946678 ns
;   Execution time lower quantile : 13.193583 ns ( 2.5%)
;   Execution time upper quantile : 30.260312 ns (97.5%)

(ring/character-encoding {:headers {}})
:=> nil
;             Execution time mean : 94.325664 ns
;    Execution time std-deviation : 19.336048 ns
;   Execution time lower quantile : 77.747319 ns ( 2.5%)
;   Execution time upper quantile : 117.942576 ns (97.5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Test for "application/x-www-form-urlencoded" content type

;; ### Positive

(-> (request/content-type {:headers {"content-type" "application/x-www-form-urlencoded"}})
    (header/form-urlencoded?))
:=> true
;             Execution time mean : 35.125634 ns
;    Execution time std-deviation : 3.367748 ns
;   Execution time lower quantile : 32.279307 ns ( 2.5%)
;   Execution time upper quantile : 39.471387 ns (97.5%)

(ring/urlencoded-form? {:headers {"content-type" "application/x-www-form-urlencoded"}})
:=> true
;             Execution time mean : 37.655865 ns
;    Execution time std-deviation : 3.070183 ns
;   Execution time lower quantile : 34.531952 ns ( 2.5%)
;   Execution time upper quantile : 42.119692 ns (97.5%)

;; ### Negative

(-> (request/content-type {:headers {"content-type" "application/json"}})
    (header/form-urlencoded?))
:=> false
;             Execution time mean : 17.072298 ns
;    Execution time std-deviation : 0.681151 ns
;   Execution time lower quantile : 16.266889 ns ( 2.5%)
;   Execution time upper quantile : 17.742046 ns (97.5%)

(ring/urlencoded-form? {:headers {"content-type" "application/json"}})
:=> false
;             Execution time mean : 22.484112 ns
;    Execution time std-deviation : 0.620227 ns
;   Execution time lower quantile : 21.878119 ns ( 2.5%)
;   Execution time upper quantile : 23.364357 ns (97.5%)

;; ### No Content-Type header

(-> (request/content-type {:headers {}})
    (header/form-urlencoded?))
:=> nil
;             Execution time mean : 18.462907 ns
;    Execution time std-deviation : 9.195107 ns
;   Execution time lower quantile : 12.686059 ns ( 2.5%)
;   Execution time upper quantile : 34.340723 ns (97.5%)

(ring/urlencoded-form? {:headers {}})
:=> nil
;             Execution time mean : 21.478331 ns
;    Execution time std-deviation : 7.139575 ns
;   Execution time lower quantile : 15.572769 ns ( 2.5%)
;   Execution time upper quantile : 32.157650 ns (97.5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
