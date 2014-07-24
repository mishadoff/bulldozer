(ns bulldozer.utils
  (:require [clojure.data.codec.base64 :as b64]))

(defn s->b64 [original]
  (->> (.getBytes original)
       (b64/encode)
       (#(String. % "UTF-8"))))

;; Integers

(defn safe-parse-int [s]
  (if (empty? s) nil (Integer/parseInt s)))

;; Generators

(defn gen-file-name
  "Generate pseudo-random, sortable filename. Use object."
  [object]
  (str (System/nanoTime) "_" (Math/abs (hash object))))

;; File operations

(defn fetch-to-file
  "Retrieve image from url to local file"
  [url file]
  (with-open [in (clojure.java.io/input-stream url) 
              out (clojure.java.io/output-stream file)]
    (clojure.java.io/copy in out)))

(defn fetch-to-folder
  "Use provided fetch function <fetch-fn >sequentially obtain function and save it to the folder provided by <path> param. Limit param specifies the number of images retrieved.
Specified folder must exist and must not end with / 
"
  [fetch-fn path limit]
  (doseq [i (range limit)]
    (let [image (fetch-fn)
          gen-name (gen-file-name (:link image))
          file-path (str path "/" gen-name ".jpg")]
      (fetch-to-file (:link image) file-path))))

;; Images

(defn scaled-size [width height max]
  "Scale size <width> x <height> to smaller
with a maximum side of <max>"
  (cond
   (and (<= width max) (<= height max))
   [width height]
   (>= width height)
   [max (Math/round (/ height (double (/ width max))))]
   :else
   [(Math/round (/ width (double (/ height max)))) max]))
