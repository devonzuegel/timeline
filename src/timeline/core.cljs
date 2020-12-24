(ns timeline.core
  (:require
   [rum.core :as rum]
   [timeline.utils :as utils])
  (:require-macros
   [timeline.utils :refer [inline-resource]]))

(def example-text (inline-resource "hopl-clojure.html"))

(println "--------------------------------------------------------------------")
(enable-console-print!)

(def inline-date-tags (array-seq (.getElementsByClassName js/document "timeline-item")))
(def years (for [d inline-date-tags] (int (.-innerText d))))

; Fetch ids
(print (for [d inline-date-tags]
         [(int (.-innerText d))
          (.-id d)]))

(defn get-percent [y -min-year -max-year]
  (* 100  (divide (- y -min-year) (- -max-year -min-year))))

; Ajust so the timeline only takes 90% of the screen
(defn get-adjusted-percent [y -min-year -max-year]
  (+ 2 (* (get-percent y -min-year -max-year) .9)))

(defn render-year [i year]
  (let [min-year (apply min years)
        max-year (apply max years)]
    [:span
     {:className "point" ; TODO: Consider using flexbox instead
      :key (str i (utils/rand-str 3))
      :style {:left (str (get-adjusted-percent year min-year max-year) "vw")}}
     year]))

(rum/defc hello-world < rum/reactive
  ([]
   [:div
    [:div {:class "timeline"} (map-indexed render-year years)]
    ; [:button {:onClick #(js/alert "hello")}  "Click me"]
    [:div {:dangerouslySetInnerHTML {:__html example-text}}]]))

;; Here's how you use JS's dot operator
(rum/mount (hello-world) (. js/document (getElementById "app")))

; TODO: Decide what to render in the case where a single date occurs >1x
; (e.g. in the HOPL example)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

; Tests
(if (not (and
          (= 100 (get-percent 2010 2000 2010))
          (= 92 (get-adjusted-percent 2010 2000 2010)) ; = (100 * .9) + 2
          (= 11 (get-adjusted-percent 2001 2000 2010)) ; = (10 * .9) + 2
          (= 2 (get-adjusted-percent 2000 2000 2010))) ; = (0 * .9) + 2
         )
  (js/alert "Tests are failing!"))
