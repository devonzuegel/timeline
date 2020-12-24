(ns timeline.core
  (:require [rum.core :as rum])
  (:require-macros [timeline.utils :refer [inline-resource]]))

(def example-text (inline-resource "hopl-clojure.html"))

(println)
(enable-console-print!)

; (def inline-date-tags (array-seq (.getElementsByClassName js/document "timeline-item")))
; (def years (for [d inline-date-tags] (int (.-innerText d)))) ; TODO: Put back
(def years [2000 2001 2003 2004 2010])

(defn get-percent [y -min-year -max-year]
  (* 100  (divide (- y -min-year) (- -max-year -min-year))))

; Ajust so the timeline only takes 90% of the screen
(defn get-adjusted-percent [y -min-year -max-year]
  (+ 2 (* (get-percent y -min-year -max-year) .9)))

(defn render-year [y]
  (let [min-year (apply min years)
        max-year (apply max years)]
    [:span
     {:className "point"
      :style {:left (str (get-adjusted-percent y min-year max-year) "vw")}}
     y]))

(rum/defc hello-world < rum/reactive
  ([]
   [:div
    [:div {:class "timeline"} (map render-year years)]
    ; [:button {:onClick #(js/alert "hello")}  "Click me"]
    [:div {:dangerouslySetInnerHTML {:__html example-text}}]]))

;; Here's how you use JS's dot operator
(rum/mount (hello-world) (. js/document (getElementById "app")))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Tests
(if (not (and
          (= 100 (get-percent 2010 2000 2010))
          (= 92 (get-adjusted-percent 2010 2000 2010)) ; = (100 * .9) + 2
          (= 11 (get-adjusted-percent 2001 2000 2010)) ; = (10 * .9) + 2
          (= 2 (get-adjusted-percent 2000 2000 2010))) ; = (0 * .9) + 2
) ; Add new tests here
  (js/alert "Tests are failing!"))
