(ns lifecycle_callbacks
 (:require [quiescent.core :as q]
           [quiescent.dom :as d]))

(def data (atom {:show-it false
                 :text "Hello"}))

(declare Parent)

(defn render
  []
  (q/render
    (Parent @data)
    (.getElementById js/document "content")))

(defn listener
  [type]
  (fn [& args]
    (.log js/console type (str args))))

(q/defcomponent Child
  :on-mount  (listener "on-mount")
  :on-update (listener "on-update")
  :on-unmount (listener "on-unmount")
  [val constant]
  (d/div {} (:text val)))

(q/defcomponent Parent
  [val]
  (d/div {}
    (d/button {:onClick (fn [_]
                          (swap! data (fn [val]
                                        (assoc val :show-it (not (:show-it val)))))
                          (render))}
      "Toggle Child")
    (d/button {:onClick (fn [_]
                          (swap! data (fn [val]
                                        (update-in val [:text] #(if (= % "Hello")
                                                                 "Goodbye"
                                                                 "Hello"))))

                          (render))}
      "Update Text")
    (when (:show-it val)
      (Child val "some-constant"))))

(defn ^:export main
 []
  (render))
