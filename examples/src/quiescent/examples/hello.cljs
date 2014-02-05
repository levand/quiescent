(ns quiescent.examples.hello
  (:require [clojure.browser.repl]))

(defn ^:export main
  []
  (js/alert "hello, world!"))
