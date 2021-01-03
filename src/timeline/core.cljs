(ns timeline.core
  (:require
   [rum.core :as rum]
   [cljs.pprint :as pp]
   [goog.dom :as dom]
   [timeline.utils :as utils]
   [goog.dom.classlist :as classlist]
   [goog.fx.dom :as fx-dom])
  (:require-macros
   [timeline.utils :refer
    [inline-resource babys-first-macro inline-json-file-as-edn]]))

(def example-text (inline-resource "hopl-clojure.html"))

(def example-annotations (inline-json-file-as-edn "example-annotations.json"))

(enable-console-print!)

;; define your app data so that doesn't get over-written on reload
(defonce app-state
  (atom {:selected-year-id nil
         :hovered-year-id nil
         :years nil}))

(defn -update-years [current-app-state years]
  (assoc current-app-state :years years)) ;; No need to deref it

(defn get-year-from-inline-year-tag [date-tag]
  (int (.-innerText date-tag)))

(defn get-id-from-inline-year-tag [date-tag]
  (str (.-id date-tag)))

(defn get-percent [y min-year max-year]
  (* 100  (divide (- y min-year) (- max-year min-year))))

; Ajust so the timeline only takes 90% of the screen
(defn get-adjusted-percent [y min-year max-year]
  (+ 2 (* (get-percent y min-year max-year) .9)))

(defn -update-selected-year-id [current-app-state year-id]
  ;; No need to deref it
  (assoc current-app-state :selected-year-id year-id))

(defn update-selected-year-id [year-id]
  (swap! app-state -update-selected-year-id year-id))

(defn -update-hovered-year-id [current-app-state year-id]
  (assoc current-app-state :hovered-year-id year-id))

(defn update-hovered-year-id [year-id]
  (swap! app-state -update-hovered-year-id year-id))

(defn get-scroll-top "Current scroll position" [] (.-y (dom/getDocumentScroll)))

(defn get-viewport-height []
  (let [width-and-height (js->clj (.values js/Object (dom/getViewportSize)))
        height (second width-and-height)]
    height))

(defn before-or-after-viewport [element-id]
  (let [element-offset (.-offsetTop (.getElementById js/document element-id))]
    (cond
      (< element-offset (get-scroll-top)) :before-viewport
      (< element-offset (+ (get-scroll-top) (get-viewport-height))) :in-viewport
      :else :after-viewport)))

(defn click-year [year-id {:keys [scroll-on-click?]}]
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
        (when scroll-on-click?
          (let [top-offset (- (.-offsetTop new-selection) 64)]
            ; TODO: Only scroll when the inline element is off screen
            (.play (fx-dom/Scroll.
                    (dom/getDocumentScrollElement)
                    #js [0 (get-scroll-top)]
                    #js [0 top-offset]
                    150))))))
    (update-selected-year-id year-id)))

(defn contains-multiple? [m keys] (every? #(contains? m %) keys))

(defn is-year-map? [y]
  (and
   (contains-multiple? y [:id :year-number])
   (string? (:id y))
   (number? (:year-number y))))

(defn render-year-fn [years selected-year-id] ; Curry the function based on entire range of years
  {:pre [(every? is-year-map? years)]}
  (fn [i year]
    (let [min-year (:year-number (first years))
          max-year (:year-number (last years))
          year-id (:id year)
          year-number (:year-number year)
          point-id (str year-id "--point")]
      [:span
       {:class ["point" (when (= year-id selected-year-id) "selected")] ; TODO: Consider using flexbox instead
        :key point-id
        :id point-id
        :on-click (click-year year-id {:scroll-on-click? true})
        :on-mouse-over #(update-hovered-year-id year-id)
        :style {:left (str (get-percent year-number min-year (+ 1 max-year)) "vw")}}

       ; TODO: Clean up this logic
       ; TODO: Handle the onclick
       (when (= year-id selected-year-id)
         [:div {:class "pulsating-dot"}
          [:div {:class "dot"}]
          [:div {:class "pulse"}]])])))

(defn render-timeline-background [years]
  (let [min-year (:year-number (first years))
        max-year (:year-number (last years))]
    [:div {:class "timeline-background"}
     (map (fn [x] [:span x])
          (range min-year (+ 1 max-year)))]))

(defn animated-inline-year [original-year-tag]
  (let [new-animated-year-tag (.createElement js/document "div")]
    (set! (.-id new-animated-year-tag))
    (classlist/add new-animated-year-tag "animated-link--wrapper")
    (classlist/add original-year-tag "animated-link--link")
    (dom/insertSiblingAfter new-animated-year-tag original-year-tag)
    ; Wrap the original node so that the border is animated
    (let [removed (dom/removeNode original-year-tag)]
      (dom/appendChild new-animated-year-tag removed)
      (let [animated-link--border (.createElement js/document "span")]
        (dom/appendChild new-animated-year-tag animated-link--border)
        (classlist/add animated-link--border "animated-link--border")))
    new-animated-year-tag))

(defn sort-years [inline-year-tags]
  (sort-by :year-number
           (for [year-tag inline-year-tags]
             {:id (get-id-from-inline-year-tag year-tag)
              :year-number (get-year-from-inline-year-tag year-tag)})))

(defn initialize-years [] ; Build up `years` variable and put it in the atom
  (let [inline-year-tags (array-seq (.getElementsByClassName js/document "timeline-item"))]
    ; Add `years` to the app state
    (swap! app-state -update-years (sort-years inline-year-tags))
    ; Initialize each inline date tag
    (doseq [original-year-tag inline-year-tags]
      ; Add on-click callback
      (.addEventListener
       original-year-tag "click"
       (click-year (get-id-from-inline-year-tag original-year-tag) {:scroll-on-click? false})
       false)
      ; Add border animation
      (animated-inline-year original-year-tag))))

(defn hovered-year-relative-to-viewport [state]
  (let [year-id (:hovered-year-id state)]
    (babys-first-macro year-id)
    (if (nil? year-id)
      nil ; Nothing is hovered
      (before-or-after-viewport year-id))))

(rum/defc hello-world <
  rum/reactive {:did-mount initialize-years}
  ([]
   (let [state (rum/react app-state) ; * Comment below
         years (:years state)]
     [:div
      [:div {:class "timeline"}
       (render-timeline-background years)
       (map-indexed (render-year-fn years (:selected-year-id state)) years)]

      ; TODO: Show this only when hovering over a timeline dot
      ; TODO: Handle the up case (right now just handling the down)
      [:div {:class "wrapper"}
       (let [relative-to-viewport (hovered-year-relative-to-viewport state)
             arrow (case relative-to-viewport
                     :before-viewport "↑"
                     :after-viewport "↓"
                     nil)]
         (babys-first-macro relative-to-viewport)
         (babys-first-macro arrow)
         [:div {:class "container"} [:div {:class "arrow bounce"} arrow]])
       [:div {:class "spacer"}]
      ;;  [:pre (with-out-str (pp/pprint example-annotations))]
       [:pre (with-out-str (pp/pprint state))]
       [:div {:class "html-text" :dangerouslySetInnerHTML {:__html example-text}}]]
      [:div {:class "spacer"}]])))

(rum/mount (hello-world) (. js/document (getElementById "app")))

; Footnotes:
; ----------
; * Register a listener to tell this component to react to the state.
;   - `app-state` is the reference to the atom
;   - `state` is the value, which gets refreshed each time the atom is updated
;      (but is not actually the source of truth reference itself)

; TODO: Decide what to render in the case where a single date occurs >1x
; (e.g. in the HOPL example)
