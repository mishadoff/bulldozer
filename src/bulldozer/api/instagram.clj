(ns bulldozer.api.instagram
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]))

(def ^:private *CLIENT_ID*
  "5271fc9c9fc24d9c9eb370d652ec04a4")

(def ^:private POPULAR_ENDPOINT
  "https://api.instagram.com/v1/media/popular")
(def ^:private TAG_ENDPOINT
  "https://api.instagram.com/v1/tags/search")
(def ^:private SEARCH_ENDPOINT
  "https://api.instagram.com/v1/tags/%s/media/recent")

(defn- validate-tag
  "Validate tag to contains only letters,
numbers and underscore"
  [tag]
  (when-not (re-matches #"[\p{L}_0-9]+" tag)
    (throw (IllegalArgumentException. "Invalid TAG"))))

(defn- recent-endpoint [tag]
  (validate-tag tag)
  (format SEARCH_ENDPOINT tag))

(defn- instagram-page-processor [response]
  (->> response
       :body
       (#(json/read-str % :key-fn keyword))
       (#(vec [(:data %)
               (get-in % [:pagination :next_url])]))))

(defn- get-raw-images
  "Return a pair [data next-url]"
  ([tag url]
     (let [real-url (if url url (recent-endpoint tag))]
       (->> (http/get real-url
                      {:query-params {"client_id" *CLIENT_ID*}})
            instagram-page-processor)))
  ([url]
     (let [real-url (if url url POPULAR_ENDPOINT)]
       (->> (http/get real-url
                      {:query-params {"client_id" *CLIENT_ID*}})
            instagram-page-processor))))
  
;; CACHE ;;

(def ^:private query-cache (atom {}))
(def ^:private random-cache (cache/create-cache
                             #(get-raw-images %)
                             :next-url true))

(defn invalidate-cache
  ([]
     (reset! random-cache {})
     (reset! query-cache {}))
  ([query] (swap! query-cache dissoc query)))

;;;;;;;;;;;;

(defn get-raw-image
  "Obtain one random image by specified keyword.
Return results according to service."
  ([] (cache/retrieve random-cache))
  ([tag]
     (let [cache (get @query-cache tag)]
       (if cache
         (cache/retrieve cache)
         ;; not found
         (let [cache (cache/create-cache
                      #(get-raw-images tag %)
                      :next-url true)]
           (swap! query-cache assoc tag cache)
           (cache/retrieve cache))))))

(defn- unify
  "Convert instagram image properties to unified image format.

Unified image format consist of following properties:
[:id :source :width :height :link :title
 :preview-link :preview-height :preview-width] 
"
  [{:keys [id images] :as bing-image}]
  (when bing-image
    (let [preview (:thumbnail images)
          image (:standard_resolution images)]
      {:id id :source :instagram
       :link (:url image)
       :preview-link (:url preview) 
       :width (:width image)
       :preview-width (:width preview)
       :height (:height image)
       :preview-height (:height preview)
       :title (get-in bing-image [:caption :text])})))

(defn get-image
  "Return one unified image"
  ([] (unify (get-raw-image)))
  ([tag] (unify (get-raw-image tag))))

(defn- get-tags
  "Return tags similar to query"
  [query]
  (->> (http/get TAG_ENDPOINT
                 {:query-params {"q" query
                                 "client_id" *CLIENT_ID*}})
       :body
       (#(json/read-str % :key-fn keyword))
       :data
       (map :name)))

(defn quota
  "Check remaining quota. Consumes request"
  []
  (->> (http/get TAG_ENDPOINT
                 {:query-params {"q" "snow"
                                 "client_id" *CLIENT_ID*}})
       :headers
       (#(get % "x-ratelimit-remaining"))
       (Integer/parseInt)))
