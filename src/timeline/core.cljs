(ns timeline.core
  (:require [rum.core :as rum]))

(enable-console-print!)

(println "This text is printed from src/timeline/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello, world.", :counter 0}))

(defn inc-counter [current-app-state] (update-in current-app-state [:counter] inc)) ;; No need to deref it

(rum/defc hello-world < rum/reactive
  []
  (let [state (rum/react app-state)] ;; Register a listener to tell this component to react to the state.
    [:div
     [:h1 (:text state)] ;; Dereference the box; "open the box and give me the value"
     [:p "Counter: " (:counter state)]
     [:button {:on-click (fn [e] (swap! app-state inc-counter)) :class "btn"} "Click me!"]]))

(rum/mount (hello-world)
           (. js/document (getElementById "app"))) ;; Here's how you use JS's dot operator