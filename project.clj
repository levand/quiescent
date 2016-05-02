(defproject quiescent "0.3.1-SNAPSHOT"
  :description "A minimal, functional ClojureScript wrapper for ReactJS"
  :url "http://github.com/levand/quiescent"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 [cljsjs/react-with-addons "15.1.0-0"]
                 [cljsjs/react-dom "15.1.0-0" :exclusions [cljsjs/react]]]
  :source-paths ["src"])
