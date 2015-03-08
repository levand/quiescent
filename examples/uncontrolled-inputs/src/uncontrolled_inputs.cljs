(ns uncontrolled-inputs
 (:require [quiescent.core :as q]
           [quiescent.dom :as d]
           [quiescent.dom.uncontrolled :as du]))

(def state-atom (atom {:sample-input "Sample Input Value"
                       :sample-textarea "Sample\nTextarea\nValue"}))

(q/defcomponent SampleInput
  [val]
  (du/input {:value val
             :style {:margin "10px"}
             :onChange (fn [evt]
                         (swap! state-atom assoc :sample-input
                                (.-value (.-target evt))))}))

(q/defcomponent SampleTextArea
  [val]
  (du/textarea {:value val
                :cols 50
                :rows 5
                :style {:margin "10px"}
                :onChange (fn [evt]
                            (swap! state-atom assoc :sample-textarea
                                   (.-value (.-target evt))))}))

(q/defcomponent UI
  [state]
  (d/div {}
    (d/pre {} (.stringify js/JSON (clj->js state) nil 2))
    (SampleInput (:sample-input state))
    (d/br {})
    (SampleTextArea (:sample-textarea state))))

(defn render
  "Render the current state atom, and schedule a render on the next
  frame"
  []
  (q/render (UI @state-atom) (.getElementById js/document "content"))
  (.requestAnimationFrame js/window render))

(defn ^:export main
  []
  (render))
