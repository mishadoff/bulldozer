(ns bulldozer.images.core
  (:require [bulldozer.cache :as cache]))



(defn get-raw-image
  "Retrieve next raw image from cache.
  If cache is empty it will be autofilled.

  options specific to service
  
  Optional: :tag - only images related to tag will be retrieved"
  [cache options & {:keys [tag]}]
  (let [cache-atom (get cache (or tag :notag))]
    (if cache-atom
      ;; cache already created
      (cache/retrieve cache-atom)
      ;; first time cache get
      (let [cache-atom (cache/create-cache options)]
        
        )
      )

    )

  
  )


(defprotocol ImageService
  (unify
    [this image] image
    "Accept image specific to some service
     and return unified images")
  
  (response-processor
    [this response] (empty [])
    "Process response returned by a service to collection of images")
  
  (get-raw-image
    [this]
    
    [this tag]
    "Retrieve one image from service provider in service-specific
    format. If tag is not provided return random image.")
  
  (get-image
    [this] (unify this (get-raw-image))
    [this tag] (unify this (get-raw-image tag))
    "Retrieve one image from service provider in unified format.
     If tag is not provided return random image.")

  (quota [this]
    "Retrieve remaining quota")

  

  )



