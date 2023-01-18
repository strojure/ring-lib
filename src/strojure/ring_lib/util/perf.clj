(ns strojure.ring-lib.util.perf
  (:import (clojure.lang Associative)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn merge*
  "Merges maps, optimized for empty input."
  {:added "1.0"}
  ([m] m)
  ([m1 m2]
   (if (and m1 m2)
     (reduce-kv (fn [m k v] (.assoc ^Associative m k v)) m1 m2)
     (or m1 m2))))

(comment
  (merge nil nil)
  ;Execution time mean : 64,112233 ns
  (merge* nil nil)
  ;Execution time mean : -2,756442 ns

  (merge {"a" "", "b" "", "c" ""} {"f" "1"})
  ;Execution time mean : 307,453694 ns
  (merge* {"a" "", "b" "", "c" ""} {"f" "1"})
  ;Execution time mean : 94,477398 ns

  (merge {"f" "1"} {"a" "", "b" "", "c" ""})
  ;Execution time mean : 420,538873 ns
  (merge* {"f" "1"} {"a" "", "b" "", "c" ""})
  ;Execution time mean : 199,432549 ns
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
