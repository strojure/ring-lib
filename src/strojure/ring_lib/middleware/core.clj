(ns strojure.ring-lib.middleware.core)

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Middleware builder function ##

(defn wrap
  "Middleware to apply `(fn request-fn [request] new-request)` and
  `(fn response-fn [response request] new-response)` to ring request/response."
  {:added "1.1"}
  [handler, request-fn, response-fn]
  (fn
    ([request]
     (response-fn (handler (request-fn request)) request))
    ([request respond raise]
     (let [request (request-fn request)]
       (handler request
                (fn [response] (respond (response-fn response request)))
                raise)))))

(defn wrap-request
  "Middleware to apply `(fn request-fn [request] new-request)` to ring request."
  {:added "1.1"}
  [handler, request-fn]
  (fn
    ([request]
     (handler (request-fn request)))
    ([request respond raise]
     (handler (request-fn request) respond raise))))

(defn wrap-response
  "Middleware to apply `(fn response-fn [response request] new-response)` to
  ring response."
  {:added "1.1"}
  [handler, response-fn]
  (fn
    ([request]
     (response-fn (handler request) request))
    ([request respond raise]
     (handler request
              (fn [response] (respond (response-fn response request)))
              raise))))

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
