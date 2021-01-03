(ns timeline.utils
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]))

(defmacro inline-resource [resource-path]
  (slurp (clojure.java.io/resource resource-path)))
  ; Given a pointer, slurp reads the contents of the file that pointer is
  ; pointing to.
  ;
  ; Slurp is a convenience function that given anything that might point to a
  ; string will try to find a string. You can give it a file, a resource, a
  ; URL... maybe one day someone will implement an S3.
  ; (slurp "https://google.com")

(defmacro inline-json-file-as-edn [resource-path]
  ; - This needs to be a macro because it needs access to the .json file
  ; - EDN = Clojure data structures
  (let [json-str (slurp (clojure.java.io/resource resource-path))]
    (json/read-str json-str :key-fn keyword)))


; You can think of the variable as quoted (i.e. 'variable). Another way to put
; it is that you're operating on the word "elephant", whereas during runtime
; you're operating on an actual elephant.
(defmacro babys-first-macro [variable]
  (if (symbol? variable) ; Check that it's a var, not a primitive
    `(print ~(str variable ":") ~variable) ; name: value
    `(println ~variable)))

; Notes for using the repl as a feedback loop
;
; ctrl+c ctrl+k => reload file into repl
; alt+shift+up  => get previous line
;
; Trick: When building up a value in the repl, put a little - in front to
; indicate that you shouldn't use that value because you built it up in repl.

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

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

