(ns benchmark.util-headers
  (:require [ring.util.request :as ring]
            [strojure.ring-lib.util.header :as header]
            [strojure.ring-lib.util.request :as request]))

(set! *warn-on-reflection* true)

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Extract charset

;; ### Content-Type with charset

(-> (request/content-type {:headers {"content-type" "application/x-www-form-urlencoded; charset=windows-1251"}})
    (header/extract-charset))
;=> #object[sun.nio.cs.MS1251 0x76afac16 "windows-1251"]

(ring/character-encoding {:headers {"content-type" "application/x-www-form-urlencoded; charset=windows-1251"}})
:=> "windows-1251"
;

;; ### Content-Type without charset

(-> (request/content-type {:headers {"content-type" "application/x-www-form-urlencoded"}})
    (header/extract-charset))
:=> nil
;

(ring/character-encoding {:headers {"content-type" "application/x-www-form-urlencoded"}})
:=> nil
;

;; ### No Content-Type

(-> (request/content-type {:headers {}})
    (header/extract-charset))
:=> nil
;

(ring/character-encoding {:headers {}})
:=> nil
;

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,

;; ## Test for "application/x-www-form-urlencoded" content type

;; ### Positive

(-> (request/content-type {:headers {"content-type" "application/x-www-form-urlencoded"}})
    (header/form-urlencoded?))
:=> true

(ring/urlencoded-form? {:headers {"content-type" "application/x-www-form-urlencoded"}})
:=> true
;Execution time mean : 39,191427 ns

;; ### Negative

(-> (request/content-type {:headers {"content-type" "application/json"}})
    (header/form-urlencoded?))
:=> false

(ring/urlencoded-form? {:headers {"content-type" "application/json"}})
:=> false
;Execution time mean : 28,851349 ns

;; ### No Content-Type header

(-> (request/content-type {:headers {}})
    (header/form-urlencoded?))
:=> nil

(ring/urlencoded-form? {:headers {}})
:=> nil
;Execution time mean : 24,786668 ns

;;,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,
