(ns bulldozer.cache)

;; Simple stupid cache implementation based on 
;; clojure.lang.PersistentQueue
;;
;; General cache structure is a map

(defn make-cache
  []
  "Create autofill cache structure

   :options - map describe how cache will be populated"
  {:options nil})

(defn make-cache-atom
  "Create atom with following properties
  :cache - queue represents cached data
  :options - options defined strategy how data must be retrieved/populated

      :function - function retrieves map {:data [...]
                                          :options {...}}
                  :data will be conjoined to original cache atom
                  :options are merged to cache atom
  "
  [options]
  (atom {:data    (clojure.lang.PersistentQueue/EMPTY)
         :options options}))

(defn- retrieve-from-cache-atom [cache-atom]
  (let [queue (:data @cache-atom)
        e     (pop queue)]
    ;; pop element from cache item
    (swap! cache-atom (fn [m] (update-in m [:data] pop)))
    e))

(defn get-item
  "Retrieve item from cache.
  If cache is empty it will be autofilled.

  Optional: :tag - only images related to tag will be retrieved"
  [cache & {:keys [tag]}]
  (let [cache-atom (get cache (or tag :notag))]
    (if cache-atom
      ;; cache already created
      (retrieve cache-atom)
      ;; first time cache get
      (let [cache-atom (make-cache-atom (:options cache))]
        ;; add cache-atom to meta
        
        )
      )

    )

  
  )

(defn- fill-sync [cache-atom]
  "Fill cache synchronously"
  (let [fun (get-in @cache-atom [:options :fun])
        cache-update (fun) ;; call function ;; probably make a function depends on functions
        new-items (:data cache-update) ;; destructuring
        new-options (:option cache-update)]
    (swap! cache-atom
           (fn [m]
             (-> m 
                 (update-in [:data] (fn [v] (into v new-items)))
                 (update-in [:options] merge new-options)
                 )))))

(defn retrieve
  "Return item from cache item
  If cacne item is empty fills the cache according to its options"
  [cache-atom]
  (let [queue (:data @cache-atom)]
    (cond
     (empty? queue) 
     (do
       (fill-sync cache-atom)
       (retrieve-from-cache-atom cache-atom))
     :else
     (retrieve-from-cache-atom cache-atom))))
