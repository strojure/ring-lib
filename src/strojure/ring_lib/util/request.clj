(ns strojure.ring-lib.util.request
  "Functions for augmenting and pulling information from request maps.")

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn content-type
  "Returns the `content-type` header of the request."
  {:tag String :added "1.0"}
  [request]
  (-> request (get :headers) (get "content-type")))

(comment
  (content-type {:headers {}})
  (content-type {:headers {"content-type" "text/plain"}})
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

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
