(ns quiescent.core)

(defmacro react-method
  "Internal helper macro to avoid code duplication.

   Like 'fn', but wraps the body in a binding of quiescent/*component*"
  [args & body]
  `(fn ~args
     (cljs.core/this-as this#
       (binding [quiescent.core/*component* this#]
         ~@body))))

(defn- extract-docstr
  [[docstr? & forms]]
  (if (string? docstr?)
    [docstr? forms]
    ["" (cons docstr? forms)]))

(defn- extract-opts
  ([forms] (extract-opts forms {}))
  ([[k v & forms] opts]
    (if (keyword? k)
      (extract-opts forms (assoc opts k v))
      [opts (concat [k v] forms)])))

(defmacro defcomponent
  "Creates a ReactJS component with the given name, a docstring (optional), any number of
  option->value pairs (optional), an argument vector and any number of forms body, which will be
  used as the rendering function to quiescent.core/component.

  For example:

    (defcomponent Widget
      'A Widget'
      :keyfn #(...)
      :on-render #(...)
      [value constant-value]
      (some-child-components))

  Is shorthand for:

    (def Widget (quiescent/component
                  (fn [value constant-value] (some-child-components))
                  {:keyfn #(...)
                   :on-render #(...)}))"
  [name & forms]
  (let [[docstr forms] (extract-docstr forms)
        [options forms] (extract-opts forms)
        [argvec & body] forms
        options (merge {:name (str name)} options)]
    `(def ~name ~docstr (quiescent.core/component (fn ~argvec ~@body) ~options))))
