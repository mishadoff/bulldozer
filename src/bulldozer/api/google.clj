(ns bulldozer.api.google
  (:require [clj-http.client :as http]
            [clojure.data.json :as json]
            [bulldozer.cache :as cache]))

(def IMAGES_API "http://ajax.googleapis.com/ajax/services/search/images?v=1.0&q=clojure")

#_(defn get-images [page]
  "return list of pairs [language raw_url] from gist page"
  (->> (str GIST_PUBLIC page)
       (http/get)
       (:body)
       (json/read-str)
       (map #(get % "files"))
       (mapcat (fn [m]
                 (map (fn [[k v]]
                        [(get v "language")
                         (get v "raw_url")]) m)))
       (remove #(nil? (first %))) ;; we drop not detected snippets
       (map (fn [[lang link]] [lang (slurp link)]))
       )) 


