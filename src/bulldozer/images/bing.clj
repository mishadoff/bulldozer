(ns bulldozer.images.bing
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]
            [bulldozer.utils :as u]))

(def ^:private IMAGES_ENDPOINT
  "https://api.datamarket.azure.com/Bing/Search/Image")
(def ^:private QUOTA_ENDPOINT
  "https://api.datamarket.azure.com/Services/My/Datasets?$format=json")

(def ^:dynamic *ACCOUNT_KEY*
  "21dm2MDccB3Zwq1OWwgGhd0Ej2/kSRz2SVDT3reRu3I=")

(defn- get-images-for-page [query skip]
  (->> (http/get IMAGES_ENDPOINT
                 {:basic-auth [*ACCOUNT_KEY* *ACCOUNT_KEY*]
                  :query-params {"Query" (format "'%s'" query)
                                 "$format" "json"
                                 "$skip" skip}})
       :body
       (#(json/read-str % :key-fn keyword))
       :d
       :results))

(defn- quota []
  (->> (http/get QUOTA_ENDPOINT
                 {:basic-auth [*ACCOUNT_KEY* *ACCOUNT_KEY*]})
       :body
       (#(json/read-str % :key-fn keyword))
       :d
       :results
       (first)
       :ResourceBalance))

(def ^:private query-cache (atom {}))

(defn invalidate-cache
  ([] (reset! query-cache {}))
  ([query] (swap! query-cache dissoc query)))

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
                      :pagesize 50)]
           (swap! query-cache assoc query cache)
           (cache/retrieve cache))))))

(defn- unify
  "Convert bing image properties to unified image format.

Unified image format consist of following properties:
[:id :source :width :height :link :title
 :preview-link :preview-height :preview-width] 
"
  [{:keys [ID
           Width
           Height
           MediaUrl
           Thumbnail
           Title] :as bing-image}]
  (when bing-image
    {:id ID :source :bing
     :link MediaUrl :preview-link (:MediaUrl Thumbnail) 
     :width (Integer/parseInt Width)
     :preview-width (Integer/parseInt (:Width Thumbnail))
     :height (Integer/parseInt Height)
     :preview-height (Integer/parseInt (:Height Thumbnail))
     :title Title}))

(defn get-image [query]
  "Return one unified image"
  (unify (get-raw-image query)))
