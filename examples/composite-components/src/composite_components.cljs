(ns composite-components
  (:require [quiescent.core :as q]
            [quiescent.dom :as d]
            [quiescent.factory :as f]
            [cljsjs.react-bootstrap]))

(f/def-factories js/ReactBootstrap
  Accordion Affix AffixMixin Alert BootstrapMixin Badge Button ButtonGroup ButtonToolbar
  Carousel CarouselItem Col CollapsableMixin DropdownButton DropdownMenu
  DropdownStateMixin FadeMixin Glyphicon Grid Input Interpolate Jumbotron Label
  ListGroup ListGroupItem MenuItemModal Nav Navbar NavItem ModalTrigger OverlayTrigger
  OverlayMixin PageHeader Panel PanelGroup PageItem Pager Popover ProgressBar Row
  SplitButton SubNav TabbedArea Table TabPane Tooltip Well)

(defn on-click
  []
  (js/alert "Clicked!"))

(q/defcomponent ExampleButton
  [label]
  (d/div {}
    (Button {:onClick on-click} label)))

(defn render
  []
  (q/render
    (ExampleButton "I'm a button!")
    (.getElementById js/document "content")))

(defn ^:export main
 []
  (render))
