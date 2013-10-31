(ns bulldozer.api.imgur
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]))

;; TODO NOT SYNCRONIZED // Not critical
;; TODO ASYNC UPDATE // Not critical
;; TODO Failsafe implementation // Critical
;; TODO search random ??? // Not critical
;; TODO increase search cache // Not critical
;; TODO How to apply paging?

(def RANDOM_ENDPOINT "https://api.imgur.com/3/gallery/random/random/")
(def SEARCH_ENDPOINT "https://api.imgur.com/3/gallery/search/time/0?")
(def CLIENT_ID "fb3657d87b4a7c1")
(def AUTH_HEADER {:headers {"Authorization" 
                            (str "Client-ID " CLIENT_ID)}})

(def random-cache (atom []))
(def search-cache (atom {}))

(defn- imgur-image-page-processor [response]
  (->> response
       :body
       (#(json/read-str % :key-fn keyword))
       :data
       (filterv (complement :is_album))))

(defn- random-images []
  "Returns one page of random images."
  (->> (http/get RANDOM_ENDPOINT AUTH_HEADER)
       imgur-image-page-processor))

(defn- search-images [query]
  "Return one page of images matches the query."
  (->> (http/get SEARCH_ENDPOINT (assoc AUTH_HEADER
                                   :query-params {"q" query}))
       imgur-image-page-processor))

(defn quota []
  "Returns remaining quota"
  (->> (http/get "https://api.imgur.com/3/credits" AUTH_HEADER)
       :body
       (#(json/read-str % :key-fn keyword))
       :data
       ))

;; Part of public API

(defn get-image
  ([]
     (cond (empty? @random-cache)
           (reset! random-cache (random-images)))
           ;; async case
     (let [e (last @random-cache)]
       (swap! random-cache pop) e))
  ([query]
     (if (empty? (get @search-cache query []))
       (swap! search-cache assoc query (search-images query)))
     (let [res (get @search-cache query [])]
       (if (empty? res) "NO IMAGE" ;; handle later
           (let [e (last res) popped (pop res)]
             (swap! search-cache assoc query popped) e)))))

(defn link-scale [link size]
  (let [k (get (zipmap [:s :b :t :m :l :h] "sbtmlh") size "")]
    (clojure.string/replace link #"(?i)(.png|.jpg|.gif)$" (str k "$1"))))

;; Cache management

(defn clean-random-cache []
  (reset! random-cache []))

(defn clean-search-cache 
  ([] (reset! search-cache {}))
  ([key] (swap! search-cache dissoc key))) 
