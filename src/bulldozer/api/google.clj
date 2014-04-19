(ns bulldozer.api.google
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]))

;; 64 Images per query

(def IMAGES_API "http://ajax.googleapis.com/ajax/services/search/images?v=1.0")

(defn get-images-for-page
  [query & {:keys [page] :or {page 0}}]
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
  "Obtain one random image or random image with
specified keyword.

Simple cache is used. First time you perform request
it can take some time, because google returns multiple images
per request. Result will be cached and perform instant
response until cache is exhausted. Keep in mynd 64 is a maximum for api request.
"
  ([query]
     (let [cache (get @query-cache query)]
       (if cache
         (cache/retrieve cache)
         ;; not found
         (let [cache (cache/create-cache
                      #(get-images-for-page query)
                      :initpage 0
                      :pagesize 4)]
           (swap! query-cache assoc query cache)
           (cache/retrieve cache))))))
