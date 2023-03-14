(defproject com.github.strojure/ring-lib "1.2.0-44-alpha1"
  :description "Opinionated implementations for Clojure ring handler."
  :url "https://github.com/strojure/ring-lib"
  :license {:name "The Unlicense" :url "https://unlicense.org"}

  :dependencies [;; Delayed values in maps
                 [com.github.strojure/zmap "1.3.26"]
                 ;; Request utils from undertow
                 [io.undertow/undertow-core "2.3.4.Final"]
                 ;; Reuse standard ring implementations
                 [ring/ring-core "1.9.6"]
                 ;; Use web-security implementations
                 [com.github.strojure/web-security "0.2.1-22"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.11.1"]]}
             :dev,,,,, {:dependencies [[ring/ring-defaults "0.3.4"]]
                        :source-paths ["doc"]}}

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo" :sign-releases false}]])
