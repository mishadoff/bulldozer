(ns bulldozer.utils
  (:require [clojure.data.codec.base64 :as b64]))



(defn s->b64 [original]
  (->> (.getBytes original)
       (b64/encode)
       (#(String. % "UTF-8"))))

;; Integers

(defn safe-parse-int [s]
  (if (empty? s) nil (Integer/parseInt s)))


(defn fetch-to-file
  "Retrieve image from link to local file"
  [url file]
  (with-open [in (io/input-stream url) 
              out (io/output-stream file)]
    (io/copy in out)))
