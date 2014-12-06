(ns bulldozer.cache)

;; Simple cache implementation based on 
;; clojure.lang.PersistentQueue
;;
;; (make-cache & :options)
;; will create a new atom contains other cache atoms and options
;; TODO what  about atom contains an atom?
;; 
;; (get-from-cache cache & :tag)
;; retrieve item or tagged item

(defn make-cache
  "Create new cache. Cache autofills if requested element from empty cache.
   If after fill 

  :options - map describe how cache will be populated"
  [options]
  (atom
   {:options options
    :data {}}))

(defn- make-cache-atom
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

(defn- pop-from-cache-atom
  "Pop element from cache atom. Atom updated."
  [cache-atom]
  (let [queue (:data @cache-atom)
        e     (first queue)]
    ;; pop element from cache item
    (swap! cache-atom (fn [m] (update-in m [:data] pop)))
    e))


(defn- fill-sync [cache-atom]
  "Fill cache atom synchronously"
  (let [fun (get-in @cache-atom [:options :fun])
        cache-update (fun) ;; call function ;; probably make a function depends on functions
        new-items (:data cache-update) ;; destructuring
        new-options (:options cache-update)]
    (swap! cache-atom
           (fn [m]
             (-> m 
                 (update-in [:data] (fn [v] (into v new-items)))
                 (update-in [:options] merge new-options)
                 )))))

(defn- pop-or-fill-cache-atom
  "Return item from cache atom
  If cache atom is empty fills the cache according to its options"
  [cache-atom]
  (let [queue (:data @cache-atom)]
    (cond
     (empty? queue) 
     (do
       (fill-sync cache-atom)
       (pop-from-cache-atom cache-atom))
     :else
     (pop-from-cache-atom cache-atom))))

(defn get-cache
  "Retrieve item from cache.
  Cache retrieve correspond cache-atom and then
  lookup for an item in that cache, if cache atom is
  not available, it creates one.
  
  Optional: :tag - only items related to tag will be retrieved"
  [cache & {:keys [tag]}]
  (let [internal-tag (or tag :notag)
        cache-atom (get-in @cache [:data internal-tag])]
    (if cache-atom
      ;; cache already created
      (pop-or-fill-cache-atom cache-atom)
      ;; first time cache get
      (let [cache-atom (make-cache-atom (:options @cache))]
        ;; add cache-atom to meta
        (swap! cache (fn [m] (assoc-in m [:data internal-tag] cache-atom)))
        (pop-or-fill-cache-atom cache-atom)))))
