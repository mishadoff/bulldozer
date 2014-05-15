(ns bulldozer.utils
  (:require [clojure.data.codec.base64 :as b64]))



(defn s->b64 [original]
  (->> (.getBytes original)
       (b64/encode)
       (#(String. % "UTF-8"))))

;; Integers

(defn safe-parse-int [s]
  (if (empty? s) nil (Integer/parseInt s)))
