(ns bulldozer.api.twitter
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]))

(def CLIENT_ID "fDvWapiu2jBvl4mq43U3Mg")
(def AUTH_HEADER {:headers {"Authorization" 
                            (str "Client-ID " CLIENT_ID)}})

(def SEARCH_ENDPOINT "https://api.twitter.com/1.1/search/tweets.json?")

;; Not as simple need additional encoding

(defn- get-tweets [query]
  (->> (http/get SEARCH_ENDPOINT ;(assoc AUTH_HEADER
                                  { :query-params {"q" query}})
       :body
       (#(json/read-str % :key-fn keyword))))
