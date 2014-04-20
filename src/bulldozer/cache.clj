(ns bulldozer.cache)

;; Simple stupid cache implementation based on 
;; clojure.lang.PersistentQueue

(defn create-cache [fun & {:keys [initpage pagesize]
                           :or {initpage nil
                                pagesize nil}}]
  (atom
   {:cache (clojure.lang.PersistentQueue/EMPTY)
    :fun fun
    :initpage initpage
    :pagesize pagesize}))

(defn- retrieve-from [cache]
  (let [q (:cache @cache) 
        popped (pop q)]
    (reset! cache {:cache popped
                   :fun (:fun @cache)
                   :initpage (:initpage @cache)
                   :pagesize (:pagesize @cache)})
    (first q)))

(defn- fill-sync [cache]
  "Fill cache synchronously"
  (let [fun (:fun @cache)
        page (:initpage @cache)
        data (if page (fun page) (fun))
        conjed (if (empty? data)
                 (:cache @cache)
                 (apply conj (:cache @cache) data))]
    (reset! cache {:cache conjed
                   :fun fun
                   :initpage (+ (:initpage @cache)
                                (:pagesize @cache))
                   :pagesize (:pagesize @cache)})))

(defn retrieve [cache]
  (let [q (:cache @cache)]
    (cond
     (empty? q) 
     (do
       (fill-sync cache)
       (retrieve-from cache))
     :else
     (retrieve-from cache))))
