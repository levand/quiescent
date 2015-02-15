(ns animations
 (:require [quiescent.core :as q]
           [quiescent.dom :as d]))

(defn item
  []
  (.random js/Math))

(def data (atom (into (sorted-set) (repeatedly 3 item))))

(declare Container)

(defn render
  []
  (q/render
    (Container @data)
    (.getElementById js/document "content")))

(defn add-handler
  []
  (swap! data conj (item))
  (render))

(defn remove-handler
  [val]
  (swap! data disj val)
  (render))

(q/defcomponent CSSTransitionItem
  :keyfn identity
  [val]
  (d/div {:className "css-transition-item"
          :onClick #(remove-handler val)} val))

(q/defcomponent CssTransitionContainer
  [val]
  (d/div {:className "css-transition-container"}
    (apply q/CSSTransitionGroup "my-transition" (map CSSTransitionItem val))))

(q/defcomponent Container
  [val]
  (d/div {}
    (d/button {:onClick add-handler}
      "Add Item")
    (CssTransitionContainer val)))

(defn ^:export main
 []
  (render))
