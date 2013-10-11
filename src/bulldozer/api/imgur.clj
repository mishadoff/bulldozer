(ns bulldozer.api.imgur
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]))

;; TODO Cache pages
;; TODO Failsafe implementation
;; TODO different image sizes
;; TODO single/two access points
;; TODO search by string query
;; TODO escape string query

(def ENDPOINT "https://api.imgur.com/3/gallery/random/random/")
(def CLIENT_ID "fb3657d87b4a7c1")
(def AUTH_HEADER {:headers {"Authorization" 
                            (str "Client-ID " CLIENT_ID)}})

(defn- get-random-images-page []
  (json/read-str (:body (http/get ENDPOINT AUTH_HEADER))
                 :key-fn keyword))

(defn- search-images [query]
  (json/read-str (:body (http/get (str "https://api.imgur.com/3/gallery/search/time/0?q=" query) AUTH_HEADER))
                 :key-fn keyword))

(defn random-image [image-page]
  (rand-nth (get image-page :data)))
