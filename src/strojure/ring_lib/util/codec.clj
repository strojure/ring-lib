(ns strojure.ring-lib.util.codec
  "Functions for encoding and decoding data."
  (:import (clojure.lang Associative)
           (java.net URLDecoder)
           (java.nio.charset StandardCharsets)
           (java.util StringTokenizer)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn url-decode
  "Decodes the supplied www-form-urlencoded string using UTF-8 charset.
  Returns `s` if decoding failed."
  {:added "1.0"}
  [^String s]
  (try
    (URLDecoder/decode s StandardCharsets/UTF_8)
    (catch IllegalArgumentException _
      s)))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn reduce-param-token
  "Adds param token string `s` into `result` using reducing function
  `(fn [result key value] new-result)`, where `s` is a URL encoded key/value
  pair like `key=value`."
  {:added "1.0"}
  [rf result ^String s]
  (let [i (.indexOf s (unchecked-int #=(int \=)))]
    (cond
      (pos? i)
      (-> result (rf (url-decode (.substring s 0 i))
                     (url-decode (.substring s (inc i)))))
      (zero? i)
      (-> result (rf "" (url-decode (.substring s (inc i)))))
      :else
      (-> result (rf (url-decode s) "")))))

(defn array-suffix-name
  "Default implementation of `:array-name-fn` in [[assoc-param-fn]]. Returns
  param name without suffix `[]` or `nil` if `s` does not end with `[]`. Used to
  collect only parameters with `[]` in vectors."
  {:added "1.0"}
  [^String s]
  (when (.endsWith s "[]")
    (-> s (.substring (unchecked-int 0)
                      (unchecked-subtract-int (.length s) (unchecked-int 2))))))

(comment
  (array-suffix-name "a")
  (array-suffix-name "a[]")
  )

(defn assoc-param-rf
  "Returns reducing function `(fn [m k v])` to collect sequence of parameters in
  map. Configuration options:

  - `:array-name-fn` – a function `(fn [param-name] array-param-name)`.
      + Returns param name for params which should be collected in vectors.
      + Default is [[array-suffix-name]] which uses suffix `[]` in names.

  - `:param-key-fn` – a function `(fn [param-name] ...)`.
      + Converts string name to another type i.e. keyword.
      + Default is not defined.
  "
  {:added "1.0"}
  [{:keys [array-name-fn param-key-fn]
    :or {array-name-fn array-suffix-name}}]
  (fn
    ([] {})
    ([m] m)
    ([^Associative m k v]
     (if-let [kk (when array-name-fn (array-name-fn k))]
       (let [kk (cond-> kk param-key-fn (param-key-fn))]
         (.assoc m kk (conj (.valAt m kk []) v)))
       (.assoc m (cond-> k param-key-fn (param-key-fn)) v)))))

(defn form-decode-fn
  "Returns function `(fn [s] params)` to convert params string (query string,
  form params) to persistent map. Configuration options:

  - `:array-name-fn` – a function `(fn [param-name] array-param-name)`.
      + Returns param name for params which should be collected in vectors.
      + Default is [[array-suffix-name]] which uses suffix `[]` in names.

  - `:param-key-fn` – a function `(fn [param-name] ...)`.
      + Converts string name to another type i.e. keyword.
      + Default is not defined.

  Accepts custom reducing function `rf` instead of `opts` map.
  "
  {:arglists '([{:keys [array-name-fn, param-key-fn]}]
               [rf])
   :added "1.0"}
  [opts]
  (let [rf (if (fn? opts) opts (assoc-param-rf opts))]
    (fn form-decode
      [s]
      (let [tok (StringTokenizer. s "&")]
        (loop [result (rf)]
          (if (.hasMoreTokens tok)
            (recur (reduce-param-token rf result (.nextToken tok)))
            (rf result)))))))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
