(ns strojure.ring-lib.util.header
  (:import (io.undertow.util Headers)
           (java.nio.charset Charset)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn extract-charset
  "Returns instance of `Charset` for charset in the `header`. Throws
  `java.nio.charset.UnsupportedCharsetException` for invalid charset
  names."
  {:tag Charset :added "1.0"}
  [header]
  (some-> header
          (Headers/extractQuotedValueFromHeader "charset")
          (Charset/forName)))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn form-urlencoded?
  "True if the `header` starts with `application/x-www-form-urlencoded`."
  {:added "1.0"}
  [header]
  (some-> ^String header (.startsWith "application/x-www-form-urlencoded")))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
