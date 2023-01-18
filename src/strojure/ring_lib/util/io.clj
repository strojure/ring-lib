(ns strojure.ring-lib.util.io
  "Utility functions for handling I/O."
  (:import (java.io ByteArrayInputStream ByteArrayOutputStream InputStream)
           (java.nio.charset Charset StandardCharsets)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(def ^{:arglists '([^InputStream input]
                   [^InputStream input, charset])
       :added "1.0"}
  read-all-bytes
  "Reads string from input stream. If `charset` is not specified or `nil` then
  UTF-8 is used."
  (if (try (.readAllBytes (ByteArrayInputStream. (.getBytes "")))
           (catch Throwable _ false))

    (fn read-all-bytes
      ([^InputStream input] (read-all-bytes input nil))
      ([^InputStream input, charset]
       ;; This implementation works since Java 9.
       (let [^Charset charset (or charset StandardCharsets/UTF_8)]
         (String. (.readAllBytes input) charset))))

    (fn fallback-read-all-bytes
      ([^InputStream input] (fallback-read-all-bytes input nil))
      ([^InputStream input, charset]
       (let [charset (or charset StandardCharsets/UTF_8)
             output (ByteArrayOutputStream.)]
         (loop [x (.read input)]
           (when-not (neg? x)
             (.write output (unchecked-int x))
             (recur (.read input))))
         (String. (.toString output ^Charset charset)))))))

(comment

  (read-all-bytes (ByteArrayInputStream. (.getBytes "f=1")))
  ;             Execution time mean : 50,134203 ns
  ;    Execution time std-deviation : 5,092112 ns
  ;   Execution time lower quantile : 46,772635 ns ( 2,5%)
  ;   Execution time upper quantile : 58,107803 ns (97,5%)

  (slurp (ByteArrayInputStream. (.getBytes "f=1")))
  ;             Execution time mean : 4,868094 µs
  ;    Execution time std-deviation : 2,827165 µs
  ;   Execution time lower quantile : 2,406678 µs ( 2,5%)
  ;   Execution time upper quantile : 8,509259 µs (97,5%)
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
