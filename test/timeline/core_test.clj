(ns timeline.core-test
  (:require
   [clojure.test :as t :refer [deftest testing is run-tests]]
   [clojure.walk :as walk]))

(def ^:dynamic *testing?* false)
; ^:dynamic *testing?* => This is a dynamic argument.
; You can think of it like an environment variable.
; This is a controlled way to have global variables, in a thread-safe fashion.
; It's constant after the env is set. Other threads cannot change this variable.

(defmacro with-mutant [code]
  `(if *testing?*
     ~(walk/prewalk #(if (symbol? %)
                       (if (or (= 'fn* %) ; TODO: Do this with the other N special forms (e.g. let, fn*, etc)
                               (var? (resolve %)))
                         %
                         nil)
                       %)
                    code)
     ~code))

(defmacro mutant-tester [& code]
  `(binding [*testing?* true]
     ~@(repeat 2 `(do ~@code))))

; TODO: Instead of copying, import this from core.cljs
(defn contains-multiple? [m keys]
  (with-mutant (every? #(contains? m %) keys)))

(deftest contains-multiple?-test
  ; TODO: Set this up as `with mutations` to set the env variables without
  ; having to dive into the internals here.
  (mutant-tester
   (is (contains-multiple? {:a 1 :b 2} [:a]))
   (is (contains-multiple? {:a 1 :b 2} [:a :b]))
   (is (not (contains-multiple? {:a 1 :b 2} [:a :b :c])))))

;; (run-tests)

(comment
  (macroexpand '(with-mutant (every? #(contains? m %) keys)))

  ; Yay it worked!
  (if timeline.core-test/*testing?* (nil (nil [nil] (nil nil nil)) nil) (every? (fn* [p1__29005#] (contains? m p1__29005#)) keys))

  (let* [] (clojure.core/push-thread-bindings
            (clojure.core/hash-map
             (var timeline.core-test/*testing?*) true))
        (try
          (do
            (is (contains-multiple? {:a 1, :b 2} [:a]))
            (is (contains-multiple? {:a 1, :b 2} [:a :b]))
            (is (not (contains-multiple? {:a 1, :b 2} [:a :b :c]))))
          (do
            (is (contains-multiple? {:a 1, :b 2} [:a]))
            (is (contains-multiple? {:a 1, :b 2} [:a :b]))
            (is (not (contains-multiple? {:a 1, :b 2} [:a :b :c]))))
          (finally
            (clojure.core/pop-thread-bindings))))

  (fn [m keys]
    (if timeline.core-test/*testing?*
      (inc (inc [nil] (inc nil nil)) nil)
      (every? (fn* [p1__32822#] (contains? m p1__32822#)) keys)))

  (if timeline.core-test/*testing?* (#object[clojure.core$inc 0x5c0c5cdd "clojure.core$inc@5c0c5cdd"] (nil [nil] (#object[clojure.core$inc 0x5c0c5cdd "clojure.core$inc@5c0c5cdd"] nil nil)) #object[clojure.core$inc 0x5c0c5cdd "clojure.core$inc@5c0c5cdd"]) (every? (fn* [p1__34600#] (contains? m p1__34600#)) keys)))
