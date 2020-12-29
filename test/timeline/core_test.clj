(ns timeline.core-test
  (:require [clojure.test :as t :refer [deftest testing is run-tests]]))

(def ^:dynamic *testing?* false)
; ^:dynamic *testing?* => This is a dynamic argument.
; You can think of it like an environment variable.
; This is a controlled way to have global variables, in a thread-safe fashion.
; It's constant after the env is set. Other threads cannot change this variable.

(defmacro with-mutant [code]
  `(if *testing?*
     nil
     ~code))

; TODO: Instead of copying, import this from core.cljs
(defn contains-multiple? [m keys]
  (with-mutant (every? #(contains? m %) keys)))

(deftest contains-multiple?-test
  ; TODO: Set this up as `with mutations` to set the env variables without
  ; having to dive into the internals here.
  (binding [*testing?* true]
    (is (contains-multiple? {:a 1 :b 2} [:a]))
    (is (contains-multiple? {:a 1 :b 2} [:a :b]))
    (is (not (contains-multiple? {:a 1 :b 2} [:a :b :c])))))

(run-tests)
