(ns component-keys
 (:require [quiescent.core :as q]
           [quiescent.dom :as d]))

(def data (atom {:sort >
                 :items [{:year 2001 :text "Enterprise"}
                         {:year 1999 :text "Voyager"}
                         {:year 1993 :text "Deep Space 9"}
                         {:year 1987 :text "Next Generation"}
                         {:year 1966 :text "The Original Series"}]}))

(declare ListComponent)

(defn render
  []
  (q/render
    (ListComponent @data)
    (.getElementById js/document "content")))

(q/defcomponent ListItem
 :keyfn :text
 [item]
 (d/li {}
   (d/span {} (:year item))
   (d/span {} " ")
   (d/span {} (:text item))))

(defn toggle
  [val opt1 opt2]
  (if (= val opt1) opt2 opt1))

(q/defcomponent ListComponent
 [val]
  (apply d/ul {}
    (d/button {:onClick (fn [_]
                          (swap! data (fn [val]
                                        (let [c (toggle (:sort val) < >)]
                                          (-> val
                                            (update-in [:items] #(sort-by :year c %))
                                            (assoc :sort c)))))
                          (render))}
      "Re-Sort")
    (map ListItem (:items val))))

(defn ^:export main
 []
  (render))
