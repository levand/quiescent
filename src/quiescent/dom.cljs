(ns quiescent.dom
  (:refer-clojure :exclude [time map meta])
  (:require [quiescent :as q])
  (:require-macros [quiescent.dom :as dm]))

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

(dm/define-tags
  a abbr address area article aside audio b base bdi bdo big blockquote body br
  button canvas caption cite code col colgroup data datalist dd del details dfn
  div dl dt em embed fieldset figcaption figure footer form h1 h2 h3 h4 h5 h6
  head header hr html i iframe img input ins kbd keygen label legend li link main
  map mark menu menuitem meta meter nav noscript object ol optgroup option output
  p param pre progress q rp rt ruby s samp script section select small source
  span strong style sub summary sup table tbody td textarea tfoot th thead time
  title tr track u ul var video wbr circle g line path polygon polyline rect svg
  text)


;;; IT IS DECIDED:

; focusables are too special-cased. It'd be cool to have a way
; to short-circuit generalized access to other lifecycle components, *when needed*. Perhaps additional args to q/component?

(comment

  (component-did-update (d/input {})
                        (fn [new-value old-value dom]
                          (if (:active dom))
                          ))

  (q/defcomponent FocusableInput [value]
    (d/input {} foo bar baz)
    :componentDidUpdate [foo]
    ()
    )
)





(defn focusable
  "Given a Quiescent component constructor function, return a
  component constructor that wraps the original, with the difference
  that it observes a :takeFocus key in its props value.

  Whenever the component is rendered, if :takeFocus is truth, the
  component will steal focus.

  To ensure a consistent user experience, clients are responsible for
  ensuring that multiple elements do not have :takeFocus set at once,
  or that elements are not stealing the focus when a user is trying
  to interact elsewhere."
  [ctor]
  (let [component
        (js/React.createClass
         #js {:render
              (fn []
                (this-as this
                         (apply ctor (.. this -props -props)
                                (.. this -props -children))))
              :componentDidUpdate
              (fn []
                (this-as this
                         (if-let [focus? (:takeFocus (.. this -props -props))]
                           (.focus (.getDOMNode this)))))})]
    (fn [props & children]
      (component #js {:props props
                      :children children}))))

;; Define focusable consturctors for elements that can take focus
(def focusable-input (focusable input))
(def focusable-select (focusable select))
(def focusable-textarea (focusable textarea))
(def focusable-button (focusable button))
(def focusable-area (focusable area))
(def focusable-a (focusable a))
