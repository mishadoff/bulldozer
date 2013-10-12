(ns bulldozer.api.imgur
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.string :as s]
            ))

;; TODO Failsafe implementation
;; TODO different image sizes
;; TODO single/two access points
;; TODO extract to core.clj

(def RANDOM_ENDPOINT "https://api.imgur.com/3/gallery/random/random/")
(def SEARCH_ENDPOINT "https://api.imgur.com/3/gallery/search/time/0?")
(def CLIENT_ID "fb3657d87b4a7c1")
(def AUTH_HEADER {:headers {"Authorization" 
                            (str "Client-ID " CLIENT_ID)}})

;; TODO get rid of set?
(def random-cache (atom #{}))

(defn- get-random-images-page []
  (:data
   (json/read-str (:body (http/get RANDOM_ENDPOINT AUTH_HEADER))
                  :key-fn keyword)))

;; TODO
(defn- search-images [query]
  (json/read-str (:body (http/get SEARCH_ENDPOINT 
                                  (assoc AUTH_HEADER 
                                    :query-params {"q" query})))
                 :key-fn keyword))


;; Part of public API

(defn get-image
  ([]
     (do 
       (if (empty? @random-cache)
         (reset! random-cache (set (get-random-images-page))))
       (let [e (first @random-cache)]
         (swap! random-cache disj e) e)))
  ([query]))
