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

(q/defcomponent TransitionItem
  :keyfn identity
  :will-enter (fn [node cb value & constants]
                  (.log js/console "will-enter")
                  (-> (new js/TWEEN.Tween #js {:x (.-innerWidth js/window)})
                    (.to #js {:x 0} 1000)
                    (.onUpdate (fn []
                                 (this-as this
                                   (set! (.-transform (.-style node))
                                     (str "translate(" (aget this "x") "px, 0px)")))))
                    (.onComplete cb)
                    (.easing (.. js/TWEEN -Easing -Bounce -Out))
                    (.start)))
  :did-enter (fn [node value & constants]
               (.log js/console "did-enter"))
  :will-leave (fn [node cb value & constants]
                (.log js/console "will-enter")
                (set! (.-position (.-style node)) "fixed")
                (aset (.-style node) "z-index" 1000)
                (-> (new js/TWEEN.Tween #js {:y 0})
                  (.to #js {:y (.-innerHeight js/window)} 1000)
                  (.onUpdate (fn []
                               (this-as this
                                 (set! (.-transform (.-style node))
                                   (str "translate(0px," (aget this "y") "px)")))))
                  (.onComplete cb)
                  (.easing (.. js/TWEEN -Easing -Quadratic -In))
                  (.start)))
  :did-leave (fn [node value & constants]
               (.log js/console "did-leave"))
  [val]
  (d/div {:className "transition-item"
          :onClick #(remove-handler val)} val))

(q/defcomponent TransitionContainer
  [val]
  (d/div {:className "transition-container"}
    (q/TransitionGroup {} (map TransitionItem val))))

(q/defcomponent Container
  [val]
  (d/div {}
    (d/button {:onClick add-handler}
      "Add Item")
    (TransitionContainer val)))

(defn animate
  []
  (.update js/TWEEN)
  (.requestAnimationFrame js/window animate))

(defn ^:export main
 []
  (animate)
  (render))
