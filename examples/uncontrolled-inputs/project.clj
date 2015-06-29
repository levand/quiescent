(defproject quiescent.examples.uncontrolled-inputs "0.1.0"
  :plugins [[lein-cljsbuild "1.0.4"]]
  :dependencies [[org.clojure/clojure "1.7.0-alpha5"]
                 [org.clojure/clojurescript "0.0-2843"]
                 [quiescent/quiescent "0.2.0-RC1"]]
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
                       :pretty-print  true}}}})
