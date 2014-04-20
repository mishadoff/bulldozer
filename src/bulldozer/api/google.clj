(ns bulldozer.api.google
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]))

;; 64 Images per query

(def IMAGES_API "http://ajax.googleapis.com/ajax/services/search/images?v=1.0")

(defn get-images-for-page
  [query page]
  "return list of pairs [language raw_url] from gist page"
  (->> (http/get IMAGES_API {:query-params {"q" query
                                            "start" page}})
       (:body)
       (#(json/read-str % :key-fn keyword))
       (:responseData)
       (:results)))

;;;; CACHE

(def query-cache (atom {}))

(defn get-image
  "Obtain one random by specified keyword.

Simple cache is used. First time you perform request
it can take some time, because google returns multiple images
per request. Result will be cached and perform instant
response until cache is exhausted.

API is actually deprecated, but works.
Keep in mind that 64 is a maximum number of images
per request query."
  ([query]
     (let [cache (get @query-cache query)]
       (if cache
         (cache/retrieve cache)
         ;; not found
         (let [cache (cache/create-cache
                      #(get-images-for-page query %)
                      :initpage 0
                      :pagesize 4)]
           (swap! query-cache assoc query cache)
           (cache/retrieve cache))))))

;; TODO think about structure
(defn unify
  "Convert google image properties to unified image format.

Unified image format consist of following properties:
[:id :source :width :height :link :title :content
     :preview-link :preview-height :preview-width] 
"
  [{:keys [imageId
           width
           height
           url
           tbWidth
           tbHeight
           tbUrl
           contentNoFormatting
           titleNoFormatting] :as google-image}]
  {:id imageId :source :google
   :link url :preview-link tbUrl 
   :width width :preview-width tbWidth
   :height height :preview-height tbHeight
   :title titleNoFormatting :content contentNoFormatting})
