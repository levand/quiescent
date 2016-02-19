(defproject quiescent.examples.lifecycle-callbacks "0.1.0"
  :plugins [[lein-cljsbuild "1.1.2"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [quiescent/quiescent "0.3.0-SNAPSHOT"]]
  :clean-targets ^{:protect false} [:target-path :compile-path "resources/public/gen"]
  :cljsbuild {:builds
              {:dev  {:source-paths ["src"]
                      :compiler
                      {:output-dir    "resources/public/gen/dev"
                       :output-to     "resources/public/gen/dev/main.js"
                       :optimizations :none
                       :source-map    true}}
               :prod {:source-paths ["src"]
                      :compiler
                      {:output-to     "resources/public/gen/main.js"
                       :optimizations :advanced
                       :pretty-print  false}}}})
