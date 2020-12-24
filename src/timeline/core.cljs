(ns timeline.core
  (:require [rum.core :as rum])
  (:require-macros [timeline.utils :refer [inline-resource]]))

(def example-text (inline-resource "hopl-clojure.html"))

(println)
(enable-console-print!)

(def inline-date-tags (array-seq (.getElementsByClassName js/document "timeline-item")))
(def years (for [d inline-date-tags] (int (.-innerText d))))
(def min-year (apply min years))
(def max-year (apply max years))

; TODO: Extract this into a loop and repeat across all dates
(def y 2004)
(def percent (* 100 (divide (- max-year y) (- max-year min-year))))
(def adjusted-percent (* percent .9)) ; Ajust so the timeline doesn't take up the entire screen
(print "min-year:" min-year)
(print "min-year:" min-year)
(print "percent:" percent)
; (print "margin:" margin)

(rum/defc hello-world < rum/reactive
  ([]
   [:div
    [:div {:class "timeline"}
     [:span {:class "point"} min-year]
     [:span {:class "point" :style {:left (str adjusted-percent "%")}} y]
     [:span {:class "point"} max-year]
     ]
    [:div {:dangerouslySetInnerHTML {:__html example-text}}]]))

;; Here's how you use JS's dot operator
(rum/mount (hello-world) (. js/document (getElementById "app")))