(ns timeline.core
  (:require
   [rum.core :as rum]
   [cljs.pprint :as pp]
   [goog.dom :as dom]
   [timeline.utils :as utils]
   [goog.dom.classlist :as classlist]
   [goog.fx.dom :as fx-dom])
  (:require-macros
   [timeline.utils :refer [inline-resource babys-first-macro]]))

(def example-text (inline-resource "hopl-clojure.html"))

(enable-console-print!)

;; define your app data so that doesn't get over-written on reload
(defonce app-state
  (atom {:selected-date-id nil
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

(defn -update-selected-date-id [current-app-state new-date]
  (assoc-in current-app-state [:selected-date-id] new-date)) ;; No need to deref it

(defn update-selected-date-id [e] (swap! app-state -update-selected-date-id "foooo"))

(defn get-scroll-top "Current scroll position" [] (.-y (dom/getDocumentScroll)))

(defn get-viewport-height []
  (let [width-and-height (js->clj (.values js/Object (dom/getViewportSize)))
        width (first width-and-height)
        height (second width-and-height)]
    height))

(defn before-or-after-viewport [element-id]
  (let [element-offset (.-offsetTop (.getElementById js/document element-id))]
    (cond
      (< element-offset (get-scroll-top)) :before-viewport
      (< element-offset (+ (get-scroll-top) (get-viewport-height))) :in-viewport
      :else :after-viewport)))

(defn create-button  [wrapper-id]
  ;; We can save the response of a js interop function call
  ;;=> var btn = document.createElement("button")
  (let [btn (.createElement js/document "div")]
    ;; (set! (.-innerText btn) "Click Me")
    (set! (.-id btn) wrapper-id)
    (classlist/add btn "effect-1-wrapper")
    ;; (dom/appendChild btn (.createElement js/document new-selection))
    ;; (babys-first-macro btn)
    ;; (babys-first-macro new-selection)

    ;; return btn
    btn))

(defn click-year [year-id scroll-on-click?]
  (fn [e]
    ; Remove .selected class from all elements to clean up state before
    ; making a new selection.
    ; Note: If you remove `vec`, this becomes buggy. So don't remove it. ;-)
    (let [elems-with-selected-class (vec (array-seq (.getElementsByClassName js/document "selected")))]
      (doseq [elem elems-with-selected-class]
        (classlist/remove elem "selected")))
    ; Add .selected class to new selection
    (if-let [new-selection (.getElementById js/document year-id)]
      (do
        (classlist/add new-selection "selected")
        (classlist/add new-selection "effect-1")
        (let [btn (create-button (str year-id "--wrapper"))]
          (dom/insertSiblingAfter btn new-selection)
          (let [removed (dom/removeNode new-selection)]
            (babys-first-macro btn)
            (babys-first-macro removed)
            (dom/appendChild btn removed)
            (let [focus-border (.createElement js/document "span")]
              (dom/appendChild btn focus-border)
              (classlist/add focus-border "focus-border"))))
        (when scroll-on-click?
          (let [top-offset (- (.-offsetTop new-selection) 64)]
            (.play (fx-dom/Scroll.
                    (dom/getDocumentScrollElement)
                    #js [0 (get-scroll-top)]
                    #js [0 top-offset]
                    150))))))
    (swap! app-state -update-selected-date-id year-id)))

(defn contains-multiple? [m keys] (every? #(contains? m %) keys))

(defn is-year-map? [y]
  (and
   (contains-multiple? y [:id :year-number])
   (string? (:id y))
   (number? (:year-number y))))

(defn render-year-fn [years selected-date-id] ; Curry the function based on entire range of years
  {:pre [(every? is-year-map? years)]}
  (fn [i year]
    (let [min-year (:year-number (first years))
          max-year (:year-number (last years))
          year-id (:id year)
          year-number (:year-number year)
          point-id (str year-id "--point")]
      [:span
       {:class ["point" (when (= year-id selected-date-id) "selected")] ; TODO: Consider using flexbox instead
        :key point-id
        :id point-id
        :on-click (click-year year-id true)
        :style {:left (str (get-percent year-number min-year (+ 1 max-year)) "vw")}}

       ; TODO: Clean up this logic
       ; TODO: Handle the onclick
       (when (= year-id selected-date-id)
         [:div {:class "pulsating-dot"}
          [:div {:class "dot"}]
          [:div {:class "pulse"}]])])))

(defn render-timeline-background [years]
  (let [min-year (:year-number (first years))
        max-year (:year-number (last years))]
    [:div {:class "timeline-background"}
     (map (fn [x] [:span x])
          (range min-year (+ 1 max-year)))]))

(defn fetch-years [] ; Build up `years` variable and put it in the atom
  (let [inline-date-tags (array-seq (.getElementsByClassName js/document "timeline-item"))]
    (let [years (sort-by :year-number
                         (for [d inline-date-tags]
                           {:id (get-id-from-inline-date-tag d)
                            :year-number (get-date-from-inline-date-tag d)}))]
      (swap! app-state -update-years years))
    (doseq [d inline-date-tags]
      (.addEventListener
       d "click"
       (click-year (get-id-from-inline-date-tag d) false) false))))

(rum/defc hello-world <
  rum/reactive {:did-mount fetch-years}
  ([]
   (let [state (rum/react app-state) ; * Comment below
         years (:years state)]
     [:div
      [:div {:class "timeline"}
       (render-timeline-background years)
       (map-indexed (render-year-fn years (:selected-date-id state)) years)]

      [:div {:class "spacer"}]
      [:div {:class "wrapper"}
      ;;  [:div {:class "effect-1-wrapper"}
      ;;   [:input {:class "effect-1"}]
      ;;   [:div {:class "focus-border"}]]

       [:div {:class "spacer"}]
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

; Footnotes:
; ----------
; * Register a listener to tell this component to react to the state.
;   - `app-state` is the reference to the atom
;   - `state` is the value, which gets refreshed each time the atom is updated
;      (but is not actually the source of truth reference itself)

; TODO: Decide what to render in the case where a single date occurs >1x
; (e.g. in the HOPL example)
