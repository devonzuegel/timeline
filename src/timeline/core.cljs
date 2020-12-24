(ns timeline.core
  (:require [rum.core :as rum])
  (:require-macros [timeline.utils :refer [inline-resource]]))

(def example-text (inline-resource "hopl-clojure.html"))

(println)
(enable-console-print!)

; (def inline-date-tags (array-seq (.getElementsByClassName js/document "timeline-item")))
; (def years (for [d inline-date-tags] (int (.-innerText d)))) ; TODO: Put back
(def years [2000 2001 2003 2004 2010])
(def min-year (apply min years))
(def max-year (apply max years))

(defn get-percent [y] (* 100  (divide (- y min-year) (- max-year min-year))))
(defn get-adjusted-percent [y] (+ 2 (* (get-percent y) .9))) ; Ajust so the timeline doesn't take up the entire screen
(print "min-year:" min-year)
(print "min-year:" min-year)


(defn render-year [y]
  [:span
   {:className "point"
    :style {:left (str (get-adjusted-percent y) "vw")}}
   y])

(rum/defc hello-world < rum/reactive
  ([]
   [:div
    [:div {:class "timeline"}
    ;  [:span {:class "point"} min-year]
     (map render-year years)
    ;  [:span {:class "point"} max-year]
     ]
    ; [:button {:onClick #(js/alert "hello")}  "Click me"]
    [:div {:dangerouslySetInnerHTML {:__html example-text}}]]))

;; Here's how you use JS's dot operator
(rum/mount (hello-world) (. js/document (getElementById "app")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Tests
(if (not (and
          (= 100 (get-percent 2010))
          (= 92 (get-adjusted-percent 2010)) ; = (100 * .9) + 2
          (= 11 (get-adjusted-percent 2001)) ; = (10 * .9) + 2
          (= 2 (get-adjusted-percent 2000))) ; = (0 * .9) + 2
) ; Add new tests here
  (js/alert "Tests are failing!"))
