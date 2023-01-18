(ns benchmark.util-codec
  (:require [ring.util.codec :as ring]
            [strojure.ring-lib.util.codec :as impl]))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^:private form-decode (impl/form-decode-fn {}))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Single params

(form-decode "a=1&b=2&c=3")                       ;=> {"a" "1", "b" "2", "c" "3"}
;             Execution time mean : 421,091511 ns
;    Execution time std-deviation : 50,200114 ns
;   Execution time lower quantile : 366,864966 ns ( 2,5%)
;   Execution time upper quantile : 470,785849 ns (97,5%)

(ring/form-decode "a=1&b=2&c=3")                  ;=> {"a" "1", "b" "2", "c" "3"}
;             Execution time mean : 796,517745 ns
;    Execution time std-deviation : 91,920243 ns
;   Execution time lower quantile : 705,171314 ns ( 2,5%)
;   Execution time upper quantile : 929,015899 ns (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Multiple params

(form-decode "a[]=1&a[]=2&a[]=3")                 ;=> {"a" ["1" "2" "3"]}
;             Execution time mean : 553,517939 ns
;    Execution time std-deviation : 66,704784 ns
;   Execution time lower quantile : 492,128156 ns ( 2,5%)
;   Execution time upper quantile : 622,036694 ns (97,5%)

(ring/form-decode "a=1&a=2&a=3")                  ;=> {"a" ["1" "2" "3"]}
;             Execution time mean : 861,051305 ns
;    Execution time std-deviation : 81,634686 ns
;   Execution time lower quantile : 782,232901 ns ( 2,5%)
;   Execution time upper quantile : 957,715869 ns (97,5%)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
