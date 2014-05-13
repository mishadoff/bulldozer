# bulldozer

Gather data.

## Available sources

- [Imgur](http://imgur.com/)
- [Google Images](https://www.google.com.ua/imghp)

*In development* 

- [Gist](http://gist.github.com/) (In development)

## Images

Every image provider has `(get-image)` or `(get-image "keyword")` or both functions.
It returns unified image object with the following properties:

* **:id** "abcdefjhijklomnp"
* **:link** "http://img.com/tarantino.jpg"
* **:width** 600
* **:height** 400
* **:title** "Quentin Tarantino"
* **:content** "Director works on his new film"
* **:preview-link** "http://img.com/tarantino_small.jpg"
* **:preview-width** 90
* **:preview-height** 60
* **:source** :google

Also, there are provider specific properties for image,
which you can inspect by using `(get-raw-image)` or `(get-raw-image "keyword")` functions.

Some provider implements `(quota)` function, which allows to inspect remaining api usage.

Almost all image providers return multiple result per request, so results are cached
internally and refreshed when images are exhausted. Such cache can be invalidated by calling
`(invalidate-cache)` or `(invalidate-cache "keyword")` function.

### Imgur

`(:use [bulldozer.api.imgur])`

* `(get-image)` - random unified image.
* `(get-image "cat")` - random unified image with cat
* `(get-raw-image)` - random imgur-specific image, properties [here](http://api.imgur.com/models/image#model)
* `(get-raw-image "cat")` - random imgur-specific image
* `(link-scale "http://i.imgur.com/abcde.jpg" :s)`
  Scale imgur image to specific size. Following sizes supported:
  * **:s** (small square 90x90)
  * **:b** (big square 160x160)
  * **:t** (small thumbnail 160x160)
  * **:m** (medium thumbnail 320x320)
  * **:l** (large thumbnail 640x640)
  * **:h** (huge thumbnail 1024x1024)
* `(quota)` - api usage info
  ``` clojure
  (:UserRemaining (quota)) => 491
  (:ClientRemaining (quota)) => 12491
  ```
* `(invalidate-cache)` - clear all pictures saved to random cache
* `(invalidate-cache "cat")` - clear all pictures saved to "cat"-cache 


Imgur provides `12500` requests for client per day, what is
approximately `50K` images.

Note, that bulldozer internal `CLIENT_ID` is shared and
just for test purposes, so if you want to have `12500`
limit for private usage, register imgur application,
obtain your own `CLIENT_ID` and override it in requests:

``` clojure
(binding [*CLIENT_ID* "my-own-client-id"]
  (get-image "cat"))
```

### Google Images

Google Images API is actually deprecated, but still works.
It is able to return upto 64 results per query.

`(:use [bulldozer.api.google])`

* `(get-image "cat")` - random unified image with cat
* `(get-raw-image "cat")` - random google-specific image, properties [here](https://developers.google.com/image-search/v1/devguide#resultobject)
* `(invalidate-cache)` - clear all pictures saved to all keyword caches
* `(invalidate-cache "cat")` - clear all pictures saved to "cat"-cache 

Google provides upto `64` results per query.

Full list of image properties [here](https://developers.google.com/image-search/v1/devguide#resultobject)

## License

Copyright © 2014

Distributed under the Eclipse Public License 1.0
