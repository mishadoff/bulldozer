(ns bulldozer.images.instagram-new
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]
            [bulldozer.images.core :refer :all]
            [bulldozer.images.keys :refer [*instagram-client-id*]]))

;; Endpoints

(def ^:private popular-endpoint "https://api.instagram.com/v1/media/popular")
(def ^:private tag-endpoint "https://api.instagram.com/v1/tags/search")
(def ^:private search-endpoint "https://api.instagram.com/v1/tags/%s/media/recent")



(defrecord InstagramImageService []
  ImageService
  
  (unify [_ image]
    image
    )
  
  
  (quota [_]
    (->> (http/get tag-endpoint
                   {:query-params {"q" "snow" ;;some fake query
                                   "client_id" *instagram-client-id*}})
         :headers
         (#(get % "x-ratelimit-remaining"))
         (Integer/parseInt))
    
    )

  
  )

