(ns bulldozer.api.google
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]))

(def ^:private IMAGES_API
  "http://ajax.googleapis.com/ajax/services/search/images?v=1.0")

(defn- get-images-for-page
  [query page]
  "return one page of google images by query"
  (->> (http/get IMAGES_API {:query-params {"q" query
                                            "start" page}})
       :body
       (#(json/read-str % :key-fn keyword))
       :responseData
       :results))

(def ^:private query-cache (atom {}))

(defn invalidate-cache
  ([] (reset! query-cache {}))
  ([query] (swap! query-cache dissoc query)))

(defn get-raw-image
  "Obtain one random image by specified keyword.
Return results according to service.
"
  ([query]
     (let [cache (get @query-cache query)]
       (if cache
         (cache/retrieve cache)
         ;; not found
         (let [cache (cache/create-cache
                      #(get-images-for-page query %)
                      :skip 0
                      :pagesize 4)]
           (swap! query-cache assoc query cache)
           (cache/retrieve cache))))))

(defn- unify
  "Convert google image properties to unified image format.

Unified image format consist of following properties:
[:id :source :width :height :link :title
 :preview-link :preview-height :preview-width] 
"
  [{:keys [imageId
           width
           height
           unescapedUrl
           tbWidth
           tbHeight
           tbUrl
           titleNoFormatting] :as google-image}]
  (when google-image
    {:id imageId :source :google
     :link unescapedUrl
     :preview-link tbUrl 
     :width (Integer/parseInt width)
     :preview-width (Integer/parseInt tbWidth)
     :height (Integer/parseInt height)
     :preview-height (Integer/parseInt tbHeight)
     :title titleNoFormatting}))

(defn get-image [query]
  "Return one unified image"
  (unify (get-raw-image query)))
