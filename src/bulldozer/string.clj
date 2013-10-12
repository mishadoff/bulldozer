(ns bulldozer.string
  (:require [clojure.string :as s]))

(defn strint [s p]
  "Interpolate string with parameters.
   (strint 'http://{:s}:{:p}' {:s 'go.com' :p 123})"
  (loop [[[a b :as x] & as] (seq p) rs s]
    (if x 
      (recur as (s/replace rs (str "{" a "}") (str b))) rs)))
