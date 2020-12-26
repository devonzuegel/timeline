(ns timeline.core
  (:require
   [rum.core :as rum]
   [timeline.utils :as utils])
  (:require-macros
   [timeline.utils :refer [inline-resource babys-first-macro]]))

(def example-text (inline-resource "hopl-clojure.html"))

(println "--------------------------------------------------------------------")
(enable-console-print!)

;; define your app data so that doesn't get over-written on reload
(defonce app-state
  (atom {:selected-date nil
         :years nil}))

(defn get-percent [y -min-year -max-year]
  (* 100  (divide (- y -min-year) (- -max-year -min-year))))

; Ajust so the timeline only takes 90% of the screen
(defn get-adjusted-percent [y -min-year -max-year]
  (+ 2 (* (get-percent y -min-year -max-year) .9)))

(defn render-year [years] ; Curry the function based on entire range of years
  (fn [i year]
    (let [min-year (apply min years)
          max-year (apply max years)]
      [:span
       {:class "point" ; TODO: Consider using flexbox instead
        :key (str i (utils/rand-str 3))
        :style {:left (str (get-adjusted-percent year min-year max-year) "vw")}}
       year])))

(defn -update-years [current-app-state years]
  (assoc-in current-app-state [:years] years)) ;; No need to deref it


(defn -update-selected-date [current-app-state new-date]
  (assoc-in current-app-state [:selected-date] new-date)) ;; No need to deref it

(defn update-selected-date [e] (swap! app-state -update-selected-date "foooo"))

(defn click-event [date]
  (fn [e]
    (println "Date clicked:" date)
    (swap! app-state -update-selected-date date)))

(defn get-date-from-inline-date-tag [date-tag]
  (int (.-innerText date-tag)))

(defn get-id-from-inline-date-tag [date-tag]
  (str (.-id date-tag)))

(defn fetch-years [] ; Build up `years` variable and put it in the atom
  (let [inline-date-tags (array-seq (.getElementsByClassName js/document "timeline-item"))]
    (let [years (for [d inline-date-tags]
                  {:id (get-id-from-inline-date-tag d)
                   :year-number (get-date-from-inline-date-tag d)})]
      (swap! app-state -update-years years))
    (doseq [d inline-date-tags]
      (.addEventListener d "click" (click-event (get-id-from-inline-date-tag d)) false))))


(rum/defc hello-world <
  rum/reactive {:did-mount fetch-years}
  ([]
   (let [state (rum/react app-state) ; * Comment below
         years (map :year-number (:years state))]
     [:div
      [:div {:class "timeline"} (map-indexed (render-year years) years)]
      [:div {:class "spacer"}]
      [:div {:class "wrapper"} ; :on-click update-selected-date }
       [:pre "app-state: " (pr-str state)]
       [:div {:class "html-text" :dangerouslySetInnerHTML {:__html example-text}}]]
      [:div {:class "spacer"}]])))

; Here's how you use JS's dot operator
(rum/mount (hello-world) (. js/document (getElementById "app")))

(comment ; A place to store useful tidbits
  (println "mounted!" (js/Date))

  ; Fetch ids
  (print (for [d inline-date-tags]
           [(get-date-from-inline-date-tag d)
            (.-id d)])))

; * Register a listener to tell this component to react to the state.
;   - `app-state` is the reference to the atom
;   - `state` is the value, which gets refreshed each time the atom is updated
;      (but is not actually the source of truth reference itself)

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
