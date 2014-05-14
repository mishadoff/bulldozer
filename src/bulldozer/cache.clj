(ns bulldozer.cache)

;; Simple stupid cache implementation based on 
;; clojure.lang.PersistentQueue

(defn create-cache [fun & {:keys [skip pagesize]
                           :or {skip nil
                                pagesize nil}}]
  (atom
   {:cache (clojure.lang.PersistentQueue/EMPTY)
    :fun fun
    :skip skip
    :pagesize pagesize}))

(defn- retrieve-from [cache]
  (let [q (:cache @cache) 
        popped (pop q)]
    (reset! cache {:cache popped
                   :fun (:fun @cache)
                   :skip (:skip @cache)
                   :pagesize (:pagesize @cache)})
    (first q)))

(defn- safe-add [a b]
  (if (or (nil? a) (nil? b)) nil (+ a b)))

(defn- fill-sync [cache]
  "Fill cache synchronously"
  (let [fun (:fun @cache)
        page (:skip @cache)
        data (if page (fun page) (fun))
        conjed (if (empty? data)
                 (:cache @cache)
                 (apply conj (:cache @cache) data))]
    (reset! cache {:cache conjed
                   :fun fun
                   :skip (safe-add (:skip @cache)
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
