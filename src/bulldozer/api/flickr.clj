(ns bulldozer.api.flickr
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]
            [bulldozer.utils :as u]))

;; https://www.flickr.com/services/api/misc.urls.html

(def ^:dynamic *APP_KEY*
  "29e9c67966b744e0d06e1961e1bb2dd9")

(def ^:private SERVICE_ENDPOINT
  "https://api.flickr.com/services/rest/")

(def ^:private EXTRAS
  "url_z,url_t")

(def ^:private SEARCH_MAP
  {;"method" "flickr.photos.search"
   "name" "value"
   "format" "json"
   "api_key" *APP_KEY*
;   "text" query
;   "start" page
   "extras" EXTRAS})

(defn- popstring
  "Flickr return jsonFlickrApi([json])
we need just [json]"
  [s]
  (subs s 14 (dec (count s))))

(defn- flickr-page-processor [response]
  (->> response
       :body
       popstring
       (#(json/read-str % :key-fn keyword))
       :photos
       :photo
       (filterv :url_z) ;; only images where original link available
       ))

(defn- get-images-for-page
  "return one page of flickr images by query"
  ([query page]
     (->> (http/get SERVICE_ENDPOINT
                    {:query-params
                     (assoc SEARCH_MAP
                       "method" "flickr.photos.search"
                       "text" query
                       "start" page)})
          flickr-page-processor))
  ([page]
     (->> (http/get SERVICE_ENDPOINT
                    {:query-params
                     (assoc SEARCH_MAP
                       "method" "flickr.photos.getRecent"
                       "start" page)})
          flickr-page-processor)))

;;; CACHE ;;;

(def ^:private query-cache (atom {}))
(def ^:private random-cache (cache/create-cache
                             #(get-images-for-page %)
                             :skip 0
                             :pagesize 1))

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
  ([query]
     (let [cache (get @query-cache query)]
       (if cache
         (cache/retrieve cache)
         ;; not found
         (let [cache (cache/create-cache
                      #(get-images-for-page query %)
                      :skip 0
                      :pagesize 1)]
           (swap! query-cache assoc query cache)
           (cache/retrieve cache))))))

(defn- unify
  "Convert flickr image properties to unified image format.

Unified image format consist of following properties:
[:id :source :width :height :link :title
 :preview-link :preview-height :preview-width] 
"
  [{:keys [id
           url_z
           height_z
           width_z
           url_t
           width_t
           height_t
           title] :as flickr-image}]
  (when flickr-image
    {:id id :source :flickr
     :link url_z
     :preview-link url_t
     :width (u/safe-parse-int width_z)
     :preview-width (u/safe-parse-int  width_t)
     :height (u/safe-parse-int height_z)
     :preview-height (u/safe-parse-int height_t)
     :title title}
    ))

(defn get-image
  ([] (unify (get-raw-image)))
  ([query] (unify (get-raw-image query))))

;; TODO quota
