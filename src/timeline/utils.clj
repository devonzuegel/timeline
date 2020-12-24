(ns timeline.utils
  (:require [clojure.java.io :as io]))

(defmacro inline-resource [resource-path]
  (slurp (clojure.java.io/resource resource-path)))
  ; Given a pointer, slurp reads the contents of the file that pointer is
  ; pointing to.
  ;
  ; Slurp is a convenience function that given anything that might point to a
  ; string will try to find a string. You can give it a file, a resource, a
  ; URL... maybe one day someone will implement an S3.
  ; (slurp "https://google.com"))

; You can think of the variable as quoted (i.e. 'variable). Another way to put
; it is that you're operating on the word "elephant", whereas during runtime
; you're operating on an actual elephant.
(defmacro babys-first-macro [variable]
  (if (symbol? variable) ; Check that it's a var, not a primitive
    `(print ~(str variable ":") ~variable) ; name: value
    `(println ~variable)))