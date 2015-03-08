# Quiescent Documentation

## Installation

[Quiescent is available](https://clojars.org/quiescent) via
Clojars. Add `[quiescent "0.2.0-alpha1"]` to the dependencies in your
ClojureScript project's `project.clj` file.

Require the `quiescent.core` and/or `quiescent.dom` namespaces in your
ClojureScript source file. There is also a `quiescent.core` *Clojure*
namespace containing a useful macro - this will be included by default
in recent versions of ClojureScript.

### tl;dr Example

```clojure
(ns foo
  (:require [quiescent.core :as q]
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

## Key Concepts

**Components** are reusable and composable definitions of how a
particular piece of data should be rendered to the DOM. In Quiescent,
a component is defined mostly in terms of its **render function**,
which takes a single value and must returns one or more **React
Elements**, which can be thought of as "instances" of components, or
also as nodes in the "virtual DOM" used by ReactJS.

When you define a component using the `quiescent.core/component`
function or the `quiescent.core/defcomponent` macro, they will return
a component constructor function. You can invoke this constructor,
passing it any immutable value, to return a React Element.

React Elements can either be passed to the `quiescent.core/render`
function to render them directly to the DOM, or returned from another
component's render function.

The key aspect of components is that if a component constructor is
passed the same value as an existing component that is already mounted
in the DOM, *evaluation stops*. You only pay to re-render data that
has actually changed since the previous time the application
rendered. Then, any changes to the DOM that actually should take place
are applied to the DOM using React's minimal diffing algorithm.

This is why the values passed to Quiescent component constructors
should be immutable - they can leverage ClojureScript's strong
equality semantics to instantly determine if a component has changed,
and therefore if it needs to re-render or not.

## API

### Defining Components

Components are created using the `quiescent.core/component`
function. It takes two arguments. The first mandatory argment is a
function used to render the component. The second argument is an
optional configuration map.

The render function should take an immutable value as an argument and
return a React Element. React Elements can be obtained by invoking
another Quiescent component constructor, a built-in DOM component
constructor, or calling another ReactJS wrapper, or ReactJS itself
through JS interop.

```clojure
(def Hello
  "A component that says hello"
  (component (fn [value]
               (d/div {:id "hello"}
                  "Hello, "
                  (d/span {:id "fname"} (:first-name value))
                  " "
                  (d/span {:id "lname"} (:last-name value))))
              {:name "HelloWidget"}))
```

In this example, `d/div` and `d/span` are constructor functions for
React Elements, or virtual DOM components. Each one can be passed *other*
React Elements as children, as well as string values.

#### Component Options

There are a number of options available in the configuration map you
can pass to `component`.

- `:keyfn` specifies a function that will be applied to the
  component's value. The return value will be used as the component's
  ReactJS *key*. See
  http://facebook.github.io/react/docs/multiple-components.html#dynamic-children
  for an explanation of when to use ReactJS keys.
- `:name` can specify a string that will be used as the name of the
  component, which can help in debugging. It is optional: ReactJS
  components do not need to have names.
- `:on-mount` specifies a function which will be invoked once,
  immediately after initial rendering occurs. It is passed the DOM
  node, the value and any constant args passed to the render fn. This
  maps to the 'componentDidMount' lifecycle method in ReactJS.
- `:on-update` specifies a function which will be invoked immediately
  after an update is flushed to the DOM, but not on the initial
  render. It is is passed the underlying DOM node, the value, the
  _old_ value, and any constant args passed to the render fn. This
  maps to the 'componentDidUpdate' lifecycle method in ReactJS.
- `:on-unmount` specifies a function which will be invoked immediately
  before the component is unmounted from the DOM. It is passed the
  underlying DOM node, the most recent value and the most recent
  constant args passed to the render fn. This maps to the
  'componentWillUnmmount' lifecycle method in ReactJS.
- `:on-render` specifies a function which will be invoked
  immediately after the DOM is updated, both on the initial render and
  any subsequent updates. It is is passed the underlying DOM node, the
  value, the _old_ value (which will be nil in the case of the initial
  render) and any constant args passed to the render fn. This maps to
  both the 'componentDidMount' and 'componentDidUpdate' lifecycle
  methods in ReactJS.
- `:will-enter` specifies a function invoked whenever this component
  is added to a ReactTransitionGroup.  Invoked at the same time as
  :onMount. Is passed the underlying DOM node, a callback function,
  the value and any constant args passed to the render fn. Maps to the
  'componentWillEnter' lifecycle method in ReactJS. See the ReactJS
  documentation at http://facebook.github.io/react/docs/animation.html
  for full documentation of the behavior.
- `:did-enter` specifies a function nvoked after the callback provided
  to :willEnter is called. It is passed the underlying DOM node, the
  value and any constant args passed to the render fn. Maps to the
  'componentDidEnter' lifecycle method in ReactJS. See the ReactJS
  documentation at http://facebook.github.io/react/docs/animation.html
  for full documentation of the behavior.
- `:will-leave` specifies a function invoked whenever this component
  is removed from a ReactTransitionGroup.  Is passed the underlying
  DOM node, a callback function, the most recent value and the most
  recent constant args passed to the render fn. The DOM node will not
  be removed until the callback is called. Maps to the
  'componentWillEnter' lifecycle method in ReactJS. See the ReactJS
  documentation at http://facebook.github.io/react/docs/animation.html
  for full documentation of the behavior.
- `:did-leave` specifies a function invoked after the callback
  provided to :willLeave is called (at the same time as
  :onUnMount). Is passed the underlying DOM node, the most recent
  value and the most recent constant args passed to the render
  fn. Maps to the 'componentDidLeave' lifecycle method in ReactJS. See
  the ReactJS documentation at
  http://facebook.github.io/react/docs/animation.html for full
  documentation of the behavior.

#### The `defcomponent` macro

To save a few keystrokes in the common case, a thin wrapper macro is
provided around the `component` function: `defcomponent`. It takes a
component name, an optional docstring, a number of config->value
pairs, an argument list, and a body.

It expands `(defcomponent C :opt val [arg] ...)` into  `(def C (fn (q/component (fn [arg] ...) {:opt val})))`

For example, the `Hello` component defined above could be defined
identically using `defcomponent` as:

```clojure
(defcomponent Hello
"A component that says hello"
  :name "HelloWidget"
  [value]
  (d/div {:id "hello"}
    "Hello, "
    (d/span {:id "fname"} (:first-name value))
    " "
    (d/span {:id "lname"} (:last-name value))))
```

### Rendering

To render a React Element, use the `quiescent.core/render` function,
which performs DOM reconciliation and renders an actual DOM node. For
example, to render the `Hello` component from above to a `div` which
has an id of `hello-div`:

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

ReactJS provides a full complement of React Element constructors which
correspond to the basic set of HTML DOM nodes. Quiescent provides
wrappers around each of these that allow more idiomatic use from
ClojureScript. Specifically, the Quiescent wrappers allow you to use a
ClojureScript map as the element's properties value instead of a
JavaScript object (though you can still use a JS object if you want.)

Within the `quiescent.dom` namespace, the component constructor names
match HTML element names.

The arguments for all DOM element constructor functions are
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

### Constant Arguments

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

It is important to emphasize again that because the values of constant
arguments are not included in the calculations on whether a component
should re-render, they should be constant for the full lifetime of the
component. If you _do_ need changes to result in changes to the HTML,
you should include them in the primary value you pass to a component
constructor, not its static arguments.

### Accessing the underlying DOM

Sometimes, unfortunately, it is necessary to access the real, raw DOM
elements behind ReactJS's virtual DOM elements. A classic example
are the `focus` or `blur` methods; these are methods that are simply
not accessible via the element's properties, and therefore not
controllable through React's standard property mechanisms.

React itself provides lifecycle events and accessor methods that, in
combination, can be used to access unadorned DOM nodes.

Quiescent wraps these object-oriented accessors in several optional
callback fuctions that you can define on a component. Typically, these
functions will take as an argument the actual DOM node of the rendered
element, as well as the value and any constant args passed to the
component constructor. Some of these callbacks are also passed the
*old* value of the component.

For example, the following component definition defines an input
element which will focus itself whenever it is provided a true value
for a `:should-focus?` key in its value.

```clojure

(defcomponent FocusingInput
  :on-render (fn [dom-node value _ _ _]
                (when (:should-focus? value)
                  (.focus input-node)))
  [input-state]
  (d/input {:id "my-input" :value (:value input-state)}))
```

Use caution with this feature; make sure that what you're trying to
accomplish can't be done via setting normal properties on a DOM
component constructor. Be aware that although you *can* do whatever
you want to the underlying node, any changes you make to attributes or
child elements (which are managed by ReactJS) are likely to be
overwritten the next time the component renders.

#### A note on wrappers

Previous versions of Quiescent used *wrappers*, a different technique
to access ReactJS lifecycle methods. These are still available but are
deprecated, since it is impossible to make them work correctly in all
cases. See the release notes on version 0.2.0 for more information.

### Cursor jumping and controlled inputs

By default, if you provide a `value` property to an `input` or
`textarea` DOM component, ReactJS will turn that component into a
[Controlled Component](http://facebook.github.io/react/docs/forms.html#controlled-components).

This means that values typed by the user are discarded after the event
handlers fire, so that the actual DOM value of the field is guaranteed
to match matches the ReactJS `value` property. To actually change the
value, you need to update the `value` property.

In React, unless a re-render is called *synchronously* from within the
event handler, the field will be changed *back* to the original value
(because it is controlled), then changed *again* to the new value,
which causes the cursor to jump to the end of the field. See [this
StackOverflow question](http://stackoverflow.com/questions/28922275/in-reactjs-why-does-setstate-behave-differently-when-called-synchronously/28922465)
for a more detailed analysis of this phenomenon.

Unfortunately, Quiescent provides no mechanism for synchronous
re-rendering within an event handler - all rendering is top-down by
philosophy and design.

However, there are two other ways to avoid having cursors jump around
in a Quiescent-managed input.

One is to commit changes back to the application state inside an
`onBlur` event, rather than `onChange`. This means that the
application state is only updated once, when the edit is complete,
rather than on every keystroke. For many applications this is
acceptable and even desirable, since it decreases the number of state
updates, therefore increasing performance.

But some applications do need to capture every keystroke in the
application state. For this purpose, Quiescent provides *unmanaged*
versions of the `input` and `textarea` components, found in the
`quiescent.dom.uncontrolled` namespace. These unmanaged components
have the same API as their managed counterparts, and will still
re-render whenever thier `value` property is changed. However, they
also allow users to type freely and will not constantly reset their
DOM value back to the `value` property after it has been edited by the
user. If the `value` property is set to the same value as the DOM
value, the DOM value is not updated, avoiding a cursor jump.

This means that they can be used exactly as expected in a Quiescent
application, with their `onChange` constantly updating the application
state, and the application state re-rendering at some future point,
without any cursor jumping. Note that if you set a `value` property to
something different than the current DOM value, the cursor will still
jump to the end of the input when the new value is applied.

See the `examples/uncontrolled-inputs` directory for a working example.

### Accessing the underlying component

Very occasionally, you might need to access the actual ReactJS object
from within a render function or a lifecycle callback function.

To allow this, Quiescent binds the `quiescent.core/*component*`
dynamic var when invoking render or DOM callback functions. The value
is the underlying React Element object itself, the same that would be
bound as `this` in the body of a vanilla ReactJS lifecycle method
written in JavaScript.

### Creating your own ReactJS components

If none of these techniques are sufficient for your use case,
Quiescent does not prevent you from manually defining and using your
own ReactJS component by other means, either through JavaScript
interop or a different ReactJS wrapper library.

This grants ubiquitous access to the full power of ReactJS, if you
need it. However, you will be responsible for defining the full
component implementation and lifecycle methods yourself, the same as
if you were writing raw ReactJS.
