(ns timeline.core
  (:require [rum.core :as rum])
  (:require-macros [timeline.utils :refer [inline-resource]]))

(def example-text (inline-resource "hopl-clojure.html"))

(println)
(enable-console-print!)

(def inline-date-tags (array-seq (.getElementsByClassName js/document "timeline-item")))
(def years (for [d inline-date-tags] (.-innerText d)))

(rum/defc hello-world < rum/reactive
  ([]
   [:div
    [:div {:dangerouslySetInnerHTML {:__html example-text}}]]))

;; Here's how you use JS's dot operator
(rum/mount (hello-world) (. js/document (getElementById "app")))