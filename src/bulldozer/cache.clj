(ns bulldozer.cache)

;; Simple stupid cache implementation based on 
;; clojure.lang.PersistentQueue

(defn create-cache [fun & {:keys [skip pagesize next-url url]
                           :or {skip nil
                                pagesize nil
                                next-url nil
                                url nil}}]
  (atom
   {:cache (clojure.lang.PersistentQueue/EMPTY)
    :fun fun
    :skip skip
    :pagesize pagesize
    :next-url next-url
    :url url}))

(defn- retrieve-from [cache]
  (let [q (:cache @cache) 
        popped (pop q)]
    (reset! cache {:cache popped
                   :fun (:fun @cache)
                   :skip (:skip @cache)
                   :pagesize (:pagesize @cache)
                   :next-url (:next-url @cache)
                   :url (:url @cache)})
    (first q)))

(defn- safe-add [a b]
  (if (or (nil? a) (nil? b)) nil (+ a b)))

(defn- fill-sync [cache]
  "Fill cache synchronously"
  (let [fun (:fun @cache)
        page (:skip @cache)
        next-url (:next-url @cache)
        raw-data (cond page (fun page)
                       next-url (fun (:url @cache))
                       :else (fun))
        data (cond next-url (first raw-data)
                   :else raw-data)
        url (cond next-url (second raw-data)
                  :else nil)
        conjed (if (empty? data)
                 (:cache @cache)
                 (apply conj (:cache @cache) data))]
    (reset! cache {:cache conjed
                   :fun fun
                   :skip (safe-add (:skip @cache)
                                   (:pagesize @cache))
                   :pagesize (:pagesize @cache)
                   :next-url next-url
                   :url url})))

(defn retrieve [cache]
  (let [q (:cache @cache)]
    (cond
     (empty? q) 
     (do
       (fill-sync cache)
       (retrieve-from cache))
     :else
     (retrieve-from cache))))
