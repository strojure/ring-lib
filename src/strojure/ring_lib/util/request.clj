(ns strojure.ring-lib.util.request
  "Functions for augmenting and pulling information from request maps.")

(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn content-type
  "Returns the `content-type` header of the request."
  {:tag String :added "1.0"}
  [request]
  (some-> (get request :headers) (get "content-type")))

(comment
  (content-type {:headers {}})
  (content-type {:headers {"content-type" "text/plain"}})
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
