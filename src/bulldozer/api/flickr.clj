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
  "url_o,url_t")

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

(defn get-raw-image
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
     :width (u/safe-parse-int width_o)
     :preview-width (u/safe-parse-int  width_t)
     :height (u/safe-parse-int height_o)
     :preview-height (u/safe-parse-int height_t)
     :title title}
    ))

(defn- get-raw-image-continuos
  "Continuosly obtain image.
If image does not contain original url, repeat reques"
  [query]
  (let [ri (get-raw-image query)]
    (if (:url_o ri) ri (get-raw-image-continuos query))))

(defn get-image [query]
  (unify (get-raw-image-continuos query)))


;; TODO quota
;; TODO recent
;; TODO originals
