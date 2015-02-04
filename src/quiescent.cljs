(ns quiescent
  (:require [cljsjs.react]))

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

(def ^:dynamic *react-element*
  "Within a component render function, is be bound to the ReactElement instance." nil)

(defn component
  "Return a factory function that will return a ReactElement, using the
  provided function as the 'render' method for a ReactJS component, which is created and
  instantiated behind-the-scenes.

  The given render function should take a single immutable value as
  its first argument, and return a single ReactElement. Additional arguments to the returned factory
  function are  /constant arguments/  which will be passed on as additional arguments to the
  supplied render function, but will *not* be included in any calculations regarding whether the
  element should re-render. As such, they are suitable for values that will remain constant for
  the lifetime of the rendered element, such as message channels and configuration objects."
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
                           (binding [*react-element* this]
                             (apply renderer
                                    (aget (.-props this) "value")
                                    (aget (.-props this) "constants")))))})]
    (fn [value & constant-args]
      (.createElement js/React react-component #js {:value value :constants constant-args}))))

(def WrapperComponent
  "Wrapper component used to mix-in lifecycle access"
  (.createClass js/React
     #js {:render
          (fn [] (this-as this (aget (.-props this) "wrappee")))
          :componentDidUpdate
          (fn [prev-props prev-state]
            (this-as this
              (when-let [f (aget (.-props this) "onUpdate")]
                (binding [*react-element* this]
                  (f (.getDOMNode this))))))
          :componentDidMount
          (fn []
            (this-as this
              (when-let [f (aget (.-props this) "onMount")]
                (binding [*react-element* this]
                  (f (.getDOMNode this))))))
          :componentWillMount
          (fn []
            (this-as this
              (when-let [f (aget (.-props this) "onWillMount")]
                (binding [*react-element* this]
                  (f)))))
          :componentWillUpdate
          (fn [_ _]
            (this-as this
              (when-let [f (aget (.-props this) "onWillUpdate")]
                (binding [*react-element* this]
                  (f)))))
          :componentWillUnmount
          (fn []
            (this-as this
              (when-let [f (aget (.-props this) "onWillUnmount")]
                (binding [*react-element* this]
                  (f (.getDOMNode this))))))}))

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
    (.createElement js/React WrapperComponent props)))

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
  "Given an Element, immediately render it, rooted to the
  specified DOM node."
  [element node]
  (.render js/React element node))

(defn unmount
  "Remove a mounted Element from the given DOM node."
  [node]
  (.unmountComponentAtNode js/React node))

(defn ^:deprecated unmount-at-node
  "DEPRECATED: Use 'unmount' instead."
  [node]
  (unmount node))
