(ns bulldozer.cache)

;; Simple stupid cache implementation based on 
;; clojure.lang.PersistentQueue

(defn create-cache [fun]
  (atom
   {:cache (clojure.lang.PersistentQueue/EMPTY)
    :fun fun}))

(defn- retrieve-from [cache]
  (let [q (:cache @cache) 
        popped (pop q)]
    (reset! cache {:cache popped :fun (:fun @cache)})
    (first q)))

(defn- fill-sync [cache]
  "Fill cache synchronously"
  (let [fun (:fun @cache) data (fun)
        conjed (apply conj (:cache @cache) data)]
    (reset! cache {:cache conjed :fun fun})))

(defn retrieve [cache]
  (let [q (:cache @cache)]
    (cond
     (empty? q) 
     (do
       (fill-sync cache)
       (retrieve-from cache))
     :else
     (retrieve-from cache))))
