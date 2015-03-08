(ns quiescent.dom.uncontrolled
  (:require [quiescent.core :as q]
            [cljsjs.react]))

(defn- uncontrolled-component
  "Delegate should be a raw ReactJS constructor.

   Prop values are the same as that of the ReactJS delegate element,
   with the exception that setting 'value' will not cause the element
   to become a controlled component."
  [name delegate-ctor]
  (q/component (fn [props]
                 (let [js-props (clj->js (dissoc props :value))]
                   (delegate-ctor js-props)))
               {:name name
                :on-render (fn [node props _ _]
                             (when (not= (.-value node) (:value props))
                               (set! (.-value node) (:value props))))}))


(def input
  "Returns an uncontrolled input component constructor"
  (uncontrolled-component "uncontrolled-input" js/React.DOM.input))

(def textarea
  "Returns an uncontrolled textara component constructor"
  (uncontrolled-component "uncontrolled-textarea" js/React.DOM.textarea))
