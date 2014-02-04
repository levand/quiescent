(ns quiescent)

(defmacro defcomponent
  "Creates a ReactJS component with the given name, and an argument
  vector (for a single argument) and a body which will be passed as
  the rendering function to quiescent/component.

  Shorthand for:

  (def name (quiescent/component (fn [value] body)))"
  [name argvec & body]
  `(def ~name (quiescent/component (fn ~argvec ~@body))))
