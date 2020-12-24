(ns timeline.utils)

(defn rand-str [len]
  (apply str (take len (repeatedly #(char (+ (rand 26) 65))))))
