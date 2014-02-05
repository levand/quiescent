(ns quiescent)

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

