(ns bulldozer.api.flickr
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]))

;; https://www.flickr.com/services/api/misc.urls.html

(def ^:dynamic *APP_KEY*
  "29e9c67966b744e0d06e1961e1bb2dd9")

(def ^:private SERVICE_ENDPOINT
  "https://api.flickr.com/services/rest/")

(def ^:private EXTRAS
  "o_dims,url_t,url_o")

(defn- popstring
  "Flickr return jsonFlickrApi([json])
we need just [json]"
  [s]
  (subs s 14 (dec (count s))))

(defn- get-images-for-page
  [query page]
  "return one page of flickr images by query"
  (->> (http/get SERVICE_ENDPOINT
                 {:query-params
                  {"method" "flickr.photos.search"
                   "name" "value"
                   "format" "json"
                   "api_key" *APP_KEY*
                   "text" query
                   "start" page
                   "extras" EXTRAS
                   }})
       :body
       popstring
       (#(json/read-str % :key-fn keyword))
       :photos
       :photo))

;;; CACHE ;;;

(def ^:private query-cache (atom {}))

(defn invalidate-cache
  ([] (reset! query-cache {}))
  ([query] (swap! query-cache dissoc query)))

;;;;;;;;;;;;

(defn- get-raw-image
  "Obtain one random image by specified keyword.
Return results according to service."
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
           url_o
           height_o
           width_o
           url_t
           width_t
           height_t
           title] :as flickr-image}]
  (when flickr-image
    {:id id :source :flickr
     :link url_o
     :preview-link url_t
     :width (Integer/parseInt width_o)
     :preview-width (Integer/parseInt width_t)
     :height (Integer/parseInt height_o)
     :preview-height (Integer/parseInt height_t)
     :title title}))

(defn get-image [query]
  (unify (get-raw-image query)))