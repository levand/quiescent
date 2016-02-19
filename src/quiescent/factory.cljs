(ns quiescent.factory
  (:require-macros [quiescent.factory :as f])
  (:require [cljsjs.react]))

(defn factory
  "Return a Component factory function. The argument may be any
   value accepted by React.createElement (that is, the string name of a
   HTML tag, or an instance of ReactClass).

   Returns a function that takes props and children (the same as the
   built-in ReactJS element constructors)."
  [type]
  (fn [props & children]
      (apply js/React.createElement type (clj->js props) children)))
