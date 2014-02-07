(ns quiescent)

(defn component
  "Return a function that will return a ReactJS component, using the
  provided function as the implementation for React's 'render' method
  on the component.

  The given render function should take a single immutable value as
  its first argument, and return a single ReactJS component.
  Additional arguments to the component constructor will be passed as
  additional arguments to the render function whenever it is invoked,
  but will *not* be included in any calculations regarding whether the
  component should re-render."
  [renderer]
  (let [react-component
        (js/React.createClass
                      #js {:shouldComponentUpdate
                           (fn [next-props _]
                             (this-as this
                                      (not= (.. this -props -value)
                                            (.. next-props -value))))
                           :render
                           (fn []
                             (this-as this
                                      (apply renderer
                                             (.. this -props -value)
                                             (.. this -props -static-args))))})]
    (fn [value & static-args]
      (react-component #js {:value value
                            :static-args static-args}))))

(defn render
  "Given a ReactJS component, render it, rooted to the specified DOM
  node, using the specified properties."
  [component node]
  (.renderComponent js/React component node))

