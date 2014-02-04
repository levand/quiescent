(ns quiescent.dom)

(def tags
  "Supported HTML and SVG tags"
  '#{a abbr address area article aside audio b base bdi bdo big
     blockquote body br button canvas caption cite code col colgroup
     data datalist dd del details dfn div dl dt em embed fieldset
     figcaption figure footer form h1 h2 h3 h4 h5 h6 head header hr
     html i iframe img input ins kbd keygen label legend li link main
     map mark menu menuitem meta meter nav noscript object ol optgroup
     option output p param pre progress q rp rt ruby s samp script
     section select small source span strong style sub summary sup
     table tbody td textarea tfoot th thead time title tr track u ul
     var video wbr circle g line path polygon polyline rect svg text})


(defmacro define-tags
  "Macro which expands to a do block which contains a defmacro for
  each supported HTML and SVG tag. The resulting macros take
  an (optional) properties argument, and any number of child
  arguments. The properties argument may be a Clojure map or a JS
  object."
  []
  (apply list 'do
         (map (fn [t]
                `(defmacro ~t [& forms#]
                   `(.. js/react ~(symbol "-DOM") ~'~t
                        (quiescent/js-props ~(first forms#))
                        ~@(rest forms#))))
              tags)))

(define-tags)
