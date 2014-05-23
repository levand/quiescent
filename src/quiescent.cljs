(ns quiescent)

(defn js-props
  "Utility function. Takes an object which is (possibly) a
  ClojureScript map. If the value is a ClojureScript map, convert it
  to a JavaScript properties object. Otherwise, return the argument
  unchanged."
  [obj]
  (if (map? obj)
    (let [o (js-obj)]
      (doseq [[k v] obj] (aset o (name k) (js-props v)))
      o)
    obj))

(def ^:dynamic *component*
  "Within a component render function, will be bound to the raw
  ReactJS component." nil)

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
        (.createClass js/React
           #js {:shouldComponentUpdate
                (fn [next-props _]
                  (this-as this
                           (not= (aget (.-props this) "value")
                                 (aget next-props "value"))))
                :render
                (fn []
                  (this-as this
                           (binding [*component* this]
                             (apply renderer
                                    (aget (.-props this) "value")
                                    (aget (.-props this) "statics")))))})]
    (fn [value & static-args]
      (react-component #js {:value value :statics static-args}))))

(def WrapperComponent
  "Wrapper component used to mix-in lifecycle access"
  (.createClass js/React
     #js {:render
          (fn [] (this-as this (aget (.-props this) "wrappee")))
          :componentDidUpdate
          (fn [prev-props prev-state]
            (this-as this
              (when-let [f (aget (.-props this) "onUpdate")]
                (binding [*component* this]
                  (f (.getDOMNode this))))))
          :componentDidMount
          (fn []
            (this-as this
              (when-let [f (aget (.-props this) "onMount")]
                (binding [*component* this]
                  (f (.getDOMNode this))))))
          :componentWillMount
          (fn []
            (this-as this
              (when-let [f (aget (.-props this) "onWillMount")]
                (binding [*component* this]
                  (f)))))
          :componentWillUpdate
          (fn [_ _]
            (this-as this
              (when-let [f (aget (.-props this) "onWillUpdate")]
                (binding [*component* this]
                  (f)))))
          :componentWillUnmount
          (fn []
            (this-as this
              (when-let [f (aget (.-props this) "onWillUnmount")]
                (binding [*component* this]
                  (f)))))}))

(defn wrapper
  "Create a wrapper function for a compoment implementing multiple
  lifecycle functions. Lifecycle functions are specified as any number
  of additional key-value pairs passed as arguments to this function.

  Valid keys and values include:

  :onUpdate - will call the provided function,
              passing the rendered DOM node as a single arg
  :onMount - will call the provided function,
             passing the rendered DOM node as a single arg
  :onWillUpdate - will call the provided function with no arguments
  :onWillMount - will call the provided function with no arguments
  :onWillUnmount - will call the provided function with no arguments"
  [child & kvs]
  (let [props (js-props (apply array-map :wrappee child kvs))]
    (when-let [key (aget (.-props child) "key")]
      (aset props "key" key))
    (WrapperComponent props)))

(defn on-update
  "Wrap a component, specifying a function to be called on the
  componentDidUpdate lifecycle event.

  The function will be passed the rendered DOM node."
  [child f]
  (wrapper child :onUpdate f))

(defn on-mount
  "Wrap a component, specifying a function to be called on the
  componentDidMount lifecycle event.

  The function will be passed the rendered DOM node."
  [child f]
  (wrapper child :onMount f))

(defn on-render
  "Wrap a component, specifying a function to be called on the
  componentDidMount AND the componentDidUpdate lifecycle events.

  The function will be passed the rendered DOM node."
  [child f]
  (wrapper child :onMount f :onUpdate f))


(defn on-will-mount
  "Wrap a component, specifying a function to be called on the
  componentWillMount lifecycle event.

  The function will be called with no arguments."
  [child f]
  (wrapper child :onWillMount f))

(defn on-will-update
  "Wrap a component, specifying a function to be called on the
  componentWillUpdate lifecycle event.

  The function will be called with no arguments."
  [child f]
  (wrapper child :onWillUpdate f))

(defn on-will-render
  "Wrap a component, specifying a function to be called on the
  componentWillMount AND the componentWillUpdate lifecycle events.

  The function will be called with no arguments."
  [child f]
  (wrapper child :onWillMount f :onWillUpdate f))


(defn on-will-unmount
  "Wrap a component, specifying a function to be called on the
  componentWillUnmount lifecycle event.

  The function will be called with no arguments."
  [child f]
  (wrapper child :onWillUnmount f))

(defn render
  "Given a ReactJS component, immediately render it, rooted to the
  specified DOM node."
  [component node]
  (.renderComponent js/React component node))
