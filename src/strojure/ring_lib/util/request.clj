(ns strojure.ring-lib.util.request
  "Functions for augmenting and pulling information from request maps."
  (:import (io.undertow.util Headers)
           (java.nio.charset Charset)))

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn content-type-header
  "Returns the `content-type` header of the request."
  {:tag String :added "1.0"}
  [request]
  (-> request (get :headers) (get "content-type")))

(comment
  (content-type-header {:headers {}})
  (content-type-header {:headers {"content-type" "text/plain"}})
  )

(defn content-type-mime
  "Returns mime-type from the `Content-Type` header of the request, or nil if no
  header is set."
  {:tag String :added "1.0"}
  [request]
  (when-some [s (content-type-header request)]
    (let [i (.indexOf s (unchecked-int #=(int \;)))]
      (if (neg? i) s (.substring s (unchecked-int 0) i)))))

(comment
  (content-type-mime {:headers {"content-type" "text-plain; charset=utf-8"}})
  :=> "text-plain"
  ;Execution time mean : 29,381042 ns
  (content-type-mime {:headers {"content-type" "text-plain"}})
  :=> "text-plain"
  ;Execution time mean : 15,058433 ns
  (content-type-mime {:headers {}})
  :=> nil
  ;Execution time mean : 10,077854 ns

  (require '[ring.util.request :as request])
  (request/content-type {:headers {"content-type" "text-plain; charset=utf-8"}})
  ;Execution time mean : 42,482573 ns
  (request/content-type {:headers {"content-type" "text-plain"}})
  ;Execution time mean : 29,248590 ns
  (request/content-type {:headers {}})
  ;Execution time mean : 20,732401 ns
  )

(defn content-type-charset
  "Returns instance of `Charset` for charset in `Content-Type` request header.
  Throws `java.nio.charset.UnsupportedCharsetException` for invalid charset
  names."
  {:tag Charset :added "1.0"}
  [request]
  (some-> (content-type-header request)
          (Headers/extractQuotedValueFromHeader "charset")
          (Charset/forName)))

(comment

  (content-type-charset {:headers {"content-type" "application/x-www-form-urlencoded; charset=windows-1251"}})
  ;=> #object[sun.nio.cs.MS1251 0x43aa921d "windows-1251"]
  ;             Execution time mean : 104,535625 ns
  ;    Execution time std-deviation : 4,165865 ns
  ;   Execution time lower quantile : 99,807111 ns ( 2,5%)
  ;   Execution time upper quantile : 109,559890 ns (97,5%)

  (content-type-charset {:headers {"content-type" "application/x-www-form-urlencoded"}})
  :=> nil
  ;             Execution time mean : 60,165379 ns
  ;    Execution time std-deviation : 1,732836 ns
  ;   Execution time lower quantile : 58,343280 ns ( 2,5%)
  ;   Execution time upper quantile : 62,658321 ns (97,5%)

  (content-type-charset {:headers {}})
  :=> nil
  ;             Execution time mean : 3,975610 ns
  ;    Execution time std-deviation : 4,388380 ns
  ;   Execution time lower quantile : 1,064353 ns ( 2,5%)
  ;   Execution time upper quantile : 9,649417 ns (97,5%)

  (content-type-charset {:headers {"content-type" "application/x-www-form-urlencoded; charset=foo"}})
  ;java.nio.charset.UnsupportedCharsetException: foo

  (require '[ring.util.request :as request])

  (request/character-encoding {:headers {"content-type" "application/x-www-form-urlencoded; charset=windows-1251"}})
  :=> "windows-1251"
  ;Execution time mean : 1,168041 µs

  (request/character-encoding {:headers {"content-type" "application/x-www-form-urlencoded"}})
  :=> nil
  ;Execution time mean : 273,681579 ns

  (request/character-encoding {:headers {}})
  :=> nil
  ;Execution time mean : 63,058580 ns
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn method-get?
  "True if request method is `:get`."
  {:added "1.0"}
  [request]
  (.equals :get (get request :request-method)))

(comment
  (method-get? {:request-method :get})            ;=> true
  ;Execution time mean : 6,234962 ns
  (method-get? {:request-method :post})           ;=> false
  ;Execution time mean : 5,924996 ns
  )

(defn method-post?
  "True if request method is `:post`."
  {:added "1.0"}
  [request]
  (.equals :post (get request :request-method)))

(defn content-type?
  "True if request content-type is `mime-type`."
  {:added "1.0"}
  [mime-type request]
  (some-> (content-type-mime request)
          ;; Use .equals for better perf on non-equals.
          (.equals mime-type)))

(comment
  (content-type? "text/plain" {:headers {"content-type" "text/plain; charset=utf-8"}})
  ;Execution time mean : 35,079784 ns
  (content-type? "text/plain" {:headers {"content-type" "text/plain"}})
  ;Execution time mean : 15,703021 ns
  (content-type? "text/plain" {:headers {"content-type" "text/html"}})
  ;Execution time mean : 15,853669 ns
  (content-type? "text/plain" {:headers {}})
  ;
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn form-urlencoded?
  "True if request method is `:post` and mime-type is
  `application/x-www-form-urlencoded`."
  {:added "1.0"}
  [request]
  (and (method-post? request)
       (content-type? "application/x-www-form-urlencoded" request)))

(comment
  (form-urlencoded? {:request-method :get})
  :=> false
  ;Execution time mean : 6,466327 ns
  (form-urlencoded? {:request-method :post :headers {}})
  :=> false
  ;Execution time mean : 18,277682 ns
  (form-urlencoded? {:request-method :post
                     :headers {"content-type" "application/x-www-form-urlencoded"}})
  :=> true
  ;Execution time mean : 20,858985 ns
  (form-urlencoded? {:request-method :post
                     :headers {"content-type" "text/plain"}})
  :=> false
  ;Execution time mean : 17,088014 ns

  (require '[ring.util.request :as request])
  (request/urlencoded-form? {:request-method :post :headers {}})
  ;Execution time mean : 24,786668 ns
  (request/urlencoded-form? {:request-method :post
                             :headers {"content-type" "application/x-www-form-urlencoded"}})
  ;Execution time mean : 39,191427 ns
  (request/urlencoded-form? {:request-method :post
                             :headers {"content-type" "text/plain"}})
  ;Execution time mean : 28,851349 ns
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
