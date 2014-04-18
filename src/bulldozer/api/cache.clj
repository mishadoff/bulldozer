(ns bulldozer.cache)

;; Synchronized cache implementation based on 
;; clojure.lang.PersistentQueue

(defn create-cache []
  {:cache (ref (clojure.lang.PersistentQueue/EMPTY))
   :agent (agent 0)
   :min 5})

(defn- retrieve-from [cache]
  (dosync
   (let [e (first @cache)]
     (alter cache pop) e)))

(defn- fill-sync [cache stream]
  (dosync
   (alter cache #(apply conj % (stream)))))

(defn- fill-async [cache agent stream]
  

(defn retrieve [cache-map stream]
  (let [cache (:cache cache-map)]
    (cond
     (empty? @cache) 
     (do
       (fill-sync cache stream)
       (retrieve-from cache))
     (< (count @cache) (:min cache-map))
     (do
       (fill-async cache (future (stream)))
       (retrieve-from cache))
     :else
     (retrieve-from cache))))
