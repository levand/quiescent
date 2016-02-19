(ns quiescent.factory
  "Provides convenience macros for easily creating idiomatic Quiescent element factory functions
  from pre-existing React components")

(defmacro def-factories
  "Define factory functions for the given components, using the specified JS object prefix"
  [prefix & components]
  `(do
     ~@(for [c components]
         `(def ~c (quiescent.factory/factory ~(symbol (str prefix "." c)))))))