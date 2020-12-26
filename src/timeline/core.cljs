(ns timeline.core
  (:require
   [rum.core :as rum]
   [cljs.pprint :as pp]
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

(defn -update-years [current-app-state years]
  (assoc-in current-app-state [:years] years)) ;; No need to deref it

(defn get-date-from-inline-date-tag [date-tag]
  (int (.-innerText date-tag)))

(defn get-id-from-inline-date-tag [date-tag]
  (str (.-id date-tag)))

(defn get-percent [y -min-year -max-year]
  (* 100  (divide (- y -min-year) (- -max-year -min-year))))

; Ajust so the timeline only takes 90% of the screen
(defn get-adjusted-percent [y -min-year -max-year]
  (+ 2 (* (get-percent y -min-year -max-year) .9)))

(defn -update-selected-date [current-app-state new-date]
  (assoc-in current-app-state [:selected-date] new-date)) ;; No need to deref it

(defn update-selected-date [e] (swap! app-state -update-selected-date "foooo"))

(defn click-event [date]
  (fn [e]
    (println "Date clicked:" date)
    (swap! app-state -update-selected-date date)))

(defn contains-multiple? [m keys]
  (every? #(contains? m %) keys))

(defn is-year-map? [y]
  (and
   (contains-multiple? y [:id :year-number])
   (string? (:id y))
   (number? (:year-number y))))

(defn render-year-fn [years] ; Curry the function based on entire range of years
  {:pre [(every? is-year-map? years)]}
  (fn [i year]
    (let [min-year (apply min (map :year-number years))
          max-year (apply max (map :year-number years))
          ; TODO: Calculate the min & max outside of fn for better performance
          year-id (:id year)
          year-number (:year-number year)
          point-id (str year-id "--point")]
      [:span
       {:class "point" ; TODO: Consider using flexbox instead
        :key point-id
        :id point-id
        :on-click (click-event year-id)
        :style {:left (str (get-adjusted-percent year-number min-year max-year) "vw")}}
       year-number])))

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
         years (:years state)]
     [:div
      [:div {:class "timeline"} (map-indexed (render-year-fn years) years)]
      [:div {:class "spacer"}]
      [:div {:class "wrapper"} ; :on-click update-selected-date }
       [:pre (with-out-str (pp/pprint state))]
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
          (= 2 (get-adjusted-percent 2000 2000 2010)) ; = (0 * .9) + 2
          (= true (contains-multiple? {:a 1 :b 2} [:a]))
          (= true (contains-multiple? {:a 1 :b 2} [:a :b]))
          (= false (contains-multiple? {:a 1 :b 2} [:a :b :c]))))
  (js/alert "Tests are failing!"))

(comment
  ; Use this as a playground for fiddling with the Paredit commands
  (+ (+ 1) 2 3)

  ; Tip: Think about moving the parens with paredit, not the values in the expression
  ;
  ; Barf Sexp Forward = Move right parens to the left
  ; Slurp Sexp Forward = Move right parens to the right
  ; Select Forward Sexp = Select current S-expression forwards
  ; Select Backward Sexp = Select current S-expression backwards
  )

