(ns bulldozer.api.instagram
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]))

(def ^:private *CLIENT_ID*
  "5271fc9c9fc24d9c9eb370d652ec04a4")

(defn- recent-endpoint [tag]
  (format "https://api.instagram.com/v1/tags/%s/media/recent" tag))

(defn- get-images-for-tag [tag url]
  (let [real-url (if url url (recent-endpoint tag))]
    (->> (http/get real-url
                   {:query-params {"client_id" *CLIENT_ID*}})
         :body
         (#(json/read-str % :key-fn keyword))
         (#(vec [(:data %)
                 (get-in % [:pagination :next_url])]
                ))
         )))

;; TODO describe tags


(def ^:private query-cache (atom {}))

(defn invalidate-cache
  ([] (reset! query-cache {}))
  ([query] (swap! query-cache dissoc query)))

(defn get-raw-image
  "Obtain one random image by specified keyword.
Return results according to service."
  ([tag]
     (let [cache (get @query-cache tag)]
       (if cache
         (cache/retrieve cache)
         ;; not found
         (let [cache (cache/create-cache
                      #(get-images-for-tag tag %)
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

(defn get-image [tag]
  "Return one unified image"
  (unify (get-raw-image tag)))