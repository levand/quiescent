(defproject quiescent "0.1.1"
  :description "A minimal, functional ClojureScript wrapper for ReactJS"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.facebook/react "0.9.0"]]
  :source-paths ["src"]

  ;; development concerns
  :profiles {:dev {:source-paths ["src" "examples/src"]
                   :resource-paths ["examples/resources"]
                   :plugins [[lein-cljsbuild "1.0.1"]]
                   :dependencies [[org.clojure/clojure "1.5.1"]
                                  [org.clojure/clojurescript "0.0-2156"]]
                   :cljsbuild
                   {:builds
                    [{:source-paths ["src" "examples/src"]
                      :compiler
                      {:output-to "examples/resources/public/main.js"
                       :output-dir "examples/resources/public/build"
                       :optimizations :whitespace
                       :preamble ["react/react.min.js"]
                       :externs ["react/externs/react.js"]
                       :pretty-print true
                       :source-map
                       "examples/resources/public/main.js.map"
                       :closure-warnings {:non-standard-jsdoc :off}}}]}}})

