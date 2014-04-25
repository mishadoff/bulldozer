(ns bulldozer.api.imgur
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]))

(def RANDOM_ENDPOINT
  "https://api.imgur.com/3/gallery/random/random/")
(def SEARCH_ENDPOINT
  "https://api.imgur.com/3/gallery/search/time/0?")

(def CLIENT_ID "fb3657d87b4a7c1")
(def AUTH_HEADER {:headers {"Authorization" 
                            (str "Client-ID " CLIENT_ID)}})

(defmulti process-response :status)

(defmethod process-response 200 [response]
  (->> response 
       :body
       (#(json/read-str % :key-fn keyword))
       :data
       (filterv (complement :is_album))       
       (list :ok)
       (zipmap [:status :data])))

(defmethod process-response :default [response]
  {:status :error
   :message (:error response)
   :data []})

(defn- imgur-image-page-processor [response]
  (process-response response))

(defn- random-images []
  "Returns one page of random images."
  (->> (http/get RANDOM_ENDPOINT AUTH_HEADER)
       imgur-image-page-processor))

(defn- search-images [query]
  "Return one page of images matches the query."
  (->> (http/get SEARCH_ENDPOINT (assoc AUTH_HEADER
                                   :query-params {"q" query}))
       imgur-image-page-processor))

;; TODO error handling
(defn quota []
  "Returns remaining quota"
  (->> (http/get "https://api.imgur.com/3/credits" AUTH_HEADER)
       :body
       (#(json/read-str % :key-fn keyword))
       :data
       ))

(defn get-images
  ([] (:data (random-images)))
  ([query] (:data (search-images query))))

;;; CACHE ;;;

(def random-cache (cache/create-cache get-images))
(def query-cache (atom {}))

;;;;;;;;;;;;;

(defn get-image
  "Obtain one random image or random image with
specified keyword.

Simple cache is used. First time you perform request
it can take some time, because imgur return multiple images
per request. Result will be cached and perform instant
response until cache is exhausted.
"
  ([] (cache/retrieve random-cache))
  ([query]
     (let [cache (get @query-cache query)]
       (if cache
         (cache/retrieve cache)
         ;; not found
         (let [cache (cache/create-cache #(get-images query))]
           (swap! query-cache assoc query cache)
           (cache/retrieve cache))))))

;; Additional methods

(defn link-scale [link size]
  (let [k ((zipmap [:s :b :t :m :l :h] "sbtmlh") size "")]
    (clojure.string/replace link #"(?i)(.png|.jpg|.gif)$" (str k "$1"))))
