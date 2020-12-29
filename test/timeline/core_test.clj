(ns timeline.core-test
  (:require [clojure.test :as t :refer [deftest testing is run-tests]]))

; TODO: Instead of copying, import this from core.cljs
(defn contains-multiple? [m keys] (every? #(contains? m %) keys))

; TODO: Instead of copying, import this from core.cljs
(defn is-year-map? [y]
  (and
   (contains-multiple? y [:id :year-number])
   (string? (:id y))
   (number? (:year-number y))))

(deftest contains-multiple?-test
  (is (contains-multiple? {:a 1 :b 2} [:a]))
  (is (contains-multiple? {:a 1 :b 2} [:a :b]))
  (is (not (contains-multiple? {:a 1 :b 2} [:a :b :c]))))

(run-tests)
(print 123)