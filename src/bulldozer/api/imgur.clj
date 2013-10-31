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

(def random-cache (agent (clojure.lang.PersistentQueue/EMPTY)))
(def search-cache (ref {}))

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

(defn- retrieve-from-cache [cache]
  (let [e (first @cache)]
    (send cache pop)
    e))

;; Part of public API

(defn get-image
  ([]
     (cond (empty? @random-cache) ;; perform initial caching
           (do
             (send random-cache #(apply conj % (:data (random-images)))) ;; async cache loading
             (await random-cache)
             (retrieve-from-cache random-cache))
           (< (count @random-cache) 20) ;; cache is close to be exhausted
           (do
             (send random-cache #(apply conj % (:data (random-images))))
             (retrieve-from-cache random-cache))
           :else (retrieve-from-cache random-cache)))
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

;; Clean Cache will be available later under another package

