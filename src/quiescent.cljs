(ns quiescent)

(defn js-props
  "Utility function. Takes an object which is (possibly) a
  ClojureScript map. If the value is a ClojureScript map, convert it
  to a JavaScript properties object. Otherwise, return the argument
  unchanged."
  [obj]
  (if (map? obj)
    (let [o (js-obj)]
      (doseq [[k v] obj] (aset o (name k) v))
      o)
    obj))

(defn component
  "Build a ReactJS component, using the provided function as the
  implementation for React's 'render' method. The given function
  should take a single immutable value as an argument, and return a
  single ReactJS component."
  [renderer]
  (.createClass js/React
                #js {:shouldComponentUpdate
                     (fn [next-props _]
                       (this-as this
                                (not= (.-props this) next-props)))
                     :render
                     (fn []
                       (this-as this
                                (renderer (.-props this))))}))

(defn render
  "Given a ReactJS component, render it, rooted to the specified DOM
  node, using the specified properties."
  [component node]
  (.renderComponent js/React component node))

