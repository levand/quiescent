# Quiescent Documentation

## Installation

[Quiescent is available](https://clojars.org/quiescent) via Clojars. Add `[quiescent "0.1.1"]` to the dependencies in your
ClojureScript project's `project.clj` file.

Require the `quiescent` and/or `quiescent.dom` namespace in your
ClojureScript source file. There is also a `quiescent` *Clojure*
namespace containing a useful macro which you can include using
`:require-macros` or `:include-macros`.

### tl;dr Example

```clojure
(ns foo
  (:require [quiescent :as q :include-macros true]
            [quiescent.dom :as d]))

;; Define some components

(q/defcomponent AuthorName
   "Span that attributes an author"
   [author]
   (d/a {:className "byline"
         :href (:email author)}
        (:name author)))

(q/defcomponent Article
   "Component representing an article"
   [article]
   (d/div {:className "article"}
     (d/div {:className "title"} (:title article))
     (AuthorName (:author article))
     (d/div {:className "article-body"} (:body article))))

(q/defcomponent ArticleList
   "Component representing multiple articles"
   [data]
   (d/div {:className "articles"}
     (apply d/div {:className "articles"}
                  (map Article (:articles data)))))

;; Create some static data
(def my-data {:articles [{:title "Why core.async is awesome"
                          :body "..."
                          :author {:name "Luke VanderHart"
                                   :email "luke@example.com"}}
                         {:title "Programming: you're doing it completely wrong"
                          :body "..."
                          :author {:name "Rich Hickey"
                                  :email "rich@example.com"}}]})

;; Render it!
(q/render (ArticleList my-data)
          (.getElementById js/document "root-element"))
```

## API

### Defining Components

Components are created using the `quiescent/component` function. It
takes a single argument, which is a function used to render the
component.

The render function should take the component's value as an argument
(an immutable ClojureScript value, when using Quiescent) and return
another ReactJS component. This may be another Quiescent component, a
ReactJS virtual DOM component (see below), or a component created by
JavaScript interop with ReactJS or some other ReactJS wrapper.

```clojure
(def Hello
  "A component that says hello"
  (component (fn [value]
               (d/div {:id "hello"}
                  "Hello, "
                  (d/span {:id "fname"} (:first-name value))
                  " "
                  (d/span {:id "fname"} (:first-name value))))))
```

In this example, `d/div` and `d/span` are constructor functions for
virtual DOM components (see below).

#### The `defcomponent` macro

To save a few keystrokes in the common case, a thin wrapper macro is
provided around the `component` function: `defcomponent`. It takes a
component name, an argument list, and a body.

It expands `(defcomponent C [arg] ...)` into  `(def C (fn (q/component (fn [arg] ...))))`

For example, the `Hello` component defined above could be defined
identically using `defcomponent` as:

```clojure
(defcomponent Hello
  "A component that says hello"
  [value]
  (d/div {:id "hello"}
    "Hello, "
    (d/span {:id "fname"} (:first-name value))
    " "
    (d/span {:id "fname"} (:first-name value))))
```

### Rendering

To render a component, use the `quiescent.render` function, which
renders a component to a DOM node. For example, to render the `Hello`
component from above to a `div` which has an id of `hello-div`:

```clojure
(render (Hello {:fname "Ned" :lname "Stark"}
               (.getElementById js/document "hello-div")))
```

Note that the `render` function will immediately and synchronously
calculate the DOM modifications that are required, and update the
DOM. If you want to do any rate-limiting of rendering, or frame
synchronization (e.g, using `requestAnimationFrame`) you are
responsible for handling that in the code that calls `render`.)

### Creating (virtual) DOM elements

ReactJS provides a full complement of components which correspond to
actual DOM elements. Quiescent provides wrappers around each of these
that allow more idiomatic use from ClojureScript. Specifically, the
Quiescent wrappers allow you to use a ClojureScript map as the
element's properties value instead of a JavaScript object (though you
can still use a JS object if you want.)

Within the `quiescent.dom` namespace, the component constructor names
match HTML element names.

The arguments for all component constructor functions are
the same: a properties map/object, and any child components.

Some examples:

```clojure
(d/div {:className "foo"}
  (d/div {:id "bar"} "Hi!"))
```

```clojure
(d/input {:value "Current Value"}
```

```clojure
(d/input {:type "checkbox"
          :checked true})
```

```clojure
(d/p #js {:class "foo"})
```

Note that property names in the property map correspond to property
accessors in the DOM API, not necessarily attribute names. For an
explanation of other differences from standard HTML attributes, see the ReactJS documentation on:

