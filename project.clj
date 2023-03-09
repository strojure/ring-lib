(defproject com.github.strojure/ring-lib "1.0.29-SNAPSHOT"
  :description "Opinionated implementations for Clojure ring handler."
  :url "https://github.com/strojure/ring-lib"
  :license {:name "The Unlicense" :url "https://unlicense.org"}

  :dependencies [;; Delayed values in maps
                 [com.github.strojure/zmap "1.3.18"]
                 ;; Request utils from undertow
                 [io.undertow/undertow-core "2.3.0.Final"]]

  :profiles {:provided {:dependencies [[org.clojure/clojure "1.11.1"]]}
             :dev,,,,, {:dependencies [[ring/ring-defaults "0.3.4"]]
                        :source-paths ["doc"]}}

  :deploy-repositories [["clojars" {:url "https://clojars.org/repo" :sign-releases false}]])
