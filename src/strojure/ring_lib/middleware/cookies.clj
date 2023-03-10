(ns strojure.ring-lib.middleware.cookies
  "Middleware for parsing and generating cookies. Reuses standard implementation
  with delayed `:cookies` in request."
  (:require [ring.middleware.cookies :as cookies]
            [ring.util.codec :as codec]
            [strojure.zmap.core :as zmap])
  (:import (clojure.lang Associative)))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn cookies-request-fn
  "Parses the cookies in the request map, then assocs the resulting map to the
  delayed `:cookies` key on the request.

  Accepts the following options:

  - `:decoder` – a function to decode the cookie value.
      + Expects a function that takes a string and returns a string.
      + Defaults to URL-decoding.

  Each cookie is represented as a map, with its value being held in the `:value`
  key. A cookie may optionally contain a `:path`, `:domain` or `:port`
  attribute.
  "
  [{:keys [decoder] :or {decoder codec/form-decode-str}}]
  (fn [^Associative request]
    (when request
      (if (.containsKey request :cookies)
        request
        (-> request
            (.assoc :cookies (zmap/delay (#'cookies/parse-cookies request decoder)))
            (zmap/wrap))))))

(comment
  (def -request-fn (cookies-request-fn {}))

  (-request-fn {:cookies {}})
  :=> {:cookies {}}
  ;; Execution time mean : 20.134905 ns

  (-request-fn {:headers {"cookie" "JSESSIONID=YXnPYqFOpP3kLAb-f8aLZ4SnJ2WGdyVV7TedaYQK"}})
  :=> {:headers {"cookie" "JSESSIONID=YXnPYqFOpP3kLAb-f8aLZ4SnJ2WGdyVV7TedaYQK"},
       :cookies {"JSESSIONID" {:value "YXnPYqFOpP3kLAb-f8aLZ4SnJ2WGdyVV7TedaYQK"}}}
  ;; Execution time mean : 87.982318 ns

  (:cookies (-request-fn {:headers {"cookie" "JSESSIONID=YXnPYqFOpP3kLAb-f8aLZ4SnJ2WGdyVV7TedaYQK"}}))
  :=> {"JSESSIONID" {:value "YXnPYqFOpP3kLAb-f8aLZ4SnJ2WGdyVV7TedaYQK"}}
  ;; Execution time mean : 3.802733 µs
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

(defn cookies-response-fn
  "For responses with `:cookies`, adds `Set-Cookie` header and returns response
  without `:cookies`.

  Accepts the following options:

  - `:encoder` – a function to encode the cookie name and value.
      + Expects a function that takes a name/value map and returns a string.
      + Defaults to URL-encoding.

  To set cookies, add a map to the :cookies key on the response. The values
  of the cookie map can either be strings, or maps containing the following
  keys:

  - `:value`     – the new value of the cookie
  - `:path`      – the subpath the cookie is valid for
  - `:domain`    – the domain the cookie is valid for
  - `:max-age`   – the maximum age in seconds of the cookie
  - `:expires`   – a date string at which the cookie will expire
  - `:secure`    – set to true if the cookie requires HTTPS, prevent HTTP access
  - `:http-only` – set to true if the cookie is valid for HTTP and HTTPS only
                   (i.e. prevent JavaScript access)
  - `:same-site` – set to `:strict` or `:lax` to set SameSite attribute of the
                   cookie
  "
  [{:keys [encoder] :or {encoder codec/form-encode}}]
  (fn cookies-response
    ([response _request] (cookies-response response))
    ([response]
     (-> (#'cookies/set-cookies response encoder)
         (dissoc :cookies)))))

(comment
  (def -response-fn (cookies-response-fn {}))

  (-response-fn {:cookies {"JSESSIONID" {:value "YXnPYqFOpP3kLAb-f8aLZ4SnJ2WGdyVV7TedaYQK"}}})
  :=> {:headers {"Set-Cookie" '("JSESSIONID=YXnPYqFOpP3kLAb-f8aLZ4SnJ2WGdyVV7TedaYQK")}}
  ;; Execution time mean : 2.423365 µs
  )

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
