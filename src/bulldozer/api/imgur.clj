(ns bulldozer.api.imgur
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]))

(def ENDPOINT "https://api.imgur.com/3")

(def CLIENT_ID "fb3657d87b4a7c1")

(defn get-random-images-page []
  "n must be 0 to 50"
  (json/read-str 
   (:body (http/get 
           (str "https://api.imgur.com/3/gallery/random/random/") 
           {:headers {"Authorization" (str "Client-ID " CLIENT_ID)}}))))

(defn random-image [image-page]
  (rand-nth (get image-page "data")))

(defn link [image]
  (get image "link"))