- [Supported Tags and Attributes](http://facebook.github.io/react/docs/tags-and-attributes.html)
- [DOM Differences](http://facebook.github.io/react/docs/dom-differences.html)
- [Special Non-DOM Attributes](http://facebook.github.io/react/docs/special-non-dom-attributes.html)

### Static Arguments

Sometimes you need to make data available to a component that isn't
logically part of the value that it bases its rendering off of. A good
example of this is the core.async channels that its event handlers
should dispatch values to, or ambient configuration that doesn't
affect how an object renders.

To support this use case, Quiescent allows you to pass more than one
argument to a component constructor. Any additional arguments to a
Quiescent component constructor will be passed through _as is_ to that
component's rendering function, but will _not_ be taken into account
when deciding if a component needs to re-render.

```clojure

(defcomponent Greet
   "A friendly span"
   [name greeting]
   (d/span (str greeting ", " name)))

(do
  (render (Greet "Luke" "Hello") (.-body js/document))
  ;; renders "<span>Hello, Luke</span>"  to the document body

  (render (Greet "Luke" "Bonjour") (.-body js/document))
  ;; Does not change the "<span>Hello, Luke</span>" that was rendered

  (render (Greet "Luc" "Bonjour") (.-body js/document))
  ;; Renders "<span>Bonjour, Luc</span>" to the document body
  ;; Change takes effect because the primary component value changed
)
```

It is important to emphasize again that beause the values of static
arguments are not included in the calculations on whether a component
should re-render, they should be constant for the full lifetime of the
component. If you _do_ need changes to result in changes to the HTML,
you should include them in the primary value you pass to a component
constructor, not its static arguments.

### Accessing the underlying DOM

Sometimes, unfortunately, it is necessary to access the real, raw DOM
elements behind ReactJS's virtual DOM components. A classic example
are the `focus` or `blur` methods; these are methods that are simply
not accessible via the element's properties, and therefore not
controllable through React's standard property mechanisms.

React itself provides lifecycle events and accessor methods that, in
combination, can be used to access unadorned DOM elements. Quiescent
wraps these object-oriented accessors in a more functional approach
and provides special component constructors that invoke a callback
function whenever the component is actually rendered, passing the
actual rendered DOM node to the callback for whatever purpose it is
needed.

These wrapping component constructors are:

- `on-mount`: fires after a component is inserted into the DOM for the
  first time
- `on-update`: fires whenever a component is re-rendered, but not on
  its initial render
- `on-render`: fires every time a component is rendered or
  re-rendered, including first time and subsequent updates.

Each of these component constructors takes a single child component
and a callback method. The component they yield will be rendered in
the usual way, with the difference that after the render is complete
the callback will be invoked and passed the DOM node. Then, you can do
whatever you need with the node.

```clojure

(defcomponent FocusingInput
  [input-state]
  (on-render (d/input {:id "my-input"
                       :value (:value input-state)})
             (fn [dom-node]
                (when (:should-focus? dom-node)
                  (.focus input-node)))))
```

Use caution with this feature; make sure that what you're trying to
accomplish can't be done via setting normal properties on a DOM
component constructor. Be aware that although you *can* do whatever
you want to the underlying node, any changes you make to properties
managed by ReactJS are likely to be undone the next time the component
renders.

As of version 0.1.2, it is also possible to define callbacks for multiple
lifecycle events using a single wrapping component.

To use this functionality, use the `quiescent/wrapper`, passing the wrapped
component as the first argument and any number of lifecycle ID/handler
function pairs as additional arguments. Valid lifecycle IDs include: 
`:onUpdate`, `:onMount`, `:onWillUpdate`, `:onWillMount`, and `:onWillUnmount`.
 
For example:
 
```clojure
(defcomponent SomeComponent
  [value]
  (wrapper (d/input {:id "some-node" :value "some value"})
    :onMount (fn [node] (.log js/console "Mounted component with DOM node:" node))
    :onWillUnmount (fn [] (.log js/console "About to unmount component"))))
 
```

### Accessing the underlying component

Very occasionally, you might need to access the actual ReactJS object
from within a render function or the callback on a wrapping component
constructor.

To allow this, Quiescent binds the `quiescent/*component*` dynamic var
when invoking render or DOM callback functions. The value is the
ReactJS component itself, the same that would be bound as `this` in
the body of a vanilla ReactJS lifecycle method written in JavaScript.

### Creating your own ReactJS components

If none of these techniques are sufficient for your use case,
Quiescent does not prevent you from manually defining and using your
own ReactJS component by other means, either through JavaScript
interop or a different ReactJS wrapper library.

This grants ubiquitous access to the full power of ReactJS, if you
need it. However, you will be responsible for defining the full
component implementation and lifecycle methods yourself, the same as
if you were writing raw ReactJS.
