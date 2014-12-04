;; (ns bulldozer.images.instagram
;;   "Retrieve images from instagram"
;;   (:require [clj-http.client :as http]
;;             [clojure.data.json :as json]
;;             [bulldozer.cache :as cache]
;;             [bulldozer.images.keys :refer [*instagram-client-id*]]))

;; (def ^:private popular-endpoint "https://api.instagram.com/v1/media/popular")
;; (def ^:private tag-endpoint "https://api.instagram.com/v1/tags/search")
;; (def ^:private search-endpoint "https://api.instagram.com/v1/tags/%s/media/recent")

(defn- validate-tag
  "Validate tag to contains only letters, numbers and underscore.
  throws IllegalArgumentException if tag is malformed.
  Non-latin tags are handled as well."
  [tag]
  (when-not (re-matches #"[\p{L}_0-9]+" tag)
    (throw (IllegalArgumentException. "Invalid instagram tag"))))

(defn- recent-endpoint
  "Build API endpoint for recent photos by tag.
  Tag must be valid according to (validate-tag)"
  [tag]
  (validate-tag tag)
  (format search-endpoint tag))

(defn- instagram-page-processor
  "Retrieve instagram data from http response.
  Response must be a 200 OK"
  [response]
  (->> response
       :body
       (#(json/read-str % :key-fn keyword))
       (#(vec [(:data %)
               (get-in % [:pagination :next_url])]))))

(defn- get-raw-images
  "Return a pair [data next-url]
  Accept optional parameters :url and :tag"
  [& {:keys [tag url]}]
  (let [real-url (cond
                  url url
                  tag (recent-endpoint tag)
                  :else popular-endpoint)]
    (->> (http/get real-url
                   {:query-params {"client_id" *instagram-client-id*}})
         instagram-page-processor)))
  
;; CACHE ;;
(def ^:private query-cache (atom {}))
(def ^:private random-cache nil)

(defn invalidate-cache
  ([]
     (reset! random-cache {})
     (reset! query-cache {}))
  ([query] (swap! query-cache dissoc query)))

;;;;;;;;;;;;

(defn get-raw-image
  "Obtain one random image by specified keyword.
Return results according to service."
  ([] (get-raw-image :notag))
  ([tag]
     (let [cache (get @query-cache tag)]
       (if cache
         (cache/retrieve cache)
         ;; not found
         (let [cache (cache/create-cache
                      #(get-raw-images :url % :tag tag)
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

;; (defn get-image
;;   "Return one unified image"
;;   ([] (unify (get-raw-image)))
;;   ([tag] (unify (get-raw-image tag))))

;; ;; (defn- get-tags
;; ;;   "Return tags similar to query"
;; ;;   [query]
;; ;;   (->> (http/get tag-endpoint
;; ;;                  {:query-params {"q" query
;; ;;                                  "client_id" *instagram-client-id*}})
;; ;;        :body
;; ;;        (#(json/read-str % :key-fn keyword))
;; ;;        :data
;; ;;        (map :name)))

;; TODO retrieve rate-limits to pic meta
;; (defn quota
;;   "Check remaining quota. Consumes request"
;;   []
;;   (->> (http/get tag-endpoint
;;                  {:query-params {"q" "snow"
;;                                  "client_id" *instagram-client-id*}})
;;        :headers
;;        (#(get % "x-ratelimit-remaining"))
;;        (Integer/parseInt))
;;  )
