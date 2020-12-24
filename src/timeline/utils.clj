(ns timeline.utils
  (:require [clojure.java.io :as io]))

(defmacro inline-resource [resource-path]
  (slurp (clojure.java.io/resource resource-path)))

(defmacro babys-first-macro [variable]
  (if (symbol? variable) ; Check that it's a var, not a primitive
    `(print ~(str variable ":") ~variable)
    `(println ~variable)))

; Desired behavior:
;
; (babys-first-macro xyz) ; xyz = 123
; => print: "xyz: 123"
;
; (babys-first-macro 123)
; => print: "123"