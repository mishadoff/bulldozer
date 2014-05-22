(ns bulldozer.gist
  (:require [clj-http.client :as http])
  (:require [clojure.data.json :as json]))

;; TODO document limits
;; Authenticated user 5000 per hour
;; Other 60

(def GIST_PUBLIC "https://api.github.com/gists/public?page=")

;; TODO authentication
;; TODO expose other parts of gist for duplicate detection
(defn get-snippets [page]
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

;; TODO no cache yet
(defn get-code []
  (rand-nth (get-snippets (inc (rand-int 100)))))
