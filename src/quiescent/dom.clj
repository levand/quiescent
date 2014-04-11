(ns quiescent.dom)

(defn tag-definition
  "Return a form to defining a wrapper function for a ReactJS tag
  component."
  [tag]
  (let [f (symbol (str "js/React.DOM." (name tag)))]
    `(defn ~tag [& args#]
       ~(str "Return a component for ")
       (let [a# (make-array 0)]
         (.push a# (quiescent.dom/js-props (first args#)))
         (doseq [arg# (rest args#)] (.push a# arg#))
         (.apply ~f nil a#)))))

(defmacro define-tags
  "Macro which expands to a do block which contains a defmacro for
  each supported HTML and SVG tag. The resulting macros take
  an (optional) properties argument, and any number of child
  arguments. The properties argument may be a Clojure map or a JS
  object."
  [& tags]
  `(do (do ~@(clojure.core/map tag-definition tags))
       (def ~'defined-tags
         ~(zipmap (map (comp keyword name) tags)
                 tags))))


