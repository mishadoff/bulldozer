(ns bulldozer.api.imgur
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.string :as s]))

;; TODO NOT SYNC
;; TODO Failsafe implementation
;; TODO Avoid repetitions
;; TODO skip albums
;; TODO increase search cache
;; TODO search random

(def RANDOM_ENDPOINT "https://api.imgur.com/3/gallery/random/random/")
(def SEARCH_ENDPOINT "https://api.imgur.com/3/gallery/search/time/0?")
(def CLIENT_ID "fb3657d87b4a7c1")
(def AUTH_HEADER {:headers {"Authorization" 
                            (str "Client-ID " CLIENT_ID)}})

(def random-cache (atom []))
(def search-cache (atom {}))

(defn- random-images []
  "Returns one page of random images."
  (:data
   (json/read-str (:body (http/get RANDOM_ENDPOINT AUTH_HEADER))
                  :key-fn keyword)))

(defn- search-images [query]
  "Return one page of images matches the query."
  (:data
   (json/read-str (:body (http/get SEARCH_ENDPOINT 
                                   (assoc AUTH_HEADER 
                                     :query-params {"q" query})))
                  :key-fn keyword)))


;; Part of public API

(defn get-image
  ([]
     (if (empty? @random-cache)
       (reset! random-cache (random-images)))
     (let [e (last @random-cache)]
       (swap! random-cache pop) e))
  ([query]
     (if (empty? (get @search-cache query []))
       (swap! search-cache assoc query (search-images query)))
     (let [res (get @search-cache query [])]
       (if (empty? res) "NO IMAGE" ;; handle later
           (let [e (last res) popped (pop res)]
             (swap! search-cache assoc query popped) e)))))

(defn link-medium [link]
  (clojure.string/replace link #"(.png|.jpg|.gif)" "m$1"))
